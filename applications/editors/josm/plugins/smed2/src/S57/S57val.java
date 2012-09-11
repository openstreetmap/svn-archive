package S57;

import java.util.ArrayList;
import java.util.EnumMap;

import S57.S57att.*;

public class S57val {

	static class S57enum {
		Integer atvl;
		String val;
		S57enum(Integer a, String v) {
			atvl = a; val = v;
		}
	}
	
	private enum Conv { S, A, L, E, F, I }
	
	static class S57key {
		Conv conv;
		EnumMap map;
		S57key(Conv c, EnumMap m) {
			conv = c; map = m;
		}
	}
	
	public static class AttVal {
		Att att;
		Conv conv;
		Object val;
		AttVal(Att a, Conv c, Object v) {
			att = a; conv = c; val = v;
		}
	}
	
	public enum BcnSHP { BCN_UNKN, BCN_STAK, BCN_WTHY, BCN_TOWR, BCN_LATT, BCN_PILE, BCN_CARN, BCN_BUOY, BCN_POLE, BCN_PRCH, BCN_POST	}
	private static final EnumMap<BcnSHP, S57enum> Bcnshp = new EnumMap<BcnSHP, S57enum>(BcnSHP.class); static { Bcnshp.put(BcnSHP.BCN_UNKN, new S57enum(0, ""));
		Bcnshp.put(BcnSHP.BCN_STAK, new S57enum(1, "stake")); Bcnshp.put(BcnSHP.BCN_WTHY, new S57enum(2, "withy")); Bcnshp.put(BcnSHP.BCN_TOWR, new S57enum(3, "tower"));
		Bcnshp.put(BcnSHP.BCN_LATT, new S57enum(4, "lattice")); Bcnshp.put(BcnSHP.BCN_PILE, new S57enum(5, "pile")); Bcnshp.put(BcnSHP.BCN_CARN, new S57enum(6, "cairn"));
		Bcnshp.put(BcnSHP.BCN_BUOY, new S57enum(7, "buoyant")); Bcnshp.put(BcnSHP.BCN_POLE, new S57enum(1, "pole")); Bcnshp.put(BcnSHP.BCN_PRCH, new S57enum(1, "perch"));
		Bcnshp.put(BcnSHP.BCN_POST, new S57enum(1, "post"));
	}
	public enum BuiSHP { BUI_UNKN, BUI_HIRS, BUI_PYRD, BUI_CYLR, BUI_SPHR, BUI_CUBE	}
	private static final EnumMap<BuiSHP, S57enum> Buishp = new EnumMap<BuiSHP, S57enum>(BuiSHP.class); static { Buishp.put(BuiSHP.BUI_UNKN, new S57enum(0, ""));
		Buishp.put(BuiSHP.BUI_HIRS, new S57enum(5, "high-rise")); Buishp.put(BuiSHP.BUI_PYRD, new S57enum(6, "pyramid")); Buishp.put(BuiSHP.BUI_CYLR, new S57enum(7, "cylindrical"));
		Buishp.put(BuiSHP.BUI_SPHR, new S57enum(8, "spherical")); Buishp.put(BuiSHP.BUI_CUBE, new S57enum(9, "cubic"));
	}
	public enum BoySHP { BOY_UNKN, BOY_CONE, BOY_CAN, BOY_SPHR, BOY_PILR, BOY_SPAR, BOY_BARL, BOY_SUPR, BOY_ICE	}
	private static final EnumMap<BoySHP, S57enum> Boyshp = new EnumMap<BoySHP, S57enum>(BoySHP.class); static { Boyshp.put(BoySHP.BOY_UNKN, new S57enum(0, ""));
		Boyshp.put(BoySHP.BOY_CONE, new S57enum(1, "conical")); Boyshp.put(BoySHP.BOY_CAN, new S57enum(2, "can")); Boyshp.put(BoySHP.BOY_SPHR, new S57enum(3, "spherical"));
		Boyshp.put(BoySHP.BOY_PILR, new S57enum(4, "pillar")); Boyshp.put(BoySHP.BOY_SPAR, new S57enum(5, "spar")); Boyshp.put(BoySHP.BOY_BARL, new S57enum(6, "barrel"));
	  Boyshp.put(BoySHP.BOY_SUPR, new S57enum(7, "super-buoy")); Boyshp.put(BoySHP.BOY_ICE, new S57enum(8, "ice_buoy"));
	}
	public enum CatAIR { AIR_UNKN, AIR_MILA, AIR_CIVA, AIR_MILH, AIR_CIVH, AIR_GLDR, AIR_SMLP, AIR_EMRG }
	private static final EnumMap<CatAIR, S57enum> Catair = new EnumMap<CatAIR, S57enum>(CatAIR.class); static { Catair.put(CatAIR.AIR_UNKN, new S57enum(0, ""));
		Catair.put(CatAIR.AIR_MILA, new S57enum(1, "military")); Catair.put(CatAIR.AIR_CIVA, new S57enum(2, "civil")); Catair.put(CatAIR.AIR_MILH, new S57enum(3, "military_heliport"));
		Catair.put(CatAIR.AIR_CIVH, new S57enum(4, "civil_heliport")); Catair.put(CatAIR.AIR_GLDR, new S57enum(5, "glider")); Catair.put(CatAIR.AIR_SMLP, new S57enum(6, "small_planes"));
		Catair.put(CatAIR.AIR_EMRG, new S57enum(8, "emergency"));
	}
	public enum CatACH { ACH_UNKN, ACH_UNRD, ACH_DEEP, ACH_TANK, ACH_EXPL, ACH_QUAR, ACH_SEAP, ACH_SMCF, ACH_SMCM, ACH_H24P, ACH_LTPD, ACH_NPSH, ACH_DRYC, ACH_RAFT }
	private static final EnumMap<CatACH, S57enum> Catach = new EnumMap<CatACH, S57enum>(CatACH.class); static {Catach.put(CatACH.ACH_UNKN, new S57enum(0, ""));
		Catach.put(CatACH.ACH_UNRD, new S57enum(1, "unrestricted")); Catach.put(CatACH.ACH_DEEP, new S57enum(2, "deep_water")); Catach.put(CatACH.ACH_TANK, new S57enum(3, "tanker"));
		Catach.put(CatACH.ACH_EXPL, new S57enum(4, "explosives")); Catach.put(CatACH.ACH_QUAR, new S57enum(5, "quarantine")); Catach.put(CatACH.ACH_SEAP, new S57enum(6, "sea-plane"));
		Catach.put(CatACH.ACH_SMCF, new S57enum(7, "small_craft")); Catach.put(CatACH.ACH_SMCM, new S57enum(8, "small_craft_mooring")); Catach.put(CatACH.ACH_H24P, new S57enum(9, "24_hour"));
		Catach.put(CatACH.ACH_LTPD, new S57enum(10, "limited_period")); Catach.put(CatACH.ACH_NPSH, new S57enum(11, "non_pushing")); Catach.put(CatACH.ACH_DRYC, new S57enum(12, "dry_cargo"));
		Catach.put(CatACH.ACH_RAFT, new S57enum(13, "raft"));
	}
	public enum CatBRG { BRG_UNKN, BRG_FIXD, BRG_OPEN, BRG_SWNG, BRG_LIFT, BRG_BASC, BRG_PONT, BRG_DRAW, BRG_TRNS, BRG_FOOT, BRG_VIAD, BRG_AQUA, BRG_SUSP }
	private static final EnumMap<CatBRG, S57enum> Catbrg = new EnumMap<CatBRG, S57enum>(CatBRG.class); static {Catbrg.put(CatBRG.BRG_UNKN, new S57enum(0, ""));
		Catbrg.put(CatBRG.BRG_FIXD, new S57enum(1, "fixed")); Catbrg.put(CatBRG.BRG_OPEN, new S57enum(2, "opening")); Catbrg.put(CatBRG.BRG_SWNG, new S57enum(3, "swing"));
		Catbrg.put(CatBRG.BRG_LIFT, new S57enum(4, "lifting")); Catbrg.put(CatBRG.BRG_BASC, new S57enum(5, "bascule")); Catbrg.put(CatBRG.BRG_PONT, new S57enum(6, "pontoon"));
		Catbrg.put(CatBRG.BRG_DRAW, new S57enum(7, "draw")); Catbrg.put(CatBRG.BRG_TRNS, new S57enum(8, "transporter")); Catbrg.put(CatBRG.BRG_FOOT, new S57enum(9, "footbridge"));
		Catbrg.put(CatBRG.BRG_VIAD, new S57enum(10, "viaduct")); Catbrg.put(CatBRG.BRG_AQUA, new S57enum(11, "aqueduct")); Catbrg.put(CatBRG.BRG_SUSP, new S57enum(12, "suspension"));
	}
	public enum CatBUA { BUA_UNKN, BUA_URBN, BUA_STTL, BUA_VLLG, BUA_TOWN, BUA_CITY, BUA_HOLV }
	private static final EnumMap<CatBUA, S57enum> Catbua = new EnumMap<CatBUA, S57enum>(CatBUA.class); static { Catbua.put(CatBUA.BUA_UNKN, new S57enum(0, ""));
		Catbua.put(CatBUA.BUA_URBN, new S57enum(1, "urban")); Catbua.put(CatBUA.BUA_STTL, new S57enum(2, "settlement")); Catbua.put(CatBUA.BUA_VLLG, new S57enum(3, "village"));
		Catbua.put(CatBUA.BUA_TOWN, new S57enum(4, "town")); Catbua.put(CatBUA.BUA_CITY, new S57enum(5, "city")); Catbua.put(CatBUA.BUA_HOLV, new S57enum(6, "holiday_village"));
	}
	public enum CatCBL { CBL_UNKN, CBL_POWR, CBL_TRNS, CBL_TELE, CBL_TGPH, CBL_MOOR, CBL_OPTC, CBL_FERY } 
	private static final EnumMap<CatCBL, S57enum> Catcbl = new EnumMap<CatCBL, S57enum>(CatCBL.class); static {Catcbl.put(CatCBL.CBL_UNKN, new S57enum(0, ""));
		Catcbl.put(CatCBL.CBL_POWR, new S57enum(1, "power")); Catcbl.put(CatCBL.CBL_TRNS, new S57enum(3, "transmission")); Catcbl.put(CatCBL.CBL_TELE, new S57enum(4, "telephone"));
		Catcbl.put(CatCBL.CBL_TGPH, new S57enum(5, "telegraph")); Catcbl.put(CatCBL.CBL_MOOR, new S57enum(6, "mooring")); Catcbl.put(CatCBL.CBL_OPTC, new S57enum(7, "optical"));
		Catcbl.put(CatCBL.CBL_FERY, new S57enum(8, "ferry"));
	}
	public enum CatCAN { CAN_UNKN, CAN_TRNS, CAN_DRNG, CAN_IRGN }
	private static final EnumMap<CatCAN, S57enum> Catcan = new EnumMap<CatCAN, S57enum>(CatCAN.class); static { Catcan.put(CatCAN.CAN_UNKN, new S57enum(0, ""));
		Catcan.put(CatCAN.CAN_TRNS, new S57enum(1, "transportation")); Catcan.put(CatCAN.CAN_DRNG, new S57enum(2, "drainage")); Catcan.put(CatCAN.CAN_IRGN, new S57enum(3, "irrigation"));
	}
	public enum CatCAM { CAM_UNKN, CAM_NORTH, CAM_EAST, CAM_SOUTH, CAM_WEST }
	private static final EnumMap<CatCAM, S57enum> Catcam = new EnumMap<CatCAM, S57enum>(CatCAM.class); static { Catcam.put(CatCAM.CAM_UNKN, new S57enum(0, ""));
		Catcam.put(CatCAM.CAM_NORTH, new S57enum(1, "north")); Catcam.put(CatCAM.CAM_EAST, new S57enum(2, "east"));
		Catcam.put(CatCAM.CAM_SOUTH, new S57enum(3, "south")); Catcam.put(CatCAM.CAM_WEST, new S57enum(4, "west"));
	}
	public enum CatCHP { CHP_UNKN, CHP_CSTM, CHP_BRDR }
	private static final EnumMap<CatCHP, S57enum> Catchp = new EnumMap<CatCHP, S57enum>(CatCHP.class); static { Catchp.put(CatCHP.CHP_UNKN, new S57enum(0, ""));
		Catchp.put(CatCHP.CHP_CSTM, new S57enum(1, "custom")); Catchp.put(CatCHP.CHP_BRDR, new S57enum(2, "border"));
	}
	public enum CatCOA { COA_UNKN, COA_STEP, COA_FLAT, COA_SAND, COA_STON, COA_SHNG, COA_GLCR, COA_MNGV, COA_MRSH, COA_CRRF, COA_ICE, COA_SHEL } 
	private static final EnumMap<CatCOA, S57enum> Catcoa = new EnumMap<CatCOA, S57enum>(CatCOA.class); static { Catcoa.put(CatCOA.COA_UNKN, new S57enum(0, ""));
		Catcoa.put(CatCOA.COA_STEP, new S57enum(1, "steep")); Catcoa.put(CatCOA.COA_FLAT, new S57enum(2, "flat")); Catcoa.put(CatCOA.COA_SAND, new S57enum(3, "sandy"));
		Catcoa.put(CatCOA.COA_STON, new S57enum(4, "stony")); Catcoa.put(CatCOA.COA_SHNG, new S57enum(5, "shingly")); Catcoa.put(CatCOA.COA_GLCR, new S57enum(6, "glacier"));
		Catcoa.put(CatCOA.COA_MNGV, new S57enum(7, "mangrove")); Catcoa.put(CatCOA.COA_MRSH, new S57enum(8, "marshy")); Catcoa.put(CatCOA.COA_CRRF, new S57enum(9, "coral_reef"));
		Catcoa.put(CatCOA.COA_ICE, new S57enum(10, "ice")); Catcoa.put(CatCOA.COA_SHEL, new S57enum(11, "shelly"));
	}
	public enum CatCTR { CTR_UNKN, CTR_TRGN, CTR_OBSV, CTR_FIXD, CTR_BMRK, CTR_BDRY, CTR_HORM, CTR_HORS } 
	private static final EnumMap<CatCTR, S57enum> Catctr = new EnumMap<CatCTR, S57enum>(CatCTR.class); static { Catctr.put(CatCTR.CTR_UNKN, new S57enum(0, ""));
		Catctr.put(CatCTR.CTR_TRGN, new S57enum(1, "triangulation")); Catctr.put(CatCTR.CTR_OBSV, new S57enum(2, "observation")); Catctr.put(CatCTR.CTR_FIXD, new S57enum(3, "fixed"));
		Catctr.put(CatCTR.CTR_BMRK, new S57enum(4, "benchmark")); Catctr.put(CatCTR.CTR_BDRY, new S57enum(5, "boundary")); Catctr.put(CatCTR.CTR_HORM, new S57enum(6, "horizontal_main"));
		Catctr.put(CatCTR.CTR_HORS, new S57enum(7, "horizontal_secondary"));
	}
	public enum CatCON { CON_UNKN, CAT_AERL, CAT_BELT }
	private static final EnumMap<CatCON, S57enum> Catcon = new EnumMap<CatCON, S57enum>(CatCON.class); static { Catcon.put(CatCON.CON_UNKN, new S57enum(0, ""));
		Catcon.put(CatCON.CAT_AERL, new S57enum(1, "aerial")); Catcon.put(CatCON.CAT_BELT, new S57enum(2, "belt"));
	}
	public enum CatCOV { COV_UNKN, COV_COVG, COV_NCOV }
	private static final EnumMap<CatCOV, S57enum> Catcov = new EnumMap<CatCOV, S57enum>(CatCOV.class); static { Catcov.put(CatCOV.COV_UNKN, new S57enum(0, ""));
		Catcov.put(CatCOV.COV_COVG, new S57enum(1, "coverage")); Catcov.put(CatCOV.COV_NCOV, new S57enum(2, "no_coverage"));
	}
	public enum CatCRN { CRN_UNKN, CRN_NONS, CRN_CONT, CRN_SHRL, CRN_TRAV, CRN_AFRM } 
	private static final EnumMap<CatCRN, S57enum> Catcrn = new EnumMap<CatCRN, S57enum>(CatCRN.class); static { Catcrn.put(CatCRN.CRN_UNKN, new S57enum(0, ""));
		Catcrn.put(CatCRN.CRN_NONS, new S57enum(1, "non-specific")); Catcrn.put(CatCRN.CRN_CONT, new S57enum(2, "container")); Catcrn.put(CatCRN.CRN_SHRL, new S57enum(3, "sheerlegs"));
		Catcrn.put(CatCRN.CRN_TRAV, new S57enum(4, "travelling")); Catcrn.put(CatCRN.CRN_AFRM , new S57enum(5, "a-frame"));
	}
	public enum CatDAM { DAM_UNKN, DAM_WEIR, DAM_DAM, DAM_FLDB }
	private static final EnumMap<CatDAM, S57enum> Catdam = new EnumMap<CatDAM, S57enum>(CatDAM.class); static { Catdam.put(CatDAM.DAM_UNKN, new S57enum(0, ""));
		Catdam.put(CatDAM.DAM_WEIR, new S57enum(1, "weir")); Catdam.put(CatDAM.DAM_DAM, new S57enum(2, "dam")); Catdam.put(CatDAM.DAM_FLDB, new S57enum(3, "flood_barrage"));
	}
	public enum CatDIS { DIS_UNKN, DIS_NONI, DIS_POLE, DIS_BORD, DIS_UKSH }
	private static final EnumMap<CatDIS, S57enum> Catdis = new EnumMap<CatDIS, S57enum>(CatDIS.class); static { Catdis.put(CatDIS.DIS_UNKN, new S57enum(0, ""));
		Catdis.put(CatDIS.DIS_NONI, new S57enum(1, "not_installed")); Catdis.put(CatDIS.DIS_POLE, new S57enum(2, "pole")); Catdis.put(CatDIS.DIS_BORD, new S57enum(3, "board"));
		Catdis.put(CatDIS.DIS_UKSH, new S57enum(4, "unknown_shape"));
	}
	public enum CatDOC { DOC_UNKN, DOC_TIDL, DOC_NTDL }
	private static final EnumMap<CatDOC, S57enum> Catdoc = new EnumMap<CatDOC, S57enum>(CatDOC.class); static { Catdoc.put(CatDOC.DOC_UNKN, new S57enum(0, ""));
		Catdoc.put(CatDOC.DOC_TIDL, new S57enum(1, "tidal")); Catdoc.put(CatDOC.DOC_NTDL, new S57enum(2, "non-tidal"));
	}
	public enum CatDPG { DPG_UNKN, DPG_GENL, DPG_CHEM, DPG_NCLR, DPG_EXPL, DPG_SPIL, DPG_VSSL } 
	private static final EnumMap<CatDPG, S57enum> Catdpg = new EnumMap<CatDPG, S57enum>(CatDPG.class); static { Catdpg.put(CatDPG.DPG_UNKN, new S57enum(0, ""));
		Catdpg.put(CatDPG.DPG_GENL, new S57enum(1, "general")); Catdpg.put(CatDPG.DPG_CHEM, new S57enum(2, "chemical")); Catdpg.put(CatDPG.DPG_NCLR, new S57enum(3, "nuclear"));
		Catdpg.put(CatDPG.DPG_EXPL, new S57enum(4, "explosives")); Catdpg.put(CatDPG.DPG_SPIL, new S57enum(5, "spoil")); Catdpg.put(CatDPG.DPG_VSSL, new S57enum(6, "vessel"));
	}
	public enum CatFNC { FNC_UNKN, FNC_FENC, FNC_MUIR, FNC_HEDG, FNC_WALL }
	private static final EnumMap<CatFNC, S57enum> Catfnc = new EnumMap<CatFNC, S57enum>(CatFNC.class); static { Catfnc.put(CatFNC.FNC_UNKN, new S57enum(0, ""));
		Catfnc.put(CatFNC.FNC_FENC, new S57enum(1, "fence")); Catfnc.put(CatFNC.FNC_MUIR, new S57enum(2, "muir")); Catfnc.put(CatFNC.FNC_HEDG, new S57enum(3, "hedge"));
		Catfnc.put(CatFNC.FNC_WALL, new S57enum(4, "wall"));
	}
	public enum CatFRY { FRY_UNKN, FRY_FREE, FRY_CABL, FRY_ICE, FRY_SWWR } 
	private static final EnumMap<CatFRY, S57enum> Catfry = new EnumMap<CatFRY, S57enum>(CatFRY.class); static { Catfry.put(CatFRY.FRY_UNKN, new S57enum(0, ""));
		Catfry.put(CatFRY.FRY_FREE, new S57enum(1, "free")); Catfry.put(CatFRY.FRY_CABL, new S57enum(2, "cable")); Catfry.put(CatFRY.FRY_ICE, new S57enum(3, "ice"));
		Catfry.put(CatFRY.FRY_SWWR, new S57enum(4, "swinging_wire_ferry"));
	}
	public enum CatFIF { FIF_UNKN, FIF_STAK, FIF_TRAP, FIF_WEIR, FIF_TUNY } 
	private static final EnumMap<CatFIF, S57enum> Catfif = new EnumMap<CatFIF, S57enum>(CatFIF.class); static { Catfif.put(CatFIF.FIF_UNKN, new S57enum(0, ""));
		Catfif.put(CatFIF.FIF_STAK, new S57enum(1, "stake")); Catfif.put(CatFIF.FIF_TRAP, new S57enum(2, "trap")); Catfif.put(CatFIF.FIF_WEIR, new S57enum(3, "weir"));
		Catfif.put(CatFIF.FIF_TUNY, new S57enum(4, "tunny"));
	}
	public enum CatFOG { FOG_UNKN, FOG_EXPL, FOG_DIA, FOG_SIRN, FOG_NAUT, FOG_REED, FOG_TYPH, FOG_BELL, FOG_WHIS, FOG_GONG, FOG_HORN }
	private static final EnumMap<CatFOG, S57enum> Catfog = new EnumMap<CatFOG, S57enum>(CatFOG.class); static {Catfog.put(CatFOG.FOG_UNKN, new S57enum(0, ""));
		Catfog.put(CatFOG.FOG_EXPL, new S57enum(1, "explosive")); Catfog.put(CatFOG.FOG_DIA, new S57enum(2, "diaphone")); Catfog.put(CatFOG.FOG_SIRN, new S57enum(3, "siren"));
		Catfog.put(CatFOG.FOG_NAUT, new S57enum(4, "nautophone")); Catfog.put(CatFOG.FOG_REED, new S57enum(5, "reed"));
	  Catfog.put(CatFOG.FOG_TYPH, new S57enum(6, "tyfon")); Catfog.put(CatFOG.FOG_BELL, new S57enum(7, "bell")); Catfog.put(CatFOG.FOG_WHIS, new S57enum(8, "whistle"));
	  Catfog.put(CatFOG.FOG_GONG, new S57enum(9, "gong")); Catfog.put(CatFOG.FOG_HORN, new S57enum(10, "horn"));
	 }
	public enum CatFOR { FOR_UNKN, FOR_CSTL, FOR_FORT, FOR_BTTY, FOR_BKHS, FOR_MTWR, FOR_RDBT } 
	private static final EnumMap<CatFOR, S57enum> Catfor = new EnumMap<CatFOR, S57enum>(CatFOR.class); static { Catfor.put(CatFOR.FOR_UNKN, new S57enum(0, ""));
		Catfor.put(CatFOR.FOR_CSTL, new S57enum(1, "castle")); Catfor.put(CatFOR.FOR_FORT, new S57enum(2, "fort")); Catfor.put(CatFOR.FOR_BTTY, new S57enum(3, "battery"));
		Catfor.put(CatFOR.FOR_BKHS, new S57enum(4, "blockhouse")); Catfor.put(CatFOR.FOR_MTWR, new S57enum(5, "martello_tower")); Catfor.put(CatFOR.FOR_RDBT, new S57enum(6, "redoubt"));
	}
	public enum CatGAT { GAT_UNKN, GAT_GNRL, GAT_FLBG, GAT_CSSN, GAT_LOCK, GAT_DYKE, GAT_SLUC } 
	private static final EnumMap<CatGAT, S57enum> Catgat = new EnumMap<CatGAT, S57enum>(CatGAT.class); static { Catgat.put(CatGAT.GAT_UNKN, new S57enum(0, ""));
		Catgat.put(CatGAT.GAT_GNRL, new S57enum(1, "general")); Catgat.put(CatGAT.GAT_FLBG, new S57enum(2, "flood_barrage")); Catgat.put(CatGAT.GAT_CSSN, new S57enum(3, "caisson"));
		Catgat.put(CatGAT.GAT_LOCK, new S57enum(4, "lock")); Catgat.put(CatGAT.GAT_DYKE, new S57enum(5, "dyke")); Catgat.put(CatGAT.GAT_SLUC, new S57enum(6, "sluice"));
	}
	public enum CatHAF { HAF_UNKN, HAF_RORO, HAF_TMBR, HAF_FERY, HAF_FISH, HAF_YCHT, HAF_NAVL, HAF_TNKR, HAF_PSGR, HAF_YARD, HAF_CNTR, HAF_BULK, HAF_SYNC, HAF_STCR, HAF_MRNA, HAF_REPR, HAF_QUAR } 
	private static final EnumMap<CatHAF, S57enum> Cathaf = new EnumMap<CatHAF, S57enum>(CatHAF.class); static { Cathaf.put(CatHAF.HAF_UNKN, new S57enum(0, ""));
		Cathaf.put(CatHAF.HAF_RORO, new S57enum(1, "roro")); Cathaf.put(CatHAF.HAF_TMBR, new S57enum(2, "timber")); Cathaf.put(CatHAF.HAF_FERY, new S57enum(3, "ferry"));
		Cathaf.put(CatHAF.HAF_FISH, new S57enum(4, "fishing")); Cathaf.put(CatHAF.HAF_YCHT, new S57enum(5, "yacht")); Cathaf.put(CatHAF.HAF_NAVL, new S57enum(6, "naval"));
		Cathaf.put(CatHAF.HAF_TNKR, new S57enum(7, "tanker")); Cathaf.put(CatHAF.HAF_PSGR, new S57enum(8, "passenger")); Cathaf.put(CatHAF.HAF_YARD, new S57enum(9, "shipyard"));
		Cathaf.put(CatHAF.HAF_CNTR, new S57enum(10, "container")); Cathaf.put(CatHAF.HAF_BULK, new S57enum(11, "bulk")); Cathaf.put(CatHAF.HAF_SYNC, new S57enum(12, "syncrolift"));
		Cathaf.put(CatHAF.HAF_STCR, new S57enum(13, "straddle_carrier")); Cathaf.put(CatHAF.HAF_MRNA, new S57enum(14, "marina")); Cathaf.put(CatHAF.HAF_REPR, new S57enum(16, "service_repair"));
		Cathaf.put(CatHAF.HAF_QUAR, new S57enum(17, "quarantine"));
	}
	public enum CatHLK { HLK_UNKN, HLK_REST, HLK_HIST, HLK_MUSM, HLK_ACCM, HLK_BWTR, HLK_CSNO } 
	private static final EnumMap<CatHLK, S57enum> Cathlk = new EnumMap<CatHLK, S57enum>(CatHLK.class); static { Cathlk.put(CatHLK.HLK_UNKN, new S57enum(0, ""));
		Cathlk.put(CatHLK.HLK_REST, new S57enum(1, "floating_restaurant")); Cathlk.put(CatHLK.HLK_HIST, new S57enum(2, "historic")); Cathlk.put(CatHLK.HLK_MUSM, new S57enum(3, "museum"));
		Cathlk.put(CatHLK.HLK_ACCM, new S57enum(4, "accommodation")); Cathlk.put(CatHLK.HLK_BWTR, new S57enum(5, "floating_breakwater")); Cathlk.put(CatHLK.HLK_CSNO, new S57enum(6, "casino_boat"));
	}
	public enum CatICE { ICE_UNKN, ICE_FAST, ICE_SEA, ICE_GRLR, ICE_PANK, ICE_GLAS, ICE_PEAK, ICE_PACK, ICE_POLR } 
	private static final EnumMap<CatICE, S57enum> Catice = new EnumMap<CatICE, S57enum>(CatICE.class); static { Catice.put(CatICE.ICE_UNKN, new S57enum(0, ""));
		Catice.put(CatICE.ICE_FAST, new S57enum(1, "fast")); Catice.put(CatICE.ICE_SEA, new S57enum(2, "sea")); Catice.put(CatICE.ICE_GRLR, new S57enum(3, "growler"));
		Catice.put(CatICE.ICE_PANK, new S57enum(4, "pancake")); Catice.put(CatICE.ICE_GLAS, new S57enum(5, "glacier")); Catice.put(CatICE.ICE_PEAK, new S57enum(6, "peak"));
		Catice.put(CatICE.ICE_PACK, new S57enum(7, "pack")); Catice.put(CatICE.ICE_POLR, new S57enum(8, "polar"));
	}
	public enum CatINB { INB_UNKN, INB_CALM, INB_SBM }
	private static final EnumMap<CatINB, S57enum> Catinb = new EnumMap<CatINB, S57enum>(CatINB.class); static { Catinb.put(CatINB.INB_UNKN, new S57enum(0, ""));
		Catinb.put(CatINB.INB_CALM, new S57enum(1, "calm")); Catinb.put(CatINB.INB_SBM, new S57enum(2, "sbm"));
	}
	public enum CatLND { LND_UNKN, LND_FEN, LND_MRSH, LND_BOG, LND_HTHL, LND_MNTN, LND_LOWL, LND_CNYN, LND_PDDY, LND_AGRI, LND_SVNA, LND_PARK, LND_SWMP, LND_LSLD, LND_LAVA,
		LND_SLTP, LND_MORN, LND_CRTR, LND_CAVE, LND_PINCL, LND_CAY
	}
	private static final EnumMap<CatLND, S57enum> Catlnd = new EnumMap<CatLND, S57enum>(CatLND.class); static { Catlnd.put(CatLND.LND_UNKN, new S57enum(0, ""));
		Catlnd.put(CatLND.LND_FEN, new S57enum(1, "fen")); Catlnd.put(CatLND.LND_MRSH, new S57enum(2, "marsh")); Catlnd.put(CatLND.LND_BOG, new S57enum(3, "bog"));
		Catlnd.put(CatLND.LND_HTHL, new S57enum(4, "heathland")); Catlnd.put(CatLND.LND_MNTN, new S57enum(5, "mountain")); Catlnd.put(CatLND.LND_LOWL, new S57enum(6, "lowlands"));
		Catlnd.put(CatLND.LND_CNYN, new S57enum(7, "canyon")); Catlnd.put(CatLND.LND_PDDY, new S57enum(8, "paddy")); Catlnd.put(CatLND.LND_AGRI, new S57enum(9, "agricultural"));
		Catlnd.put(CatLND.LND_SVNA, new S57enum(10, "savanna")); Catlnd.put(CatLND.LND_PARK, new S57enum(11, "parkland")); Catlnd.put(CatLND.LND_SWMP, new S57enum(12, "swamp"));
		Catlnd.put(CatLND.LND_LSLD, new S57enum(13, "landslide")); Catlnd.put(CatLND.LND_LAVA, new S57enum(14, "lava")); Catlnd.put(CatLND.LND_SLTP, new S57enum(15, "salt_pan"));
		Catlnd.put(CatLND.LND_MORN, new S57enum(16, "moraine")); Catlnd.put(CatLND.LND_CRTR, new S57enum(17, "crater")); Catlnd.put(CatLND.LND_CAVE, new S57enum(18, "cave"));
		Catlnd.put(CatLND.LND_PINCL, new S57enum(19, "rock_pinnacle")); Catlnd.put(CatLND.LND_CAY, new S57enum(20, "cay"));
	}
	public enum CatLMK { LMK_UNKN, LMK_CARN, LMK_CMTY, LMK_CHMY, LMK_DISH, LMK_FLAG, LMK_FLAR, LMK_MAST, LMK_WNDS, LMK_MNMT, LMK_CLMN, LMK_MEML,	LMK_OBLK, LMK_STAT, LMK_CROS,
		LMK_DOME, LMK_RADR, LMK_TOWR, LMK_WNDM, LMK_WNDG, LMK_SPIR, LMK_BLDR, LMK_MNRT, LMK_WTRT }
	private static final EnumMap<CatLMK, S57enum> Catlmk = new EnumMap<CatLMK, S57enum>(CatLMK.class); static {Catlmk.put(CatLMK.LMK_UNKN, new S57enum(0, ""));
		Catlmk.put(CatLMK.LMK_CARN, new S57enum(1, "cairn")); Catlmk.put(CatLMK.LMK_CMTY, new S57enum(2, "cemetery")); Catlmk.put(CatLMK.LMK_CHMY, new S57enum(3, "chimney"));
		Catlmk.put(CatLMK.LMK_DISH, new S57enum(4, "dish_aerial")); Catlmk.put(CatLMK.LMK_FLAG, new S57enum(5, "flagstaff")); Catlmk.put(CatLMK.LMK_FLAR, new S57enum(6, "flare_stack"));
	  Catlmk.put(CatLMK.LMK_MAST, new S57enum(7, "mast")); Catlmk.put(CatLMK.LMK_WNDS, new S57enum(8, "windsock")); Catlmk.put(CatLMK.LMK_MNMT, new S57enum(9, "monument"));
	  Catlmk.put(CatLMK.LMK_CLMN, new S57enum(10, "column")); Catlmk.put(CatLMK.LMK_MEML, new S57enum(11, "memorial")); Catlmk.put(CatLMK.LMK_OBLK, new S57enum(12, "obelisk"));
	  Catlmk.put(CatLMK.LMK_STAT, new S57enum(13, "statue")); Catlmk.put(CatLMK.LMK_CROS, new S57enum(14, "cross")); Catlmk.put(CatLMK.LMK_DOME, new S57enum(15, "dome"));
	  Catlmk.put(CatLMK.LMK_RADR, new S57enum(16, "radar_scanner")); Catlmk.put(CatLMK.LMK_TOWR, new S57enum(17, "tower")); Catlmk.put(CatLMK.LMK_WNDM, new S57enum(18, "windmill"));
	  Catlmk.put(CatLMK.LMK_WNDG, new S57enum(19, "windmotor")); Catlmk.put(CatLMK.LMK_SPIR, new S57enum(20, "spire")); Catlmk.put(CatLMK.LMK_BLDR, new S57enum(21, "boulder"));
	  Catlmk.put(CatLMK.LMK_MNRT, new S57enum(22, "minaret")); Catlmk.put(CatLMK.LMK_WTRT, new S57enum(23, "water_tower"));
	}
	public enum CatLAM { LAM_UNKN, LAM_PORT, LAM_STBD, LAM_PCST, LAM_PCPT, LAM_WWLT, LAM_WWRT, LAM_CHLT, LAM_CHRT, LAM_WWSN, LAM_CHSN, LAM_CHRB, LAM_CHLB, LAM_CRRT, LAM_CRLT,
		LAM_DRLT, LAM_DRRT, LAM_TOLT, LAM_TPRT, LAM_JBRT, LAM_JNLT, LAM_HBRT, LAM_HBLT, LAM_BRGP } 
	private static final EnumMap<CatLAM, S57enum> Catlam = new EnumMap<CatLAM, S57enum>(CatLAM.class); static { Catlam.put(CatLAM.LAM_UNKN, new S57enum(0, ""));
		Catlam.put(CatLAM.LAM_PORT, new S57enum(1, "port")); Catlam.put(CatLAM.LAM_STBD, new S57enum(2, "starboard")); Catlam.put(CatLAM.LAM_PCST, new S57enum(3, "preferred_channel_starboard"));
		Catlam.put(CatLAM.LAM_PCPT, new S57enum(4, "preferred_channel_port")); Catlam.put(CatLAM.LAM_WWLT, new S57enum(5, "waterway_left")); Catlam.put(CatLAM.LAM_WWRT, new S57enum(6, "waterway_right"));
		Catlam.put(CatLAM.LAM_CHLT, new S57enum(7, "channel_left")); Catlam.put(CatLAM.LAM_CHRT, new S57enum(8, "channel_right")); Catlam.put(CatLAM.LAM_WWSN, new S57enum(9, "waterway_separation"));
		Catlam.put(CatLAM.LAM_CHSN, new S57enum(10, "channel_separation")); Catlam.put(CatLAM.LAM_CHRB, new S57enum(11, "channel_right_bank")); Catlam.put(CatLAM.LAM_CHLB, new S57enum(12, "channel_left_bank"));
		Catlam.put(CatLAM.LAM_CRRT, new S57enum(13, "crossover_right")); Catlam.put(CatLAM.LAM_CRLT, new S57enum(14, "crossover_left")); Catlam.put(CatLAM.LAM_DRLT, new S57enum(15, "danger_right"));
		Catlam.put(CatLAM.LAM_DRRT, new S57enum(16, "danger_left")); Catlam.put(CatLAM.LAM_TOLT, new S57enum(17, "turnoff_right")); Catlam.put(CatLAM.LAM_TPRT, new S57enum(18, "turnoff_left"));
		Catlam.put(CatLAM.LAM_JBRT, new S57enum(19, "junction_right")); Catlam.put(CatLAM.LAM_JNLT, new S57enum(20, "junction_left")); Catlam.put(CatLAM.LAM_HBRT, new S57enum(21, "harbour_right"));
		Catlam.put(CatLAM.LAM_HBLT, new S57enum(22, "harbour_left")); Catlam.put(CatLAM.LAM_BRGP, new S57enum(23, "bridge_pier"));
	}
	public enum CatLIT { LIT_UNKN, LIT_DIR, LIT_LEAD, LIT_AERO, LIT_AIR, LIT_FOG, LIT_FLDL, LIT_STRP, LIT_SUBS, LIT_SPOT, LIT_FRNT, LIT_REAR, LIT_LOWR, LIT_UPPR, LIT_MOIR, LIT_EMRG, LIT_BRNG, LIT_HORI, LIT_VERT }
	private static final EnumMap<CatLIT, S57enum> Catlit = new EnumMap<CatLIT, S57enum>(CatLIT.class); static { Catlit.put(CatLIT.LIT_UNKN, new S57enum(0, ""));
		Catlit.put(CatLIT.LIT_DIR, new S57enum(1, "directional")); Catlit.put(CatLIT.LIT_LEAD, new S57enum(4, "leading")); Catlit.put(CatLIT.LIT_AERO, new S57enum(5, "aero"));
		Catlit.put(CatLIT.LIT_AIR, new S57enum(6, "air_obstruction")); Catlit.put(CatLIT.LIT_FOG, new S57enum(7, "fog_detector")); Catlit.put(CatLIT.LIT_FLDL, new S57enum(8, "floodlight"));
		Catlit.put(CatLIT.LIT_STRP, new S57enum(9, "strip_light")); Catlit.put(CatLIT.LIT_SUBS, new S57enum(10, "subsidiary")); Catlit.put(CatLIT.LIT_SPOT, new S57enum(11, "spotlight"));
		Catlit.put(CatLIT.LIT_FRNT, new S57enum(12, "front")); Catlit.put(CatLIT.LIT_REAR, new S57enum(13, "rear")); Catlit.put(CatLIT.LIT_LOWR, new S57enum(14, "lower"));
		Catlit.put(CatLIT.LIT_UPPR, new S57enum(15, "upper")); Catlit.put(CatLIT.LIT_MOIR, new S57enum(16, "moire")); Catlit.put(CatLIT.LIT_EMRG, new S57enum(17, "emergency"));
		Catlit.put(CatLIT.LIT_BRNG, new S57enum(18, "bearing")); Catlit.put(CatLIT.LIT_HORI, new S57enum(19, "horizontal")); Catlit.put(CatLIT.LIT_VERT, new S57enum(20, "vertical"));
	}
	public enum CatMFA { MFA_UNKN, MFA_CRST, MFA_OYMS, MFA_FISH, MFA_SEAW, MFA_PRLC }
	private static final EnumMap<CatMFA, S57enum> Catmfa = new EnumMap<CatMFA, S57enum>(CatMFA.class); static { Catmfa.put(CatMFA.MFA_UNKN, new S57enum(0, ""));
		Catmfa.put(CatMFA.MFA_CRST, new S57enum(1, "crustaceans")); Catmfa.put(CatMFA.MFA_OYMS, new S57enum(2, "oysters_mussels")); Catmfa.put(CatMFA.MFA_FISH, new S57enum(3, "fish"));
		Catmfa.put(CatMFA.MFA_SEAW, new S57enum(4, "seaweed")); Catmfa.put(CatMFA.MFA_PRLC, new S57enum(5, "pearl_culture"));
	}
	public enum CatMPA { MPA_UNKN, MPA_PRCT, MPA_TRPD, MPA_SUBM, MPA_FIRG, MPA_MINL, MPA_SMLA } 
	private static final EnumMap<CatMPA, S57enum> Catmpa = new EnumMap<CatMPA, S57enum>(CatMPA.class); static { Catmpa.put(CatMPA.MPA_UNKN, new S57enum(0, ""));
		Catmpa.put(CatMPA.MPA_PRCT, new S57enum(1, "practice")); Catmpa.put(CatMPA.MPA_TRPD, new S57enum(2, "torpedo")); Catmpa.put(CatMPA.MPA_SUBM, new S57enum(3, "submarine"));
		Catmpa.put(CatMPA.MPA_FIRG, new S57enum(4, "firing")); Catmpa.put(CatMPA.MPA_MINL, new S57enum(5, "mine-laying")); Catmpa.put(CatMPA.MPA_SMLA, new S57enum(6, "small_arms"));
	}
	public enum CatMOR { MOR_UNKN, MOR_DLPN, MOR_DDPN, MOR_BLRD, MOR_WALL, MOR_PILE, MOR_CHAN, MOR_BUOY, MOR_SHRP, MOR_AUTO, MOR_POST, MOR_WIRE, MOR_CABL } 
	private static final EnumMap<CatMOR, S57enum> Catmor = new EnumMap<CatMOR, S57enum>(CatMOR.class); static { Catmor.put(CatMOR.MOR_UNKN, new S57enum(0, ""));
		Catmor.put(CatMOR.MOR_DLPN, new S57enum(1, "dolphin")); Catmor.put(CatMOR.MOR_DDPN, new S57enum(2, "deviation_dolphin")); Catmor.put(CatMOR.MOR_BLRD, new S57enum(3, "bollard"));
		Catmor.put(CatMOR.MOR_WALL, new S57enum(4, "wall")); Catmor.put(CatMOR.MOR_PILE, new S57enum(5, "pile")); Catmor.put(CatMOR.MOR_CHAN, new S57enum(6, "chain"));
		Catmor.put(CatMOR.MOR_BUOY, new S57enum(7, "buoy")); Catmor.put(CatMOR.MOR_SHRP, new S57enum(8, "shore_ropes")); Catmor.put(CatMOR.MOR_AUTO, new S57enum(9, "automatic"));
		Catmor.put(CatMOR.MOR_POST, new S57enum(10, "post")); Catmor.put(CatMOR.MOR_WIRE, new S57enum(11, "wire")); Catmor.put(CatMOR.MOR_CABL, new S57enum(12, "cable"));
	}
	public enum CatNAV { NAV_UNKN, NAV_CLRG, NAV_TRST, NAV_LDNG }
	private static final EnumMap<CatNAV, S57enum> Catnav = new EnumMap<CatNAV, S57enum>(CatNAV.class); static { Catnav.put(CatNAV.NAV_UNKN, new S57enum(0, ""));
		Catnav.put(CatNAV.NAV_CLRG, new S57enum(1, "clearing")); Catnav.put(CatNAV.NAV_TRST, new S57enum(2, "transit")); Catnav.put(CatNAV.NAV_LDNG, new S57enum(3, "leading"));
	}
	public enum CatOBS { OBS_UNKN, OBS_STMP, OBS_WELH, OBS_DIFF, OBS_CRIB, OBS_FHVN, OBS_FLAR, OBS_FLGD, OBS_ICEB, OBS_GTKL, OBS_BOOM } 
	private static final EnumMap<CatOBS, S57enum> Catobs = new EnumMap<CatOBS, S57enum>(CatOBS.class); static { Catobs.put(CatOBS.OBS_UNKN, new S57enum(0, ""));
		Catobs.put(CatOBS.OBS_STMP, new S57enum(1, "stump")); Catobs.put(CatOBS.OBS_WELH, new S57enum(2, "wellhead")); Catobs.put(CatOBS.OBS_DIFF, new S57enum(3, "diffuser"));
		Catobs.put(CatOBS.OBS_CRIB, new S57enum(4, "crib")); Catobs.put(CatOBS.OBS_FHVN, new S57enum(5, "fish_haven")); Catobs.put(CatOBS.OBS_FLAR, new S57enum(6, "foul_area"));
		Catobs.put(CatOBS.OBS_FLGD, new S57enum(7, "foul_ground")); Catobs.put(CatOBS.OBS_ICEB, new S57enum(8, "ice_boom")); Catobs.put(CatOBS.OBS_GTKL, new S57enum(9, "ground_tackle"));
		Catobs.put(CatOBS.OBS_BOOM, new S57enum(10, "boom"));
	}
	public enum CatOFP { OFP_UNKN, OFP_OIL, OFP_PROD, OFP_OBS, OFP_ALP, OFP_SALM, OFP_MOOR, OFP_AISL, OFP_FPSO, OFP_ACCN, OFP_NCCB } 
	private static final EnumMap<CatOFP, S57enum> Catofp = new EnumMap<CatOFP, S57enum>(CatOFP.class); static { Catofp.put(CatOFP.OFP_UNKN, new S57enum(0, ""));
		Catofp.put(CatOFP.OFP_OIL, new S57enum(1, "oil")); Catofp.put(CatOFP.OFP_PROD, new S57enum(2, "production")); Catofp.put(CatOFP.OFP_OBS, new S57enum(3, "observation"));
		Catofp.put(CatOFP.OFP_ALP, new S57enum(4, "alp")); Catofp.put(CatOFP.OFP_SALM, new S57enum(5, "salm")); Catofp.put(CatOFP.OFP_MOOR, new S57enum(6, "mooring"));
		Catofp.put(CatOFP.OFP_AISL, new S57enum(7, "artificial_island")); Catofp.put(CatOFP.OFP_FPSO, new S57enum(8, "fpso")); Catofp.put(CatOFP.OFP_ACCN, new S57enum(9, "accommodation"));
		Catofp.put(CatOFP.OFP_NCCB, new S57enum(10, "nccb"));
	}
	public enum CatOLB { OLB_UNKN, OLB_RETN, OLB_FLTG }
	private static final EnumMap<CatOLB, S57enum> Catolb = new EnumMap<CatOLB, S57enum>(CatOLB.class); static { Catolb.put(CatOLB.OLB_UNKN, new S57enum(0, ""));
		Catolb.put(CatOLB.OLB_RETN, new S57enum(1, "retention")); Catolb.put(CatOLB.OLB_FLTG, new S57enum(2, "floating"));
	}
	public enum CatPLE { PLE_UNKN, PLE_STAK, PLE_SNAG, PLE_POST, PLE_TRIP }
	private static final EnumMap<CatPLE, S57enum> Catple = new EnumMap<CatPLE, S57enum>(CatPLE.class); static { Catple.put(CatPLE.PLE_UNKN, new S57enum(0, ""));
		Catple.put(CatPLE.PLE_STAK, new S57enum(1, "stake")); Catple.put(CatPLE.PLE_SNAG, new S57enum(2, "snag")); Catple.put(CatPLE.PLE_POST, new S57enum(3, "post"));
		Catple.put(CatPLE.PLE_TRIP, new S57enum(4, "tripodal"));
	}
	public enum CatPIL { PIL_UNKN, PIL_CVSL, PIL_HELI, PIL_SHOR }
	private static final EnumMap<CatPIL, S57enum> Catpil = new EnumMap<CatPIL, S57enum>(CatPIL.class); static { Catpil.put(CatPIL.PIL_UNKN, new S57enum(0, ""));
		Catpil.put(CatPIL.PIL_CVSL, new S57enum(1, "cruising_vessel")); Catpil.put(CatPIL.PIL_HELI, new S57enum(2, "helicopter")); Catpil.put(CatPIL.PIL_SHOR, new S57enum(3, "from_shore"));
	}
	public enum CatPIP { PIP_UNKN, PIP_OFAL, PIP_ITAK, PIP_SEWR, PIP_BBLR, PIP_SPPL }
	private static final EnumMap<CatPIP, S57enum> Catpip = new EnumMap<CatPIP, S57enum>(CatPIP.class); static { Catpip.put(CatPIP.PIP_UNKN, new S57enum(0, ""));
		Catpip.put(CatPIP.PIP_OFAL, new S57enum(2, "outfall")); Catpip.put(CatPIP.PIP_ITAK, new S57enum(3, "intake")); Catpip.put(CatPIP.PIP_SEWR, new S57enum(4, "sewer"));
		Catpip.put(CatPIP.PIP_BBLR, new S57enum(5, "bubbler")); Catpip.put(CatPIP.PIP_SPPL, new S57enum(6, "supply"));
	}
	public enum CatPRA { PRA_UNKN, PRA_QRRY, PRA_MINE, PRA_STPL, PRA_PSTN, PRA_RFNY, PRA_TYRD, PRA_FACT, PRA_TFRM, PRA_WFRM, PRA_SLAG } 
	private static final EnumMap<CatPRA, S57enum> Catpra = new EnumMap<CatPRA, S57enum>(CatPRA.class); static { Catpra.put(CatPRA.PRA_UNKN, new S57enum(0, ""));
		Catpra.put(CatPRA.PRA_QRRY, new S57enum(1, "quarry")); Catpra.put(CatPRA.PRA_MINE, new S57enum(2, "mine")); Catpra.put(CatPRA.PRA_STPL, new S57enum(3, "stockpile"));
		Catpra.put(CatPRA.PRA_PSTN, new S57enum(4, "power_station")); Catpra.put(CatPRA.PRA_RFNY, new S57enum(5, "refinery")); Catpra.put(CatPRA.PRA_TYRD, new S57enum(6, "timber_yard"));
		Catpra.put(CatPRA.PRA_FACT, new S57enum(7, "factory")); Catpra.put(CatPRA.PRA_TFRM, new S57enum(8, "tank_farm")); Catpra.put(CatPRA.PRA_WFRM, new S57enum(9, "wind_farm"));
		Catpra.put(CatPRA.PRA_SLAG, new S57enum(10, "slag"));
	}
	public enum CatPYL { PYL_UNKN, PYL_POWR, PYL_TELE, PYL_AERL, PYL_BRDG, PYL_PIER } 
	private static final EnumMap<CatPYL, S57enum> Catpyl = new EnumMap<CatPYL, S57enum>(CatPYL.class); static { Catpyl.put(CatPYL.PYL_UNKN, new S57enum(0, ""));
		Catpyl.put(CatPYL.PYL_POWR, new S57enum(1, "power")); Catpyl.put(CatPYL.PYL_TELE, new S57enum(2, "telecom")); Catpyl.put(CatPYL.PYL_AERL, new S57enum(3, "aerial"));
		Catpyl.put(CatPYL.PYL_BRDG, new S57enum(4, "bridge")); Catpyl.put(CatPYL.PYL_PIER, new S57enum(5, "bridge_pier"));
	}
	public enum CatQUA { QUA_UNKN, QUA_A, QUA_B, QUA_C, QUA_D, QUA_E, QUA_NEVL } 
	private static final EnumMap<CatQUA, S57enum> Catqua = new EnumMap<CatQUA, S57enum>(CatQUA.class); static { Catqua.put(CatQUA.QUA_UNKN, new S57enum(0, ""));
		Catqua.put(CatQUA.QUA_A, new S57enum(1, "a")); Catqua.put(CatQUA.QUA_B, new S57enum(2, "b")); Catqua.put(CatQUA.QUA_C, new S57enum(3, "c"));
		Catqua.put(CatQUA.QUA_D, new S57enum(4, "d")); Catqua.put(CatQUA.QUA_E, new S57enum(5, "e")); Catqua.put(CatQUA.QUA_NEVL, new S57enum(6, "not_evaluated"));
	}
	public enum CatRAS { RAS_UNKN, RAS_SURV, RAS_COST }
	private static final EnumMap<CatRAS, S57enum> Catras = new EnumMap<CatRAS, S57enum>(CatRAS.class); static { Catras.put(CatRAS.RAS_UNKN, new S57enum(0, ""));
		Catras.put(CatRAS.RAS_SURV, new S57enum(1, "surveillance")); Catras.put(CatRAS.RAS_COST, new S57enum(2, "coast"));
	}
	public enum CatRTB { RTB_UNKN, RTB_RAMK, RTB_RACN, RTB_LDG }
	private static final EnumMap<CatRTB, S57enum> Catrtb = new EnumMap<CatRTB, S57enum>(CatRTB.class); static {Catrtb.put(CatRTB.RTB_UNKN, new S57enum(0, ""));
		Catrtb.put(CatRTB.RTB_RAMK, new S57enum(1, "ramark")); Catrtb.put(CatRTB.RTB_RACN, new S57enum(2, "racon")); Catrtb.put(CatRTB.RTB_LDG, new S57enum(3, "leading"));
	}
	public enum CatROS { ROS_UNKN, ROS_OMNI, ROS_DIRL, ROS_ROTP, ROS_CNSL, ROS_RDF, ROS_QTA, ROS_AERO, ROS_DECA, ROS_LORN, ROS_DAPS, ROS_TORN, ROS_OMGA, ROS_SYLD, ROS_CHKA } 
	private static final EnumMap<CatROS, S57enum> Catros = new EnumMap<CatROS, S57enum>(CatROS.class); static { Catros.put(CatROS.ROS_UNKN, new S57enum(0, ""));
		Catros.put(CatROS.ROS_OMNI, new S57enum(1, "omnidirectional")); Catros.put(CatROS.ROS_DIRL, new S57enum(2, "directional"));	Catros.put(CatROS.ROS_ROTP, new S57enum(3, "rotating_pattern"));
		Catros.put(CatROS.ROS_CNSL, new S57enum(4, "consol")); Catros.put(CatROS.ROS_RDF, new S57enum(5, "rdf")); Catros.put(CatROS.ROS_QTA, new S57enum(6, "qtg"));
		Catros.put(CatROS.ROS_AERO, new S57enum(7, "aeronautical")); Catros.put(CatROS.ROS_DECA, new S57enum(8, "decca")); Catros.put(CatROS.ROS_LORN, new S57enum(9, "loran"));
		Catros.put(CatROS.ROS_DAPS, new S57enum(10, "dgps")); Catros.put(CatROS.ROS_TORN, new S57enum(11, "toran")); Catros.put(CatROS.ROS_OMGA, new S57enum(12, "omega"));
		Catros.put(CatROS.ROS_SYLD, new S57enum(13, "syledis")); Catros.put(CatROS.ROS_CHKA, new S57enum(14, "chaika"));
	}
	public enum CatTRK { TRK_UNKN, TRK_FIXM, TRK_NFXM }
	private static final EnumMap<CatTRK, S57enum> Cattrk = new EnumMap<CatTRK, S57enum>(CatTRK.class); static { Cattrk.put(CatTRK.TRK_UNKN, new S57enum(0, ""));
		Cattrk.put(CatTRK.TRK_FIXM, new S57enum(1, "fixed_marks")); Cattrk.put(CatTRK.TRK_NFXM, new S57enum(2, "no_fixed_marks"));
	}
	public enum CatRSC { RSC_UNKN, RSC_LIFB, RSC_ROKT, RSC_LBRK, RSC_RFSW, RSC_RFIT, RSC_LBOM, RSC_RDIO, RSC_FSTA } 
	private static final EnumMap<CatRSC, S57enum> Catrsc = new EnumMap<CatRSC, S57enum>(CatRSC.class); static { Catrsc.put(CatRSC.RSC_UNKN, new S57enum(0, ""));
		Catrsc.put(CatRSC.RSC_LIFB, new S57enum(1, "lifeboat")); Catrsc.put(CatRSC.RSC_ROKT, new S57enum(2, "rocket")); Catrsc.put(CatRSC.RSC_LBRK, new S57enum(3, "lifeboat_rocket"));
		Catrsc.put(CatRSC.RSC_RFSW, new S57enum(4, "refuge_shipwrecked")); Catrsc.put(CatRSC.RSC_RFIT, new S57enum(5, "refuge_intertidal")); Catrsc.put(CatRSC.RSC_LBOM, new S57enum(6, "lifeboat_on_mooring"));
		Catrsc.put(CatRSC.RSC_RDIO, new S57enum(7, "radio")); Catrsc.put(CatRSC.RSC_FSTA, new S57enum(8, "first_aid"));
	}
	public enum CatREA { REA_UNKN, REA_SFTY, REA_NANC, REA_NFSH, REA_NATR, REA_BRDS, REA_GRSV, REA_SEAL, REA_DEGR, REA_MILY, REA_HIST, REA_INST,
		REA_NASF, REA_STRD, REA_MINE, REA_NDIV, REA_TBAV, REA_PROH, REA_SWIM, REA_WAIT, REA_RSCH, REA_DREG, REA_FSNC, REA_ERES, REA_NWAK, REA_SWNG, REA_WSKI } 
	private static final EnumMap<CatREA, S57enum> Catrea = new EnumMap<CatREA, S57enum>(CatREA.class); static { Catrea.put(CatREA.REA_UNKN, new S57enum(0, ""));
		Catrea.put(CatREA.REA_SFTY, new S57enum(1, "safety")); Catrea.put(CatREA.REA_NANC, new S57enum(2, "no_anchoring")); Catrea.put(CatREA.REA_NFSH, new S57enum(3, "no_fishing"));
		Catrea.put(CatREA.REA_NATR, new S57enum(4, "nature_reserve")); Catrea.put(CatREA.REA_BRDS, new S57enum(5, "bird_sanctuary")); Catrea.put(CatREA.REA_GRSV, new S57enum(6, "game_reserve"));
		Catrea.put(CatREA.REA_SEAL, new S57enum(7, "seal_sanctuary")); Catrea.put(CatREA.REA_DEGR, new S57enum(8, "degaussing_range")); Catrea.put(CatREA.REA_MILY, new S57enum(9, "military"));
	  Catrea.put(CatREA.REA_HIST, new S57enum(10, "historic_wreck")); Catrea.put(CatREA.REA_INST, new S57enum(11, "inshore_traffic")); Catrea.put(CatREA.REA_NASF, new S57enum(12, "navigational_aid_safety"));
	  Catrea.put(CatREA.REA_STRD, new S57enum(13, "stranding_danger")); Catrea.put(CatREA.REA_MINE, new S57enum(14, "minefield")); Catrea.put(CatREA.REA_NDIV, new S57enum(15, "no_diving"));
	  Catrea.put(CatREA.REA_TBAV, new S57enum(16, "to_be_avoided")); Catrea.put(CatREA.REA_PROH, new S57enum(17, "prohibited")); Catrea.put(CatREA.REA_SWIM, new S57enum(18, "swimming"));
	  Catrea.put(CatREA.REA_WAIT, new S57enum(19, "waiting")); Catrea.put(CatREA.REA_RSCH, new S57enum(20, "research")); Catrea.put(CatREA.REA_DREG, new S57enum(21, "dredging"));
	  Catrea.put(CatREA.REA_FSNC, new S57enum(22, "fish_sanctuary")); Catrea.put(CatREA.REA_ERES, new S57enum(23, "ecological_reserve")); Catrea.put(CatREA.REA_NWAK, new S57enum(24, "no_wake"));
	  Catrea.put(CatREA.REA_SWNG, new S57enum(25, "swinging")); Catrea.put(CatREA.REA_WSKI, new S57enum(26, "water_skiing"));
	}
	public enum CatROD { ROD_UNKN, ROD_MWAY, ROD_MAJR, ROD_MINR, ROD_TRAK, ROD_MAJS, ROD_MINS, ROD_CRSG, ROD_PATH } 
	private static final EnumMap<CatROD, S57enum> Catrod = new EnumMap<CatROD, S57enum>(CatROD.class); static { Catrod.put(CatROD.ROD_UNKN, new S57enum(0, ""));
		Catrod.put(CatROD.ROD_MWAY, new S57enum(1, "motorway")); Catrod.put(CatROD.ROD_MAJR, new S57enum(2, "major_road")); Catrod.put(CatROD.ROD_MINR, new S57enum(3, "minor_road"));
		Catrod.put(CatROD.ROD_TRAK, new S57enum(4, "track")); Catrod.put(CatROD.ROD_MAJS, new S57enum(5, "major_street")); Catrod.put(CatROD.ROD_MINS, new S57enum(6, "minor_street"));
		Catrod.put(CatROD.ROD_CRSG, new S57enum(7, "crossing")); Catrod.put(CatROD.ROD_PATH, new S57enum(8, "path"));
	}
	public enum CatRUN { RUN_UNKN, RUN_AERP, RUN_HELI }
	private static final EnumMap<CatRUN, S57enum> Catrun = new EnumMap<CatRUN, S57enum>(CatRUN.class); static { Catrun.put(CatRUN.RUN_UNKN, new S57enum(0, ""));
		Catrun.put(CatRUN.RUN_AERP, new S57enum(1, "aeroplane")); Catrun.put(CatRUN.RUN_HELI, new S57enum(2, "helicopter"));
	}
	public enum CatSEA { SEA_UNKN, SEA_GENL, SEA_GAT, SEA_BANK, SEA_DEEP, SEA_BAY, SEA_TRCH, SEA_BASN, SEA_MDFT, SEA_REEF, SEA_LEDG, SEA_CNYN, SEA_NRRW, SEA_SHOL,
		SEA_KNOL, SEA_RIDG, SEA_SMNT, SEA_PNCL, SEA_APLN, SEA_PLTU, SEA_SPUR, SEA_SHLF, SEA_TRGH, SEA_SDDL, SEA_AHLL, SEA_APRN, SEA_AAPN, SEA_BLND, SEA_CMGN, SEA_CRIS,
		SEA_ESCT, SEA_FAN, SEA_FZON, SEA_GAP, SEA_GUYT, SEA_HILL, SEA_HOLE, SEA_LEVE, SEA_MVLY, SEA_MOAT, SEA_MTNS, SEA_PEAK, SEA_PVNC, SEA_RISE, SEA_SCNL, SEA_SCHN,
		SEA_SEDG, SEA_SILL, SEA_SLOP, SEA_TRRC, SEA_VLLY, SEA_CANL, SEA_LAKE, SEA_RIVR, SEA_RECH }
	private static final EnumMap<CatSEA, S57enum> Catsea = new EnumMap<CatSEA, S57enum>(CatSEA.class); static { Catsea.put(CatSEA.SEA_UNKN, new S57enum(0, ""));
		Catsea.put(CatSEA.SEA_GENL, new S57enum(1, "general")); Catsea.put(CatSEA.SEA_GAT, new S57enum(2, "gat")); Catsea.put(CatSEA.SEA_BANK, new S57enum(3, "bank"));
		Catsea.put(CatSEA.SEA_DEEP, new S57enum(4, "deep")); Catsea.put(CatSEA.SEA_BAY, new S57enum(5, "bay")); Catsea.put(CatSEA.SEA_TRCH, new S57enum(6, "trench"));
		Catsea.put(CatSEA.SEA_BASN, new S57enum(7, "basin")); Catsea.put(CatSEA.SEA_MDFT, new S57enum(8, "mud_flats")); Catsea.put(CatSEA.SEA_REEF, new S57enum(9, "reef"));
		Catsea.put(CatSEA.SEA_LEDG, new S57enum(10, "ledge")); Catsea.put(CatSEA.SEA_CNYN, new S57enum(11, "canyon")); Catsea.put(CatSEA.SEA_NRRW, new S57enum(12, "narrows"));
		Catsea.put(CatSEA.SEA_SHOL, new S57enum(13, "shoal")); Catsea.put(CatSEA.SEA_KNOL, new S57enum(14, "knoll")); Catsea.put(CatSEA.SEA_RIDG, new S57enum(15, "ridge"));
		Catsea.put(CatSEA.SEA_SMNT, new S57enum(16, "seamount")); Catsea.put(CatSEA.SEA_PNCL, new S57enum(17, "pinnacle")); Catsea.put(CatSEA.SEA_APLN, new S57enum(18, "abyssal_plain"));
		Catsea.put(CatSEA.SEA_PLTU, new S57enum(19, "plateau")); Catsea.put(CatSEA.SEA_SPUR, new S57enum(20, "spur")); Catsea.put(CatSEA.SEA_SHLF, new S57enum(21, "shelf"));
		Catsea.put(CatSEA.SEA_TRGH, new S57enum(22, "trough")); Catsea.put(CatSEA.SEA_SDDL, new S57enum(23, "saddle")); Catsea.put(CatSEA.SEA_AHLL, new S57enum(24, "abyssal_hills"));
		Catsea.put(CatSEA.SEA_APRN, new S57enum(25, "apron")); Catsea.put(CatSEA.SEA_AAPN, new S57enum(26, "archipelagic_apron")); Catsea.put(CatSEA.SEA_BLND, new S57enum(27, "borderland"));
		Catsea.put(CatSEA.SEA_CMGN, new S57enum(28, "continental_margin")); Catsea.put(CatSEA.SEA_CRIS, new S57enum(29, "continental_rise")); Catsea.put(CatSEA.SEA_ESCT, new S57enum(30, "escarpment"));
		Catsea.put(CatSEA.SEA_FAN, new S57enum(31, "fan")); Catsea.put(CatSEA.SEA_FZON, new S57enum(32, "fracture_zone")); Catsea.put(CatSEA.SEA_GAP, new S57enum(33, "gap"));
		Catsea.put(CatSEA.SEA_GUYT, new S57enum(34, "guyot")); Catsea.put(CatSEA.SEA_HILL, new S57enum(35, "hill")); Catsea.put(CatSEA.SEA_HOLE, new S57enum(36, "hole"));
		Catsea.put(CatSEA.SEA_LEVE, new S57enum(37, "levee")); Catsea.put(CatSEA.SEA_MVLY, new S57enum(38, "median valley")); Catsea.put(CatSEA.SEA_MOAT, new S57enum(39, "moat"));
		Catsea.put(CatSEA.SEA_MTNS, new S57enum(40, "mountains")); Catsea.put(CatSEA.SEA_PEAK, new S57enum(41, "peak")); Catsea.put(CatSEA.SEA_PVNC, new S57enum(42, "province"));
		Catsea.put(CatSEA.SEA_RISE, new S57enum(43, "rise")); Catsea.put(CatSEA.SEA_SCNL, new S57enum(44, "sea channel")); Catsea.put(CatSEA.SEA_SCHN, new S57enum(45, "seamount_chain"));
		Catsea.put(CatSEA.SEA_SEDG, new S57enum(46, "shelf-edge")); Catsea.put(CatSEA.SEA_SILL, new S57enum(47, "sill")); Catsea.put(CatSEA.SEA_SLOP, new S57enum(48, "slope"));
		Catsea.put(CatSEA.SEA_TRRC, new S57enum(49, "terrace")); Catsea.put(CatSEA.SEA_VLLY, new S57enum(50, "valley")); Catsea.put(CatSEA.SEA_CANL, new S57enum(51, "canal")); 
		Catsea.put(CatSEA.SEA_LAKE, new S57enum(52, "lake")); Catsea.put(CatSEA.SEA_RIVR, new S57enum(53, "river")); Catsea.put(CatSEA.SEA_RECH, new S57enum(54, "reach"));
	}
	public enum CatSLC { SLC_UNKN, SLC_BWTR, SLC_GRYN, SLC_MOLE, SLC_PIER, SLC_PPER, SLC_WHRF, SLC_TWAL, SLC_RPRP, SLC_RVMT, SLC_SWAL, SLC_LSTP,
		SLC_RAMP, SLC_SWAY, SLC_FNDR, SLC_SFWF,  SLC_OFWF,  SLC_LRMP,  SLC_LWAL } 
	private static final EnumMap<CatSLC, S57enum> Catslc = new EnumMap<CatSLC, S57enum>(CatSLC.class); static { Catslc.put(CatSLC.SLC_UNKN, new S57enum(0, ""));
		Catslc.put(CatSLC.SLC_BWTR, new S57enum(1, "breakwater")); Catslc.put(CatSLC.SLC_GRYN, new S57enum(2, "groyne")); Catslc.put(CatSLC.SLC_MOLE, new S57enum(3, "mole"));
		Catslc.put(CatSLC.SLC_PIER, new S57enum(4, "pier")); Catslc.put(CatSLC.SLC_PPER, new S57enum(5, "promenade_pier")); Catslc.put(CatSLC.SLC_WHRF, new S57enum(6, "wharf"));
		Catslc.put(CatSLC.SLC_TWAL, new S57enum(7, "training_wall")); Catslc.put(CatSLC.SLC_RPRP, new S57enum(8, "rip_rap")); Catslc.put(CatSLC.SLC_RVMT, new S57enum(9, "revetment"));
		Catslc.put(CatSLC.SLC_SWAL, new S57enum(10, "sea_wall")); Catslc.put(CatSLC.SLC_LSTP, new S57enum(11, "landing_steps")); Catslc.put(CatSLC.SLC_RAMP, new S57enum(12, "ramp"));
		Catslc.put(CatSLC.SLC_SWAY, new S57enum(13, "slipway")); Catslc.put(CatSLC.SLC_FNDR, new S57enum(14, "fender")); Catslc.put(CatSLC.SLC_SFWF, new S57enum(15, "solid_face_wharf"));
		Catslc.put(CatSLC.SLC_OFWF, new S57enum(16, "open_face_wharf")); Catslc.put(CatSLC.SLC_LRMP, new S57enum(17, "log_ramp")); Catslc.put(CatSLC.SLC_LWAL, new S57enum(18, "lock_wall"));
	}
	public enum CatSIT { SIT_UNKN, SIT_PRTC, SIT_PRTE, SIT_IPT, SIT_BRTH, SIT_DOCK, SIT_LOCK, SIT_FLDB, SIT_BRDG, SIT_DRDG, SIT_TCLT }
	private static final EnumMap<CatSIT, S57enum>  Catsit = new EnumMap<CatSIT, S57enum>(CatSIT.class); static {Catsit.put(CatSIT.SIT_UNKN, new S57enum(0, ""));
		Catsit.put(CatSIT.SIT_PRTC, new S57enum(1, "port_control")); Catsit.put(CatSIT.SIT_PRTE, new S57enum(2, "port_entry_departure")); Catsit.put(CatSIT.SIT_IPT, new S57enum(3, "ipt"));
		Catsit.put(CatSIT.SIT_BRTH, new S57enum(4, "berthing")); Catsit.put(CatSIT.SIT_DOCK, new S57enum(5, "dock")); Catsit.put(CatSIT.SIT_LOCK, new S57enum(6, "lock"));
		Catsit.put(CatSIT.SIT_FLDB, new S57enum(7, "flood_barrage")); Catsit.put(CatSIT.SIT_BRDG, new S57enum(8, "bridge_passage")); Catsit.put(CatSIT.SIT_DRDG, new S57enum(9, "dredging"));
		Catsit.put(CatSIT.SIT_TCLT, new S57enum(10, "traffic_control_light"));
	}
	public enum CatSIW { SIW_UNKN, SIW_DNGR, SIW_OBST, SIW_CABL, SIW_MILY, SIW_DSTR, SIW_WTHR, SIW_STRM, SIW_ICE, SIW_TIME, SIW_TIDE, SIW_TSTR,
	  SIW_TIDG, SIW_TIDS, SIW_DIVE, SIW_WTLG, SIW_VRCL, SIW_DPTH }
	private static final EnumMap<CatSIW, S57enum>  Catsiw = new EnumMap<CatSIW, S57enum>(CatSIW.class); static {Catsiw.put(CatSIW.SIW_UNKN, new S57enum(0, ""));
		Catsiw.put(CatSIW.SIW_DNGR, new S57enum(1, "danger")); Catsiw.put(CatSIW.SIW_OBST, new S57enum(2, "maritime_obstruction")); Catsiw.put(CatSIW.SIW_CABL, new S57enum(3, "cable"));
		Catsiw.put(CatSIW.SIW_MILY, new S57enum(4, "military")); Catsiw.put(CatSIW.SIW_DSTR, new S57enum(5, "distress")); Catsiw.put(CatSIW.SIW_WTHR, new S57enum(6, "weather"));
		Catsiw.put(CatSIW.SIW_STRM, new S57enum(7, "storm")); Catsiw.put(CatSIW.SIW_ICE, new S57enum(8, "ice")); Catsiw.put(CatSIW.SIW_TIME, new S57enum(9, "time"));
		Catsiw.put(CatSIW.SIW_TIDE, new S57enum(10, "tide")); Catsiw.put(CatSIW.SIW_TSTR, new S57enum(11, "tidal_stream")); Catsiw.put(CatSIW.SIW_TIDG, new S57enum(12, "tide_gauge"));
	  Catsiw.put(CatSIW.SIW_TIDS, new S57enum(13, "tide_scale")); Catsiw.put(CatSIW.SIW_DIVE, new S57enum(14, "diving")); Catsiw.put(CatSIW.SIW_WTLG, new S57enum(15, "water_level_gauge"));
	  Catsiw.put(CatSIW.SIW_VRCL, new S57enum(16, "vertical_clearance")); Catsiw.put(CatSIW.SIW_DPTH, new S57enum(18, "depth"));
	}
	public enum CatSIL { SIL_UNKN, SIL_SILO, SIL_TANK, SIL_GRNE, SIL_WTRT } 
	private static final EnumMap<CatSIL, S57enum> Catsil = new EnumMap<CatSIL, S57enum>(CatSIL.class); static { Catsil.put(CatSIL.SIL_UNKN, new S57enum(0, ""));
		Catsil.put(CatSIL.SIL_SILO, new S57enum(1, "silo")); Catsil.put(CatSIL.SIL_TANK, new S57enum(2, "tank")); Catsil.put(CatSIL.SIL_GRNE, new S57enum(3, "grain_elevator"));
		Catsil.put(CatSIL.SIL_WTRT, new S57enum(4, "water_tower"));
	}
	public enum CatSLO { SLO_UNKN, SLO_CUTG, SLO_EMBK, SLO_DUNE, SLO_HILL, SLO_PINO, SLO_CLIF, SLO_SCRE } 
	private static final EnumMap<CatSLO, S57enum> Catslo = new EnumMap<CatSLO, S57enum>(CatSLO.class); static { Catslo.put(CatSLO.SLO_UNKN, new S57enum(0, ""));
		Catslo.put(CatSLO.SLO_CUTG, new S57enum(1, "cutting")); Catslo.put(CatSLO.SLO_EMBK, new S57enum(2, "embankment")); Catslo.put(CatSLO.SLO_DUNE, new S57enum(3, "dune"));
		Catslo.put(CatSLO.SLO_HILL, new S57enum(4, "hill")); Catslo.put(CatSLO.SLO_PINO, new S57enum(5, "pingo")); Catslo.put(CatSLO.SLO_CLIF, new S57enum(6, "cliff")); Catslo.put(CatSLO.SLO_SCRE, new S57enum(7, "scree"));
	}
	public enum CatSCF { SCF_UNKN, SCF_VBTH, SCF_CLUB, SCF_BHST, SCF_SMKR, SCF_BTYD, SCF_INN, SCF_RSRT, SCF_CHDR, SCF_PROV, SCF_DCTR, SCF_PHRM,
		SCF_WTRT, SCF_FUEL, SCF_ELEC, SCF_BGAS, SCF_SHWR, SCF_LAUN, SCF_WC, SCF_POST, SCF_TELE, SCF_REFB, SCF_CARP, SCF_BTPK, SCF_CRVN, SCF_CAMP,
		SCF_PMPO, SCF_EMRT, SCF_SLPW, SCF_VMOR, SCF_SCRB, SCF_PCNC, SCF_MECH, SCF_SECS }
	private static final EnumMap<CatSCF, S57enum> Catscf = new EnumMap<CatSCF, S57enum>(CatSCF.class); static { Catscf.put(CatSCF.SCF_UNKN, new S57enum(0, ""));
		Catscf.put(CatSCF.SCF_VBTH, new S57enum(1, "visitor_berth")); Catscf.put(CatSCF.SCF_CLUB, new S57enum(2, "nautical_club")); Catscf.put(CatSCF.SCF_BHST, new S57enum(3, "boat_hoist"));
		Catscf.put(CatSCF.SCF_SMKR, new S57enum(4, "sailmaker")); Catscf.put(CatSCF.SCF_BTYD, new S57enum(5, "boatyard")); Catscf.put(CatSCF.SCF_INN, new S57enum(6, "public_inn"));
		Catscf.put(CatSCF.SCF_RSRT, new S57enum(7, "restaurant")); Catscf.put(CatSCF.SCF_CHDR, new S57enum(8, "chandler")); Catscf.put(CatSCF.SCF_PROV, new S57enum(9, "provisions"));
		Catscf.put(CatSCF.SCF_DCTR, new S57enum(10, "doctor")); Catscf.put(CatSCF.SCF_PHRM, new S57enum(11, "pharmacy")); Catscf.put(CatSCF.SCF_WTRT, new S57enum(12, "water_tap"));
	  Catscf.put(CatSCF.SCF_FUEL, new S57enum(13, "fuel_station")); Catscf.put(CatSCF.SCF_ELEC, new S57enum(14, "electricity")); Catscf.put(CatSCF.SCF_BGAS, new S57enum(15, "bottle_gas"));
	  Catscf.put(CatSCF.SCF_SHWR, new S57enum(16, "showers")); Catscf.put(CatSCF.SCF_LAUN, new S57enum(17, "laundrette")); Catscf.put(CatSCF.SCF_WC, new S57enum(18, "toilets"));
	  Catscf.put(CatSCF.SCF_POST, new S57enum(19, "post_box")); Catscf.put(CatSCF.SCF_TELE, new S57enum(20, "telephone")); Catscf.put(CatSCF.SCF_REFB, new S57enum(21, "refuse_bin"));
	  Catscf.put(CatSCF.SCF_CARP, new S57enum(22, "car_park")); Catscf.put(CatSCF.SCF_BTPK, new S57enum(23, "boat_trailers_park")); Catscf.put(CatSCF.SCF_CRVN, new S57enum(24, "caravan_site"));
	  Catscf.put(CatSCF.SCF_CAMP, new S57enum(25, "camping_site"));  Catscf.put(CatSCF.SCF_PMPO, new S57enum(26, "pump-out")); Catscf.put(CatSCF.SCF_EMRT, new S57enum(27, "emergency_telephone"));
	  Catscf.put(CatSCF.SCF_SLPW, new S57enum(28, "slipway")); Catscf.put(CatSCF.SCF_VMOR, new S57enum(29, "visitors_mooring")); Catscf.put(CatSCF.SCF_SCRB, new S57enum(30, "scrubbing_berth"));
	  Catscf.put(CatSCF.SCF_PCNC, new S57enum(31, "picnic_area")); Catscf.put(CatSCF.SCF_MECH, new S57enum(32, "mechanics_workshop")); Catscf.put(CatSCF.SCF_SECS, new S57enum(33, "security_service"));
	}
	public enum CatSPM { SPM_UNKN, SPM_FDGA, SPM_TRGT, SPM_MSHP, SPM_DGRG, SPM_BARG, SPM_CABL, SPM_SPLG, SPM_OFAL, SPM_ODAS, SPM_RCDG, SPM_SPLA, SPM_RCZN, SPM_PRVT, SPM_MOOR, SPM_LNBY, SPM_LDNG, SPM_MDST,
		SPM_NOTC, SPM_TSS, SPM_NANC, SPM_NBRT, SPM_NOTK, SPM_NTWT, SPM_RWAK, SPM_SPDL, SPM_STOP, SPM_WRNG, SPM_SSSN, SPM_RVCL, SPM_MVDT, SPM_RHCL, SPM_SCNT, SPM_BRTH, SPM_OHPC, SPM_CHEG, SPM_TELE, SPM_FCRS,
		SPM_MTRL, SPM_PLIN, SPM_ANCH, SPM_CLRG, SPM_CTRL, SPM_DIVG, SPM_RBCN, SPM_FGND, SPM_YCHT, SPM_HPRT, SPM_GPS, SPM_SLDG, SPM_NENT, SPM_WRKP, SPM_UKPP, SPM_WELH, SPM_CHSP, SPM_MFRM, SPM_AREF } 
	private static final EnumMap<CatSPM, S57enum> Catspm = new EnumMap<CatSPM, S57enum>(CatSPM.class); static { Catspm.put(CatSPM.SPM_UNKN, new S57enum(0, ""));
		Catspm.put(CatSPM.SPM_FDGA, new S57enum(1, "firing_danger_area")); Catspm.put(CatSPM.SPM_TRGT, new S57enum(2, "target")); Catspm.put(CatSPM.SPM_MSHP, new S57enum(3, "marker_ship"));
		Catspm.put(CatSPM.SPM_DGRG, new S57enum(4, "degaussing_range")); Catspm.put(CatSPM.SPM_BARG, new S57enum(5, "barge")); Catspm.put(CatSPM.SPM_CABL, new S57enum(6, "cable"));
		Catspm.put(CatSPM.SPM_SPLG, new S57enum(7, "spoil_ground")); Catspm.put(CatSPM.SPM_OFAL, new S57enum(8, "outfall")); Catspm.put(CatSPM.SPM_ODAS, new S57enum(9, "odas"));
		Catspm.put(CatSPM.SPM_RCDG, new S57enum(10, "recording")); Catspm.put(CatSPM.SPM_SPLA, new S57enum(11, "seaplane_anchorage")); Catspm.put(CatSPM.SPM_RCZN, new S57enum(12, "recreation_zone"));
		Catspm.put(CatSPM.SPM_PRVT, new S57enum(13, "private")); Catspm.put(CatSPM.SPM_MOOR, new S57enum(14, "mooring")); Catspm.put(CatSPM.SPM_LNBY, new S57enum(15, "lanby"));
		Catspm.put(CatSPM.SPM_LDNG, new S57enum(16, "leading")); Catspm.put(CatSPM.SPM_MDST, new S57enum(17, "measured_distance")); Catspm.put(CatSPM.SPM_NOTC, new S57enum(18, "notice"));
		Catspm.put(CatSPM.SPM_TSS, new S57enum(19, "tss")); Catspm.put(CatSPM.SPM_NANC, new S57enum(20, "no_anchoring")); Catspm.put(CatSPM.SPM_NBRT, new S57enum(21, "no_berthing"));
		Catspm.put(CatSPM.SPM_NOTK, new S57enum(22, "no_overtaking")); Catspm.put(CatSPM.SPM_NTWT, new S57enum(23, "no_two-way_traffic")); Catspm.put(CatSPM.SPM_RWAK, new S57enum(24, "reduced_wake"));
		Catspm.put(CatSPM.SPM_SPDL, new S57enum(25, "speed_limit")); Catspm.put(CatSPM.SPM_STOP, new S57enum(26, "stop")); Catspm.put(CatSPM.SPM_WRNG, new S57enum(27, "warning"));
		Catspm.put(CatSPM.SPM_SSSN, new S57enum(28, "sound_ship_siren")); Catspm.put(CatSPM.SPM_RVCL, new S57enum(29, "restricted_vertical_clearance"));
		Catspm.put(CatSPM.SPM_MVDT, new S57enum(30, "maximum_vessel_draught")); Catspm.put(CatSPM.SPM_RHCL, new S57enum(31, "restricted_horizontal_clearance"));
		Catspm.put(CatSPM.SPM_SCNT, new S57enum(32, "strong_current")); Catspm.put(CatSPM.SPM_BRTH, new S57enum(33, "berthing")); Catspm.put(CatSPM.SPM_OHPC, new S57enum(34, "overhead_power_cable"));
		Catspm.put(CatSPM.SPM_CHEG, new S57enum(35, "channel_edge_gradient")); Catspm.put(CatSPM.SPM_TELE, new S57enum(36, "telephone")); Catspm.put(CatSPM.SPM_FCRS, new S57enum(37, "ferry_crossing"));
		Catspm.put(CatSPM.SPM_MTRL, new S57enum(38, "marine_traffic_lights")); Catspm.put(CatSPM.SPM_PLIN, new S57enum(39, "pipeline")); Catspm.put(CatSPM.SPM_ANCH, new S57enum(40, "anchorage"));
		Catspm.put(CatSPM.SPM_CLRG, new S57enum(41, "clearing")); Catspm.put(CatSPM.SPM_CTRL, new S57enum(42, "control")); Catspm.put(CatSPM.SPM_DIVG, new S57enum(43, "diving"));
		Catspm.put(CatSPM.SPM_RBCN, new S57enum(44, "refuge_beacon")); Catspm.put(CatSPM.SPM_FGND, new S57enum(45, "foul_ground")); Catspm.put(CatSPM.SPM_YCHT, new S57enum(46, "yachting"));
		Catspm.put(CatSPM.SPM_HPRT, new S57enum(47, "heliport")); Catspm.put(CatSPM.SPM_GPS, new S57enum(48, "gps")); Catspm.put(CatSPM.SPM_SLDG, new S57enum(49, "seaplane_landing"));
	  Catspm.put(CatSPM.SPM_NENT, new S57enum(50, "no_entry")); Catspm.put(CatSPM.SPM_WRKP, new S57enum(51, "work_in_progress")); Catspm.put(CatSPM.SPM_UKPP, new S57enum(52, "unknown_purpose"));
	  Catspm.put(CatSPM.SPM_WELH, new S57enum(53, "wellhead")); Catspm.put(CatSPM.SPM_CHSP, new S57enum(54, "channel_separation")); Catspm.put(CatSPM.SPM_MFRM, new S57enum(55, "marine_farm"));
	  Catspm.put(CatSPM.SPM_AREF, new S57enum(56, "artificial_reef"));
	}
	public enum CatTSS { TSS_UNKN, TSS_IMOA, TSS_NIMO }
	private static final EnumMap<CatTSS, S57enum> Cattss = new EnumMap<CatTSS, S57enum>(CatTSS.class); static { Cattss.put(CatTSS.TSS_UNKN, new S57enum(0, ""));
		Cattss.put(CatTSS.TSS_IMOA, new S57enum(1, "imo_adopted")); Cattss.put(CatTSS.TSS_NIMO, new S57enum(2, "not_imo_adopted"));
	}
	public enum CatVEG { VEG_UNKN, VEG_GRAS, VEG_PDDY, VEG_BUSH, VEG_DCDW, VEG_CONW, VEG_WOOD, VEG_MGRV, VEG_PARK, VEG_PKLD, VEG_MCRP, VEG_REED, VEG_MOSS,
		VEG_TREE, VEG_EVGT, VEG_CONT, VEG_PLMT, VEG_NPMT, VEG_CSAT, VEG_EUCT, VEG_DCDT, VEG_MRVT, VEG_FLOT }
	private static final EnumMap<CatVEG, S57enum> Catveg = new EnumMap<CatVEG, S57enum>(CatVEG.class); static { Catveg.put(CatVEG.VEG_UNKN, new S57enum(0, ""));
		Catveg.put(CatVEG.VEG_GRAS, new S57enum(1, "grass")); Catveg.put(CatVEG.VEG_PDDY, new S57enum(2, "paddy")); Catveg.put(CatVEG.VEG_BUSH, new S57enum(3, "bush"));
		Catveg.put(CatVEG.VEG_DCDW, new S57enum(4, "deciduous_wood")); Catveg.put(CatVEG.VEG_CONW, new S57enum(5, "coniferous_wood")); Catveg.put(CatVEG.VEG_WOOD, new S57enum(6, "wood"));
		Catveg.put(CatVEG.VEG_MGRV, new S57enum(7, "mangroves")); Catveg.put(CatVEG.VEG_PARK, new S57enum(8, "park")); Catveg.put(CatVEG.VEG_PKLD, new S57enum(9, "parkland"));
		Catveg.put(CatVEG.VEG_MCRP, new S57enum(10, "mixed_crops")); Catveg.put(CatVEG.VEG_REED, new S57enum(11, "reed")); Catveg.put(CatVEG.VEG_MOSS, new S57enum(12, "moss"));
		Catveg.put(CatVEG.VEG_TREE, new S57enum(13, "tree")); Catveg.put(CatVEG.VEG_EVGT, new S57enum(14, "evergreen_tree")); Catveg.put(CatVEG.VEG_CONT, new S57enum(15, "coniferous_tree"));
		Catveg.put(CatVEG.VEG_PLMT, new S57enum(16, "palm_tree")); Catveg.put(CatVEG.VEG_NPMT, new S57enum(17, "nipa_palm_tree")); Catveg.put(CatVEG.VEG_CSAT, new S57enum(18, "casuarina_tree"));
		Catveg.put(CatVEG.VEG_EUCT, new S57enum(19, "eucalypt_tree")); Catveg.put(CatVEG.VEG_DCDT, new S57enum(20, "deciduous_tree")); Catveg.put(CatVEG.VEG_MRVT, new S57enum(21, "mangrove_tree"));
		Catveg.put(CatVEG.VEG_FLOT, new S57enum(22, "filao_tree"));
	}
		 /*
	private static final EnumMap<, S57enum> Catwat = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "breakers")); []={2, "eddies")); []={3, "overfalls")); []={4, "tide_rips"));
	 []={5, "bombora")); }
	private static final EnumMap<, S57enum> Catwed = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "kelp")); []={2, "sea_weed")); []={3, "sea_grass")); []={4, "saragasso")); }
	private static final EnumMap<, S57enum> Catwrk = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "non-dangerous")); []={2, "dangerous")); []={3, "distributed_remains"));
	 []={4, "mast_showing")); []={5, "hull_showing")); }
	private static final EnumMap<, S57enum> Catzoc = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "a1")); []={2, "a2")); []={3, "b")); []={4, "c")); []={5, "d")); []={6, "u")); }
	public enum ColCOL []={ COL_UNK, COL_WHT, COL_BLK, COL_RED, COL_GRN, COL_BLU, COL_YEL, COL_GRY, COL_BRN, COL_AMB, COL_VIO, COL_ORG, COL_MAG, COL_PNK }
	private static final EnumMap<, S57enum> Colour = new EnumMap<, S57enum>(.class); static {[COL_UNK]={0, "")); [COL_WHT]={1, "white")); [COL_BLK]={2, "black")); [COL_RED]={3, "red"));
	 [COL_GRN]={4, "green")); [COL_BLU]={5, "blue")); [COL_YEL]={6, "yellow")); [COL_GRY]={7, "grey"));
	  [COL_BRN]={8, "brown")); [COL_AMB]={9, "amber")); [COL_VIO]={10, "violet")); [COL_ORG]={11, "orange")); [COL_MAG]={12, "magenta")); [COL_PNK]={13, "pink")); }
	public enum ColPAT []={ PAT_UNKN, PAT_HORI, PAT_VERT, PAT_DIAG, PAT_SQUR, PAT_STRP, PAT_BRDR, PAT_CROS, PAT_SALT }
	private static final EnumMap<, S57enum> Colpat = new EnumMap<, S57enum>(.class); static {[PAT_UNKN]={0, "")); [PAT_HORI]={1, "horizontal")); [PAT_VERT]={2, "vertical"));
	 [PAT_DIAG]={3, "diagonal")); [PAT_SQUR]={4, "squared")); [PAT_STRP]={5, "stripes")); [PAT_BRDR]={6, "border"));
	  [PAT_CROS]={7, "cross")); [PAT_SALT]={8, "saltire")); }
	private static final EnumMap<, S57enum> Condtn = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "under_construction")); []={2, "ruined")); []={3, "under_reclamation"));
	 []={4, "wingless")); []={5, "planned_construction")); }
	private static final EnumMap<, S57enum> Conrad = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "conspicuous")); []={2, "not_conspicuous")); []={3, "reflector")); }
	private static final EnumMap<, S57enum> Convis = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "conspicuous")); []={2, "not_conspicuous")); }
	private static final EnumMap<, S57enum> Dunits = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "metres")); []={2, "fathoms_feet")); []={3, "fathoms")); []={4, "fathoms_fractions")); }
	private static final EnumMap<, S57enum> Exclit = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "24h")); []={2, "day")); []={3, "fog")); []={4, "night")); []={5, "warning")); []={6, "storm")); }
	private static final EnumMap<, S57enum> Expsou = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "within")); []={2, "shoaler")); []={3, "deeper")); }
	public enum FncFNC []={ FNC_UNKN, FNC_HBRM, FNC_CSTM, FNC_HLTH, FNC_HOSP, FNC_POST, FNC_HOTL, FNC_RAIL, FNC_POLC, FNC_WPOL, FNC_PILO, FNC_PILL, FNC_BANK,
		FNC_DIST, FNC_TRNS, FNC_FCTY, FNC_POWR, FNC_ADMIN, FNC_EDUC, FNC_CHCH, FNC_CHPL, FNC_TMPL, FNC_PGDA, FNC_SHSH, FNC_BTMP, FNC_MOSQ, FNC_MRBT,
		FNC_LOOK, FNC_COMM, FNC_TV, FNC_RADO, FNC_RADR, FNC_LGHT, FNC_MCWV, FNC_COOL, FNC_OBS, FNC_TMBL, FNC_CLOK, FNC_CTRL, FNC_ASHM, FNC_STAD, FNC_BUSS }
	private static final EnumMap<, S57enum> Functn = new EnumMap<, S57enum>(.class); static {[FNC_UNKN]={0, "")); [FNC_HBRM]={2, "harbour-master")); [FNC_CSTM]={3, "customs")); [FNC_HLTH]={4, "health")); [FNC_HOSP]={5, "hospital")); [FNC_POST]={6, "post_office")); [FNC_HOTL]={7, "hotel"));
	  [FNC_RAIL]={8, "railway_station")); [FNC_POLC]={9, "police_station")); [FNC_WPOL]={10, "water-police_station")); [FNC_PILO]={11, "pilot_office")); [FNC_PILL]={12, "pilot_lookout")); [FNC_BANK]={13, "bank"));
	  [FNC_DIST]={14, "district_control")); [FNC_TRNS]={15, "transit_shed")); [FNC_FCTY]={16, "factory")); [FNC_POWR]={17, "power_station")); [FNC_ADMIN]={18, "administrative")); [FNC_EDUC]={19, "eduCational"));
	  [FNC_CHCH]={20, "church")); [FNC_CHPL]={21, "chapel")); [FNC_TMPL]={22, "temple")); [FNC_PGDA]={23, "pagoda")); [FNC_SHSH]={24, "shinto_shrine")); [FNC_BTMP]={25, "buddhist_temple")); [FNC_MOSQ]={26, "mosque"));
	  [FNC_MRBT]={27, "marabout")); [FNC_LOOK]={28, "lookout")); [FNC_COMM]={29, "communiCation")); [FNC_TV]={30, "television")); [FNC_RADO]={31, "radio")); [FNC_RADR]={32, "radar")); [FNC_LGHT]={33, "light_support"));
	  [FNC_MCWV]={34, "microwave")); [FNC_COOL]={35, "cooling")); [FNC_OBS]={36, "observation")); [FNC_TMBL]={37, "time_ball")); [FNC_CLOK]={38, "clock")); [FNC_CTRL]={39, "control")); [FNC_ASHM]={40, "airship_mooring"));
	  [FNC_STAD]={41, "stadium")); [FNC_BUSS]={42, "bus_station")); }
	private static final EnumMap<, S57enum> Hunits = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "metres")); []={2, "feet")); []={3, "kilometres")); []={4, "hectometres"));
	 []={5, "statute_miles")); []={6, "nautical_miles")); }
	private static final EnumMap<, S57enum> Jrsdtn = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "international")); []={2, "national")); []={3, "national_sub-division")); }
	public enum LitCHR []={ CHR_UNKN, CHR_F, CHR_FL, CHR_LFL, CHR_Q, CHR_VQ, CHR_UQ, CHR_ISO, CHR_OC, CHR_IQ, CHR_IVQ, CHR_IUQ, CHR_MO, CHR_FFL,
		CHR_FLLFL, CHR_OCFL, CHR_FLFL, CHR_ALOC, CHR_ALLFL, CHR_ALFL, CHR_ALGR, CHR_QLFL, CHR_VQLFL, CHR_UQLFL, CHR_AL, CHR_ALFFL }
	private static final EnumMap<, S57enum> Litchr = new EnumMap<, S57enum>(.class); static {[CHR_UNKN]={0, "")); [CHR_F]={1, "F")); [CHR_FL]={2, "Fl")); [CHR_LFL]={3, "LFl")); [CHR_Q]={4, "Q")); [CHR_VQ]={5, "VQ")); [CHR_UQ]={6, "UQ")); [CHR_ISO]={7, "Iso")); [CHR_OC]={8, "Oc"));
	  [CHR_IQ]={9, "IQ")); [CHR_IVQ]={10, "IVQ")); [CHR_IUQ]={11, "IUQ")); [CHR_MO]={12, "Mo")); [CHR_FFL]={13, "FFl")); [CHR_FLLFL]={14, "FlLFl")); [CHR_OCFL]={15, "OcFl")); [CHR_FLFL]={16, "FLFl")); [CHR_ALOC]={17, "Al.Oc"));
	  [CHR_ALLFL]={18, "Al.LFl")); [CHR_ALFL]={19, "Al.Fl")); [CHR_ALGR]={20, "Al.Gr")); [CHR_QLFL]={25, "Q+LFl")); [CHR_VQLFL]={26, "VQ+LFl")); [CHR_UQLFL]={27, "UQ+LFl")); [CHR_AL]={28, "Al")); [CHR_ALFFL]={29, "Al.FFl")); }
	private static final EnumMap<, S57enum> Litvis = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "high")); []={2, "low")); []={3, "faint")); []={4, "intensified"));
	 []={5, "unintensified")); []={6, "restricted")); []={7, "obscured")); []={8, "part_obscured")); }
	private static final EnumMap<, S57enum> Marsys = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "iala-a")); []={2, "iala-b")); []={9, "none")); []={10, "other"));
	 []={11, "cevni")); []={12, "riwr")); }
	private static final EnumMap<, S57enum> Natcon = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "masonry")); []={2, "concreted")); []={3, "loose_boulders"));
	 []={4, "hard-surfaced")); []={5, "unsurfaced")); []={6, "wooden")); []={7, "metal")); []={8, "grp")); []={9, "painted")); }
	private static final EnumMap<, S57enum> Natsur = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "mud")); []={2, "clay")); []={3, "silt")); []={4, "sand")); []={5, "stone"));
	 []={6, "gravel")); []={7, "pebbles")); []={8, "cobbles")); []={9, "rock")); []={11, "lava")); []={14, "coral")); []={17, "shells")); []={18, "boulder")); }
	private static final EnumMap<, S57enum> Natqua = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "fine")); []={2, "medium")); []={3, "coarse")); []={4, "broken"));
	 []={5, "sticky")); []={6, "soft")); []={7, "stiff")); []={8, "volcanic")); []={9, "calcareous")); []={10, "hard")); }
	private static final EnumMap<, S57enum> Prodct = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "oil")); []={2, "gas")); []={3, "water")); []={4, "stone"));
	 []={5, "coal")); []={6, "ore")); []={7, "chemicals")); []={8, "drinking_water")); []={9, "milk")); []={10, "bauxite")); []={11, "coke")); []={12, "iron_ingots")); []={13, "salt"));
	  []={14, "sand")); []={15, "timber")); []={16, "sawdust")); []={17, "scrap")); []={18, "lng")); []={19, "lpg")); []={20, "wine")); []={21, "cement")); []={22, "grain")); }
	private static final EnumMap<, S57enum> Quasou = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "known")); []={2, "unknown")); []={3, "doubtful")); []={4, "unreliable"));
	 []={5, "no_bottom_found")); []={6, "least_known")); []={7, "least_unknown")); []={8, "not_surveyed")); []={9, "not_confirmed")); []={10, "maintained"));
	  []={11, "not_maintained")); }
	private static final EnumMap<, S57enum> Restrn = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "no_anchoring")); []={2, "restricted_anchoring")); []={3, "no_fishing"));
	 []={4, "restricted_fishing")); []={5, "no_trawling")); []={6, "restricted_trawling")); []={7, "no_entry")); []={8, "restricted_entry"));
	  []={9, "no_dredging")); []={10, "restricted_dredging")); []={11, "no_diving")); []={12, "restricted_diving")); []={13, "no_wake")); []={14, "to_be_avoided")); []={15, "no_construction"));
	   []={16, "no_discharging")); []={17, "restricted_discharging"));
	  []={18, "no_ exploration_development")); []={19, "restricted_exploration_development")); []={20, "no_drilling")); []={21, "restricted_drilling")); []={22, "no_historical_artifacts_removal"));
	   []={23, "no_lightering")); []={24, "no_dragging"));
	  []={25, "no_stopping")); []={26, "no_landing")); []={27, "restricted_speed")); []={28, "overtaking prohibited")); []={29, "overtaking of convoys by convoys prohibited"));
	   []={30, "passing or overtaking prohibited"));
	  []={31, "berthing prohibited")); []={32, "berthing restricted")); []={33, "making fast prohibited")); []={34, "making fast restricted")); []={35, "turning prohibited"));
	   []={36, "restricted fairway depth")); }
	private static final EnumMap<, S57enum> Siggen = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "automatic")); []={2, "wave")); []={3, "hand")); []={4, "wind")); }
	private static final EnumMap<, S57enum> Status = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "permanent")); []={2, "occasional")); []={3, "recommended"));
	 []={4, "not_in_use")); []={5, "intermittent")); []={6, "reserved")); []={7, "tempory")); []={8, "private")); []={9, "mandatory")); []={11, "extinguished"));
	  []={12, "illuminated")); []={13, "historic")); []={14, "public")); []={15, "synchronised")); []={16, "watched")); []={17, "unwatched")); []={18, "existence_doubtful"));
	   []={19, "on_request")); []={20, "drop_away")); []={21, "rising")); []={22, "increasing"));
	  []={23, "decreasing")); []={24, "strong")); []={25, "good")); []={26, "moderately")); []={27, "poor")); }
	private static final EnumMap<, S57enum> Surtyp = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "sketch")); []={2, "controlled")); []={4, "examination"));
	 []={5, "passage")); []={6, "remote")); }
	private static final EnumMap<, S57enum> Tecsou = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "echo-sounder")); []={2, "side-scan_sonar")); []={3, "multi-beam"));
	 []={4, "diver")); []={5, "lead-line")); []={6, "wire-drag")); []={7, "laser")); []={8, "vertical_acoustic")); []={9, "electromagnetic"));
	  []={10, "photogrammetry")); []={11, "satellite")); []={12, "levelling")); []={13, "side-scan_sonar")); []={14, "computer")); }
	public enum TopSHP []={ TOP_UNKN, TOP_CONE, TOP_ICONE, TOP_SPHR, TOP_ISD, TOP_CAN, TOP_BORD, TOP_SALT, TOP_CROS, TOP_CUBE, TOP_WEST, TOP_EAST, TOP_RHOM,
	  TOP_NORTH, TOP_SOUTH, TOP_BESM, TOP_IBESM, TOP_FLAG, TOP_SPRH, TOP_SQUR, TOP_HRECT, TOP_VRECT, TOP_TRAP, TOP_ITRAP, TOP_TRI, TOP_ITRI, TOP_CIRC,
	  TOP_CRSS, TOP_T, TOP_TRCL, TOP_CRCL, TOP_RHCL, TOP_CLTR, TOP_OTHR }
	private static final EnumMap<, S57enum> Topshp = new EnumMap<, S57enum>(.class); static {[TOP_UNKN]={0, "")); [TOP_CONE]={1, "cone, point up")); [TOP_ICONE]={2, "cone, point down"));
	 [TOP_SPHR]={3, "sphere")); [TOP_ISD]={4, "2 spheres")); [TOP_CAN]={5, "cylinder")); [TOP_BORD]={6, "board"));
	  [TOP_SALT]={7, "x-shape")); [TOP_CROS]={8, "cross")); [TOP_CUBE]={9, "cube, point up")); [TOP_WEST]={10, "2 cones point together")); [TOP_EAST]={11, "2 cones base together"));
	   [TOP_RHOM]={12, "rhombus"));
	  [TOP_NORTH]={13, "2 cones up")); [TOP_SOUTH]={14, "2 cones down")); [TOP_BESM]={15, "besom, point up")); [TOP_IBESM]={16, "besom, point down")); [TOP_FLAG]={17, "flag"));
	   [TOP_SPRH]={18, "sphere over rhombus"));
	  [TOP_SQUR]={19, "square")); [TOP_HRECT]={20, "rectangle, horizontal")); [TOP_VRECT]={21, "rectangle, vertical")); [TOP_TRAP]={22, "trapezium, up")); [TOP_ITRAP]={23, "trapezium, down"));
	   [TOP_TRI]={24, "triangle, point up"));
	  [TOP_ITRI]={25, "triangle, point down")); [TOP_CIRC]={26, "circle")); [TOP_CRSS]={27, "2 upright crosses")); [TOP_T]={28, "t-shape")); [TOP_TRCL]={29, "triangle, point up over circle"));
	   [TOP_CRCL]={30, "upright cross over circle"));
	  [TOP_RHCL]={31, "rhombus over circle")); [TOP_CLTR]={32, "circle over triangle, point up")); [TOP_OTHR]={33, "other")); }
	private static final EnumMap<, S57enum> Trafic = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "inbound")); []={2, "outbbound")); []={3, "one-way")); []={4, "two-way")); }
	private static final EnumMap<, S57enum> Watlev = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "part-submerged")); []={2, "dry")); []={3, "submerged")); []={4, "covers"));
	 []={5, "awash")); []={6, "floods")); []={7, "floating")); []={8, "above_mwl")); []={9, "below_mwl")); }
	private static final EnumMap<, S57enum> Cat_ts = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "flood")); []={2, "ebb")); []={3, "other")); }
	private static final EnumMap<, S57enum> Punits = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "metres")); []={2, "degrees")); []={3, "millimetres")); []={4, "feet"));
	 []={5, "cables")); }
	private static final EnumMap<, S57enum> Quapos = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "surveyed")); []={2, "unsurveyed")); []={3, "part-surveyed"));
	 []={4, "approximate")); []={5, "doubtful")); []={6, "unreliable")); []={7, "reported_unsurveyd")); []={8, "unconfirmed")); []={9, "estimated"));
	  []={10, "precise")); []={11, "calculated")); }
	private static final EnumMap<, S57enum> Catachi = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "unrestricted")); []={2, "deep-water")); []={3, "tanker"));
	 []={4, "explosives")); []={5, "quarantine")); []={6, "sea-plane")); []={7, "small_craft")); []={9, "24_hours")); []={10, "pushing-navigation_vessels"));
	  []={11, "non-pushing-navigation_vessels")); }
	private static final EnumMap<, S57enum> Verdat = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "mlws")); []={2, "mllws")); []={3, "msl")); []={4, "llw"));
	 []={5, "mlw")); []={6, "llws")); []={7, "amlws")); []={8, "islw")); []={9, "lws")); []={10, "alat"));  []={11, "nllw")); []={12, "mllw")); []={13, "lw")); []={14, "amlw")); []={15, "amllw"));
	  []={16, "mhw")); []={17, "mhws")); []={18, "hw")); []={19, "amsl")); []={20, "hws")); []={21, "mhhw")); []={22, "eslw")); []={23, "lat")); []={24, "local")); []={25, "igld1985"));
	   []={26, "mlw")); []={27, "llwlt")); []={28, "hhwlt")); []={29, "nhhw")); []={30, "hat"));
	  []={31, "llwrl")); []={32, "lhwrl")); []={33, "lmwrl")); []={34, "ehw_dglw")); []={35, "hshw_dhsw")); []={36, "rlwl_donau")); []={37, "hshw_donau")); []={38, "drlwrl_olr"));
	   []={39, "rpwl")); []={40, "rnbl")); []={41, "ohio_rd")); }
	public enum AddMRK []={ MRK_UNKN, MRK_TOPB, MRK_BOTB, MRK_RTRI, MRK_LTRI, MRK_BTRI	}
	private static final EnumMap<, S57enum> Addmrk = new EnumMap<, S57enum>(.class); static {[MRK_UNKN]={0, "")); [MRK_TOPB]={1, "top_board")); [MRK_BOTB]={2, "bottom_board")); [MRK_RTRI]={3, "right_triangle")); [MRK_LTRI]={4, "left_triangle")); [MRK_BTRI]={5, "bottom_triangle")); }
	private static final EnumMap<, S57enum> Catbnk = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "steep")); []={2, "flat")); []={3, "fastened")); []={4, "unfastened")); }
	public enum Cat_NMK []={ NMK_UNKN, NMK_NENT, NMK_CLSA, NMK_NOVK, NMK_NCOV, NMK_NPAS, NMK_NBRT, NMK_NBLL, NMK_NANK, NMK_NMOR, NMK_NTRN, NMK_NWSH,
		NMK_NPSL, NMK_NPSR, NMK_NMTC, NMK_NSPC, NMK_NWSK, NMK_NSLC, NMK_NUPC, NMK_NSLB, NMK_NWBK, NMK_NHSC, NMK_NLBG, NMK_MVTL, NMK_MVTR, NMK_MVTP,
		NMK_MVTS, NMK_KPTP, NMK_KPTS, NMK_CSTP, NMK_CSTS, NMK_STOP, NMK_SPDL, NMK_SHRN, NMK_KPLO, NMK_GWJN, NMK_GWCS, NMK_MKRC, NMK_LMDP, NMK_LMHR,
		NMK_LMWD, NMK_NAVR, NMK_CHDL, NMK_CHDR, NMK_CHTW, NMK_CHOW, NMK_OPTR, NMK_OPTL, NMK_PRTL, NMK_PRTR, NMK_ENTP, NMK_OVHC, NMK_WEIR, NMK_FERN,
		NMK_FERI, NMK_BRTP, NMK_BTLL, NMK_BTLS, NMK_BTRL, NMK_BTUP, NMK_BTP1, NMK_BTP2, NMK_BTP3, NMK_BTUN, NMK_BTN1, NMK_BTN2, NMK_BTN3, NMK_BTUM,
		NMK_BTU1, NMK_BTU2, NMK_BTU3, NMK_ANKP, NMK_MORP, NMK_VLBT, NMK_TRNA, NMK_SWWC, NMK_SWWR, NMK_SWWL, NMK_WRSA, NMK_WLSA, NMK_WRSL, NMK_WLSR,
		NMK_WRAL, NMK_WLAR, NMK_MWWC, NMK_MWWJ, NMK_MWAR, NMK_MWAL, NMK_WARL, NMK_WALR, NMK_PEND, NMK_DWTR, NMK_TELE, NMK_MTCP, NMK_SPCP, NMK_WSKP,
		NMK_SLCP, NMK_UPCP, NMK_SLBP, NMK_RADI, NMK_WTBP, NMK_HSCP, NMK_LBGP }
	private static final EnumMap<, S57enum> Catnmk = new EnumMap<, S57enum>(.class); static {[NMK_UNKN]={0, "")); [NMK_NENT]={1, "no_entry")); [NMK_CLSA]={2, "closed_area")); [NMK_NOVK]={3, "no_overtaking")); [NMK_NCOV]={4, "no_convoy_overtaking")); [NMK_NPAS]={5, "no_passing"));
	  [NMK_NBRT]={6, "no_berthing")); [NMK_NBLL]={7, "no_berthing_lateral_limit")); [NMK_NANK]={8, "no_anchoring")); [NMK_NMOR]={9, "no_mooring")); [NMK_NTRN]={10, "no_turning")); [NMK_NWSH]={11, "no_wash"));
	  [NMK_NPSL]={12, "no_passage_left")); [NMK_NPSR]={13, "no_passage_right")); [NMK_NMTC]={14, "no_motor_craft")); [NMK_NSPC]={15, "no_sports_craft")); [NMK_NWSK]={16, "no_waterskiing")); [NMK_NSLC]={17, "no_sailing_craft"));
	  [NMK_NUPC]={18, "no_unpowered_craft")); [NMK_NSLB]={19, "no_sailboards")); [NMK_NWBK]={20, "no_waterbikes")); [NMK_NHSC]={21, "no_high_speeds")); [NMK_NLBG]={22, "no_launching_beaching")); [NMK_MVTL]={23, "move_to_left"));
	  [NMK_MVTR]={24, "move_to_right")); [NMK_MVTP]={25, "move_to_port")); [NMK_MVTS]={26, "move_to_starboard")); [NMK_KPTP]={27, "keep_to_port")); [NMK_KPTS]={28, "keep_to_starboard")); [NMK_CSTP]={29, "cross_to_port"));
	  [NMK_CSTS]={30, "cross_to_starboard")); [NMK_STOP]={31, "stop")); [NMK_SPDL]={32, "speed_limit")); [NMK_SHRN]={33, "sound_horn")); [NMK_KPLO]={34, "keep_lookout")); [NMK_GWJN]={35, "give_way_junction"));
	  [NMK_GWCS]={36, "give_way_crossing")); [NMK_MKRC]={37, "make_radio_contact")); [NMK_LMDP]={38, "limited_depth")); [NMK_LMHR]={39, "limited_headroom")); [NMK_LMWD]={40, "limited_width")); [NMK_NAVR]={41, "navigation_restrictions"));
	  [NMK_CHDL]={42, "channel_distance_left")); [NMK_CHDR]={43, "channel_distance_right")); [NMK_CHTW]={44, "channel_two_way")); [NMK_CHOW]={45, "channel_one_way")); [NMK_OPTR]={46, "opening_to_right"));
	  [NMK_OPTL]={47, "opening_to_left")); [NMK_PRTL]={48, "proceed_to_left")); [NMK_PRTR]={49, "proceed_to_right")); [NMK_ENTP]={50, "entry_permitted")); [NMK_OVHC]={51, "overhead_cable")); [NMK_WEIR]={52, "weir"));
	  [NMK_FERN]={53, "ferry_non_independent"));  [NMK_FERI]={54, "ferry_independent")); [NMK_BRTP]={55, "berthing_permitted")); [NMK_BTLL]={56, "berthing_lateral_limit")); [NMK_BTLS]={57, "berthing_lateral_limits"));
	  [NMK_BTRL]={58, "berth_rafting_limit"));  [NMK_BTUP]={59, "berthing_unmarked_pushing")); [NMK_BTP1]={60, "berthing_marked_pushing_1")); [NMK_BTP2]={61, "berthing_marked_pushing_2")); [NMK_BTP3]={62, "berthing_marked_pushing_3"));
	  [NMK_BTUN]={63, "berthing_unmarked_non-pushing")); [NMK_BTN1]={64, "berthing_marked_non-pushing_1")); [NMK_BTN2]={65, "berthing_marked_non-pushing_2")); [NMK_BTN3]={66, "berthing_marked_non-pushing_3"));
	  [NMK_BTUM]={67, "berthing_unmarked")); [NMK_BTU1]={68, "berthing_marked_1")); [NMK_BTU2]={69, "berthing_marked_2")); [NMK_BTU3]={70, "berthing_marked_3")); [NMK_ANKP]={71, "anchoring_permitted"));[NMK_MORP]={72, "mooring_permitted"));
	  [NMK_VLBT]={73, "vehicle_loading_berth")); [NMK_TRNA]={74, "turning_area")); [NMK_SWWC]={75, "secondary_waterway_crossing")); [NMK_SWWR]={76, "secondary_waterway_right")); [NMK_SWWL]={77, "secondary_waterway_left"));
	  [NMK_WRSA]={78, "main_waterway_right_secondary_ahead")); [NMK_WLSA]={79, "main_waterway_left_secondary_ahead")); [NMK_WRSL]={80, "main_waterway_right_secondary_left")); [NMK_WLSR]={81, "main_waterway_left_secondary_right"));
	  [NMK_WRAL]={82, "main_waterway_right_secondary_ahead_left")); [NMK_WLAR]={83, "main_waterway_left_secondary waterway_ahead_right")); [NMK_MWWC]={84, "main_waterway_crossing")); [NMK_MWWJ]={85, "main_waterway_junction"));
	  [NMK_MWAR]={86, "main_waterway_ahead_right")); [NMK_MWAL]={87, "main_waterway_ahead_left")); [NMK_WARL]={88, "main_waterway_ahead_right_secondary_left")); [NMK_WALR]={89, "main_waterway_ahead_left_secondary_right"));
	  [NMK_PEND]={90, "prohibition_ends")); [NMK_DWTR]={91, "drinking_water")); [NMK_TELE]={92, "telephone")); [NMK_MTCP]={93, "motor_craft_permitted")); [NMK_SPCP]={94, "sport_craft_permitted")); [NMK_WSKP]={95, "waterskiing_permitted"));
	  [NMK_SLCP]={96, "sailing_craft_permitted")); [NMK_UPCP]={97, "unpowered_craft_permitted")); [NMK_SLBP]={98, "sailboards_permitted")); [NMK_RADI]={99, "radio_information"));
	  [NMK_WTBP]={100, "waterbikes_permitted")); [NMK_HSCP]={101, "high_speeds_permitted")); [NMK_LBGP]={102, "launching_beaching_permitted")); }
	private static final EnumMap<, S57enum> Clsdng = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "one_blue")); []={2, "two_blue")); []={3, "three_blue")); []={4, "no_blue")); }
	private static final EnumMap<, S57enum> Dirimp = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "upstream")); []={2, "downstream")); []={3, "left_bank")); []={4, "right_bank")); []={5, "to_harbour")); }
	private static final EnumMap<, S57enum> Fnctnm = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "prohibition")); []={2, "regulation")); []={3, "restriction"));
	 []={4, "recommendation")); []={5, "information")); }
	private static final EnumMap<, S57enum> Bunves = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "bunker_vessel_available")); []={2, "no_bunker_vessel_available")); }
	private static final EnumMap<, S57enum> Catbrt = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "loading")); []={2, "unloading")); []={3, "overnight_accommodation"));
	 []={4, "pushing-navigation")); []={5, "non-pushing-navigation")); []={6, "fleeting")); []={7, "first_class")); []={8, "second_class")); }
	private static final EnumMap<, S57enum> Catbun = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "diesel oil")); []={2, "water")); []={3, "ballast")); }
	private static final EnumMap<, S57enum> Catccl = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "small_vessels")); []={2, "peniche")); []={3, "campine_barge"));
	 []={4, "dortmund-ems_barge")); []={5, "rhine-herne_barge")); []={6, "1-barge_push-tow"));
	  []={7, "2-barge_push-tow_long")); []={8, "2-barge_push-tow_wide")); []={9, "4-barge_push-tow")); []={10, "6-barge_push-tow")); []={11, "no_cemt_class")); }
	private static final EnumMap<, S57enum> Catcom = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "vts_centre")); []={2, "vts_sector")); []={3, "ivs_point")); []={4, "mid")); []={5, "lock")); []={6, "bridge")); []={7, "custom")); []={8, "harbour")); }
	private static final EnumMap<, S57enum> Cathbr = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "custom")); []={2, "refuge")); []={3, "marina")); []={4, "fishing")); []={5, "private")); }
	private static final EnumMap<, S57enum> Catrfd = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "cargo_residue")); []={2, "waste_oil")); []={3, "grey_black_water")); []={4, "domestic_refuse")); }
	private static final EnumMap<, S57enum> Cattml = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "passenger")); []={2, "ferry")); []={3, "transhipment")); []={4, "roro")); }
	private static final EnumMap<, S57enum> Trshgd = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "containers")); []={2, "bulk")); []={3, "oil")); []={4, "fuel"));
	 []={5, "chemicals")); []={6, "liquid")); []={7, "explosive")); []={8, "fish")); []={9, "cars")); []={10, "general")); }
	private static final EnumMap<, S57enum> Catgag = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "staff")); []={2, "recording")); []={3, "recording_remote_access"));
	 []={4, "recording_external_indiCator")); []={5, "recording_remote_access_indiCator")); }
	private static final EnumMap<, S57enum> Reflev = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "baltic")); []={2, "adriatic")); []={3, "amsterdam")); []={4, "msl"));
	 []={5, "other")); []={6, "ngvd29")); []={7, "navd88")); []={8, "msl1912")); []={9, "msl1929")); }
	private static final EnumMap<, S57enum> Catvtr = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "official")); []={2, "private")); []={3, "car_cranes")); []={4, "car_planks")); []={5, "permission_required")); []={6, "locked_gate")); }
	private static final EnumMap<, S57enum> Cattab = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "operational_period")); []={2, "non-operational_period")); }
	private static final EnumMap<, S57enum> Useshp = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "liner_trade")); []={2, "occasional_professional_shipping")); []={3, "leisure")); }
	private static final EnumMap<, S57enum> Catexs = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "lift-lock")); []={2, "aqueduct")); []={3, "sloping_plane_lock"));
	 []={4, "water_slope_lock")); []={5, "other")); }
	private static final EnumMap<, S57enum> Catwwm = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "waterway_right")); []={2, "waterway_left")); []={3, "waterway_separation"));
	 []={4, "channel_right")); []={5, "channel_left")); []={6, "channel_separation")); []={7, "channel_right_bank")); []={8, "channel_left_bank"));
	  []={9, "crossover_right")); []={10, "crossover_left")); []={11, "danger_right")); []={12, "danger_left")); []={13, "turnoff_right")); []={14, "turnoff_left")); []={15, "junction_right"));
	   []={16, "junction_left")); []={17, "harbour_right"));
	  []={18, "harbour_left")); []={19, "bridge_pier")); }
	private static final EnumMap<, S57enum> Lg_spr = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "other")); []={2, "speed_over_ground")); []={3, "speed_through_water")); }
	private static final EnumMap<, S57enum> Lg_wdu = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "other")); []={2, "cubic_metres")); []={3, "tonnes")); }
	private static final EnumMap<, S57enum> Lg_rel = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "other")); []={2, "usage_of_waterway")); []={3, "carriage_of_equipment"));
	 []={4, "task_operation")); }
	private static final EnumMap<, S57enum> Lg_fnc = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "other")); []={2, "prohibited")); []={3, "prohibited_with_exceptions"));
	 []={4, "permitted")); []={5, "permitted_with_exceptions")); []={6, "recommended")); []={7, "not_recommended")); }
	private static final EnumMap<, S57enum> Lc_csi = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "all")); []={2, "other")); []={3, "non-motorized")); []={5, "craft"));
	 []={6, "vessel")); []={7, "inland_waterway")); []={8, "sea-going")); []={9, "motor")); []={10, "motor_tanker")); []={11, "motor_cargo")); []={12, "canal_barge"));
	  []={13, "tug")); []={14, "pusher")); []={15, "barge")); []={16, "tank_barge")); []={17, "dumb_barge")); []={18, "lighter")); []={19, "tank_lighter")); []={20, "cargo_lighter"));
	   []={21, "ship_borne_lighter")); []={22, "passenger")); []={23, "passenger_sailing"));
	  []={24, "day_trip")); []={25, "cabin")); []={26, "high-speed")); []={27, "floating_equipment")); []={28, "worksite")); []={29, "recreational")); []={30, "dinghy"));
	   []={31, "floating_establishment")); []={32, "floating_object")); }
	private static final EnumMap<, S57enum> Lc_cse = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "all")); []={2, "other")); []={3, "non-motorized")); []={5, "craft"));
	 []={6, "vessel")); []={7, "inland_waterway")); []={8, "sea-going")); []={9, "motor")); []={10, "motor_tanker")); []={11, "motor_cargo")); []={12, "canal_barge"));
	  []={13, "tug")); []={14, "pusher")); []={15, "barge")); []={16, "tank_barge")); []={17, "dumb_barge")); []={18, "lighter")); []={19, "tank_lighter")); []={20, "cargo_lighter"));
	   []={21, "ship_borne_lighter")); []={22, "passenger")); []={23, "passenger_sailing"));
	  []={24, "day_trip")); []={25, "cabin")); []={26, "high-speed")); []={27, "floating_equipment")); []={28, "worksite")); []={29, "recreational")); []={30, "dinghy"));
	   []={31, "floating_establishment")); []={32, "floating_object")); }
	private static final EnumMap<, S57enum> Lc_asi = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "all")); []={2, "other")); []={3, "single")); []={5, "convoy"));
	 []={6, "formation")); []={7, "rigid_convoy")); []={8, "pushed_convoy")); []={9, "breasted")); []={10, "towed_convoy")); }
	private static final EnumMap<, S57enum> Lc_ase = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "all")); []={2, "other")); []={3, "single")); []={5, "convoy"));
	 []={6, "formation")); []={7, "rigid_convoy")); []={8, "pushed_convoy")); []={9, "breasted")); []={10, "towed_convoy")); }
	private static final EnumMap<, S57enum> Lc_cci = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "all")); []={2, "other")); []={4, "bulk")); []={5, "dry"));
	 []={6, "liquid")); []={7, "liquid_n")); []={8, "liquid_c")); []={9, "gas")); }
	private static final EnumMap<, S57enum> Lc_cce = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "all")); []={2, "other")); []={4, "bulk")); []={5, "dry"));
	 []={6, "liquid")); []={7, "liquid_n")); []={8, "liquid_c")); []={9, "gas")); }
	private static final EnumMap<, S57enum> Shptyp = new EnumMap<, S57enum>(.class); static { []={0, "")); []={1, "cargo")); []={2, "container")); []={3, "tanker")); []={4, "sailing"));
	 []={5, "fishing")); []={6, "special_purpose.")); []={7, "man_of_war")); []={8, "submarine")); []={9, "high-speed")); []={10, "bulk_carrier"));
	  []={11, "seaplane")); []={12, "tugboat")); []={13, "passenger")); []={14, "ferry")); []={15, "boat")); }
*/
	private static final EnumMap<Att, S57key> keys = new EnumMap<Att, S57key>(Att.class);
	static {
		keys.put(Att.UNKATT, new S57key(Conv.A, null)); keys.put(Att.AGENCY, new S57key(Conv.A, null)); keys.put(Att.BCNSHP, new S57key(Conv.E, Bcnshp));
		keys.put(Att.BUISHP, new S57key(Conv.E, Buishp)); keys.put(Att.BOYSHP, new S57key(Conv.E, Boyshp)); keys.put(Att.BURDEP, new S57key(Conv.F, null));
		keys.put(Att.CALSGN, new S57key(Conv.S, null)); keys.put(Att.CATAIR, new S57key(Conv.L, Catair)); keys.put(Att.CATACH, new S57key(Conv.L, Catach));
		keys.put(Att.CATBRG, new S57key(Conv.L, Catbrg)); keys.put(Att.CATBUA, new S57key(Conv.E, Catbua)); keys.put(Att.CATCBL, new S57key(Conv.E, Catcbl));
		keys.put(Att.CATCAN, new S57key(Conv.E, Catcan)); keys.put(Att.CATCAM, new S57key(Conv.E, Catcam)); keys.put(Att.CATCHP, new S57key(Conv.E, Catchp));
		keys.put(Att.CATCOA, new S57key(Conv.E, Catcoa)); keys.put(Att.CATCTR, new S57key(Conv.E, Catctr)); keys.put(Att.CATCON, new S57key(Conv.E, Catcon));
		keys.put(Att.CATCOV, new S57key(Conv.E, Catcov)); keys.put(Att.CATCRN, new S57key(Conv.E, Catcrn)); keys.put(Att.CATDAM, new S57key(Conv.E, Catdam));
		keys.put(Att.CATDIS, new S57key(Conv.E, Catdis)); keys.put(Att.CATDOC, new S57key(Conv.E, Catdoc)); keys.put(Att.CATDPG, new S57key(Conv.L, Catdpg));
		keys.put(Att.CATFNC, new S57key(Conv.E, Catfnc)); keys.put(Att.CATFRY, new S57key(Conv.E, Catfry)); keys.put(Att.CATFIF, new S57key(Conv.E, Catfif));
		keys.put(Att.CATFOG, new S57key(Conv.E, Catfog)); keys.put(Att.CATFOR, new S57key(Conv.E, Catfor)); keys.put(Att.CATGAT, new S57key(Conv.E, Catgat));
		keys.put(Att.CATHAF, new S57key(Conv.L, Cathaf)); keys.put(Att.CATHLK, new S57key(Conv.L, Cathlk)); keys.put(Att.CATICE, new S57key(Conv.E, Catice));
		keys.put(Att.CATINB, new S57key(Conv.E, Catinb)); keys.put(Att.CATLND, new S57key(Conv.L, Catlnd)); keys.put(Att.CATLMK, new S57key(Conv.L, Catlmk));
		keys.put(Att.CATLAM, new S57key(Conv.E, Catlam)); keys.put(Att.CATLIT, new S57key(Conv.L, Catlit)); keys.put(Att.CATMFA, new S57key(Conv.E, Catmfa));
		keys.put(Att.CATMPA, new S57key(Conv.L, Catmpa)); keys.put(Att.CATMOR, new S57key(Conv.E, Catmor)); keys.put(Att.CATNAV, new S57key(Conv.E, Catnav));
		keys.put(Att.CATOBS, new S57key(Conv.E, Catobs)); keys.put(Att.CATOFP, new S57key(Conv.L, Catofp)); keys.put(Att.CATOLB, new S57key(Conv.E, Catolb));
		keys.put(Att.CATPLE, new S57key(Conv.E, Catple)); keys.put(Att.CATPIL, new S57key(Conv.E, Catpil)); keys.put(Att.CATPIP, new S57key(Conv.L, Catpip));
		keys.put(Att.CATPRA, new S57key(Conv.E, Catpra)); keys.put(Att.CATPYL, new S57key(Conv.E, Catpyl)); keys.put(Att.CATQUA, new S57key(Conv.E, Catqua));
		keys.put(Att.CATRAS, new S57key(Conv.E, Catras)); keys.put(Att.CATRTB, new S57key(Conv.E, Catrtb)); keys.put(Att.CATROS, new S57key(Conv.L, Catros));
		keys.put(Att.CATTRK, new S57key(Conv.E, Cattrk)); keys.put(Att.CATRSC, new S57key(Conv.L, Catrsc)); keys.put(Att.CATREA, new S57key(Conv.L, Catrea));
		keys.put(Att.CATROD, new S57key(Conv.E, Catrod)); keys.put(Att.CATRUN, new S57key(Conv.E, Catrun)); keys.put(Att.CATSEA, new S57key(Conv.E, Catsea));
		keys.put(Att.CATSIL, new S57key(Conv.E, Catsil)); keys.put(Att.CATSLO, new S57key(Conv.E, Catslo)); keys.put(Att.CATSCF, new S57key(Conv.L, Catscf));
		keys.put(Att.CATSLC, new S57key(Conv.E, Catslc)); keys.put(Att.CATSIT, new S57key(Conv.L, Catsit)); keys.put(Att.CATSIW, new S57key(Conv.L, Catsiw));
		keys.put(Att.CATSPM, new S57key(Conv.L, Catspm)); keys.put(Att.CATTSS, new S57key(Conv.E, Cattss)); keys.put(Att.CATVEG, new S57key(Conv.L, Catveg));
		/*
		keys.put(Att.CATWAT, new S57key(Conv.E, Catwat)); keys.put(Att.CATWED, new S57key(Conv.E, Catwed)); keys.put(Att.CATWRK, new S57key(Conv.E, Catwrk));
		keys.put(Att.CATZOC, new S57key(Conv.E, Catzoc)); keys.put(Att.$SPACE, new S57key(Conv.E, null)); keys.put(Att.$CHARS, new S57key(Conv.A, null));
		keys.put(Att.COLOUR, new S57key(Conv.L, Colour)); keys.put(Att.COLPAT, new S57key(Conv.L, Colpat)); keys.put(Att.COMCHA, new S57key(Conv.A, null));
		keys.put(Att.$CSIZE, new S57key(Conv.F, null)); keys.put(Att.CPDATE, new S57key(Conv.A, null)); keys.put(Att.CSCALE, new S57key(Conv.I, null));
		keys.put(Att.CONDTN, new S57key(Conv.E, Condtn)); keys.put(Att.CONRAD, new S57key(Conv.E, Conrad)); keys.put(Att.CONVIS, new S57key(Conv.E, Convis));
		keys.put(Att.CURVEL, new S57key(Conv.F, null)); keys.put(Att.DATEND, new S57key(Conv.A, null)); keys.put(Att.DATSTA, new S57key(Conv.A, null));
		keys.put(Att.DRVAL1, new S57key(Conv.F, null)); keys.put(Att.DRVAL2, new S57key(Conv.F, null)); keys.put(Att.DUNITS, new S57key(Conv.E, Dunits));
		keys.put(Att.ELEVAT, new S57key(Conv.F, null)); keys.put(Att.ESTRNG, new S57key(Conv.F, null)); keys.put(Att.EXCLIT, new S57key(Conv.E, Exclit));
		keys.put(Att.EXPSOU, new S57key(Conv.E, Expsou)); keys.put(Att.FUNCTN, new S57key(Conv.L, Functn)); keys.put(Att.HEIGHT, new S57key(Conv.A, null));
		keys.put(Att.HUNITS, new S57key(Conv.E, Hunits)); keys.put(Att.HORACC, new S57key(Conv.F, null)); keys.put(Att.HORCLR, new S57key(Conv.F, null));
		keys.put(Att.HORLEN, new S57key(Conv.F, null)); keys.put(Att.HORWID, new S57key(Conv.F, null)); keys.put(Att.ICEFAC, new S57key(Conv.F, null));
		keys.put(Att.INFORM, new S57key(Conv.S, null)); keys.put(Att.JRSDTN, new S57key(Conv.E, Jrsdtn)); keys.put(Att.$JUSTH, new S57key(Conv.E, null));
		keys.put(Att.$JUSTV, new S57key(Conv.E, null)); keys.put(Att.LIFCAP, new S57key(Conv.F, null)); keys.put(Att.LITCHR, new S57key(Conv.E, Litchr));
		keys.put(Att.LITVIS, new S57key(Conv.L, Litvis)); keys.put(Att.MARSYS, new S57key(Conv.E, Marsys)); keys.put(Att.MLTYLT, new S57key(Conv.I, null));
		keys.put(Att.NATION, new S57key(Conv.A, null)); keys.put(Att.NATCON, new S57key(Conv.L, Natcon)); keys.put(Att.NATSUR, new S57key(Conv.L, Natsur));
		keys.put(Att.NATQUA, new S57key(Conv.L, Natqua)); keys.put(Att.NMDATE, new S57key(Conv.A, null)); keys.put(Att.OBJNAM, new S57key(Conv.S, null));
		keys.put(Att.ORIENT, new S57key(Conv.F, null)); keys.put(Att.PEREND, new S57key(Conv.A, null)); keys.put(Att.PERSTA, new S57key(Conv.A, null));
		keys.put(Att.PICREP, new S57key(Conv.S, null)); keys.put(Att.PILDST, new S57key(Conv.S, null)); keys.put(Att.PRCTRY, new S57key(Conv.A, null));
		keys.put(Att.PRODCT, new S57key(Conv.L, Prodct)); keys.put(Att.PUBREF, new S57key(Conv.S, null)); keys.put(Att.QUASOU, new S57key(Conv.L, Quasou));
		keys.put(Att.RADWAL, new S57key(Conv.A, null)); keys.put(Att.RADIUS, new S57key(Conv.F, null)); keys.put(Att.RECDAT, new S57key(Conv.A, null));
		keys.put(Att.RECIND, new S57key(Conv.A, null)); keys.put(Att.RYRMGV, new S57key(Conv.A, null)); keys.put(Att.RESTRN, new S57key(Conv.L, Restrn));
		keys.put(Att.SCAMAX, new S57key(Conv.I, null)); keys.put(Att.SCAMIN, new S57key(Conv.I, null)); keys.put(Att.SCVAL1, new S57key(Conv.I, null));
		keys.put(Att.SCVAL2, new S57key(Conv.I, null)); keys.put(Att.SECTR1, new S57key(Conv.F, null)); keys.put(Att.SECTR2, new S57key(Conv.F, null));
		keys.put(Att.SHIPAM, new S57key(Conv.A, null)); keys.put(Att.SIGFRQ, new S57key(Conv.I, null)); keys.put(Att.SIGGEN, new S57key(Conv.E, Siggen));
		keys.put(Att.SIGGRP, new S57key(Conv.A, null)); keys.put(Att.SIGPER, new S57key(Conv.F, null)); keys.put(Att.SIGSEQ, new S57key(Conv.A, null));
		keys.put(Att.SOUACC, new S57key(Conv.F, null)); keys.put(Att.SDISMX, new S57key(Conv.I, null)); keys.put(Att.SDISMN, new S57key(Conv.I, null));
		keys.put(Att.SORDAT, new S57key(Conv.A, null)); keys.put(Att.SORIND, new S57key(Conv.A, null)); keys.put(Att.STATUS, new S57key(Conv.L, Status));
		keys.put(Att.SURATH, new S57key(Conv.S, null)); keys.put(Att.SUREND, new S57key(Conv.A, null)); keys.put(Att.SURSTA, new S57key(Conv.A, null));
		keys.put(Att.SURTYP, new S57key(Conv.L, Surtyp)); keys.put(Att.$SCALE, new S57key(Conv.F, null)); keys.put(Att.$SCODE, new S57key(Conv.A, null));
		keys.put(Att.TECSOU, new S57key(Conv.L, Tecsou)); keys.put(Att.$TXSTR, new S57key(Conv.S, null)); keys.put(Att.TXTDSC, new S57key(Conv.S, null));
		keys.put(Att.TS_TSP, new S57key(Conv.A, null)); keys.put(Att.TS_TSV, new S57key(Conv.A, null)); keys.put(Att.T_ACWL, new S57key(Conv.E, null));
		keys.put(Att.T_HWLW, new S57key(Conv.A, null)); keys.put(Att.T_MTOD, new S57key(Conv.E, null)); keys.put(Att.T_THDF, new S57key(Conv.A, null));
		keys.put(Att.T_TINT, new S57key(Conv.I, null)); keys.put(Att.T_TSVL, new S57key(Conv.A, null)); keys.put(Att.T_VAHC, new S57key(Conv.A, null));
		keys.put(Att.TIMEND, new S57key(Conv.A, null)); keys.put(Att.TIMSTA, new S57key(Conv.A, null)); keys.put(Att.$TINTS, new S57key(Conv.E, null));
		keys.put(Att.TOPSHP, new S57key(Conv.E, Topshp)); keys.put(Att.TRAFIC, new S57key(Conv.E, Trafic)); keys.put(Att.VALACM, new S57key(Conv.F, null));
		keys.put(Att.VALDCO, new S57key(Conv.F, null)); keys.put(Att.VALLMA, new S57key(Conv.F, null)); keys.put(Att.VALMAG, new S57key(Conv.F, null));
		keys.put(Att.VALMXR, new S57key(Conv.F, null)); keys.put(Att.VALNMR, new S57key(Conv.F, null)); keys.put(Att.VALSOU, new S57key(Conv.F, null));
		keys.put(Att.VERACC, new S57key(Conv.F, null)); keys.put(Att.VERCLR, new S57key(Conv.F, null)); keys.put(Att.VERCCL, new S57key(Conv.F, null));
		keys.put(Att.VERCOP, new S57key(Conv.F, null)); keys.put(Att.VERCSA, new S57key(Conv.F, null)); keys.put(Att.VERDAT, new S57key(Conv.E, null));
		keys.put(Att.VERLEN, new S57key(Conv.F, null)); keys.put(Att.WATLEV, new S57key(Conv.E, Watlev)); keys.put(Att.CAT_TS, new S57key(Conv.E, Cat_ts));
		keys.put(Att.PUNITS, new S57key(Conv.E, Punits)); keys.put(Att.NINFOM, new S57key(Conv.S, null)); keys.put(Att.NOBJNM, new S57key(Conv.S, null));
		keys.put(Att.NPLDST, new S57key(Conv.S, null)); keys.put(Att.$NTXST, new S57key(Conv.S, null)); keys.put(Att.NTXTDS, new S57key(Conv.S, null));
		keys.put(Att.HORDAT, new S57key(Conv.E, null)); keys.put(Att.POSACC, new S57key(Conv.F, null)); keys.put(Att.QUAPOS, new S57key(Conv.E, Quapos));
		keys.put(Att.CLSDNG, new S57key(Conv.E, Clsdng)); keys.put(Att.DIRIMP, new S57key(Conv.L, Dirimp)); keys.put(Att.DISBK1, new S57key(Conv.F, null));
		keys.put(Att.DISBK2, new S57key(Conv.F, null)); keys.put(Att.DISIPU, new S57key(Conv.F, null)); keys.put(Att.DISIPD, new S57key(Conv.F, null));
		keys.put(Att.ELEVA1, new S57key(Conv.F, null)); keys.put(Att.ELEVA2, new S57key(Conv.F, null)); keys.put(Att.FNCTNM, new S57key(Conv.E, Fnctnm));
		keys.put(Att.WTWDIS, new S57key(Conv.F, null)); keys.put(Att.BUNVES, new S57key(Conv.E, Bunves)); keys.put(Att.COMCTN, new S57key(Conv.S, null));
		keys.put(Att.HORCLL, new S57key(Conv.F, null)); keys.put(Att.HORCLW, new S57key(Conv.F, null)); keys.put(Att.TRSHGD, new S57key(Conv.L, Trshgd));
		keys.put(Att.UNLOCD, new S57key(Conv.S, null)); keys.put(Att.HIGWAT, new S57key(Conv.F, null)); keys.put(Att.HIGNAM, new S57key(Conv.S, null));
		keys.put(Att.LOWWAT, new S57key(Conv.F, null)); keys.put(Att.LOWNAM, new S57key(Conv.S, null)); keys.put(Att.MEAWAT, new S57key(Conv.F, null));
		keys.put(Att.MEANAM, new S57key(Conv.S, null)); keys.put(Att.OTHWAT, new S57key(Conv.F, null)); keys.put(Att.OTHNAM, new S57key(Conv.S, null));
		keys.put(Att.REFLEV, new S57key(Conv.E, Reflev)); keys.put(Att.SDRLEV, new S57key(Conv.S, null)); keys.put(Att.VCRLEV, new S57key(Conv.S, null));
		keys.put(Att.SCHREF, new S57key(Conv.S, null)); keys.put(Att.USESHP, new S57key(Conv.E, Useshp)); keys.put(Att.CURVHW, new S57key(Conv.F, null));
		keys.put(Att.CURVLW, new S57key(Conv.F, null)); keys.put(Att.CURVMW, new S57key(Conv.F, null)); keys.put(Att.CURVOW, new S57key(Conv.F, null));
		keys.put(Att.APTREF, new S57key(Conv.S, null)); keys.put(Att.SHPTYP, new S57key(Conv.E, Shptyp)); keys.put(Att.UPDMSG, new S57key(Conv.S, null));
		keys.put(Att.ADDMRK, new S57key(Conv.L, Addmrk)); keys.put(Att.CATBNK, new S57key(Conv.E, Catbnk)); keys.put(Att.CATNMK, new S57key(Conv.E, Catnmk));
		keys.put(Att.CATBRT, new S57key(Conv.L, Catbrt)); keys.put(Att.CATBUN, new S57key(Conv.L, Catbun)); keys.put(Att.CATCCL, new S57key(Conv.L, Catccl));
		keys.put(Att.CATCOM, new S57key(Conv.L, Catcom)); keys.put(Att.CATHBR, new S57key(Conv.L, Cathbr)); keys.put(Att.CATRFD, new S57key(Conv.L, Catrfd));
		keys.put(Att.CATTML, new S57key(Conv.L, Cattml)); keys.put(Att.CATGAG, new S57key(Conv.L, Catgag)); keys.put(Att.CATVTR, new S57key(Conv.L, Catvtr));
		keys.put(Att.CATTAB, new S57key(Conv.E, Cattab)); keys.put(Att.CATEXS, new S57key(Conv.E, Catexs)); keys.put(Att.CATWWM, new S57key(Conv.E, Catwwm));
		keys.put(Att.LG_SPD, new S57key(Conv.F, null)); keys.put(Att.LG_SPR, new S57key(Conv.L, lg_spr)); keys.put(Att.LG_BME, new S57key(Conv.F, null));
		keys.put(Att.LG_LGS, new S57key(Conv.F, null)); keys.put(Att.LG_DRT, new S57key(Conv.F, null)); keys.put(Att.LG_WDP, new S57key(Conv.F, null));
		keys.put(Att.LG_WDU, new S57key(Conv.E, Lg_wdu)); keys.put(Att.LG_REL, new S57key(Conv.L, Lg_rel)); keys.put(Att.LG_FNC, new S57key(Conv.L, Lg_fnc));
		keys.put(Att.LG_DES, new S57key(Conv.S, null)); keys.put(Att.LG_PBR, new S57key(Conv.S, null)); keys.put(Att.LC_CSI, new S57key(Conv.L, lc_csi));
		keys.put(Att.LC_CSE, new S57key(Conv.L, Lc_cse)); keys.put(Att.LC_ASI, new S57key(Conv.L, Lc_asi)); keys.put(Att.LC_ASE, new S57key(Conv.L, Lc_ase));
		keys.put(Att.LC_CCI, new S57key(Conv.L, Lc_cci)); keys.put(Att.LC_CCE, new S57key(Conv.L, Lc_cce)); keys.put(Att.LC_BM1, new S57key(Conv.F, null));
		keys.put(Att.LC_BM2, new S57key(Conv.F, null)); keys.put(Att.LC_LG1, new S57key(Conv.F, null)); keys.put(Att.LC_LG2, new S57key(Conv.F, null));
		keys.put(Att.LC_DR1, new S57key(Conv.F, null)); keys.put(Att.LC_DR2, new S57key(Conv.F, null)); keys.put(Att.LC_SP1, new S57key(Conv.F, null));
		keys.put(Att.LC_SP2, new S57key(Conv.F, null)); keys.put(Att.LC_WD1, new S57key(Conv.F, null)); keys.put(Att.LC_WD2, new S57key(Conv.F, null));
		keys.put(Att.LITRAD, new S57key(Conv.A, null));
*/
	}
	
