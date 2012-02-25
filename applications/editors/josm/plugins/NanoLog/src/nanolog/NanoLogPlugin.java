package nanolog;

import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Add NanoLog opening menu item and the panel.
 * 
 * @author zverik
 */
public class NanoLogPlugin extends Plugin {
    public NanoLogPlugin( PluginInformation info ) {
        super(info);
        Main.main.menu.fileMenu.add(new OpenNanoLogLayerAction());
    }
    
    @Override
    public void mapFrameInitialized( MapFrame oldFrame, MapFrame newFrame ) {
        if( oldFrame == null && newFrame != null ) {
            newFrame.addToggleDialog(new NanoLogPanel());
        }
    }
    
    private class OpenNanoLogLayerAction extends JosmAction {

        public OpenNanoLogLayerAction() {
            super(tr("Open NanoLog file..."), "nanolog.png", tr("Open NanoLog file..."), null, false);
        }

        public void actionPerformed( ActionEvent e ) {
            JFileChooser fc = new JFileChooser();
            if( fc.showOpenDialog(Main.parent) == JFileChooser.APPROVE_OPTION ) {
                // open layer, ok
            }
        }        
    }
}
