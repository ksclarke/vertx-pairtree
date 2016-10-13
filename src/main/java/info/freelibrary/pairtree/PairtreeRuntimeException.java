
package info.freelibrary.pairtree;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;

import info.freelibrary.util.I18nRuntimeException;

/**
 * A generic Pairtree related runtime exception.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class PairtreeRuntimeException extends I18nRuntimeException {

    /**
     * The <code>serialVersionUID</code> for <code>PairtreeException</code>.
     */
    private static final long serialVersionUID = -3816513744343100056L;

    /**
     * Creates a generic Pairtree runtime exception.
     *
     * @param aMessage
     */
    public PairtreeRuntimeException(final String aMessage) {
        super(BUNDLE_NAME, aMessage);
    }

    /**
     * Creates a generic Pairtree runtime exception that takes an exception message and additional details to be input
     * into the {}s in the message.
     *
     * @param aMessage An exception message
     * @param aDetailsVarargs Additional details to insert into the message
     */
    public PairtreeRuntimeException(final String aMessage, final Object... aDetailsVarargs) {
        super(BUNDLE_NAME, aMessage, aDetailsVarargs);
    }

    /**
     * Creates a generic Pairtree runtime exception that takes an exception message and a related exception.
     *
     * @param aMessage An exception message
     * @param aCause An upstream exception
     */
    public PairtreeRuntimeException(final String aMessage, final Exception aCause) {
        super(BUNDLE_NAME, aMessage, aCause);
    }

    /**
     * Creates a generic Pairtree runtime exception that takes an exception message, a related exception, and
     * additional details to be input into the {}s in the message.
     *
     * @param aMessage An exception message
     * @param aCause An upstream exception
     * @param aDetailsVarargs Additional details to insert into the message
     */
    public PairtreeRuntimeException(final String aMessage, final Exception aCause, final String... aDetailsVarargs) {
        super(BUNDLE_NAME, aMessage, aDetailsVarargs, aCause);
    }
}
