
package info.freelibrary.pairtree;

import java.util.List;
import java.util.Optional;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * A public interface for Pairtree root implementations.
 */
public interface Pairtree {

    /** Version of the Pairtree specification */
    String VERSION_NUM = "0.1";

    /** Default Pairtree prefix file name */
    String PREFIX = "pairtree_prefix";

    /** Default Pairtree root directory name */
    String ROOT = "pairtree_root";

    /** Default Pairtree version file name */
    String VERSION = "pairtree_version";

    /** Default character set for the Pairtree */
    String DEFAULT_CHARSET = "UTF-8";

    /** Default Pairtree name */
    String DEFAULT_PAIRTREE = "pairtree";

    /**
     * Gets the Pairtree prefix.
     *
     * @return The Pairtree prefix
     */
    Optional<String> getPrefix();

    /**
     * Returns whether the Pairtree is using a prefix.
     *
     * @return True if the Pairtree uses a prefix; else, false
     */
    boolean hasPrefix();

    /**
     * Gets the name of the Pairtree prefix file.
     *
     * @return The name of the Pairtree prefix file
     */
    String getPrefixFileName();

    /**
     * Gets the path of the Pairtree prefix file.
     *
     * @return The path of the Pairtree prefix file or null if no prefix is set
     */
    String getPrefixFilePath();

    /**
     * Gets the name of the Pairtree version file.
     *
     * @return The name of the Pairtree version file
     */
    String getVersionFileName();

    /**
     * Gets the path of the Pairtree version file.
     *
     * @return The path of the Pairtree version file
     */
    String getVersionFilePath();

    /**
     * Gets the Pairtree object identified by the supplied ID.
     *
     * @param aID An object name
     * @return The Pairtree object
     */
    PairtreeObject getObject(String aID);

    /**
     * Gets the Pairtree objects identified by the supplied ID.
     *
     * @param aIDList An object name
     * @return The Pairtree objects for the supplied IDs
     */
    List<PairtreeObject> getObjects(List<String> aIDList);

    /**
     * Tests whether the Pairtree root exists.
     *
     * @return A future with a boolean result or an exception
     */
    Future<Boolean> exists();

    /**
     * Tests whether the Pairtree root exists.
     *
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    void exists(Handler<AsyncResult<Boolean>> aHandler);

    /**
     * Creates a Pairtree root file system. It will succeed quietly if the root file system already exists.
     *
     * @return A future that either succeeded or that contains an exception
     */
    Future<Void> create();

    /**
     * Creates the Pairtree root file system. It will succeed quietly if the root file system already exists.
     *
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    void create(Handler<AsyncResult<Void>> aHandler);

    /**
     * Deletes the Pairtree.
     *
     * @return A future that either succeeded or that contains an exception
     */
    Future<Void> delete();

    /**
     * Deletes the Pairtree.
     *
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    void delete(Handler<AsyncResult<Void>> aHandler);

    /**
     * Returns the implementation specific path of the Pairtree.
     *
     * @return The implementation specific path of the Pairtree
     */
    String getPath();

}
