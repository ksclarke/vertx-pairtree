
package info.freelibrary.pairtree;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

import info.freelibrary.util.Logger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * An abstract Pairtree test object.
 */
@RunWith(VertxUnitRunner.class)
public abstract class AbstractPairtreeTest {

    /** The name of a test object */
    protected static final String TEST_OBJECT_NAME = UUID.randomUUID().toString();

    /** The logger for the test */
    protected final Logger LOGGER = getLogger();

    /** The connection to the Vertx framework */
    protected Vertx myVertx;

    /**
     * Setup for the tests.
     *
     * @param aContext A test context
     */
    @Before
    public void setUp(final TestContext aContext) {
        myVertx = Vertx.vertx();
    }

    /**
     * Tear down for the tests.
     *
     * @param aContext A test context
     */
    @After
    public void tearDown(final TestContext aContext) {
        // Subclasses will do tear down
    }

    /**
     * Returns the logger for the Pairtree implementation.
     *
     * @return The logger for the Pairtree implementation
     */
    protected abstract Logger getLogger();

    /**
     * Does the work of creating a test Pairtree object in the file system so that tests against that object can be
     * run.
     *
     * @param aPairtreeImpl A Pairtree implementation
     * @param aHandler to handle the result of the creation event
     * @param aFile A file system directory Pairtree
     * @param aID A Pairtree object ID
     */
    protected void createTestFsPairtreeObject(final Handler<AsyncResult<PairtreeObject>> aHandler, final File aFile,
            final String aID) throws PairtreeException {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final Pairtree root = new PairtreeFactory(myVertx).getPairtree(aFile);
        final Future<PairtreeObject> future = Future.<PairtreeObject>future().setHandler(aHandler);

        root.create(createPtResult -> {
            if (createPtResult.succeeded()) {
                // Last thing passed in via our configuration arguments is the test object's ID
                final PairtreeObject ptObject = root.getObject(aID);

                ptObject.create(createPtObjResult -> {
                    if (createPtObjResult.succeeded()) {
                        future.complete(ptObject);
                    } else {
                        future.fail(createPtObjResult.cause());
                    }
                });
            } else {
                future.fail(createPtResult.cause());
            }
        });
    }

    /**
     * Does the work of creating a test Pairtree object in S3 so that tests against that object can be run.
     *
     * @param aPairtreeImpl A Pairtree implementation
     * @param aHandler to handle the result of the creation event
     * @param aFile A file system directory Pairtree
     * @param aID A Pairtree object ID
     */
    protected void createTestS3PairtreeObject(final Handler<AsyncResult<PairtreeObject>> aHandler,
            final String aBucket, final String aAccessKey, final String aSecretKey, final String aEndpoint,
            final String aID) throws PairtreeException {
        Objects.requireNonNull(aHandler, LOGGER.getMessage(MessageCodes.PT_010));

        final Region region = RegionUtils.getRegion(aEndpoint);
        final Pairtree root = new PairtreeFactory(myVertx).getPairtree(aBucket, aAccessKey, aSecretKey, region);
        final Future<PairtreeObject> future = Future.<PairtreeObject>future().setHandler(aHandler);

        root.create(createPtResult -> {
            if (createPtResult.succeeded()) {
                // Last thing passed in via our configuration arguments is the test object's ID
                final PairtreeObject ptObject = root.getObject(aID);

                ptObject.create(createPtObjResult -> {
                    if (createPtObjResult.succeeded()) {
                        future.complete(ptObject);
                    } else {
                        future.fail(createPtObjResult.cause());
                    }
                });
            } else {
                future.fail(createPtResult.cause());
            }
        });
    }

}
