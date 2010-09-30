package oseam;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import oseam.dialogs.OSeaMAction;

import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;

public class OSeaM implements SmedPluggable {

	private OSeaMAction osm = null;
	public SmedPluginManager manager = null;
	
	@Override
	public JComponent getComponent() {
		osm = new OSeaMAction();
		osm.init();
		manager.setString("it works realy fine");
		return osm.getPM01SeaMap();
	}

	@Override
	public String getInfo() {return "mapping seamarks"; }

	@Override
	public String getName() {return "Seamarks"; }

	@Override
	public void setPluginManager(SmedPluginManager manager) {
		this.manager = manager;
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


}
