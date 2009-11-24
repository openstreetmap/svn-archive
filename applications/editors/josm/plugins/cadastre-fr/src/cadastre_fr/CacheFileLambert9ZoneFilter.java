// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class CacheFileLambert9ZoneFilter extends FileFilter {

    /**
     * Derived from ExtensionFileFilter writen by imi
     */
    private final String extension;
    private final String description;

    public static CacheFileLambert9ZoneFilter[] filters = {
        new CacheFileLambert9ZoneFilter("cc1", tr("Lambert CC9 Zone {0} cache file (.CC{0})",1)),
        new CacheFileLambert9ZoneFilter("cc2", tr("Lambert CC9 Zone {0} cache file (.CC{0})",2)),
        new CacheFileLambert9ZoneFilter("cc3", tr("Lambert CC9 Zone {0} cache file (.CC{0})",3)),
        new CacheFileLambert9ZoneFilter("cc4", tr("Lambert CC9 Zone {0} cache file (.CC{0})",4)),
        new CacheFileLambert9ZoneFilter("cc5", tr("Lambert CC9 Zone {0} cache file (.CC{0})",5)),
        new CacheFileLambert9ZoneFilter("cc6", tr("Lambert CC9 Zone {0} cache file (.CC{0})",6)),
        new CacheFileLambert9ZoneFilter("cc7", tr("Lambert CC9 Zone {0} cache file (.CC{0})",7)),
        new CacheFileLambert9ZoneFilter("cc8", tr("Lambert CC9 Zone {0} cache file (.CC{0})",8)),
        new CacheFileLambert9ZoneFilter("cc9", tr("Lambert CC9 Zone {0} cache file (.CC{0})",9))
        };

    /**
     * Construct an extension file filter by giving the extension to check after.
     *
     */
    private CacheFileLambert9ZoneFilter(String extension, String description) {
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
