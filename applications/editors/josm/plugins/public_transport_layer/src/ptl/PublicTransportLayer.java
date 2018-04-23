// License: GPL. For details, see LICENSE file.
package ptl;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.data.osm.visitor.paint.StyledMapRenderer;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.mappaint.Cascade;
import org.openstreetmap.josm.gui.mappaint.Environment;
import org.openstreetmap.josm.gui.mappaint.MultiCascade;
import org.openstreetmap.josm.gui.mappaint.StyleKeys;
import org.openstreetmap.josm.gui.mappaint.styleelement.TextLabel;
import org.openstreetmap.josm.gui.mappaint.styleelement.placement.OnLineStrategy;
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.MultiMap;
import org.openstreetmap.josm.tools.Pair;
import org.openstreetmap.josm.tools.Utils;

/**
 * Public transport layer.
 */
public class PublicTransportLayer extends Layer {
    private static final ImageProvider ICON = new ImageProvider("presets/misc", "route");
    private boolean doDrawArrows = true;
    private boolean doDrawRefLabels = true;

    public PublicTransportLayer() {
        super(tr("Public transport routes"));
    }

    @Override
    public void paint(Graphics2D g, MapView mv, Bounds box) {
        if (mv == null || mv.getLayerManager().getEditLayer() == null || mv.getLayerManager().getEditLayer().data.selectionEmpty()) {
            return;
        }
        final StyledMapRenderer renderer = new StyledMapRenderer(g, mv, false);
        renderer.getSettings(false);

        final Collection<Relation> selectedRelations = mv.getLayerManager().getEditLayer().data.getSelectedRelations();
        final MultiMap<Pair<Node, Node>, String> segmentRefs = new MultiMap<>();
        for (final Relation relation : selectedRelations) {
            if (relation.isIncomplete() || relation.hasIncompleteMembers()
                    || !relation.hasTag("type", "route") || !relation.hasTag("public_transport:version", "2")) {
                continue;
            }

            final Way way = new Way();
            Node previousNode = null;
            for (final RelationMember member : relation.getMembers()) {
                if (OsmPrimitiveType.NODE.equals(member.getType()) && "stop".equals(member.getRole())) {
                    way.addNode(member.getNode());
                    if (previousNode != null) {
                        segmentRefs.put(Pair.create(previousNode, member.getNode()), relation.get("ref"));
                    }
                    previousNode = member.getNode();
                }
            }

            Color color = Color.GREEN;
            try {
                color = ColorHelper.html2color(relation.get("colour"));
            } catch (RuntimeException ignore) {
                Logging.trace(ignore);
            }
            renderer.drawWay(way, color, new BasicStroke(1), null, null, 0, doDrawArrows, false, false, false);
        }

        if (doDrawRefLabels) {
            drawRefLabels(renderer, segmentRefs);
        }
    }

    protected void drawRefLabels(StyledMapRenderer renderer, MultiMap<Pair<Node, Node>, String> segmentRefs) {
        Environment env = new Environment();
        env.mc = new MultiCascade();
        Cascade c = env.mc.getOrCreateCascade("default");
        c.put(StyleKeys.FONT_FAMILY, "SansSerif");
        c.put(StyleKeys.FONT_SIZE, 16);
        Color color = new Color(0x80FFFFFF, true);

        for (Pair<Node, Node> nodePair : segmentRefs.keySet()) {
            final String label = Utils.join(tr(", "), new TreeSet<>(segmentRefs.get(nodePair)));
            c.put(StyleKeys.TEXT, label);
            final TextLabel text = TextLabel.create(env, color, false);
            final Way way = new Way();
            way.addNode(nodePair.a);
            way.addNode(nodePair.b);
            renderer.drawText(way, text, OnLineStrategy.INSTANCE);
        }
    }

    @Override
    public Icon getIcon() {
        return ICON.setMaxSize(ImageProvider.ImageSizes.LAYER).get();
    }

    @Override
    public String getToolTipText() {
        return "";
    }

    @Override
    public void mergeFrom(Layer from) {
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
    }

    @Override
    public Object getInfoComponent() {
        return null;
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[]{
                new AbstractAction(tr("Toggle direction arrows")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doDrawArrows = !doDrawArrows;
                        MainApplication.getMap().repaint();
                    }
                },
                new AbstractAction(tr("Toggle reference labels")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doDrawRefLabels = !doDrawRefLabels;
                        MainApplication.getMap().repaint();
                    }
                }
        };
    }

    public static class AddLayerAction extends JosmAction {
        public AddLayerAction() {
            super(tr("Visualize public transport routes"), ICON, tr("Displays stops of selected public transport routes as graph"),
                    null, false, null, false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MainApplication.getLayerManager().addLayer(new PublicTransportLayer());
        }
    }
}

