// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.flatlaf;

import javax.swing.UIManager;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * FlatLaf for JOSM
 */
public class FlatLafPlugin extends Plugin {

    /**
     * Constructs a new {@code FlatLafPlugin}.
     *
     * @param info plugin info
     */
    public FlatLafPlugin(PluginInformation info) {
        super(info);
        UIManager.getDefaults().put("ClassLoader", getClass().getClassLoader());
        UIManager.installLookAndFeel("FlatLaf Light", FlatLightLaf.class.getName());
        UIManager.installLookAndFeel("FlatLaf Dark", FlatDarkLaf.class.getName());
    }

}
