
package info.freelibrary.pairtree;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;

import info.freelibrary.util.I18nObject;
import info.freelibrary.util.Logger;

public abstract class AbstractPairtree extends I18nObject implements PairtreeRoot {

    protected final Logger LOGGER = getLogger();

    protected String myPrefix;

    protected AbstractPairtree() {
        super(BUNDLE_NAME);
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
