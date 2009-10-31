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
import java.util.Collection;
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
import org.openstreetmap.josm.plugins.tageditor.ac.AutoCompletionCache;
import org.openstreetmap.josm.plugins.tageditor.ac.AutoCompletionList;
import org.openstreetmap.josm.plugins.tageditor.ac.AutoCompletionListViewer;
import org.openstreetmap.josm.plugins.tageditor.editor.TagEditor;
import org.openstreetmap.josm.plugins.tageditor.editor.TagEditorModel;
import org.openstreetmap.josm.plugins.tageditor.preset.Item;
import org.openstreetmap.josm.plugins.tageditor.preset.ui.IPresetSelectorListener;
import org.openstreetmap.josm.plugins.tageditor.preset.ui.TabularPresetSelector;
import org.openstreetmap.josm.plugins.tageditor.tagspec.KeyValuePair;
import org.openstreetmap.josm.plugins.tageditor.tagspec.ui.ITagSelectorListener;
import org.openstreetmap.josm.plugins.tageditor.tagspec.ui.TabularTagSelector;
/**
 * The dialog for editing name/value-pairs (aka <em>tags</em>) associated with {@link OsmPrimitive}s.
 * 
 *
 */
@SuppressWarnings("serial")
public class TagEditorDialog extends JDialog {

	static private Logger logger = Logger.getLogger(TagEditorDialog.class.getName());

	/** the unique instance */
	static protected  TagEditorDialog instance = null;

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


	/**
	 * default preferred size
	 */
	static public final Dimension PREFERRED_SIZE = new Dimension(700, 500);


	/** the properties table */
	private TagEditor tagEditor = null;
	private TagEditorModel model = null;

	/**  the auto completion list viewer */
	private AutoCompletionListViewer aclViewer = null;

	/** the cache of auto completion values used by the tag editor */
	private AutoCompletionCache acCache = null;

	/** widgets */
	private JButton btnOK = null;
	private JButton btnCancel = null;
	private JButton btnAdd = null;
	private JButton btnDelete = null;
	private OKAction okAction = null;
	private CancelAction cancelAction = null;


	public OKAction getOKAction() {
		return okAction;
	}

	/**
	 * @return the tag editor model
	 */
	public TagEditorModel getModel() {
		return model;
	}



	protected JPanel buildButtonRow() {
		// create the rows of action buttons at the bottom
		// of the dialog
		//
		JPanel pnl = new JPanel();
		pnl.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// the ok button
		//
		okAction = new OKAction();
		btnOK = new JButton(okAction);
		getModel().addPropertyChangeListener(okAction);

		// the cancel button
		//
		cancelAction = new CancelAction();
		btnCancel = new JButton(cancelAction);
		pnl.add(btnOK);
		pnl.add(btnCancel);

		JPanel pnl1 = new JPanel();
		pnl.setLayout(new FlowLayout(FlowLayout.LEFT));

		// the add button
		//
		btnAdd = new JButton(tagEditor.getAddAction());
		btnDelete = new JButton(tagEditor.getDeleteAction());

		pnl1.add(btnAdd);
		pnl1.add(btnDelete);

		JPanel pnl2 = new JPanel();
		pnl2.setLayout(new BorderLayout());
		pnl2.add(pnl1, BorderLayout.WEST);
		pnl2.add(pnl, BorderLayout.EAST);

		return pnl2;
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


		// create tag editor and inject an instance of the tag
		// editor model
		//
		tagEditor = new TagEditor();
		tagEditor.setTagEditorModel(model);


		// create the auto completion list viewer and connect it
		// to the tag editor
		//
		AutoCompletionList autoCompletionList = new AutoCompletionList();
		aclViewer = new AutoCompletionListViewer(autoCompletionList);
		tagEditor.setAutoCompletionList(autoCompletionList);
		tagEditor.setAutoCompletionCache(acCache);
		aclViewer.addAutoCompletionListListener(tagEditor);

		JPanel pnlTagGrid = new JPanel();
		pnlTagGrid.setLayout(new BorderLayout());
		pnlTagGrid.add(tagEditor, BorderLayout.CENTER);
		pnlTagGrid.add(aclViewer, BorderLayout.EAST);
		pnlTagGrid.setBorder(BorderFactory.createEmptyBorder(5, 0,0,0));


		// create the preset selector
		//
		TabularPresetSelector presetSelector = new TabularPresetSelector();
		presetSelector.addPresetSelectorListener(
				new IPresetSelectorListener() {
					public void itemSelected(Item item) {
						tagEditor.stopEditing();
						model.applyPreset(item);
						tagEditor.requestFocusInTopLeftCell();
					}
				}
		);


		JPanel pnlPresetSelector = new JPanel();
		pnlPresetSelector.setLayout(new BorderLayout());
		pnlPresetSelector.add(presetSelector,BorderLayout.CENTER);
		pnlPresetSelector.setBorder(BorderFactory.createEmptyBorder(0,0,5,0	));

		// create the tag selector
		//
		TabularTagSelector tagSelector = new TabularTagSelector();
		tagSelector.addTagSelectorListener(
				new ITagSelectorListener() {
					public void itemSelected(KeyValuePair pair) {
						tagEditor.stopEditing();
						model.applyKeyValuePair(pair);
						tagEditor.requestFocusInTopLeftCell();
					}
				}
		);
		JPanel pnlTagSelector = new JPanel();
		pnlTagSelector.setLayout(new BorderLayout());
		pnlTagSelector.add(tagSelector,BorderLayout.CENTER);
		pnlTagSelector.setBorder(BorderFactory.createEmptyBorder(0,0,5,0	));




		// create the tabbed pane
		//
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(pnlPresetSelector, tr("Presets"));
		tabbedPane.add(pnlTagSelector, tr("Tags"));

		// create split pane
		//
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				tabbedPane, pnlTagGrid);
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
		acCache = new AutoCompletionCache();
		model = new TagEditorModel();
		build();
	}


	@Override
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}



	/**
	 * start an editing session. This method should be called before the dialog
	 * is shown on the screen, i.e. before {@link Dialog#setVisible(boolean)} is
	 * called.
	 */
	public void startEditSession() {
		model.clearAppliedPresets();
		model.initFromJOSMSelection();
		acCache.initFromJOSMDataset();
		getModel().ensureOneTag();
	}



	@SuppressWarnings("serial")
	class CancelAction extends AbstractAction {

		public CancelAction() {
			putValue(Action.NAME, tr("Cancel"));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0));
		}

		public void actionPerformed(ActionEvent arg0) {
			setVisible(false);
		}
	}



	@SuppressWarnings("serial")
	class OKAction extends AbstractAction implements PropertyChangeListener {

		public OKAction() {
			putValue(Action.NAME, tr("OK"));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl ENTER"));
		}

		public void actionPerformed(ActionEvent e) {
			run();
		}

		public void run() {
			tagEditor.stopEditing();
			setVisible(false);
			model.updateJOSMSelection();
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
