package s57;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import s57.S57obj.*;

public class S57att {
	
	public enum Att {
	  UNKATT, AGENCY, BCNSHP, BUISHP, BOYSHP, BURDEP, CALSGN, CATAIR, CATACH, CATBRG, CATBUA, CATCBL, CATCAN, CATCAM, CATCHP, CATCOA, CATCTR, CATCON, CATCRN, CATDAM,
	  CATDIS, CATDOC, CATDPG, CATFNC, CATFRY, CATFIF, CATFOG, CATFOR, CATGAT, CATHAF, CATHLK, CATICE, CATINB, CATLND, CATLMK, CATLAM, CATLIT, CATMFA, CATMPA, CATMOR,
	  CATNAV, CATOBS, CATOFP, CATOLB, CATPLE, CATPIL, CATPIP, CATPRA, CATPYL, CATRAS, CATRTB, CATROS, CATTRK, CATRSC, CATREA, CATROD, CATRUN, CATSEA, CATSLC, CATSIT,
	  CATSIW, CATSIL, CATSLO, CATSCF, CATSPM, CATTSS, CATVEG, CATWAT, CATWED, CATWRK, CATZOC, COLOUR, COLPAT, COMCHA, CPDATE, CSCALE, CONDTN, CONRAD, CONVIS, CURVEL,
	  DATEND, DATSTA, DRVAL1, DRVAL2, DUNITS, ELEVAT, ESTRNG, EXCLIT, EXPSOU, FUNCTN, HEIGHT, HUNITS, HORACC, HORCLR, HORLEN, HORWID, ICEFAC, INFORM, JRSDTN, LIFCAP,
	  LITCHR, LITVIS, MARSYS, MLTYLT, NATION, NATCON, NATSUR, NATQUA, NMDATE, OBJNAM, ORIENT, PEREND, PERSTA, PICREP, PILDST, PRCTRY, PRODCT, PUBREF, QUASOU, RADWAL,
	  RADIUS, RECDAT, RECIND, RYRMGV, RESTRN, SCAMAX, SCAMIN, SCVAL1, SCVAL2, SECTR1, SECTR2, SHIPAM, SIGFRQ, SIGGEN, SIGGRP, SIGPER, SIGSEQ, SOUACC, SDISMX, SDISMN,
	  SORDAT, SORIND, STATUS, SURATH, SUREND, SURSTA, SURTYP, TECSOU, TXTDSC, TS_TSP, TS_TSV, T_ACWL, T_HWLW, T_MTOD, T_THDF, T_TINT, T_TSVL, T_VAHC, TIMEND, TIMSTA,
	  TOPSHP, TRAFIC, VALACM, VALDCO, VALLMA, VALMAG, VALMXR, VALNMR, VALSOU, VERACC, VERCLR, VERCCL, VERCOP, VERCSA, VERDAT, VERLEN, WATLEV, CAT_TS, PUNITS, NINFOM,
	  NOBJNM, NPLDST, NTXTDS, HORDAT, POSACC, QUAPOS, CLSDNG, DIRIMP, DISBK1, DISBK2, DISIPU, DISIPD, ELEVA1, ELEVA2, FNCTNM, WTWDIS, BUNVES, BNKWTW, COMCTN, HORCLL,
	  HORCLW, TRSHGD, UNLOCD, HIGWAT, HIGNAM, LOWWAT, LOWNAM, MEAWAT, MEANAM, OTHWAT, OTHNAM, REFLEV, SDRLEV, VCRLEV, SCHREF, USESHP, CURVHW, CURVLW, CURVMW, CURVOW,
	  APTREF, SHPTYP, UPDMSG, ADDMRK, CATBNK, CATNMK, CATBRT, CATBUN, CATCCL, CATCOM, CATHBR, CATRFD, CATTML, CATGAG, CATVTR, CATTAB, CATEXS, CATWWM, LG_SPD, LG_SPR,
	  LG_BME, LG_LGS, LG_DRT, LG_WDP, LG_WDU, LG_REL, LG_FNC, LG_DES, LG_PBR, LC_CSI, LC_CSE, LC_ASI, LC_ASE, LC_CCI, LC_CCE, LC_BM1, LC_BM2, LC_LG1, LC_LG2, LC_DR1,
	  LC_DR2, LC_SP1, LC_SP2, LC_WD1, LC_WD2, LITRAD
	}

