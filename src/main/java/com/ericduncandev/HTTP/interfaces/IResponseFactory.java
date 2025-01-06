package com.ericduncandev.HTTP.interfaces;

import com.ericduncandev.HTTP.model.HTTPResponse;

/**
 * Interface for creating HTTP response objects with various status codes and content.
 */
public interface IResponseFactory {
    /**
     * Creates a 200 OK response with body and content type.
     * @param body The response body as byte array
     * @param contentType The MIME type of the content
     * @return HTTPResponse object
     */
    HTTPResponse ok(byte[] body, String contentType);

    /**
     * Creates a 201 Created response with body, content type, and location.
     * @param body The response body as byte array
     * @param contentType The MIME type of the content
     * @param location The URI of the created resource
     * @return HTTPResponse object
     */
    HTTPResponse created(byte[] body, String contentType, String location);

    /**
     * Creates a 204 No Content response with content location.
     * @param contentLocation The location of the content
     * @return HTTPResponse object
     */
    HTTPResponse noContent(String contentLocation);

    /**
     * Creates a 400 Bad Request response with body and content type.
     * @param body The response body as byte array
     * @param contentType The MIME type of the content
     * @return HTTPResponse object
     */
    HTTPResponse badRequest(byte[] body, String contentType);

    /**
     * Creates a 400 Bad Request response with error message.
     * @param message The error message
     * @return HTTPResponse object
     */
    HTTPResponse badRequest(String message);

    /**
     * Creates a 403 Forbidden response with error message.
     * @param message The error message
     * @return HTTPResponse object
     */
    HTTPResponse forbidden(String message);

    /**
     * Creates a 404 Not Found response.
     * @return HTTPResponse object
     */
    HTTPResponse notFound();

    /**
     * Creates a 500 Internal Server Error response with error message.
     * @param message The error message
     * @return HTTPResponse object
     */
    HTTPResponse serverError(String message);

    /**
     * Creates a 500 Internal Server Error response with body and content type.
     * @param body The response body as byte array
     * @param contentType The MIME type of the content
     * @return HTTPResponse object
     */
    HTTPResponse serverError(byte[] body, String contentType);

    /**
     * Creates a 503 Service Unavailable response.
     * @return HTTPResponse object
     */
    HTTPResponse serviceUnavailable();
}