	public static String decodeValue(String val, Integer attl) {          // Convert S57 attribute value string to OSeaM attribute value string
		Att att = S57att.lookupAttribute(attl);
		switch (keys.get(att).conv) {
		case A:
		case S:
			return val;
		case E:
		case L:
			return (String)(keys.get(att).map).get(val);
		case I:
			return (Integer.valueOf(val.trim())).toString();
		case F:
			return (Float.valueOf(val.trim())).toString();
		}
		return "";
	}

	public static Integer encodeValue(String val, Att att) {        // Convert OSeaM attribute value string to S57 attribute value
		EnumMap map = keys.get(att).map;
		for (Object item : map.keySet()) {
			if (((S57enum)map.get(item)).val.equals(val))
				return ((S57enum)map.get(item)).atvl;
		}
		return 0;
	}

	
	public static String stringValue(AttVal attval) {                  // Convert OSeaM value struct to OSeaM attribute value string
		switch (attval.conv) {
		case A:
		case S:
			return (String)attval.val;
		case E:
			return (String)((EnumMap)attval.val).get(attval.att);
		case L:
			String str = "";
			for (Object item : (ArrayList)attval.val) {
				if (!str.isEmpty()) str += ";";
				str += keys.get(attval.att).map.get(item);
			}
			return str;
		case I:
			return ((Integer)attval.val).toString();
		case F:
			return ((Float)attval.val).toString();
		}
		return "";
	}
	
	public static Enum enumValue(String val, Att att) {           // Convert OSeaM attribute value string to OSeaM enumeration
		EnumMap map = keys.get(att).map;
		Enum unkn = null;
		for (Object item : map.keySet()) {
			if (unkn == null) unkn = (Enum)item;
			if (((S57enum)map.get(item)).val.equals(val))
				return (Enum)item;
		}
		return unkn;
	}
	
	public static AttVal convertValue(String val, Att att) {         // Convert OSeaM attribute value string to OSeaM value struct
		switch (keys.get(att).conv) {
		case A:
		case S:
			return new AttVal(att, keys.get(att).conv, val);
		case E:
				return new AttVal(att, keys.get(att).conv, enumValue(val, att));
		case L:
			ArrayList list = new ArrayList();
			for (String item : val.split(";")) {
				list.add(enumValue(item, att));
			}
			return new AttVal(att, keys.get(att).conv, list);
		case I:
			return new AttVal(att, keys.get(att).conv, Integer.parseInt(val));
		case F:
			return new AttVal(att, keys.get(att).conv, Float.parseFloat(val));
		}
		return new AttVal(att, keys.get(att).conv, null);
	}

}
