
package harbour;

import java.net.URL;

import harbour.dialogs.HarbourAction;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapView;

import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;

public class Harbour implements SmedPluggable {
	
	private int index = -1;
	private String msg = "";
	private HarbourAction harbour = null;
	private SmedPluginManager manager = null;

	@Override
	public boolean start() {
		return false;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasFocus() {
//		Main.map.mapView.addPropertyChangeListener(harbour);
//		MapView.addLayerChangeListener(harbour);
		return true;
	}

	@Override
	public boolean lostFocus() {
//		Main.map.mapView.removePropertyChangeListener(harbour);
//		MapView.removeLayerChangeListener(harbour);
		return true;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String getName() {
		return "Harbour";
	}

	@Override
	public String getFileName() {
		return "Harbour.jar";
	}

	@Override
	public ImageIcon getIcon() {
		URL url = getClass().getResource("/images/Hbr.png");
		if(url == null) return null;
		else return new ImageIcon(url);
	}

	@Override
	public String getInfo() {
		return "Harbour editor";
	}

	@Override
	public JComponent getComponent() {
		manager.showVisualMessage(msg);
		harbour = new HarbourAction();
		
		return harbour.getHarbourPanel();
	}

	@Override
	public void setPluginManager(SmedPluginManager manager) {
		this.manager = manager;
	}

}
