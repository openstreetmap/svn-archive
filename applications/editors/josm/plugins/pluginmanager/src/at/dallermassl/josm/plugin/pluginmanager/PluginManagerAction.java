/**
 *
 */
package at.dallermassl.josm.plugin.pluginmanager;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.tools.ImageProvider;

/**
 * @author cdaller
 *
 */
public class PluginManagerAction extends AbstractAction {

    /**
     * Constructor
     */
    public PluginManagerAction() {
        super("pluginmanager"); //, ImageProvider.get("preferences", "plugin"));
        putValue(AbstractAction.NAME, "Plugin Manager");
        putValue(AbstractAction.LONG_DESCRIPTION, "Allows to download/install plugins");
        putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_P);
        //putValue(AbstractAction.LARGE_ICON_KEY, ImageProvider.get("preferences", "plugin"));
        putValue(AbstractAction.SMALL_ICON, ImageProvider.get("pluginmanager"));
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        // show window like eclipse software update window:
        // left a list of sites, right buttons "add remote site"
        // list has checkboxes
        // adding: name and url where site.xml can be found.
        // site.xml containing a list of plugin.xml files
        // plugin info holds name, version and dependencies (including versions)
        // button next: check for updates and present list of (new/updateable) plugins
        // button to resolve dependencies automatically
        // page to download, page to install, page to enable new plugins
        // finish

        // plugin.xml holds info
        // name, version, dependencies, installpath,

        // info about other resources: images, log4j.jar, ....
        // need source url and target dir (relative to .josm? or main app dir (for josm update))
        // need restart of josm afterwards

        // pluginmanager could also load the plugins itself, so dependencies could be respected
    }

}
