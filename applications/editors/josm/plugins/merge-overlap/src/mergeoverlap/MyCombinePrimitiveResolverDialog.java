// License: GPL. For details, see LICENSE file.
package mergeoverlap;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.RelationToChildReference;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.gui.JMultilineLabel;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.conflict.tags.MultiValueCellEditor;
import org.openstreetmap.josm.gui.conflict.tags.MultiValueDecisionType;
import org.openstreetmap.josm.gui.conflict.tags.MultiValueResolutionDecision;
import org.openstreetmap.josm.gui.conflict.tags.RelationMemberConflictDecision;
import org.openstreetmap.josm.gui.conflict.tags.RelationMemberConflictDecisionType;
import org.openstreetmap.josm.gui.conflict.tags.RelationMemberConflictResolverColumnModel;
import org.openstreetmap.josm.gui.conflict.tags.TagConflictResolverColumnModel;
import org.openstreetmap.josm.gui.help.ContextSensitiveHelpAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingTextField;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionList;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.WindowGeometry;

/**
 * This dialog helps to resolve conflicts occurring when ways are combined or
 * nodes are merged.
 *
 * There is a singleton instance of this dialog which can be retrieved using
 * {@see #getInstance()}.
 *
 * The dialog uses two models: one  for resolving tag conflicts, the other
 * for resolving conflicts in relation memberships. For both models there are accessors,
 * i.e {@see #getTagConflictResolverModel()} and {@see #getRelationMemberConflictResolverModel()}.
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
 * merged to, see {@see #setTargetPrimitive(OsmPrimitive)}.
 *
 * After the dialog is closed use {@see #isCancelled()} to check whether the user canceled
 * the dialog. If it wasn't canceled you may build a collection of {@see Command} objects
 * which reflect the conflict resolution decisions the user made in the dialog:
 * see {@see #buildResolutionCommands()}
 *
 *
 */
public class MyCombinePrimitiveResolverDialog extends JDialog {

    /** the unique instance of the dialog */
    static private MyCombinePrimitiveResolverDialog instance;

    /**
     * Replies the unique instance of the dialog
     *
     * @return the unique instance of the dialog
     */
    public static MyCombinePrimitiveResolverDialog getInstance() {
        if (instance == null) {
            instance = new MyCombinePrimitiveResolverDialog(Main.parent);
        }
        return instance;
    }

    private AutoAdjustingSplitPane spTagConflictTypes;
    private MyTagConflictResolver pnlTagConflictResolver;
    private MyRelationMemberConflictResolver pnlRelationMemberConflictResolver;
    private boolean cancelled;
    private JPanel pnlButtons;
    private OsmPrimitive targetPrimitive;

    /** the private help action */
    private ContextSensitiveHelpAction helpAction;
    /** the apply button */
    private SideButton btnApply;

    /**
     * Replies the target primitive the collection of primitives is merged
     * or combined to.
     *
     * @return the target primitive
     */
    public OsmPrimitive getTargetPrimitmive() {
        return targetPrimitive;
    }

    /**
     * Sets the primitive the collection of primitives is merged or combined
     * to.
     *
     * @param primitive the target primitive
     */
    public void setTargetPrimitive(OsmPrimitive primitive) {
        this.targetPrimitive = primitive;
        updateTitle();
        if (primitive instanceof Way) {
            pnlRelationMemberConflictResolver.initForWayCombining();
        } else if (primitive instanceof Node) {
            pnlRelationMemberConflictResolver.initForNodeMerging();
        }
    }

    protected void updateTitle() {
        if (targetPrimitive == null) {
            setTitle(tr("Conflicts when combining primitives"));
            return;
        }
        if (targetPrimitive instanceof Way) {
            setTitle(tr("Conflicts when combining ways - combined way is ''{0}''", targetPrimitive
                    .getDisplayName(DefaultNameFormatter.getInstance())));
            helpAction.setHelpTopic(ht("/Action/CombineWay#ResolvingConflicts"));
            getRootPane().putClientProperty("help", ht("/Action/CombineWay#ResolvingConflicts"));
        } else if (targetPrimitive instanceof Node) {
            setTitle(tr("Conflicts when merging nodes - target node is ''{0}''", targetPrimitive
                    .getDisplayName(DefaultNameFormatter.getInstance())));
            helpAction.setHelpTopic(ht("/Action/MergeNodes#ResolvingConflicts"));
            getRootPane().putClientProperty("help", ht("/Action/MergeNodes#ResolvingConflicts"));
        }
    }

    protected void build() {
        getContentPane().setLayout(new BorderLayout());
        updateTitle();
        spTagConflictTypes = new AutoAdjustingSplitPane(JSplitPane.VERTICAL_SPLIT);
        spTagConflictTypes.setTopComponent(buildTagConflictResolverPanel());
        spTagConflictTypes.setBottomComponent(buildRelationMemberConflictResolverPanel());
        getContentPane().add(pnlButtons = buildButtonPanel(), BorderLayout.SOUTH);
        addWindowListener(new AdjustDividerLocationAction());
        HelpUtil.setHelpContext(getRootPane(), ht("/"));
    }

    protected JPanel buildTagConflictResolverPanel() {
        pnlTagConflictResolver = new MyTagConflictResolver();
        return pnlTagConflictResolver;
    }

    protected JPanel buildRelationMemberConflictResolverPanel() {
        pnlRelationMemberConflictResolver = new MyRelationMemberConflictResolver();
        return pnlRelationMemberConflictResolver;
    }

