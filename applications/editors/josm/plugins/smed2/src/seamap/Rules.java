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
		if ((objects = map.features.get(Obj.ISTZNE)) != null) for (Feature feature : objects) separation(feature);
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
	private static void buoys(Feature feature) {
		BoySHP shape = (BoySHP) Renderer.getAttVal(feature, feature.type, 0, Att.BOYSHP);
		Renderer.symbol(feature, Buoys.Shapes.get(shape), feature.type, null);
		if (feature.objs.get(Obj.TOPMAR) != null) {
			Renderer.symbol(feature, Topmarks.Shapes.get(feature.objs.get(Obj.TOPMAR).get(0).get(Att.TOPSHP).val), Obj.TOPMAR, Topmarks.Buoys.get(shape));
		}
	}
	private static void bridges(Feature feature) {
		if (zoom >= 16) {
			
		}
/*      Att_t *attv = getAtt(getObj(item, BRIDGE, 0), VERCLR);
      if (attv == NULL) attv = getAtt(getObj(item, BRIDGE, 0), VERCSA);
      Att_t *attc = getAtt(getObj(item, BRIDGE, 0), VERCCL);
      Att_t *atto = getAtt(getObj(item, BRIDGE, 0), VERCOP);
      if (attv != NULL) {
        renderSymbol(item, obja, "clear_v", "", "", CC, 0, 0, 0);
        drawText(item, stringValue(attv->val), "font-family:Arial; font-weight:normal; font-size:70; text-anchor:middle", 0, 12);
      }
      else if ((attc != NULL) && (atto == NULL)) {
        renderSymbol(item, obja, "clear_v", "", "", CC, 0, 0, 0);
        drawText(item, stringValue(attc->val), "font-family:Arial; font-weight:normal; font-size:70; text-anchor:middle", 0, 12);
      }
      else if ((attc != NULL) && (atto != NULL)) {
        renderSymbol(item, obja, "clear_v", "", "", RC, 5, 0, 0);
        drawText(item, stringValue(attc->val), "font-family:Arial; font-weight:normal; font-size:70; text-anchor:middle", -35, 12);
        renderSymbol(item, obja, "clear_v", "", "", LC, -5, 0, 0);
        drawText(item, stringValue(atto->val), "font-family:Arial; font-weight:normal; font-size:70; text-anchor:middle", 35, 12);
      }
    }
*/
	}
	private static void cables(Feature feature) {
		if (zoom >= 14) {
			if (feature.type == Obj.CBLSUB) {
				Renderer.lineSymbols(feature, Areas.Cable, 0.0, null, 0);
			} else if (feature.type == Obj.CBLOHD) {

			}
		}
	}
	private static void distances(Feature feature) {
/*object_rules(distances) {
  if ((zoom>=16) && (has_attribute("category"))) {
    attribute_switch("category")
    attribute_case("installed") symbol("distance_i");
    attribute_default symbol("distance_u");
    end_switch
  }
}
*/
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
			Renderer.symbol(feature, Buoys.Super, feature.type, null);
			break;
		}
		if (feature.objs.get(Obj.TOPMAR) != null)
			Renderer.symbol(feature, Topmarks.Shapes.get(feature.objs.get(Obj.TOPMAR).get(0).get(Att.TOPSHP).val), Obj.TOPMAR, Topmarks.Floats);
	}
	private static void gauges(Feature feature) {
/*object_rules(gauge) {
  if (zoom >= 14) symbol("tide_gauge");
}
*/
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
/*  if (!has_attribute("function") && !has_attribute("category") && has_object("light")) {
    symbol("lighthouse");
    if ((zoom >= 15) && has_item_attribute("name"))
      text(item_attribute("name"), "font-family:Arial; font-weight:bold; font-size:80; text-anchor:middle", 0, -70);
  } else {
    if ((zoom >= 15) && has_item_attribute("name"))
      text(item_attribute("name"), "font-family:Arial; font-weight:bold; font-size:80; text-anchor:start", 60, -50);
  }
  if (has_object("fog_signal")) object(fogs);
  if (has_object("radar_transponder")) object(rtbs);
  if (has_object("radar_station") && (zoom >= 12)) symbol("radar_station");
  if (has_object("light")) object(lights);
}
*/
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
	private static void locks(Feature feature) {
/*object_rules(locks) {
  if ((zoom>=13) && is_type("lock_basin|lock_basin_part")) symbol("lock");
  if ((zoom>=15) && is_type("gate")) symbol("lock_gate");
}
*/
	}
	private static void marinas(Feature feature) {
		if (zoom >= 16) {
			
		}
/*      int n = countObjects(item, type);
      Atta_t atta = enumAttribute("category", obja);
      char **map = cluster_map(obja);
      if (map == NULL) return;
      switch (n) {
        case 0: {
          Obj_t *obj = getObj(item, obja, 0);
          int n = countValues(getAtt(obj, atta));
          switch (n) {
            case 1:
              renderSymbol(item, obja, map[getAttEnum(obj, atta, 0)], "", "", CC, 0, 0, 0);
              break;
            case 2:
              renderSymbol(item, obja, map[getAttEnum(obj, atta, 0)], "", "", RC, 0, 0, 0);
              renderSymbol(item, obja, map[getAttEnum(obj, atta, 1)], "", "", LC, 0, 0, 0);
              break;
            case 3:
              renderSymbol(item, obja, map[getAttEnum(obj, atta, 0)], "", "", BC, 0, 0, 0);
              renderSymbol(item, obja, map[getAttEnum(obj, atta, 1)], "", "", TR, 0, 0, 0);
              renderSymbol(item, obja, map[getAttEnum(obj, atta, 2)], "", "", TL, 0, 0, 0);
              break;
            case 4:
              renderSymbol(item, obja, map[getAttEnum(obj, atta, 0)], "", "", BR, 0, 0, 0);
              renderSymbol(item, obja, map[getAttEnum(obj, atta, 1)], "", "", BL, 0, 0, 0);
              renderSymbol(item, obja, map[getAttEnum(obj, atta, 2)], "", "", TR, 0, 0, 0);
              renderSymbol(item, obja, map[getAttEnum(obj, atta, 3)], "", "", TL, 0, 0, 0);
              break;
          }
        }
          break;
        case 1:
          renderSymbol(item, obja, map[getAttEnum(getObj(item, obja, 1), atta, 0)], "", "", CC, 0, 0, 0);
          break;
        case 2:
          renderSymbol(item, obja, map[getAttEnum(getObj(item, obja, 1), atta, 0)], "", "", RC, 0, 0, 0);
          renderSymbol(item, obja, map[getAttEnum(getObj(item, obja, 2), atta, 0)], "", "", LC, 0, 0, 0);
          break;
        case 3:
          renderSymbol(item, obja, map[getAttEnum(getObj(item, obja, 1), atta, 0)], "", "", BC, 0, 0, 0);
          renderSymbol(item, obja, map[getAttEnum(getObj(item, obja, 2), atta, 0)], "", "", TR, 0, 0, 0);
          renderSymbol(item, obja, map[getAttEnum(getObj(item, obja, 3), atta, 0)], "", "", TL, 0, 0, 0);
          break;
        case 4:
          renderSymbol(item, obja, map[getAttEnum(getObj(item, obja, 1), atta, 0)], "", "", BR, 0, 0, 0);
          renderSymbol(item, obja, map[getAttEnum(getObj(item, obja, 2), atta, 0)], "", "", BL, 0, 0, 0);
          renderSymbol(item, obja, map[getAttEnum(getObj(item, obja, 3), atta, 0)], "", "", TR, 0, 0, 0);
          renderSymbol(item, obja, map[getAttEnum(getObj(item, obja, 4), atta, 0)], "", "", TL, 0, 0, 0);
          break;
      }
*/
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
/*  if (has_object("fog_signal")) object(fogs);
  if (has_object("radar_transponder")) object(rtbs);
  if (has_object("light")) object(lights);
}
*/
	}

	private static void notices(Feature feature) {
		if (zoom >= 14) {
			double dx = 0.0, dy = 0.0;
			switch (feature.type) {
			case BCNCAR:
			case BCNISD:
			case BCNLAT:
			case BCNSAW:
			case BCNSPP:
			case BCNWTW:
				dy = 45.0;
				break;
			case NOTMRK:
				dy = 0.0;
				break;
			default:
				return;
			}
			Symbol s1 = null, s2 = null;
			MarSYS sys = MarSYS.SYS_CEVN;
			AttItem att = feature.atts.get(Att.MARSYS);
			if (att != null) sys = (MarSYS)att.val;
			ObjTab objs = feature.objs.get(Obj.NOTMRK);
			int n = objs.size();
			if (n > 2) {
				s1 = Notices.Notice;
				n = 1;
			} else {
				for (AttMap atts : objs.values()) {
					if (atts.get(Att.MARSYS) != null) sys = (MarSYS)atts.get(Att.MARSYS).val;
					CatNMK cat = CatNMK.NMK_UNKN;
					if (atts.get(Att.CATNMK) != null) cat = (CatNMK)atts.get(Att.CATNMK).val;
					s2 = Notices.getNotice(cat, sys);
				}
			}
/*      Obj_t *obj = getObj(item, NOTMRK, i);
      if (obj == NULL) continue;
      Atta_t add;
      int idx = 0;
      while ((add = getAttEnum(obj, ADDMRK, idx++)) != MRK_UNKN) {
        if ((add == MRK_LTRI) && (i == 2)) swap = true;
        if ((add == MRK_RTRI) && (i != 2)) swap = true;
      }
    }
  } else {
  	
  }
  for (int i = 0; i <=2; i++) {
    Obj_t *obj = getObj(item, NOTMRK, i);
    if (obj == NULL) continue;
    Atta_t category = getAttEnum(obj, CATNMK, i);
    Atta_t add;
    int idx = 0;
    int top=0, bottom=0, left=0, right=0;
    while ((add = getAttEnum(obj, ADDMRK, idx++)) != MRK_UNKN) {
      switch (add) {
        case MRK_TOPB:
          top = add;
          break;
        case MRK_BOTB:
        case MRK_BTRI:
          bottom = add;
          break;
        case MRK_LTRI:
          left = add;
          break;
        case MRK_RTRI:
          right = add;
          break;
        default:
          break;
      }
    }
    double orient = getAtt(obj, ORIENT) != NULL ? getAtt(obj, ORIENT)->val.val.f : 0.0;
    int system = getAtt(obj, MARSYS) != NULL ? getAtt(obj, MARSYS)->val.val.e : 0;
    double flip = 0.0;
    char *symb = "";
    char *base = "";
    char *colour = "black";
    if ((system == SYS_BWR2) || (system == SYS_BNWR)) {
      symb = bniwr_map[category];
      switch (category) {
        case NMK_NANK:
        case NMK_LMHR:
        case NMK_KTPM...NMK_RSPD:
        {
          int bank = getAtt(obj, BNKWTW) != NULL ? getAtt(obj, BNKWTW)->val.val.e : 0;
          switch (bank) {
            case BWW_LEFT:
              base = "notice_blb";
              colour = "red";
              break;
            case BWW_RGHT:
              base = "notice_brb";
              colour = "green";
              break;
            default:
              base = "notice_bsi";
              colour = "black";
              break;
          }
        }
        default:
          break;
      }
    } else if (system == SYS_PPWB) {
      int bank = getAtt(obj, BNKWTW) != NULL ? getAtt(obj, BNKWTW)->val.val.e : 0;
      if (bank != 0) {
        switch (category) {
          case NMK_WLAR:
            if (bank == BNK_LEFT)
              base = "notice_pwlarl";
            else
              base = "notice_pwlarr";
            break;
          case NMK_WRAL:
            if (bank == BNK_LEFT)
              base = "notice_pwrall";
            else
              base = "notice_pwralr";
            break;
          case NMK_KTPM:
            if (bank == BNK_LEFT)
              base = "notice_ppml";
            else
              base = "notice_ppmr";
            break;
          case NMK_KTSM:
            if (bank == BNK_LEFT)
              base = "notice_psml";
            else
              base = "notice_psmr";
            break;
          case NMK_KTMR:
            if (bank == BNK_LEFT)
              base = "notice_pmrl";
            else
              base = "notice_pmrr";
            break;
          case NMK_CRTP:
          if (bank == BNK_LEFT)
              base = "notice_pcpl";
            else
              base = "notice_pcpr";
            break;
          case NMK_CRTS:
            if (bank == BNK_LEFT)
              base = "notice_pcsl";
            else
              base = "notice_pcsr";
            break;
          default:
            break;
        }
      }
    } else {
      symb = notice_map[category];
      switch (category) {
        case NMK_NOVK...NMK_NWSH:
        case NMK_NMTC...NMK_NLBG:
          base = "notice_a";
          break;
        case NMK_MVTL...NMK_CHDR:
          base = "notice_b";
          break;
        case NMK_PRTL...NMK_PRTR:
        case NMK_OVHC...NMK_LBGP:
          base = "notice_e";
          colour = "white";
          break;
        default:
          break;
      }
      switch (category) {
        case NMK_MVTL:
        case NMK_ANKP:
        case NMK_PRTL:
        case NMK_MWAL:
        case NMK_MWAR:
          flip = 180.0;
          break;
        case NMK_SWWR:
        case NMK_WRSL:
        case NMK_WARL:
          flip = -90.0;
          break;
        case NMK_SWWC:
        case NMK_SWWL:
        case NMK_WLSR:
        case NMK_WALR:
          flip = 90.0;
          break;
        default:
          break;
      }
    }
    if (n == 2) {
      dx = (((i != 2) && swap) || ((i == 2) && !swap)) ? -30.0 : 30.0;
    }
    if (top == MRK_TOPB)
      renderSymbol(item, NOTMRK, "notice_board", "", "", BC, dx, dy, orient);
    if (bottom == MRK_BOTB)
      renderSymbol(item, NOTMRK, "notice_board", "", "", BC, dx, dy, orient+180);
    if (bottom == MRK_BTRI)
      renderSymbol(item, NOTMRK, "notice_triangle", "", "", BC, dx, dy, orient+180);
    if (left == MRK_LTRI)
      renderSymbol(item, NOTMRK, "notice_triangle", "", "", BC, dx, dy, orient-90);
    if (right == MRK_RTRI)
      renderSymbol(item, NOTMRK, "notice_triangle", "", "", BC, dx, dy, orient+90);
    renderSymbol(item, NOTMRK, base, "", "", CC, dx, dy, orient);
    renderSymbol(item, NOTMRK, symb, "", colour, CC, dx, dy, orient+flip);
  }
*/
		}
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
	private static void pipelines(Feature feature) {
		if (zoom >= 14) {
			if (feature.type == Obj.PIPSOL) {
				Renderer.lineSymbols(feature, Areas.Pipeline, 1.0, null, 0);
			} else if (feature.type == Obj.PIPOHD) {

			}
		}
	}
	private static void platforms(Feature feature) {
		ArrayList<CatOFP> cats = (ArrayList<CatOFP>)Renderer.getAttVal(feature, Obj.OFSPLF, 0, Att.CATOFP);
		if ((CatOFP) cats.get(0) == CatOFP.OFP_FPSO)
			Renderer.symbol(feature, Buoys.Storage, null, null);
		else
			Renderer.symbol(feature, Landmarks.Platform, null, null);
		AttItem name = feature.atts.get(Att.OBJNAM);
		if ((zoom >= 15) && (name != null))
			Renderer.labelText(feature, (String) name.val, new Font("Arial", Font.BOLD, 80), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(60, -50)));
/*object_rules(platforms) {
  if (has_object("fog_signal")) object(fogs);
  if (has_object("radar_transponder")) object(rtbs);
  if (has_object("light")) object(lights);
}
*/
	}
	private static void ports(Feature feature) {
		if (zoom >= 14) {
			if (feature.type == Obj.CRANES) {
				if ((CatCRN) Renderer.getAttVal(feature, feature.type, 0, Att.CATCRN) == CatCRN.CRN_CONT)
					Renderer.symbol(feature, Harbours.ContainerCrane, null, null);
				else
					Renderer.symbol(feature, Harbours.PortCrane, null, null);
			} else if (feature.type == Obj.HULKES) {
				Renderer.lineVector(feature, new LineStyle(Color.black, 4, null, new Color(0xffe000)));
				AttItem name = feature.atts.get(Att.OBJNAM);
				if ((zoom >= 15) && (name != null))
					Renderer.labelText(feature, (String) name.val, new Font("Arial", Font.BOLD, 80), Color.black, null);
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
		case ISTZNE:
			Renderer.lineSymbols(feature, Areas.Restricted, 1.0, null, 0);
			break;
		}
	}
	private static void shoreline(Feature feature) {
		if (zoom >= 12) {
			switch ((CatSLC) Renderer.getAttVal(feature, feature.type, 0, Att.CATSLC)) {
			case SLC_TWAL:
				WatLEV lev = (WatLEV) Renderer.getAttVal(feature, feature.type, 0, Att.WATLEV);
				if (lev == WatLEV.LEV_CVRS) {
					Renderer.lineVector(feature, new LineStyle(Color.black, 10, new float[] { 40, 40 }, null));
					if (zoom >= 15)
						Renderer.lineText(feature, "(covers)", new Font("Arial", Font.PLAIN, 80), Color.black, 0.5, 20);
				} else {
					Renderer.lineVector(feature, new LineStyle(Color.black, 10, null, null));
				}
				if (zoom >= 15)
					Renderer.lineText(feature, "Training Wall", new Font("Arial", Font.PLAIN, 80), Color.black, 0.5, -20);
			}
		}
	}
	private static void signals(Feature feature) {
		if (zoom >= 14) {
			switch (feature.type) {
			case SISTAT:
			case SISTAW:
				Renderer.symbol(feature, Harbours.SignalStation, null, null);
				break;
			case RDOSTA:
				Renderer.symbol(feature, Harbours.SignalStation, null, null);
				Renderer.symbol(feature, Beacons.RadarStation, null, null);
				ArrayList<CatROS> cats = (ArrayList<CatROS>)Renderer.getAttVal(feature, Obj.RDOSTA, 0, Att.CATROS);
				String str = "";
				for (CatROS ros : cats) {
					switch (ros) {
					case ROS_OMNI:
						str += " RC";
						break;
					case ROS_DIRL:
						str += " RD";
						break;
					case ROS_ROTP:
						str += " RW";
						break;
					case ROS_CNSL:
						str += " Consol";
						break;
					case ROS_RDF:
						str += " RG";
						break;
					case ROS_QTA:
						str += " R";
						break;
					case ROS_AERO:
						str += " AeroRC";
						break;
					case ROS_DECA:
						str += " Decca";
						break;
					case ROS_LORN:
						str += " Loran";
						break;
					case ROS_DGPS:
						str += " DGPS";
						break;
					case ROS_TORN:
						str += " Toran";
						break;
					case ROS_OMGA:
						str += " Omega";
						break;
					case ROS_SYLD:
						str += " Syledis";
						break;
					case ROS_CHKA:
						str += " Chiaka";
						break;
					case ROS_PCOM:
					case ROS_COMB:
					case ROS_FACS:
					case ROS_TIME:
						break;
					case ROS_PAIS:
					case ROS_SAIS:
						str += " AIS";
						break;
					case ROS_VAIS:
						Renderer.labelText(feature, " V-AIS", new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 180)));
						break;
					case ROS_VANC:
						Renderer.labelText(feature, " V-AIS", new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 180)));
						Renderer.symbol(feature, Topmarks.TopNorth, null, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
						break;
					case ROS_VASC:
						Renderer.labelText(feature, " V-AIS", new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 180)));
						Renderer.symbol(feature, Topmarks.TopSouth, null, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
						break;
					case ROS_VAEC:
						Renderer.labelText(feature, " V-AIS", new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 180)));
						Renderer.symbol(feature, Topmarks.TopEast, null, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
						break;
					case ROS_VAWC:
						Renderer.labelText(feature, " V-AIS", new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 180)));
						Renderer.symbol(feature, Topmarks.TopWest, null, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
						break;
					case ROS_VAPL:
						Renderer.labelText(feature, " V-AIS", new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 180)));
						Renderer.symbol(feature, Topmarks.TopCan, null, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
						break;
					case ROS_VASL:
						Renderer.labelText(feature, " V-AIS", new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 180)));
						Renderer.symbol(feature, Topmarks.TopCone, null, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
						break;
					case ROS_VAID:
						Renderer.labelText(feature, " V-AIS", new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 180)));
						Renderer.symbol(feature, Topmarks.TopIsol, null, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
						break;
					case ROS_VASW:
						Renderer.labelText(feature, " V-AIS", new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 180)));
						Renderer.symbol(feature, Topmarks.TopSphere, null, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
						break;
					case ROS_VASP:
						Renderer.labelText(feature, " V-AIS", new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 180)));
						Renderer.symbol(feature, Topmarks.TopX, null, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
						break;
					case ROS_VAWK:
						Renderer.labelText(feature, " V-AIS", new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 180)));
						Renderer.symbol(feature, Topmarks.TopCross, null, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
						break;
					}
				}
				if (!str.isEmpty()) Renderer.labelText(feature, str, new Font("Arial", Font.PLAIN, 70), Color.black, new Delta(Handle.BR, AffineTransform.getTranslateInstance(-30, -180)));
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
		/*  if (has_object("fog_signal")) object(fogs);
  if (has_object("radar_transponder")) object(rtbs);
  if (has_object("light")) object(lights);
}
*/
	}
	private static void transits(Feature feature) {
	  if (zoom >= 12) {
	  	if (feature.type == Obj.RECTRC) Renderer.lineVector (feature, new LineStyle(Color.black, 10, null, null));
	  	else if (feature.type == Obj.NAVLNE) Renderer.lineVector (feature, new LineStyle(Color.black, 10, new float[] { 25, 25 }, null));
	  }
	  if (zoom >= 15) {
	  	String str = "";
			AttItem name = feature.atts.get(Att.OBJNAM);
			if (name != null) str += (String)name.val + " ";
			Double ort = (Double) Renderer.getAttVal(feature, feature.type, 0, Att.ORIENT);
			if (ort != null) str += ort.toString() + "\u0152";
			if (!str.isEmpty()) Renderer.lineText(feature, str, new Font("Arial", Font.PLAIN, 80), Color.black, 0.5, -20);
	  }
	}
	private static void waterways(Feature feature) {
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
		}
	}
}
