/**
 * 
 */
package at.dallermassl.josm.plugin.openvisible;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.layer.RawGpsLayer;
import org.openstreetmap.josm.gui.layer.RawGpsLayer.GpsPoint;
import org.openstreetmap.josm.gui.layer.markerlayer.Marker;
import org.openstreetmap.josm.gui.layer.markerlayer.MarkerLayer;
import org.openstreetmap.josm.io.OsmReader;
import org.openstreetmap.josm.io.RawCsvReader;
import org.openstreetmap.josm.io.RawGpsReader;
import org.xml.sax.SAXException;

/**
 * @author cdaller
 *
 */
public class OpenVisibleAction extends JosmAction {
    private File lastDirectory;
    
    public OpenVisibleAction() {
        super(tr("Open Visible"), "openvisible", tr("Open only files that are visible in current view."), KeyEvent.VK_O, 
            InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, true);
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
                        openFileAsRawGps(file);
                    }
                    
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (SAXException e1) {
                e1.printStackTrace();
            }
        }
        
    }
    
    private void openAsData(File file) throws SAXException, IOException, FileNotFoundException {
        String fn = file.getName();
        if (ExtensionFileFilter.filters[ExtensionFileFilter.OSM].acceptName(fn)) {
            DataSet dataSet = OsmReader.parseDataSet(new FileInputStream(file), null, Main.pleaseWaitDlg);
            OsmDataLayer layer = new OsmDataLayer(dataSet, file.getName(), file);
            Main.main.addLayer(layer);
        } else if (ExtensionFileFilter.filters[ExtensionFileFilter.CSV].acceptName(fn))
            JOptionPane.showMessageDialog(Main.parent, fn+": "+tr("CSV Data import for non-GPS data is not implemented yet."));
        else
            JOptionPane.showMessageDialog(Main.parent, fn+": "+tr("Unknown file extension: {0}", fn.substring(file.getName().lastIndexOf('.')+1)));
    }

    private void openFileAsRawGps(File file) throws SAXException, IOException, FileNotFoundException {
        String fn = file.getName();
        Collection<Collection<GpsPoint>> gpsData = null;
        Collection<Marker> markerData = null;
        if (ExtensionFileFilter.filters[ExtensionFileFilter.GPX].acceptName(fn)) {
            RawGpsReader r = null;
            if (file.getName().endsWith(".gpx.gz"))
                r = new RawGpsReader(new GZIPInputStream(new FileInputStream(file)), file.getAbsoluteFile().getParentFile());
            else
                r = new RawGpsReader(new FileInputStream(file), file.getAbsoluteFile().getParentFile());
            gpsData = r.trackData;
            markerData = r.markerData;
        } else if (ExtensionFileFilter.filters[ExtensionFileFilter.CSV].acceptName(fn)) {
            gpsData = new LinkedList<Collection<GpsPoint>>();
            gpsData.add(new RawCsvReader(new FileReader(file)).parse());
        } else
            throw new IllegalStateException();
        if (gpsData != null && !gpsData.isEmpty())
            Main.main.addLayer(new RawGpsLayer(false, gpsData, tr("Tracks from {0}", file.getName()), file));
        if (markerData != null && !markerData.isEmpty())
            Main.main.addLayer(new MarkerLayer(markerData, tr ("Markers from {0}", file.getName()), file));
    }


}
