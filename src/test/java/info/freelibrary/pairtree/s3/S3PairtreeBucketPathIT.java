
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.Pairtree.PAIRTREE_PREFIX;
import static info.freelibrary.pairtree.Pairtree.PAIRTREE_VERSION;
import static info.freelibrary.pairtree.Pairtree.PT_VERSION_NUM;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.Pairtree;
import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests for the S3 Pairtree implementation that puts the Pairtree in a subdirectory of the S3 bucket.
 */
@RunWith(VertxUnitRunner.class)
public class S3PairtreeBucketPathIT extends AbstractS3IT {

    private final String BUCKET_PATH = "/path/to/pairtree";

    /** The Pairtree that's being tested */
    private Pairtree myPairtree;

    /**
     * Test setup creates a Pairtree object and puts an expected resource into S3
     *
     * @param aContext A test context
     */
    @Override
    @Before
    public void setUp(final TestContext aContext) {
        super.setUp(aContext);

        LOGGER.debug(MessageCodes.PT_DEBUG_001, "s3:///" + myTestBucket);
        LOGGER.debug("Using AWS region: {}", myRegion.getName());

        final PairtreeFactory factory = new PairtreeFactory(myVertx);

        myPairtree = factory.getPairtree(myTestBucket, BUCKET_PATH, myAccessKey, mySecretKey, myRegion);
    }

    @Test
    public final void testGetObject(final TestContext aContext) {
        final String id = UUID.randomUUID().toString();
        aContext.assertEquals(myPairtree.getObject(id).getID(), id);
    }

    @Test
    public final void testExists(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(createResult -> {
            if (createResult.succeeded()) {
                final boolean prefixFile = myS3Client.doesObjectExist(myTestBucket, myPairtree.getPrefixFilePath());
                final boolean versionFile = myS3Client.doesObjectExist(myTestBucket, myPairtree.getVersionFilePath());

                aContext.assertTrue(versionFile, LOGGER.getMessage(MessageCodes.PT_DEBUG_055));

                if (myPairtree.hasPrefix()) {
                    aContext.assertTrue(prefixFile, LOGGER.getMessage(MessageCodes.PT_DEBUG_054));
                } else {
                    aContext.assertFalse(prefixFile, LOGGER.getMessage(MessageCodes.PT_DEBUG_052));
                }

                myPairtree.exists(existsResults -> {
                    if (!existsResults.succeeded()) {
                        aContext.fail(existsResults.cause());
                    } else {
                        aContext.assertTrue(existsResults.result());
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createResult.cause());
                async.complete();
            }
        });
    }

    @Test
    public final void testCreate(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(result -> {
            if (result.succeeded()) {
                boolean expected = myS3Client.doesObjectExist(myTestBucket, myPairtree.getVersionFilePath());

                aContext.assertTrue(expected, LOGGER.getMessage(MessageCodes.PT_DEBUG_055));

                if (myPairtree.hasPrefix()) {
                    expected = myS3Client.doesObjectExist(myTestBucket, myPairtree.getPrefixFilePath());
                    aContext.assertTrue(expected, LOGGER.getMessage(MessageCodes.PT_DEBUG_054));
                }
            } else {
                aContext.fail(result.cause());
            }

            async.complete();
        });
    }

    @Test
    public final void testDelete(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(createResult -> {
            if (createResult.succeeded()) {
                final boolean prefixFile = myS3Client.doesObjectExist(myTestBucket, myPairtree.getPrefixFilePath());
                final boolean versionFile = myS3Client.doesObjectExist(myTestBucket, myPairtree.getVersionFilePath());

                aContext.assertTrue(versionFile, LOGGER.getMessage(MessageCodes.PT_DEBUG_055));

                if (myPairtree.hasPrefix()) {
                    aContext.assertTrue(prefixFile, LOGGER.getMessage(MessageCodes.PT_DEBUG_054));
                } else {
                    aContext.assertFalse(prefixFile, LOGGER.getMessage(MessageCodes.PT_DEBUG_052));
                }

                // Now delete the Pairtree we just created
                myPairtree.delete(deleteResult -> {
                    if (!deleteResult.succeeded()) {
                        aContext.fail(deleteResult.cause());
                    } else {
                        final String versionFilePath = myPairtree.getVersionFilePath();
                        final boolean vfpExists = myS3Client.doesObjectExist(myTestBucket, versionFilePath);

                        aContext.assertFalse(vfpExists, LOGGER.getMessage(MessageCodes.PT_DEBUG_053));

                        if (myPairtree.hasPrefix()) {
                            final String prefixFilePath = myPairtree.getPrefixFilePath();
                            final boolean pfpExists = myS3Client.doesObjectExist(myTestBucket, prefixFilePath);

                            aContext.assertFalse(pfpExists, LOGGER.getMessage(MessageCodes.PT_DEBUG_052));
                        }
                    }

                    async.complete();
                });
            } else {
                aContext.fail(createResult.cause());
                async.complete();
            }
        });
    }

    @Test
    public final void testToString(final TestContext aContext) {
        aContext.assertEquals("s3:///" + myTestBucket + BUCKET_PATH + "/pairtree_root", myPairtree.toString());
    }

    @Test
    public final void testGetPath(final TestContext aContext) {
        aContext.assertEquals(myTestBucket + BUCKET_PATH, myPairtree.getPath());
    }

    @Test
    public final void testGetPrefixFilePath(final TestContext aContext) {
        aContext.assertEquals(BUCKET_PATH + '/' + PAIRTREE_PREFIX, myPairtree.getPrefixFilePath());
    }

    @Test
    public final void testGetVersionFilePath(final TestContext aContext) {
        final String expected = BUCKET_PATH + '/' + PAIRTREE_VERSION + PT_VERSION_NUM.replace('.', '_');
        aContext.assertEquals(expected, myPairtree.getVersionFilePath());
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(S3PairtreeBucketPathIT.class, BUNDLE_NAME);
    }
}
