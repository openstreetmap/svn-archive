package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public class RevertChangesetAction extends JosmAction {

    public RevertChangesetAction() {
        super(tr("Revert changeset"),null,tr("Revert changeset"),
                Shortcut.registerShortcut("tool:revert",
                        tr("Tool: {0}", tr("Revert changeset")),
                        KeyEvent.VK_T, Shortcut.GROUP_EDIT, 
                        Shortcut.SHIFT_DEFAULT),  
                true);
    }
    
    @Override
    protected void updateEnabledState() {
        setEnabled(getCurrentDataSet() != null);
    }

    public void actionPerformed(ActionEvent arg0)  {
        if (getCurrentDataSet() == null)
            return;
        ChangesetIdQuery dlg = new ChangesetIdQuery();
        dlg.setVisible(true);
        if (dlg.getValue() != 1) return;
        final int changesetId = dlg.ChangesetId();
        if (changesetId == 0) return;
        Main.worker.submit(new PleaseWaitRunnable(tr("Reverting...")) {
            private ChangesetReverter rev;
            private boolean downloadConfirmed = false;
            
            private boolean checkMissing() throws OsmTransferException {
                if (!rev.haveMissingObjects()) return true;
                if (!downloadConfirmed) {
                    downloadConfirmed = JOptionPane.showConfirmDialog(Main.parent,
                            tr("This changeset have objects that doesn't present in current dataset.\n" + 
                                    "It is needed to download them before reverting. Do you want to continue?"),
                            tr("Confirm"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                    if (!downloadConfirmed) return false;
                }
                final PleaseWaitProgressMonitor monitor = 
                    new PleaseWaitProgressMonitor(tr("Fetching missing primitives"));
                try {
                    rev.downloadMissingPrimitives(monitor);
                } finally {
                    monitor.close();
                }
                return true;
            }
            
            @Override
            protected void realRun() throws OsmTransferException {
                progressMonitor.indeterminateSubTask("Downloading changeset");
                rev = new ChangesetReverter(changesetId, NullProgressMonitor.INSTANCE);
                if (progressMonitor.isCancelled()) return;
                if (!checkMissing()) return;
                rev.downloadObjectsHistory(progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false));
                if (progressMonitor.isCancelled()) return;
                if (!checkMissing()) return;
                List<Command> cmds = rev.getCommands();
                Command cmd = new SequenceCommand(tr("Revert changeset #{0}",changesetId),cmds);
                Main.main.undoRedo.add(cmd);
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