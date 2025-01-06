package com.ericduncandev.HTTP.interfaces;

import com.ericduncandev.HTTP.core.ServerConfig;

public interface IServerConfig {
    /**
     * @return the port number the server listens on
     */
    int getPort();

    /**
     * @return the size of the thread pool for handling concurrent connections
     */
    int getThreadPoolSize();

    /**
     * @return the maximum number of concurrent connections allowed
     */
    int getMaxConnections();

    /**
     * @return the root directory path for serving web content
     */
    String getWebRoot();

    interface IBuilder {
        /**
         * Sets the server port number.
         * @param port the port number to listen on
         * @return the builder instance
         */
        ServerConfig.Builder port(int port);

        /**
         * Sets the thread pool size.
         * @param size the number of threads in the pool
         * @return the builder instance
         */
        ServerConfig.Builder threadPoolSize(int size);

        /**
         * Sets the maximum allowed concurrent connections.
         * @param max the maximum number of connections
         * @return the builder instance
         */
        ServerConfig.Builder maxConnections(int max);

        /**
         * Sets the web root directory.
         * @param root the path to the web root directory
         * @return the builder instance
         */
        ServerConfig.Builder webRoot(String root);

        /**
         * Builds and returns a new ServerConfig instance.
         * @return a new ServerConfig instance
         */
        ServerConfig build();
    }
}
