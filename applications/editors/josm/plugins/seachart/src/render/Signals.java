/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package render;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.EnumMap;

import s57.S57att.*;
import s57.S57obj.*;
import s57.S57val.*;
import s57.S57map.*;
import symbols.Beacons;
import symbols.Symbols;
import symbols.Topmarks;
import symbols.Symbols.*;

public class Signals {

	static final EnumMap<ColCOL, Color> lightColours = new EnumMap<ColCOL, Color>(ColCOL.class);
	static {
		lightColours.put(ColCOL.COL_WHT, new Color(0xffff00));
		lightColours.put(ColCOL.COL_RED, new Color(0xff0000));
		lightColours.put(ColCOL.COL_GRN, new Color(0x00ff00));
		lightColours.put(ColCOL.COL_BLU, new Color(0x0000ff));
		lightColours.put(ColCOL.COL_YEL, new Color(0xffff00));
		lightColours.put(ColCOL.COL_AMB, new Color(0xffc200));
		lightColours.put(ColCOL.COL_VIO, new Color(0xee82ee));
		lightColours.put(ColCOL.COL_ORG, Color.orange);
		lightColours.put(ColCOL.COL_MAG, Color.magenta);
	}

	static final EnumMap<ColCOL, String> lightLetters = new EnumMap<ColCOL, String>(ColCOL.class);
	static {
		lightLetters.put(ColCOL.COL_WHT, "W");
		lightLetters.put(ColCOL.COL_RED, "R");
		lightLetters.put(ColCOL.COL_GRN, "G");
		lightLetters.put(ColCOL.COL_BLU, "Bu");
		lightLetters.put(ColCOL.COL_YEL, "Y");
		lightLetters.put(ColCOL.COL_AMB, "Am");
		lightLetters.put(ColCOL.COL_VIO, "Vi");
		lightLetters.put(ColCOL.COL_ORG, "Or");
	}

	static final EnumMap<LitCHR, String> lightCharacters = new EnumMap<LitCHR, String>(LitCHR.class);
	static {
		lightCharacters.put(LitCHR.CHR_F, "F");
		lightCharacters.put(LitCHR.CHR_FL, "Fl");
		lightCharacters.put(LitCHR.CHR_LFL, "LFl");
		lightCharacters.put(LitCHR.CHR_Q, "Q");
		lightCharacters.put(LitCHR.CHR_VQ, "VQ");
		lightCharacters.put(LitCHR.CHR_UQ, "UQ");
		lightCharacters.put(LitCHR.CHR_ISO, "Iso");
		lightCharacters.put(LitCHR.CHR_OC, "Oc");
		lightCharacters.put(LitCHR.CHR_IQ, "IQ");
		lightCharacters.put(LitCHR.CHR_IVQ, "IVQ");
		lightCharacters.put(LitCHR.CHR_IUQ, "IUQ");
		lightCharacters.put(LitCHR.CHR_MO, "Mo");
		lightCharacters.put(LitCHR.CHR_FFL, "FFl");
		lightCharacters.put(LitCHR.CHR_FLLFL, "FlLFl");
		lightCharacters.put(LitCHR.CHR_OCFL, "OcFl");
		lightCharacters.put(LitCHR.CHR_FLFL, "FLFl");
		lightCharacters.put(LitCHR.CHR_ALOC, "Al.Oc");
		lightCharacters.put(LitCHR.CHR_ALLFL, "Al.LFl");
		lightCharacters.put(LitCHR.CHR_ALFL, "Al.Fl");
		lightCharacters.put(LitCHR.CHR_ALGR, "Al.Gr");
		lightCharacters.put(LitCHR.CHR_QLFL, "Q+LFl");
		lightCharacters.put(LitCHR.CHR_VQLFL, "VQ+LFl");
		lightCharacters.put(LitCHR.CHR_UQLFL, "UQ+LFl");
		lightCharacters.put(LitCHR.CHR_AL, "Al");
		lightCharacters.put(LitCHR.CHR_ALFFL, "Al.FFl");
	}
	
