package smed.plug.manager;

import smed.plug.ifc.SmedPluginManager;

public class SmedPluginManagerImpl implements SmedPluginManager {
	private static String string = null;
	
	@Override
	public void showVisualMessage(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setString(String string) {
		System.out.println("setString string:\t" + string);
		this.string = string;
	}

	@Override
	public String getString() {
		System.out.println("getString string\t" + string);
		return string;
	}

}
