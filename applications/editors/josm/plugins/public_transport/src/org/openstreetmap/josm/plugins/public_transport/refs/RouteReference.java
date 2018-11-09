// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.refs;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.osm.Relation;

public class RouteReference implements Comparable<RouteReference> {
    public final Relation route;

    public RouteReference(Relation route) {
        this.route = route;
    }

    @Override
    public int compareTo(RouteReference rr) {
        if (route.get("route") != null) {
            if (rr.route.get("route") == null)
                return -1;
            int result = route.get("route").compareTo(rr.route.get("route"));
            if (result != 0)
                return result;
        } else if (rr.route.get("route") != null)
            return 1;
        if (route.get("ref") != null) {
            if (rr.route.get("ref") == null)
                return -1;
            int result = route.get("ref").compareTo(rr.route.get("ref"));
            if (result != 0)
                return result;
        } else if (rr.route.get("ref") != null)
            return 1;
        if (route.get("to") != null) {
            if (rr.route.get("to") == null)
                return -1;
            int result = route.get("to").compareTo(rr.route.get("to"));
            if (result != 0)
                return result;
        } else if (rr.route.get("to") != null)
            return 1;
        if (route.get("direction") != null) {
            if (rr.route.get("direction") == null)
                return -1;
            int result = route.get("direction").compareTo(rr.route.get("direction"));
            if (result != 0)
                return result;
        } else if (rr.route.get("direction") != null)
            return 1;
        if (route.getId() < rr.route.getId())
            return -1;
        else if (route.getId() > rr.route.getId())
            return 1;
        return 0;
    }

    @Override
    public String toString() {
        String buf = route.get("route");
        if ((route.get("ref") != null) && (route.get("ref") != "")) {
            buf += " " + route.get("ref");
        }
        if ((route.get("loc_ref") != null) && (route.get("loc_ref") != "")) {
            buf += " [" + route.get("loc_ref") + "]";
        }

        if ((route.get("to") != null) && (route.get("to") != "")) {
            buf += ": " + route.get("to");
        } else if ((route.get("direction") != null) && (route.get("direction") != "")) {
            buf += " " + route.get("ref") + ": " + route.get("direction");
        } else {
            buf += " " + route.get("ref");
        }
        buf += tr(" [ID] {0}", Long.toString(route.getId()));

        return buf;
    }
}
