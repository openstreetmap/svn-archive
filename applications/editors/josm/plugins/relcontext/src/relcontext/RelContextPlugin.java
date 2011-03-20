package relcontext;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class RelContextPlugin extends Plugin {
    private RelContextDialog dialog;

    public RelContextPlugin( PluginInformation info ) {
        super(info);
    }

    @Override
    public void mapFrameInitialized( MapFrame oldFrame, MapFrame newFrame ) {
        if( oldFrame == null && newFrame != null ) {
            dialog = new RelContextDialog();
            newFrame.addToggleDialog(dialog);
        }
    }
}
