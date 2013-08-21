package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.turnrestrictions.TurnRestrictionBuilder;
import org.openstreetmap.josm.plugins.turnrestrictions.list.TurnRestrictionCellRenderer;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * TurnRestrictionSelectionPopupPanel is displayed in a {@link Popup} to select whether
 * the user wants to create a new turn restriction or whether he wants to edit one
 * of a list of turn restrictions.
 *
 */
public class TurnRestrictionSelectionPopupPanel extends JPanel{
    //static private final Logger logger = Logger.getLogger(TurnRestrictionSelectionPopupPanel.class.getName());

    /** the parent popup */
    private Popup parentPopup;
    /** the button for creating a new turn restriction */
    private JButton btnNew;
    /** the table with the turn restrictions which can be edited */
    private JTable tblTurnRestrictions; 
    private OsmDataLayer layer;
    
    /**
     * Replies the collection of turn restrictions the primitives in {@code primitives}
     * currently participate in.
     * 
     * @param primitives the collection of primitives. May be null.
     * @return the collection of "parent" turn restrictions. 
     */
    static public Collection<Relation> getTurnRestrictionsParticipatingIn(Collection<OsmPrimitive> primitives){
        HashSet<Relation> ret = new HashSet<Relation>();
        if (primitives == null) return ret;
        for (OsmPrimitive p: primitives){
            if (p == null) continue;
            if (p.isDeleted() || !p.isVisible()) continue;
            for (OsmPrimitive parent: p.getReferrers()){
                if (!(parent instanceof Relation)) continue;
                String type = parent.get("type");
                if (type == null || ! type.equals("restriction")) continue;
                if (parent.isDeleted() || ! parent.isVisible()) continue;
                ret.add((Relation)parent);
            }
        }
        return ret;
    }
    
