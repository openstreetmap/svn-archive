package surveyor2;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Create two toggle dialogs and one action or whatever.
 * 
 * @author zverik
 */
public class SurveyorPlugin extends Plugin {
    public SurveyorPlugin( PluginInformation info ) {
        super(info);
    }
    
    @Override
    public void mapFrameInitialized( MapFrame oldFrame, MapFrame newFrame ) {
        if( oldFrame == null && newFrame != null ) {
            newFrame.addToggleDialog(new SurveyorTimelineDialog());
            newFrame.addToggleDialog(new SurveyorButtonsDialog());
            // todo: drawing on canvas action
        }
    }
}
