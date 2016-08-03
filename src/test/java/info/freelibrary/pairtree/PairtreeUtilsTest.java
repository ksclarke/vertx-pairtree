/*

 ===============================================================================
 This class (Pairtree) was originally written by Justin Littman (jlit@loc.gov)
 It has been modified and renamed by Kevin S. Clarke (ksclarke@ksclarke.io)
 ===============================================================================

 This software is a work of the United States Government and is not subject
 to copyright protection in the United States.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR THE UNITED STATES BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.


 Foreign copyrights may apply. To the extent that foreign copyrights in the
 software exist outside the United States, the following terms apply:

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

*/

package info.freelibrary.pairtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import info.freelibrary.util.InvalidPtPathException;

public class PairtreeUtilsTest {

    /**
     * Sets up the Pairtree path separator for the tests.
     */
    @BeforeClass
    public static void setUp() {
        PairtreeUtils.setSeparator('/');
    }

    /**
     * Tests mapping the supplied ID to a Pairtree path.
     */
    @Test
    public void testMapToPtPath() {
        assertEquals("ab/cd", PairtreeUtils.mapToPtPath("abcd"));
        assertEquals("ab/cd/ef/g", PairtreeUtils.mapToPtPath("abcdefg"));
        assertEquals("12/-9/86/xy/4", PairtreeUtils.mapToPtPath("12-986xy4"));

        assertEquals("13/03/0_/45/xq/v_/79/38/42/49/5", PairtreeUtils.mapToPtPath(null, "13030_45xqv_793842495",
                null));
        assertEquals("13/03/0_/45/xq/v_/79/38/42/49/5/793842495", PairtreeUtils.mapToPtPath(null,
                "13030_45xqv_793842495", "793842495"));
        assertEquals("/data/13/03/0_/45/xq/v_/79/38/42/49/5", PairtreeUtils.mapToPtPath("/data",
                "13030_45xqv_793842495", null));
        assertEquals("/data/13/03/0_/45/xq/v_/79/38/42/49/5", PairtreeUtils.mapToPtPath("/data/",
                "13030_45xqv_793842495", null));
        assertEquals("/data/13/03/0_/45/xq/v_/79/38/42/49/5/793842495", PairtreeUtils.mapToPtPath("/data",
                "13030_45xqv_793842495", "793842495"));
        assertEquals("/data/13/03/0_/45/xq/v_/79/38/42/49/5/ark+=13030=xt12t3", PairtreeUtils.mapToPtPath("/data",
                "13030_45xqv_793842495", "ark:/13030/xt12t3"));
    }

    /**
     * Tests encoding (i.e., "cleaning") the supplied ID.
     */
    @Test
    public void testEncodeID() {
        assertEquals("ark+=13030=xt12t3", PairtreeUtils.encodeID("ark:/13030/xt12t3"));
        assertEquals("http+==n2t,info=urn+nbn+se+kb+repos-1", PairtreeUtils.encodeID(
                "http://n2t.info/urn:nbn:se:kb:repos-1"));
        assertEquals("what-the-^2a@^3f#!^5e!^3f", PairtreeUtils.encodeID("what-the-*@?#!^!?"));
    }

    /**
     * Tests decoding (i.e., the opposite of "cleaning") the supplied ID.
     */
    @Test
    public void testDecodeID() {
        assertEquals("ark:/13030/xt12t3", PairtreeUtils.decodeID("ark+=13030=xt12t3"));
        assertEquals("http://n2t.info/urn:nbn:se:kb:repos-1", PairtreeUtils.decodeID(
                "http+==n2t,info=urn+nbn+se+kb+repos-1"));
        assertEquals("what-the-*@?#!^!?", PairtreeUtils.decodeID("what-the-^2a@^3f#!^5e!^3f"));
    }

    /**
     * Tests mapping the supplied ID to a Pairtree path, with ID encoding.
     */
    @Test
    public void testMapToPtPathWithIdCleaning() {
        assertEquals("ar/k+/=1/30/30/=x/t1/2t/3", PairtreeUtils.mapToPtPath("ark:/13030/xt12t3"));

        assertEquals("ht/tp/+=/=n/2t/,i/nf/o=/ur/n+/nb/n+/se/+k/b+/re/po/s-/1", PairtreeUtils.mapToPtPath(
                "http://n2t.info/urn:nbn:se:kb:repos-1"));

        assertEquals("wh/at/-t/he/-^/2a/@^/3f/#!/^5/e!/^3/f", PairtreeUtils.mapToPtPath("what-the-*@?#!^!?"));
    }

