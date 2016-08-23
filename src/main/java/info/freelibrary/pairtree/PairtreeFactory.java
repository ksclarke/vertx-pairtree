
package info.freelibrary.pairtree;

import static info.freelibrary.pairtree.MessageCodes.PT_009;
import static info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl.FileSystem;
import static info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl.S3Bucket;
import static info.freelibrary.pairtree.PairtreeRoot.DEFAULT_PAIRTREE_NAME;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import info.freelibrary.pairtree.fs.FsPairtree;
import info.freelibrary.pairtree.s3.S3Pairtree;

import io.vertx.core.Vertx;

public class PairtreeFactory {

    public static enum PairtreeImpl {
        FileSystem, S3Bucket
    };

    public static final PairtreeImpl DEFAULT_TYPE = PairtreeImpl.FileSystem;

    private static List<Map.Entry<PairtreeImpl, PairtreeFactory>> myFactories =
            new ArrayList<>(PairtreeImpl.values().length);

    private final Vertx myVertx;

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
     * Gets a Pairtree root.
     *
     * @param aConfigVarargs The configuration values in this order: location (file system path or bucket name), AWS
     *        access key, and AWS secret key (the last two are only needed if the implementation is an S3 Pairtree)
     * @return A Pairtree root
     */
    public final PairtreeRoot getPairtree(final String... aConfigVarargs) {
        if (myImplType.equals(FileSystem)) {
            return getPairtree(FileSystem, aConfigVarargs[0]);
        } else if (myImplType.equals(S3Bucket)) {
            return getPairtree(S3Bucket, aConfigVarargs);
        } else {
            throw new PairtreeRuntimeException(PT_009, myImplType);
        }
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
     * Gets a Pairtree backed by the supplied back-end type.
     *
     * @param aImpl The type of Pairtree implementation
     * @param aConfigVarargs The configuration values in this order: location (file system path or bucket name), AWS
     *        access key, and AWS secret key (the last two are only needed if the implementation is an S3 Pairtree)
     * @return A Pairtree root
     */
    private final PairtreeRoot getPairtree(final PairtreeImpl aImpl, final String... aConfigVarargs) {
        switch (aImpl) {
            case S3Bucket:
                final String bucket = aConfigVarargs.length > 0 ? aConfigVarargs[0] : DEFAULT_PAIRTREE_NAME;
                final String accessKey;
                final String secretKey;

                if (aConfigVarargs.length > 2) {
                    accessKey = aConfigVarargs[1];
                    secretKey = aConfigVarargs[2];
                } else {
                    accessKey = System.getProperty("AWS_ACCESS_KEY");
                    secretKey = System.getProperty("AWS_SECRET_KEY");
                }

                return new S3Pairtree(myVertx, bucket, accessKey, secretKey);
            default: // FileSystem backed
                final String fsPath = aConfigVarargs.length > 0 ? aConfigVarargs[0] : DEFAULT_PAIRTREE_NAME;
                return new FsPairtree(myVertx, fsPath);
        }
    }
}
