package org.jenkinsci.plugins.slave_proxy;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.model.Label;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.ChannelProperty;
import hudson.remoting.forward.ForwarderFactory;
import hudson.remoting.forward.ListeningPort;
import hudson.remoting.forward.PortForwarder;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

/**
 * Retain configuration for the slave proxy service.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class MasterToSlaveProxy extends GlobalConfiguration {
    private String label;

    public MasterToSlaveProxy() {
        load();
    }

    public Label getApplicableLabel() {
        try {
            return Label.parseExpression(label);
        } catch (ANTLRException e) {
            return null; // invalid syntax
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
        save();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this,json);
        return true;
    }

    /**
     * Starts the HTTP proxy service over this channel, and attach the access coordinate as the channel property.
     *
     * This service can be later obtained as {@code channel.getProperty(PROXY_SERVICE)}.
     *
     * @return
     *      Newly created proxy service.
     */
    public ListeningPort startProxy(Channel channel, TaskListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Starting a proxy service");
        SlaveProxyService service = channel.call(new ProxyServiceLauncher());

        // create the port forwarding from the master to the HTTP proxy on this slave.
        int slavePort = service.getPort();

        // fatal bug fixed in 2.21
        //        ListeningPort lp = channel.createLocalToRemotePortForwarding(0, "localhost", slavePort);
        //
        PortForwarder lp = new PortForwarder(0,
                ForwarderFactory.create(channel, "localhost", slavePort));
        lp.start();

        channel.setProperty(PROXY_SERVICE, lp);

        listener.getLogger().println("Proxy service on slave port "+slavePort+" is forwarded to the master on port "+lp.getPort());
        return lp;
    }

    public static final ChannelProperty<ListeningPort> PROXY_SERVICE = new ChannelProperty<ListeningPort>(ListeningPort.class,"Proxy service");

}
