
package info.freelibrary.pairtree;

import static info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl.FileSystem;
import static info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl.S3Bucket;
import static info.freelibrary.pairtree.PairtreeRoot.DEFAULT_PAIRTREE;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import info.freelibrary.pairtree.fs.FsPairtree;
import info.freelibrary.pairtree.s3.S3Pairtree;

import io.vertx.core.Vertx;

/**
 * A factory which can be used to create pairtree objects.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public final class PairtreeFactory {

    /** The types of pairtree backends supported by this library. */
    public static enum PairtreeImpl {
        /** The file system Pairtree implementation */
        FileSystem,
        /** The S3 Pairtree implementation */
        S3Bucket
    }

    /** The default type of Pairtree implementation. */
    public static final PairtreeImpl DEFAULT_TYPE = FileSystem;

    /** The number of Pairtree implementations supported by this library */
    private static final int PT_IMPL_COUNT = PairtreeImpl.values().length;

    /** A list of Pairtree implementation factories */
    private static final List<Map.Entry<PairtreeImpl, PairtreeFactory>> myFactories = new ArrayList<>(PT_IMPL_COUNT);

    /** Minimum number of configuration options required by a Pairtree */
    private static final int MIN_CONFIG_COUNT = 2;

    /** Connection to the Vertx framework */
    private final Vertx myVertx;

    /** Reference to the Pairtree implementation type created by this factory */
    private final PairtreeImpl myImplType;

    /**
     * Creates a PairtreeFactory with the supplied default type
     *
     * @param aVertx A Vertx object
     * @param aImpl A Pairtree implementation
     */
    private PairtreeFactory(final Vertx aVertx, final PairtreeImpl aImpl) {
        myVertx = aVertx;
        myImplType = aImpl;
    }

    /**
     * Gets a <code>PairtreeFactory</code> that uses the default back-end type.
     *
     * @param aVertx A Vertx object
     * @return A <code>PairtreeFactory</code> backed by the default implementation
     */
    public static final PairtreeFactory getFactory(final Vertx aVertx) {
        return getFactory(aVertx, DEFAULT_TYPE);
    }

    /**
     * Gets a <code>PairtreeFactory</code> that uses the supplied default back-end type.
     *
     * @param aVertx A Vertx object
     * @param aImpl The desired Pairtree implementation
     * @return A <code>PairtreeFactory</code> backed by the desired implementation
     */
    public static final PairtreeFactory getFactory(final Vertx aVertx, final PairtreeImpl aImpl) {
        PairtreeFactory factory = null;

        for (final Map.Entry<PairtreeImpl, PairtreeFactory> entry : myFactories) {
            if (aImpl.equals(entry.getKey())) {
                factory = entry.getValue();
            }
        }

        if (factory == null) {
            factory = new PairtreeFactory(aVertx, aImpl);
            myFactories.add(new AbstractMap.SimpleEntry<>(aImpl, factory));
        }

        return factory;
    }

    /**
     * Gets a Pairtree root.
     *
     * @param aConfig The configuration values in this order: location (file system path or bucket name), AWS access
     *        key, and AWS secret key (the last two are only needed if the implementation is an S3 Pairtree)
     * @return A Pairtree root
     */
    public final PairtreeRoot getPairtree(final String... aConfig) {
        final PairtreeRoot pairtree;

        if (myImplType.equals(FileSystem)) {
            pairtree = getPairtree(FileSystem, aConfig[0]);
        } else if (myImplType.equals(S3Bucket)) {
            pairtree = getPairtree(S3Bucket, aConfig);
        } else {
            throw new PairtreeRuntimeException(MessageCodes.PT_009, myImplType);
        }

        return pairtree;
    }

    /**
     * Gets a Pairtree backed by the supplied back-end type.
     *
     * @param aImpl The type of Pairtree implementation
     * @param aConfig The configuration values in this order: location (file system path or bucket name), AWS access
     *        key, and AWS secret key (the last two are only needed if the implementation is an S3 Pairtree)
     * @return A Pairtree root
     */
    private final PairtreeRoot getPairtree(final PairtreeImpl aImpl, final String... aConfig) {
        final PairtreeRoot pairtree;

        if (aImpl.equals(S3Bucket)) {
            final String bucket = aConfig.length > 0 ? aConfig[0] : DEFAULT_PAIRTREE;
            final String accessKey;
            final String secretKey;
            final String endpoint;

            if (aConfig.length > MIN_CONFIG_COUNT) {
                accessKey = aConfig[1];
                secretKey = aConfig[2];

                if (aConfig.length > MIN_CONFIG_COUNT + 1) {
                    endpoint = aConfig[3];
                } else {
                    endpoint = null;
                }
            } else {
                accessKey = System.getProperty("AWS_ACCESS_KEY");
                secretKey = System.getProperty("AWS_SECRET_KEY");
                endpoint = System.getProperty("S3_ENDPOINT");
            }

            // FIXME: support Pairtree prefix
            if (endpoint == null) {
                pairtree = new S3Pairtree(myVertx, bucket, accessKey, secretKey);
            } else {
                pairtree = new S3Pairtree(myVertx, bucket, accessKey, secretKey, endpoint);
            }
        } else {
            // Default file-system implementation
            pairtree = new FsPairtree(myVertx, aConfig.length > 0 ? aConfig[0] : DEFAULT_PAIRTREE);
        }

        return pairtree;
    }
}
