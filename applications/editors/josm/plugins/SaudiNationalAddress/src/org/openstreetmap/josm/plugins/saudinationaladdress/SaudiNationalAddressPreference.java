package org.openstreetmap.josm.plugins.saudinationaladdress;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.gui.widgets.JosmTextField;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.tr;

public class SaudiNationalAddressPreference extends DefaultTabPreferenceSetting {

    static final String API_KEY = "saudi-national-address.api.key";
    private final JTextField apiKEY = new JosmTextField(33);

    public SaudiNationalAddressPreference() {
        super("icon.png",
                tr("Saudi National Address"),
                tr("Please obtain an API key from here https://api.address.gov.sa/"));
    }


    @Override
    public void addGui(PreferenceTabbedPane preferenceTabbedPane) {
        final JPanel tab = preferenceTabbedPane.createPreferenceTab(this);

        apiKEY.setText(Config.getPref().get(API_KEY));
        tab.add(new JLabel(tr("API Key")), GBC.std());
        tab.add(apiKEY, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

    }

    @Override
    public boolean ok() {
        Config.getPref().put(API_KEY, apiKEY.getText());
        return false;
    }
}
