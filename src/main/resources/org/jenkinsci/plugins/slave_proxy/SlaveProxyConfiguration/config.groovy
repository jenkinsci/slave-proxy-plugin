package org.jenkinsci.plugins.slave_proxy.SlaveProxyConfiguration;

def f=namespace(lib.FormTagLib)

f.entry(title:_("Label to select slaves"),field:"label") {
    f.textbox()
}
f.entry(title:_("TCP port on master to listen"),field:"masterPort") {
    f.textbox()
}
f.entry(field: "localOnly") {
    f.checkbox(title:_("Accept connection from localhost only"), default:true)
}
f.entry {
    div(align:"right") {
        f.repeatableDeleteButton()
    }
}
