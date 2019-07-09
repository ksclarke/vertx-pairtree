
package info.freelibrary.pairtree;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class ConstantsTest {

    @Test
    public final void test() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        final Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();

        // Confirm this constructor is private because it's a constants class
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        constructor.newInstance();
    }

}
