
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

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Integration tests for {@link S3Client}.
 *
 * @author Kevin S. Clarke (<a href="mailto:ksclarke@ksclarke.io">ksclarke@ksclarke.io</a>)
 */
@RunWith(VertxUnitRunner.class)
public class S3ClientIT extends AbstractS3IT {

    private S3Client myClient;

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

        if (createResource(s3Key, aContext)) {
            myClient.head(myTestBucket, s3Key, headResponse -> {
                final int statusCode = headResponse.statusCode();

                if (statusCode != 200) {
                    aContext.fail(getI18n(PT_DEBUG_045, statusCode, s3Key, headResponse.statusMessage()));
                } else {
                    final String contentLength = headResponse.getHeader("Content-Length");

                    aContext.assertNotNull(contentLength);
                    aContext.assertTrue(Integer.parseInt(contentLength) > 0);
                }

                async.complete();
            });
        } else {
            async.complete();
        }
    }

    @Test
    public void testList(final TestContext aContext) {
        final Async async = aContext.async();
        final String[] keys = { "path/to/one-" + myTestID, "path/to/two-" + myTestID, "path/from/one-" + myTestID,
            "path/from/two-" + myTestID };

        if (createResources(keys, aContext)) {
            myClient.list(myTestBucket, listResponse -> {
                final int statusCode = listResponse.statusCode();

                if (statusCode == 200) {
                    listResponse.bodyHandler(buffer -> {
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
                    final String message = listResponse.statusMessage();

                    aContext.fail(getI18n(PT_DEBUG_045, statusCode, keyValues, message));
                    async.complete();
                }
            });
        } else {
            async.complete();
        }
    }

    @Test
    public void testListWithPrefix(final TestContext aContext) {
        final Async async = aContext.async();
        final String[] keys = { "path/to/one-" + myTestID, "path/to/two-" + myTestID, "path/from/one-" + myTestID,
            "path/from/two-" + myTestID };

        if (createResources(keys, aContext)) {
            myClient.list(myTestBucket, "path/from", listResponse -> {
                final int statusCode = listResponse.statusCode();

                if (statusCode == 200) {
                    listResponse.bodyHandler(buffer -> {
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
                    final String message = listResponse.statusMessage();

                    aContext.fail(getI18n(PT_DEBUG_045, statusCode, keyValues, message));
                    async.complete();
                }
            });
        } else {
            async.complete();
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

        if (createResource(s3Key, aContext)) {
            myClient.get(myTestBucket, s3Key, getResponse -> {
                final int statusCode = getResponse.statusCode();

                if (statusCode != 200) {
                    aContext.fail(getI18n(PT_DEBUG_045, statusCode, s3Key, getResponse.statusMessage()));
                    async.complete();
                } else {
                    getResponse.bodyHandler(buffer -> {
                        if (buffer.length() != myResource.length) {
                            aContext.fail("Length: " + buffer.length());
                        }

                        async.complete();
                    });
                }
            });
        } else {
            async.complete();
        }
    }

    /**
     * Test for
     * {@link info.freelibrary.pairtree.s3.S3Client#put(java.lang.String, java.lang.String, io.vertx.core.buffer.Buffer, io.vertx.core.Handler)}
     */
    @Test
    public void testPut(final TestContext aContext) {
        final Async async = aContext.async();
        final String s3Key = "green-" + myTestID + ".gif";

        if (createResource(s3Key, aContext)) {
            myClient.put(myTestBucket, s3Key, Buffer.buffer(myResource), putResponse -> {
                final int statusCode = putResponse.statusCode();

                if (statusCode != 200) {
                    aContext.fail(getI18n(PT_DEBUG_045, statusCode, s3Key, putResponse.statusMessage()));
                }

                async.complete();
            });
        } else {
            async.complete();
        }
    }

    /**
     * Test for
     * {@link info.freelibrary.pairtree.s3.S3Client#delete(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @Test
    public void testDelete(final TestContext aContext) {
        final Async async = aContext.async();
        final String s3Key = "green-" + myTestID + ".gif";

        if (createResource(s3Key, aContext)) {
            myClient.delete(myTestBucket, s3Key, deleteResponse -> {
                final int statusCode = deleteResponse.statusCode();

                if (statusCode != 204) {
                    aContext.fail(getI18n(PT_DEBUG_045, statusCode, s3Key, deleteResponse.statusMessage()));
                }

                async.complete();
            });
        } else {
            async.complete();
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

        if (createResource(s3Key, aContext)) {
            final S3ClientRequest request = myClient.createPutRequest(myTestBucket, s3Key, putResponse -> {
                final int statusCode = putResponse.statusCode();

                if (statusCode != 200) {
                    aContext.fail(getI18n(PT_DEBUG_045, statusCode, s3Key, putResponse.statusMessage()));
                }

                async.complete();
            });

            request.end(Buffer.buffer(myResource));
        } else {
            async.complete();
        }
    }

    /**
     * Test for
     * {@link info.freelibrary.pairtree.s3.S3Client#createGetRequest(java.lang.String, java.lang.String, io.vertx.core.Handler)}
     */
    @Test
    public void testCreateGetRequest(final TestContext aContext) {
        final Async async = aContext.async();
        final String s3Key = "green-" + myTestID + ".gif";

        if (createResource(s3Key, aContext)) {
            final S3ClientRequest request = myClient.createGetRequest(myTestBucket, s3Key, getResponse -> {
                final int statusCode = getResponse.statusCode();

                if (statusCode != 200) {
                    aContext.fail(getI18n(PT_DEBUG_045, statusCode, s3Key, getResponse.statusMessage()));
                    async.complete();
                } else {
                    getResponse.bodyHandler(buffer -> {
                        final byte[] bytes = buffer.getBytes();

                        if (bytes.length != myResource.length) {
                            aContext.fail("Length: " + bytes.length);
                        }

                        async.complete();
                    });
                }
            });

            request.end();
        } else {
            async.complete();
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

        if (createResource(s3Key, aContext)) {
            final S3ClientRequest request = myClient.createDeleteRequest(myTestBucket, s3Key, deleteResponse -> {
                final int statusCode = deleteResponse.statusCode();

                if (statusCode != 204) {
                    aContext.fail(getI18n(PT_DEBUG_045, statusCode, s3Key, deleteResponse.statusMessage()));
                }

                async.complete();
            });

            request.end();
        } else {
            async.complete();
        }
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(S3ClientIT.class, BUNDLE_NAME);
    }

    private boolean createResource(final String aResource, final TestContext aContext) {
        return createResources(new String[] { aResource }, aContext);
    }

    private boolean createResources(final String[] aResourceArray, final TestContext aContext) {
        for (final String resource : aResourceArray) {
            try {
                myS3Client.putObject(myTestBucket, resource, TEST_FILE);
            } catch (final AmazonClientException details) {
                aContext.fail(getI18n(PT_DEBUG_046, resource));
                LOGGER.error(details.getMessage(), details);
                return false;
            }
        }

        return true;
    }

}
