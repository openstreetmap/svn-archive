package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public class ObjectsHistoryAction extends JosmAction {

    public ObjectsHistoryAction() {
        super(tr("Objects history"),null,tr("History reverter"),
            Shortcut.registerShortcut("tool:history",
                "Tool: Display objects history dialog",
                KeyEvent.VK_H, Shortcut.ALT_CTRL_SHIFT),
                true);
        setEnabled(false);
    }
    public void actionPerformed(ActionEvent arg0) {
        new ObjectsHistoryDialog().setVisible(true);
    }
}
