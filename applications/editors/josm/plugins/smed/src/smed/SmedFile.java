package smed;

import java.io.File;

import smed.plug.util.JARFileFilter;

public class SmedFile extends File{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SmedFile(String pathname) {
		super(pathname);
	}

	public boolean needUpdate(File[] jars, String name) {
		
		for(File j : jars) {
			String jName = j.getName();
			
			if(jName.length() > 15) { // < 16 isn'nt a SmedFile

				if (jName.substring(16).equals(name.substring(16))) {
					if(jName.substring(0, 15).compareTo(name.substring(0, 15)) < 0) { // obsolet
						j.delete();
						return true;			
					} else return false;	// no update needed
				}
			}
		}

		// nothing found -> extract
		return true;
	}
}
