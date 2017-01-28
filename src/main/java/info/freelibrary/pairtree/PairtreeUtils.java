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

import static info.freelibrary.pairtree.Constants.BUNDLE_NAME;
import static info.freelibrary.pairtree.Constants.PATH_SEP;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

/**
 * Utilities for working with Pairtrees.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public final class PairtreeUtils {

    /** A logger for the Pairtree utilities class */
    private static final Logger LOGGER = LoggerFactory.getLogger(PairtreeUtils.class, BUNDLE_NAME);

    /** A colon */
    private static final char COLON = ':';

    /** A hex indicator */
    private static final char HEX_INDICATOR = '^';

    /** A period */
    private static final char PERIOD = '.';

    /** A comma */
    private static final char COMMA = ',';

    /** A plus sign which replaces a colon when encoding */
    private static final char PLUS_SIGN = '+';

    /** An equals sign which replaces a slash when encoding */
    private static final char EQUALS_SIGN = '=';

    /** Indicator that the Pairtree path only has one part */
    private static final int SINGLE_PART = 1;

    /** The Pairtree's separating character */
    private static Character mySeparator = File.separatorChar;

    /** Default length of Pairtree shorties */
    private static int myShortyLength = 2;

    /** A private constructor for this utility class */
    private PairtreeUtils() {
    }

    /**
     * Returns the Pairtree &quot;shorty&quot; length.
     *
     * @return The Pairtree &quot;shorty&quot; length
     */
    public static int getShortyLength() {
        return myShortyLength;
    }

    /**
     * Sets the Pairtree &quot;shorty&quot; length
     *
     * @param aLength A length to set as the &quot;shorty&quot; length
     */
    public static void setShortyLength(final int aLength) {
        myShortyLength = aLength;
    }

    /**
     * Returns the path separator character.
     *
     * @return The path separator character
     */
    public static Character getSeparator() {
        return mySeparator;
    }

    /**
     * Sets the path separator character.
     *
     * @param aSeparator The path separator character
     */
    public static void setSeparator(final Character aSeparator) {
        mySeparator = aSeparator;
    }

    /**
     * Maps the supplied ID to a Pairtree path.
     *
     * @param aID An ID to map to a Pairtree path
     * @return The Pairtree path for the supplied ID
     */
    public static String mapToPtPath(final String aID) {
        Objects.requireNonNull(aID);

        final String encodedID = encodeID(aID);
        final List<String> shorties = new ArrayList<>();

        int start = 0;

        while (start < encodedID.length()) {
            int end = start + myShortyLength;

            if (end > encodedID.length()) {
                end = encodedID.length();
            }

            shorties.add(encodedID.substring(start, end));
            start = end;
        }

        return concat(shorties.toArray(new String[shorties.size()]));
    }

    /**
     * Maps the supplied ID to a Pairtree path using the supplied base path.
     *
     * @param aID An ID to map to a Pairtree path
     * @param aBasePath The base path to use in the mapping
     * @return The Pairtree path for the supplied ID
     */
    public static String mapToPtPath(final String aBasePath, final String aID) {
        return concat(aBasePath, mapToPtPath(aID), null);
    }

    /**
     * Maps the supplied ID to a Pairtree path using the supplied base path.
     *
     * @param aID An ID to map to a Pairtree path
     * @param aBasePath The base path to use in the mapping
     * @param aEncapsulatedName The name of the encapsulating directory
     * @return The Pairtree path for the supplied ID
     */
    public static String mapToPtPath(final String aBasePath, final String aID, final String aEncapsulatedName) {
        final String ptPath;

        Objects.requireNonNull(aID);

        if (aEncapsulatedName == null) {
            ptPath = concat(aBasePath, mapToPtPath(aID));
        } else {
            ptPath = concat(aBasePath, mapToPtPath(aID), encodeID(aEncapsulatedName));
        }

        return ptPath;
    }

    /**
     * Maps the supplied base path to an ID using the supplied Pairtree path.
     *
     * @param aBasePath A base path to use for the mapping
     * @param aPtPath A Pairtree path to map to an ID
     * @return The ID that is a result of the mapping
     * @throws InvalidPathException If there is trouble mapping the path
     */
    public static String mapToID(final String aBasePath, final String aPtPath) throws InvalidPathException {
        final String newPath = removeBasePath(aBasePath, aPtPath);
        return mapToID(newPath);
    }

    /**
     * Maps the supplied base path to an ID.
     *
     * @param aPtPath A Pairtree path to map to an ID
     * @return The ID that is a result of the mapping
     * @throws InvalidPathException If there is trouble mapping the path
     */
    public static String mapToID(final String aPtPath) throws InvalidPathException {
        final String encapsulatingDir = getEncapsulatingDir(aPtPath);

        String id = aPtPath;

        if (id.endsWith(Character.toString(mySeparator))) {
            id = id.substring(0, id.length() - 1);
        }

        if (encapsulatingDir != null) {
            id = id.substring(0, id.length() - encapsulatingDir.length());
        }

        id = id.replace(Character.toString(mySeparator), "");
        id = decodeID(id);

        return id;
    }

    /**
     * Extracts the encapsulating directory from the supplied Pairtree path, using the supplied base path.
     *
     * @param aBasePath A base path for the Pairtree path
     * @param aPtPath The Pairtree path
     * @return The name of the encapsulating directory
     * @throws InvalidPathException If there is a problem extracting the encapsulating directory
     */
    public static String getEncapsulatingDir(final String aBasePath, final String aPtPath) throws InvalidPathException {
        return getEncapsulatingDir(removeBasePath(aBasePath, aPtPath));
    }

    /**
     * Extracts the encapsulating directory from the supplied Pairtree path.
     *
     * @param aPtPath The Pairtree path from which to extract the encapsulating directory
     * @return The name of the encapsulating directory
     * @throws InvalidPathException If there is a problem extracting the encapsulating directory
     */
    public static String getEncapsulatingDir(final String aPtPath) throws InvalidPathException {
        Objects.requireNonNull(aPtPath, LOGGER.getMessage(MessageCodes.PT_003));

        // Walk the Pairtree path looking for first non-shorty
        final String[] pPathParts = aPtPath.split("\\" + mySeparator);

        // If there is only 1 part
        if (pPathParts.length == SINGLE_PART) {
            // If part <= shorty length then no encapsulating directory
            if (pPathParts[0].length() <= myShortyLength) {
                return null;
            } else {
                // Else no Pairtree path
                throw new InvalidPathException(MessageCodes.PT_001, aPtPath);
            }
        }

        // All parts up to next to last and last should have shorty length
        for (int index = 0; index < pPathParts.length - 2; index++) {
            if (pPathParts[index].length() != myShortyLength) {
                throw new InvalidPathException(MessageCodes.PT_002, myShortyLength, pPathParts[index].length(),
                        aPtPath);
            }
        }

        final String nextToLastPart = pPathParts[pPathParts.length - 2];

        // Next to last should have shorty length or less
        if (nextToLastPart.length() > myShortyLength) {
            throw new InvalidPathException(MessageCodes.PT_005, aPtPath);
        }

        String lastPart = pPathParts[pPathParts.length - 1];

        // If next to last has shorty length
        if (nextToLastPart.length() == myShortyLength) {
            // If last has length > shorty length then encapsulating directory
            if (lastPart.length() > myShortyLength) {
                lastPart = decodeID(lastPart);
            } else {
                // Else no encapsulating directory
                lastPart = null;
            }
        }

        // Else last is encapsulating directory
        return lastPart == null ? null : decodeID(lastPart);
    }

    /**
     * Concatenates the Pairtree paths varargs.
     *
     * @param aPathsVarargs The Pairtree paths varargs
     * @return The concatenated Pairtree paths
     */
    private static String concat(final String... aPathsVarargs) {
        final String path;

        if (aPathsVarargs == null || aPathsVarargs.length == 0) {
            path = null;
        } else {
            final StringBuffer pathBuf = new StringBuffer();

            Character lastChar = null;

            for (final String aPathsVararg : aPathsVarargs) {
                if (aPathsVararg != null) {
                    final int length;

                    if (lastChar != null && !mySeparator.equals(lastChar)) {
                        pathBuf.append(mySeparator);
                    }

                    pathBuf.append(aPathsVararg);
                    length = aPathsVararg.length();
                    lastChar = aPathsVararg.charAt(length - 1);
                }
            }

            path = pathBuf.toString();
        }

        return path;
    }

    /**
     * Removes the supplied Pairtree prefix from the supplied ID.
     *
     * @param aPrefix A Pairtree prefix
     * @param aID An ID
     * @return The ID without the Pairtree prefix prepended to it
     * @throws PairtreeRuntimeException If the supplied prefix or ID is null
     */
    public static String removePrefix(final String aPrefix, final String aID) {
        Objects.requireNonNull(aPrefix, LOGGER.getMessage(MessageCodes.PT_006));
        Objects.requireNonNull(aID, LOGGER.getMessage(MessageCodes.PT_004));

        final String id;

        if (aID.indexOf(aPrefix) == 0) {
            id = aID.substring(aPrefix.length());
        } else {
            id = aID;
        }

        return id;
    }

    /**
     * Removes the base path from the supplied Pairtree path.
     *
     * @param aBasePath A base path for a Pairtree path
     * @param aPtPath A Pairtree path
     * @return The Pairtree path without the base path
     * @throws PairtreeRuntimeException If the supplied base path or Pairtree path are null
     */
    public static String removeBasePath(final String aBasePath, final String aPtPath) {
        Objects.requireNonNull(aBasePath, LOGGER.getMessage(MessageCodes.PT_007));
        Objects.requireNonNull(aPtPath, LOGGER.getMessage(MessageCodes.PT_003));

        String newPath = aPtPath;

        if (aPtPath.startsWith(aBasePath)) {
            newPath = newPath.substring(aBasePath.length());

            if (newPath.startsWith(Character.toString(mySeparator))) {
                newPath = newPath.substring(1);
            }
        }

        return newPath;
    }

    /**
     * Cleans an ID for use in a Pairtree path.
     *
     * @param aID An idea to be cleaned
     * @return The cleaned ID for use in a Pairtree path
     * @throws PairtreeRuntimeException If the supplied ID is null
     */
    public static String encodeID(final String aID) {
        Objects.requireNonNull(aID, LOGGER.getMessage(MessageCodes.PT_004));

        final byte[] bytes;

        try {
            bytes = aID.getBytes("utf-8");
        } catch (final UnsupportedEncodingException details) {
            throw new PairtreeRuntimeException(MessageCodes.PT_008, details);
        }

        final StringBuffer idBuffer = new StringBuffer();

        for (final byte b : bytes) {
            final int i = b & 0xff;

            if (i < 0x21 || i > 0x7e || i == 0x22 || i == 0x2a || i == 0x2b || i == 0x2c || i == 0x3c || i == 0x3d ||
                    i == 0x3e || i == 0x3f || i == 0x5c || i == 0x5e || i == 0x7c) {
                // Encode
                idBuffer.append(HEX_INDICATOR);
                idBuffer.append(Integer.toHexString(i));
            } else {
                // Don't encode
                final char[] chars = Character.toChars(i);

                assert chars.length == 1;
                idBuffer.append(chars[0]);
            }
        }

        for (int index = 0; index < idBuffer.length(); index++) {
            final char character = idBuffer.charAt(index);

            // Encode characters that need to be encoded according to Pairtree specification
            if (character == PATH_SEP) {
                idBuffer.setCharAt(index, EQUALS_SIGN);
            } else if (character == COLON) {
                idBuffer.setCharAt(index, PLUS_SIGN);
            } else if (character == PERIOD) {
                idBuffer.setCharAt(index, COMMA);
            }
        }

        return idBuffer.toString();
    }

    /**
     * Unclean the ID from the Pairtree path.
     *
     * @param aID A cleaned ID to unclean
     * @return The unclean ID
     */
    public static String decodeID(final String aID) {
        Objects.requireNonNull(aID, LOGGER.getMessage(MessageCodes.PT_004));

        final StringBuilder idBuf = new StringBuilder();

        for (int index = 0; index < aID.length(); index++) {
            final char character = aID.charAt(index);

            // Decode characters that need to be decoded according to Pairtree specification
            if (character == EQUALS_SIGN) {
                idBuf.append(PATH_SEP);
            } else if (character == PLUS_SIGN) {
                idBuf.append(COLON);
            } else if (character == COMMA) {
                idBuf.append(PERIOD);
            } else if (character == HEX_INDICATOR) {
                /* Get the next two characters since they are hex characters */
                final String hex = aID.substring(index + 1, index + 3);
                final char[] chars = Character.toChars(Integer.parseInt(hex, 16));

                assert chars.length == 1;

                idBuf.append(chars[0]);
                index = index + 2;
            } else {
                idBuf.append(character);
            }
        }

        return idBuf.toString();
    }

}
