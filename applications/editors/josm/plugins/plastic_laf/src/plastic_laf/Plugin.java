package plastic_laf;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.openstreetmap.josm.plugins.PluginInformation;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;

public class Plugin {
    public Plugin(PluginInformation info) throws UnsupportedLookAndFeelException {
        UIManager.getDefaults().put("ClassLoader", getClass().getClassLoader());
        UIManager.setLookAndFeel(new PlasticLookAndFeel());
    }
}
