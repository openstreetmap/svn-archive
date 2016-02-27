/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package s57;

import java.util.*;

import s57.S57obj.*;

public class S57att { // S57 Attribute lookup tables & methods
 
 public enum Att {
   UNKATT, AGENCY, BCNSHP, BUISHP, BOYSHP, BURDEP, CALSGN, CATAIR, CATACH, CATBRG, CATBUA, CATCBL, CATCAN, CATCAM, CATCHP, CATCOA, CATCTR, CATCON, CATCRN, CATDAM,
   CATDIS, CATDOC, CATDPG, CATFNC, CATFRY, CATFIF, CATFOG, CATFOR, CATGAT, CATHAF, CATHLK, CATICE, CATINB, CATLND, CATLMK, CATLAM, CATLIT, CATMFA, CATMPA, CATMOR,
   CATNAV, CATOBS, CATOFP, CATOLB, CATPLE, CATPIL, CATPIP, CATPRA, CATPYL, CATRAS, CATRTB, CATROS, CATTRK, CATRSC, CATREA, CATROD, CATRUN, CATSEA, CATSLC, CATSIT,
   CATSIW, CATSIL, CATSLO, CATSCF, CATSPM, CATTSS, CATVEG, CATWAT, CATWED, CATWRK, COLOUR, COLPAT, COMCHA, CPDATE, CSCALE, CONDTN, CONRAD, CONVIS, CURVEL, DATEND,
   DATSTA, DRVAL1, DRVAL2, ELEVAT, ESTRNG, EXCLIT, EXPSOU, FUNCTN, HEIGHT, HORACC, HORCLR, HORLEN, HORWID, ICEFAC, INFORM, JRSDTN, LIFCAP, LITCHR, LITVIS, MARSYS,
   MLTYLT, NATION, NATCON, NATSUR, NATQUA, NMDATE, OBJNAM, ORIENT, PEREND, PERSTA, PICREP, PILDST, PRCTRY, PRODCT, PUBREF, QUASOU, RADWAL, RADIUS, RYRMGV, RESTRN,
   SCAMIN, SCVAL1, SCVAL2, SECTR1, SECTR2, SHIPAM, SIGFRQ, SIGGEN, SIGGRP, SIGPER, SIGSEQ, SOUACC, SDISMX, SDISMN, SORDAT, SORIND, STATUS, SURATH, SUREND, SURSTA,
   SURTYP, TECSOU, TXTDSC, TS_TSP, TS_TSV, T_ACWL, T_HWLW, T_MTOD, T_THDF, T_TINT, T_TSVL, T_VAHC, TIMEND, TIMSTA, TOPSHP, TRAFIC, VALACM, VALDCO, VALLMA, VALMAG,
   VALMXR, VALNMR, VALSOU, VERACC, VERCLR, VERCCL, VERCOP, VERCSA, VERDAT, VERLEN, WATLEV, CAT_TS, NINFOM, NOBJNM, NPLDST, NTXTDS, HORDAT, POSACC, QUAPOS, CLSDNG,
   DIRIMP, DISBK1, DISBK2, DISIPU, DISIPD, ELEVA1, ELEVA2, FNCTNM, WTWDIS, BUNVES, BNKWTW, COMCTN, HORCLL, HORCLW, TRSHGD, UNLOCD, HIGWAT, HIGNAM, LOWWAT, LOWNAM,
   MEAWAT, MEANAM, OTHWAT, OTHNAM, REFLEV, SDRLEV, VCRLEV, SCHREF, USESHP, CURVHW, CURVLW, CURVMW, CURVOW, APTREF, SHPTYP, UPDMSG, ADDMRK, CATNMK, CATBRT, CATBUN,
   CATCCL, CATCOM, CATHBR, CATRFD, CATTML, CATGAG, CATVTR, CATTAB, CATEXS, LG_SPD, LG_SPR, LG_BME, LG_LGS, LG_DRT, LG_WDP, LG_WDU, LG_REL, LG_FNC, LG_DES, LG_PBR,
   LC_CSI, LC_CSE, LC_ASI, LC_ASE, LC_CCI, LC_CCE, LC_BM1, LC_BM2, LC_LG1, LC_LG2, LC_DR1, LC_DR2, LC_SP1, LC_SP2, LC_WD1, LC_WD2, LITRAD, CATCVR, HUNITS
 }

 private static final EnumMap<Att, Integer> AttS57 = new EnumMap<Att, Integer>(Att.class);
 static {
  AttS57.put(Att.UNKATT, 0); AttS57.put(Att.AGENCY, 1); AttS57.put(Att.BCNSHP, 2); AttS57.put(Att.BUISHP, 3); AttS57.put(Att.BOYSHP, 4); AttS57.put(Att.BURDEP, 5);
  AttS57.put(Att.CALSGN, 6); AttS57.put(Att.CATAIR, 7); AttS57.put(Att.CATACH, 8); AttS57.put(Att.CATBRG, 9); AttS57.put(Att.CATBUA, 10); AttS57.put(Att.CATCBL, 11);
  AttS57.put(Att.CATCAN, 12); AttS57.put(Att.CATCAM, 13); AttS57.put(Att.CATCHP, 14); AttS57.put(Att.CATCOA, 15); AttS57.put(Att.CATCTR, 16); AttS57.put(Att.CATCON, 17);
  AttS57.put(Att.CATCVR, 18); AttS57.put(Att.CATCRN, 19); AttS57.put(Att.CATDAM, 20); AttS57.put(Att.CATDIS, 21); AttS57.put(Att.CATDOC, 22); AttS57.put(Att.CATDPG, 23);
  AttS57.put(Att.CATFNC, 24); AttS57.put(Att.CATFRY, 25); AttS57.put(Att.CATFIF, 26); AttS57.put(Att.CATFOG, 27); AttS57.put(Att.CATFOR, 28); AttS57.put(Att.CATGAT, 29);
  AttS57.put(Att.CATHAF, 30); AttS57.put(Att.CATHLK, 31); AttS57.put(Att.CATICE, 32); AttS57.put(Att.CATINB, 33); AttS57.put(Att.CATLND, 34); AttS57.put(Att.CATLMK, 35);
  AttS57.put(Att.CATLAM, 36); AttS57.put(Att.CATLIT, 37); AttS57.put(Att.CATMFA, 38); AttS57.put(Att.CATMPA, 39); AttS57.put(Att.CATMOR, 40); AttS57.put(Att.CATNAV, 41);
  AttS57.put(Att.CATOBS, 42); AttS57.put(Att.CATOFP, 43); AttS57.put(Att.CATOLB, 44); AttS57.put(Att.CATPLE, 45); AttS57.put(Att.CATPIL, 46); AttS57.put(Att.CATPIP, 47);
  AttS57.put(Att.CATPRA, 48); AttS57.put(Att.CATPYL, 49); AttS57.put(Att.CATRAS, 51); AttS57.put(Att.CATRTB, 52); AttS57.put(Att.CATROS, 53); AttS57.put(Att.CATTRK, 54);
  AttS57.put(Att.CATRSC, 55); AttS57.put(Att.CATREA, 56); AttS57.put(Att.CATROD, 57); AttS57.put(Att.CATRUN, 58); AttS57.put(Att.CATSEA, 59); AttS57.put(Att.CATSLC, 60);
  AttS57.put(Att.CATSIT, 61); AttS57.put(Att.CATSIW, 62); AttS57.put(Att.CATSIL, 63); AttS57.put(Att.CATSLO, 64); AttS57.put(Att.CATSCF, 65); AttS57.put(Att.CATSPM, 66);
  AttS57.put(Att.CATTSS, 67); AttS57.put(Att.CATVEG, 68); AttS57.put(Att.CATWAT, 69); AttS57.put(Att.CATWED, 70); AttS57.put(Att.CATWRK, 71); AttS57.put(Att.COLOUR, 75);
  AttS57.put(Att.COLPAT, 76); AttS57.put(Att.COMCHA, 77); AttS57.put(Att.CONDTN, 81); AttS57.put(Att.CONRAD, 82); AttS57.put(Att.CONVIS, 83); AttS57.put(Att.CURVEL, 84);
  AttS57.put(Att.DATEND, 85); AttS57.put(Att.DATSTA, 86); AttS57.put(Att.DRVAL1, 87); AttS57.put(Att.DRVAL2, 88); AttS57.put(Att.ELEVAT, 90); AttS57.put(Att.ESTRNG, 91);
  AttS57.put(Att.EXCLIT, 92); AttS57.put(Att.EXPSOU, 93); AttS57.put(Att.FUNCTN, 94); AttS57.put(Att.HEIGHT, 95); AttS57.put(Att.HUNITS, 96); AttS57.put(Att.HORACC, 97);
  AttS57.put(Att.HORCLR, 98); AttS57.put(Att.HORLEN, 99); AttS57.put(Att.HORWID, 100); AttS57.put(Att.ICEFAC, 101); AttS57.put(Att.INFORM, 102); AttS57.put(Att.JRSDTN, 103);
  AttS57.put(Att.LIFCAP, 106); AttS57.put(Att.LITCHR, 107); AttS57.put(Att.LITVIS, 108); AttS57.put(Att.MARSYS, 109); AttS57.put(Att.MLTYLT, 110); AttS57.put(Att.NATION, 111);
  AttS57.put(Att.NATCON, 112); AttS57.put(Att.NATSUR, 113); AttS57.put(Att.NATQUA, 114); AttS57.put(Att.NMDATE, 115); AttS57.put(Att.OBJNAM, 116); AttS57.put(Att.ORIENT, 117);
  AttS57.put(Att.PEREND, 118); AttS57.put(Att.PERSTA, 119); AttS57.put(Att.PICREP, 120); AttS57.put(Att.PILDST, 121); AttS57.put(Att.PRCTRY, 122); AttS57.put(Att.PRODCT, 123);
  AttS57.put(Att.PUBREF, 124); AttS57.put(Att.QUASOU, 125); AttS57.put(Att.RADWAL, 126); AttS57.put(Att.RADIUS, 127); AttS57.put(Att.RYRMGV, 130); AttS57.put(Att.RESTRN, 131);
  AttS57.put(Att.SCAMIN, 133); AttS57.put(Att.SCVAL1, 134); AttS57.put(Att.SCVAL2, 135); AttS57.put(Att.SECTR1, 136); AttS57.put(Att.SECTR2, 137); AttS57.put(Att.SHIPAM, 138);
  AttS57.put(Att.SIGFRQ, 139); AttS57.put(Att.SIGGEN, 140); AttS57.put(Att.SIGGRP, 141); AttS57.put(Att.SIGPER, 142); AttS57.put(Att.SIGSEQ, 143); AttS57.put(Att.SOUACC, 144);
  AttS57.put(Att.SDISMX, 145); AttS57.put(Att.SDISMN, 146); AttS57.put(Att.SORDAT, 147); AttS57.put(Att.SORIND, 148); AttS57.put(Att.STATUS, 149); AttS57.put(Att.SURATH, 150);
  AttS57.put(Att.SUREND, 151); AttS57.put(Att.SURSTA, 152); AttS57.put(Att.SURTYP, 153); AttS57.put(Att.TECSOU, 156); AttS57.put(Att.TXTDSC, 158); AttS57.put(Att.TIMEND, 168);
  AttS57.put(Att.TIMSTA, 169); AttS57.put(Att.TOPSHP, 171); AttS57.put(Att.TRAFIC, 172); AttS57.put(Att.VALACM, 173); AttS57.put(Att.VALDCO, 174); AttS57.put(Att.VALLMA, 175);
  AttS57.put(Att.VALMAG, 176); AttS57.put(Att.VALMXR, 177); AttS57.put(Att.VALNMR, 178); AttS57.put(Att.VALSOU, 179); AttS57.put(Att.VERACC, 180); AttS57.put(Att.VERCLR, 181);
  AttS57.put(Att.VERCCL, 182); AttS57.put(Att.VERCOP, 183); AttS57.put(Att.VERCSA, 184); AttS57.put(Att.VERDAT, 185); AttS57.put(Att.VERLEN, 186); AttS57.put(Att.WATLEV, 187);
  AttS57.put(Att.CAT_TS, 188); AttS57.put(Att.NINFOM, 300); AttS57.put(Att.NOBJNM, 301); AttS57.put(Att.NPLDST, 302); AttS57.put(Att.NTXTDS, 304); AttS57.put(Att.HORDAT, 400);
  AttS57.put(Att.POSACC, 401); AttS57.put(Att.QUAPOS, 402);
 }

