package smed_fw;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.FelixConstants;
import org.openstreetmap.josm.Main;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

import smed_fw.io.JARFileFilter;

public class RunFW implements BundleActivator{
	
	private BundleContext context;
	private Felix felix;
	private String pluginDirName;

	
	@SuppressWarnings("unchecked")
	public void init() {
		Map config = new HashMap();
		List list = new ArrayList();

	
		File pluginDir = Main.pref.getPluginsDirectory();
		pluginDirName = pluginDir.getAbsolutePath()+ "/";
		
        
        
		list.add(this);
		// config.put(AutoProcessor.AUTO_START_PROP + ".1",
		//		"file:" + pluginDirName + "/bundle/de.vogella.felix.firstbundle_1.0.0.201010271606.jar");
		config.put(BundleCache.CACHE_ROOTDIR_PROP, pluginDirName);
		config.put(Constants.FRAMEWORK_STORAGE_CLEAN,"onFirstInit");
		config.put(FelixConstants.LOG_LEVEL_PROP, "4");
		config.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);
		
		// Now create an instance of the framework with
		// our configurations properties
		felix = new Felix(config);
        
		// now start Felix instance
		try {
			felix.start();
		} catch (BundleException e) {
			System.err.println("Could not generate framework: " + e);
			e.printStackTrace();
		}
	}


	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		Bundle b = null;
		// Bundle b = context.installBundle("file:" + pluginDirName + "bundle/de.vogella.felix.firstbundle_1.0.0.201010271606.jar");
		// Bundle b = context.installBundle("file:" + pluginDirName + "bundle/de.vogella.felix.firstbundle_1.0.0.201010271606.jar");
		// b.start();
		File plugDir = new File(pluginDirName + SmedFW.FW_BUNDLE_LOCATION);
		
		File[] plugins = plugDir.listFiles(new JARFileFilter());
		if(plugins != null) {
			for(File p : plugins) {
				b = context.installBundle("file:" + p.getAbsolutePath());
				b.start();
			}
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("Framework gestoppt");
		context = null;
	}

	public Bundle[] getBundles() {
		if(context != null) return context.getBundles();
		
		return null;
	}
}
