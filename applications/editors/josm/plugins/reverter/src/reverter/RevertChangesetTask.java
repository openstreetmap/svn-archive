// License: GPL. For details, see LICENSE file.
package reverter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.command.conflict.ConflictAddCommand;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.progress.swing.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.UserCancelException;

import reverter.ChangesetReverter.RevertType;

public class RevertChangesetTask extends PleaseWaitRunnable {
    private final Collection<Integer> changesetIds;
    private final RevertType revertType;
    private boolean newLayer;

    private ChangesetReverter rev;
    private boolean downloadConfirmed;
    private int numberOfConflicts;

    public RevertChangesetTask(int changesetId, RevertType revertType) {
        this(changesetId, revertType, false);
    }

    public RevertChangesetTask(int changesetId, RevertType revertType, boolean autoConfirmDownload) {
        this(changesetId, revertType, autoConfirmDownload, false);
    }

    public RevertChangesetTask(int changesetId, RevertType revertType, boolean autoConfirmDownload, boolean newLayer) {
        this(Collections.singleton(changesetId), revertType, autoConfirmDownload, newLayer);
    }

    public RevertChangesetTask(Collection<Integer> changesetIds, RevertType revertType, boolean autoConfirmDownload, boolean newLayer) {
        super(tr("Reverting..."));
        this.changesetIds = new ArrayList<>(changesetIds);
        this.revertType = revertType;
        this.downloadConfirmed = autoConfirmDownload;
        this.newLayer = newLayer;
    }

    private boolean checkAndDownloadMissing() throws OsmTransferException {
        if (!rev.hasMissingObjects()) return true;
        if (!downloadConfirmed) {
            final Integer selectedOption = GuiHelper.runInEDTAndWaitAndReturn(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return JOptionPane.showConfirmDialog(Main.parent,
                            tr("This changeset has objects that are not present in current dataset.\n" +
                                    "It is needed to download them before reverting. Do you want to continue?"),
                            tr("Confirm"), JOptionPane.YES_NO_OPTION);
                }
            });
            downloadConfirmed = selectedOption != null && selectedOption == JOptionPane.YES_OPTION;
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
        numberOfConflicts = 0;
        final List<Command> allcmds = new ArrayList<>();
        Logging.info("Reverting {0} changeset(s): {1}",
                changesetIds.size(), changesetIds.stream().map(Long::toString).collect(Collectors.toList()));
        for (int changesetId : changesetIds) {
            try {
                Logging.info("Reverting changeset {0}", Long.toString(changesetId));
                RevertChangesetCommand cmd = revertChangeset(changesetId);
                if (cmd != null) {
                    allcmds.add(cmd);
                }
                Logging.info("Reverted changeset {0}", Long.toString(changesetId));
                newLayer = false; // reuse layer for subsequent reverts
            } catch (OsmTransferException e) {
                Logging.error(e);
                throw e;
            } catch (UserCancelException e) {
                Logging.warn("Revert canceled");
                Logging.trace(e);
                return;
            }
        }
        if (!allcmds.isEmpty()) {
            Command cmd = allcmds.size() == 1 ? allcmds.get(0) : new SequenceCommand(tr("Revert changeset"), allcmds);
            GuiHelper.runInEDT(() -> {
                MainApplication.undoRedo.add(cmd);
                if (numberOfConflicts > 0) {
                    MainApplication.getMap().conflictDialog.warnNumNewConflicts(numberOfConflicts);
                }
            });
        }
    }

    private RevertChangesetCommand revertChangeset(int changesetId) throws OsmTransferException, UserCancelException {
        progressMonitor.indeterminateSubTask(tr("Downloading changeset"));
        try {
            rev = new ChangesetReverter(changesetId, revertType, newLayer, progressMonitor.createSubTaskMonitor(0, true));
        } catch (final RevertRedactedChangesetException e) {
            GuiHelper.runInEDT(() -> new Notification(
                    e.getMessage()+"<br>"+
                    tr("See {0}", "<a href=\"https://www.openstreetmap.org/redactions\">https://www.openstreetmap.org/redactions</a>"))
            .setIcon(JOptionPane.ERROR_MESSAGE)
            .setDuration(Notification.TIME_LONG)
            .show());
            progressMonitor.cancel();
        }
        if (progressMonitor.isCanceled())
            throw new UserCancelException();

        // Check missing objects
        rev.checkMissingCreated();
        rev.checkMissingUpdated();
        if (rev.hasMissingObjects()) {
            // If missing created or updated objects, ask user
            rev.checkMissingDeleted();
            if (!checkAndDownloadMissing())
                throw new UserCancelException();
        } else {
            // Don't ask user to download primitives going to be undeleted
            rev.checkMissingDeleted();
            rev.downloadMissingPrimitives(progressMonitor.createSubTaskMonitor(0, false));
        }

        if (progressMonitor.isCanceled())
            throw new UserCancelException();
        rev.downloadObjectsHistory(progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false));
        if (progressMonitor.isCanceled())
            throw new UserCancelException();
        if (!checkAndDownloadMissing())
            throw new UserCancelException();
        rev.fixNodesWithoutCoordinates(progressMonitor);
        List<Command> cmds = rev.getCommands();
        if (cmds.isEmpty()) {
            Logging.warn(MessageFormat.format("No revert commands found for changeset {0}", Long.toString(changesetId)));
            return null;
        }
        for (Command c : cmds) {
            if (c instanceof ConflictAddCommand) {
                numberOfConflicts++;
            }
        }
        return new RevertChangesetCommand(tr(revertType == RevertType.FULL ? "Revert changeset #{0}" :
                "Partially revert changeset #{0}", changesetId), cmds);
    }

    @Override
    protected void cancel() {
    }

    @Override
    protected void finish() {
    }

    public final int getNumberOfConflicts() {
        return numberOfConflicts;
    }
}