 private static final EnumMap<Att, Integer> AttIENC = new EnumMap<Att, Integer>(Att.class);
 static {
  AttIENC.put(Att.CATACH, 17000); AttIENC.put(Att.CATDIS, 17001); AttIENC.put(Att.CATSIT, 17002); AttIENC.put(Att.CATSIW, 17003); AttIENC.put(Att.RESTRN, 17004);
  AttIENC.put(Att.VERDAT, 17005); AttIENC.put(Att.CATBRG, 17006); AttIENC.put(Att.CATFRY, 17007); AttIENC.put(Att.CATHAF, 17008); AttIENC.put(Att.MARSYS, 17009);
  AttIENC.put(Att.CATCHP, 17010); AttIENC.put(Att.CATLAM, 17011); AttIENC.put(Att.CATSLC, 17012); AttIENC.put(Att.ADDMRK, 17050); AttIENC.put(Att.CATNMK, 17052);
  AttIENC.put(Att.CLSDNG, 17055); AttIENC.put(Att.DIRIMP, 17056); AttIENC.put(Att.DISBK1, 17057); AttIENC.put(Att.DISBK2, 17058); AttIENC.put(Att.DISIPU, 17059);
  AttIENC.put(Att.DISIPD, 17060); AttIENC.put(Att.ELEVA1, 17061); AttIENC.put(Att.ELEVA2, 17062); AttIENC.put(Att.FNCTNM, 17063); AttIENC.put(Att.WTWDIS, 17064);
  AttIENC.put(Att.BUNVES, 17065); AttIENC.put(Att.CATBRT, 17066); AttIENC.put(Att.CATBUN, 17067); AttIENC.put(Att.CATCCL, 17069); AttIENC.put(Att.CATHBR, 17070);
  AttIENC.put(Att.CATRFD, 17071); AttIENC.put(Att.CATTML, 17072); AttIENC.put(Att.COMCTN, 17073); AttIENC.put(Att.HORCLL, 17074); AttIENC.put(Att.HORCLW, 17075);
  AttIENC.put(Att.TRSHGD, 17076); AttIENC.put(Att.UNLOCD, 17077); AttIENC.put(Att.CATGAG, 17078); AttIENC.put(Att.HIGWAT, 17080); AttIENC.put(Att.HIGNAM, 17081);
  AttIENC.put(Att.LOWWAT, 17082); AttIENC.put(Att.LOWNAM, 17083); AttIENC.put(Att.MEAWAT, 17084); AttIENC.put(Att.MEANAM, 17085); AttIENC.put(Att.OTHWAT, 17086);
  AttIENC.put(Att.OTHNAM, 17087); AttIENC.put(Att.REFLEV, 17088); AttIENC.put(Att.SDRLEV, 17089); AttIENC.put(Att.VCRLEV, 17090); AttIENC.put(Att.CATVTR, 17091);
  AttIENC.put(Att.CATTAB, 17092); AttIENC.put(Att.SCHREF, 17093); AttIENC.put(Att.USESHP, 17094); AttIENC.put(Att.CURVHW, 17095); AttIENC.put(Att.CURVLW, 17096);
  AttIENC.put(Att.CURVMW, 17097); AttIENC.put(Att.CURVOW, 17098); AttIENC.put(Att.APTREF, 17099); AttIENC.put(Att.CATEXS, 17100); AttIENC.put(Att.CATCBL, 17101);
  AttIENC.put(Att.CATHLK, 17102); AttIENC.put(Att.HUNITS, 17103); AttIENC.put(Att.WATLEV, 17104); AttIENC.put(Att.LG_SPD, 18001); AttIENC.put(Att.LG_SPR, 18002);
  AttIENC.put(Att.LG_BME, 18003); AttIENC.put(Att.LG_LGS, 18004); AttIENC.put(Att.LG_DRT, 18005); AttIENC.put(Att.LG_WDP, 18006); AttIENC.put(Att.LG_WDU, 18007);
  AttIENC.put(Att.LG_REL, 18008); AttIENC.put(Att.LG_FNC, 18009); AttIENC.put(Att.LG_DES, 18010); AttIENC.put(Att.LG_PBR, 18011); AttIENC.put(Att.LC_CSI, 18012);
  AttIENC.put(Att.LC_CSE, 18013); AttIENC.put(Att.LC_ASI, 18014); AttIENC.put(Att.LC_ASE, 18015); AttIENC.put(Att.LC_CCI, 18016); AttIENC.put(Att.LC_CCE, 18017);
  AttIENC.put(Att.LC_BM1, 18018); AttIENC.put(Att.LC_BM2, 18019); AttIENC.put(Att.LC_LG1, 18020); AttIENC.put(Att.LC_LG2, 18021); AttIENC.put(Att.LC_DR1, 18022);
  AttIENC.put(Att.LC_DR2, 18023); AttIENC.put(Att.LC_SP1, 18024); AttIENC.put(Att.LC_SP2, 18025); AttIENC.put(Att.LC_WD1, 18026); AttIENC.put(Att.LC_WD2, 18027);
  AttIENC.put(Att.SHPTYP, 33066); AttIENC.put(Att.UPDMSG, 40000); AttIENC.put(Att.BNKWTW, 17999);
 }
 