    protected JPanel buildButtonPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new FlowLayout(FlowLayout.CENTER));

        // -- apply button
        ApplyAction applyAction = new ApplyAction();
        pnlTagConflictResolver.getModel().addPropertyChangeListener(applyAction);
        pnlRelationMemberConflictResolver.getModel().addPropertyChangeListener(applyAction);
        btnApply = new SideButton(applyAction);
        btnApply.setFocusable(true);
        pnl.add(btnApply);

        // -- cancel button
        CancelAction cancelAction = new CancelAction();
        pnl.add(new SideButton(cancelAction));

        // -- help button
        helpAction = new ContextSensitiveHelpAction();
        pnl.add(new SideButton(helpAction));

        return pnl;
    }

    public MyCombinePrimitiveResolverDialog(Component owner) {
        super(JOptionPane.getFrameForComponent(owner), ModalityType.DOCUMENT_MODAL);
        build();
    }

    public MyTagConflictResolverModel getTagConflictResolverModel() {
        return pnlTagConflictResolver.getModel();
    }

    public MyRelationMemberConflictResolverModel getRelationMemberConflictResolverModel() {
        return pnlRelationMemberConflictResolver.getModel();
    }

    protected List<Command> buildTagChangeCommand(OsmPrimitive primitive, TagCollection tc) {
        LinkedList<Command> cmds = new LinkedList<Command>();
        for (String key : tc.getKeys()) {
            if (tc.hasUniqueEmptyValue(key)) {
                if (primitive.get(key) != null) {
                    cmds.add(new ChangePropertyCommand(primitive, key, null));
                }
            } else {
                String value = tc.getJoinedValues(key);
                if (!value.equals(primitive.get(key))) {
                    cmds.add(new ChangePropertyCommand(primitive, key, value));
                }
            }
        }
        return cmds;
    }

    public List<Command> buildWayResolutionCommands() {
        List<Command> cmds = new LinkedList<Command>();

        TagCollection allResolutions = getTagConflictResolverModel().getAllResolutions();
        if (allResolutions.size() > 0) {
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
    
    protected void prepareDefaultTagDecisions() {
        MyTagConflictResolverModel model = getTagConflictResolverModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            MultiValueResolutionDecision decision = model.getDecision(i);
            List<String> values = decision.getValues();
            values.remove("");
            if (values.size() == 1) {
                decision.keepOne(values.get(0));
            } else {
                decision.keepAll();
            }
        }
        model.rebuild();
    }

    protected void prepareDefaultRelationDecisions() {
        MyRelationMemberConflictResolverModel model = getRelationMemberConflictResolverModel();
        Set<Relation> relations = new HashSet<Relation>();
        for (int i = 0; i < model.getNumDecisions(); i++) {
            RelationMemberConflictDecision decision = model.getDecision(i);
            if (!relations.contains(decision.getRelation())) {
                decision.decide(RelationMemberConflictDecisionType.KEEP);
                relations.add(decision.getRelation());
            } else {
                decision.decide(RelationMemberConflictDecisionType.REMOVE);
            }
        }
        model.refresh();
    }

    public void prepareDefaultDecisions() {
        prepareDefaultTagDecisions();
        prepareDefaultRelationDecisions();
    }

    protected JPanel buildEmptyConflictsPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout());
        pnl.add(new JLabel(tr("No conflicts to resolve")));
        return pnl;
    }

    protected void prepareGUIBeforeConflictResolutionStarts() {
        MyRelationMemberConflictResolverModel relModel = getRelationMemberConflictResolverModel();
        MyTagConflictResolverModel tagModel = getTagConflictResolverModel();
        getContentPane().removeAll();

        if (relModel.getNumDecisions() > 0 && tagModel.getNumDecisions() > 0) {
            // display both, the dialog for resolving relation conflicts and for resolving
            // tag conflicts
            spTagConflictTypes.setTopComponent(pnlTagConflictResolver);
            spTagConflictTypes.setBottomComponent(pnlRelationMemberConflictResolver);
            getContentPane().add(spTagConflictTypes, BorderLayout.CENTER);
        } else if (relModel.getNumDecisions() > 0) {
            // relation conflicts only
            //
            getContentPane().add(pnlRelationMemberConflictResolver, BorderLayout.CENTER);
        } else if (tagModel.getNumDecisions() > 0) {
            // tag conflicts only
            //
            getContentPane().add(pnlTagConflictResolver, BorderLayout.CENTER);
        } else {
            getContentPane().add(buildEmptyConflictsPanel(), BorderLayout.CENTER);
        }

        getContentPane().add(pnlButtons, BorderLayout.SOUTH);
        validate();
        int numTagDecisions = getTagConflictResolverModel().getNumDecisions();
        int numRelationDecisions = getRelationMemberConflictResolverModel().getNumDecisions();
        if (numTagDecisions > 0 && numRelationDecisions > 0) {
            spTagConflictTypes.setDividerLocation(0.5);
        }
        pnlRelationMemberConflictResolver.prepareForEditing();
    }

    protected void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            prepareGUIBeforeConflictResolutionStarts();
            new WindowGeometry(getClass().getName() + ".geometry", WindowGeometry.centerInWindow(Main.parent,
                    new Dimension(600, 400))).applySafe(this);
            setCancelled(false);
            btnApply.requestFocusInWindow();
        } else {
            new WindowGeometry(this).remember(getClass().getName() + ".geometry");
        }
        super.setVisible(visible);
    }

    class CancelAction extends AbstractAction {

        public CancelAction() {
            putValue(Action.SHORT_DESCRIPTION, tr("Cancel conflict resolution"));
            putValue(Action.NAME, tr("Cancel"));
            putValue(Action.SMALL_ICON, ImageProvider.get("", "cancel"));
            setEnabled(true);
        }

        public void actionPerformed(ActionEvent arg0) {
            setCancelled(true);
            setVisible(false);
        }
    }

    class ApplyAction extends AbstractAction implements PropertyChangeListener {

        public ApplyAction() {
            putValue(Action.SHORT_DESCRIPTION, tr("Apply resolved conflicts"));
            putValue(Action.NAME, tr("Apply"));
            putValue(Action.SMALL_ICON, ImageProvider.get("ok"));
            updateEnabledState();
        }

        public void actionPerformed(ActionEvent arg0) {
            setVisible(false);
            pnlTagConflictResolver.rememberPreferences();
        }

        protected void updateEnabledState() {
            setEnabled(pnlTagConflictResolver.getModel().getNumConflicts() == 0
                    && pnlRelationMemberConflictResolver.getModel().getNumConflicts() == 0);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(MyTagConflictResolverModel.NUM_CONFLICTS_PROP)) {
                updateEnabledState();
            }
            if (evt.getPropertyName().equals(MyRelationMemberConflictResolverModel.NUM_CONFLICTS_PROP)) {
                updateEnabledState();
            }
        }
    }

    class AdjustDividerLocationAction extends WindowAdapter {
        @Override
        public void windowOpened(WindowEvent e) {
            int numTagDecisions = getTagConflictResolverModel().getNumDecisions();
            int numRelationDecisions = getRelationMemberConflictResolverModel().getNumDecisions();
            if (numTagDecisions > 0 && numRelationDecisions > 0) {
                spTagConflictTypes.setDividerLocation(0.5);
            }
        }
    }

    static class AutoAdjustingSplitPane extends JSplitPane implements PropertyChangeListener, HierarchyBoundsListener {
        private double dividerLocation;

        public AutoAdjustingSplitPane(int newOrientation) {
            super(newOrientation);
            addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, this);
            addHierarchyBoundsListener(this);
        }

        public void ancestorResized(HierarchyEvent e) {
            setDividerLocation((int) (dividerLocation * getHeight()));
        }

        public void ancestorMoved(HierarchyEvent e) {
            // do nothing
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
                int newVal = (Integer) evt.getNewValue();
                if (getHeight() != 0) {
                    dividerLocation = (double) newVal / (double) getHeight();
                }
            }
        }
    }

    /**
     * This model manages a list of conflicting relation members.
     *
     * It can be used as {@see TableModel}.
     *
     *
     */
    public static class MyRelationMemberConflictResolverModel extends DefaultTableModel {
        /** the property name for the number conflicts managed by this model */
        static public final String NUM_CONFLICTS_PROP = MyRelationMemberConflictResolverModel.class.getName() + ".numConflicts";

        /** the list of conflict decisions */
        private List<RelationMemberConflictDecision> decisions;
        /** the collection of relations for which we manage conflicts */
        private Collection<Relation> relations;
        /** the number of conflicts */
        private int numConflicts;
        private PropertyChangeSupport support;

        /**
         * Replies the current number of conflicts
         *
         * @return the current number of conflicts
         */
        public int getNumConflicts() {
            return numConflicts;
        }

        /**
         * Updates the current number of conflicts from list of decisions and emits
         * a property change event if necessary.
         *
         */
        protected void updateNumConflicts() {
            int count = 0;
            for (RelationMemberConflictDecision decision: decisions) {
                if (!decision.isDecided()) {
                    count++;
                }
            }
            int oldValue = numConflicts;
            numConflicts = count;
            if (numConflicts != oldValue) {
                support.firePropertyChange(NUM_CONFLICTS_PROP, oldValue, numConflicts);
            }
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            support.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            support.removePropertyChangeListener(l);
        }

        public MyRelationMemberConflictResolverModel() {
            decisions = new ArrayList<RelationMemberConflictDecision>();
            support = new PropertyChangeSupport(this);
        }

        @Override
        public int getRowCount() {
            if (decisions == null) return 0;
            return decisions.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (decisions == null) return null;

            RelationMemberConflictDecision d = decisions.get(row);
            switch(column) {
            case 0: /* relation */ return d.getRelation();
            case 1: /* pos */ return Integer.toString(d.getPos() + 1); // position in "user space" starting at 1
            case 2: /* role */ return d.getRole();
            case 3: /* original */ return d.getOriginalPrimitive();
            case 4: /* decision */ return d.getDecision();
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            RelationMemberConflictDecision d = decisions.get(row);
            switch(column) {
            case 2: /* role */
                d.setRole((String)value);
                break;
            case 4: /* decision */
                d.decide((RelationMemberConflictDecisionType)value);
                refresh();
                break;
            }
            fireTableDataChanged();
        }

        /**
         * Populates the model with the members of the relation <code>relation</code>
         * referring to <code>primitive</code>.
         *
         * @param relation the parent relation
         * @param primitive the child primitive
         */
        protected void populate(Relation relation, OsmPrimitive primitive, Map<Way, Way> oldWays) {
            for (int i = 0; i<relation.getMembersCount(); i++) {
                if (MergeOverlapAction.getOld(relation.getMember(i).getWay(), oldWays) == MergeOverlapAction.getOld((Way)primitive, oldWays)) {
                    decisions.add(new RelationMemberConflictDecision(relation, i));
                }
            }
        }

        /**
         * Populates the model with the relation members belonging to one of the relations in <code>relations</code>
         * and referring to one of the primitives in <code>memberPrimitives</code>.
         *
         * @param relations  the parent relations. Empty list assumed if null.
         * @param memberPrimitives the child primitives. Empty list assumed if null.
         */
        public void populate(Collection<Relation> relations, Collection<? extends OsmPrimitive> memberPrimitives, Map<Way, Way> oldWays) {
            decisions.clear();
        	
            relations = relations == null ? new LinkedList<Relation>() : relations;
            memberPrimitives = memberPrimitives == null ? new LinkedList<OsmPrimitive>() : memberPrimitives;
            for (Relation r : relations) {
                for (OsmPrimitive p: memberPrimitives) {
                    populate(r, p, oldWays);
                }
            }
            this.relations = relations;
            refresh();
        }

        /**
         * Populates the model with the relation members represented as a collection of
         * {@see RelationToChildReference}s.
         *
         * @param references the references. Empty list assumed if null.
         */
        public void populate(Collection<RelationToChildReference> references) {
            references = references == null ? new LinkedList<RelationToChildReference>() : references;
            decisions.clear();
            this.relations = new HashSet<Relation>(references.size());
            for (RelationToChildReference reference: references) {
                decisions.add(new RelationMemberConflictDecision(reference.getParent(), reference.getPosition()));
                relations.add(reference.getParent());
            }
            refresh();
        }

        /**
         * Replies the decision at position <code>row</code>
         *
         * @param row
         * @return the decision at position <code>row</code>
         */
        public RelationMemberConflictDecision getDecision(int row) {
            return decisions.get(row);
        }

        /**
         * Replies the number of decisions managed by this model
         *
         * @return the number of decisions managed by this model
         */
        public int getNumDecisions() {
            return  getRowCount();
        }

        /**
         * Refreshes the model state. Invoke this method to trigger necessary change
         * events after an update of the model data.
         *
         */
        public void refresh() {
            updateNumConflicts();
            fireTableDataChanged();
        }

        /**
         * Apply a role to all member managed by this model.
         *
         * @param role the role. Empty string assumed if null.
         */
        public void applyRole(String role) {
            role = role == null ? "" : role;
            for (RelationMemberConflictDecision decision : decisions) {
                decision.setRole(role);
            }
            refresh();
        }

        protected RelationMemberConflictDecision getDecision(Relation relation, int pos) {
            for(RelationMemberConflictDecision decision: decisions) {
                if (decision.matches(relation, pos)) return decision;
            }
            return null;
        }

        protected void buildResolveCorrespondance(Relation relation, OsmPrimitive newPrimitive, Map<Relation, Relation> newRelations, Map<Way, Way> oldWays) {

        	List<RelationMember> relationsMembers = relation.getMembers();
        	Relation modifiedRelation = MergeOverlapAction.getNew(relation, newRelations);
            modifiedRelation.setMembers(null);
//            boolean isChanged = false;
            for (int i=0; i < relationsMembers.size(); i++) {
            	RelationMember rm = relationsMembers.get(i);
//                RelationMember rm = relation.getMember(i);
//                RelationMember rmNew;
                RelationMemberConflictDecision decision = getDecision(relation, i);
                if (decision == null) {
                    modifiedRelation.addMember(rm);
                } else {
                	System.out.println(modifiedRelation);
                	System.out.println(111);
                    switch(decision.getDecision()) {
                    case KEEP:
//                    	modifiedRelation.removeMembersFor(newPrimitive);
                    	System.out.println(222);
                    	if (newPrimitive instanceof Way) {
	                        modifiedRelation.addMember(new RelationMember(decision.getRole(), MergeOverlapAction.getOld((Way)newPrimitive, oldWays)));
                    	}
                    	else {
                    		modifiedRelation.addMember(new RelationMember(decision.getRole(), newPrimitive));
                    	}
//                    	modifiedRelation.addMember(new RelationMember(decision.getRole(), newPrimitive));
                        break;
                    case REMOVE:
                    	System.out.println(333);
//                    	modifiedRelation.removeMembersFor(rm.getMember());
//                        isChanged = true;
                        // do nothing
                        break;
                    case UNDECIDED:
                        // FIXME: this is an error
                        break;
                    }
                }
            }
        }

        /**
         * Builds a collection of commands executing the decisions made in this model.
         *
         * @param newPrimitive the primitive which members shall refer to if the
         * decision is {@see RelationMemberConflictDecisionType#REPLACE}
         * @return a list of commands
         */
        public void buildRelationCorrespondance(OsmPrimitive newPrimitive, Map<Relation, Relation> newRelations, Map<Way, Way> oldWays) {
            for (Relation relation : relations) {
            	buildResolveCorrespondance(relation, newPrimitive, newRelations, oldWays);
            }
        }

        protected boolean isChanged(Relation relation, OsmPrimitive newPrimitive) {
            for (int i=0; i < relation.getMembersCount(); i++) {
                RelationMemberConflictDecision decision = getDecision(relation, i);
                if (decision == null) {
                    continue;
                }
                switch(decision.getDecision()) {
                case REMOVE: return true;
                case KEEP:
                    if (!relation.getMember(i).getRole().equals(decision.getRole()))
                        return true;
                    if (relation.getMember(i).getMember() != newPrimitive)
                        return true;
                case UNDECIDED:
                    // FIXME: handle error
                }
            }
            return false;
        }

        /**
         * Replies the set of relations which have to be modified according
         * to the decisions managed by this model.
         *
         * @param newPrimitive the primitive which members shall refer to if the
         * decision is {@see RelationMemberConflictDecisionType#REPLACE}
         *
         * @return the set of relations which have to be modified according
         * to the decisions managed by this model
         */
        public Set<Relation> getModifiedRelations(OsmPrimitive newPrimitive) {
            HashSet<Relation> ret = new HashSet<Relation>();
            for (Relation relation: relations) {
                if (isChanged(relation, newPrimitive)) {
                    ret.add(relation);
                }
            }
            return ret;
        }
    }
    
    public class MyRelationMemberConflictResolver extends JPanel {

        private AutoCompletingTextField tfRole;
        private AutoCompletingTextField tfKey;
        private AutoCompletingTextField tfValue;
        private JCheckBox cbTagRelations;
        private MyRelationMemberConflictResolverModel model;
        private MyRelationMemberConflictResolverTable tblResolver;
        private JMultilineLabel lblHeader;

        protected void build() {
            setLayout(new GridBagLayout());
            JPanel pnl = new JPanel();
            pnl.setLayout(new BorderLayout());
            pnl.add(lblHeader = new JMultilineLabel(""));
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weighty = 0.0;
            gc.weightx = 1.0;
            gc.insets = new Insets(5,5,5,5);
            add(pnl, gc);
            model = new MyRelationMemberConflictResolverModel();

            gc.gridy = 1;
            gc.weighty = 1.0;
            gc.fill = GridBagConstraints.BOTH;
            gc.insets = new Insets(0,0,0,0);
            add(new JScrollPane(tblResolver = new MyRelationMemberConflictResolverTable(model)), gc);
            pnl = new JPanel();
            pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
            pnl.add(buildRoleEditingPanel());
            pnl.add(buildTagRelationsPanel());
            gc.gridy = 2;
            gc.weighty = 0.0;
            gc.fill = GridBagConstraints.HORIZONTAL;
            add(pnl,gc);
        }

        protected JPanel buildRoleEditingPanel() {
            JPanel pnl = new JPanel();
            pnl.setLayout(new FlowLayout(FlowLayout.LEFT));
            pnl.add(new JLabel(tr("Role:")));
            pnl.add(tfRole = new AutoCompletingTextField(10));
            tfRole.setToolTipText(tr("Enter a role for all relation memberships"));
            pnl.add(new JButton(new ApplyRoleAction()));
            tfRole.addActionListener(new ApplyRoleAction());
            tfRole.addFocusListener(
                    new FocusAdapter() {
                        @Override
                        public void focusGained(FocusEvent e) {
                            tfRole.selectAll();
                        }
                    }
            );
            return pnl;
        }

        protected JPanel buildTagRelationsPanel() {
            JPanel pnl = new JPanel();
            pnl.setLayout(new FlowLayout(FlowLayout.LEFT));
            cbTagRelations = new JCheckBox(tr("Tag modified relations with "));
            cbTagRelations.addChangeListener(new ToggleTagRelationsAction());
            cbTagRelations.setToolTipText(
                    tr("<html>Select to enable entering a tag which will be applied<br>"
                            + "to all modified relations.</html>"));
            pnl.add(cbTagRelations);
            pnl.add(new JLabel(trc("tag", "Key:")));
            pnl.add(tfKey = new AutoCompletingTextField(10));
            tfKey.setToolTipText(tr("<html>Enter a tag key, i.e. <strong><tt>fixme</tt></strong></html>"));
            pnl.add(new JLabel(tr("Value:")));
            pnl.add(tfValue = new AutoCompletingTextField(10));
            tfValue.setToolTipText(tr("<html>Enter a tag value, i.e. <strong><tt>check members</tt></strong></html>"));
            cbTagRelations.setSelected(false);
            tfKey.setEnabled(false);
            tfValue.setEnabled(false);
            return pnl;
        }

        public MyRelationMemberConflictResolver() {
            build();
        }

        public void initForWayCombining() {
            lblHeader.setText(tr("<html>The combined ways are members in one ore more relations. "
                    + "Please decide whether you want to <strong>keep</strong> these memberships "
                    + "for the combined way or whether you want to <strong>remove</strong> them.<br>"
                    + "The default is to <strong>keep</strong> the first way and <strong>remove</strong> "
                    + "the other ways that are members of the same relation: the combined way will "
                    + "take the place of the original way in the relation."
                    + "</html>"));
            invalidate();
        }

        public void initForNodeMerging() {
            lblHeader.setText(tr("<html>The merged nodes are members in one ore more relations. "
                    + "Please decide whether you want to <strong>keep</strong> these memberships "
                    + "for the target node or whether you want to <strong>remove</strong> them.<br>"
                    + "The default is to <strong>keep</strong> the first node and <strong>remove</strong> "
                    + "the other nodes that are members of the same relation: the target node will "
                    + "take the place of the original node in the relation."
                    + "</html>"));
            invalidate();
        }

        class ApplyRoleAction extends AbstractAction {
            public ApplyRoleAction() {
                putValue(NAME, tr("Apply"));
                putValue(SMALL_ICON, ImageProvider.get("ok"));
                putValue(SHORT_DESCRIPTION, tr("Apply this role to all members"));
            }

            public void actionPerformed(ActionEvent e) {
                model.applyRole(tfRole.getText());
            }
        }

        class ToggleTagRelationsAction implements ChangeListener {
            public void stateChanged(ChangeEvent e) {
                ButtonModel buttonModel = ((AbstractButton) e.getSource()).getModel();
                tfKey.setEnabled(buttonModel.isSelected());
                tfValue.setEnabled(buttonModel.isSelected());
                tfKey.setBackground(buttonModel.isSelected() ? UIManager.getColor("TextField.background") : UIManager
                        .getColor("Panel.background"));
                tfValue.setBackground(buttonModel.isSelected() ? UIManager.getColor("TextField.background") : UIManager
                        .getColor("Panel.background"));
            }
        }

        public MyRelationMemberConflictResolverModel getModel() {
            return model;
        }

        public Command buildTagApplyCommands(Collection<? extends OsmPrimitive> primitives) {
            if (!cbTagRelations.isSelected())
                return null;
            if (tfKey.getText().trim().equals(""))
                return null;
            if (tfValue.getText().trim().equals(""))
                return null;
            if (primitives == null || primitives.isEmpty())
                return null;
            return new ChangePropertyCommand(primitives, tfKey.getText(), tfValue.getText());
        }

        public void prepareForEditing() {
            AutoCompletionList acList = new AutoCompletionList();
            Main.main.getEditLayer().data.getAutoCompletionManager().populateWithMemberRoles(acList);
            tfRole.setAutoCompletionList(acList);
            AutoCompletingTextField editor = (AutoCompletingTextField) tblResolver.getColumnModel().getColumn(2).getCellEditor();
            if (editor != null) {
                editor.setAutoCompletionList(acList);
            }
            AutoCompletionList acList2 = new AutoCompletionList();
            Main.main.getEditLayer().data.getAutoCompletionManager().populateWithKeys(acList2);
            tfKey.setAutoCompletionList(acList2);
        }
    }


    public class MyRelationMemberConflictResolverTable extends JTable implements MultiValueCellEditor.NavigationListener {

        private SelectNextColumnCellAction selectNextColumnCellAction;
        private SelectPreviousColumnCellAction selectPreviousColumnCellAction;

        public MyRelationMemberConflictResolverTable(MyRelationMemberConflictResolverModel model) {
            super(model, new RelationMemberConflictResolverColumnModel());
            build();
        }

        protected void build() {
            setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

            // make ENTER behave like TAB
            //
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "selectNextColumnCell");

            // install custom navigation actions
            //
            selectNextColumnCellAction = new SelectNextColumnCellAction();
            selectPreviousColumnCellAction = new SelectPreviousColumnCellAction();
            getActionMap().put("selectNextColumnCell", selectNextColumnCellAction);
            getActionMap().put("selectPreviousColumnCell", selectPreviousColumnCellAction);

            setRowHeight((int)new JComboBox().getPreferredSize().getHeight());
        }

        /**
         * Action to be run when the user navigates to the next cell in the table, for instance by
         * pressing TAB or ENTER. The action alters the standard navigation path from cell to cell: <ul>
         * <li>it jumps over cells in the first column</li> <li>it automatically add a new empty row
         * when the user leaves the last cell in the table</li> <ul>
         *
         *
         */
        class SelectNextColumnCellAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                run();
            }

            public void run() {
                int col = getSelectedColumn();
                int row = getSelectedRow();
                if (getCellEditor() != null) {
                    getCellEditor().stopCellEditing();
                }

                if (col == 2 && row < getRowCount() - 1) {
                    row++;
                } else if (row < getRowCount() - 1) {
                    col = 2;
                    row++;
                }
                changeSelection(row, col, false, false);
                editCellAt(getSelectedRow(), getSelectedColumn());
                getEditorComponent().requestFocusInWindow();
            }
        }

        /**
         * Action to be run when the user navigates to the previous cell in the table, for instance by
         * pressing Shift-TAB
         *
         */
        class SelectPreviousColumnCellAction extends AbstractAction {

            public void actionPerformed(ActionEvent e) {
                run();
            }

            public void run() {
                int col = getSelectedColumn();
                int row = getSelectedRow();
                if (getCellEditor() != null) {
                    getCellEditor().stopCellEditing();
                }

                if (col <= 0 && row <= 0) {
                    // change nothing
                } else if (row > 0) {
                    col = 2;
                    row--;
                }
                changeSelection(row, col, false, false);
                editCellAt(getSelectedRow(), getSelectedColumn());
                getEditorComponent().requestFocusInWindow();
            }
        }

        public void gotoNextDecision() {
            selectNextColumnCellAction.run();
        }

        public void gotoPreviousDecision() {
            selectPreviousColumnCellAction.run();
        }
    }


    public static class MyTagConflictResolverModel extends DefaultTableModel {
        static public final String NUM_CONFLICTS_PROP = MyTagConflictResolverModel.class.getName() + ".numConflicts";

        private TagCollection tags;
        private List<String> displayedKeys;
        private Set<String> keysWithConflicts;
        private HashMap<String, MultiValueResolutionDecision> decisions;
        private int numConflicts;
        private PropertyChangeSupport support;
        private boolean showTagsWithConflictsOnly = false;
        private boolean showTagsWithMultiValuesOnly = false;

        public MyTagConflictResolverModel() {
            numConflicts = 0;
            support = new PropertyChangeSupport(this);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

        protected void setNumConflicts(int numConflicts) {
            int oldValue = this.numConflicts;
            this.numConflicts = numConflicts;
            if (oldValue != this.numConflicts) {
                support.firePropertyChange(NUM_CONFLICTS_PROP, oldValue, this.numConflicts);
            }
        }

        protected void refreshNumConflicts() {
            int count = 0;
            for (MultiValueResolutionDecision d : decisions.values()) {
                if (!d.isDecided()) {
                    count++;
                }
            }
            setNumConflicts(count);
        }

        protected void sort() {
            Collections.sort(
                    displayedKeys,
                    new Comparator<String>() {
                        public int compare(String key1, String key2) {
                            if (decisions.get(key1).isDecided() && ! decisions.get(key2).isDecided())
                                return 1;
                            else if (!decisions.get(key1).isDecided() && decisions.get(key2).isDecided())
                                return -1;
                            return key1.compareTo(key2);
                        }
                    }
            );
        }

        /**
         * initializes the model from the current tags
         *
         */
        protected void rebuild() {
            if (tags == null) return;
            for(String key: tags.getKeys()) {
                MultiValueResolutionDecision decision = new MultiValueResolutionDecision(tags.getTagsFor(key));
                if (decisions.get(key) == null) {
                    decisions.put(key,decision);
                }
            }
            displayedKeys.clear();
            Set<String> keys = tags.getKeys();
            if (showTagsWithConflictsOnly) {
                keys.retainAll(keysWithConflicts);
                if (showTagsWithMultiValuesOnly) {
                    Set<String> keysWithMultiValues = new HashSet<String>();
                    for (String key: keys) {
                        if (decisions.get(key).canKeepAll()) {
                            keysWithMultiValues.add(key);
                        }
                    }
                    keys.retainAll(keysWithMultiValues);
                }
                for (String key: tags.getKeys()) {
                    if (!decisions.get(key).isDecided() && !keys.contains(key)) {
                        keys.add(key);
                    }
                }
            }
            displayedKeys.addAll(keys);
            refreshNumConflicts();
            sort();
            fireTableDataChanged();
        }

        /**
         * Populates the model with the tags for which conflicts are to be resolved.
         *
         * @param tags  the tag collection with the tags. Must not be null.
         * @param keysWithConflicts the set of tag keys with conflicts
         * @throws IllegalArgumentException thrown if tags is null
         */
        public void populate(TagCollection tags, Set<String> keysWithConflicts) {
            CheckParameterUtil.ensureParameterNotNull(tags, "tags");
            this.tags = tags;
            displayedKeys = new ArrayList<String>();
            this.keysWithConflicts = keysWithConflicts == null ? new HashSet<String>() : keysWithConflicts;
            decisions = new HashMap<String, MultiValueResolutionDecision>();
            rebuild();
        }

        @Override
        public int getRowCount() {
            if (displayedKeys == null) return 0;
            return displayedKeys.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            return decisions.get(displayedKeys.get(row));
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            MultiValueResolutionDecision decision = decisions.get(displayedKeys.get(row));
            if (value instanceof String) {
                decision.keepOne((String)value);
            } else if (value instanceof MultiValueDecisionType) {
                MultiValueDecisionType type = (MultiValueDecisionType)value;
                switch(type) {
                case KEEP_NONE:
                    decision.keepNone();
                    break;
                case KEEP_ALL:
                    decision.keepAll();
                    break;
                }
            }
            fireTableDataChanged();
            refreshNumConflicts();
        }

        /**
         * Replies true if each {@see MultiValueResolutionDecision} is decided.
         *
         * @return true if each {@see MultiValueResolutionDecision} is decided; false
         * otherwise
         */
        public boolean isResolvedCompletely() {
            return numConflicts == 0;
        }

        public int getNumConflicts() {
            return numConflicts;
        }

        public int getNumDecisions() {
            return getRowCount();
        }

        //TODO Should this method work with all decisions or only with displayed decisions? For MergeNodes it should be
        //all decisions, but this method is also used on other places, so I've made new method just for MergeNodes
        public TagCollection getResolution() {
            TagCollection tc = new TagCollection();
            for (String key: displayedKeys) {
                tc.add(decisions.get(key).getResolution());
            }
            return tc;
        }

        public TagCollection getAllResolutions() {
            TagCollection tc = new TagCollection();
            for (MultiValueResolutionDecision value: decisions.values()) {
                tc.add(value.getResolution());
            }
            return tc;
        }

        public MultiValueResolutionDecision getDecision(int row) {
            return decisions.get(displayedKeys.get(row));
        }

        /**
         * Sets whether all tags or only tags with conflicts are displayed
         *
         * @param showTagsWithConflictsOnly if true, only tags with conflicts are displayed
         */
        public void setShowTagsWithConflictsOnly(boolean showTagsWithConflictsOnly) {
            this.showTagsWithConflictsOnly = showTagsWithConflictsOnly;
            rebuild();
        }

        /**
         * Sets whether all conflicts or only conflicts with multiple values are displayed
         *
         * @param showTagsWithMultiValuesOnly if true, only tags with multiple values are displayed
         */
        public void setShowTagsWithMultiValuesOnly(boolean showTagsWithMultiValuesOnly) {
            this.showTagsWithMultiValuesOnly = showTagsWithMultiValuesOnly;
            rebuild();
        }

        /**
         * Prepare the default decisions for the current model
         *
         */
        public void prepareDefaultTagDecisions() {
            for (MultiValueResolutionDecision decision: decisions.values()) {
                List<String> values = decision.getValues();
                values.remove("");
                if (values.size() == 1) {
                    decision.keepOne(values.get(0));
                } else {
                    decision.keepAll();
                }
            }
            rebuild();
        }

    }


    /**
     * This is a UI widget for resolving tag conflicts, i.e. differences of the tag values
     * of multiple {@see OsmPrimitive}s.
     *
     *
     */
    public class MyTagConflictResolver extends JPanel {

        /** the model for the tag conflict resolver */
        private MyTagConflictResolverModel model;
        /** selects wheter only tags with conflicts are displayed */
        private JCheckBox cbShowTagsWithConflictsOnly;
        private JCheckBox cbShowTagsWithMultiValuesOnly;

        protected JPanel buildInfoPanel() {
            JPanel pnl = new JPanel();
            pnl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            pnl.setLayout(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.BOTH;
            gc.weighty = 1.0;
            gc.weightx = 1.0;
            gc.anchor = GridBagConstraints.LINE_START;
            pnl.add(new JLabel(tr("<html>Please select the values to keep for the following tags.</html>")), gc);

            gc.gridy = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weighty = 0.0;
            pnl.add(cbShowTagsWithConflictsOnly = new JCheckBox(tr("Show tags with conflicts only")), gc);
            pnl.add(cbShowTagsWithMultiValuesOnly = new JCheckBox(tr("Show tags with multiple values only")), gc);
            cbShowTagsWithConflictsOnly.addChangeListener(
                    new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            model.setShowTagsWithConflictsOnly(cbShowTagsWithConflictsOnly.isSelected());
                            cbShowTagsWithMultiValuesOnly.setEnabled(cbShowTagsWithConflictsOnly.isSelected());
                        }
                    }
            );
            cbShowTagsWithConflictsOnly.setSelected(
                    Main.pref.getBoolean(getClass().getName() + ".showTagsWithConflictsOnly", false)
            );
            cbShowTagsWithMultiValuesOnly.addChangeListener(
                    new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            model.setShowTagsWithMultiValuesOnly(cbShowTagsWithMultiValuesOnly.isSelected());
                        }
                    }
            );
            cbShowTagsWithMultiValuesOnly.setSelected(
                    Main.pref.getBoolean(getClass().getName() + ".showTagsWithMultiValuesOnly", false)
            );
            cbShowTagsWithMultiValuesOnly.setEnabled(cbShowTagsWithConflictsOnly.isSelected());
            return pnl;
        }

        /**
         * Remembers the current settings in the global preferences
         *
         */
        public void rememberPreferences() {
            Main.pref.put(getClass().getName() + ".showTagsWithConflictsOnly", cbShowTagsWithConflictsOnly.isSelected());
            Main.pref.put(getClass().getName() + ".showTagsWithMultiValuesOnly", cbShowTagsWithMultiValuesOnly.isSelected());
        }

        protected void build() {
            setLayout(new BorderLayout());
            add(buildInfoPanel(), BorderLayout.NORTH);
            add(new JScrollPane(new MyTagConflictResolverTable(model)), BorderLayout.CENTER);
        }

        public MyTagConflictResolver() {
            this.model = new MyTagConflictResolverModel();
            build();
        }

        /**
         * Replies the model used by this dialog
         *
         * @return the model
         */
        public MyTagConflictResolverModel getModel() {
            return model;
        }
    }

    public class MyTagConflictResolverTable extends JTable implements MultiValueCellEditor.NavigationListener {

        private SelectNextColumnCellAction selectNextColumnCellAction;
        private SelectPreviousColumnCellAction selectPreviousColumnCellAction;

        public MyTagConflictResolverTable(MyTagConflictResolverModel model) {
            super(model, new TagConflictResolverColumnModel());
            build();
        }

        protected void build() {
            setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

            // make ENTER behave like TAB
            //
            getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "selectNextColumnCell");

            // install custom navigation actions
            //
            selectNextColumnCellAction = new SelectNextColumnCellAction();
            selectPreviousColumnCellAction = new SelectPreviousColumnCellAction();
            getActionMap().put("selectNextColumnCell", selectNextColumnCellAction);
            getActionMap().put("selectPreviousColumnCell", selectPreviousColumnCellAction);

            ((MultiValueCellEditor)getColumnModel().getColumn(2).getCellEditor()).addNavigationListeners(this);

            setRowHeight((int)new JComboBox().getPreferredSize().getHeight());
        }

        /**
         * Action to be run when the user navigates to the next cell in the table, for instance by
         * pressing TAB or ENTER. The action alters the standard navigation path from cell to cell: <ul>
         * <li>it jumps over cells in the first column</li> <li>it automatically add a new empty row
         * when the user leaves the last cell in the table</li> <ul>
         *
         *
         */
        class SelectNextColumnCellAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                run();
            }

            public void run() {
                int col = getSelectedColumn();
                int row = getSelectedRow();
                if (getCellEditor() != null) {
                    getCellEditor().stopCellEditing();
                }

                if (col == 2 && row < getRowCount() - 1) {
                    row++;
                } else if (row < getRowCount() - 1) {
                    col = 2;
                    row++;
                }
                changeSelection(row, col, false, false);
                editCellAt(getSelectedRow(), getSelectedColumn());
                getEditorComponent().requestFocusInWindow();
            }
        }

        /**
         * Action to be run when the user navigates to the previous cell in the table, for instance by
         * pressing Shift-TAB
         *
         */
        class SelectPreviousColumnCellAction extends AbstractAction {

            public void actionPerformed(ActionEvent e) {
                run();
            }

            public void run() {
                int col = getSelectedColumn();
                int row = getSelectedRow();
                if (getCellEditor() != null) {
                    getCellEditor().stopCellEditing();
                }

                if (col <= 0 && row <= 0) {
                    // change nothing
                } else if (row > 0) {
                    col = 2;
                    row--;
                }
                changeSelection(row, col, false, false);
                editCellAt(getSelectedRow(), getSelectedColumn());
                getEditorComponent().requestFocusInWindow();
            }
        }

        public void gotoNextDecision() {
            selectNextColumnCellAction.run();
        }

        public void gotoPreviousDecision() {
            selectPreviousColumnCellAction.run();
        }
    }
}
