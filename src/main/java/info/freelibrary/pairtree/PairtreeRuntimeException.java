
package info.freelibrary.pairtree;

import info.freelibrary.util.I18nRuntimeException;

/**
 * A generic Pairtree related runtime exception.
 */
public class PairtreeRuntimeException extends I18nRuntimeException {

    /**
     * The <code>serialVersionUID</code> for <code>PairtreeRuntimeException</code>.
     */
    private static final long serialVersionUID = -3816513744343100056L;

    /**
     * Creates a generic Pairtree runtime exception.
     *
     * @param aMessage The detailed message about the exception
     */
    public PairtreeRuntimeException(final String aMessage) {
        super(MessageCodes.BUNDLE, aMessage);
    }

    /**
     * Creates a generic Pairtree runtime exception that takes an exception message and additional details to be input
     * into the {}s in the message.
     *
     * @param aMessage An exception message
     * @param aMoreDetails Additional details to insert into the message
     */
    public PairtreeRuntimeException(final String aMessage, final Object... aMoreDetails) {
        super(MessageCodes.BUNDLE, aMessage, aMoreDetails);
    }

    /**
     * Creates a generic Pairtree runtime exception that takes an exception message and a related exception.
     *
     * @param aMessage An exception message
     * @param aCause An upstream exception
     */
    public PairtreeRuntimeException(final Exception aCause, final String aMessage) {
        super(aCause, MessageCodes.BUNDLE, aMessage);
    }

    /**
     * Creates a generic Pairtree runtime exception that takes an exception message, a related exception, and additional
     * details to be input into the {}s in the message.
     *
     * @param aMessage An exception message
     * @param aCause An upstream exception
     * @param aMoreDetails Additional details to insert into the message
     */
    public PairtreeRuntimeException(final Exception aCause, final String aMessage, final Object... aMoreDetails) {
        super(aCause, MessageCodes.BUNDLE, aMessage, aMoreDetails);
    }

}
