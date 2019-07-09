
package info.freelibrary.pairtree;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

import info.freelibrary.pairtree.fs.FsPairtree;
import info.freelibrary.pairtree.s3.S3Pairtree;
import info.freelibrary.pairtree.s3.S3Profile;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.Vertx;

/**
 * A factory which can be used to create pairtree objects.
 */
public final class PairtreeFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PairtreeFactory.class, Constants.BUNDLE_NAME);

    private static final String AWS_PROFILE = "AWS_PROFILE";

    private static final String AWS_REGION = "AWS_REGION";

    private final Vertx myVertx;

    private final Optional<String> myAccessKey;

    private final Optional<String> mySecretKey;

    private final Optional<Region> myRegion;

    /**
     * Creates a Pairtree factory using a newly created Vert.x environment. The AWS_PROFILE ENV property is checked
     * first for S3 credentials; if that doesn't exist, AWS_ACCESS_KEY and AWS_SECRET_KEY from the ENV and/or system
     * properties are checked next (in that order, with the latter taking precedence). Region can also be set from the
     * AWS_REGION ENV property.
     */
    public PairtreeFactory() {
        this(Vertx.vertx());
    }

    /**
     * Creates a Pairtree factory using a newly created Vert.x environment and credentials from the supplied S3
     * profile. If the supplied S3 profile is null, the system ENV is checked for the AWS_PROFILE property; if that
     * isn't found, AWS_ACCESS_KEY and AWS_SECRET_KEY from the ENV and/or system properties are checked next (in that
     * order, with the latter taking precedence). Region can also be set from the AWS_REGION ENV property.
     *
     * @param aProfile An S3 profile
     */
    public PairtreeFactory(final S3Profile aProfile) {
        this(Vertx.vertx(), aProfile);
    }

    /**
     * Creates a Pairtree factory using the supplied Vert.x environment. The AWS_PROFILE ENV property is checked
     * first; if that doesn't exist, AWS_ACCESS_KEY and AWS_SECRET_KEY from the ENV and/or system properties are
     * checked next (in that order, with the latter taking precedence). Region can also be set from the AWS_REGION ENV
     * property.
     *
     * @param aVertx A Vert.x environment
     */
    public PairtreeFactory(final Vertx aVertx) {
        this(aVertx, null);
    }

    /**
     * Creates a Pairtree factory using the supplied Vert.x environment and credentials from the supplied profile. If
     * an S3 profile isn't explicitly supplied, AWS_PROFILE from the ENV is checked first; if that isn't found
     * AWS_ACCESS_KEY and AWS_SECRET_KEY from the ENV and then system properties are checked next (with the latter
     * taking precedence). Region can also be set from the AWS_REGION system ENV property.
     *
     * @param aVertx A Vert.x instance
     * @param aProfile An S3 profile
     */
    public PairtreeFactory(final Vertx aVertx, final S3Profile aProfile) {
        final String profileName = StringUtils.trimToNull(System.getenv(AWS_PROFILE));
        final Path credsFilePath = Paths.get(System.getProperty("user.home"), ".aws/credentials");
        final boolean credsFileFound = Files.exists(credsFilePath);

        if ((profileName != null || aProfile != null) && !credsFileFound) {
            LOGGER.warn(MessageCodes.PT_024);
        }

        if (aProfile != null && credsFileFound) {
            final AWSCredentials creds = aProfile.getCredentials();

            myAccessKey = Optional.ofNullable(creds.getAWSAccessKeyId());
            mySecretKey = Optional.ofNullable(creds.getAWSSecretKey());
        } else if (profileName != null && credsFileFound) {
            final S3Profile profile = new S3Profile(profileName);
            final AWSCredentials creds = profile.getCredentials();

            LOGGER.debug(MessageCodes.PT_DEBUG_065);

            myAccessKey = Optional.ofNullable(creds.getAWSAccessKeyId());
            mySecretKey = Optional.ofNullable(creds.getAWSSecretKey());
        } else {
            final String accessKey = StringUtils.trimToNull(System.getenv(S3Pairtree.AWS_ACCESS_KEY));
            final String secretKey = StringUtils.trimToNull(System.getenv(S3Pairtree.AWS_SECRET_KEY));

            myAccessKey = accessKey == null ? Optional.ofNullable(System.getProperty(S3Pairtree.AWS_ACCESS_KEY))
                    : Optional.ofNullable(accessKey);
            mySecretKey = secretKey == null ? Optional.ofNullable(System.getProperty(S3Pairtree.AWS_SECRET_KEY))
                    : Optional.ofNullable(secretKey);
        }

        // Set the region from ENV first, then system properties
        String region = StringUtils.trimToNull(System.getenv(AWS_REGION));

        if (region != null) {
            myRegion = Optional.ofNullable(RegionUtils.getRegion(region));
        } else {
            region = StringUtils.trimToNull(System.getProperty("vertx.s3.region"));
            myRegion = region != null ? Optional.ofNullable(RegionUtils.getRegion(region)) : Optional.empty();
        }

        myVertx = aVertx;
    }

    /**
     * Gets a file system based Pairtree using the supplied directory as the Pairtree root.
     *
     * @param aDirectory A directory to use for the Pairtree root
     * @return A Pairtree root
     * @throws PairtreeException If there is trouble creating the Pairtree
     */
    public Pairtree getPairtree(final File aDirectory) throws PairtreeException {
        return new FsPairtree(myVertx, getDirPath(aDirectory));
    }

    /**
     * Gets a file system based Pairtree using the supplied directory as the Pairtree root and the supplied prefix as
     * the Pairtree prefix.
     *
     * @param aPrefix A Pairtree prefix
     * @param aDirectory A directory to use for the Pairtree root
     * @return A Pairtree root
     * @throws PairtreeException If there is trouble creating the Pairtree
     */
    public Pairtree getPrefixedPairtree(final String aPrefix, final File aDirectory) throws PairtreeException {
        return new FsPairtree(aPrefix, myVertx, getDirPath(aDirectory));
    }

    /**
     * Gets the S3 based Pairtree using the supplied S3 bucket.
     *
     * @param aBucket An S3 bucket in which to create the Pairtree
     * @return A Pairtree root
     * @throws PairtreeException If there is trouble creating the Pairtree
     */
    public Pairtree getPairtree(final String aBucket) throws PairtreeException {
        if (myAccessKey.isPresent() && mySecretKey.isPresent()) {
            final String accessKey = myAccessKey.get();
            final String secretKey = mySecretKey.get();

            if (myRegion.isPresent()) {
                return new S3Pairtree(myVertx, aBucket, accessKey, secretKey, myRegion.get());
            } else {
                return new S3Pairtree(myVertx, aBucket, accessKey, secretKey);
            }
        } else {
            throw new PairtreeException(MessageCodes.PT_021);
        }
    }

    /**
     * Gets the S3 based Pairtree using the supplied S3 bucket and the supplied Pairtree prefix.
     *
     * @param aPrefix A Pairtree prefix
     * @param aBucket An S3 bucket in which to create the Pairtree
     * @return A Pairtree root
     * @throws PairtreeException If there is trouble creating the Pairtree
     */
    public Pairtree getPrefixedPairtree(final String aPrefix, final String aBucket) throws PairtreeException {
        if (myAccessKey.isPresent() && mySecretKey.isPresent()) {
            final String accessKey = myAccessKey.get();
            final String secretKey = mySecretKey.get();

            if (myRegion.isPresent()) {
                return new S3Pairtree(aPrefix, myVertx, aBucket, accessKey, secretKey, myRegion.get());
            } else {
                return new S3Pairtree(aPrefix, myVertx, aBucket, accessKey, secretKey);
            }
        } else {
            throw new PairtreeException(MessageCodes.PT_021);
        }
    }

    /**
     * Gets the S3 based Pairtree using the supplied S3 bucket and bucket path.
     *
     * @param aBucket An S3 bucket in which to create the Pairtree
     * @param aBucketPath A path in the S3 bucket at which to put the Pairtree
     * @return A Pairtree root
     * @throws PairtreeException If there is trouble creating the Pairtree
     */
    public Pairtree getPairtree(final String aBucket, final String aBucketPath) throws PairtreeException {
        if (myAccessKey.isPresent() && mySecretKey.isPresent()) {
            final String accessKey = myAccessKey.get();
            final String secretKey = mySecretKey.get();

            if (myRegion.isPresent()) {
                return new S3Pairtree(myVertx, aBucket, aBucketPath, accessKey, secretKey, myRegion.get());
            } else {
                return new S3Pairtree(myVertx, aBucket, aBucketPath, accessKey, secretKey);
            }
        } else {
            throw new PairtreeException(MessageCodes.PT_021);
        }
    }

    /**
     * Gets the S3 based Pairtree, with the supplied prefix, using the supplied S3 bucket and bucket path.
     *
     * @param aPrefix A Pairtree prefix
     * @param aBucket An S3 bucket in which to create the Pairtree
     * @param aBucketPath A path in the S3 bucket at which to put the Pairtree
     * @return A Pairtree root
     * @throws PairtreeException If there is trouble creating the Pairtree
     */
    public Pairtree getPrefixedPairtree(final String aPrefix, final String aBucket, final String aBucketPath)
            throws PairtreeException {
        if (myAccessKey.isPresent() && mySecretKey.isPresent()) {
            final String accessKey = myAccessKey.get();
            final String secretKey = mySecretKey.get();

            if (myRegion.isPresent()) {
                return new S3Pairtree(aPrefix, myVertx, aBucket, aBucketPath, accessKey, secretKey, myRegion.get());
            } else {
                return new S3Pairtree(aPrefix, myVertx, aBucket, aBucketPath, accessKey, secretKey);
            }
        } else {
            throw new PairtreeException(MessageCodes.PT_021);
        }
    }

    /**
     * Creates a Pairtree using the supplied bucket and AWS credentials.
     *
     * @param aBucket An S3 bucket
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @return A Pairtree
     */
    public Pairtree getPairtree(final String aBucket, final String aAccessKey, final String aSecretKey) {
        return new S3Pairtree(myVertx, aBucket, aAccessKey, aSecretKey);
    }

    /**
     * Creates a Pairtree using the supplied bucket and AWS credentials.
     *
     * @param aBucket An S3 bucket
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @param aRegion An AWS region in which to put the bucket
     * @return A Pairtree
     */
    public Pairtree getPairtree(final String aBucket, final String aAccessKey, final String aSecretKey,
            final Region aRegion) {
        return new S3Pairtree(myVertx, aBucket, aAccessKey, aSecretKey, aRegion);
    }

    /**
     * Creates a Pairtree, with the supplied prefix, using the supplied bucket and AWS credentials.
     *
     * @param aPrefix A Pairtree prefix
     * @param aBucket An S3 bucket
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @return A Pairtree
     */
    public Pairtree getPrefixedPairtree(final String aPrefix, final String aBucket, final String aAccessKey,
            final String aSecretKey) {
        return new S3Pairtree(aPrefix, myVertx, aBucket, aAccessKey, aSecretKey);
    }

    /**
     * Creates a Pairtree, with the supplied prefix, using the supplied bucket and AWS credentials.
     *
     * @param aPrefix A Pairtree prefix
     * @param aBucket An S3 bucket
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @param aRegion An AWS region in which to put the bucket
     * @return A Pairtree
     */
    public Pairtree getPrefixedPairtree(final String aPrefix, final String aBucket, final String aAccessKey,
            final String aSecretKey, final Region aRegion) {
        return new S3Pairtree(aPrefix, myVertx, aBucket, aAccessKey, aSecretKey, aRegion);
    }

    /**
     * Creates a Pairtree using the supplied S3 bucket and internal bucket path.
     *
     * @param aBucket An S3 bucket
     * @param aBucketPath A path in the S3 bucket to the Pairtree root
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AwS secret key
     * @return A Pairtree
     */
    public Pairtree getPairtree(final String aBucket, final String aBucketPath, final String aAccessKey,
            final String aSecretKey) {
        return new S3Pairtree(myVertx, aBucket, aBucketPath, aAccessKey, aSecretKey);
    }

    /**
     * Creates a Pairtree using the supplied S3 bucket and internal bucket path.
     *
     * @param aBucket An S3 bucket
     * @param aBucketPath A path in the S3 bucket to the Pairtree root
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @param aRegion An AWS region in which to put the bucket
     * @return A Pairtree
     */
    public Pairtree getPairtree(final String aBucket, final String aBucketPath, final String aAccessKey,
            final String aSecretKey, final Region aRegion) {
        return new S3Pairtree(myVertx, aBucket, aBucketPath, aAccessKey, aSecretKey, aRegion);
    }

    /**
     * Creates a Pairtree, with the supplied prefix, using the supplied S3 bucket and internal bucket path.
     *
     * @param aPrefix A Pairtree prefix
     * @param aBucket An S3 bucket
     * @param aBucketPath A path in the S3 bucket to the Pairtree root
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @return A Pairtree
     */
    public Pairtree getPrefixedPairtree(final String aPrefix, final String aBucket, final String aBucketPath,
            final String aAccessKey, final String aSecretKey) {
        return new S3Pairtree(aPrefix, myVertx, aBucket, aBucketPath, aAccessKey, aSecretKey);
    }

    /**
     * Creates a Pairtree, with the supplied prefix, using the supplied S3 bucket and internal bucket path.
     *
     * @param aPrefix A Pairtree prefix
     * @param aBucket An S3 bucket
     * @param aBucketPath A path in the S3 bucket to the Pairtree root
     * @param aAccessKey An AWS access key
     * @param aSecretKey An AWS secret key
     * @param aRegion An AWS region in which to put the Pairtree
     * @return A Pairtree
     */
    public Pairtree getPrefixedPairtree(final String aPrefix, final String aBucket, final String aBucketPath,
            final String aAccessKey, final String aSecretKey, final Region aRegion) {
        return new S3Pairtree(aPrefix, myVertx, aBucket, aBucketPath, aAccessKey, aSecretKey, aRegion);
    }

    /**
     * Get directory's absolute path, checking that the File represents a directory and is write-able.
     *
     * @param aDirectory A file system directory
     * @return The absolute path of the supplied file system directory
     * @throws PairtreeException An exception if the file system directory can't be used
     */
    private static String getDirPath(final File aDirectory) throws PairtreeException {
        Objects.requireNonNull(aDirectory, MessageCodes.PT_022);

        if (aDirectory.exists() && (!aDirectory.isDirectory() || !aDirectory.canWrite())) {
            throw new PairtreeException(MessageCodes.PT_023, aDirectory);
        }

        return aDirectory.getAbsolutePath();
    }

}
