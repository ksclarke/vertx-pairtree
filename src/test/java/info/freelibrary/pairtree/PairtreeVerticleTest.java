
package info.freelibrary.pairtree;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(PairtreeVerticleTest.class, BUNDLE_NAME);

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

    /**
     * Tests the <code>PairtreeVerticle</code> class.
     *
     * @param aContext A test context
     */
    @Test
    public void testMyApplication(final TestContext aContext) {
        final int port = Integer.parseInt(System.getProperty("vertx.port"));
        final Async async = aContext.async();

        LOGGER.debug("Starting Vertx test verticle at port {}", port);

        vertx.createHttpClient().getNow(port, "localhost", "/", response -> {
            response.handler(body -> {
                aContext.assertTrue(body.toString().contains("Hello"));
                async.complete();
            });
        });
    }

}
