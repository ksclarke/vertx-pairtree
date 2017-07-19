
package info.freelibrary.pairtree;

import static info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl.FileSystem;
import static info.freelibrary.pairtree.PairtreeRoot.PAIRTREE_ROOT;
import static java.util.UUID.randomUUID;

import java.nio.file.Paths;

import org.junit.Test;

import info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import junit.framework.TestCase;

/**
 * Test of the <code>PairtreeFactory</code>.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class PairtreeFactoryTest extends TestCase {

    /** The system's directory for temporary files */
    private static final String TMPDIR = System.getProperty("java.io.tmpdir");

    /** The connection to the Vertx framework */
    private Vertx myVertx;

    /**
     * Setup for the tests.
     */
    @Override
    public void setUp() throws Exception {
        myVertx = Vertx.vertx(new VertxOptions());
    }

    @Test
    public void testPairtreeFactoryEnum() {
        assertEquals(2, PairtreeImpl.values().length);

        for (final PairtreeImpl impl : PairtreeImpl.values()) {
            switch (impl) {
                case S3Bucket:
                    PairtreeImpl.valueOf(PairtreeImpl.S3Bucket.toString());
                    break;
                case FileSystem:
                    PairtreeImpl.valueOf(PairtreeImpl.FileSystem.toString());
                    break;
                default:
                    fail("Found unexpected Pairtree implementation value");
            }
        }
    }

    @Test
    public void testGetPairtreeFsImplicitFactory() {
        final String path = Paths.get(TMPDIR, randomUUID().toString()).toString();
        final PairtreeRoot root = PairtreeFactory.getFactory(myVertx).getPairtree(path);

        assertEquals(Paths.get(path, PAIRTREE_ROOT).toString(), root.toString());
    }

    @Test
    public void testGetPairtreeFsExplicitFactory() {
        final String path = Paths.get(TMPDIR, randomUUID().toString()).toString();
        final PairtreeRoot root = PairtreeFactory.getFactory(myVertx, FileSystem).getPairtree(path);

        assertEquals(Paths.get(path, PAIRTREE_ROOT).toString(), root.toString());
    }

}
