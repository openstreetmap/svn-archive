package S57;

import java.util.ArrayList;
import java.util.EnumMap;

import S57.S57obj.*;

public class S57att {
	
	public enum Att {
	  UNKATT, AGENCY, BCNSHP, BUISHP, BOYSHP, BURDEP, CALSGN, CATAIR, CATACH, CATBRG, CATBUA, CATCBL, CATCAN, CATCAM, CATCHP, CATCOA, CATCTR, CATCON, CATCOV, CATCRN,
	  CATDAM, CATDIS, CATDOC, CATDPG, CATFNC, CATFRY, CATFIF, CATFOG, CATFOR, CATGAT, CATHAF, CATHLK, CATICE, CATINB, CATLND, CATLMK, CATLAM, CATLIT, CATMFA, CATMPA,
	  CATMOR, CATNAV, CATOBS, CATOFP, CATOLB, CATPLE, CATPIL, CATPIP, CATPRA, CATPYL, CATQUA, CATRAS, CATRTB, CATROS, CATTRK, CATRSC, CATREA, CATROD, CATRUN, CATSEA,
	  CATSLC, CATSIT, CATSIW, CATSIL, CATSLO, CATSCF, CATSPM, CATTSS, CATVEG, CATWAT, CATWED, CATWRK, CATZOC, $SPACE, $CHARS, COLOUR, COLPAT, COMCHA, $CSIZE, CPDATE,
	  CSCALE, CONDTN, CONRAD, CONVIS, CURVEL, DATEND, DATSTA, DRVAL1, DRVAL2, DUNITS, ELEVAT, ESTRNG, EXCLIT, EXPSOU, FUNCTN, HEIGHT, HUNITS, HORACC, HORCLR, HORLEN,
	  HORWID, ICEFAC, INFORM, JRSDTN, $JUSTH, $JUSTV, LIFCAP, LITCHR, LITVIS, MARSYS, MLTYLT, NATION, NATCON, NATSUR, NATQUA, NMDATE, OBJNAM, ORIENT, PEREND, PERSTA,
	  PICREP, PILDST, PRCTRY, PRODCT, PUBREF, QUASOU, RADWAL, RADIUS, RECDAT, RECIND, RYRMGV, RESTRN, SCAMAX, SCAMIN, SCVAL1, SCVAL2, SECTR1, SECTR2, SHIPAM, SIGFRQ,
	  SIGGEN, SIGGRP, SIGPER, SIGSEQ, SOUACC, SDISMX, SDISMN, SORDAT, SORIND, STATUS, SURATH, SUREND, SURSTA, SURTYP, $SCALE, $SCODE, TECSOU, $TXSTR, TXTDSC, TS_TSP,
	  TS_TSV, T_ACWL, T_HWLW, T_MTOD, T_THDF, T_TINT, T_TSVL, T_VAHC, TIMEND, TIMSTA, $TINTS, TOPSHP, TRAFIC, VALACM, VALDCO, VALLMA, VALMAG, VALMXR, VALNMR, VALSOU,
	  VERACC, VERCLR, VERCCL, VERCOP, VERCSA, VERDAT, VERLEN, WATLEV, CAT_TS, PUNITS, NINFOM, NOBJNM, NPLDST, $NTXST, NTXTDS, HORDAT, POSACC, QUAPOS, CLSDNG, DIRIMP,
	  DISBK1, DISBK2, DISIPU, DISIPD, ELEVA1, ELEVA2, FNCTNM, WTWDIS, BUNVES, COMCTN, HORCLL, HORCLW, TRSHGD, UNLOCD, HIGWAT, HIGNAM, LOWWAT, LOWNAM, MEAWAT, MEANAM,
	  OTHWAT, OTHNAM, REFLEV, SDRLEV, VCRLEV, SCHREF, USESHP, CURVHW, CURVLW, CURVMW, CURVOW, APTREF, SHPTYP, UPDMSG, ADDMRK, CATBNK, CATNMK, CATBRT, CATBUN, CATCCL,
	  CATCOM, CATHBR, CATRFD, CATTML, CATGAG, CATVTR, CATTAB, CATEXS, CATWWM, LG_SPD, LG_SPR, LG_BME, LG_LGS, LG_DRT, LG_WDP, LG_WDU, LG_REL, LG_FNC, LG_DES, LG_PBR,
	  LC_CSI, LC_CSE, LC_ASI, LC_ASE, LC_CCI, LC_CCE, LC_BM1, LC_BM2, LC_LG1, LC_LG2, LC_DR1, LC_DR2, LC_SP1, LC_SP2, LC_WD1, LC_WD2, LITRAD
	}

