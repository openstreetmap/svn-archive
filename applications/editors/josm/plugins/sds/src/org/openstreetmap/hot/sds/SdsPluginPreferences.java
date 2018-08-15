// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;

public class SdsPluginPreferences extends DefaultTabPreferenceSetting {

    static final String SDS_SERVER = "sds-server.url";
    static final String SDS_USERNAME = "sds-server.username";
    static final String SDS_PASSWORD = "sds-server.password";
    static final String SDS_PREFIX = "sds-server.tag-prefix";

    private final JTextField server = new JTextField(8);
    private final JTextField username = new JTextField(8);
    private final JPasswordField password = new JPasswordField(8);
    private final JTextField prefix = new JTextField(8);

    public SdsPluginPreferences() {
        super("sds", tr("Separate Data Store"), tr("Configures access to the Separate Data Store."));
    }

    @Override
    public void addGui(final PreferenceTabbedPane gui) {
        final JPanel tab = gui.createPreferenceTab(this);

        final JPanel access = new JPanel(new GridBagLayout());
        access.setBorder(BorderFactory.createTitledBorder(tr("Server")));

        server.setText(Config.getPref().get(SDS_SERVER, "http://datastore.hotosm.org"));
        username.setText(Config.getPref().get(SDS_USERNAME, ""));
        password.setText(Config.getPref().get(SDS_PASSWORD, ""));
        prefix.setText(Config.getPref().get(SDS_PREFIX, "hot:"));
        server.setToolTipText(tr("The URL under which the SDS server can be contacted."));
        username.setToolTipText(tr("The user name at the SDS server. You need to create an account with the SDS admin first."));
        password.setToolTipText(tr("The password at the SDS server. You need to create an account with the SDS admin first."));
        prefix.setToolTipText(tr("Tags beginning with this prefix are never saved to OSM, but to the SDS server only."));

        access.add(new JLabel(tr("SDS server URL")), GBC.std());
        access.add(server, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        access.add(new JLabel(tr("SDS username")), GBC.std());
        access.add(username, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        access.add(new JLabel(tr("SDS password")), GBC.std());
        access.add(password, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        JButton test = new JButton(tr("Test credentials now"));
        access.add(test, GBC.eol().anchor(GBC.EAST).insets(5, 0, 0, 5));

        tab.add(access, GBC.eol().fill(GBC.HORIZONTAL));

        tab.add(new JLabel(tr("SDS tag prefix")), GBC.std());
        tab.add(prefix, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        tab.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));

        test.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                SdsApi api = new SdsApi(server.getText());
                String olduser = Config.getPref().get(SDS_USERNAME);
                String oldpass = Config.getPref().get(SDS_PASSWORD);
                Config.getPref().put(SDS_USERNAME, username.getText());
                Config.getPref().put(SDS_PASSWORD, new String(password.getPassword()));
                try {
                    api.requestShadowsFromSds(Collections.singletonList(Long.valueOf(1L)), null, null, null);
                    JOptionPane.showMessageDialog(
                            MainApplication.getMainFrame(),
                            tr("Connection successful."),
                            tr("Success"),
                            JOptionPane.PLAIN_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            MainApplication.getMainFrame(),
                            tr("Cannot connect to SDS server: ") + ex.getMessage(),
                            tr("Error"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
                // restore old credentials even if successful; user might still
                // choose to press cancel!
                Config.getPref().put(SDS_USERNAME, olduser);
                Config.getPref().put(SDS_PASSWORD, oldpass);
            }
        });
    }

    @Override
    public boolean ok() {
        Config.getPref().put(SDS_SERVER, server.getText());
        Config.getPref().put(SDS_USERNAME, username.getText());
        Config.getPref().put(SDS_PASSWORD, new String(password.getPassword()));
        Config.getPref().put(SDS_PREFIX, prefix.getText());
        return false;
    }

}
