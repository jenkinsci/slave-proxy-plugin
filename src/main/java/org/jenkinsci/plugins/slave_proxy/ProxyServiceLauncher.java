package org.jenkinsci.plugins.slave_proxy;

import hudson.remoting.Callable;
import hudson.remoting.Channel;
import org.littleshoot.proxy.DefaultHttpProxyServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Callable} that actually starts the HTTP proxy service.
 *
 * @author Kohsuke Kawaguchi
 */
class ProxyServiceLauncher implements Callable<SlaveProxyService, IOException> {
    public SlaveProxyService call() throws IOException {
        final int port = allocatePort();
        final DefaultHttpProxyServer s = new DefaultHttpProxyServer(port);
        s.start(true, false);

        return Channel.current().export(SlaveProxyService.class, new SlaveProxyService() {
            public int getPort() {
                return port;
            }

            public void stop() {
                s.stop();
            }

            @Override
            protected void finalize() throws Throwable {
                new TerminationThread(s).start();
                super.finalize();
            }
        });
    }

    // TODO: patch littleProxy to support ephemeral port
    private int allocatePort() throws IOException {
        ServerSocket ss = new ServerSocket(0);
        int p = ss.getLocalPort();
        ss.close();
        return p;
    }

    private static final long serialVersionUID = 1L;

    private static final Logger LITTLE_PROXY_LOGGER = Logger.getLogger("org.littleshoot.proxy");

    static {
        // info level logging on little proxy is too verbose
        LITTLE_PROXY_LOGGER.setLevel(Level.WARNING);
    }
}
