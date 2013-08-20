// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tageditor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionList;
import org.openstreetmap.josm.plugins.tageditor.ac.AutoCompletionListViewer;
import org.openstreetmap.josm.plugins.tageditor.editor.TagEditor;
import org.openstreetmap.josm.plugins.tageditor.editor.TagEditorModel;
import org.openstreetmap.josm.plugins.tageditor.preset.Item;
import org.openstreetmap.josm.plugins.tageditor.preset.ui.IPresetSelectorListener;
import org.openstreetmap.josm.plugins.tageditor.preset.ui.TabularPresetSelector;
import org.openstreetmap.josm.plugins.tageditor.tagspec.KeyValuePair;
import org.openstreetmap.josm.plugins.tageditor.tagspec.ui.ITagSelectorListener;
import org.openstreetmap.josm.plugins.tageditor.tagspec.ui.TabularTagSelector;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.WindowGeometry;
/**
 * The dialog for editing name/value-pairs (aka <em>tags</em>) associated with {@link OsmPrimitive}s.
 * 
 */
@SuppressWarnings("serial")
public class TagEditorDialog extends JDialog {
    static private final Logger logger = Logger.getLogger(TagEditorDialog.class.getName());

    /** the unique instance */
    static private  TagEditorDialog instance = null;

    /**
     * Access to the singleton instance
     * 
     * @return the singleton instance of the dialog
     */
    static public TagEditorDialog getInstance() {
        if (instance == null) {
            instance = new TagEditorDialog();
        }
        return instance;
    }

    /** default preferred size */
    static public final Dimension PREFERRED_SIZE = new Dimension(700, 500);

    /** the properties table */
    private TagEditor tagEditor = null;

    /**  the auto completion list viewer */
    private AutoCompletionListViewer aclViewer = null;

    /** the cache of auto completion values used by the tag editor */
    private AutoCompletionManager autocomplete = null;

    private OKAction okAction = null;
    private CancelAction cancelAction = null;

    /**
     * @return the tag editor model
     */
    public TagEditorModel getModel() {
        return tagEditor.getModel();
    }

    protected JPanel buildButtonRow() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        // the ok button
        //
        pnl.add(new JButton(okAction = new OKAction()));
        getModel().addPropertyChangeListener(okAction);

