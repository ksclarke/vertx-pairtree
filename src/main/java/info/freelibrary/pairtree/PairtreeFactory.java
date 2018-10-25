
package info.freelibrary.pairtree;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;

import info.freelibrary.pairtree.fs.FsPairtree;
import info.freelibrary.pairtree.s3.S3Pairtree;
import info.freelibrary.util.StringUtils;

import io.vertx.core.Vertx;

/**
 * A factory which can be used to create pairtree objects.
 */
public final class PairtreeFactory {

    private final Vertx myVertx;

    private final Optional<String> myAccessKey;

    private final Optional<String> mySecretKey;

    private final Optional<Region> myRegion;

    /**
     * Creates a Pairtree factory using a newly created Vert.x environment.
     */
    public PairtreeFactory() {
        this(Vertx.vertx());
    }

    /**
     * Creates a Pairtree factory using the supplied Vert.x environment.
     *
     * @param aVertx A Vert.x environment
     */
    public PairtreeFactory(final Vertx aVertx) {
        final String accessKey = StringUtils.trimToNull(System.getenv(S3Pairtree.AWS_ACCESS_KEY));
        final String secretKey = StringUtils.trimToNull(System.getenv(S3Pairtree.AWS_SECRET_KEY));

        String region = StringUtils.trimToNull(System.getenv(S3Pairtree.AWS_REGION));

        myAccessKey = accessKey == null ? Optional.ofNullable(System.getProperty(S3Pairtree.AWS_ACCESS_KEY))
                : Optional.ofNullable(accessKey);
        mySecretKey = secretKey == null ? Optional.ofNullable(System.getProperty(S3Pairtree.AWS_SECRET_KEY))
                : Optional.ofNullable(secretKey);

        if (region != null) {
            myRegion = Optional.ofNullable(RegionUtils.getRegion(region));
        } else {
            region = System.getProperty(S3Pairtree.AWS_REGION);
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
    public PairtreeRoot getPairtree(final File aDirectory) throws PairtreeException {
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
    public PairtreeRoot getPrefixedPairtree(final String aPrefix, final File aDirectory) throws PairtreeException {
        return new FsPairtree(aPrefix, myVertx, getDirPath(aDirectory));
    }

    /**
     * Gets the S3 based Pairtree using the supplied S3 bucket.
     *
     * @param aBucket An S3 bucket in which to create the Pairtree
     * @return A Pairtree root
     * @throws PairtreeException If there is trouble creating the Pairtree
     */
    public PairtreeRoot getPairtree(final String aBucket) throws PairtreeException {
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
    public PairtreeRoot getPrefixedPairtree(final String aPrefix, final String aBucket) throws PairtreeException {
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
    public PairtreeRoot getPairtree(final String aBucket, final String aBucketPath) throws PairtreeException {
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
    public PairtreeRoot getPrefixedPairtree(final String aPrefix, final String aBucket, final String aBucketPath)
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
    public PairtreeRoot getPairtree(final String aBucket, final String aAccessKey, final String aSecretKey) {
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
    public PairtreeRoot getPairtree(final String aBucket, final String aAccessKey, final String aSecretKey,
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
    public PairtreeRoot getPrefixedPairtree(final String aPrefix, final String aBucket, final String aAccessKey,
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
    public PairtreeRoot getPrefixedPairtree(final String aPrefix, final String aBucket, final String aAccessKey,
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
    public PairtreeRoot getPairtree(final String aBucket, final String aBucketPath, final String aAccessKey,
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
    public PairtreeRoot getPairtree(final String aBucket, final String aBucketPath, final String aAccessKey,
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
    public PairtreeRoot getPrefixedPairtree(final String aPrefix, final String aBucket, final String aBucketPath,
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
    public PairtreeRoot getPrefixedPairtree(final String aPrefix, final String aBucket, final String aBucketPath,
            final String aAccessKey, final String aSecretKey, final Region aRegion) {
        return new S3Pairtree(aPrefix, myVertx, aBucket, aBucketPath, aAccessKey, aSecretKey, aRegion);
    }

    private static String getDirPath(final File aDirectory) throws PairtreeException {
        Objects.requireNonNull(aDirectory, MessageCodes.PT_022);

        if (aDirectory.exists() && (!aDirectory.isDirectory() || !aDirectory.canWrite())) {
            throw new PairtreeException(MessageCodes.PT_023, aDirectory);
        }

        return aDirectory.getAbsolutePath();
    }

}