 private static final EnumMap<Att, String> AttStr = new EnumMap<Att, String>(Att.class);
 static {
  AttStr.put(Att.UNKATT, ""); AttStr.put(Att.AGENCY, "agency"); AttStr.put(Att.BCNSHP, "shape"); AttStr.put(Att.BUISHP, "shape"); AttStr.put(Att.BOYSHP, "shape");
  AttStr.put(Att.BURDEP, "depth_buried"); AttStr.put(Att.CALSGN, "callsign"); AttStr.put(Att.CATAIR, "category"); AttStr.put(Att.CATACH, "category");
  AttStr.put(Att.CATBRG, "category"); AttStr.put(Att.CATBUA, "category"); AttStr.put(Att.CATCBL, "category"); AttStr.put(Att.CATCAN, "category");
  AttStr.put(Att.CATCAM, "category"); AttStr.put(Att.CATCHP, "category"); AttStr.put(Att.CATCOA, "category"); AttStr.put(Att.CATCTR, "category");
  AttStr.put(Att.CATCON, "category"); AttStr.put(Att.CATCRN, "category"); AttStr.put(Att.CATDAM, "category"); AttStr.put(Att.CATDIS, "category");
  AttStr.put(Att.CATDOC, "category"); AttStr.put(Att.CATDPG, "category"); AttStr.put(Att.CATFNC, "category"); AttStr.put(Att.CATFRY, "category");
  AttStr.put(Att.CATFIF, "category"); AttStr.put(Att.CATFOG, "category"); AttStr.put(Att.CATFOR, "category"); AttStr.put(Att.CATGAT, "category");
  AttStr.put(Att.CATHAF, "category"); AttStr.put(Att.CATHLK, "category"); AttStr.put(Att.CATICE, "category"); AttStr.put(Att.CATINB, "category");
  AttStr.put(Att.CATLND, "category"); AttStr.put(Att.CATLMK, "category"); AttStr.put(Att.CATLAM, "category"); AttStr.put(Att.CATLIT, "category");
  AttStr.put(Att.CATMFA, "category"); AttStr.put(Att.CATMPA, "category"); AttStr.put(Att.CATMOR, "category"); AttStr.put(Att.CATNAV, "category");
  AttStr.put(Att.CATOBS, "category"); AttStr.put(Att.CATOFP, "category"); AttStr.put(Att.CATOLB, "category"); AttStr.put(Att.CATPLE, "category");
  AttStr.put(Att.CATPIL, "category"); AttStr.put(Att.CATPIP, "category"); AttStr.put(Att.CATPRA, "category"); AttStr.put(Att.CATPYL, "category");
  AttStr.put(Att.CATRAS, "category"); AttStr.put(Att.CATRTB, "category"); AttStr.put(Att.CATROS, "category"); AttStr.put(Att.CATTRK, "category");
  AttStr.put(Att.CATRSC, "category"); AttStr.put(Att.CATREA, "category"); AttStr.put(Att.CATROD, "category"); AttStr.put(Att.CATRUN, "category");
  AttStr.put(Att.CATSEA, "category"); AttStr.put(Att.CATSLC, "category"); AttStr.put(Att.CATSIT, "category"); AttStr.put(Att.CATSIW, "category");
  AttStr.put(Att.CATSIL, "category"); AttStr.put(Att.CATSLO, "category"); AttStr.put(Att.CATSCF, "category"); AttStr.put(Att.CATSPM, "category");
  AttStr.put(Att.CATTSS, "category"); AttStr.put(Att.CATVEG, "category"); AttStr.put(Att.CATWAT, "category"); AttStr.put(Att.CATWED, "category");
  AttStr.put(Att.CATWRK, "category"); AttStr.put(Att.COLOUR, "colour"); AttStr.put(Att.COLPAT, "colour_pattern"); AttStr.put(Att.COMCHA, "channel");
  AttStr.put(Att.CONDTN, "condition"); AttStr.put(Att.CONRAD, "reflectivity"); AttStr.put(Att.CONVIS, "conspicuity"); AttStr.put(Att.CURVEL, "velocity");
  AttStr.put(Att.DATEND, "end_date"); AttStr.put(Att.DATSTA, "start_date"); AttStr.put(Att.DRVAL1, "minimum_depth"); AttStr.put(Att.DRVAL2, "maximum_depth");
  AttStr.put(Att.ELEVAT, "elevation"); AttStr.put(Att.ESTRNG, "estimated_range"); AttStr.put(Att.EXCLIT, "exhibition"); AttStr.put(Att.EXPSOU, "exposition");
  AttStr.put(Att.FUNCTN, "function"); AttStr.put(Att.HEIGHT, "height"); AttStr.put(Att.HUNITS, "units"); AttStr.put(Att.HORACC, "accuracy");
  AttStr.put(Att.HORCLR, "clearance_width"); AttStr.put(Att.HORLEN, "length"); AttStr.put(Att.HORWID, "width"); AttStr.put(Att.ICEFAC, "factor");
  AttStr.put(Att.INFORM, "information"); AttStr.put(Att.JRSDTN, "jurisdiction"); AttStr.put(Att.LIFCAP, "maximum_load"); AttStr.put(Att.LITCHR, "character");
  AttStr.put(Att.LITVIS, "visibility"); AttStr.put(Att.MARSYS, "system"); AttStr.put(Att.MLTYLT, "multiple"); AttStr.put(Att.NATION, "nationality");
  AttStr.put(Att.NATCON, "construction"); AttStr.put(Att.NATSUR, "surface"); AttStr.put(Att.NATQUA, "surface_qualification"); AttStr.put(Att.NMDATE, "nm_date");
  AttStr.put(Att.OBJNAM, "name"); AttStr.put(Att.ORIENT, "orientation"); AttStr.put(Att.PEREND, "period_end"); AttStr.put(Att.PERSTA, "period_start");
  AttStr.put(Att.PICREP, "picture"); AttStr.put(Att.PILDST, "pilot_district"); AttStr.put(Att.PRCTRY, "producing_country"); AttStr.put(Att.PRODCT, "product");
  AttStr.put(Att.PUBREF, "reference"); AttStr.put(Att.QUASOU, "quality"); AttStr.put(Att.RADWAL, "wavelength"); AttStr.put(Att.RADIUS, "radius");
  AttStr.put(Att.RYRMGV, "year"); AttStr.put(Att.RESTRN, "restriction"); AttStr.put(Att.SECTR1, "sector_start"); AttStr.put(Att.SECTR2, "sector_end");
  AttStr.put(Att.SHIPAM, "shift"); AttStr.put(Att.SIGFRQ, "frequency"); AttStr.put(Att.SIGGEN, "generation"); AttStr.put(Att.SIGGRP, "group");
  AttStr.put(Att.SIGPER, "period"); AttStr.put(Att.SIGSEQ, "sequence"); AttStr.put(Att.SOUACC, "sounding_accuracy"); AttStr.put(Att.SDISMX, "maximum_sounding");
  AttStr.put(Att.SDISMN, "minimum_sounding"); AttStr.put(Att.SORDAT, "source_date"); AttStr.put(Att.SORIND, "source"); AttStr.put(Att.STATUS, "status");
  AttStr.put(Att.SURATH, "authority"); AttStr.put(Att.SUREND, "survey_end"); AttStr.put(Att.SURSTA, "survey_start"); AttStr.put(Att.SURTYP, "survey");
  AttStr.put(Att.TECSOU, "technique"); AttStr.put(Att.TXTDSC, "document"); AttStr.put(Att.TIMEND, "end_time"); AttStr.put(Att.TIMSTA, "start_time");
  AttStr.put(Att.TOPSHP, "shape"); AttStr.put(Att.TRAFIC, "traffic_flow"); AttStr.put(Att.VALACM, "variation_change"); AttStr.put(Att.VALDCO, "depth");
  AttStr.put(Att.VALLMA, "anomaly"); AttStr.put(Att.VALMAG, "variation"); AttStr.put(Att.VALMXR, "maximum_range"); AttStr.put(Att.VALNMR, "range");
  AttStr.put(Att.VALSOU, "depth"); AttStr.put(Att.VERACC, "vertical_accuracy"); AttStr.put(Att.VERCLR, "clearance_height");
  AttStr.put(Att.VERCCL, "clearance_height_closed"); AttStr.put(Att.VERCOP, "clearance_height_open"); AttStr.put(Att.VERCSA, "clearance_height_safe");
  AttStr.put(Att.VERDAT, "vertical_datum"); AttStr.put(Att.VERLEN, "vertical_length"); AttStr.put(Att.WATLEV, "water_level"); AttStr.put(Att.CAT_TS, "category");
  AttStr.put(Att.NINFOM, "national_information"); AttStr.put(Att.NOBJNM, "national_name"); AttStr.put(Att.NPLDST, "national_pilot_district");
  AttStr.put(Att.NTXTDS, "national_description"); AttStr.put(Att.HORDAT, "horizontal_datum"); AttStr.put(Att.POSACC, "positional_accuracy");
  AttStr.put(Att.QUAPOS, "position_quality"); AttStr.put(Att.ADDMRK, "addition"); AttStr.put(Att.BNKWTW, "bank"); AttStr.put(Att.CATNMK, "category");
  AttStr.put(Att.CLSDNG, "danger_class"); AttStr.put(Att.DIRIMP, "impact"); AttStr.put(Att.DISBK1, "distance_start"); AttStr.put(Att.DISBK2, "distance_end");
  AttStr.put(Att.DISIPU, "distance_up"); AttStr.put(Att.DISIPD, "distance_down"); AttStr.put(Att.ELEVA1, "minimum_elevation");
  AttStr.put(Att.ELEVA2, "maximum_elevation"); AttStr.put(Att.FNCTNM, "function"); AttStr.put(Att.WTWDIS, "distance"); AttStr.put(Att.BUNVES, "availability");
  AttStr.put(Att.CATBRT, "category"); AttStr.put(Att.CATBUN, "category"); AttStr.put(Att.CATCCL, "category"); AttStr.put(Att.CATHBR, "category");
  AttStr.put(Att.CATRFD, "category"); AttStr.put(Att.CATTML, "category"); AttStr.put(Att.COMCTN, "communication"); AttStr.put(Att.HORCLL, "horizontal_clearance_length");
  AttStr.put(Att.HORCLW, "horizontal_clearance_width"); AttStr.put(Att.TRSHGD, "goods"); AttStr.put(Att.UNLOCD, ""); AttStr.put(Att.CATGAG, "category");
  AttStr.put(Att.HIGWAT, "high_value"); AttStr.put(Att.HIGNAM, "high_name"); AttStr.put(Att.LOWWAT, "low_value"); AttStr.put(Att.LOWNAM, "low_name");
  AttStr.put(Att.MEAWAT, "mean_value"); AttStr.put(Att.MEANAM, "mean_name"); AttStr.put(Att.OTHWAT, "local_value"); AttStr.put(Att.OTHNAM, "local_name");
  AttStr.put(Att.REFLEV, "gravity_reference"); AttStr.put(Att.SDRLEV, "sounding_name"); AttStr.put(Att.VCRLEV, "vertical_name"); AttStr.put(Att.CATVTR, "category");
  AttStr.put(Att.CATTAB, "operation"); AttStr.put(Att.SCHREF, "schedule"); AttStr.put(Att.USESHP, "use"); AttStr.put(Att.CURVHW, "high_velocity");
  AttStr.put(Att.CURVLW, "low_velocity"); AttStr.put(Att.CURVMW, "mean_velocity"); AttStr.put(Att.CURVOW, "other_velocity"); AttStr.put(Att.APTREF, "passing_time");
  AttStr.put(Att.CATCOM, "category"); AttStr.put(Att.CATCVR, "category"); AttStr.put(Att.CATEXS, "category"); AttStr.put(Att.SHPTYP, "ship");
  AttStr.put(Att.UPDMSG, "message"); AttStr.put(Att.LITRAD, "radius");
 }
 private static final EnumMap<Obj, Att> Accuracy = new EnumMap<>(Obj.class); static { Accuracy.put(Obj.UNKOBJ, Att.HORACC); }
 private static final EnumMap<Obj, Att> Addition = new EnumMap<>(Obj.class); static { Addition.put(Obj.UNKOBJ, Att.ADDMRK); }
 private static final EnumMap<Obj, Att> Agency = new EnumMap<>(Obj.class); static { Agency.put(Obj.UNKOBJ, Att.AGENCY); }
 private static final EnumMap<Obj, Att> Anomaly = new EnumMap<>(Obj.class); static { Anomaly.put(Obj.UNKOBJ, Att.VALLMA); }
 private static final EnumMap<Obj, Att> Authority = new EnumMap<>(Obj.class); static { Authority.put(Obj.UNKOBJ, Att.SURATH); }
 private static final EnumMap<Obj, Att> Availability = new EnumMap<>(Obj.class); static { Availability.put(Obj.UNKOBJ, Att.BUNVES); }
 private static final EnumMap<Obj, Att> Bank = new EnumMap<>(Obj.class); static { Bank.put(Obj.UNKOBJ, Att.BNKWTW); }
 private static final EnumMap<Obj, Att> Callsign = new EnumMap<>(Obj.class); static { Callsign.put(Obj.UNKOBJ, Att.CALSGN); }
 private static final EnumMap<Obj, Att> Category = new EnumMap<>(Obj.class); static {
  Category.put(Obj.ACHARE, Att.CATACH); Category.put(Obj.ACHBRT, Att.CATACH); Category.put(Obj.AIRARE, Att.CATAIR); Category.put(Obj.BCNCAR, Att.CATCAM); Category.put(Obj.BCNLAT, Att.CATLAM);
  Category.put(Obj.BCNSPP, Att.CATSPM); Category.put(Obj.BOYLAT, Att.CATLAM); Category.put(Obj.BOYINB, Att.CATINB); Category.put(Obj.BOYSPP, Att.CATSPM); Category.put(Obj.DAYMAR, Att.CATSPM);
  Category.put(Obj.BRIDGE, Att.CATBRG); Category.put(Obj.BUAARE, Att.CATBUA); Category.put(Obj.BUNSTA, Att.CATBUN); Category.put(Obj.CANALS, Att.CATCAN);
  Category.put(Obj.CBLARE, Att.CATCBL); Category.put(Obj.CBLOHD, Att.CATCBL); Category.put(Obj.CBLSUB, Att.CATCBL); Category.put(Obj.CHKPNT, Att.CATCHP); Category.put(Obj.COMARE, Att.CATCOM);
  Category.put(Obj.COALNE, Att.CATCOA); Category.put(Obj.CONVYR, Att.CATCON); Category.put(Obj.CRANES, Att.CATCRN); Category.put(Obj.CTRPNT, Att.CATCTR); Category.put(Obj.DAMCON, Att.CATDAM);
  Category.put(Obj.DISMAR, Att.CATDIS); Category.put(Obj.DMPGRD, Att.CATDPG); Category.put(Obj.DOCARE, Att.CATDOC); Category.put(Obj.EXCNST, Att.CATEXS); Category.put(Obj.FERYRT, Att.CATFRY);
  Category.put(Obj.FNCLNE, Att.CATFNC); Category.put(Obj.FOGSIG, Att.CATFOG); Category.put(Obj.FORSTC, Att.CATFOR); Category.put(Obj.FSHFAC, Att.CATFIF); Category.put(Obj.GATCON, Att.CATGAT);
  Category.put(Obj.HRBFAC, Att.CATHAF); Category.put(Obj.HRBARE, Att.CATHBR); Category.put(Obj.HRBBSN, Att.CATHBR); Category.put(Obj.HULKES, Att.CATHLK); Category.put(Obj.ICEARE, Att.CATICE);
  Category.put(Obj.LNDRGN, Att.CATLND); Category.put(Obj.LNDMRK, Att.CATLMK); Category.put(Obj.LIGHTS, Att.CATLIT); Category.put(Obj.M_COVR, Att.CATCVR); Category.put(Obj.MARCUL, Att.CATMFA);
  Category.put(Obj.MIPARE, Att.CATMPA); Category.put(Obj.MORFAC, Att.CATMOR); Category.put(Obj.NAVLNE, Att.CATNAV); Category.put(Obj.NOTMRK, Att.CATNMK); Category.put(Obj.OBSTRN, Att.CATOBS);
  Category.put(Obj.OFSPLF, Att.CATOFP); Category.put(Obj.OILBAR, Att.CATOLB); Category.put(Obj.OSPARE, Att.CATPRA); Category.put(Obj.PILPNT, Att.CATPLE); Category.put(Obj.PILBOP, Att.CATPIL);
  Category.put(Obj.PIPARE, Att.CATPIP); Category.put(Obj.PIPOHD, Att.CATPIP); Category.put(Obj.PIPSOL, Att.CATPIP); Category.put(Obj.PRDARE, Att.CATPRA); Category.put(Obj.PYLONS, Att.CATPYL);
  Category.put(Obj.RADSTA, Att.CATRAS); Category.put(Obj.RCRTCL, Att.CATTRK); Category.put(Obj.RCTLPT, Att.CATTRK); Category.put(Obj.RDOSTA, Att.CATROS); Category.put(Obj.RDOCAL, Att.CATCOM);
  Category.put(Obj.RECTRC, Att.CATTRK); Category.put(Obj.REFDMP, Att.CATRFD); Category.put(Obj.RESARE, Att.CATREA); Category.put(Obj.RSCSTA, Att.CATRSC);
  Category.put(Obj.RTPBCN, Att.CATRTB); Category.put(Obj.ROADWY, Att.CATROD); Category.put(Obj.RUNWAY, Att.CATRUN); Category.put(Obj.SEAARE, Att.CATSEA); Category.put(Obj.SILTNK, Att.CATSIL);
  Category.put(Obj.SISTAT, Att.CATSIT); Category.put(Obj.SISTAW, Att.CATSIW); Category.put(Obj.SLCONS, Att.CATSLC); Category.put(Obj.SLOTOP, Att.CATSLO); Category.put(Obj.SLOGRD, Att.CATSLO);
  Category.put(Obj.SMCFAC, Att.CATSCF); Category.put(Obj.TERMNL, Att.CATTML); Category.put(Obj.TS_FEB, Att.CAT_TS); Category.put(Obj.TSELNE, Att.CATTSS); Category.put(Obj.TSEZNE, Att.CATTSS);
  Category.put(Obj.TSSBND, Att.CATTSS); Category.put(Obj.TSSCRS, Att.CATTSS); Category.put(Obj.TSSLPT, Att.CATTSS); Category.put(Obj.TSSRON, Att.CATTSS); Category.put(Obj.TWRTPT, Att.CATTRK);
  Category.put(Obj.VEGATN, Att.CATVEG); Category.put(Obj.VEHTRF, Att.CATVTR); Category.put(Obj.WATTUR, Att.CATWAT); Category.put(Obj.WEDKLP, Att.CATWED); Category.put(Obj.WRECKS, Att.CATWRK);
  Category.put(Obj.WTWAXS, Att.CATCCL); Category.put(Obj.WTWARE, Att.CATCCL); Category.put(Obj.WTWGAG, Att.CATGAG); Category.put(Obj.BERTHS, Att.CATBRT);
 }
 