	private static final EnumMap<Att, Integer> AttS57 = new EnumMap<Att, Integer>(Att.class);
	static {
		AttS57.put(Att.UNKATT, 0); AttS57.put(Att.AGENCY, 1); AttS57.put(Att.BCNSHP, 2); AttS57.put(Att.BUISHP, 3); AttS57.put(Att.BOYSHP, 4); AttS57.put(Att.BURDEP, 5);
		AttS57.put(Att.CALSGN, 6); AttS57.put(Att.CATAIR, 7); AttS57.put(Att.CATACH, 8); AttS57.put(Att.CATBRG, 9); AttS57.put(Att.CATBUA, 10); AttS57.put(Att.CATCBL, 11);
		AttS57.put(Att.CATCAN, 12); AttS57.put(Att.CATCAM, 13); AttS57.put(Att.CATCHP, 14); AttS57.put(Att.CATCOA, 15); AttS57.put(Att.CATCTR, 16); AttS57.put(Att.CATCON, 17);
		AttS57.put(Att.CATCRN, 19); AttS57.put(Att.CATDAM, 20); AttS57.put(Att.CATDIS, 21); AttS57.put(Att.CATDOC, 22); AttS57.put(Att.CATDPG, 23);	AttS57.put(Att.CATFNC, 24);
		AttS57.put(Att.CATFRY, 25); AttS57.put(Att.CATFIF, 26); AttS57.put(Att.CATFOG, 27); AttS57.put(Att.CATFOR, 28); AttS57.put(Att.CATGAT, 29); AttS57.put(Att.CATHAF, 30);
		AttS57.put(Att.CATHLK, 31); AttS57.put(Att.CATICE, 32); AttS57.put(Att.CATINB, 33); AttS57.put(Att.CATLND, 34); AttS57.put(Att.CATLMK, 35);	AttS57.put(Att.CATLAM, 36);
		AttS57.put(Att.CATLIT, 37); AttS57.put(Att.CATMFA, 38); AttS57.put(Att.CATMPA, 39); AttS57.put(Att.CATMOR, 40); AttS57.put(Att.CATNAV, 41);	AttS57.put(Att.CATOBS, 42);
		AttS57.put(Att.CATOFP, 43); AttS57.put(Att.CATOLB, 44); AttS57.put(Att.CATPLE, 45); AttS57.put(Att.CATPIL, 46); AttS57.put(Att.CATPIP, 47);	AttS57.put(Att.CATPRA, 48);
		AttS57.put(Att.CATPYL, 49); AttS57.put(Att.CATRAS, 51); AttS57.put(Att.CATRTB, 52); AttS57.put(Att.CATROS, 53);	AttS57.put(Att.CATTRK, 54); AttS57.put(Att.CATRSC, 55);
		AttS57.put(Att.CATREA, 56); AttS57.put(Att.CATROD, 57); AttS57.put(Att.CATRUN, 58); AttS57.put(Att.CATSEA, 59);	AttS57.put(Att.CATSLC, 60); AttS57.put(Att.CATSIT, 61);
		AttS57.put(Att.CATSIW, 62); AttS57.put(Att.CATSIL, 63); AttS57.put(Att.CATSLO, 64); AttS57.put(Att.CATSCF, 65);	AttS57.put(Att.CATSPM, 66); AttS57.put(Att.CATTSS, 67);
		AttS57.put(Att.CATVEG, 68); AttS57.put(Att.CATWAT, 69); AttS57.put(Att.CATWED, 70); AttS57.put(Att.CATWRK, 71);	AttS57.put(Att.CATZOC, 72); AttS57.put(Att.COLOUR, 75);
		AttS57.put(Att.COLPAT, 76); AttS57.put(Att.COMCHA, 77); AttS57.put(Att.CONDTN, 81); AttS57.put(Att.CONRAD, 82);	AttS57.put(Att.CONVIS, 83); AttS57.put(Att.CURVEL, 84);
		AttS57.put(Att.DATEND, 85); AttS57.put(Att.DATSTA, 86); AttS57.put(Att.DRVAL1, 87); AttS57.put(Att.DRVAL2, 88);	AttS57.put(Att.DUNITS, 89); AttS57.put(Att.ELEVAT, 90);
		AttS57.put(Att.ESTRNG, 91); AttS57.put(Att.EXCLIT, 92); AttS57.put(Att.EXPSOU, 93); AttS57.put(Att.FUNCTN, 94);	AttS57.put(Att.HEIGHT, 95); AttS57.put(Att.HUNITS, 96);
		AttS57.put(Att.HORACC, 97); AttS57.put(Att.HORCLR, 98); AttS57.put(Att.HORLEN, 99); AttS57.put(Att.HORWID, 100); AttS57.put(Att.ICEFAC, 101); AttS57.put(Att.INFORM, 102);
		AttS57.put(Att.JRSDTN, 103); AttS57.put(Att.LIFCAP, 106); AttS57.put(Att.LITCHR, 107); AttS57.put(Att.LITVIS, 108);	AttS57.put(Att.MARSYS, 109); AttS57.put(Att.MLTYLT, 110);
		AttS57.put(Att.NATION, 111); AttS57.put(Att.NATCON, 112); AttS57.put(Att.NATSUR, 113); AttS57.put(Att.NATQUA, 114);	AttS57.put(Att.NMDATE, 115); AttS57.put(Att.OBJNAM, 116);
		AttS57.put(Att.ORIENT, 117); AttS57.put(Att.PEREND, 118);	AttS57.put(Att.PERSTA, 119); AttS57.put(Att.PICREP, 120);	AttS57.put(Att.PILDST, 121); AttS57.put(Att.PRCTRY, 122);
		AttS57.put(Att.PRODCT, 123); AttS57.put(Att.PUBREF, 124);	AttS57.put(Att.QUASOU, 125); AttS57.put(Att.RADWAL, 126);	AttS57.put(Att.RADIUS, 127); AttS57.put(Att.RECDAT, 128);
		AttS57.put(Att.RECIND, 129); AttS57.put(Att.RYRMGV, 130);	AttS57.put(Att.RESTRN, 131); AttS57.put(Att.SECTR1, 136);	AttS57.put(Att.SECTR2, 137); AttS57.put(Att.SHIPAM, 138);
		AttS57.put(Att.SIGFRQ, 139); AttS57.put(Att.SIGGEN, 140);	AttS57.put(Att.SIGGRP, 141); AttS57.put(Att.SIGPER, 142);	AttS57.put(Att.SIGSEQ, 143); AttS57.put(Att.SOUACC, 144);
		AttS57.put(Att.SDISMX, 145); AttS57.put(Att.SDISMN, 146);	AttS57.put(Att.SORDAT, 147); AttS57.put(Att.SORIND, 148);	AttS57.put(Att.STATUS, 149); AttS57.put(Att.SURATH, 150);
		AttS57.put(Att.SUREND, 151); AttS57.put(Att.SURSTA, 152);	AttS57.put(Att.SURTYP, 153); AttS57.put(Att.TECSOU, 156);	AttS57.put(Att.TXTDSC, 158); AttS57.put(Att.TIMEND, 168);
		AttS57.put(Att.TIMSTA, 169); AttS57.put(Att.TOPSHP, 171);	AttS57.put(Att.TRAFIC, 172); AttS57.put(Att.VALACM, 173);	AttS57.put(Att.VALDCO, 174); AttS57.put(Att.VALLMA, 175);
		AttS57.put(Att.VALMAG, 176); AttS57.put(Att.VALMXR, 177);	AttS57.put(Att.VALNMR, 178); AttS57.put(Att.VALSOU, 179);	AttS57.put(Att.VERACC, 180); AttS57.put(Att.VERCLR, 181);
		AttS57.put(Att.VERCCL, 182); AttS57.put(Att.VERCOP, 183);	AttS57.put(Att.VERCSA, 184); AttS57.put(Att.VERDAT, 185);	AttS57.put(Att.VERLEN, 186); AttS57.put(Att.WATLEV, 187);
		AttS57.put(Att.CAT_TS, 188); AttS57.put(Att.PUNITS, 189);	AttS57.put(Att.NINFOM, 300); AttS57.put(Att.NOBJNM, 301);	AttS57.put(Att.NPLDST, 302); AttS57.put(Att.NTXTDS, 304);
		AttS57.put(Att.HORDAT, 400); AttS57.put(Att.POSACC, 401);	AttS57.put(Att.QUAPOS, 402);
	}

