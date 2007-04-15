package wmsplugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;


public class NPE_WMSmenuAction extends AbstractAction {

	/**
	 * 
	 */
	

	public NPE_WMSmenuAction() {
		super("Use NPE maps");
		
	}
	
	public void actionPerformed(ActionEvent e) {
	
		/*String npeURL = ("	http://www.getmapping.com/iedirectimage/getmappingwms.aspx?"+
			"srs=EPSG:27700&Service=WMS&Version=1.1.0&"+
			"Request=GetMap&format=image/jpeg&layers=npeoocmap");*/
		
		String npeURL = ("http://nick.dev.openstreetmap.org/openpaths/freemap.php?layers=npe&");
			
		

			Main.pref.put("wmsplugin.url", npeURL);	
	
			JOptionPane.showMessageDialog(null, "WMS set to New Public Edition.");
		
		//need to reset the particular download task
		//DownloadWMSTask
		;
		for (int i = 0; i < Main.main.menu.download.downloadTasks.size(); ++i) {
			
			if (Main.main.menu.download.downloadTasks.get(i).getPreferencesSuffix().compareTo("wmsplugin") == 0){
				
				Main.main.menu.download.downloadTasks.remove(i);
			}
		}



	}
}
