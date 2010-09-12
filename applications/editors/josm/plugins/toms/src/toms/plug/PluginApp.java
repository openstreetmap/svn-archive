package toms.plug;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openstreetmap.josm.Main;


import toms.plug.ifc.Pluggable;
import toms.plug.ifc.PluginManager;
import toms.plug.util.PluginLoader;

public class PluginApp implements Runnable {
	
	public static void runPlugins() throws IOException {
		String pluginDirName = Main.pref.getPluginsDirectory().getAbsolutePath();

		List<Pluggable> plugins = PluginLoader.loadPlugins(new File(pluginDirName + "/tplug"));

		if(plugins == null) return;
		
		PluginManager manager = new PluginManagerImpl();
		
		for(Pluggable p : plugins) p.setPluginManager(manager);
		for(Pluggable p : plugins) p.start();
		
		// wait
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(Pluggable p: plugins) p.stop();
	}

	@Override
	public void run() {
		try {
			runPlugins();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
