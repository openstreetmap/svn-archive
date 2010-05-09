package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.GBC;

@SuppressWarnings("serial")
public class ObjectsHistoryDialog extends ExtendedDialog {
    public ObjectsHistoryDialog() {
        super(Main.parent, tr("Objects history"), new String[] {"Revert","Cancel"}, false);
        contentConstraints = GBC.eol().fill().insets(10,10,10,5);
        setButtonIcons(new String[] {"ok.png", "cancel.png" });
        setContent(new JPanel(new GridBagLayout()));
        setupDialog();        
    }
}
