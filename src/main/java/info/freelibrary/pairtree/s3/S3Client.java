
package info.freelibrary.pairtree.s3;

import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.StringUtils;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerFileUpload;
import io.vertx.core.streams.Pump;

public class S3Client {

    public static final String DEFAULT_ENDPOINT = "s3.amazonaws.com"; // test bad endpoint: s3-us-east-1.amazonaws.com

    private static final Logger LOGGER = LoggerFactory.getLogger(S3Client.class);

    private final String myAccessKey;

    private final String mySecretKey;

    private final String mySessionToken;

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
        this(aVertx, aAccessKey, aSecretKey, null, DEFAULT_ENDPOINT, -1, -1);
    }

    /**
     * Creates a new S3 client using the supplied access key, secret key, and endpoint.
     */
    public S3Client(final Vertx aVertx, final String aAccessKey, final String aSecretKey, final String aEndpoint) {
        this(aVertx, aAccessKey, aSecretKey, null, aEndpoint, -1, -1);
    }

    /**
     * Creates a new S3 client using the supplied access key, secret key, session token and endpoint.
     *
     * @param aVertx A Vert.x instance from which to create the <code>HttpClient</code>
     * @param aAccessKey An S3 access key
     * @param aSecretKey An S3 secret key
     * @param aSessionToken An S3 session token
     * @param aEndpoint An S3 endpoint
     * @param aIdleTimeout An idle timeout in seconds
     * @param aConnectTimeout A connect timeout in milliseconds
     */
    public S3Client(final Vertx aVertx, final String aAccessKey, final String aSecretKey, final String aSessionToken,
            final String aEndpoint, final int aIdleTimeout, final int aConnectTimeout) {
        final HttpClientOptions opts = new HttpClientOptions();

        myAccessKey = aAccessKey;
        mySecretKey = aSecretKey;
        mySessionToken = aSessionToken;

        if (StringUtils.trimToNull(aEndpoint) != null) {
            opts.setDefaultHost(aEndpoint);
        }

        if (aIdleTimeout != -1) {
            opts.setIdleTimeout(aIdleTimeout);
        }

        if (aConnectTimeout != -1) {
            opts.setConnectTimeout(aConnectTimeout);
        }

        myHTTPClient = aVertx.createHttpClient(opts);
    }

    /**
     * Direct asynchronous HEAD call.
     * <p>
     * <code>HEAD (bucket, key) -> handler(Data)</code>
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
     * <code>GET (bucket, key) -> handler(Data)</code>
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
     * <code>PUT (bucket, key, data) -> handler(Response)</code>
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
     * @param aUpload An HttpServerFileUpload
     */
    public void put(final String aBucket, final String aKey, final HttpServerFileUpload aUpload,
            final Handler<HttpClientResponse> aHandler) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("S3 request bucket: {}, key: {}", aBucket, aKey);
        }

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
     * Uploads the file contents to S3.
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aUpload An HttpServerFileUpload
     */
    public void put(final String aBucket, final String aKey, final HttpServerFileUpload aUpload, final long aFileSize,
            final Handler<HttpClientResponse> aHandler) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("S3 request bucket: {}, key: {}", aBucket, aKey);
        }

        final S3ClientRequest request = createPutRequest(aBucket, aKey, aHandler);
        final Buffer buffer = Buffer.buffer();

        request.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(aFileSize));

        aUpload.endHandler(event -> {
            request.end(buffer);
        });

        Pump.pump(aUpload, request).start();
    }

    /**
     * Deletes an S3 resource.
     * <p>
     * <code>DELETE (bucket, key) -> handler(Response)</code>
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
     * <code>create PUT -> requestObject</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @return An S3 PUT request
     */
    public S3ClientRequest createPutRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        final HttpClientRequest httpRequest = myHTTPClient.put("/" + aBucket + "/" + aKey, aHandler);
        return new S3ClientRequest("PUT", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Creates an S3 HEAD request.
     * <p>
     * <code>create HEAD -> request Object</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @return A S3 client HEAD request
     */
    public S3ClientRequest createHeadRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        final HttpClientRequest httpRequest = myHTTPClient.head("/" + aBucket + "/" + aKey, aHandler);
        return new S3ClientRequest("HEAD", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Creates an S3 GET request.
     * <p>
     * <code>create GET -> request Object</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler A response handler
     * @return A S3 client GET request
     */
    public S3ClientRequest createGetRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        final HttpClientRequest httpRequest = myHTTPClient.get("/" + aBucket + "/" + aKey, aHandler);
        return new S3ClientRequest("GET", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Creates an S3 DELETE request.
     * <p>
     * <code>create DELETE -> request Object</code>
     * </p>
     *
     * @param aBucket An S3 bucket
     * @param aKey An S3 key
     * @param aHandler An S3 handler
     * @return An S3 client request
     */
    public S3ClientRequest createDeleteRequest(final String aBucket, final String aKey,
            final Handler<HttpClientResponse> aHandler) {
        final HttpClientRequest httpRequest = myHTTPClient.delete("/" + aBucket + "/" + aKey, aHandler);
        return new S3ClientRequest("DELETE", aBucket, aKey, httpRequest, myAccessKey, mySecretKey, mySessionToken);
    }

    /**
     * Closes the S3 client.
     */
    public void close() {
        myHTTPClient.close();
    }

}