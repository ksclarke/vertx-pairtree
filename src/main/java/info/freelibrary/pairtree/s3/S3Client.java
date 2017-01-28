
package info.freelibrary.pairtree.s3;

import static info.freelibrary.pairtree.Constants.PATH_SEP;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerFileUpload;

/**
 * An S3 client implementation used by the S3Pairtree object.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class S3Client {

    // TODO: test for a bad endpoint, for example: s3-us-east-1.amazonaws.com
    /** Default S3 endpoint */
    public static final String DEFAULT_ENDPOINT = "s3.amazonaws.com";

    /** AWS access key */
    private final String myAccessKey;

    /** AWS secret key */
    private final String mySecretKey;

    /** S3 session token */
    private final String mySessionToken;

    /** HTTP client used to interact with S3 */
    private final HttpClient myHTTPClient;

    /**
     * Creates a new S3 client.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     */
    public S3Client(final Vertx aVertx) {
        this(aVertx, null, null);
    }

    /**
     * Creates a new S3 client using the supplied access and secret keys.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     */
    public S3Client(final Vertx aVertx, final String aAccessKey, final String aSecretKey) {
        this(aVertx, aAccessKey, aSecretKey, null, new HttpClientOptions().setDefaultHost(DEFAULT_ENDPOINT));
    }

    /**
     * Creates a new S3 client using the supplied access key, secret key, and endpoint.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aEndpoint An S3 endpoint
     */
    public S3Client(final Vertx aVertx, final String aAccessKey, final String aSecretKey, final String aEndpoint) {
        this(aVertx, aAccessKey, aSecretKey, null, new HttpClientOptions().setDefaultHost(aEndpoint));

    }

    /**
     * Creates a new S3 client using the supplied access key, secret key, session token and endpoint.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aSessionToken An S3 session token
     * @param aConfig A configuration of HTTP options to use when connecting
     */
    public S3Client(final Vertx aVertx, final String aAccessKey, final String aSecretKey, final String aSessionToken,
            final HttpClientOptions aConfig) {

        myAccessKey = aAccessKey;
        mySecretKey = aSecretKey;
        mySessionToken = aSessionToken;

        myHTTPClient = aVertx.createHttpClient(aConfig);
    }

    /**
     * Direct asynchronous HEAD call.
     * <p>
     * <code>HEAD (bucket, key) -&gt; handler(Data)</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void head(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler) {
        createHeadRequest(aBucket, aKey, aHandler).end();
    }

    /**
     * Direct asynchronous GET call.
     * <p>
     * <code>GET (bucket, key) -&gt; handler(Data)</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void get(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler) {
        createGetRequest(aBucket, aKey, aHandler).end();
    }

    /**
     * Asynchronous listing of an S3 bucket.
     *
     * @param aBucket A bucket from which to get a listing
     * @param aHandler A response handler
     */
    public void list(final String aBucket, final Handler<HttpClientResponse> aHandler) {
        createGetRequest(aBucket, "?list-type=2", aHandler).end();
    }

    /**
     * Asynchronous listing of an S3 bucket using the supplied prefix.
     *
     * @param aBucket An S3 bucket
     * @param aPrefix A prefix to use to limit which objects are listed
     * @param aHandler A response handler
     */
    public void list(final String aBucket, final String aPrefix, final Handler<HttpClientResponse> aHandler) {
        createGetRequest(aBucket, "?list-type=2&prefix=" + aPrefix, aHandler).end();
    }

    /**
     * Direct asynchronous PUT call.
     * <p>
     * <code>PUT (bucket, key, data) -&gt; handler(Response)</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aBuffer A data buffer
     * @param aHandler A response handler
     */
    public void put(final String aBucket, final String aKey, final Buffer aBuffer,
            final Handler<HttpClientResponse> aHandler) {
        createPutRequest(aBucket, aKey, aHandler).end(aBuffer);
    }

    /**
     * Uploads the file contents to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aFile A file to upload
     * @param aHandler A response handler for the upload
     */
    public void put(final String aBucket, final String aKey, final AsyncFile aFile,
            final Handler<HttpClientResponse> aHandler) {
        final S3ClientRequest request = createPutRequest(aBucket, aKey, aHandler);
        final Buffer buffer = Buffer.buffer();

        aFile.endHandler(event -> {
            request.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
            request.end(buffer);
        });

        aFile.handler(data -> {
            buffer.appendBuffer(data);
        });
    }

    /**
     * Uploads the file contents to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aUpload An HttpServerFileUpload
     * @param aHandler An upload response handler
     */
    public void put(final String aBucket, final String aKey, final HttpServerFileUpload aUpload,
            final Handler<HttpClientResponse> aHandler) {
        final S3ClientRequest request = createPutRequest(aBucket, aKey, aHandler);
        final Buffer buffer = Buffer.buffer();

        aUpload.endHandler(event -> {
            request.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
            request.end(buffer);
        });

        aUpload.handler(data -> {
            buffer.appendBuffer(data);
        });
    }

    /**
     * Deletes an S3 resource.
     * <p>
     * <code>DELETE (bucket, key) -&gt; handler(Response)</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     */
    public void delete(final String aBucket, final String aKey, final Handler<HttpClientResponse> aHandler) {
        createDeleteRequest(aBucket, aKey, aHandler).end();
    }

    /**
     * Creates an S3 PUT request.
     * <p>
     * <code>create PUT -&gt; requestObject</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @return An S3 PUT request
     */
    public S3ClientRequest createPutRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        final HttpClientRequest httpRequest = myHTTPClient.put(PATH_SEP + aBucket + PATH_SEP + aKey, aHandler);
        return new S3ClientRequest("PUT", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Creates an S3 HEAD request.
     * <p>
     * <code>create HEAD -&gt; request Object</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @return A S3 client HEAD request
     */
    public S3ClientRequest createHeadRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        final HttpClientRequest httpRequest = myHTTPClient.head(PATH_SEP + aBucket + PATH_SEP + aKey, aHandler);
        return new S3ClientRequest("HEAD", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Creates an S3 GET request.
     * <p>
     * <code>create GET -&gt; request Object</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @return A S3 client GET request
     */
    public S3ClientRequest createGetRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        final HttpClientRequest httpRequest = myHTTPClient.get(PATH_SEP + aBucket + PATH_SEP + aKey, aHandler);
        return new S3ClientRequest("GET", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Creates an S3 DELETE request.
     * <p>
     * <code>create DELETE -&gt; request Object</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler An S3 handler
     * @return An S3 client request
     */
    public S3ClientRequest createDeleteRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        final HttpClientRequest httpRequest = myHTTPClient.delete(PATH_SEP + aBucket + PATH_SEP + aKey, aHandler);
        return new S3ClientRequest("DELETE", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Closes the S3 client.
     */
    public void close() {
        myHTTPClient.close();
    }

}
