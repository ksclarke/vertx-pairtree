
package info.freelibrary.pairtree.s3;

import info.freelibrary.vertx.s3.Profile;

/**
 * An S3 profile that should be used to get S3 credentials.
 */
public class S3Profile extends Profile {

    /**
     * Creates a new S3 profile.
     *
     * @param aProfileName An AWS profile name
     */
    public S3Profile(final String aProfileName) {
        super(aProfileName);
    }

}
