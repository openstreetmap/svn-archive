package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

import reverter.ChangesetReverter.RevertType;

@SuppressWarnings("serial")
public class RevertChangesetAction extends JosmAction {

    public RevertChangesetAction() {
        super(tr("Revert changeset"),"revert-changeset",tr("Revert changeset"),
            Shortcut.registerShortcut("tool:revert",
                tr("Tool: {0}", tr("Revert changeset")),
                KeyEvent.VK_T, Shortcut.CTRL_SHIFT),
                true);
    }

    public void actionPerformed(ActionEvent arg0)  {
        final ChangesetIdQuery dlg = new ChangesetIdQuery();
        if (dlg.showDialog().getValue() != 1) return;
        final int changesetId = dlg.getChangesetId();
        final RevertType revertType = dlg.getRevertType();
        if (changesetId == 0) return;
        if (revertType == null) return;

        boolean newLayer = dlg.isNewLayerRequired();
        Main.worker.submit(new RevertChangesetTask(changesetId, revertType, newLayer, newLayer));
    }
}
