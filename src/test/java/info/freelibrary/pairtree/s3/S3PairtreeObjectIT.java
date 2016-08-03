
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl.S3Bucket;
import static java.util.UUID.randomUUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.AmazonClientException;

import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.pairtree.PairtreeRoot;
import info.freelibrary.pairtree.PairtreeUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class S3PairtreeObjectIT extends AbstractS3IT {

    private PairtreeRoot myPairtree;

    private String myUID;

    /**
     * Test setup creates a Pairtree object and puts an expected resource into S3
     *
     * @param aContext A test context
     */
    @Override
    @Before
    public void setUp(final TestContext aContext) {
        super.setUp(aContext);

        // Initialize our S3-backed Pairtree
        LOGGER.debug("Initializing S3-backed Pairtree");
        myPairtree = PairtreeFactory.getFactory(myVertx, S3Bucket).getPairtree(myTestBucket, myAccessKey,
                mySecretKey);

        // Create a test ID for each test run
        myUID = randomUUID().toString();
    }

    @Test
    public final void testExists(final TestContext aContext) {
        final Async async = aContext.async();

        createTestPairtreeObject(S3Bucket, createResult -> {
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
        }, myTestBucket, myAccessKey, mySecretKey, myUID);
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
    public final void testDelete(final TestContext aContext) {
        final Async async = aContext.async();

        createTestPairtreeObject(S3Bucket, createResult -> {
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
        }, myTestBucket, myAccessKey, mySecretKey, myUID);
    }

    @Test
    public final void testGetID(final TestContext aContext) {
        aContext.assertEquals(myUID, myPairtree.getObject(myUID).getID());
    }

    @Test
    public final void testGetPath(final TestContext aContext) {
        final String expectedS3Path = PairtreeUtils.mapToPtPath(myUID) + "/" + myUID;
        aContext.assertEquals(expectedS3Path, myPairtree.getObject(myUID).getPath());
    }

    @Test
    public final void testGetPathString(final TestContext aContext) {
        final String expectedS3Path = PairtreeUtils.mapToPtPath(myUID) + "/" + myUID + "/a/b/green.gif";
        aContext.assertEquals(expectedS3Path, myPairtree.getObject(myUID).getPath("a/b/green.gif"));
    }

    @Test
    public final void testGetPathStringStartsWithSlash(final TestContext aContext) {
        final String expectedS3Path = PairtreeUtils.mapToPtPath(myUID) + "/" + myUID + "/a/b/green.gif";
        aContext.assertEquals(expectedS3Path, myPairtree.getObject(myUID).getPath("/a/b/green.gif"));
    }

    @Test
    public final void testPut(final TestContext aContext) {
        final Async async = aContext.async();

        myPairtree.getObject(myUID).put("a/b/green.gif", Buffer.buffer(myResource), result -> {
            if (!result.succeeded()) {
                aContext.fail(result.cause());
            }

            async.complete();
        });
    }

    @Test
    public final void testGet(final TestContext aContext) {
        final Async async = aContext.async();

        try {
            final String s3Path = PairtreeUtils.mapToPtPath(myUID) + "/" + myUID;

            myS3Client.putObject(myTestBucket, s3Path + "/a/b/green.gif", TEST_FILE);

            myPairtree.getObject(myUID).get("a/b/green.gif", result -> {
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
            final String s3Path = PairtreeUtils.mapToPtPath(myUID) + "/" + myUID;

            myS3Client.putObject(myTestBucket, s3Path + "/a/b/green.gif", TEST_FILE);

            myPairtree.getObject(myUID).find("a/b/green.gif", result -> {
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
