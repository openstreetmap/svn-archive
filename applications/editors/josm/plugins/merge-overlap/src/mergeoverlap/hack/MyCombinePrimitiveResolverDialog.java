// License: GPL. For details, see LICENSE file.
package mergeoverlap.hack;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;
import org.openstreetmap.josm.gui.conflict.tags.RelationMemberConflictResolver;
import org.openstreetmap.josm.gui.util.GuiHelper;

/**
 * This dialog helps to resolve conflicts occurring when ways are combined or
 * nodes are merged.
 *
 * There is a singleton instance of this dialog which can be retrieved using
 * {@link #getInstance()}.
 *
 * The dialog uses two models: one  for resolving tag conflicts, the other
 * for resolving conflicts in relation memberships. For both models there are accessors,
 * i.e {@link #getTagConflictResolverModel()} and {@link #getRelationMemberConflictResolverModel()}.
 *
 * Models have to be <strong>populated</strong> before the dialog is launched. Example:
 * <pre>
 *    CombinePrimitiveResolverDialog dialog = CombinePrimitiveResolverDialog.getInstance();
 *    dialog.getTagConflictResolverModel().populate(aTagCollection);
 *    dialog.getRelationMemberConflictResolverModel().populate(aRelationLinkCollection);
 *    dialog.prepareDefaultDecisions();
 * </pre>
 *
 * You should also set the target primitive which other primitives (ways or nodes) are
 * merged to, see {@link #setTargetPrimitive(OsmPrimitive)}.
 *
 * After the dialog is closed use {@link #isCanceled()} to check whether the user canceled
 * the dialog. If it wasn't canceled you may build a collection of {@link Command} objects
 * which reflect the conflict resolution decisions the user made in the dialog:
 * see {@link #buildResolutionCommands()}
 */
public class MyCombinePrimitiveResolverDialog extends CombinePrimitiveResolverDialog {

    /** the unique instance of the dialog */
    private static MyCombinePrimitiveResolverDialog instance;

    /**
     * Replies the unique instance of the dialog
     *
     * @return the unique instance of the dialog
     */
    public static MyCombinePrimitiveResolverDialog getInstance() {
        if (instance == null) {
            GuiHelper.runInEDTAndWait(new Runnable() {
                @Override public void run() {
                    instance = new MyCombinePrimitiveResolverDialog(Main.parent);
                }
            });
        }
        return instance;
    }

    @Override
    protected JPanel buildRelationMemberConflictResolverPanel() {
        pnlRelationMemberConflictResolver = new RelationMemberConflictResolver(new MyRelationMemberConflictResolverModel());
        return pnlRelationMemberConflictResolver;
    }

    @Override
    protected ApplyAction buildApplyAction() {
        return new ApplyAction() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                super.propertyChange(evt);
                if (evt.getPropertyName().equals(MyRelationMemberConflictResolverModel.NUM_CONFLICTS_PROP)) {
                    updateEnabledState();
                }
            }
        };
    }

    /**
     * Constructs a new {@code MyCombinePrimitiveResolverDialog}.
     * @param parent The parent component in which this dialog will be displayed.
     */
    public MyCombinePrimitiveResolverDialog(Component parent) {
        super(parent);
    }

    /**
     * Replies the relation membership conflict resolver model.
     * @return The relation membership conflict resolver model.
     */
    @Override
    public MyRelationMemberConflictResolverModel getRelationMemberConflictResolverModel() {
        return (MyRelationMemberConflictResolverModel) pnlRelationMemberConflictResolver.getModel();
    }

    /**
     * Replies the list of {@link Command commands} needed to apply resolution choices.
     * @return The list of {@link Command commands} needed to apply resolution choices.
     */
    public List<Command> buildWayResolutionCommands() {
        List<Command> cmds = new LinkedList<>();

        TagCollection allResolutions = getTagConflictResolverModel().getAllResolutions();
        if (!allResolutions.isEmpty()) {
            cmds.addAll(buildTagChangeCommand(targetPrimitive, allResolutions));
        }
        if (targetPrimitive.get("created_by") != null) {
            cmds.add(new ChangePropertyCommand(targetPrimitive, "created_by", null));
        }

        Command cmd = pnlRelationMemberConflictResolver.buildTagApplyCommands(getRelationMemberConflictResolverModel()
                .getModifiedRelations(targetPrimitive));
        if (cmd != null) {
            cmds.add(cmd);
        }
        return cmds;
    }

    public void buildRelationCorrespondance(Map<Relation, Relation> newRelations, Map<Way, Way> oldWays) {
    	getRelationMemberConflictResolverModel().buildRelationCorrespondance(targetPrimitive, newRelations, oldWays);
    }
}
