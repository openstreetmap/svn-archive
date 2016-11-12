// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.dialogs.relation.GenericRelationEditor;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.LayerPositionStrategy;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.pt_assistant.data.PTWay;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Layer that visualizes the routes in a more convenient way
 *
 * @author darya
 *
 */
public final class PTAssistantLayer extends Layer
        implements SelectionChangedListener, PropertyChangeListener, LayerChangeListener {

    private static PTAssistantLayer layer;
    private List<OsmPrimitive> primitives = new ArrayList<>();
    private PTAssistantPaintVisitor paintVisitor;
    private HashMap<Character, List<PTWay>> fixVariants = new HashMap<>();
    private HashMap<Way, List<Character>> wayColoring = new HashMap<>();

    private PTAssistantLayer() {
        super("pt_assistant layer");
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);
        Main.getLayerManager().addLayerChangeListener(this);
        layer = this;
    }

    public static PTAssistantLayer getLayer() {
        if (layer == null) {
            new PTAssistantLayer();
        }
        return layer;
    }

    /**
     * Adds a primitive (route) to be displayed in this layer
     *
     * @param primitive primitive (route)
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
        Main.map.mapView.repaint();
    }

    /**
     * Adds the first 5 fix variants to be displayed in the pt_assistant layer
     *
     * @param fixVariants fix variants
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

        for (Character currentFixVariantLetter : this.fixVariants.keySet()) {
            List<PTWay> fixVariant = this.fixVariants.get(currentFixVariantLetter);
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
     * Returns fix variant (represented by a list of PTWays) that corresponds to
     * the given character.
     *
     * @param c character
     * @return fix variant
     */
    public List<PTWay> getFixVariant(char c) {
        return this.fixVariants.get(Character.toUpperCase(c));
    }

    @Override
    public void paint(final Graphics2D g, final MapView mv, Bounds bounds) {

        paintVisitor = new PTAssistantPaintVisitor(g, mv);

        for (OsmPrimitive primitive : primitives) {
            paintVisitor.visit(primitive);
        }

        paintVisitor.visitFixVariants(this.fixVariants, this.wayColoring);

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
        return new Action[] {LayerListDialog.getInstance().createShowHideLayerAction(),
                LayerListDialog.getInstance().createDeleteLayerAction(), SeparatorLayerAction.INSTANCE,
                new RenameLayerAction(null, this), SeparatorLayerAction.INSTANCE, new LayerListPopup.InfoAction(this)};
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
     * Listens to a selection change
     */
    @Override
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {

        ArrayList<Relation> routes = new ArrayList<>();

        for (OsmPrimitive primitive : newSelection) {
            if (primitive.getType().equals(OsmPrimitiveType.RELATION)) {
                Relation relation = (Relation) primitive;
                if (RouteUtils.isTwoDirectionRoute(relation)) {
                    routes.add(relation);
                }

            }
        }

        if (!routes.isEmpty()) {
            this.primitives.clear();
            this.primitives.addAll(routes);
            if (!Main.getLayerManager().containsLayer(this)) {
                Main.getLayerManager().addLayer(this);
            }
        }

    }

    /**
     * Listens to a focus change, sets the primitives attribute to the route
     * relation in the top Relation Editor and repaints the map
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if ("focusedWindow".equals(evt.getPropertyName())) {

            if (evt.getNewValue() == null) {
                return;
            }

            if (evt.getNewValue().getClass().equals(GenericRelationEditor.class)) {

                GenericRelationEditor editor = (GenericRelationEditor) evt.getNewValue();
                Relation relation = editor.getRelation();

                if (RouteUtils.isTwoDirectionRoute(relation)) {

                    this.repaint(relation);

                }

            }
        }
    }

    /**
     * Repaints the layer in cases when there was no selection change
     *
     * @param relation relation
     */
    public void repaint(Relation relation) {
        this.primitives.clear();
        this.primitives.add(relation);
        if (!Main.getLayerManager().containsLayer(this)) {
            Main.getLayerManager().addLayer(this);
        }

        if (paintVisitor == null) {
            Graphics g = Main.map.mapView.getGraphics();
            MapView mv = Main.map.mapView;
            paintVisitor = new PTAssistantPaintVisitor(g, mv);
        }

        for (OsmPrimitive primitive : primitives) {
            paintVisitor.visit(primitive);
        }

        paintVisitor.visitFixVariants(this.fixVariants, this.wayColoring);

        Main.map.mapView.repaint();
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
            this.primitives.clear();
            this.fixVariants.clear();
            this.wayColoring.clear();
            Main.map.mapView.repaint();
        }

    }
}