	private static final EnumMap<Att, Integer> AttIENC = new EnumMap<Att, Integer>(Att.class);
	static {
		AttIENC.put(Att.CATACH, 17000); AttIENC.put(Att.CATDIS, 17001); AttIENC.put(Att.CATSIT, 17002); AttIENC.put(Att.CATSIW, 17003); AttIENC.put(Att.RESTRN, 17004);
		AttIENC.put(Att.VERDAT, 17005); AttIENC.put(Att.CATBRG, 17006); AttIENC.put(Att.CATFRY, 17007); AttIENC.put(Att.CATHAF, 17008); AttIENC.put(Att.MARSYS, 17009);
		AttIENC.put(Att.CATCHP, 17010); AttIENC.put(Att.CATLAM, 17011); AttIENC.put(Att.CATSLC, 17012); AttIENC.put(Att.ADDMRK, 17050); AttIENC.put(Att.CATBNK, 17051);
		AttIENC.put(Att.CATNMK, 17052); AttIENC.put(Att.CLSDNG, 17055); AttIENC.put(Att.DIRIMP, 17056); AttIENC.put(Att.DISBK1, 17057); AttIENC.put(Att.DISBK2, 17058);
		AttIENC.put(Att.DISIPU, 17059); AttIENC.put(Att.DISIPD, 17060); AttIENC.put(Att.ELEVA1, 17061); AttIENC.put(Att.ELEVA2, 17062); AttIENC.put(Att.FNCTNM, 17063);
		AttIENC.put(Att.WTWDIS, 17064); AttIENC.put(Att.BUNVES, 17065); AttIENC.put(Att.CATBRT, 17066); AttIENC.put(Att.CATBUN, 17067); AttIENC.put(Att.CATCCL, 17069);
		AttIENC.put(Att.CATHBR, 17070); AttIENC.put(Att.CATRFD, 17071); AttIENC.put(Att.CATTML, 17072); AttIENC.put(Att.COMCTN, 17073); AttIENC.put(Att.HORCLL, 17074);
		AttIENC.put(Att.HORCLW, 17075); AttIENC.put(Att.TRSHGD, 17076); AttIENC.put(Att.UNLOCD, 17077); AttIENC.put(Att.CATGAG, 17078); AttIENC.put(Att.HIGWAT, 17080);
		AttIENC.put(Att.HIGNAM, 17081); AttIENC.put(Att.LOWWAT, 17082); AttIENC.put(Att.LOWNAM, 17083); AttIENC.put(Att.MEAWAT, 17084); AttIENC.put(Att.MEANAM, 17085);
		AttIENC.put(Att.OTHWAT, 17086); AttIENC.put(Att.OTHNAM, 17087); AttIENC.put(Att.REFLEV, 17088); AttIENC.put(Att.SDRLEV, 17089); AttIENC.put(Att.VCRLEV, 17090);
		AttIENC.put(Att.CATVTR, 17091); AttIENC.put(Att.CATTAB, 17092); AttIENC.put(Att.SCHREF, 17093); AttIENC.put(Att.USESHP, 17094); AttIENC.put(Att.CURVHW, 17095);
		AttIENC.put(Att.CURVLW, 17096); AttIENC.put(Att.CURVMW, 17097); AttIENC.put(Att.CURVOW, 17098); AttIENC.put(Att.APTREF, 17099); AttIENC.put(Att.CATEXS, 17100);
		AttIENC.put(Att.CATCBL, 17101); AttIENC.put(Att.CATHLK, 17102); AttIENC.put(Att.HUNITS, 17103); AttIENC.put(Att.WATLEV, 17104); AttIENC.put(Att.CATWWM, 17112);
		AttIENC.put(Att.LG_SPD, 18001); AttIENC.put(Att.LG_SPR, 18002); AttIENC.put(Att.LG_BME, 18003); AttIENC.put(Att.LG_LGS, 18004); AttIENC.put(Att.LG_DRT, 18005);
		AttIENC.put(Att.LG_WDP, 18006); AttIENC.put(Att.LG_WDU, 18007); AttIENC.put(Att.LG_REL, 18008); AttIENC.put(Att.LG_FNC, 18009); AttIENC.put(Att.LG_DES, 18010);
		AttIENC.put(Att.LG_PBR, 18011); AttIENC.put(Att.LC_CSI, 18012); AttIENC.put(Att.LC_CSE, 18013); AttIENC.put(Att.LC_ASI, 18014); AttIENC.put(Att.LC_ASE, 18015);
		AttIENC.put(Att.LC_CCI, 18016); AttIENC.put(Att.LC_CCE, 18017); AttIENC.put(Att.LC_BM1, 18018); AttIENC.put(Att.LC_BM2, 18019); AttIENC.put(Att.LC_LG1, 18020);
		AttIENC.put(Att.LC_LG2, 18021); AttIENC.put(Att.LC_DR1, 18022); AttIENC.put(Att.LC_DR2, 18023); AttIENC.put(Att.LC_SP1, 18024); AttIENC.put(Att.LC_SP2, 18025);
		AttIENC.put(Att.LC_WD1, 18026); AttIENC.put(Att.LC_WD2, 18027); AttIENC.put(Att.SHPTYP, 33066); AttIENC.put(Att.UPDMSG, 40000); AttIENC.put(Att.BNKWTW, 17999);
	}
	
