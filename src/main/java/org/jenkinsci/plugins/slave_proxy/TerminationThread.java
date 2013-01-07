package org.jenkinsci.plugins.slave_proxy;

import org.littleshoot.proxy.HttpProxyServer;

/**
 * Calls {@link HttpProxyServer#stop()}.
 *
 * This process blocks, so we use another thread to avoid calling it from the finalizer thread.
 *
 * @author Kohsuke Kawaguchi
 */
class TerminationThread extends Thread {
    private final HttpProxyServer server;

    public TerminationThread(HttpProxyServer s) {
        this.server = s;
    }

    @Override
    public void run() {
        if (server!=null)
            server.stop();
    }
}
