package com.ericduncandev.HTTP.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class HTTPResponse {
    private final String protocolVersion;
    private final int statusCode;
    private final Map<String, String> headers;
    private final byte[] body;
    private static final Logger logger = LogManager.getLogger(HTTPResponse.class);

    private HTTPResponse(Builder builder) {
        this.protocolVersion = builder.protocolVersion;
        this.statusCode = builder.statusCode;
        this.headers = new HashMap<>(builder.headers);
        this.body = builder.body;
    }

    public static class Builder {
        private String protocolVersion = "HTTP/1.1";
        private int statusCode = 200;
        private final Map<String, String> headers = new HashMap<>();
        private byte[] body = new byte[0];

        public Builder() {
            // Set default headers

            headers.put("Connection", "close");
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            String httpDate = DateTimeFormatter.RFC_1123_DATE_TIME
                    .format(now);
            headers.put("Date", httpDate);

            // these are going be a checklist of what I want to implement later
            // Security headers
            // headers.put("X-Content-Type-Options", "nosniff");
            // headers.put("X-Frame-Options", "SAMEORIGIN");
            // headers.put("X-XSS-Protection", "1; mode=block");
            // headers.put("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            // headers.put("Referrer-Policy", "strict-origin-when-cross-origin");

            // Cache control (can be overridden as needed)
            //  headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
            //  headers.put("Pragma", "no-cache");
            //  headers.put("Expires", "0");

            // Server identification
            //  headers.put("Server", "Java-HTTPServer");
        }

        public Builder protocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            logger.debug("Content-Length: {}", String.valueOf(body.length));
            if (body != null) {
                headers.put("Content-Length", String.valueOf(body.length));
            }
            return this;
        }

        public Builder contentType(String contentType) {
            headers.put("Content-Type", contentType);
            return this;
        }

        // for 201 created
        public Builder location(String location) {
            headers.put("Location", location);
            return this;
        }

        // for 204 no content
        public Builder contentLocation(String contentLocation) {
            headers.put("Content-Location", contentLocation);
            return this;
        }

        public HTTPResponse build() {
            return new HTTPResponse(this);
        }
    }

    public void writeTo(PrintWriter writer) {
        // Write status line
        try {
            writer.print(String.format("%s %d %s\r\n",
                    protocolVersion, statusCode, getStatusMessage(statusCode)));

            // Write headers
            headers.forEach((key, value) ->
                    writer.print(String.format("%s: %s\r\n", key, value)));

            // Write blank line
            writer.print("\r\n");
            writer.flush();

            // Write body if it exists
            if (body != null && body.length > 0) {
                writer.write(new String(body, StandardCharsets.UTF_8));
                writer.flush();
            }
        } catch (Exception e) {
            logger.error("Error writing to client", e);
        }
    }

    private String getStatusMessage(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            default -> "Unknown";
        };
    }
}