	private static final HashMap<Integer, Att> S57Att = new HashMap<Integer, Att>();
	static {
		for (Map.Entry<Att, Integer> entry : AttS57.entrySet()) {
			S57Att.put(entry.getValue(), entry.getKey());
		}
		for (Map.Entry<Att, Integer> entry : AttIENC.entrySet()) {
			S57Att.put(entry.getValue(), entry.getKey());
		}
	}
	
	private static final EnumMap<Att, String> AttStr = new EnumMap<Att, String>(Att.class);
	static {
		AttStr.put(Att.UNKATT, ""); AttStr.put(Att.AGENCY, "agency"); AttStr.put(Att.BCNSHP, "shape"); AttStr.put(Att.BUISHP, "shape"); AttStr.put(Att.BOYSHP, "shape");
		AttStr.put(Att.BURDEP, "depth_buried"); AttStr.put(Att.CALSGN, "callsign"); AttStr.put(Att.CATAIR, "category"); AttStr.put(Att.CATACH, "category");
		AttStr.put(Att.CATBRG, "category"); AttStr.put(Att.CATBUA, "category"); AttStr.put(Att.CATCBL, "category"); AttStr.put(Att.CATCAN, "category");
		AttStr.put(Att.CATCAM, "category"); AttStr.put(Att.CATCHP, "category"); AttStr.put(Att.CATCOA, "category"); AttStr.put(Att.CATCTR, "category");
		AttStr.put(Att.CATCON, "category"); AttStr.put(Att.CATCRN, "category"); AttStr.put(Att.CATDAM, "category");
		AttStr.put(Att.CATDIS, "category"); AttStr.put(Att.CATDOC, "category"); AttStr.put(Att.CATDPG, "category"); AttStr.put(Att.CATFNC, "category");
		AttStr.put(Att.CATFRY, "category"); AttStr.put(Att.CATFIF, "category"); AttStr.put(Att.CATFOG, "category"); AttStr.put(Att.CATFOR, "category");
		AttStr.put(Att.CATGAT, "category"); AttStr.put(Att.CATHAF, "category"); AttStr.put(Att.CATHLK, "category"); AttStr.put(Att.CATICE, "category");
		AttStr.put(Att.CATINB, "category"); AttStr.put(Att.CATLND, "category"); AttStr.put(Att.CATLMK, "category"); AttStr.put(Att.CATLAM, "category");
		AttStr.put(Att.CATLIT, "category"); AttStr.put(Att.CATMFA, "category"); AttStr.put(Att.CATMPA, "category"); AttStr.put(Att.CATMOR, "category");
		AttStr.put(Att.CATNAV, "category"); AttStr.put(Att.CATOBS, "category"); AttStr.put(Att.CATOFP, "category"); AttStr.put(Att.CATOLB, "category");
		AttStr.put(Att.CATPLE, "category"); AttStr.put(Att.CATPIL, "category"); AttStr.put(Att.CATPIP, "category"); AttStr.put(Att.CATPRA, "category");
		AttStr.put(Att.CATPYL, "category"); AttStr.put(Att.CATRAS, "category"); AttStr.put(Att.CATRTB, "category");
		AttStr.put(Att.CATROS, "category"); AttStr.put(Att.CATTRK, "category"); AttStr.put(Att.CATRSC, "category"); AttStr.put(Att.CATREA, "category");
		AttStr.put(Att.CATROD, "category"); AttStr.put(Att.CATRUN, "category"); AttStr.put(Att.CATSEA, "category"); AttStr.put(Att.CATSLC, "category");
		AttStr.put(Att.CATSIT, "category"); AttStr.put(Att.CATSIW, "category"); AttStr.put(Att.CATSIL, "category"); AttStr.put(Att.CATSLO, "category");
		AttStr.put(Att.CATSCF, "category"); AttStr.put(Att.CATSPM, "category"); AttStr.put(Att.CATTSS, "category"); AttStr.put(Att.CATVEG, "category");
		AttStr.put(Att.CATWAT, "category"); AttStr.put(Att.CATWED, "category"); AttStr.put(Att.CATWRK, "category"); AttStr.put(Att.CATZOC, "category");
		AttStr.put(Att.COLOUR, "colour"); AttStr.put(Att.COLPAT, "colour_pattern"); AttStr.put(Att.COMCHA, "channel"); AttStr.put(Att.CONDTN, "condition");
		AttStr.put(Att.CONRAD, "reflectivity"); AttStr.put(Att.CONVIS, "conspicuity"); AttStr.put(Att.CURVEL, "velocity"); AttStr.put(Att.DATEND, "end_date");
		AttStr.put(Att.DATSTA, "start_date"); AttStr.put(Att.DRVAL1, "minimum_depth"); AttStr.put(Att.DRVAL2, "maximum_depth"); AttStr.put(Att.DUNITS, "depth_units");
		AttStr.put(Att.ELEVAT, "elevation"); AttStr.put(Att.ESTRNG, "estimated_range"); AttStr.put(Att.EXCLIT, "exhibition"); AttStr.put(Att.EXPSOU, "exposition");
		AttStr.put(Att.FUNCTN, "function"); AttStr.put(Att.HEIGHT, "height"); AttStr.put(Att.HUNITS, "units"); AttStr.put(Att.HORACC, "accuracy");
		AttStr.put(Att.HORCLR, "clearance"); AttStr.put(Att.HORLEN, "length"); AttStr.put(Att.HORWID, "width"); AttStr.put(Att.ICEFAC, "factor");
		AttStr.put(Att.INFORM, "information"); AttStr.put(Att.JRSDTN, "jurisdiction"); AttStr.put(Att.LIFCAP, "maximum_load"); AttStr.put(Att.LITCHR, "character");
		AttStr.put(Att.LITVIS, "visibility"); AttStr.put(Att.MARSYS, "system");	AttStr.put(Att.MLTYLT, "multiple"); AttStr.put(Att.NATION, "nationality");
		AttStr.put(Att.NATCON, "construction"); AttStr.put(Att.NATSUR, "surface"); AttStr.put(Att.NATQUA, "surface_qualification"); AttStr.put(Att.NMDATE, "nm_date");
		AttStr.put(Att.OBJNAM, "name"); AttStr.put(Att.ORIENT, "orientation"); AttStr.put(Att.PEREND, "end_date"); AttStr.put(Att.PERSTA, "start_date");
		AttStr.put(Att.PICREP, "representation"); AttStr.put(Att.PILDST, "pilot_district");	AttStr.put(Att.PRCTRY, "producing_country"); AttStr.put(Att.PRODCT, "product");
		AttStr.put(Att.PUBREF, "reference"); AttStr.put(Att.QUASOU, "quality");	AttStr.put(Att.RADWAL, "wavelength"); AttStr.put(Att.RADIUS, "radius");
		AttStr.put(Att.RECDAT, "date"); AttStr.put(Att.RECIND, "indication");	AttStr.put(Att.RYRMGV, "year"); AttStr.put(Att.RESTRN, "restriction");
		AttStr.put(Att.SECTR1, "sector_start"); AttStr.put(Att.SECTR2, "sector_end");	AttStr.put(Att.SHIPAM, "shift"); AttStr.put(Att.SIGFRQ, "frequency");
		AttStr.put(Att.SIGGEN, "generation"); AttStr.put(Att.SIGGRP, "group"); AttStr.put(Att.SIGPER, "period"); AttStr.put(Att.SIGSEQ, "sequence");
		AttStr.put(Att.SOUACC, "accuracy"); AttStr.put(Att.SDISMX, "maximum_sounding");	AttStr.put(Att.SDISMN, "minimum_sounding"); AttStr.put(Att.SORDAT, "source_date");
		AttStr.put(Att.SORIND, "source"); AttStr.put(Att.STATUS, "status");	AttStr.put(Att.SURATH, "authority"); AttStr.put(Att.SUREND, "end_date");
		AttStr.put(Att.SURSTA, "start_date"); AttStr.put(Att.SURTYP, "survey");	AttStr.put(Att.TECSOU, "technique"); AttStr.put(Att.TXTDSC, "description");
		AttStr.put(Att.TIMEND, "end_time"); AttStr.put(Att.TIMSTA, "start_time");	AttStr.put(Att.TOPSHP, "shape"); AttStr.put(Att.TRAFIC, "traffic_flow");
		AttStr.put(Att.VALACM, "variation_change"); AttStr.put(Att.VALDCO, "depth"); AttStr.put(Att.VALLMA, "anomaly"); AttStr.put(Att.VALMAG, "variation");
		AttStr.put(Att.VALMXR, "maximum_range"); AttStr.put(Att.VALNMR, "range");	AttStr.put(Att.VALSOU, "sounding"); AttStr.put(Att.VERACC, "vertical_accuracy");
		AttStr.put(Att.VERCLR, "clearance_height");	AttStr.put(Att.VERCCL, "clearance_height_closed"); AttStr.put(Att.VERCOP, "clearance_height_open");
		AttStr.put(Att.VERCSA, "clearance_height_safe"); AttStr.put(Att.VERDAT, "vertical_datum"); AttStr.put(Att.VERLEN, "vertical_length"); AttStr.put(Att.WATLEV, "water_level");
		AttStr.put(Att.CAT_TS, "category");	AttStr.put(Att.PUNITS, "positional_units"); AttStr.put(Att.NINFOM, "national_information"); AttStr.put(Att.NOBJNM, "national_name");
		AttStr.put(Att.NPLDST, "national_pilot_district"); AttStr.put(Att.NTXTDS, "national_description"); AttStr.put(Att.HORDAT, "horizontal_datum");
		AttStr.put(Att.POSACC, "positional_accuracy"); AttStr.put(Att.QUAPOS, "position_quality"); AttStr.put(Att.ADDMRK, "addition"); AttStr.put(Att.BNKWTW, "bank");
		AttStr.put(Att.CATBNK, "category");	AttStr.put(Att.CATNMK, "category"); AttStr.put(Att.CLSDNG, "class"); AttStr.put(Att.DIRIMP, "impact");
		AttStr.put(Att.DISBK1, "distance_start");	AttStr.put(Att.DISBK2, "distance_end");AttStr.put(Att.DISIPU, "distance_up"); AttStr.put(Att.DISIPD, "distance_down");
		AttStr.put(Att.ELEVA1, "minimum_elevation"); AttStr.put(Att.ELEVA2, "maximum_elevation"); AttStr.put(Att.FNCTNM, "function"); AttStr.put(Att.WTWDIS, "distance");
		AttStr.put(Att.BUNVES, "availibility");	AttStr.put(Att.CATBRT, "category"); AttStr.put(Att.CATBUN, "category"); AttStr.put(Att.CATCCL, "category");
		AttStr.put(Att.CATHBR, "category");	AttStr.put(Att.CATRFD, "category"); AttStr.put(Att.CATTML, "category"); AttStr.put(Att.COMCTN, "communication");
		AttStr.put(Att.HORCLL, "clearance_length");	AttStr.put(Att.HORCLW, "clearance_width"); AttStr.put(Att.TRSHGD, "goods"); AttStr.put(Att.UNLOCD, "locode");
		AttStr.put(Att.CATGAG, "category");	AttStr.put(Att.HIGWAT, "high_value"); AttStr.put(Att.HIGNAM, "high_name"); AttStr.put(Att.LOWWAT, "low_value");
		AttStr.put(Att.LOWNAM, "low_name");	AttStr.put(Att.MEAWAT, "mean_value"); AttStr.put(Att.MEANAM, "mean_name"); AttStr.put(Att.OTHWAT, "local_value");
		AttStr.put(Att.OTHNAM, "local_name");	AttStr.put(Att.REFLEV, "gravity_reference"); AttStr.put(Att.SDRLEV, "sounding_name"); AttStr.put(Att.VCRLEV, "vertical_name");
		AttStr.put(Att.CATVTR, "category");	AttStr.put(Att.CATTAB, "operation"); AttStr.put(Att.SCHREF, "schedule"); AttStr.put(Att.USESHP, "use"); AttStr.put(Att.CURVHW, "high_velocity");
		AttStr.put(Att.CURVLW, "low_velocity"); AttStr.put(Att.CURVMW, "mean_velocity"); AttStr.put(Att.CURVOW, "other_velocity"); AttStr.put(Att.APTREF, "passing_time");
		AttStr.put(Att.CATEXS, "category"); AttStr.put(Att.CATWWM, "category"); AttStr.put(Att.SHPTYP, "ship"); AttStr.put(Att.UPDMSG, "message"); AttStr.put(Att.LITRAD, "radius");
	}
	
