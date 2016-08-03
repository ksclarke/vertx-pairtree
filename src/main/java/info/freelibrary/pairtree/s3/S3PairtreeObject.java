/**
 *
 */

package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.MessageCodes.PT_010;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_045;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

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
 * @author Kevin S. Clarke (<a href="mailto:ksclarke@ksclarke.io">ksclarke@ksclarke.io</a>)
 */
public class S3PairtreeObject extends I18nObject implements PairtreeObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3PairtreeObject.class, BUNDLE_NAME);

    private final S3Client myS3Client;

    private final String myPairtreeBucket;

    private final String myPrefix;

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
        final Future<Boolean> future = Future.future();

        if (aHandler != null) {
            future.setHandler(aHandler);

            myS3Client.head(myPairtreeBucket, getPath() + "/README.txt", response -> {
                final int statusCode = response.statusCode();

                if (statusCode != 200) {
                    final String statusMessage = response.statusMessage();
                    future.fail(getI18n(PT_DEBUG_045, statusCode, getPath() + "/README.txt", statusMessage));
                } else {
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
                }
            });
        } else {
            throw new NullPointerException(getI18n(PT_010, getClass().getSimpleName(), ".exists()"));
        }
    }

    @Override
    public void create(final Handler<AsyncResult<Void>> aHandler) {
        final Future<Void> future = Future.future();

        if (aHandler != null) {
            future.setHandler(aHandler);

            myS3Client.put(myPairtreeBucket, getPath() + "/README.txt", Buffer.buffer(myID), response -> {
                final int statusCode = response.statusCode();

                if (statusCode != 200) {
                    final String statusMessage = response.statusMessage();
                    future.fail(getI18n(PT_DEBUG_045, statusCode, getPath() + "/README.txt", statusMessage));
                } else {
                    future.complete();
                }
            });
        } else {
            throw new NullPointerException(getI18n(PT_010, getClass().getSimpleName(), ".create()"));
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void delete(final Handler<AsyncResult<Void>> aHandler) {
        final Future<Void> future = Future.future();

        if (aHandler != null) {
            future.setHandler(aHandler);

            myS3Client.list(myPairtreeBucket, getPath(), listResponse -> {
                final int statusCode = listResponse.statusCode();

                if (statusCode != 200) {
                    future.fail(getI18n(PT_DEBUG_045, statusCode, getPath(), listResponse.statusMessage()));
                } else {
                    listResponse.bodyHandler(bodyHandler -> {
                        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

                        saxParserFactory.setNamespaceAware(true);

                        try {
                            final SAXParser saxParser = saxParserFactory.newSAXParser();
                            final XMLReader xmlReader = saxParser.getXMLReader();
                            final ObjectListHandler s3ListHandler = new ObjectListHandler();
                            final List<Future> futures = new ArrayList<Future>();
                            final List<String> keyList;

                            xmlReader.setContentHandler(s3ListHandler);
                            xmlReader.parse(new InputSource(new StringReader(bodyHandler.toString())));
                            keyList = s3ListHandler.getKeys();

                            for (final String key : keyList) {
                                final Future<Void> keyFuture = Future.future();

                                futures.add(keyFuture);

                                myS3Client.delete(myPairtreeBucket, key, deleteResponse -> {
                                    final int deleteStatusCode = deleteResponse.statusCode();

                                    if (deleteStatusCode != 204) {
                                        final String statusMessage = deleteResponse.statusMessage();
                                        keyFuture.fail(getI18n(PT_DEBUG_045, deleteStatusCode, key, statusMessage));
                                    } else {
                                        keyFuture.complete();
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
                }
            });
        } else {
            throw new NullPointerException(getI18n(PT_010, getClass().getSimpleName(), ".delete()"));
        }
    }

    @Override
    public String getID() {
        return myPrefix == null ? myID : myPrefix + "/" + myID;
    }

    @Override
    public String getPath() {
        return PairtreeUtils.mapToPtPath(myID) + "/" + PairtreeUtils.encodeID(myID);
    }

    @Override
    public String getPath(final String aResourcePath) {
        return aResourcePath.startsWith("/") ? getPath() + aResourcePath : getPath() + "/" + aResourcePath;
    }

    @Override
    public void put(final String aPath, final Buffer aBuffer, final Handler<AsyncResult<Void>> aHandler) {
        final Future<Void> future = Future.future();

        if (aHandler != null) {
            future.setHandler(aHandler);

            LOGGER.debug("Putting Pairtree object resource: {}", aPath);

            myS3Client.put(myPairtreeBucket, getPath() + "/" + aPath, aBuffer, response -> {
                final int statusCode = response.statusCode();

                if (statusCode == 200) {
                    future.complete();
                } else {
                    future.fail(getI18n(PT_DEBUG_045, statusCode, getPath() + "/" + aPath, response.statusMessage()));
                }
            });
        } else {
            throw new NullPointerException(getI18n(PT_010, getClass().getSimpleName(), ".put()"));
        }
    }

    @Override
    public void get(final String aPath, final Handler<AsyncResult<Buffer>> aHandler) {
        final Future<Buffer> future = Future.future();

        if (aHandler != null) {
            future.setHandler(aHandler);

            LOGGER.debug("Getting Pairtree object resource: {}", aPath);

            myS3Client.get(myPairtreeBucket, getPath() + "/" + aPath, response -> {
                final int statusCode = response.statusCode();

                if (statusCode == 200) {
                    response.bodyHandler(bodyHandlerResult -> {
                        future.complete(Buffer.buffer(bodyHandlerResult.getBytes()));
                    });
                } else {
                    future.fail(getI18n(PT_DEBUG_045, statusCode, getPath() + "/" + aPath, response.statusMessage()));
                }
            });
        } else {
            throw new NullPointerException(getI18n(PT_010, getClass().getSimpleName(), ".get()"));
        }

    }

    @Override
    public void find(final String aPath, final Handler<AsyncResult<Boolean>> aHandler) {
        final Future<Boolean> future = Future.future();

        if (aHandler != null) {
            future.setHandler(aHandler);

            LOGGER.debug("Finding Pairtree object resource: {}", aPath);

            myS3Client.head(myPairtreeBucket, getPath() + "/" + aPath, response -> {
                final int statusCode = response.statusCode();

                if (statusCode != 200) {
                    future.fail(getI18n(PT_DEBUG_045, statusCode, getPath() + "/" + aPath, response.statusMessage()));
                } else {
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
                }
            });
        } else {
            throw new NullPointerException(getI18n(PT_010, getClass().getSimpleName(), ".find()"));
        }
    }

}
