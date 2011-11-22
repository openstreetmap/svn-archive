package oseam.seamarks;

import java.awt.*;
import javax.swing.*;
import java.util.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.command.ChangePropertyCommand;

import oseam.dialogs.OSeaMAction;

public class SeaMark {

	public OSeaMAction dlg = null;

	public SeaMark(OSeaMAction dia) {
		dlg = dia;
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
	}

	public enum Obj {
		UNKNOWN, BCNCAR, BCNISD, BCNLAT, BCNSAW, BCNSPP,
		BOYCAR, BOYISD, BOYLAT, BOYSAW, BOYSPP,
		FLTCAR, FLTISD, FLTLAT, FLTSAW, FLTSPP,
		LITMAJ, LITMIN, LITFLT, LITVES, LITHSE, LNDMRK,
		MORFAC, SISTAW, SISTAT
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
		ObjSTR.put(Obj.LNDMRK, "landmark");
		ObjSTR.put(Obj.LITHSE, "landmark");
		ObjSTR.put(Obj.MORFAC, "mooring");
		ObjSTR.put(Obj.SISTAW, "signal_station_warning");
		ObjSTR.put(Obj.SISTAT, "signal_station_traffic");
	}

	private Obj object = Obj.UNKNOWN;

	public Obj getObject() {
		return object;
	}

	public void setObject(Obj obj) {
		object = obj;
	}

	public enum Ent {
		BODY, BUOY, BEACON, FLOAT, TOPMARK, LIGHT, MOORING, STATION
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
		EntMAP.put(Obj.LITFLT, Ent.FLOAT);
		EntMAP.put(Obj.FLTCAR, Ent.FLOAT);
		EntMAP.put(Obj.FLTLAT, Ent.FLOAT);
		EntMAP.put(Obj.FLTSAW, Ent.FLOAT);
		EntMAP.put(Obj.FLTISD, Ent.FLOAT);
		EntMAP.put(Obj.FLTSPP, Ent.FLOAT);
		EntMAP.put(Obj.LITVES, Ent.FLOAT);
		EntMAP.put(Obj.LITHSE, Ent.LIGHT);
		EntMAP.put(Obj.LNDMRK, Ent.LIGHT);
		EntMAP.put(Obj.MORFAC, Ent.MOORING);
		EntMAP.put(Obj.SISTAW, Ent.STATION);
		EntMAP.put(Obj.SISTAT, Ent.STATION);
	}

	public enum Grp {
		NUL, LAT, CAR, SAW, ISD, SPP, LIT, SIS
	}

	public static final EnumMap<Obj, Grp> GrpMAP = new EnumMap<Obj, Grp>(Obj.class);
	static {
		GrpMAP.put(Obj.UNKNOWN, Grp.NUL);
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
		GrpMAP.put(Obj.LITFLT, Grp.LIT);
		GrpMAP.put(Obj.LITMAJ, Grp.LIT);
		GrpMAP.put(Obj.LITMIN, Grp.LIT);
		GrpMAP.put(Obj.LITVES, Grp.LIT);
		GrpMAP.put(Obj.LITHSE, Grp.LIT);
		GrpMAP.put(Obj.LNDMRK, Grp.LIT);
		GrpMAP.put(Obj.MORFAC, Grp.SPP);
		GrpMAP.put(Obj.SISTAW, Grp.SIS);
		GrpMAP.put(Obj.SISTAT, Grp.SIS);
	}

	public enum Cat {
		NONE, LAM_PORT, LAM_STBD, LAM_PPORT, LAM_PSTBD, CAM_NORTH, CAM_EAST, CAM_SOUTH, CAM_WEST,
		ACH_URST, ACH_DEEP, ACH_TANK, ACH_EXPL, ACH_QUAR, ACH_SPLN, ACH_SCAN, ACH_SCMO, ACH_T24H, ACH_TLIM,
		SPM_UNKN, SPM_WARN, SPM_CHBF, SPM_YCHT, SPM_CABL, SPM_OFAL, SPM_ODAS, SPM_RECN, SPM_MOOR, SPM_LNBY,
		SPM_LDNG, SPM_NOTC, SPM_TSS, SPM_FOUL, SPM_DIVE, SPM_FRRY, SPM_ANCH,
		MOR_DLPN, MOR_DDPN, MOR_BLRD, MOR_WALL, MOR_POST, MOR_CHWR, MOR_BUOY,
		SIS_PTCL, SIS_PTED, SIS_IPT, SIS_BRTH, SIS_DOCK, SIS_LOCK, SIS_FBAR, SIS_BRDG, SIS_DRDG, SIS_TRFC,
		SIS_DNGR, SIS_OBST, SIS_CABL, SIS_MILY, SIS_DSTR, SIS_WTHR, SIS_STRM, SIS_ICE, SIS_TIME, SIS_TIDE,
		SIS_TSTM, SIS_TGAG, SIS_TSCL, SIS_DIVE, SIS_LGAG,
		LIT_DIRF, LIT_LEDG, LMK_CHMY, LMK_CARN, LMK_DSHA, LMK_FLGS, LMK_FLRS, LMK_MNMT, LMK_RADM, LMK_TOWR, LMK_WNDM, LMK_WTRT
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
		CatSTR.put(Cat.MOR_BUOY, "buoy");
		CatSTR.put(Cat.SIS_PTCL, "control");
		CatSTR.put(Cat.SIS_PTED, "entry");
		CatSTR.put(Cat.SIS_IPT, "IPT");
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
		CatSTR.put(Cat.LMK_RADM, "radio_mast");
		CatSTR.put(Cat.LMK_TOWR, "tower");
		CatSTR.put(Cat.LMK_WNDM, "windmotor");
		CatSTR.put(Cat.LMK_WTRT, "water_tower");
	}

