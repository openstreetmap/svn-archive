// License: GPL. For details, see LICENSE file.
package render;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumMap;

import s57.S57att.Att;
import s57.S57map.AttMap;
import s57.S57map.ObjTab;
import s57.S57obj.Obj;
import s57.S57val.BoySHP;
import s57.S57val.CatFOG;
import s57.S57val.CatLIT;
import s57.S57val.CatROS;
import s57.S57val.CatRTB;
import s57.S57val.ColCOL;
import s57.S57val.LitCHR;
import symbols.Beacons;
import symbols.Symbols;
import symbols.Symbols.Delta;
import symbols.Symbols.Handle;
import symbols.Symbols.Scheme;
import symbols.Topmarks;

/**
 * @author Malcolm Herring
 */
public class Signals extends Rules {

	static final EnumMap<ColCOL, Color> LightColours = new EnumMap<>(ColCOL.class);
	static {
		LightColours.put(ColCOL.COL_WHT, new Color(0xffff00));
		LightColours.put(ColCOL.COL_RED, new Color(0xff0000));
		LightColours.put(ColCOL.COL_GRN, new Color(0x00ff00));
		LightColours.put(ColCOL.COL_BLU, new Color(0x0000ff));
		LightColours.put(ColCOL.COL_YEL, new Color(0xffff00));
		LightColours.put(ColCOL.COL_AMB, new Color(0xffc200));
		LightColours.put(ColCOL.COL_VIO, new Color(0xee82ee));
		LightColours.put(ColCOL.COL_ORG, Color.orange);
		LightColours.put(ColCOL.COL_MAG, Color.magenta);
	}

	static final EnumMap<ColCOL, String> LightLetters = new EnumMap<>(ColCOL.class);
	static {
		LightLetters.put(ColCOL.COL_WHT, "W");
		LightLetters.put(ColCOL.COL_RED, "R");
		LightLetters.put(ColCOL.COL_GRN, "G");
		LightLetters.put(ColCOL.COL_BLU, "Bu");
		LightLetters.put(ColCOL.COL_YEL, "Y");
		LightLetters.put(ColCOL.COL_AMB, "Am");
		LightLetters.put(ColCOL.COL_VIO, "Vi");
		LightLetters.put(ColCOL.COL_ORG, "Or");
	}

	static final EnumMap<LitCHR, String> LightCharacters = new EnumMap<>(LitCHR.class);
	static {
		LightCharacters.put(LitCHR.CHR_F, "F");
		LightCharacters.put(LitCHR.CHR_FL, "Fl");
		LightCharacters.put(LitCHR.CHR_LFL, "LFl");
		LightCharacters.put(LitCHR.CHR_Q, "Q");
		LightCharacters.put(LitCHR.CHR_VQ, "VQ");
		LightCharacters.put(LitCHR.CHR_UQ, "UQ");
		LightCharacters.put(LitCHR.CHR_ISO, "Iso");
		LightCharacters.put(LitCHR.CHR_OC, "Oc");
		LightCharacters.put(LitCHR.CHR_IQ, "IQ");
		LightCharacters.put(LitCHR.CHR_IVQ, "IVQ");
		LightCharacters.put(LitCHR.CHR_IUQ, "IUQ");
		LightCharacters.put(LitCHR.CHR_MO, "Mo");
		LightCharacters.put(LitCHR.CHR_FFL, "FFl");
		LightCharacters.put(LitCHR.CHR_FLLFL, "FlLFl");
		LightCharacters.put(LitCHR.CHR_OCFL, "OcFl");
		LightCharacters.put(LitCHR.CHR_FLFL, "FLFl");
		LightCharacters.put(LitCHR.CHR_ALOC, "Al.Oc");
		LightCharacters.put(LitCHR.CHR_ALLFL, "Al.LFl");
		LightCharacters.put(LitCHR.CHR_ALFL, "Al.Fl");
		LightCharacters.put(LitCHR.CHR_ALGR, "Al.Gr");
		LightCharacters.put(LitCHR.CHR_QLFL, "Q+LFl");
		LightCharacters.put(LitCHR.CHR_VQLFL, "VQ+LFl");
		LightCharacters.put(LitCHR.CHR_UQLFL, "UQ+LFl");
		LightCharacters.put(LitCHR.CHR_AL, "Al");
		LightCharacters.put(LitCHR.CHR_ALFFL, "Al.FFl");
	}

