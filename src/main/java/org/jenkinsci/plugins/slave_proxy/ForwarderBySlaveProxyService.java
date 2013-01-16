package org.jenkinsci.plugins.slave_proxy;

import hudson.remoting.Channel;
import hudson.remoting.forward.Forwarder;
import hudson.remoting.forward.ForwarderFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link Forwarder} that holds a reference to the remote proxy service.
 *
 * @author Kohsuke Kawaguchi
 */
class ForwarderBySlaveProxyService implements Forwarder {
    private final Forwarder f;
    private final SlaveProxyService service; // just holding a reference to avoid GC

    public ForwarderBySlaveProxyService(Channel channel, SlaveProxyService service) throws IOException, InterruptedException {
        this.f = ForwarderFactory.create(channel, "localhost", service.getPort());
        this.service = service;
    }

    public OutputStream connect(OutputStream out) throws IOException {
        return f.connect(out);
    }
}
