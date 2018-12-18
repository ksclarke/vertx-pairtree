
package info.freelibrary.pairtree;

import java.util.Optional;

import info.freelibrary.util.I18nObject;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * A base pairtree class which can be extended by specific implementations.
 */
public abstract class AbstractPairtree extends I18nObject implements Pairtree {

    /** The Pairtree's prefix */
    protected Optional<String> myPrefix;

    /**
     * Creates an abstract pairtree object.
     */
    protected AbstractPairtree() {
        super(Constants.BUNDLE_NAME);
    }

    @Override
    public Optional<String> getPrefix() {
        return myPrefix;
    }

    @Override
    public boolean hasPrefix() {
        return myPrefix.isPresent() && myPrefix.get().length() > 0;
    }

    @Override
    public String getVersionFileName() {
        return PAIRTREE_VERSION + PT_VERSION_NUM.replace('.', '_');
    }

    @Override
    public String getPrefixFileName() {
        return PAIRTREE_PREFIX;
    }

    @Override
    public abstract void create(Handler<AsyncResult<Void>> aHandler);

    @Override
    public abstract void delete(Handler<AsyncResult<Void>> aHandler);

    @Override
    public abstract void exists(Handler<AsyncResult<Boolean>> aHandler);

    @Override
    public void createIfNeeded(final Handler<AsyncResult<Void>> aHandler) {
        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        exists(existsHandler -> {
            if (existsHandler.succeeded()) {
                if (existsHandler.result()) {
                    future.complete();
                } else {
                    create(createHandler -> {
                        if (createHandler.succeeded()) {
                            future.complete();
                        } else {
                            future.fail(createHandler.cause());
                        }
                    });
                }
            } else {
                future.fail(existsHandler.cause());
            }
        });
    }

}
