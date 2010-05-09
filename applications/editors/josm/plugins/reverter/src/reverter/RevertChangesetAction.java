package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public class RevertChangesetAction extends JosmAction {

    public RevertChangesetAction()
    {
        super(tr("Revert changeset"),null,tr("Revert changeset"),
                Shortcut.registerShortcut("tool:revert",
                        "Tool: Revert changeset",
                        KeyEvent.VK_T, Shortcut.GROUP_EDIT, 
                        Shortcut.SHIFT_DEFAULT),  
                true);
    }
//    private ObjectsHistoryDialog dlg = null;
    public void actionPerformed(ActionEvent arg0) {
        
        ChangesetIdQuery dlg = new ChangesetIdQuery();
        dlg.setVisible(true);
        System.out.println(tr("reverter: {0}",dlg.getValue()));
        if (dlg.getValue() != 1) return;
        final int changesetId = dlg.ChangesetId();
        if (changesetId == 0) return;
        Main.worker.submit(new PleaseWaitRunnable("Reverting...") {
            @Override
            protected void realRun() {
                try {
                    ChangesetReverter rev = new ChangesetReverter(changesetId);
                    rev.RevertChangeset(progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, true));
                    List<Command> cmds = rev.getCommands();
                    Command cmd = new SequenceCommand(tr("Revert changeset #{0}",changesetId),cmds);
                    Main.main.undoRedo.add(cmd);
                } catch (OsmTransferException e) {
                    e.printStackTrace();
                } finally {
                }
            }

            @Override
            protected void cancel() {
            }

            @Override
            protected void finish() {
            }
        });
    }

}