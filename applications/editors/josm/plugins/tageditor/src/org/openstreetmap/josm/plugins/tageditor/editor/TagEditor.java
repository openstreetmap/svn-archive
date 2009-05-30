package org.openstreetmap.josm.plugins.tageditor.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.openstreetmap.josm.plugins.tageditor.ac.AutoCompletionCache;
import org.openstreetmap.josm.plugins.tageditor.ac.AutoCompletionList;
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

	private final TagEditorModel tagEditorModel;
	private TagTable tblTagEditor;
	private PresetManager presetManager;
	private AutoCompletionList autoCompletionList;


	/**
	 * builds the GUI
	 * 
	 */
	protected void build() {
		setLayout(new BorderLayout());

		// build the scrollable table for editing tag names and tag values
		//
		tblTagEditor = new TagTable(tagEditorModel);
		final JScrollPane scrollPane = new JScrollPane(tblTagEditor);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scrollPane, BorderLayout.CENTER);

		// this adapters ensures that the width of the tag table columns is adjusted
		// to the width of the scroll pane viewport. Also tried to overwrite
		// getPreferredViewportSize() in JTable, but did not work.
		//
		scrollPane.addComponentListener(
				new ComponentAdapter() {
					@Override public void componentResized(ComponentEvent e) {
						super.componentResized(e);
						Dimension d = scrollPane.getViewport().getExtentSize();
						tblTagEditor.adjustColumnWidth(d.width);
					}
				}
		);

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
		// creates a default model and a default cache
		//
		tagEditorModel = new TagEditorModel();

		build();
	}


	/**
	 * replies the tag editor model
	 * @return the tag editor model
	 */
	public TagEditorModel getTagEditorModel() {
		return tagEditorModel;
	}

	/**
	 * sets the tag editor model
	 * 
	 * @param tagEditorModel the tag editor model
	 */
	public void setTagEditorModel(TagEditorModel tagEditorModel) {
		tblTagEditor.setModel(tagEditorModel);
		for (int i=0; i<=1; i++) {
			TableCellEditor editor = (TableCellEditor)tblTagEditor.getColumnModel().getColumn(i).getCellEditor();
			if (editor != null) {
				editor.setTagEditorModel(tagEditorModel);
			}
		}
		presetManager.setModel(tagEditorModel);
	}

	public RunnableAction getDeleteAction() {
		return tblTagEditor.getDeleteAction();
	}

	public RunnableAction getAddAction() {
		return tblTagEditor.getAddAction();
	}



	public void clearSelection() {
		tblTagEditor.getSelectionModel().clearSelection();
	}



	public void stopEditing() {
		TableCellEditor editor = (TableCellEditor) tblTagEditor.getCellEditor();
		if (editor != null) {
			editor.stopCellEditing();
		}
	}


	public AutoCompletionList getAutoCompletionList() {
		return ((org.openstreetmap.josm.plugins.tageditor.editor.TableCellEditor)tblTagEditor.getCellEditor()).getAutoCompletionList();
	}

	public void setAutoCompletionList(AutoCompletionList autoCompletionList) {
		tblTagEditor.setAutoCompletionList(autoCompletionList);
	}

	public void setAutoCompletionCache(AutoCompletionCache acCache) {
		tblTagEditor.setAutoCompletionCache(acCache);
	}


	public void autoCompletionItemSelected(String item) {
		org.openstreetmap.josm.plugins.tageditor.editor.TableCellEditor editor = ((org.openstreetmap.josm.plugins.tageditor.editor.TableCellEditor)tblTagEditor.getCellEditor());
		if (editor != null) {
			editor.autoCompletionItemSelected(item);
		}
	}

	public void requestFocusInTopLeftCell() {
		tblTagEditor.requestFocusInCell(0,0);
	}






}
