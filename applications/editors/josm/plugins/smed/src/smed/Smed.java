package smed;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

import smed.plug.SmedPluginApp;
import smed.plug.ifc.SmedPluggable;
import smed.plug.util.SmedPluginLoader;
import smed.tabs.SmedTabAction;

public class Smed extends Plugin{

    private JMenuItem item;
    private SmedTabAction SmedTab;

    public Smed(PluginInformation info) {
        super(info);

        String os = "";
        String userHome = "";

        File pluginDir = Main.pref.getPluginsDirectory();
        String pluginDirName = pluginDir.getAbsolutePath();
        File splug = new File(pluginDirName + "/splug");
        if(!splug.exists()) splug.mkdir();

        // build smed_ifc.jar from smed.jar
        JarEntry ent = null;
        BufferedInputStream inp = null;
        String entName = null;
        byte[] buffer = new byte[16384];
        int len;

        try {
            JarFile file = new JarFile(pluginDirName  + "/smed.jar");
            FileOutputStream fos = new FileOutputStream(pluginDirName + "/splug/smed_ifc.jar");
            JarOutputStream jos = new JarOutputStream(fos);
            BufferedOutputStream oos = new BufferedOutputStream( jos);

            ent = file.getJarEntry("smed/plug/ifc/SmedPluggable.class");
            inp = new BufferedInputStream(file.getInputStream( ent ));
            entName = ent.getName();

            jos.putNextEntry(new JarEntry(entName));

            while ((len = inp.read(buffer)) > 0) {
                oos.write(buffer, 0, len);
            }

            oos.flush();
            inp.close();

            ent = file.getJarEntry("smed/plug/ifc/SmedPluginManager.class");
            inp = new BufferedInputStream(file.getInputStream( ent ));
            entName = ent.getName();
            jos.putNextEntry(new JarEntry(entName));

            while ((len = inp.read(buffer)) > 0) {
                oos.write(buffer, 0, len);
            }

            oos.flush();
            oos.close();
            fos.flush();
            fos.close();
            inp.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


        // add smed_ifc.jar to classpath (josm need this archive, or perhaps only the interface)
        File f = new java.io.File(pluginDirName + "/splug/smed_ifc.jar");
        ClassLoader myClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            addUrlMethod.setAccessible(true);
            addUrlMethod.invoke(myClassLoader, f.toURI().toURL());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SmedTab = new SmedTabAction();
        item = Main.main.menu.toolsMenu.add(SmedTab);

        item.setEnabled(false);

    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            item.setEnabled(true);
        } else {
            item.setEnabled(false);
            // SmpDialog.CloseDialog();
        }
    }

}
