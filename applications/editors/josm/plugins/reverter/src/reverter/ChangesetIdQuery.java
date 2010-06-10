package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

@SuppressWarnings("serial")
public class ChangesetIdQuery extends ExtendedDialog {
    private JFormattedTextField tcid = new JFormattedTextField(NumberFormat.getInstance());

    public int ChangesetId() {
        try {
          return NumberFormat.getInstance().parse(tcid.getText()).intValue();
        } catch (ParseException e) {            
          return 0;
        }
    }
    public ChangesetIdQuery() {
        super(Main.parent, tr("Objects history"), new String[] {tr("Revert"),tr("Cancel")}, true);
        contentConstraints = GBC.eol().fill().insets(10,10,10,5);
        setButtonIcons(new String[] {"ok.png", "cancel.png" });
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(tr("Changeset id:")));
        panel.add(tcid, GBC.eol().fill(GBC.HORIZONTAL));
        setContent(panel);
        setupDialog();        
    }
}
