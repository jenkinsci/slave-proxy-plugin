package org.jenkinsci.plugins.slave_proxy;

import hudson.CopyOnWrite;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.ChannelProperty;
import hudson.remoting.forward.Forwarder;
import hudson.remoting.forward.ForwarderFactory;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Retain configuration for the slave proxy service.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class MasterToSlaveProxy extends GlobalConfiguration {
    private List<SlaveProxyConfiguration> slaveProxies = new ArrayList<SlaveProxyConfiguration>();

    @CopyOnWrite
    private transient volatile Map<SlaveProxyConfiguration,SmartPortForwarder> forwarders = Collections.emptyMap();

    public MasterToSlaveProxy() {
        load();
        restartForwarder();
    }

    private synchronized void restartForwarder() {
        // figure out port forwarders that we don't need
        Map<SlaveProxyConfiguration,SmartPortForwarder> unwanted = new HashMap<SlaveProxyConfiguration, SmartPortForwarder>(forwarders);
        unwanted.keySet().removeAll(slaveProxies);
        for (SmartPortForwarder f : unwanted.values())
            try {
                f.close();
            } catch (IOException _) {
                // ignore
            }

        // get new ones created.
        Map<SlaveProxyConfiguration,SmartPortForwarder> updated = new HashMap<SlaveProxyConfiguration,SmartPortForwarder>();
        for (SlaveProxyConfiguration sp : slaveProxies) {
            SmartPortForwarder f = forwarders.get(sp);
            if (f==null) {// if f!=null, reuse the existing proxy
                try {
                    f = new SmartPortForwarder(sp);
                    f.start();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to start the port forwarding service",e);
                    continue;
                }
            }
            f.setSlaveSelector(sp.getApplicableLabel());

            updated.put(sp,f);
        }
        forwarders = Collections.unmodifiableMap(updated);
    }

    public List<SlaveProxyConfiguration> getSlaveProxies() {
        return slaveProxies;
    }

    public void setSlaveProxies(List<SlaveProxyConfiguration> slaveProxies) {
        this.slaveProxies = new ArrayList<SlaveProxyConfiguration>(slaveProxies);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this,json);
        restartForwarder();
        save();
        return true;
    }

    /**
     * Starts the HTTP proxy service over this channel, and attach the access coordinate as the channel property.
     *
     * This service can be later obtained as {@code channel.getProperty(FORWARDER_SERVICE)}.
     *
     * @return
     *      Newly created proxy service.
     */
    public Forwarder startProxy(Channel channel, TaskListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Starting a proxy service");
        SlaveProxyService service = channel.call(new ProxyServiceLauncher());

        // create the port forwarding from the master to the HTTP proxy on this slave.
        int slavePort = service.getPort();

        Forwarder f = ForwarderFactory.create(channel, "localhost", slavePort);

        channel.setProperty(FORWARDER_SERVICE, f);

        listener.getLogger().println("Proxy service on slave port "+slavePort);
        return f;
    }

    public static final ChannelProperty<Forwarder> FORWARDER_SERVICE = new ChannelProperty<Forwarder>(Forwarder.class,"Forwarder service");

    private static final Logger LOGGER = Logger.getLogger(MasterToSlaveProxy.class.getName());
}
