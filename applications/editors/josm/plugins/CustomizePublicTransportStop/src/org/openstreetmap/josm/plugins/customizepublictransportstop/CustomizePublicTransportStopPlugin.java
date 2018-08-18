// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.customizepublictransportstop;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Class of plugin of customizing of stop area Plugin for josm editor
 * 
 * @author Rodion Scherbakov
 */
public class CustomizePublicTransportStopPlugin extends Plugin {
    /**
     * Stop area customizing action
     */
    private CustomizeStopAction stopAreaCreatorAction;

    /**
     * Constructor of plug-in object
     * 
     * @param info Plug-in properties
     */
    public CustomizePublicTransportStopPlugin(PluginInformation info) {
        super(info);
        stopAreaCreatorAction = CustomizeStopAction.createCustomizeStopAction();
        MainApplication.getMenu().toolsMenu.add(stopAreaCreatorAction);
    }

}
