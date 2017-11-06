package org.openstreetmap.josm.plugins.osminspector.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.plugins.osminspector.OsmInspectorLayer;
import org.openstreetmap.josm.plugins.osminspector.OsmInspectorLayer.BugInfo;
import org.openstreetmap.josm.tools.Shortcut;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class OsmInspectorDialog extends ToggleDialog implements LayerChangeListener, ActiveLayerChangeListener {

	private OsmInspectorLayer layer;
	private JList<String> bugsList;
	private OsmInspectorNextAction actNext;
	private OsmInspectorPrevAction actPrev;
	private DefaultListModel<String> model;

	private OsmInspectorBugInfoDialog bugInfoDialog;
	
	public void updateNextPrevAction(OsmInspectorLayer l) {
		this.actNext.inspectlayer = l;
		this.actPrev.inspectlayer = l;
	}
	
	/**
	 * Builds the content panel for this dialog
	 */
	protected void buildContentPanel() {
		MainApplication.getMap().addToggleDialog(this, true);

		model = new DefaultListModel<>();
		refreshModel();
		bugsList = new JList<>(model);
		bugsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bugsList.setLayoutOrientation(JList.VERTICAL_WRAP);
		
		bugsList.setVisibleRowCount(-1);
		JScrollPane scroll = new JScrollPane(bugsList,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		bugsList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				layer.setOsmiIndex(e.getFirstIndex());
				BugInfo next = layer.getOsmiIndex().getItemPointedByNext();
				layer.setOsmiIndex((e.getFirstIndex() + 1) % layer.getOsmiBugInfo().size());
				Geometry geom = next.getGeom();
				Point centroid = geom.getCentroid();
				LatLon center = new LatLon(centroid.getY(), centroid.getX());
				MainApplication.getMap().mapView.zoomTo(center);
				layer.selectFeatures(center);
				bugInfoDialog.setBugDescription(next);
			}
		});
		
		// refreshBugList();
		// the next action
		final SideButton nextButton = new SideButton(
				actNext = new OsmInspectorNextAction(layer));
		nextButton.createArrow(new ActionListener() {
		    @Override
			public void actionPerformed(ActionEvent e) {
				int index = bugsList.getSelectedIndex();
				Geometry geom = layer.getOsmBugGeometry(index);
				Point centroid = geom.getCentroid();
				LatLon center = new LatLon(centroid.getY(), centroid.getX());
				MainApplication.getMap().mapView.zoomTo(center);
				layer.selectFeatures(center);
			}
		});

		// the previous button
		final SideButton prevButton = new SideButton(
				actPrev = new OsmInspectorPrevAction(layer));
		prevButton.createArrow(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
			}
		});

		createLayout(scroll, true,
				Arrays.asList(new SideButton[] { nextButton, prevButton }));
		this.add(scroll);

		Shortcut sprev = Shortcut.registerShortcut("osmi:prev", tr("Prev OSMI bug"),
				KeyEvent.VK_J, Shortcut.CTRL_SHIFT);
		MainApplication.registerActionShortcut(actPrev, sprev);

		Shortcut snext = Shortcut.registerShortcut("osmi:next", tr("Next OSMI bug"),
				KeyEvent.VK_K, Shortcut.CTRL_SHIFT);
		MainApplication.registerActionShortcut(actNext, snext);
	}

	public void refreshModel() {
		model.clear();
		for (Object b : layer.getOsmiBugInfo().keySet()) {
			if (b instanceof BugInfo) {
				model.addElement(b.toString());
			}
		}
	}

	public OsmInspectorDialog(OsmInspectorLayer layer) {

		super(tr("OSM Inspector Bugs"), "selectionlist",
				tr("Open a OSM Inspector selection list window."), Shortcut.registerShortcut("subwindow:osminspector",
								tr("Toggle: {0}", tr("OSM Inspector Bugs")),
								KeyEvent.VK_W, Shortcut.ALT_SHIFT), 150, // default height
				true // default is "show dialog"
		);
		this.layer = layer;
		buildContentPanel();
		bugInfoDialog = new OsmInspectorBugInfoDialog(layer);
		bugInfoDialog.setTitle(tr("Selected Bug Info"));
	}

	public void updateDialog(OsmInspectorLayer l) {
		this.layer = l;
		bugInfoDialog.updateDialog(l);
		refreshModel();
		refreshBugList();
	}
	
	@Override
	public void showNotify() {
		super.showNotify();
	}

	@Override
	public void hideNotify() {
		if (dialogsPanel != null) {
			super.hideNotify();
		}
	}

	public class OsmInspectorNextAction extends AbstractAction {

		private OsmInspectorLayer inspectlayer;

		public OsmInspectorNextAction(Layer inspector) {
			super("next");
			inspectlayer = (OsmInspectorLayer) inspector;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			inspectlayer.getOsmiIndex().next();
			BugInfo next = inspectlayer.getOsmiIndex().getItemPointedByNext();
			Geometry geom = next.getGeom();
			Point centroid = geom.getCentroid();
			LatLon center = new LatLon(centroid.getY(), centroid.getX());
			MainApplication.getMap().mapView.zoomTo(center);
			inspectlayer.selectFeatures(center);
			bugInfoDialog.setBugDescription(next);
			updateSelection(next);
		}
	}

	private void updateSelection(BugInfo prev) {
		int idx = layer.getOsmiIndex().indexOf(prev);
		if (idx >= 0) {
			bugsList.setSelectedIndex(idx);
		}
	}

	private class OsmInspectorPrevAction extends AbstractAction {

		private OsmInspectorLayer inspectlayer;

		public OsmInspectorPrevAction(Layer inspector) {
			super("prev");
			inspectlayer = (OsmInspectorLayer) inspector;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			inspectlayer.getOsmiIndex().prev();
			BugInfo prev = inspectlayer.getOsmiIndex().getItemPointedByPrev();
			Geometry geom = prev.getGeom();
			Point centroid = geom.getCentroid();
			LatLon center = new LatLon(centroid.getY(), centroid.getX());
			MainApplication.getMap().mapView.zoomTo(center);
			inspectlayer.selectFeatures(center);
			bugInfoDialog.setBugDescription(prev);
			updateSelection(prev);
		}
	}

	@Override
	public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
	    Layer newLayer = MainApplication.getLayerManager().getActiveLayer();
		if (newLayer instanceof OsmInspectorLayer) {
			this.layer = (OsmInspectorLayer) newLayer;
			refreshModel();
			refreshBugList();
		}
	}

	private void refreshBugList() {
		bugsList.clearSelection();
		bugsList = new JList<>(model);
	}

	@Override
	public void layerAdded(LayerAddEvent e) {
		if (layer instanceof OsmInspectorLayer) {
			refreshModel();
			refreshBugList();
		}
	}

	@Override
	public void layerRemoving(LayerRemoveEvent e) {
		if (layer instanceof OsmInspectorLayer) {
			bugsList.clearSelection();
			model.clear();
		}
	}

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
    }
}