	public static void addSignals(Feature feature) {
	  if (feature.objs.containsKey(Obj.FOGSIG)) fogSignals(feature);
	  if (feature.objs.containsKey(Obj.RTPBCN)) radarStations(feature);
	  if (feature.objs.containsKey(Obj.RADSTA)) radarStations(feature);
	  if (feature.objs.containsKey(Obj.RDOSTA)) radioStations(feature);
	  if (feature.objs.containsKey(Obj.LIGHTS)) lights(feature);
	}
	
	static final EnumMap<CatFOG, String> fogSignals = new EnumMap<CatFOG, String>(CatFOG.class);
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

	public static void fogSignals(Feature feature) {
		Renderer.symbol(feature, Beacons.FogSignal);
		AttMap atts = feature.objs.get(Obj.FOGSIG).get(0);
		String str = "";
		if (atts.containsKey(Att.CATFOG)) {
			str += fogSignals.get(((ArrayList<?>)(atts.get(Att.CATFOG).val)).get(0));
		}
		if (atts.containsKey(Att.SIGGRP)) {
			str += "(" + atts.get(Att.SIGGRP).val + ")";
		} else {
			str += " ";
		}
		if (atts.containsKey(Att.SIGPER)) {
			str += atts.get(Att.SIGPER).val + "s";
		}
		if (atts.containsKey(Att.VALMXR)) {
			str += atts.get(Att.VALMXR).val + "M";
		}
		if ((Renderer.zoom >= 15) && !str.isEmpty()) {
			Renderer.labelText(feature, str, new Font("Arial", Font.PLAIN, 40),Color.black, new Delta(Handle.TR, AffineTransform.getTranslateInstance(-60, -30)));
		}
	}

	public static void radarStations(Feature feature) {
		Renderer.symbol(feature, Beacons.RadarStation);
		String bstr = "";
		CatRTB cat = (CatRTB) Rules.getAttEnum(feature, Obj.RTPBCN, 0, Att.CATRTB);
		String wal = Rules.getAttStr(feature, Obj.RTPBCN, 0, Att.RADWAL);
		switch (cat) {
		case RTB_RAMK:
			bstr += " Ramark";
			break;
		case RTB_RACN:
			bstr += " Racon";
			String astr = Rules.getAttStr(feature, Obj.RTPBCN, 0, Att.SIGGRP);
			if (!astr.isEmpty()) {
				bstr += "(" + astr + ")";
			}
			Double per = (Double) Rules.getAttVal(feature, Obj.RTPBCN, 0, Att.SIGPER);
			Double mxr = (Double) Rules.getAttVal(feature, Obj.RTPBCN, 0, Att.VALMXR);
			if ((per != null) || (mxr != null)) {
				bstr += (astr.isEmpty() ? " " : "");
				if (per != null) bstr += (per != 0) ? per.toString() + "s" : "";
				if (mxr != null) bstr += (mxr != 0) ? mxr.toString() + "M" : "";
			}
			break;
		default:
			break;
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
		if ((Renderer.zoom >= 15) && !bstr.isEmpty()) {
			Renderer.labelText(feature, bstr, new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.TR, AffineTransform.getTranslateInstance(-30, -70)));
		}
	}

