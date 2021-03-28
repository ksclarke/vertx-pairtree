
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.amazonaws.regions.Region;

import info.freelibrary.pairtree.AbstractPairtree;
import info.freelibrary.pairtree.Constants;
import info.freelibrary.pairtree.HTTP;
import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;
import info.freelibrary.vertx.s3.S3Client;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

/**
 * A S3 backed Pairtree implementation.
 */
public class S3Pairtree extends AbstractPairtree {

    /** AWS access key */
    public static final String AWS_ACCESS_KEY = "AWS_ACCESS_KEY";

    /** AWS secret key */
    public static final String AWS_SECRET_KEY = "AWS_SECRET_KEY";

    /** An S3 Pairtree logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Pairtree.class, BUNDLE_NAME);

    /** The S3 endpoint prefix */
    private static final String S3_SERVICE_ENDPOINT = "s3";

    /** The Pairtree's S3 bucket */
    private final String myBucket;

    /** The Pairtree's S3 bucket path */
    private final Optional<String> myBucketPath;

    /** The S3 client to use for the Pairtree's operations */
    private final S3Client myS3Client;

    /**
     * Creates S3Pairtree using the supplied S3 bucket, access key and secret key.
     *
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     */
    public S3Pairtree(final Vertx aVertx, final String aBucket, final String aAccessKey, final String aSecretKey) {
        this(Optional.empty(), aVertx, aBucket, Optional.empty(), aAccessKey, aSecretKey, Optional.empty());
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket and credentials from the supplied AWS profile.
     *
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aProfile An S3 profile with the appropriate credentials
     * @param aBucket An S3 bucket in which to put the Pairtree
     */
    public S3Pairtree(final Vertx aVertx, final S3Profile aProfile, final String aBucket) {
        myS3Client = new S3Client(aVertx, aProfile);
        myBucketPath = Optional.empty();
        myBucket = aBucket;
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket and credentials from the supplied AWS profile.
     *
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aProfile An S3 profile with the appropriate credentials
     * @param aBucketPath A path within the bucket at which the Pairtree should be put
     * @param aBucket An S3 bucket in which to put the Pairtree
     */
    public S3Pairtree(final Vertx aVertx, final S3Profile aProfile, final String aBucket, final String aBucketPath) {
        myS3Client = new S3Client(aVertx, aProfile);
        myBucketPath = Optional.of(aBucketPath);
        myBucket = aBucket;
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket, access key and secret key.
     *
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aBucketPath A path in an S3 bucket in which to put the Pairtree
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     */
    public S3Pairtree(final Vertx aVertx, final String aBucket, final String aBucketPath, final String aAccessKey,
            final String aSecretKey) {
        this(Optional.empty(), aVertx, aBucket, Optional.of(aBucketPath), aAccessKey, aSecretKey, Optional.empty());
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket, access key and secret key.
     *
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aEndpoint An S3 endpoint
     */
    public S3Pairtree(final Vertx aVertx, final String aBucket, final String aAccessKey, final String aSecretKey,
            final Region aEndpoint) {
        this(Optional.empty(), aVertx, aBucket, Optional.empty(), aAccessKey, aSecretKey, Optional.of(aEndpoint));
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket and credentials from the supplied AWS profile.
     *
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aProfile An S3 profile with the appropriate credentials
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aRegion An S3 region in which to create the bucket
     */
    public S3Pairtree(final Vertx aVertx, final S3Profile aProfile, final String aBucket, final Region aRegion) {
        myS3Client = new S3Client(aVertx, aProfile, aRegion.getServiceEndpoint(S3_SERVICE_ENDPOINT));
        myBucketPath = Optional.empty();
        myBucket = aBucket;
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket and credentials from the supplied AWS profile.
     *
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aProfile An S3 profile with the appropriate credentials
     * @param aBucketPath A path within the bucket at which the Pairtree should be put
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aRegion An S3 region in which to create the bucket
     */
    public S3Pairtree(final Vertx aVertx, final S3Profile aProfile, final String aBucket, final String aBucketPath,
            final Region aRegion) {
        myS3Client = new S3Client(aVertx, aProfile, aRegion.getServiceEndpoint(S3_SERVICE_ENDPOINT));
        myBucketPath = Optional.of(aBucketPath);
        myBucket = aBucket;
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket, access key and secret key.
     *
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aBucketPath A path in an S3 bucket in which to put the Pairtree
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aEndpoint An S3 endpoint
     */
    public S3Pairtree(final Vertx aVertx, final String aBucket, final String aBucketPath, final String aAccessKey,
            final String aSecretKey, final Region aEndpoint) {
        this(Optional.empty(), aVertx, aBucket, Optional.of(aBucketPath), aAccessKey, aSecretKey, Optional.of(
                aEndpoint));
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket, access key and secret key.
     *
     * @param aPairtreePrefix A Pairtree prefix
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     */
    public S3Pairtree(final String aPairtreePrefix, final Vertx aVertx, final String aBucket, final String aAccessKey,
            final String aSecretKey) {
        this(Optional.of(aPairtreePrefix), aVertx, aBucket, Optional.empty(), aAccessKey, aSecretKey, Optional
                .empty());
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket, access key and secret key.
     *
     * @param aPairtreePrefix A Pairtree prefix
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aBucketPath A path in an S3 bucket in which to put the Pairtree
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     */
    public S3Pairtree(final String aPairtreePrefix, final Vertx aVertx, final String aBucket,
            final String aBucketPath, final String aAccessKey, final String aSecretKey) {
        this(Optional.of(aPairtreePrefix), aVertx, aBucket, Optional.of(aBucketPath), aAccessKey, aSecretKey, Optional
                .empty());
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket, access key and secret key.
     *
     * @param aPairtreePrefix A Pairtree prefix
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aRegion An S3 endpoint
     */
    public S3Pairtree(final String aPairtreePrefix, final Vertx aVertx, final String aBucket, final String aAccessKey,
            final String aSecretKey, final Region aRegion) {
        this(Optional.of(aPairtreePrefix), aVertx, aBucket, Optional.empty(), aAccessKey, aSecretKey, Optional.of(
                aRegion));
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket, access key and secret key.
     *
     * @param aPairtreePrefix A Pairtree prefix
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aBucketPath A path in an S3 bucket in which to put the Pairtree
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aRegion An S3 endpoint
     */
    public S3Pairtree(final String aPairtreePrefix, final Vertx aVertx, final String aBucket,
            final String aBucketPath, final String aAccessKey, final String aSecretKey, final Region aRegion) {
        this(Optional.of(aPairtreePrefix), aVertx, aBucket, Optional.of(aBucketPath), aAccessKey, aSecretKey, Optional
                .of(aRegion));
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket, access key and secret key.
     *
     * @param aPairtreePrefix A Pairtree prefix
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aRegion An S3 endpoint
     */
    private S3Pairtree(final Optional<String> aPairtreePrefix, final Vertx aVertx, final String aBucket,
            final Optional<String> aBucketPath, final String aAccessKey, final String aSecretKey,
            final Optional<Region> aRegion) {
        Objects.requireNonNull(StringUtils.trimToNull(aBucket), getI18n(MessageCodes.PT_015));
        Objects.requireNonNull(StringUtils.trimToNull(aAccessKey), getI18n(MessageCodes.PT_016));
        Objects.requireNonNull(StringUtils.trimToNull(aSecretKey), getI18n(MessageCodes.PT_017));

        if (aRegion.isPresent()) {
            final String region = aRegion.get().getServiceEndpoint(S3_SERVICE_ENDPOINT);
            myS3Client = new S3Client(aVertx, aAccessKey, aSecretKey, region);
        } else {
            myS3Client = new S3Client(aVertx, aAccessKey, aSecretKey);
        }

        myBucket = aBucket;
        myPrefix = aPairtreePrefix;

        if (aBucketPath.isPresent()) {
            if (aBucketPath.get().charAt(0) == '/') {
                myBucketPath = aBucketPath;
            } else {
                myBucketPath = Optional.of(Constants.PATH_SEP + aBucketPath.get());
            }
        } else {
            myBucketPath = aBucketPath;
        }

        if (myPrefix.isPresent()) {
            LOGGER.debug(MessageCodes.PT_DEBUG_002, myBucket, myPrefix.get());
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
    public void exists(final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));

        final Future<Boolean> future = Future.<Boolean>future().setHandler(aHandler);

        myS3Client.get(myBucket, getVersionFilePath(), getVersionResponse -> {
            final int versionStatusCode = getVersionResponse.statusCode();

            if (versionStatusCode == HTTP.OK) {
                if (hasPrefix()) {
                    myS3Client.get(myBucket, getPrefixFilePath(), getPrefixResponse -> {
                        final int prefixStatusCode = getPrefixResponse.statusCode();

                        if (prefixStatusCode == HTTP.OK) {
                            future.complete(true);
                        } else if (prefixStatusCode == HTTP.NOT_FOUND) {
                            future.complete(false);
                        } else {
                            final int statusCode = getPrefixResponse.statusCode();
                            final String statusMessage = getPrefixResponse.statusMessage();

                            future.fail(getI18n(MessageCodes.PT_018, statusCode, statusMessage));
                        }
                    });
                } else {
                    future.complete(true);
                }
            } else if (versionStatusCode == HTTP.NOT_FOUND) {
                future.complete(false);
            } else {
                final int statusCode = getVersionResponse.statusCode();
                final String statusMessage = getVersionResponse.statusMessage();

                future.fail(getI18n(MessageCodes.PT_018, statusCode, statusMessage));
            }
        });
    }

    @Override
    public void create(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);
        final StringBuilder specNote = new StringBuilder();
        final String ptVersion = getI18n(MessageCodes.PT_011, VERSION_NUM);

        specNote.append(ptVersion).append(System.lineSeparator()).append(getI18n(MessageCodes.PT_012));

        myS3Client.put(myBucket, getVersionFilePath(), Buffer.buffer(specNote.toString()), putVersionResponse -> {
            if (putVersionResponse.statusCode() == HTTP.OK) {
                if (hasPrefix()) {
                    final String prefix = myPrefix.get();

                    myS3Client.put(myBucket, getPrefixFilePath(), Buffer.buffer(prefix), putPrefixResponse -> {
                        if (putPrefixResponse.statusCode() == HTTP.OK) {
                            future.complete();
                        } else {
                            final int statusCode = putPrefixResponse.statusCode();
                            final String statusMessage = putPrefixResponse.statusMessage();

                            future.fail(getI18n(MessageCodes.PT_018, statusCode, statusMessage));
                        }
                    });
                } else {
                    future.complete();
                }
            } else {
                final int statusCode = putVersionResponse.statusCode();
                final String statusMessage = putVersionResponse.statusMessage();

                future.fail(getI18n(MessageCodes.PT_018, statusCode, statusMessage));
            }
        });
    }

    @Override
    public void delete(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        myS3Client.list(myBucket, listResponse -> {
            if (listResponse.statusCode() == HTTP.OK) {
                listResponse.bodyHandler(bodyHandler -> {
                    final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

                    saxParserFactory.setNamespaceAware(true);

                    try {
                        final SAXParser saxParser = saxParserFactory.newSAXParser();
                        final XMLReader xmlReader = saxParser.getXMLReader();
                        final ObjectListHandler s3ListHandler = new ObjectListHandler();
                        final List<Future> futures = new ArrayList<>();
                        final List<String> keyList;

                        xmlReader.setContentHandler(s3ListHandler);
                        xmlReader.parse(new InputSource(new StringReader(bodyHandler.toString())));
                        keyList = s3ListHandler.getKeys();

                        for (final String key : keyList) {
                            final Future<Void> deleteFuture = Future.future();

                            futures.add(deleteFuture);

                            myS3Client.delete(myBucket, key, deleteResponse -> {
                                if (deleteResponse.statusCode() == HTTP.NO_CONTENT) {
                                    deleteFuture.complete();
                                } else {
                                    final int statusCode = deleteResponse.statusCode();
                                    final String statusMessage = deleteResponse.statusMessage();

                                    deleteFuture.fail(getI18n(MessageCodes.PT_018, statusCode, statusMessage));
                                }
                            });
                        }

                        CompositeFuture.all(futures).setHandler(futuresHandler -> {
                            if (futuresHandler.succeeded()) {
                                future.complete();
                            } else {
                                future.fail(futuresHandler.cause());
                            }
                        });
                    } catch (final ParserConfigurationException | SAXException | IOException details) {
                        future.fail(details);
                    }
                });
            } else {
                final int statusCode = listResponse.statusCode();
                final String statusMessage = listResponse.statusMessage();

                future.fail(getI18n(MessageCodes.PT_018, statusCode, statusMessage));
            }
        });
    }

    @Override
    public String toString() {
        return "s3://" + Constants.PATH_SEP + myBucket + getBucketPath() + Constants.PATH_SEP + ROOT;
    }

    @Override
    public String getPath() {
        return myBucket + getBucketPath();
    }

    /**
     * Gets the path at which the Pairtree is found. This will be an empty string if the Pairtree is at the root of
     * the S3 bucket.
     *
     * @return The path in the s3 bucket to the Pairtree
     */
    public String getBucketPath() {
        return myBucketPath.orElse("");
    }

    @Override
    public String getPrefixFilePath() {
        final String bucketPath = getBucketPath();

        return "".equals(bucketPath) ? PREFIX : bucketPath + Constants.PATH_SEP + PREFIX;
    }

    @Override
    public String getVersionFilePath() {
        final String bucketPath = getBucketPath();

        return "".equals(bucketPath) ? getVersionFileName() : bucketPath + Constants.PATH_SEP + getVersionFileName();
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
