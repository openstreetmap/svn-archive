package org.openstreetmap.josm.plugins.turnlanes.gui;

import org.openstreetmap.josm.plugins.turnlanes.model.Road;

interface State {
	public class Invalid implements State {
		private final State wrapped;
		
		public Invalid(State wrapped) {
			this.wrapped = wrapped;
		}
		
		public JunctionGui getJunction() {
			return wrapped.getJunction();
		}
		
		public State unwrap() {
			return wrapped;
		}
	}
	
	public class Dirty implements State {
		private final State wrapped;
		
		public Dirty(State wrapped) {
			this.wrapped = wrapped;
		}
		
		public JunctionGui getJunction() {
			return wrapped.getJunction();
		}
		
		public State unwrap() {
			return wrapped;
		}
	}
	
	class Default implements State {
		private final JunctionGui junction;
		
		public Default(JunctionGui junction) {
			this.junction = junction;
		}
		
		public JunctionGui getJunction() {
			return junction;
		}
	}
	
	class IncomingActive implements State {
		private final JunctionGui junction;
		private final Road.End roadEnd;
		
		public IncomingActive(JunctionGui junction, Road.End roadEnd) {
			this.junction = junction;
			this.roadEnd = roadEnd;
		}
		
		public Road.End getRoadEnd() {
			return roadEnd;
		}
		
		@Override
		public JunctionGui getJunction() {
			return junction;
		}
	}
	
	class OutgoingActive implements State {
		private final JunctionGui junction;
		private final LaneGui lane;
		
		public OutgoingActive(JunctionGui junction, LaneGui lane) {
			this.junction = junction;
			this.lane = lane;
		}
		
		public LaneGui getLane() {
			return lane;
		}
		
		@Override
		public JunctionGui getJunction() {
			return junction;
		}
	}
	
	JunctionGui getJunction();
}
