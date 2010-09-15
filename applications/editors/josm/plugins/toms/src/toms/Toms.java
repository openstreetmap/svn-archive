// License: GPL. For details, see LICENSE file.
// Copyright (c) 2009 / 2010 by Werner Koenig

package toms;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.gui.MapFrame;

import toms.dialogs.SmpDialogAction;
import toms.plug.PluginApp;
import toms.plug.ifc.Pluggable;
import toms.plug.util.PluginLoader;
import toms.seamarks.SeaMark;

// Kommentar zum Ausprobieren von svn
// und nochmal
// und nochmal, diesmal mit kwallet als Passwortmanager

public class Toms extends Plugin {

    private JMenuItem Smp;
    private SmpDialogAction SmpDialog;

    public Toms(PluginInformation info) {
        super(info);

        String os = ""; //$NON-NLS-1$
        String userHome = ""; //$NON-NLS-1$

        SmpDialog = new SmpDialogAction();
        Smp = Main.main.menu.toolsMenu.add(SmpDialog);
        // Smp = MainMenu.add(Main.main.menu.toolsMenu, SmpDialog);

        SmpDialog.setSmpItem(Smp);
        SmpDialog.setOs(os);
        SmpDialog.setUserHome(userHome);
        Smp.setEnabled(false);

        File pluginDir = Main.pref.getPluginsDirectory();
        String pluginDirName = pluginDir.getAbsolutePath();
        File tplug = new File(pluginDirName + "/tplug");
        if(!tplug.exists()) tplug.mkdir();
        
        // build ifc.jar from toms.jar
        JarEntry ent = null;
        BufferedInputStream inp = null;
        String entName = null;
        byte[] buffer = new byte[16384];
        int len;

        try {
            JarFile file = new JarFile(pluginDirName  + "/toms.jar");           
            FileOutputStream fos = new FileOutputStream(pluginDirName + "/tplug/ifc.jar");          
            JarOutputStream jos = new JarOutputStream(fos);
            BufferedOutputStream oos = new BufferedOutputStream( jos);

            ent = file.getJarEntry("toms/plug/ifc/Pluggable.class");
            inp = new BufferedInputStream(file.getInputStream( ent ));
            entName = ent.getName();

            jos.putNextEntry(new JarEntry(entName));
            
            while ((len = inp.read(buffer)) > 0) {
                oos.write(buffer, 0, len);
            }

            oos.flush();
            inp.close();
        
            ent = file.getJarEntry("toms/plug/ifc/PluginManager.class");
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
        
        
        // add ifc.jar to classpath (josm need this archive, or perhaps only the interface)
        File f = new java.io.File(pluginDirName + "/tplug/ifc.jar");
        ClassLoader myClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            addUrlMethod.setAccessible(true);
            addUrlMethod.invoke(myClassLoader, f.toURI().toURL());
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        try {
            PluginApp.runPlugins();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            Smp.setEnabled(true);
        } else {
            Smp.setEnabled(false);
            SmpDialog.CloseDialog();
        }
    }

}
