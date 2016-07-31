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
	private List<List<PTWay>> fixVariants;
	
	public PTRouteSegment(PTStop firstStop, PTStop lastStop, List<PTWay> ways) {
		this.firstStop = firstStop;
		this.lastStop = lastStop;
		this.ptways = new ArrayList<>(ways.size());
		ptways.addAll(ways);
		fixVariants = new ArrayList<>();
	}
	
	public List<PTWay> getPTWays() {
		return this.ptways;
	}
	
	public void setPTWays(List<PTWay> ptwayList) {
		this.ptways = ptwayList;
		this.fixVariants.clear();
	}
	
	public PTStop getFirstStop() {
		return this.firstStop;
	}
	
	public PTStop getLastStop() {
		return this.lastStop;
	}
	
	public PTWay getFirstPTWay() {
		if (ptways.isEmpty()) {
			return null;
		}
		return ptways.get(0);
	}
	
	public PTWay getLastPTWay() {
		if (ptways.isEmpty()) {
			return null;
		}
		return ptways.get(ptways.size() - 1);
	}
	
	public void addFixVariant(List<PTWay> list) {
		this.fixVariants.add(list);
	}
	
	public List<List<PTWay>> getFixVariants() {
		return this.fixVariants;
	}
	

}
