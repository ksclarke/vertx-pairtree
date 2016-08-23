
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.MessageCodes.PT_010;
import static info.freelibrary.pairtree.MessageCodes.PT_011;
import static info.freelibrary.pairtree.MessageCodes.PT_012;
import static info.freelibrary.pairtree.MessageCodes.PT_015;
import static info.freelibrary.pairtree.MessageCodes.PT_016;
import static info.freelibrary.pairtree.MessageCodes.PT_017;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import info.freelibrary.pairtree.AbstractPairtree;
import info.freelibrary.pairtree.PairtreeObject;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;

public class S3Pairtree extends AbstractPairtree {

    public static final String AWS_ACCESS_KEY = "AWS_ACCESS_KEY";

    public static final String AWS_SECRET_KEY = "AWS_SECRET_KEY";

    private static final String PATH_SEP = "/";

    private final String myBucket;

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
        this(aVertx, aBucket, aAccessKey, aSecretKey, null);
    }

    /**
     * Creates S3Pairtree using the supplied S3 bucket, access key and secret key.
     *
     * @param aVertx A Vert.x instance with which to instantiate the <code>S3Client</code>
     * @param aBucket An S3 bucket in which to put the Pairtree
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aPairtreePrefix A Pairtree prefix
     */
    public S3Pairtree(final Vertx aVertx, final String aBucket, final String aAccessKey, final String aSecretKey,
            final String aPairtreePrefix) {
        Objects.requireNonNull(StringUtils.trimToNull(aBucket), getI18n(PT_015));
        Objects.requireNonNull(StringUtils.trimToNull(aAccessKey), getI18n(PT_016));
        Objects.requireNonNull(StringUtils.trimToNull(aSecretKey), getI18n(PT_017));

        myS3Client = new S3Client(aVertx, aAccessKey, aSecretKey);
        myBucket = aBucket;

        if (StringUtils.trimToNull(aPairtreePrefix) != null) {
            myPrefix = aPairtreePrefix;
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
        Objects.requireNonNull(aHandler, getI18n(PT_010, getClass().getSimpleName(), ".exists()"));

        final Future<Boolean> future = Future.<Boolean>future().setHandler(aHandler);

        myS3Client.get(myBucket, getVersionFilePath(), getVersionResponse -> {
            if (getVersionResponse.statusCode() != 200) {
                future.fail("Response code: " + getVersionResponse.statusCode() + " [" + getVersionResponse
                        .statusMessage() + "]");
            } else if (hasPrefix()) {
                myS3Client.get(myBucket, getPrefixFilePath(), getPrefixResponse -> {
                    if (getPrefixResponse.statusCode() != 200) {
                        future.fail("Response code: " + getPrefixResponse.statusCode() + " [" + getPrefixResponse
                                .statusMessage() + "]");
                    } else {
                        future.complete(true);
                    }
                });
            } else {
                future.complete(true);
            }
        });
    }

    @Override
    public void create(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(PT_010, getClass().getSimpleName(), ".create()"));

        final String specNote = getI18n(PT_011, PT_VERSION_NUM) + PATH_SEP + getI18n(PT_012);
        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        myS3Client.put(myBucket, getVersionFilePath(), Buffer.buffer(specNote), putVersionResponse -> {
            if (putVersionResponse.statusCode() != 200) {
                future.fail("Response code: " + putVersionResponse.statusCode() + " [" + putVersionResponse
                        .statusMessage() + "]");
            } else if (hasPrefix()) {
                myS3Client.put(myBucket, getPrefixFilePath(), Buffer.buffer(myPrefix), putPrefixResponse -> {
                    if (putPrefixResponse.statusCode() != 200) {
                        future.fail("Response code: " + putPrefixResponse.statusCode() + " [" + putPrefixResponse
                                .statusMessage() + "]");
                    } else {
                        future.complete();
                    }
                });
            } else {
                future.complete();
            }
        });
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void delete(final Handler<AsyncResult<Void>> aHandler) {
        Objects.requireNonNull(aHandler, getI18n(PT_010, getClass().getSimpleName(), ".delete()"));

        final Future<Void> future = Future.<Void>future().setHandler(aHandler);

        myS3Client.list(myBucket, listResponse -> {
            if (listResponse.statusCode() == 200) {
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
                                if (deleteResponse.statusCode() != 204) {
                                    deleteFuture.fail("Response status code: " + deleteResponse.statusCode());
                                } else {
                                    deleteFuture.complete();
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
                future.fail("Response status code: " + listResponse.statusCode());
            }
        });
    }

    @Override
    public String toString() {
        return "s3://" + PATH_SEP + myBucket + PATH_SEP + PAIRTREE_ROOT;
    }

    @Override
    public String getPath() {
        return myBucket;
    }

    @Override
    public String getPrefixFilePath() {
        return PAIRTREE_PREFIX;
    }

    @Override
    public String getVersionFilePath() {
        return getVersionFileName();
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(S3Pairtree.class, BUNDLE_NAME);
    }

}