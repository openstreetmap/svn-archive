// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.io.File;
import javax.swing.filechooser.FileFilter;

public class CacheFileFilter extends FileFilter {

    /**
     * Derived from ExtensionFileFilter writen by imi
     */
    private final String extension;
    private final String description;

    public static CacheFileFilter[] filters = {
        new CacheFileFilter("1", tr("Lambert Zone 1 cache file (.1)")),
        new CacheFileFilter("2", tr("Lambert Zone 2 cache file (.2)")),
        new CacheFileFilter("3", tr("Lambert Zone 3 cache file (.3)")),
        new CacheFileFilter("4", tr("Lambert Zone 4 cache file (.4)"))
        };

    /**
     * Construct an extension file filter by giving the extension to check after.
     *
     */
    private CacheFileFilter(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    public boolean acceptName(String filename) {
        String name = filename.toLowerCase();
        for (String ext : extension.split(","))
            if (name.endsWith("." + ext))
                return true;
        return false;
    }

    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory())
            return true;
        return acceptName(pathname.getName());
    }

    @Override
    public String getDescription() {
        return description;
    }

}
