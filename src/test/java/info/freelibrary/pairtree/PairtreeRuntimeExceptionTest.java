
package info.freelibrary.pairtree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PairtreeRuntimeExceptionTest {

    private static final String MESSAGE = "A Pairtree path may not be null";

    private static final String DETAILED_MESSAGE = "Response code: 403 [Not found]";

    private static final String CODE = "403";

    private static final String REASON = "Not found";

    private static final Exception EXC = new Exception();

    @Test
    public final void testPairtreeRuntimeExceptionString() {
        assertEquals(MESSAGE, new PairtreeRuntimeException(MessageCodes.PT_003).getMessage());
    }

    @Test
    public final void testPairtreeRuntimeExceptionStringObjectArray() {
        assertEquals(DETAILED_MESSAGE, new PairtreeRuntimeException(MessageCodes.PT_018, CODE, REASON).getMessage());
    }

    @Test
    public final void testPairtreeRuntimeExceptionStringException() {
        assertEquals(MESSAGE, new PairtreeRuntimeException(EXC, MessageCodes.PT_003).getMessage());
    }

    @Test
    public final void testPairtreeRuntimeExceptionStringExceptionStringArray() {
        assertEquals(DETAILED_MESSAGE, new PairtreeRuntimeException(EXC, MessageCodes.PT_018, CODE, REASON).getMessage());
    }

}
