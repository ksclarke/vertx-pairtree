
package info.freelibrary.pairtree.fs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import info.freelibrary.pairtree.Constants;
import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.pairtree.PairtreeUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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
    private final String myPrefix;

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

        if (myPrefix == null) {
            myID = aID;
        } else {
            myID = PairtreeUtils.removePrefix(myPrefix, aID);
        }
    }

    @Override
    public void exists(final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final Future<Boolean> future = Future.<Boolean>future().setHandler(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_021, this);

        myFileSystem.exists(getPath(), result -> {
            if (result.succeeded()) {
                future.complete(result.result());
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void create(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_023, this);

        myFileSystem.mkdirs(getPath(), result -> {
            if (result.succeeded()) {
                future.complete();
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void delete(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_022, this);

        myFileSystem.deleteRecursive(getPath(), true, result -> {
            if (result.succeeded()) {
                future.complete();
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public String getID() {
        return myPrefix == null ? myID : Paths.get(myPrefix, myID).toString();
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
    public String getPath(final String aResourcePath) {
        return Paths.get(getPath(), aResourcePath).toString();
    }

    @Override
    public void put(final String aPath, final Buffer aBuffer, final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final Path resourcePath = Paths.get(getPath(), aPath);
        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_026, resourcePath);

        // First, create the parent directory path if it doesn't already exist
        myFileSystem.mkdirs(resourcePath.getParent().toString(), mkdirsResult -> {
            if (mkdirsResult.succeeded()) {
                // Then, write the Pairtree object resource into that directory
                myFileSystem.writeFile(resourcePath.toString(), aBuffer, writeResult -> {
                    if (writeResult.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(writeResult.cause());
                    }
                });
            } else {
                future.fail(mkdirsResult.cause());
            }
        });
    }

    @Override
    public void get(final String aPath, final Handler<AsyncResult<Buffer>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final String resourcePath = Paths.get(getPath(), aPath).toString();
        final Future<Buffer> future = Future.<Buffer>future().setHandler(aHandler);

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
    public void find(final String aPath, final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final String resourcePath = Paths.get(getPath(), aPath).toString();
        final Future<Boolean> future = Future.<Boolean>future().setHandler(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_025, resourcePath);

        myFileSystem.exists(resourcePath, result -> {
            if (result.succeeded()) {
                future.complete(result.result());
            } else {
                future.fail(result.cause());
            }
        });
    }

}
