package wmsplugin;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView;

public class Map_Rectifier_WMSmenuAction extends JosmAction {

	/**
	 * tim waters "chippy"
	 */
	private static final long serialVersionUID = 1L;

	public Map_Rectifier_WMSmenuAction() {
		super("Rectified Image ...", "OLmarker", "Download Rectified Image from Metacarta's Map Rectifer WMS", 0, 0, false);
	}

	public void actionPerformed(ActionEvent e) {
		String newid = JOptionPane.showInputDialog(Main.parent, "Metacarta Map Rectifier image id");

		if (newid != null && !newid.equals("")) {
			String newURL = "http://labs.metacarta.com/rectifier/wms.cgi?id="+newid+
			"&srs=EPSG:4326&Service=WMS&Version=1.1.0&Request=GetMap&format=image/png";

			DownloadWMSTask.download("rectifier id="+newid, newURL);
		}
	}
}
