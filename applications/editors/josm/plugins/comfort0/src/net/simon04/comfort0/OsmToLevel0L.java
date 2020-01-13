package net.simon04.comfort0;

import java.util.Collection;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DefaultNameFormatter;
import org.openstreetmap.josm.data.osm.KeyValueVisitor;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveComparator;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Tagged;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.OsmPrimitiveVisitor;

/**
 * Implements a conversion from {@link OsmPrimitive} to <a href="https://wiki.openstreetmap.org/wiki/Level0L">Level0L</a>.
 */
public class OsmToLevel0L implements OsmPrimitiveVisitor, KeyValueVisitor {

    private final StringBuilder sb = new StringBuilder();

    /**
     * Visits a collection of primitives
     * @param primitives The collection of primitives
     * @return {@code this}
     */
    public OsmToLevel0L visit(Collection<OsmPrimitive> primitives) {
        primitives.stream()
                .sorted(OsmPrimitiveComparator.orderingWaysRelationsNodes().thenComparing(OsmPrimitiveComparator.comparingUniqueId()))
                .forEachOrdered(p -> p.accept(this));
        return this;
    }

    @Override
    public void visit(Node n) {
        appendCommon(n);
    }

    @Override
    public void visit(Way w) {
        appendCommon(w);
        for (Node node : w.getNodes()) {
            appendRef(node, "");
        }
    }

    @Override
    public void visit(Relation r) {
        appendCommon(r);
        for (RelationMember member : r.getMembers()) {
            appendRef(member.getMember(), " " + member.getRole());
        }
    }

    private void appendRef(OsmPrimitive primitive, String mixin) {
        sb.append("  ");
        switch (primitive.getType()) {
            case NODE:
                sb.append("nd ");
                break;
            case WAY:
                sb.append("wy ");
                break;
            case RELATION:
                sb.append("rel ");
                break;
            default:
                break;
        }
        sb.append(primitive.getUniqueId());
        sb.append(mixin);
        appendDisplayName(primitive);
    }

    void appendCommon(OsmPrimitive p) {
        sb.append("\n");
        sb.append(p.getType().getAPIName()).append(" ").append(p.getUniqueId());
        if (p instanceof Node) {
            final LatLon latLon = ((Node) p).getCoor();
            if (latLon != null) {
                sb.append(": ").append(latLon.lat()).append(", ").append(latLon.lon());
            }
        }
        appendDisplayName(p);
        p.visitKeys(this);
    }

    void appendDisplayName(OsmPrimitive p) {
        sb.append(" #").append(p.getDisplayName(DefaultNameFormatter.getInstance())).append("\n");
    }

    @Override
    public void visitKeyValue(Tagged primitive, String key, String value) {
        if (key.contains("#") || key.contains("=")) {
            throw new UnsupportedOperationException("# and = are not supported in keys");
        }
        sb.append("  ").append(key).append(" = ").append(value).append("\n");
    }

    @Override
    public String toString() {
        return sb.toString().replaceFirst("^\\n", "");
    }
}
