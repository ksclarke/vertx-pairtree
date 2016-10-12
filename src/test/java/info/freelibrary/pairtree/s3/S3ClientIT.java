
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_045;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_046;
import static java.util.UUID.randomUUID;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.amazonaws.AmazonClientException;

import info.freelibrary.pairtree.HTTP;
import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.OpenOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Integration tests for {@link S3Client}.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientIT extends AbstractS3IT {

    /** The S3 client being used in the tests */
    private S3Client myClient;

    /** A test ID */
    private String myTestID;

    @Override
    @Before
    public void setUp(final TestContext aContext) {
        super.setUp(aContext);

        myTestID = randomUUID().toString();
        myClient = new S3Client(myVertx, myAccessKey, mySecretKey);
    }

    @Override
    @After
    public void tearDown(final TestContext aContext) {
        super.tearDown(aContext);

        myClient.close();
    }

    /**
     * Test for
     * {@link info.freelibrary.pairtree.s3.S3Client#head(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @Test
    public void testHead(final TestContext aContext) {
        final Async async = aContext.async();
        final String s3Key = "green-" + myTestID + ".gif";

        if (createResource(s3Key, aContext, async)) {
            myClient.head(myTestBucket, s3Key, response -> {
                final int statusCode = response.statusCode();

                if (statusCode != 200) {
                    aContext.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, s3Key, response.statusMessage()));
                } else {
                    final String contentLength = response.getHeader(HTTP.CONTENT_LENGTH);

                    aContext.assertNotNull(contentLength);
                    aContext.assertTrue(Integer.parseInt(contentLength) > 0);
                }

                async.complete();
            });
        }
    }

    @Test
    public void testList(final TestContext aContext) {
        final Async async = aContext.async();
        final String[] keys = { "path/to/one-" + myTestID, "path/to/two-" + myTestID, "path/from/one-" + myTestID,
            "path/from/two-" + myTestID };

        if (createResources(keys, aContext, async)) {
            myClient.list(myTestBucket, response -> {
                final int statusCode = response.statusCode();

                if (statusCode == 200) {
                    response.bodyHandler(buffer -> {
                        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

                        saxParserFactory.setNamespaceAware(true);

                        try {
                            final SAXParser saxParser = saxParserFactory.newSAXParser();
                            final XMLReader xmlReader = saxParser.getXMLReader();
                            final ObjectListHandler s3ListHandler = new ObjectListHandler();
                            final List<String> keyList;

                            xmlReader.setContentHandler(s3ListHandler);
                            xmlReader.parse(new InputSource(new StringReader(buffer.toString())));
                            keyList = s3ListHandler.getKeys();

                            for (final String key : keys) {
                                aContext.assertTrue(keyList.contains(key), "Didn't find expected key: " + key);
                            }

                            aContext.assertEquals(keyList.size(), keys.length);
                        } catch (final ParserConfigurationException | SAXException | IOException details) {
                            aContext.fail(details);
                        }

                        async.complete();
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String message = response.statusMessage();

                    aContext.fail(getI18n(PT_DEBUG_045, statusCode, keyValues, message));
                    async.complete();
                }
            });
        }
    }

    @Test
    public void testListWithPrefix(final TestContext aContext) {
        final Async async = aContext.async();
        final String[] keys = { "path/to/one-" + myTestID, "path/to/two-" + myTestID, "path/from/one-" + myTestID,
            "path/from/two-" + myTestID };

        if (createResources(keys, aContext, async)) {
            myClient.list(myTestBucket, "path/from", response -> {
                final int statusCode = response.statusCode();

                if (statusCode == 200) {
                    response.bodyHandler(buffer -> {
                        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

                        saxParserFactory.setNamespaceAware(true);

                        try {
                            final SAXParser saxParser = saxParserFactory.newSAXParser();
                            final XMLReader xmlReader = saxParser.getXMLReader();
                            final ObjectListHandler s3ListHandler = new ObjectListHandler();
                            final List<String> keyList;

                            xmlReader.setContentHandler(s3ListHandler);
                            xmlReader.parse(new InputSource(new StringReader(buffer.toString())));

                            keyList = s3ListHandler.getKeys();

                            aContext.assertEquals(keyList.size(), 2);
                            aContext.assertTrue(keyList.contains(keys[2]));
                            aContext.assertTrue(keyList.contains(keys[3]));
                        } catch (final ParserConfigurationException | SAXException | IOException details) {
                            aContext.fail(details);
                        }

                        async.complete();
                    });
                } else {
                    final String keyValues = StringUtils.toString(keys, '|');
                    final String message = response.statusMessage();

                    aContext.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, keyValues, message));
                    async.complete();
                }
            });
        }
    }

    /**
     * Test for
     * {@link info.freelibrary.pairtree.s3.S3Client#get(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @Test
    public void testGet(final TestContext aContext) {
        final Async async = aContext.async();
        final String s3Key = "green-" + myTestID + ".gif";

        if (createResource(s3Key, aContext, async)) {
            myClient.get(myTestBucket, s3Key, response -> {
                final int statusCode = response.statusCode();

                if (statusCode != 200) {
                    aContext.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, s3Key, response.statusMessage()));
                    async.complete();
                } else {
                    response.bodyHandler(buffer -> {
                        if (buffer.length() != myResource.length) {
                            aContext.fail("Length: " + buffer.length());
                        }

                        async.complete();
                    });
                }
            });
        }
    }

    @Test
    public void testPut(final TestContext aContext) {
        final Async async = aContext.async();
        final String s3Key = "green-" + myTestID + ".gif";

        myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), response -> {
            final int statusCode = response.statusCode();

            if (statusCode != 200) {
                aContext.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, s3Key, response.statusMessage()));
            }

            async.complete();
        });
    }

    @Test
    public void testPutAsyncFile(final TestContext aContext) {
        final Async async = aContext.async();
        final String s3Key = "green-" + myTestID + ".gif";

        myVertx.fileSystem().open(TEST_FILE.getAbsolutePath(), new OpenOptions(), openResult -> {
            if (openResult.succeeded()) {
                myClient.put(myTestBucket, s3Key, openResult.result(), response -> {
                    final int statusCode = response.statusCode();

                    if (statusCode != 200) {
                        aContext.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, s3Key, response.statusMessage()));
                    }

                    async.complete();
                });
            } else {
                aContext.fail(openResult.cause());
                async.complete();
            }
        });
    }

    /**
     * Test for
     * {@link info.freelibrary.pairtree.s3.S3Client#delete(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @Test
    public void testDelete(final TestContext aContext) {
        final Async async = aContext.async();
        final String s3Key = "green-" + myTestID + ".gif";

        if (createResource(s3Key, aContext, async)) {
            myClient.delete(myTestBucket, s3Key, response -> {
                final int statusCode = response.statusCode();

                if (statusCode != 204) {
                    aContext.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, s3Key, response.statusMessage()));
                }

                async.complete();
            });
        }
    }

    /**
     * Test for
     * {@link info.freelibrary.pairtree.s3.S3Client#createPutRequest(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @Test
    public void testCreatePutRequest(final TestContext aContext) {
        final Async async = aContext.async();
        final String s3Key = "green-" + myTestID + ".gif";

        final S3ClientRequest request = myClient.createPutRequest(myTestBucket, s3Key, response -> {
            final int statusCode = response.statusCode();

            if (statusCode != 200) {
                aContext.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, s3Key, response.statusMessage()));
            }

            async.complete();
        });

        request.end(Buffer.buffer(myResource));
    }

    /**
     * Test for
     * {@link info.freelibrary.pairtree.s3.S3Client#createGetRequest(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @Test
    public void testCreateGetRequest(final TestContext aContext) {
        final Async async = aContext.async();
        final String s3Key = "green-" + myTestID + ".gif";

        if (createResource(s3Key, aContext, async)) {
            final S3ClientRequest request = myClient.createGetRequest(myTestBucket, s3Key, response -> {
                final int statusCode = response.statusCode();

                if (statusCode != 200) {
                    aContext.fail(getI18n(MessageCodes.PT_DEBUG_045, statusCode, s3Key, response.statusMessage()));
                    async.complete();
                } else {
                    response.bodyHandler(buffer -> {
                        final byte[] bytes = buffer.getBytes();

                        if (bytes.length != myResource.length) {
                            aContext.fail("Length: " + bytes.length);
                        }

                        async.complete();
                    });
                }
            });

            request.end();
        }
    }

    /**
     * Test for
     * {@link info.freelibrary.pairtree.s3.S3Client#createDeleteRequest(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @Test
    public void testCreateDeleteRequest(final TestContext aContext) {
        final Async async = aContext.async();
        final String s3Key = "green-" + myTestID + ".gif";

        if (createResource(s3Key, aContext, async)) {
            final S3ClientRequest request = myClient.createDeleteRequest(myTestBucket, s3Key, response -> {
                final int statusCode = response.statusCode();

                if (statusCode != 204) {
                    aContext.fail(getI18n(PT_DEBUG_045, statusCode, s3Key, response.statusMessage()));
                }

                async.complete();
            });

            request.end();
        }
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(S3ClientIT.class, BUNDLE_NAME);
    }

    private boolean createResource(final String aResource, final TestContext aContext, final Async aAsync) {
        return createResources(new String[] { aResource }, aContext, aAsync);
    }

    private boolean createResources(final String[] aResourceArray, final TestContext aContext, final Async aAsync) {
        for (final String resource : aResourceArray) {
            try {
                myS3Client.putObject(myTestBucket, resource, TEST_FILE);
            } catch (final AmazonClientException details) {
                LOGGER.error(details.getMessage(), details);

                aContext.fail(getI18n(PT_DEBUG_046, resource));
                aAsync.complete();

                return false;
            }
        }

        return true;
    }

}
