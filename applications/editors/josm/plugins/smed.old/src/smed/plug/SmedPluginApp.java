package smed.plug;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openstreetmap.josm.Main;

import smed.plug.ifc.SmedPluggable;
import smed.plug.util.SmedPluginLoader;

public class SmedPluginApp implements Runnable {

    @Override
    public void run() {
        try {
            runPlugins();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runPlugins() throws IOException {
        String pluginDirName = Main.pref.getPluginsDirectory().getAbsolutePath();
        
        List<SmedPluggable> plugins = SmedPluginLoader.loadPlugins(new File(pluginDirName + "/splug"));
        
    }

}
