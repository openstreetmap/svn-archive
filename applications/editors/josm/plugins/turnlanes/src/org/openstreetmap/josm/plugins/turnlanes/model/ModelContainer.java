// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.turnlanes.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.turnlanes.CollectionUtils;
import org.openstreetmap.josm.tools.Pair;

public final class ModelContainer {

    //set default direction of traffic for connecting lanes
    private static final boolean DEFAULT_LEFT_DIRECTION = false;

    private static final ModelContainer EMPTY = new ModelContainer(Collections.<Node>emptySet(),
            Collections.<Way>emptySet(), false, DEFAULT_LEFT_DIRECTION);

    public static ModelContainer create(Iterable<Node> primaryNodes, Iterable<Way> primaryWays) {
        return new ModelContainer(new HashSet<>(CollectionUtils.toList(primaryNodes)), new HashSet<>(
                CollectionUtils.toList(primaryWays)), false, DEFAULT_LEFT_DIRECTION);
    }

    public static ModelContainer createEmpty(Iterable<Node> primaryNodes, Iterable<Way> primaryWays) {
        return new ModelContainer(new HashSet<>(CollectionUtils.toList(primaryNodes)), new HashSet<>(
                CollectionUtils.toList(primaryWays)), true, DEFAULT_LEFT_DIRECTION);
    }

    public static ModelContainer empty() {
        return EMPTY;
    }

    private static void close(Set<Node> closedNodes, Set<Way> closedWays) {
        boolean closed = false;

        while (!closed) {
            closed = true;

            for (Node n : new ArrayList<>(closedNodes)) {
                for (Way w : TurnlanesUtils.filterRoads(n.getReferrers())) {
                    if (w.isFirstLastNode(n)) {
                        closed &= close(closedNodes, closedWays, w);
                    }
                }

                for (Way w : new ArrayList<>(closedWays)) {
                    closed &= close(closedNodes, closedWays, w);
                }
            }
        }
    }

    private static boolean close(Set<Node> closedNodes, Set<Way> closedWays, Way w) {
        boolean closed = true;

        for (Relation r : org.openstreetmap.josm.tools.Utils.filteredCollection(w.getReferrers(), Relation.class)) {
            if (!r.get("type").equals(Constants.TYPE_TURNS)) {
                continue;
            }

            for (RelationMember m : r.getMembers()) {
                if (m.getRole().equals(Constants.TURN_ROLE_VIA) && m.getMember().equals(w)) {
                    closed &= close(closedNodes, closedWays, r);
                }
            }
        }

        return closed;
    }

    private static boolean close(Set<Node> closedNodes, Set<Way> closedWays, Relation r) {
        boolean closed = true;

        final List<Way> via = new ArrayList<>();
        for (RelationMember m : TurnlanesUtils.getMembers(r, Constants.TURN_ROLE_VIA)) {
            if (m.isWay()) {
                closed &= !closedWays.add(m.getWay());
                via.add(m.getWay());
            } else if (m.isNode()) {
                closed &= !closedNodes.add(m.getNode());
            }
        }

        if (!via.isEmpty()) {
            final Way from = TurnlanesUtils.getMemberWay(r, Constants.TURN_ROLE_FROM);
            final Way to = TurnlanesUtils.getMemberWay(r, Constants.TURN_ROLE_TO);

            closed &= !closedNodes.add(TurnlanesUtils.lineUp(from, via.get(0)));
            closed &= !closedNodes.add(TurnlanesUtils.lineUp(via.get(via.size() - 1), to));
        }

        return closed;
    }

    private static <E extends OsmPrimitive, C extends Collection<E>> C filterUsables(C collection) {
        final Iterator<E> it = collection.iterator();

        while (it.hasNext()) {
            final E e = it.next();

            if (e.getDataSet() == null || !e.isUsable()) {
                it.remove();
            }
        }

        return collection;
    }

    private final Map<Node, Junction> junctions = new HashMap<>();
    private final Map<Way, Road> roads = new HashMap<>();

    private final Set<Node> primaryNodes;
    private final Set<Way> primaryWays;

    private final boolean empty;

    private boolean leftDirection;

    private ModelContainer(Set<Node> primaryNodes, Set<Way> primaryWays, boolean empty, boolean leftDirection) {
        if (empty) {
            this.primaryNodes = Collections.unmodifiableSet(new HashSet<>(primaryNodes));
            this.primaryWays = Collections.unmodifiableSet(new HashSet<>(primaryWays));
            this.empty = true;
        } else {
            final Set<Node> closedNodes = filterUsables(new HashSet<>(primaryNodes));
            final Set<Way> closedWays = filterUsables(new HashSet<>(primaryWays));

            close(closedNodes, closedWays);

            this.primaryNodes = Collections.unmodifiableSet(closedNodes);
            this.primaryWays = Collections.unmodifiableSet(closedWays);

            for (Pair<Way, Junction> w : createPrimaryJunctions()) {
                if (!this.primaryWays.contains(w.a)) {
                    addRoad(new Road(this, w.a, w.b));
                }
            }

            for (Route r : TurnlanesUtils.orderWays(this.primaryWays, this.primaryNodes)) {
                addRoad(new Road(this, r));
            }

            for (Road r : roads.values()) {
                r.initialize();
            }

            this.empty = junctions.isEmpty();
        }
        this.leftDirection = leftDirection;
    }

