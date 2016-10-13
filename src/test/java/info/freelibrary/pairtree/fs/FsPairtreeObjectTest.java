
package info.freelibrary.pairtree.fs;

import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_011;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_019;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_020;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_024;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_028;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_030;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_037;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_038;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_042;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_044;

import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests for the <code>FsPairtreeObject</code>.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
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
                            aContext.fail(getI18n(PT_DEBUG_028, ptObj));
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
                            aContext.fail(getI18n(PT_DEBUG_019, ptObj));
                        }
                    } else {
                        aContext.fail(getI18n(PT_DEBUG_030, ptObj));
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
    public void testExists(final TestContext aContext) {
        final Async async = aContext.async();

        createTestPairtreeObject(PairtreeImpl.FileSystem, createResult -> {
            if (createResult.succeeded()) {
                final PairtreeObject ptObj = createResult.result();

                ptObj.exists(existsResult -> {
                    if (existsResult.succeeded()) {
                        if (!existsResult.result()) {
                            aContext.fail(getI18n(PT_DEBUG_020, ptObj));
                        }
                    } else {
                        aContext.fail(getI18n(PT_DEBUG_030, ptObj));
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createResult.cause());
                async.complete();
            }
        }, myPairtree.getPath(), TEST_OBJECT_NAME);
    }

    @Test
    public void testDelete(final TestContext aContext) {
        final Async async = aContext.async();

        createTestPairtreeObject(PairtreeImpl.FileSystem, createResult -> {
            if (createResult.succeeded()) {
                final PairtreeObject ptObj = createResult.result();

                ptObj.delete(deleteResult -> {
                    if (deleteResult.succeeded()) {
                        if (myFileSystem.existsBlocking(ptObj.getPath())) {
                            aContext.fail(getI18n(PT_DEBUG_024, ptObj));
                        }
                    } else {
                        aContext.fail(getI18n(PT_DEBUG_011, ptObj));
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createResult.cause());
                async.complete();
            }
        }, myPairtree.getPath(), TEST_OBJECT_NAME);
    }

    @Test
    public void testNullHandlerExists(final TestContext aContext) {
        final Async async = aContext.async();

        createTestPairtreeObject(PairtreeImpl.FileSystem, result -> {
            if (result.succeeded()) {
                try {
                    result.result().exists(null);
                    aContext.fail(PT_DEBUG_044);
                } catch (final NullPointerException details) {
                    // Expected
                }
            } else {
                aContext.fail(result.cause());
            }

            async.complete();
        }, myPairtree.getPath(), TEST_OBJECT_NAME);
    }

    @Test
    public void testNullHandlerDelete(final TestContext aContext) {
        final Async async = aContext.async();

        createTestPairtreeObject(PairtreeImpl.FileSystem, result -> {
            if (result.succeeded()) {
                try {
                    result.result().delete(null);
                    aContext.fail(PT_DEBUG_044);
                } catch (final NullPointerException details) {
                    // Expected
                }
            } else {
                aContext.fail(result.cause());
            }

            async.complete();
        }, myPairtree.getPath(), TEST_OBJECT_NAME);
    }

    @Test
    public void testNullHandlerCreate(final TestContext aContext) {
        final Async async = aContext.async();

        createTestPairtreeObject(PairtreeImpl.FileSystem, result -> {
            if (result.succeeded()) {
                try {
                    result.result().create(null);
                    aContext.fail(PT_DEBUG_044);
                } catch (final NullPointerException details) {
                    // Expected
                }
            } else {
                aContext.fail(result.cause());
            }

            async.complete();
        }, myPairtree.getPath(), TEST_OBJECT_NAME);
    }

    @Test
    public void testGet(final TestContext aContext) {
        final Async async = aContext.async();

        createTestPairtreeObject(PairtreeImpl.FileSystem, createResult -> {
            if (createResult.succeeded()) {
                final PairtreeObject ptObj = createResult.result();

                createFile(Paths.get(ptObj.getPath(), RESOURCE_PATH).toString(), RESOURCE_CONTENT);

                ptObj.get(RESOURCE_PATH, getResult -> {
                    if (getResult.succeeded()) {
                        if (!getResult.result().toString().equals(RESOURCE_CONTENT)) {
                            aContext.fail(getI18n(PT_DEBUG_042, RESOURCE_CONTENT));
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
        }, myPairtree.getPath(), TEST_OBJECT_NAME);
    }

    @Test
    public void testPut(final TestContext aContext) {
        final Async async = aContext.async();

        createTestPairtreeObject(PairtreeImpl.FileSystem, createResult -> {
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
                                aContext.fail(getI18n(PT_DEBUG_038, path));
                            }
                        } else {
                            aContext.fail(getI18n(PT_DEBUG_037, path));
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
        }, myPairtree.getPath(), TEST_OBJECT_NAME);
    }

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(FsPairtreeObjectTest.class);
    }
}
