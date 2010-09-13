package toms.plug.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import toms.plug.ifc.Pluggable;


public class PluginLoader {

//!!	public static List<Pluggable> loadPlugins(File plugDir) throws IOException {
//!!		File[] plugJars = plugDir.listFiles(new JARFileFilter());
//!!		ClassLoader cl = new URLClassLoader(PluginLoader.fileArrayToURLArray(plugJars));
		
//!!		if(cl == null) return null;
		
//!!		List<Class<Pluggable>> plugClasses = PluginLoader.extractClassesFromJARs(plugJars, cl);
		
//!!		return PluginLoader.createPluggableObjects(plugClasses);
//!!	}

	private static List<Pluggable> createPluggableObjects(List<Class<Pluggable>> pluggables) {
		List<Pluggable> plugs = new ArrayList<Pluggable>(pluggables.size());
		for(Class<Pluggable> plug : pluggables) {
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

	private static List<Class<Pluggable>> extractClassesFromJARs(	File[] jars, ClassLoader cl) throws FileNotFoundException, IOException {
		List<Class<Pluggable>> classes = new ArrayList<Class<Pluggable>>();
		
		for(File jar : jars) {
			classes.addAll(PluginLoader.extractClassesFromJAR(jar, cl));
		}

		return classes;
	}

	@SuppressWarnings("unchecked")
	private static Collection<? extends Class<Pluggable>> extractClassesFromJAR(File jar, ClassLoader cl) throws FileNotFoundException, IOException {
		List<Class<Pluggable>> classes = new ArrayList<Class<Pluggable>>();
		JarInputStream jaris = new JarInputStream(new FileInputStream(jar));
		JarEntry ent = null;
		
		while ((ent = jaris.getNextJarEntry()) != null) {
			String entName = ent.getName(); //.toLowerCase();
			
			if (entName.endsWith(".class")) {
				try {
					Class<?> cls = cl.loadClass(entName.substring(0, entName.length()- 6).replace('/', '.'));
					if (PluginLoader.isPluggableClass(cls)) classes.add((Class<Pluggable>) cls);
				} catch (ClassNotFoundException e) {
					System.err.println("Can't load Class" + entName);
					e.printStackTrace();
				}
			}
		}
		
		jaris.close();
		
		return classes;
	}

	private static boolean isPluggableClass(Class<?> cls) {
		for (Class<?> i: cls.getInterfaces()) {
			if (i.equals(Pluggable.class)) return true;
		}
		
		return false;

	}

	private static URL[] fileArrayToURLArray(File[] files) throws MalformedURLException {
		URL[] urls = new URL[files.length];
		
		if(urls == null) return null;
		
		for(int i = 0; i < files.length; i++) {
			urls[i] = files[i].toURI().toURL();
		}
		
		return urls;
	}

}
