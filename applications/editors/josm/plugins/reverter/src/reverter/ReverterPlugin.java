// License: GPL. For details, see LICENSE file.
package reverter;

import javax.swing.JMenu;

import org.openstreetmap.josm.actions.UploadAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.io.remotecontrol.RemoteControl;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * The reverter plugin
 */
public class ReverterPlugin extends Plugin {
    static boolean reverterUsed;

    /**
     * Constructs a new {@code ReverterPlugin}.
     * @param info plugin information
     */
    public ReverterPlugin(PluginInformation info) {
        super(info);
        JMenu historyMenu = MainApplication.getMenu().dataMenu;
        MainMenu.add(historyMenu, new RevertChangesetAction());
        UploadAction.registerUploadHook(new ReverterUploadHook(info));
        new RemoteControl().addRequestHandler(RevertChangesetHandler.command, RevertChangesetHandler.class);
    }
}
