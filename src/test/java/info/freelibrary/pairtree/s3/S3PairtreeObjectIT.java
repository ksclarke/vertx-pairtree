
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.Pairtree.PAIRTREE_ROOT;
import static java.util.UUID.randomUUID;

import java.util.StringJoiner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.AmazonClientException;

import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.Pairtree;
import info.freelibrary.pairtree.PairtreeException;
import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.pairtree.PairtreeUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests for the <code>S3PairtreeObject</code>.
 */
@RunWith(VertxUnitRunner.class)
public class S3PairtreeObjectIT extends AbstractS3IT {

    /** A path for a test object */
    private static final String GREEN_GIF = "a/b/green.gif";

    /** A path for a secondary test object */
    private static final String GREEN_BLUE_GIF = "green+blue.gif";

    private static final String SLASH = "/";

    private static final String S3 = "s3:///";

    private static final String ARK = "ark:/99999/88888888";

    /** The Pairtree being tested */
    private Pairtree myPairtree;

    /** The ID for the object being tested */
    private String myUID;

    /** The S3 path for the object being tested */
    private String myS3Path;

    /**
     * Test setup creates a Pairtree object and puts an expected resource into S3
     *
     * @param aContext A test context
     */
    @Override
    @Before
    public void setUp(final TestContext aContext) {
        super.setUp(aContext);

        getLogger().debug(MessageCodes.PT_DEBUG_001, S3 + myTestBucket);

        final PairtreeFactory factory = new PairtreeFactory(myVertx);

        myPairtree = factory.getPairtree(myTestBucket, myAccessKey, mySecretKey, myRegion);

        // Create a test ID for each test run
        myUID = randomUUID().toString();
        myS3Path = PAIRTREE_ROOT + SLASH + PairtreeUtils.mapToPtPath(myUID) + SLASH + myUID;
    }

    @Test
    public final void testExists(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();

        createTestS3PairtreeObject(createResult -> {
            if (createResult.succeeded()) {
                myPairtree.getObject(myUID).exists(existsResult -> {
                    if (!existsResult.succeeded()) {
                        aContext.fail(existsResult.cause());
                    }
                });
            } else {
                aContext.fail(createResult.cause());
            }

            async.complete();
        }, myTestBucket, myAccessKey, mySecretKey, myRegion, myUID);
    }

    @Test
    public final void testCreate(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.getObject(myUID).create(result -> {
            if (!result.succeeded()) {
                aContext.fail(result.cause());
            }

            async.complete();
        });
    }

    @Test
    public final void testCreateWithPlus(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.getObject(ARK).create(result -> {
            if (result.succeeded()) {
                final String ptPath = PairtreeUtils.mapToPtPath(ARK);
                final StringJoiner objectPath = new StringJoiner(SLASH);

                objectPath.add(PAIRTREE_ROOT).add(ptPath).add("ark+=99999=88888888/README.txt");

                if (!myS3Client.doesObjectExist(myTestBucket, objectPath.toString())) {
                    aContext.fail(getLogger().getMessage(MessageCodes.PT_DEBUG_050));
                }
            } else {
                aContext.fail(result.cause());
            }

            async.complete();
        });
    }

    @Test
    public final void testDelete(final TestContext aContext) throws PairtreeException {
        final Async async = aContext.async();

        createTestS3PairtreeObject(createResult -> {
            if (createResult.succeeded()) {
                myPairtree.getObject(myUID).delete(deleteResult -> {
                    if (!deleteResult.succeeded()) {
                        aContext.fail(deleteResult.cause());
                    }
                });
            } else {
                aContext.fail(createResult.cause());
            }

            async.complete();
        }, myTestBucket, myAccessKey, mySecretKey, myRegion, myUID);
    }

    @Test
    public final void testGetID(final TestContext aContext) {
        aContext.assertEquals(myUID, myPairtree.getObject(myUID).getID());
    }

    @Test
    public final void testGetPath(final TestContext aContext) {
        aContext.assertEquals(myS3Path, myPairtree.getObject(myUID).getPath());
    }

