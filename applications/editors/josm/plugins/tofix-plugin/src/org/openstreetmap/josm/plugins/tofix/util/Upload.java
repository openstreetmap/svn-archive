package org.openstreetmap.josm.plugins.tofix.util;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.upload.ApiPreconditionCheckerHook;
import org.openstreetmap.josm.actions.upload.DiscardTagsHook;
import org.openstreetmap.josm.actions.upload.FixDataHook;
import org.openstreetmap.josm.actions.upload.RelationUploadOrderHook;
import org.openstreetmap.josm.actions.upload.UploadHook;
import org.openstreetmap.josm.actions.upload.ValidateUploadHook;
import org.openstreetmap.josm.data.APIDataSet;
import org.openstreetmap.josm.data.conflict.ConflictCollection;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.help.HelpUtil;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import org.openstreetmap.josm.gui.io.UploadDialog;
import org.openstreetmap.josm.gui.io.UploadPrimitivesTask;
import org.openstreetmap.josm.gui.layer.AbstractModifiableLayer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.util.GuiHelper;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Customize from UploadAction
 */
public class Upload extends JosmAction {

    private static final List<UploadHook> uploadHooks = new LinkedList<>();
    private static final List<UploadHook> lateUploadHooks = new LinkedList<>();

    static {

        uploadHooks.add(new ValidateUploadHook());

        /**
         * Fixes database errors
         */
        uploadHooks.add(new FixDataHook());

        /**
         * Checks server capabilities before upload.
         */
        uploadHooks.add(new ApiPreconditionCheckerHook());

        /**
         * Adjusts the upload order of new relations
         */
        uploadHooks.add(new RelationUploadOrderHook());

        /**
         * Removes discardable tags like created_by on modified objects
         */
        lateUploadHooks.add(new DiscardTagsHook());
    }

    /**
     * Registers an upload hook. Adds the hook at the first position of the
     * upload hooks.
     *
     * @param hook the upload hook. Ignored if null.
     */
    public static void registerUploadHook(UploadHook hook) {
        registerUploadHook(hook, false);
    }

    public static void registerUploadHook(UploadHook hook, boolean late) {
        if (hook == null) {
            return;
        }
        if (late) {
            if (!lateUploadHooks.contains(hook)) {
                lateUploadHooks.add(0, hook);
            }
        } else {
            if (!uploadHooks.contains(hook)) {
                uploadHooks.add(0, hook);
            }
        }
    }

    /**
     * Unregisters an upload hook. Removes the hook from the list of upload
     * hooks.
     *
     * @param hook the upload hook. Ignored if null.
     */
    public static void unregisterUploadHook(UploadHook hook) {
        if (hook == null) {
            return;
        }
        if (uploadHooks.contains(hook)) {
            uploadHooks.remove(hook);
        }
        if (lateUploadHooks.contains(hook)) {
            lateUploadHooks.remove(hook);
        }
    }

    private String customized_comment;

    public Upload() {
        super(tr("Upload data"), "upload", tr("Upload all changes in the active data layer to the OSM server"),
                Shortcut.registerShortcut("file:upload", tr("File: {0}", tr("Upload data")), KeyEvent.VK_UP, Shortcut.CTRL_SHIFT), true);
        putValue("help", ht("/Action/Upload"));
    }

    /**
     * Refreshes the enabled state
     *
     */
    @Override
    protected void updateEnabledState() {
        setEnabled(getEditLayer() != null);
    }

    public static boolean checkPreUploadConditions(AbstractModifiableLayer layer) {
        return checkPreUploadConditions(layer,
                layer instanceof OsmDataLayer ? new APIDataSet(((OsmDataLayer) layer).data) : null);
    }

    protected static void alertUnresolvedConflicts(OsmDataLayer layer) {
        HelpAwareOptionPane.showOptionDialog(
                Main.parent,
                tr("<html>The data to be uploaded participates in unresolved conflicts of layer ''{0}''.<br>"
                        + "You have to resolve them first.</html>", layer.getName()
                ),
                tr("Warning"),
                JOptionPane.WARNING_MESSAGE,
                HelpUtil.ht("/Action/Upload#PrimitivesParticipateInConflicts")
        );
    }

