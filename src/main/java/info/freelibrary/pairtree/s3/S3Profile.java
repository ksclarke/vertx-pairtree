
package info.freelibrary.pairtree.s3;

import info.freelibrary.vertx.s3.AwsProfile;

/**
 * An S3 profile that should be used to get S3 credentials.
 */
public class S3Profile extends AwsProfile {

    /**
     * Creates a new S3 profile.
     *
     * @param aProfileName An AWS profile name
     */
    public S3Profile(final String aProfileName) {
        super(aProfileName);
    }

}
