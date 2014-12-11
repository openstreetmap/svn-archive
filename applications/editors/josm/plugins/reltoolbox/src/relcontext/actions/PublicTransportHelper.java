package relcontext.actions;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;

/**
 * @see http://wiki.openstreetmap.org/wiki/Key:public_transport
 */

/**
 *
 * @author freeExec
 */
public final class PublicTransportHelper {

    public final static String PUBLIC_TRANSPORT = "public_transport";
    public final static String STOP_POSITION = "stop_position";
    public final static String STOP = "stop";
    public final static String STOP_AREA = "stop_area";
    public final static String PLATFORM = "platform";
    public final static String HIGHWAY = "highway";
    public final static String RAILWAY = "railway";
    public final static String BUS_STOP = "bus_stop";
    public final static String RAILWAY_HALT = "halt";
    public final static String RAILWAY_STATION = "station";    
    
    private PublicTransportHelper() {
        // Hide default constructor for utils classes
    }        
    
    public static String getRoleByMember(RelationMember m) {
        if (isMemberStop(m)) return STOP;
        else if (isMemberPlatform(m)) return PLATFORM;
        return null;
    }
    
    public static boolean isMemberStop(RelationMember m) {
        return isNodeStop(m);   // stop is only node
    }
    
    public static boolean isMemberPlatform(RelationMember m) {
        return isNodePlatform(m) || isWayPlatform(m);
    }
    
    public static boolean isNodeStop(RelationMember m) {
        return isNodeStop(m.getMember());
    }
    
    public static boolean isNodeStop(OsmPrimitive p) {
        if (p.getType() == OsmPrimitiveType.NODE && !p.isIncomplete()) {
            if (p.hasKey(PUBLIC_TRANSPORT)) {
                String pt = p.get(PUBLIC_TRANSPORT);
                if (STOP_POSITION.equals(pt)) return true;
            }
            else if (p.hasKey(RAILWAY)) {
                String rw = p.get(RAILWAY);
                if (RAILWAY_HALT.equals(rw) || RAILWAY_STATION.equals(rw)) return true;
            }
        }
        return false;
    }
    
    public static boolean isNodePlatform(RelationMember m) {
        return isNodePlatform(m.getMember());
    }
    
    public static boolean isNodePlatform(OsmPrimitive p) {
        if (p.getType() == OsmPrimitiveType.NODE && !p.isIncomplete()) {
            if (p.hasKey(PUBLIC_TRANSPORT)) {
                String pt = p.get(PUBLIC_TRANSPORT);
                if (PLATFORM.equals(pt)) return true;
            } else if (p.hasKey(HIGHWAY)) {
                String hw = p.get(HIGHWAY);
                if (BUS_STOP.equals(hw)) return true;
                else if (PLATFORM.equals(hw)) return true;
            } else if (p.hasKey(RAILWAY)) {
                String rw = p.get(RAILWAY);
                if (PLATFORM.equals(rw)) return true;
            }
        }
        return false;
    }
    public static boolean isWayPlatform(RelationMember m) {
        return isWayPlatform(m.getMember());
    }
    
    public static boolean isWayPlatform(OsmPrimitive p) {
        if (p.getType() == OsmPrimitiveType.WAY && !p.isIncomplete()) {
            if (p.hasKey(PUBLIC_TRANSPORT)) {
                String pt = p.get(PUBLIC_TRANSPORT);
                if (PLATFORM.equals(pt)) return true;
            } else if (p.hasKey(HIGHWAY)) {
                String hw = p.get(HIGHWAY);
                if (PLATFORM.equals(hw)) return true;
            } else if (p.hasKey(RAILWAY)) {
                String rw = p.get(RAILWAY);
                if (PLATFORM.equals(rw)) return true;
            }
        }
        return false;
    }
    
    public static boolean isMemberRouteway(RelationMember m) {
        return isWayRouteway(m.getMember());
    }
    
    public static boolean isWayRouteway(OsmPrimitive p) {
        if (p.getType() == OsmPrimitiveType.WAY && !p.isIncomplete()) {
            return p.hasKey(HIGHWAY) || p.hasKey(RAILWAY);
        }
        return false;
    }
    
    public static String getNameViaStoparea(RelationMember m) {
        return getNameViaStoparea(m.getMember());
    }
    
    public static String getNameViaStoparea(OsmPrimitive prim) {
        String result = prim.getName();
        if (result != null) return result;
        // try to get name by stop_area
        for (OsmPrimitive refOp : prim.getReferrers())
            if (refOp.getType() == OsmPrimitiveType.RELATION
                && refOp.hasTag(PUBLIC_TRANSPORT, STOP_AREA)) {
                result = refOp.getName();
                if (result != null) return result;
            }
        return result;
    }    
}
