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
import org.openstreetmap.josm.tools.GBC;

public class MenuActionLambertZone extends JosmAction {

    private static final long serialVersionUID = 1L;

    public static String name = "Change Lambert zone";

    public MenuActionLambertZone() {
        super(tr(name), "cadastre_small", tr("Set manually the Lambert zone (e.g. for locations between two zones)"),
                null, false);
    }

    public void actionPerformed(ActionEvent e) {
        JPanel p = new JPanel(new GridBagLayout());
        String[] zones = { "", "1 (51.30° to 48.15°)", "2 (48.15° to 45.45°)", "3 (45.45° to 42.76°)", "4 (Corsica)" };
        final JComboBox inputLambertZone = new JComboBox(zones);
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
        }
    }
}
