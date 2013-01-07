package org.jenkinsci.plugins.slave_proxy;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Label;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Configuration of one slave proxy.
 *
 * @author Kohsuke Kawaguchi
 */
public class SlaveProxyConfiguration extends AbstractDescribableImpl<SlaveProxyConfiguration> {
    /**
     * Label expression that selects the slave to tunnel the connections to.
     */
    private String label;

    /**
     * Port on the master that the proxy service is made available.
     */
    private int masterPort;

    /**
     * If true, bind on 127.0.0.1
     */
    private boolean localOnly;

    @DataBoundConstructor
    public SlaveProxyConfiguration(String label, int masterPort, boolean localOnly) {
        this.label = label;
        this.masterPort = masterPort;
        this.localOnly = localOnly;
    }

    public String getLabel() {
        return label;
    }

    public int getMasterPort() {
        return masterPort;
    }

    public boolean isLocalOnly() {
        return localOnly;
    }

    /**
     * Value equality is based on whether the {@link SmartPortForwarder} needs
     * to be restarted.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SlaveProxyConfiguration that = (SlaveProxyConfiguration) o;
        return masterPort == that.masterPort && localOnly==that.localOnly;
    }

    @Override
    public int hashCode() {
        return masterPort;
    }

    public Label getApplicableLabel() {
        try {
            return Label.parseExpression(label);
        } catch (ANTLRException e) {
            // invalid syntax
            return null;
        }
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SlaveProxyConfiguration> {
        @Override
        public String getDisplayName() {
            return "";
        }
    }
}
