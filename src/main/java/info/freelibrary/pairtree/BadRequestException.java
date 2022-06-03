package info.freelibrary.pairtree;

/**
 * An exception thrown if access is not allowed.
 */
public class BadRequestException extends PairtreeException {

    /**
     * The exception's <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 6327182730904408171L;

    /**
     * Creates a new exception from the supplied message code.
     *
     * @param aMessageCode A message code
     */
    public BadRequestException(final String aMessageCode) {
        super(aMessageCode);
    }

    /**
     * Creates a new exception from the supplied message code and additional details.
     *
     * @param aMessageCode A message code
     * @param aDetailsArray An array of additional details
     */
    public BadRequestException(final String aMessageCode, final Object... aDetailsArray) {
        super(aMessageCode, aDetailsArray);
    }

    /**
     * Creates a new exception from the supplied parent exception and message code.
     *
     * @param aCause A parent exception
     * @param aMessageCode A message code
     */
    public BadRequestException(final Throwable aCause, final String aMessageCode) {
        super(aCause, aMessageCode);
    }

    /**
     * Creates a new exception from the supplied parent exception, message code, and additional details.
     *
     * @param aCause A parent exception
     * @param aMessageCode A message code
     * @param aDetailsArray An array of additional details
     */
    public BadRequestException(final Throwable aCause, final String aMessageCode,
            final Object... aDetailsArray) {
        super(aCause, aMessageCode, aDetailsArray);
    }

}
