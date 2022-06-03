
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.PATH_SEP;
import static info.freelibrary.pairtree.Pairtree.ROOT;
import static info.freelibrary.util.Constants.SLASH;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import info.freelibrary.util.HTTP;
import info.freelibrary.util.I18nObject;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import info.freelibrary.vertx.s3.S3Client;
import info.freelibrary.vertx.s3.UnexpectedStatusException;

import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.PairtreeException;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.pairtree.PairtreeUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;

/**
 * An S3-backed Pairtree object implementation.
 */
public class S3PairtreeObject extends I18nObject implements PairtreeObject {

    /** The logger used when interacting with the S3 Pairtree object */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3PairtreeObject.class, MessageCodes.BUNDLE);

    /** Creates a README file for an S3 Pairtree */
    private static final String README_FILE = "/README.txt";

    /** A regular plus symbol */
    private static final String UNENCODED_PLUS = "+";

    /** A URL encoded plus symbol */
    private static final String ENCODED_PLUS = "%2B";

    /** The client used to interact with the S3 Pairtree */
    private final S3Client myS3Client;

    /** The bucket in which the Pairtree resides */
    private final String myPairtreeBucket;

    /** The path in the bucket to the Pairtree */
    private final String myBucketPath;

    /** The Pairtree's prefix (optional) */
    private final Optional<String> myPrefix;

    /** The Pairtree's ID */
    private final String myID;

    /**
     * Creates a new Pairtree object.
     *
     * @param aS3Client An S3 client to communicate with S3
     * @param aPairtree An S3-backed Pairtree
     * @param aID An ID for the Pairtree resource
     */
    public S3PairtreeObject(final S3Client aS3Client, final S3Pairtree aPairtree, final String aID) {
        super(MessageCodes.BUNDLE);

        myBucketPath = aPairtree.getBucketPath();
        myPairtreeBucket = aPairtree.getPath();
        myPrefix = aPairtree.getPrefix();
        myS3Client = aS3Client;
        myID = aID;
    }

    @Override
    public Future<Boolean> exists() {
        final Promise<Boolean> promise = Promise.<Boolean>promise();

        myS3Client.head(myPairtreeBucket, myBucketPath + getPath() + README_FILE).onSuccess(head -> {
            final String contentLength = head.get(HttpHeaders.CONTENT_LENGTH);

            try {
                if (Integer.parseInt(contentLength) > 0) {
                    promise.complete();
                } else {
                    promise.fail(new UnexpectedStatusException(500, getI18n(MessageCodes.PT_016, contentLength)));
                }
            } catch (final NumberFormatException details) {
                promise.fail(getI18n(MessageCodes.PT_019, contentLength));
            }
        }).onFailure(promise::fail);

        return promise.future();
    }

    @Override
    public void exists(final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));
        exists().onComplete(aHandler);
    }

    @Override
    public Future<Void> create() {
        final Promise<Void> promise = Promise.<Void>promise();
        final String key = myBucketPath + getPath() + README_FILE;

        myS3Client.put(myPairtreeBucket, key, Buffer.buffer(myID)).onSuccess(headers -> {
            promise.complete();
        }).onFailure(promise::fail);

        return promise.future();
    }

    @Override
    public void create(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));
        create().onComplete(aHandler);
    }

    @Override
    public Future<Void> delete() {
        final Promise<Void> promise = Promise.<Void>promise();

        myS3Client.list(myPairtreeBucket, myBucketPath + getPath()).onSuccess(bucketList -> {
            @SuppressWarnings("rawtypes")
            final List<Future> deletions = new ArrayList<>();

            bucketList.forEach(s3Object -> {
                deletions.add(myS3Client.delete(myPairtreeBucket, s3Object.getKey()));
            });

            CompositeFuture.all(deletions).onComplete(allDeletions -> {
                if (allDeletions.succeeded()) {
                    promise.complete();
                } else {
                    promise.fail(allDeletions.cause());
                }
            });
        }).onFailure(promise::fail);

        return promise.future();
    }

    @Override
    public void delete(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));
        delete().onComplete(aHandler);
    }

    @Override
    public String getID() {
        return !myPrefix.isPresent() ? myID : myPrefix.get() + SLASH + myID;
    }

    /**
     * Gets the object path. If the path contains a '+' it will be URL encoded for interaction with S3's HTTP API.
     *
     * @return the path of the Pairtree object as it's found in S3
     */
    @Override
    public String getPath() {
        // We need to URL encode '+'s to work around an S3 bug
        // (Cf. https://forums.aws.amazon.com/thread.jspa?threadID=55746)
        return ROOT + SLASH + PairtreeUtils.mapToPtPath(myID).replace(UNENCODED_PLUS, ENCODED_PLUS) + SLASH +
                PairtreeUtils.encodeID(myID).replace(UNENCODED_PLUS, ENCODED_PLUS);
    }

    /**
     * Gets the path of the requested object resource. If the path contains a '+' it will be URL encoded for interaction
     * with S3's HTTP API.
     *
     * @param aResourcePath The Pairtree resource which the returned path should represent
     * @return The path of the requested object resource as it's found in S3
     */
    @Override
    public String getPath(final String aResourcePath) {
        // We need to URL encode '+'s to work around an S3 bug
        // (Cf. https://forums.aws.amazon.com/thread.jspa?threadID=55746)
        return aResourcePath.charAt(0) == '/' ? getPath() + aResourcePath.replace(UNENCODED_PLUS, ENCODED_PLUS)
                : getPath() + SLASH + aResourcePath.replace(UNENCODED_PLUS, ENCODED_PLUS);
    }

    @Override
    public Future<Void> put(final String aPath, final Buffer aBuffer) {
        final Promise<Void> promise = Promise.<Void>promise();

        myS3Client.put(myPairtreeBucket, myBucketPath + getPath(aPath), aBuffer).onSuccess(response -> {
            promise.complete();
        }).onFailure(promise::fail);

        return promise.future();
    }

    @Override
    public void put(final String aPath, final Buffer aBuffer, final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));
        put(aPath, aBuffer).onComplete(aHandler);
    }

    @Override
    public Future<Buffer> get(final String aPath) {
        final Promise<Buffer> promise = Promise.<Buffer>promise();

        myS3Client.get(myPairtreeBucket, myBucketPath).onSuccess(response -> {
            response.body().onComplete(body -> {
                if (body.succeeded()) {
                    promise.complete(body.result());
                } else {
                    promise.fail(PairtreeException.from(body.cause(), MessageCodes.PT_001));
                }
            });
        }).onFailure(cause -> promise.fail(PairtreeException.from(cause, MessageCodes.PT_001)));

        return promise.future();
    }

    @Override
    public void get(final String aPath, final Handler<AsyncResult<Buffer>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));

        final Promise<Buffer> future = Promise.<Buffer>promise();

        future.future().onComplete(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_058, aPath);

        myS3Client.get(myPairtreeBucket, myBucketPath + getPath(aPath), response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                response.bodyHandler(bodyHandlerResult -> {
                    future.complete(Buffer.buffer(bodyHandlerResult.getBytes()));
                });
            } else {
                final String status = response.statusMessage();
                future.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, getPath() + PATH_SEP + aPath, status));
            }
        });
    }

    @Override
    public void find(final String aPath, final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));

        final Promise<Boolean> promise = Promise.<Boolean>promise();

        promise.future().onComplete(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_059, aPath, myPairtreeBucket, getPath(aPath));

        myS3Client.head(myPairtreeBucket, myBucketPath + getPath(aPath), response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                final String contentLength = response.getHeader(HTTP.CONTENT_LENGTH);

                try {
                    if (Integer.parseInt(contentLength) > 0) {
                        promise.complete(true);
                    } else {
                        promise.complete(false);
                    }
                } catch (final NumberFormatException details) {
                    promise.fail(getI18n(MessageCodes.PT_019, contentLength));
                }
            } else if (statusCode == HTTP.NOT_FOUND) {
                promise.complete(false);
            } else {
                final String status = response.statusMessage();
                promise.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, getPath() + PATH_SEP + aPath, status));
            }
        });
    }

}
