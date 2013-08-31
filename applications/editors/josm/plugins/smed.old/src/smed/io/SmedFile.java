package smed.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import smed.plug.ifc.SmedPluggable;
import smed.plug.util.JARFileFilter;

public class SmedFile extends File{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static File plugDir = null;
    private static String pathname = null;
    private static File[] plugJars = null; 

    public SmedFile(String pathname) {
        super(pathname);
        this.pathname = pathname;
        plugDir = new File(pathname);
        plugJars = plugDir.listFiles(new JARFileFilter());
    }

    /**
     * show if plugin need update
     * 
     * @param jars
     * @param name
     * @return boolean true/false
     */
    public boolean needUpdate(File[] jars, String name) {
        for(File j : jars) {
            String jName = j.getName();
            
            if(jName.length() > 15) { // < 16 isn'nt a SmedFile

                if (jName.substring(16).equals(name.substring(16))) {
                    if(jName.substring(0, 15).compareTo(name.substring(0, 15)) < 0) { // obsolet
                        j.delete();
                        return true;            
                    } else return false;    // no update needed
                }
            }
        }

        // nothing found -> extract
        return true;
    }

    
    
    /**
     * show if plugin is visible
     * 
     * @param name
     * @return boolean true/false
     */
	public boolean isVisible(String name) { return getAttributeState("visible", name); }
	
	/**
	 * show if plugin is deleted
	 * 
	 * @param name
	 * @return boolean true/false
	 */
	public boolean isDeleted(String name) { return getAttributeState("deleted", name); }
	
	public boolean getAttributeState(String attribute, String name) {		
		File f = getFile(plugJars, name, true);
		
		if(f == null) return false;
		else {
			String str;
			JarFile test;
			try {
				test = new JarFile(pathname + "/.ini/MF" + f.getName().substring(16));
				Manifest mf = test.getManifest();
				str = mf.getMainAttributes().getValue(attribute);
				
				if(str == null || !str.equals("yes")) return false;

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return true;
		}

	}

	/**
	 * search file in files
	 *  
	 * @param
	 * @return if found -> File, if not found -> null
	 */
	private static File getFile(File[] files, String name, Boolean isSmed) {
		for(int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();
			
			if(isSmed) {
				if(fileName.length() > 15) { // < 16 isn'nt a SmedFile
					if (fileName.substring(16).equals("_" + name)) return files[i];
				}
			} else if(fileName.equals(name)) return files[i];
		}
		return null;
	}

	/**
	 * set the state "visible" of the plugin (name)
	 * 
	 * @param name
	 * @param isVisible
	 */
	public void setVisible(String name, boolean isVisible) { setAttr("visible", name, isVisible); }
	
	/**
	 * set the state "deleted" of the plugin (name)
	 * 
	 * @param name
	 * @param isDeleted 
	 */
	public void setDeleted(String name, boolean isDeleted) { setAttr("deleted", name, isDeleted); }


	private void setAttr(String attribute, String name, boolean b) {		
		File f = getFile(plugJars,name,true);
		if(f == null) return;
		else {
			try {
				JarFile file = new JarFile(pathname + "/.ini/MF" + f.getName().substring(16));
				
				Manifest mf = file.getManifest();
				Attributes attr = mf.getMainAttributes();
				if(b) attr.putValue(attribute,"yes");
				else attr.putValue(attribute,"no");
				
				FileOutputStream fos = new FileOutputStream(pathname + "/.ini/MF" + f.getName().substring(16));
				JarOutputStream jos = new JarOutputStream(fos,mf);
				jos.close();
				fos.flush();
				fos.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * copies the manifests from the plugin.jar to
	 * .ini/mf_plugin.jar. Necessary, because you
	 * can't modified the original jars under windows,
	 * (because under windows, they are used)
	 */
	private static void copyMFs(File[] plugInis) {
		if(plugJars == null) return;
		
		for(int i = 0; i < plugJars.length; i++) {
			String source = plugJars[i].getName();
			String dest;
			
			if(source.length() > 15) { // < 16 isn'nt a SmedFile
				dest = "MF" + source.substring(16);

				if(getFile(plugInis, dest, false) == null) {
					try {
						JarFile file = new JarFile(pathname + "/" + source);
						Manifest mf = file.getManifest();
						Attributes attr = mf.getMainAttributes();
						attr.putValue("visible","yes");
						attr.putValue("deleted","no");

						FileOutputStream fos = new FileOutputStream(pathname + "/.ini/" + dest);
						JarOutputStream jos = new JarOutputStream(fos,mf);
						
						jos.close();
						fos.flush();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}
			
			}
		}
		
	}

	public static void init() {
		File splugDirIni = new File(pathname + "/.ini");
		
		if(!splugDirIni.exists()) splugDirIni.mkdir();
		File[] plugInis = splugDirIni.listFiles(new JARFileFilter());
		
		copyMFs(plugInis);
		
		SmedFile smedDir = new SmedFile(pathname);
		
		for(int i = 0; i < plugJars.length; i++) {
			File file = plugJars[i];
			
			if(file.getName().length() > 16) { // < 16 isn'nt a SmedFile
				String name = file.getName().substring(17);
				
				if(smedDir.isDeleted(name)) {
					File fileMF = new File(pathname + "/.ini/MF_" + name);
					
					fileMF.delete();
					file.delete();
				}
			}
		}
	}

	public static void createMF(String name) {
		String dest;
		if(name.length() > 17) { // < 17 isn'nt a SmedFile
			dest = "MF_" + name.substring(17);
			try {
				JarFile file = new JarFile(pathname + "/" + name);
				Manifest mf = file.getManifest();
				Attributes attr = mf.getMainAttributes();
				attr.putValue("visible","yes");
				attr.putValue("deleted","no");
				
				FileOutputStream fos = new FileOutputStream(pathname + "/.ini/" + dest);
				JarOutputStream jos = new JarOutputStream(fos,mf);
				
				jos.close();
				fos.flush();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
