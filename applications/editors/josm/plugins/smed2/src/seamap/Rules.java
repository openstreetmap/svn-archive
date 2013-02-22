/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seamap;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import s57.S57val.*;
import s57.S57att.*;
import s57.S57obj.*;

import seamap.SeaMap.*;
import symbols.*;
import symbols.Symbols.*;

public class Rules {

	static SeaMap map;
	static int zoom;
	
	public static void rules (SeaMap m, int z) {
		map = m;
		zoom = z;
		ArrayList<Feature> objects;
		if ((objects = map.features.get(Obj.SLCONS)) != null) for (Feature feature : objects) shoreline(feature);
		if ((objects = map.features.get(Obj.SLCONS)) != null) for (Feature feature : objects) shoreline(feature);;
		if ((objects = map.features.get(Obj.PIPSOL)) != null) for (Feature feature : objects) pipelines(feature);
		if ((objects = map.features.get(Obj.CBLSUB)) != null) for (Feature feature : objects) cables(feature);
		if ((objects = map.features.get(Obj.PIPOHD)) != null) for (Feature feature : objects) pipelines(feature);
		if ((objects = map.features.get(Obj.CBLOHD)) != null) for (Feature feature : objects) cables(feature);
		if ((objects = map.features.get(Obj.TSEZNE)) != null) for (Feature feature : objects) separation(feature);
		if ((objects = map.features.get(Obj.TSSCRS)) != null) for (Feature feature : objects) separation(feature);
		if ((objects = map.features.get(Obj.TSSRON)) != null) for (Feature feature : objects) separation(feature);
		if ((objects = map.features.get(Obj.TSELNE)) != null) for (Feature feature : objects) separation(feature);
		if ((objects = map.features.get(Obj.TSSLPT)) != null) for (Feature feature : objects) separation(feature);
		if ((objects = map.features.get(Obj.TSSBND)) != null) for (Feature feature : objects) separation(feature);
		if ((objects = map.features.get(Obj.SNDWAV)) != null) for (Feature feature : objects) areas(feature);
		if ((objects = map.features.get(Obj.OSPARE)) != null) for (Feature feature : objects) areas(feature);
		if ((objects = map.features.get(Obj.FAIRWY)) != null) for (Feature feature : objects) areas(feature);
		if ((objects = map.features.get(Obj.DRGARE)) != null) for (Feature feature : objects) areas(feature);
		if ((objects = map.features.get(Obj.RESARE)) != null) for (Feature feature : objects) areas(feature);
		if ((objects = map.features.get(Obj.SPLARE)) != null) for (Feature feature : objects) areas(feature);
		if ((objects = map.features.get(Obj.SEAARE)) != null) for (Feature feature : objects) areas(feature);
		if ((objects = map.features.get(Obj.OBSTRN)) != null) for (Feature feature : objects) obstructions(feature);
		if ((objects = map.features.get(Obj.UWTROC)) != null) for (Feature feature : objects) obstructions(feature);
		if ((objects = map.features.get(Obj.MARCUL)) != null) for (Feature feature : objects) areas(feature);
		if ((objects = map.features.get(Obj.WTWAXS)) != null) for (Feature feature : objects) waterways(feature);
		if ((objects = map.features.get(Obj.RECTRC)) != null) for (Feature feature : objects) transits(feature);
		if ((objects = map.features.get(Obj.NAVLNE)) != null) for (Feature feature : objects) transits(feature);
		if ((objects = map.features.get(Obj.HRBFAC)) != null) for (Feature feature : objects) harbours(feature);
		if ((objects = map.features.get(Obj.ACHARE)) != null) for (Feature feature : objects) harbours(feature);
		if ((objects = map.features.get(Obj.ACHBRT)) != null) for (Feature feature : objects) harbours(feature);
		if ((objects = map.features.get(Obj.LOKBSN)) != null) for (Feature feature : objects) locks(feature);
		if ((objects = map.features.get(Obj.LKBSPT)) != null) for (Feature feature : objects) locks(feature);
		if ((objects = map.features.get(Obj.GATCON)) != null) for (Feature feature : objects) locks(feature);
		if ((objects = map.features.get(Obj.DISMAR)) != null) for (Feature feature : objects) distances(feature);
		if ((objects = map.features.get(Obj.HULKES)) != null) for (Feature feature : objects) ports(feature);
		if ((objects = map.features.get(Obj.CRANES)) != null) for (Feature feature : objects) ports(feature);
		if ((objects = map.features.get(Obj.LNDMRK)) != null) for (Feature feature : objects) landmarks(feature);
		if ((objects = map.features.get(Obj.MORFAC)) != null) for (Feature feature : objects) moorings(feature);
		if ((objects = map.features.get(Obj.NOTMRK)) != null) for (Feature feature : objects) notices(feature);
		if ((objects = map.features.get(Obj.SMCFAC)) != null) for (Feature feature : objects) marinas(feature);
		if ((objects = map.features.get(Obj.BRIDGE)) != null) for (Feature feature : objects) bridges(feature);
		if ((objects = map.features.get(Obj.LITMAJ)) != null) for (Feature feature : objects) lights(feature);
		if ((objects = map.features.get(Obj.LITMIN)) != null) for (Feature feature : objects) lights(feature);
		if ((objects = map.features.get(Obj.LIGHTS)) != null) for (Feature feature : objects) lights(feature);
		if ((objects = map.features.get(Obj.SISTAT)) != null) for (Feature feature : objects) signals(feature);
		if ((objects = map.features.get(Obj.SISTAW)) != null) for (Feature feature : objects) signals(feature);
		if ((objects = map.features.get(Obj.CGUSTA)) != null) for (Feature feature : objects) signals(feature);
		if ((objects = map.features.get(Obj.RDOSTA)) != null) for (Feature feature : objects) signals(feature);
		if ((objects = map.features.get(Obj.RADSTA)) != null) for (Feature feature : objects) signals(feature);
		if ((objects = map.features.get(Obj.RSCSTA)) != null) for (Feature feature : objects) signals(feature);
		if ((objects = map.features.get(Obj.PILBOP)) != null) for (Feature feature : objects) signals(feature);
		if ((objects = map.features.get(Obj.WTWGAG)) != null) for (Feature feature : objects) gauges(feature);
		if ((objects = map.features.get(Obj.OFSPLF)) != null) for (Feature feature : objects) platforms(feature);
		if ((objects = map.features.get(Obj.WRECKS)) != null) for (Feature feature : objects) wrecks(feature);
		if ((objects = map.features.get(Obj.LITVES)) != null) for (Feature feature : objects) floats(feature);
		if ((objects = map.features.get(Obj.LITFLT)) != null) for (Feature feature : objects) floats(feature);
		if ((objects = map.features.get(Obj.BOYINB)) != null) for (Feature feature : objects) floats(feature);
		if ((objects = map.features.get(Obj.BOYLAT)) != null) for (Feature feature : objects) buoys(feature);
		if ((objects = map.features.get(Obj.BOYCAR)) != null) for (Feature feature : objects) buoys(feature);
		if ((objects = map.features.get(Obj.BOYISD)) != null) for (Feature feature : objects) buoys(feature);
		if ((objects = map.features.get(Obj.BOYSAW)) != null) for (Feature feature : objects) buoys(feature);
		if ((objects = map.features.get(Obj.BOYSPP)) != null) for (Feature feature : objects) buoys(feature);
		if ((objects = map.features.get(Obj.BOYWTW)) != null) for (Feature feature : objects) buoys(feature);
		if ((objects = map.features.get(Obj.BCNLAT)) != null) for (Feature feature : objects) beacons(feature);
		if ((objects = map.features.get(Obj.BCNCAR)) != null) for (Feature feature : objects) beacons(feature);
		if ((objects = map.features.get(Obj.BCNISD)) != null) for (Feature feature : objects) beacons(feature);
		if ((objects = map.features.get(Obj.BCNSAW)) != null) for (Feature feature : objects) beacons(feature);
		if ((objects = map.features.get(Obj.BCNSPP)) != null) for (Feature feature : objects) beacons(feature);
		if ((objects = map.features.get(Obj.BCNWTW)) != null) for (Feature feature : objects) beacons(feature);
	}
	
