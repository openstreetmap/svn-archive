package oseam.seamarks;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.Arc2D;

import java.util.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.command.ChangePropertyCommand;

import oseam.Messages;
import oseam.dialogs.OSeaMAction;

public class SeaMark extends JPanel {

	public OSeaMAction dlg = null;

	public SeaMark(OSeaMAction dia) {
		dlg = dia;
		clrLight();
	}

	public enum Reg {
		A, B, C, R, X
	}

	public static final EnumMap<Reg, String> RegSTR = new EnumMap<Reg, String>(Reg.class);
	static {
		RegSTR.put(Reg.A, "iala-a");
		RegSTR.put(Reg.B, "iala-b");
		RegSTR.put(Reg.C, "cevni");
		RegSTR.put(Reg.R, "riwr");
		RegSTR.put(Reg.X, "other");
	}

	private Reg region = Reg.A;

	public Reg getRegion() {
		return region;
	}

	public void setRegion(Reg reg) {
		region = reg;
	}

	private String name = "";

	public String getName() {
		return name;
	}

	public void setName(String str) {
		name = str.trim();
		repaint();
	}
	
	private String longName = "";

	public enum Obj {
		UNKOBJ, BCNCAR, BCNISD, BCNLAT, BCNSAW, BCNSPP,
		BOYCAR, BOYISD, BOYLAT, BOYSAW, BOYSPP, NOTMRK,
		FLTCAR, FLTISD, FLTLAT, FLTSAW, FLTSPP,
		LITMAJ, LITMIN, LITFLT, LITVES, LITHSE, LNDMRK,
		MORFAC, BOYINB, SISTAW, SISTAT, OFSPLF,
		CGUSTA, PILBOP, RSCSTA, RDOSTA, RADSTA
	}

	public static final EnumMap<Obj, String> ObjSTR = new EnumMap<Obj, String>(Obj.class);
	static {
		ObjSTR.put(Obj.BCNCAR, "beacon_cardinal");
		ObjSTR.put(Obj.BCNISD, "beacon_isolated_danger");
		ObjSTR.put(Obj.BCNLAT, "beacon_lateral");
		ObjSTR.put(Obj.BCNSAW, "beacon_safe_water");
		ObjSTR.put(Obj.BCNSPP, "beacon_special_purpose");
		ObjSTR.put(Obj.BOYCAR, "buoy_cardinal");
		ObjSTR.put(Obj.BOYISD, "buoy_isolated_danger");
		ObjSTR.put(Obj.BOYLAT, "buoy_lateral");
		ObjSTR.put(Obj.BOYSAW, "buoy_safe_water");
		ObjSTR.put(Obj.BOYSPP, "buoy_special_purpose");
		ObjSTR.put(Obj.FLTCAR, "light_float");
		ObjSTR.put(Obj.FLTLAT, "light_float");
		ObjSTR.put(Obj.FLTSAW, "light_float");
		ObjSTR.put(Obj.FLTISD, "light_float");
		ObjSTR.put(Obj.FLTSPP, "light_float");
		ObjSTR.put(Obj.LITMAJ, "light_major");
		ObjSTR.put(Obj.LITMIN, "light_minor");
		ObjSTR.put(Obj.LITFLT, "light_float");
		ObjSTR.put(Obj.LITVES, "light_vessel");
		ObjSTR.put(Obj.NOTMRK, "notice");
		ObjSTR.put(Obj.LNDMRK, "landmark");
		ObjSTR.put(Obj.LITHSE, "landmark");
		ObjSTR.put(Obj.MORFAC, "mooring");
		ObjSTR.put(Obj.BOYINB, "buoy_installation");
		ObjSTR.put(Obj.OFSPLF, "platform");
		ObjSTR.put(Obj.SISTAW, "signal_station_warning");
		ObjSTR.put(Obj.SISTAT, "signal_station_traffic");
		ObjSTR.put(Obj.CGUSTA, "coastguard_station");
		ObjSTR.put(Obj.PILBOP, "pilot_boarding");
		ObjSTR.put(Obj.RSCSTA, "rescue_station");
		ObjSTR.put(Obj.RDOSTA, "radio_station");
		ObjSTR.put(Obj.RADSTA, "radar_station");
	}

	private Obj object = Obj.UNKOBJ;

	public Obj getObject() {
		return object;
	}

	public void setObject(Obj obj) {
		object = obj;
		if (obj == Obj.UNKOBJ) {
			setCategory(Cat.NOCAT);
			setFunc(Fnc.UNKFNC);
			setShape(Shp.UNKSHP);
			setColour(Ent.BODY, Col.UNKCOL);
			setPattern(Ent.BODY, Pat.NOPAT);
			setTopmark(Top.NOTOP);
			setColour(Ent.TOPMARK, Col.UNKCOL);
			setPattern(Ent.TOPMARK, Pat.NOPAT);
		}
		repaint();
	}

	public enum Ent {
		BODY, BUOY, BEACON, LFLOAT, TOPMARK, LIGHT, MOORING, STATION, PLATFORM, NOTICE
	}

	public static final EnumMap<Obj, Ent> EntMAP = new EnumMap<Obj, Ent>(Obj.class);
	static {
		EntMAP.put(Obj.BCNCAR, Ent.BEACON);
		EntMAP.put(Obj.BCNISD, Ent.BEACON);
		EntMAP.put(Obj.BCNLAT, Ent.BEACON);
		EntMAP.put(Obj.BCNSAW, Ent.BEACON);
		EntMAP.put(Obj.BCNSPP, Ent.BEACON);
		EntMAP.put(Obj.BOYCAR, Ent.BUOY);
		EntMAP.put(Obj.BOYISD, Ent.BUOY);
		EntMAP.put(Obj.BOYLAT, Ent.BUOY);
		EntMAP.put(Obj.BOYSAW, Ent.BUOY);
		EntMAP.put(Obj.BOYSPP, Ent.BUOY);
		EntMAP.put(Obj.LITMAJ, Ent.LIGHT);
		EntMAP.put(Obj.LITMIN, Ent.LIGHT);
		EntMAP.put(Obj.LITFLT, Ent.LFLOAT);
		EntMAP.put(Obj.FLTCAR, Ent.LFLOAT);
		EntMAP.put(Obj.FLTLAT, Ent.LFLOAT);
		EntMAP.put(Obj.FLTSAW, Ent.LFLOAT);
		EntMAP.put(Obj.FLTISD, Ent.LFLOAT);
		EntMAP.put(Obj.FLTSPP, Ent.LFLOAT);
		EntMAP.put(Obj.LITVES, Ent.LFLOAT);
		EntMAP.put(Obj.LITHSE, Ent.LIGHT);
		EntMAP.put(Obj.LNDMRK, Ent.LIGHT);
		EntMAP.put(Obj.MORFAC, Ent.MOORING);
		EntMAP.put(Obj.BOYINB, Ent.MOORING);
		EntMAP.put(Obj.OFSPLF, Ent.PLATFORM);
		EntMAP.put(Obj.SISTAW, Ent.STATION);
		EntMAP.put(Obj.SISTAT, Ent.STATION);
		EntMAP.put(Obj.CGUSTA, Ent.STATION);
		EntMAP.put(Obj.PILBOP, Ent.STATION);
		EntMAP.put(Obj.RSCSTA, Ent.STATION);
		EntMAP.put(Obj.RDOSTA, Ent.STATION);
		EntMAP.put(Obj.RADSTA, Ent.STATION);
		EntMAP.put(Obj.NOTMRK, Ent.NOTICE);
	}

	public enum Grp {
		NUL, LAT, CAR, SAW, ISD, SPP, LGT, STN, PLF, NTC
	}

	public static final EnumMap<Obj, Grp> GrpMAP = new EnumMap<Obj, Grp>(Obj.class);
	static {
		GrpMAP.put(Obj.UNKOBJ, Grp.NUL);
		GrpMAP.put(Obj.BCNCAR, Grp.CAR);
		GrpMAP.put(Obj.BCNISD, Grp.ISD);
		GrpMAP.put(Obj.BCNLAT, Grp.LAT);
		GrpMAP.put(Obj.BCNSAW, Grp.SAW);
		GrpMAP.put(Obj.BCNSPP, Grp.SPP);
		GrpMAP.put(Obj.BOYCAR, Grp.CAR);
		GrpMAP.put(Obj.BOYISD, Grp.ISD);
		GrpMAP.put(Obj.BOYLAT, Grp.LAT);
		GrpMAP.put(Obj.BOYSAW, Grp.SAW);
		GrpMAP.put(Obj.BOYSPP, Grp.SPP);
		GrpMAP.put(Obj.FLTCAR, Grp.CAR);
		GrpMAP.put(Obj.FLTLAT, Grp.LAT);
		GrpMAP.put(Obj.FLTSAW, Grp.SAW);
		GrpMAP.put(Obj.FLTISD, Grp.ISD);
		GrpMAP.put(Obj.FLTSPP, Grp.SPP);
		GrpMAP.put(Obj.LITFLT, Grp.LGT);
		GrpMAP.put(Obj.LITMAJ, Grp.LGT);
		GrpMAP.put(Obj.LITMIN, Grp.LGT);
		GrpMAP.put(Obj.LITVES, Grp.LGT);
		GrpMAP.put(Obj.LITHSE, Grp.LGT);
		GrpMAP.put(Obj.LNDMRK, Grp.LGT);
		GrpMAP.put(Obj.MORFAC, Grp.SPP);
		GrpMAP.put(Obj.BOYINB, Grp.SPP);
		GrpMAP.put(Obj.OFSPLF, Grp.PLF);
		GrpMAP.put(Obj.SISTAW, Grp.STN);
		GrpMAP.put(Obj.SISTAT, Grp.STN);
		GrpMAP.put(Obj.CGUSTA, Grp.STN);
		GrpMAP.put(Obj.PILBOP, Grp.STN);
		GrpMAP.put(Obj.RSCSTA, Grp.STN);
		GrpMAP.put(Obj.RDOSTA, Grp.STN);
		GrpMAP.put(Obj.RADSTA, Grp.STN);
		GrpMAP.put(Obj.NOTMRK, Grp.NTC);
	}

	public enum Cat {
		NOCAT, LAM_PORT, LAM_STBD, LAM_PPORT, LAM_PSTBD, CAM_NORTH, CAM_EAST, CAM_SOUTH, CAM_WEST,
		ACH_URST, ACH_DEEP, ACH_TANK, ACH_EXPL, ACH_QUAR, ACH_SPLN, ACH_SCAN, ACH_SCMO, ACH_T24H, ACH_TLIM,
		SPM_UNKN, SPM_WARN, SPM_CHBF, SPM_YCHT, SPM_CABL, SPM_OFAL, SPM_ODAS, SPM_RECN, SPM_MOOR, SPM_LNBY,
		SPM_LDNG, SPM_NOTC, SPM_TSS, SPM_FOUL, SPM_DIVE, SPM_FRRY, SPM_ANCH,
		MOR_DLPN, MOR_DDPN, MOR_BLRD, MOR_WALL, MOR_POST, MOR_CHWR, MOR_ROPE, MOR_AUTO, MOR_BUOY, INB_CALM, INB_SBM,
		SIS_PTCL, SIS_PTED, SIS_IPT, SIS_BRTH, SIS_DOCK, SIS_LOCK, SIS_FBAR, SIS_BRDG, SIS_DRDG, SIS_TRFC,
		SIS_DNGR, SIS_OBST, SIS_CABL, SIS_MILY, SIS_DSTR, SIS_WTHR, SIS_STRM, SIS_ICE, SIS_TIME, SIS_TIDE,
		SIS_TSTM, SIS_TGAG, SIS_TSCL, SIS_DIVE, SIS_LGAG, LIT_DIRF, LIT_LEDG,
		LMK_CHMY, LMK_CARN, LMK_DSHA, LMK_FLGS, LMK_FLRS, LMK_MNMT, LMK_TOWR, LMK_WNDM, LMK_WTRT, LMK_MNRT,
		LMK_MAST, LMK_WNDS, LMK_CLMN, LMK_OBLK, LMK_STAT, LMK_CROS, LMK_DOME, LMK_SCNR, LMK_WNDL, LMK_SPIR,
		OFP_OIL, OFP_PRD, OFP_OBS, OFP_ALP, OFP_SALM, OFP_MOR, OFP_ISL, OFP_FPSO, OFP_ACC, OFP_NCCB,
		RSC_LFB, RSC_RKT, RSC_RSW, RSC_RIT, RSC_MLB, RSC_RAD, RSC_FAE, RSC_SPL, RSC_AIR, RSC_TUG,
		ROS_BNO, ROS_BND, ROS_BNR, ROS_BNC, ROS_RDF, ROS_QTG, ROS_AER, ROS_DCA, ROS_LRN, ROS_DGPS, ROS_TRN, ROS_OMA,
		ROS_SDS, ROS_CKA, ROS_PUB, ROS_COM, ROS_FAX, ROS_TIM, RAS_SRV, RAS_CST, PIL_VESS, PIL_HELI, PIL_SHORE,
		NTC_A1, NTC_A1a, NTC_A2, NTC_A3, NTC_A4, NTC_A4_1, NTC_A5, NTC_A5_1, NTC_A6, NTC_A7, NTC_A8, NTC_A9,
		NTC_A10a, NTC_A10b, NTC_A12, NTC_A13, NTC_A14, NTC_A15, NTC_A16, NTC_A17, NTC_A18, NTC_A19, NTC_A20,
		NTC_B1a, NTC_B1b, NTC_B2a, NTC_B2b, NTC_B3a, NTC_B3b, NTC_B4a, NTC_B4b, NTC_B5, NTC_B6, NTC_B7, NTC_B8, NTC_B9a, NTC_B9b, NTC_B11,
		NTC_C1, NTC_C2, NTC_C3, NTC_C4, NTC_C5a, NTC_C5b, NTC_D1a, NTC_D1b, NTC_D2a, NTC_D2b, NTC_D3a, NTC_D3b
	}
		 
