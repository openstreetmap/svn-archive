package org.openstreetmap.josm.plugins.tageditor.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;

import org.openstreetmap.josm.gui.tagging.TagTable;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionList;
import org.openstreetmap.josm.plugins.tageditor.ac.IAutoCompletionListListener;


/**
 * TagEditor is a {@link JPanel} which consists of a two sub panels:
 * <ul>
 *   <li>a small area in which a drop-down box with a list of presets is displayed.
 *       Two buttons allow to highlight and remove a presets respectively.</li>
 *   <li>the main table with the tag names and tag values
 * </ul>
 * 
 *  This component depends on a couple of other components which have to be
 *  injected with the respective setter methods:
 *  <ul>
 *    <li>an instance of {@link TagEditorModel} - use {@see #setTagEditorModel(TagEditorModel)}</li>
 *    <li>an instance of {@link AutoCompletionCache} - inject it using {@see #setAutoCompletionCache(AutoCompletionCache)}.
 *      The table cell editor used by the table in this component uses the AutoCompletionCache in order to
 *      build up a list of auto completion values from the current data set</li>
 *    <li>an instance of {@link AutoCompletionList} - inject it using {@see #setAutoCompletionList(AutoCompletionList)}.
 *      The table cell editor used by the table in this component uses the AutoCompletionList
 *      to build up a list of auto completion values from the set of  standardized
 *      OSM tags</li>
 *  </ul>O
 *
 *  Typical usage is therefore:
 *  <pre>
 *     AutoCompletionList autoCompletionList = .... // init the autocompletion list
 *     AutoCompletionCache autoCompletionCache = ... // init the auto completion cache
 *     TagEditorModel model = ... // init the tag editor model
 * 
 *     TagEditor tagEditor = new TagEditor();
 *     tagEditor.setTagEditorModel(model);
 *     tagEditor.setAutoCompletionList(autoCompletionList);
 *     tagEditor.setAutoCompletionCache(autoCompletionCache);
 *  </pre>
 */
public class TagEditor extends JPanel implements IAutoCompletionListListener {

    private static final Logger logger = Logger.getLogger(TagEditor.class.getName());

    private TagEditorModel tagEditorModel;
    private TagTable tblTagEditor;
    private PresetManager presetManager;
    
     /**
     * builds the panel with the button row
     *
     * @return the panel
     */
    protected JPanel buildButtonsPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));

        // add action
        JButton btn;
        pnl.add(btn = new JButton(tblTagEditor.getAddAction()));
        btn.setMargin(new Insets(0,0,0,0));
        tblTagEditor.addComponentNotStoppingCellEditing(btn);

        // delete action
        pnl.add(btn = new JButton(tblTagEditor.getDeleteAction()));
        btn.setMargin(new Insets(0,0,0,0));
        tblTagEditor.addComponentNotStoppingCellEditing(btn);
        return pnl;
    }
    
    public void addComponentNotStoppingCellEditing(Component c) {
        tblTagEditor.addComponentNotStoppingCellEditing(c);
    }
    
    /**
     * builds the GUI
     */
    protected JPanel buildTagEditorPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());

        DefaultListSelectionModel rowSelectionModel = new DefaultListSelectionModel();
        DefaultListSelectionModel colSelectionModel = new DefaultListSelectionModel();
        
        tagEditorModel = new TagEditorModel(rowSelectionModel,colSelectionModel);
        
        // build the scrollable table for editing tag names and tag values
        //
        tblTagEditor = new TagTable(tagEditorModel);
        tblTagEditor.setTagCellEditor(new TagSpecificationAwareTagCellEditor());
        TableCellRenderer renderer = new TableCellRenderer();
        tblTagEditor.getColumnModel().getColumn(0).setCellRenderer(renderer);
        tblTagEditor.getColumnModel().getColumn(1).setCellRenderer(renderer);

        final JScrollPane scrollPane = new JScrollPane(tblTagEditor);
        JPanel pnlTagTable = new JPanel(new BorderLayout());
        pnlTagTable.add(scrollPane, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();

        // -- buttons panel
        //
        gc.fill = GridBagConstraints.VERTICAL;
        gc.weightx = 0.0;
        gc.weighty = 1.0;
        gc.anchor = GridBagConstraints.NORTHWEST;
        pnl.add(buildButtonsPanel(),gc);

        // -- the panel with the editor table
        //
        gc.gridx = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.anchor = GridBagConstraints.CENTER;
        pnl.add(pnlTagTable,gc);
        
        return pnl;
    }
    
    /**
     * builds the GUI
     * 
     */
    protected void build() {
        setLayout(new BorderLayout());
        
        add(buildTagEditorPanel(), BorderLayout.CENTER);

        // build the preset manager which shows a list of applied presets
        //
        presetManager = new PresetManager();
        presetManager.setModel(tagEditorModel);
        add(presetManager, BorderLayout.NORTH);
    }

    /**
     * constructor
     */
    public TagEditor() {
        build();
    }

    /**
     * replies the tag editor model
     * @return the tag editor model
     */
    public TagEditorModel getTagEditorModel() {
        return tagEditorModel;
    }
    
    public void clearSelection() {
        tblTagEditor.getSelectionModel().clearSelection();
    }

    public void stopEditing() {
        TableCellEditor editor = tblTagEditor.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }
    }
    
    public void setAutoCompletionList(AutoCompletionList autoCompletionList) {
        tblTagEditor.setAutoCompletionList(autoCompletionList);
    }

    public void setAutoCompletionManager(AutoCompletionManager autocomplete) {
        tblTagEditor.setAutoCompletionManager(autocomplete);
    }

    public void autoCompletionItemSelected(String item) {
        logger.info("autocompletion item selected ...");
        TagSpecificationAwareTagCellEditor editor = (TagSpecificationAwareTagCellEditor)tblTagEditor.getCellEditor();
        if (editor != null) {
            editor.autoCompletionItemSelected(item);
        }
    }

    public void requestFocusInTopLeftCell() {
        tblTagEditor.requestFocusInCell(0,0);
    }
    
    public TagEditorModel getModel() {
        return tagEditorModel;
    }
}
