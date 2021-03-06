
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.Pairtree.PAIRTREE_PREFIX;
import static info.freelibrary.pairtree.Pairtree.PAIRTREE_VERSION;
import static info.freelibrary.pairtree.Pairtree.PT_VERSION_NUM;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.pairtree.Pairtree;
import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.vertx.s3.S3Client;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Tests for the S3 Pairtree implementation.
 */
@RunWith(VertxUnitRunner.class)
public class S3PairtreeIT extends AbstractS3IT {

    private static final String S3 = "s3:///";

    private static final String ASDF = "asdf";

    private static final String MY_PATH = "mypath";

    private static final String PREFIX = "prefix";

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

        getLogger().debug(MessageCodes.PT_DEBUG_001, S3 + myTestBucket);
        getLogger().debug("Using AWS region: {}", myRegion.getName());

        final PairtreeFactory factory = new PairtreeFactory(myVertx);

        myPairtree = factory.getPairtree(myTestBucket, myAccessKey, mySecretKey, myRegion);
    }

    @Test
    public final void testGetObject(final TestContext aContext) {
        aContext.assertEquals(myPairtree.getObject(ASDF).getID(), ASDF);
    }

    @Test
    public final void testConstructor1(final TestContext aContext) {
        myPairtree = new PairtreeFactory(myVertx).getPairtree(myTestBucket, myAccessKey, mySecretKey);
    }

    @Test
    public final void testConstructor2(final TestContext aContext) {
        myPairtree = new PairtreeFactory(myVertx).getPairtree(myTestBucket, MY_PATH, myAccessKey, mySecretKey);
    }

    @Test
    public final void testConstructor3(final TestContext aContext) {
        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(PREFIX, myTestBucket, MY_PATH, myAccessKey,
                mySecretKey);
    }

    @Test
    public final void testConstructor4(final TestContext aContext) {
        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(PREFIX, myTestBucket, myAccessKey, mySecretKey);
    }

    @Test
    public final void testConstructor5(final TestContext aContext) {
        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(PREFIX, myTestBucket, myAccessKey, mySecretKey,
                myRegion);
    }

    @Test
    public final void testConstructor6(final TestContext aContext) {
        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(PREFIX, myTestBucket, MY_PATH, myAccessKey,
                mySecretKey, myRegion);
    }

    @Test
    public final void testExists(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(createResult -> {
            if (createResult.succeeded()) {
                final boolean prefixFile = myS3Client.doesObjectExist(myTestBucket, myPairtree.getPrefixFilePath());
                final boolean versionFile = myS3Client.doesObjectExist(myTestBucket, myPairtree.getVersionFilePath());

                aContext.assertTrue(versionFile, getLogger().getMessage(MessageCodes.PT_DEBUG_055));

                if (myPairtree.hasPrefix()) {
                    aContext.assertTrue(prefixFile, getLogger().getMessage(MessageCodes.PT_DEBUG_054));
                } else {
                    aContext.assertFalse(prefixFile, getLogger().getMessage(MessageCodes.PT_DEBUG_052));
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
    public final void testGetS3Client(final TestContext aContext) {
        final Async async = aContext.async();
        final S3Client s3Client = ((S3Pairtree) myPairtree).getS3Client();

        aContext.assertNotNull(s3Client);

        async.complete();
    }

    @Test
    public final void testExistedPrefixed(final TestContext aContext) {
        final Async async = aContext.async();

        // Create a prefixed Pairtree
        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(PREFIX, myTestBucket, myAccessKey, mySecretKey);

        myPairtree.create(createHandler -> {
            if (createHandler.succeeded()) {
                myPairtree.exists(existsHandler -> {
                    if (existsHandler.succeeded()) {
                        if (existsHandler.result()) {
                            async.complete();
                        } else {
                            aContext.fail(getLogger().getMessage(MessageCodes.PT_DEBUG_013, myPairtree.getPath()));
                        }
                    } else {
                        aContext.fail(existsHandler.cause());
                    }
                });
            } else {
                aContext.fail(createHandler.cause());
            }
        });
    }

    @Test
    public final void testCreatePrefixed(final TestContext aContext) {
        final Async async = aContext.async();

        // Create a prefixed Pairtree
        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(PREFIX, myTestBucket, myAccessKey, mySecretKey);

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
    public final void testGetObjects(final TestContext aContext) {
        final Async async = aContext.async();
        final List<String> ids = Arrays.asList(new String[] { UUID.randomUUID().toString(), UUID.randomUUID()
                .toString() });

        myPairtree.create(result -> {
            if (result.succeeded()) {
                aContext.assertEquals(2, myPairtree.getObjects(ids).size());
                async.complete();
            } else {
                aContext.fail(result.cause());
            }
        });
    }

    @Test
    public final void testDelete(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.create(createResult -> {
            if (createResult.succeeded()) {
                final boolean prefixFile = myS3Client.doesObjectExist(myTestBucket, myPairtree.getPrefixFilePath());
                final boolean versionFile = myS3Client.doesObjectExist(myTestBucket, myPairtree.getVersionFilePath());

                aContext.assertTrue(versionFile, getLogger().getMessage(MessageCodes.PT_DEBUG_055));

                if (myPairtree.hasPrefix()) {
                    aContext.assertTrue(prefixFile, getLogger().getMessage(MessageCodes.PT_DEBUG_054));
                } else {
                    aContext.assertFalse(prefixFile, getLogger().getMessage(MessageCodes.PT_DEBUG_052));
                }

                // Now delete the Pairtree we just created
                myPairtree.delete(deleteResult -> {
                    if (!deleteResult.succeeded()) {
                        aContext.fail(deleteResult.cause());
                    } else {
                        final String versionFilePath = myPairtree.getVersionFilePath();
                        final boolean vfpExists = myS3Client.doesObjectExist(myTestBucket, versionFilePath);

                        aContext.assertFalse(vfpExists, getLogger().getMessage(MessageCodes.PT_DEBUG_053));

                        if (myPairtree.hasPrefix()) {
                            final String prefixFilePath = myPairtree.getPrefixFilePath();
                            final boolean pfpExists = myS3Client.doesObjectExist(myTestBucket, prefixFilePath);

                            aContext.assertFalse(pfpExists, getLogger().getMessage(MessageCodes.PT_DEBUG_052));
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
        aContext.assertEquals(S3 + myTestBucket + "/pairtree_root", myPairtree.toString());
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
