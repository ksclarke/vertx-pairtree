
package info.freelibrary.pairtree;

/**
 * Constants defined for use by the pairtree library.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public final class Constants {

    /** A bundle name for I18N messages */
    public static final String BUNDLE_NAME = "pairtree_messages";

    /** A hex indicator */
    public static final char HEX_INDICATOR = '^';

    /** A colon */
    public static final char COLON = ':';

    /** A semicolon */
    public static final char SEMICOLON = ';';

    /** A period */
    public static final char PERIOD = '.';

    /** A slash */
    public static final char SLASH = '/';

    /** A comma */
    public static final char COMMA = ',';

    /** A plus sign which replaces a colon when encoding */
    public static final char PLUS_SIGN = '+';

    /** An equals sign which replaces a slash when encoding */
    public static final char EQUALS_SIGN = '=';

    /** OK HTTP response code */
    public static final int OK = 200;

    /** Not Found HTTP response code */
    public static final int NOT_FOUND = 404;

    /** No content HTTP response code */
    public static final int NO_CONTENT = 204;

    /** Forbidden HTTP response code */
    public static final int FORBIDDEN = 403;

    /** System-independent end of line */
    public static final String EOL = "\n";

    /** An empty private constructor for this utility class */
    private Constants() {
    }

}
