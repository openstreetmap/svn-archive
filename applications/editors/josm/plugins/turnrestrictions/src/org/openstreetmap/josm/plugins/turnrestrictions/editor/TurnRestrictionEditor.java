package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ConflictAddCommand;
import org.openstreetmap.josm.data.Preferences.PreferenceChangeEvent;
import org.openstreetmap.josm.data.Preferences.PreferenceChangedListener;
import org.openstreetmap.josm.data.conflict.Conflict;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.gui.HelpAwareOptionPane;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.HelpAwareOptionPane.ButtonSpec;
import org.openstreetmap.josm.gui.dialogs.relation.RelationEditor;
import org.openstreetmap.josm.gui.help.ContextSensitiveHelpAction;
import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.turnrestrictions.preferences.PreferenceKeys;
import org.openstreetmap.josm.plugins.turnrestrictions.qa.IssuesView;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.ImageProvider;

public class TurnRestrictionEditor extends JDialog implements NavigationControler{
    final private static Logger logger = Logger.getLogger(TurnRestrictionEditor.class.getName());
    
    /** the property name for the current turn restriction
     * @link #setRelation(Relation)
     * @link #getRelation()
     */
    static public final String TURN_RESTRICION_PROP = RelationEditor.class.getName() + ".turnRestriction";

    /** the property name for the current relation snapshot
     * @link #getRelationSnapshot()
     */
    static public final String TURN_RESTRICION_SNAPSHOT_PROP = RelationEditor.class.getName() + ".turnRestrictionSnapshot";
    
    /**
     * The turn restriction this editor is working on
     */
    protected Relation turnRestriction;

    /**
     * The version of the turn restriction when editing is started.  This is
     * null if a new turn restriction is created. 
     */
    protected Relation turnRestrictionSnapshot;

    /** the data layer the turn restriction belongs to */
    private OsmDataLayer layer;
    
    private JosmSelectionPanel pnlJosmSelection;
    private BasicEditorPanel pnlBasicEditor;
    private TurnRestrictionEditorModel editorModel;
    private JTabbedPane tpEditors;
    private PreferenceChangeHandler preferenceChangeHandler;
    