    @Test
    public final void testGetPathString(final TestContext aContext) {
        aContext.assertEquals(myS3Path + SLASH + GREEN_GIF, myPairtree.getObject(myUID).getPath(GREEN_GIF));
    }

    @Test
    public final void testGetPathStringStartsWithSlash(final TestContext aContext) {
        aContext.assertEquals(myS3Path + SLASH + GREEN_GIF, myPairtree.getObject(myUID).getPath(GREEN_GIF));
    }

    @Test
    public final void testGetPathStringWithPlus(final TestContext aContext) {
        // The S3 path has a URL encoded '+' because of this S3 bug that will probably never be fixed:
        // https://forums.aws.amazon.com/thread.jspa?threadID=55746
        // It appears normal in S3 or in the resource name once it's copied down to a file system
        aContext.assertEquals(myS3Path + SLASH + GREEN_BLUE_GIF, myPairtree.getObject(myUID).getPath(GREEN_BLUE_GIF)
                .replace("%2B", "+"));
    }

    @Test
    public final void testPut(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.getObject(myUID).put(GREEN_GIF, Buffer.buffer(myResource), result -> {
            if (!result.succeeded()) {
                aContext.fail(result.cause());
            }

            async.complete();
        });
    }

    @Test
    public final void testPutWithPlus(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.getObject(myUID).put("ark+=99999=99999999.gif", Buffer.buffer(myResource), result -> {
            if (result.succeeded()) {
                if (!myS3Client.doesObjectExist(myTestBucket, myS3Path + "/ark+=99999=99999999.gif")) {
                    aContext.fail(getLogger().getMessage(MessageCodes.PT_DEBUG_051));
                }
            } else {
                aContext.fail(result.cause());
            }

            async.complete();
        });
    }

    @Test
    public final void testGet(final TestContext aContext) {
        final Async async = aContext.async();

        try {
            myS3Client.putObject(myTestBucket, myS3Path + SLASH + GREEN_GIF, TEST_FILE);

            myPairtree.getObject(myUID).get(GREEN_GIF, result -> {
                if (!result.succeeded()) {
                    aContext.fail(result.cause());
                }

                async.complete();
            });
        } catch (final AmazonClientException details) {
            aContext.fail(details);
            async.complete();
        }
    }

    @Test
    public final void testGetWithPlus(final TestContext aContext) {
        final Async async = aContext.async();

        try {
            myS3Client.putObject(myTestBucket, myS3Path + SLASH + GREEN_BLUE_GIF, TEST_FILE);

            myPairtree.getObject(myUID).get(GREEN_BLUE_GIF, result -> {
                if (!result.succeeded()) {
                    aContext.fail(result.cause());
                }

                async.complete();
            });
        } catch (final AmazonClientException details) {
            aContext.fail(details);
            async.complete();
        }
    }

    @Test
    public final void testFindWithPlus(final TestContext aContext) {
        final Async async = aContext.async();

        try {
            myS3Client.putObject(myTestBucket, myS3Path + SLASH + GREEN_BLUE_GIF, TEST_FILE);

            myPairtree.getObject(myUID).find(GREEN_BLUE_GIF, result -> {
                if (!result.succeeded()) {
                    aContext.fail(result.cause());
                }

                async.complete();
            });
        } catch (final AmazonClientException details) {
            aContext.fail(details);
            async.complete();
        }
    }

    @Test
    public final void testFind(final TestContext aContext) {
        final Async async = aContext.async();

        try {
            myS3Client.putObject(myTestBucket, myS3Path + SLASH + GREEN_GIF, TEST_FILE);

            myPairtree.getObject(myUID).find(GREEN_GIF, result -> {
                if (!result.succeeded()) {
                    aContext.fail(result.cause());
                }

                async.complete();
            });
        } catch (final AmazonClientException details) {
            aContext.fail(details);
            async.complete();
        }
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(S3PairtreeObjectIT.class, BUNDLE_NAME);
    }

}
