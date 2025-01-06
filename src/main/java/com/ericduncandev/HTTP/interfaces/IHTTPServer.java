package com.ericduncandev.HTTP.interfaces;

import com.ericduncandev.HTTP.core.HTTPServer;

public interface IHTTPServer {
    /**
     * Starts the HTTP server and begins accepting client connections.
     * Creates a server socket on the configured port and processes incoming requests
     * using a thread pool for concurrent connection handling.
     *
     * @throws HTTPServer.ServerInitializationException if the server fails to start
     */
    void start() throws HTTPServer.ServerInitializationException;

    /**
     * Gracefully stops the HTTP server.
     * Closes the server socket and shuts down the executor service,
     * preventing new connections while allowing existing ones to complete.
     */
    void stop();
}
