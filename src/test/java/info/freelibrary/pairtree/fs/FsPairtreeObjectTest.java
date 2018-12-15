
package info.freelibrary.pairtree.fs;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.pairtree.Constants;
import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.PairtreeException;
import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.pairtree.PairtreeUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests for the <code>FsPairtreeObject</code>.
 */
@RunWith(VertxUnitRunner.class)
public class FsPairtreeObjectTest extends AbstractFsPairtreeTest {

    /** The resource used in the tests */
    private static final String RESOURCE_PATH = "path/to/something";

    /** The contents of the resource used in the tests */
    private static final String RESOURCE_CONTENT = "something";

    @Test
    public void testCreate(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(createPtResult -> {
            if (createPtResult.succeeded()) {
                final PairtreeObject ptObj = myPairtree.getObject(TEST_OBJECT_NAME);

                ptObj.create(createPtObjResult -> {
                    if (createPtObjResult.succeeded()) {
                        if (!myFileSystem.existsBlocking(ptObj.getPath())) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_028, ptObj));
                        }
                    } else {
                        aContext.fail(createPtObjResult.cause());
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createPtResult.cause());
                async.complete();
            }
        });
    }

    @Test
    public void testGetID(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(createPtResult -> {
            if (createPtResult.succeeded()) {
                final PairtreeObject ptObj = myPairtree.getObject(TEST_OBJECT_NAME);

                ptObj.create(createPtObjResult -> {
                    if (createPtObjResult.succeeded()) {
                        aContext.assertEquals(TEST_OBJECT_NAME, ptObj.getID());
                    } else {
                        aContext.fail(createPtObjResult.cause());
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createPtResult.cause());
                async.complete();
            }
        });
    }

    @Test
    public void testFind(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(createPtResult -> {
            if (createPtResult.succeeded()) {
                final PairtreeObject ptObj = myPairtree.getObject(TEST_OBJECT_NAME);

                ptObj.create(createPtObjResult -> {
                    if (createPtObjResult.succeeded()) {
                        ptObj.find("found", findHandler -> {
                            if (findHandler.succeeded()) {
                                if (findHandler.result()) {
                                    aContext.fail("Found something that doesn't exist");
                                } else {
                                    async.complete();
                                }
                            } else {
                                aContext.fail(findHandler.cause());
                            }
                        });
                    } else {
                        aContext.fail(createPtObjResult.cause());
                    }
                });
            } else {
                aContext.fail(createPtResult.cause());
                async.complete();
            }
        });
    }

    @Test
    public void testGetPath(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();

        myPairtree.create(createPtResult -> {
            if (createPtResult.succeeded()) {
                final PairtreeObject ptObj = myPairtree.getObject(TEST_OBJECT_NAME);

                ptObj.create(createPtObjResult -> {
                    if (createPtObjResult.succeeded()) {
                        final String resourceName = "asdf";
                        final String objPath = PairtreeUtils.mapToPtPath(TEST_OBJECT_NAME);
                        final String found = ptObj.getPath(resourceName);
                        final String ptPath = myPairtree.getPath();
                        final String expected = Paths.get(ptPath, objPath, TEST_OBJECT_NAME, resourceName).toString();

                        aContext.assertEquals(expected, found);

                        async.complete();
                    } else {
                        aContext.fail(createPtObjResult.cause());
                    }
                });
            } else {
                aContext.fail(createPtResult.cause());
                async.complete();
            }
        });
    }

    @Test
    public void testGetPrefixedID(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();
        final String prefix = "asdf";

        // Use a prefixed Pairtree
        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(prefix, new File(myPairtree.getPath()));

        myPairtree.create(createPtResult -> {
            if (createPtResult.succeeded()) {
                final PairtreeObject ptObj = myPairtree.getObject(TEST_OBJECT_NAME);

                ptObj.create(createPtObjResult -> {
                    if (createPtObjResult.succeeded()) {
                        aContext.assertEquals(prefix + "/" + TEST_OBJECT_NAME, ptObj.getID());
                    } else {
                        aContext.fail(createPtObjResult.cause());
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createPtResult.cause());
                async.complete();
            }
        });
    }

    @Test
    public void testExistsNot(final TestContext aContext) {
        final Async async = aContext.async();

        // Do a little test setup first
        myPairtree.create(createResult -> {
            if (createResult.succeeded()) {
                final PairtreeObject ptObj = myPairtree.getObject(TEST_OBJECT_NAME);

                // Then run the Pairtree object exists (not) test
                ptObj.exists(existsResult -> {
                    if (existsResult.succeeded()) {
                        if (existsResult.result()) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_019, ptObj));
                        }
                    } else {
                        aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_030, ptObj));
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
    public void testExists(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();

        createTestFsPairtreeObject(createResult -> {
            if (createResult.succeeded()) {
                final PairtreeObject ptObj = createResult.result();

                ptObj.exists(existsResult -> {
                    if (existsResult.succeeded()) {
                        if (!existsResult.result()) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_020, ptObj));
                        }
                    } else {
                        aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_030, ptObj));
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createResult.cause());
                async.complete();
            }
        }, new File(myPairtree.getPath()), TEST_OBJECT_NAME);
    }

    @Test
    public void testDelete(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();

        createTestFsPairtreeObject(createResult -> {
            if (createResult.succeeded()) {
                final PairtreeObject ptObj = createResult.result();

                ptObj.delete(deleteResult -> {
                    if (deleteResult.succeeded()) {
                        if (myFileSystem.existsBlocking(ptObj.getPath())) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_024, ptObj));
                        }
                    } else {
                        aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_011, ptObj));
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createResult.cause());
                async.complete();
            }
        }, new File(myPairtree.getPath()), TEST_OBJECT_NAME);
    }

    @Test
    public void testNullHandlerExists(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();

        createTestFsPairtreeObject(result -> {
            if (result.succeeded()) {
                try {
                    result.result().exists(null);
                    aContext.fail(MessageCodes.PT_DEBUG_044);
                } catch (final NullPointerException details) {
                    // Expected
                }
            } else {
                aContext.fail(result.cause());
            }

            async.complete();
        }, new File(myPairtree.getPath()), TEST_OBJECT_NAME);
    }

    @Test
    public void testNullHandlerDelete(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();

        createTestFsPairtreeObject(result -> {
            if (result.succeeded()) {
                try {
                    result.result().delete(null);
                    aContext.fail(MessageCodes.PT_DEBUG_044);
                } catch (final NullPointerException details) {
                    // Expected
                }
            } else {
                aContext.fail(result.cause());
            }

            async.complete();
        }, new File(myPairtree.getPath()), TEST_OBJECT_NAME);
    }

    @Test
    public void testNullHandlerCreate(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();

        createTestFsPairtreeObject(result -> {
            if (result.succeeded()) {
                try {
                    result.result().create(null);
                    aContext.fail(MessageCodes.PT_DEBUG_044);
                } catch (final NullPointerException details) {
                    // Expected
                }
            } else {
                aContext.fail(result.cause());
            }

            async.complete();
        }, new File(myPairtree.getPath()), TEST_OBJECT_NAME);
    }

    @Test
    public void testGet(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();

        createTestFsPairtreeObject(createResult -> {
            if (createResult.succeeded()) {
                final PairtreeObject ptObj = createResult.result();

                createFile(Paths.get(ptObj.getPath(), RESOURCE_PATH).toString(), RESOURCE_CONTENT);

                ptObj.get(RESOURCE_PATH, getResult -> {
                    if (getResult.succeeded()) {
                        if (!getResult.result().toString().equals(RESOURCE_CONTENT)) {
                            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_042, RESOURCE_CONTENT));
                        }
                    } else {
                        aContext.fail(getResult.cause());
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createResult.cause());
                async.complete();
            }
        }, new File(myPairtree.getPath()), TEST_OBJECT_NAME);
    }

    @Test
    public void testPut(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();

        createTestFsPairtreeObject(createResult -> {
            if (createResult.succeeded()) {
                final PairtreeObject ptObj = createResult.result();
                final Buffer buffer = Buffer.buffer(RESOURCE_CONTENT);

                LOGGER.debug("Testing putting Pairtree object resource: {}", TEST_OBJECT_NAME);

                ptObj.put(RESOURCE_PATH, buffer, putResult -> {
                    final String path = Paths.get(ptObj.getPath(), RESOURCE_PATH).toString();

                    if (putResult.succeeded()) {
                        if (myFileSystem.existsBlocking(path)) {
                            final Buffer fileBuffer = myFileSystem.readFileBlocking(path);

                            if (!fileBuffer.toString().equals(RESOURCE_CONTENT)) {
                                aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_038, path));
                            }
                        } else {
                            aContext.fail(LOGGER.getMessage(MessageCodes.PT_DEBUG_037, path));
                        }
                    } else {
                        aContext.fail(putResult.cause());
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createResult.cause());
                async.complete();
            }
        }, new File(myPairtree.getPath()), TEST_OBJECT_NAME);
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(FsPairtreeObjectTest.class, Constants.BUNDLE_NAME);
    }
}
