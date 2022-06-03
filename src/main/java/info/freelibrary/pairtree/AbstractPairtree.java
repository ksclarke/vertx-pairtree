
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
    protected String myPrefix;

    /**
     * Creates an abstract pairtree object.
     */
    protected AbstractPairtree() {
        super(MessageCodes.BUNDLE);
    }

    @Override
    public Optional<String> getPrefix() {
        return Optional.ofNullable(myPrefix);
    }

    @Override
    public boolean hasPrefix() {
        return myPrefix != null && !myPrefix.isEmpty();
    }

    @Override
    public String getVersionFileName() {
        return VERSION + VERSION_NUM.replace('.', '_');
    }

    @Override
    public String getPrefixFileName() {
        return PREFIX;
    }

    @Override
    public abstract Future<Void> create();

    @Override
    public abstract void create(Handler<AsyncResult<Void>> aHandler);

    @Override
    public abstract Future<Void> delete();

    @Override
    public abstract void delete(Handler<AsyncResult<Void>> aHandler);

    @Override
    public abstract Future<Boolean> exists();

    @Override
    public abstract void exists(Handler<AsyncResult<Boolean>> aHandler);

}
