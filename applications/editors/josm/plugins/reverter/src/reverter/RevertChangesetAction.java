package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.Shortcut;

import reverter.ChangesetReverter.RevertType;

@SuppressWarnings("serial")
public class RevertChangesetAction extends JosmAction {

    public RevertChangesetAction() {
        super(tr("Revert changeset"),"revert-changeset",tr("Revert changeset"),
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
        final ChangesetIdQuery dlg = new ChangesetIdQuery();
        dlg.setVisible(true);
        if (dlg.getValue() != 1) return;
        final int changesetId = dlg.getChangesetId();
        final RevertType revertType = dlg.getRevertType();
        if (changesetId == 0) return;
        if (revertType == null) return;

        Main.worker.submit(new PleaseWaitRunnable(tr("Reverting...")) {
            private ChangesetReverter rev;
            private boolean downloadConfirmed = false;

            private boolean checkAndDownloadMissing() throws OsmTransferException {
                if (!rev.hasMissingObjects()) return true;
                if (!downloadConfirmed) {
                    downloadConfirmed = JOptionPane.showConfirmDialog(Main.parent,
                            tr("This changeset has objects that are not present in current dataset.\n" +
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
                return !monitor.isCanceled();
            }

            @Override
            protected void realRun() throws OsmTransferException {
                progressMonitor.indeterminateSubTask(tr("Downloading changeset"));
                rev = new ChangesetReverter(changesetId, revertType,
                        progressMonitor.createSubTaskMonitor(0, true));
                if (progressMonitor.isCanceled()) return;

                // Check missing objects
                rev.checkMissingCreated();
                rev.checkMissingUpdated();
                if (rev.hasMissingObjects()) {
                    // If missing created or updated objects, ask user
                    rev.checkMissingDeleted();
                    if (!checkAndDownloadMissing()) return;
                } else {
                    // Don't ask user to download primitives going to be undeleted
                    rev.checkMissingDeleted();
                    rev.downloadMissingPrimitives(progressMonitor.createSubTaskMonitor(0, false));
                }

                if (progressMonitor.isCanceled()) return;
                rev.downloadObjectsHistory(progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false));
                if (progressMonitor.isCanceled()) return;
                if (!checkAndDownloadMissing()) return;
                List<Command> cmds = rev.getCommands();
                Command cmd = new RevertChangesetCommand(tr(revertType == RevertType.FULL ? "Revert changeset #{0}" :
                        "Partially revert changeset #{0}",changesetId),cmds);
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