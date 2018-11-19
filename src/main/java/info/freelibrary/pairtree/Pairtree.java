
package info.freelibrary.pairtree;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * A public interface for Pairtree root implementations.
 */
public interface Pairtree {

    /** Default Pairtree name */
    String DEFAULT_PAIRTREE = "pairtree";

    /** Version of the Pairtree specification */
    String PT_VERSION_NUM = "0.1";

    /** Default Pairtree prefix file name */
    String PAIRTREE_PREFIX = "pairtree_prefix";

    /** Default Pairtree root directory name */
    String PAIRTREE_ROOT = "pairtree_root";

    /** Default Pairtree version file name */
    String PAIRTREE_VERSION = "pairtree_version";

    /** Default character set for the Pairtree */
    String DEFAULT_CHARSET = "UTF-8";

    /**
     * Gets the Pairtree prefix.
     *
     * @return The Pairtree prefix
     */
    String getPrefix();

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
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    void exists(Handler<AsyncResult<Boolean>> aHandler);

    /**
     * Creates the Pairtree root file system.
     *
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    void create(Handler<AsyncResult<Void>> aHandler);

    /**
     * Creates the Pairtree root file system only if needed. This is a way to make sure it exists before using it.
     *
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    void createIfNeeded(Handler<AsyncResult<Void>> aHandler);

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
