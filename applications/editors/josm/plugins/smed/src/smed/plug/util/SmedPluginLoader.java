package smed.plug.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import smed.plug.ifc.SmedPluggable;


public class SmedPluginLoader {


    public static List<SmedPluggable> loadPlugins(File plugDir) throws IOException {
        File[] plugJars = plugDir.listFiles(new JARFileFilter());
        Arrays.sort(plugJars);
        
        URL[] urls = fileArrayToURLArray(plugJars);
        if(urls == null) return null;
        
        ClassLoader cl = new URLClassLoader(urls);
        List<Class<SmedPluggable>> plugClasses = extractClassesFromJARs(plugJars, cl);
        
        if(plugClasses == null) return null;
        else return createPluggableObjects(plugClasses);
    }

    private static List<SmedPluggable> createPluggableObjects(List<Class<SmedPluggable>> pluggables) {
        List<SmedPluggable> plugs = new ArrayList<SmedPluggable>(pluggables.size());
        for(Class<SmedPluggable> plug : pluggables) {
            try {
                plugs.add(plug.newInstance());
            } catch (InstantiationException e) {
                System.err.println("Can't instantiate plugin: " + plug.getName());
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                System.err.println("IllegalAccess for plugin: " + plug.getName());
                e.printStackTrace();
            }
        }
        
        return plugs;
    }

    private static List<Class<SmedPluggable>> extractClassesFromJARs(File[] jars, ClassLoader cl) throws FileNotFoundException, IOException {
        List<Class<SmedPluggable>> classes = new ArrayList<Class<SmedPluggable>>();
        
        for(File jar : jars) {
            classes.addAll(extractClassesFromJAR(jar, cl));
        }

        if(classes.isEmpty()) return null;
        else return classes;
    }

    @SuppressWarnings("unchecked")
    private static Collection<? extends Class<SmedPluggable>> extractClassesFromJAR (File jar, ClassLoader cl) throws FileNotFoundException, IOException {
        List<Class<SmedPluggable>> classes = new ArrayList<Class<SmedPluggable>>();
        JarInputStream jaris = new JarInputStream(new FileInputStream(jar));
        JarEntry ent = null;
        
        while ((ent = jaris.getNextJarEntry()) != null) {
            String entName = ent.getName(); //.toLowerCase();
            
            if (entName.endsWith(".class")) {
                try {
                    Class<?> cls = cl.loadClass(entName.substring(0, entName.length()- 6).replace('/', '.'));
                    if(isPluggableSmedClass(cls)) classes.add((Class<SmedPluggable>) cls);
                } catch (ClassNotFoundException e) {
                    System.err.println("Can't load Class" + entName);
                    e.printStackTrace();
                }
            }
        }
        
        jaris.close();
        
        return classes;
    }

    private static boolean isPluggableSmedClass(Class<?> cls) {
        for (Class<?> i: cls.getInterfaces()) {
            if (i.equals(SmedPluggable.class)) return true;
        }
        
        return false;
    }

    private static URL[] fileArrayToURLArray(File[] files) throws MalformedURLException {
        // splug contains at least smed_ifc.jar, but smed_ifc.jar isn't pluggable
        if(files.length <= 1) return null;
        
        URL[] urls = new URL[files.length];
        
        
        for(int i = 0; i < files.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }
        
        return urls;
    }
}