	static final EnumMap<CatFOG, String> fogSignals = new EnumMap<>(CatFOG.class);
	static {
		fogSignals.put(CatFOG.FOG_EXPL, "Explos");
		fogSignals.put(CatFOG.FOG_DIA, "Dia");
		fogSignals.put(CatFOG.FOG_SIRN, "Siren");
		fogSignals.put(CatFOG.FOG_NAUT, "Horn");
		fogSignals.put(CatFOG.FOG_REED, "Horn");
		fogSignals.put(CatFOG.FOG_TYPH, "Horn");
		fogSignals.put(CatFOG.FOG_BELL, "Bell");
		fogSignals.put(CatFOG.FOG_WHIS, "Whis");
		fogSignals.put(CatFOG.FOG_GONG, "Gong");
		fogSignals.put(CatFOG.FOG_HORN, "Horn");
	}

	static final DecimalFormat df = new DecimalFormat("#.#");

	public static void addSignals() {
		if (feature.objs.containsKey(Obj.RADRFL))
			reflectors();
		if (feature.objs.containsKey(Obj.FOGSIG))
			fogSignals();
		if (feature.objs.containsKey(Obj.RTPBCN))
			radarStations();
		if (feature.objs.containsKey(Obj.RADSTA))
			radarStations();
		if (feature.objs.containsKey(Obj.RDOSTA))
			radioStations();
		if (feature.objs.containsKey(Obj.LIGHTS))
			lights();
	}

