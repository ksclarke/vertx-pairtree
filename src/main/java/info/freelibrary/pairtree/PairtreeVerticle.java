
package info.freelibrary.pairtree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;

/**
 * A verticle for pairtrees.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class PairtreeVerticle extends AbstractVerticle {

    /* Using SLF4J until https://github.com/dazraf/vertx-hot/issues/1 is fixed */
    /** Logger for the verticle */
    private static final Logger LOGGER = LoggerFactory.getLogger(PairtreeVerticle.class.getName());

    /** Default port the verticle will run on */
    private static final int PORT = 8888;

    /** Default host the verticle will run at */
    private static final String HOST = "0.0.0.0";

    @Override
    public void start(final Future<Void> aFuture) {
        final HttpServerOptions options = new HttpServerOptions();

        options.setPort(PORT);
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
                    LOGGER.debug("{} deployed: {}", PairtreeVerticle.class.getName(), deploymentID());
                }

                aFuture.complete();
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Pairtree server started at port: {}", PORT);
                }

                aFuture.fail(result.cause());
            }
        });
    }
}