    /**
     * builds the panel with the OK and the Cancel button
     *
     * @return the panel with the OK and the Cancel button
     */
    protected JPanel buildOkCancelButtonPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new FlowLayout(FlowLayout.CENTER));

        SideButton b;
        pnl.add(b = new SideButton(new OKAction()));
        b.setName("btnOK");
        pnl.add(b = new SideButton(new CancelAction()));
        b.setName("btnCancel");
        pnl.add(b = new SideButton(new ContextSensitiveHelpAction(ht("/Plugin/TurnRestrictions#TurnRestrictionEditor"))));
        b.setName("btnHelp");
        return pnl;
    }
    
    /**
     * builds the panel which displays the JOSM selection 
     * @return
     */
    protected JPanel buildJOSMSelectionPanel() {
        pnlJosmSelection = new JosmSelectionPanel(layer,editorModel.getJosmSelectionListModel());
        return pnlJosmSelection;
    }
    
    /**
     * Builds the panel with the editor forms (the left panel in the split pane of 
     * this dialog)
     * 
     * @return
     */
    protected JPanel buildEditorPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        tpEditors = new JTabbedPane();
        JScrollPane pane = new JScrollPane(pnlBasicEditor =new BasicEditorPanel(editorModel));
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tpEditors.add(pane);
        tpEditors.setTitleAt(0, tr("Basic"));
        tpEditors.setToolTipTextAt(0, tr("Edit basic attributes of a turn restriction"));
        
        tpEditors.add(new AdvancedEditorPanel(editorModel));
        tpEditors.setTitleAt(1, tr("Advanced"));
        tpEditors.setToolTipTextAt(1, tr("Edit the raw tags and members of this turn restriction"));
        
        tpEditors.add(new IssuesView(editorModel.getIssuesModel()));
        tpEditors.setTitleAt(2, tr("Errors/Warnings"));
        tpEditors.setToolTipTextAt(2, tr("Show errors and warnings related to this turn restriction"));
        
        pnl.add(tpEditors, BorderLayout.CENTER);
        return pnl;
    }
    
    /**
     * Builds the content panel, i.e. the core area of the dialog with the editor
     * masks and the JOSM selection view 
     * 
     * @return
     */
    protected JPanel buildContentPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        final JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pnl.add(sp, BorderLayout.CENTER);
        sp.setLeftComponent(buildEditorPanel());
        sp.setRightComponent(buildJOSMSelectionPanel());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // has to be called when the window is visible, otherwise
                // no effect
                sp.setDividerLocation(0.7);
            }
        });

        return pnl;
    }
    
    /**
     * Creates the toolbar
     *
     * @return the toolbar
     */
    protected JToolBar buildToolBar() {
        JToolBar tb  = new JToolBar();
        tb.setFloatable(false);
        tb.add(new ApplyAction());
        tb.addSeparator();
        DeleteAction actDelete = new DeleteAction();
        tb.add(actDelete);
        addPropertyChangeListener(actDelete);
        tb.addSeparator();
        SelectAction actSelect = new SelectAction();
        tb.add(actSelect);
        addPropertyChangeListener(actSelect);

        ZoomToAction actZoomTo = new ZoomToAction();
        tb.add(actZoomTo);
        addPropertyChangeListener(actZoomTo);
        return tb;
    }
    
    /**
     * builds the UI
     */
    protected void build() {        
        editorModel = new TurnRestrictionEditorModel(getLayer(), this);
        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(buildToolBar(), BorderLayout.NORTH);
        c.add(buildContentPanel(), BorderLayout.CENTER);        
        c.add(buildOkCancelButtonPanel(), BorderLayout.SOUTH);
        
        editorModel.getIssuesModel().addObserver(new IssuesModelObserver());
        setSize(600,600);       
    }    
    
    /**
    * Creates a new turn restriction editor
    *
    * @param owner the component relative to which the dialog is displayed 
    * @param layer  the {@link OsmDataLayer} in whose context a relation is edited. Must not be null.
    * @throws IllegalArgumentException thrown if layer is null
    */
    public TurnRestrictionEditor(Component owner, OsmDataLayer layer) {
        this(owner, layer, null);
    }
    
     /**
     * Creates a new turn restriction editor
     *
     * @param owner the component relative to which the dialog is displayed 
     * @param layer  the {@link OsmDataLayer} in whose context a relation is edited. Must not be null.
     * @param turnRestriction the relation. Can be null if a new relation is to be edited.
     * @throws IllegalArgumentException thrown if layer is null
     */
    public TurnRestrictionEditor(Component owner, OsmDataLayer layer, Relation turnRestriction)  throws IllegalArgumentException{
        super(JOptionPane.getFrameForComponent(owner),false /* not modal */);
        CheckParameterUtil.ensureParameterNotNull(layer, "layer");
        this.layer = layer;
        build();
        setTurnRestriction(turnRestriction);
    }
    
    /**
     * Replies the currently edited turn restriction
     *
     * @return the currently edited relation
     */
    protected Relation getTurnRestriction() {
        return turnRestriction;
    }

    /**
     * <p>Sets the currently edited turn restriction. Creates a snapshot of the current
     * state of the turn restriction. See {@link #getTurnRestrictionSnapshot()}</p>
     *
     * <p>{@code turnRestriction} can be null if a new restriction is created. A turn
     * restriction which isn't assigned to a data set is allowed too. If {@code turnRestriction}
     * is already assigned to a dataset, the dataset of {@link #getLayer()} is required, otherwise
     * a {@link IllegalArgumentException} is thrown.</p>
     * 
     * @param turnRestriction the turn restriction
     * @throws IllegalArgumentException thrown if {@code turnRestriction} belongs to a different dataset than
     * that owned by the layer {@link #getLayer()}
     */
    protected void setTurnRestriction(Relation turnRestriction) {      
        if (turnRestriction == null) {
            editorModel.populate(new Relation());
        } else if (turnRestriction.getDataSet() == null || turnRestriction.getDataSet() == getLayer().data) {
            editorModel.populate(turnRestriction);
        } else {
            throw new IllegalArgumentException(MessageFormat.format("turnRestriction must belong to layer ''{0}''", getLayer().getName()));
        }
        setTurnRestrictionSnapshot(turnRestriction == null ? null : new Relation(turnRestriction));
        Relation oldValue = this.turnRestriction;
        this.turnRestriction = turnRestriction;
        support.firePropertyChange(TURN_RESTRICION_PROP, null, this.turnRestriction);
        updateTitle();
    }
    
    /**
     * updates the title of the turn restriction editor
     */
    protected void updateTitle() {
        if (getTurnRestriction() == null || getTurnRestriction().getDataSet() == null) {
            setTitle(tr("Create a new turn restriction in layer ''{0}''", layer.getName()));
        } else if (getTurnRestriction().isNew()) {
            setTitle(tr("Edit a new turn restriction in layer ''{0}''", layer.getName()));
        } else {
            setTitle(tr("Edit turn restriction ''{0}'' in layer ''{1}''", Long.toString(turnRestriction.getId()), layer.getName()));
        }
    }
    
    /**
     * Replies the {@link OsmDataLayer} in whose context this relation editor is
     * open
     *
     * @return the {@link OsmDataLayer} in whose context this relation editor is
     * open
     */
    protected OsmDataLayer getLayer() {
        return layer;
    }

    /**
     * Replies the state of the edited relation when the editor has been launched
     *
     * @return the state of the edited relation when the editor has been launched
     */
    protected Relation getTurnRestrictionSnapshot() {
        return turnRestrictionSnapshot;
    }

    /**
     * Sets the turn restriction snapshot
     * 
     * @param snapshot the snapshot
     */
    protected void setTurnRestrictionSnapshot(Relation snapshot) {        
        turnRestrictionSnapshot = snapshot;
        support.firePropertyChange(TURN_RESTRICION_SNAPSHOT_PROP, null, turnRestrictionSnapshot);
    }
    
    /**
     * <p>Replies true if the currently edited turn restriction has been changed elsewhere.</p>
     *
     * <p>In this case a turn restriction editor can't apply updates to the turn restriction
     * directly. Rather, it has to create a conflict.</p>
     *
     * @return true if the currently edited turn restriction has been changed elsewhere.
     */
    protected boolean isDirtyTurnRestriction() {
        return ! turnRestriction.hasEqualSemanticAttributes(turnRestrictionSnapshot);
    }
    
    /**
     * Replies the editor model for this editor 
     */
    public TurnRestrictionEditorModel getModel() {
        return editorModel;
    }
    
    public void setVisible(boolean visible) {
        if (visible && ! isVisible()) {
            pnlJosmSelection.wireListeners();
            editorModel.registerAsEventListener();
            Main.pref.addPreferenceChangeListener(this.preferenceChangeHandler = new PreferenceChangeHandler());
            pnlBasicEditor.initIconSetFromPreferences(Main.pref);
        } else if (!visible && isVisible()) {
            pnlJosmSelection.unwireListeners();
            editorModel.unregisterAsEventListener();
            Main.pref.removePreferenceChangeListener(preferenceChangeHandler);
        }
        super.setVisible(visible);
        if (!visible){
            dispose();
        }
    }
    
    /* ----------------------------------------------------------------------- */
    /* property change support                                                 */
    /* ----------------------------------------------------------------------- */
    final private PropertyChangeSupport support = new PropertyChangeSupport(this);

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }
    
    /* ----------------------------------------------------------------------- */
    /* interface NavigationControler                                           */
    /* ----------------------------------------------------------------------- */
    public void gotoBasicEditor() {
        tpEditors.setSelectedIndex(0);
    }

    public void gotoAdvancedEditor() {
        tpEditors.setSelectedIndex(1);
    }

    public void gotoBasicEditor(BasicEditorFokusTargets focusTarget) {
        tpEditors.setSelectedIndex(0);
        pnlBasicEditor.requestFocusFor(focusTarget);
    }

    /**
     * The abstract base action for applying the updates of a turn restriction
     * to the dataset.
     */
    abstract class SavingAction extends AbstractAction {        
        protected boolean confirmSaveDespiteOfErrorsAndWarnings(){
            int numErrors = editorModel.getIssuesModel().getNumErrors();
            int numWarnings = editorModel.getIssuesModel().getNumWarnings();
            if (numErrors + numWarnings == 0) return true;
            
            StringBuffer sb = new StringBuffer();
            sb.append("<html>");
            sb.append(trn(
                "There is still an unresolved error or warning identified for this turn restriction. "
                    + "You are recommended to resolve this issue first.",
                  "There are still {0} errors and/or warnings identified for this turn restriction. "
                    + "You are recommended to resolve these issues first.",
                  numErrors + numWarnings,
                  numErrors + numWarnings
            ));
            sb.append("<br>");
            sb.append(tr("Do you want to save anyway?"));
            ButtonSpec[] options = new ButtonSpec[] {
                    new ButtonSpec(
                            tr("Yes, save anyway"),
                            ImageProvider.get("ok"),
                            tr("Save the turn restriction despite of errors and/or warnings"),
                            null // no specific help topic
                    ),
                    new ButtonSpec(
                            tr("No, resolve issues first"),
                            ImageProvider.get("cancel"),
                            tr("Cancel saving and start resolving pending issues first"),
                            null // no specific help topic
                    )
            };
            
            int ret = HelpAwareOptionPane.showOptionDialog(
                    JOptionPane.getFrameForComponent(TurnRestrictionEditor.this),
                    sb.toString(),
                    tr("Pending errors and warnings"),
                    JOptionPane.WARNING_MESSAGE,
                    null, // no special icon
                    options,
                    options[1], // cancel is default operation
                    HelpUtil.ht("/Plugin/TurnRestrictions#PendingErrorsAndWarnings")
            );
            return ret == 0 /* OK */;           
        }
        
        /**
         * Replies the list of relation members in {@code r} which refer to
         * a deleted or invisible primitives.
         * 
         * @param r the relation 
         * @return the list of relation members in {@code r} which refer to
         * a deleted or invisible member
         */
        protected List<RelationMember> getDeletedRelationMembers(Relation r) {
            List<RelationMember> ret = new ArrayList<RelationMember>();
            for(RelationMember rm: r.getMembers()) {
                if (rm.getMember().isDeleted() || !rm.getMember().isVisible()) {
                    ret.add(rm);
                }
            }
            return ret;
        }
        
        /**
         * Removes all members referring to deleted or invisible primitives
         * from the turn restriction {@code tr}.
         * 
         * @param tr  the turn restriction
         */
        protected void removeDeletedMembers(Relation tr) {
            List<RelationMember> members = tr.getMembers();
            for(Iterator<RelationMember> it = members.iterator(); it.hasNext();) {
                RelationMember rm = it.next();
                if (rm.getMember().isDeleted() || !rm.getMember().isVisible()) {
                    it.remove();
                }
            }
            tr.setMembers(members);
        }
        
        /**
         * <p>Asks the user how to proceed if a turn restriction refers to deleted or invisible
         * primitives.</p>
         * 
         * <p>If this method returns true the respective members should be removed and the turn
         * restriction should be saved anyway. If it replies false, the turn restriction must not
         * be saved. </p>
         * 
         * @param deletedMembers the list of members referring to deleted or invisible primitives  
         * @return the confirmation 
         */
        protected boolean confirmSaveTurnRestrictionWithDeletePrimitives(List<RelationMember> deletedMembers) {                     
            StringBuffer sb = new StringBuffer();
            sb.append("<html>");
            sb.append(trn("This turn restriction refers to an object which was deleted outside "
                       + "of this turn restriction editor:",
                       "This turn restriction refers to {0} objects which were deleted outside "
                       + "of this turn restriction editor:", deletedMembers.size(), deletedMembers.size()));
            sb.append("<ul>");
            for(RelationMember rm: deletedMembers){
                sb.append("<li>");
                if (!rm.getRole().equals("")) {
                    sb.append(rm.getRole()).append(": ");
                }
                sb.append(rm.getMember().getDisplayName(DefaultNameFormatter.getInstance()));
                sb.append("</li>");
            }
            sb.append(tr("Updates to this turn restriction can''t be saved unless deleted members are removed.<br>"
                    + "How to you want to proceed?"));
            
            ButtonSpec[] options = new ButtonSpec[] {
                    new ButtonSpec(
                        tr("Remove deleted members and save"),
                        ImageProvider.get("OK"),
                        tr("Remove deleted members and save"),
                        null
                     ),
                      new ButtonSpec(
                            tr("Cancel and return to editor"),
                            ImageProvider.get("cancel"),
                            tr("Cancel and return to editor"),
                            null
                       )
            };
            
            int ret = HelpAwareOptionPane.showOptionDialog(
                    TurnRestrictionEditor.this,
                    sb.toString(),
                    tr("Deleted members in turn restriction"),
                    JOptionPane.WARNING_MESSAGE,
                    null, // no special icon
                    options,
                    options[1], // cancel is default
                    null // FIXME: provide help topic
            );          
            return ret == 0 /* OK button */; 
        }
        
        /**
         * apply updates to a new turn restriction
         */
        protected boolean applyNewTurnRestriction() {           
            Relation newTurnRestriction = new Relation();
            editorModel.apply(newTurnRestriction);

            // If the user wanted to create a new turn restriction, but didn't add any members or
            // tags, don't add an empty relation
            if (newTurnRestriction.getMembersCount() == 0 && !newTurnRestriction.hasKeys())
                return true;
            
            // check whether the turn restriction refers to new 
            List<RelationMember> deletedMembers = getDeletedRelationMembers(newTurnRestriction);
            if (!deletedMembers.isEmpty()) {
                if (!confirmSaveTurnRestrictionWithDeletePrimitives(deletedMembers)) {
                    return false;
                }
                removeDeletedMembers(newTurnRestriction);
            }
            
            Main.main.undoRedo.add(new AddCommand(getLayer(),newTurnRestriction));

            // make sure everybody is notified about the changes
            //
            TurnRestrictionEditor.this.setTurnRestriction(newTurnRestriction);
            TurnRestrictionEditorManager.getInstance().updateContext(
                    getLayer(),
                    getTurnRestriction(),
                    TurnRestrictionEditor.this
            );
            return true;
        }

        /**
         * Apply the updates for an existing turn restriction which has been changed
         * outside of the turn restriction editor.
         *
         */
        protected void applyExistingConflictingTurnRestriction() {
            Relation toUpdate = new Relation(getTurnRestriction());
            editorModel.apply(toUpdate);
            Conflict<Relation> conflict = new Conflict<Relation>(getTurnRestriction(), toUpdate);
            Main.main.undoRedo.add(new ConflictAddCommand(getLayer(),conflict));
        }

        /**
         * Apply the updates for an existing turn restriction which has not been changed
         * outside of the turn restriction editor.
         */
        protected void applyExistingNonConflictingTurnRestriction() {           
            if (getTurnRestriction().getDataSet() == null) {
                editorModel.apply(getTurnRestriction());
                Main.main.undoRedo.add(new AddCommand(getTurnRestriction()));
            } else {
                Relation toUpdate = new Relation(getTurnRestriction());
                editorModel.apply(toUpdate);            
                Main.main.undoRedo.add(new ChangeCommand(getTurnRestriction(), toUpdate));
            }
            // this will refresh the snapshot and update the dialog title
            //
            setTurnRestriction(getTurnRestriction());
        }

        protected boolean confirmClosingBecauseOfDirtyState() {
            ButtonSpec [] options = new ButtonSpec[] {
                    new ButtonSpec(
                            tr("Yes, create a conflict and close"),
                            ImageProvider.get("ok"),
                            tr("Create a conflict and close this turn restriction editor") ,
                            null /* no specific help topic */
                    ),
                    new ButtonSpec(
                            tr("No, continue editing"),
                            ImageProvider.get("cancel"),
                            tr("Return to the turn restriction editor and resume editing") ,
                            null /* no specific help topic */
                    )
            };

            int ret = HelpAwareOptionPane.showOptionDialog(
                    Main.parent,
                    tr("<html>This turn restriction has been changed outside of the editor.<br>"
                            + "You cannot apply your changes and continue editing.<br>"
                            + "<br>"
                            + "Do you want to create a conflict and close the editor?</html>"),
                            tr("Conflict in data"),
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            options,
                            options[0], // OK is default
                            null // FIXME: provide help topic
            );
            return ret == 0;
        }

        protected void warnDoubleConflict() {
            JOptionPane.showMessageDialog(
                    Main.parent,
                    tr("<html>Layer ''{0}'' already has a conflict for object<br>"
                            + "''{1}''.<br>"
                            + "Please resolve this conflict first, then try again.</html>",
                            getLayer().getName(),
                            getTurnRestriction().getDisplayName(DefaultNameFormatter.getInstance())
                    ),
                    tr("Already participating in a conflict"),
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    class ApplyAction extends SavingAction {
        public ApplyAction() {
            putValue(SHORT_DESCRIPTION, tr("Apply the current updates"));
            putValue(SMALL_ICON, ImageProvider.get("save"));
            putValue(NAME, tr("Apply"));
            setEnabled(true);
        }

        public void run() {
            if (!confirmSaveDespiteOfErrorsAndWarnings()){
                tpEditors.setSelectedIndex(2); // show the errors and warnings
                return;
            }
            if (getTurnRestriction() == null || getTurnRestriction().getDataSet() == null) {
                applyNewTurnRestriction();
                return;
            } 
            
            Relation toUpdate = new Relation(getTurnRestriction());
            editorModel.apply(toUpdate);
            if (TurnRestrictionEditorModel.hasSameMembersAndTags(toUpdate, getTurnRestriction()))
                // nothing to update 
                return;
            
            if (isDirtyTurnRestriction()) {
                if (confirmClosingBecauseOfDirtyState()) {
                    if (getLayer().getConflicts().hasConflictForMy(getTurnRestriction())) {
                        warnDoubleConflict();
                        return;
                    }
                    applyExistingConflictingTurnRestriction();
                    setVisible(false);
                }
            } else {
                applyExistingNonConflictingTurnRestriction();
            }            
        }

        public void actionPerformed(ActionEvent e) {
            run();
        }
    }
    
    class OKAction extends SavingAction {
        public OKAction() {
            putValue(SHORT_DESCRIPTION, tr("Apply the updates and close the dialog"));
            putValue(SMALL_ICON, ImageProvider.get("ok"));
            putValue(NAME, tr("OK"));
            setEnabled(true);
        }

        public void run() {
            if (!confirmSaveDespiteOfErrorsAndWarnings()){
                tpEditors.setSelectedIndex(2); // show the errors and warnings
                return;
            }
            if (getTurnRestriction() == null || getTurnRestriction().getDataSet() == null) {
                // it's a new turn restriction. Try to save it and close the dialog
                if (applyNewTurnRestriction()) {
                    setVisible(false);
                }
                return;
            } 
            
            Relation toUpdate = new Relation(getTurnRestriction());
            editorModel.apply(toUpdate);
            if (TurnRestrictionEditorModel.hasSameMembersAndTags(toUpdate, getTurnRestriction())){
                // nothing to update 
                setVisible(false);
                return;
            }
            
            if (isDirtyTurnRestriction()) {
                // the turn restriction this editor is working on has changed outside
                // of the editor. 
                if (confirmClosingBecauseOfDirtyState()) {
                    if (getLayer().getConflicts().hasConflictForMy(getTurnRestriction())) {
                        warnDoubleConflict();
                        return;
                    }
                    applyExistingConflictingTurnRestriction();
                } else
                    return;
            } else {
                applyExistingNonConflictingTurnRestriction();
            }        
            setVisible(false);
        }

        public void actionPerformed(ActionEvent e) {
            run();
        }
    }

    /**
     * Action for canceling the current dialog 
     */
    class CancelAction extends AbstractAction {
        public CancelAction() {
            putValue(SHORT_DESCRIPTION, tr("Cancel the updates and close the dialog"));
            putValue(SMALL_ICON, ImageProvider.get("cancel"));
            putValue(NAME, tr("Cancel"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ESCAPE"));
            TurnRestrictionEditor.this.getRootPane().registerKeyboardAction(this,KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
            setEnabled(true);
        }

        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }
    
    class DeleteAction extends AbstractAction implements PropertyChangeListener{
        public DeleteAction() {
            putValue(NAME, tr("Delete"));
            putValue(SHORT_DESCRIPTION, tr("Delete this turn restriction"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "delete"));
            updateEnabledState();
        }
        
        protected void updateEnabledState() {           
            Relation tr = getTurnRestriction();
            setEnabled(tr != null && tr.getDataSet() != null);
        }

        public void actionPerformed(ActionEvent e) {
            Relation tr = getTurnRestriction();
            if (tr == null || tr.getDataSet() == null) return;
            org.openstreetmap.josm.actions.mapmode.DeleteAction.deleteRelation(
                    getLayer(),
                    tr
            );
            setVisible(false);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(TURN_RESTRICION_PROP)){
                updateEnabledState();
            }
        }
    }
    
    class SelectAction extends AbstractAction implements PropertyChangeListener{
        public SelectAction() {
            putValue(NAME, tr("Select"));
            putValue(SHORT_DESCRIPTION, tr("Select this turn restriction"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs", "select"));
            updateEnabledState();
        }
        
        protected void updateEnabledState() {
            Relation tr = getTurnRestriction();
            setEnabled(tr != null && tr.getDataSet() != null);
        }

        public void actionPerformed(ActionEvent e) {
            Relation tr = getTurnRestriction();
            if (tr == null || tr.getDataSet() == null) return;
            getLayer().data.setSelected(tr);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(TURN_RESTRICION_PROP)){
                updateEnabledState();
            }
        }
    }
    
    class ZoomToAction extends AbstractAction implements PropertyChangeListener{
        public ZoomToAction() {
            putValue(NAME, tr("Zoom to"));
            putValue(SHORT_DESCRIPTION, tr("Activate the layer this turn restriction belongs to and zoom to it"));
            putValue(SMALL_ICON, ImageProvider.get("dialogs/autoscale", "data"));
            updateEnabledState();
        }
        
        protected void updateEnabledState() {
            Relation tr = getTurnRestriction();
            setEnabled(tr != null && tr.getDataSet() != null);
        }

        public void actionPerformed(ActionEvent e) {
            if (Main.main.getActiveLayer() != getLayer()){
                Main.map.mapView.setActiveLayer(getLayer());
            }
            Relation tr = getTurnRestriction();
            if (tr == null || tr.getDataSet() == null) return;
            getLayer().data.setSelected(tr);            
            AutoScaleAction.zoomToSelection();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(TURN_RESTRICION_PROP)){
                updateEnabledState();
            }
        }
    }
    
    class IssuesModelObserver implements Observer {
        public void update(Observable o, Object arg) {
            int numWarnings = editorModel.getIssuesModel().getNumWarnings();
            int numErrors = editorModel.getIssuesModel().getNumErrors();
            String warningText = null;
            if (numWarnings > 0){
                warningText = trn("{0} warning", "{0} warnings", numWarnings, numWarnings);
            }
            String errorText = null;
            if (numErrors > 0){
                errorText = trn("{0} error", "{0} errors", numErrors, numErrors);
            }
            String title = "";
            if (errorText != null) {
                title += errorText;
            }
            if (warningText != null){
                if (title.length() > 0){
                    title += "/";
                }
                title += warningText;
            }
            if (title.length() == 0){
                title = tr("no issues");
            }
            tpEditors.setTitleAt(2, title);
            tpEditors.setEnabledAt(2, numWarnings + numErrors > 0);
        }       
    }
    
    /**
     * <p>Listens to changes of the preference {@link PreferenceKeys#ROAD_SIGNS}
     * and refreshes the set of road icons.</p>
     * 
     * <p>Listens to changes of the preference {@link PreferenceKeys#SHOW_VIAS_IN_BASIC_EDITOR}
     * and toggles the visibility of the list of via-objects in the Basic
     * Editor.</p>
     *
     */
    class PreferenceChangeHandler implements PreferenceChangedListener {        
        public void refreshIconSet() {
            pnlBasicEditor.initIconSetFromPreferences(Main.pref);
        }
        
        public void preferenceChanged(PreferenceChangeEvent evt) {          
            if (evt.getKey().equals(PreferenceKeys.ROAD_SIGNS)){
                refreshIconSet();
            } else if (evt.getKey().equals(PreferenceKeys.SHOW_VIAS_IN_BASIC_EDITOR)) {
                pnlBasicEditor.initViasVisibilityFromPreferences(Main.pref);
            }           
        }
    }
}
