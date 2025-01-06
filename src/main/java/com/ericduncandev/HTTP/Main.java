package com.ericduncandev.HTTP;

import com.ericduncandev.HTTP.core.HTTPServer;

public class Main {
    public static void main(String[] args) {
        try (HTTPServer server = new HTTPServer()) {
            server.start();
        }
    }

}