 private static final EnumMap<Obj, Att> Channel = new EnumMap<>(Obj.class); static { Channel.put(Obj.UNKOBJ, Att.COMCHA); }
 private static final EnumMap<Obj, Att> Character = new EnumMap<>(Obj.class); static { Character.put(Obj.UNKOBJ, Att.LITCHR); }
 private static final EnumMap<Obj, Att> Clearance_height = new EnumMap<>(Obj.class); static { Clearance_height.put(Obj.UNKOBJ, Att.VERCLR); }
 private static final EnumMap<Obj, Att> Clearance_height_closed = new EnumMap<>(Obj.class); static { Clearance_height_closed.put(Obj.UNKOBJ, Att.VERCCL); }
 private static final EnumMap<Obj, Att> Clearance_height_open = new EnumMap<>(Obj.class); static { Clearance_height_open.put(Obj.UNKOBJ, Att.VERCOP); }
 private static final EnumMap<Obj, Att> Clearance_height_safe = new EnumMap<>(Obj.class); static { Clearance_height_safe.put(Obj.UNKOBJ, Att.VERCSA); }
 private static final EnumMap<Obj, Att> Clearance_width = new EnumMap<>(Obj.class); static { Clearance_width.put(Obj.UNKOBJ, Att.HORCLR); }
 private static final EnumMap<Obj, Att> Colour = new EnumMap<>(Obj.class); static { Colour.put(Obj.UNKOBJ, Att.COLOUR); }
 private static final EnumMap<Obj, Att> Colour_pattern = new EnumMap<>(Obj.class); static { Colour_pattern.put(Obj.UNKOBJ, Att.COLPAT); }
 private static final EnumMap<Obj, Att> Communication = new EnumMap<>(Obj.class); static { Communication.put(Obj.UNKOBJ, Att.COMCTN); }
 private static final EnumMap<Obj, Att> Condition = new EnumMap<>(Obj.class); static { Condition.put(Obj.UNKOBJ, Att.CONDTN); }
 private static final EnumMap<Obj, Att> Conspicuity = new EnumMap<>(Obj.class); static { Conspicuity.put(Obj.UNKOBJ, Att.CONVIS); }
 private static final EnumMap<Obj, Att> Construction = new EnumMap<>(Obj.class); static { Construction.put(Obj.UNKOBJ, Att.NATCON); }
 private static final EnumMap<Obj, Att> Danger_class = new EnumMap<>(Obj.class); static { Danger_class.put(Obj.UNKOBJ, Att.CLSDNG); }
 private static final EnumMap<Obj, Att> Depth = new EnumMap<>(Obj.class); static { Depth.put(Obj.UNKOBJ, Att.VALDCO); Depth.put(Obj.SOUNDG, Att.VALSOU); }
 private static final EnumMap<Obj, Att> Depth_buried = new EnumMap<>(Obj.class); static { Depth_buried.put(Obj.UNKOBJ, Att.BURDEP); }
 private static final EnumMap<Obj, Att> Description = new EnumMap<>(Obj.class); static { Description.put(Obj.UNKOBJ, Att.TXTDSC); }
 private static final EnumMap<Obj, Att> Distance = new EnumMap<>(Obj.class); static { Distance.put(Obj.UNKOBJ, Att.WTWDIS); }
 private static final EnumMap<Obj, Att> Distance_down = new EnumMap<>(Obj.class); static { Distance_down.put(Obj.UNKOBJ, Att.DISIPD); }
 private static final EnumMap<Obj, Att> Distance_end = new EnumMap<>(Obj.class); static { Distance_end.put(Obj.UNKOBJ, Att.DISBK2); }
 private static final EnumMap<Obj, Att> Distance_start = new EnumMap<>(Obj.class); static { Distance_start.put(Obj.UNKOBJ, Att.DISBK1); }
 private static final EnumMap<Obj, Att> Distance_up = new EnumMap<>(Obj.class); static { Distance_up.put(Obj.UNKOBJ, Att.DISIPU); }
 private static final EnumMap<Obj, Att> Elevation = new EnumMap<>(Obj.class); static { Elevation.put(Obj.UNKOBJ, Att.ELEVAT); }
 private static final EnumMap<Obj, Att> End_date = new EnumMap<>(Obj.class); static { End_date.put(Obj.UNKOBJ, Att.DATEND); }
 private static final EnumMap<Obj, Att> End_time = new EnumMap<>(Obj.class); static { End_time.put(Obj.UNKOBJ, Att.TIMEND); }
 private static final EnumMap<Obj, Att> Estimated_range = new EnumMap<>(Obj.class); static { Estimated_range.put(Obj.UNKOBJ, Att.ESTRNG); }
 private static final EnumMap<Obj, Att> Exhibition = new EnumMap<>(Obj.class); static { Exhibition.put(Obj.UNKOBJ, Att.EXCLIT); }
 private static final EnumMap<Obj, Att> Exposition = new EnumMap<>(Obj.class); static { Exposition.put(Obj.UNKOBJ, Att.EXPSOU); }
 private static final EnumMap<Obj, Att> Factor = new EnumMap<>(Obj.class); static { Factor.put(Obj.UNKOBJ, Att.ICEFAC); }
 private static final EnumMap<Obj, Att> Frequency = new EnumMap<>(Obj.class); static { Frequency.put(Obj.UNKOBJ, Att.SIGFRQ); }
 private static final EnumMap<Obj, Att> Function = new EnumMap<>(Obj.class); static { Function.put(Obj.BUISGL, Att.FUNCTN); Function.put(Obj.LNDMRK, Att.FUNCTN); Function.put(Obj.NOTMRK, Att.FNCTNM); }
 private static final EnumMap<Obj, Att> Generation = new EnumMap<>(Obj.class); static { Generation.put(Obj.UNKOBJ, Att.SIGGEN); }
 private static final EnumMap<Obj, Att> Goods = new EnumMap<>(Obj.class); static { Goods.put(Obj.UNKOBJ, Att.TRSHGD); }
 private static final EnumMap<Obj, Att> Gravity_reference = new EnumMap<>(Obj.class); static { Gravity_reference.put(Obj.UNKOBJ, Att.REFLEV); }
 private static final EnumMap<Obj, Att> Group = new EnumMap<>(Obj.class); static { Group.put(Obj.UNKOBJ, Att.SIGGRP); }
 private static final EnumMap<Obj, Att> Height = new EnumMap<>(Obj.class); static { Height.put(Obj.UNKOBJ, Att.HEIGHT); }
 private static final EnumMap<Obj, Att> High_name = new EnumMap<>(Obj.class); static { High_name.put(Obj.UNKOBJ, Att.HIGNAM); }
 private static final EnumMap<Obj, Att> High_value = new EnumMap<>(Obj.class); static { High_value.put(Obj.UNKOBJ, Att.HIGWAT); }
 private static final EnumMap<Obj, Att> High_velocity = new EnumMap<>(Obj.class); static { High_velocity.put(Obj.UNKOBJ, Att.CURVHW); }
 private static final EnumMap<Obj, Att> Horizontal_clearance_length = new EnumMap<>(Obj.class); static { Horizontal_clearance_length.put(Obj.UNKOBJ, Att.HORCLL); }
 private static final EnumMap<Obj, Att> Horizontal_clearance_width = new EnumMap<>(Obj.class); static { Horizontal_clearance_width.put(Obj.UNKOBJ, Att.HORCLW); }
 private static final EnumMap<Obj, Att> Horizontal_datum = new EnumMap<>(Obj.class); static { Horizontal_datum.put(Obj.UNKOBJ, Att.HORDAT); }
 private static final EnumMap<Obj, Att> Impact = new EnumMap<>(Obj.class); static { Impact.put(Obj.UNKOBJ, Att.DIRIMP); }
 private static final EnumMap<Obj, Att> Information = new EnumMap<>(Obj.class); static { Information.put(Obj.UNKOBJ, Att.INFORM); }
 private static final EnumMap<Obj, Att> Jurisdiction = new EnumMap<>(Obj.class); static { Jurisdiction.put(Obj.UNKOBJ, Att.JRSDTN); }
 private static final EnumMap<Obj, Att> Length = new EnumMap<>(Obj.class); static { Length.put(Obj.UNKOBJ, Att.HORLEN); }
 private static final EnumMap<Obj, Att> Local_name = new EnumMap<>(Obj.class); static { Local_name.put(Obj.UNKOBJ, Att.OTHNAM); }
 private static final EnumMap<Obj, Att> Local_value = new EnumMap<>(Obj.class); static { Local_value.put(Obj.UNKOBJ, Att.OTHWAT); }
 private static final EnumMap<Obj, Att> Low_name = new EnumMap<>(Obj.class); static { Low_name.put(Obj.UNKOBJ, Att.LOWNAM); }
 private static final EnumMap<Obj, Att> Low_value = new EnumMap<>(Obj.class); static { Low_value.put(Obj.UNKOBJ, Att.LOWWAT); }
 private static final EnumMap<Obj, Att> Low_velocity = new EnumMap<>(Obj.class); static { Low_velocity.put(Obj.UNKOBJ, Att.CURVLW); }
 private static final EnumMap<Obj, Att> Maximum_depth = new EnumMap<>(Obj.class); static { Maximum_depth.put(Obj.UNKOBJ, Att.DRVAL2); }
 private static final EnumMap<Obj, Att> Maximum_elevation = new EnumMap<>(Obj.class); static { Maximum_elevation.put(Obj.UNKOBJ, Att.ELEVA2); }
 private static final EnumMap<Obj, Att> Maximum_load = new EnumMap<>(Obj.class); static { Maximum_load.put(Obj.UNKOBJ, Att.LIFCAP); }
 private static final EnumMap<Obj, Att> Maximum_range = new EnumMap<>(Obj.class); static { Maximum_range.put(Obj.UNKOBJ, Att.VALMXR); }
 private static final EnumMap<Obj, Att> Maximum_sounding = new EnumMap<>(Obj.class); static { Maximum_sounding.put(Obj.UNKOBJ, Att.SDISMX); }
 private static final EnumMap<Obj, Att> Mean_name = new EnumMap<>(Obj.class); static { Mean_name.put(Obj.UNKOBJ, Att.MEANAM); }
 private static final EnumMap<Obj, Att> Mean_value = new EnumMap<>(Obj.class); static { Mean_value.put(Obj.UNKOBJ, Att.MEAWAT); }
 private static final EnumMap<Obj, Att> Mean_velocity = new EnumMap<>(Obj.class); static { Mean_velocity.put(Obj.UNKOBJ, Att.CURVMW); }
 private static final EnumMap<Obj, Att> Message = new EnumMap<>(Obj.class); static { Message.put(Obj.UNKOBJ, Att.UPDMSG); }
 private static final EnumMap<Obj, Att> Minimum_depth = new EnumMap<>(Obj.class); static { Minimum_depth.put(Obj.UNKOBJ, Att.DRVAL1); }
 private static final EnumMap<Obj, Att> Minimum_elevation = new EnumMap<>(Obj.class); static { Minimum_elevation.put(Obj.UNKOBJ, Att.ELEVA1); }
 private static final EnumMap<Obj, Att> Minimum_sounding = new EnumMap<>(Obj.class); static { Minimum_sounding.put(Obj.UNKOBJ, Att.SDISMN); }
 private static final EnumMap<Obj, Att> Multiple = new EnumMap<>(Obj.class); static { Multiple.put(Obj.UNKOBJ, Att.MLTYLT); }
 private static final EnumMap<Obj, Att> Name = new EnumMap<>(Obj.class); static { Name.put(Obj.UNKOBJ, Att.OBJNAM); }
 private static final EnumMap<Obj, Att> National_information = new EnumMap<>(Obj.class); static { National_information.put(Obj.UNKOBJ, Att.NINFOM); }
 private static final EnumMap<Obj, Att> Nationality = new EnumMap<>(Obj.class); static { Nationality.put(Obj.UNKOBJ, Att.NATION); }
 private static final EnumMap<Obj, Att> National_description = new EnumMap<>(Obj.class); static { National_description.put(Obj.UNKOBJ, Att.NTXTDS); }
 private static final EnumMap<Obj, Att> National_name = new EnumMap<>(Obj.class); static { National_name.put(Obj.UNKOBJ, Att.NOBJNM); }
 private static final EnumMap<Obj, Att> National_pilot_district = new EnumMap<>(Obj.class); static { National_pilot_district.put(Obj.UNKOBJ, Att.NPLDST); }
 private static final EnumMap<Obj, Att> Nm_date = new EnumMap<>(Obj.class); static { Nm_date.put(Obj.UNKOBJ, Att.NMDATE); }
 private static final EnumMap<Obj, Att> Other_velocity = new EnumMap<>(Obj.class); static { Other_velocity.put(Obj.UNKOBJ, Att.CURVOW); }
 private static final EnumMap<Obj, Att> Operation = new EnumMap<>(Obj.class); static { Operation.put(Obj.UNKOBJ, Att.CATTAB); }
 private static final EnumMap<Obj, Att> Orientation = new EnumMap<>(Obj.class); static { Orientation.put(Obj.UNKOBJ, Att.ORIENT); }
 private static final EnumMap<Obj, Att> Passing_time = new EnumMap<>(Obj.class); static { Passing_time.put(Obj.UNKOBJ, Att.APTREF); }
 private static final EnumMap<Obj, Att> Period = new EnumMap<>(Obj.class); static { Period.put(Obj.UNKOBJ, Att.SIGPER); }
 private static final EnumMap<Obj, Att> Period_end = new EnumMap<>(Obj.class); static { Period_end.put(Obj.UNKOBJ, Att.PEREND); }
 private static final EnumMap<Obj, Att> Period_start = new EnumMap<>(Obj.class); static { Period_start.put(Obj.UNKOBJ, Att.PERSTA); }
 private static final EnumMap<Obj, Att> Pilot_district = new EnumMap<>(Obj.class); static { Pilot_district.put(Obj.UNKOBJ, Att.PILDST); }
 private static final EnumMap<Obj, Att> Position_quality = new EnumMap<>(Obj.class); static { Position_quality.put(Obj.UNKOBJ, Att.QUAPOS); }
 private static final EnumMap<Obj, Att> Positional_accuracy = new EnumMap<>(Obj.class); static { Positional_accuracy.put(Obj.UNKOBJ, Att.POSACC); }
 private static final EnumMap<Obj, Att> Producing_country = new EnumMap<>(Obj.class); static { Producing_country.put(Obj.UNKOBJ, Att.PRCTRY); }
 private static final EnumMap<Obj, Att> Product = new EnumMap<>(Obj.class); static { Product.put(Obj.UNKOBJ, Att.PRODCT); }
 private static final EnumMap<Obj, Att> Quality = new EnumMap<>(Obj.class); static { Quality.put(Obj.UNKOBJ, Att.QUASOU); }
 private static final EnumMap<Obj, Att> Radius = new EnumMap<>(Obj.class); static { Radius.put(Obj.UNKOBJ, Att.RADIUS); Radius.put(Obj.LIGHTS, Att.LITRAD); }
 private static final EnumMap<Obj, Att> Range = new EnumMap<>(Obj.class); static { Range.put(Obj.UNKOBJ, Att.VALNMR); }
 private static final EnumMap<Obj, Att> Reference = new EnumMap<>(Obj.class); static { Reference.put(Obj.UNKOBJ, Att.PUBREF); }
 private static final EnumMap<Obj, Att> Reflectivity = new EnumMap<>(Obj.class); static { Reflectivity.put(Obj.UNKOBJ, Att.CONRAD); }
 private static final EnumMap<Obj, Att> Restriction = new EnumMap<>(Obj.class); static { Restriction.put(Obj.UNKOBJ, Att.RESTRN); }
 private static final EnumMap<Obj, Att> Schedule = new EnumMap<>(Obj.class); static { Schedule.put(Obj.UNKOBJ, Att.SCHREF); }
 private static final EnumMap<Obj, Att> Shape = new EnumMap<>(Obj.class); static { Shape.put(Obj.BCNCAR, Att.BCNSHP); Shape.put(Obj.BCNISD, Att.BCNSHP);
  Shape.put(Obj.BCNLAT, Att.BCNSHP); Shape.put(Obj.BCNSAW, Att.BCNSHP); Shape.put(Obj.BCNSPP, Att.BCNSHP); Shape.put(Obj.BUISGL, Att.BUISHP);
  Shape.put(Obj.BOYCAR, Att.BOYSHP); Shape.put(Obj.BOYISD, Att.BOYSHP); Shape.put(Obj.BOYLAT, Att.BOYSHP); Shape.put(Obj.BOYSAW, Att.BOYSHP); Shape.put(Obj.BOYSPP, Att.BOYSHP);
  Shape.put(Obj.BOYINB, Att.BOYSHP); Shape.put(Obj.DAYMAR, Att.TOPSHP); Shape.put(Obj.TOPMAR, Att.TOPSHP); Shape.put(Obj.MORFAC, Att.BOYSHP);
  Shape.put(Obj.SILTNK, Att.BUISHP);
 }
 private static final EnumMap<Obj, Att> Sector_end = new EnumMap<>(Obj.class); static { Sector_end.put(Obj.UNKOBJ, Att.SECTR2); }
 private static final EnumMap<Obj, Att> Sector_start = new EnumMap<>(Obj.class); static { Sector_start.put(Obj.UNKOBJ, Att.SECTR1); }
 private static final EnumMap<Obj, Att> Sequence = new EnumMap<>(Obj.class); static { Sequence.put(Obj.UNKOBJ, Att.SIGSEQ); }
 private static final EnumMap<Obj, Att> Shift = new EnumMap<>(Obj.class); static { Shift.put(Obj.UNKOBJ, Att.SHIPAM); }
 private static final EnumMap<Obj, Att> Ship = new EnumMap<>(Obj.class); static { Ship.put(Obj.UNKOBJ, Att.SHPTYP); }
 private static final EnumMap<Obj, Att> Sounding_accuracy = new EnumMap<>(Obj.class); static { Sounding_accuracy.put(Obj.UNKOBJ, Att.SOUACC); }
 private static final EnumMap<Obj, Att> Sounding_name = new EnumMap<>(Obj.class); static { Sounding_name.put(Obj.UNKOBJ, Att.SDRLEV); }
 private static final EnumMap<Obj, Att> Start_date = new EnumMap<>(Obj.class); static { Start_date.put(Obj.UNKOBJ, Att.DATSTA); }
 private static final EnumMap<Obj, Att> Start_time = new EnumMap<>(Obj.class); static { Start_time.put(Obj.UNKOBJ, Att.TIMSTA); }
 private static final EnumMap<Obj, Att> Status = new EnumMap<>(Obj.class); static { Status.put(Obj.UNKOBJ, Att.STATUS); }
 private static final EnumMap<Obj, Att> Surface = new EnumMap<>(Obj.class); static { Surface.put(Obj.UNKOBJ, Att.NATSUR); }
 private static final EnumMap<Obj, Att> Surface_qualification = new EnumMap<>(Obj.class); static { Surface_qualification.put(Obj.UNKOBJ, Att.NATQUA); }
 private static final EnumMap<Obj, Att> Survey = new EnumMap<>(Obj.class); static { Survey.put(Obj.UNKOBJ, Att.SURTYP); }
 private static final EnumMap<Obj, Att> Survey_end = new EnumMap<>(Obj.class); static { Survey_end.put(Obj.UNKOBJ, Att.SUREND); }
 private static final EnumMap<Obj, Att> Survey_start = new EnumMap<>(Obj.class); static { Survey_start.put(Obj.UNKOBJ, Att.SURSTA); }
 private static final EnumMap<Obj, Att> System = new EnumMap<>(Obj.class); static { System.put(Obj.UNKOBJ, Att.MARSYS); }
 private static final EnumMap<Obj, Att> Technique = new EnumMap<>(Obj.class); static { Technique.put(Obj.UNKOBJ, Att.TECSOU); }
 private static final EnumMap<Obj, Att> Traffic_flow = new EnumMap<>(Obj.class); static { Traffic_flow.put(Obj.UNKOBJ, Att.TRAFIC); }
 private static final EnumMap<Obj, Att> Units = new EnumMap<>(Obj.class); static { Units.put(Obj.UNKOBJ, Att.HUNITS); }
 private static final EnumMap<Obj, Att> Use = new EnumMap<>(Obj.class); static { Use.put(Obj.UNKOBJ, Att.USESHP); }
 private static final EnumMap<Obj, Att> Variation = new EnumMap<>(Obj.class); static { Variation.put(Obj.UNKOBJ, Att.VALMAG); }
 private static final EnumMap<Obj, Att> Variation_change = new EnumMap<>(Obj.class); static { Variation_change.put(Obj.UNKOBJ, Att.VALACM); }
 private static final EnumMap<Obj, Att> Velocity = new EnumMap<>(Obj.class); static { Velocity.put(Obj.UNKOBJ, Att.CURVEL); }
 private static final EnumMap<Obj, Att> Vertical_accuracy = new EnumMap<>(Obj.class); static { Vertical_accuracy.put(Obj.UNKOBJ, Att.VERACC); }
 private static final EnumMap<Obj, Att> Vertical_datum = new EnumMap<>(Obj.class); static { Vertical_datum.put(Obj.UNKOBJ, Att.VERDAT); }
 private static final EnumMap<Obj, Att> Vertical_length = new EnumMap<>(Obj.class); static { Vertical_length.put(Obj.UNKOBJ, Att.VERLEN); }
 private static final EnumMap<Obj, Att> Vertical_name = new EnumMap<>(Obj.class); static { Vertical_name.put(Obj.UNKOBJ, Att.VCRLEV); }
 private static final EnumMap<Obj, Att> Visibility = new EnumMap<>(Obj.class); static { Visibility.put(Obj.UNKOBJ, Att.LITVIS); }
 private static final EnumMap<Obj, Att> Water_level = new EnumMap<>(Obj.class); static { Water_level.put(Obj.UNKOBJ, Att.WATLEV); }
 private static final EnumMap<Obj, Att> Wavelength = new EnumMap<>(Obj.class); static { Wavelength.put(Obj.UNKOBJ, Att.RADWAL); }
 private static final EnumMap<Obj, Att> Width = new EnumMap<>(Obj.class); static { Width.put(Obj.UNKOBJ, Att.HORWID); }
 private static final EnumMap<Obj, Att> Year = new EnumMap<>(Obj.class); static { Year.put(Obj.UNKOBJ, Att.RYRMGV); }
 