    /**
     * Tests getting the encapsulating directory from the supplied Pairtree path.
     *
     * @throws InvalidPtPathException If the supplied path isn't a valid Pairtree path
     */
    @Test
    public void testGetEncapsulatingDir() throws InvalidPathException {
        assertNull(PairtreeUtils.getEncapsulatingDir("ab"));
        assertNull(PairtreeUtils.getEncapsulatingDir("ab/cd"));
        assertNull(PairtreeUtils.getEncapsulatingDir("ab/cd/"));
        assertNull(PairtreeUtils.getEncapsulatingDir("ab/cd/ef/g"));
        assertNull(PairtreeUtils.getEncapsulatingDir("ab/cd/ef/g/"));
        assertEquals("h", PairtreeUtils.getEncapsulatingDir("ab/cd/ef/g/h"));
        assertEquals("h", PairtreeUtils.getEncapsulatingDir("ab/cd/ef/g/h/"));
        assertEquals("efg", PairtreeUtils.getEncapsulatingDir("ab/cd/efg"));
        assertEquals("efg", PairtreeUtils.getEncapsulatingDir("ab/cd/efg/"));
        assertEquals("h", PairtreeUtils.getEncapsulatingDir("ab/cd/ef/g/h"));
        assertEquals("h", PairtreeUtils.getEncapsulatingDir("ab/cd/ef/g/h/"));

        assertNull(PairtreeUtils.getEncapsulatingDir("/data", "/data/ab"));
        assertNull(PairtreeUtils.getEncapsulatingDir("/data/", "/data/ab"));
        assertEquals("h", PairtreeUtils.getEncapsulatingDir("/data", "/data/ab/cd/ef/g/h"));
        assertEquals("h", PairtreeUtils.getEncapsulatingDir("/data/", "/data/ab/cd/ef/g/h"));

        assertEquals("ark:/13030/xt12t3", PairtreeUtils.getEncapsulatingDir(
                "ar/k+/=1/30/30/=x/t1/2t/3/ark+=13030=xt12t3"));
        assertEquals("ark:/13030/xt12t3", PairtreeUtils.getEncapsulatingDir("/data",
                "/data/ar/k+/=1/30/30/=x/t1/2t/3/ark+=13030=xt12t3"));
    }

    @Test
    public void testRemovePrefix() {
        assertEquals("xt12t3", PairtreeUtils.removePrefix("ark:/13030/", "ark:/13030/xt12t3"));
    }

    /**
     * Tests extracting an ID from a supplied Pairtree path.
     *
     * @throws InvalidPtPathException If the supplied path isn't a valid Pairtree path.
     */
    @Test
    public void testMapToId() throws InvalidPathException {
        assertEquals("ab", PairtreeUtils.mapToID("ab"));
        assertEquals("abcd", PairtreeUtils.mapToID("ab/cd"));
        assertEquals("abcd", PairtreeUtils.mapToID("ab/cd/"));
        assertEquals("abcdefg", PairtreeUtils.mapToID("ab/cd/ef/g"));
        assertEquals("abcdefg", PairtreeUtils.mapToID("ab/cd/ef/g/"));
        assertEquals("abcdefg", PairtreeUtils.mapToID("ab/cd/ef/g/h"));
        assertEquals("abcdefg", PairtreeUtils.mapToID("ab/cd/ef/g/h/"));
        assertEquals("abcd", PairtreeUtils.mapToID("ab/cd/efg"));
        assertEquals("abcd", PairtreeUtils.mapToID("ab/cd/efg/"));
        assertEquals("abcdefg", PairtreeUtils.mapToID("ab/cd/ef/g/h"));
        assertEquals("abcdefg", PairtreeUtils.mapToID("ab/cd/ef/g/h/"));

        assertEquals("ab/cd/ef/g", PairtreeUtils.mapToPtPath("abcdefg"));
        assertEquals("12-986xy4", PairtreeUtils.mapToID("12/-9/86/xy/4"));

        assertEquals("13030_45xqv_793842495", PairtreeUtils.mapToID("13/03/0_/45/xq/v_/79/38/42/49/5"));
        assertEquals("13030_45xqv_793842495", PairtreeUtils.mapToID("13/03/0_/45/xq/v_/79/38/42/49/5/793842495"));
        assertEquals("13030_45xqv_793842495", PairtreeUtils.mapToID("/data",
                "/data/13/03/0_/45/xq/v_/79/38/42/49/5"));
        assertEquals("13030_45xqv_793842495", PairtreeUtils.mapToID("/data/",
                "/data/13/03/0_/45/xq/v_/79/38/42/49/5"));
        assertEquals("13030_45xqv_793842495", PairtreeUtils.mapToID("/data",
                "/data/13/03/0_/45/xq/v_/79/38/42/49/5/793842495"));
    }

    /**
     * Tests that an invalid Pairtree path isn't accepted when requesting the encapsulating directory; it fails if an
     * exception isn't thrown.
     *
     * @throws InvalidPtPathException If the supplied path isn't a valid Pairtree path
     */
    @Test(expected = InvalidPathException.class)
    public void testInvalidGetEncapsulatingDir() throws InvalidPathException {
        PairtreeUtils.getEncapsulatingDir("ab/cdx/efg/");
        PairtreeUtils.getEncapsulatingDir("abc");
    }

    /**
     * Tests mapping a Pairtree path to an ID, with ID encoding.
     *
     * @throws InvalidPtPathException If the supplied path isn't a valid Pairtree path
     */
    @Test
    public void testMapToIdWithIdEncoding() throws InvalidPathException {
        final String path = "ht/tp/+=/=n/2t/,i/nf/o=/ur/n+/nb/n+/se/+k/b+/re/po/s-/1";

        assertEquals("http://n2t.info/urn:nbn:se:kb:repos-1", PairtreeUtils.mapToID(path));
        assertEquals("ark:/13030/xt12t3", PairtreeUtils.mapToID("ar/k+/=1/30/30/=x/t1/2t/3"));
        assertEquals("what-the-*@?#!^!?", PairtreeUtils.mapToID("wh/at/-t/he/-^/2a/@^/3f/#!/^5/e!/^3/f"));
    }
}
