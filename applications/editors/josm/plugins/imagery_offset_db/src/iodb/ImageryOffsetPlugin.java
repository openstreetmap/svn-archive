package iodb;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

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
        
        // todo: correct menu
        Main.main.menu.viewMenu.addSeparator();
        Main.main.menu.viewMenu.add(getAction);
        Main.main.menu.viewMenu.add(storeAction);

        // todo: add a button on toolbar
        // todo: make MapMode for viewing and updating imagery offsets (is it needed?)
    }
}
