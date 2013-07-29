/* Copyright 2013 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package seamap;

import java.awt.geom.*;

import seamap.SeaMap.*;

public class Lights {

	private static Point2D.Double radial(Snode centre, double radius, double angle) {
		Point2D origin = Renderer.helper.getPoint(centre);
		double mile = Renderer.helper.getPoint(Renderer.map.new Snode((centre.lat + Math.toRadians(1/60)), centre.lon)).getY() - origin.getY();
		return new Point2D.Double(origin.getX() - (radius * mile * Math.sin(angle)), origin.getY() - (radius * mile * Math.cos(angle)));
	}
/*
void renderFlare(Item_t *item) {
  char *col = light_colours[COL_MAG];
  Obj_t *obj = getObj(item, LIGHTS, 0);
  Att_t *att;
  if (((att = getAtt(obj, COLOUR)) != NULL) && (att->val.val.l->next == NULL)) {
    col = light_colours[att->val.val.l->val];
  }
  renderSymbol(item, LIGHTS, "light", "", col, CC, 0, 0, 120);
}

void renderSector(Item_t *item, int s, char *text, char *style, double offset, int dy) {
  Obj_t *sector;
  double start, end;
  Att_t *att;
  XY_t p0, p1;
  double r0, r1;
  double b0, b1, span;
  char *col;
  XY_t pos = findCentroid(item);
  if ((sector = getObj(item, LIGHTS, s)) != NULL) {
    strcpy(string1, (att = getAtt(sector, LITRAD)) != NULL ? att->val.val.a : "0.2");
    if (((att = getAtt(sector, CATLIT)) != NULL) && (testAtt(att, LIT_DIR)) && ((att = getAtt(sector, ORIENT)) != NULL)) {
      b0 = fmod(540.0 - att->val.val.f, 360.0);
      if ((att = getAtt(sector, COLOUR)) != NULL) {
        col = light_colours[att->val.val.l->val];
        r0 = atof(string1);
        p0 = radial(pos, r0, b0);
        printf("<path d=\"M %g,%g L %g,%g\" style=\"fill:none;stroke:#808080;stroke-width:%g;stroke-dasharray:%g\"/>\n",
               pos.x, pos.y, p0.x, p0.y, (4 * symbolScale[zoom]), (20 * symbolScale[zoom]));
        start = fmod(b0 + 2.0, 360.0);
        end = fmod(360.0 + b0 - 2.0, 360.0);
        Obj_t *adj;
        for (int i = s-1; i <= s+1; i++) {
          if (i == s) continue;
          if ((adj = getObj(item, LIGHTS, i)) == NULL) continue;
          Att_t *att;
          if (((att = getAtt(adj, CATLIT)) != NULL) && (testAtt(att, LIT_DIR)) && ((att = getAtt(adj, ORIENT)) != NULL)) {
            b1 = fmod(540.0 - att->val.val.f, 360.0);
            if (fabs(b0 - b1) > 180.0) {
              if (b0 < b1) b0 += 360.0;
              else b1 += 360.0;
            }
            if (fabs(b0 - b1) < 4.0) {
              if (b1 > b0) start = fmod((720.0 + b0 + b1) / 2.0, 360.0);
              else end = fmod((720.0 + b0 + b1) / 2.0, 360.0);
            }
          }
        }
        p0 = radial(pos, r0, start);
        p1 = radial(pos, r0, end);
        printf("<path id=\"%d\" d=\"M %g,%g A %g,%g,0,0,1,%g,%g\" style=\"fill:none;stroke:%s;stroke-width:%g\"/>\n",
               ++ref, p0.x, p0.y, r0*mile, r0*mile, p1.x, p1.y, col, (20 * symbolScale[zoom]));
        if (att->val.val.l->next != NULL) {
          char *col = light_colours[att->val.val.l->next->val];
          r1 = r0 - (20 * symbolScale[zoom]/mile);
          p0 = radial(pos, r1, start);
          p1 = radial(pos, r1, end);
          printf("<path d=\"M %g,%g A %g,%g,0,0,1,%g,%g\" style=\"fill:none;stroke:%s;stroke-width:%g\"/>\n",
                 p0.x, p0.y, r1*mile, r1*mile, p1.x, p1.y, col, (20 * symbolScale[zoom]));
        }
      }
    } else if ((att = getAtt(sector, SECTR1)) != NULL) {
      start = fmod(540.0 - att->val.val.f, 360.0);
      if ((att = getAtt(sector, SECTR2)) != NULL) {
        end = fmod(540.0 - att->val.val.f, 360.0);
        start += start < end ? 360.0 : 0.0;
        if ((att = getAtt(sector, COLOUR)) != NULL) {
          char *ttok, *etok;
          char *radstr = strdup(string1);
          int arc = 0;
          col = light_colours[att->val.val.l->val];
          r0 = 0.0;
          b0 = b1 = start;
          for (char *tpl = strtok_r(radstr, ";", &ttok); tpl != NULL; tpl = strtok_r(NULL, ";", &ttok)) {
            p0 = radial(pos, r0, b0);
            span = 0.0;
            char *ele = strtok_r(tpl, ":", &etok);
            if ((*tpl == ':') && (r0 == 0.0)) {
              r1 = 0.2;
            } else if (*tpl != ':') {
              r1 = atof(tpl);
              ele = strtok_r(NULL, ":", &etok);
            }
            while (ele != NULL) {
              if (isalpha(*ele)) {
                if (strcmp(ele, "suppress") == 0) arc = 2;
                else if (strcmp(ele, "dashed") == 0) arc = 1;
                else arc = 0;
              } else {
                span = atof(ele);
              }
              ele = strtok_r(NULL, ":", &etok);
            }
            if (span == 0.0) {
              char *back = (ttok != NULL) ? strstr(ttok, "-") : NULL;
              if (back != NULL) {
                span = b0 - end + atof(back);
              } else {
                span = b0 - end;
              }
            }
            if (r1 != r0) {
              p1 = radial(pos, r1, b0);
              if (!((start == 180.0) && (end == 180.0)))
                printf("<path d=\"M %g,%g L %g,%g\" style=\"fill:none;stroke:#808080;stroke-width:%g;stroke-dasharray:%g\"/>\n",
                       p0.x, p0.y, p1.x, p1.y, (4 * symbolScale[zoom]), (20 * symbolScale[zoom]));
              r0 = r1;
              p0 = p1;
            }
            if (span < 0.0) {
              b1 = end - span;
              b1 = b1 > b0 ? b0 : b1;
              b0 = b1;
              b1 = end;
              p0 = radial(pos, r0, b0);
            } else {
              b1 = b0 - span;
              b1 = b1 < end ? end : b1;
            }
            p1 = radial(pos, r1, b1);
            if ((b0 == 180.0) && (b1 == 180.0)) {
              span = 360.0;
              p1 = radial(pos, r1, b1+0.01);
            }
            if (arc == 0) {
              if (p0.x < p1.x)
                printf("<path id=\"%d\" d=\"M %g,%g A %g,%g,0,%d,1,%g,%g\" style=\"fill:none;stroke:%s;stroke-width:%g\"/>\n",
                       ++ref, p0.x, p0.y, r1*mile, r1*mile, span>180.0, p1.x, p1.y, col, (20 * symbolScale[zoom]));
              else
                printf("<path id=\"%d\" d=\"M %g,%g A %g,%g,0,%d,0,%g,%g\" style=\"fill:none;stroke:%s;stroke-width:%g\"/>\n",
                       ++ref, p1.x, p1.y, r1*mile, r1*mile, span>180.0, p0.x, p0.y, col, (20 * symbolScale[zoom]));
              if (text != NULL) {
                double chord = sqrt(pow((p0.x - p1.x), 2) + pow((p0.y - p1.y), 2));
                if ((chord > (strlen(text) * textScale[zoom] * 50)) || ((b0 == 180.0) && (b1 == 180.0)))
                  drawLineText(item, text, style, offset, dy, ref);
              }
            } else if (arc == 1) {
              printf("<path d=\"M %g,%g A %g,%g,0,%d,1,%g,%g\" style=\"fill:none;stroke:%s;stroke-width:%g;stroke-opacity:0.5;stroke-dasharray:%g\"/>\n",
                     p0.x, p0.y, r1*mile, r1*mile, span>180.0, p1.x, p1.y, col, (10 * symbolScale[zoom]), (30 * symbolScale[zoom]));
            }
            if ((arc == 0) && (att->val.val.l->next != NULL)) {
              char *col = light_colours[att->val.val.l->next->val];
              double r2 = r1 - (20 * symbolScale[zoom]/mile);
              XY_t p2 = radial(pos, r2, b0);
              XY_t p3 = radial(pos, r2, b1);
              printf("<path d=\"M %g,%g A %g,%g,0,%d,1,%g,%g\" style=\"fill:none;stroke:%s;stroke-width:%g\"/>\n",
                     p2.x, p2.y, r1*mile, r1*mile, span>180.0, p3.x, p3.y, col, (20 * symbolScale[zoom]));
            }
            b0 = b1;
            if (b0 == end) break;
          }
          if (!((start == 180.0) && (end == 180.0)))
            printf("<path d=\"M %g,%g L %g,%g\" style=\"fill:none;stroke:#808080;stroke-width:%g;stroke-dasharray:%g\"/>\n",
                   pos.x, pos.y, p1.x, p1.y, (4 * symbolScale[zoom]), (20 * symbolScale[zoom]));
          free(radstr);
        }
      }
    }
  }
}
char *charString(Item_t *item, char *type, int idx) {
  strcpy(string1, "");
  Att_t *att = NULL;
  Obj_t *obj = getObj(item, enumType(type), idx);
  switch (enumType(type)) {
    case CGUSTA:
      strcpy(string1, "CG");
      if ((obj != NULL) && (att = getAtt(obj, COMCHA)) != NULL)
        sprintf(strchr(string1, 0), " Ch.%s", stringValue(att->val));
      break;
    case FOGSIG:
      if (obj != NULL) {
        if ((att = getAtt(obj, CATFOG)) != NULL)
          strcat(string1, fog_signals[att->val.val.e]);
        if ((att = getAtt(obj, SIGGRP)) != NULL)
          sprintf(strchr(string1, 0), "(%s)", stringValue(att->val));
        else 
          strcat(string1, " ");
        if ((att = getAtt(obj, SIGPER)) != NULL)
          sprintf(strchr(string1, 0), "%ss ", stringValue(att->val));
        if ((att = getAtt(obj, VALMXR)) != NULL)
          sprintf(strchr(string1, 0), "%sM", stringValue(att->val));
      }
      break;
    case RTPBCN:
      if (obj != NULL) {
        if ((att = getAtt(obj, CATRTB)) != NULL)
          strcat(string1, rtb_map[att->val.val.e]);
        if ((att = getAtt(obj, SIGGRP)) != NULL)
          sprintf(strchr(string1, 0), "(%s)", stringValue(att->val));
        else 
          strcat(string1, " ");
        if ((att = getAtt(obj, SIGPER)) != NULL)
          sprintf(strchr(string1, 0), "%ss ", stringValue(att->val));
        if ((att = getAtt(obj, VALMXR)) != NULL)
          sprintf(strchr(string1, 0), "%sM", stringValue(att->val));
      }
      break;
    case SISTAT:
      strcpy(string1, "SS");
      if (obj != NULL) {
        if ((att = getAtt(obj, CATSIT)) != NULL)
          strcat(string1, sit_map[att->val.val.l->val]);
        if ((att = getAtt(obj, COMCHA)) != NULL)
          sprintf(strchr(string1, 0), "\nCh.%s", stringValue(att->val));
      }
      break;
    case SISTAW:
      strcpy(string1, "SS");
      if (obj != NULL) {
        if ((att = getAtt(obj, CATSIW)) != NULL)
          strcat(string1, siw_map[att->val.val.l->val]);
        if ((att = getAtt(obj, COMCHA)) != NULL)
          sprintf(strchr(string1, 0), "\nCh.%s", stringValue(att->val));
      }
      break;
    case LIGHTS:
    {
      int secmax = countObjects(item, "light");
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
      break;
    default: break;
 }
  return string1;
}
*/
	
}
