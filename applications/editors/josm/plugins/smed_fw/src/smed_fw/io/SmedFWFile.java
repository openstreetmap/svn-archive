package smed_fw.io;

import java.io.File;

public class SmedFWFile extends File{

	public SmedFWFile(String pathname) {
		super(pathname);
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
}
