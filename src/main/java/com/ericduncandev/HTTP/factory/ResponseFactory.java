package com.ericduncandev.HTTP.factory;

import com.ericduncandev.HTTP.interfaces.IResponseFactory;
import com.ericduncandev.HTTP.model.HTTPResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public final class ResponseFactory {
    private static final String DEFAULT_PROTOCOL = "HTTP/1.1";
    private static final Logger logger = LogManager.getLogger(ResponseFactory.class);


    /**
     * Creates a 200 OK response with body and content type.
     * @param body The response body as byte array
     * @param contentType The MIME type of the content
     * @return HTTPResponse object
     */
    public static HTTPResponse ok(byte[] body, String contentType) {
        logger.debug("200 OK");
        return new HTTPResponse.Builder()
                .statusCode(200)
                .body(body)
                .contentType(contentType)
                .build();
    }

    /**
     * Creates a 201 Created response with body, content type, and location.
     * @param body The response body as byte array
     * @param contentType The MIME type of the content
     * @param location The URI of the created resource
     * @return HTTPResponse object
     */
    public static HTTPResponse created(byte[] body, String contentType, String location) {
        logger.debug("201 Created");
        return new HTTPResponse.Builder()
                .statusCode(201)
                .contentType(contentType)
                .location(location)
                .body(body)
                .build();
    }

    /**
     * Creates a 204 No Content response with content location.
     * @param contentLocation The location of the content
     * @return HTTPResponse object
     */
    public static HTTPResponse noContent(String contentLocation) {
        logger.debug("204 No Content");
        return new HTTPResponse.Builder()
                .statusCode(204)
                .contentLocation(contentLocation)
                .build();
    }

    /**
     * Creates a 400 Bad Request response with body and content type.
     * @param body The response body as byte array
     * @param contentType The MIME type of the content
     * @return HTTPResponse object
     */
    public static HTTPResponse badRequest(byte[] body, String contentType) {
        logger.warn("400 Bad Request");
        return new HTTPResponse.Builder()
                .statusCode(400)
                .body(body)
                .contentType(contentType)
                .build();
    }

    /**
     * Creates a 400 Bad Request response with error message.
     * @param message The error message
     * @return HTTPResponse object
     */
    public static HTTPResponse badRequest(String message) {
        logger.warn("400 Bad Request");
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", "Bad Request");
        errorJson.put("message", message);

        return new HTTPResponse.Builder()
                .statusCode(400)
                .body(errorJson.toString().getBytes())
                .contentType("application/json")
                .build();
    }


    /**
     * Creates a 403 Forbidden response with error message.
     * @param message The error message
     * @return HTTPResponse object
     */
    public static HTTPResponse forbidden(String message) {
        logger.warn("403 Forbidden");
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", "InsufficientPermissions");
        errorJson.put("message", message);


        return new HTTPResponse.Builder()
                .statusCode(403)
                .contentType("application/json")
                .body(errorJson.toString().getBytes())
                .build();


    }



    /**
     * Creates a 404 Not Found response.
     * @return HTTPResponse object
     */
    public static HTTPResponse notFound() {
        logger.warn("404 Not Found");
        String body = "<html><body><h1>404 Not Found</h1></body></html>";
        return new HTTPResponse.Builder()
                .statusCode(404)
                .body(body.getBytes(StandardCharsets.UTF_8))
                .contentType("text/html")
                .build();
    }

    /**
     * Creates a 500 Internal Server Error response with error message.
     * @param message The error message
     * @return HTTPResponse object
     */
    public static HTTPResponse serverError(String message) {
        logger.error("500 Internal Server Error");
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", "Server error");
        errorJson.put("message", message);

        return new HTTPResponse.Builder()
                .statusCode(500)
                .body(errorJson.toString().getBytes())
                .contentType("application/json")
                .build();
    }

    /**
     * Creates a 500 Internal Server Error response with body and content type.
     * @param body The response body as byte array
     * @param contentType The MIME type of the content
     * @return HTTPResponse object
     */
    public static HTTPResponse serverError(byte[] body, String contentType) {
        logger.error("500 Internal Server Error");
        return new HTTPResponse.Builder()
                .statusCode(500)
                .body(body)
                .contentType(contentType)
                .build();
    }

    /**
     * Creates a 503 Service Unavailable response.
     * @return HTTPResponse object
     */
    public static HTTPResponse serviceUnavailable() {
        logger.error("503 Service Unavailable");
        String body = "<html><body>" +
                "<h1>503 Service Unavailable</h1>" +
                "<p>The server is currently unable to handle the request due to temporary overloading or maintenance.</p>" +
                "</body></html>";
        return new HTTPResponse.Builder()
                .statusCode(503)
                .body(body.getBytes(StandardCharsets.UTF_8))
                .contentType("text/html")
                .header("Retry-After", "60")  // Suggests client to retry after 60 seconds
                .build();
    }
}