/*
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.List;
import java.util.Locale;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import org.openstreetmap.josm.tools.Pair;

/**
 * The Class OsmUtils provides some utilities not provided by the OSM data framework.
 */
public class OsmUtils {
	private OsmUtils() {}
	
	private static String cachedLocale = null;
	
	/**
	 * Gets the minimum distance of single coordinate to a way.
	 *
	 * @param coor the coordinate to get the minimum distance for.
	 * @param w the w the way to compare against
	 * @return the minimum distance between the given coordinate and the way
	 */
	public static double getMinimumDistanceToWay(LatLon coor, Way w) {
		if (coor == null || w == null) return Double.POSITIVE_INFINITY;
		
		double minDist = Double.MAX_VALUE;
		List<Pair<Node,Node>> x = w.getNodePairs(true);
		
		for (Pair<Node, Node> pair : x) {
			LatLon ap = pair.a.getCoor();
			LatLon bp = pair.b.getCoor();
			
			double dist = findMinimum(ap, bp, coor);
			if (dist < minDist) {
				minDist = dist;
			}
		}
		return minDist;
	}
	
	/**
	 * Find the minimum distance between a point and two way coordinates recursively.
	 *
	 * @param a the a the first way point coordinate
	 * @param b the b the second way point coordinate
	 * @param c the c the node coordinate
	 * @return the double the minimum distance in m of the way and the node
	 */
	private static double findMinimum(LatLon a, LatLon b, LatLon c) {
		CheckParameterUtil.ensureParameterNotNull(c, "c");
		CheckParameterUtil.ensureParameterNotNull(b, "b");
		CheckParameterUtil.ensureParameterNotNull(a, "a");
		
		LatLon mid = new LatLon((a.lat() + b.lat()) / 2, (a.lon() + b.lon()) / 2);
		
		double ac = a.greatCircleDistance(c);
		double bc = b.greatCircleDistance(c);
		double mc = mid.greatCircleDistance(c);
		
		double min = Math.min(Math.min(ac, mc), bc);
		
				
		if (min < 5.0) { // close enough?
			return min;
		}
		
		if (mc < ac && mc < bc) { 
			// mid point has lower distance than a and b
			if (ac > bc) { // recurse
				return findMinimum(b, mid, c);
			} else {
				return findMinimum(a, mid, c);
			}
		} else { // mid point is not closer than a or b
			return Math.min(ac, bc);
		}
	}
	
	/**
	 * Gets the associated street name via relation of the address node, if present.
	 *
	 * @param address the address
	 * @return true, if an associated street has been found.
	 */
	public static boolean getValuesFromRelation(OSMAddress address) {
		if (address == null) {
			return false;
		}
		
		boolean hasValuesFromRel = false; /* true, if we applied some address props from the relation */
		OsmPrimitive addrNode = address.getOsmObject();
		
		for (OsmPrimitive osm : addrNode.getReferrers()) {
			if (osm instanceof Relation) {
				Relation r = (Relation) osm;
				
				if (!TagUtils.isAssociatedStreetRelation(r)) continue;
				
				for (RelationMember rm : r.getMembers()) {
					if (TagUtils.isStreetMember(rm)) {
						OsmPrimitive street = rm.getMember();
						if (TagUtils.hasHighwayTag(street)) {
							String streetName = TagUtils.getNameValue(street);
							if (!StringUtils.isNullOrEmpty(streetName)) {
								address.setDerivedValue(TagUtils.ADDR_STREET_TAG, streetName);
								hasValuesFromRel = true;
								break;
							}
						} // TODO: Issue a warning here						
					}
				}
				
				// Check for other address properties
				if (TagUtils.hasAddrCityTag(r)) { // city
					address.setDerivedValue(TagUtils.ADDR_CITY_TAG, TagUtils.getAddrCityValue(r));
					hasValuesFromRel = true;
				}
				if (TagUtils.hasAddrCountryTag(r)) { // country
					address.setDerivedValue(TagUtils.ADDR_COUNTRY_TAG, TagUtils.getAddrCountryValue(r));
					hasValuesFromRel = true;
				}
				if (TagUtils.hasAddrPostcodeTag(r)) { // postcode
					address.setDerivedValue(TagUtils.ADDR_POSTCODE_TAG, TagUtils.getAddrPostcodeValue(r));
					hasValuesFromRel = true;
				}
			}
		}
		return hasValuesFromRel;
	}
	
	/**
	 * Gets the tag values from an address interpolation ref, if present.
	 *
	 * @param address the address
	 * @return the values from address interpolation
	 */
	public static boolean getValuesFromAddressInterpolation(OSMAddress address) {
		if (address == null) return false;
		
		OsmPrimitive osmAddr = address.getOsmObject();
		
		for (OsmPrimitive osm : osmAddr.getReferrers()) {
			if (osm instanceof Way) {
				Way w = (Way) osm;
				if (TagUtils.hasAddrInterpolationTag(w)) {
					applyDerivedValue(address, w, TagUtils.ADDR_POSTCODE_TAG);
					applyDerivedValue(address, w, TagUtils.ADDR_CITY_TAG);
					applyDerivedValue(address, w, TagUtils.ADDR_COUNTRY_TAG);
					applyDerivedValue(address, w, TagUtils.ADDR_STREET_TAG);
					applyDerivedValue(address, w, TagUtils.ADDR_STATE_TAG);
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Gets the locale code as string.
	 *
	 * @return the locale
	 */
	public static String getLocale() {
		// Check if user could prefer imperial system
		if (cachedLocale == null) {
			Locale l = Locale.getDefault();
			cachedLocale = l.toString();
		}
		return cachedLocale;
	}

	private static void applyDerivedValue(OSMAddress address, Way w, String tag) {
		if (!address.hasTag(tag) && TagUtils.hasTag(w, tag)) {						
			address.setDerivedValue(tag, w.get(tag));
		}
	}
}
