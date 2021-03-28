
package info.freelibrary.pairtree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests of the InvalidPathException.
 */
public class InvalidPathExceptionTest {

    private static final String MESSAGE = "A Pairtree path may not be null";

    private static final String DETAILED_MESSAGE = "Response code: 403 [Not found]";

    private static final String CODE = "403";

    private static final String REASON = "Not found";

    private static final Exception EXC = new Exception();

    /**
     * Tests the InvalidPathException string constructor.
     */
    @Test
    public final void testInvalidPathExceptionString() {
        assertEquals(MESSAGE, new InvalidPathException(MessageCodes.PT_003).getMessage());
    }

    /**
     * Tests the InvalidPathException string and object array constructor.
     */
    @Test
    public final void testInvalidPathExceptionStringObjectArray() {
        assertEquals(DETAILED_MESSAGE, new InvalidPathException(MessageCodes.PT_018, CODE, REASON).getMessage());
    }

    /**
     * Tests the InvalidPathException string and exception constructor.
     */
    @Test
    public final void testInvalidPathExceptionStringException() {
        assertEquals(MESSAGE, new InvalidPathException(EXC, MessageCodes.PT_003).getMessage());
    }

    /**
     * Tests the InvalidPathException string, exception, and details array constructor.
     */
    @Test
    public final void testInvalidPathExceptionStringExceptionStringArray() {
        assertEquals(DETAILED_MESSAGE, new InvalidPathException(EXC, MessageCodes.PT_018, CODE, REASON).getMessage());
    }

}
