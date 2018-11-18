
package info.freelibrary.pairtree;

import static info.freelibrary.pairtree.Pairtree.PAIRTREE_ROOT;
import static java.util.UUID.randomUUID;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import junit.framework.TestCase;

/**
 * Test of the <code>PairtreeFactory</code>.
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
    public void testGetPairtreeFsImplicitFactory() throws PairtreeException {
        final String path = Paths.get(TMPDIR, randomUUID().toString()).toString();
        final Pairtree root = new PairtreeFactory(myVertx).getPairtree(new File(path));

        assertEquals(Paths.get(path, PAIRTREE_ROOT).toString(), root.toString());
    }

    @Test
    public void testGetPairtreeFsImplicitFactoryNoVertx() throws PairtreeException {
        final String path = Paths.get(TMPDIR, randomUUID().toString()).toString();
        final Pairtree root = new PairtreeFactory().getPairtree(new File(path));

        assertEquals(Paths.get(path, PAIRTREE_ROOT).toString(), root.toString());
    }

    @Test
    public void testGetPairtreeFsExplicitFactory() throws PairtreeException {
        final String path = Paths.get(TMPDIR, randomUUID().toString()).toString();
        final Pairtree root = new PairtreeFactory(myVertx).getPairtree(new File(path));

        assertEquals(Paths.get(path, PAIRTREE_ROOT).toString(), root.toString());
    }

    @Test
    public void testGetPairtreeFsExplicitFactoryNoVertx() throws PairtreeException {
        final String path = Paths.get(TMPDIR, randomUUID().toString()).toString();
        final Pairtree root = new PairtreeFactory().getPairtree(new File(path));

        assertEquals(Paths.get(path, PAIRTREE_ROOT).toString(), root.toString());
    }

}