	private static void shoreline(Feature feature) {
		if (zoom >= 12) {
			switch ((CatSLC) Renderer.getAttVal(feature, feature.type, 0, Att.CATSLC)) {
			case SLC_TWAL:
				WatLEV lev = (WatLEV) Renderer.getAttVal(feature, feature.type, 0, Att.WATLEV);
				if (lev == WatLEV.LEV_CVRS) {
					Renderer.lineVector(feature, new LineStyle(Color.black, 10, new float[] { 40, 40 }, null));
					if (zoom >= 15)
						Renderer.lineText(feature, "(covers)", new Font("Arial", Font.PLAIN, 80), 0.5, 20);
				} else {
					Renderer.lineVector(feature, new LineStyle(Color.black, 10, null, null));
				}
				if (zoom >= 15)
					Renderer.lineText(feature, "Training Wall", new Font("Arial", Font.PLAIN, 80), 0.5, -20);
			}
		}
	}
	private static void pipelines(Feature feature) {
		if (zoom >= 14) {
			if (feature.type == Obj.PIPSOL) {
				Renderer.lineSymbols(feature, Areas.Pipeline, 1.0, null, 0);
			} else if (feature.type == Obj.PIPOHD) {

			}
		}
	}
	private static void cables(Feature feature) {
		if (zoom >= 14) {
			if (feature.type == Obj.CBLSUB) {
				Renderer.lineSymbols(feature, Areas.Cable, 0.0, null, 0);
			} else if (feature.type == Obj.CBLOHD) {

			}
		}
	}
	private static void separation(Feature feature) {
		switch (feature.type) {
		case TSEZNE:
		case TSSCRS:
		case TSSRON:
			if (zoom <= 15)
				Renderer.lineVector(feature, new LineStyle(null, 0, null, new Color(0x80c480ff, true)));
			else
				Renderer.lineVector(feature, new LineStyle(new Color(0x80c480ff, true), 20, null, null));
			AttItem name = feature.atts.get(Att.OBJNAM);
			if ((zoom >= 10) && (name != null))
				Renderer.labelText(feature, (String) name.val, new Font("Arial", Font.BOLD, 150), new Color(0x80c480ff), null);
			break;
		case TSELNE:
			Renderer.lineVector(feature, new LineStyle(new Color(0x80c480ff, true), 20, null, null));
			break;
		case TSSLPT:
			Renderer.lineSymbols(feature, Areas.LaneArrow, 0.5, null, 0);
			break;
		case TSSBND:
			Renderer.lineVector(feature, new LineStyle(new Color(0x80c480ff, true), 20, new float[] { 40, 40 }, null));
			break;
		}
	}
	private static void areas(Feature feature) {
		AttItem name = feature.atts.get(Att.OBJNAM);
		switch (feature.type) {
		case SPLARE:
			if (zoom >= 12) {
				Renderer.symbol(feature, Areas.Plane, Obj.SPLARE, null);
				Renderer.lineSymbols(feature, Areas.Restricted, 0.5, Areas.LinePlane, 10);
			}
			if ((zoom >= 15) && (name != null))
				Renderer.labelText(feature, (String) name.val, new Font("Arial", Font.BOLD, 80), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -90)));
			break;
		case MARCUL:
			if (zoom >= 14)
				Renderer.symbol(feature, Areas.MarineFarm, Obj.MARCUL, null);
			if (zoom >= 16)
				Renderer.lineVector(feature, new LineStyle( Color.black, 4, new float[] { 10, 10 }, null));
			break;
		case FAIRWY:
			if (feature.area > 2.0) {
				if (zoom < 16)
					Renderer.lineVector(feature, new LineStyle(new Color(0xc480ff), 8, new float[] { 50, 50 }, new Color(0x40ffffff, true)));
				else
					Renderer.lineVector(feature, new LineStyle(new Color(0xc480ff), 8, new float[] { 50, 50 }, null));
			} else {
				if (zoom >= 14)
					Renderer.lineVector(feature, new LineStyle(new Color(0x40ffffff, true), 0, null, null));
			}
			break;
		case DRGARE:
			if (zoom < 16)
				Renderer.lineVector(feature, new LineStyle(Color.black, 8, new float[] { 25, 25 }, new Color(0x40ffffff, true)));
			else
				Renderer.lineVector(feature, new LineStyle(Color.black, 8, new float[] { 25, 25 }, null));
			if ((zoom >= 12) && (name != null))
				Renderer.labelText(feature, (String) name.val, new Font("Arial", Font.PLAIN, 100), Color.black, null);
			break;
		case RESARE:
			if (zoom >= 12) {
				Renderer.lineSymbols(feature, Areas.Restricted, 1.0, null, 0);
//				if ((CatREA)Renderer.getAttVal(feature, feature.type, 0, Att.CATREA) == CatREA.REA_NWAK)
//					Renderer.symbol(feature, Areas.NoWake, Obj.RESARE, null);
			}
			break;
		case OSPARE:
			if ((CatPRA)Renderer.getAttVal(feature, feature.type, 0, Att.CATPRA) == CatPRA.PRA_WFRM) {
				Renderer.symbol(feature, Areas.WindFarm, Obj.OSPARE, null);
				Renderer.lineVector(feature, new LineStyle(Color.black, 20, new float[] { 40, 40 }, null));
				if ((zoom >= 15) && (name != null))
					Renderer.labelText(feature, (String) name.val, new Font("Arial", Font.BOLD, 80), Color.black, new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 10)));
			}
			break;
		case SEAARE:
			switch ((CatSEA) Renderer.getAttVal(feature, feature.type, 0, Att.CATSEA)) {
			case SEA_RECH:
				break;
			case SEA_BAY:
				break;
			case SEA_SHOL:
				break;
			case SEA_GAT:
			case SEA_NRRW:
				break;
			}
			break;
		case SNDWAV:
//	  if (zoom>=12)) area("fill:url(#sandwaves)");
			break;
		}
/*
  if (is_type("sea_area")) {
    if (has_attribute("category")) {
      make_string("");
      attribute_switch("category")
      attribute_case("reach") { if (zoom >= 10) add_string("font-family:Arial;font-weight:normal;font-style:italic;font-size:150;text-anchor:middle") }
      attribute_case("bay") { if (zoom >= 12) add_string("font-family:Arial;font-weight:normal;font-style:italic;font-size:150;text-anchor:middle") }
      attribute_case("shoal") { if (zoom >= 14) {
        if (is_area) {
          area("stroke:#c480ff;stroke-width:4;stroke-dasharray:25,25;fill:none");
          if (has_item_attribute("name")) text(item_attribute("name"), "font-family:Arial;font-weight:normal;font-style:italic;font-size:75;text-anchor:middle", 0, -40);
          text("(Shoal)", "font-family:Arial;font-weight:normal;font-size:60;text-anchor:middle", 0, 0);
        } else if (is_line) {
          if (has_item_attribute("name")) way_text(item_attribute("name"), "font-family:Arial;font-weight:normal;font-style:italic;font-size:75;text-anchor:middle", 0.5, -40, line("stroke:none;fill:none"));
          way_text("(Shoal)", "font-family:Arial;font-weight:normal;font-size:60;text-anchor:middle", 0.5, 0, line("stroke:none;fill:none"));
        } else {
          if (has_item_attribute("name")) text(item_attribute("name"), "font-family:Arial;font-weight:normal;font-style:italic;font-size:75;text-anchor:middle", 0, -40);
          text("(Shoal)", "font-family:Arial;font-weight:normal;font-size:60;text-anchor:middle", 0, 0);
        }
      }
      }
      attribute_case("gat|narrows") { if (zoom >= 12) add_string("font-family:Arial;font-weight:normal;font-style:italic;font-size:100;text-anchor:middle") }
      end_switch
      if ((strlen(string) > 0) && !attribute_test("category", "shoal")) {
        int ref = line("stroke:none;fill:none");
        if (ref != 0) {
          if (has_item_attribute("name")) way_text(item_attribute("name"), string, 0.5, 0, ref);
        } else {
          if (has_item_attribute("name")) text(item_attribute("name"), string, 0, 0);
        }
      }
      free_string
    }
  }
 */
	}

	private static void obstructions(Feature feature) {
		if ((zoom >= 14) && (feature.type == Obj.UWTROC)) {
			WatLEV lvl = (WatLEV) Renderer.getAttVal(feature, feature.type, 0, Att.WATLEV);
			switch (lvl) {
			case LEV_CVRS:
				Renderer.symbol(feature, Areas.RockC, null, null);
				break;
			case LEV_AWSH:
				Renderer.symbol(feature, Areas.RockA, null, null);
				break;
			default:
				Renderer.symbol(feature, Areas.Rock, null, null);
			}
		} else {
			Renderer.symbol(feature, Areas.Rock, null, null);
		}
	}
	private static void waterways(Feature feature) {
		if ((zoom >= 14) && (feature.atts.get(Att.OBJNAM) != null)) {
			// lineText(item_attribute("name"), "font-family:Arial;font-weight:bold;font-size:80;text-anchor:middle", 0.5, 15, line("stroke:none;fill:none"));
		}
	}
	private static void transits(Feature feature) {
	}
	private static void harbours(Feature feature) {
		AttItem name = feature.atts.get(Att.OBJNAM);
		switch (feature.type) {
		case ACHARE:
			if (zoom >= 12) {
				if (feature.flag != Fflag.LINE)
					Renderer.symbol(feature, Harbours.Anchorage, null, null);
				Renderer.lineSymbols(feature, Areas.Restricted, 1.0, Areas.LineAnchor, 10);
				if ((zoom >= 15) && ((name) != null)) {
					Renderer.labelText(feature, (String) name.val, new Font("Arial", Font.BOLD, 80), new Color(0x80c480ff), null);
				}
			}
			break;
		}
	}
	/*
	 *   if ((zoom >= 12) && is_type("anchorage")) {
    symbol("anchorage");
    if ((zoom >= 15) && (has_item_attribute("name")))
      text(item_attribute("name"), "font-family:Arial; font-weight:bold; font-size:80; text-anchor:middle", 0, -90);
    if ((zoom >= 12) && (is_area)) line_symbols("restricted_line", 0.5, "line_anchor", 10);
  }
  if ((zoom >= 16) && is_type("anchor_berth")) symbol("anchor_berth");
  if ((zoom >= 12) && is_type("harbour")) {
    if (has_attribute("category")) {
      attribute_switch("category")
      attribute_case("marina|yacht") symbol("marina");
      attribute_case("marina_no_facilities") symbol("marina_nf");
      attribute_default symbol("harbour");
      end_switch
    } else symbol("harbour");
    if ((zoom >= 15) && (has_item_attribute("name")))
      text(item_attribute("name"), "font-family:Arial; font-weight:bold; font-size:80; text-anchor:middle", 0, -90);
  }

	 */
	private static void locks(Feature feature) {
	}
	private static void distances(Feature feature) {
	}
	private static void ports(Feature feature) {
	}
	private static void landmarks(Feature feature) {
		ArrayList<CatLMK> cats = (ArrayList<CatLMK>) Renderer.getAttVal(feature, feature.type, 0, Att.CATLMK);
		Symbol catSym = Landmarks.Shapes.get(cats.get(0));
		ArrayList<FncFNC> fncs = (ArrayList<FncFNC>) Renderer.getAttVal(feature, feature.type, 0, Att.FUNCTN);
		Symbol fncSym = Landmarks.Funcs.get(fncs.get(0));
		if ((fncs.get(0) == FncFNC.FNC_CHCH) && (cats.get(0) == CatLMK.LMK_TOWR))
			catSym = Landmarks.ChurchTower;
		if ((cats.get(0) == CatLMK.LMK_UNKN) && (fncs.get(0) == FncFNC.FNC_UNKN) && (feature.objs.get(Obj.LIGHTS) != null))
			catSym = Beacons.LightMajor;
		if (cats.get(0) == CatLMK.LMK_RADR)
			fncSym = Landmarks.RadioTV;
		Renderer.symbol(feature, catSym, null, null);
		Renderer.symbol(feature, fncSym, null, null);
	}
	private static void moorings(Feature feature) {
		CatMOR cat = (CatMOR) Renderer.getAttVal(feature, feature.type, 0, Att.CATMOR);
		switch (cat) {
		case MOR_DLPN:
			Renderer.symbol(feature, Harbours.Dolphin, null, null);
			break;
		case MOR_DDPN:
			Renderer.symbol(feature, Harbours.DeviationDolphin, null, null);
			break;
		case MOR_BLRD:
		case MOR_POST:
			Renderer.symbol(feature, Harbours.Bollard, null, null);
			break;
		case MOR_BUOY:
			BoySHP shape = (BoySHP) Renderer.getAttVal(feature, feature.type, 0, Att.BOYSHP);
			if (shape == BoySHP.BOY_UNKN)
				shape = BoySHP.BOY_SPHR;
			Renderer.symbol(feature, Buoys.Shapes.get(shape), feature.type, null);
			break;
		}
	}
	private static void notices(Feature feature) {
	}
	private static void marinas(Feature feature) {
	}
	private static void bridges(Feature feature) {
	}
	private static void wrecks(Feature feature) {
		if (zoom >= 14) {
			CatWRK cat = (CatWRK) Renderer.getAttVal(feature, feature.type, 0, Att.CATWRK);
			switch (cat) {
			case WRK_DNGR:
			case WRK_MSTS:
				Renderer.symbol(feature, Areas.WreckD, null, null);
				break;
			case WRK_HULS:
				Renderer.symbol(feature, Areas.WreckS, null, null);
				break;
			default:
				Renderer.symbol(feature, Areas.WreckND, null, null);
			}
		} else {
			Renderer.symbol(feature, Areas.WreckND, null, null);
		}
	}
	private static void gauges(Feature feature) {
	}
	private static void lights(Feature feature) {
		switch (feature.type) {
		case LITMAJ:
			Renderer.symbol(feature, Beacons.LightMajor, null, null);
			break;
		case LITMIN:
		case LIGHTS:
			Renderer.symbol(feature, Beacons.LightMinor, null, null);
			break;
		}
	}
	private static void signals(Feature feature) {
		switch (feature.type) {
		case SISTAT:
		case SISTAW:
			Renderer.symbol(feature, Harbours.SignalStation, null, null);
			break;
		case RDOSTA:
			Renderer.symbol(feature, Harbours.SignalStation, null, null);
			break;
		case RADSTA:
			Renderer.symbol(feature, Harbours.SignalStation, null, null);
			Renderer.symbol(feature, Beacons.RadarStation, null, null);
			break;
		case PILBOP:
			Renderer.symbol(feature, Harbours.Pilot, null, null);
			break;
		case CGUSTA:
			Renderer.symbol(feature, Harbours.SignalStation, null, null);
			break;
		case RSCSTA:
			Renderer.symbol(feature, Harbours.Rescue, null, null);
			break;
		}
	}
	private static void floats(Feature feature) {
		switch (feature.type) {
		case LITVES:
			Renderer.symbol(feature, Buoys.Super, feature.type, null);
			break;
		case LITFLT:
			Renderer.symbol(feature, Buoys.Float, feature.type, null);
			break;
		case BOYINB:
			Renderer.symbol(feature, Buoys.Storage, feature.type, null);
			break;
		}
		if (feature.objs.get(Obj.TOPMAR) != null)
			Renderer.symbol(feature, Topmarks.Shapes.get(feature.objs.get(Obj.TOPMAR).get(0).get(Att.TOPSHP).val), Obj.TOPMAR, Topmarks.Floats);
	}
	private static void platforms(Feature feature) {
		Renderer.symbol(feature, Landmarks.Platform, null, null);
	}
	private static void buoys(Feature feature) {
		BoySHP shape = (BoySHP) Renderer.getAttVal(feature, feature.type, 0, Att.BOYSHP);
		Renderer.symbol(feature, Buoys.Shapes.get(shape), feature.type, null);
		if (feature.objs.get(Obj.TOPMAR) != null) {
			Renderer.symbol(feature, Topmarks.Shapes.get(feature.objs.get(Obj.TOPMAR).get(0).get(Att.TOPSHP).val), Obj.TOPMAR, Topmarks.Buoys.get(shape));
		}
	}
	private static void beacons(Feature feature) {
		BcnSHP shape = (BcnSHP) Renderer.getAttVal(feature, feature.type, 0, Att.BCNSHP);
		if (((shape == BcnSHP.BCN_PRCH) || (shape == BcnSHP.BCN_WTHY)) && (feature.type == Obj.BCNLAT)) {
			CatLAM cat = (CatLAM) Renderer.getAttVal(feature, feature.type, 0, Att.CATLAM);
			switch (cat) {
			case LAM_PORT:
				if (shape == BcnSHP.BCN_PRCH)
					Renderer.symbol(feature, Beacons.PerchPort, null, null);
				else
					Renderer.symbol(feature, Beacons.WithyPort, null, null);
				break;
			case LAM_STBD:
				if (shape == BcnSHP.BCN_PRCH)
					Renderer.symbol(feature, Beacons.PerchStarboard, null, null);
				else
					Renderer.symbol(feature, Beacons.WithyStarboard, null, null);
				break;
			default:
				Renderer.symbol(feature, Beacons.Stake, feature.type, null);
			}
		} else {
			Renderer.symbol(feature, Beacons.Shapes.get(shape), feature.type, null);
			if (feature.objs.get(Obj.TOPMAR) != null)
				Renderer.symbol(feature, Topmarks.Shapes.get(feature.objs.get(Obj.TOPMAR).get(0).get(Att.TOPSHP).val), Obj.TOPMAR, Topmarks.Beacons);
		}
	}
}