	private static final EnumMap<Att, Integer> AttS57 = new EnumMap<Att, Integer>(Att.class);
	static {
		AttS57.put(Att.UNKATT, 0); AttS57.put(Att.AGENCY, 1); AttS57.put(Att.BCNSHP, 2); AttS57.put(Att.BUISHP, 3); AttS57.put(Att.BOYSHP, 4); AttS57.put(Att.BURDEP, 5);
		AttS57.put(Att.CALSGN, 6); AttS57.put(Att.CATAIR, 7); AttS57.put(Att.CATACH, 8); AttS57.put(Att.CATBRG, 9); AttS57.put(Att.CATBUA, 10); AttS57.put(Att.CATCBL, 11);
		AttS57.put(Att.CATCAN, 12); AttS57.put(Att.CATCAM, 13); AttS57.put(Att.CATCHP, 14); AttS57.put(Att.CATCOA, 15); AttS57.put(Att.CATCTR, 16); AttS57.put(Att.CATCON, 17);
		AttS57.put(Att.CATCOV, 18); AttS57.put(Att.CATCRN, 19); AttS57.put(Att.CATDAM, 20); AttS57.put(Att.CATDIS, 21); AttS57.put(Att.CATDOC, 22); AttS57.put(Att.CATDPG, 23);
		AttS57.put(Att.CATFNC, 24); AttS57.put(Att.CATFRY, 25); AttS57.put(Att.CATFIF, 26); AttS57.put(Att.CATFOG, 27); AttS57.put(Att.CATFOR, 28); AttS57.put(Att.CATGAT, 29);
		AttS57.put(Att.CATHAF, 30); AttS57.put(Att.CATHLK, 31); AttS57.put(Att.CATICE, 32); AttS57.put(Att.CATINB, 33); AttS57.put(Att.CATLND, 34); AttS57.put(Att.CATLMK, 35);
		AttS57.put(Att.CATLAM, 36); AttS57.put(Att.CATLIT, 37); AttS57.put(Att.CATMFA, 38); AttS57.put(Att.CATMPA, 39); AttS57.put(Att.CATMOR, 40); AttS57.put(Att.CATNAV, 41);
		AttS57.put(Att.CATOBS, 42); AttS57.put(Att.CATOFP, 43); AttS57.put(Att.CATOLB, 44); AttS57.put(Att.CATPLE, 45); AttS57.put(Att.CATPIL, 46); AttS57.put(Att.CATPIP, 47);
		AttS57.put(Att.CATPRA, 48); AttS57.put(Att.CATPYL, 49); AttS57.put(Att.CATQUA, 50); AttS57.put(Att.CATRAS, 51); AttS57.put(Att.CATRTB, 52); AttS57.put(Att.CATROS, 53);
		AttS57.put(Att.CATTRK, 54); AttS57.put(Att.CATRSC, 55); AttS57.put(Att.CATREA, 56); AttS57.put(Att.CATROD, 57); AttS57.put(Att.CATRUN, 58); AttS57.put(Att.CATSEA, 59);
		AttS57.put(Att.CATSLC, 60); AttS57.put(Att.CATSIT, 61); AttS57.put(Att.CATSIW, 62); AttS57.put(Att.CATSIL, 63); AttS57.put(Att.CATSLO, 64); AttS57.put(Att.CATSCF, 65);
		AttS57.put(Att.CATSPM, 66); AttS57.put(Att.CATTSS, 67); AttS57.put(Att.CATVEG, 68); AttS57.put(Att.CATWAT, 69); AttS57.put(Att.CATWED, 70); AttS57.put(Att.CATWRK, 71);
		AttS57.put(Att.CATZOC, 72); AttS57.put(Att.COLOUR, 75); AttS57.put(Att.COLPAT, 76); AttS57.put(Att.COMCHA, 77); AttS57.put(Att.CONDTN, 81); AttS57.put(Att.CONRAD, 82);
		AttS57.put(Att.CONVIS, 83); AttS57.put(Att.CURVEL, 84); AttS57.put(Att.DATEND, 85); AttS57.put(Att.DATSTA, 86); AttS57.put(Att.DRVAL1, 87); AttS57.put(Att.DRVAL2, 88);
		AttS57.put(Att.DUNITS, 89); AttS57.put(Att.ELEVAT, 90); AttS57.put(Att.ESTRNG, 91); AttS57.put(Att.EXCLIT, 92); AttS57.put(Att.EXPSOU, 93); AttS57.put(Att.FUNCTN, 94);
		AttS57.put(Att.HEIGHT, 95); AttS57.put(Att.HUNITS, 96); AttS57.put(Att.HORACC, 97); AttS57.put(Att.HORCLR, 98); AttS57.put(Att.HORLEN, 99); AttS57.put(Att.HORWID, 100);
		AttS57.put(Att.ICEFAC, 101); AttS57.put(Att.INFORM, 102); AttS57.put(Att.JRSDTN, 103); AttS57.put(Att.$JUSTH, 104); AttS57.put(Att.$JUSTV, 105); AttS57.put(Att.LIFCAP, 106);
		AttS57.put(Att.LITCHR, 107); AttS57.put(Att.LITVIS, 108); AttS57.put(Att.MARSYS, 109); AttS57.put(Att.MLTYLT, 110); AttS57.put(Att.NATION, 111); AttS57.put(Att.NATCON, 112);
		AttS57.put(Att.NATSUR, 113); AttS57.put(Att.NATQUA, 114); AttS57.put(Att.NMDATE, 115); AttS57.put(Att.OBJNAM, 116); AttS57.put(Att.ORIENT, 117); AttS57.put(Att.PEREND, 118);
		AttS57.put(Att.PERSTA, 119); AttS57.put(Att.PICREP, 120); AttS57.put(Att.PILDST, 121); AttS57.put(Att.PRCTRY, 122); AttS57.put(Att.PRODCT, 123); AttS57.put(Att.PUBREF, 124);
		AttS57.put(Att.QUASOU, 125); AttS57.put(Att.RADWAL, 126); AttS57.put(Att.RADIUS, 127); AttS57.put(Att.RECDAT, 128); AttS57.put(Att.RECIND, 129); AttS57.put(Att.RYRMGV, 130);
		AttS57.put(Att.RESTRN, 131); AttS57.put(Att.SECTR1, 136); AttS57.put(Att.SECTR2, 137); AttS57.put(Att.SHIPAM, 138); AttS57.put(Att.SIGFRQ, 139); AttS57.put(Att.SIGGEN, 140);
		AttS57.put(Att.SIGGRP, 141); AttS57.put(Att.SIGPER, 142); AttS57.put(Att.SIGSEQ, 143); AttS57.put(Att.SOUACC, 144); AttS57.put(Att.SDISMX, 145); AttS57.put(Att.SDISMN, 146);
		AttS57.put(Att.SORDAT, 147); AttS57.put(Att.SORIND, 148); AttS57.put(Att.STATUS, 149); AttS57.put(Att.SURATH, 150); AttS57.put(Att.SUREND, 151); AttS57.put(Att.SURSTA, 152);
		AttS57.put(Att.SURTYP, 153); AttS57.put(Att.TECSOU, 156); AttS57.put(Att.TXTDSC, 158); AttS57.put(Att.TIMEND, 168); AttS57.put(Att.TIMSTA, 169); AttS57.put(Att.TOPSHP, 171);
		AttS57.put(Att.TRAFIC, 172); AttS57.put(Att.VALACM, 173); AttS57.put(Att.VALDCO, 174); AttS57.put(Att.VALLMA, 175); AttS57.put(Att.VALMAG, 176); AttS57.put(Att.VALMXR, 177);
		AttS57.put(Att.VALNMR, 178); AttS57.put(Att.VALSOU, 179); AttS57.put(Att.VERACC, 180); AttS57.put(Att.VERCLR, 181); AttS57.put(Att.VERCCL, 182); AttS57.put(Att.VERCOP, 183);
		AttS57.put(Att.VERCSA, 184); AttS57.put(Att.VERDAT, 185); AttS57.put(Att.VERLEN, 186); AttS57.put(Att.WATLEV, 187); AttS57.put(Att.CAT_TS, 188); AttS57.put(Att.PUNITS, 189);
		AttS57.put(Att.NINFOM, 300); AttS57.put(Att.NOBJNM, 301); AttS57.put(Att.NPLDST, 302); AttS57.put(Att.NTXTDS, 304); AttS57.put(Att.HORDAT, 400); AttS57.put(Att.POSACC, 401);
		AttS57.put(Att.QUAPOS, 402);
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
		AttIENC.put(Att.LC_WD1, 18026); AttIENC.put(Att.LC_WD2, 18027); AttIENC.put(Att.SHPTYP, 33066); AttIENC.put(Att.UPDMSG, 40000);
	}
	
