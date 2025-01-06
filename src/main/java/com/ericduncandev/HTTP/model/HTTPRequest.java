package com.ericduncandev.HTTP.model;

import java.util.Map;

public record HTTPRequest(String method, String uri, String protocolVersion, Map<String, String> headers, String body) {

    // displays request in HTTP format
    @Override
    public String toString() {
        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append(method).append(" ").append(uri).append(" ").append(protocolVersion).append("\r\n");

        // Append headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            requestBuilder.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        // Add a blank line to indicate the end of the headers
        requestBuilder.append("\r\n");
        requestBuilder.append(body);

        return requestBuilder.toString();
    }
}