    private Set<Pair<Way, Junction>> createPrimaryJunctions() {
        final Set<Pair<Way, Junction>> roads = new HashSet<>();

        for (Node n : primaryNodes) {
            final List<Way> ws = new ArrayList<>();
            for (Way w : TurnlanesUtils.filterRoads(n.getReferrers())) {
                if (w.isFirstLastNode(n)) {
                    ws.add(w);
                }
            }

            if (ws.size() > 1) {
                final Junction j = register(new Junction(this, n));
                for (Way w : ws) {
                    roads.add(new Pair<>(w, j));
                }
            }
        }

        return roads;
    }

    Junction getOrCreateJunction(Node n) {
        final Junction existing = junctions.get(n);

        if (existing != null) {
            return existing;
        }

        return register(new Junction(this, n));
    }

    public Junction getJunction(Node n) {
        Junction j = junctions.get(n);

        if (j == null) {
            throw new IllegalArgumentException();
        }

        return j;
    }

    Road getRoad(Way w) {
        final Road r = roads.get(w);

        if (r == null) {
            throw new IllegalArgumentException("There is no road containing the given way.");
        }

        return r;
    }

    private void addRoad(Road newRoad, Road mergedA, Road mergedB) {
        assert (mergedA == null) == (mergedB == null);

        for (Route.Segment s : newRoad.getRoute().getSegments()) {
            final Road oldRoad = roads.put(s.getWay(), newRoad);

            if (oldRoad != null) {
                if (mergedA == null) {
                    addRoad(mergeRoads(oldRoad, newRoad), oldRoad, newRoad);
                } else if (!oldRoad.equals(mergedA) && !oldRoad.equals(mergedB)) {
                    throw new RuntimeException("A road can't be connected to more than two junctions.");
                }
            }
        }
    }

    private void addRoad(Road newRoad) {
        addRoad(newRoad, null, null);
    }

    private Road mergeRoads(Road a, Road b) {
        final String ERR_ILLEGAL_ARGS = "The given roads can not be merged into one.";

        final List<Way> ws = new ArrayList<>(CollectionUtils.toList(CollectionUtils.reverse(a.getRoute().getWays())));
        final List<Way> bws = b.getRoute().getWays();

        int i = -1;
        for (Way w : ws) {
            if (w.equals(bws.get(i + 1))) {
                ++i;
            } else if (i >= 0) {
                throw new IllegalArgumentException(ERR_ILLEGAL_ARGS);
            }
        }

        if (i < 0) {
            throw new IllegalArgumentException(ERR_ILLEGAL_ARGS);
        }
        ws.addAll(bws.subList(i + 1, bws.size()));

        final Route mergedRoute = Route.create(ws, a.getRoute().getLastSegment().getEnd());
        return new Road(this, mergedRoute);
    }

    private Junction register(Junction j) {
        if (junctions.put(j.getNode(), j) != null) {
            throw new IllegalStateException();
        }

        return j;
    }

    public Set<Junction> getPrimaryJunctions() {
        if (empty) {
            return Collections.emptySet();
        }

        final Set<Junction> pjs = new HashSet<>();
        for (Node n : primaryNodes) {
            pjs.add(getJunction(n));
        }
        return pjs;
    }

    public Set<Road> getPrimaryRoads() {
        if (empty) {
            return Collections.emptySet();
        }

        final Set<Road> prs = new HashSet<>();
        for (Way w : primaryWays) {
            prs.add(roads.get(w));
        }
        return prs;
    }

    public ModelContainer recalculate() {
        return new ModelContainer(primaryNodes, primaryWays, false, leftDirection);
    }

    public boolean isPrimary(Junction j) {
        return primaryNodes.contains(j.getNode());
    }

    public boolean isPrimary(Road r) {
        return primaryWays.contains(r.getRoute().getFirstSegment().getWay());
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isLeftDirection() {
        return leftDirection;
    }

    public void setLeftDirection(boolean leftDirection) {
        this.leftDirection = leftDirection;
    }

    public boolean hasRoad(Way w) {
        return roads.containsKey(w);
    }

    public boolean hasJunction(Node n) {
        return junctions.containsKey(n);
    }
}
