
package info.freelibrary.pairtree.s3;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.StringJoiner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;

public class S3ClientRequest implements HttpClientRequest {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ClientRequest.class);

    private final HttpClientRequest myRequest;

    private final String myMethod;

    private final String myBucket;

    private final String myKey;

    private String myContentMd5;

    private String myContentType;

    private String myAccessKey;

    private String mySecretKey;

    private final String mySessionToken;

    /**
     * Creates a new S3 client request.
     *
     * @param aMethod An HTTP method
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aRequest A HttpClientRequest
     */
    public S3ClientRequest(final String aMethod, final String aBucket, final String aKey,
            final HttpClientRequest aRequest) {
        this(aMethod, aBucket, aKey, aRequest, null, null, null);
    }

    /**
     * Creates a new S3 client request.
     *
     * @param aMethod An HTTP method
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aRequest A HttpClientRequest
     */
    public S3ClientRequest(final String aMethod, final String aBucket, final String aKey,
            final HttpClientRequest aRequest, final String aAccessKey, final String aSecretKey,
            final String aSessionToken) {
        this(aMethod, aBucket, aKey, aRequest, aAccessKey, aSecretKey, aSessionToken, "", "");
    }

    /**
     * Creates a new S3 client request.
     *
     * @param aMethod An HTTP method
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aRequest A HttpClientRequest
     */
    public S3ClientRequest(final String aMethod, final String aBucket, final String aKey,
            final HttpClientRequest aRequest, final String aAccessKey, final String aSecretKey,
            final String aSessionToken, final String aContentMd5, final String aContentType) {
        myMethod = aMethod;
        myBucket = aBucket;
        myKey = aKey;
        myRequest = aRequest;
        myAccessKey = aAccessKey;
        mySecretKey = aSecretKey;
        mySessionToken = aSessionToken;
        myContentMd5 = aContentMd5;
        myContentType = aContentType;
    }

    @Override
    public HttpClientRequest setWriteQueueMaxSize(final int aMaxSize) {
        return myRequest.setWriteQueueMaxSize(aMaxSize);
    }

    @Override
    public HttpClientRequest handler(final Handler<HttpClientResponse> aHandler) {
        return myRequest.handler(aHandler);
    }

    @Override
    public boolean writeQueueFull() {
        return myRequest.writeQueueFull();
    }

    @Override
    public HttpClientRequest drainHandler(final Handler<Void> aHandler) {
        return myRequest.drainHandler(aHandler);
    }

    @Override
    public HttpClientRequest exceptionHandler(final Handler<Throwable> aHandler) {
        return myRequest.exceptionHandler(aHandler);
    }

    @Override
    public HttpClientRequest setChunked(final boolean aChunked) {
        return myRequest.setChunked(aChunked);
    }

    @Override
    public MultiMap headers() {
        return myRequest.headers();
    }

    @Override
    public HttpClientRequest pause() {
        return myRequest.pause();
    }

    @Override
    public HttpClientRequest resume() {
        return myRequest.resume();
    }

    @Override
    public HttpClientRequest endHandler(final Handler<Void> aEndHandler) {
        return myRequest.endHandler(aEndHandler);
    }

    @Override
    public boolean isChunked() {
        return myRequest.isChunked();
    }

    @Override
    public HttpMethod method() {
        return myRequest.method();
    }

    @Override
    public String uri() {
        return myRequest.uri();
    }

    @Override
    public HttpClientRequest putHeader(final String aName, final String aValue) {
        return myRequest.putHeader(aName, aValue);
    }

    @Override
    public HttpClientRequest putHeader(final CharSequence aName, final CharSequence aValue) {
        return myRequest.putHeader(aName, aValue);
    }

    @Override
    public HttpClientRequest putHeader(final String aName, final Iterable<String> aValues) {
        return myRequest.putHeader(aName, aValues);
    }

    @Override
    public HttpClientRequest putHeader(final CharSequence aName, final Iterable<CharSequence> aValues) {
        return myRequest.putHeader(aName, aValues);
    }

    @Override
    public HttpClientRequest setTimeout(final long aTimeoutMs) {
        return myRequest.setTimeout(aTimeoutMs);
    }

    @Override
    public HttpClientRequest write(final Buffer aChunk) {
        return myRequest.write(aChunk);
    }

    @Override
    public HttpClientRequest write(final String aChunk) {
        return myRequest.write(aChunk);
    }

    @Override
    public HttpClientRequest write(final String aChunk, final String aEncoding) {
        return myRequest.write(aChunk, aEncoding);
    }

    @Override
    public HttpClientRequest continueHandler(final Handler<Void> aHandler) {
        return myRequest.continueHandler(aHandler);
    }

    @Override
    public HttpClientRequest sendHead() {
        initAuthenticationHeader();
        return myRequest.sendHead();
    }

    @Override
    public void end(final String aChunk) {
        initAuthenticationHeader();
        myRequest.end(aChunk);
    }

    @Override
    public void end(final String aChunk, final String aEncoding) {
        initAuthenticationHeader();
        myRequest.end(aChunk, aEncoding);
    }

    @Override
    public void end(final Buffer aChunk) {
        initAuthenticationHeader();
        myRequest.end(aChunk);
    }

    @Override
    public void end() {
        initAuthenticationHeader();
        myRequest.end();
    }

    protected void initAuthenticationHeader() {
        if (isAuthenticated()) {
            // Calculate the signature
            // http://docs.amazonwebservices.com/AmazonS3/latest/dev/RESTAuthentication.html
            // #ConstructingTheAuthenticationHeader

            // Date should look like Thu, 17 Nov 2005 18:49:58 GMT, and must be
            // within 15 min of S3 server time. contentMd5 and type are optional

            // We can't risk letting our date get clobbered and being inconsistent
            final String xamzdate = currentDateString();

            headers().add("X-Amz-Date", xamzdate);

            if (!isSessionTokenBlank()) {
                headers().add("X-Amz-Security-Token", mySessionToken);
            }

            final StringJoiner canonicalizedAmzHeadersBuilder = new StringJoiner("\n", "", "\n");

            canonicalizedAmzHeadersBuilder.add("x-amz-date:" + xamzdate);

            if (!isSessionTokenBlank()) {
                canonicalizedAmzHeadersBuilder.add("x-amz-security-token:" + mySessionToken);
            }

            final String canonicalizedAmzHeaders = canonicalizedAmzHeadersBuilder.toString();
            final String canonicalizedResource = "/" + myBucket + "/" + (myKey.startsWith("?") ? "" : myKey);

            // Skipping the date, we'll use the x-amz date instead
            final String toSign = myMethod + "\n" + myContentMd5 + "\n" + myContentType + "\n\n" +
                    canonicalizedAmzHeaders + canonicalizedResource;

            String signature;

            try {
                signature = b64SignHmacSha1(mySecretKey, toSign);
            } catch (InvalidKeyException | NoSuchAlgorithmException details) {
                signature = "ERRORSIGNATURE";
                // This will totally fail, but downstream users can handle it
                LOGGER.error("Failed to sign S3 request due to " + details);
            }

            final String authorization = "AWS" + " " + myAccessKey + ":" + signature;

            // Put that nasty authentication string in the headers and let vert.x deal
            headers().add("Authorization", authorization);
        }
    }

    private boolean isSessionTokenBlank() {
        return mySessionToken == null || mySessionToken.trim().length() == 0;
    }

    /**
     * Returns whether the request is authenticated.
     *
     * @return True if request is authenticated; else, false
     */
    public boolean isAuthenticated() {
        return myAccessKey != null && mySecretKey != null;
    }

    /**
     * Sets an access key for the request.
     *
     * @param aAccessKey An S3 access key
     */
    public void setAccessKey(final String aAccessKey) {
        myAccessKey = aAccessKey;
    }

    /**
     * Sets a secret key for the request.
     *
     * @param aSecretKey An S3 secret key
     */
    public void setSecretKey(final String aSecretKey) {
        mySecretKey = aSecretKey;
    }

    /**
     * Gets the HTTP method to be used with the request.
     *
     * @return The HTTP method to be used with the request
     */
    public String getMethod() {
        return myMethod;
    }

    /**
     * Gets the content MD5.
     *
     * @return The content MD5
     */
    public String getContentMd5() {
        return myContentMd5;
    }

    /**
     * Sets the content MD5.
     *
     * @param aContentMd5 An MD5 value for the content
     */
    public void setContentMd5(final String aContentMd5) {
        myContentMd5 = aContentMd5;
    }

    /**
     * Gets the content type for the request.
     *
     * @return The content type for the request
     */
    public String getContentType() {
        return myContentType;
    }

    /**
     * Sets the content type for the request
     *
     * @param aContentType The content type for the request
     */
    public void setContentType(final String aContentType) {
        myContentType = aContentType;
    }

    private static String b64SignHmacSha1(final String aAwsSecretKey, final String aCanonicalString)
            throws NoSuchAlgorithmException, InvalidKeyException {
        final SecretKeySpec signingKey = new SecretKeySpec(aAwsSecretKey.getBytes(), "HmacSHA1");
        final Mac mac = Mac.getInstance("HmacSHA1");

        mac.init(signingKey);

        return new String(Base64.getEncoder().encode(mac.doFinal(aCanonicalString.getBytes())));
    }

    private static String currentDateString() {
        return dateFormat.format(new Date());
    }

    @Override
    public HttpConnection connection() {
        return myRequest.connection();
    }

    @Override
    public HttpClientRequest connectionHandler(final Handler<HttpConnection> aHandler) {
        return myRequest.connectionHandler(aHandler);
    }

    @Override
    public String getHost() {
        return myRequest.getHost();
    }

    @Override
    public String getRawMethod() {
        return myRequest.getRawMethod();
    }

    @Override
    public String path() {
        return myRequest.path();
    }

    @Override
    public HttpClientRequest pushHandler(final Handler<HttpClientRequest> aHandler) {
        return myRequest.pushHandler(aHandler);
    }

    @Override
    public String query() {
        return myRequest.query();
    }

    @Override
    public void reset(final long aCode) {
        myRequest.reset(aCode);
    }

    @Override
    public HttpClientRequest sendHead(final Handler<HttpVersion> aHandler) {
        return myRequest.sendHead(aHandler);
    }

    @Override
    public HttpClientRequest setHost(final String aHost) {
        return myRequest.setHost(aHost);
    }

    @Override
    public HttpClientRequest setRawMethod(final String aMethod) {
        return myRequest.setRawMethod(aMethod);
    }

    @Override
    public HttpClientRequest writeCustomFrame(final int aType, final int aFlagsInt, final Buffer aPayload) {
        return myRequest.writeCustomFrame(aType, aFlagsInt, aPayload);
    }

}
