package info.freelibrary.pairtree;

/**
 * An exception thrown if a Pairtree object is unavailable.
 */
public class ObjectUnavailableException extends PairtreeException {

    /**
     * The exception's <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 5427182431114408170L;

    /**
     * Creates a new exception from the supplied message code.
     *
     * @param aMessageCode A message code
     */
    public ObjectUnavailableException(final String aMessageCode) {
        super(aMessageCode);
    }

    /**
     * Creates a new exception from the supplied message code and additional details.
     *
     * @param aMessageCode A message code
     * @param aDetailsArray An array of additional details
     */
    public ObjectUnavailableException(final String aMessageCode, final Object... aDetailsArray) {
        super(aMessageCode, aDetailsArray);
    }

    /**
     * Creates a new exception from the supplied parent exception and message code.
     *
     * @param aCause A parent exception
     * @param aMessageCode A message code
     */
    public ObjectUnavailableException(final Throwable aCause, final String aMessageCode) {
        super(aCause, aMessageCode);
    }

    /**
     * Creates a new exception from the supplied parent exception, message code, and additional details.
     *
     * @param aCause A parent exception
     * @param aMessageCode A message code
     * @param aDetailsArray An array of additional details
     */
    public ObjectUnavailableException(final Throwable aCause, final String aMessageCode,
            final Object... aDetailsArray) {
        super(aCause, aMessageCode, aDetailsArray);
    }

}
