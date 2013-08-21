package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import org.openstreetmap.josm.gui.help.HelpUtil;
import org.openstreetmap.josm.gui.tagging.TagEditorPanel;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * AdvancedEditorPanel consists of two advanced editors for parts of the turn
 * restriction data: a tag editor and a relation member editor.
 */
public class AdvancedEditorPanel extends JPanel {
    //private static final Logger logger = Logger.getLogger(AdvancedEditorPanel.class.getName());

    private TurnRestrictionEditorModel model;
    private TagEditorPanel pnlTagEditor;
    private JPanel pnlRelationMemberEditor;
    private JTable tblRelationMemberEditor;
    private JSplitPane spEditors;

    /**
     * Creates the panel with the tag editor
     *
     * @return
     */
    protected JPanel buildTagEditorPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        HtmlPanel msg = new HtmlPanel();
        msg.setText("<html><body>" +
                tr("In the following table you can edit the <strong>raw tags</strong>"
              + " of the OSM relation representing this turn restriction.")
              + "</body></html>"
        );
        pnl.add(msg, BorderLayout.NORTH);
        pnlTagEditor = new TagEditorPanel(model.getTagEditorModel(), null);
        pnlTagEditor.initAutoCompletion(model.getLayer());
        pnl.add(pnlTagEditor, BorderLayout.CENTER);
        return pnl;
    }

    /**
     * Builds the panel with the table for editing relation members
     *
     * @return
     */
    protected JPanel buildMemberEditorPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        HtmlPanel msg = new HtmlPanel();
        msg.setText("<html><body>"
              + tr("In the following table you can edit the <strong>raw members</strong>"
              + " of the OSM relation representing this turn restriction.") + "</body></html>"
        );
        pnl.add(msg, BorderLayout.NORTH);

        tblRelationMemberEditor = new RelationMemberTable(model);
        JScrollPane pane = new JScrollPane(tblRelationMemberEditor);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pnl.add(pane);
        return pnl;
    }

    /**
     * Creates the main split panel
     * @return
     */
    protected JSplitPane buildSplitPane() {
        spEditors = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        spEditors.setTopComponent(buildTagEditorPanel());
        spEditors.setBottomComponent(buildMemberEditorPanel());
        spEditors.setOneTouchExpandable(false);
        spEditors.setDividerSize(5);
        spEditors.addHierarchyListener(new SplitPaneDividerInitializer());
        return spEditors;
    }

    /**
     * Builds the user interface
     */
    protected void build() {
        setLayout(new BorderLayout());
        add(buildSplitPane(), BorderLayout.CENTER);
    }

    /**
     * Creates the advanced editor
     *
     * @param model the editor model. Must not be null.
     * @throws IllegalArgumentException thrown if model is null
     */
    public AdvancedEditorPanel(TurnRestrictionEditorModel model) throws IllegalArgumentException{
        CheckParameterUtil.ensureParameterNotNull(model, "model");
        this.model = model;
        build();
        HelpUtil.setHelpContext(this, HelpUtil.ht("/Plugin/TurnRestrictions#AdvancedEditor"));
    }

    /**
     * Initializes the divider location when the components becomes visible the
     * first time
     */
    class SplitPaneDividerInitializer implements HierarchyListener {
        public void hierarchyChanged(HierarchyEvent e) {
            if (isShowing()) {
                spEditors.setDividerLocation(0.5);
                spEditors.removeHierarchyListener(this);
            }
        }
    }
}
