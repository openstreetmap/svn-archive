package rgisopen;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * Action responsible for creation of a new layer based on
 * an image file. Based on PicLayer's NewLayerFromFileAction.
 */
public class RGISLayerFromFileAction extends JosmAction {
    /**
     * Provides filtering of only image files.
     */
    private class ImageFileFilter extends FileFilter {
        @Override
        public boolean accept( File f ) {
            return f.isDirectory() || f.getName().endsWith(".png");
        }

        @Override
        public String getDescription() {
            return tr("Image files");
        }
    }

    public RGISLayerFromFileAction() {
        super(tr("New picture layer from RGIS..."), null, null, null, false);
    }

    public void actionPerformed( ActionEvent arg0 ) {
        // Choose a file
        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new ImageFileFilter());
        int result = fc.showOpenDialog(Main.parent);

        // Create a layer?
        if( result == JFileChooser.APPROVE_OPTION ) {
            // Create layer from file
            RGISPicLayer layer = new RGISPicLayer(fc.getSelectedFile());
            // Add layer only if successfully initialized
            try {
                layer.initialize();
                Main.main.addLayer(layer);
            } catch( IOException e ) {
                // Failed
                System.out.println("RGISLayerFromFileAction::actionPerformed - " + e.getMessage());
                JOptionPane.showMessageDialog(null, e.getMessage());
                return;
            }
        }
    }
}
