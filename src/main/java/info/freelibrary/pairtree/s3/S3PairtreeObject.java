
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.Constants.FORBIDDEN;
import static info.freelibrary.pairtree.Constants.NOT_FOUND;
import static info.freelibrary.pairtree.Constants.NO_CONTENT;
import static info.freelibrary.pairtree.Constants.OK;
import static info.freelibrary.pairtree.Constants.SLASH;
import static info.freelibrary.pairtree.PairtreeRoot.PAIRTREE_ROOT;

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
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class S3PairtreeObject extends I18nObject implements PairtreeObject {

    /** The logger used when interacting with the S3 Pairtree object */
    private static final Logger LOGGER = LoggerFactory.getLogger(S3PairtreeObject.class, BUNDLE_NAME);

    /** The client used to interact with the S3 Pairtree */
    private final S3Client myS3Client;

    /** The bucket in which the Pairtree resides */
    private final String myPairtreeBucket;

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

        myPairtreeBucket = aPairtree.getPath();
        myPrefix = aPairtree.getPrefix();
        myS3Client = aS3Client;
        myID = aID;
    }

    @Override
    public void exists(final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010, getClass().getSimpleName(), ".exists()"));

        final Future<Boolean> future = Future.<Boolean>future().setHandler(aHandler);

        myS3Client.head(myPairtreeBucket, getPath() + "/README.txt", response -> {
            final int statusCode = response.statusCode();

            if (statusCode == OK) {
                final String contentLength = response.getHeader("Content-Length");

                try {
                    if (Integer.parseInt(contentLength) > 0) {
                        future.complete(true);
                    } else {
                        future.complete(false);
                    }
                } catch (final NumberFormatException details) {
                    future.fail("Content-Length was not an integer: " + contentLength);
                }
            } else if (statusCode == NOT_FOUND) {
                future.complete(false);
            } else {
                final String statusMessage = response.statusMessage();
                future.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, getPath() + "/README.txt", statusMessage));
            }
        });
    }

    @Override
    public void create(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010, getClass().getSimpleName(), ".create()"));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        myS3Client.put(myPairtreeBucket, getPath() + "/README.txt", Buffer.buffer(myID), response -> {
            final int statusCode = response.statusCode();

            if (statusCode == OK) {
                future.complete();
            } else {
                final String statusMessage = response.statusMessage();
                future.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, getPath() + "/README.txt", statusMessage));
            }
        });
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void delete(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010, getClass().getSimpleName(), ".delete()"));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        myS3Client.list(myPairtreeBucket, getPath(), listResponse -> {
            final int listStatusCode = listResponse.statusCode();

            if (listStatusCode == OK) {
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

                                if (deleteStatusCode == NO_CONTENT) {
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
        return myPrefix == null ? myID : myPrefix + SLASH + myID;
    }

    @Override
    public String getPath() {
        // Pairtree encodes colons to pluses so we need to pre-encode them as a workaround for an S3 bug
        // Cf. https://forums.aws.amazon.com/thread.jspa?threadID=55746
        final String awsID = myID.replace(':', '~');
        return PAIRTREE_ROOT + SLASH + PairtreeUtils.mapToPtPath(awsID) + SLASH + PairtreeUtils.encodeID(awsID);
    }

    @Override
    public String getPath(final String aResourcePath) {
        // We need to encode any pluses in our resource path as a workaround for an S3 bug
        // Cf. https://forums.aws.amazon.com/thread.jspa?threadID=55746
        final String awsPath = aResourcePath.replace('+', '~');
        return awsPath.startsWith("/") ? getPath() + awsPath : getPath() + SLASH + awsPath;
    }

    @Override
    public void put(final String aPath, final Buffer aBuffer, final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010, getClass().getSimpleName(), ".put()"));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        LOGGER.debug("Putting Pairtree object resource: {}", aPath);

        myS3Client.put(myPairtreeBucket, getPath(aPath), aBuffer, response -> {
            final int statusCode = response.statusCode();

            if (statusCode == OK) {
                future.complete();
            } else {
                if (statusCode == FORBIDDEN) {
                    response.bodyHandler(handler -> {
                        System.out.println(new String(handler.getBytes()));
                    });
                }

                final String status = response.statusMessage();
                future.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, getPath() + SLASH + aPath, status));
            }
        });
    }

    @Override
    public void get(final String aPath, final Handler<AsyncResult<Buffer>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010, getClass().getSimpleName(), ".get()"));

        final Future<Buffer> future = Future.<Buffer>future().setHandler(aHandler);

        LOGGER.debug("Getting Pairtree object resource: {}", aPath);

        myS3Client.get(myPairtreeBucket, getPath(aPath), response -> {
            final int statusCode = response.statusCode();

            if (statusCode == OK) {
                response.bodyHandler(bodyHandlerResult -> {
                    future.complete(Buffer.buffer(bodyHandlerResult.getBytes()));
                });
            } else {
                final String status = response.statusMessage();
                future.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, getPath() + SLASH + aPath, status));
            }
        });
    }

    @Override
    public void find(final String aPath, final Handler<AsyncResult<Boolean>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(MessageCodes.PT_010, getClass().getSimpleName(), ".find()"));

        final Future<Boolean> future = Future.<Boolean>future().setHandler(aHandler);

        LOGGER.debug("Finding Pairtree object resource '{}' in '{}' [{}]", aPath, myPairtreeBucket, getPath(aPath));

        myS3Client.head(myPairtreeBucket, getPath(aPath), response -> {
            final int statusCode = response.statusCode();

            if (statusCode == OK) {
                final String contentLength = response.getHeader("Content-Length");

                try {
                    if (Integer.parseInt(contentLength) > 0) {
                        future.complete(true);
                    } else {
                        future.complete(false);
                    }
                } catch (final NumberFormatException details) {
                    future.fail("Content-Length was not an integer: " + contentLength);
                }
            } else if (statusCode == NOT_FOUND) {
                future.complete(false);
            } else {
                final String status = response.statusMessage();
                future.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, getPath() + SLASH + aPath, status));
            }
        });
    }

}
