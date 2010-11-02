package smed_fw;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

import smed_fw.io.JARFileFilter;
import smed_fw.io.SmedFWFile;

public class SmedFW extends Plugin {
	
	public static final String FW_BIN_DIR				= "bin";
	public static final String FW_SMED_JAR			= "smed_fw.jar";
	public static final String FW_NAME				= "felix.jar";
	public static final String FW_BUNDLE_LOCATION		= "fwplug";
	
	public SmedFW(PluginInformation info) {
		super(info);

        JarEntry e = null;
        BufferedInputStream inp = null;
        byte[] buffer = new byte[16384];
        int len;
        List<String> lib = new ArrayList<String>();
        
        lib.add("org.apache.felix.scr-1.4.0.jar");
        lib.add("osgi.cmpn-4.2.1.jar");

        String eName = null;
		String pluginDirName = Main.pref.getPluginsDirectory().getAbsolutePath() + "/";
		File fwDir = new File(pluginDirName + FW_BIN_DIR);

        if(!fwDir.exists()) fwDir.mkdir();
        
		SmedFWFile fwplugDir = new SmedFWFile(pluginDirName + FW_BUNDLE_LOCATION);
		if(!fwplugDir.exists()) fwplugDir.mkdir();
		
		File[] jars = fwplugDir.listFiles(new JARFileFilter());

		// extract framework and plugins
		try {
			JarFile file = new JarFile(pluginDirName  + FW_SMED_JAR);

			boolean fwFound = false;
			FileOutputStream pfos = null;
			Enumeration<JarEntry> ent = file.entries();
            while(ent.hasMoreElements()) {
                e = ent.nextElement();
                eName = e.getName();
                
                if(eName.endsWith(".jar")) {
                	if(eName.equals(FW_NAME)) {
                		pfos = new FileOutputStream(pluginDirName + FW_BIN_DIR + "/" + FW_NAME);
                		fwFound = true;
                	}
                	else { 
                		pfos = new FileOutputStream(pluginDirName + FW_BUNDLE_LOCATION + "/" + eName);
                		fwFound = false;
                	}
                	if(fwFound || lib.contains(eName)|| fwplugDir.needUpdate(jars,eName)) {
                		BufferedOutputStream pos = new BufferedOutputStream(pfos);
                		inp = new BufferedInputStream(file.getInputStream( e ));

                		while ((len = inp.read(buffer)) > 0) pos.write(buffer, 0, len);
                    
                		pos.flush();
                		pos.close();
                		inp.close();
                		pfos.close();
                	}
                }
            }
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
					
		RunFW runFW = new RunFW();
		runFW.init();
		System.out.println("SmedFW (OSGi-Implementation) noch nicht weiter programmiert");
	}
	
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
        	System.out.println("josm zeigt Karte");
        } else {
        	System.out.println("josm zeigt keine Karte");
        }
	}

}
