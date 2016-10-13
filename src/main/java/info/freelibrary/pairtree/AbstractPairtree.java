
package info.freelibrary.pairtree;

import info.freelibrary.util.I18nObject;
import info.freelibrary.util.Logger;

/**
 * A base pairtree class which can be extended by specific implementations.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public abstract class AbstractPairtree extends I18nObject implements PairtreeRoot {

    /** The logger used by the Pairtree implementation */
    protected final Logger LOGGER = getLogger();

    /** The Pairtree's prefix (optional) */
    protected String myPrefix;

    /**
     * Creates an abstract pairtree object.
     */
    protected AbstractPairtree() {
        super(Constants.BUNDLE_NAME);
    }

    @Override
    public String getPrefix() {
        return myPrefix;
    }

    @Override
    public boolean hasPrefix() {
        return myPrefix != null && myPrefix.length() > 0;
    }

    @Override
    public String getVersionFileName() {
        return PAIRTREE_VERSION + PT_VERSION_NUM.replace('.', '_');
    }

    @Override
    public String getPrefixFileName() {
        return PAIRTREE_PREFIX;
    }

    /**
     * Gets the logger of the class overriding this abstract class.
     *
     * @return The logger of the class override this abstract class
     */
    protected abstract Logger getLogger();
}
