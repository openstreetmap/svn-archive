package nanolog;

import javax.swing.*;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * NanoLog Panel. Displays the selected log item, along with surrounding 30-50 lines.
 * 
 * @author zverik
 */
public class NanoLogPanel extends ToggleDialog {
    private JList logPanel;
    private LogListModel listModel;
    
    public NanoLogPanel() {
        super(tr("NanoLog"), "nanolog", tr("Open NanoLog panel"), null, 150, true);
        
        listModel = new LogListModel();
        logPanel = new JList(listModel);
        createLayout(logPanel, true, null);
    }
    
    private class LogListModel extends AbstractListModel {

        public int getSize() {
            return 0;
        }

        public String getElementAt( int index ) {
            return ""; // todo
        }
    }
}
