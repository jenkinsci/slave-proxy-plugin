package org.jenkinsci.plugins.slave_proxy;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Label;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import hudson.slaves.ComputerListener;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Looks for new connected slaves and bring up the proxy service.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class ComputerListenerImpl extends ComputerListener {
    @Inject
    transient MasterToSlaveProxy config;

    @Override
    public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        VirtualChannel ch = c.getChannel();
        if (ch instanceof Channel) {
            Channel channel = (Channel) ch;
            for (SlaveProxyConfiguration sp : config.getSlaveProxies()) {
                Label al = sp.getApplicableLabel();
                if (al!=null && al.matches(c.getNode())) {
                    try {
                        config.startProxy(channel, listener);
                        return;
                    } catch (IOException e) {
                        e.printStackTrace(listener.error("Failed to start the HTTP proxy service"));
                    }
                }
            }
        }
    }
}
