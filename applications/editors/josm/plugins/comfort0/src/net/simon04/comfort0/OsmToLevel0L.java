package net.simon04.comfort0;

import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.AbstractPrimitive;
import org.openstreetmap.josm.data.osm.Changeset;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveComparator;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.Visitor;
import org.openstreetmap.josm.gui.DefaultNameFormatter;

public class OsmToLevel0L implements Visitor, AbstractPrimitive.KeyValueVisitor {

    private final StringBuilder sb = new StringBuilder();

    public void visit(List<OsmPrimitive> primitives) {
        Collections.sort(primitives, new OsmPrimitiveComparator());
        for (OsmPrimitive primitive : primitives) {
            primitive.accept(this);
        }
    }

    @Override
    public void visit(Node n) {
        appendCommon(n);
    }

    @Override
    public void visit(Way w) {
        appendCommon(w);
        for (Node node : w.getNodes()) {
            sb.append("  n").append(node.getUniqueId());
            appendDisplayName(node);
        }
    }

    @Override
    public void visit(Relation r) {
        appendCommon(r);
        for (RelationMember member : r.getMembers()) {
            sb.append("  ");
            switch (member.getType()) {
                case NODE:
                    sb.append("nd ");
                    break;
                case WAY:
                    sb.append("wy ");
                    break;
                case RELATION:
                    sb.append("rel ");
                    break;
            }
            sb.append(member.getUniqueId()).append(" ");
            sb.append(member.getRole());
            appendDisplayName(member.getMember());
        }
    }

    void appendCommon(OsmPrimitive p) {
        sb.append("\n");
        sb.append(p.getType().getAPIName()).append(" ").append(p.getUniqueId());
        if (p instanceof Node && ((Node) p).isLatLonKnown()) {
            final LatLon coor = ((Node) p).getCoor();
            sb.append(": ").append(coor.lat()).append(", ").append(coor.lon());
        }
        appendDisplayName(p);
        p.visitKeys(this);
    }

    void appendDisplayName(OsmPrimitive p) {
        sb.append(" #").append(p.getDisplayName(DefaultNameFormatter.getInstance())).append("\n");
    }

    @Override
    public void visit(Changeset cs) {
    }

    @Override
    public void visitKeyValue(AbstractPrimitive primitive, String key, String value) {
        if (key.contains("#") || key.contains("=")) {
            throw new UnsupportedOperationException("# and = are not supported in keys");
        }
        sb.append("  ").append(key).append(" = ").append(value).append("\n");
    }

    @Override
    public String toString() {
        return sb.toString().trim();
    }
}
