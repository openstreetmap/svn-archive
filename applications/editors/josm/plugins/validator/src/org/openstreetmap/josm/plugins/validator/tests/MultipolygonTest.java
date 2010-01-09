package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.visitor.paint.relations.Multipolygon;
import org.openstreetmap.josm.data.osm.visitor.paint.relations.Multipolygon.PolyData;
import org.openstreetmap.josm.data.osm.visitor.paint.relations.Multipolygon.PolyData.Intersection;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

public class MultipolygonTest extends Test {

    protected static final int WRONG_MEMBER_TYPE = 1601;
    protected static final int WRONG_MEMBER_ROLE = 1602;
    protected static final int NON_CLOSED_WAY = 1603;
    protected static final int MISSING_OUTER_WAY = 1604;
    protected static final int INNER_WAY_OUTSIDE = 1605;
    protected static final int CROSSING_WAYS = 1606;

    public MultipolygonTest() {
        super(tr("Multipolygon"),
                tr("This test checks if multipolygons are valid"));
    }

    @Override
    public void visit(Relation r) {
        if ("multipolygon".equals(r.get("type"))) {
            checkMembersAndRoles(r);

            Multipolygon polygon = new Multipolygon(Main.map.mapView);
            polygon.load(r);

            if (polygon.hasNonClosedWays()) {
                errors.add( new TestError(this, Severity.WARNING, tr("Multipolygon is not closed"), NON_CLOSED_WAY,  r));
            }

            if (polygon.getOuterWays().isEmpty()) {
                errors.add( new TestError(this, Severity.WARNING, tr("No outer way for multipolygon"), MISSING_OUTER_WAY,  r));
            }

            for (RelationMember rm: r.getMembers()) {
                if (!rm.getMember().isUsable()) {
                    return; // Rest of checks is only for complete multipolygons
                }
            }

            for (PolyData pdInner: polygon.getInnerPolygons()) {
                PolyData pdOuter = polygon.findOuterPolygon(pdInner, polygon.getOuterPolygons());
                if (pdOuter == null) {
                    errors.add(new TestError(this, Severity.WARNING, tr("Multipolygon inner way is outside."), INNER_WAY_OUTSIDE,  r));
                } else if (pdOuter.contains(pdInner.poly) == Intersection.CROSSING) {
                    errors.add(new TestError(this, Severity.WARNING, tr("Intersection between multipolygon ways"), CROSSING_WAYS, r));
                }
            }
        }
    }

    private void checkMembersAndRoles(Relation r) {
        for (RelationMember rm: r.getMembers()) {
            if (rm.isWay()) {
                if (!("inner".equals(rm.getRole()) || "outer".equals(rm.getRole()) || !rm.hasRole())) {
                    errors.add( new TestError(this, Severity.WARNING, tr("No useful role for multipolygon member"), WRONG_MEMBER_ROLE, rm.getMember()));
                }
            } else {
                errors.add( new TestError(this, Severity.WARNING, tr("Non-Way in multipolygon."), WRONG_MEMBER_TYPE, rm.getMember()));
            }
        }
    }


}