	static class ObjAtt {
		Obj obj;
		Att att;
		ObjAtt(Obj object, Att attribute) {
			obj = object; att = attribute;
		}
	}
	
	private static final ArrayList<ObjAtt> objatt = new ArrayList<ObjAtt>();
	static {
	  objatt.add(new ObjAtt(Obj.ACHARE, Att.CATACH)); objatt.add(new ObjAtt(Obj.ACHBRT, Att.CATACH)); objatt.add(new ObjAtt(Obj.AIRARE, Att.CATAIR));
	  objatt.add(new ObjAtt(Obj.BCNCAR, Att.BCNSHP)); objatt.add(new ObjAtt(Obj.BCNCAR, Att.CATCAM)); objatt.add(new ObjAtt(Obj.BCNISD, Att.BCNSHP));
	  objatt.add(new ObjAtt(Obj.BCNLAT, Att.BCNSHP)); objatt.add(new ObjAtt(Obj.BCNLAT, Att.CATLAM)); objatt.add(new ObjAtt(Obj.BCNSAW, Att.BCNSHP));
	  objatt.add(new ObjAtt(Obj.BCNSPP, Att.BCNSHP)); objatt.add(new ObjAtt(Obj.BCNSPP, Att.CATSPM)); objatt.add(new ObjAtt(Obj.BCNWTW, Att.BCNSHP));
	  objatt.add(new ObjAtt(Obj.BCNWTW, Att.CATWWM)); objatt.add(new ObjAtt(Obj.BOYCAR, Att.BOYSHP)); objatt.add(new ObjAtt(Obj.BOYCAR, Att.CATCAM));
	  objatt.add(new ObjAtt(Obj.BOYISD, Att.BOYSHP)); objatt.add(new ObjAtt(Obj.BOYLAT, Att.BOYSHP)); objatt.add(new ObjAtt(Obj.BOYLAT, Att.CATLAM));
	  objatt.add(new ObjAtt(Obj.BOYSAW, Att.BOYSHP)); objatt.add(new ObjAtt(Obj.BOYSPP, Att.BOYSHP)); objatt.add(new ObjAtt(Obj.BOYSPP, Att.CATSPM));
	  objatt.add(new ObjAtt(Obj.BOYWTW, Att.BOYSHP)); objatt.add(new ObjAtt(Obj.BOYWTW, Att.CATWWM)); objatt.add(new ObjAtt(Obj.BOYINB, Att.BOYSHP));
	  objatt.add(new ObjAtt(Obj.BOYINB, Att.CATINB)); objatt.add(new ObjAtt(Obj.BRIDGE, Att.CATBRG)); objatt.add(new ObjAtt(Obj.BUAARE, Att.CATBUA));
	  objatt.add(new ObjAtt(Obj.BUNSTA, Att.CATBUN)); objatt.add(new ObjAtt(Obj.BUISGL, Att.FUNCTN)); objatt.add(new ObjAtt(Obj.BUISGL, Att.BUISHP));
	  objatt.add(new ObjAtt(Obj.CANALS, Att.CATCAN)); objatt.add(new ObjAtt(Obj.CANBNK, Att.CATBNK)); objatt.add(new ObjAtt(Obj.CBLARE, Att.CATCBL));
	  objatt.add(new ObjAtt(Obj.CBLOHD, Att.CATCBL)); objatt.add(new ObjAtt(Obj.CBLSUB, Att.CATCBL)); objatt.add(new ObjAtt(Obj.CHKPNT, Att.CATCHP));
	  objatt.add(new ObjAtt(Obj.COALNE, Att.CATCOA)); objatt.add(new ObjAtt(Obj.COMARE, Att.CATCCL)); objatt.add(new ObjAtt(Obj.CONVYR, Att.CATCON));
	  objatt.add(new ObjAtt(Obj.CTRPNT, Att.CATCTR)); objatt.add(new ObjAtt(Obj.CRANES, Att.CATCRN)); objatt.add(new ObjAtt(Obj.DAMCON, Att.CATDAM));
	  objatt.add(new ObjAtt(Obj.DAYMAR, Att.TOPSHP)); objatt.add(new ObjAtt(Obj.DISMAR, Att.CATDIS)); objatt.add(new ObjAtt(Obj.DMPGRD, Att.CATDPG));
	  objatt.add(new ObjAtt(Obj.DOCARE, Att.CATDOC)); objatt.add(new ObjAtt(Obj.EXCNST, Att.CATEXS)); objatt.add(new ObjAtt(Obj.FERYRT, Att.CATFRY));
	  objatt.add(new ObjAtt(Obj.FNCLNE, Att.CATFNC)); objatt.add(new ObjAtt(Obj.FOGSIG, Att.CATFOG)); objatt.add(new ObjAtt(Obj.FORSTC, Att.CATFOR));
	  objatt.add(new ObjAtt(Obj.FSHFAC, Att.CATFIF)); objatt.add(new ObjAtt(Obj.GATCON, Att.CATGAT)); objatt.add(new ObjAtt(Obj.HRBARE, Att.CATHBR));
	  objatt.add(new ObjAtt(Obj.HRBBSN, Att.CATHBR)); objatt.add(new ObjAtt(Obj.HRBFAC, Att.CATHAF)); objatt.add(new ObjAtt(Obj.HRBFAC, Att.CATHBR));
	  objatt.add(new ObjAtt(Obj.HULKES, Att.CATHLK)); objatt.add(new ObjAtt(Obj.ICEARE, Att.CATICE)); objatt.add(new ObjAtt(Obj.LIGHTS, Att.CATLIT));
	  objatt.add(new ObjAtt(Obj.LNDRGN, Att.CATLND)); objatt.add(new ObjAtt(Obj.LNDMRK, Att.FUNCTN)); objatt.add(new ObjAtt(Obj.LNDMRK, Att.CATLMK));
	  objatt.add(new ObjAtt(Obj.MARCUL, Att.CATMFA)); objatt.add(new ObjAtt(Obj.MIPARE, Att.CATMPA)); objatt.add(new ObjAtt(Obj.MORFAC, Att.CATMOR));
	  objatt.add(new ObjAtt(Obj.MORFAC, Att.BOYSHP)); objatt.add(new ObjAtt(Obj.NAVLNE, Att.CATNAV)); objatt.add(new ObjAtt(Obj.NOTMRK, Att.CATNMK));
	  objatt.add(new ObjAtt(Obj.NOTMRK, Att.FNCTNM)); objatt.add(new ObjAtt(Obj.OBSTRN, Att.CATOBS)); objatt.add(new ObjAtt(Obj.OFSPLF, Att.CATOFP));
	  objatt.add(new ObjAtt(Obj.OILBAR, Att.CATOLB)); objatt.add(new ObjAtt(Obj.OSPARE, Att.CATPRA)); objatt.add(new ObjAtt(Obj.PILBOP, Att.CATPIL));
	  objatt.add(new ObjAtt(Obj.PILPNT, Att.CATPLE)); objatt.add(new ObjAtt(Obj.PIPARE, Att.CATPIP)); objatt.add(new ObjAtt(Obj.PIPOHD, Att.CATPIP));
	  objatt.add(new ObjAtt(Obj.PIPSOL, Att.CATPIP)); objatt.add(new ObjAtt(Obj.PRDARE, Att.CATPRA)); objatt.add(new ObjAtt(Obj.PYLONS, Att.CATPYL));
	  objatt.add(new ObjAtt(Obj.RADSTA, Att.CATRAS)); objatt.add(new ObjAtt(Obj.RCRTCL, Att.CATTRK)); objatt.add(new ObjAtt(Obj.RCTLPT, Att.CATTRK));
	  objatt.add(new ObjAtt(Obj.RDOSTA, Att.CATROS)); objatt.add(new ObjAtt(Obj.RECTRC, Att.CATTRK)); objatt.add(new ObjAtt(Obj.REFDMP, Att.CATRFD));
	  objatt.add(new ObjAtt(Obj.RESARE, Att.CATREA)); objatt.add(new ObjAtt(Obj.RIVBNK, Att.CATBNK)); objatt.add(new ObjAtt(Obj.ROADWY, Att.CATROD));
	  objatt.add(new ObjAtt(Obj.RSCSTA, Att.CATRSC)); objatt.add(new ObjAtt(Obj.RTPBCN, Att.CATRTB)); objatt.add(new ObjAtt(Obj.RUNWAY, Att.CATRUN));
	  objatt.add(new ObjAtt(Obj.SEAARE, Att.CATSEA)); objatt.add(new ObjAtt(Obj.SILTNK, Att.CATSIL)); objatt.add(new ObjAtt(Obj.SILTNK, Att.BUISHP));
	  objatt.add(new ObjAtt(Obj.SISTAT, Att.CATSIT)); objatt.add(new ObjAtt(Obj.SISTAW, Att.CATSIW)); objatt.add(new ObjAtt(Obj.SLCONS, Att.CATSLC));
	  objatt.add(new ObjAtt(Obj.SLOTOP, Att.CATSLO)); objatt.add(new ObjAtt(Obj.SLOGRD, Att.CATSLO)); objatt.add(new ObjAtt(Obj.SMCFAC, Att.CATSCF));
	  objatt.add(new ObjAtt(Obj.TERMNL, Att.CATTML)); objatt.add(new ObjAtt(Obj.TOPMAR, Att.TOPSHP)); objatt.add(new ObjAtt(Obj.TSELNE, Att.CATTSS));
	  objatt.add(new ObjAtt(Obj.TSEZNE, Att.CATTSS)); objatt.add(new ObjAtt(Obj.TSSBND, Att.CATTSS)); objatt.add(new ObjAtt(Obj.TSSCRS, Att.CATTSS));
	  objatt.add(new ObjAtt(Obj.TSSLPT, Att.CATTSS)); objatt.add(new ObjAtt(Obj.TSSRON, Att.CATTSS)); objatt.add(new ObjAtt(Obj.TWRTPT, Att.CATTRK));
	  objatt.add(new ObjAtt(Obj.VEGATN, Att.CATVEG)); objatt.add(new ObjAtt(Obj.VEHTRF, Att.CATVTR)); objatt.add(new ObjAtt(Obj.WATTUR, Att.CATWAT));
	  objatt.add(new ObjAtt(Obj.WEDKLP, Att.CATWED)); objatt.add(new ObjAtt(Obj.WRECKS, Att.CATWRK)); objatt.add(new ObjAtt(Obj.TS_FEB, Att.CAT_TS));
	  objatt.add(new ObjAtt(Obj.UNKOBJ, Att.RADIUS)); objatt.add(new ObjAtt(Obj.LIGHTS, Att.LITRAD)); objatt.add(new ObjAtt(Obj.NOTMRK, Att.BNKWTW));
	}
	
