// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import static org.geotools.data.shapefile.files.ShpFileType.SHP;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.geotools.data.shapefile.files.ShpFileType;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.util.URLs;
import org.openstreetmap.josm.tools.JosmRuntimeException;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.ReflectionUtils;

/**
 * Extension of {@link ShpFiles} class modified to fit MapInfo TAB needs.
 */
public class TabFiles extends ShpFiles {

    /**
     * The urls for each type of file that is associated with the shapefile. The
     * key is the type of file
     */
    private final Map<ShpFileType, URL> urls;

    ////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public TabFiles(File headerFile, File dataFile) throws IllegalArgumentException {
        super(fakeShpFile(headerFile)); // Useless but necessary

        try {
            Field furls = ShpFiles.class.getDeclaredField("urls");
            ReflectionUtils.setObjectsAccessible(furls);
            urls = (Map<ShpFileType, URL>) furls.get(this);
        } catch (ReflectiveOperationException e) {
            throw new JosmRuntimeException(e);
        }

        init(URLs.fileToUrl(headerFile));
        urls.put(ShpFileType.DBF, URLs.fileToUrl(dataFile));
    }

    /**
     * Used only to give a fake shp file to ShpFiles constructor to avoid IllegalArgument at initialization.
     */
    private static URL fakeShpFile(File headerFile) {
        return URLs.fileToUrl(new File(headerFile.getAbsolutePath()+".shp"));
    }

    private String baseName(Object obj) {
        if (obj instanceof URL) {
            return toBase(((URL) obj).toExternalForm());
        }
        return null;
    }

    private String toBase(String path) {
        return path.substring(0, path.toLowerCase().lastIndexOf(".tab"));
    }

    ////////////////////////////////////////////////////

    private void init(URL url) {
        String base = baseName(url);
        if (base == null) {
            throw new IllegalArgumentException(
                    url.getPath()
                            + " is not one of the files types that is known to be associated with a MapInfo TAB file");
        }

        String urlString = url.toExternalForm();
        char lastChar = urlString.charAt(urlString.length()-1);
        boolean upperCase = Character.isUpperCase(lastChar);

        for (ShpFileType type : ShpFileType.values()) {

            String extensionWithPeriod = type.extensionWithPeriod;
            if (upperCase) {
                extensionWithPeriod = extensionWithPeriod.toUpperCase();
            } else {
                extensionWithPeriod = extensionWithPeriod.toLowerCase();
            }

            URL newURL;
            String string = base + extensionWithPeriod;
            try {
                newURL = new URL(url, string);
            } catch (MalformedURLException e) {
                // shouldn't happen because the starting url was constructable
                throw new RuntimeException(e);
            }
            urls.put(type, newURL);
        }

        // if the files are local check each file to see if it exists
        // if not then search for a file of the same name but try all combinations of the
        // different cases that the extension can be made up of.
        // IE Shp, SHP, Shp, ShP etc...
        if (isLocal()) {
            Set<Entry<ShpFileType, URL>> entries = urls.entrySet();
            Map<ShpFileType, URL> toUpdate = new HashMap<>();
            for (Entry<ShpFileType, URL> entry : entries) {
                if (!exists(entry.getKey())) {
                    url = findExistingFile(entry.getKey(), entry.getValue());
                    if (url != null) {
                        toUpdate.put(entry.getKey(), url);
                    }
                }
            }
            urls.putAll(toUpdate);
        }
    }

    private URL findExistingFile(ShpFileType shpFileType, URL value) {
        final File file = URLs.urlToFile(value);
        File directory = file.getParentFile();
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles((FilenameFilter) (dir, name) -> file.getName().equalsIgnoreCase(name));
            if (files.length > 0) {
                try {
                    return files[0].toURI().toURL();
                } catch (MalformedURLException e) {
                    Logging.error(e);
                }
            }
        }
        return null;
    }

    /**
     * This verifies that this class has been closed correctly (nothing locking)
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    @Override
    public String getTypeName() {
        String path = SHP.toBase(urls.get(SHP));
        int slash = Math.max(0, path.lastIndexOf('/') + 1);
        int dot = path.indexOf('.', slash);

        if (dot < 0) {
            dot = path.length();
        }

        return path.substring(slash, dot);
    }
}