	public static void reflectors() {
		if (Renderer.zoom >= 14) {
			switch (feature.type) {
			case BCNLAT:
			case BCNCAR:
			case BCNISD:
			case BCNSAW:
			case BCNSPP:
				if (feature.objs.containsKey(Obj.TOPMAR) || feature.objs.containsKey(Obj.DAYMAR)) {
					Renderer.symbol(Topmarks.RadarReflector, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -140)));
				} else {
					Renderer.symbol(Topmarks.RadarReflector, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -80)));
				}
				break;
			case LITFLT:
			case LITVES:
			case BOYINB:
				if (feature.objs.containsKey(Obj.TOPMAR) || feature.objs.containsKey(Obj.DAYMAR)) {
					Renderer.symbol(Topmarks.RadarReflector, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -110)));
				} else {
					Renderer.symbol(Topmarks.RadarReflector, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -60)));
				}
				break;
			case LITMAJ:
			case LITMIN:
				if (feature.objs.containsKey(Obj.TOPMAR) || feature.objs.containsKey(Obj.DAYMAR)) {
					Renderer.symbol(Topmarks.RadarReflector, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -90)));
				} else {
					Renderer.symbol(Topmarks.RadarReflector, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -30)));
				}
				break;
			case BOYLAT:
			case BOYCAR:
			case BOYISD:
			case BOYSAW:
			case BOYSPP:
				if (feature.objs.containsKey(Obj.TOPMAR) || feature.objs.containsKey(Obj.DAYMAR)) {
					if (testAttribute(feature.type, Att.BOYSHP, BoySHP.BOY_PILR) || testAttribute(feature.type, Att.BOYSHP, BoySHP.BOY_SPAR)) {
						Renderer.symbol(Topmarks.RadarReflector, new Delta(Handle.BC, AffineTransform.getTranslateInstance(50, -160)));
					} else {
						Renderer.symbol(Topmarks.RadarReflector, new Delta(Handle.BC, AffineTransform.getTranslateInstance(25, -80)));
					}
				} else {
					if (testAttribute(feature.type, Att.BOYSHP, BoySHP.BOY_PILR) || testAttribute(feature.type, Att.BOYSHP, BoySHP.BOY_SPAR)) {
						Renderer.symbol(Topmarks.RadarReflector, new Delta(Handle.BC, AffineTransform.getTranslateInstance(30, -100)));
					} else {
						Renderer.symbol(Topmarks.RadarReflector, new Delta(Handle.BC, AffineTransform.getTranslateInstance(10, -50)));
					}
				}
				break;
			default:
				break;
			}
		}
	}

	public static void fogSignals() {
		if (Renderer.zoom >= 11)
			Renderer.symbol(Beacons.FogSignal);
		if (Renderer.zoom >= 15) {
			AttMap atts = feature.objs.get(Obj.FOGSIG).get(0);
			if (atts != null) {
				String str = "";
				if (atts.containsKey(Att.CATFOG)) {
					str += fogSignals.get(((ArrayList<?>) atts.get(Att.CATFOG).val).get(0));
				}
				if (atts.containsKey(Att.SIGGRP)) {
					str += "(" + atts.get(Att.SIGGRP).val + ")";
				} else {
					str += " ";
				}
				if (atts.containsKey(Att.SIGPER)) {
					str += df.format(atts.get(Att.SIGPER).val) + "s";
				}
				if (atts.containsKey(Att.VALMXR)) {
					str += df.format(atts.get(Att.VALMXR).val) + "M";
				}
				if (!str.isEmpty()) {
					Renderer.labelText(str, new Font("Arial", Font.PLAIN, 40), Color.black, new Delta(Handle.TR, AffineTransform.getTranslateInstance(-60, -30)));
				}
			}
		}
	}

	public static void radarStations() {
		if (Renderer.zoom >= 11)
			Renderer.symbol(Beacons.RadarStation);
		if (Renderer.zoom >= 15) {
			String bstr = "";
			CatRTB cat = (CatRTB) getAttEnum(Obj.RTPBCN, Att.CATRTB);
			String wal = getAttStr(Obj.RTPBCN, Att.RADWAL);
            if ((cat == CatRTB.RTB_RAMK) || (cat == CatRTB.RTB_RACN)) {
                switch (cat) {
                case RTB_RAMK:
                    bstr += " Ramark";
                    break;
                case RTB_RACN:
                    bstr += " Racon";
                    break;
                default:
                    break;
                }
                String astr = getAttStr(Obj.RTPBCN, Att.SIGGRP);
                if (!astr.isEmpty()) {
                    bstr += "(" + astr + ")";
                }
                Double per = (Double) getAttVal(Obj.RTPBCN, Att.SIGPER);
                Double mxr = (Double) getAttVal(Obj.RTPBCN, Att.VALMXR);
                if ((per != null) || (mxr != null)) {
                    bstr += (astr.isEmpty() ? " " : "");
                    if (per != null)
                        bstr += (per != 0) ? per.toString() + "s" : "";
                    if (mxr != null)
                        bstr += (mxr != 0) ? mxr.toString() + "M" : "";
                }
            }
			if (!wal.isEmpty()) {
				switch (wal) {
				case "0.03-X":
					bstr += "(3cm)";
					break;
				case "0.10-S":
					bstr += "(10cm)";
					break;
				}
			}
			if (!bstr.isEmpty()) {
				Renderer.labelText(bstr, new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.TR, AffineTransform.getTranslateInstance(-30, -70)));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void radioStations() {
		String bstr = "";
		if (Renderer.zoom >= 11) {
			ArrayList<CatROS> cats = (ArrayList<CatROS>) getAttList(Obj.RDOSTA, Att.CATROS);
			for (CatROS ros : cats) {
				switch (ros) {
				case ROS_OMNI:
					bstr += " RC";
					break;
				case ROS_DIRL:
					bstr += " RD";
					break;
				case ROS_ROTP:
					bstr += " RW";
					break;
				case ROS_CNSL:
					bstr += " Consol";
					break;
				case ROS_RDF:
					bstr += " RG";
					break;
				case ROS_QTA:
					bstr += " R";
					break;
				case ROS_AERO:
					bstr += " AeroRC";
					break;
				case ROS_DECA:
					bstr += " Decca";
					break;
				case ROS_LORN:
					bstr += " Loran";
					break;
				case ROS_DGPS:
					bstr += " DGPS";
					break;
				case ROS_TORN:
					bstr += " Toran";
					break;
				case ROS_OMGA:
					bstr += " Omega";
					break;
				case ROS_SYLD:
					bstr += " Syledis";
					break;
				case ROS_CHKA:
					bstr += " Chiaka";
					break;
				case ROS_PCOM:
				case ROS_COMB:
				case ROS_FACS:
				case ROS_TIME:
					break;
                case ROS_AISB:
                case ROS_PAIS:
					bstr += " AIS";
					break;
				default:
					break;
				}
			}
			Renderer.symbol(Beacons.RadarStation);
		}
		if (Renderer.zoom >= 15) {
			if (!bstr.isEmpty()) {
				Renderer.labelText(bstr, new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.TR, AffineTransform.getTranslateInstance(-30, -110)));
			}
		}
	}

	static class Sect {
		int dir;
		LitCHR chr;
		ColCOL col;
		ColCOL alt;
		String grp;
		double per;
		double rng;
	}

    @SuppressWarnings("unchecked")
    public static void lights() {
        Enum<ColCOL> col = null;
        Enum<ColCOL> tcol = null;
        ObjTab lights = feature.objs.get(Obj.LIGHTS);
        for (AttMap atts : lights.values()) {
            if (atts.containsKey(Att.COLOUR)) {
                ArrayList<Enum<ColCOL>> cols = (ArrayList<Enum<ColCOL>>) atts.get(Att.COLOUR).val;
                if (cols.size() == 1) {
                    if (atts.containsKey(Att.CATLIT) && ((ArrayList<?>) atts.get(Att.CATLIT).val).contains(CatLIT.LIT_FLDL)) {
                        Renderer.symbol(Beacons.Floodlight, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(90))));
                    } else {
                        tcol = cols.get(0);
                        if (col == null) {
                            col = tcol;
                        } else if (tcol != col) {
                            col = ColCOL.COL_MAG;
                            break;
                        }
                    }
                } else {
                    col = ColCOL.COL_MAG;
                    break;
                }
            }
        }
        if (col != null) {
            Renderer.symbol(Beacons.LightFlare, new Scheme(LightColours.get(col)), new Delta(Handle.BC, AffineTransform.getRotateInstance(Math.toRadians(120))));
        }
        String str = "";
        if ((lights.get(1) != null) && (Renderer.zoom >= 12)) {
            for (AttMap atts : lights.values()) {
                Enum<ColCOL> col1 = null;
                Enum<ColCOL> col2 = null;
                double radius = 0.5;
                if (atts.containsKey(Att.VALNMR)) {
                    radius += Math.log10((Double) atts.get(Att.VALNMR).val) * 2.0;
                }
                radius /= Math.pow(Renderer.zoom, 4) / 5000;
                double s1 = 361;
                double s2 = 361;
                Double dir = null;
                if (atts.containsKey(Att.COLOUR)) {
                    ArrayList<Enum<ColCOL>> cols = (ArrayList<Enum<ColCOL>>) atts.get(Att.COLOUR).val;
                    col1 = cols.get(0);
                    if (cols.size() > 1)
                        col2 = cols.get(1);
                } else {
                    continue;
                }
                if (atts.containsKey(Att.CATLIT)) {
                    ArrayList<CatLIT> cats = (ArrayList<CatLIT>) atts.get(Att.CATLIT).val;
                    if (cats.contains(CatLIT.LIT_DIR)) {
                        if (atts.containsKey(Att.ORIENT)) {
                            dir = (Double) atts.get(Att.ORIENT).val;
                            s1 = ((dir - 4) + 360) % 360;
                            s2 = (dir + 4) % 360;
                            for (AttMap satts : lights.values()) {
                                double ss1 = 361;
                                double ss2 = 361;
                                Double sdir = null;
                                if (satts == atts)
                                    continue;
                                ArrayList<CatLIT> scats = (ArrayList<CatLIT>) (satts.containsKey(Att.CATLIT) ? (ArrayList<CatLIT>) satts.get(Att.CATLIT).val : new ArrayList<>());
                                if (scats.contains(CatLIT.LIT_DIR)) {
                                    if (satts.containsKey(Att.ORIENT)) {
                                        sdir = (Double) satts.get(Att.ORIENT).val;
                                        ss1 = sdir;
                                        ss2 = sdir;
                                    }
                                } else {
                                    if (satts.containsKey(Att.SECTR1)) {
                                        ss1 = (Double) satts.get(Att.SECTR1).val;
                                    }
                                    if (satts.containsKey(Att.SECTR2)) {
                                        ss2 = (Double) satts.get(Att.SECTR2).val;
                                    }
                                }
                                if ((ss1 > 360) || (ss2 > 360))
                                    continue;
                                if (sdir != null) {
                                    if (((dir - sdir + 360) % 360) < 8) {
                                        s1 = ((((sdir > dir) ? 360 : 0) + sdir + dir) / 2) % 360;
                                    }
                                    if (((sdir - dir + 360) % 360) < 8) {
                                        s2 = ((((dir > sdir) ? 360 : 0) + sdir + dir) / 2) % 360;
                                    }
                                } else {
                                    if (((dir - ss2 + 360) % 360) < 4) {
                                        s1 = ss2;
                                    }
                                    if (((ss1 - dir + 360) % 360) < 4) {
                                        s2 = ss1;
                                    }
                                }
                            }
                        }
                    }
                }
                if ((s1 > 360) && atts.containsKey(Att.SECTR1)) {
                    s1 = (Double) atts.get(Att.SECTR1).val;
                } else if (dir == null) {
                    continue;
                }
                if ((s2 > 360) && atts.containsKey(Att.SECTR2)) {
                    s2 = (Double) atts.get(Att.SECTR2).val;
                } else if (dir == null) {
                    continue;
                }
                str = "";
                if (atts.containsKey(Att.LITCHR)) {
                    str += LightCharacters.get(((ArrayList<LitCHR>) atts.get(Att.LITCHR).val).get(0));
                }
                if (atts.containsKey(Att.SIGGRP)) {
                    str += "(" + atts.get(Att.SIGGRP).val + ")";
                } else if (!str.isEmpty()) {
                    str += ".";
                }
                if (atts.containsKey(Att.COLOUR)) {
                    ArrayList<Enum<ColCOL>> cols = (ArrayList<Enum<ColCOL>>) atts.get(Att.COLOUR).val;
                    str += LightLetters.get(cols.get(0));
                    if (cols.size() > 1)
                        str += LightLetters.get(cols.get(1));
                }
                if (atts.containsKey(Att.SIGPER)) {
                    str += "." + df.format(atts.get(Att.SIGPER).val) + "s";
                }
                if ((s1 <= 360) && (s2 <= 360) && (s1 != s2))
                    Renderer.lightSector(LightColours.get(col1), LightColours.get(col2), radius, s1, s2, dir, (Renderer.zoom >= 15) ? str : "");
            }
            if (Renderer.zoom >= 15) {
                class LitSect {
                    boolean dir;
                    LitCHR chr;
                    ColCOL col;
                    String grp;
                    double per;
                    double rng;
                    double hgt;
                }

                ArrayList<LitSect> litatts = new ArrayList<>();
                for (AttMap atts : lights.values()) {
                    LitSect sect = new LitSect();
                    sect.dir = (atts.containsKey(Att.CATLIT) && ((ArrayList<CatLIT>) atts.get(Att.CATLIT).val).contains(CatLIT.LIT_DIR));
                    sect.chr = atts.containsKey(Att.LITCHR) ? ((ArrayList<LitCHR>) atts.get(Att.LITCHR).val).get(0) : LitCHR.CHR_UNKN;
                    switch (sect.chr) {
                    case CHR_AL:
                        sect.chr = LitCHR.CHR_F;
                        break;
                    case CHR_ALOC:
                        sect.chr = LitCHR.CHR_OC;
                        break;
                    case CHR_ALLFL:
                        sect.chr = LitCHR.CHR_LFL;
                        break;
                    case CHR_ALFL:
                        sect.chr = LitCHR.CHR_FL;
                        break;
                    case CHR_ALFFL:
                        sect.chr = LitCHR.CHR_FFL;
                        break;
                    default:
                        break;
                    }
                    sect.grp = atts.containsKey(Att.SIGGRP) ? (String) atts.get(Att.SIGGRP).val : "";
                    sect.per = atts.containsKey(Att.SIGPER) ? (Double) atts.get(Att.SIGPER).val : 0.0;
                    sect.rng = atts.containsKey(Att.VALNMR) ? (Double) atts.get(Att.VALNMR).val : 0.0;
                    sect.hgt = atts.containsKey(Att.HEIGHT) ? (Double) atts.get(Att.HEIGHT).val : 0.0;
                    ArrayList<ColCOL> cols = (ArrayList<ColCOL>) (atts.containsKey(Att.COLOUR) ? atts.get(Att.COLOUR).val : new ArrayList<>());
                    sect.col = cols.size() > 0 ? cols.get(0) : ColCOL.COL_UNK;
                    if ((sect.chr != LitCHR.CHR_UNKN) && (sect.col != null))
                        litatts.add(sect);
                }
                ArrayList<ArrayList<LitSect>> groupings = new ArrayList<>();
                for (LitSect lit : litatts) {
                    boolean found = false;
                    for (ArrayList<LitSect> group : groupings) {
                        LitSect mem = group.get(0);
                        if (lit.dir == mem.dir && lit.chr == mem.chr && lit.grp.equals(mem.grp) && lit.per == mem.per && lit.hgt == mem.hgt) {
                            group.add(lit);
                            found = true;
                        }
                    }
                    if (!found) {
                        ArrayList<LitSect> tmp = new ArrayList<>();
                        tmp.add(lit);
                        groupings.add(tmp);
                    }
                }
                for (boolean moved = true; moved;) {
                    moved = false;
                    for (int i = 0; i < groupings.size() - 1; i++) {
                        if (groupings.get(i).size() < groupings.get(i + 1).size()) {
                            ArrayList<LitSect> tmp = groupings.remove(i);
                            groupings.add(i + 1, tmp);
                            moved = true;
                        }
                    }
                }
                class ColRng {
                    ColCOL col;
                    double rng;

                    ColRng(ColCOL c, double r) {
                        col = c;
                        rng = r;
                    }
                }

                int y = -30;
                for (ArrayList<LitSect> group : groupings) {
                    ArrayList<ColRng> colrng = new ArrayList<>();
                    for (LitSect lit : group) {
                        boolean found = false;
                        for (ColRng cr : colrng) {
                            if (cr.col == lit.col) {
                                if (lit.rng > cr.rng) {
                                    cr.rng = lit.rng;
                                }
                                found = true;
                            }
                        }
                        if (!found) {
                            colrng.add(new ColRng(lit.col, lit.rng));
                        }
                    }
                    for (boolean moved = true; moved;) {
                        moved = false;
                        for (int i = 0; i < colrng.size() - 1; i++) {
                            if (colrng.get(i).rng < colrng.get(i + 1).rng) {
                                ColRng tmp = colrng.remove(i);
                                colrng.add(i + 1, tmp);
                                moved = true;
                            }
                        }
                    }
                    LitSect tmp = group.get(0);
                    str = tmp.dir ? "Dir" : "";
                    str += LightCharacters.get(tmp.chr);
                    if (!tmp.grp.isEmpty())
                        str += "(" + tmp.grp + ")";
                    else
                        str += ".";
                    for (ColRng cr : colrng) {
                        str += LightLetters.get(cr.col);
                    }
                    if ((tmp.per > 0) || (tmp.hgt > 0) || (colrng.get(0).rng > 0))
                        str += ".";
                    if (tmp.per > 0)
                        str += df.format(tmp.per) + "s";
                    if (tmp.hgt > 0)
                        str += df.format(tmp.hgt) + "m";
                    if (colrng.get(0).rng > 0)
                        str += df.format(colrng.get(0).rng) + ((colrng.size() > 1) ? ((colrng.size() > 2) ? ("-" + df.format(colrng.get(colrng.size() - 1).rng)) : ("/" + df.format(colrng.get(1).rng))) : "") + "M";
                    Renderer.labelText(str, new Font("Arial", Font.PLAIN, 40), Color.black, new Delta(Handle.TL, AffineTransform.getTranslateInstance(60, y)));
                    y += 40;
                    str = "";
                }
            }
        } else {
            if (Renderer.zoom >= 15) {
                AttMap atts = lights.get(0);
                ArrayList<CatLIT> cats = new ArrayList<>();
                if (atts.containsKey(Att.CATLIT)) {
                    cats = (ArrayList<CatLIT>) atts.get(Att.CATLIT).val;
                }
                str = cats.contains(CatLIT.LIT_DIR) ? "Dir" : "";
                str += atts.containsKey(Att.MLTYLT) ? atts.get(Att.MLTYLT).val : "";
                if (atts.containsKey(Att.LITCHR)) {
                    LitCHR chr = ((ArrayList<LitCHR>) atts.get(Att.LITCHR).val).get(0);
                    if (atts.containsKey(Att.SIGGRP)) {
                        String grp = (String) atts.get(Att.SIGGRP).val;
                        switch (chr) {
                        case CHR_QLFL:
                            str += String.format("Q(%s)+LFl", grp);
                            break;
                        case CHR_VQLFL:
                            str += String.format("VQ(%s)+LFl", grp);
                            break;
                        case CHR_UQLFL:
                            str += String.format("UQ(%s)+LFl", grp);
                            break;
                        default:
                            str += String.format("%s(%s)", LightCharacters.get(chr), grp);
                            break;
                        }
                    } else {
                        str += LightCharacters.get(chr);
                    }
                }
                if (atts.containsKey(Att.COLOUR)) {
                    ArrayList<ColCOL> cols = (ArrayList<ColCOL>) atts.get(Att.COLOUR).val;
                    if (!((cols.size() == 1) && (cols.get(0) == ColCOL.COL_WHT))) {
                        if (!str.isEmpty() && !str.endsWith(")")) {
                            str += ".";
                        }
                        for (ColCOL acol : cols) {
                            str += LightLetters.get(acol);
                        }
                    }
                }
                str += cats.contains(CatLIT.LIT_VERT) ? "(vert)" : "";
                str += cats.contains(CatLIT.LIT_HORI) ? "(hor)" : "";
                str += (!str.isEmpty() && (atts.containsKey(Att.SIGPER) || atts.containsKey(Att.HEIGHT) || atts.containsKey(Att.VALMXR)) && !str.endsWith(")")) ? "." : "";
                str += atts.containsKey(Att.SIGPER) ? df.format(atts.get(Att.SIGPER).val) + "s" : "";
                str += atts.containsKey(Att.HEIGHT) ? df.format(atts.get(Att.HEIGHT).val) + "m" : "";
                str += atts.containsKey(Att.VALNMR) ? df.format(atts.get(Att.VALNMR).val) + "M" : "";
                str += cats.contains(CatLIT.LIT_FRNT) ? "(Front)" : "";
                str += cats.contains(CatLIT.LIT_REAR) ? "(Rear)" : "";
                str += cats.contains(CatLIT.LIT_UPPR) ? "(Upper)" : "";
                str += cats.contains(CatLIT.LIT_LOWR) ? "(Lower)" : "";
                Renderer.labelText(str, new Font("Arial", Font.PLAIN, 40), Color.black, new Delta(Handle.TL, AffineTransform.getTranslateInstance(60, -30)));
            }
        }
    }
}