    /**
     * Registers 1..9 shortcuts for the first 9 turn restrictions to
     * edit
     * 
     * @param editCandiates the edit candidates 
     */
    protected void registerEditShortcuts(Collection<Relation> editCandiates){
        for(int i=1; i <= Math.min(editCandiates.size(),9);i++){
            int vkey = 0;
            switch(i){
            case 1: vkey = KeyEvent.VK_1; break;
            case 2: vkey = KeyEvent.VK_2; break;
            case 3: vkey = KeyEvent.VK_3; break;
            case 4: vkey = KeyEvent.VK_4; break;
            case 5: vkey = KeyEvent.VK_5; break;
            case 6: vkey = KeyEvent.VK_6; break;
            case 7: vkey = KeyEvent.VK_7; break;
            case 8: vkey = KeyEvent.VK_8; break;
            case 9: vkey = KeyEvent.VK_9; break;
            }
            registerKeyboardAction(new EditTurnRestrictionAction(i-1), KeyStroke.getKeyStroke(vkey,0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        }
    }
    /**
     * Builds the panel with the turn restrictions table 
     * 
     * @param editCandiates the list of edit candiates  
     * @return the panel 
     */
    protected JPanel buildTurnRestrictionTablePanel(Collection<Relation> editCandiates) {
        tblTurnRestrictions = new JTable(new TurnRestrictionTableModel(editCandiates), new TurnRestrictionTableColumnModel());
        tblTurnRestrictions.setColumnSelectionAllowed(false);
        tblTurnRestrictions.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        TurnRestrictionCellRenderer renderer = new TurnRestrictionCellRenderer();
        tblTurnRestrictions.setRowHeight((int)renderer.getPreferredSize().getHeight());
        
        // create a scroll pane, remove the table header 
        JScrollPane pane = new JScrollPane(tblTurnRestrictions);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tblTurnRestrictions.setTableHeader(null);
        pane.setColumnHeaderView(null);
        
        // respond to double click and ENTER 
        EditSelectedTurnRestrictionAction action = new EditSelectedTurnRestrictionAction();
        tblTurnRestrictions.addMouseListener(action);
        tblTurnRestrictions.registerKeyboardAction(action, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), WHEN_FOCUSED);
        
        tblTurnRestrictions.addFocusListener(new FocusHandler());
        
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.add(pane, BorderLayout.CENTER);
        
        pnl.setBackground(UIManager.getColor("Table.background"));
        pane.setBackground(UIManager.getColor("Table.background"));
        return pnl;     
    }
    
    /**
     * Builds the panel 
     * 
     * @param editCandiates the edit candidates
     */
    protected void build(Collection<Relation> editCandiates) {
        setLayout(new BorderLayout());
        add(btnNew = new JButton(new NewAction()), BorderLayout.NORTH);
        btnNew.setFocusable(true);
        btnNew.registerKeyboardAction(btnNew.getAction(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), WHEN_FOCUSED);
        registerKeyboardAction(new CloseAction(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        registerKeyboardAction(btnNew.getAction(), KeyStroke.getKeyStroke(KeyEvent.VK_N,0), WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        btnNew.addFocusListener(new FocusHandler());
        
        if (editCandiates != null && ! editCandiates.isEmpty()) {
            add(buildTurnRestrictionTablePanel(editCandiates), BorderLayout.CENTER);    
            registerEditShortcuts(editCandiates);
        }
        
        setBackground(UIManager.getColor("Table.background"));      
    }

    
    /**
     * Creates the panel
     * 
     * @param layer the reference OSM data layer. Must not be null.
     * @throws IllegalArgumentException thrown if {@code layer} is null
     */
    public TurnRestrictionSelectionPopupPanel(OsmDataLayer layer) throws IllegalArgumentException {
        CheckParameterUtil.ensureParameterNotNull(layer, "layer");
        this.layer = layer;
        build(getTurnRestrictionsParticipatingIn(layer.data.getSelected()));
    }
    
    /**
     * Creates the panel
     * 
     * @param layer the reference OSM data layer. Must not be null.
     * @param editCandidates a collection of turn restrictions as edit candidates. May be null. 
     * @throws IllegalArgumentException thrown if {@code layer} is null
     */
    public TurnRestrictionSelectionPopupPanel(OsmDataLayer layer, Collection<Relation> editCandiates) {
        CheckParameterUtil.ensureParameterNotNull(layer, "layer");
        this.layer = layer;
        build(editCandiates);
    }
    
    /**
     * Launches a popup with this panel as content 
     */
    public void launch(){
        PointerInfo info = MouseInfo.getPointerInfo();
        Point pt = info.getLocation();
        parentPopup = PopupFactory.getSharedInstance().getPopup(Main.map.mapView,this, pt.x, pt.y);
        parentPopup.show();
        btnNew.requestFocusInWindow();
    }

    @Override
    public Dimension getPreferredSize() {
        int bestheight = (int)btnNew.getPreferredSize().getHeight()
              + Math.min(2, tblTurnRestrictions.getRowCount()) * tblTurnRestrictions.getRowHeight()
              + 5;
        return new Dimension(300, bestheight);
    }
    
    /* --------------------------------------------------------------------------------------- */
    /* inner classes                                                                           */
    /* --------------------------------------------------------------------------------------- */
    
    private class NewAction extends AbstractAction {
        public NewAction() {
            putValue(NAME, tr("Create new turn restriction"));
            putValue(SHORT_DESCRIPTION, tr("Launch the turn restriction editor to create a new turn restriction"));
            putValue(SMALL_ICON, ImageProvider.get("new"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
        }

        public void actionPerformed(ActionEvent e) {
            Relation tr = new TurnRestrictionBuilder().buildFromSelection(layer);
            TurnRestrictionEditor editor = new TurnRestrictionEditor(Main.map.mapView,layer,tr);
            TurnRestrictionEditorManager.getInstance().positionOnScreen(editor);
            TurnRestrictionEditorManager.getInstance().register(layer, tr, editor);
            if (parentPopup != null){
                parentPopup.hide();
            }
            editor.setVisible(true);
        }
    }
    
    abstract private  class AbstractEditTurnRestrictionAction extends AbstractAction {
        protected void launchEditor(Relation tr){
            TurnRestrictionEditorManager manager = TurnRestrictionEditorManager.getInstance();
            TurnRestrictionEditor editor = manager.getEditorForRelation(layer, tr);
            if (parentPopup != null){
                parentPopup.hide();
            }
            if (editor != null) {
                editor.setVisible(true);
                editor.toFront();
            } else {
                editor = new TurnRestrictionEditor(Main.map.mapView, layer,tr);
                manager.positionOnScreen(editor);
                manager.register(layer, tr,editor);
                editor.setVisible(true);
            }
        }
    }
    
    private class EditTurnRestrictionAction extends AbstractEditTurnRestrictionAction {
        private int idx;
        
        public EditTurnRestrictionAction(int idx){
            this.idx = idx;
        }
        
        public void actionPerformed(ActionEvent e) {
            Relation tr = (Relation)tblTurnRestrictions.getModel().getValueAt(idx, 1);
            launchEditor(tr);
        }       
    }
    
    private class EditSelectedTurnRestrictionAction extends AbstractEditTurnRestrictionAction implements MouseListener{
        public void editTurnRestrictionAtRow(int row){
            if (row < 0) return;
            Relation tr = (Relation)tblTurnRestrictions.getModel().getValueAt(row, 1);
            launchEditor(tr);
        }
        public void actionPerformed(ActionEvent e) {
            int row = tblTurnRestrictions.getSelectedRow();
            editTurnRestrictionAtRow(row);
        }
        public void mouseClicked(MouseEvent e) {
            if (!(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2)) return;
            int row = tblTurnRestrictions.rowAtPoint(e.getPoint());
            if (row < 0) return;
            editTurnRestrictionAtRow(row);          
        }
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
    }
    
    private class CloseAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (parentPopup != null){
                parentPopup.hide();
            }
        }       
    }
    
    private static class TurnRestrictionTableModel extends AbstractTableModel {
        private final ArrayList<Relation> turnrestrictions = new ArrayList<Relation>();

        public TurnRestrictionTableModel(Collection<Relation> turnrestrictions){
            this.turnrestrictions.clear();
            if (turnrestrictions != null){
                this.turnrestrictions.addAll(turnrestrictions);
            }
            fireTableDataChanged();
        }
        
        public int getRowCount() {
            return turnrestrictions.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch(columnIndex){
            case 0:
                if (rowIndex <=8 ) {
                    return Integer.toString(rowIndex+1);
                } else {
                    return "";
                }
            case 1:
                return turnrestrictions.get(rowIndex);
            }
            // should not happen
            return null;
        }
    }
    
    private static class TurnRestrictionTableColumnModel extends DefaultTableColumnModel {      
        public TurnRestrictionTableColumnModel() {          
            // the idx column
            TableColumn col = new TableColumn(0);           
            col.setResizable(false);
            col.setWidth(50);
            addColumn(col);
            
            // the column displaying turn restrictions 
            col = new TableColumn(1);           
            col.setResizable(false);
            col.setPreferredWidth(400);
            col.setCellRenderer(new TurnRestrictionCellRenderer());
            addColumn(col);         
        }
    }
    
    private class FocusHandler extends FocusAdapter {       
        @Override
        public void focusLost(FocusEvent e) {
            // if we loose the focus to a component outside of the popup panel
            // we hide the popup            
            if (e.getOppositeComponent() == null ||!SwingUtilities.isDescendingFrom(e.getOppositeComponent(), TurnRestrictionSelectionPopupPanel.this)) {
                if (parentPopup != null){
                    parentPopup.hide();
                }
            }
        }
    }
}
