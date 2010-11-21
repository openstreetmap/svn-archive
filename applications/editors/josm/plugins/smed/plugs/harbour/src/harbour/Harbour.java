
package harbour;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;

public class Harbour implements SmedPluggable {
	
	private int index = -1;
	private String msg = "";
	private SmedPluginManager manager = null;

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasFocus() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean lostFocus() {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfo() {
		return "Harbour editor";
	}

	@Override
	public JComponent getComponent() {
		manager.showVisualMessage(msg);
		return null;
	}

	@Override
	public void setPluginManager(SmedPluginManager manager) {
		this.manager = manager;
	}

}
