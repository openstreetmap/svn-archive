package org.openstreetmap.josm.plugins.pt_assistant.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a piece of a route that includes two consecutive stops and the
 * ways between them. Route segments are ordered, i.e. for most routes there
 * will be two route segments between each pair of consecutive stops, one in
 * each direction.
 * 
 * @author darya
 *
 */

public class PTRouteSegment {

	private PTStop firstStop;
	private PTStop lastStop;
	private List<PTWay> ptways;
	
	public PTRouteSegment(PTStop firstStop, PTStop lastStop, List<PTWay> ways) {
		this.firstStop = firstStop;
		this.lastStop = lastStop;
		this.ptways = new ArrayList<>(ways.size());
		ptways.addAll(ways);
	}
	
	public List<PTWay> getPTWays() {
		return this.ptways;
	}
	
	public PTStop getFirstStop() {
		return this.firstStop;
	}
	
	public PTStop getLastStop() {
		return this.lastStop;
	}
	
	
	
	

}
