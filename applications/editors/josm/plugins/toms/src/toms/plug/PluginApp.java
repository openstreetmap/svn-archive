package toms.plug;

import java.io.File;
import java.util.List;

import toms.plug.ifc.Pluggable;
import toms.plug.util.PluginLoader;

public class PluginApp {
	
	public static void runPlugins() {
		List<Pluggable> plugins = PluginLoader.loadPlugins(new File("./tplug"));
		System.out.println("hello world");
	}

}
