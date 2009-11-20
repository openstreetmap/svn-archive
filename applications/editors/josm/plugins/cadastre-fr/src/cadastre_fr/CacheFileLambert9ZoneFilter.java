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
        new CacheFileLambert9ZoneFilter("cc1", tr("Lambert CC9 Zone 1 cache file (.CC1)")),
        new CacheFileLambert9ZoneFilter("cc2", tr("Lambert CC9 Zone 2 cache file (.CC2)")),
        new CacheFileLambert9ZoneFilter("cc3", tr("Lambert CC9 Zone 3 cache file (.CC3)")),
        new CacheFileLambert9ZoneFilter("cc4", tr("Lambert CC9 Zone 4 cache file (.CC4)")),
        new CacheFileLambert9ZoneFilter("cc5", tr("Lambert CC9 Zone 5 cache file (.CC5)")),
        new CacheFileLambert9ZoneFilter("cc6", tr("Lambert CC9 Zone 6 cache file (.CC6)")),
        new CacheFileLambert9ZoneFilter("cc7", tr("Lambert CC9 Zone 7 cache file (.CC7)")),
        new CacheFileLambert9ZoneFilter("cc8", tr("Lambert CC9 Zone 8 cache file (.CC8)")),
        new CacheFileLambert9ZoneFilter("cc9", tr("Lambert CC9 Zone 9 cache file (.CC9)"))
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
