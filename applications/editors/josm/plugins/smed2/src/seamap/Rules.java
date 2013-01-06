/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seamap;

import java.util.ArrayList;

import s57.S57val.*;
import s57.S57att.*;
import s57.S57obj.*;

import seamap.SeaMap.*;
import symbols.Beacons;
import symbols.Buoys;
import symbols.Harbours;
import symbols.Landmarks;
import symbols.Symbols.Instr;

public class Rules {

	static SeaMap map;
	static int zoom;
	
	public static void MainRules (SeaMap m, int z) {
		map = m;
		zoom = z;
		ArrayList<Feature> feature;
		if ((feature = map.features.get(Obj.SLCONS)) != null) shoreline(feature);
		if ((feature = map.features.get(Obj.SLCONS)) != null) shoreline(feature);
		if ((feature = map.features.get(Obj.PIPSOL)) != null) pipelines(feature);
		if ((feature = map.features.get(Obj.CBLSUB)) != null) cables(feature);
		if ((feature = map.features.get(Obj.PIPOHD)) != null) pipelines(feature);
		if ((feature = map.features.get(Obj.CBLOHD)) != null) cables(feature);
		if ((feature = map.features.get(Obj.TSEZNE)) != null) separation(feature);
		if ((feature = map.features.get(Obj.TSSCRS)) != null) separation(feature);
		if ((feature = map.features.get(Obj.TSSRON)) != null) separation(feature);
		if ((feature = map.features.get(Obj.TSELNE)) != null) separation(feature);
		if ((feature = map.features.get(Obj.TSSLPT)) != null) separation(feature);
		if ((feature = map.features.get(Obj.TSSBND)) != null) separation(feature);
		if ((feature = map.features.get(Obj.SNDWAV)) != null) areas(feature);
		if ((feature = map.features.get(Obj.OSPARE)) != null) areas(feature);
		if ((feature = map.features.get(Obj.FAIRWY)) != null) areas(feature);
		if ((feature = map.features.get(Obj.DRGARE)) != null) areas(feature);
		if ((feature = map.features.get(Obj.RESARE)) != null) areas(feature);
		if ((feature = map.features.get(Obj.SPLARE)) != null) areas(feature);
		if ((feature = map.features.get(Obj.SEAARE)) != null) areas(feature);
		if ((feature = map.features.get(Obj.OBSTRN)) != null) obstructions(feature);
		if ((feature = map.features.get(Obj.UWTROC)) != null) obstructions(feature);
		if ((feature = map.features.get(Obj.MARCUL)) != null) areas(feature);
		if ((feature = map.features.get(Obj.WTWAXS)) != null) waterways(feature);
		if ((feature = map.features.get(Obj.RECTRC)) != null) transits(feature);
		if ((feature = map.features.get(Obj.NAVLNE)) != null) transits(feature);
		if ((feature = map.features.get(Obj.HRBFAC)) != null) harbours(feature);
		if ((feature = map.features.get(Obj.ACHARE)) != null) harbours(feature);
		if ((feature = map.features.get(Obj.ACHBRT)) != null) harbours(feature);
		if ((feature = map.features.get(Obj.LOKBSN)) != null) locks(feature);
		if ((feature = map.features.get(Obj.LKBSPT)) != null) locks(feature);
		if ((feature = map.features.get(Obj.GATCON)) != null) locks(feature);
		if ((feature = map.features.get(Obj.DISMAR)) != null) distances(feature);
		if ((feature = map.features.get(Obj.HULKES)) != null) ports(feature);
		if ((feature = map.features.get(Obj.CRANES)) != null) ports(feature);
		if ((feature = map.features.get(Obj.LNDMRK)) != null) landmarks(feature);
		if ((feature = map.features.get(Obj.MORFAC)) != null) moorings(feature);
		if ((feature = map.features.get(Obj.NOTMRK)) != null) notices(feature);
		if ((feature = map.features.get(Obj.SMCFAC)) != null) marinas(feature);
		if ((feature = map.features.get(Obj.BRIDGE)) != null) bridges(feature);
		if ((feature = map.features.get(Obj.LITMAJ)) != null) lights(feature);
		if ((feature = map.features.get(Obj.LITMIN)) != null) lights(feature);
		if ((feature = map.features.get(Obj.LIGHTS)) != null) lights(feature);
		if ((feature = map.features.get(Obj.SISTAT)) != null) signals(feature);
		if ((feature = map.features.get(Obj.SISTAW)) != null) signals(feature);
		if ((feature = map.features.get(Obj.CGUSTA)) != null) signals(feature);
		if ((feature = map.features.get(Obj.RDOSTA)) != null) signals(feature);
		if ((feature = map.features.get(Obj.RADSTA)) != null) signals(feature);
		if ((feature = map.features.get(Obj.RSCSTA)) != null) signals(feature);
		if ((feature = map.features.get(Obj.PILBOP)) != null) signals(feature);
		if ((feature = map.features.get(Obj.WTWGAG)) != null) gauges(feature);
		if ((feature = map.features.get(Obj.OFSPLF)) != null) platforms(feature);
		if ((feature = map.features.get(Obj.WRECKS)) != null) wrecks(feature);
		if ((feature = map.features.get(Obj.LITVES)) != null) floats(feature);
		if ((feature = map.features.get(Obj.LITFLT)) != null) floats(feature);
		if ((feature = map.features.get(Obj.BOYINB)) != null) floats(feature);
		if ((feature = map.features.get(Obj.BOYLAT)) != null) buoys(feature);
		if ((feature = map.features.get(Obj.BOYCAR)) != null) buoys(feature);
		if ((feature = map.features.get(Obj.BOYISD)) != null) buoys(feature);
		if ((feature = map.features.get(Obj.BOYSAW)) != null) buoys(feature);
		if ((feature = map.features.get(Obj.BOYSPP)) != null) buoys(feature);
		if ((feature = map.features.get(Obj.BOYWTW)) != null) buoys(feature);
		if ((feature = map.features.get(Obj.BCNLAT)) != null) beacons(feature);
		if ((feature = map.features.get(Obj.BCNCAR)) != null) beacons(feature);
		if ((feature = map.features.get(Obj.BCNISD)) != null) beacons(feature);
		if ((feature = map.features.get(Obj.BCNSAW)) != null) beacons(feature);
		if ((feature = map.features.get(Obj.BCNSPP)) != null) beacons(feature);
		if ((feature = map.features.get(Obj.BCNWTW)) != null) beacons(feature);
	}
	
