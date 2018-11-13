
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.Constants.PATH_SEP;
import static info.freelibrary.pairtree.Pairtree.PAIRTREE_ROOT;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import info.freelibrary.pairtree.HTTP;
import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.pairtree.PairtreeUtils;
import info.freelibrary.util.I18nObject;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

/**
 * An S3-backed Pairtree object implementation.
 */
public class S3PairtreeObject extends I18nObject implements PairtreeObject {

    /** The logger used when interacting with the S3 Pairtree object */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3PairtreeObject.class, BUNDLE_NAME);

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
    private final String myPrefix;

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
        super(BUNDLE_NAME);

        myBucketPath = aPairtree.getBucketPath();
        myPairtreeBucket = aPairtree.getPath();
        myPrefix = aPairtree.getPrefix();
        myS3Client = aS3Client;
        myID = aID;
    }

    @Override
    public void exists(final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));

        final Future<Boolean> future = Future.<Boolean>future().setHandler(aHandler);

        myS3Client.head(myPairtreeBucket, myBucketPath + getPath() + README_FILE, response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                final String contentLength = response.getHeader(HTTP.CONTENT_LENGTH);

                try {
                    if (Integer.parseInt(contentLength) > 0) {
                        future.complete(true);
                    } else {
                        future.complete(false);
                    }
                } catch (final NumberFormatException details) {
                    future.fail(getI18n(MessageCodes.PT_019, contentLength));
                }
            } else if (statusCode == HTTP.NOT_FOUND) {
                future.complete(false);
            } else {
                final String statusMessage = response.statusMessage();
                future.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, getPath() + README_FILE, statusMessage));
            }
        });
    }

    @Override
    public void create(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        myS3Client.put(myPairtreeBucket, myBucketPath + getPath() + README_FILE, Buffer.buffer(myID), response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                future.complete();
            } else {
                final String statusMessage = response.statusMessage();

                future.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, getPath() + README_FILE, statusMessage));
            }
        });
    }

    @Override
    public void delete(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        myS3Client.list(myPairtreeBucket, myBucketPath + getPath(), listResponse -> {
            final int listStatusCode = listResponse.statusCode();

            if (listStatusCode == HTTP.OK) {
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
                            final Future<Void> keyFuture = Future.future();

                            futures.add(keyFuture);

                            myS3Client.delete(myPairtreeBucket, key, deleteResponse -> {
                                final int deleteStatusCode = deleteResponse.statusCode();

                                if (deleteStatusCode == HTTP.NO_CONTENT) {
                                    keyFuture.complete();
                                } else {
                                    final String status = deleteResponse.statusMessage();
                                    keyFuture.fail(getI18n(MessageCodes.PT_DEBUG_045, deleteStatusCode, key, status));
                                }
                            });
                        }

                        CompositeFuture.all(futures).setHandler(result -> {
                            if (result.succeeded()) {
                                future.complete();
                            } else {
                                future.fail(result.cause());
                            }
                        });
                    } catch (final ParserConfigurationException | SAXException | IOException details) {
                        future.fail(details);
                    }
                });
            } else {
                final String status = listResponse.statusMessage();
                future.fail(getI18n(MessageCodes.PT_DEBUG_045, listStatusCode, getPath(), status));
            }
        });
    }

    @Override
    public String getID() {
        return myPrefix == null ? myID : myPrefix + PATH_SEP + myID;
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
        return PAIRTREE_ROOT + PATH_SEP + PairtreeUtils.mapToPtPath(myID).replace(UNENCODED_PLUS, ENCODED_PLUS) +
                PATH_SEP + PairtreeUtils.encodeID(myID).replace(UNENCODED_PLUS, ENCODED_PLUS);
    }

    /**
     * Gets the path of the requested object resource. If the path contains a '+' it will be URL encoded for
     * interaction with S3's HTTP API.
     *
     * @param aResourcePath The Pairtree resource which the returned path should represent
     * @return The path of the requested object resource as it's found in S3
     */
    @Override
    public String getPath(final String aResourcePath) {
        // We need to URL encode '+'s to work around an S3 bug
        // (Cf. https://forums.aws.amazon.com/thread.jspa?threadID=55746)
        return aResourcePath.charAt(0) == '/' ? getPath() + aResourcePath.replace(UNENCODED_PLUS, ENCODED_PLUS)
                : getPath() + PATH_SEP + aResourcePath.replace(UNENCODED_PLUS, ENCODED_PLUS);
    }

    @Override
    public void put(final String aPath, final Buffer aBuffer, final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_057, aPath);

        myS3Client.put(myPairtreeBucket, myBucketPath + getPath(aPath), aBuffer, response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                future.complete();
            } else {
                if (statusCode == HTTP.FORBIDDEN) {
                    response.bodyHandler(handler -> {
                        LOGGER.debug(new String(handler.getBytes()));
                    });
                }

                final String status = response.statusMessage();
                future.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, getPath() + PATH_SEP + aPath, status));
            }
        });
    }

    @Override
    public void get(final String aPath, final Handler<AsyncResult<Buffer>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010));

        final Future<Buffer> future = Future.<Buffer>future().setHandler(aHandler);

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

        final Future<Boolean> future = Future.<Boolean>future().setHandler(aHandler);

        LOGGER.debug(MessageCodes.PT_DEBUG_059, aPath, myPairtreeBucket, getPath(aPath));

        myS3Client.head(myPairtreeBucket, myBucketPath + getPath(aPath), response -> {
            final int statusCode = response.statusCode();

            if (statusCode == HTTP.OK) {
                final String contentLength = response.getHeader(HTTP.CONTENT_LENGTH);

                try {
                    if (Integer.parseInt(contentLength) > 0) {
                        future.complete(true);
                    } else {
                        future.complete(false);
                    }
                } catch (final NumberFormatException details) {
                    future.fail(getI18n(MessageCodes.PT_019, contentLength));
                }
            } else if (statusCode == HTTP.NOT_FOUND) {
                future.complete(false);
            } else {
                final String status = response.statusMessage();
                future.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, getPath() + PATH_SEP + aPath, status));
            }
        });
    }

}
