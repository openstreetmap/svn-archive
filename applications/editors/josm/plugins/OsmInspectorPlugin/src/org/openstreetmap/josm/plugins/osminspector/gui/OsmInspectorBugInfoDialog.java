package org.openstreetmap.josm.plugins.osminspector.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.osminspector.OsmInspectorLayer;
import org.openstreetmap.josm.plugins.osminspector.OsmInspectorLayer.BugInfo;
import org.openstreetmap.josm.tools.Shortcut;

public class OsmInspectorBugInfoDialog extends ToggleDialog implements
		ListSelectionListener, LayerChangeListener, MouseListener {

	/**
     * 
     */
    private static final long serialVersionUID = 7730268317164906579L;
    private OsmInspectorLayer layer;
	private JTextPane bugTextArea;
	

	/**
	 * Builds the content panel for this dialog
	 */
	protected void buildContentPanel() {
		Main.map.addToggleDialog(this, true);
		
		bugTextArea = new JTextPane();
		createLayout(bugTextArea, true, Arrays.asList(new SideButton[] {}));
		bugTextArea.setText("This is a demo");
		this.add(bugTextArea);
		
	}


	public OsmInspectorBugInfoDialog(OsmInspectorLayer layer) {

		super(tr("OsmBugInfo"), "select",
				tr("Open a OSM Inspector selection list window."), Shortcut.registerShortcut("subwindow:select",
								tr("Toggle: {0}", tr("Current Selected Bug Info")),
								KeyEvent.VK_D, Shortcut.ALT_SHIFT), 150, // default
																			// height
				true // default is "show dialog"
		);
		this.layer = layer;
		buildContentPanel();
	}

	public void updateDialog(OsmInspectorLayer l) {
		this.layer = l;
	}
	
	public void setBugDescription(BugInfo i){
	    bugTextArea.setText(i.getContentString());
	}
	
	@Override
	public void showNotify() {
		super.showNotify();
	}

	@Override
	public void hideNotify() {
		if (dialogsPanel != null) {
			// TODO Auto-generated method stub
			super.hideNotify();
		}
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if(newLayer instanceof OsmInspectorLayer) {
			this.layer = (OsmInspectorLayer) newLayer;
		}

	}

	@Override
	public void layerAdded(Layer layer) {
	}

	@Override
	public void layerRemoved(Layer arg0) {
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		System.out.println(e.getFirstIndex());
	}

}
