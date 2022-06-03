
package info.freelibrary.pairtree;

import java.io.File;
import java.util.Objects;

import info.freelibrary.util.I18nObject;

import info.freelibrary.pairtree.fs.FsPairtree;
import info.freelibrary.pairtree.s3.S3Pairtree;
// import info.freelibrary.pairtree.s3.S3Pairtree;
import info.freelibrary.pairtree.s3.S3PairtreeOptions;

import io.vertx.core.Vertx;

/**
 * A factory which can be used to create pairtree objects.
 */
public final class PairtreeFactory extends I18nObject {

    /** The PairtreeFactory's Vert.x instance. */
    private final Vertx myVertx;

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
        super(MessageCodes.BUNDLE);
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
     * Gets a file system based Pairtree using the supplied directory as the Pairtree root.
     *
     * @param aDirectory A directory to use for the Pairtree root
     * @return A Pairtree root
     * @throws PairtreeException If there is trouble creating the Pairtree
     */
    public Pairtree getPairtree(final String aPrefix, final File aDirectory) throws PairtreeException {
        return new FsPairtree(aPrefix, myVertx, getDirPath(aDirectory));
    }

    /**
     * Gets an S3 based Pairtree using the supplied S3 Pairtree options.
     *
     * @param aConfig An S3 Pairtree configuration
     * @return A Pairtree root
     * @throws PairtreeException If there is trouble creating the Pairtree
     */
    public Pairtree getPairtree(final S3PairtreeOptions aConfig) throws PairtreeException {
        return new S3Pairtree(myVertx, aConfig);
    }

    /**
     * Gets a prefixed S3 based Pairtree using the supplied S3 Pairtree options.
     *
     * @param aPrefix A Pairtree prefix
     * @param aConfig An S3 Pairtree configuration
     * @return A Pairtree root
     * @throws PairtreeException If there is trouble creating the Pairtree
     */
    public Pairtree getPairtree(final String aPrefix, final S3PairtreeOptions aConfig) throws PairtreeException {
        return new S3Pairtree(aPrefix, myVertx, aConfig);
    }

    /**
     * Get directory's absolute path, checking that the File represents a directory and is write-able.
     *
     * @param aDirectory A file system directory
     * @return The absolute path of the supplied file system directory
     * @throws PairtreeException An exception if the file system directory can't be used
     */
    private String getDirPath(final File aDirectory) throws PairtreeException {
        Objects.requireNonNull(aDirectory, getI18n(MessageCodes.PT_022));

        if (aDirectory.exists() && (!aDirectory.isDirectory() || !aDirectory.canWrite())) {
            throw new PairtreeException(MessageCodes.PT_023, aDirectory);
        }

        return aDirectory.getAbsolutePath();
    }

}
