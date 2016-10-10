
package info.freelibrary.pairtree.s3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import info.freelibrary.pairtree.AbstractPairtreeTest;
import info.freelibrary.pairtree.MessageCodes;
import info.freelibrary.util.IOUtils;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public abstract class AbstractS3IT extends AbstractPairtreeTest {

    protected static final File TEST_FILE = new File("src/test/resources/green.gif");

    protected static String myAccessKey;

    protected static String mySecretKey;

    protected static String myTestBucket;

    protected static byte[] myResource;

    protected AmazonS3Client myS3Client;

    @BeforeClass
    public static void setUpBeforeClass(final TestContext aContext) {
        try {
            myResource = IOUtils.readBytes(new FileInputStream(TEST_FILE));

            myTestBucket = System.getProperty("vertx.pairtree.bucket", "vertx-pairtree-tests");
            myAccessKey = System.getProperty("vertx.pairtree.access_key", "YOUR_ACCESS_KEY");
            mySecretKey = System.getProperty("vertx.pairtree.secret_key", "YOUR_SECRET_KEY");
        } catch (final IOException details) {
            aContext.fail(details.getMessage());
        }
    }

    @Override
    @Before
    public void setUp(final TestContext aContext) {
        super.setUp(aContext);

        if (mySecretKey.equals("YOUR_SECRET_KEY") || myAccessKey.equals("YOUR_ACCESS_KEY")) {
            aContext.fail(getI18n(MessageCodes.PT_DEBUG_049));
        }

        // Initialize the S3 client we use for test set up and tear down
        myS3Client = new AmazonS3Client(new BasicAWSCredentials(myAccessKey, mySecretKey));
    }

    /**
     * Test cleanup deletes everything in the bucket so obviously only use on Pairtree test buckets.
     *
     * @param aContext A test context
     */
    @Override
    @After
    public void tearDown(final TestContext aContext) {
        super.tearDown(aContext);

        // Clean up our test resources in the S3 bucket
        final ObjectListing listing = myS3Client.listObjects(myTestBucket);
        final Iterator<S3ObjectSummary> iterator = listing.getObjectSummaries().iterator();

        while (iterator.hasNext()) {
            try {
                myS3Client.deleteObject(myTestBucket, iterator.next().getKey());
            } catch (final AmazonClientException details) {
                aContext.fail(details);
            }
        }
    }

}
