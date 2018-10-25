
package info.freelibrary.pairtree.fs;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests for <code>FsPairtree</code>.
 */
@RunWith(VertxUnitRunner.class)
public class FsPairtreeTest extends AbstractFsPairtreeTest {

    @Test
    public void testNullHandlerExists(final TestContext aContext) {
        try {
            myPairtree.exists(null);
            aContext.fail(getI18n(MessageCodes.PT_DEBUG_044));
        } catch (final NullPointerException details) {
            // Expected
        }
    }

    @Test
    public void testNullHandlerCreate(final TestContext aContext) {
        try {
            myPairtree.create(null);
            aContext.fail(getI18n(MessageCodes.PT_DEBUG_044));
        } catch (final NullPointerException details) {
            // Expected
        }
    }

    @Test
    public void testNullHandlerDelete(final TestContext aContext) {
        try {
            myPairtree.delete(null);
            aContext.fail(getI18n(MessageCodes.PT_DEBUG_044));
        } catch (final NullPointerException details) {
            // Expected
        }
    }

    @Test
    public void testPairtreeDeletion(final TestContext aContext) {
        final String versionFilePath = myPairtree.getVersionFileName();
        final Async async = aContext.async();

        // First, setup Pairtree to delete
        myPairtree.create(createResult -> {
            if (createResult.succeeded()) {
                final boolean result = myFileSystem.existsBlocking(myPairtree.toString());
                aContext.assertEquals(true, result, getI18n(MessageCodes.PT_DEBUG_031, myPairtree));

                // Then, test ability to delete
                myPairtree.delete(deleteResult -> {
                    if (!deleteResult.succeeded()) {
                        aContext.fail(getI18n(MessageCodes.PT_DEBUG_011, myPairtree));
                    } else {
                        if (myFileSystem.existsBlocking(myPairtree.toString())) {
                            aContext.fail(getI18n(MessageCodes.PT_DEBUG_015, myPairtree));
                        } else if (myFileSystem.existsBlocking(versionFilePath)) {
                            aContext.fail(getI18n(MessageCodes.PT_DEBUG_016, versionFilePath));
                        }
                    }

                    async.complete();
                });
            } else {
                aContext.fail(getI18n(MessageCodes.PT_DEBUG_015, myPairtree));
                async.complete();
            }
        });
    }

    @Test
    public void testPairtreeCreationConflict(final TestContext aContext) throws IOException {
        final Async async = aContext.async();

        // Create conflicting file in place of Pairtree root
        createFile(myPairtree.toString());

        myPairtree.create(result -> {
            if (result.succeeded()) {
                aContext.fail(getI18n(MessageCodes.PT_DEBUG_014, myPairtree));
            }

            async.complete();
        });
    }

    @Test
    public void testPairtreeCreation(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(result -> {
            if (result.succeeded()) {
                final boolean exists = myFileSystem.existsBlocking(myPairtree.toString());
                final String message = getI18n(MessageCodes.PT_DEBUG_010, myPairtree);

                aContext.assertEquals(true, exists, message);
            } else {
                aContext.fail(getI18n(MessageCodes.PT_DEBUG_029, myPairtree));
            }

            async.complete();
        });
    }

    @Test
    public void testPairtreeExistsNot(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.exists(result -> {
            if (result.succeeded()) {
                if (result.result()) {
                    aContext.fail(getI18n(MessageCodes.PT_DEBUG_012, myPairtree));
                }
            } else {
                aContext.fail(getI18n(MessageCodes.PT_DEBUG_030, myPairtree));
            }

            async.complete();
        });
    }

    @Test
    public void testPairtreeExists(final TestContext aContext) {
        final Async async = aContext.async();

        // Do a little test setup first
        myPairtree.create(createResult -> {
            if (createResult.succeeded()) {
                // Then run the Pairtree exists test
                myPairtree.exists(existsResult -> {
                    if (existsResult.succeeded()) {
                        if (!existsResult.result()) {
                            aContext.fail(getI18n(MessageCodes.PT_DEBUG_013, myPairtree));
                        }
                    } else {
                        aContext.fail(getI18n(MessageCodes.PT_DEBUG_030, myPairtree));
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createResult.cause().getMessage());
                async.complete();
            }
        });
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(FsPairtreeTest.class, BUNDLE_NAME);
    }
}
