
package info.freelibrary.pairtree;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.MessageCodes.PT_010;

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

@RunWith(VertxUnitRunner.class)
public abstract class AbstractPairtreeTest extends I18nObject {

    protected static final String TEST_OBJECT_NAME = "asdf";

    protected final Logger LOGGER = getLogger();

    protected Vertx myVertx;

    public AbstractPairtreeTest() {
        super(BUNDLE_NAME);
    }

    @Before
    public void setUp(final TestContext aContext) {
        LOGGER.debug("Initializing Vert.x");
        myVertx = Vertx.vertx();
    }

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
     * Does the work of creating a test Pairtree object in a test Pairtree so that tests against that object can be
     * run.
     *
     * @param aID An ID of the <code>PairtreeObject</code> to be created
     * @param aHandler A handler to handle the creation of the test <code>PairtreeObject</code>
     */
    protected void createTestPairtreeObject(final PairtreeImpl aPairtreeImpl,
            final Handler<AsyncResult<PairtreeObject>> aHandler, final String... aConfigVarargs) {
        final PairtreeRoot root = PairtreeFactory.getFactory(myVertx, aPairtreeImpl).getPairtree(aConfigVarargs);
        final Future<PairtreeObject> future = Future.future();

        if (aHandler != null) {
            future.setHandler(aHandler);

            root.create(createPtResult -> {
                // FIXME: We don't get here!
                if (createPtResult.succeeded()) {
                    // Last thing passed in via our configuration arguments is the test object's ID
                    final PairtreeObject ptObject = root.getObject(aConfigVarargs[aConfigVarargs.length - 1]);

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
        } else {
            final String simpleName = getClass().getSimpleName();
            throw new NullPointerException(getI18n(PT_010, simpleName, ".createTestPairtreeObject()"));
        }
    }

}
