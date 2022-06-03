
package info.freelibrary.pairtree;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

/**
 * A public interface for Pairtree objects.
 */
public interface PairtreeObject {

    /**
     * Tests whether the Pairtree object exists.
     *
     * @return A {@link io.vertx.core.Future} that indicates whether the Pairtree object exists or not
     */
    Future<Boolean> exists();

    /**
     * Tests whether the Pairtree object exists.
     *
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    void exists(Handler<AsyncResult<Boolean>> aHandler);

    /**
     * Creates the Pairtree object.
     *
     * @return A {@link io.vertx.core.Future} with the success or failure of the creation
     */
    Future<Void> create();

    /**
     * Creates the Pairtree object. This fails if the location of the Pairtree object exists and cannot have things
     * written into it (e.g., if it is a file). It doesn't fail if the object already exists and can have things written
     * into it.
     *
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    void create(Handler<AsyncResult<Void>> aHandler);

    /**
     * Deletes the Pairtree object.
     *
     * @return A {@link io.vertx.core.Future} with the success or failure of the deletion
     */
    Future<Void> delete();

    /**
     * Deletes the Pairtree object.
     *
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    void delete(Handler<AsyncResult<Void>> aHandler);

    /**
     * Gets the object ID.
     *
     * @return The ID of the Pairtree object
     */
    String getID();

    /**
     * Gets the object path.
     *
     * @return the path of the Pairtree object
     */
    String getPath();

    /**
     * Gets the path of the requested Pairtree object.
     *
     * @param aPtPath The Pairtree object which the returned path should represent
     * @return The path of the requested object resource
     */
    String getPath(String aPtPath);

    /**
     * Puts a resources into the Pairtree object. The overwrites an existing resource at that path.
     *
     * @param aPtPath The path (relative to the Pairtree object) at which the Buffer should be written
     * @param aBuffer Content to be written into a resource in the Pairtree object
     * @return The result of the put
     */
    Future<Void> put(String aPtPath, Buffer aBuffer);

    /**
     * Puts a resource into the Pairtree object. This overwrites an existing resource at that path.
     *
     * @param aPtPath The path (relative to the Pairtree object) at which the Buffer should be written
     * @param aBuffer Content to be written into a resource in the Pairtree object
     * @param aHandler To handle the writing of the content into the Pairtree object
     */
    void put(String aPtPath, Buffer aBuffer, Handler<AsyncResult<Void>> aHandler);

    /**
     * Puts a resources into the Pairtree object. The overwrites an existing resource at that path.
     *
     * @param aPtPath The path (relative to the Pairtree object) at which the Buffer should be written
     * @param aFilePath The path to the file to be written into the Pairtree
     * @return The result of the put
     */
    Future<Void> put(String aPtPath, String aFilePath);

    /**
     * Puts a resource into the Pairtree object. This overwrites an existing resource at that path.
     *
     * @param aPtPath The path (relative to the Pairtree object) at which the Buffer should be written
     * @param aFilePath The path to the file to be written into the Pairtree
     * @param aHandler To handle the writing of the content into the Pairtree object
     */
    void put(String aPtPath, String aFilePath, Handler<AsyncResult<Void>> aHandler);

    /**
     * Gets a resource from the Pairtree object.
     *
     * @param aPtPath The path (relative to the Pairtree object) from which a Buffer can be retrieved
     * @return The result of the get request
     */
    Future<Buffer> get(String aPtPath);

    /**
     * Gets a resource from the Pairtree object.
     *
     * @param aPtPath The path (relative to the Pairtree object) from which a Buffer can be retrieved
     * @param aHandler To handle the reading of the content from the Pairtree object into a Buffer
     */
    void get(String aPtPath, Handler<AsyncResult<Buffer>> aHandler);

    /**
     * Checks whether a resource exists in the Pairtree object.
     *
     * @param aPtPath The path (relative to the Pairtree object) for a resource to be found
     * @return A future containing the boolean result of the find
     */
    Future<Boolean> find(String aPtPath);

    /**
     * Checks whether a resource exists in the Pairtree object.
     *
     * @param aPtPath The path (relative to the Pairtree object) for a resource to be found
     * @param aHandler To handle the finding of a resource in the Pairtree object
     */
    void find(String aPtPath, Handler<AsyncResult<Boolean>> aHandler);

}
