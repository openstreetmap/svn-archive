//License: GPL
package plastic_laf;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.openstreetmap.josm.plugins.PluginInformation;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

/**
 * Plugin that brings JGoodies Plastic Look and Feel to JOSM.
 */
public class Plugin {

	/**
	 * Constructs a new {@code Plugin}.
	 * @param info plugin info
	 * @throws UnsupportedLookAndFeelException if look and feel cannot be set
	 */
    public Plugin(PluginInformation info) throws UnsupportedLookAndFeelException {
        UIManager.getDefaults().put("ClassLoader", getClass().getClassLoader());
    	UIManager.installLookAndFeel("Plastic", PlasticLookAndFeel.class.getName());
    	UIManager.installLookAndFeel("Plastic3D", Plastic3DLookAndFeel.class.getName());
    	UIManager.installLookAndFeel("PlasticXP", PlasticXPLookAndFeel.class.getName());
    }
}
