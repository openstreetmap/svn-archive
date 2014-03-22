package org.openstreetmap.josm.plugins.utilsplugin2.multitagger;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import static javax.swing.Action.SHORT_DESCRIPTION;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.actions.search.SearchAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.preferences.StringProperty;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.tagging.TagCellEditor;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingTextField;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionList;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;
import org.openstreetmap.josm.gui.util.HighlightHelper;
import org.openstreetmap.josm.gui.util.TableHelper;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;
import org.openstreetmap.josm.tools.GBC;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.WindowGeometry;

/**
 * Dialog fo redinitg multible object tags
 */
public class MultiTagDialog extends ExtendedDialog implements SelectionChangedListener {

    private final MultiTaggerTableModel tableModel = new MultiTaggerTableModel();
    private final JTable tbl = new JTable(tableModel);
    //
    private final HighlightHelper highlightHelper = new HighlightHelper();
    private final HistoryComboBox tagSetSelector = new HistoryComboBox();
    
    private static final String HISTORY_KEY = "utilsplugin2.multitaghistory";
    String defaultHistory[] = {"addr:street, addr:housenumber, building",
        "highway, name, ${id}, ${length}, ${type}",
        "name name:en name:ru name:de"};
    private final StringProperty LAST_TAGS = new StringProperty("utilsplugin2.multitag.last", defaultHistory[0]);
    TagCellEditor cellEditor;
    
            
    public MultiTagDialog() {
        super(Main.parent,  tr("Edit tags"), new String[]{tr("Ok"), tr("Cancel")}, false);
        JPanel pnl = new JPanel(new GridBagLayout());
        tbl.setFillsViewportHeight(true);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.addMouseListener(tableMouseAdapter);
        tbl.setRowSelectionAllowed(true);
        tbl.setColumnSelectionAllowed(true);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        loadHistory();
        
        tagSetSelector.addItemListener(tagSetChanger);
        tagSetSelector.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
           .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "applyTagSet");
        tagSetSelector.getActionMap().put("applyTagSet", tagSetChanger);
        
        tagSetChanger.itemStateChanged(null);
        pnl.add(tagSetSelector,GBC.std().fill(GBC.HORIZONTAL));
        pnl.add(new JButton(new DeleteFromHistoryAction()),GBC.std());
        pnl.add(new JButton(new FindMatchingAction()),GBC.eol());
        pnl.add(tbl.getTableHeader(),GBC.eop().fill(GBC.HORIZONTAL));

        pnl.add(new JScrollPane(tbl), GBC.eol().fill(GBC.BOTH));
        tbl.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tbl.getSelectionModel().addListSelectionListener(selectionListener);
        setContent(pnl);
        setDefaultButton(-1);
        
        WindowGeometry defaultGeometry = WindowGeometry.centerInWindow(Main.parent, new Dimension(500, 500));
        setRememberWindowGeometry(getClass().getName() + ".geometry", defaultGeometry);
        

    }

    private void loadHistory() {
        List<String> cmtHistory = new LinkedList<String>(
                Main.pref.getCollection(HISTORY_KEY, Arrays.asList(defaultHistory)));
        Collections.reverse(cmtHistory);
        tagSetSelector.setPossibleItems(cmtHistory);
        tagSetSelector.setText(LAST_TAGS.get());
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
    
    private OsmPrimitive getSelectedPrimitive() {
        int idx = tbl.getSelectedRow();
        if (idx>=0) {
            return tableModel.getPrimitiveAt(tbl.convertRowIndexToModel(idx));
        } else {
            return null;
        }
    }
    
    private final MouseAdapter tableMouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                AutoScaleAction.zoomTo(Collections.singletonList(getSelectedPrimitive()));
            }
        }
        
    };
    private final ListSelectionListener selectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            OsmPrimitive p = getSelectedPrimitive();
            if (p != null && Main.isDisplayingMapView() ) {
                if (highlightHelper.highlightOnly(p)) {
                    Main.map.mapView.repaint();
                }
            }
        }
    };
    
    public void setAutoCompletion(boolean enable){
        if (!enable) {
            
        }
            
        OsmDataLayer l = Main.main.getEditLayer();
        AutoCompletionManager autocomplete = l.data.getAutoCompletionManager();
        AutoCompletionList acList = new AutoCompletionList();
          
//        TagCellEditor editor = ((TagCellEditor) tagTable.getColumnModel().getColumn(0).getCellEditor());
//        editor.setAutoCompletionManager(autocomplete);
//        editor.setAutoCompletionList(acList);
//        editor = ((TagCellEditor) tagTable.getColumnModel().getColumn(1).getCellEditor());
//        editor.setAutoCompletionManager(autocomplete);
//        editor.setAutoCompletionList(acList);
    }
    
    private final TagSetChanger tagSetChanger = new TagSetChanger();

    private void initAutocompletion() {
        OsmDataLayer l = Main.main.getEditLayer();
        AutoCompletionManager autocomplete = l.data.getAutoCompletionManager();
        for (int i=0; i<tableModel.mainTags.length; i++) {
                if (tableModel.isSpecialTag[i]) continue;
                AutoCompletingTextField tf = new AutoCompletingTextField();
                AutoCompletionList acList = new AutoCompletionList();
                autocomplete.populateWithTagValues(acList, tableModel.mainTags[i]);
                tf.setAutoCompletionList(acList);
                tbl.getColumnModel().getColumn(i).setCellEditor(tf);
        }
    }

    private class DeleteFromHistoryAction extends AbstractAction {
        public DeleteFromHistoryAction() {
            super("", ImageProvider.get("dialogs","delete"));
            putValue(SHORT_DESCRIPTION, tr("Delete from history"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String txt = tagSetSelector.getText();
            System.out.println(txt);
            List<String> history = tagSetSelector.getHistory();
            history.remove(txt);
            if (history.isEmpty()) {
                history = Arrays.asList(defaultHistory);
            }
                
            Main.pref.putCollection(HISTORY_KEY, history);
            if (!history.isEmpty()) {
                LAST_TAGS.put(history.get(0));
            } 
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
            if (tagSetSelector.getSelectedIndex()<0) return;
            actionPerformed(null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String s = tagSetSelector.getText();
            if (s==null || s.isEmpty() || s.equals(oldTags)) return;
            oldTags = s;
            Main.info("Multitagger tags="+s);
            tagSetSelector.addCurrentItemToHistory();
            Main.pref.putCollection(HISTORY_KEY, tagSetSelector.getHistory());
            LAST_TAGS.put(tagSetSelector.getText());

            tableModel.setupColumnsFromText(s);
           
            tbl.createDefaultColumnsFromModel();
            tbl.setAutoCreateRowSorter(true);
            for (int i=0; i<tableModel.getColumnCount(); i++) {
                TableHelper.adjustColumnWidth(tbl, i, 100);
            }
            initAutocompletion();
            tableModel.fireTableDataChanged();
        }
    };
    
}