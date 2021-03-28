
package info.freelibrary.pairtree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests of <code>PairtreeException</code>.
 */
public class PairtreeExceptionTest {

    private static final String MESSAGE = "A Pairtree path may not be null";

    private static final String DETAILED_MESSAGE = "Response code: 403 [Not found]";

    private static final String CODE = "403";

    private static final String REASON = "Not found";

    private static final Exception EXC = new Exception();

    /**
     * Tests the PairtreeException string constructor.
     */
    @Test
    public final void testPairtreeExceptionString() {
        assertEquals(MESSAGE, new PairtreeException(MessageCodes.PT_003).getMessage());
    }

    /**
     * Tests the PairtreeException string and object array constructor.
     */
    @Test
    public final void testPairtreeExceptionStringObjectArray() {
        assertEquals(DETAILED_MESSAGE, new PairtreeException(MessageCodes.PT_018, CODE, REASON).getMessage());
    }

    /**
     * Tests the PairtreeException string exception constructor.
     */
    @Test
    public final void testPairtreeExceptionStringException() {
        assertEquals(MESSAGE, new PairtreeException(EXC, MessageCodes.PT_003).getMessage());
    }

    /**
     * Tests the PairtreeException String object array constructor.
     */
    @Test
    public final void testPairtreeExceptionStringExceptionStringArray() {
        assertEquals(DETAILED_MESSAGE, new PairtreeException(EXC, MessageCodes.PT_018, CODE, REASON).getMessage());
    }

}
