package oseam;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import oseam.dialogs.OSeaMAction;

import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;

public class OSeaM implements SmedPluggable {

	private OSeaMAction osm = null;
	public static SmedPluginManager manager = null;
	private int index = -1;
	
	@Override
	public JComponent getComponent() {
		osm = new OSeaMAction();
		osm.init();
		manager.setString(tr("it works realy fine"));
		return osm.getPM01SeaMap();
	}

	@Override
	public String getInfo() {return tr("mapping seamarks"); }

	@Override
	public String getName() {return tr("Seamarks"); }

	@Override
	public void setPluginManager(SmedPluginManager manager) {
		OSeaM.manager = manager;
	}

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stop() {
		osm.closePanel();
		return true;
	}

	@Override
	public String getFileName() {
		return "OSeaM.jar";
	
	}

	@Override
	public ImageIcon getIcon() {

		return new ImageIcon(getClass().getResource("/images/Smp.png"));
	}

	@Override
	public boolean hasFocus() {
		osm.setQueued();

		return true;
	}

	@Override
	public boolean lostFocus() {
		osm.setDequeued();
		
		return true;
	}

	@Override
	public int getIndex() { return index; }

	@Override
	public void setIndex(int index) { this.index = index; }


}
