package org.openstreetmap.josm.plugins.slippymap;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.tools.GBC;

/**
 * Preference Dialog for Slippy Map Tiles
 * 
 * @author Hakan Tandogan <hakan@gurkensalat.com>
 * 
 */
public class SlippyMapPreferenceSetting implements PreferenceSetting
{
    /**
     * ComboBox with all known tile sources.
     */
    private JComboBox tileSourceCombo;

    public void addGui(PreferenceDialog gui)
    {
        String[] allMapUrls = SlippyMapPreferences.getAllMapUrls();
        tileSourceCombo = new JComboBox(allMapUrls);

        String source = SlippyMapPreferences.getMapUrl();

        for (int i = 0; i < allMapUrls.length; i++)
        {
//            System.err.println("Comparing '" + source + "' to '"
//                   + allMapUrls[i]);

            if (source.equals(allMapUrls[i]))
            {
                tileSourceCombo.setSelectedIndex(i);
                break;
            }
        }

        gui.display.add(new JLabel(tr("Tile Sources")), GBC.std());
        gui.display.add(GBC.glue(5, 0), GBC.std().fill(GBC.HORIZONTAL));
        gui.display.add(tileSourceCombo, GBC.eol().fill(GBC.HORIZONTAL));
    }

    /**
     * Someone pressed the "ok" button
     */
    public void ok()
    {
        Main.pref.put(SlippyMapPreferences.PREFERENCE_TILE_URL, tileSourceCombo
                .getSelectedItem().toString());
    }
}
