package nanolog;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * Add NanoLog opening menu item and the panel.
 *
 * @author zverik
 */
public class NanoLogPlugin extends Plugin {
    public NanoLogPlugin(PluginInformation info) {
        super(info);
        MainApplication.getMenu().fileMenu.insert(new OpenNanoLogLayerAction(), 4);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            NanoLogPanel panel = new NanoLogPanel();
            newFrame.addToggleDialog(panel);
            MainApplication.getLayerManager().addLayerChangeListener(panel);
        }
    }

    private static class OpenNanoLogLayerAction extends JosmAction {

        OpenNanoLogLayerAction() {
            super(tr("Open NanoLog file..."), "nanolog.png", tr("Open NanoLog file..."), null, false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(MainApplication.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
                try {
                    List<NanoLogEntry> entries = NanoLogLayer.readNanoLog(fc.getSelectedFile());
                    if (!entries.isEmpty()) {
                        NanoLogLayer layer = new NanoLogLayer(entries);
                        MainApplication.getLayerManager().addLayer(layer);
                        layer.setupListeners();
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Could not read NanoLog file:") + "\n" + ex.getMessage());
                }
            }
        }
    }
}
