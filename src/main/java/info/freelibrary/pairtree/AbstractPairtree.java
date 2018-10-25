
package info.freelibrary.pairtree;

import info.freelibrary.util.I18nObject;

/**
 * A base pairtree class which can be extended by specific implementations.
 */
public abstract class AbstractPairtree extends I18nObject implements PairtreeRoot {

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

}
