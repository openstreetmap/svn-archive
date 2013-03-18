package iodb;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Download a list of imagery offsets for the current position, let user choose which one to use.
 * 
 * @author zverik
 */
public class OffsetInfoAction extends AbstractAction {
    private ImageryOffsetBase offset;
    
    public OffsetInfoAction( ImageryOffsetBase offset ) {
        super(tr("Offset Information"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete"));
        this.offset = offset;
        setEnabled(offset != null);
    }

    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(Main.parent, "TODO", ImageryOffsetTools.DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE);
    }
}