 private static final HashMap<String, EnumMap<Obj, Att>> StrAtt = new HashMap<String, EnumMap<Obj, Att>>();
 static {
  StrAtt.put("accuracy", Accuracy); StrAtt.put("addition", Addition); StrAtt.put("agency", Agency); StrAtt.put("anomaly", Anomaly); StrAtt.put("authority", Authority);
  StrAtt.put("availability", Availability); StrAtt.put("bank", Bank); StrAtt.put("callsign", Callsign); StrAtt.put("category", Category); StrAtt.put("channel", Channel);
  StrAtt.put("character", Character); StrAtt.put("clearance_height", Clearance_height); StrAtt.put("clearance_height_closed", Clearance_height_closed);
  StrAtt.put("clearance_height_open", Clearance_height_open); StrAtt.put("clearance_height_safe", Clearance_height_safe); StrAtt.put("clearance_width", Clearance_width);
  StrAtt.put("colour", Colour); StrAtt.put("colour_pattern", Colour_pattern); StrAtt.put("communication", Communication); StrAtt.put("condition", Condition);
  StrAtt.put("conspicuity", Conspicuity); StrAtt.put("construction", Construction); StrAtt.put("danger_class", Danger_class); StrAtt.put("depth", Depth);
  StrAtt.put("depth_buried", Depth_buried); StrAtt.put("description", Description); StrAtt.put("distance", Distance); StrAtt.put("distance_down", Distance_down);
  StrAtt.put("distance_end", Distance_end); StrAtt.put("distance_start", Distance_start); StrAtt.put("distance_up", Distance_up); StrAtt.put("elevation", Elevation);
  StrAtt.put("end_date", End_date); StrAtt.put("end_time", End_time); StrAtt.put("estimated_range", Estimated_range); StrAtt.put("exhibition", Exhibition);
  StrAtt.put("exposition", Exposition); StrAtt.put("factor", Factor); StrAtt.put("frequency", Frequency); StrAtt.put("function", Function);
  StrAtt.put("generation", Generation); StrAtt.put("goods", Goods); StrAtt.put("gravity_reference", Gravity_reference); StrAtt.put("group", Group);
  StrAtt.put("height", Height); StrAtt.put("high_name", High_name); StrAtt.put("high_value", High_value); StrAtt.put("high_velocity", High_velocity);
  StrAtt.put("horizontal_clearance_length", Horizontal_clearance_length); StrAtt.put("horizontal_clearance_width", Horizontal_clearance_width);
  StrAtt.put("horizontal_datum", Horizontal_datum); StrAtt.put("impact", Impact); StrAtt.put("information", Information); StrAtt.put("jurisdiction", Jurisdiction);
  StrAtt.put("length", Length); StrAtt.put("local_name", Local_name); StrAtt.put("local_value", Local_value); StrAtt.put("low_name", Low_name);
  StrAtt.put("low_value", Low_value); StrAtt.put("low_velocity", Low_velocity); StrAtt.put("maximum_depth", Maximum_depth); StrAtt.put("maximum_elevation", Maximum_elevation);
  StrAtt.put("maximum_load", Maximum_load); StrAtt.put("maximum_range", Maximum_range); StrAtt.put("maximum_sounding", Maximum_sounding); StrAtt.put("mean_name", Mean_name);
  StrAtt.put("mean_value", Mean_value); StrAtt.put("mean_velocity", Mean_velocity); StrAtt.put("message", Message); StrAtt.put("minimum_depth", Minimum_depth);
  StrAtt.put("minimum_elevation", Minimum_elevation); StrAtt.put("minimum_sounding", Minimum_sounding); StrAtt.put("multiple", Multiple); StrAtt.put("name", Name);
  StrAtt.put("national_information", National_information); StrAtt.put("nationality", Nationality); StrAtt.put("national_description", National_description);
  StrAtt.put("national_name", National_name); StrAtt.put("national_pilot_district", National_pilot_district); StrAtt.put("nm_date", Nm_date); StrAtt.put("other_velocity", Other_velocity);
  StrAtt.put("operation", Operation); StrAtt.put("orientation", Orientation); StrAtt.put("passing_time", Passing_time); StrAtt.put("period", Period); StrAtt.put("period_end", Period_end);
  StrAtt.put("period_start", Period_start); StrAtt.put("pilot_district", Pilot_district); StrAtt.put("position_quality", Position_quality); StrAtt.put("positional_accuracy", Positional_accuracy);
  StrAtt.put("producing_country", Producing_country); StrAtt.put("product", Product); StrAtt.put("quality", Quality); StrAtt.put("radius", Radius); StrAtt.put("range", Range);
  StrAtt.put("reference", Reference); StrAtt.put("reflectivity", Reflectivity); StrAtt.put("restriction", Restriction); StrAtt.put("schedule", Schedule); StrAtt.put("shape", Shape);
  StrAtt.put("sector_end", Sector_end); StrAtt.put("sector_start", Sector_start); StrAtt.put("sequence", Sequence); StrAtt.put("shift", Shift); StrAtt.put("ship", Ship);
  StrAtt.put("sounding_accuracy", Sounding_accuracy); StrAtt.put("sounding_name", Sounding_name); StrAtt.put("start_date", Start_date); StrAtt.put("start_time", Start_time);
  StrAtt.put("status", Status); StrAtt.put("surface", Surface); StrAtt.put("surface_qualification", Surface_qualification); StrAtt.put("survey", Survey);
  StrAtt.put("survey_end", Survey_end); StrAtt.put("survey_start", Survey_start); StrAtt.put("system", System); StrAtt.put("technique", Technique); StrAtt.put("traffic_flow", Traffic_flow);
  StrAtt.put("units", Units); StrAtt.put("use", Use); StrAtt.put("variation", Variation); StrAtt.put("variation_change", Variation_change); StrAtt.put("velocity", Velocity);
  StrAtt.put("vertical_accuracy", Vertical_accuracy); StrAtt.put("vertical_datum", Vertical_datum); StrAtt.put("vertical_length", Vertical_length); StrAtt.put("vertical_name", Vertical_name);
  StrAtt.put("visibility", Visibility); StrAtt.put("water_level", Water_level); StrAtt.put("wavelength", Wavelength); StrAtt.put("width", Width); StrAtt.put("year", Year);
 }

