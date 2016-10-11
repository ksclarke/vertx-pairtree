
package info.freelibrary.pairtree.s3;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX handler for S3's ObjectList response.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class ObjectListHandler extends DefaultHandler {

    final List<String> myKeys = new ArrayList<>();

    final StringBuilder myKeyText = new StringBuilder();

    String myLastElement;

    @Override
    public void characters(final char[] aCharArray, final int aStart, final int aLength) throws SAXException {
        if (myLastElement.equals("Key")) {
            myKeyText.append(aCharArray, aStart, aLength);
        }
    }

    @Override
    public void startElement(final String aURI, final String aLocalName, final String aQName,
            final Attributes aAttributes) throws SAXException {
        if (aLocalName.equals("Key")) {
            myKeyText.delete(0, myKeyText.length());
        }

        myLastElement = aLocalName;
    }

    @Override
    public void endElement(final String aURI, final String aLocalName, final String aQName) {
        if (aLocalName.equals("Key")) {
            myKeys.add(myKeyText.toString());
        }
    }

    /**
     * Gets the S3 keys returned by a List Objects command.
     *
     * @return The S3 keys returned by a List Objects command
     */
    public List<String> getKeys() {
        return myKeys;
    }

}