	private Cat category = Cat.NONE;

	public Cat getCategory() {
		return category;
	}

	public void setCategory(Cat cat) {
		category = cat;
	}

	public enum Shp {
		UNKNOWN, PILLAR, SPAR, CAN, CONE, SPHERE, BARREL, FLOAT, SUPER, BUOYANT, CAIRN, PILE, LATTICE, TOWER, STAKE, POLE, POST, PERCH, BUOY, BEACON
	}

	public static final EnumMap<Shp, String> ShpSTR = new EnumMap<Shp, String>(Shp.class);
	static {
		ShpSTR.put(Shp.PILLAR, "pillar");
		ShpSTR.put(Shp.SPAR, "spar");
		ShpSTR.put(Shp.CAN, "can");
		ShpSTR.put(Shp.CONE, "conical");
		ShpSTR.put(Shp.SPHERE, "spherical");
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

	private Shp shape = Shp.UNKNOWN;

	public Shp getShape() {
		return shape;
	}

	public void setShape(Shp shp) {
		shape = shp;
	}

	public enum Col {
		UNKNOWN, BLANK, WHITE, RED, ORANGE, AMBER, YELLOW, GREEN, BLUE, VIOLET, BLACK, GREY, BROWN, MAGENTA, PINK
	}

	public static final EnumMap<Col, Color> ColMAP = new EnumMap<Col, Color>(Col.class);
	static {
		ColMAP.put(Col.UNKNOWN, new Color(0xc0c0c0));
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
			return Col.UNKNOWN;
	}

	public void setObjColour(Col col) {
		bodyColour.clear();
		bodyColour.add(col);
	}

	public void setObjColour(int i, Col col) {
		if (bodyColour.size() > i)
			bodyColour.set(i, col);
	}

	public void addObjColour(int i, Col col) {
		if (bodyColour.size() >= i)
			bodyColour.add(i, col);
	}

	public void addObjColour(Col col) {
		bodyColour.add(col);
	}

	public void subObjColour(int i) {
		if (bodyColour.size() > i)
			bodyColour.remove(i);
	}

	private ArrayList<Col> topmarkColour = new ArrayList<Col>();

	public Col getTopColour(int i) {
		if (i < topmarkColour.size())
			return topmarkColour.get(i);
		else
			return Col.UNKNOWN;
	}

	public void setTopColour(Col col) {
		topmarkColour.clear();
		topmarkColour.add(col);
	}

	public void setTopColour(int i, Col col) {
		if (topmarkColour.size() > i)
			topmarkColour.set(i, col);
	}

	public void addTopColour(int i, Col col) {
		if (topmarkColour.size() >= i)
			topmarkColour.add(i, col);
	}

	public void addTopColour(Col col) {
		topmarkColour.add(col);
	}

	public void subTopColour(int i) {
		if (topmarkColour.size() > i)
			topmarkColour.remove(i);
	}

	public enum Chr {
		UNKNOWN, FIXED, FLASH, LFLASH, QUICK, VQUICK, UQUICK, ISOPHASED, OCCULTING, MORSE, ALTERNATING, IQUICK, IVQUICK, IUQUICK
	}

	public static final Map<EnumSet<Chr>, String> ChrMAP = new HashMap<EnumSet<Chr>, String>();
	static {
		ChrMAP.put(EnumSet.of(Chr.UNKNOWN), "");
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
		ChrMAP.put(EnumSet.of(Chr.OCCULTING, Chr.FLASH), "OcFl");
		ChrMAP.put(EnumSet.of(Chr.FIXED, Chr.LFLASH), "FLFl");
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
		UNKNOWN, HIGH, LOW, FAINT, INTEN, UNINTEN, REST, OBS, PARTOBS
	}

	public static final EnumMap<Vis, String> VisSTR = new EnumMap<Vis, String>(Vis.class);
	static {
		VisSTR.put(Vis.UNKNOWN, "");
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
		UNKNOWN, VERT, HORIZ, DIR, UPPER, LOWER, LEAD, REAR, FRONT, AERO, AIROBS, FOGDET, FLOOD, STRIP, SUBS, SPOT, MOIRE, EMERG, BEAR
	}

	public static final EnumMap<Lit, String> LitSTR = new EnumMap<Lit, String>(Lit.class);
	static {
		LitSTR.put(Lit.UNKNOWN, "");
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
		UNKNOWN, H24, DAY, NIGHT, FOG
	}

	public static final EnumMap<Exh, String> ExhSTR = new EnumMap<Exh, String>(Exh.class);
	static {
		ExhSTR.put(Exh.UNKNOWN, "");
		ExhSTR.put(Exh.H24, "24h");
		ExhSTR.put(Exh.DAY, "day");
		ExhSTR.put(Exh.NIGHT, "night");
		ExhSTR.put(Exh.FOG, "fog");
	}

	public enum Att {
		COL, CHR, GRP, SEQ, PER, LIT, BEG, END, RAD, HGT, RNG, VIS, EXH, ORT, MLT
	}

	public Object[] sector = { Col.UNKNOWN, "", "", "", "", Lit.UNKNOWN, "", "",
			"", "", "", Vis.UNKNOWN, Exh.UNKNOWN, "", "" };

	private ArrayList<Object[]> sectors = new ArrayList<Object[]>();

	public int getSectorCount() {
		return sectors.size();
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
			sectors.get(i)[att] = obj;
	}

	public void addLight(int i) {
		if (sectors.size() >= i) {
			if (sectors.size() == 0)
				sectors.add(sector.clone());
			else
				sectors.add(i, sectors.get(0).clone());
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
	}

	public void clrLight() {
		delLight(0);
		addLight();
	}

	public enum Pat {
		NONE, HORIZ, VERT, DIAG, SQUARE, BORDER
	}

	public static final EnumMap<Pat, String> PatSTR = new EnumMap<Pat, String>(Pat.class);
	static {
		PatSTR.put(Pat.HORIZ, "horizontal");
		PatSTR.put(Pat.VERT, "vertical");
		PatSTR.put(Pat.DIAG, "diagonal");
		PatSTR.put(Pat.SQUARE, "squared");
		PatSTR.put(Pat.BORDER, "border");
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

	private Pat bodyPattern = Pat.NONE;

	public Pat getObjPattern() {
		return bodyPattern;
	}

	public void setObjPattern(Pat pat) {
		bodyPattern = pat;
	}

	private Pat topPattern = Pat.NONE;

	public Pat getTopPattern() {
		return topPattern;
	}

	public void setTopPattern(Pat pat) {
		topPattern = pat;
	}

	public enum Top {
		NONE, CAN, CONE, SPHERE, X_SHAPE, NORTH, SOUTH, EAST, WEST, SPHERES2, BOARD, DIAMOND, CIRCLE, TRIANGLE, TRIANGLE_INV, SQUARE
	}

	public static final EnumMap<Top, String> TopSTR = new EnumMap<Top, String>(Top.class);
	static {
		TopSTR.put(Top.CAN, "cylinder");
		TopSTR.put(Top.CONE, "cone, point up");
		TopSTR.put(Top.SPHERE, "sphere");
		TopSTR.put(Top.X_SHAPE, "x-shape");
		TopSTR.put(Top.NORTH, "2 cones up");
		TopSTR.put(Top.SOUTH, "2 cones down");
		TopSTR.put(Top.EAST, "2 cones base together");
		TopSTR.put(Top.WEST, "2 cones point together");
		TopSTR.put(Top.SPHERES2, "2 spheres");
		TopSTR.put(Top.BOARD, "board");
		TopSTR.put(Top.DIAMOND, "diamond");
		TopSTR.put(Top.CIRCLE, "circle");
		TopSTR.put(Top.TRIANGLE, "triangle, point up");
		TopSTR.put(Top.TRIANGLE_INV, "triangle, point down");
		TopSTR.put(Top.SQUARE, "square");
	}

	private Top topShape = Top.NONE;

	public Top getTopmark() {
		return topShape;
	}

	public void setTopmark(Top top) {
		topShape = top;
	}

	public enum Rtb {
		NONE, REFLECTOR, RACON, RAMARK, LEADING
	}

	public static final EnumMap<Rtb, String> RtbSTR = new EnumMap<Rtb, String>(Rtb.class);
	static {
		RtbSTR.put(Rtb.RACON, "racon");
		RtbSTR.put(Rtb.RAMARK, "ramark");
		RtbSTR.put(Rtb.LEADING, "leading");
	}

	private Rtb RaType = Rtb.NONE;

	public Rtb getRadar() {
		return RaType;
	}

	public void setRadar(Rtb type) {
		RaType = type;
	}

	private String raconGroup = "";

	public String getRaconGroup() {
		return raconGroup;
	}

	public void setRaconGroup(String grp) {
		raconGroup = grp;
	}

	private String raconSequence = "";

	public String getRaconSequence() {
		return raconSequence;
	}

	public void setRaconSequence(String seq) {
		raconSequence = seq;
	}

	private String raconPeriod = "";

	public String getRaconPeriod() {
		return raconPeriod;
	}

	public void setRaconPeriod(String per) {
		raconPeriod = validDecimal(per);
	}

	private String raconRange = "";

	public String getRaconRange() {
		return raconRange;
	}

	public void setRaconRange(String rng) {
		raconRange = validDecimal(rng);
	}

	private String raconSector1 = "";

	public String getRaconSector1() {
		return raconSector1;
	}

	public void setRaconSector1(String sec) {
		raconSector1 = validDecimal(sec);
	}

	private String raconSector2 = "";

	public String getRaconSector2() {
		return raconSector2;
	}

	public void setRaconSector2(String sec) {
		raconSector2 = validDecimal(sec);
	}

	public enum Fog {
		NONE, UNKNOWN, HORN, SIREN, DIA, BELL, WHIS, GONG, EXPLOS
	}

	public static final EnumMap<Fog, String> FogSTR = new EnumMap<Fog, String>(Fog.class);
	static {
		FogSTR.put(Fog.UNKNOWN, "yes");
		FogSTR.put(Fog.HORN, "horn");
		FogSTR.put(Fog.SIREN, "siren");
		FogSTR.put(Fog.DIA, "diaphone");
		FogSTR.put(Fog.BELL, "bell");
		FogSTR.put(Fog.WHIS, "whistle");
		FogSTR.put(Fog.GONG, "gong");
		FogSTR.put(Fog.EXPLOS, "explosion");
	}

	private Fog fogSound = Fog.NONE;

	public Fog getFogSound() {
		return fogSound;
	}

	public void setFogSound(Fog sound) {
		fogSound = sound;
	}

	private String fogGroup = "";

	public String getFogGroup() {
		return fogGroup;
	}

	public void setFogGroup(String grp) {
		fogGroup = grp;
	}

	private String fogSequence = "";

	public String getFogSequence() {
		return fogSequence;
	}

	public void setFogSequence(String seq) {
		fogSequence = seq;
	}

	private String fogRange = "";

	public String getFogRange() {
		return fogRange;
	}

	public void setFogRange(String rng) {
		fogRange = validDecimal(rng);
	}

	private String fogPeriod = "";

	public String getFogPeriod() {
		return fogPeriod;
	}

	public void setFogPeriod(String per) {
		fogPeriod = validDecimal(per);
	}

	public enum Sts {
		UNKNOWN, PERM, OCC, REC, NIU, INT, RESV, TEMP, PRIV, MAND, DEST, EXT, ILLUM, HIST, PUB, SYNC, WATCH, UNWAT, DOUBT
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

	private Sts status = Sts.UNKNOWN;

	public Sts getStatus() {
		return status;
	}

	public void setStatus(Sts sts) {
		status = sts;
	}

	public enum Cns {
		UNKNOWN, BRICK, CONC, BOULD, HSURF, USURF, WOOD, METAL, GRP, PAINT
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
		CnsSTR.put(Cns.GRP, "grp");
		CnsSTR.put(Cns.PAINT, "painted");
	}

	private Cns construction = Cns.UNKNOWN;

	public Cns getConstr() {
		return construction;
	}

	public void setConstr(Cns cns) {
		construction = cns;
	}

	public enum Con {
		UNKNOWN, CONSP, NCONS, REFL
	}

	public static final EnumMap<Con, String> ConSTR = new EnumMap<Con, String>(Con.class);
	static {
		ConSTR.put(Con.CONSP, "conspicuous");
		ConSTR.put(Con.NCONS, "not_conspicuous");
		ConSTR.put(Con.REFL, "reflector");
	}

	private Con conspicuity = Con.UNKNOWN;

	public Con getConsp() {
		return conspicuity;
	}

	public void setConsp(Con con) {
		conspicuity = con;
	}

	private Con reflectivity = Con.UNKNOWN;

	public Con getRefl() {
		return reflectivity;
	}

	public void setRefl(Con con) {
		reflectivity = con;
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

	public String getHeight() {
		return height;
	}

	public void setHeight(String str) {
		height = validDecimal(str);
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
			if ((getCategory() != Cat.NONE) && (getShape() != Shp.UNKNOWN))
				tmp = true;
			break;
		case BCNISD:
		case BCNSAW:
		case BCNSPP:
		case BOYISD:
		case BOYSAW:
		case BOYSPP:
			if (getShape() != Shp.UNKNOWN)
				tmp = true;
			break;
		case FLTCAR:
		case FLTISD:
		case FLTLAT:
		case FLTSAW:
		case FLTSPP:
			if (getObjColour(0) != Col.UNKNOWN)
				tmp = true;
			break;
		case LITMAJ:
		case LITMIN:
		case LITFLT:
		case LITVES:
		case LITHSE:
		case MORFAC:
		case SISTAW:
		case SISTAT:
			tmp = true;
			break;
		case LNDMRK:
			if (getCategory() != Cat.NONE)
				tmp = true;
			break;
		}
		if (tmp) {
			dlg.panelMain.moreButton.setVisible(true);
			dlg.panelMain.saveButton.setEnabled(true);
			Ent ent = EntMAP.get(getObject());
			dlg.panelMain.topButton.setEnabled((ent == Ent.BUOY)
					|| (ent == Ent.BEACON) || (ent == Ent.FLOAT));
			dlg.panelMain.fogButton.setEnabled(true);
			dlg.panelMain.radButton.setEnabled(true);
			dlg.panelMain.litButton.setEnabled(true);
			return true;
		} else {
			dlg.panelMain.moreButton.setVisible(false);
			dlg.panelMain.moreButton.setText("v v v");
			dlg.panelMain.topButton.setEnabled(false);
			dlg.panelMain.fogButton.setEnabled(false);
			dlg.panelMain.radButton.setEnabled(false);
			dlg.panelMain.litButton.setEnabled(false);
			dlg.manager.showVisualMessage("Seamark not recognised");
			return false;
		}
	}

	public void clearSign() {
		setObject(Obj.UNKNOWN);
		setCategory(Cat.NONE);
		setShape(Shp.UNKNOWN);
		setColour(Ent.BODY, Col.UNKNOWN);
		setPattern(Ent.BODY, Pat.NONE);
		setTopmark(Top.NONE);
		setColour(Ent.TOPMARK, Col.UNKNOWN);
		setPattern(Ent.TOPMARK, Pat.NONE);
		setFogSound(Fog.NONE);
		setRadar(Rtb.NONE);
		clrLight();
		dlg.panelMain.moreButton.setVisible(false);
		dlg.panelMain.saveButton.setEnabled(false);
		dlg.panelMain.topButton.setEnabled(false);
		dlg.panelMain.fogButton.setEnabled(false);
		dlg.panelMain.radButton.setEnabled(false);
		dlg.panelMain.litButton.setEnabled(false);
		dlg.panelMain.panelMore.syncPanel();
		dlg.panelMain.panelMore.setVisible(false);
		paintSign();
	}

	public String validDecimal(String str) {
		str = str.trim().replace(',', '.');
		if ((!str.isEmpty()) && (!str.matches("^[+-]??\\d+(\\.\\d+)??$"))) {
			dlg.manager.showVisualMessage("Not a valid decimal string");
			return "";
		} else {
			dlg.manager.showVisualMessage("");
			return str;
		}
	}

	private boolean paintlock = false;

	public void parseMark(Node node) {
		paintlock = true;
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

		setObject(Obj.UNKNOWN);
		for (Obj obj : ObjSTR.keySet()) {
			if (ObjSTR.get(obj).equals(str)) {
				setObject(obj);
			}
		}

		if (str.equals("")) {
			dlg.manager.showVisualMessage("No seamark");
		}
		if (getObject() == Obj.UNKNOWN) {
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
		if (getName().isEmpty()) {
			if (keys.containsKey("name")) {
				str = keys.get("name");
				setName(str);
			}
		}

		for (Obj obj : ObjSTR.keySet()) {
			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":category")) {
				str = keys.get("seamark:" + ObjSTR.get(obj) + ":category");
				setCategory(Cat.NONE);
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
				setShape(Shp.UNKNOWN);
				for (Shp shp : ShpSTR.keySet()) {
					if (ShpSTR.get(shp).equals(str)) {
						setShape(shp);
					}
				}
			}
		}
		if (getShape() == Shp.UNKNOWN) {
			if (EntMAP.get(getObject()) == Ent.BUOY)
				setShape(Shp.BUOY);
			if (EntMAP.get(getObject()) == Ent.BEACON)
				setShape(Shp.BEACON);
			if (EntMAP.get(getObject()) == Ent.FLOAT)
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
				setObjPattern(Pat.NONE);
				for (Pat pat : PatSTR.keySet()) {
					if (PatSTR.get(pat).equals(str)) {
						setObjPattern(pat);
					}
				}
			}

			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":height")) {
				setHeight(keys.get("seamark:" + ObjSTR.get(obj) + ":height"));
			}
			if (keys.containsKey("seamark:" + ObjSTR.get(obj) + ":elevation")) {
				setElevation(keys.get("seamark:" + ObjSTR.get(obj) + ":elevation"));
			}
		}

		if ((getObject() == Obj.LNDMRK) && (getCategory() == Cat.NONE)) {
			setObject(Obj.LITHSE);
		}

		if (getObject() == Obj.LITFLT) {
			switch (getObjColour(0)) {
			case RED:
				if ((getObjColour(1) == Col.WHITE) && (getObjColour(2) == Col.UNKNOWN)) {
					setObject(Obj.FLTSAW);
					setCategory(Cat.NONE);
				} else if (getObjColour(1) == Col.UNKNOWN) {
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
					setCategory(Cat.NONE);
				}
				break;
			case GREEN:
				if (getObjColour(1) == Col.UNKNOWN) {
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
					setCategory(Cat.NONE);
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
					setCategory(Cat.NONE);
				}
				break;
			case BLACK:
				if (getObjColour(1) == Col.RED) {
					setObject(Obj.FLTISD);
					setCategory(Cat.NONE);
				} else if (getObjColour(1) == Col.YELLOW) {
					setObject(Obj.FLTCAR);
					if (getObjColour(2) == Col.BLACK) {
						setCategory(Cat.CAM_EAST);
					} else {
						setCategory(Cat.CAM_NORTH);
					}
				} else {
					setObject(Obj.FLTSPP);
					setCategory(Cat.NONE);
				}
				break;
			default:
				setCategory(Cat.NONE);
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
							setRegion(Reg.A);
					}
					if (getObjColour(0) == Col.GREEN)
						setRegion(Reg.B);
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
						setRegion(Reg.A);
					if (getObjColour(0) == Col.RED) {
						if (getObjColour(3) == Col.GREEN)
							setRegion(Reg.C);
						else
							setRegion(Reg.B);
					}
					break;
				}
			}
		}

		if (keys.containsKey("seamark:topmark:shape")) {
			str = keys.get("seamark:topmark:shape");
			setTopmark(Top.NONE);
			for (Top top : TopSTR.keySet()) {
				if (TopSTR.get(top).equals(str)) {
					setTopmark(top);
				}
			}
		}
		if (keys.containsKey("seamark:topmark:colour")) {
			str = keys.get("seamark:topmark:colour");
			setTopColour(Col.UNKNOWN);
			for (Col col : ColSTR.keySet()) {
				if (ColSTR.get(col).equals(str)) {
					setTopColour(col);
				}
			}
		}
		if (keys.containsKey("seamark:topmark:colour_pattern")) {
			str = keys.get("seamark:topmark:colour_pattern");
			setTopPattern(Pat.NONE);
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
				str = keys.get("seamark:light" + secStr + ":colour");
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
			setFogSound(Fog.UNKNOWN);
		}
		if (keys.containsKey("seamark:fog_signal:category")) {
			str = keys.get("seamark:fog_signal:category");
			setFogSound(Fog.NONE);
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
			setRadar(Rtb.NONE);
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
		if (keys.containsKey("seamark:source")) {
			setSource(keys.get("seamark:source"));
		}
		if (keys.containsKey("seamark:height")) {
			setHeight(keys.get("seamark:height"));
		}
		if (keys.containsKey("seamark:elevation")) {
			setElevation(keys.get("seamark:elevation"));
		}
		if (keys.containsKey("seamark:status")) {
			str = keys.get("seamark:status");
			setStatus(Sts.UNKNOWN);
			for (Sts sts : StsSTR.keySet()) {
				if (StsSTR.get(sts).equals(str)) {
					setStatus(sts);
				}
			}
		}
		if (keys.containsKey("seamark:construction")) {
			str = keys.get("seamark:construction");
			setConstr(Cns.UNKNOWN);
			for (Cns cns : CnsSTR.keySet()) {
				if (CnsSTR.get(cns).equals(str)) {
					setConstr(cns);
				}
			}
		}
		if (keys.containsKey("seamark:conspicuity")) {
			str = keys.get("seamark:conspicuity");
			setConsp(Con.UNKNOWN);
			for (Con con : ConSTR.keySet()) {
				if (ConSTR.get(con).equals(str)) {
					setConsp(con);
				}
			}
		}
		if (keys.containsKey("seamark:reflectivity")) {
			str = keys.get("seamark:reflectivity");
			setRefl(Con.UNKNOWN);
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

		paintlock = false;
		paintSign();
	}

	public void paintSign() {

		if (paintlock)
			return;

		dlg.panelMain.shapeIcon.setIcon(null);
		dlg.panelMain.litLabel.setText("");
		dlg.panelMain.colLabel.setText("");
		dlg.panelMain.radarLabel.setText("");
		dlg.panelMain.fogLabel.setText("");
		dlg.panelMain.topIcon.setIcon(null);
		dlg.panelMain.fogIcon.setIcon(null);
		dlg.panelMain.radarIcon.setIcon(null);
		dlg.panelMain.lightIcon.setIcon(null);

		String colStr;
		String lblStr;
		String imgStr = "/images/";
		if (getShape() != Shp.UNKNOWN) {
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
			case CONE:
				imgStr += "Cone";
				break;
			case SPHERE:
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
			if (getShape() == Shp.PERCH) {
				if (getCategory() == Cat.LAM_PORT) {
					colStr = "/images/Perch_Port";
				} else {
					colStr = "/images/Perch_Starboard";
				}
			}
			if (!imgStr.equals("/images/")) {
				colStr += ".png";
				if (getClass().getResource(colStr) == null) {
					System.out.println("Missing image: " + colStr);
					imgStr += ".png";
					if (getClass().getResource(imgStr) == null) {
						System.out.println("Missing image: " + imgStr);
					} else {
						dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass()
								.getResource(imgStr)));
						dlg.panelMain.colLabel.setText(lblStr);
					}
				} else {
					dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource(colStr)));
				}
			} else {
				dlg.panelMain.shapeIcon.setIcon(null);
			}
		} else if (getObject() != Obj.UNKNOWN) {
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
					imgStr += "Monument";
					break;
				case LMK_RADM:
					imgStr += "RadioMast";
					break;
				case LMK_TOWR:
					imgStr += "LandTower";
					break;
				case LMK_WNDM:
					imgStr += "Wind_Motor";
					break;
				case LMK_WTRT:
					imgStr += "WaterTower";
					break;
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
			}
			if (!imgStr.equals("/images/")) {
				imgStr += ".png";
				if (getClass().getResource(imgStr) == null) {
					System.out.println("Missing image: " + imgStr);
				} else {
					dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource(imgStr)));
				}
			} else {
				dlg.panelMain.shapeIcon.setIcon(null);
			}
		}

		if (getTopmark() != Top.NONE) {
			imgStr = "/images/Top_";
			switch (getTopmark()) {
			case CAN:
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
			case CONE:
			case SPHERE:
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
				System.out.println("Missing image: " + colStr);
				imgStr += ".png";
				if (getClass().getResource(imgStr) == null) {
					System.out.println("Missing image: " + imgStr);
					return;
				} else {
					dlg.panelMain.topIcon.setIcon(new ImageIcon(getClass().getResource(imgStr)));
				}
			} else {
				dlg.panelMain.topIcon.setIcon(new ImageIcon(getClass().getResource(colStr)));
			}
		} else {
			dlg.panelMain.topIcon.setIcon(null);
		}

		if (getFogSound() != Fog.NONE) {
			dlg.panelMain.fogIcon.setIcon(new ImageIcon(getClass().getResource("/images/Fog_Signal.png")));
			String str = "";
			if (getFogSound() != Fog.UNKNOWN)
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
			dlg.panelMain.fogLabel.setText(str);
		}

		if (RaType != Rtb.NONE) {
			if (getRadar() == Rtb.REFLECTOR) {
				dlg.panelMain.radarIcon.setIcon(new ImageIcon(getClass().getResource("/images/Radar_Reflector_355.png")));
			} else {
				dlg.panelMain.radarIcon.setIcon(new ImageIcon(getClass().getResource("/images/Radar_Station.png")));
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
				dlg.panelMain.radarLabel.setText(str);
			}
		}

		if (getLightAtt(Att.COL, 0) != Col.UNKNOWN) {
			if (sectors.size() == 1) {
				switch ((Col) getLightAtt(Att.COL, 0)) {
				case RED:
					dlg.panelMain.lightIcon.setIcon(new ImageIcon(getClass().getResource("/images/Light_Red_120.png")));
					break;
				case GREEN:
					dlg.panelMain.lightIcon.setIcon(new ImageIcon(getClass().getResource("/images/Light_Green_120.png")));
					break;
				case WHITE:
				case YELLOW:
					dlg.panelMain.lightIcon.setIcon(new ImageIcon(getClass().getResource("/images/Light_White_120.png")));
					break;
				default:
					dlg.panelMain.lightIcon.setIcon(new ImageIcon(getClass().getResource("/images/Light_Magenta_120.png")));
				}
			}
			String c = (String) dlg.mark.getLightAtt(Att.CHR, 0);
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
			case RED:
				c += " R";
				break;
			case GREEN:
				c += " G";
				break;
			case AMBER:
				c += " Am";
				break;
			case ORANGE:
				c += " Or";
				break;
			case BLUE:
				c += " Bu";
				break;
			case VIOLET:
				c += " Vi";
				break;
			}
			tmp = (String) getLightAtt(Att.PER, 0);
			if (!tmp.isEmpty())
				c += " " + tmp + "s";
			dlg.panelMain.litLabel.setText(c);
		}

		paintlock = false;
	}

	public void saveSign(Node node) {

		if (getObject() != Obj.UNKNOWN) {

			Main.pref.put("smedplugin.IALA", getRegion() == Reg.C ? "C" : (getRegion() == Reg.B ? "B" : "A"));

			for (String str : node.getKeys().keySet()) {
				if (str.trim().matches("^seamark:\\S+"))
					Main.main.undoRedo.add(new ChangePropertyCommand(node, str, null));
			}

			if (!getName().isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:name", getName()));

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

				if ((getObjColour(0) != Col.UNKNOWN) && getShape() != Shp.PERCH) {
					String str = ColSTR.get(getObjColour(0));
					for (int i = 1; bodyColour.size() > i; i++) {
						str += (";" + ColSTR.get(getObjColour(i)));
					}
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour", str));
				}

				if (getObjPattern() != Pat.NONE) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour_pattern", PatSTR.get(getObjPattern())));
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
				if (!getHeight().isEmpty()) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":height", getHeight()));
				}
				if (!getElevation().isEmpty()) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":elevation", getElevation()));
				}
			}
			if (getTopmark() != Top.NONE) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:topmark:shape", TopSTR.get(getTopmark())));
				if (getTopPattern() != Pat.NONE)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:topmark:colour_pattern", PatSTR.get(getTopPattern())));
				if (getTopColour(0) != Col.UNKNOWN) {
					String str = ColSTR.get(getTopColour(0));
					for (int i = 1; topmarkColour.size() > i; i++) {
						str += (";" + ColSTR.get(getTopColour(i)));
					}
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:topmark:colour", str));
				}
			}

			for (int i = 0; i < sectors.size(); i++) {
				String secStr = (i == 0) ? "" : (":" + Integer.toString(i));
				if (sectors.get(i)[0] != Col.UNKNOWN)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":colour", ColSTR.get(sectors.get(i)[0])));
				if (!((String) sectors.get(i)[1]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":character", (String) sectors.get(i)[1]));
				if (!((String) sectors.get(i)[2]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":group", (String) sectors.get(i)[2]));
				if (!((String) sectors.get(i)[3]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":sequence", (String) sectors.get(i)[3]));
				if (!((String) sectors.get(i)[4]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":period", (String) sectors.get(i)[4]));
				if (sectors.get(i)[5] != Lit.UNKNOWN)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":category", LitSTR.get(sectors.get(i)[5])));
				if (!((String) sectors.get(i)[6]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":sector_start", (String) sectors.get(i)[6]));
				if (!((String) sectors.get(i)[7]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":sector_end", (String) sectors.get(i)[7]));
				if (!((String) sectors.get(i)[8]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":radius", (String) sectors.get(i)[8]));
				if (!((String) sectors.get(i)[9]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":height", (String) sectors.get(i)[9]));
				if (!((String) sectors.get(i)[10]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":range", (String) sectors.get(i)[10]));
				if (sectors.get(i)[11] != Vis.UNKNOWN)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":visibility", VisSTR.get(sectors.get(i)[11])));
				if (sectors.get(i)[12] != Exh.UNKNOWN)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":exhibition", ExhSTR.get(sectors.get(i)[12])));
				if (!((String) sectors.get(i)[13]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":orientation", (String) sectors.get(i)[13]));
				if (!((String) sectors.get(i)[14]).isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light" + secStr + ":multiple", (String) sectors.get(i)[14]));
			}

			if (getFogSound() != Fog.NONE) {
				if (getFogSound() == Fog.UNKNOWN)
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

			if (RaType != Rtb.NONE) {
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
			if (getStatus() != Sts.UNKNOWN) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:status", StsSTR.get(getStatus())));
			}
			if (getConstr() != Cns.UNKNOWN) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:construction", CnsSTR.get(getConstr())));
			}
			if (getConsp() != Con.UNKNOWN) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:conspicuity", ConSTR.get(getConsp())));
			}
			if (getRefl() != Con.UNKNOWN) {
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
