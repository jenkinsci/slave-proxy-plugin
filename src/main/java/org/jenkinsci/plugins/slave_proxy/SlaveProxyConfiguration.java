package org.jenkinsci.plugins.slave_proxy;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.labels.LabelExpression;
import org.kohsuke.stapler.DataBoundConstructor;
import org.springframework.aop.ClassFilter;

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

    @DataBoundConstructor
    public SlaveProxyConfiguration(String label, int masterPort) {
        this.label = label;
        this.masterPort = masterPort;
    }

    public String getLabel() {
        return label;
    }

    public int getMasterPort() {
        return masterPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SlaveProxyConfiguration that = (SlaveProxyConfiguration) o;
        return masterPort == that.masterPort;
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
