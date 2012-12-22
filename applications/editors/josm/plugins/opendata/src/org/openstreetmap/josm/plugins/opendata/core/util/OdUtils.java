//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleHandler;
import org.openstreetmap.josm.tools.ImageProvider;

public abstract class OdUtils {
	
    private static final String TEMP_DIR_PREFIX = "josm_opendata_temp_";
    
    public static final boolean isMultipolygon(OsmPrimitive p) {
        return p instanceof Relation && ((Relation) p).isMultipolygon();
    }
    
	public static final String[] stripQuotes(String[] split, String sep) {
		List<String> result = new ArrayList<String>();
		boolean append = false;
		for (int i = 0; i<split.length; i++) {
			if (append) {
				int index = result.size()-1;
				if (split[i].endsWith("\"") && StringUtils.countMatches(split[i], "\"") % 2 == 1) {
					append = false;
				}
				result.set(index, result.get(index)+sep+split[i].replaceAll("\"", ""));
			} else if (split[i].startsWith("\"")) {
				if (!(split[i].endsWith("\"") && StringUtils.countMatches(split[i], "\"") % 2 == 0)) {
					append = true;
				}
				result.add(split[i].replaceAll("\"", ""));
			} else {
				result.add(split[i]);
			}
		}
		return result.toArray(new String[0]);
	}
	
	public static final ImageIcon getImageIcon(String iconName) {
		return getImageIcon(iconName, false);
	}
	
	public static final ImageIcon getImageIcon(String iconName, boolean optional) {
		return new ImageProvider(iconName).setOptional(optional).setAdditionalClassLoaders(ModuleHandler.getResourceClassLoaders()).get();
	}
	
	public static final String getJosmLanguage() {
		String lang = Main.pref.get("language");
		if (lang == null || lang.isEmpty()) {
			lang = Locale.getDefault().toString();
		}
		return lang;
	}
	
    public static final double convertMinuteSecond(double minute, double second) {
        return (minute/60.0) + (second/3600.0);
    }

    public static final double convertDegreeMinuteSecond(double degree, double minute, double second) {
        return degree + convertMinuteSecond(minute, second);
    }
    
    public static final File createTempDir() throws IOException {
        final File temp = File.createTempFile(TEMP_DIR_PREFIX, Long.toString(System.nanoTime()));

        if (!temp.delete()) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!temp.mkdir()) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        
        return temp;
    }
    
    public static final void deleteDir(File dir) {
        for (File file : dir.listFiles()) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }
        if (!dir.delete()) {
            dir.deleteOnExit();
        }
    }
    
    public static final void deletePreviousTempDirs() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        if (tmpDir.exists() && tmpDir.isDirectory()) {
            for (File dir : tmpDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(TEMP_DIR_PREFIX);
                }
            })) {
                deleteDir(dir);
            }
        }
    }
}
