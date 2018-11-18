
package info.freelibrary.pairtree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;

/**
 * A verticle for pairtrees.
 */
public class PairtreeVerticle extends AbstractVerticle {

    /* Using SLF4J until https://github.com/dazraf/vertx-hot/issues/1 is fixed */
    /** Logger for the verticle */
    private static final Logger LOGGER = LoggerFactory.getLogger(PairtreeVerticle.class.getName());

    /** A default port to use if another one isn't supplied */
    private static final String DEFAULT_PORT = "8888";

    /** Default host the verticle will run at */
    private static final String HOST = "0.0.0.0"; // NOPMD

    @Override
    public void start(final Future<Void> aFuture) {
        final HttpServerOptions options = new HttpServerOptions();
        final String portValue = System.getProperty("vertx.port", DEFAULT_PORT);

        try {
            options.setPort(Integer.parseInt(portValue));
        } catch (final NumberFormatException details) {
            LOGGER.warn("Supplied port value '{}' wasn't a valid integer so using {}", portValue, DEFAULT_PORT);
            options.setPort(Integer.parseInt(DEFAULT_PORT));
        }

        options.setHost(HOST);

        vertx.createHttpServer(options).requestHandler(request -> {
            /* Browsers want to send along a favicon request, which we can ignore */
            if (request.absoluteURI().endsWith("favicon.ico")) {
                request.response().end();
            } else {
                request.response().end("<h1>Hello World!</h1>");
            }
        }).listen(result -> {
            if (result.succeeded()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.PT_DEBUG_060, PairtreeVerticle.class.getName(), deploymentID());
                }

                aFuture.complete();
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(MessageCodes.PT_DEBUG_061, options.getPort());
                }

                aFuture.fail(result.cause());
            }
        });
    }
}
