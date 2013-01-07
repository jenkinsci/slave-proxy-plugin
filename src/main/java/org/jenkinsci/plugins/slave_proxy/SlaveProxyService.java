package org.jenkinsci.plugins.slave_proxy;

/**
 * Remote interface for the HTTP proxy service that's running.
 * @author Kohsuke Kawaguchi
 */
public interface SlaveProxyService {
    /**
     * Returns the port on the slave where the proxy is listening.
     */
    int getPort();

    /**
     * Shuts down the service.
     */
    void stop();
}
