package org.jenkinsci.plugins.slave_proxy.MasterToSlaveProxy;

def f=namespace(lib.FormTagLib)

f.section(title:_("Master->Slave HTTP Proxy Service"))
f.entry(title:_("Applicable Label"),field:"label") {
    f.textbox()
}
