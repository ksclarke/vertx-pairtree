
package info.freelibrary.pairtree;

import static info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl.FileSystem;
import static info.freelibrary.pairtree.PairtreeRoot.PAIRTREE_ROOT;
import static java.io.File.separatorChar;
import static java.util.UUID.randomUUID;

import org.junit.Test;

import info.freelibrary.pairtree.PairtreeFactory.PairtreeImpl;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import junit.framework.TestCase;

public class PairtreeFactoryTest extends TestCase {

    private static final String TMPDIR = System.getProperty("java.io.tmpdir") + separatorChar;

    private Vertx myVertx;

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
        final String path = TMPDIR + randomUUID();
        final PairtreeRoot root = PairtreeFactory.getFactory(myVertx).getPairtree(path);

        assertEquals(path + separatorChar + PAIRTREE_ROOT, root.toString());
    }

    @Test
    public void testGetPairtreeFsExplicitFactory() {
        final String path = TMPDIR + randomUUID();
        final PairtreeRoot root = PairtreeFactory.getFactory(myVertx, FileSystem).getPairtree(path);

        assertEquals(path + separatorChar + PAIRTREE_ROOT, root.toString());
    }

}
