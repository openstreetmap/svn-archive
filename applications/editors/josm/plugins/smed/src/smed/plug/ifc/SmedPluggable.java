package smed.plug.ifc;

import javax.swing.JComponent;


public interface SmedPluggable {

    boolean start();
    boolean start(JComponent panel);
    boolean stop();
    String getName();
    
    void setPluginManager(SmedPluginManager manager);
}

