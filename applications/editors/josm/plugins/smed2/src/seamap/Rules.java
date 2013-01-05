/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seamap;

import s57.S57obj.Obj;
import seamap.SeaMap.Feature;

public class Rules {

	public static void MainRules (SeaMap map) {
		for (Feature feature : map.features.get(Obj.SLCONS)) shoreline(feature);
		for (Feature feature : map.features.get(Obj.PIPSOL)) pipelines(feature);
		for (Feature feature : map.features.get(Obj.CBLSUB)) cables(feature);
		for (Feature feature : map.features.get(Obj.PIPOHD)) pipelines(feature);
		for (Feature feature : map.features.get(Obj.CBLOHD)) cables(feature);
		for (Feature feature : map.features.get(Obj.TSEZNE)) separation(feature);
		for (Feature feature : map.features.get(Obj.TSSCRS)) separation(feature);
		for (Feature feature : map.features.get(Obj.TSSRON)) separation(feature);
		for (Feature feature : map.features.get(Obj.TSELNE)) separation(feature);
		for (Feature feature : map.features.get(Obj.TSSLPT)) separation(feature);
		for (Feature feature : map.features.get(Obj.TSSBND)) separation(feature);
		for (Feature feature : map.features.get(Obj.SNDWAV)) areas(feature);
		for (Feature feature : map.features.get(Obj.OSPARE)) areas(feature);
		for (Feature feature : map.features.get(Obj.FAIRWY)) areas(feature);
		for (Feature feature : map.features.get(Obj.DRGARE)) areas(feature);
		for (Feature feature : map.features.get(Obj.RESARE)) areas(feature);
		for (Feature feature : map.features.get(Obj.SPLARE)) areas(feature);
		for (Feature feature : map.features.get(Obj.SEAARE)) areas(feature);
		for (Feature feature : map.features.get(Obj.OBSTRN)) obstructions(feature);
		for (Feature feature : map.features.get(Obj.UWTROC)) obstructions(feature);
		for (Feature feature : map.features.get(Obj.MARCUL)) areas(feature);
		for (Feature feature : map.features.get(Obj.WTWAXS)) waterways(feature);
		for (Feature feature : map.features.get(Obj.RECTRC)) transits(feature);
		for (Feature feature : map.features.get(Obj.NAVLNE)) transits(feature);
		for (Feature feature : map.features.get(Obj.HRBFAC)) harbours(feature);
		for (Feature feature : map.features.get(Obj.ACHARE)) harbours(feature);
		for (Feature feature : map.features.get(Obj.ACHBRT)) harbours(feature);
		for (Feature feature : map.features.get(Obj.LOKBSN)) locks(feature);
		for (Feature feature : map.features.get(Obj.LKBSPT)) locks(feature);
		for (Feature feature : map.features.get(Obj.GATCON)) locks(feature);
		for (Feature feature : map.features.get(Obj.DISMAR)) distances(feature);
		for (Feature feature : map.features.get(Obj.HULKES)) ports(feature);
		for (Feature feature : map.features.get(Obj.CRANES)) ports(feature);
		for (Feature feature : map.features.get(Obj.LNDMRK)) landmarks(feature);
		for (Feature feature : map.features.get(Obj.MORFAC)) moorings(feature);
		for (Feature feature : map.features.get(Obj.NOTMRK)) notices(feature);
		for (Feature feature : map.features.get(Obj.SMCFAC)) marinas(feature);
		for (Feature feature : map.features.get(Obj.BRIDGE)) bridges(feature);
		for (Feature feature : map.features.get(Obj.LITMAJ)) lights(feature);
		for (Feature feature : map.features.get(Obj.LITMIN)) lights(feature);
		for (Feature feature : map.features.get(Obj.LIGHTS)) lights(feature);
		for (Feature feature : map.features.get(Obj.SISTAT)) signals(feature);
		for (Feature feature : map.features.get(Obj.SISTAW)) signals(feature);
		for (Feature feature : map.features.get(Obj.CGUSTA)) signals(feature);
		for (Feature feature : map.features.get(Obj.RDOSTA)) signals(feature);
		for (Feature feature : map.features.get(Obj.RADSTA)) signals(feature);
		for (Feature feature : map.features.get(Obj.RSCSTA)) signals(feature);
		for (Feature feature : map.features.get(Obj.PILBOP)) signals(feature);
		for (Feature feature : map.features.get(Obj.WTWGAG)) gauges(feature);
		for (Feature feature : map.features.get(Obj.OFSPLF)) platforms(feature);
		for (Feature feature : map.features.get(Obj.WRECKS)) wrecks(feature);
		for (Feature feature : map.features.get(Obj.LITVES)) floats(feature);
		for (Feature feature : map.features.get(Obj.LITFLT)) floats(feature);
		for (Feature feature : map.features.get(Obj.BOYINB)) floats(feature);
		for (Feature feature : map.features.get(Obj.BOYLAT)) buoys(feature);
		for (Feature feature : map.features.get(Obj.BOYCAR)) buoys(feature);
		for (Feature feature : map.features.get(Obj.BOYISD)) buoys(feature);
		for (Feature feature : map.features.get(Obj.BOYSAW)) buoys(feature);
		for (Feature feature : map.features.get(Obj.BOYSPP)) buoys(feature);
		for (Feature feature : map.features.get(Obj.BOYWTW)) buoys(feature);
		for (Feature feature : map.features.get(Obj.BCNLAT)) beacons(feature);
		for (Feature feature : map.features.get(Obj.BCNCAR)) beacons(feature);
		for (Feature feature : map.features.get(Obj.BCNISD)) beacons(feature);
		for (Feature feature : map.features.get(Obj.BCNSAW)) beacons(feature);
		for (Feature feature : map.features.get(Obj.BCNSPP)) beacons(feature);
		for (Feature feature : map.features.get(Obj.BCNWTW)) beacons(feature);
	}
	
	private static void shoreline(Feature feature) {}
	private static void pipelines(Feature feature) {}
	private static void cables(Feature feature) {}
	private static void separation(Feature feature) {}
	private static void areas(Feature feature) {}
	private static void obstructions(Feature feature) {}
	private static void waterways(Feature feature) {}
	private static void transits(Feature feature) {}
	private static void harbours(Feature feature) {}
	private static void locks(Feature feature) {}
	private static void distances(Feature feature) {}
	private static void ports(Feature feature) {}
	private static void landmarks(Feature feature) {}
	private static void moorings(Feature feature) {}
	private static void notices(Feature feature) {}
	private static void marinas(Feature feature) {}
	private static void bridges(Feature feature) {}
	private static void lights(Feature feature) {}
	private static void floats(Feature feature) {}
	private static void signals(Feature feature) {}
	private static void wrecks(Feature feature) {}
	private static void gauges(Feature feature) {}
	private static void platforms(Feature feature) {}
	private static void buoys(Feature feature) {}
	private static void beacons(Feature feature) {}

}
