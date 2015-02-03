package org.openstreetmap.josm.plugins.utilsplugin2.multitagger;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.search.SearchAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingTextField;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionList;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;
import org.openstreetmap.josm.gui.util.HighlightHelper;
import org.openstreetmap.josm.gui.util.TableHelper;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;
import org.openstreetmap.josm.gui.widgets.PopupMenuLauncher;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.WindowGeometry;

/**
 * Dialog for editing multiple object tags
 */
public class MultiTagDialog extends ExtendedDialog implements SelectionChangedListener {

    private final MultiTaggerTableModel tableModel = new MultiTaggerTableModel();
    private final JTable tbl;

    private final HighlightHelper highlightHelper = new HighlightHelper();
    private final HistoryComboBox cbTagSet = new HistoryComboBox();
    private List<OsmPrimitive> currentSelection;
    
    private static final String HISTORY_KEY = "utilsplugin2.multitaghistory";
    String defaultHistory[] = {"addr:street, addr:housenumber, building, ${area}",
        "highway, name, ${id}, ${length}",
        "name name:en name:ru name:de"};
    
    public MultiTagDialog() {
        super(Main.parent,  tr("Edit tags"), new String[]{tr("Ok"), tr("Cancel")}, false);
        JPanel pnl = new JPanel(new GridBagLayout());
        tbl = createTable();
        
        cbTagSet.addItemListener(tagSetChanger);
        cbTagSet.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
           .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "applyTagSet");
        cbTagSet.getActionMap().put("applyTagSet", tagSetChanger);
        
        tbl.addMouseListener(new PopupMenuLauncher(createPopupMenu()));
        
