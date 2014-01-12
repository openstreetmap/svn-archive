package org.openstreetmap.josm.plugins.turnlanes.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.plugins.turnlanes.gui.RoadGui.ViaConnector;
import org.openstreetmap.josm.plugins.turnlanes.model.Junction;
import org.openstreetmap.josm.plugins.turnlanes.model.Lane;
import org.openstreetmap.josm.plugins.turnlanes.model.Road;

abstract class State {
    static class AllTurns extends State {
        private final State wrapped;
        
        public AllTurns(State wrapped) {
            this.wrapped = wrapped;
        }
        
        public State unwrap() {
            return wrapped;
        }
        
        @Override
        State carryOver(GuiContainer newContainer) {
            return new AllTurns(wrapped.carryOver(newContainer));
        }
    }
    
    static class Connecting extends State {
        private final Lane lane;
        private final List<RoadGui.ViaConnector> vias;
        
        public Connecting(Lane lane) {
            this(lane, Collections.<RoadGui.ViaConnector> emptyList());
        }
        
        public Connecting(Lane lane, List<ViaConnector> vias) {
            this.lane = lane;
            this.vias = vias;
        }
        
        public Connecting next(RoadGui.ViaConnector via) {
            if (vias.isEmpty()) {
                return new Connecting(lane, Collections.unmodifiableList(Arrays.asList(via)));
            }
            
            final List<RoadGui.ViaConnector> tmp = new ArrayList<RoadGui.ViaConnector>(vias.size() + 1);
            final boolean even = (vias.size() & 1) == 0;
            final RoadGui.ViaConnector last = vias.get(vias.size() - 1);
            
            if (last.equals(via) || !even && last.getRoadEnd().getJunction().equals(via.getRoadEnd().getJunction())) {
                return pop().next(via);
            }
            
            if (vias.size() >= 2) {
                if (lane.getOutgoingJunction().equals(via.getRoadEnd().getJunction())) {
                    return new Connecting(lane);
                } else if (via.equals(getBacktrackViaConnector())) {
                    return new Connecting(lane, vias.subList(0, vias.size() - 1));
                }
            }
            
            for (RoadGui.ViaConnector v : vias) {
                tmp.add(v);
                
                if (!(even && v.equals(last)) && v.getRoadEnd().getJunction().equals(via.getRoadEnd().getJunction())) {
                    return new Connecting(lane, Collections.unmodifiableList(tmp));
                }
            }
            
            tmp.add(via);
            return new Connecting(lane, Collections.unmodifiableList(tmp));
        }
        
        public Junction getJunction() {
            return vias.isEmpty() ? lane.getOutgoingJunction() : vias.get(vias.size() - 1).getRoadEnd().getJunction();
        }
        
        public RoadGui.ViaConnector getBacktrackViaConnector() {
            return vias.size() < 2 ? null : vias.get(vias.size() - 2);
        }
        
        public List<RoadGui.ViaConnector> getViaConnectors() {
            return vias;
        }
        
        public Lane getLane() {
            return lane;
        }
        
        public Connecting pop() {
            return new Connecting(lane, vias.subList(0, vias.size() - 1));
        }
    }
    
    static class Invalid extends State {
        private final State wrapped;
        
        public Invalid(State wrapped) {
            this.wrapped = wrapped;
        }
        
        public State unwrap() {
            return wrapped;
        }
    }
    
    static class Dirty extends State {
        private final State wrapped;
        
        public Dirty(State wrapped) {
            this.wrapped = wrapped;
        }
        
        public State unwrap() {
            return wrapped;
        }
        
        @Override
        State carryOver(GuiContainer newContainer) {
            return new Dirty(wrapped.carryOver(newContainer));
        }
    }
    
    static class Default extends State {
        public Default() {}
    }
    
    static class IncomingActive extends State {
        private final Road.End roadEnd;
        
        public IncomingActive(Road.End roadEnd) {
            this.roadEnd = roadEnd;
        }
        
        public Road.End getRoadEnd() {
            return roadEnd;
        }
        
        @Override
        State carryOver(GuiContainer newContainer) {
            if (newContainer.getModel().equals(roadEnd.getRoad().getContainer())) {
                return this;
            }
            
            final Junction newJunction = newContainer.getModel().getJunction(roadEnd.getJunction().getNode());
            
            for (Road.End e : newJunction.getRoadEnds()) {
                if (e.isToEnd() && e.getWay().equals(roadEnd.getWay())) {
                    return new IncomingActive(e);
                }
            }
            
            return new Default();
        }
    }
    
    static class OutgoingActive extends State {
        private final LaneGui lane;
        
        public OutgoingActive(LaneGui lane) {
            this.lane = lane;
        }
        
        public LaneGui getLane() {
            return lane;
        }
        
        @Override
        State delete() {
            if (!lane.getModel().isExtra()) {
                return this;
            }
            
            lane.getModel().remove();
            
            return new Invalid(this);
        }
        
        @Override
        State carryOver(GuiContainer newContainer) {
            if (newContainer.equals(lane.getContainer())) {
                return this;
            }
            
            final Lane model = lane.getModel();
            final Junction newJunction = newContainer.getModel().getJunction(model.getOutgoingJunction().getNode());
            
            for (Road.End e : newJunction.getRoadEnds()) {
                if (e.isToEnd() && e.getWay().equals(model.getOutgoingRoadEnd().getWay())) {
                    for (Lane l : e.getLanes()) { // e.getLane(...) can fail on lane removal
                        if (l.getKind() == model.getKind() && l.getIndex() == model.getIndex()) {
                            return new OutgoingActive(newContainer.getGui(l));
                        }
                    }
                    
                    break;
                }
            }
            
            return new Default();
        }
    }
    
    State delete() {
        return this;
    }
    
    State carryOver(GuiContainer newContainer) {
        return this;
    }
}
