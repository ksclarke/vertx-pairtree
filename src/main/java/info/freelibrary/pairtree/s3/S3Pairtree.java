
package info.freelibrary.pairtree.s3;

import static info.freelibrary.util.Constants.EMPTY;
import static info.freelibrary.util.Constants.EOL;
import static info.freelibrary.util.Constants.SLASH;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import info.freelibrary.vertx.s3.S3Client;

import info.freelibrary.pairtree.AbstractPairtree;
import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.PairtreeObject;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

/**
 * A S3 backed Pairtree implementation.
 */
public class S3Pairtree extends AbstractPairtree {

    /** An S3 Pairtree logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Pairtree.class, MessageCodes.BUNDLE);

    /** The Pairtree's S3 bucket path */
    private final String myBucketPath;

    /** The Pairtree's S3 bucket */
    private final String myBucket;

    /** The S3 client to use for the Pairtree's operations */
    private final S3Client myS3Client;

    /**
     * Creates an S3Pairtree using the supplied S3PairtreeOptions.
     *
     * @param aVertx A Vert.x instance
     * @param aConfig An S3 Pairtree configuration
     */
    public S3Pairtree(final Vertx aVertx, final S3PairtreeOptions aConfig) {
        this(null, aVertx, aConfig);
    }

    /**
     * Creates a prefixed S3Pairtree using the supplied S3PairtreeOptions.
     *
     * @param aPrefix A Pairtree prefix
     * @param aVertx A Vert.x instance
     * @param aConfig An S3 Pairtree configuration
     */
    public S3Pairtree(final String aPrefix, final Vertx aVertx, final S3PairtreeOptions aConfig) {
        final String bucketPath;

        myBucket = Objects.requireNonNull(aConfig, getI18n(MessageCodes.PT_017)).getBucket();
        Objects.requireNonNull(StringUtils.trimToNull(myBucket), getI18n(MessageCodes.PT_015));
        myS3Client = new S3Client(aVertx, aConfig);
        bucketPath = aConfig.getBucketPath();
        myBucketPath = (bucketPath != null && bucketPath.charAt(0) != '/') ? SLASH + bucketPath : bucketPath;
        myPrefix = aPrefix;

        if (myPrefix != null) {
            LOGGER.debug(MessageCodes.PT_DEBUG_002, myBucket, myPrefix);
        } else {
            LOGGER.debug(MessageCodes.PT_DEBUG_001, myBucket);
        }
    }

    @Override
    public PairtreeObject getObject(final String aID) {
        return new S3PairtreeObject(myS3Client, this, aID);
    }

    @Override
    public List<PairtreeObject> getObjects(final List<String> aIDList) {
        final List<PairtreeObject> ptObjList = new ArrayList<>();
        final Iterator<String> iterator = aIDList.iterator();

        while (iterator.hasNext()) {
            final String id = iterator.next();

            Objects.requireNonNull(StringUtils.trimToNull(id));
            ptObjList.add(new S3PairtreeObject(myS3Client, this, id));
        }

        return ptObjList;
    }

    @Override
    public Future<Boolean> exists() {
        final Promise<Boolean> promise = Promise.<Boolean>promise();

        myS3Client.get(myBucket, getVersionFilePath()).onSuccess(versionFileResponse -> {
            if (hasPrefix()) {
                myS3Client.get(myBucket, getPrefixFilePath()).onSuccess(response -> {
                    promise.complete(true);
                }).onFailure(promise::fail);
            } else {
                promise.complete(true);
            }
        }).onFailure(promise::fail);

        return promise.future();
    }

    @Override
    public void exists(final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));
        exists().onComplete(aHandler::handle);
    }

    @Override
    public Future<Void> create() {
        final Promise<Void> promise = Promise.promise();

        exists().onSuccess(exists -> {
            if (!exists) {
                final String ptVersion = getI18n(MessageCodes.PT_011, VERSION_NUM);
                final Buffer buffer = Buffer.buffer().appendString(ptVersion).appendString(EOL)
                        .appendString(getI18n(MessageCodes.PT_012));

                myS3Client.put(myBucket, getVersionFilePath(), buffer).compose((result) -> {
                    if (hasPrefix()) {
                        return myS3Client.put(myBucket, getPrefixFilePath(), Buffer.buffer(myPrefix));
                    }

                    return Future.succeededFuture();
                }).onFailure(promise::fail).onSuccess(result -> {
                    promise.complete();
                });
            } else {
                promise.complete();
            }
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
        final Promise<Void> promise = Promise.promise();

        myS3Client.list(myBucket).onSuccess(bucketList -> {
            @SuppressWarnings("rawtypes")
            final List<Future> deletions = new ArrayList<>();

            bucketList.forEach(s3Object -> {
                deletions.add(myS3Client.delete(myBucket, s3Object.getKey()));
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
    public String toString() {
        return "s3://" + SLASH + myBucket + getBucketPath() + SLASH + ROOT;
    }

    @Override
    public String getPath() {
        return myBucket + getBucketPath();
    }

    /**
     * Gets the path at which the Pairtree is found. This will be an empty string if the Pairtree is at the root of the
     * S3 bucket.
     *
     * @return The path in the s3 bucket to the Pairtree
     */
    public String getBucketPath() {
        return myBucketPath != null ? myBucketPath : EMPTY;
    }

    @Override
    public String getPrefixFilePath() {
        final String bucketPath = getBucketPath();

        return EMPTY.equals(bucketPath) ? PREFIX : bucketPath + SLASH + PREFIX;
    }

    @Override
    public String getVersionFilePath() {
        final String bucketPath = getBucketPath();

        return EMPTY.equals(bucketPath) ? getVersionFileName() : bucketPath + SLASH + getVersionFileName();
    }

    /**
     * Returns the S3 client that the Pairtree uses.
     *
     * @return The S3 client that the Pairtree uses
     */
    public S3Client getS3Client() {
        return myS3Client;
    }

}
