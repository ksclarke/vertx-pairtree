
package info.freelibrary.pairtree.s3;

import info.freelibrary.vertx.s3.S3ClientOptions;

/**
 * S3 Pairtree configuration options.
 */
public class S3PairtreeOptions extends S3ClientOptions {

    /**
     * An S3 bucket.
     */
    private String myBucket;

    /**
     * A path within a bucket at which the Pairtree lives.
     */
    private String myBucketPath;

    /**
     * Create a new S3 configuration from the supplied profile.
     *
     * @param aProfile An S3 profile
     */
    public S3PairtreeOptions() {
        super();
    }

    /**
     * Sets the S3 bucket in which the Pairtree should be placed.
     *
     * @param aBucket An S3 bucket
     * @return These S3 options
     */
    public S3PairtreeOptions setBucket(final String aBucket) {
        myBucket = aBucket;
        return this;
    }

    /**
     * Gets the S3 bucket that houses the Pairtree.
     *
     * @return The S3 bucket that houses the Pairtree.
     */
    public String getBucket() {
        return myBucket;
    }

    /**
     * Gets the path within the S3 bucket that contains the Pairtree.
     *
     * @param aBucketPath A path within the S3 bucket
     * @return These S3 options
     */
    public S3PairtreeOptions setBucketPath(final String aBucketPath) {
        myBucketPath = aBucketPath;
        return this;
    }

    /**
     * Gets the path within the S3 bucket that contains the Pairtree.
     *
     * @return A path within the S3 bucket
     */
    public String getBucketPath() {
        return myBucketPath;
    }

}
