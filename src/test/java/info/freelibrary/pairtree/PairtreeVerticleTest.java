
package info.freelibrary.pairtree;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests for <code>PairtreeVerticle</code>.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
@RunWith(VertxUnitRunner.class)
public class PairtreeVerticleTest {

    /** The connection to the Vertx framework */
    private Vertx vertx;

    /**
     * Setup for the tests.
     *
     * @param aContext A test context
     */
    @Before
    public void setUp(final TestContext aContext) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(PairtreeVerticle.class.getName(), aContext.asyncAssertSuccess());
    }

    /**
     * Tear down for the tests.
     *
     * @param aContext A test context
     */
    @After
    public void tearDown(final TestContext aContext) {
        vertx.close(aContext.asyncAssertSuccess());
    }

    @Test
    public void testMyApplication(final TestContext aContext) {
        final Async async = aContext.async();

        vertx.createHttpClient().getNow(8888, "localhost", "/", response -> {
            response.handler(body -> {
                aContext.assertTrue(body.toString().contains("Hello"));
                async.complete();
            });
        });
    }
}
