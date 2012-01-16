package wayarea;

import java.net.URL;

import wayarea.dialogs.WayAreaAction;
import wayarea.Messages;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.openstreetmap.josm.data.osm.DataSet;

import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;

public class WayArea implements SmedPluggable {

	private int index = -1;
	private String msg = "";
	private WayAreaAction wayarea = null;
	private SmedPluginManager manager = null;

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean stop() {
		// DataSet.removeSelectionListener(oseam.SmpListener);
		return true;
	}

	@Override
	public boolean hasFocus() {
		// DataSet.addSelectionListener(oseam.SmpListener);
		return true;
	}

	@Override
	public boolean lostFocus() {
		// DataSet.removeSelectionListener(oseam.SmpListener);
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
		return Messages.getString("TabName");
	}

	@Override
	public String getFileName() {
		return "WayArea.jar";
	}

	@Override
	public ImageIcon getIcon() {
		URL url = getClass().getResource("/images/wayarea.png");
		if (url == null)
			return null;
		else
			return new ImageIcon(url);
	}

	@Override
	public String getInfo() {
		return Messages.getString("TabInfo");
	}

	@Override
	public JComponent getComponent() {
		manager.showVisualMessage(msg);
		wayarea = new WayAreaAction(manager);

		return wayarea.getWayAreaPanel();
	}

	@Override
	public void setPluginManager(SmedPluginManager manager) {
		this.manager = manager;
	}

}
