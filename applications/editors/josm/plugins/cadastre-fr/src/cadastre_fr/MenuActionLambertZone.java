// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.projection.Lambert;
import org.openstreetmap.josm.data.projection.LambertCC9Zones;
import org.openstreetmap.josm.tools.GBC;

public class MenuActionLambertZone extends JosmAction {

    private static final long serialVersionUID = 1L;

    public static String name = "Change Lambert zone";

    public static String[] lambert4zones = { "", "1 (51.30 to 48.15 degrees)", "2 (48.15 to 45.45 degrees)", "3 (45.45 to 42.76 degrees)", "4 (Corsica)" };
    public static String[] lambert9zones = { "", "1 (41 to 43 degrees)", "2 (42 to 44 degrees)",
            "3 (43 to 45 degrees)", "4 (44 to 46 degrees)", "5 (45 to 47 degrees)",
            "6 (46 to 48 degrees)", "7 (47 to 49 degrees)", "8 (48 to 50 degrees)",
            "9 (49 to 51 degrees)" };

    public MenuActionLambertZone() {
        super(tr(name), "cadastre_small", tr("Set manually the Lambert zone"),
                null, false);
    }

    public void actionPerformed(ActionEvent e) {
        JPanel p = new JPanel(new GridBagLayout());
        final JComboBox inputLambertZone;
        if (Main.proj instanceof LambertCC9Zones)
            inputLambertZone = new JComboBox(lambert9zones);
        else
            inputLambertZone = new JComboBox(lambert4zones);
        JLabel newLambertZone = new JLabel(tr("Zone"));
        p.add(newLambertZone, GBC.std());
        p.add(inputLambertZone, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 0));
        JOptionPane pane = new JOptionPane(p, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null) {
            private static final long serialVersionUID = 1L;

            @Override
            public void selectInitialValue() {
                inputLambertZone.setSelectedIndex(0);
            }
        };
        pane.createDialog(Main.parent, tr("Lambert zone")).setVisible(true);
        if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue()))
            return;
        if (inputLambertZone.getSelectedIndex() > 0) {
            Lambert.layoutZone = inputLambertZone.getSelectedIndex() - 1;
            LambertCC9Zones.layoutZone = Lambert.layoutZone; 
        }
    }
}