	@SuppressWarnings("unchecked")
	public static void radioStations(Feature feature) {
		Renderer.symbol(feature, Beacons.RadarStation);
		ArrayList<CatROS> cats = (ArrayList<CatROS>)Rules.getAttList(feature, Obj.RDOSTA, 0, Att.CATROS);
		boolean vais = false;
		String bstr = "";
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
			case ROS_PAIS:
			case ROS_SAIS:
				bstr += " AIS";
				break;
			case ROS_VAIS:
				vais = true;
				break;
			case ROS_VANC:
				vais = true;
				Renderer.symbol(feature, Topmarks.TopNorth, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
				break;
			case ROS_VASC:
				vais = true;
				Renderer.symbol(feature, Topmarks.TopSouth, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
				break;
			case ROS_VAEC:
				vais = true;
				Renderer.symbol(feature, Topmarks.TopEast, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
				break;
			case ROS_VAWC:
				vais = true;
				Renderer.symbol(feature, Topmarks.TopWest, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
				break;
			case ROS_VAPL:
				vais = true;
				Renderer.symbol(feature, Topmarks.TopCan, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
				break;
			case ROS_VASL:
				vais = true;
				Renderer.symbol(feature, Topmarks.TopCone, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
				break;
			case ROS_VAID:
				vais = true;
				Renderer.symbol(feature, Topmarks.TopIsol, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
				break;
			case ROS_VASW:
				vais = true;
				Renderer.symbol(feature, Topmarks.TopSphere, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
				break;
			case ROS_VASP:
				vais = true;
				Renderer.symbol(feature, Topmarks.TopX, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
				break;
			case ROS_VAWK:
				vais = true;
				Renderer.symbol(feature, Topmarks.TopCross, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
				break;
			default:
				break;
			}
		}
		if (Renderer.zoom >= 15) {
			if (vais) {
				Renderer.labelText(feature, "V-AIS", new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 70)));
			}
			if (!bstr.isEmpty()) {
				Renderer.labelText(feature, bstr, new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.TR, AffineTransform.getTranslateInstance(-30, -110)));
			}
		}
	}

	class Sect {
    int dir;
    LitCHR chr;
    ColCOL col;
    ColCOL alt;
    String grp;
    double per;
    double rng;
	}
	
	@SuppressWarnings("unchecked")
	public static void lights(Feature feature) {
		Enum<ColCOL> col = null;
		Enum<ColCOL> tcol = null;
		ObjTab lights = feature.objs.get(Obj.LIGHTS);
		for (AttMap atts : lights.values()) {
			if (atts.containsKey(Att.COLOUR)) {
				ArrayList<Enum<ColCOL>> cols = (ArrayList<Enum<ColCOL>>) atts.get(Att.COLOUR).val;
				if (cols.size() == 1) {
					tcol = cols.get(0);
					if (col == null) {
						col = tcol;
					} else if (tcol != col) {
						col = ColCOL.COL_MAG;
						break;
					}
				} else {
					col = ColCOL.COL_MAG;
					break;
				}
			}
		}
		Renderer.symbol(feature, Beacons.LightFlare, new Scheme(lightColours.get(col)), new Delta(Handle.BC, AffineTransform.getRotateInstance(Math.toRadians(120))));
		if (lights.get(1) != null) {
			for (AttMap atts : lights.values()) {
				Enum<ColCOL> col1 = null;
				Enum<ColCOL> col2 = null;
				double radius = 0.2;
				double s1 = 0;
				double s2 = 0;
				boolean dir = false;
				if (atts.containsKey(Att.COLOUR)) {
					ArrayList<Enum<ColCOL>> cols = (ArrayList<Enum<ColCOL>>) atts.get(Att.COLOUR).val;
					col1 = cols.get(0);
					if (cols.size() > 1) col2 = cols.get(1);
				} else {
					continue;
				}
				if (atts.containsKey(Att.RADIUS)) {
					radius = (Double) atts.get(Att.RADIUS).val;
				}
				if (atts.containsKey(Att.SECTR1)) {
					s1 = (Double) atts.get(Att.SECTR1).val;
				} else {
					continue;
				}
				if (atts.containsKey(Att.SECTR2)) {
					s2 = (Double) atts.get(Att.SECTR2).val;
				} else {
					continue;
				}
				if (atts.containsKey(Att.CATLIT)) {
					ArrayList<CatLIT> cats = (ArrayList<CatLIT>) atts.get(Att.CATLIT).val;
					if (cats.contains(CatLIT.LIT_DIR)) {
						dir = true;
					}
				}
				String str = "";
				if (atts.containsKey(Att.LITCHR)) {
					str += lightCharacters.get(atts.get(Att.LITCHR).val);
				}
				if (atts.containsKey(Att.SIGGRP)) {
					str += "(" + atts.get(Att.SIGGRP).val + ")";
				} else if (!str.isEmpty()) {
					str += ".";
				}
				if (atts.containsKey(Att.COLOUR)) {
					ArrayList<Enum<ColCOL>> cols = (ArrayList<Enum<ColCOL>>) atts.get(Att.COLOUR).val;
					str += lightLetters.get(cols.get(0));
					if (cols.size() > 1)
						str += lightLetters.get(cols.get(1));
				}
				if (dir && atts.containsKey(Att.ORIENT)) {
					double orient = (Double)atts.get(Att.ORIENT).val;
					str += " " + orient + "°";
					s1 = (orient - 4 + 360) % 360;
					s2 = (orient + 4) % 360;
					double n1 = 360;
					double n2 = 360;
					for (AttMap sect : lights.values()) {
						if (sect != atts) {
							
						}
					}
				}
				Renderer.lightSector(feature, lightColours.get(col1), lightColours.get(col2), radius, s1, s2, dir, str);
			}
		}
/*      int secmax = countObjects(item, "light");
      if ((idx == 0) && (secmax > 0)) {
        struct SECT {
          struct SECT *next;
          int dir;
          LitCHR_t chr;
          ColCOL_t col;
          ColCOL_t alt;
          char *grp;
          double per;
          double rng;
        } *lights = NULL;
        for (int i = secmax; i > 0; i--) {
          struct SECT *tmp = calloc(1, sizeof(struct SECT));
          tmp->next = lights;
          lights = tmp;
          obj = getObj(item, LIGHTS, i);
          if ((att = getAtt(obj, CATLIT)) != NULL) {
            lights->dir = testAtt(att, LIT_DIR);
          }
          if ((att = getAtt(obj, LITCHR)) != NULL) {
            lights->chr = att->val.val.e;
            switch (lights->chr) {
              case CHR_AL:
                lights->chr = CHR_F;
                break;
              case CHR_ALOC:
                lights->chr = CHR_OC;
                break;
              case CHR_ALLFL:
                lights->chr = CHR_LFL;
                break;
              case CHR_ALFL:
                lights->chr = CHR_FL;
                break;
              case CHR_ALFFL:
                lights->chr = CHR_FFL;
                break;
              default:
                break;
            }
          }
          if ((att = getAtt(obj, SIGGRP)) != NULL) {
            lights->grp = att->val.val.a;
          } else {
            lights->grp = "";
          }
          if ((att = getAtt(obj, SIGPER)) != NULL) {
            lights->per = att->val.val.f;
          }
          if ((att = getAtt(obj, VALNMR)) != NULL) {
            lights->rng = att->val.val.f;
          }
          if ((att = getAtt(obj, COLOUR)) != NULL) {
            lights->col = att->val.val.l->val;
            if (att->val.val.l->next != NULL)
              lights->alt = att->val.val.l->next->val;
          }
        }
        struct COLRNG {
          int col;
          double rng;
        } colrng[14];
        while (lights != NULL) {
          strcpy(string2, "");
          bzero(colrng, 14*sizeof(struct COLRNG));
          colrng[lights->col].col = 1;
          colrng[lights->col].rng = lights->rng;
          struct SECT *this = lights;
          struct SECT *next = lights->next;
          while (next != NULL) {
            if ((this->dir == next->dir) && (this->chr == next->chr) &&
                (strcmp(this->grp, next->grp) == 0) && (this->per == next->per)) {
              colrng[next->col].col = 1;
              if (next->rng > colrng[next->col].rng)
                colrng[next->col].rng = next->rng;
              struct SECT *tmp = lights;
              while (tmp->next != next) tmp = tmp->next;
              tmp->next = next->next;
              free(next);
              next = tmp->next;
            } else {
              next = next->next;
            }
          }
          if (this->chr != CHR_UNKN) {
            if (this->dir) strcpy(string2, "Dir.");
            strcat(string2, light_characters[this->chr]);
            if (strcmp(this->grp, "") != 0) {
              if (this->grp[0] == '(')
                sprintf(strchr(string2, 0), "%s", this->grp);
              else
                sprintf(strchr(string2, 0), "(%s)", this->grp);
            } else {
              if (strlen(string2) > 0) strcat(string2, ".");
            }
            int n = 0;
            for (int i = 0; i < 14; i++) if (colrng[i].col) n++;
            double max = 0.0;
            for (int i = 0; i < 14; i++) if (colrng[i].col && (colrng[i].rng > max)) max = colrng[i].rng;
            double min = max;
            for (int i = 0; i < 14; i++) if (colrng[i].col && (colrng[i].rng > 0.0) && (colrng[i].rng < min)) min = colrng[i].rng;
            if (min == max) {
              for (int i = 0; i < 14; i++) if (colrng[i].col) strcat(string2, light_letters[i]);
            } else {
              for (int i = 0; i < 14; i++) if (colrng[i].col && (colrng[i].rng == max)) strcat(string2, light_letters[i]);
              for (int i = 0; i < 14; i++) if (colrng[i].col && (colrng[i].rng < max) && (colrng[i].rng > min)) strcat(string2, light_letters[i]);
              for (int i = 0; i < 14; i++) if (colrng[i].col && colrng[i].rng == min) strcat(string2, light_letters[i]);
            }
            strcat(string2, ".");
            if (this->per > 0.0) sprintf(strchr(string2, 0), "%gs", this->per);
            if (max > 0.0) {
              sprintf(strchr(string2, 0), "%g", max);
              if (min != max) {
                if (n == 2) strcat(string2, "/");
                else if (n > 2) strcat(string2, "-");
                if (min < max) sprintf(strchr(string2, 0), "%g", min);
              }
              strcat(string2, "M");
            }
            if (strlen(string1) > 0) strcat(string1, "\n");
            strcat(string1, string2);
          }
          lights = this->next;
          free(this);
          this = lights;
        }
      } else {
        if ((att = getAtt(obj, CATLIT)) != NULL) {
          if (testAtt(att, LIT_DIR))
            strcat(string1, "Dir");
        }
        if ((att = getAtt(obj, MLTYLT)) != NULL)
          sprintf(strchr(string1, 0), "%s", stringValue(att->val));
        if ((att = getAtt(obj, LITCHR)) != NULL) {
          char *chrstr = strdup(stringValue(att->val));
          Att_t *grp = getAtt(obj, SIGGRP);
          if (grp != NULL) {
            char *strgrp = strdup(stringValue(grp->val));
            char *grpstr = strtok(strgrp, "()");
            switch (att->val.val.e) {
              case CHR_QLFL:
                sprintf(strchr(string1, 0), "Q(%s)+LFl", grpstr);
                break;
              case CHR_VQLFL:
                sprintf(strchr(string1, 0), "VQ(%s)+LFl", grpstr);
                break;
              case CHR_UQLFL:
                sprintf(strchr(string1, 0), "UQ(%s)+LFl", grpstr);
                break;
              default:
                sprintf(strchr(string1, 0), "%s(%s)", chrstr, grpstr);
                break;
            }
            free(strgrp);
          } else {
            sprintf(strchr(string1, 0), "%s", chrstr);
          }
          free(chrstr);
        }
        if ((att = getAtt(obj, COLOUR)) != NULL) {
          int n = countValues(att);
          if (!((n == 1) && (idx == 0) && (testAtt(att, COL_WHT)))) {
            if ((strlen(string1) > 0) && ((string1[strlen(string1)-1] != ')')))
              strcat(string1, ".");
            Lst_t *lst = att->val.val.l;
            while (lst != NULL) {
              strcat(string1, light_letters[lst->val]);
              lst = lst->next;
            }
          }
        }
        if ((idx == 0) && (att = getAtt(obj, CATLIT)) != NULL) {
          if (testAtt(att, LIT_VERT))
            strcat(string1, "(vert)");
          if (testAtt(att, LIT_HORI))
            strcat(string1, "(hor)");
        }
        if ((strlen(string1) > 0) &&
            ((getAtt(obj, SIGPER) != NULL) ||
             (getAtt(obj, HEIGHT) != NULL) ||
             (getAtt(obj, VALMXR) != NULL)) &&
            (string1[strlen(string1)-1] != ')'))
          strcat(string1, ".");
        if ((att = getAtt(obj, SIGPER)) != NULL)
          sprintf(strchr(string1, 0), "%ss", stringValue(att->val));
        if ((idx == 0) && (item->objs.obj != LITMIN)) {
          if ((att = getAtt(obj, HEIGHT)) != NULL)
            sprintf(strchr(string1, 0), "%sm", stringValue(att->val));
          if ((att = getAtt(obj, VALNMR)) != NULL)
            sprintf(strchr(string1, 0), "%sM", stringValue(att->val));
        }
        if ((idx == 0) && (att = getAtt(obj, CATLIT)) != NULL) {
          if (testAtt(att, LIT_FRNT))
            strcat(string1, "(Front)");
          if (testAtt(att, LIT_REAR))
            strcat(string1, "(Rear)");
          if (testAtt(att, LIT_UPPR))
            strcat(string1, "(Upper)");
          if (testAtt(att, LIT_LOWR))
            strcat(string1, "(Lower)");
        }
      }
    }
*/	}

}
