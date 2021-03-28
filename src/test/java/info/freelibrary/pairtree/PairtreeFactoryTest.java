
package info.freelibrary.pairtree;

import static info.freelibrary.pairtree.Pairtree.ROOT;
import static java.util.UUID.randomUUID;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Test of the <code>PairtreeFactory</code>.
 */
@RunWith(VertxUnitRunner.class)
public class PairtreeFactoryTest {

    /** The system's directory for temporary files */
    private static final String TMPDIR = System.getProperty("java.io.tmpdir");

    /** The connection to the Vertx framework */
    private Vertx myVertx;

    /** The Pairtree being tested */
    private Pairtree myPairtree;

    /**
     * Setup for the tests.
     *
     * @param aContext A test context
     */
    @Before
    public void setUp(final TestContext aContext) throws Exception {
        myVertx = Vertx.vertx(new VertxOptions());
    }

    /**
     * Tear down test resources.
     *
     * @param aContext A test context
     */
    @After
    public void tearDown(final TestContext aContext) throws Exception {
        final Async asyncTask = aContext.async();

        // Clean up any resources we created
        if (myPairtree != null) {
            myPairtree.exists(existsCheck -> {
                if (existsCheck.succeeded()) {
                    if (existsCheck.result()) {
                        myPairtree.delete(deletion -> {
                            if (deletion.succeeded()) {
                                asyncTask.complete();
                            } else {
                                aContext.fail(deletion.cause());
                            }
                        });
                    } else {
                        asyncTask.complete();
                    }
                } else {
                    asyncTask.complete();
                }
            });
        } else {
            asyncTask.complete();
        }
    }

    /**
     * Tests getting a file system Pairtree implicitly.
     *
     * @param aContext A test context
     * @throws PairtreeException If there is a problem retrieving the Pairtree
     */
    @Test
    public void testGetPairtreeFsImplicitFactory(final TestContext aContext) throws PairtreeException {
        final String path = Paths.get(TMPDIR, randomUUID().toString()).toString();

        myPairtree = new PairtreeFactory(myVertx).getPairtree(new File(path));

        aContext.assertEquals(Paths.get(path, ROOT).toString(), myPairtree.toString());
    }

    /**
     * Tests getting a file system Pairtree from a supplied path.
     *
     * @param aContext A test context
     * @throws PairtreeException If there is trouble getting the Pairtree.
     */
    @Test
    public void testGetPairtreeFsDirPath(final TestContext aContext) throws PairtreeException {
        final Path path = Paths.get(TMPDIR, randomUUID().toString());

        try {
            myPairtree = new PairtreeFactory(myVertx).getPairtree(Files.createFile(path).toFile());
            aContext.fail();
        } catch (final PairtreeException details) {
            // expected
        } catch (final IOException details) {
            aContext.fail(details);
        }
    }

    /**
     * Tests trying to get a Pairtree from a path that isn't writable.
     *
     * @param aContext A test context
     * @throws PairtreeException If there is trouble getting the Pairtree
     */
    @Test
    public void testGetPairtreeFsDirPathNotWriteable(final TestContext aContext) throws PairtreeException {
        final Path path = Paths.get(TMPDIR, randomUUID().toString());
        final Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("r-xr-xr-x");
        final FileAttribute<Set<PosixFilePermission>> dirAttrs = PosixFilePermissions.asFileAttribute(permissions);

        try {
            myPairtree = new PairtreeFactory(myVertx).getPairtree(Files.createDirectory(path, dirAttrs).toFile());
            aContext.fail();
        } catch (final PairtreeException details) {
            // expected
        } catch (final IOException details) {
            aContext.fail(details);
        }
    }

    /**
     * Tests getting a Pairtree from the factory without supplying a Vert.x instance.
     *
     * @param aContext A test context
     * @throws PairtreeException If there is trouble getting the Pairtree from the factory
     */
    @Test
    public void testGetPairtreeFsImplicitFactoryNoVertx(final TestContext aContext) throws PairtreeException {
        final String path = Paths.get(TMPDIR, randomUUID().toString()).toString();

        myPairtree = new PairtreeFactory().getPairtree(new File(path));

        aContext.assertEquals(Paths.get(path, ROOT).toString(), myPairtree.toString());
    }

    /**
     * Tests getting the Pairtree from the factory explicitly.
     *
     * @param aContext A test context
     * @throws PairtreeException If there is trouble getting the Pairtree from the factory
     */
    @Test
    public void testGetPairtreeFsExplicitFactory(final TestContext aContext) throws PairtreeException {
        final String path = Paths.get(TMPDIR, randomUUID().toString()).toString();

        myPairtree = new PairtreeFactory(myVertx).getPairtree(new File(path));

        aContext.assertEquals(Paths.get(path, ROOT).toString(), myPairtree.toString());
    }

    /**
     * Tests getting a Pairtree from the factory, explicitly, with a supplied Vert.x instance.
     *
     * @param aContext A test context
     * @throws PairtreeException If there is trouble getting the Pairtree from the factory
     */
    @Test
    public void testGetPairtreeFsExplicitFactoryNoVertx(final TestContext aContext) throws PairtreeException {
        final String path = Paths.get(TMPDIR, randomUUID().toString()).toString();

        myPairtree = new PairtreeFactory().getPairtree(new File(path));

        aContext.assertEquals(Paths.get(path, ROOT).toString(), myPairtree.toString());
    }

}
