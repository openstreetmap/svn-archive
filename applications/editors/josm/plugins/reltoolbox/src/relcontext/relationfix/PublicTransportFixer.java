// License: GPL. For details, see LICENSE file.
package relcontext.relationfix;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.MainApplication;

import relcontext.actions.PublicTransportHelper;

/**
 * @see https://wiki.openstreetmap.org/wiki/Key:public_transport
 */

/**
 * Helper function for determinate role in public_transport relation
 * @author freeExec
 */
public class PublicTransportFixer extends RelationFixer {

    public PublicTransportFixer() {
        super("route", "public_transport");
    }

    /*protected PublicTransportFixer(String...types) {
        super(types);
    }*/

    @Override
    public boolean isRelationGood(Relation rel) {
        for (RelationMember m : rel.getMembers()) {
            if (m.getType().equals(OsmPrimitiveType.NODE)
                    && !(m.getRole().startsWith(PublicTransportHelper.STOP) || m.getRole().startsWith(PublicTransportHelper.PLATFORM))) {
                setWarningMessage(tr("Node without ''stop'' or ''platform'' role found"));
                return false;
            }
            if (m.getType().equals(OsmPrimitiveType.WAY)
                    && PublicTransportHelper.isWayPlatform(m)
                    && !m.getRole().startsWith(PublicTransportHelper.PLATFORM)) {
                setWarningMessage(tr("Way platform without ''platform'' role found") + " r" + m.getUniqueId());
                return false;
            }
        }
        clearWarningMessage();
        return true;
    }

    /*@Override
    public boolean isFixerApplicable(Relation rel) {
        return true;
    }*/

    @Override
    public Command fixRelation(Relation rel) {
        Relation r = rel;
        Relation rr = fixStopPlatformRole(r);
        boolean fixed = false;
        if (rr != null) {
            fixed = true;
            r = rr;
        }
        return fixed ? new ChangeCommand(MainApplication.getLayerManager().getEditDataSet(), rel, r) : null;
    }

    private Relation fixStopPlatformRole(Relation source) {
        Relation r = new Relation(source);
        boolean fixed = false;
        for (int i = 0; i < r.getMembersCount(); i++) {
            RelationMember m = r.getMember(i);
            String role = PublicTransportHelper.getRoleByMember(m);

            if (role != null && !m.getRole().startsWith(role)) {
                r.setMember(i, new RelationMember(role, m.getMember()));
                fixed = true;
            }
        }
        return fixed ? r : null;
    }
}
