package com.ericduncandev.HTTP.core;

import com.ericduncandev.HTTP.parser.HTTPParser;
import com.ericduncandev.HTTP.interfaces.IHTTPServer;
import com.ericduncandev.HTTP.factory.ResponseFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class HTTPServer implements IHTTPServer, AutoCloseable {
    private static final Logger logger = LogManager.getLogger(HTTPServer.class);
    private static final AtomicInteger activeConnections = new AtomicInteger(0);

    private final ServerConfig config;
    private final ExecutorService executorService;
    private final AtomicBoolean running;
    private ServerSocket serverSocket;

    public HTTPServer(ServerConfig config) {
        this.config = config;
        this.executorService = Executors.newFixedThreadPool(config.getThreadPoolSize());
        this.running = new AtomicBoolean(false);
    }

    public HTTPServer() {
        this(new ServerConfig.Builder().build());
    }

    public void start() {
        logger.info("Starting HTTP Server on port {}", config.getPort());
        try {
            serverSocket = new ServerSocket(config.getPort());
            running.set(true);

            while (running.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running.get()) {
                        logger.error("Error accepting client connection", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Could not start server on port {}: {}", config.getPort(), e);
            throw new ServerInitializationException("Failed to start server", e);
        }
    }

    private void handleClient(Socket clientSocket) {
        if (activeConnections.get() >= config.getMaxConnections()) {
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                ResponseFactory.serviceUnavailable().writeTo(out);
            } catch (IOException e) {
                logger.error("Error sending service unavailable response", e);
            }
            return;
        }

        activeConnections.incrementAndGet();
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            HTTPParser httpParser = new HTTPParser(in, out);
            httpParser.parseRequest();
        } catch (IOException e) {
            logger.error("Error handling client request", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Error closing client socket", e);
            }
            activeConnections.decrementAndGet();
        }
    }

    public void stop() {
        logger.info("Shutting down HTTP Server");
        running.set(false);
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }
        executorService.shutdown();
    }

    @Override
    public void close() {
        stop();
    }

    public static class ServerInitializationException extends RuntimeException {
        public ServerInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}