package simple;

import toms.plug.ifc.Pluggable;
import toms.plug.ifc.PluginManager;

public class SimplePlugin implements Pluggable {

	private PluginManager manager; 
	
	@Override
	public boolean start() {
		this.manager.showVisualMessage("Started!");
		return true;
	}

	@Override
	public boolean stop() {
		this.manager.showVisualMessage("Stopped!");
		return true;
	}

	@Override
	public void setPluginManager(PluginManager manager) {
		this.manager = manager;		this.manager = manager;
		
	}

}
