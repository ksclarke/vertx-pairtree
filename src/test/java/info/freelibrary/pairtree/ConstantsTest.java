
package info.freelibrary.pairtree;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * Tests of the project's constants.
 */
public class ConstantsTest {

    /**
     * Tests the project's constants.
     *
     * @throws NoSuchMethodException If the expected method could not be found
     * @throws IllegalAccessException If the constants were accessed illegally in the test
     * @throws InvocationTargetException If the constants class could not be invocated
     * @throws InstantiationException If the constants class could not be instantiated
     */
    @Test
    public final void test() throws Exception {
        final Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();

        // Confirm this constructor is private because it's a constants class
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        constructor.newInstance();
    }

}
