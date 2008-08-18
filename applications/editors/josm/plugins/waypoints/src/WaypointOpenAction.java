package waypoints; 

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import javax.xml.parsers.ParserConfigurationException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.actions.DiskAccessAction;
import org.xml.sax.SAXException;

/**
 * Based on standard JOSM OpenAction 
 * For opening a waypoint file to convert to nodes.
 */
public class WaypointOpenAction extends DiskAccessAction {
	
	/**
	 * Create an open action. The name is "Open a file".
	 */
	public WaypointOpenAction() {
		super(tr("Open waypoints file"), "open", tr("Open a waypoints file."), 
						KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK);
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = createAndOpenFileChooser(true, true, null);
		if (fc == null)
			return;
		File[] files = fc.getSelectedFiles();
		for (int i = files.length; i > 0; --i)
			openFile(files[i-1]);
	}

	/**
	 * Open the given file.
	 */
	public void openFile(File file) {
		String fn = file.getName();
		try {
				DataSet dataSet = 
						WaypointReader.parse(new FileInputStream(file));
				Main.main.addLayer(new OsmDataLayer(dataSet, file.getName(), 
										file));
		} catch (SAXException x) {
			x.printStackTrace();
			JOptionPane.showMessageDialog(Main.parent, 
					tr("Error while parsing {0}",fn)+": "+x.getMessage());
		} catch (ParserConfigurationException x) {
			x.printStackTrace(); // broken SAXException chaining
			JOptionPane.showMessageDialog(Main.parent, 
					tr("Error while parsing {0}",fn)+": "+x.getMessage());
		} catch (IOException x) {
			x.printStackTrace();
			JOptionPane.showMessageDialog(Main.parent, 
					tr("Could not read \"{0}\"",fn)+"\n"+x.getMessage());
		}
	}
}
