
package info.freelibrary.pairtree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests of the PairtreeRuntimeException.
 */
public class PairtreeRuntimeExceptionTest {

    private static final String MESSAGE = "A Pairtree path may not be null";

    private static final String DETAILED_MESSAGE = "Response code: 403 [Not found]";

    private static final String CODE = "403";

    private static final String REASON = "Not found";

    private static final Exception EXC = new Exception();

    /**
     * Tests the PairtreeRuntimeException constructor with a single string argument.
     */
    @Test
    public final void testPairtreeRuntimeExceptionString() {
        assertEquals(MESSAGE, new PairtreeRuntimeException(MessageCodes.PT_003).getMessage());
    }

    /**
     * Tests the PairtreeRuntimeException constructor with a string and object array arguments.
     */
    @Test
    public final void testPairtreeRuntimeExceptionStringObjectArray() {
        assertEquals(DETAILED_MESSAGE, new PairtreeRuntimeException(MessageCodes.PT_018, CODE, REASON).getMessage());
    }

    /**
     * Tests the PairtreeRuntimeException constructor with a string and exception arguments
     */
    @Test
    public final void testPairtreeRuntimeExceptionStringException() {
        assertEquals(MESSAGE, new PairtreeRuntimeException(EXC, MessageCodes.PT_003).getMessage());
    }

    /**
     * Tests the PairtreeRuntimeException constructor with a string array argument.
     */
    @Test
    public final void testPairtreeRuntimeExceptionStringExceptionStringArray() {
        assertEquals(DETAILED_MESSAGE,
            new PairtreeRuntimeException(EXC, MessageCodes.PT_018, CODE, REASON).getMessage());
    }

}
