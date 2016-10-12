
package info.freelibrary.pairtree;

import static info.freelibrary.pairtree.PairtreeConstants.BUNDLE_NAME;

import java.util.Arrays;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl;
import info.freelibrary.util.I18nObject;
import info.freelibrary.util.Logger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * An abstract Pairtree test object.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class AbstractPairtreeTest extends I18nObject {

    protected static final String TEST_OBJECT_NAME = "asdf";

    protected final Logger LOGGER = getLogger();

    protected Vertx myVertx;

    /**
     * Creates an abstract Pairtree test.
     */
    public AbstractPairtreeTest() {
        super(BUNDLE_NAME);
    }

    /**
     * Setup for the tests.
     *
     * @param aContext A test context
     */
    @Before
    public void setUp(final TestContext aContext) {
        LOGGER.debug("Initializing Vert.x");
        myVertx = Vertx.vertx();
    }

    /**
     * Tear down for the tests.
     *
     * @param aContext A test context
     */
    @After
    public void tearDown(final TestContext aContext) {
        // FIXME: This causes problems... Why?
        // LOGGER.debug("Shutting down Vert.x");
        // myVertx.close(aContext.asyncAssertSuccess());
    }

    /**
     * Returns the logger for the Pairtree implementation.
     *
     * @return The logger for the Pairtree implementation
     */
    protected abstract Logger getLogger();

    /**
     * Does the work of creating a test Pairtree object in a test Pairtree so that tests against that object can be run.
     *
     * @param aPairtreeImpl A Pairtree implementation
     * @param aHandler to handle the result of the creation event
     * @param aConfig A configuration passed as a varargs
     */
    protected void createTestPairtreeObject(final PairtreeImpl aPairtreeImpl,
            final Handler<AsyncResult<PairtreeObject>> aHandler, final String... aConfig) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010, getClass().getSimpleName(),
                ".createTestPairtreeObject()"));

        final String[] config = Arrays.copyOf(aConfig, aConfig.length - 1);
        final PairtreeRoot root = PairtreeFactory.getFactory(myVertx, aPairtreeImpl).getPairtree(config);
        final Future<PairtreeObject> future = Future.<PairtreeObject>future().setHandler(aHandler);

        root.create(createPtResult -> {
            if (createPtResult.succeeded()) {
                // Last thing passed in via our configuration arguments is the test object's ID
                final PairtreeObject ptObject = root.getObject(aConfig[aConfig.length - 1]);

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
