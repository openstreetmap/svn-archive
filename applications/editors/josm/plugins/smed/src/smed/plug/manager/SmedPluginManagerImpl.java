package smed.plug.manager;

import smed.plug.ifc.SmedPluginManager;
import smed.tabs.SmedTabAction;

public class SmedPluginManagerImpl implements SmedPluginManager {
	private String string = null;
	
	@Override
	public void showVisualMessage(String message) {
		if(SmedTabAction.smedStatusBar != null) SmedTabAction.smedStatusBar.setText(message);
	}

	@Override
	public void setString(String string) {
		this.string = string;
	}

	@Override
	public String getString() {
		return string;
	}

}