	private enum Ver {NON, NOT, YES}

	private static Ver verifyAttribute(Obj obj, Att att) {
	  Ver ver = Ver.NOT;
	  for (ObjAtt oa : objatt) {
	  	if (oa.att == att) {
	  		if (oa.obj == obj)
	  			return Ver.YES;
	  		else
	  			ver = Ver.NON;
	  	}
	  }
	  return ver;
	}

	public static Att decodeAttribute(Integer attribute) {	// Convert S57 attribute code to OSeaM attribute enumeration
		Att att = S57Att.get(attribute);
		return (att != null) ? att : Att.UNKATT;
	}
	
	public static Integer encodeAttribute(String attribute) {	// Convert OSeaM attribute enumeration to S57 attribute code
		if (AttS57.containsKey(attribute))
			return AttS57.get(attribute);
		else if (AttIENC.containsKey(attribute))
			return AttIENC.get(attribute);
		return 0;
	}

	public static Integer encodeAttribute(Att attribute) {	// Convert OSeaM attribute enumeration to S57 attribute code
	  return AttS57.get(attribute) != 0 ? AttS57.get(attribute) : AttIENC.get(attribute);
	}

	public static String stringAttribute(Att attribute) {	// Convert OSeaM enumeration to OSeaM attribute string
		String str = AttStr.get(attribute);
		return str != null ? str : "";
	}
	
	public static Att enumAttribute(String attribute, Obj obj) {	// Convert OSeaM attribute string to OSeaM enumeration
	  if ((attribute != null) && !attribute.isEmpty()) {
	    for (Att att : AttStr.keySet()) {
	      if (AttStr.get(att).equals(attribute) && (verifyAttribute(obj, att) != Ver.NON ))
	        return att;
	    }
	  }
		return Att.UNKATT;
	}

}
