
package info.freelibrary.pairtree;

import static info.freelibrary.util.Constants.SLASH;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import info.freelibrary.pairtree.s3.S3PairtreeOptions;

/**
 * Options available for Pairtree creation.
 */
public class PairtreeOptions {

    /** A URI scheme for an S3 Pairtree. */
    private static final String S3_SCHEME = "s3://";

    /** A URI scheme for a file system Pairtree. */
    private static final String FILE_SCHEME = "file://";

    /** The unadulterated path to the Pairtree. */
    private String mySourcePath;

    /** A configuration for an S3-based Pairtree. */
    private S3PairtreeOptions myS3Config;

    /**
     * Creates new Pairtree options from a path to the Pairtree. The path can be one of three options:
     * <dl>
     * <dt>S3 path</dt>
     * <dd><code>s3://bucket/path</code> or <code>s3://bucket</code></dd>
     * <dt>File path</dt>
     * <dd><code>file:///absolute/path</code> or <code>file://relative/path</code></dd>
     * <dt>Bare path</dt>
     * <dd><code>/absolute/path</code> or <code>relative/path</code> or <code>bucket</code> or
     * <code>bucket/name</code></dd>
     * </dl>
     * <p>
     * The bare path will be treated as a file system path unless S3 credentials are set. Once they're set, it's treated
     * as an S3 bucket name and, optionally, bucket path.
     * </p>
     *
     * @param aSourcePath A path to the location of the Pairtree
     */
    public PairtreeOptions(final String aSourcePath) {
        if (aSourcePath.startsWith(S3_SCHEME)) {
            configureS3Options(aSourcePath);
        } else if (aSourcePath.startsWith(FILE_SCHEME)) {
            mySourcePath = aSourcePath.substring(6);
        } else {
            mySourcePath = aSourcePath;
        }
    }

    /**
     * Set S3 specific options. The bucket name and path do not have to be supplied again if the source path used when
     * constructing this object was correct.
     *
     * @param aConfig An S3 configuration
     * @return These options
     */
    public PairtreeOptions setS3Options(final S3PairtreeOptions aConfig) {
        if (myS3Config == null && mySourcePath != null) {
            // Set our source path if our constructor took a bare source path at creation
            configureS3Options(mySourcePath);
        }

        if (myS3Config != null) {
            final String currentBucketName = myS3Config.getBucket();
            final String currentBucketPath = myS3Config.getBucketPath();

            // If we already have our bucket name and path set (and aren't overriding them), preserve them
            if (currentBucketName != null && aConfig.getBucket() == null) {
                aConfig.setBucket(currentBucketName);
            }

            if (currentBucketPath != null && aConfig.getBucketPath() == null) {
                aConfig.setBucketPath(currentBucketPath);
            }
        }

        myS3Config = aConfig;
        return this;
    }

    /**
     * Returns S3 options, if they're set; else, an empty Optional.
     *
     * @return S3 options, if they're set; else, an empty Optional
     */
    public Optional<S3PairtreeOptions> getS3Options() {
        return Optional.ofNullable(myS3Config);
    }

    /**
     * Returns true if the Pairtree options specify the Pairtree should be created on a file system.
     *
     * @return True if the Pairtree options specify the Pairtree should be created on a file system; else, false
     */
    public boolean isFsBacked() {
        return myS3Config == null;
    }

    /**
     * Returns true if the Pairtree options specify the Pairtree should be created in S3.
     *
     * @return True if the Pairtree options specify the Pairtree should be created in S3; else, false
     */
    public boolean isS3Backed() {
        return myS3Config != null;
    }

    /**
     * Gets the S3 bucket associated with this Pairtree configuration.
     *
     * @return An S3 bucket
     */
    public Optional<String> getBucket() {
        return myS3Config != null ? Optional.ofNullable(myS3Config.getBucket()) : Optional.empty();
    }

    /**
     * Gets the S3 bucket path associated with this Pairtree configuration.
     *
     * @return An S3 bucket path
     */
    public Optional<String> getBucketPath() {
        return myS3Config != null ? Optional.ofNullable(myS3Config.getBucketPath()) : Optional.empty();
    }

    /**
     * Gets the file system directory containing the Pairtree.
     *
     * @return A file system directory
     */
    public Optional<File> getFsDir() {
        return myS3Config == null ? Optional.of(new File(mySourcePath)) : Optional.empty();
    }

    /**
     * Configures basic S3 options from a supplied source path.
     *
     * @param aSourcePath A path at which to create a Pairtree
     */
    private final void configureS3Options(final String aSourcePath) {
        final String sourcePath = aSourcePath.startsWith(S3_SCHEME) ? aSourcePath.substring(5) : aSourcePath;
        final String[] pathParts = sourcePath.split(SLASH);

        mySourcePath = sourcePath;
        myS3Config = new S3PairtreeOptions().setBucket(pathParts[0]);

        if (pathParts.length > 1) {
            myS3Config.setBucketPath(String.join(SLASH, Arrays.copyOfRange(pathParts, 1, pathParts.length)));
        }
    }
}