    /**
     * returns true if the user wants to cancel, false if they want to continue
     */
    public static boolean warnUploadDiscouraged(AbstractModifiableLayer layer) {
        return GuiHelper.warnUser(tr("Upload discouraged"),
                "<html>"
                + tr("You are about to upload data from the layer ''{0}''.<br /><br />"
                        + "Sending data from this layer is <b>strongly discouraged</b>. If you continue,<br />"
                        + "it may require you subsequently have to revert your changes, or force other contributors to.<br /><br />"
                        + "Are you sure you want to continue?", layer.getName())
                + "</html>",
                ImageProvider.get("upload"), tr("Ignore this hint and upload anyway"));
    }

    public static boolean checkPreUploadConditions(AbstractModifiableLayer layer, APIDataSet apiData) {
        if (layer.isUploadDiscouraged()) {
            if (warnUploadDiscouraged(layer)) {
                return false;
            }
        }
        if (layer instanceof OsmDataLayer) {
            OsmDataLayer osmLayer = (OsmDataLayer) layer;
            ConflictCollection conflicts = osmLayer.getConflicts();
            if (apiData.participatesInConflict(conflicts)) {
                alertUnresolvedConflicts(osmLayer);
                return false;
            }
        }
        // Call all upload hooks in sequence.
        // FIXME: this should become an asynchronous task
        //
        if (apiData != null) {
            for (UploadHook hook : uploadHooks) {
                if (!hook.checkUpload(apiData)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Uploads data to the OSM API.
     *
     * @param layer the source layer for the data to upload
     * @param apiData the primitives to be added, updated, or deleted
     */
    public void uploadData(final OsmDataLayer layer, APIDataSet apiData) {
        if (apiData.isEmpty()) {
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("No changes to upload."),
                    tr("Warning"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        if (!checkPreUploadConditions(layer, apiData)) {
            return;
        }

        final UploadDialog dialog = UploadDialog.getUploadDialog();
        // If we simply set the changeset comment here, it would be
        // overridden by subsequent events in EDT that are caused by
        // dialog creation. The current solution is to queue this operation
        // after these events.
        // TODO: find better way to initialize the comment field
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final Map<String, String> tags = new HashMap<>(layer.data.getChangeSetTags());
                if (!tags.containsKey("source")) {
                    tags.put("source", dialog.getLastChangesetSourceFromHistory());
                }
                if (!tags.containsKey("comment")) {
                    String comment = dialog.getLastChangesetCommentFromHistory();
                    if (!comment.contains(getCustomized_comment())) {
                        comment = getCustomized_comment();
                    }
                    tags.put("comment", comment);
                }

                dialog.setDefaultChangesetTags(tags);
            }
        });
        dialog.setUploadedPrimitives(apiData);
        dialog.setVisible(true);
        if (dialog.isCanceled()) {
            return;
        }
        dialog.rememberUserInput();

        for (UploadHook hook : lateUploadHooks) {
            if (!hook.checkUpload(apiData)) {
                return;
            }
        }

        Main.worker.execute(
                new UploadPrimitivesTask(
                        UploadDialog.getUploadDialog().getUploadStrategySpecification(),
                        layer,
                        apiData,
                        UploadDialog.getUploadDialog().getChangeset()
                )
        );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) {
            return;
        }
        if (Main.map == null) {
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("Nothing to upload. Get some data first."),
                    tr("Warning"),
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        APIDataSet apiData = new APIDataSet(Main.main.getCurrentDataSet());
        uploadData(Main.main.getEditLayer(), apiData);
    }

    public String getCustomized_comment() {
        return customized_comment;
    }

    public void setCustomized_comment(String customized_comment) {
        this.customized_comment = customized_comment;
    }

}
