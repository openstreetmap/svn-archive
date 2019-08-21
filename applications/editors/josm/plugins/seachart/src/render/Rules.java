// License: GPL. For details, see LICENSE file.
package render;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.HashMap;

import render.ChartContext.RuleSet;
import render.Renderer.LabelStyle;
import s57.S57att.Att;
import s57.S57map.AttMap;
import s57.S57map.Feature;
import s57.S57map.ObjTab;
import s57.S57map.Pflag;
import s57.S57map.Rflag;
import s57.S57obj.Obj;
import s57.S57val;
import s57.S57val.AddMRK;
import s57.S57val.AttVal;
import s57.S57val.BcnSHP;
import s57.S57val.BnkWTW;
import s57.S57val.BoySHP;
import s57.S57val.CatACH;
import s57.S57val.CatCBL;
import s57.S57val.CatCRN;
import s57.S57val.CatDIS;
import s57.S57val.CatHAF;
import s57.S57val.CatLAM;
import s57.S57val.CatLIT;
import s57.S57val.CatLMK;
import s57.S57val.CatMOR;
import s57.S57val.CatNMK;
import s57.S57val.CatOBS;
import s57.S57val.CatOFP;
import s57.S57val.CatOPA;
import s57.S57val.CatPIL;
import s57.S57val.CatREA;
import s57.S57val.CatROD;
import s57.S57val.CatROS;
import s57.S57val.CatSCF;
import s57.S57val.CatSEA;
import s57.S57val.CatSIL;
import s57.S57val.CatSIT;
import s57.S57val.CatSIW;
import s57.S57val.CatSLC;
import s57.S57val.CatWED;
import s57.S57val.CatWRK;
import s57.S57val.ColCOL;
import s57.S57val.ColPAT;
import s57.S57val.FncFNC;
import s57.S57val.MarSYS;
import s57.S57val.StsSTS;
import s57.S57val.TopSHP;
import s57.S57val.TrfTRF;
import s57.S57val.UniHLU;
import s57.S57val.WatLEV;
import s57.S57val.CatVAN;
import symbols.Areas;
import symbols.Beacons;
import symbols.Buoys;
import symbols.Facilities;
import symbols.Harbours;
import symbols.Landmarks;
import symbols.Notices;
import symbols.Symbols;
import symbols.Symbols.Delta;
import symbols.Symbols.Handle;
import symbols.Symbols.LineStyle;
import symbols.Symbols.Patt;
import symbols.Symbols.Scheme;
import symbols.Symbols.Symbol;
import symbols.Topmarks;

/**
 * @author Malcolm Herring
 */
public class Rules {

	static final DecimalFormat df = new DecimalFormat("#.#");

	static final EnumMap<ColCOL, Color> bodyColours = new EnumMap<>(ColCOL.class);
	static {
		bodyColours.put(ColCOL.COL_UNK, new Color(0, true));
		bodyColours.put(ColCOL.COL_WHT, new Color(0xffffff));
		bodyColours.put(ColCOL.COL_BLK, new Color(0x000000));
		bodyColours.put(ColCOL.COL_RED, new Color(0xd40000));
		bodyColours.put(ColCOL.COL_GRN, new Color(0x00d400));
		bodyColours.put(ColCOL.COL_BLU, Color.blue);
		bodyColours.put(ColCOL.COL_YEL, new Color(0xffd400));
		bodyColours.put(ColCOL.COL_GRY, Color.gray);
		bodyColours.put(ColCOL.COL_BRN, new Color(0x8b4513));
		bodyColours.put(ColCOL.COL_AMB, new Color(0xfbf00f));
		bodyColours.put(ColCOL.COL_VIO, new Color(0xee82ee));
		bodyColours.put(ColCOL.COL_ORG, Color.orange);
		bodyColours.put(ColCOL.COL_MAG, new Color(0xf000f0));
		bodyColours.put(ColCOL.COL_PNK, Color.pink);
	}

	static final EnumMap<ColPAT, Patt> pattMap = new EnumMap<>(ColPAT.class);
	static {
		pattMap.put(ColPAT.PAT_UNKN, Patt.Z);
		pattMap.put(ColPAT.PAT_HORI, Patt.H);
		pattMap.put(ColPAT.PAT_VERT, Patt.V);
		pattMap.put(ColPAT.PAT_DIAG, Patt.D);
		pattMap.put(ColPAT.PAT_BRDR, Patt.B);
		pattMap.put(ColPAT.PAT_SQUR, Patt.S);
		pattMap.put(ColPAT.PAT_CROS, Patt.C);
		pattMap.put(ColPAT.PAT_SALT, Patt.X);
		pattMap.put(ColPAT.PAT_STRP, Patt.H);
	}

	static String getName() {
		AttVal<?> name = feature.atts.get(Att.OBJNAM);
		if (name == null) {
			AttMap atts = feature.objs.get(feature.type).get(0);
			if (atts != null) {
				name = atts.get(Att.OBJNAM);
			}
		}
		return (name != null) ? ((String) name.val).replace("&quot;", "\"") : null;
	}

	public static void addName(int z, Font font) {
		addName(z, font, Color.black, new Delta(Handle.CC, new AffineTransform()));
	}

	public static void addName(int z, Font font, Color colour) {
		addName(z, font, colour, new Delta(Handle.CC, new AffineTransform()));
	}

	public static void addName(int z, Font font, Delta delta) {
		addName(z, font, Color.black, delta);
	}

	public static void addName(int z, Font font, Color colour, Delta delta) {
		if (Renderer.zoom >= z) {
			String name = getName();
			if (name != null) {
				Renderer.labelText(name, font, colour, delta);
			}
		}
	}

	static AttMap getAtts(Obj obj, int idx) {
		HashMap<Integer, AttMap> objs = feature.objs.get(obj);
		if (objs == null)
			return null;
		else
			return objs.get(idx);
	}

	public static Object getAttVal(Obj obj, Att att) {
		AttMap atts;
		HashMap<Integer, AttMap> objs;
		AttVal<?> item;
		if ((objs = feature.objs.get(obj)) != null)
			atts = objs.get(0);
		else
			return null;
		if ((item = atts.get(att)) == null)
			return null;
		else
			return item.val;
	}

