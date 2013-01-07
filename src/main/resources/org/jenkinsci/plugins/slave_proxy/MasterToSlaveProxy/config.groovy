package org.jenkinsci.plugins.slave_proxy.MasterToSlaveProxy;

def f=namespace(lib.FormTagLib)

f.section(title:_("Master->Slave HTTP Proxy Service")) {
    f.nested(add:_("Add Proxy")) {
        f.repeatableProperty(field:"slaveProxies")
    }
}
