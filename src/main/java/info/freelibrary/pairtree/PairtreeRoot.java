
package info.freelibrary.pairtree;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * A public interface for Pairtree root implementations.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public interface PairtreeRoot {

    public final String DEFAULT_PAIRTREE_NAME = "pairtree";

    public final String PT_VERSION_NUM = "0.1";

    public final String PAIRTREE_PREFIX = "pairtree_prefix";

    public final String PAIRTREE_ROOT = "pairtree_root";

    public final String PAIRTREE_VERSION = "pairtree_version";

    public final String DEFAULT_CHARSET = "UTF-8";

    /**
     * Gets the Pairtree prefix.
     *
     * @return The Pairtree prefix
     */
    public String getPrefix();

    /**
     * Returns whether the Pairtree is using a prefix.
     */
    public boolean hasPrefix();

    /**
     * Gets the name of the Pairtree prefix file.
     *
     * @return The name of the Pairtree prefix file
     */
    public String getPrefixFileName();

    /**
     * Gets the path of the Pairtree prefix file.
     *
     * @return The path of the Pairtree prefix file or null if no prefix is set
     */
    public String getPrefixFilePath();

    /**
     * Gets the name of the Pairtree version file.
     *
     * @return The name of the Pairtree version file
     */
    public String getVersionFileName();

    /**
     * Gets the path of the Pairtree version file.
     *
     * @return The path of the Pairtree version file
     */
    public String getVersionFilePath();

    /**
     * Gets the Pairtree object identified by the supplied ID.
     *
     * @param aID An object name
     * @return The Pairtree object
     */
    public PairtreeObject getObject(final String aID);

    /**
     * Gets the Pairtree objects identified by the supplied ID.
     *
     * @param aIDList An object name
     * @return The Pairtree objects for the supplied IDs
     */
    public List<PairtreeObject> getObjects(final List<String> aIDList);

    /**
     * Tests whether the Pairtree root exists.
     *
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    public void exists(final Handler<AsyncResult<Boolean>> aHandler);

    /**
     * Creates the Pairtree root file system.
     *
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    public void create(final Handler<AsyncResult<Void>> aHandler);

    /**
     * Deletes the Pairtree.
     *
     * @param aHandler A {@link io.vertx.core.Handler} with an {@link io.vertx.core.AsyncResult}
     */
    public void delete(final Handler<AsyncResult<Void>> aHandler);

    /**
     * Returns the implementation specific path of the Pairtree.
     */
    public String getPath();

}
