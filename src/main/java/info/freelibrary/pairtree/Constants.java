
package info.freelibrary.pairtree;

/**
 * Constants defined for use by the pairtree library.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public final class Constants {

    /** A bundle name for I18N messages */
    public static final String BUNDLE_NAME = "pairtree_messages";

    /** OK HTTP response code */
    public static final int OK = 200;

    /** Not Found HTTP response code */
    public static final int NOT_FOUND = 404;

    /** No content HTTP response code */
    public static final int NO_CONTENT = 204;

    /** Forbidden HTTP response code */
    public static final int FORBIDDEN = 403;

    /** Content-Length HTTP header */
    public static final String CONTENT_LENGTH = "Content-Length";

    /** A system independent path separator */
    public static final char PATH_SEP = '/';

    /** An empty private constructor for this utility class */
    private Constants() {
    }

}
