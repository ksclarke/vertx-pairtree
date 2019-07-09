
package info.freelibrary.pairtree;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

import info.freelibrary.pairtree.s3.S3Profile;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Test of the <code>PairtreeFactory</code>.
 */
@RunWith(VertxUnitRunner.class)
public class PairtreeFactoryIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PairtreeFactoryIT.class, Constants.BUNDLE_NAME);

    private static final Path CREDS_FILE_PATH = Paths.get(System.getProperty("user.home"), ".aws/credentials");

    /** The test profile used for testing */
    private static final String TEST_PROFILE = "vertx-s3";

    private static final String AWS_PROFILE = "AWS_PROFILE";

    private static final String PT_PREFIX = "my_prefix";

    /** The connection to the Vertx framework */
    private Vertx myVertx;

    /** The Pairtree being tested */
    private Pairtree myPairtree;

    /** The S3 bucket for testing */
    private String myTestBucket;

    /** The S3 region for testing */
    private Region myRegion;

    /**
     * Setup for the tests.
     */
    @Before
    public void setUp(final TestContext aContext) throws Exception {
        final String endpoint = StringUtils.trimToNull(System.getProperty("vertx.s3.region"));

        myVertx = Vertx.vertx(new VertxOptions());
        myTestBucket = System.getProperty("vertx.s3.bucket", "vertx-pairtree-tests");

        // We use "us-east-1" as the default region
        if (endpoint != null) {
            myRegion = RegionUtils.getRegion(endpoint);
        } else {
            myRegion = RegionUtils.getRegion("us-east-1");
        }
    }

    @Test
    public void testGetPairtreeS3Profile(final TestContext aContext) throws PairtreeException {
        checkCredentialsFile();

        myPairtree = new PairtreeFactory(new S3Profile(TEST_PROFILE)).getPairtree(myTestBucket);
    }

    @Test
    public void testGetPairtreeBucketPath(final TestContext aContext) throws PairtreeException {
        checkCredentialsFile();

        myPairtree = new PairtreeFactory(myVertx, new S3Profile(TEST_PROFILE)).getPairtree(myTestBucket, "fake_path");
    }

    @Test
    public void testGetPrefixedPairtreeBucketPath(final TestContext aContext) throws PairtreeException {
        checkCredentialsFile();

        myPairtree = new PairtreeFactory(myVertx, new S3Profile(TEST_PROFILE)).getPrefixedPairtree(PT_PREFIX,
                myTestBucket, "fake_path");
    }

    @Test
    public void testGetPairtreeFactoryWithProfile(final TestContext aContext) throws PairtreeException {
        checkCredentialsFile();

        myPairtree = new PairtreeFactory(myVertx, new S3Profile(TEST_PROFILE)).getPairtree(myTestBucket);
    }

    @Test
    public void testGetPairtreeFactoryWithENVProfile(final TestContext aContext) throws PairtreeException {
        final String profileName = System.getenv(AWS_PROFILE);

        checkProfileName(profileName);
        LOGGER.debug(MessageCodes.PT_DEBUG_062, profileName);

        assertEquals(TEST_PROFILE, profileName);
        myPairtree = new PairtreeFactory(myVertx).getPairtree(myTestBucket);
    }

    @Test
    public void testGetPairtreeFactoryWithProfileAndPrefix(final TestContext aContext) throws PairtreeException {
        final S3Profile testProfile = new S3Profile(TEST_PROFILE);

        checkCredentialsFile();

        myPairtree = new PairtreeFactory(myVertx, testProfile).getPrefixedPairtree(PT_PREFIX, myTestBucket);
    }

    @Test
    public void testGetPairtreeFactoryWithENVProfileAndPrefix(final TestContext aContext) throws PairtreeException {
        final String profileName = System.getenv(AWS_PROFILE);

        checkProfileName(profileName);
        LOGGER.debug(MessageCodes.PT_DEBUG_062, profileName);

        assertEquals(TEST_PROFILE, profileName);
        myPairtree = new PairtreeFactory(myVertx).getPrefixedPairtree(PT_PREFIX, myTestBucket);
    }

    /**
     * Skip some tests if the testing environment isn't properly set up.
     *
     * @param aProfileName An AWS profile name
     */
    private void checkProfileName(final String aProfileName) {
        if (aProfileName == null) {
            LOGGER.warn(MessageCodes.PT_DEBUG_063);
            throw new AssumptionViolatedException(LOGGER.getMessage(MessageCodes.PT_DEBUG_063));
        } else if (!Files.exists(CREDS_FILE_PATH)) {
            throw new AssumptionViolatedException(LOGGER.getMessage(MessageCodes.PT_DEBUG_064));
        }
    }

    /**
     * Skip some tests if the testing environment isn't properly set up.
     */
    private void checkCredentialsFile() {
        if (!Files.exists(CREDS_FILE_PATH)) {
            LOGGER.warn(MessageCodes.PT_DEBUG_064);
            throw new AssumptionViolatedException(LOGGER.getMessage(MessageCodes.PT_DEBUG_063));
        }
    }
}