	private static void shoreline(ArrayList<Feature> features) {
//		for (Feature feature : features) {
//		}
	}
	private static void pipelines(ArrayList<Feature> features) {}
	private static void cables(ArrayList<Feature> features) {}
	private static void separation(ArrayList<Feature> features) {}
	private static void areas(ArrayList<Feature> features) {}
	private static void obstructions(ArrayList<Feature> features) {}
	private static void waterways(ArrayList<Feature> features) {}
	private static void transits(ArrayList<Feature> features) {}
	private static void harbours(ArrayList<Feature> features) {}
	private static void locks(ArrayList<Feature> features) {}
	private static void distances(ArrayList<Feature> features) {}
	private static void ports(ArrayList<Feature> features) {}
	private static void landmarks(ArrayList<Feature> features) {
		for (Feature feature : features) {
			ArrayList<CatLMK> cats = (ArrayList<CatLMK>) Renderer.getAttVal(feature, feature.type, 0, Att.CATLMK);
			ArrayList<Instr> catSym = Landmarks.Shapes.get(cats.get(0));
			ArrayList<FncFNC> fncs = (ArrayList<FncFNC>) Renderer.getAttVal(feature, feature.type, 0, Att.FUNCTN);
			ArrayList<Instr> fncSym = Landmarks.Funcs.get(fncs.get(0));
			if ((fncs.get(0) == FncFNC.FNC_CHCH) && (cats.get(0) == CatLMK.LMK_TOWR)) catSym = Landmarks.ChurchTower;
			Renderer.symbol(feature, catSym, feature.type);
			Renderer.symbol(feature, fncSym, feature.type);
		}
	}
	private static void moorings(ArrayList<Feature> features) {
		for (Feature feature : features) {
			CatMOR cat = (CatMOR) Renderer.getAttVal(feature, feature.type, 0, Att.CATMOR);
			switch (cat) {
			case MOR_DLPN:
				Renderer.symbol(feature, Harbours.Dolphin, feature.type);
				break;
			case MOR_DDPN:
				Renderer.symbol(feature, Harbours.DeviationDolphin, feature.type);
				break;
			case MOR_BLRD:
			case MOR_POST:
				Renderer.symbol(feature, Harbours.Bollard, feature.type);
				break;
			case MOR_BUOY:
				BoySHP shape = (BoySHP) Renderer.getAttVal(feature, feature.type, 0, Att.BOYSHP);
				if (shape == BoySHP.BOY_UNKN) shape = BoySHP.BOY_SPHR;
				Renderer.symbol(feature, Buoys.Shapes.get(shape), feature.type);
				break;
			}
		}
	}
	private static void notices(ArrayList<Feature> features) {}
	private static void marinas(ArrayList<Feature> features) {}
	private static void bridges(ArrayList<Feature> features) {}
	private static void wrecks(ArrayList<Feature> features) {}
	private static void gauges(ArrayList<Feature> features) {}
	private static void lights(ArrayList<Feature> features) {
		for (Feature feature : features) {
			switch (feature.type) {
			case LITMAJ:
				Renderer.symbol(feature, Beacons.LightMajor, feature.type);
				break;
			case LITMIN:
			case LIGHTS:
				Renderer.symbol(feature, Beacons.LightMinor, feature.type);
				break;
			}
		}
	}
	private static void signals(ArrayList<Feature> features) {
		for (Feature feature : features) {
			switch (feature.type) {
			case SISTAT:
			case SISTAW:
				Renderer.symbol(feature, Harbours.SignalStation, feature.type);
				break;
			case RDOSTA:
				Renderer.symbol(feature, Harbours.SignalStation, feature.type);
				break;
			case RADSTA:
				Renderer.symbol(feature, Harbours.SignalStation, feature.type);
				break;
			case PILBOP:
				Renderer.symbol(feature, Harbours.SignalStation, feature.type);
				break;
			case CGUSTA:
//			Renderer.symbol(feature, Harbours.CGuardStation, feature.type);
			break;
			case RSCSTA:
//				Renderer.symbol(feature, Harbours.RescueStation, feature.type);
				break;
			}
		}
	}
	private static void floats(ArrayList<Feature> features) {
		for (Feature feature : features) {
			switch (feature.type) {
			case LITVES:
				Renderer.symbol(feature, Buoys.Super, feature.type);
				break;
			case LITFLT:
				Renderer.symbol(feature, Buoys.Float, feature.type);
				break;
			case BOYINB:
				Renderer.symbol(feature, Buoys.Storage, feature.type);
				break;
			}
		}
	}
	private static void platforms(ArrayList<Feature> features) {
		for (Feature feature : features) {
			Renderer.symbol(feature, Landmarks.Platform, feature.type);
		}
	}
	private static void buoys(ArrayList<Feature> features) {
		for (Feature feature : features) {
			BoySHP shape = (BoySHP) Renderer.getAttVal(feature, feature.type, 0, Att.BOYSHP);
			Renderer.symbol(feature, Buoys.Shapes.get(shape), feature.type);
		}
	}
	private static void beacons(ArrayList<Feature> features) {
		for (Feature feature : features) {
			BcnSHP shape = (BcnSHP) Renderer.getAttVal(feature, feature.type, 0, Att.BCNSHP);
			if (((shape == BcnSHP.BCN_PRCH) || (shape == BcnSHP.BCN_WTHY)) && (feature.type == Obj.BCNLAT)) {
				CatLAM cat = (CatLAM) Renderer.getAttVal(feature, feature.type, 0, Att.CATLAM);
				switch (cat) {
				case LAM_PORT:
					if (shape == BcnSHP.BCN_PRCH)
						Renderer.symbol(feature, Beacons.PerchPort, feature.type);
					else
						Renderer.symbol(feature, Beacons.WithyPort, feature.type);
					break;
				case LAM_STBD:
					if (shape == BcnSHP.BCN_PRCH)
						Renderer.symbol(feature, Beacons.PerchStarboard, feature.type);
					else
						Renderer.symbol(feature, Beacons.WithyStarboard, feature.type);
					break;
				default:
					Renderer.symbol(feature, Beacons.Stake, feature.type);
				}
			} else {
				Renderer.symbol(feature, Beacons.Shapes.get(shape), feature.type);
			}
		}
	}
}
