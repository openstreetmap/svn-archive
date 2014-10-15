// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.datasets;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;

public class DataSetCategory {

    private final String name;
    private final ImageIcon icon;
    
    public DataSetCategory(String name, ImageIcon icon) {
        this.name = name;
        this.icon = icon;
    }
    
    public DataSetCategory(String name, String iconName) {
        this(name, iconName != null && !iconName.isEmpty() ? OdUtils.getImageIcon(iconName, true) : null);
    }

    public final String getName() {
        return name;
    }

    public final ImageIcon getIcon() {
        return icon;
    }
}
