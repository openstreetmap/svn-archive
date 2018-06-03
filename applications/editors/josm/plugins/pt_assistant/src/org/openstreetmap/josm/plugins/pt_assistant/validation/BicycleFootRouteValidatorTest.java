// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.validation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.validation.Severity;
import org.openstreetmap.josm.data.validation.Test;
import org.openstreetmap.josm.data.validation.TestError;
import org.openstreetmap.josm.data.validation.TestError.Builder;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionTypeCalculator;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class BicycleFootRouteValidatorTest extends Test {

    public static final int ERROR_CODE_CONTINUITY = 3701;

    public BicycleFootRouteValidatorTest() {
        super(tr("Bicycle and foot routes Tests"));
    }

    @Override
    public void visit(Relation r) {

        if (r.hasIncompleteMembers()) {
            return;
        }

        if (!RouteUtils.isBicycleRoute(r)
                && !RouteUtils.isFootRoute(r)
                && !RouteUtils.isHorseRoute(r)) {
            return;
        }

        List<RelationMember> members = new ArrayList<>(r.getMembers());
        members.removeIf(m -> !m.isWay());
        WayConnectionTypeCalculator connectionTypeCalculator = new WayConnectionTypeCalculator();
        List<WayConnectionType> links = connectionTypeCalculator.updateLinks(members);

        for (Integer i : getGaps(links)) {
            Builder builder = TestError.builder(this, Severity.WARNING,
                    ERROR_CODE_CONTINUITY);
            builder.message(tr("PT: There is a gap in the {0} route", r.get("route")));
            builder.primitives(members.get(i).getWay(), members.get(i+1).getWay(), r);
            errors.add(builder.build());
        }
    }

    private List<Integer> getGaps(List<WayConnectionType> links) {
        List<Integer> gaps = new ArrayList<>();

        for (int i = 0; i < links.size() -1; i++) {
            if (!links.get(i).linkNext) {
                gaps.add(i);
            }
        }

        return gaps;
    }

}
