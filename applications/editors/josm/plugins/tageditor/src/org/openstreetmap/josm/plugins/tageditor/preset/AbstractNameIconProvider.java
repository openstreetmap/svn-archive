package org.openstreetmap.josm.plugins.tageditor.preset;

import java.awt.Image;
import java.io.File;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.ImageProvider;

public abstract class AbstractNameIconProvider implements INameIconProvider {
    
    protected String name;
    protected String iconName;
    protected ImageIcon icon;
    protected File zipIconArchive;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName, File zipIconArchive) {
        this.iconName = iconName;
    }
    
    public Icon getIcon() {
        if (icon == null && getIconName() != null) {
            Collection<String> s = Main.pref.getCollection("taggingpreset.icon.sources", null);
            icon = new ImageProvider(getIconName()).setDirs(s).setId("presets").setArchive(zipIconArchive).setOptional(true)
            .setMaxWidth(16).setMaxHeight(16).get();
        }
        return icon;
    }
}