	public static final EnumMap<Cat, String> CatSTR = new EnumMap<Cat, String>(Cat.class);
	static {
		CatSTR.put(Cat.LAM_PORT, "port");
		CatSTR.put(Cat.LAM_STBD, "starboard");
		CatSTR.put(Cat.LAM_PPORT, "preferred_channel_port");
		CatSTR.put(Cat.LAM_PSTBD, "preferred_channel_starboard");
		CatSTR.put(Cat.CAM_NORTH, "north");
		CatSTR.put(Cat.CAM_EAST, "east");
		CatSTR.put(Cat.CAM_SOUTH, "south");
		CatSTR.put(Cat.CAM_WEST, "west");
		CatSTR.put(Cat.SPM_UNKN, "unknown_purpose");
		CatSTR.put(Cat.SPM_WARN, "warning");
		CatSTR.put(Cat.SPM_CHBF, "channel_separation");
		CatSTR.put(Cat.SPM_YCHT, "yachting");
		CatSTR.put(Cat.SPM_CABL, "cable");
		CatSTR.put(Cat.SPM_OFAL, "outfall");
		CatSTR.put(Cat.SPM_ODAS, "odas");
		CatSTR.put(Cat.SPM_RECN, "recreational");
		CatSTR.put(Cat.SPM_MOOR, "mooring");
		CatSTR.put(Cat.SPM_LNBY, "lanby");
		CatSTR.put(Cat.SPM_LDNG, "leading");
		CatSTR.put(Cat.SPM_NOTC, "notice");
		CatSTR.put(Cat.SPM_TSS, "tss");
		CatSTR.put(Cat.SPM_FOUL, "foul");
		CatSTR.put(Cat.SPM_DIVE, "diving");
		CatSTR.put(Cat.SPM_FRRY, "ferry");
		CatSTR.put(Cat.SPM_ANCH, "anchorage");
		CatSTR.put(Cat.MOR_DLPN, "dolphin");
		CatSTR.put(Cat.MOR_DDPN, "deviation_dolphin");
		CatSTR.put(Cat.MOR_BLRD, "bollard");
		CatSTR.put(Cat.MOR_WALL, "wall");
		CatSTR.put(Cat.MOR_POST, "post");
		CatSTR.put(Cat.MOR_CHWR, "chain");
		CatSTR.put(Cat.MOR_ROPE, "shore_rope");
		CatSTR.put(Cat.MOR_AUTO, "automatic");
		CatSTR.put(Cat.MOR_BUOY, "buoy");
		CatSTR.put(Cat.INB_CALM, "calm");
		CatSTR.put(Cat.INB_SBM, "sbm");
		CatSTR.put(Cat.SIS_PTCL, "port_control");
		CatSTR.put(Cat.SIS_PTED, "port_entry");
		CatSTR.put(Cat.SIS_IPT, "ipt");
		CatSTR.put(Cat.SIS_BRTH, "berthing");
		CatSTR.put(Cat.SIS_DOCK, "dock");
		CatSTR.put(Cat.SIS_LOCK, "lock");
		CatSTR.put(Cat.SIS_FBAR, "barrage");
		CatSTR.put(Cat.SIS_BRDG, "bridge");
		CatSTR.put(Cat.SIS_DRDG, "dredging");
		CatSTR.put(Cat.SIS_TRFC, "traffic");
		CatSTR.put(Cat.SIS_DNGR, "danger");
		CatSTR.put(Cat.SIS_OBST, "obstruction");
		CatSTR.put(Cat.SIS_CABL, "cable");
		CatSTR.put(Cat.SIS_MILY, "military");
		CatSTR.put(Cat.SIS_DSTR, "distress");
		CatSTR.put(Cat.SIS_WTHR, "weather");
		CatSTR.put(Cat.SIS_STRM, "storm");
		CatSTR.put(Cat.SIS_ICE, "ice");
		CatSTR.put(Cat.SIS_TIME, "time");
		CatSTR.put(Cat.SIS_TIDE, "tide");
		CatSTR.put(Cat.SIS_TSTM, "stream");
		CatSTR.put(Cat.SIS_TGAG, "gauge");
		CatSTR.put(Cat.SIS_TSCL, "scale");
		CatSTR.put(Cat.SIS_DIVE, "diving");
		CatSTR.put(Cat.SIS_LGAG, "level");
		CatSTR.put(Cat.LMK_CHMY, "chimney");
		CatSTR.put(Cat.LMK_CARN, "cairn");
		CatSTR.put(Cat.LMK_DSHA, "dish_aerial");
		CatSTR.put(Cat.LMK_FLGS, "flagstaff");
		CatSTR.put(Cat.LMK_FLRS, "flare_stack");
		CatSTR.put(Cat.LMK_MNMT, "monument");
		CatSTR.put(Cat.LMK_TOWR, "tower");
		CatSTR.put(Cat.LMK_WNDM, "windmotor");
		CatSTR.put(Cat.LMK_WTRT, "water_tower");
		CatSTR.put(Cat.LMK_MAST, "mast");
		CatSTR.put(Cat.LMK_WNDS, "windsock");
		CatSTR.put(Cat.LMK_CLMN, "column");
		CatSTR.put(Cat.LMK_OBLK, "obelisk");
		CatSTR.put(Cat.LMK_STAT, "statue");
		CatSTR.put(Cat.LMK_CROS, "cross");
		CatSTR.put(Cat.LMK_DOME, "dome");
		CatSTR.put(Cat.LMK_SCNR, "radar_scanner");
		CatSTR.put(Cat.LMK_WNDL, "windmill");
		CatSTR.put(Cat.LMK_SPIR, "spire");
		CatSTR.put(Cat.LMK_MNRT, "minaret");
		CatSTR.put(Cat.OFP_OIL, "oil");
		CatSTR.put(Cat.OFP_PRD,  "production");
		CatSTR.put(Cat.OFP_OBS, "observation"); 
		CatSTR.put(Cat.OFP_ALP, "alp");
		CatSTR.put(Cat.OFP_SALM, "salm");
		CatSTR.put(Cat.OFP_MOR, "mooring");
		CatSTR.put(Cat.OFP_ISL, "island");
		CatSTR.put(Cat.OFP_FPSO, "fpso");
		CatSTR.put(Cat.OFP_ACC, "accommodation");
		CatSTR.put(Cat.OFP_NCCB, "nccb");
		CatSTR.put(Cat.PIL_VESS, "cruising_vessel");
		CatSTR.put(Cat.PIL_HELI, "helicopter");
		CatSTR.put(Cat.PIL_SHORE, "from_shore");
		CatSTR.put(Cat.RSC_LFB, "lifeboat");
		CatSTR.put(Cat.RSC_RKT, "rocket");
		CatSTR.put(Cat.RSC_RSW, "refuge_shipwrecked");
		CatSTR.put(Cat.RSC_RIT, "refuge_intertidal");
		CatSTR.put(Cat.RSC_MLB, "lifeboat_moored");
		CatSTR.put(Cat.RSC_RAD, "radio");
		CatSTR.put(Cat.RSC_FAE, "firstaid");
		CatSTR.put(Cat.RSC_SPL, "seaplane");
		CatSTR.put(Cat.RSC_AIR, "aircraft");
		CatSTR.put(Cat.RSC_TUG, "tug");
		CatSTR.put(Cat.RAS_SRV, "surveillance");
		CatSTR.put(Cat.RAS_CST, "coast");
		CatSTR.put(Cat.ROS_BNO, "beacon_circular");
		CatSTR.put(Cat.ROS_BND, "beacon_directional");
		CatSTR.put(Cat.ROS_BNR, "beacon_rotating");
		CatSTR.put(Cat.ROS_BNC, "beacon_consol");
		CatSTR.put(Cat.ROS_RDF, "direction_finding");
		CatSTR.put(Cat.ROS_QTG, "qtg_service");
		CatSTR.put(Cat.ROS_AER, "beacon_aero");
		CatSTR.put(Cat.ROS_DCA, "decca");
		CatSTR.put(Cat.ROS_LRN, "loran");
		CatSTR.put(Cat.ROS_DGPS, "dgps");
		CatSTR.put(Cat.ROS_TRN, "toran");
		CatSTR.put(Cat.ROS_OMA, "omega");
		CatSTR.put(Cat.ROS_SDS, "syledis");
		CatSTR.put(Cat.ROS_CKA, "chiaka");
		CatSTR.put(Cat.ROS_PUB, "public_communication");
		CatSTR.put(Cat.ROS_COM, "comercial_broadcast");
		CatSTR.put(Cat.ROS_FAX, "facsimile");
		CatSTR.put(Cat.ROS_TIM, "time_signal");
		CatSTR.put(Cat.NTC_A1, "no_entry");
		CatSTR.put(Cat.NTC_A1a, "closed_area");
		CatSTR.put(Cat.NTC_A2, "no_overtaking");
		CatSTR.put(Cat.NTC_A3, "no_convoy_overtaking");
		CatSTR.put(Cat.NTC_A4, "no_passing");
		CatSTR.put(Cat.NTC_A4, "no_convoy_passing");
		CatSTR.put(Cat.NTC_A5, "no_berthing");
		CatSTR.put(Cat.NTC_A5_1, "no_berthing_lateral_limit");
		CatSTR.put(Cat.NTC_A6, "no_anchoring");
		CatSTR.put(Cat.NTC_A7, "no_mooring");
		CatSTR.put(Cat.NTC_A8, "no_turning");
		CatSTR.put(Cat.NTC_A9, "no_wash");
		CatSTR.put(Cat.NTC_A10a, "no_passage_left");
		CatSTR.put(Cat.NTC_A10b, "no_passage_right");
		CatSTR.put(Cat.NTC_A12, "no_motor_craft");
		CatSTR.put(Cat.NTC_A13, "no_sport_craft");
		CatSTR.put(Cat.NTC_A14, "no_waterskiing");
		CatSTR.put(Cat.NTC_A15, "no_sailing_craft");
		CatSTR.put(Cat.NTC_A16, "no_unpowered_craft");
		CatSTR.put(Cat.NTC_A17, "no_sailboards");
		CatSTR.put(Cat.NTC_A18, "no_high_speeds");
		CatSTR.put(Cat.NTC_A19, "no_launching_beaching");
		CatSTR.put(Cat.NTC_A20, "no_waterbikes");
		CatSTR.put(Cat.NTC_B1a, "");
		CatSTR.put(Cat.NTC_B1b, "");
		CatSTR.put(Cat.NTC_B2a, "");
		CatSTR.put(Cat.NTC_B2a, "");
		CatSTR.put(Cat.NTC_B3a, "");
		CatSTR.put(Cat.NTC_B3a, "");
		CatSTR.put(Cat.NTC_B4a, "");
		CatSTR.put(Cat.NTC_B4a, "");
		CatSTR.put(Cat.NTC_B5, "");
		CatSTR.put(Cat.NTC_B6, "");
		CatSTR.put(Cat.NTC_B7, "");
		CatSTR.put(Cat.NTC_B8, "");
		CatSTR.put(Cat.NTC_B9a, "");
		CatSTR.put(Cat.NTC_B9b, "");
		CatSTR.put(Cat.NTC_B11, "");
		CatSTR.put(Cat.NTC_C1, "");
		CatSTR.put(Cat.NTC_C2, "");
		CatSTR.put(Cat.NTC_C3, "");
		CatSTR.put(Cat.NTC_C4, "");
		CatSTR.put(Cat.NTC_C5a, "");
		CatSTR.put(Cat.NTC_C5b, "");
		CatSTR.put(Cat.NTC_D1a, "");
		CatSTR.put(Cat.NTC_D1b, "");
		CatSTR.put(Cat.NTC_D2a, "");
		CatSTR.put(Cat.NTC_D2b, "");
		CatSTR.put(Cat.NTC_D3a, "");
		CatSTR.put(Cat.NTC_D3b, "");
	}