 public static Att decodeAttribute(long attl) { // Convert S57 attribute code to SCM attribute enumeration
		for (Att att : AttS57.keySet()) {
			if (AttS57.get(att) == attl) return att;
		}
		for (Att att : AttIENC.keySet()) {
			if (AttIENC.get(att) == attl) return att;
		}
  return Att.UNKATT;
 }
 
 public static Integer encodeAttribute(String attribute) { // Convert SCM attribute enumeration to S57 attribute code
  if (AttS57.containsKey(attribute))
   return AttS57.get(attribute);
  else if (AttIENC.containsKey(attribute))
   return AttIENC.get(attribute);
  return 0;
 }

 public static Integer encodeAttribute(Att attribute) { // Convert SCM attribute enumeration to S57 attribute code
   return AttS57.get(attribute) != 0 ? AttS57.get(attribute) : AttIENC.get(attribute);
 }

 public static String stringAttribute(Att attribute) { // Convert SCM enumeration to OSM attribute string
  String str = AttStr.get(attribute);
  return str != null ? str : "";
 }
 
 public static Att enumAttribute(String attribute, Obj obj) { // Convert OSM attribute string to SCM enumeration
   if ((attribute != null) && !attribute.isEmpty()) {
   EnumMap<Obj, Att> map = StrAtt.get(attribute);
   if (map != null) {
    if (map.containsKey(obj)) {
     return map.get(obj);
    } else if (map.containsKey(Obj.UNKOBJ)) {
     return map.get(Obj.UNKOBJ);
    } else {
     return Att.UNKATT;
    }
   }
   }
  return Att.UNKATT;
 }

}
