package smed_fw.io;

import java.io.File;
import java.io.FileFilter;

public class JARFileFilter implements FileFilter {

	@Override
    public boolean accept(File f) {
        return f.getName().toLowerCase().endsWith(".jar");
    }

}
