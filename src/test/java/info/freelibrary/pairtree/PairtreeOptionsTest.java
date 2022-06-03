
package info.freelibrary.pairtree;

import static info.freelibrary.util.Constants.SLASH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.UUID;

import org.junit.Test;

import info.freelibrary.util.StringUtils;

import info.freelibrary.pairtree.s3.S3PairtreeOptions;

/**
 * Tests PairtreeOptions creation.
 */
public class PairtreeOptionsTest {

    /**
     * Tests getting the bucket name from an S3 options object.
     */
    @Test
    public final void testGetBucket() {
        final String bucketName = UUID.randomUUID().toString();
        final PairtreeOptions opts = new PairtreeOptions(StringUtils.format("s3://{}", bucketName));

        assertTrue(opts.isS3Backed());
        assertEquals(bucketName, opts.getBucket().get());
        assertTrue(opts.getBucketPath().isEmpty());
        assertTrue(opts.getFsDir().isEmpty());
    }

    /**
     * Tests getting the bucket path from an S3 options object.
     */
    @Test
    public final void testGetBucketPath() {
        final String bucketName = UUID.randomUUID().toString();
        final String bucketPath = UUID.randomUUID().toString();
        final PairtreeOptions opts = new PairtreeOptions(StringUtils.format("s3://{}/{}", bucketName, bucketPath));

        assertTrue(opts.isS3Backed());
        assertTrue(opts.getFsDir().isEmpty());
        assertEquals(bucketName, opts.getBucket().get());
        assertEquals(bucketPath, opts.getBucketPath().get());
    }

    /**
     * Tests getting an absolute file system directory from a file systems options object.
     */
    @Test
    public final void testFsAbsolute() {
        final String absolutePath = System.getProperty("java.io.tmpdir") + SLASH + UUID.randomUUID();
        final PairtreeOptions opts = new PairtreeOptions(absolutePath);
        final File dir = opts.getFsDir().get();

        assertTrue(dir.isAbsolute());
        assertTrue(opts.isFsBacked());
        assertTrue(opts.getBucket().isEmpty());
        assertTrue(opts.getBucketPath().isEmpty());
        assertEquals(absolutePath, dir.getPath());
    }

    /**
     * Tests getting a relative file system directory from a file systems options object.
     */
    @Test
    public final void testFsRelative() {
        final String relativePath = UUID.randomUUID().toString();
        final PairtreeOptions opts = new PairtreeOptions(relativePath);
        final File dir = opts.getFsDir().get();

        assertFalse(dir.isAbsolute());
        assertTrue(opts.isFsBacked());
        assertTrue(opts.getBucket().isEmpty());
        assertTrue(opts.getBucketPath().isEmpty());
        assertEquals(relativePath, dir.getPath());
    }

    /**
     * Tests getting a bucket name and bucket path from an options object created with a bare path.
     */
    @Test
    public final void testBarePathConstructor() {
        final String bucketName = UUID.randomUUID().toString();
        final String bucketPath = UUID.randomUUID().toString();
        final PairtreeOptions opts = new PairtreeOptions(StringUtils.format("{}/{}", bucketName, bucketPath));

        assertFalse(opts.isS3Backed());
        assertTrue(opts.getBucket().isEmpty());
        assertTrue(opts.getBucketPath().isEmpty());
        assertTrue(opts.setS3Options(new S3PairtreeOptions()).isS3Backed());
        assertEquals(bucketName, opts.getBucket().get());
        assertEquals(bucketPath, opts.getBucketPath().get());
        assertTrue(opts.isS3Backed());
    }

    /**
     * Tests preservation of default bucket and bucket path when empty S3PairtreeObject is used.
     */
    @Test
    public final void testGetS3OptionsFromEmptyS3Opts() {
        final String bucketName = UUID.randomUUID().toString();
        final String bucketPath = UUID.randomUUID().toString();
        final PairtreeOptions opts = new PairtreeOptions(StringUtils.format("{}/{}", bucketName, bucketPath))
                .setS3Options(new S3PairtreeOptions());
        final S3PairtreeOptions s3Opts = opts.getS3Options().get();

        assertTrue(opts.isS3Backed());
        assertEquals(bucketName, s3Opts.getBucket());
        assertEquals(bucketPath, s3Opts.getBucketPath());
    }

    /**
     * Tests the overriding of default bucket and bucket path with values from an S3PairtreeObject.
     */
    @Test
    public final void testGetS3OptionsFromS3Opts() {
        final String bucketName = UUID.randomUUID().toString();
        final String bucketPath = UUID.randomUUID().toString();
        final PairtreeOptions opts = new PairtreeOptions(StringUtils.format("{}/{}", "bucketName", "bucketPath"))
                .setS3Options(new S3PairtreeOptions().setBucket(bucketName).setBucketPath(bucketPath));
        final S3PairtreeOptions s3Opts = opts.getS3Options().get();

        assertTrue(opts.isS3Backed());
        assertEquals(bucketName, s3Opts.getBucket());
        assertEquals(bucketPath, s3Opts.getBucketPath());
    }

}
