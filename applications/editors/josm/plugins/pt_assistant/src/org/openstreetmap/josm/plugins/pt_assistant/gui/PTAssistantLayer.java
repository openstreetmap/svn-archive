// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.gui;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.LayerPositionStrategy;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPlugin;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTWay;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Layer that visualizes the routes in a more convenient way
 *
 * @author darya
 *
 */
public final class PTAssistantLayer extends Layer implements LayerChangeListener {

	private List<OsmPrimitive> primitives = new ArrayList<>();
	private PTAssistantPaintVisitor paintVisitor;
	private HashMap<Character, List<PTWay>> fixVariants = new HashMap<>();
	private HashMap<Way, List<Character>> wayColoring = new HashMap<>();
	public String modeOfTravel = null;

	public PTAssistantLayer() {
		super("pt_assistant layer");
		MainApplication.getLayerManager().addLayerChangeListener(this);
		MainApplication.getLayerManager().addLayer(this);
	}

	/**
	 * Adds a primitive (route) to be displayed in this layer
	 *
	 * @param primitive
	 *            primitive (route)
	 */
	public void addPrimitive(OsmPrimitive primitive) {
		this.primitives.add(primitive);
	}

	/**
	 * Clears all primitives (routes) from being displayed.
	 */
	public void clear() {
		this.primitives.clear();
	}

	public void clearFixVariants() {
		fixVariants.clear();
		wayColoring.clear();
		MainApplication.getMap().mapView.repaint();
	}

	/**
	 * Adds the first 5 fix variants to be displayed in the pt_assistant layer
	 *
	 * @param fixVariants
	 *            fix variants
	 */
	public void addFixVariants(List<List<PTWay>> fixVariants) {
		HashMap<List<PTWay>, Character> fixVariantLetterMap = new HashMap<>();

		char alphabet = 'A';
		for (int i = 0; i < 5 && i < fixVariants.size(); i++) {
			List<PTWay> fixVariant = fixVariants.get(i);
			this.fixVariants.put(alphabet, fixVariant);
			fixVariantLetterMap.put(fixVariant, alphabet);
			alphabet++;
		}

		for (Entry<Character, List<PTWay>> entry : this.fixVariants.entrySet()) {
			Character currentFixVariantLetter = entry.getKey();
			List<PTWay> fixVariant = entry.getValue();
			for (PTWay ptway : fixVariant) {
				for (Way way : ptway.getWays()) {
					if (wayColoring.containsKey(way)) {
						if (!wayColoring.get(way).contains(currentFixVariantLetter)) {
							wayColoring.get(way).add(currentFixVariantLetter);
						}
					} else {
						List<Character> letterList = new ArrayList<>();
						letterList.add(currentFixVariantLetter);
						wayColoring.put(way, letterList);
					}
				}
			}
		}
	}

	/**
	 * Returns fix variant (represented by a list of PTWays) that corresponds to the
	 * given character.
	 *
	 * @param c
	 *            character
	 * @return fix variant
	 */
	public List<PTWay> getFixVariant(char c) {
		return fixVariants.get(Character.toUpperCase(c));
	}

	public void setPrimitives(List<OsmPrimitive> newPrimitives) {
		primitives = new ArrayList<>(newPrimitives);
	}

	@Override
	public void paint(final Graphics2D g, final MapView mv, Bounds bounds) {

		paintVisitor = new PTAssistantPaintVisitor(g, mv);

		for (OsmPrimitive primitive : primitives) {
			paintVisitor.visit(primitive);
		}

		paintVisitor.visitFixVariants(fixVariants, wayColoring);
	}

	@Override
	public Icon getIcon() {
		return ImageProvider.get("layer", "osmdata_small");
	}

	@Override
	public Object getInfoComponent() {
		return getToolTipText();
	}

	@Override
	public Action[] getMenuEntries() {
		return new Action[] { LayerListDialog.getInstance().createShowHideLayerAction(),
				LayerListDialog.getInstance().createDeleteLayerAction(), SeparatorLayerAction.INSTANCE,
				new RenameLayerAction(null, this), SeparatorLayerAction.INSTANCE, new LayerListPopup.InfoAction(this) };
	}

	@Override
	public String getToolTipText() {
		return "pt_assistant layer";
	}

	@Override
	public boolean isMergable(Layer arg0) {
		return false;
	}

	@Override
	public void mergeFrom(Layer arg0) {
		// do nothing

	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor arg0) {
		// do nothing

	}

	@Override
	public LayerPositionStrategy getDefaultLayerPosition() {
		return LayerPositionStrategy.IN_FRONT;
	}

	/**
	 * Repaints the layer in cases when there was no selection change
	 *
	 * @param relation
	 *            relation
	 */
	public void repaint(Relation relation) {
		primitives.clear();
		primitives.add(relation);
		if (!MainApplication.getLayerManager().containsLayer(this)) {
			MainApplication.getLayerManager().addLayer(this);
		}

		if (paintVisitor == null) {
			MapView mv = MainApplication.getMap().mapView;
			paintVisitor = new PTAssistantPaintVisitor(mv.getGraphics(), mv);
		}

		for (OsmPrimitive primitive : primitives) {
			paintVisitor.visit(primitive);
		}

		paintVisitor.visitFixVariants(fixVariants, wayColoring);

		MainApplication.getMap().mapView.repaint();
		setModeOfTravel(relation);
	}

	private void setModeOfTravel(Relation relation) {
		if (relation.hasKey("route"))
			modeOfTravel = relation.get("route");
	}

	public String getModeOfTravel() {
		return modeOfTravel;
	}

	@Override
	public void layerAdded(LayerAddEvent arg0) {
		// do nothing
	}

	@Override
	public void layerOrderChanged(LayerOrderChangeEvent arg0) {
		// do nothing

	}

	@Override
	public void layerRemoving(LayerRemoveEvent event) {

		if (event.getRemovedLayer() instanceof OsmDataLayer) {
			primitives.clear();
			fixVariants.clear();
			wayColoring.clear();
			MainApplication.getMap().mapView.repaint();
		}

		if (event.getRemovedLayer() instanceof OsmDataLayer
				&& event.getSource().getLayersOfType(OsmDataLayer.class).isEmpty())
			event.scheduleRemoval(Collections.singleton(this));

		if (event.getRemovedLayer() == this) {
			PTAssistantLayerManager.PTLM.resetLayer();
			PTAssistantPlugin.clearHighlightedRelations();
		}
	}

	@Override
	public synchronized void destroy() {
		MainApplication.getLayerManager().removeLayerChangeListener(this);
		super.destroy();
	}
}
