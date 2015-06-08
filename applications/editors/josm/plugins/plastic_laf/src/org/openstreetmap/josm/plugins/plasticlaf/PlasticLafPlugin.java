// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.plasticlaf;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

/**
 * PlasticLafPlugin that brings JGoodies Plastic Look and Feel to JOSM.
 */
public class PlasticLafPlugin extends Plugin {

    /**
     * Constructs a new {@code PlasticLafPlugin}.
     * @param info plugin info
     * @throws UnsupportedLookAndFeelException if look and feel cannot be set
     */
    public PlasticLafPlugin(PluginInformation info) throws UnsupportedLookAndFeelException {
        super(info);
        UIManager.getDefaults().put("ClassLoader", getClass().getClassLoader());
        UIManager.installLookAndFeel("Plastic", PlasticLookAndFeel.class.getName());
        UIManager.installLookAndFeel("Plastic3D", Plastic3DLookAndFeel.class.getName());
        UIManager.installLookAndFeel("PlasticXP", PlasticXPLookAndFeel.class.getName());
    }
}