        // the cancel button
        //
        pnl.add(new JButton(cancelAction  = new CancelAction()));
        return pnl;
    }

    protected JPanel buildTagGridPanel() {
        // create tag editor and inject an instance of the tag
        // editor model
        //
        tagEditor = new TagEditor();
        
        // create the auto completion list viewer and connect it
        // to the tag editor
        //
        AutoCompletionList autoCompletionList = new AutoCompletionList();
        aclViewer = new AutoCompletionListViewer(autoCompletionList);
        tagEditor.setAutoCompletionList(autoCompletionList);
        aclViewer.addAutoCompletionListListener(tagEditor);
        tagEditor.addComponentNotStoppingCellEditing(aclViewer);

        JPanel pnlTagGrid = new JPanel();
        pnlTagGrid.setLayout(new BorderLayout());
        
        
        pnlTagGrid.add(tagEditor, BorderLayout.CENTER);
        pnlTagGrid.add(aclViewer, BorderLayout.EAST);
        pnlTagGrid.setBorder(BorderFactory.createEmptyBorder(5, 0,0,0));
        
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                tagEditor, 
                aclViewer
        );
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerLocation(600);      
        pnlTagGrid.add(splitPane, BorderLayout.CENTER);
        return pnlTagGrid;
    }
        
    /**
     * build the GUI
     */
    protected void build() {
        getContentPane().setLayout(new BorderLayout());

        // basic UI prpoperties
        //
        setModal(true);
        setSize(PREFERRED_SIZE);
        setTitle(tr("JOSM Tag Editor Plugin"));
        
        JPanel pnlTagGrid = buildTagGridPanel();

        // create the preset selector
        //
        TabularPresetSelector presetSelector = new TabularPresetSelector();
        presetSelector.addPresetSelectorListener(
                new IPresetSelectorListener() {
                    public void itemSelected(Item item) {
                        tagEditor.stopEditing();
                        tagEditor.getModel().applyPreset(item);
                        tagEditor.requestFocusInTopLeftCell();
                    }
                }
        );

        JPanel pnlPresetSelector = new JPanel();
        pnlPresetSelector.setLayout(new BorderLayout());
        pnlPresetSelector.add(presetSelector,BorderLayout.CENTER);
        pnlPresetSelector.setBorder(BorderFactory.createEmptyBorder(0,0,5,0 ));

        // create the tag selector
        //
        TabularTagSelector tagSelector = new TabularTagSelector();
        tagSelector.addTagSelectorListener(
                new ITagSelectorListener() {
                    public void itemSelected(KeyValuePair pair) {
                        tagEditor.stopEditing();
                        tagEditor.getModel().applyKeyValuePair(pair);
                        tagEditor.requestFocusInTopLeftCell();
                    }
                }
        );
        JPanel pnlTagSelector = new JPanel();
        pnlTagSelector.setLayout(new BorderLayout());
        pnlTagSelector.add(tagSelector,BorderLayout.CENTER);
        pnlTagSelector.setBorder(BorderFactory.createEmptyBorder(0,0,5,0    ));

        // create the tabbed pane
        //
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(pnlPresetSelector, tr("Presets"));
        tabbedPane.add(pnlTagSelector, tr("Tags"));

        
        // create split pane
        //
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                tabbedPane, 
                pnlTagGrid
        );
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(200);

        Dimension minimumSize = new Dimension(100, 50);
        presetSelector.setMinimumSize(minimumSize);
        pnlTagGrid.setMinimumSize(minimumSize);

        getContentPane().add(splitPane,BorderLayout.CENTER);

        getContentPane().add(buildButtonRow(), BorderLayout.SOUTH);


        addWindowListener(
                new WindowAdapter() {
                    @Override public void windowActivated(WindowEvent e) {
                        SwingUtilities.invokeLater(new Runnable(){
                            public void run()
                            {
                                getModel().ensureOneTag();
                                tagEditor.clearSelection();
                                tagEditor.requestFocusInTopLeftCell();
                            }
                        });
                    }
                }
        );

        // makes sure that 'Ctrl-Enter' in the properties table
        // and in the aclViewer is handled by okAction
        //
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke)cancelAction.getValue(Action.ACCELERATOR_KEY), okAction.getValue(AbstractAction.NAME));
        getRootPane().getActionMap().put(cancelAction.getValue(Action.NAME), cancelAction);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke)okAction.getValue(Action.ACCELERATOR_KEY), okAction.getValue(AbstractAction.NAME));
        getRootPane().getActionMap().put(okAction.getValue(Action.NAME), okAction);


        // make sure the OK action is also enabled in sub components. I registered
        // the action in the input and action maps of the dialogs root pane and I expected
        // it to get triggered regardless of what subcomponent had focus, but it didn't.
        //
        aclViewer.installKeyAction(okAction);
        aclViewer.installKeyAction(cancelAction);
        presetSelector.installKeyAction(okAction);
        presetSelector.installKeyAction(cancelAction);
    }

    /**
     * constructor
     */
    protected TagEditorDialog() {
        build();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            new WindowGeometry(
                    getClass().getName() + ".geometry",
                    WindowGeometry.centerInWindow(
                            Main.parent,
                            PREFERRED_SIZE
                    )
            ).applySafe(this);
        } else if (isShowing()) { // Avoid IllegalComponentStateException like in #8775
            new WindowGeometry(this).remember(getClass().getName() + ".geometry");
        }
        super.setVisible(visible);
    }

    /**
     * start an editing session. This method should be called before the dialog
     * is shown on the screen, i.e. before {@link Dialog#setVisible(boolean)} is
     * called.
     */
    public void startEditSession() {
        tagEditor.getModel().clearAppliedPresets();
        tagEditor.getModel().initFromJOSMSelection();
        autocomplete = Main.main.getEditLayer().data.getAutoCompletionManager();
        tagEditor.setAutoCompletionManager(autocomplete);
        getModel().ensureOneTag();
    }

    class CancelAction extends AbstractAction {
        public CancelAction() {
            putValue(NAME, tr("Cancel"));
            putValue(SMALL_ICON, ImageProvider.get("cancel"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0));
            putValue(SHORT_DESCRIPTION, tr("Abort tag editing and close dialog"));
        }

        public void actionPerformed(ActionEvent arg0) {
            setVisible(false);
        }
    }

    class OKAction extends AbstractAction implements PropertyChangeListener {

        public OKAction() {
            putValue(NAME, tr("OK"));
            putValue(SMALL_ICON, ImageProvider.get("ok"));
            putValue(SHORT_DESCRIPTION, tr("Apply edited tags and close dialog"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl ENTER"));
        }

        public void actionPerformed(ActionEvent e) {
            run();
        }

        public void run() {
            tagEditor.stopEditing();
            setVisible(false);
            tagEditor.getModel().updateJOSMSelection();
            DataSet ds = Main.main.getCurrentDataSet();
            ds.fireSelectionChanged();
            Main.parent.repaint(); // repaint all - drawing could have been changed
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (! evt.getPropertyName().equals(TagEditorModel.PROP_DIRTY))
                return;
            if (! evt.getNewValue().getClass().equals(Boolean.class))
                return;
            boolean dirty = (Boolean)evt.getNewValue();
            setEnabled(dirty);
        }
    }
}