	public static String getAttStr(Obj obj, Att att) {
		String str = (String) getAttVal(obj, att);
		if (str != null) {
			return str;
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public static Enum<?> getAttEnum(Obj obj, Att att) {
		ArrayList<?> list = (ArrayList<?>) getAttVal(obj, att);
		if (list != null) {
			return ((ArrayList<Enum<?>>) list).get(0);
		}
		return S57val.unknAtt(att);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<?> getAttList(Obj obj, Att att) {
		ArrayList<Enum<?>> list = (ArrayList<Enum<?>>) getAttVal(obj, att);
		if (list != null) {
			return list;
		}
		list = new ArrayList<>();
		list.add(S57val.unknAtt(att));
		return list;
	}

	@SuppressWarnings("unchecked")
	static Scheme getScheme(Obj obj) {
		ArrayList<Color> colours = new ArrayList<>();
		for (ColCOL col : (ArrayList<ColCOL>) getAttList(obj, Att.COLOUR)) {
			colours.add(bodyColours.get(col));
		}
		ArrayList<Patt> patterns = new ArrayList<>();
		for (ColPAT pat : (ArrayList<ColPAT>) getAttList(obj, Att.COLPAT)) {
			patterns.add(pattMap.get(pat));
		}
		return new Scheme(patterns, colours);
	}

	static boolean hasAttribute(Obj obj, Att att) {
		AttMap atts;
		if ((atts = getAtts(obj, 0)) != null) {
			AttVal<?> item = atts.get(att);
			return item != null;
		}
		return false;
	}

	static boolean testAttribute(Obj obj, Att att, Object val) {
		AttMap atts;
		if ((atts = getAtts(obj, 0)) != null) {
			AttVal<?> item = atts.get(att);
			if (item != null) {
				switch (item.conv) {
				case S:
				case A:
					return ((String) item.val).equals(val);
				case E:
				case L:
					return ((ArrayList<?>) item.val).contains(val);
				case F:
				case I:
					return item.val == val;
				}
			}
		}
		return false;
	}

	static boolean hasObject(Obj obj) {
		return (feature.objs.containsKey(obj));
	}

	public static Feature feature;
	static ArrayList<Feature> objects;

	static boolean testObject(Obj obj) {
		return ((objects = Renderer.map.features.get(obj)) != null);
	}

	static boolean testFeature(Feature f) {
		return ((feature = f).reln == Rflag.MASTER);
	}

	public static boolean rules() {
		try {
			if ((Renderer.context.ruleset() == RuleSet.ALL) || (Renderer.context.ruleset() == RuleSet.BASE)) {
				if (testObject(Obj.LNDARE)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.BUAARE)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.HRBFAC)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.HRBBSN)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.LOKBSN)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.LKBSPT)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.LAKARE)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.RIVERS)) for (Feature f : objects) if (testFeature(f)) waterways();
				if (testObject(Obj.CANALS)) for (Feature f : objects) if (testFeature(f)) waterways();
				if (testObject(Obj.DEPARE)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.COALNE)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.ROADWY)) for (Feature f : objects) if (testFeature(f)) highways();
				if (testObject(Obj.RAILWY)) for (Feature f : objects) if (testFeature(f)) highways();
			}
			if (Renderer.context.ruleset() == RuleSet.ALL) {
				if (testObject(Obj.SOUNDG)) for (Feature f : objects) if (testFeature(f)) depths();
				if (testObject(Obj.DEPCNT)) for (Feature f : objects) if (testFeature(f)) depths();
			}
			if (testObject(Obj.SLCONS)) for (Feature f : objects) if (testFeature(f)) shoreline();
			if ((Renderer.context.ruleset() == RuleSet.ALL) || (Renderer.context.ruleset() == RuleSet.SEAMARK)) {
				if (testObject(Obj.PIPSOL)) for (Feature f : objects) if (testFeature(f)) pipelines();
				if (testObject(Obj.CBLSUB)) for (Feature f : objects) if (testFeature(f)) cables();
				if (testObject(Obj.PIPOHD)) for (Feature f : objects) if (testFeature(f)) pipelines();
				if (testObject(Obj.CBLOHD)) for (Feature f : objects) if (testFeature(f)) cables();
				if (testObject(Obj.TSEZNE)) for (Feature f : objects) if (testFeature(f)) separation();
				if (testObject(Obj.TSSCRS)) for (Feature f : objects) if (testFeature(f)) separation();
				if (testObject(Obj.TSSRON)) for (Feature f : objects) if (testFeature(f)) separation();
				if (testObject(Obj.TSELNE)) for (Feature f : objects) if (testFeature(f)) separation();
				if (testObject(Obj.TSSLPT)) for (Feature f : objects) if (testFeature(f)) separation();
				if (testObject(Obj.TSSBND)) for (Feature f : objects) if (testFeature(f)) separation();
				if (testObject(Obj.ISTZNE)) for (Feature f : objects) if (testFeature(f)) separation();
				if (testObject(Obj.SNDWAV)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.WEDKLP)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.OSPARE)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.FAIRWY)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.DRGARE)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.RESARE)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.PRCARE)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.SPLARE)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.SEAARE)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.DMPGRD)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.OBSTRN)) for (Feature f : objects) if (testFeature(f)) obstructions();
				if (testObject(Obj.UWTROC)) for (Feature f : objects) if (testFeature(f)) obstructions();
				if (testObject(Obj.MARCUL)) for (Feature f : objects) if (testFeature(f)) areas();
				if (testObject(Obj.RECTRC)) for (Feature f : objects) if (testFeature(f)) transits();
				if (testObject(Obj.NAVLNE)) for (Feature f : objects) if (testFeature(f)) transits();
				if (testObject(Obj.HRBFAC)) for (Feature f : objects) if (testFeature(f)) harbours();
				if (testObject(Obj.ACHARE)) for (Feature f : objects) if (testFeature(f)) harbours();
				if (testObject(Obj.ACHBRT)) for (Feature f : objects) if (testFeature(f)) harbours();
				if (testObject(Obj.BERTHS)) for (Feature f : objects) if (testFeature(f)) harbours();
				if (testObject(Obj.DISMAR)) for (Feature f : objects) if (testFeature(f)) distances();
				if (testObject(Obj.HULKES)) for (Feature f : objects) if (testFeature(f)) ports();
				if (testObject(Obj.CRANES)) for (Feature f : objects) if (testFeature(f)) ports();
				if (testObject(Obj.LNDMRK)) for (Feature f : objects) if (testFeature(f)) landmarks();
				if (testObject(Obj.SILTNK)) for (Feature f : objects) if (testFeature(f)) landmarks();
				if (testObject(Obj.BUISGL)) for (Feature f : objects) if (testFeature(f)) harbours();
				if (testObject(Obj.MORFAC)) for (Feature f : objects) if (testFeature(f)) moorings();
				if (testObject(Obj.NOTMRK)) for (Feature f : objects) if (testFeature(f)) notices();
				if (testObject(Obj.SMCFAC)) for (Feature f : objects) if (testFeature(f)) marinas();
				if (testObject(Obj.BRIDGE)) for (Feature f : objects) if (testFeature(f)) bridges();
				if (testObject(Obj.PILPNT)) for (Feature f : objects) if (testFeature(f)) points();
				if (testObject(Obj.TOPMAR)) for (Feature f : objects) if (testFeature(f)) points();
				if (testObject(Obj.DAYMAR)) for (Feature f : objects) if (testFeature(f)) points();
				if (testObject(Obj.FOGSIG)) for (Feature f : objects) if (testFeature(f)) points();
				if (testObject(Obj.RDOCAL)) for (Feature f : objects) if (testFeature(f)) callpoint();
				if (testObject(Obj.LITMIN)) for (Feature f : objects) if (testFeature(f)) lights();
				if (testObject(Obj.LITMAJ)) for (Feature f : objects) if (testFeature(f)) lights();
				if (testObject(Obj.LIGHTS)) for (Feature f : objects) if (testFeature(f)) lights();
				if (testObject(Obj.SISTAT)) for (Feature f : objects) if (testFeature(f)) stations();
				if (testObject(Obj.SISTAW)) for (Feature f : objects) if (testFeature(f)) stations();
				if (testObject(Obj.CGUSTA)) for (Feature f : objects) if (testFeature(f)) stations();
				if (testObject(Obj.RDOSTA)) for (Feature f : objects) if (testFeature(f)) stations();
				if (testObject(Obj.RADRFL)) for (Feature f : objects) if (testFeature(f)) stations();
				if (testObject(Obj.RADSTA)) for (Feature f : objects) if (testFeature(f)) stations();
				if (testObject(Obj.RTPBCN)) for (Feature f : objects) if (testFeature(f)) stations();
				if (testObject(Obj.RSCSTA)) for (Feature f : objects) if (testFeature(f)) stations();
				if (testObject(Obj.PILBOP)) for (Feature f : objects) if (testFeature(f)) stations();
				if (testObject(Obj.WTWGAG)) for (Feature f : objects) if (testFeature(f)) gauges();
				if (testObject(Obj.OFSPLF)) for (Feature f : objects) if (testFeature(f)) platforms();
				if (testObject(Obj.WRECKS)) for (Feature f : objects) if (testFeature(f)) wrecks();
				if (testObject(Obj.LITVES)) for (Feature f : objects) if (testFeature(f)) floats();
				if (testObject(Obj.LITFLT)) for (Feature f : objects) if (testFeature(f)) floats();
				if (testObject(Obj.BOYINB)) for (Feature f : objects) if (testFeature(f)) floats();
				if (testObject(Obj.BOYLAT)) for (Feature f : objects) if (testFeature(f)) buoys();
				if (testObject(Obj.BOYCAR)) for (Feature f : objects) if (testFeature(f)) buoys();
				if (testObject(Obj.BOYISD)) for (Feature f : objects) if (testFeature(f)) buoys();
				if (testObject(Obj.BOYSAW)) for (Feature f : objects) if (testFeature(f)) buoys();
				if (testObject(Obj.BOYSPP)) for (Feature f : objects) if (testFeature(f)) buoys();
				if (testObject(Obj.BCNLAT)) for (Feature f : objects) if (testFeature(f)) beacons();
				if (testObject(Obj.BCNCAR)) for (Feature f : objects) if (testFeature(f)) beacons();
				if (testObject(Obj.BCNISD)) for (Feature f : objects) if (testFeature(f)) beacons();
				if (testObject(Obj.BCNSAW)) for (Feature f : objects) if (testFeature(f)) beacons();
                if (testObject(Obj.BCNSPP)) for (Feature f : objects) if (testFeature(f)) beacons();
                if (testObject(Obj.VAATON)) for (Feature f : objects) if (testFeature(f)) virtual();
			}
		} catch (ConcurrentModificationException e) {
			return false;
		} catch (Exception e) {
			return true;
		}
		return true;
	}

	private static void areas() {
		String name = getName();
		switch (feature.type) {
		case BUAARE:
			Renderer.lineVector(new LineStyle(new Color(0x20000000, true)));
			break;
		case COALNE:
			if (Renderer.zoom >= 12)
				Renderer.lineVector(new LineStyle(Color.black, 10));
			break;
		case DEPARE:
			Double depmax = 0.0;
			if (((depmax = (Double) getAttVal(Obj.DEPARE, Att.DRVAL2)) != null) && (depmax <= 0.0)) {
				Renderer.lineVector(new LineStyle(Symbols.Gdries));
			}
			break;
		case LAKARE:
			if ((Renderer.zoom >= 12) || (feature.geom.area > 10.0))
				Renderer.lineVector(new LineStyle(Symbols.Bwater));
			break;
		case DRGARE:
			if (Renderer.zoom < 16)
				Renderer.lineVector(new LineStyle(Color.black, 8, new float[] { 25, 25 }, new Color(0x40ffffff, true)));
			else
				Renderer.lineVector(new LineStyle(Color.black, 8, new float[] { 25, 25 }));
			addName(12, new Font("Arial", Font.PLAIN, 100), new Delta(Handle.CC, new AffineTransform()));
			break;
		case FAIRWY:
            if (Renderer.zoom >= 12) {
                if (feature.geom.area > 1.0) {
                    if (Renderer.zoom < 16)
                        Renderer.lineVector(new LineStyle(new Color(0x20ffffff, true)));
                    else
                        Renderer.lineVector(new LineStyle(Symbols.Mline, 8, new float[] { 50, 50 }));
                } else {
                    if (Renderer.zoom >= 14)
                        Renderer.lineVector(new LineStyle(new Color(0x20ffffff, true)));
                }
            }
			break;
		case LKBSPT:
		case LOKBSN:
		case HRBBSN:
			if (Renderer.zoom >= 12) {
				Renderer.lineVector(new LineStyle(Color.black, 10, Symbols.Bwater));
			} else {
				Renderer.lineVector(new LineStyle(Symbols.Bwater));
			}
			break;
		case HRBFAC:
			if (feature.objs.get(Obj.HRBBSN) != null) {
				if (Renderer.zoom >= 12) {
					Renderer.lineVector(new LineStyle(Color.black, 10, Symbols.Bwater));
				} else {
					Renderer.lineVector(new LineStyle(Symbols.Bwater));
				}
			}
			break;
		case LNDARE:
			Renderer.lineVector(new LineStyle(Symbols.Yland));
			break;
		case MARCUL:
			if (Renderer.zoom >= 12) {
				if (Renderer.zoom >= 14) {
					Renderer.symbol(Areas.MarineFarm);
				}
				if ((feature.geom.area > 0.2) || ((feature.geom.area > 0.05) && (Renderer.zoom >= 14)) || ((feature.geom.area > 0.005) && (Renderer.zoom >= 16))) {
					Renderer.lineVector(new LineStyle(Color.black, 4, new float[] { 10, 10 }));
				}
			}
			break;
		case OSPARE:
			if (testAttribute(feature.type, Att.CATOPA, CatOPA.OPA_WIND)) {
				Renderer.symbol(Areas.WindFarm);
				Renderer.lineVector(new LineStyle(Color.black, 12, new float[] { 40, 40 }));
				addName(15, new Font("Arial", Font.BOLD, 80), new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 120)));
			}
			break;
		case RESARE:
		case MIPARE:
		case DMPGRD:
			if (Renderer.zoom >= 12) {
				Renderer.lineSymbols(Areas.Restricted, 1.0, null, null, 0, Symbols.Mline);
				if (testAttribute(feature.type, Att.CATREA, CatREA.REA_NWAK)) {
					Renderer.symbol(Areas.NoWake);
				}
			}
			break;
		case PRCARE:
			if (Renderer.zoom >= 12) {
				Renderer.lineVector(new LineStyle(Symbols.Mline, 10, new float[] { 40, 40 }));
			}
			break;
		case SEAARE:
			switch ((CatSEA) getAttEnum(feature.type, Att.CATSEA)) {
			case SEA_RECH:
				if ((Renderer.zoom >= 10) && (name != null))
					if (feature.geom.prim == Pflag.LINE) {
						Renderer.lineText(name, new Font("Arial", Font.PLAIN, 150), Color.black, -40);
					} else {
						Renderer.labelText(name, new Font("Arial", Font.PLAIN, 150), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -40)));
					}
				break;
			case SEA_BAY:
				if ((Renderer.zoom >= 12) && (name != null))
					if (feature.geom.prim == Pflag.LINE) {
						Renderer.lineText(name, new Font("Arial", Font.PLAIN, 150), Color.black, -40);
					} else {
						Renderer.labelText(name, new Font("Arial", Font.PLAIN, 150), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -40)));
					}
				break;
			case SEA_SHOL:
				if (Renderer.zoom >= 14) {
					if (feature.geom.prim == Pflag.AREA) {
						Renderer.lineVector(new LineStyle(new Color(0xc480ff), 4, new float[] { 25, 25 }));
						if (name != null) {
							Renderer.labelText(name, new Font("Arial", Font.ITALIC, 75), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -40)));
							Renderer.labelText("(Shoal)", new Font("Arial", Font.PLAIN, 60), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 20)));
						}
					} else if (feature.geom.prim == Pflag.LINE) {
						if (name != null) {
							Renderer.lineText(name, new Font("Arial", Font.ITALIC, 75), Color.black, -40);
							Renderer.lineText("(Shoal)", new Font("Arial", Font.PLAIN, 60), Color.black, 20);
						}
					} else {
						if (name != null) {
							Renderer.labelText(name, new Font("Arial", Font.ITALIC, 75), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -40)));
							Renderer.labelText("(Shoal)", new Font("Arial", Font.PLAIN, 60), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 20)));
						}
					}
				}
				break;
			case SEA_GAT:
			case SEA_NRRW:
				addName(12, new Font("Arial", Font.PLAIN, 100));
				break;
			default:
				break;
			}
			break;
		case SNDWAV:
			if (Renderer.zoom >= 12)
				Renderer.fillPattern(Areas.Sandwaves);
			break;
		case WEDKLP:
			if (Renderer.zoom >= 12) {
				switch ((CatWED) getAttEnum(feature.type, Att.CATWED)) {
				case WED_KELP:
					if (feature.geom.prim == Pflag.AREA) {
						Renderer.fillPattern(Areas.KelpA);
					} else {
						Renderer.symbol(Areas.KelpS);
					}
					break;
				default:
					break;
				}
			}
			break;
		case SPLARE:
			if (Renderer.zoom >= 12) {
				Renderer.symbol(Areas.Plane, new Scheme(Symbols.Msymb));
				Renderer.lineSymbols(Areas.Restricted, 0.5, Areas.LinePlane, null, 10, Symbols.Mline);
			}
			addName(15, new Font("Arial", Font.BOLD, 80), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -90)));
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private static void beacons() {
		if ((Renderer.zoom >= 14) || ((Renderer.zoom >= 12) && ((feature.type == Obj.BCNLAT) || (feature.type == Obj.BCNCAR)))
				|| ((Renderer.zoom >= 11) && ((feature.type == Obj.BCNSAW) || hasObject(Obj.RTPBCN)))) {
			if (testAttribute(feature.type, Att.STATUS, StsSTS.STS_ILLD)) {
				Renderer.symbol(Beacons.Floodlight);
			}
			BcnSHP shape = (BcnSHP) getAttEnum(feature.type, Att.BCNSHP);
			if (shape == BcnSHP.BCN_UNKN)
				shape = BcnSHP.BCN_PILE;
			if ((shape == BcnSHP.BCN_WTHY) && (feature.type == Obj.BCNLAT)) {
				switch ((CatLAM) getAttEnum(feature.type, Att.CATLAM)) {
				case LAM_PORT:
					Renderer.symbol(Beacons.WithyPort);
					break;
				case LAM_STBD:
					Renderer.symbol(Beacons.WithyStarboard);
					break;
				default:
					Renderer.symbol(Beacons.Stake, getScheme(feature.type));
				}
			} else if (shape == BcnSHP.BCN_PRCH && feature.type == Obj.BCNLAT && !feature.objs.containsKey(Obj.TOPMAR)) {
				switch ((CatLAM) getAttEnum(feature.type, Att.CATLAM)) {
				case LAM_PORT:
					Renderer.symbol(Beacons.PerchPort);
					break;
				case LAM_STBD:
					Renderer.symbol(Beacons.PerchStarboard);
					break;
				default:
					Renderer.symbol(Beacons.Stake, getScheme(feature.type));
				}
			} else {
				Renderer.symbol(Beacons.Shapes.get(shape), getScheme(feature.type));
				if (feature.objs.containsKey(Obj.TOPMAR)) {
					AttMap topmap = feature.objs.get(Obj.TOPMAR).get(0);
					if (testAttribute(Obj.TOPMAR, Att.STATUS, StsSTS.STS_ILLD)) {
						Renderer.symbol(Beacons.Floodlight);
					}
					if (topmap.containsKey(Att.TOPSHP)) {
						Renderer.symbol(Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(Obj.TOPMAR), Topmarks.BeaconDelta);
					}
				} else if (feature.objs.containsKey(Obj.DAYMAR)) {
					AttMap topmap = feature.objs.get(Obj.DAYMAR).get(0);
					if (topmap.containsKey(Att.TOPSHP)) {
						Renderer.symbol(Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(Obj.DAYMAR), Topmarks.BeaconDelta);
					}
				}
			}
			if (hasObject(Obj.NOTMRK))
				notices();
			addName(15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(60, -50)));
			Signals.addSignals();
		}
	}

	@SuppressWarnings("unchecked")
	private static void buoys() {
		if ((Renderer.zoom >= 14) || ((Renderer.zoom >= 12) && ((feature.type == Obj.BOYLAT) || (feature.type == Obj.BOYCAR))) || ((Renderer.zoom >= 11) && ((feature.type == Obj.BOYSAW) || hasObject(Obj.RTPBCN)))) {
			BoySHP shape = (BoySHP) getAttEnum(feature.type, Att.BOYSHP);
			if (shape == BoySHP.BOY_UNKN)
				shape = BoySHP.BOY_PILR;
			Renderer.symbol(Buoys.Shapes.get(shape), getScheme(feature.type));
			if (feature.objs.containsKey(Obj.TOPMAR)) {
				AttMap topmap = feature.objs.get(Obj.TOPMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					Renderer.symbol(Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(Obj.TOPMAR), Topmarks.BuoyDeltas.get(shape));
				}
			} else if (feature.objs.containsKey(Obj.DAYMAR)) {
				AttMap topmap = feature.objs.get(Obj.DAYMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					Renderer.symbol(Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(Obj.DAYMAR), Topmarks.BuoyDeltas.get(shape));
				}
			}
			addName(15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(60, -50)));
			Signals.addSignals();
		}
	}

	private static void bridges() {
		if (Renderer.zoom >= 16) {
			double verclr, verccl, vercop, horclr;
			AttMap atts = feature.objs.get(Obj.BRIDGE).get(0);
			String vstr = "";
			String hstr = "";
			if (atts != null) {
				if (atts.containsKey(Att.HORCLR)) {
					horclr = (Double) atts.get(Att.HORCLR).val;
					hstr = String.valueOf(horclr);
				}
				if (atts.containsKey(Att.VERCLR)) {
					verclr = (Double) atts.get(Att.VERCLR).val;
				} else {
					verclr = atts.containsKey(Att.VERCSA) ? (Double) atts.get(Att.VERCSA).val : 0;
				}
				verccl = atts.containsKey(Att.VERCCL) ? (Double) atts.get(Att.VERCCL).val : 0;
				vercop = atts.containsKey(Att.VERCOP) ? (Double) atts.get(Att.VERCOP).val : 0;
				if (verclr > 0) {
					vstr += String.valueOf(verclr);
				} else if (verccl > 0) {
					if (vercop == 0) {
						vstr += String.valueOf(verccl) + "/-";
					} else {
						vstr += String.valueOf(verccl) + "/" + String.valueOf(vercop);
					}
				}
				if (hstr.isEmpty() && !vstr.isEmpty()) {
					Renderer.labelText(vstr, new Font("Arial", Font.PLAIN, 30), Color.black, LabelStyle.VCLR, Color.black, Color.white, new Delta(Handle.CC));
				} else if (!hstr.isEmpty() && !vstr.isEmpty()) {
					Renderer.labelText(vstr, new Font("Arial", Font.PLAIN, 30), Color.black, LabelStyle.VCLR, Color.black, Color.white, new Delta(Handle.BC));
					Renderer.labelText(hstr, new Font("Arial", Font.PLAIN, 30), Color.black, LabelStyle.HCLR, Color.black, Color.white, new Delta(Handle.TC));
				} else if (!hstr.isEmpty() && vstr.isEmpty()) {
					Renderer.labelText(hstr, new Font("Arial", Font.PLAIN, 30), Color.black, LabelStyle.HCLR, Color.black, Color.white, new Delta(Handle.CC));
				}
			}
		}
	}

	private static void cables() {
		if ((Renderer.zoom >= 16) && (feature.geom.length < 2)) {
			if (feature.type == Obj.CBLSUB) {
				Renderer.lineSymbols(Areas.Cable, 0.0, null, null, 0, Symbols.Mline);
			} else if (feature.type == Obj.CBLOHD) {
				AttMap atts = feature.objs.get(Obj.CBLOHD).get(0);
				if (atts != null && atts.containsKey(Att.CATCBL) && atts.get(Att.CATCBL).val == CatCBL.CBL_POWR) {
					Renderer.lineSymbols(Areas.CableDash, 0, Areas.CableDot, Areas.CableFlash, 2, Color.black);
				} else {
					Renderer.lineSymbols(Areas.CableDash, 0, Areas.CableDot, null, 2, Color.black);
				}
				if (atts != null) {
					if (atts.containsKey(Att.VERCLR)) {
						Renderer.labelText(String.valueOf(atts.get(Att.VERCLR).val), new Font("Arial", Font.PLAIN, 50), Color.black, LabelStyle.VCLR, Color.black, new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 25)));
					} else if (atts.containsKey(Att.VERCSA)) {
						Renderer.labelText(String.valueOf(atts.get(Att.VERCSA).val), new Font("Arial", Font.PLAIN, 50), Color.black, LabelStyle.PCLR, Color.black, new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 25)));
					}
				}
			}
		}
	}

	private static void callpoint() {
		if (Renderer.zoom >= 14) {
			Symbol symb = Harbours.CallPoint2;
			TrfTRF trf = (TrfTRF) getAttEnum(feature.type, Att.TRAFIC);
			if (trf != TrfTRF.TRF_TWOW) {
				symb = Harbours.CallPoint1;
			}
			Double orient = 0.0;
			if ((orient = (Double) getAttVal(feature.type, Att.ORIENT)) == null) {
				orient = 0.0;
			}
			Renderer.symbol(symb, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(orient))));
			String chn;
			if (!(chn = getAttStr(feature.type, Att.COMCHA)).isEmpty()) {
				Renderer.labelText(("Ch." + chn), new Font("Arial", Font.PLAIN, 50), Color.black, new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 50)));
			}
		}
	}

	private static void depths() {
		switch (feature.type) {
		case SOUNDG:
			if ((Renderer.zoom >= 14) && hasAttribute(Obj.SOUNDG, Att.VALSOU)) {
				double depth = (double) getAttVal(Obj.SOUNDG, Att.VALSOU);
				String dstr = df.format(depth);
				String[] tok = dstr.split("[-.]");
				String ul = "";
				String id = tok[0];
				String dd = "";
				if (tok[0].equals("")) {
					for (int i = 0; i < tok[1].length(); i++) {
						ul += "_";
					}
					id = tok[1];
					dd = (tok.length == 3) ? tok[2] : "";
				} else {
					dd = (tok.length == 2) ? tok[1] : "";
				}
				Renderer.labelText(ul, new Font("Arial", Font.PLAIN, 30), Color.black, new Delta(Handle.RC, AffineTransform.getTranslateInstance(10, 15)));
				Renderer.labelText(id, new Font("Arial", Font.PLAIN, 30), Color.black, new Delta(Handle.RC, AffineTransform.getTranslateInstance(10, 0)));
				Renderer.labelText(dd, new Font("Arial", Font.PLAIN, 20), Color.black, new Delta(Handle.LC, AffineTransform.getTranslateInstance(15, 10)));
			}
			break;
		case DEPCNT:
			break;
		default:
			break;
		}
	}

	private static void distances() {
		if (Renderer.zoom >= 14) {
			if (!testAttribute(Obj.DISMAR, Att.CATDIS, CatDIS.DIS_NONI)) {
				Renderer.symbol(Harbours.DistanceI);
			} else {
				Renderer.symbol(Harbours.DistanceU);
			}
			if (Renderer.zoom >= 15) {
				AttMap atts = getAtts(Obj.DISMAR, 0);
				if (atts != null && atts.containsKey(Att.WTWDIS)) {
					Double dist = (Double) atts.get(Att.WTWDIS).val;
					String str = "";
					if (atts.containsKey(Att.HUNITS)) {
						switch ((UniHLU) getAttEnum(Obj.DISMAR, Att.HUNITS)) {
						case HLU_METR:
							str += "m ";
							break;
						case HLU_FEET:
							str += "ft ";
							break;
						case HLU_HMTR:
							str += "hm ";
							break;
						case HLU_KMTR:
							str += "km ";
							break;
						case HLU_SMIL:
							str += "M ";
							break;
						case HLU_NMIL:
							str += "NM ";
							break;
						default:
							break;
						}
					}
					str += String.format("%1.0f", dist);
					Renderer.labelText(str, new Font("Arial", Font.PLAIN, 40), Color.black, new Delta(Handle.CC, AffineTransform.getTranslateInstance(0, 45)));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void floats() {
		if ((Renderer.zoom >= 12) || ((Renderer.zoom >= 11) && ((feature.type == Obj.LITVES) || (feature.type == Obj.BOYINB) || hasObject(Obj.RTPBCN)))) {
			switch (feature.type) {
			case LITVES:
				Renderer.symbol(Buoys.Super, getScheme(feature.type));
				break;
			case LITFLT:
				Renderer.symbol(Buoys.Float, getScheme(feature.type));
				break;
			case BOYINB:
				Renderer.symbol(Buoys.Super, getScheme(feature.type));
				break;
			default:
				break;
			}
			if (feature.objs.containsKey(Obj.TOPMAR)) {
				AttMap topmap = feature.objs.get(Obj.TOPMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					Renderer.symbol(Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(Obj.TOPMAR), Topmarks.FloatDelta);
				}
			} else if (feature.objs.containsKey(Obj.DAYMAR)) {
				AttMap topmap = feature.objs.get(Obj.DAYMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					Renderer.symbol(Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(Obj.DAYMAR), Topmarks.FloatDelta);
				}
			}
			addName(15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(20, -50)));
			Signals.addSignals();
		}
	}

	private static void gauges() {
		if (Renderer.zoom >= 14) {
			Renderer.symbol(Harbours.TideGauge);
			addName(15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(20, -50)));
			Signals.addSignals();
		}
	}

	@SuppressWarnings("unchecked")
	private static void harbours() {
		String name = getName();
		switch (feature.type) {
		case ACHBRT:
			if (Renderer.zoom >= 14) {
				Renderer.symbol(Harbours.Anchor, new Scheme(Symbols.Msymb));
				if (Renderer.zoom >= 15) {
					Renderer.labelText(name == null ? "" : name, new Font("Arial", Font.PLAIN, 30), Symbols.Msymb, LabelStyle.RRCT, Symbols.Msymb, Color.white, new Delta(Handle.BC));
				}
			}
			if (getAttVal(Obj.ACHBRT, Att.RADIUS) != null) {
				double radius;
				if ((radius = (Double) getAttVal(Obj.ACHBRT, Att.RADIUS)) != 0) {
					UniHLU units = (UniHLU) getAttEnum(Obj.ACHBRT, Att.HUNITS);
					if (units == UniHLU.HLU_UNKN) {
						units = UniHLU.HLU_METR;
					}
					Renderer.lineCircle(new LineStyle(Symbols.Mline, 4, new float[] { 10, 10 }, null), radius, units);
				}
			}
			break;
		case ACHARE:
			if (Renderer.zoom >= 12) {
				if (feature.geom.prim != Pflag.AREA) {
					Renderer.symbol(Harbours.Anchorage, new Scheme(Color.black));
				} else {
					Renderer.symbol(Harbours.Anchorage, new Scheme(Symbols.Mline));
					Renderer.lineSymbols(Areas.Restricted, 1.0, Areas.LineAnchor, null, 10, Symbols.Mline);
				}
				addName(15, new Font("Arial", Font.BOLD, 60), Symbols.Mline, new Delta(Handle.LC, AffineTransform.getTranslateInstance(70, 0)));
				ArrayList<StsSTS> sts = (ArrayList<StsSTS>) getAttList(Obj.ACHARE, Att.STATUS);
				if (Renderer.zoom >= 15 && sts.contains(StsSTS.STS_RESV)) {
					Renderer.labelText("Reserved", new Font("Arial", Font.PLAIN, 50), Symbols.Mline, new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 60)));
				}
				ArrayList<CatACH> cats = (ArrayList<CatACH>) getAttList(Obj.ACHARE, Att.CATACH);
				int dy = (cats.size() - 1) * -30;
				for (CatACH cat : cats) {
					switch (cat) {
					case ACH_DEEP:
						Renderer.labelText("DW", new Font("Arial", Font.BOLD, 50), Symbols.Msymb, new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					case ACH_TANK:
						Renderer.labelText("Tanker", new Font("Arial", Font.BOLD, 50), Symbols.Msymb, new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					case ACH_H24P:
						Renderer.labelText("24h", new Font("Arial", Font.BOLD, 50), Symbols.Msymb, new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					case ACH_EXPL:
						Renderer.symbol(Harbours.Explosives, new Scheme(Symbols.Msymb), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					case ACH_QUAR:
						Renderer.symbol(Harbours.Hospital, new Scheme(Symbols.Msymb), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					case ACH_SEAP:
						Renderer.symbol(Areas.Seaplane, new Scheme(Symbols.Msymb), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					default:
					}
				}
			}
			break;
		case BERTHS:
			if (Renderer.zoom >= 14) {
				Renderer.lineVector(new LineStyle(Symbols.Mline, 6, new float[] { 20, 20 }));
				Renderer.labelText(name == null ? " " : name, new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, LabelStyle.RRCT, Symbols.Mline, Color.white);
			}
			break;
        case BUISGL:
            if (Renderer.zoom >= 16) {
                Renderer.lineVector(new LineStyle(Color.black, 8, new Color(0xffc0c0c0, true)));
                if (testAttribute(Obj.BUISGL, Att.STATUS, StsSTS.STS_ILLD)) {
                    Renderer.symbol(Beacons.Floodlight);
                }
                ArrayList<Symbol> symbols = new ArrayList<>();
                ArrayList<FncFNC> fncs = (ArrayList<FncFNC>) getAttList(Obj.BUISGL, Att.FUNCTN);
                for (FncFNC fnc : fncs) {
                    symbols.add(Landmarks.Funcs.get(fnc));
                }
                if (feature.objs.containsKey(Obj.SMCFAC)) {
                    ArrayList<CatSCF> scfs = (ArrayList<CatSCF>) getAttList(Obj.SMCFAC, Att.CATSCF);
                    for (CatSCF scf : scfs) {
                        symbols.add(Facilities.Cats.get(scf));
                    }
                }
                Renderer.cluster(symbols);
                Signals.addSignals();
            }
            break;
		case HRBFAC:
			if (Renderer.zoom >= 12) {
				ArrayList<CatHAF> cathaf = (ArrayList<CatHAF>) getAttList(Obj.HRBFAC, Att.CATHAF);
				if (cathaf.size() == 1) {
					switch (cathaf.get(0)) {
					case HAF_MRNA:
						Renderer.symbol(Harbours.Marina);
						break;
					case HAF_MANF:
						Renderer.symbol(Harbours.MarinaNF);
						break;
					case HAF_FISH:
						Renderer.symbol(Harbours.Fishing);
						break;
					default:
						Renderer.symbol(Harbours.Harbour);
						break;
					}
				} else {
					Renderer.symbol(Harbours.Harbour);
				}
			}
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private static void highways() {
		switch (feature.type) {
		case ROADWY:
			ArrayList<CatROD> cat = (ArrayList<CatROD>) getAttList(Obj.ROADWY, Att.CATROD);
			if (cat.size() > 0) {
				switch (cat.get(0)) {
				case ROD_MWAY:
					Renderer.lineVector(new LineStyle(Color.black, 20));
					break;
				case ROD_MAJR:
					Renderer.lineVector(new LineStyle(Color.black, 15));
					break;
				case ROD_MINR:
					Renderer.lineVector(new LineStyle(Color.black, 10));
					break;
				default:
					Renderer.lineVector(new LineStyle(Color.black, 5));
				}
			} else {
				Renderer.lineVector(new LineStyle(Color.black, 5));
			}
			break;
		case RAILWY:
			Renderer.lineVector(new LineStyle(Color.gray, 10));
			Renderer.lineVector(new LineStyle(Color.black, 10, new float[] { 30, 30 }));
			break;
		default:
		}
	}

	@SuppressWarnings("unchecked")
	private static void landmarks() {
		if (!hasAttribute(Obj.LNDMRK, Att.CATLMK) && (!hasAttribute(Obj.LNDMRK, Att.FUNCTN) || testAttribute(Obj.LNDMRK, Att.FUNCTN, FncFNC.FNC_LGHT)) && hasObject(Obj.LIGHTS)) {
			lights();
			addName(15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(60, -50)));
		}
		else if (Renderer.zoom >= 12) {
			switch (feature.type) {
			case LNDMRK:
				if (testAttribute(Obj.LNDMRK, Att.STATUS, StsSTS.STS_ILLD)) {
					Renderer.symbol(Beacons.Floodlight);
				}
				ArrayList<CatLMK> cats = (ArrayList<CatLMK>) getAttList(feature.type, Att.CATLMK);
				Symbol catSym = Landmarks.Shapes.get(cats.get(0));
				ArrayList<FncFNC> fncs = (ArrayList<FncFNC>) getAttList(feature.type, Att.FUNCTN);
				Symbol fncSym = Landmarks.Funcs.get(fncs.get(0));
				if ((fncs.get(0) == FncFNC.FNC_CHCH) && (cats.get(0) == CatLMK.LMK_TOWR))
					catSym = Landmarks.ChurchTower;
				if (cats.get(0) == CatLMK.LMK_RADR)
					fncSym = Landmarks.RadioTV;
				Renderer.symbol(catSym);
				if (catSym != Landmarks.Spire)
					Renderer.symbol(fncSym);
				break;
			case SILTNK:
				if (testAttribute(feature.type, Att.CATSIL, CatSIL.SIL_WTRT))
					Renderer.symbol(Landmarks.WaterTower);
				break;
			default:
				break;
			}
			Signals.addSignals();
            addName(15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(60, -50)));
		}
	}

	@SuppressWarnings("unchecked")
	private static void lights() {
		boolean ok = false;
		switch (feature.type) {
		case LITMAJ:
		case LNDMRK:
            Renderer.symbol(Beacons.LightMajor);
			if (Renderer.zoom >= 12) {
				ok = true;
			} else {
			    Signals.lights();
			}
			break;
		case LITMIN:
		case LIGHTS:
		case PILPNT:
			if (Renderer.zoom >= 14) {
				if (testAttribute(Obj.LIGHTS, Att.CATLIT, CatLIT.LIT_FLDL)) {
					Renderer.symbol(Beacons.Floodlight, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(90))));
					Renderer.symbol(Harbours.SignalStation);
				} else {
					Renderer.symbol(Beacons.LightMinor);
				}
				ok = true;
			}
			break;
		default:
			break;
		}
		if (ok) {
			if (feature.objs.containsKey(Obj.TOPMAR)) {
				if (testAttribute(Obj.TOPMAR, Att.STATUS, StsSTS.STS_ILLD)) {
					Renderer.symbol(Beacons.Floodlight);
				}
				AttMap topmap = feature.objs.get(Obj.TOPMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					Renderer.symbol(Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(Obj.TOPMAR), Topmarks.LightDelta);
				}
			} else if (feature.objs.containsKey(Obj.DAYMAR)) {
				AttMap topmap = feature.objs.get(Obj.DAYMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					Renderer.symbol(Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(Obj.DAYMAR), Topmarks.LightDelta);
				}
			}
			Signals.addSignals();
            addName(15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -50)));
		}
	}

	@SuppressWarnings("unchecked")
	private static void marinas() {
		if (Renderer.zoom >= 16) {
			ArrayList<Symbol> symbols = new ArrayList<>();
			ArrayList<CatSCF> scfs = (ArrayList<CatSCF>) getAttList(Obj.SMCFAC, Att.CATSCF);
			for (CatSCF scf : scfs) {
				symbols.add(Facilities.Cats.get(scf));
			}
			Renderer.cluster(symbols);
		}
	}

	private static void moorings() {
		if (Renderer.zoom >= 14) {
			switch ((CatMOR) getAttEnum(feature.type, Att.CATMOR)) {
			case MOR_DLPN:
				Renderer.symbol(Harbours.Dolphin);
	            Signals.addSignals();
				break;
			case MOR_DDPN:
				Renderer.symbol(Harbours.DeviationDolphin);
	            Signals.addSignals();
				break;
			case MOR_BLRD:
			case MOR_POST:
				Renderer.symbol(Harbours.Bollard);
				break;
			case MOR_BUOY:
			    if (Renderer.zoom >= 17) {
			        BoySHP shape = (BoySHP) getAttEnum(feature.type, Att.BOYSHP);
			        if (shape == BoySHP.BOY_UNKN) {
			            shape = BoySHP.BOY_SPHR;
			        }
			        Renderer.symbol(Buoys.Shapes.get(shape), getScheme(feature.type));
			        Renderer.symbol(Topmarks.TopMooring, Topmarks.BuoyDeltas.get(shape));
			    }
				break;
			default:
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void notices() {
		if (Renderer.zoom >= 14) {
			double dx = 0.0, dy = 0.0;
			switch (feature.type) {
			case BCNCAR:
			case BCNISD:
			case BCNLAT:
			case BCNSAW:
			case BCNSPP:
				if (testAttribute(Obj.TOPMAR, Att.TOPSHP, TopSHP.TOP_BORD) || testAttribute(Obj.DAYMAR, Att.TOPSHP, TopSHP.TOP_BORD)) {
					dy = -100.0;
				} else {
					dy = -45.0;
				}
				break;
			case NOTMRK:
				dy = 0.0;
				break;
			default:
				return;
			}
			MarSYS sys = MarSYS.SYS_CEVN;
			BnkWTW bnk = BnkWTW.BWW_UNKN;
			AttVal<?> att = feature.atts.get(Att.MARSYS);
			if (att != null)
				sys = (MarSYS) att.val;
			att = feature.atts.get(Att.BNKWTW);
			if (att != null)
				bnk = (BnkWTW) att.val;
			ObjTab objs = feature.objs.get(Obj.NOTMRK);
			int n = objs.size();
			if (n > 5) {
				Renderer.symbol(Notices.Notice, new Delta(Handle.CC, AffineTransform.getTranslateInstance(dx, dy)));
			} else {
				int i = 0;
				for (AttMap atts : objs.values()) {
					if (atts.get(Att.MARSYS) != null)
						sys = ((ArrayList<MarSYS>) atts.get(Att.MARSYS).val).get(0);
					if (atts.get(Att.BNKWTW) != null)
						bnk = ((ArrayList<BnkWTW>) atts.get(Att.BNKWTW).val).get(0);
					CatNMK cat = CatNMK.NMK_UNKN;
					if (atts.get(Att.CATNMK) != null)
						cat = ((ArrayList<CatNMK>) atts.get(Att.CATNMK).val).get(0);
					Symbol sym = Notices.getNotice(cat, sys, bnk);
					Scheme sch = Notices.getScheme(sys, bnk);
					ArrayList<AddMRK> add = new ArrayList<>();
					if (atts.get(Att.ADDMRK) != null)
						add = (ArrayList<AddMRK>) atts.get(Att.ADDMRK).val;
					Handle h = Handle.CC;
					double ax = 0.0;
					double ay = 0.0;
					switch (i) {
					case 0:
						if (n != 1)
							h = null;
						break;
					case 1:
						if (n <= 3) {
							h = Handle.RC;
							ax = -30;
							ay = dy;
						} else {
							h = Handle.BR;
						}
						break;
					case 2:
						if (n <= 3)
							h = Handle.LC;
						else
							h = Handle.BL;
						break;
					case 3:
						if (n == 4)
							h = Handle.TC;
						else
							h = Handle.TR;
						break;
					case 4:
						h = Handle.TL;
						break;
					}
					if (h != null) {
						Renderer.symbol(sym, sch, new Delta(h, AffineTransform.getTranslateInstance(dx, dy)));
						if (!add.isEmpty())
							Renderer.symbol(Notices.NoticeBoard, new Delta(Handle.BC, AffineTransform.getTranslateInstance(ax, ay - 30)));
					}
					i++;
				}
			}
		}
	}

	private static void obstructions() {
		if ((Renderer.zoom >= 12) && (feature.type == Obj.OBSTRN)) {
			if (getAttEnum(feature.type, Att.CATOBS) == CatOBS.OBS_BOOM) {
				Renderer.lineVector(new LineStyle(Color.black, 5, new float[] { 20, 20 }, null));
				if (Renderer.zoom >= 15) {
					Renderer.lineText("Boom", new Font("Arial", Font.PLAIN, 40), Color.black, -20);
				}
			}
		}
		if ((Renderer.zoom >= 14) && (feature.type == Obj.UWTROC)) {
			switch ((WatLEV) getAttEnum(feature.type, Att.WATLEV)) {
			case LEV_CVRS:
				Renderer.symbol(Areas.RockC);
				break;
			case LEV_AWSH:
				Renderer.symbol(Areas.RockA);
				break;
			default:
				Renderer.symbol(Areas.Rock);
			}
		}
	}

	private static void pipelines() {
		if ((Renderer.zoom >= 16) && (feature.geom.length < 2)) {
			if (feature.type == Obj.PIPSOL) {
				Renderer.lineSymbols(Areas.Pipeline, 0.33, null, null, 0, Symbols.Mline);
			} else if (feature.type == Obj.PIPOHD) {
				Renderer.lineVector(new LineStyle(Color.black, 8));
				AttMap atts = feature.atts;
				double verclr = 0;
				if (atts != null) {
					if (atts.containsKey(Att.VERCLR)) {
						verclr = (Double) atts.get(Att.VERCLR).val;
					} else {
						verclr = atts.containsKey(Att.VERCSA) ? (Double) atts.get(Att.VERCSA).val : 0;
					}
					if (verclr > 0) {
						Renderer.labelText(String.valueOf(verclr), new Font("Arial", Font.PLAIN, 50), Color.black, LabelStyle.VCLR, Color.black, new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 25)));
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void platforms() {
		ArrayList<CatOFP> cats = (ArrayList<CatOFP>) getAttList(Obj.OFSPLF, Att.CATOFP);
		if (cats.get(0) == CatOFP.OFP_FPSO)
			Renderer.symbol(Buoys.Storage);
		else
			Renderer.symbol(Landmarks.Platform);
		if (testAttribute(feature.type, Att.STATUS, StsSTS.STS_ILLD)) {
			Renderer.symbol(Beacons.Floodlight);
		}
		addName(15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(20, -50)));
		Signals.addSignals();
	}

	@SuppressWarnings("unchecked")
	private static void points() {
		boolean ok = false;
		switch (feature.type) {
		case FOGSIG:
			if (Renderer.zoom >= 12) {
				if (feature.objs.containsKey(Obj.LIGHTS))
					lights();
				else
					Renderer.symbol(Harbours.Post);
				ok = true;
			}
			break;
		default:
			if (Renderer.zoom >= 14) {
				if (testAttribute(feature.type, Att.STATUS, StsSTS.STS_ILLD)) {
					Renderer.symbol(Beacons.Floodlight);
				}
				if (feature.objs.containsKey(Obj.LIGHTS))
					lights();
				else
					Renderer.symbol(Harbours.Post);
				ok = true;
			}
			break;
		}
		if (ok) {
			if (feature.objs.containsKey(Obj.TOPMAR)) {
				AttMap topmap = feature.objs.get(Obj.TOPMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					Renderer.symbol(Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(Obj.TOPMAR), null);
				}
			} else if (feature.objs.containsKey(Obj.DAYMAR)) {
				AttMap topmap = feature.objs.get(Obj.DAYMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					Renderer.symbol(Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(Obj.DAYMAR), null);
				}
			}
			Signals.addSignals();
		}
	}

	private static void ports() {
		if (Renderer.zoom >= 14) {
			if (feature.type == Obj.CRANES) {
				if ((CatCRN) getAttEnum(feature.type, Att.CATCRN) == CatCRN.CRN_CONT)
					Renderer.symbol(Harbours.ContainerCrane);
				else
					Renderer.symbol(Harbours.PortCrane);
			} else if (feature.type == Obj.HULKES) {
				Renderer.lineVector(new LineStyle(Color.black, 4, null, new Color(0xffe000)));
				addName(15, new Font("Arial", Font.BOLD, 40));
			}
		}
	}

	private static void separation() {
		switch (feature.type) {
		case TSEZNE:
		case TSSCRS:
		case TSSRON:
			if (Renderer.zoom <= 15)
				Renderer.lineVector(new LineStyle(Symbols.Mtss));
			else
				Renderer.lineVector(new LineStyle(Symbols.Mtss, 20, null, null));
			addName(10, new Font("Arial", Font.BOLD, 150), Symbols.Mline);
			break;
		case TSELNE:
			Renderer.lineVector(new LineStyle(Symbols.Mtss, 20, null, null));
			break;
		case TSSLPT:
			Renderer.lineSymbols(Areas.LaneArrow, 0.5, null, null, 0, Symbols.Mtss);
			break;
		case TSSBND:
			Renderer.lineVector(new LineStyle(Symbols.Mtss, 20, new float[] { 40, 40 }, null));
			break;
		case ISTZNE:
			Renderer.lineSymbols(Areas.Restricted, 1.0, null, null, 0, Symbols.Mtss);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private static void shoreline() {
		CatSLC cat = (CatSLC) getAttEnum(feature.type, Att.CATSLC);
		if ((Renderer.context.ruleset() == RuleSet.ALL) || (Renderer.context.ruleset() == RuleSet.BASE)) {
			if ((cat != CatSLC.SLC_SWAY) && (cat != CatSLC.SLC_TWAL)) {
				if (Renderer.zoom >= 12) {
					Renderer.lineVector(new LineStyle(Color.black, 10, Symbols.Yland));
				} else {
					Renderer.lineVector(new LineStyle(Symbols.Yland));
				}
			}
		}
		if ((Renderer.context.ruleset() == RuleSet.ALL) || (Renderer.context.ruleset() == RuleSet.SEAMARK)) {
			if (Renderer.zoom >= 12) {
				switch (cat) {
				case SLC_TWAL:
					WatLEV lev = (WatLEV) getAttEnum(feature.type, Att.WATLEV);
					if (lev == WatLEV.LEV_CVRS) {
						Renderer.lineVector(new LineStyle(Color.black, 10, new float[] { 40, 40 }, null));
						if (Renderer.zoom >= 15)
							Renderer.lineText("(covers)", new Font("Arial", Font.PLAIN, 40), Color.black, 80);
					} else {
						Renderer.lineVector(new LineStyle(Color.black, 10, null, null));
					}
					if (Renderer.zoom >= 15)
						Renderer.lineText("Training Wall", new Font("Arial", Font.PLAIN, 40), Color.black, -30);
					break;
				case SLC_SWAY:
					Renderer.lineVector(new LineStyle(Color.black, 2, null, new Color(0xffe000)));
					if ((Renderer.zoom >= 16) && feature.objs.containsKey(Obj.SMCFAC)) {
						ArrayList<Symbol> symbols = new ArrayList<>();
						ArrayList<CatSCF> scfs = (ArrayList<CatSCF>) getAttList(Obj.SMCFAC, Att.CATSCF);
						for (CatSCF scf : scfs) {
							symbols.add(Facilities.Cats.get(scf));
						}
						Renderer.cluster(symbols);
					}
					break;
				default:
					break;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void stations() {
		if (Renderer.zoom >= 14) {
			String str = "";
			switch (feature.type) {
			case SISTAT:
				Renderer.symbol(Harbours.SignalStation);
				str = "SS";
				ArrayList<CatSIT> tcats = (ArrayList<CatSIT>) getAttList(Obj.SISTAT, Att.CATSIT);
				switch (tcats.get(0)) {
				case SIT_IPT:
					str += "(INT)";
					break;
				case SIT_PRTE:
					str += "(Traffic)";
					break;
				case SIT_PRTC:
					str += "(Port Control)";
					break;
				case SIT_LOCK:
					str += "(Lock)";
					break;
				case SIT_BRDG:
					str += "(Bridge)";
					break;
				default:
					break;
				}
				break;
			case SISTAW:
				Renderer.symbol(Harbours.SignalStation);
				str = "SS";
				str = "SS";
				ArrayList<CatSIW> wcats = (ArrayList<CatSIW>) getAttList(Obj.SISTAW, Att.CATSIW);
				switch (wcats.get(0)) {
				case SIW_STRM:
					str += "(Storm)";
					break;
				case SIW_WTHR:
					str += "(Weather)";
					break;
				case SIW_ICE:
					str += "(Ice)";
					break;
				case SIW_TIDG:
					str = "Tide gauge";
					break;
				case SIW_TIDS:
					str = "Tide scale";
					break;
				case SIW_TIDE:
					str += "(Tide)";
					break;
				case SIW_TSTR:
					str += "(Stream)";
					break;
				case SIW_DNGR:
					str += "(Danger)";
					break;
				case SIW_MILY:
					str += "(Firing)";
					break;
				case SIW_TIME:
					str += "(Time)";
					break;
				default:
					break;
				}
				break;
			case RDOSTA:
			case RTPBCN:
				Renderer.symbol(Harbours.SignalStation);
				Renderer.symbol(Beacons.RadarStation);
				break;
			case RADRFL:
				Renderer.symbol(Topmarks.RadarReflector);
				break;
			case RADSTA:
				Renderer.symbol(Harbours.SignalStation);
				Renderer.symbol(Beacons.RadarStation);
				Renderer.labelText("Ra", new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.TR, AffineTransform.getTranslateInstance(-30, -70)));
				break;
			case PILBOP:
				Renderer.symbol(Harbours.Pilot);
				addName(15, new Font("Arial", Font.BOLD, 40), Symbols.Msymb, new Delta(Handle.LC, AffineTransform.getTranslateInstance(70, -40)));
				CatPIL cat = (CatPIL) getAttEnum(feature.type, Att.CATPIL);
				if (cat == CatPIL.PIL_HELI) {
					Renderer.labelText("H", new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.LC, AffineTransform.getTranslateInstance(70, 0)));
				}
				break;
			case CGUSTA:
				Renderer.symbol(Harbours.SignalStation);
				str = "CG";
				if (feature.objs.containsKey(Obj.RSCSTA))
					Renderer.symbol(Harbours.Rescue, new Delta(Handle.CC, AffineTransform.getTranslateInstance(130, 0)));
				break;
			case RSCSTA:
				Renderer.symbol(Harbours.Rescue);
				break;
			default:
				break;
			}
			if ((Renderer.zoom >= 15) && !str.isEmpty()) {
				Renderer.labelText(str, new Font("Arial", Font.PLAIN, 40), Color.black, new Delta(Handle.CC, AffineTransform.getTranslateInstance(0, -50)));
			}
			Signals.addSignals();
		}
	}

	private static void transits() {
		if (Renderer.zoom >= 14) {
			if (feature.type == Obj.RECTRC)
				Renderer.lineVector(new LineStyle(Color.black, 5, null, null));
			else if (feature.type == Obj.NAVLNE)
				Renderer.lineVector(new LineStyle(Color.black, 5, new float[] { 25, 25 }, null));
		}
		if (Renderer.zoom >= 15) {
			String str = "";
			String name = getName();
			if (name != null)
				str += name + " ";
			Double ort;
			if ((ort = (Double) getAttVal(feature.type, Att.ORIENT)) != null) {
				str += df.format(ort) + "º";
				if (!str.isEmpty())
					Renderer.lineText(str, new Font("Arial", Font.PLAIN, 40), Color.black, -20);
			}
		}
	}

    @SuppressWarnings("unchecked")
    private static void virtual() {
        if (Renderer.zoom >= 12) {
            Renderer.symbol(Harbours.SignalStation, new Scheme(Symbols.Msymb));
            Renderer.symbol(Beacons.RadarStation, new Scheme(Symbols.Msymb));
           ArrayList<CatVAN> cats = (ArrayList<CatVAN>) getAttList(Obj.VAATON, Att.CATVAN);
            for (CatVAN van : cats) {
                switch (van) {
                case VAN_NCAR:
                    Renderer.symbol(Topmarks.TopNorth, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_SCAR:
                    Renderer.symbol(Topmarks.TopSouth, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_ECAR:
                    Renderer.symbol(Topmarks.TopEast, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_WCAR:
                    Renderer.symbol(Topmarks.TopWest, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_PLAT:
                case VAN_PCHS:
                    Renderer.symbol(Topmarks.TopCan, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_SLAT:
                case VAN_PCHP:
                    Renderer.symbol(Topmarks.TopCone, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_IDGR:
                    Renderer.symbol(Topmarks.TopIsol, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_SAFW:
                    Renderer.symbol(Topmarks.TopSphere, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_SPPM:
                    Renderer.symbol(Topmarks.TopX, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_WREK:
                    Renderer.symbol(Topmarks.TopCross, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                default:
                    break;
                }
            }
        }
        if (Renderer.zoom >= 15) {
                Renderer.labelText("V-AIS", new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 70)));
        }
    }
        
	private static void waterways() {
		Renderer.lineVector(new LineStyle(Symbols.Bwater, 20, (feature.geom.prim == Pflag.AREA) ? Symbols.Bwater : null));
	}

	private static void wrecks() {
		if (Renderer.zoom >= 14) {
			switch ((CatWRK) getAttEnum(feature.type, Att.CATWRK)) {
			case WRK_DNGR:
			case WRK_MSTS:
				Renderer.symbol(Areas.WreckD);
				break;
			case WRK_HULS:
				Renderer.symbol(Areas.WreckS);
				break;
			default:
				Renderer.symbol(Areas.WreckND);
			}
		}
	}
}
