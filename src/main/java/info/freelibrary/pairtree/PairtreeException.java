
package info.freelibrary.pairtree;

import java.io.FileNotFoundException;

import info.freelibrary.util.HTTP;
import info.freelibrary.util.I18nException;

import info.freelibrary.vertx.s3.UnexpectedStatusException;

/**
 * A generic Pairtree related exception.
 */
public class PairtreeException extends I18nException {

    /**
     * The <code>serialVersionUID</code> for <code>PairtreeException</code>.
     */
    private static final long serialVersionUID = -3816513744343123356L;

    /**
     * Creates a generic Pairtree exception.
     *
     * @param aMessage An exception message
     */
    public PairtreeException(final String aMessage) {
        super(MessageCodes.BUNDLE, aMessage);
    }

    /**
     * Creates a generic Pairtree exception that takes an exception message and additional details to be input into the
     * {}s in the message.
     *
     * @param aMessage An exception message
     * @param aDetailsArray Additional details to insert into the message
     */
    public PairtreeException(final String aMessage, final Object... aDetailsArray) {
        super(MessageCodes.BUNDLE, aMessage, aDetailsArray);
    }

    /**
     * Creates a generic Pairtree exception that takes an exception message and a related exception.
     *
     * @param aMessage An exception message
     * @param aCause An upstream exception
     */
    public PairtreeException(final Throwable aCause, final String aMessage) {
        super(aCause, MessageCodes.BUNDLE, aMessage);
    }

    /**
     * Creates a generic Pairtree exception that takes an exception message, a related exception, and additional details
     * to be input into the {}s in the message.
     *
     * @param aMessage An exception message
     * @param aCause An upstream exception
     * @param aDetailsVarargs Additional details to insert into the message
     */
    public PairtreeException(final Throwable aCause, final String aMessage, final Object... aDetailsVarargs) {
        super(aCause, MessageCodes.BUNDLE, aMessage, aDetailsVarargs);
    }

    /**
     * Creates a new exception from the supplied parent exception and message code.
     *
     * @param aThrowable A parent exception
     * @param aMessageCode A message code
     * @param aDetailsArray An array of additional details
     */
    public static final PairtreeException from(final Throwable aThrowable, final String aMessageCode,
            final String... aDetailsArray) {
        final PairtreeException exception;

        if (aThrowable instanceof UnexpectedStatusException) {
            switch (((UnexpectedStatusException) aThrowable).getStatusCode()) {
                case HTTP.NOT_FOUND:
                    exception = new ObjectNotFoundException(aThrowable, aMessageCode, (Object) aDetailsArray);
                    break;
                case HTTP.FORBIDDEN:
                    exception = new AccessDeniedException(aThrowable, aMessageCode, (Object) aDetailsArray);
                    break;
                case HTTP.BAD_REQUEST:
                    exception = new BadRequestException(aThrowable, aMessageCode, (Object) aDetailsArray);
                    break;
                case HTTP.SERVICE_UNAVAILABLE:
                    exception = new ObjectUnavailableException(aThrowable, aMessageCode, (Object) aDetailsArray);
                    break;
                default:
                    exception = new PairtreeException(aThrowable, aMessageCode, (Object) aDetailsArray);
                    break;
            }
        } else if (aThrowable instanceof FileNotFoundException) {
            exception = new ObjectNotFoundException(aThrowable, aMessageCode, (Object) aDetailsArray);
        } else if (aThrowable instanceof PairtreeException) {
            exception = (PairtreeException) aThrowable;
        } else {
            exception = new PairtreeException(aThrowable, aMessageCode, (Object) aDetailsArray);
        }

        return exception;
    }
}
