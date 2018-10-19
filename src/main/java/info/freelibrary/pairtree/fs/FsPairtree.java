
package info.freelibrary.pairtree.fs;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import info.freelibrary.pairtree.AbstractPairtree;
import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.PairtreeException;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

/**
 * A file-system backed Pairtree implementation.
 */
public class FsPairtree extends AbstractPairtree {

    /** A logger for the file-system based Pairtree implementation */
    private static final Logger LOGGER = LoggerFactory.getLogger(FsPairtree.class, BUNDLE_NAME);

    /** The underlying file system */
    private final FileSystem myFileSystem;

    /** File system location of Pairtree root */
    private final String myPath;

    /**
     * Creates a file system backed Pairtree in the supplied directory.
     *
     * @param aVertx A VertX object
     * @param aDirPath The directory in which to put the Pairtree
     */
    public FsPairtree(final Vertx aVertx, final String aDirPath) {
        this(null, aVertx, aDirPath);
    }

    /**
     * Creates a file system backed Pairtree, using the supplied Pairtree prefix, in the supplied directory.
     *
     * @param aVertx A VertX object
     * @param aDirPath The directory in which to put the Pairtree
     * @param aPairtreePrefix The Pairtree's prefix
     */
    public FsPairtree(final String aPairtreePrefix, final Vertx aVertx, final String aDirPath) {
        Objects.requireNonNull(aVertx);
        Objects.requireNonNull(aDirPath);

        myPath = Paths.get(aDirPath, PAIRTREE_ROOT).toString();
        myFileSystem = aVertx.fileSystem();

        if (aPairtreePrefix == null) {
            LOGGER.debug(MessageCodes.PT_DEBUG_001, aDirPath);
        } else {
            myPrefix = aPairtreePrefix;
            LOGGER.debug(MessageCodes.PT_DEBUG_002, aDirPath, aPairtreePrefix);
        }
    }

    @Override
    public PairtreeObject getObject(final String aID) {
        return new FsPairtreeObject(myFileSystem, this, aID);
    }

    @Override
    public List<PairtreeObject> getObjects(final List<String> aIDList) {
        final List<PairtreeObject> ptObjList = new ArrayList<>();
        final Iterator<String> iterator = aIDList.iterator();

        while (iterator.hasNext()) {
            final String id = iterator.next();

            Objects.requireNonNull(StringUtils.trimToNull(id));
            ptObjList.add(new FsPairtreeObject(myFileSystem, this, id));
        }

        return ptObjList;
    }

