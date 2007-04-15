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
		super("Rectified Image", "OLmarker", "Download Rectified Image from Metacarta's Map Rectifer WMS", 0, 0, false);


	}
	DownloadWMSTask downloadTask;

	public void actionPerformed(ActionEvent e) {

		//String newid;
		String newURL = "";
		String newid = "";

		newid = JOptionPane.showInputDialog(Main.parent, "Metacarta Map Rectifier image id ");
	//	System.out.println("newid= " +newid);
		if (newid != null){


			if (newid.compareTo("") != 0) 
			{
				newURL = "http://labs.metacarta.com/rectifier/wms.cgi?id="+newid+
				"&srs=EPSG:4326&Service=WMS&Version=1.1.0&Request=GetMap&format=image/png";


				//System.out.println(newURL);

			//	if (downloadTask == null){
					//System.out.println("new download task!");
					downloadTask = new DownloadWMSTask("rectifier id="+newid, newURL);
			//	}
				MapView mv = Main.map.mapView;

				downloadTask.download(null,
						mv.getLatLon(0, mv.getHeight()).lat(),
						mv.getLatLon(0, mv.getHeight()).lon(),
						mv.getLatLon(mv.getWidth(), 0).lat(),
						mv.getLatLon(mv.getWidth(), 0).lon());			


			}
		}
		//else do nuffink

	}



}

