// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.layer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Filter for the file dialog. Allows only calibration files.
 */
public class CalibrationFileFilter extends FileFilter {

    // Extension used by calibration files
    public static final String EXTENSION = ".cal";

    @Override
    public boolean accept(File f) {
        String ext3 = (f.getName().length() > 4) ? f.getName().substring(f.getName().length() - 4).toLowerCase() : "";

        // TODO: check what is supported by Java :)
        return (f.isDirectory() || ext3.equals(EXTENSION));
    }

    @Override
    public String getDescription() {
        return tr("Calibration Files")+ " (*" + EXTENSION + ")";
    }
}