        pnl.add(cbTagSet,GBC.std().fill(GBC.HORIZONTAL));
        pnl.add(new JButton(new DeleteFromHistoryAction()),GBC.std());
        pnl.add(new JButton(new FindMatchingAction()),GBC.std());
        final JToggleButton jt = new JToggleButton("", ImageProvider.get("restart"), true);
        jt.setToolTipText(tr("Sync with JOSM selection"));
        jt.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent e) {
                tableModel.setWatchSelection(jt.isSelected());
            };
        });
        pnl.add(jt,GBC.eol());
        

        pnl.add(createTypeFilterPanel(), GBC.eol().fill(GBC.HORIZONTAL));
        pnl.add(tbl.getTableHeader(),GBC.eop().fill(GBC.HORIZONTAL));
        
        pnl.add(new JScrollPane(tbl), GBC.eol().fill(GBC.BOTH));
        setContent(pnl);
        setDefaultButton(-1);
        loadHistory();
        
        WindowGeometry defaultGeometry = WindowGeometry.centerInWindow(Main.parent, new Dimension(500, 500));
        setRememberWindowGeometry(getClass().getName() + ".geometry", defaultGeometry);
    }

    private JTable createTable() {
        JTable t = new JTable(tableModel);
        tableModel.setTable(t);
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        t.addMouseListener(tableMouseAdapter);
        t.setRowSelectionAllowed(true);
        t.setColumnSelectionAllowed(true);
        t.setDefaultRenderer(OsmPrimitiveType.class, new PrimitiveTypeIconRenderer());
        t.setDefaultRenderer(String.class, new ColoredRenderer());
        t.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        t.getSelectionModel().addListSelectionListener(selectionListener);
        return t;
    }

    private JPanel createTypeFilterPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        for (final OsmPrimitiveType type: OsmPrimitiveType.values()) {
            final JToggleButton jt = new JToggleButton("", ImageProvider.get(type), true);
            jt.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (jt.isSelected()) tableModel.shownTypes.add(type); else tableModel.shownTypes.remove(type);
                    tableModel.updateData(Main.main.getCurrentDataSet().getSelected());
                };
            });
            ImageProvider.get(type);
            p.add(jt);
        }
        return p;
    }

    private void loadHistory() {
        List<String> cmtHistory = new LinkedList<>(
                Main.pref.getCollection(HISTORY_KEY, Arrays.asList(defaultHistory)));
        Collections.reverse(cmtHistory);
        cbTagSet.setPossibleItems(cmtHistory);
        String s = cmtHistory.get(cmtHistory.size()-1);
        cbTagSet.setText(s);
        specifyTagSet(s);
    }

    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        highlightHelper.clear();
        tbl.getSelectionModel().removeListSelectionListener(selectionListener);
        super.buttonAction(buttonIndex, evt); 
    }
    
    @Override
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
        tableModel.selectionChanged(newSelection);
    }
    
    /*private OsmPrimitive getSelectedPrimitive() {
        int idx = tbl.getSelectedRow();
        if (idx>=0) {
            return tableModel.getPrimitiveAt(tbl.convertRowIndexToModel(idx));
        } else {
            return null;
        }
    }*/
    
   private final MouseAdapter tableMouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount()>1 && Main.isDisplayingMapView()) {
                AutoScaleAction.zoomTo(currentSelection);
            }
        }
        
    };
    private final ListSelectionListener selectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            currentSelection = getSelectedPrimitives();
            if (currentSelection != null && Main.isDisplayingMapView() ) {
                if (highlightHelper.highlightOnly(currentSelection)) {
                    Main.map.mapView.repaint();
                }
            }
        }
    };
    
    public List<OsmPrimitive> getSelectedPrimitives() {
        ArrayList<OsmPrimitive> sel = new ArrayList<>(100);
        for (int idx: tbl.getSelectedRows()) {
            sel.add(tableModel.getPrimitiveAt(tbl.convertRowIndexToModel(idx)));
        }
        return sel;
    }
    
    private final TagSetChanger tagSetChanger = new TagSetChanger();

    private void initAutocompletion() {
        OsmDataLayer l = Main.main.getEditLayer();
        AutoCompletionManager autocomplete = l.data.getAutoCompletionManager();
        for (int i=0; i<tableModel.mainTags.length; i++) {
                if (tableModel.isSpecialTag[i]) continue;
                AutoCompletingTextField tf = new AutoCompletingTextField(0, false);
                AutoCompletionList acList = new AutoCompletionList();
                autocomplete.populateWithTagValues(acList, tableModel.mainTags[i]);
                tf.setAutoCompletionList(acList);
                tbl.getColumnModel().getColumn(i+1).setCellEditor(tf);
        }
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new AbstractAction(tr("Zoom to objects"), ImageProvider.get("dialogs/autoscale", "selection")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Main.isDisplayingMapView()) {
                    AutoScaleAction.zoomTo(currentSelection);
                }
            }
        });
        menu.add(new AbstractAction(tr("Select"), ImageProvider.get("dialogs", "select")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.main.getCurrentDataSet().setSelected(getSelectedPrimitives());
            }
        });
        menu.add(new AbstractAction(tr("Remove tag"), ImageProvider.get("dialogs", "delete")){
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.setAutoCommit(false);
                for (int c: tbl.getSelectedColumns()) {
                    for (int r: tbl.getSelectedRows()) {
                        tableModel.setValueAt("", tbl.convertRowIndexToModel(r), tbl.convertColumnIndexToModel(c));
                    }
                }
                tableModel.commit(tr("Delete tags from multiple objects"));
                tableModel.setAutoCommit(true);
            }
        });
        menu.add(new AbstractAction(tr("Duplicate tags from the first"), ImageProvider.get("copy")){
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.setAutoCommit(false);
                for (int c: tbl.getSelectedColumns()) {
                    if (c==0 || tableModel.isSpecialTag[c-1]) continue;
                    boolean first = true;
                    String value = "";
                    for (int r: tbl.getSelectedRows()) {
                        if (first) {
                            value = (String) tableModel.getValueAt(tbl.convertRowIndexToModel(r), tbl.convertColumnIndexToModel(c));
                        }
                        first=false;
                        tableModel.setValueAt(value, tbl.convertRowIndexToModel(r), tbl.convertColumnIndexToModel(c));
                    }
                }
                tableModel.commit(tr("Set tags for multiple objects"));
                tableModel.setAutoCommit(true);
            }
        });
        return menu;
    }

    private static class PrimitiveTypeIconRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {
                ((JLabel) c).setIcon(ImageProvider.get((OsmPrimitiveType) value));
                ((JLabel) c).setText("");
            }
            return c;
        }
    }

    private class DeleteFromHistoryAction extends AbstractAction {
        public DeleteFromHistoryAction() {
            super("", ImageProvider.get("dialogs","delete"));
            putValue(SHORT_DESCRIPTION, tr("Delete from history"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String txt = cbTagSet.getText();
            System.out.println(txt);
            List<String> history = cbTagSet.getHistory();
            history.remove(txt);
            if (history.isEmpty()) {
                history = Arrays.asList(defaultHistory);
            }
            Main.pref.putCollection(HISTORY_KEY, history);
            loadHistory();
        }
    }
   
    private class FindMatchingAction extends AbstractAction {
        public FindMatchingAction() {
            super("", ImageProvider.get("dialogs","search"));
            putValue(SHORT_DESCRIPTION, tr("Find primitives with these tags"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SearchAction.search(tableModel.getSearchExpression(), SearchAction.SearchMode.replace);
        }
    }
   
    private class TagSetChanger extends AbstractAction implements ItemListener {
        String oldTags;
        
 
        @Override
        public void itemStateChanged(ItemEvent e) {
            // skip text-changing enevts, we need only combobox-selecting ones
            if (cbTagSet.getSelectedIndex()<0) return;
            actionPerformed(null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String s = cbTagSet.getText();
            if (s==null || s.isEmpty() || s.equals(oldTags)) return;
            oldTags = s;
            cbTagSet.addCurrentItemToHistory();
            Main.pref.putCollection(HISTORY_KEY, cbTagSet.getHistory());
            specifyTagSet(s);
        }

    };
    
    private void specifyTagSet(String s) {
        Main.info("Multitagger tags="+s);
        tableModel.setupColumnsFromText(s);

        tbl.createDefaultColumnsFromModel();
        tbl.setAutoCreateRowSorter(true);

        tbl.getColumnModel().getColumn(0).setMaxWidth(20);
        for (int i=1; i<tableModel.getColumnCount(); i++) {
            TableHelper.adjustColumnWidth(tbl, i, 100);
        }
        initAutocompletion();
        tableModel.fireTableDataChanged();
    }
    
    class ColoredRenderer extends DefaultTableCellRenderer {
        private final Color highlightColor = 
                Main.pref.getColor( marktr("Multitag Background: highlight"),
                        new Color(255,255,200));
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean
                isSelected, boolean hasFocus, int row, int column) {
            int row1 = tbl.convertRowIndexToModel(row);
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            if (tbl.isRowSelected(row1) && !tbl.isColumnSelected(column)) {
                label.setBackground(highlightColor);
            } else {
                if (isSelected) {
                    label.setBackground(Main.pref.getUIColor("Table.selectionBackground"));
                } else {
                    label.setBackground(Main.pref.getUIColor("Table.background"));
                }
            }
            return label;
        }
    }
}