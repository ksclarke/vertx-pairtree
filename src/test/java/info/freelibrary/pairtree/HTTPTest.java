
package info.freelibrary.pairtree;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * Tests of the HTTP constants class.
 */
public class HTTPTest {

    /**
     * Tests the HTTP constants class.
     *
     * @throws NoSuchMethodException If the test tries to access a method that doesn't exist
     * @throws IllegalAccessException If the test tries to access the constants class illegally
     * @throws InvocationTargetException If there is trouble invocating the constants class
     * @throws InstantiationException If there is trouble instantiating the constants class
     */
    @Test
    public final void test() throws Exception {
        final Constructor<HTTP> constructor = HTTP.class.getDeclaredConstructor();

        // Confirm this constructor is private because it's a constants class
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        constructor.newInstance();
    }

}
