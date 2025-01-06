package com.ericduncandev.HTTP.core;

import com.ericduncandev.HTTP.interfaces.IServerConfig;

public final class ServerConfig implements IServerConfig {
    private int port;
    private final int threadPoolSize;
    private final int maxConnections;
    private final String webRoot;

    public static class Builder implements IServerConfig.IBuilder {
        private int port = 80;
        private int threadPoolSize = 100;
        private int maxConnections = 400;
        private String webRoot = "./";

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder threadPoolSize(int size) {
            this.threadPoolSize = size;
            return this;
        }

        public Builder maxConnections(int max) {
            this.maxConnections = max;
            return this;
        }

        public Builder webRoot(String root) {
            this.webRoot = root;
            return this;
        }

        public ServerConfig build() {
            return new ServerConfig(this);
        }
    }

    private ServerConfig(Builder builder) {
        this.port = builder.port;
        this.threadPoolSize = builder.threadPoolSize;
        this.maxConnections = builder.maxConnections;
        this.webRoot = builder.webRoot;
    }

    // Getters
    public int getPort() { return port; }
    public int getThreadPoolSize() { return threadPoolSize; }
    public int getMaxConnections() { return maxConnections; }
    public String getWebRoot() { return webRoot; }

    // Setters
    public void setPort(int port) {
        this.port = port;
    }
}