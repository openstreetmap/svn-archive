package org.openstreetmap.josm.plugins.pt_assistant.data;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Way;

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
	
	public Way getFirstWay() {
		if (ptways.isEmpty()) {
			return null;
		}
		return ptways.get(0).getWays().get(0);
	}
	
	public Way getLastWay() {
		if (ptways.isEmpty()) {
			return null;
		}
		List<Way> waysOfLast = ptways.get(ptways.size() - 1).getWays();
		return waysOfLast.get(waysOfLast.size() - 1);
	}

	/**
	 * Adds the new fix variant if an identical fix variant (i.e. same ways) is
	 * not already contained in the list of the fix variants of this.
	 * 
	 * @param list the PTWays of the new fix variant
	 */
	public synchronized void addFixVariant(List<PTWay> list) {
		List<Way> otherWays = new ArrayList<>();
		for (PTWay ptway : list) {
			otherWays.addAll(ptway.getWays());
		}

		for (List<PTWay> fixVariant : this.fixVariants) {
			List<Way> thisWays = new ArrayList<>();
			for (PTWay ptway : fixVariant) {
				thisWays.addAll(ptway.getWays());
			}
			boolean listsEqual = (thisWays.size() == otherWays.size());
			if (listsEqual) {
				for (int i = 0; i < thisWays.size(); i++) {
					if (thisWays.get(i).getId() != otherWays.get(i).getId()) {
						listsEqual = false;
						break;
					}
				}
			}
			if (listsEqual) {
				return;
			}
		}

		this.fixVariants.add(list);
	}

	public List<List<PTWay>> getFixVariants() {
		return this.fixVariants;
	}

	/**
	 * Checks if this and the other route segments are equal
	 * 
	 * @param other
	 * @return
	 */
	public boolean equalsRouteSegment(PTRouteSegment other) {

		List<Way> thisWays = new ArrayList<>();
		for (PTWay ptway : this.ptways) {
			thisWays.addAll(ptway.getWays());
		}
		List<Way> otherWays = new ArrayList<>();
		for (PTWay ptway : other.getPTWays()) {
			otherWays.addAll(ptway.getWays());
		}

		if (thisWays.size() != otherWays.size()) {
			return false;
		}

		for (int i = 0; i < thisWays.size(); i++) {
			if (thisWays.get(i).getId() != otherWays.get(i).getId()) {
				return false;
			}
		}

		return true;
	}

}
