package info.freelibrary.pairtree;

/**
 * An exception thrown if a Pairtree object cannot be found.
 */
public class ObjectNotFoundException extends PairtreeException {

    /**
     * The exception's <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 6427172430904408170L;

    /**
     * Creates a new exception from the supplied message code.
     *
     * @param aMessageCode A message code
     */
    public ObjectNotFoundException(final String aMessageCode) {
        super(aMessageCode);
    }

    /**
     * Creates a new exception from the supplied message code and additional details.
     *
     * @param aMessageCode A message code
     * @param aDetailsArray An array of additional details
     */
    public ObjectNotFoundException(final String aMessageCode, final Object... aDetailsArray) {
        super(aMessageCode, aDetailsArray);
    }

    /**
     * Creates a new exception from the supplied parent exception and message code.
     *
     * @param aCause A parent exception
     * @param aMessageCode A message code
     */
    public ObjectNotFoundException(final Throwable aCause, final String aMessageCode) {
        super(aCause, aMessageCode);
    }

    /**
     * Creates a new exception from the supplied parent exception, message code, and additional details.
     *
     * @param aCause A parent exception
     * @param aMessageCode A message code
     * @param aDetailsArray An array of additional details
     */
    public ObjectNotFoundException(final Throwable aCause, final String aMessageCode,
            final Object... aDetailsArray) {
        super(aCause, aMessageCode, aDetailsArray);
    }

}
