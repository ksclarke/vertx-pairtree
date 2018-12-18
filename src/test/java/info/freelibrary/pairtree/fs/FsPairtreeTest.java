
package info.freelibrary.pairtree.fs;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.PairtreeException;
import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.pairtree.PairtreeObject;
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
            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_044));
        } catch (final NullPointerException details) {
            // Expected
        }
    }

    @Test
    public void hasNoPrefix(final TestContext aContext) {
        aContext.assertFalse(myPairtree.hasPrefix());
    }

    @Test
    public void hasPrefix(final TestContext aContext) throws PairtreeException {
        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree("asdf", new File(myPairtree.getPath()));
        aContext.assertTrue(myPairtree.hasPrefix());
    }

    @Test
    public void hasNullPrefix(final TestContext aContext) throws PairtreeException {
        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(null, new File(myPairtree.getPath()));
        aContext.assertFalse(myPairtree.hasPrefix());
    }

    @Test
    public void hasEmptyPrefix(final TestContext aContext) throws PairtreeException {
        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree("", new File(myPairtree.getPath()));
        aContext.assertFalse(myPairtree.hasPrefix());
    }

    @Test
    public void testNullHandlerCreate(final TestContext aContext) {
        try {
            myPairtree.create(null);
            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_044));
        } catch (final NullPointerException details) {
            // Expected
        }
    }

    @Test
    public void testNullHandlerDelete(final TestContext aContext) {
        try {
            myPairtree.delete(null);
            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_044));
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
                aContext.assertEquals(true, result, LOGGER.getMessage(MessageCodes.PT_DEBUG_031, myPairtree));

                // Then, test ability to delete
                myPairtree.delete(deleteResult -> {
                    if (!deleteResult.succeeded()) {
                        aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_011, myPairtree));
                    } else {
                        if (myFileSystem.existsBlocking(myPairtree.toString())) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_015, myPairtree));
                        } else if (myFileSystem.existsBlocking(versionFilePath)) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_016, versionFilePath));
                        }
                    }

                    async.complete();
                });
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_015, myPairtree));
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
                aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_014, myPairtree));
            }

            async.complete();
        });
    }

    @Test
    public void testPairtreeCreationIfNeeded(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.createIfNeeded(result -> {
            if (result.succeeded()) {
                final boolean exists = myFileSystem.existsBlocking(myPairtree.toString());
                final String message = LOGGER.getMessage(MessageCodes.PT_DEBUG_010, myPairtree);

                aContext.assertEquals(true, exists, message);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_029, myPairtree));
            }

            async.complete();
        });
    }

    @Test
    public void testExistingPairtreeCreationIfNeeded(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(createHandler -> {
            if (createHandler.succeeded()) {
                myPairtree.createIfNeeded(createIfNeededHandler -> {
                    if (createIfNeededHandler.succeeded()) {
                        final boolean exists = myFileSystem.existsBlocking(myPairtree.toString());
                        final String message = LOGGER.getMessage(MessageCodes.PT_DEBUG_010, myPairtree);

                        aContext.assertEquals(true, exists, message);
                    } else {
                        aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_029, myPairtree));
                    }

                    async.complete();
                });
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_029, myPairtree));
                async.complete();
            }
        });
    }

    @Test
    public void testPairtreeCreation(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(result -> {
            if (result.succeeded()) {
                final boolean exists = myFileSystem.existsBlocking(myPairtree.toString());
                final String message = LOGGER.getMessage(MessageCodes.PT_DEBUG_010, myPairtree);

                aContext.assertEquals(true, exists, message);
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_029, myPairtree));
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
                    aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_012, myPairtree));
                }
            } else {
                aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_030, myPairtree));
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
                            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_013, myPairtree));
                        }
                    } else {
                        aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_030, myPairtree));
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createResult.cause().getMessage());
                async.complete();
            }
        });
    }

    @Test
    public void testGetObjects(final TestContext aContext) {
        final Async async = aContext.async();

        final List<String> ids = Arrays.asList(new String[] { UUID.randomUUID().toString(), UUID.randomUUID()
                .toString() });
        final List<PairtreeObject> list = myPairtree.getObjects(ids);

        aContext.assertEquals(2, list.size());

        async.complete();
    }

    @Test
    public void testSetPrefix(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();
        final String prefix = "asdf";

        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(prefix, new File(myPairtree.getPath()));
        aContext.assertEquals(Optional.of(prefix), myPairtree.getPrefix());

        myPairtree.create(handler -> {
            if (handler.succeeded()) {
                async.complete();
            } else {
                aContext.fail(handler.cause());
            }
        });
    }

    @Test
    public void testDeletePrefix(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();
        final String prefix = "asdf";

        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(prefix, new File(myPairtree.getPath()));
        aContext.assertEquals(Optional.of(prefix), myPairtree.getPrefix());

        myPairtree.create(createHandler -> {
            if (createHandler.succeeded()) {
                myPairtree.delete(deleteHandler -> {
                    if (deleteHandler.succeeded()) {
                        async.complete();
                    } else {
                        aContext.fail(deleteHandler.cause());
                    }
                });
            } else {
                aContext.fail(createHandler.cause());
            }
        });
    }

    @Test
    public void testCheckPrefix(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();
        final String prefix = "asdf";

        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(prefix, new File(myPairtree.getPath()));
        aContext.assertEquals(Optional.of(prefix), myPairtree.getPrefix());

        myPairtree.create(createHandler -> {
            if (createHandler.succeeded()) {
                myPairtree.exists(existsHandler -> {
                    if (existsHandler.succeeded()) {
                        if (existsHandler.result().booleanValue()) {
                            async.complete();
                        } else {
                            aContext.fail();
                        }
                    } else {
                        aContext.fail(existsHandler.cause());
                    }
                });
            } else {
                aContext.fail(createHandler.cause());
            }
        });
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(FsPairtreeTest.class, BUNDLE_NAME);
    }
}
