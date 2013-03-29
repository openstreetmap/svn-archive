package iodb;

import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.JMenu;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Version;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import static org.openstreetmap.josm.tools.I18n.marktr;

/**
 * A plugin to request and store imagery offsets in the centralized database.
 * 
 * @author Zverik
 * @license WTFPL
 */
public class ImageryOffsetPlugin extends Plugin {
    private GetImageryOffsetAction getAction;
    private StoreImageryOffsetAction storeAction;
    
    /**
     * Add both actions to their own menu. This creates
     * "Offset" menu, because "Imagery" is constantly rebuilt,
     * losing all changes, and other menus are either too long already,
     * or completely unsuitable for imagery offset actions.
     */
    public ImageryOffsetPlugin( PluginInformation info ) {
        super(info);
        
        getAction = new GetImageryOffsetAction();
        storeAction = new StoreImageryOffsetAction();
        
        // before 5803 imagery menu was constantly regenerated, erasing extra items
        // before 5729 it was regenerated only when the imagery list was modified (also bad)
        int version = Version.getInstance().getVersion();
        JMenu offsetMenu = version < 5803
                ? Main.main.menu.addMenu(marktr("Offset"), KeyEvent.VK_O, 6, "help")
                : Main.main.menu.imageryMenu;
        offsetMenu.add(getAction);
        offsetMenu.add(storeAction);

        // an ugly hack to add this plugin to the toolbar
        if( Main.pref.getBoolean("iodb.modify.toolbar", true) ) {
            Collection<String> toolbar = new LinkedList<String>(Main.toolbar.getToolString());
            if( !toolbar.contains("getoffset") ) {
                toolbar.add("getoffset");
                Main.pref.putCollection("toolbar", toolbar);
                Main.toolbar.refreshToolbarControl();
            }
            Main.pref.put("iodb.modify.toolbar", false);
        }
    }
}
