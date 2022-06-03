
package info.freelibrary.pairtree.fs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.pairtree.Constants;
import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.pairtree.PairtreeUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

/**
 * A file-system backed implementation of a Pairtree object.
 */
public class FsPairtreeObject implements PairtreeObject {

    /** The logger used with the file-system based Pairtree object */
    private static final Logger LOGGER = LoggerFactory.getLogger(FsPairtreeObject.class, Constants.BUNDLE_NAME);

    /** A connection to the Pairtree's file system */
    private final FileSystem myFileSystem;

    /** The path to the file-system based Pairtree */
    private final String myPairtreePath;

    /** The Pairtree's prefix (optional) */
    private final Optional<String> myPrefix;

    /** The ID of this Pairtree */
    private final String myID;

    /**
     * Creates a file system backed Pairtree object.
     *
     * @param aFileSystem A file system
     * @param aPairtree The object's Pairtree
     * @param aID The object's ID
     */
    public FsPairtreeObject(final FileSystem aFileSystem, final FsPairtree aPairtree, final String aID) {
        Objects.requireNonNull(aFileSystem);
        Objects.requireNonNull(aPairtree);
        Objects.requireNonNull(aID);

        myPairtreePath = aPairtree.toString();
        myPrefix = aPairtree.getPrefix();
        myFileSystem = aFileSystem;

        if (!myPrefix.isPresent()) {
            myID = aID;
        } else {
            myID = PairtreeUtils.removePrefix(myPrefix.get(), aID);
        }
    }

    @Override
    public void exists(final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final Promise<Boolean> promise = Promise.<Boolean>promise();

        promise.future().onComplete(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_021, this);

        myFileSystem.exists(getPath(), result -> {
            if (result.succeeded()) {
                promise.complete(result.result());
            } else {
                promise.fail(result.cause());
            }
        });
    }

    @Override
    public void create(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final Promise<Void> promise = Promise.<Void>promise();

        promise.future().onComplete(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_023, this);

        myFileSystem.mkdirs(getPath(), result -> {
            if (result.succeeded()) {
                promise.complete();
            } else {
                promise.fail(result.cause());
            }
        });
    }

    @Override
    public void delete(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final Promise<Void> promise = Promise.<Void>promise();

        promise.future().onComplete(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_022, this);

        myFileSystem.deleteRecursive(getPath(), true, result -> {
            if (result.succeeded()) {
                promise.complete();
            } else {
                promise.fail(result.cause());
            }
        });
    }

    @Override
    public String getID() {
        return !myPrefix.isPresent() ? myID : Paths.get(myPrefix.get(), myID).toString();
    }

    @Override
    public String toString() {
        return getPath();
    }

    @Override
    public String getPath() {
        return Paths.get(myPairtreePath, PairtreeUtils.mapToPtPath(myID), PairtreeUtils.encodeID(myID)).toString();
    }

    @Override
    public String getPath(final String aPtPath) {
        return Paths.get(getPath(), aPtPath).toString();
    }

    @Override
    public void put(final String aPtPath, final Buffer aBuffer, final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final Path resourcePath = Paths.get(getPath(), aPtPath);
        final Promise<Void> promise = Promise.<Void>promise();

        promise.future().onComplete(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_026, resourcePath);

        // First, create the parent directory path if it doesn't already exist
        myFileSystem.mkdirs(resourcePath.getParent().toString(), mkdirsResult -> {
            if (mkdirsResult.succeeded()) {
                // Then, write the Pairtree object resource into that directory
                myFileSystem.writeFile(resourcePath.toString(), aBuffer, writeResult -> {
                    if (writeResult.succeeded()) {
                        promise.complete();
                    } else {
                        promise.fail(writeResult.cause());
                    }
                });
            } else {
                promise.fail(mkdirsResult.cause());
            }
        });
    }

    @Override
    public void put(final String aPtPath, final String aFilePath, final Handler<AsyncResult<Void>> aHandler) {
        final Path resourcePath = Paths.get(getPath(), aPtPath);
        final Promise<Void> promise = Promise.promise();

        LOGGER.debug(MessageCodes.PT_DEBUG_026, resourcePath);

        promise.future().onComplete(aHandler);

        // First, create the parent directory path if it doesn't already exist
        myFileSystem.mkdirs(resourcePath.getParent().toString(), mkdirsResult -> {
            if (mkdirsResult.succeeded()) {
                // Then, write the Pairtree object resource into that directory
                myFileSystem.copy(aFilePath, resourcePath.toString()).onSuccess(copyResult -> {
                    promise.complete();
                }).onFailure(error -> promise.fail(error));
            } else {
                promise.fail(mkdirsResult.cause());
            }
        });
    }

    @Override
    public void get(final String aPtPath, final Handler<AsyncResult<Buffer>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final String resourcePath = Paths.get(getPath(), aPtPath).toString();
        final Promise<Buffer> future = Promise.<Buffer>promise();

        future.future().onComplete(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_027, resourcePath);

        myFileSystem.readFile(resourcePath, result -> {
            if (result.succeeded()) {
                future.complete(result.result());
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void find(final String aPtPath, final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final String resourcePath = Paths.get(getPath(), aPtPath).toString();
        final Promise<Boolean> promise = Promise.<Boolean>promise();

        promise.future().onComplete(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_025, resourcePath);

        myFileSystem.exists(resourcePath, result -> {
            if (result.succeeded()) {
                promise.complete(result.result());
            } else {
                promise.fail(result.cause());
            }
        });
    }

}
