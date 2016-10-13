
package info.freelibrary.pairtree;

import info.freelibrary.util.I18nException;

/**
 * A generic Pairtree related exception.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class PairtreeException extends I18nException {

    /**
     * The <code>serialVersionUID</code> for <code>PairtreeException</code>.
     */
    private static final long serialVersionUID = -3816513744343123356L;

    /**
     * Creates a generic Pairtree exception.
     *
     * @param aMessage
     */
    public PairtreeException(final String aMessage) {
        super(Constants.BUNDLE_NAME, aMessage);
    }

    /**
     * Creates a generic Pairtree exception that takes an exception message and additional details to be input into
     * the {}s in the message.
     *
     * @param aMessage An exception message
     * @param aDetailsVarargs Additional details to insert into the message
     */
    public PairtreeException(final String aMessage, final Object... aDetailsVarargs) {
        super(Constants.BUNDLE_NAME, aMessage, aDetailsVarargs);
    }

    /**
     * Creates a generic Pairtree exception that takes an exception message and a related exception.
     *
     * @param aMessage An exception message
     * @param aCause An upstream exception
     */
    public PairtreeException(final String aMessage, final Exception aCause) {
        super(Constants.BUNDLE_NAME, aMessage, aCause);
    }

    /**
     * Creates a generic Pairtree exception that takes an exception message, a related exception, and additional
     * details to be input into the {}s in the message.
     *
     * @param aMessage An exception message
     * @param aCause An upstream exception
     * @param aDetailsVarargs Additional details to insert into the message
     */
    public PairtreeException(final String aMessage, final Exception aCause, final String... aDetailsVarargs) {
        super(Constants.BUNDLE_NAME, aMessage, aDetailsVarargs, aCause);
    }
}
