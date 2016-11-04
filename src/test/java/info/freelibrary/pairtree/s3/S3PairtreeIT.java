
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl.S3Bucket;
import static info.freelibrary.pairtree.PairtreeRoot.PAIRTREE_PREFIX;
import static info.freelibrary.pairtree.PairtreeRoot.PAIRTREE_VERSION;
import static info.freelibrary.pairtree.PairtreeRoot.PT_VERSION_NUM;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.pairtree.PairtreeRoot;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests for the S3 Pairtree implementation.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
@RunWith(VertxUnitRunner.class)
public class S3PairtreeIT extends AbstractS3IT {

    /** The Pairtree that's being tested */
    private PairtreeRoot myPairtree;

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
        LOGGER.debug("Using AWS region: {}", myRegion);

        final PairtreeFactory factory = PairtreeFactory.getFactory(myVertx, S3Bucket);

        myPairtree = factory.getPairtree(myTestBucket, myAccessKey, mySecretKey, myRegion);
    }

    @Test
    public final void testGetObject(final TestContext aContext) {
        aContext.assertEquals(myPairtree.getObject("asdf").getID(), "asdf");
    }

    @Test
    public final void testExists(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(createResult -> {
            if (createResult.succeeded()) {
                final boolean prefixFile = myS3Client.doesObjectExist(myTestBucket, myPairtree.getPrefixFilePath());
                final boolean versionFile = myS3Client.doesObjectExist(myTestBucket, myPairtree.getVersionFilePath());

                aContext.assertTrue(versionFile, MessageCodes.PT_DEBUG_055);

                if (myPairtree.hasPrefix()) {
                    aContext.assertTrue(prefixFile, MessageCodes.PT_DEBUG_054);
                } else {
                    aContext.assertFalse(prefixFile, MessageCodes.PT_DEBUG_052);
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
                aContext.assertTrue(myS3Client.doesObjectExist(myTestBucket, myPairtree.getVersionFilePath()));

                if (myPairtree.hasPrefix()) {
                    aContext.assertTrue(myS3Client.doesObjectExist(myTestBucket, myPairtree.getPrefixFilePath()));
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

                aContext.assertTrue(versionFile, MessageCodes.PT_DEBUG_055);

                if (myPairtree.hasPrefix()) {
                    aContext.assertTrue(prefixFile, MessageCodes.PT_DEBUG_054);
                } else {
                    aContext.assertFalse(prefixFile, MessageCodes.PT_DEBUG_052);
                }

                // Now delete the Pairtree we just created
                myPairtree.delete(deleteResult -> {
                    if (!deleteResult.succeeded()) {
                        aContext.fail(deleteResult.cause());
                    } else {
                        final String versionFilePath = myPairtree.getVersionFilePath();
                        final boolean vfpExists = myS3Client.doesObjectExist(myTestBucket, versionFilePath);

                        aContext.assertFalse(vfpExists, MessageCodes.PT_DEBUG_053);

                        if (myPairtree.hasPrefix()) {
                            final String prefixFilePath = myPairtree.getPrefixFilePath();
                            final boolean pfpExists = myS3Client.doesObjectExist(myTestBucket, prefixFilePath);

                            aContext.assertFalse(pfpExists, MessageCodes.PT_DEBUG_052);
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
        aContext.assertEquals("s3:///" + myTestBucket + "/pairtree_root", myPairtree.toString());
    }

    @Test
    public final void testGetPath(final TestContext aContext) {
        aContext.assertEquals(myTestBucket, myPairtree.getPath());
    }

    @Test
    public final void testGetPrefixFilePath(final TestContext aContext) {
        aContext.assertEquals(PAIRTREE_PREFIX, myPairtree.getPrefixFilePath());
    }

    @Test
    public final void testGetVersionFilePath(final TestContext aContext) {
        aContext.assertEquals(PAIRTREE_VERSION + PT_VERSION_NUM.replace('.', '_'), myPairtree.getVersionFilePath());
    }

    @Override
    public Logger getLogger() {
        return LoggerFactory.getLogger(S3PairtreeIT.class, BUNDLE_NAME);
    }
}
