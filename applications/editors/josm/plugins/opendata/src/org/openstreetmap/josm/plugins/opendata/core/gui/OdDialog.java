package org.openstreetmap.josm.plugins.opendata.core.gui;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.EditLayerChangeListener;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.layers.OdDataLayer;
import org.openstreetmap.josm.plugins.opendata.core.layers.OdLayer;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public class OdDialog extends ToggleDialog implements OdConstants, LayerChangeListener, EditLayerChangeListener {

    private final SideButton selectButton;
    private final SideButton downloadButton;
    private final SideButton diffButton;
    
    private final List<SideButton> buttons;
    
    private final TreeModel treeModel;
    
    private OdDataLayer dataLayer;
    
    private class DownloadAction extends JosmAction {
		public DownloadAction() {
			super(marktr("Download"), "download", tr("Download OSM data corresponding to the current data set."), null, false);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Main.main.getEditLayer() instanceof OdLayer) {
				dataLayer.downloadOsmData();
				diffButton.setEnabled(dataLayer.osmLayer != null);
			}
		}
    }

    private class SelectAction extends JosmAction {
		public SelectAction() {
			super(marktr("Select"), "dialogs/select", tr("Set the selected elements on the map to the selected items in the list above."), null, false);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO
		}
    }

    private class DiffAction extends JosmAction {
		public DiffAction() {
			super(marktr("Diff"), "dialogs/diff", tr("Perform diff between current data set and existing OSM data."), null, false);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			if (Main.main.getEditLayer() instanceof OdLayer) {
				dataLayer.makeDiff();
			}
		}
    }

	public OdDialog() {
		super("OpenData", ICON_CORE_24, tr("Open the OpenData window."), 
				Shortcut.registerShortcut("subwindow:opendata", tr("Toggle: {0}", "OpenData"),
						KeyEvent.VK_A, Shortcut.ALT_CTRL_SHIFT), 150);
		
		this.buttons = Arrays.asList(new SideButton[] {
				selectButton = new SideButton(new SelectAction()), 
				downloadButton = new SideButton(new DownloadAction()), 
				diffButton = new SideButton(new DiffAction())
        });
		
		disableAllButtons();
		
		this.treeModel = new DefaultTreeModel(null); // TODO: treeNode
		this.dataLayer = null;
		
		createLayout(new JTree(treeModel), true, buttons);
		
		MapView.addEditLayerChangeListener(this);
	}
	
	private void disableAllButtons() {
		for (SideButton button : buttons) {
			button.setEnabled(false);
		}
	}

	@Override
	public void editLayerChanged(OsmDataLayer oldLayer, OsmDataLayer newLayer) {
		activeLayerChange(oldLayer, newLayer);
	}

	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if (newLayer instanceof OdLayer) {
			dataLayer = ((OdLayer) newLayer).getDataLayer();
		} else {
			dataLayer = null;
		}
		
		if (dataLayer != null) {
			if (dataLayer.osmLayer == null) {
				downloadButton.setEnabled(true);
			} else if (dataLayer.diffLayer == null) {
				diffButton.setEnabled(true);
			}
		} else {
			disableAllButtons();
		}
	}

	@Override
	public void layerAdded(Layer newLayer) {
	}

	@Override
	public void layerRemoved(Layer oldLayer) {
	}
}
