package iodb;

import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import static org.openstreetmap.josm.tools.I18n.marktr;

/**
 * Add some actions to the imagery menu.
 * 
 * @author zverik
 */
public class ImageryOffsetPlugin extends Plugin {
    private GetImageryOffsetAction getAction;
    private StoreImageryOffsetAction storeAction;
    
    public ImageryOffsetPlugin( PluginInformation info ) {
        super(info);
        
        getAction = new GetImageryOffsetAction();
        storeAction = new StoreImageryOffsetAction();
        
        JMenu offsetMenu = Main.main.menu.addMenu(marktr("Offset"), KeyEvent.VK_O, 6, "help");
        offsetMenu.add(getAction);
        offsetMenu.add(storeAction);
    }
}
