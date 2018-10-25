/**
 * Licensed under the GNU LGPL v.2.1 or later.
 */

package info.freelibrary.pairtree;

/**
 * An exception thrown for an invalid Pairtree path.
 */
public class InvalidPathException extends PairtreeException {

    /**
     * The <code>serialVersionUID</code> for <code>InvalidPpathException</code>.
     */
    private static final long serialVersionUID = -3816513744343123355L;

    /**
     * Creates an invalid Pairtree path exception.
     *
     * @param aMessage An exception message
     */
    public InvalidPathException(final String aMessage) {
        super(aMessage);
    }

    /**
     * Creates an invalid Pairtree path exception that takes an exception message and additional details to be input
     * into the {}s in the message.
     *
     * @param aMessage An exception message
     * @param aDetailsVarargs Additional details to insert into the message
     */
    public InvalidPathException(final String aMessage, final Object... aDetailsVarargs) {
        super(aMessage, aDetailsVarargs);
    }

    /**
     * Creates an invalid Pairtree path exception that takes an exception message and a related exception.
     *
     * @param aMessage An exception message
     * @param aCause An upstream exception
     */
    public InvalidPathException(final String aMessage, final Exception aCause) {
        super(aMessage, aCause);
    }

    /**
     * Creates an invalid Pairtree path exception that takes an exception message, a related exception, and additional
     * details to be input into the {}s in the message.
     *
     * @param aMessage An exception message
     * @param aCause An upstream exception
     * @param aDetailsVarargs Additional details to insert into the message
     */
    public InvalidPathException(final String aMessage, final Exception aCause, final String... aDetailsVarargs) {
        super(aMessage, aDetailsVarargs, aCause);
    }
}