	private Cat category = Cat.NOCAT;

	public Cat getCategory() {
		return category;
	}

	public void setCategory(Cat cat) {
		category = cat;
		repaint();
	}

	public enum Shp {
		UNKSHP, PILLAR, SPAR, CAN, CONI, SPHERI, BARREL, FLOAT, SUPER, BUOYANT, CAIRN, PILE, LATTICE, TOWER, STAKE, POLE, POST, PERCH, BUOY, BEACON
	}

	public static final EnumMap<Shp, String> ShpSTR = new EnumMap<Shp, String>(Shp.class);
	static {
		ShpSTR.put(Shp.PILLAR, "pillar");
		ShpSTR.put(Shp.SPAR, "spar");
		ShpSTR.put(Shp.CAN, "can");
		ShpSTR.put(Shp.CONI, "conical");
		ShpSTR.put(Shp.SPHERI, "spherical");
		ShpSTR.put(Shp.BARREL, "barrel");
		ShpSTR.put(Shp.FLOAT, "float");
		ShpSTR.put(Shp.SUPER, "super-buoy");
		ShpSTR.put(Shp.BUOYANT, "buoyant");
		ShpSTR.put(Shp.CAIRN, "cairn");
		ShpSTR.put(Shp.PILE, "pile");
		ShpSTR.put(Shp.LATTICE, "lattice");
		ShpSTR.put(Shp.TOWER, "tower");
		ShpSTR.put(Shp.STAKE, "stake");
		ShpSTR.put(Shp.PERCH, "perch");
	}

	private Shp shape = Shp.UNKSHP;

	public Shp getShape() {
		return shape;
	}

	public void setShape(Shp shp) {
		shape = shp;
		repaint();
	}

	public enum Col {
		UNKCOL, BLANK, WHITE, RED, ORANGE, AMBER, YELLOW, GREEN, BLUE, VIOLET, BLACK, GREY, BROWN, MAGENTA, PINK
	}

	public static final EnumMap<Col, Color> ColMAP = new EnumMap<Col, Color>(Col.class);
	static {
		ColMAP.put(Col.UNKCOL, new Color(0xc0c0c0));
		ColMAP.put(Col.WHITE, Color.WHITE);
		ColMAP.put(Col.RED, Color.RED);
		ColMAP.put(Col.ORANGE, Color.ORANGE);
		ColMAP.put(Col.AMBER, new Color(0xfbf00f));
		ColMAP.put(Col.YELLOW, Color.YELLOW);
		ColMAP.put(Col.GREEN, Color.GREEN);
		ColMAP.put(Col.BLUE, Color.BLUE);
		ColMAP.put(Col.VIOLET, new Color(0x8f00ff));
		ColMAP.put(Col.BLACK, Color.BLACK);
		ColMAP.put(Col.GREY, Color.GRAY);
		ColMAP.put(Col.BROWN, new Color(0xa45a58));
		ColMAP.put(Col.MAGENTA, Color.MAGENTA);
		ColMAP.put(Col.PINK, Color.PINK);
	}

	public static final EnumMap<Col, String> ColSTR = new EnumMap<Col, String>(Col.class);
	static {
		ColSTR.put(Col.WHITE, "white");
		ColSTR.put(Col.RED, "red");
		ColSTR.put(Col.ORANGE, "orange");
		ColSTR.put(Col.AMBER, "amber");
		ColSTR.put(Col.YELLOW, "yellow");
		ColSTR.put(Col.GREEN, "green");
		ColSTR.put(Col.BLUE, "blue");
		ColSTR.put(Col.VIOLET, "violet");
		ColSTR.put(Col.BLACK, "black");
		ColSTR.put(Col.GREY, "grey");
		ColSTR.put(Col.BROWN, "brown");
		ColSTR.put(Col.MAGENTA, "magenta");
		ColSTR.put(Col.PINK, "pink");
	}

	public Col getColour(Ent ent, int idx) {
		if (ent == Ent.BODY)
			return getObjColour(idx);
		else
			return getTopColour(idx);
	}

	public void setColour(Ent ent, Col col) {
		if (ent == Ent.BODY)
			setObjColour(col);
		else
			setTopColour(col);
	}

	public void setColour(Ent ent, int idx, Col col) {
		if (ent == Ent.BODY)
			setObjColour(idx, col);
		else
			setTopColour(idx, col);
	}

	public void addColour(Ent ent, int idx, Col col) {
		if (ent == Ent.BODY)
			addObjColour(idx, col);
		else
			addTopColour(idx, col);
	}

	public void subColour(Ent ent, int idx) {
		if (ent == Ent.BODY)
			subObjColour(idx);
		else
			subTopColour(idx);
	}

	private ArrayList<Col> bodyColour = new ArrayList<Col>();

	public Col getObjColour(int i) {
		if (i < bodyColour.size())
			return bodyColour.get(i);
		else
			return Col.UNKCOL;
	}

	public void setObjColour(Col col) {
		bodyColour.clear();
		bodyColour.add(col);
		repaint();
	}

	public void setObjColour(int i, Col col) {
		if (i < bodyColour.size())
			bodyColour.set(i, col);
		repaint();
	}

	public void addObjColour(int i, Col col) {
		if (bodyColour.size() >= i)
			bodyColour.add(i, col);
		repaint();
	}

	public void addObjColour(Col col) {
		bodyColour.add(col);
		repaint();
	}

	public void subObjColour(int i) {
		if (bodyColour.size() > i)
			bodyColour.remove(i);
		repaint();
	}

	private ArrayList<Col> topmarkColour = new ArrayList<Col>();

	public Col getTopColour(int i) {
		if (i < topmarkColour.size())
			return topmarkColour.get(i);
		else
			return Col.UNKCOL;
	}

	public void setTopColour(Col col) {
		topmarkColour.clear();
		topmarkColour.add(col);
		repaint();
	}

	public void setTopColour(int i, Col col) {
		if (topmarkColour.size() > i)
			topmarkColour.set(i, col);
		repaint();
	}

	public void addTopColour(int i, Col col) {
		if (topmarkColour.size() >= i)
			topmarkColour.add(i, col);
		repaint();
	}

	public void addTopColour(Col col) {
		topmarkColour.add(col);
		repaint();
	}

	public void subTopColour(int i) {
		if (topmarkColour.size() > i)
			topmarkColour.remove(i);
		repaint();
	}

	public enum Chr {
		UNKCHR, FIXED, FLASH, LFLASH, QUICK, VQUICK, UQUICK, ISOPHASED, OCCULTING, MORSE, ALTERNATING, IQUICK, IVQUICK, IUQUICK
	}