    @Override
    public void exists(final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010, getClass().getSimpleName(), ".exists()"));

        final Future<Boolean> future = Future.<Boolean>future().setHandler(aHandler);

        myFileSystem.exists(myPath, result -> {
            if (result.succeeded()) {
                if (result.result()) {
                    checkVersion(future);
                } else {
                    future.complete(result.result());
                }
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void create(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010, getClass().getSimpleName(), ".create()"));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_004, myPath);

        myFileSystem.mkdirs(myPath, result -> {
            if (result.succeeded()) {
                setVersion(future);
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void delete(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010, getClass().getSimpleName(), ".delete()"));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_003, myPath);

        myFileSystem.deleteRecursive(myPath, true, result -> {
            if (result.succeeded()) {
                deleteVersion(future);
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public String getPrefixFilePath() {
        final String parent = Paths.get(myPath).getParent().toString();
        return Paths.get(parent, getPrefixFileName()).toString();
    }

    @Override
    public String getVersionFilePath() {
        final String parent = Paths.get(myPath).getParent().toString();
        return Paths.get(parent, getVersionFileName()).toString();
    }

    @Override
    public String toString() {
        return myPath;
    }

    @Override
    public String getPath() {
        return myPath;
    }

    /**
     * Checks that Pairtree version file exists.
     *
     * @param aFuture The result of an action that may, or may not, have occurred yet.
     */
    private void checkVersion(final Future<Boolean> aFuture) {
        final String versionFilePath = getVersionFilePath();

        myFileSystem.exists(versionFilePath, result -> {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(MessageCodes.PT_DEBUG_007, versionFilePath);
            }

            if (result.succeeded()) {
                if (result.result()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(MessageCodes.PT_DEBUG_008, versionFilePath);
                    }

                    checkPrefix(aFuture);
                } else {
                    aFuture.complete(!result.result());
                }
            } else {
                aFuture.fail(result.cause());
            }
        });
    }

    /**
     * Checks whether a Pairtree prefix file exists.
     *
     * @param aFuture The result of an action that may, or may not, have occurred yet.
     */
    private void checkPrefix(final Future<Boolean> aFuture) {
        final String prefixFilePath = getPrefixFilePath();

        myFileSystem.exists(prefixFilePath, result -> {
            if (result.succeeded()) {
                if (hasPrefix()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(MessageCodes.PT_DEBUG_035, prefixFilePath);
                    }

                    if (result.result()) {
                        aFuture.complete(result.result());
                    } else {
                        aFuture.fail(new PairtreeException(MessageCodes.PT_013, prefixFilePath));
                    }
                } else {
                    LOGGER.debug(MessageCodes.PT_DEBUG_009, prefixFilePath);

                    if (result.result()) {
                        aFuture.fail(new PairtreeException(MessageCodes.PT_014, prefixFilePath));
                    } else {
                        aFuture.complete(!result.result());
                    }
                }
            } else {
                aFuture.fail(result.cause());
            }
        });
    }

    /**
     * Deletes a Pairtree version file.
     *
     * @param aFuture The result of an action that may, or may not, have occurred yet.
     */
    private void deleteVersion(final Future<Void> aFuture) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.PT_DEBUG_006, myPath);
        }

        myFileSystem.delete(getVersionFilePath(), result -> {
            if (result.succeeded()) {
                if (myPrefix != null && myPrefix.length() > 0) {
                    deletePrefix(aFuture);
                } else {
                    aFuture.complete();
                }
            } else {
                aFuture.fail(result.cause());
            }
        });
    }

    /**
     * Deletes a Pairtree prefix file.
     *
     * @param aFuture The result of an action that may, or may not, have occurred yet.
     */
    private void deletePrefix(final Future<Void> aFuture) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.PT_DEBUG_034, myPath);
        }

        myFileSystem.delete(getPrefixFilePath(), result -> {
            if (result.succeeded()) {
                aFuture.complete();
            } else {
                aFuture.fail(result.cause());
            }
        });
    }

    /**
     * Creates a Pairtree version file.
     *
     * @param aFuture The result of an action that may, or may not, have occurred yet.
     */
    private void setVersion(final Future<Void> aFuture) {
        final StringBuilder specNote = new StringBuilder();
        final String ptVersion = getI18n(MessageCodes.PT_011, PT_VERSION_NUM);

        specNote.append(ptVersion).append(System.lineSeparator()).append(getI18n(MessageCodes.PT_012));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.PT_DEBUG_005, myPath);
        }

        myFileSystem.writeFile(getVersionFilePath(), Buffer.buffer(specNote.toString()), result -> {
            if (result.succeeded()) {
                if (myPrefix != null && myPrefix.length() > 0) {
                    setPrefix(aFuture);
                } else {
                    aFuture.complete();
                }
            } else {
                aFuture.fail(result.cause());
            }
        });
    }

    /**
     * Creates a Pairtree prefix file.
     *
     * @param aFuture The result of an action that may, or may not, have occurred yet.
     */
    private void setPrefix(final Future<Void> aFuture) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(MessageCodes.PT_DEBUG_033, myPath);
        }

        myFileSystem.writeFile(getPrefixFilePath(), Buffer.buffer(myPrefix), result -> {
            if (result.succeeded()) {
                aFuture.complete();
            } else {
                aFuture.fail(result.cause());
            }
        });
    }

}
