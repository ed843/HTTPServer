package com.ericduncandev.HTTP.interfaces;

public interface IHTTPParser {
    /**
     * Parses an incoming HTTP request from a client connection.
     * Processes the request line, headers, and body if present.
     * Handles different types of requests (GET, POST, PUT, DELETE) and their content types.
     * Supports parsing of multipart/form-data, application/json, and x-www-form-urlencoded content.
     */
    void parseRequest();
}