	public static final Map<EnumSet<Chr>, String> ChrMAP = new HashMap<EnumSet<Chr>, String>();
	static {
		ChrMAP.put(EnumSet.of(Chr.FIXED), "F");
		ChrMAP.put(EnumSet.of(Chr.FLASH), "Fl");
		ChrMAP.put(EnumSet.of(Chr.LFLASH), "LFl");
		ChrMAP.put(EnumSet.of(Chr.QUICK), "Q");
		ChrMAP.put(EnumSet.of(Chr.VQUICK), "VQ");
		ChrMAP.put(EnumSet.of(Chr.UQUICK), "UQ");
		ChrMAP.put(EnumSet.of(Chr.ISOPHASED), "Iso");
		ChrMAP.put(EnumSet.of(Chr.OCCULTING), "Oc");
		ChrMAP.put(EnumSet.of(Chr.IQUICK), "IQ");
		ChrMAP.put(EnumSet.of(Chr.IVQUICK), "IVQ");
		ChrMAP.put(EnumSet.of(Chr.IUQUICK), "IUQ");
		ChrMAP.put(EnumSet.of(Chr.MORSE), "Mo");
		ChrMAP.put(EnumSet.of(Chr.FIXED, Chr.FLASH), "FFl");
		ChrMAP.put(EnumSet.of(Chr.FLASH, Chr.LFLASH), "FlLFl");
		ChrMAP.put(EnumSet.of(Chr.FIXED, Chr.OCCULTING), "FOc");
		ChrMAP.put(EnumSet.of(Chr.FIXED, Chr.LFLASH), "FLFl");
		ChrMAP.put(EnumSet.of(Chr.OCCULTING, Chr.FLASH), "OcFl");
		ChrMAP.put(EnumSet.of(Chr.QUICK, Chr.LFLASH), "Q+LFl");
		ChrMAP.put(EnumSet.of(Chr.VQUICK, Chr.LFLASH), "VQ+LFl");
		ChrMAP.put(EnumSet.of(Chr.UQUICK, Chr.LFLASH), "UQ+LFl");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING), "Al");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.OCCULTING), "Al.Oc");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.LFLASH), "Al.LFl");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.FLASH), "Al.Fl");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.FIXED), "Al.F");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.FIXED, Chr.FLASH), "Al.FFl");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.ISOPHASED), "Al.Iso");
	}

	public enum Vis {
		UNKVIS, HIGH, LOW, FAINT, INTEN, UNINTEN, REST, OBS, PARTOBS
	}

	public static final EnumMap<Vis, String> VisSTR = new EnumMap<Vis, String>(Vis.class);
	static {
		VisSTR.put(Vis.HIGH, "high");
		VisSTR.put(Vis.LOW, "low");
		VisSTR.put(Vis.FAINT, "faint");
		VisSTR.put(Vis.INTEN, "intensified");
		VisSTR.put(Vis.UNINTEN, "unintensified");
		VisSTR.put(Vis.REST, "restricted");
		VisSTR.put(Vis.OBS, "obscured");
		VisSTR.put(Vis.PARTOBS, "part_obscured");
	}

	public enum Lit {
		UNKLIT, VERT, HORIZ, DIR, UPPER, LOWER, LEAD, REAR, FRONT, AERO, AIROBS, FOGDET, FLOOD, STRIP, SUBS, SPOT, MOIRE, EMERG, BEAR
	}

	public static final EnumMap<Lit, String> LitSTR = new EnumMap<Lit, String>(Lit.class);
	static {
		LitSTR.put(Lit.VERT, "vertical");
		LitSTR.put(Lit.HORIZ, "horizontal");
		LitSTR.put(Lit.DIR, "directional");
		LitSTR.put(Lit.UPPER, "upper");
		LitSTR.put(Lit.LOWER, "lower");
		LitSTR.put(Lit.LEAD, "leading");
		LitSTR.put(Lit.REAR, "rear");
		LitSTR.put(Lit.FRONT, "front");
		LitSTR.put(Lit.AERO, "aero");
		LitSTR.put(Lit.AIROBS, "air_obstruction");
		LitSTR.put(Lit.FOGDET, "fog_detector");
		LitSTR.put(Lit.FLOOD, "floodlight");
		LitSTR.put(Lit.STRIP, "striplight");
		LitSTR.put(Lit.SUBS, "subsidairy");
		LitSTR.put(Lit.SPOT, "spotlight");
		LitSTR.put(Lit.MOIRE, "moire");
		LitSTR.put(Lit.EMERG, "emergency");
		LitSTR.put(Lit.BEAR, "bearing");
	}

	public enum Exh {
		UNKEXH, H24, DAY, NIGHT, FOG, WARN, STORM
	}

	public static final EnumMap<Exh, String> ExhSTR = new EnumMap<Exh, String>(Exh.class);
	static {
		ExhSTR.put(Exh.H24, "24h");
		ExhSTR.put(Exh.DAY, "day");
		ExhSTR.put(Exh.NIGHT, "night");
		ExhSTR.put(Exh.FOG, "fog");
		ExhSTR.put(Exh.WARN, "warning");
		ExhSTR.put(Exh.STORM, "storm");
	}

	public enum Att {
		COL, CHR, GRP, SEQ, PER, LIT, BEG, END, RAD, HGT, RNG, VIS, EXH, ORT, MLT, ALT
	}

	public Object[] sector = { Col.UNKCOL, "", "", "", "", Lit.UNKLIT, "", "",
			"", "", "", Vis.UNKVIS, Exh.UNKEXH, "", "", Col.UNKCOL };

	private ArrayList<Object[]> sectors = new ArrayList<Object[]>();

	public int getSectorCount() {
		return sectors.size();
	}

	public boolean isSectored() {
		return (sectors.size() > 1);
	}

	public Object getLightAtt(Att att, int i) {
		return getLightAtt(att.ordinal(), i);
	}

	public Object getLightAtt(int att, int i) {
		if (i < sectors.size())
			return sectors.get(i)[att];
		else
			return null;
	}

	public void setLightAtt(Att att, int i, Object obj) {
		setLightAtt(att.ordinal(), i, obj);
	}

	public void setLightAtt(int att, int i, Object obj) {
		if (sectors.size() == i)
			addLight(i);
		if (sectors.size() > i)
			switch (att) {
			case 4:
			case 8:
			case 9:
			case 10:
				sectors.get(i)[att] = validDecimal((String)obj);
				break;
			case 6:
			case 7:
			case 13:
				sectors.get(i)[att] = validDecimal((String)obj, 360);
				break;
			default:
				sectors.get(i)[att] = obj;
			}
		repaint();
	}

	public void addLight(int i) {
		if (sectors.size() >= i) {
			if (sectors.size() == 0)
				sectors.add(sector.clone());
			else
				sectors.add(i, sectors.get(0).clone());
		}
	}

	public void nulLight(int i) {
		if (i == 0) {
			clrLight();
		} else {
			sectors.add(i, sector.clone());
		}
	}

	public void addLight() {
		if (sectors.size() == 0)
			sectors.add(sector.clone());
		else
			sectors.add(sectors.get(0).clone());
	}

	public void delLight(int i) {
		if (sectors.size() > i)
			sectors.remove(i);
		repaint();
	}

	public void clrLight() {
		sectors.clear();
		addLight();
		setLightRef("");
		repaint();
	}

	public enum Pat {
		NOPAT, HSTRP, VSTRP, DIAG, SQUARED, BORDER, CROSS, SALTIRE
	}

	public static final EnumMap<Pat, String> PatSTR = new EnumMap<Pat, String>(Pat.class);
	static {
		PatSTR.put(Pat.HSTRP, "horizontal");
		PatSTR.put(Pat.VSTRP, "vertical");
		PatSTR.put(Pat.DIAG, "diagonal");
		PatSTR.put(Pat.SQUARED, "squared");
		PatSTR.put(Pat.BORDER, "border");
		PatSTR.put(Pat.CROSS, "cross");
		PatSTR.put(Pat.SALTIRE, "saltire");
	}

	public Pat getPattern(Ent ent) {
		if (ent == Ent.BODY)
			return getObjPattern();
		else
			return getTopPattern();
	}

	public void setPattern(Ent ent, Pat pat) {
		if (ent == Ent.BODY)
			setObjPattern(pat);
		else
			setTopPattern(pat);
	}

	private Pat bodyPattern = Pat.NOPAT;

	public Pat getObjPattern() {
		return bodyPattern;
	}

	public void setObjPattern(Pat pat) {
		bodyPattern = pat;
	}

	private Pat topPattern = Pat.NOPAT;

	public Pat getTopPattern() {
		return topPattern;
	}

	public void setTopPattern(Pat pat) {
		topPattern = pat;
	}

	public enum Top {
		NOTOP, CYL, CONE, SPHERE, X_SHAPE, NORTH, SOUTH, EAST, WEST, SPHERES2, BOARD, RHOMBUS, CIRCLE, TRIANGLE, TRIANGLE_INV, SQUARE
	}

	public static final EnumMap<Top, String> TopSTR = new EnumMap<Top, String>(Top.class);
	static {
		TopSTR.put(Top.CYL, "cylinder");
		TopSTR.put(Top.CONE, "cone, point up");
		TopSTR.put(Top.SPHERE, "sphere");
		TopSTR.put(Top.X_SHAPE, "x-shape");
		TopSTR.put(Top.NORTH, "2 cones up");
		TopSTR.put(Top.SOUTH, "2 cones down");
		TopSTR.put(Top.EAST, "2 cones base together");
		TopSTR.put(Top.WEST, "2 cones point together");
		TopSTR.put(Top.SPHERES2, "2 spheres");
		TopSTR.put(Top.BOARD, "board");
		TopSTR.put(Top.RHOMBUS, "rhombus");
		TopSTR.put(Top.CIRCLE, "circle");
		TopSTR.put(Top.TRIANGLE, "triangle, point up");
		TopSTR.put(Top.TRIANGLE_INV, "triangle, point down");
		TopSTR.put(Top.SQUARE, "square");
	}

	private Top topShape = Top.NOTOP;

	public Top getTopmark() {
		return topShape;
	}

	public void setTopmark(Top top) {
		topShape = top;
		repaint();
	}

	public enum Rtb {
		NORTB, REFLECTOR, RACON, RAMARK, LEADING
	}

	public static final EnumMap<Rtb, String> RtbSTR = new EnumMap<Rtb, String>(Rtb.class);
	static {
		RtbSTR.put(Rtb.RACON, "racon");
		RtbSTR.put(Rtb.RAMARK, "ramark");
		RtbSTR.put(Rtb.LEADING, "leading");
	}

	private Rtb RaType = Rtb.NORTB;

	public Rtb getRadar() {
		return RaType;
	}

	public void setRadar(Rtb type) {
		RaType = type;
		if (type == Rtb.NORTB) {
			setRaconGroup("");
			setRaconSequence("");
			setRaconPeriod("");
			setRaconRange("");
			setRaconSector1("");
			setRaconSector2("");
		}
		repaint();
	}

	private String raconGroup = "";

	public String getRaconGroup() {
		return raconGroup;
	}

	public void setRaconGroup(String grp) {
		raconGroup = grp;
		repaint();
	}

	private String raconSequence = "";

	public String getRaconSequence() {
		return raconSequence;
	}

	public void setRaconSequence(String seq) {
		raconSequence = seq;
		repaint();
	}

	private String raconPeriod = "";

	public String getRaconPeriod() {
		return raconPeriod;
	}

	public void setRaconPeriod(String per) {
		raconPeriod = validDecimal(per);
		repaint();
	}

	private String raconRange = "";

	public String getRaconRange() {
		return raconRange;
	}

	public void setRaconRange(String rng) {
		raconRange = validDecimal(rng);
		repaint();
	}

	private String raconSector1 = "";

	public String getRaconSector1() {
		return raconSector1;
	}

	public void setRaconSector1(String sec) {
		raconSector1 = validDecimal(sec);
		repaint();
	}

	private String raconSector2 = "";

	public String getRaconSector2() {
		return raconSector2;
	}

	public void setRaconSector2(String sec) {
		raconSector2 = validDecimal(sec);
		repaint();
	}

	public enum Fog {
		NOFOG, FOGSIG, HORN, SIREN, DIA, BELL, WHIS, GONG, EXPLOS
	}

	public static final EnumMap<Fog, String> FogSTR = new EnumMap<Fog, String>(Fog.class);
	static {
		FogSTR.put(Fog.FOGSIG, "yes");
		FogSTR.put(Fog.HORN, "horn");
		FogSTR.put(Fog.SIREN, "siren");
		FogSTR.put(Fog.DIA, "diaphone");
		FogSTR.put(Fog.BELL, "bell");
		FogSTR.put(Fog.WHIS, "whistle");
		FogSTR.put(Fog.GONG, "gong");
		FogSTR.put(Fog.EXPLOS, "explosion");
	}

	private Fog fogSound = Fog.NOFOG;

	public Fog getFogSound() {
		return fogSound;
	}

	public void setFogSound(Fog sound) {
		fogSound = sound;
		if (sound == Fog.NOFOG) {
			setFogGroup("");
			setFogSequence("");
			setFogPeriod("");
			setFogRange("");
		}
		repaint();
	}

	private String fogGroup = "";

	public String getFogGroup() {
		return fogGroup;
	}

	public void setFogGroup(String grp) {
		fogGroup = grp;
		repaint();
	}

	private String fogSequence = "";

	public String getFogSequence() {
		return fogSequence;
	}

	public void setFogSequence(String seq) {
		fogSequence = seq;
		repaint();
	}

	private String fogRange = "";

	public String getFogRange() {
		return fogRange;
	}

	public void setFogRange(String rng) {
		fogRange = validDecimal(rng);
		repaint();
	}

	private String fogPeriod = "";

	public String getFogPeriod() {
		return fogPeriod;
	}

	public void setFogPeriod(String per) {
		fogPeriod = validDecimal(per);
		repaint();
	}

	public enum Sts {
		UNKSTS, PERM, OCC, REC, NIU, INT, RESV, TEMP, PRIV, MAND, DEST, EXT, ILLUM, HIST, PUB, SYNC, WATCH, UNWAT, DOUBT
	}

	public static final EnumMap<Sts, String> StsSTR = new EnumMap<Sts, String>(Sts.class);
	static {
		StsSTR.put(Sts.PERM, "permanent");
		StsSTR.put(Sts.OCC, "occasional");
		StsSTR.put(Sts.REC, "recommended");
		StsSTR.put(Sts.NIU, "not_in_use");
		StsSTR.put(Sts.INT, "intermittent");
		StsSTR.put(Sts.RESV, "reserved");
		StsSTR.put(Sts.TEMP, "tempory");
		StsSTR.put(Sts.PRIV, "private");
		StsSTR.put(Sts.MAND, "mandatory");
		StsSTR.put(Sts.DEST, "destroyed");
		StsSTR.put(Sts.EXT, "extinguished");
		StsSTR.put(Sts.ILLUM, "illuminated");
		StsSTR.put(Sts.HIST, "historic");
		StsSTR.put(Sts.PUB, "public");
		StsSTR.put(Sts.SYNC, "synchronized");
		StsSTR.put(Sts.WATCH, "watched");
		StsSTR.put(Sts.UNWAT, "unwatched");
		StsSTR.put(Sts.DOUBT, "existence_doubtful");
	}

	private Sts status = Sts.UNKSTS;

	public Sts getStatus() {
		return status;
	}

	public void setStatus(Sts sts) {
		status = sts;
	}

	public enum Cns {
		UNKCNS, BRICK, CONC, BOULD, HSURF, USURF, WOOD, METAL, GLAS, PAINT
	}

	public static final EnumMap<Cns, String> CnsSTR = new EnumMap<Cns, String>(Cns.class);
	static {
		CnsSTR.put(Cns.BRICK, "masonry");
		CnsSTR.put(Cns.CONC, "concreted");
		CnsSTR.put(Cns.BOULD, "boulders");
		CnsSTR.put(Cns.HSURF, "hard_surfaced");
		CnsSTR.put(Cns.USURF, "unsurfaced");
		CnsSTR.put(Cns.WOOD, "wooden");
		CnsSTR.put(Cns.METAL, "metal");
		CnsSTR.put(Cns.GLAS, "grp");
		CnsSTR.put(Cns.PAINT, "painted");
	}

	private Cns construction = Cns.UNKCNS;

	public Cns getConstr() {
		return construction;
	}

	public void setConstr(Cns cns) {
		construction = cns;
	}

	public enum Con {
		UNKCON, CONSP, NCONS, REFL
	}

	public static final EnumMap<Con, String> ConSTR = new EnumMap<Con, String>(Con.class);
	static {
		ConSTR.put(Con.CONSP, "conspicuous");
		ConSTR.put(Con.NCONS, "not_conspicuous");
		ConSTR.put(Con.REFL, "reflector");
	}

	private Con conspicuity = Con.UNKCON;

	public Con getConsp() {
		return conspicuity;
	}

	public void setConsp(Con con) {
		conspicuity = con;
	}

	private Con reflectivity = Con.UNKCON;

	public Con getRefl() {
		return reflectivity;
	}

	public void setRefl(Con con) {
		reflectivity = con;
	}

	public enum Fnc {
	  UNKFNC, HMO, CSTM, HLTH, HOSP, POFF, HOTEL, RWSTA, POLICE, WPOL, PILO, PILL, BANK, DCHQ, TRNS, FACT, PWRS, ADMIN, EDUC, CHCH, CHPL,
	  TMPL, PGDA, SHSH, BTMP, MOSQ, MRBT, LOOK, COMS, TV, RADO, RADR, LSUP, MWAV, COOL, OBSV, TIMB, CLK, CTRL, AMOR, STAD, BUSS,
	  PRHB, RGLN, RSTN, RCMD, INFO
	}

	public static final EnumMap<Fnc, String> FncSTR = new EnumMap<Fnc, String>(Fnc.class);
	static {
		FncSTR.put(Fnc.UNKFNC, "");
		FncSTR.put(Fnc.HMO, "harbour-master");
		FncSTR.put(Fnc.CSTM, "customs");
		FncSTR.put(Fnc.HLTH, "health");
		FncSTR.put(Fnc.HOSP, "hospital");
		FncSTR.put(Fnc.POFF, "post_office");
		FncSTR.put(Fnc.HOTEL, "hotel");
		FncSTR.put(Fnc.RWSTA, "railway_station");
		FncSTR.put(Fnc.POLICE, "police_station");
		FncSTR.put(Fnc.WPOL, "water-police_station");
		FncSTR.put(Fnc.PILO, "pilot_office");
		FncSTR.put(Fnc.PILL, "pilot_lookout");
		FncSTR.put(Fnc.BANK, "bank");
		FncSTR.put(Fnc.DCHQ, "district_control");
		FncSTR.put(Fnc.TRNS, "transit_shed");
		FncSTR.put(Fnc.FACT, "factory");
		FncSTR.put(Fnc.PWRS, "power_station");
		FncSTR.put(Fnc.ADMIN, "administrative");
		FncSTR.put(Fnc.EDUC, "educational");
		FncSTR.put(Fnc.CHCH, "church");
		FncSTR.put(Fnc.CHPL, "chapel");
		FncSTR.put(Fnc.TMPL, "temple");
		FncSTR.put(Fnc.PGDA, "pagoda");
		FncSTR.put(Fnc.SHSH, "shinto_shrine");
		FncSTR.put(Fnc.BTMP, "buddhist_temple");
		FncSTR.put(Fnc.MOSQ, "mosque");
		FncSTR.put(Fnc.MRBT, "marabout");
		FncSTR.put(Fnc.LOOK, "lookout");
		FncSTR.put(Fnc.COMS, "communication");
		FncSTR.put(Fnc.TV, "television");
		FncSTR.put(Fnc.RADO, "radio");
		FncSTR.put(Fnc.RADR, "radar");
		FncSTR.put(Fnc.LSUP, "light_support");
		FncSTR.put(Fnc.MWAV, "microwave");
		FncSTR.put(Fnc.COOL, "cooling");
		FncSTR.put(Fnc.OBSV, "observation");
		FncSTR.put(Fnc.TIMB, "time_ball");
		FncSTR.put(Fnc.CLK, "clock");
		FncSTR.put(Fnc.CTRL, "control");
		FncSTR.put(Fnc.AMOR, "airship_mooring");
		FncSTR.put(Fnc.STAD, "stadium");
		FncSTR.put(Fnc.BUSS, "bus_station");
		FncSTR.put(Fnc.PRHB, "prohibition");
		FncSTR.put(Fnc.RGLN, "regulation");
		FncSTR.put(Fnc.RSTN, "restriction");
		FncSTR.put(Fnc.RCMD, "recommendation");
		FncSTR.put(Fnc.INFO, "information");
	}

	private Fnc function = Fnc.UNKFNC;

	public Fnc getFunc() {
		return function;
	}

	public void setFunc(Fnc fnc) {
		function = fnc;
		repaint();
	}

	public String information = "";

	public String getInfo() {
		return information;
	}

	public void setInfo(String str) {
		information = str.trim();
	}

	public String source = "";

	public String getSource() {
		return source;
	}

	public void setSource(String str) {
		source = str.trim();
	}

	public String elevation = "";

	public String getElevation() {
		return elevation;
	}

	public void setElevation(String str) {
		elevation = validDecimal(str);
	}

	public String height = "";

	public String getObjectHeight() {
		return height;
	}

	public void setObjectHeight(String str) {
		height = validDecimal(str);
	}

	private String channel = "";

	public String getChannel() {
		return channel;
	}

	public void setChannel(String per) {
		channel = validDecimal(per);
		repaint();
	}

	public String ref = "";

	public String getRef() {
		return ref;
	}

	public void setRef(String str) {
		ref = str;
	}

	public String lightRef = "";

	public String getLightRef() {
		return lightRef;
	}

	public void setLightRef(String str) {
		lightRef = str;
	}

	public String fixme = "";

	public String getFixme() {
		return fixme;
	}

	public void setFixme(String str) {
		fixme = str;
	}

	public boolean testValid() {
		boolean tmp = false;
		dlg.manager.showVisualMessage("");
		switch (getObject()) {
		case BCNCAR:
		case BCNLAT:
		case BOYCAR:
		case BOYLAT:
			if ((getCategory() != Cat.NOCAT) && (getShape() != Shp.UNKSHP))
				tmp = true;
			break;
		case BCNISD:
		case BCNSAW:
		case BCNSPP:
		case BOYISD:
		case BOYSAW:
		case BOYSPP:
			if (getShape() != Shp.UNKSHP)
				tmp = true;
			break;
		case FLTCAR:
		case FLTISD:
		case FLTLAT:
		case FLTSAW:
		case FLTSPP:
			if (getObjColour(0) != Col.UNKCOL)
				tmp = true;
			break;
		case LITMAJ:
		case LITMIN:
		case LITFLT:
		case LITVES:
		case LITHSE:
		case SISTAW:
		case SISTAT:
		case OFSPLF:
		case MORFAC:
		case BOYINB:
		case PILBOP:
		case RSCSTA:
		case RDOSTA:
		case RADSTA:
 			tmp = true;
			break;
		case NOTMRK:
			if (getCategory() != Cat.NOCAT) 
				tmp = true;
		case LNDMRK:
			if ((getCategory() != Cat.NOCAT) || (getFunc() != Fnc.UNKFNC))
				tmp = true;
			break;
		}
		if (tmp) {
			dlg.panelMain.moreButton.setVisible(true);
			dlg.panelMain.saveButton.setEnabled(true);
			dlg.panelMain.topButton.setEnabled(true);
			dlg.panelMain.fogButton.setEnabled(true);
			dlg.panelMain.radButton.setEnabled(true);
			dlg.panelMain.litButton.setEnabled(true);
			return true;
		} else {
			dlg.panelMain.moreButton.setVisible(false);
			dlg.panelMain.moreButton.setText(">>");
			dlg.panelMain.topButton.setEnabled(false);
			dlg.panelMain.fogButton.setEnabled(false);
			dlg.panelMain.radButton.setEnabled(false);
			dlg.panelMain.litButton.setEnabled(false);
			dlg.manager.showVisualMessage("Seamark not recognised");
			return false;
		}
	}

	public void clrMark() {
		setName("");
		setObject(Obj.UNKOBJ);
		clrLight();
		setFogSound(Fog.NOFOG);
		setRadar(Rtb.NORTB);
		setStatus(Sts.UNKSTS);
		setConstr(Cns.UNKCNS);
		setConsp(Con.UNKCON);
		setRefl(Con.UNKCON);
		setRef("");
		setObjectHeight("");
		setElevation("");
		setChannel("");
		setInfo("");
		setSource("");
		setFixme("");
		dlg.panelMain.syncPanel();
		repaint();
	}

	public String validDecimal(String str) {
		str = str.trim().replace(',', '.');
		if (!(str.isEmpty()) && !(str.matches("^[+-]??\\d+(\\.\\d+)??$"))) {
			dlg.manager.showVisualMessage(Messages.getString("NotDecimal"));
			return "";
		} else {
			dlg.manager.showVisualMessage("");
			return str;
		}
	}

	public String validDecimal(String str, float max) {
		str = validDecimal(str);
		if (!(str.isEmpty()) && (new Float(str) > max)) {
			dlg.manager.showVisualMessage(Messages.getString("TooBig") + " (" + max + ")");
			return "";
		} else {
			dlg.manager.showVisualMessage("");
			return str;
		}
	}

	public void parseMark(Node node) {
		dlg.manager.showVisualMessage("");
		String str = Main.pref.get("smedplugin.IALA");
		if (str.equals("C"))
			setRegion(Reg.C);
		else if (str.equals("B"))
			setRegion(Reg.B);
		else
			setRegion(Reg.A);

		Map<String, String> keys = node.getKeys();

		str = "";
		if (keys.containsKey("seamark:type"))
			str = keys.get("seamark:type");

		clrMark();
		for (Obj obj : ObjSTR.keySet()) {
			if (ObjSTR.get(obj).equals(str)) {
				setObject(obj);
			}
		}

		if (str.equals("")) {
			dlg.manager.showVisualMessage("No seamark");
		}
		if (getObject() == Obj.UNKOBJ) {
			dlg.manager.showVisualMessage("Seamark not recognised");
		}

		setName("");
		for (Obj obj : ObjSTR.keySet()) {
			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":name")) {
				str = keys.get("seamark:" + ObjSTR.get(obj) + ":name");
				setName(str);
			}
		}
		if (keys.containsKey("seamark:name")) {
			str = keys.get("seamark:name");
			setName(str);
		}
		
		if (keys.containsKey("seamark:longname"))
			longName = keys.get("seamark:longname");
		else
			longName = "";

		for (Obj obj : ObjSTR.keySet()) {
			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":category")) {
				str = keys.get("seamark:" + ObjSTR.get(obj) + ":category");
				setCategory(Cat.NOCAT);
				for (Cat cat : CatSTR.keySet()) {
					if (CatSTR.get(cat).equals(str)) {
						setCategory(cat);
					}
				}
			}
		}

		for (Obj obj : ObjSTR.keySet()) {
			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":shape")) {
				str = keys.get("seamark:" + ObjSTR.get(obj) + ":shape");
				setShape(Shp.UNKSHP);
				for (Shp shp : ShpSTR.keySet()) {
					if (ShpSTR.get(shp).equals(str)) {
						setShape(shp);
					}
				}
			}
		}
		if (getShape() == Shp.UNKSHP) {
			if (EntMAP.get(getObject()) == Ent.BUOY)
				setShape(Shp.BUOY);
			if (EntMAP.get(getObject()) == Ent.BEACON)
				setShape(Shp.BEACON);
			if (EntMAP.get(getObject()) == Ent.LFLOAT)
				if (getObject() == Obj.LITVES)
					setShape(Shp.SUPER);
				else
					setShape(Shp.FLOAT);
		}

		for (Obj obj : ObjSTR.keySet()) {
			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":colour")) {
				str = keys.get("seamark:" + ObjSTR.get(obj) + ":colour");
				bodyColour.clear();
				for (String item : str.split(";")) {
					for (Col col : ColSTR.keySet()) {
						if (ColSTR.get(col).equals(item)) {
							bodyColour.add(col);
						}
					}
				}
			}
		}

		for (Obj obj : ObjSTR.keySet()) {
			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":colour_pattern")) {
				str = keys.get("seamark:" + ObjSTR.get(obj) + ":colour_pattern");
				setObjPattern(Pat.NOPAT);
				for (Pat pat : PatSTR.keySet()) {
					if (PatSTR.get(pat).equals(str)) {
						setObjPattern(pat);
					}
				}
			}

			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":height")) {
				setObjectHeight(keys.get("seamark:" + ObjSTR.get(obj) + ":height"));
			}
			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":elevation")) {
				setElevation(keys.get("seamark:" + ObjSTR.get(obj) + ":elevation"));
			}
			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":channel")) {
				setChannel(keys.get("seamark:" + ObjSTR.get(obj) + ":channel"));
			}
		}

		for (Obj obj : ObjSTR.keySet()) {
			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":function")) {
				str = keys.get("seamark:" + ObjSTR.get(obj) + ":function");
				setFunc(Fnc.UNKFNC);
				for (Fnc fnc : FncSTR.keySet()) {
					if (FncSTR.get(fnc).equals(str)) {
						setFunc(fnc);
					}
				}
			}
		}

		if ((getObject() == Obj.LNDMRK) && (getCategory() == Cat.NOCAT) && (getFunc() == Fnc.UNKFNC)) {
			setObject(Obj.LITHSE);
		}

		if (getObject() == Obj.LITFLT) {
			switch (getObjColour(0)) {
			case RED:
				if ((getObjColour(1) == Col.WHITE) && (getObjColour(2) == Col.UNKCOL)) {
					setObject(Obj.FLTSAW);
					setCategory(Cat.NOCAT);
				} else if (getObjColour(1) == Col.UNKCOL) {
					setObject(Obj.FLTLAT);
					if (getRegion() == Reg.B) {
						setCategory(Cat.LAM_STBD);
					} else {
						setCategory(Cat.LAM_PORT);
					}
				} else if ((getObjColour(1) == Col.GREEN)
						&& (getObjColour(2) == Col.RED)) {
					setObject(Obj.FLTLAT);
					if (getRegion() == Reg.B) {
						setCategory(Cat.LAM_PSTBD);
					} else {
						setCategory(Cat.LAM_PPORT);
					}
				} else if ((getObjColour(1) == Col.WHITE)
						&& (getObjColour(2) == Col.RED)) {
					setObject(Obj.FLTLAT);
					setCategory(Cat.LAM_PORT);
				} else {
					setObject(Obj.FLTSPP);
					setCategory(Cat.NOCAT);
				}
				break;
			case GREEN:
				if (getObjColour(1) == Col.UNKCOL) {
					setObject(Obj.FLTLAT);
					if (getRegion() == Reg.B) {
						setCategory(Cat.LAM_PORT);
					} else {
						setCategory(Cat.LAM_STBD);
					}
				} else if ((getObjColour(1) == Col.RED)
						&& (getObjColour(2) == Col.GREEN)) {
					setObject(Obj.FLTLAT);
					if (getRegion() == Reg.B) {
						setCategory(Cat.LAM_PPORT);
					} else {
						setCategory(Cat.LAM_PSTBD);
					}
				} else if ((getObjColour(1) == Col.WHITE)
						&& (getObjColour(2) == Col.GREEN)) {
					setObject(Obj.FLTLAT);
					setCategory(Cat.LAM_STBD);
				} else {
					setObject(Obj.FLTSPP);
					setCategory(Cat.NOCAT);
				}
				break;
			case YELLOW:
				if (getObjColour(1) == Col.BLACK) {
					setObject(Obj.FLTCAR);
					if (getObjColour(2) == Col.YELLOW) {
						setCategory(Cat.CAM_WEST);
					} else {
						setCategory(Cat.CAM_SOUTH);
					}
				} else {
					setObject(Obj.FLTSPP);
					setCategory(Cat.NOCAT);
				}
				break;
			case BLACK:
				if (getObjColour(1) == Col.RED) {
					setObject(Obj.FLTISD);
					setCategory(Cat.NOCAT);
				} else if (getObjColour(1) == Col.YELLOW) {
					setObject(Obj.FLTCAR);
					if (getObjColour(2) == Col.BLACK) {
						setCategory(Cat.CAM_EAST);
					} else {
						setCategory(Cat.CAM_NORTH);
					}
				} else {
					setObject(Obj.FLTSPP);
					setCategory(Cat.NOCAT);
				}
				break;
			default:
				setCategory(Cat.NOCAT);
			}
		}

		for (Obj obj : ObjSTR.keySet()) {
			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":system")) {
				str = keys.get("seamark:" + ObjSTR.get(obj) + ":system");
				if (str.equals("iala-a"))
					setRegion(Reg.A);
				else if (str.equals("iala-b"))
					setRegion(Reg.B);
				else
					setRegion(Reg.C);
			} else if (GrpMAP.get(object) == Grp.LAT) {
				switch (getCategory()) {
				case LAM_PORT:
					if (getObjColour(0) == Col.RED) {
						if (getObjColour(1) == Col.WHITE)
							setRegion(Reg.C);
						else
							setRegion(Reg.A);
					}
					if (getObjColour(0) == Col.GREEN)
						setRegion(Reg.B);
					break;
				case LAM_PPORT:
					if (getObjColour(0) == Col.RED) {
						if (getObjColour(3) == Col.GREEN)
							setRegion(Reg.C);
						else
							setRegion(Reg.B);
					}
					if (getObjColour(0) == Col.GREEN)
						setRegion(Reg.A);
					break;
				case LAM_STBD:
					if (getObjColour(0) == Col.GREEN) {
						if (getObjColour(1) == Col.WHITE)
							setRegion(Reg.C);
						else
							setRegion(Reg.A);
					}
					if (getObjColour(0) == Col.RED)
						setRegion(Reg.B);
					break;
				case LAM_PSTBD:
					if (getObjColour(0) == Col.GREEN)
						setRegion(Reg.B);
					if (getObjColour(0) == Col.RED) {
						if (getObjColour(3) == Col.GREEN)
							setRegion(Reg.C);
						else
							setRegion(Reg.A);
					}
					break;
				}
			}
		}

		if (keys.containsKey("seamark:topmark:shape")) {
			str = keys.get("seamark:topmark:shape");
			setTopmark(Top.NOTOP);
			for (Top top : TopSTR.keySet()) {
				if (TopSTR.get(top).equals(str)) {
					setTopmark(top);
				}
			}
		}
		if (keys.containsKey("seamark:topmark:colour")) {
			str = keys.get("seamark:topmark:colour");
			setTopColour(Col.UNKCOL);
			for (Col col : ColSTR.keySet()) {
				if (ColSTR.get(col).equals(str)) {
					setTopColour(col);
				}
			}
		}
		if (keys.containsKey("seamark:topmark:colour_pattern")) {
			str = keys.get("seamark:topmark:colour_pattern");
			setTopPattern(Pat.NOPAT);
			for (Pat pat : PatSTR.keySet()) {
				if (PatSTR.get(pat).equals(str)) {
					setTopPattern(pat);
				}
			}
		}

		clrLight();
		for (int i = 0; i < 30; i++) {
			String secStr = (i == 0) ? "" : (":" + Integer.toString(i));
			if (keys.containsKey("seamark:light" + secStr + ":colour")) {
				nulLight(i);
				str = keys.get("seamark:light" + secStr + ":colour");
				if (str.contains(";")) {
					String strs[] = str.split(";");
					for (Col col : ColSTR.keySet())
						if ((strs.length > 1) && ColSTR.get(col).equals(strs[1]))
							setLightAtt(Att.ALT, i, col);
					str = strs[0];
				}
				for (Col col : ColSTR.keySet())
					if (ColSTR.get(col).equals(str))
						setLightAtt(Att.COL, i, col);
			}
			if (keys.containsKey("seamark:light" + secStr + ":character")) {
				str = keys.get("seamark:light" + secStr + ":character");
				if (str.contains("(") && str.contains(")")) {
					int i1 = str.indexOf("(");
					int i2 = str.indexOf(")");
					setLightAtt(Att.GRP, i, str.substring((i1+1), i2));
					str = str.substring(0, i1) + str.substring((i2+1), str.length());
					}
				setLightAtt(Att.CHR, i, str);
			}
			if (keys.containsKey("seamark:light" + secStr + ":group"))
				setLightAtt(Att.GRP, i, keys.get("seamark:light" + secStr + ":group"));
			if (keys.containsKey("seamark:light" + secStr + ":sequence"))
				setLightAtt(Att.SEQ, i, keys.get("seamark:light" + secStr + ":sequence"));
			if (keys.containsKey("seamark:light" + secStr + ":period"))
				setLightAtt(Att.PER, i, keys.get("seamark:light" + secStr + ":period"));
			if (keys.containsKey("seamark:light" + secStr + ":category")) {
				str = keys.get("seamark:light" + secStr + ":category");
				if (str.equals("vert"))
					str = "vertical";
				if (str.equals("horiz"))
					str = "horizontal";
				for (Lit lit : LitSTR.keySet())
					if (LitSTR.get(lit).equals(str))
						setLightAtt(Att.LIT, i, lit);
			}
			if (keys.containsKey("seamark:light" + secStr + ":sector_start"))
				setLightAtt(Att.BEG, i, keys.get("seamark:light" + secStr + ":sector_start"));
			if (keys.containsKey("seamark:light" + secStr + ":sector_end"))
				setLightAtt(Att.END, i, keys.get("seamark:light" + secStr + ":sector_end"));
			if (keys.containsKey("seamark:light" + secStr + ":radius"))
				setLightAtt(Att.RAD, i, keys.get("seamark:light" + secStr + ":radius"));
			if (keys.containsKey("seamark:light" + secStr + ":height"))
				setLightAtt(Att.HGT, i, keys.get("seamark:light" + secStr + ":height"));
			if (keys.containsKey("seamark:light" + secStr + ":range"))
				setLightAtt(Att.RNG, i, keys.get("seamark:light" + secStr + ":range"));
			if (keys.containsKey("seamark:light" + secStr + ":visibility")) {
				str = keys.get("seamark:light" + secStr + ":visibility");
				for (Vis vis : VisSTR.keySet())
					if (VisSTR.get(vis).equals(str))
						setLightAtt(Att.VIS, i, vis);
			}
			if (keys.containsKey("seamark:light" + secStr + ":exhibition")) {
				str = keys.get("seamark:light" + secStr + ":exhibition");
				for (Exh exh : ExhSTR.keySet())
					if (ExhSTR.get(exh).equals(str))
						setLightAtt(Att.EXH, i, exh);
			}
			if (keys.containsKey("seamark:light" + secStr + ":orientation"))
				setLightAtt(Att.ORT, i, keys.get("seamark:light" + secStr + ":orientation"));
			if (keys.containsKey("seamark:light" + secStr + ":multiple"))
				setLightAtt(Att.MLT, i, keys.get("seamark:light" + secStr + ":multiple"));
			
			if (sectors.size() == i)
				break;
		}

		if (keys.containsKey("seamark:fog_signal")) {
			setFogSound(Fog.FOGSIG);
		}
		if (keys.containsKey("seamark:fog_signal:category")) {
			str = keys.get("seamark:fog_signal:category");
			setFogSound(Fog.NOFOG);
			for (Fog fog : FogSTR.keySet()) {
				if (FogSTR.get(fog).equals(str)) {
					setFogSound(fog);
				}
			}
		}
		if (keys.containsKey("seamark:fog_signal:group")) {
			setFogGroup(keys.get("seamark:fog_signal:group"));
		}
		if (keys.containsKey("seamark:fog_signal:period")) {
			setFogPeriod(keys.get("seamark:fog_signal:period"));
		}
		if (keys.containsKey("seamark:fog_signal:sequence")) {
			setFogSequence(keys.get("seamark:fog_signal:sequence"));
		}
		if (keys.containsKey("seamark:fog_signal:range")) {
			setFogRange(keys.get("seamark:fog_signal:range"));
		}

		if (keys.containsKey("seamark:radar_reflector")) {
			setRadar(Rtb.REFLECTOR);
		}
		if (keys.containsKey("seamark:radar_transponder:category")) {
			str = keys.get("seamark:radar_transponder:category");
			setRadar(Rtb.NORTB);
			for (Rtb rtb : RtbSTR.keySet()) {
				if (RtbSTR.get(rtb).equals(str)) {
					setRadar(rtb);
				}
			}
		}
		if (keys.containsKey("seamark:radar_transponder:group")) {
			setRaconGroup(keys.get("seamark:radar_transponder:group"));
		}
		if (keys.containsKey("seamark:radar_transponder:period")) {
			setRaconPeriod(keys.get("seamark:radar_transponder:period"));
		}
		if (keys.containsKey("seamark:radar_transponder:sequence")) {
			setRaconSequence(keys.get("seamark:radar_transponder:sequence"));
		}
		if (keys.containsKey("seamark:radar_transponder:range")) {
			setRaconRange(keys.get("seamark:radar_transponder:range"));
		}
		if (keys.containsKey("seamark:radar_transponder:sector_start")) {
			setRaconSector1(keys.get("seamark:radar_transponder:sector_start"));
		}
		if (keys.containsKey("seamark:radar_transponder:sector_end")) {
			setRaconSector2(keys.get("seamark:radar_transponder:sector_end"));
		}

		if (keys.containsKey("seamark:information")) {
			setInfo(keys.get("seamark:information"));
		}
		if (keys.containsKey("seamark:light:information")) {
			setInfo(getInfo() + keys.get("seamark:light:information"));
		}
		if (keys.containsKey("seamark:" + ObjSTR.get(getObject()) + "information")) {
			setInfo(getInfo() + keys.get("seamark:" + ObjSTR.get(getObject()) + "information"));
		}
		if (keys.containsKey("seamark:source")) {
			setSource(keys.get("seamark:source"));
		}
		if (keys.containsKey("seamark:light:source")) {
			setSource(getSource() + keys.get("seamark:light:source"));
		}
		if (keys.containsKey("seamark:" + ObjSTR.get(getObject()) + "source")) {
			setSource(getSource() + keys.get("seamark:" + ObjSTR.get(getObject()) + "source"));
		}
		if (keys.containsKey("seamark:height")) {
			setObjectHeight(keys.get("seamark:height"));
		}
		if (keys.containsKey("seamark:elevation")) {
			setElevation(keys.get("seamark:elevation"));
		}
		if (keys.containsKey("seamark:status")) {
			str = keys.get("seamark:status");
			setStatus(Sts.UNKSTS);
			for (Sts sts : StsSTR.keySet()) {
				if (StsSTR.get(sts).equals(str)) {
					setStatus(sts);
				}
			}
		}
		if (keys.containsKey("seamark:construction")) {
			str = keys.get("seamark:construction");
			setConstr(Cns.UNKCNS);
			for (Cns cns : CnsSTR.keySet()) {
				if (CnsSTR.get(cns).equals(str)) {
					setConstr(cns);
				}
			}
		}
		if (keys.containsKey("seamark:conspicuity")) {
			str = keys.get("seamark:conspicuity");
			setConsp(Con.UNKCON);
			for (Con con : ConSTR.keySet()) {
				if (ConSTR.get(con).equals(str)) {
					setConsp(con);
				}
			}
		}
		if (keys.containsKey("seamark:reflectivity")) {
			str = keys.get("seamark:reflectivity");
			setRefl(Con.UNKCON);
			for (Con con : ConSTR.keySet()) {
				if (ConSTR.get(con).equals(str)) {
					setRefl(con);
				}
			}
		}

		if (keys.containsKey("seamark:ref")) {
			setRef(keys.get("seamark:ref"));
		}
		if (keys.containsKey("seamark:reference")) {
			setRef(keys.get("seamark:reference"));
		}
		if (keys.containsKey("seamark:light:ref")) {
			setLightRef(keys.get("seamark:light:ref"));
		}
		if (keys.containsKey("seamark:light:reference")) {
			setLightRef(keys.get("seamark:light:reference"));
		}
		if (keys.containsKey("seamark:fixme")) {
			setFixme(keys.get("seamark:fixme"));
		}

		dlg.panelMain.syncPanel();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (dlg.node == null) return;
		
    Graphics2D g2 = (Graphics2D) g;
    
    String colStr;
		String lblStr;
		String imgStr = "/images/";
		if (getShape() != Shp.UNKSHP) {
			switch (getShape()) {
			case TOWER:
				imgStr += "Tower";
				break;
			case BUOY:
			case PILLAR:
				imgStr += "Pillar";
				break;
			case SPAR:
				imgStr += "Spar";
				break;
			case CAN:
				imgStr += "Can";
				break;
			case CONI:
				imgStr += "Cone";
				break;
			case SPHERI:
				imgStr += "Sphere";
				break;
			case BARREL:
				imgStr += "Barrel";
				break;
			case CAIRN:
				imgStr += "Cairn";
				break;
			case FLOAT:
				imgStr += "Float";
				break;
			case BEACON:
			case PILE:
			case LATTICE:
			case BUOYANT:
				imgStr += "Beacon";
				break;
			case SUPER:
				imgStr += "Super";
				break;
			case STAKE:
			case POLE:
			case POST:
				imgStr += "Stake";
				break;
			case PERCH:
				if (getCategory() == Cat.LAM_PORT) {
					imgStr += "Perch_Port";
				} else {
					imgStr += "Perch_Starboard";
				}
				break;
			}
			colStr = imgStr;
			lblStr = "";
			for (Col col : bodyColour) {
				switch (col) {
				case WHITE:
					colStr += "_White";
					lblStr += "W";
					break;
				case RED:
					colStr += "_Red";
					lblStr += "R";
					break;
				case ORANGE:
					colStr += "_Orange";
					lblStr += "Or";
					break;
				case AMBER:
					colStr += "_Amber";
					lblStr += "Am";
					break;
				case YELLOW:
					colStr += "_Yellow";
					lblStr += "Y";
					break;
				case GREEN:
					colStr += "_Green";
					lblStr += "G";
					break;
				case BLUE:
					colStr += "_Blue";
					lblStr += "Bu";
					break;
				case VIOLET:
					colStr += "_Violet";
					lblStr += "Vi";
					break;
				case BLACK:
					colStr += "_Black";
					lblStr += "B";
					break;
				case GREY:
					colStr += "_Grey";
					lblStr += "Gr";
					break;
				case BROWN:
					colStr += "_Brown";
					lblStr += "Br";
					break;
				case MAGENTA:
					colStr += "_Magenta";
					lblStr += "Mg";
					break;
				case PINK:
					colStr += "_Pink";
					lblStr += "Pk";
					break;
				}
			}
			if (!imgStr.equals("/images/")) {
				colStr += ".png";
				if (getClass().getResource(colStr) == null) {
					System.out.println("Missing image1: " + colStr);
					imgStr += ".png";
					if (getClass().getResource(imgStr) == null) {
						System.out.println("Missing image2: " + imgStr);
					} else {
						g2.drawImage(new ImageIcon(getClass().getResource(imgStr)).getImage(), 7, -15, null);
						g2.drawString(lblStr, 75, 110);
					}
				} else {
					g2.drawImage(new ImageIcon(getClass().getResource(colStr)).getImage(), 7, -15, null);
				}
			}
		} else if (getObject() != Obj.UNKOBJ) {
			switch (getObject()) {
			case LNDMRK:
				switch (getCategory()) {
				case LMK_CHMY:
					imgStr += "Chimney";
					break;
				case LMK_CARN:
					imgStr += "Cairn";
					break;
				case LMK_DSHA:
					imgStr += "DishAerial";
					break;
				case LMK_FLGS:
					imgStr += "Flagstaff";
					break;
				case LMK_FLRS:
					imgStr += "FlareStack";
					break;
				case LMK_MNMT:
				case LMK_CLMN:
				case LMK_OBLK:
				case LMK_STAT:
					imgStr += "Monument";
					break;
				case LMK_MAST:
					imgStr += "RadioMast";
					break;
				case LMK_TOWR:
					if ((getFunc() == Fnc.CHCH) || (getFunc() == Fnc.CHPL))
						imgStr += "ChurchTower";
					else
						imgStr += "LandTower";
					break;
				case LMK_WNDM:
					imgStr += "Wind_Motor";
					break;
				case LMK_WTRT:
					imgStr += "WaterTower";
					break;
				case LMK_DOME:
					if ((getFunc() == Fnc.CHCH) || (getFunc() == Fnc.CHPL))
						imgStr += "ChurchDome";
					else
						imgStr += "Dome";
					break;
				case LMK_SPIR:
					if ((getFunc() == Fnc.CHCH) || (getFunc() == Fnc.CHPL))
						imgStr += "ChurchSpire";
					else
						imgStr += "Spire";
					break;
				case LMK_MNRT:
					imgStr += "Minaret";
					break;
				case LMK_WNDS:
					imgStr += "Windsock";
					break;
				case LMK_CROS:
					imgStr += "Cross";
					break;
				case LMK_SCNR:
					imgStr += "Signal_Station";
					break;
				case LMK_WNDL:
					imgStr += "Windmill";
					break;
				case NOCAT:
					switch (getFunc()) {
					case CHCH:
					case CHPL:
						imgStr += "Church";
						break;
					case TMPL:
					case PGDA:
					case SHSH:
					case BTMP:
						imgStr += "Temple";
						break;
					case MOSQ:
						imgStr += "Minaret";
						break;
					case MRBT:
						imgStr += "Spire";
						break;
					}
				}
				break;
			case LITHSE:
				imgStr += "Light_House";
				break;
			case LITMAJ:
				imgStr += "Light_Major";
				break;
			case LITMIN:
				imgStr += "Light_Minor";
				break;
			case LITFLT:
				imgStr += "Float";
				break;
			case LITVES:
				imgStr += "Super";
				break;
			case SISTAW:
				imgStr += "Signal_Station";
				break;
			case SISTAT:
				imgStr += "Signal_Station";
				break;
			case OFSPLF:
				if (getCategory() == Cat.OFP_FPSO)
					imgStr += "Storage";
				else
					imgStr += "Platform";
				break;
			case MORFAC:
				switch (getCategory()) {
				case MOR_DLPN:
					imgStr += "Dolphin";
					break;
				case MOR_DDPN:
					imgStr += "DeviationDolphin";
					break;
				case MOR_POST:
					imgStr += "Post";
					break;
				case MOR_BUOY:
					imgStr += "Sphere";
					break;
				}
				break;
			case BOYINB:
				imgStr += "Super";
				break;
			case CGUSTA:
				imgStr += "Signal_Station";
				break;
			case PILBOP:
				imgStr += "Pilot";
				break;
			case RSCSTA:
				imgStr += "Rescue";
				break;
			case RDOSTA:
			case RADSTA:
				imgStr += "Signal_Station";
				g2.drawImage(new ImageIcon(getClass().getResource("/images/Radar_Station.png")).getImage(), 7, -15, null);
				break;
			}
			if (!imgStr.equals("/images/")) {
				imgStr += ".png";
				if (getClass().getResource(imgStr) == null) {
					System.out.println("Missing image3: " + imgStr);
				} else {
					g2.drawImage(new ImageIcon(getClass().getResource(imgStr)).getImage(), 7, -15, null);
				}
			}
		}

		if (getTopmark() != Top.NOTOP) {
			imgStr = "/images/Top_";
			switch (getTopmark()) {
			case CYL:
				imgStr += "Can";
				break;
			case CONE:
				imgStr += "Cone";
				break;
			case SPHERE:
				imgStr += "Sphere";
				break;
			case X_SHAPE:
				imgStr += "X";
				break;
			case NORTH:
				imgStr += "North";
				break;
			case SOUTH:
				imgStr += "South";
				break;
			case EAST:
				imgStr += "East";
				break;
			case WEST:
				imgStr += "West";
				break;
			case SPHERES2:
				imgStr += "Isol";
				break;
			}
			colStr = imgStr;
			for (Col col : topmarkColour) {
				switch (col) {
				case WHITE:
					colStr += "_White";
					break;
				case RED:
					colStr += "_Red";
					break;
				case ORANGE:
					colStr += "_Orange";
					break;
				case AMBER:
					colStr += "_Amber";
					break;
				case YELLOW:
					colStr += "_Yellow";
					break;
				case GREEN:
					colStr += "_Green";
					break;
				case BLUE:
					colStr += "_Blue";
					break;
				case VIOLET:
					colStr += "_Violet";
					break;
				case BLACK:
					colStr += "_Black";
					break;
				}
			}
			switch (getShape()) {
			case CAN:
			case CONI:
			case SPHERI:
			case BARREL:
				imgStr += "_Buoy_Small";
				colStr += "_Buoy_Small";
				break;
			case PILLAR:
			case SPAR:
				imgStr += "_Buoy";
				colStr += "_Buoy";
				break;
			case FLOAT:
			case SUPER:
				imgStr += "_Float";
				colStr += "_Float";
				break;
			case BUOYANT:
			case CAIRN:
			case PILE:
			case LATTICE:
			case TOWER:
			case STAKE:
			case POLE:
			case POST:
			case BEACON:
				imgStr += "_Beacon";
				colStr += "_Beacon";
				break;
			}
			colStr += ".png";
			if (getClass().getResource(colStr) == null) {
				System.out.println("Missing image4: " + colStr);
				imgStr += ".png";
				if (getClass().getResource(imgStr) == null) {
					System.out.println("Missing image5: " + imgStr);
					return;
				} else {
					g2.drawImage(new ImageIcon(getClass().getResource(imgStr)).getImage(), 7, -15, null);
				}
			} else {
				g2.drawImage(new ImageIcon(getClass().getResource(colStr)).getImage(), 7, -15, null);
			}
		} else {
			if ((getObject() == Obj.BOYINB) || ((getObject() == Obj.MORFAC) && (getCategory() == Cat.MOR_BUOY))) {
				imgStr = "/images/Top_Mooring";
				switch (getShape()) {
				case CAN:
				case CONI:
				case SPHERI:
				case BARREL:
					imgStr += "_Buoy_Small";
					break;
				case FLOAT:
				case SUPER:
					imgStr += "_Float";
					break;
				default:
					if (getObject() == Obj.MORFAC) {
						imgStr += "_Buoy_Small";
					} else {
						imgStr += "_Float";
					}
					break;
				}
				imgStr += ".png";
				if (getClass().getResource(imgStr) == null) {
					System.out.println("Missing image6: " + imgStr);
					return;
				} else {
					g2.drawImage(new ImageIcon(getClass().getResource(imgStr)).getImage(), 7, -15, null);
				}
			}
		}

		for (int i = 1; i < sectors.size(); i++) {
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setStroke(new BasicStroke(6.0f));
	    if (!((String)getLightAtt(Att.BEG, i)).isEmpty() && !((String)getLightAtt(Att.END, i)).isEmpty()) {
				if (getLightAtt(Att.COL, i) != Col.UNKCOL) {
					g2.setPaint(ColMAP.get(getLightAtt(Att.COL, i)));
					Double a0 = 270 - Double.parseDouble((String)getLightAtt(Att.BEG, i));
					Double da = 270 - Double.parseDouble((String)getLightAtt(Att.END, i)) - a0;
					da -= da > 0 ? 360 : 0;
					g2.draw(new Arc2D.Double(12, 15, 140, 140, a0, da, Arc2D.OPEN));
				}
				if (getLightAtt(Att.ALT, i) != Col.UNKCOL) {
					g2.setPaint(ColMAP.get(getLightAtt(Att.ALT, i)));
					Double a0 = 270 - Double.parseDouble((String)getLightAtt(Att.BEG, i));
					Double da = 270 - Double.parseDouble((String)getLightAtt(Att.END, i)) - a0;
					da -= da > 0 ? 360 : 0;
					g2.draw(new Arc2D.Double(17, 20, 130, 130, a0, da, Arc2D.OPEN));
				}
	    } else if ((getLightAtt(Att.LIT, i) == Lit.DIR) && !((String)getLightAtt(Att.ORT, i)).isEmpty()) {
				if (getLightAtt(Att.COL, i) != Col.UNKCOL) {
					g2.setPaint(ColMAP.get(getLightAtt(Att.COL, i)));
					Double a0 = 270 - Double.parseDouble((String)getLightAtt(Att.ORT, i)) + 2.0;
					Double da = -4.0;
					g2.draw(new Arc2D.Double(12, 15, 140, 140, a0, da, Arc2D.OPEN));
				}
				if (getLightAtt(Att.ALT, i) != Col.UNKCOL) {
					g2.setPaint(ColMAP.get(getLightAtt(Att.ALT, i)));
					Double a0 = 270 - Double.parseDouble((String)getLightAtt(Att.ORT, i)) + 2.0;
					Double da = -4.0;
					g2.draw(new Arc2D.Double(17, 20, 130, 130, a0, da, Arc2D.OPEN));
				}
	    }
		}
    g2.setPaint(Color.BLACK);
		if ((getLightAtt(Att.COL, 0) != Col.UNKCOL) || !(((String)getLightAtt(Att.CHR, 0)).isEmpty())) {
			if (sectors.size() == 1) {
				if (((String) getLightAtt(Att.CHR, 0)).contains("Al")) {
					g2.drawImage(new ImageIcon(getClass().getResource("/images/Light_Magenta_120.png")).getImage(), 7, -15, null);
				} else {
					switch ((Col) getLightAtt(Att.COL, 0)) {
					case RED:
						g2.drawImage(new ImageIcon(getClass().getResource("/images/Light_Red_120.png")).getImage(), 7, -15, null);
						break;
					case GREEN:
						g2.drawImage(new ImageIcon(getClass().getResource("/images/Light_Green_120.png")).getImage(), 7, -15, null);
						break;
					case WHITE:
					case YELLOW:
						g2.drawImage(new ImageIcon(getClass().getResource("/images/Light_White_120.png")).getImage(), 7, -15, null);
						break;
					default:
						g2.drawImage(new ImageIcon(getClass().getResource("/images/Light_Magenta_120.png")).getImage(), 7, -15, null);
					}
				}
			}
			String c = (String) getLightAtt(Att.CHR, 0);
			String tmp = "";
			if (c.contains("+")) {
				int i1 = c.indexOf("+");
				tmp = c.substring(i1, c.length());
				c = c.substring(0, i1);
				if (!((String) getLightAtt(Att.GRP, 0)).isEmpty()) {
					c += "(" + (String) getLightAtt(Att.GRP, 0) + ")";
				}
				if (tmp != null)
					c += tmp;
			} else if (!((String) getLightAtt(Att.GRP, 0)).isEmpty())
				c += "(" + (String) getLightAtt(Att.GRP, 0) + ")";
			switch ((Col) getLightAtt(Att.COL, 0)) {
			case WHITE:
				c += ".W";
				break;
			case YELLOW:
				c += ".Y";
				break;
			case RED:
				c += ".R";
				break;
			case GREEN:
				c += ".G";
				break;
			case AMBER:
				c += ".Am";
				break;
			case ORANGE:
				c += ".Or";
				break;
			case BLUE:
				c += ".Bu";
				break;
			case VIOLET:
				c += ".Vi";
				break;
			}
			switch ((Col) getLightAtt(Att.ALT, 0)) {
			case WHITE:
				c += "W";
				break;
			case YELLOW:
				c += "Y";
				break;
			case RED:
				c += "R";
				break;
			case GREEN:
				c += "G";
				break;
			case AMBER:
				c += "Am";
				break;
			case ORANGE:
				c += "Or";
				break;
			case BLUE:
				c += "Bu";
				break;
			case VIOLET:
				c += "Vi";
				break;
			}
			tmp = (String) getLightAtt(Att.MLT, 0);
			if (!tmp.isEmpty())
				c = tmp + c;
			if (getLightAtt(Att.LIT, 0) != Lit.UNKLIT) {
				switch ((Lit)getLightAtt(Att.LIT, 0)) {
				case VERT:
					c += "(Vert)";
					break;
				case HORIZ:
						c += "(Horiz)";
						break;
				}
			}
			tmp = (String) getLightAtt(Att.PER, 0);
			if (!tmp.isEmpty())
				c += " " + tmp + "s";
			g2.drawString(c, 100, 70);
		}

		if (getFogSound() != Fog.NOFOG) {
			g2.drawImage(new ImageIcon(getClass().getResource("/images/Fog_Signal.png")).getImage(), 7, -15, null);
			String str = "";
			if (getFogSound() != Fog.FOGSIG)
				switch (getFogSound()) {
				case HORN:
					str = "Horn";
					break;
				case SIREN:
					str = "Siren";
					break;
				case DIA:
					str = "Dia";
					break;
				case BELL:
					str = "Bell";
					break;
				case WHIS:
					str = "Whis";
					break;
				case GONG:
					str = "Gong";
					break;
				case EXPLOS:
					str = "Explos";
					break;
				}
			if (!getFogGroup().isEmpty())
				str += ("(" + getFogGroup() + ")");
			else
				str += " ";
			if (!getFogPeriod().isEmpty())
				str += getFogPeriod() + "s";
			g2.drawString(str, 0, 70);
		}

		if (RaType != Rtb.NORTB) {
			if (getRadar() == Rtb.REFLECTOR) {
				g2.drawImage(new ImageIcon(getClass().getResource("/images/Radar_Reflector_355.png")).getImage(), 7, -15, null);
			} else {
				g2.drawImage(new ImageIcon(getClass().getResource("/images/Radar_Station.png")).getImage(), 7, -15, null);
				String str = "";
				if (getRadar() == Rtb.RAMARK)
					str += "Ramark";
				else
					str += "Racon";
				if (!getRaconGroup().isEmpty())
					str += ("(" + getRaconGroup() + ")");
				else
					str += " ";
				if (!getRaconPeriod().isEmpty())
					str += getRaconPeriod() + "s";
				g2.drawString(str, 0, 50);
			}
		}
	}

	public void saveSign(Node node) {
		
		if (getObject() != Obj.UNKOBJ) {

			Main.pref.put("smedplugin.IALA", getRegion() == Reg.C ? "C" : (getRegion() == Reg.B ? "B" : "A"));

			for (String str : node.getKeys().keySet()) {
				if (str.trim().matches("^seamark:\\S+"))
					Main.main.undoRedo.add(new ChangePropertyCommand(node, str, null));
			}

			if (!getName().isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:name", getName()));

			if (!longName.isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:longname", longName));

			String objStr = ObjSTR.get(object);
			if (objStr != null) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:type", objStr));

				if (getShape() != Shp.FLOAT) {
					String str = CatSTR.get(getCategory());
					if (str != null)
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":category", str));
					if ((getShape() != Shp.BUOY) && (getShape() != Shp.BEACON))
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":shape", ShpSTR.get(getShape())));
				}

				if ((getObjColour(0) != Col.UNKCOL) && getShape() != Shp.PERCH) {
					String str = ColSTR.get(getObjColour(0));
					for (int i = 1; bodyColour.size() > i; i++) {
						str += (";" + ColSTR.get(getObjColour(i)));
					}
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour", str));
				}

				if (getObjPattern() != Pat.NOPAT) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour_pattern", PatSTR.get(getObjPattern())));
				}

				if (getFunc() != Fnc.UNKFNC) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":function", FncSTR.get(getFunc())));
				}

				if ((GrpMAP.get(object) == Grp.LAT) && (getShape() != Shp.PERCH)
						|| (getObject() == Obj.FLTLAT)) {
					switch (region) {
					case A:
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":system", "iala-a"));
						break;
					case B:
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":system", "iala-b"));
						break;
					case C:
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":system", "other"));
						break;
					}
				}
				if (!getObjectHeight().isEmpty()) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":height", getObjectHeight()));
				}
				if (!getElevation().isEmpty()) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":elevation", getElevation()));
				}
				if (!getChannel().isEmpty()) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":channel", getChannel()));
				}
			}
			if (getTopmark() != Top.NOTOP) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:topmark:shape", TopSTR.get(getTopmark())));
				if (getTopPattern() != Pat.NOPAT)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:topmark:colour_pattern", PatSTR.get(getTopPattern())));
				if (getTopColour(0) != Col.UNKCOL) {
					String str = ColSTR.get(getTopColour(0));
					for (int i = 1; topmarkColour.size() > i; i++) {
						str += (";" + ColSTR.get(getTopColour(i)));
					}
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:topmark:colour", str));
				}
			}

			for (int i = (sectors.size() > 1 ? 1 : 0); i < sectors.size(); i++) {
				String secStr = (i == 0) ? "" : (":" + Integer.toString(i));
				if (sectors.get(i)[0] != Col.UNKCOL)
					if ((sectors.get(i)[15] != Col.UNKCOL) && ((String)sectors.get(i)[1]).contains("Al"))
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":colour", (ColSTR.get(sectors.get(i)[0])) + ";" + ColSTR.get(sectors.get(i)[15])));
					else
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":colour", ColSTR.get(sectors.get(i)[0])));
				if (!((String) sectors.get(i)[1]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":character", (String) sectors.get(i)[1]));
				else if (!((String) sectors.get(0)[1]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":character", (String) sectors.get(0)[1]));
				if (!((String) sectors.get(i)[2]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":group", (String) sectors.get(i)[2]));
				else if (!((String) sectors.get(0)[2]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":group", (String) sectors.get(0)[2]));
				if (!((String) sectors.get(i)[3]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":sequence", (String) sectors.get(i)[3]));
				else if (!((String) sectors.get(0)[3]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":sequence", (String) sectors.get(0)[3]));
				if (!((String) sectors.get(i)[4]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":period", (String) sectors.get(i)[4]));
				else if (!((String) sectors.get(0)[4]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":period", (String) sectors.get(0)[4]));
				if (sectors.get(i)[5] != Lit.UNKLIT)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":category", LitSTR.get(sectors.get(i)[5])));
				else if (sectors.get(0)[5] != Lit.UNKLIT)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":category", LitSTR.get(sectors.get(0)[5])));
				if (!((String) sectors.get(i)[6]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":sector_start", (String) sectors.get(i)[6]));
				if (!((String) sectors.get(i)[7]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":sector_end", (String) sectors.get(i)[7]));
				if (!((String) sectors.get(i)[8]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":radius", (String) sectors.get(i)[8]));
				else if (!((String) sectors.get(0)[8]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":radius", (String) sectors.get(0)[8]));
				if (!((String) sectors.get(i)[9]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":height", (String) sectors.get(i)[9]));
				else if (!((String) sectors.get(0)[9]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":height", (String) sectors.get(0)[9]));
				if (!((String) sectors.get(i)[10]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":range", (String) sectors.get(i)[10]));
				else if (!((String) sectors.get(0)[10]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":range", (String) sectors.get(0)[10]));
				if (sectors.get(i)[11] != Vis.UNKVIS)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":visibility", VisSTR.get(sectors.get(i)[11])));
				else if (sectors.get(0)[11] != Vis.UNKVIS)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":visibility", VisSTR.get(sectors.get(0)[11])));
				if (sectors.get(i)[12] != Exh.UNKEXH)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":exhibition", ExhSTR.get(sectors.get(i)[12])));
				else if (sectors.get(0)[12] != Exh.UNKEXH)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":exhibition", ExhSTR.get(sectors.get(0)[12])));
				if (!((String) sectors.get(i)[13]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":orientation", (String) sectors.get(i)[13]));
				if (!((String) sectors.get(i)[14]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":multiple", (String) sectors.get(i)[14]));
				else if (!((String) sectors.get(0)[14]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":multiple", (String) sectors.get(0)[14]));
			}

			if (getFogSound() != Fog.NOFOG) {
				if (getFogSound() == Fog.FOGSIG)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal", "yes"));
				else
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:category", FogSTR.get(getFogSound())));
				if (!getFogGroup().isEmpty()) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:group", getFogGroup()));
				}
				if (!getFogPeriod().isEmpty()) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:period", getFogPeriod()));
				}
				if (!getFogSequence().isEmpty()) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:sequence", getFogSequence()));
				}
				if (!getFogRange().isEmpty()) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:range", getFogRange()));
				}
			}

			if (RaType != Rtb.NORTB) {
				if (getRadar() == Rtb.REFLECTOR) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_reflector", "yes"));
				} else {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder:category", RtbSTR.get(getRadar())));
					if (!getRaconGroup().isEmpty()) {
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder:group", getRaconGroup()));
					}
					if (!getRaconPeriod().isEmpty()) {
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder:period", getRaconPeriod()));
					}
					if (!getRaconSequence().isEmpty()) {
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder:sequence", getRaconSequence()));
					}
					if (!getRaconRange().isEmpty()) {
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder:range", getRaconRange()));
					}
					if ((!getRaconSector1().isEmpty()) && (!getRaconSector2().isEmpty())) {
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder:sector_start", getRaconSector1()));
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder:sector_end", getRaconSector2()));
					}
				}
			}

			if (!getInfo().isEmpty()) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:information", getInfo()));
			}
			if (!getSource().isEmpty()) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:source", getSource()));
			}
			if (getStatus() != Sts.UNKSTS) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:status", StsSTR.get(getStatus())));
			}
			if (getConstr() != Cns.UNKCNS) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:construction", CnsSTR.get(getConstr())));
			}
			if (getConsp() != Con.UNKCON) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:conspicuity", ConSTR.get(getConsp())));
			}
			if (getRefl() != Con.UNKCON) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:reflectivity", ConSTR.get(getRefl())));
			}
			if (!getRef().isEmpty()) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:reference", getRef()));
			}
			if (!getLightRef().isEmpty()) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:reference", getLightRef()));
			}
			if (!getFixme().isEmpty()) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fixme", getFixme()));
			}
		}
	}

}
