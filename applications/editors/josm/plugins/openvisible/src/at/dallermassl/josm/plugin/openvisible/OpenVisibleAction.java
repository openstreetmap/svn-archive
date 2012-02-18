/**
 *
 */
package at.dallermassl.josm.plugin.openvisible;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.io.GpxImporter;
import org.openstreetmap.josm.io.GpxReader;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmImporter;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.tools.Shortcut;
import org.xml.sax.SAXException;

import at.dallermassl.josm.plugin.openvisible.OsmGpxBounds;

/**
 * @author cdaller
 *
 */
public class OpenVisibleAction extends JosmAction {
    private File lastDirectory;

    public OpenVisibleAction() {
        super(tr("Open Visible..."), "openvisible",
        tr("Open only files that are visible in current view."),
        Shortcut.registerShortcut("tools:openvisible", tr("Menu: {0}", tr("Open Visible...")),
        KeyEvent.VK_I, Shortcut.ALT_CTRL_SHIFT), true);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if(Main.map == null || Main.map.mapView == null) {
            JOptionPane.showMessageDialog(Main.parent, tr("No view open - cannot determine boundaries!"));
            return;
        }
        MapView view = Main.map.mapView;
        Rectangle bounds = view.getBounds();
        LatLon bottomLeft = view.getLatLon(bounds.x, bounds.y + bounds.height);
        LatLon topRight = view.getLatLon(bounds.x + bounds.width, bounds.y);

        System.err.println("FileFind Bounds: " + bottomLeft + " to " + topRight);

        JFileChooser fileChooser;
        if(lastDirectory != null) {
            fileChooser = new JFileChooser(lastDirectory);
        } else {
            fileChooser = new JFileChooser();
        }
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.showOpenDialog(Main.parent);
        File[] files = fileChooser.getSelectedFiles();
        lastDirectory = fileChooser.getCurrentDirectory();

        for(File file : files) {
            try {
                OsmGpxBounds parser = new OsmGpxBounds();
                parser.parse(new BufferedInputStream(new FileInputStream(file)));
                if(parser.intersects(bottomLeft.lat(), topRight.lat(), bottomLeft.lon(), topRight.lon())) {
                    System.out.println(file.getAbsolutePath()); // + "," + parser.minLat + "," + parser.maxLat + "," + parser.minLon + "," + parser.maxLon);
                    if(file.getName().endsWith("osm")) {
                        openAsData(file);
                    } else if(file.getName().endsWith("gpx")) {
                        openFileAsGpx(file);
                    }

                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (SAXException e1) {
                e1.printStackTrace();
            } catch(IllegalDataException e1) {
                e1.printStackTrace();
            }
        }

    }

    private void openAsData(File file) throws SAXException, IOException, FileNotFoundException, IllegalDataException {
        String fn = file.getName();
        if (new OsmImporter().acceptFile(file)) {
            DataSet dataSet = OsmReader.parseDataSet(new FileInputStream(file), NullProgressMonitor.INSTANCE);
            OsmDataLayer layer = new OsmDataLayer(dataSet, fn, file);
            Main.main.addLayer(layer);
        }
        else
            JOptionPane.showMessageDialog(Main.parent, fn+": "+tr("Unknown file extension: {0}", fn.substring(fn.lastIndexOf('.')+1)));
    }

    private void openFileAsGpx(File file) throws SAXException, IOException, FileNotFoundException {
        String fn = file.getName();
        if (new GpxImporter().acceptFile(file)) {
            GpxReader r = null;
            if (file.getName().endsWith(".gpx.gz")) {
                r = new GpxReader(new GZIPInputStream(new FileInputStream(file)));
            } else{
                r = new GpxReader(new FileInputStream(file));
            }
            if (!r.parse(true)) {
                // input was not properly parsed, abort
                JOptionPane.showMessageDialog(Main.parent, tr("Parsing file \"{0}\" failed", file));
                throw new IllegalStateException();
            }
            r.data.storageFile = file;
            GpxLayer gpxLayer = new GpxLayer(r.data, fn);
            Main.main.addLayer(gpxLayer);
            Main.main.addLayer(new MarkerLayer(r.data, tr("Markers from {0}", fn), file, gpxLayer));

        } else {
            throw new IllegalStateException();
        }
    }
}
