
package info.freelibrary.pairtree.fs;

import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_040;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_041;
import static info.freelibrary.pairtree.MessageCodes.PT_DEBUG_042;
import static java.io.File.separatorChar;
import static java.util.UUID.randomUUID;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;

import info.freelibrary.pairtree.AbstractPairtreeTest;
import info.freelibrary.pairtree.PairtreeException;
import info.freelibrary.pairtree.PairtreeFactory;
import info.freelibrary.pairtree.PairtreeRoot;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.ext.unit.TestContext;

/**
 * Tests for the <code>AbstractFsPairtree</code>.
 */
public abstract class AbstractFsPairtreeTest extends AbstractPairtreeTest {

    /** The Pairtree being tested */
    protected PairtreeRoot myPairtree;

    /** A connection to the file system */
    protected FileSystem myFileSystem;

    /**
     * Setup for the tests.
     *
     * @param aContext A test context
     */
    @Override
    @Before
    public void setUp(final TestContext aContext) {
        super.setUp(aContext);

        final String path = System.getProperty("java.io.tmpdir") + separatorChar + randomUUID();

        myFileSystem = myVertx.fileSystem();

        try {
            myPairtree = new PairtreeFactory(myVertx).getPairtree(new File(path));
        } catch (final PairtreeException details) {
            throw new RuntimeException(details);
        }
    }

    /**
     * Tear down for the tests.
     *
     * @param aContext A test context
     */
    @Override
    @After
    public void tearDown(final TestContext aContext) {
        if (myFileSystem.existsBlocking(myPairtree.getPath())) {
            myFileSystem.deleteRecursiveBlocking(myPairtree.getPath(), true);
        }

        super.tearDown(aContext);
    }

    /**
     * Creates a file system directory recursively.
     *
     * @param aDirPath A path to the directory to be created
     */
    protected void createDir(final String aDirPath) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getI18n(PT_DEBUG_040, aDirPath));
        }

        myFileSystem.mkdirsBlocking(aDirPath);
    }

    /**
     * Creates a file system file.
     *
     * @param aFilePath A path to the file to be created
     */
    protected void createFile(final String aFilePath) {
        final Path path = Paths.get(aFilePath);
        final String file = path.toString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getI18n(PT_DEBUG_041, file));
        }

        createDir(path.getParent().toString());
        myFileSystem.createFileBlocking(file);
    }

    /**
     * Creates a file system file with the supplied value in it.
     *
     * @param aFilePath A path to the file to be created
     */
    protected void createFile(final String aFilePath, final String aContentValue) {
        final Path path = Paths.get(aFilePath);
        final String file = path.toString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getI18n(PT_DEBUG_042, file, aContentValue));
        }

        createDir(path.getParent().toString());
        myFileSystem.writeFileBlocking(aFilePath, Buffer.buffer(aContentValue));
    }

}