	private static final EnumMap<Att, String> AttSTR = new EnumMap<Att, String>(Att.class);
	static {
		AttSTR.put(Att.UNKATT, ""); AttSTR.put(Att.AGENCY, "agency"); AttSTR.put(Att.BCNSHP, "shape"); AttSTR.put(Att.BUISHP, "shape"); AttSTR.put(Att.BOYSHP, "shape");
		AttSTR.put(Att.BURDEP, "depth_buried"); AttSTR.put(Att.CALSGN, "callsign"); AttSTR.put(Att.CATAIR, "category"); AttSTR.put(Att.CATACH, "category");
		AttSTR.put(Att.CATBRG, "category"); AttSTR.put(Att.CATBUA, "category"); AttSTR.put(Att.CATCBL, "category"); AttSTR.put(Att.CATCAN, "category");
		AttSTR.put(Att.CATCAM, "category"); AttSTR.put(Att.CATCHP, "category"); AttSTR.put(Att.CATCOA, "category"); AttSTR.put(Att.CATCTR, "category");
		AttSTR.put(Att.CATCON, "category"); AttSTR.put(Att.CATCOV, "category"); AttSTR.put(Att.CATCRN, "category"); AttSTR.put(Att.CATDAM, "category");
		AttSTR.put(Att.CATDIS, "category"); AttSTR.put(Att.CATDOC, "category"); AttSTR.put(Att.CATDPG, "category"); AttSTR.put(Att.CATFNC, "category");
		AttSTR.put(Att.CATFRY, "category"); AttSTR.put(Att.CATFIF, "category"); AttSTR.put(Att.CATFOG, "category"); AttSTR.put(Att.CATFOR, "category");
		AttSTR.put(Att.CATGAT, "category"); AttSTR.put(Att.CATHAF, "category"); AttSTR.put(Att.CATHLK, "category"); AttSTR.put(Att.CATICE, "category");
		AttSTR.put(Att.CATINB, "category"); AttSTR.put(Att.CATLND, "category"); AttSTR.put(Att.CATLMK, "category"); AttSTR.put(Att.CATLAM, "category");
		AttSTR.put(Att.CATLIT, "category"); AttSTR.put(Att.CATMFA, "category"); AttSTR.put(Att.CATMPA, "category"); AttSTR.put(Att.CATMOR, "category");
		AttSTR.put(Att.CATNAV, "category"); AttSTR.put(Att.CATOBS, "category"); AttSTR.put(Att.CATOFP, "category"); AttSTR.put(Att.CATOLB, "category");
		AttSTR.put(Att.CATPLE, "category"); AttSTR.put(Att.CATPIL, "category"); AttSTR.put(Att.CATPIP, "category"); AttSTR.put(Att.CATPRA, "category");
		AttSTR.put(Att.CATPYL, "category"); AttSTR.put(Att.CATQUA, "category"); AttSTR.put(Att.CATRAS, "category"); AttSTR.put(Att.CATRTB, "category");
		AttSTR.put(Att.CATROS, "category"); AttSTR.put(Att.CATTRK, "category"); AttSTR.put(Att.CATRSC, "category"); AttSTR.put(Att.CATREA, "category");
		AttSTR.put(Att.CATROD, "category"); AttSTR.put(Att.CATRUN, "category"); AttSTR.put(Att.CATSEA, "category"); AttSTR.put(Att.CATSLC, "category");
		AttSTR.put(Att.CATSIT, "category"); AttSTR.put(Att.CATSIW, "category"); AttSTR.put(Att.CATSIL, "category"); AttSTR.put(Att.CATSLO, "category");
		AttSTR.put(Att.CATSCF, "category"); AttSTR.put(Att.CATSPM, "category"); AttSTR.put(Att.CATTSS, "category"); AttSTR.put(Att.CATVEG, "category");
		AttSTR.put(Att.CATWAT, "category"); AttSTR.put(Att.CATWED, "category"); AttSTR.put(Att.CATWRK, "category"); AttSTR.put(Att.CATZOC, "category");
		AttSTR.put(Att.COLOUR, "colour"); AttSTR.put(Att.COLPAT, "colour_pattern"); AttSTR.put(Att.COMCHA, "channel"); AttSTR.put(Att.CONDTN, "condition");
		AttSTR.put(Att.CONRAD, "reflectivity"); AttSTR.put(Att.CONVIS, "conspicuity"); AttSTR.put(Att.CURVEL, "velocity"); AttSTR.put(Att.DATEND, "end_date");
		AttSTR.put(Att.DATSTA, "start_date"); AttSTR.put(Att.DRVAL1, "minimum_depth"); AttSTR.put(Att.DRVAL2, "maximum_depth"); AttSTR.put(Att.DUNITS, "depth_units");
		AttSTR.put(Att.ELEVAT, "elevation"); AttSTR.put(Att.ESTRNG, "estimated_range"); AttSTR.put(Att.EXCLIT, "exhibition"); AttSTR.put(Att.EXPSOU, "exposition");
		AttSTR.put(Att.FUNCTN, "function"); AttSTR.put(Att.HEIGHT, "height"); AttSTR.put(Att.HUNITS, "height_units"); AttSTR.put(Att.HORACC, "accuracy");
		AttSTR.put(Att.HORCLR, "clearance"); AttSTR.put(Att.HORLEN, "length"); AttSTR.put(Att.HORWID, "width"); AttSTR.put(Att.ICEFAC, "factor");
		AttSTR.put(Att.INFORM, "information"); AttSTR.put(Att.JRSDTN, "jurisdiction"); AttSTR.put(Att.$JUSTH, ""); AttSTR.put(Att.$JUSTV, "");
		AttSTR.put(Att.LIFCAP, "maximum_load"); AttSTR.put(Att.LITCHR, "character"); AttSTR.put(Att.LITVIS, "visibility"); AttSTR.put(Att.MARSYS, "system");
		AttSTR.put(Att.MLTYLT, "multiple"); AttSTR.put(Att.NATION, "nationality"); AttSTR.put(Att.NATCON, "construction"); AttSTR.put(Att.NATSUR, "surface");
		AttSTR.put(Att.NATQUA, "surface_qualification"); AttSTR.put(Att.NMDATE, "nm_date"); AttSTR.put(Att.OBJNAM, "name"); AttSTR.put(Att.ORIENT, "orientation");
		AttSTR.put(Att.PEREND, "end_date"); AttSTR.put(Att.PERSTA, "start_date"); AttSTR.put(Att.PICREP, "representation"); AttSTR.put(Att.PILDST, "pilot_district");
		AttSTR.put(Att.PRCTRY, "producing_country"); AttSTR.put(Att.PRODCT, "product"); AttSTR.put(Att.PUBREF, "reference"); AttSTR.put(Att.QUASOU, "quality");
		AttSTR.put(Att.RADWAL, "wavelength"); AttSTR.put(Att.RADIUS, "radius"); AttSTR.put(Att.RECDAT, "date"); AttSTR.put(Att.RECIND, "indication");
		AttSTR.put(Att.RYRMGV, "year"); AttSTR.put(Att.RESTRN, "restriction"); AttSTR.put(Att.SECTR1, "sector_start"); AttSTR.put(Att.SECTR2, "sector_end");
		AttSTR.put(Att.SHIPAM, "shift"); AttSTR.put(Att.SIGFRQ, "frequency"); AttSTR.put(Att.SIGGEN, "generation"); AttSTR.put(Att.SIGGRP, "group");
		AttSTR.put(Att.SIGPER, "period"); AttSTR.put(Att.SIGSEQ, "sequence"); AttSTR.put(Att.SOUACC, "accuracy"); AttSTR.put(Att.SDISMX, "maximum_sounding");
		AttSTR.put(Att.SDISMN, "minimum_sounding"); AttSTR.put(Att.SORDAT, "source_date"); AttSTR.put(Att.SORIND, "source"); AttSTR.put(Att.STATUS, "status");
		AttSTR.put(Att.SURATH, "authority"); AttSTR.put(Att.SUREND, "end_date"); AttSTR.put(Att.SURSTA, "start_date"); AttSTR.put(Att.SURTYP, "survey");
		AttSTR.put(Att.TECSOU, "technique"); AttSTR.put(Att.TXTDSC, "description"); AttSTR.put(Att.TIMEND, "end_time"); AttSTR.put(Att.TIMSTA, "start_time");
		AttSTR.put(Att.TOPSHP, "shape"); AttSTR.put(Att.TRAFIC, "flow"); AttSTR.put(Att.VALACM, "variation_change"); AttSTR.put(Att.VALDCO, "depth");
		AttSTR.put(Att.VALLMA, "anomaly"); AttSTR.put(Att.VALMAG, "variation"); AttSTR.put(Att.VALMXR, "maximum_range"); AttSTR.put(Att.VALNMR, "range");
		AttSTR.put(Att.VALSOU, "sounding"); AttSTR.put(Att.VERACC, "vertical_accuracy"); AttSTR.put(Att.VERCLR, "clearance_height");
		AttSTR.put(Att.VERCCL, "clearance_height_closed"); AttSTR.put(Att.VERCOP, "clearance_height_open"); AttSTR.put(Att.VERCSA, "clearance_height_safe");
		AttSTR.put(Att.VERDAT, "vertical_datum"); AttSTR.put(Att.VERLEN, "vertical_length"); AttSTR.put(Att.WATLEV, "water_level"); AttSTR.put(Att.CAT_TS, "category");
		AttSTR.put(Att.PUNITS, "units"); AttSTR.put(Att.NINFOM, "national_information"); AttSTR.put(Att.NOBJNM, "national_name"); AttSTR.put(Att.NPLDST, "national_pilot_district");
		AttSTR.put(Att.NTXTDS, "national_description"); AttSTR.put(Att.HORDAT, "horizontal_datum"); AttSTR.put(Att.POSACC, "positional_accuracy");
		AttSTR.put(Att.QUAPOS, "position_quality"); AttSTR.put(Att.ADDMRK, "addition"); AttSTR.put(Att.CATBNK, "category"); AttSTR.put(Att.CATNMK, "category");
		AttSTR.put(Att.CLSDNG, "class"); AttSTR.put(Att.DIRIMP, "impact"); AttSTR.put(Att.DISBK1, "distance_start"); AttSTR.put(Att.DISBK2, "distance_end");
		AttSTR.put(Att.DISIPU, "distance_up"); AttSTR.put(Att.DISIPD, "distance_down"); AttSTR.put(Att.ELEVA1, "minimum_elevation"); AttSTR.put(Att.ELEVA2, "maximum_elevation");
		AttSTR.put(Att.FNCTNM, "function"); AttSTR.put(Att.WTWDIS, "distance"); AttSTR.put(Att.BUNVES, "availibility"); AttSTR.put(Att.CATBRT, "category");
		AttSTR.put(Att.CATBUN, "category"); AttSTR.put(Att.CATCCL, "category"); AttSTR.put(Att.CATHBR, "category"); AttSTR.put(Att.CATRFD, "category");
		AttSTR.put(Att.CATTML, "category"); AttSTR.put(Att.COMCTN, "communication"); AttSTR.put(Att.HORCLL, "clearance_length"); AttSTR.put(Att.HORCLW, "clearance_width");
		AttSTR.put(Att.TRSHGD, "goods"); AttSTR.put(Att.UNLOCD, "locode"); AttSTR.put(Att.CATGAG, "category"); AttSTR.put(Att.HIGWAT, "high_value");
		AttSTR.put(Att.HIGNAM, "high_name"); AttSTR.put(Att.LOWWAT, "low_value"); AttSTR.put(Att.LOWNAM, "low_name"); AttSTR.put(Att.MEAWAT, "mean_value");
		AttSTR.put(Att.MEANAM, "mean_name"); AttSTR.put(Att.OTHWAT, "local_value"); AttSTR.put(Att.OTHNAM, "local_name"); AttSTR.put(Att.REFLEV, "gravity_reference");
		AttSTR.put(Att.SDRLEV, "sounding_name"); AttSTR.put(Att.VCRLEV, "vertical_name"); AttSTR.put(Att.CATVTR, "category"); AttSTR.put(Att.CATTAB, "operation");
		AttSTR.put(Att.SCHREF, "schedule"); AttSTR.put(Att.USESHP, "use"); AttSTR.put(Att.CURVHW, "high_velocity"); AttSTR.put(Att.CURVLW, "low_velocity");
		AttSTR.put(Att.CURVMW, "mean_velocity"); AttSTR.put(Att.CURVOW, "other_velocity"); AttSTR.put(Att.APTREF, "passing_time"); AttSTR.put(Att.CATEXS, "category");
		AttSTR.put(Att.CATWWM, "category"); AttSTR.put(Att.SHPTYP, "ship"); AttSTR.put(Att.UPDMSG, "message"); AttSTR.put(Att.LITRAD, "radius");
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
	  objatt.add(new ObjAtt(Obj.MORFAC, Att.BOYSHP)); objatt.add(new ObjAtt(Obj.M_COVR, Att.CATCOV)); objatt.add(new ObjAtt(Obj.M_QUAL, Att.CATQUA));
	  objatt.add(new ObjAtt(Obj.M_QUAL, Att.CATZOC)); objatt.add(new ObjAtt(Obj.NAVLNE, Att.CATNAV)); objatt.add(new ObjAtt(Obj.NOTMRK, Att.CATNMK));
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
	  objatt.add(new ObjAtt(Obj.UNKOBJ, Att.RADIUS)); objatt.add(new ObjAtt(Obj.LIGHTS, Att.LITRAD));
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

	public static String decodeAttribute(Integer attribute) {	// Convert S57 attribute code to OSeaM attribute string
		String str = AttSTR.get(lookupAttribute(attribute));
		return str != null ? str : "";
	}
	
	public static Integer encodeAttribute(String attribute, Integer objl) {	// Convert OSeaM attribute string to S57 attribute code
	  Att atta = enumAttribute(attribute, S57obj.lookupType(objl));
	  return AttS57.get(atta) != 0 ? AttS57.get(atta) : AttIENC.get(atta);
	}

	public static Integer encodeAttribute(Att attribute) {	// Convert OSeaM attribute enumeration to S57 attribute code
	  return AttS57.get(attribute) != 0 ? AttS57.get(attribute) : AttIENC.get(attribute);
	}

	public static Att lookupAttribute(Integer attribute) {	// Convert S57 attribute code to OSeaM enumeration
		if (attribute < 10000) {
			for (Att att : AttS57.keySet()) {
				if (AttS57.get(att).equals(attribute)) {
					return att;
				}
			}
		} else { 
			for (Att att : AttIENC.keySet()) {
				if (AttIENC.get(att).equals(attribute)) {
					return att;
				}
			}
		}
		return Att.UNKATT;
	}

	public static String stringAttribute(Att attribute) {	// Convert OSeaM enumeration to OSeaM attribute string
		String str = AttSTR.get(attribute);
		return str != null ? str : "";
	}
	
	public static Att enumAttribute(String attribute, Obj obj) {	// Convert OSeaM attribute string to OSeaM enumeration
	  if ((attribute != null) && (attribute.length() > 0)) {
	    for (Att att : AttSTR.keySet()) {
	      if (AttSTR.get(att).equals(attribute) && (verifyAttribute(obj, att) != Ver.NON ))
	        return att;
	    }
	  }
		return Att.UNKATT;
	}

}
