package smed_fw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class SmedFW extends Plugin implements BundleActivator{

	private BundleContext context;
	private Felix felix;
	
	public SmedFW(PluginInformation info) {
		super(info);
		
		init();
		System.out.println("SmedFW (OSGi-Implementation) noch nicht weiter programmiert");
	}

	private void init() {
		Map config = new HashMap();
		List list = new ArrayList();
		
		list.add(this);
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
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		context = null;
	}
	
	public Bundle[] getBundles() {
		if(context != null) return context.getBundles();
		
		return null;
	}

}
