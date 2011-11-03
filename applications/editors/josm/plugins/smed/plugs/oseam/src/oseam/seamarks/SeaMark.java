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

	public Light light = new Light(dlg);

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

	public enum Reg {
		A, B, C
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

	public void setName(String nam) {
		name = nam.trim();
	}

	public enum Obj {
		UNKNOWN, BCNCAR, BCNISD, BCNLAT, BCNSAW, BCNSPP, BOYCAR, BOYISD, BOYLAT, BOYSAW, BOYSPP, FLTCAR, FLTISD, FLTLAT, FLTSAW, FLTSPP, LITMAJ, LITMIN, LITFLT, LITVES, LNDMRK, MORFAC, SISTAW, SISTAT
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
		EntMAP.put(Obj.LITVES, Ent.LIGHT);
		EntMAP.put(Obj.LNDMRK, Ent.LIGHT);
		EntMAP.put(Obj.MORFAC, Ent.MOORING);
		EntMAP.put(Obj.SISTAW, Ent.STATION);
		EntMAP.put(Obj.SISTAT, Ent.STATION);
	}

	public enum Grp {
		NUL, LAT, CAR, SAW, ISD, SPP, FLT, LIT, SIS
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
		GrpMAP.put(Obj.LITMAJ, Grp.LIT);
		GrpMAP.put(Obj.LITMIN, Grp.LIT);
		GrpMAP.put(Obj.LITFLT, Grp.FLT);
		GrpMAP.put(Obj.FLTCAR, Grp.CAR);
		GrpMAP.put(Obj.FLTLAT, Grp.LAT);
		GrpMAP.put(Obj.FLTSAW, Grp.SAW);
		GrpMAP.put(Obj.FLTISD, Grp.ISD);
		GrpMAP.put(Obj.FLTSPP, Grp.SPP);
		GrpMAP.put(Obj.LITVES, Grp.LIT);
		GrpMAP.put(Obj.LNDMRK, Grp.LIT);
		GrpMAP.put(Obj.MORFAC, Grp.SPP);
		GrpMAP.put(Obj.SISTAW, Grp.SIS);
		GrpMAP.put(Obj.SISTAT, Grp.SIS);
	}

	public enum Cat {
		NONE, LAM_PORT, LAM_STBD, LAM_PPORT, LAM_PSTBD, CAM_NORTH, CAM_EAST, CAM_SOUTH, CAM_WEST, ACH_URST, ACH_DEEP, ACH_TANK, ACH_EXPL, ACH_QUAR, ACH_SPLN, ACH_SCAN, ACH_SCMO, ACH_T24H, ACH_TLIM, SPM_UNKN, SPM_WARN, SPM_CHBF, SPM_YCHT, SPM_CABL, SPM_OFAL, SPM_ODAS, SPM_RECN, SPM_MOOR, SPM_LNBY, SPM_LDNG, SPM_NOTC, SPM_TSS, SPM_FOUL, SPM_DIVE, SPM_FRRY, SPM_ANCH, MOR_DLPN, MOR_DDPN, MOR_BLRD, MOR_WALL, MOR_POST, MOR_CHWR, MOR_BUOY, SIS_PTCL, SIS_PTED, SIS_IPT, SIS_BRTH, SIS_DOCK, SIS_LOCK, SIS_FBAR, SIS_BRDG, SIS_DRDG, SIS_TRFC, SIS_DNGR, SIS_OBST, SIS_CABL, SIS_MILY, SIS_DSTR, SIS_WTHR, SIS_STRM, SIS_ICE, SIS_TIME, SIS_TIDE, SIS_TSTM, SIS_TGAG, SIS_TSCL, SIS_DIVE, SIS_LGAG, LIT_DIRF, LIT_LEDG
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
		ColMAP.put(Col.WHITE, Color.WHITE);
		ColMAP.put(Col.RED, Color.RED);
		ColMAP.put(Col.ORANGE, Color.ORANGE);
		ColMAP.put(Col.AMBER, new Color(0xffbf00f));
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

	private ArrayList<Col> bodyColour = new ArrayList<Col>();
	private ArrayList<Col> topColour = new ArrayList<Col>();
	private ArrayList<Col> lightColour = new ArrayList<Col>();

	public Col getColour(Ent ent, int i) {
		switch (ent) {
		case BODY:
		case BUOY:
		case BEACON:
		case FLOAT:
			if (i < bodyColour.size())
				return bodyColour.get(i);
			break;
		case TOPMARK:
			if (i < topColour.size())
				return topColour.get(i);
			break;
		case LIGHT:
			if (i < lightColour.size())
				return lightColour.get(i);
			break;
		}
		return Col.UNKNOWN;
	}

	public void setColour(Ent ent, Col col) {
		switch (ent) {
		case BODY:
		case BUOY:
		case BEACON:
		case FLOAT:
			bodyColour.clear();
			bodyColour.add(col);
			break;
		case TOPMARK:
			topColour.clear();
			topColour.add(col);
			break;
		case LIGHT:
			lightColour.clear();
			lightColour.add(col);
			break;
		}
	}

	public void setColour(Ent ent, int i, Col col) {
		switch (ent) {
		case BODY:
		case BUOY:
		case BEACON:
		case FLOAT:
			if (bodyColour.size() > i)
				bodyColour.set(i, col);
			break;
		case TOPMARK:
			if (topColour.size() > i)
				topColour.set(i, col);
			break;
		case LIGHT:
			if (lightColour.size() > i)
				lightColour.set(i, col);
			break;
		}
	}

	public void addColour(Ent ent, int i, Col col) {
		switch (ent) {
		case BODY:
		case BUOY:
		case BEACON:
		case FLOAT:
			if (bodyColour.size() >= i)
				bodyColour.add(i, col);
			break;
		case TOPMARK:
			if (topColour.size() >= i)
				topColour.add(i, col);
			break;
		case LIGHT:
			if (lightColour.size() >= i)
				lightColour.add(i, col);
			break;
		}
	}

	public void addColour(Ent ent, Col col) {
		switch (ent) {
		case BODY:
		case BUOY:
		case BEACON:
		case FLOAT:
			bodyColour.add(col);
			break;
		case TOPMARK:
			topColour.add(col);
			break;
		case LIGHT:
			lightColour.add(col);
			break;
		}
	}

	public void subColour(Ent ent, int i) {
		switch (ent) {
		case BODY:
		case BUOY:
		case BEACON:
		case FLOAT:
			if (bodyColour.size() > i)
				bodyColour.remove(i);
			break;
		case TOPMARK:
			if (topColour.size() > i)
				topColour.remove(i);
			break;
		case LIGHT:
			if (lightColour.size() > i)
				lightColour.remove(i);
			break;
		}
	}

	public enum Chr {
		UNKNOWN, FIXED, FLASH, LONGFLASH, QUICK, VERYQUICK, ULTRAQUICK, ISOPHASED, OCCULTING, MORSE, ALTERNATING, INTERRUPTEDQUICK, INTERRUPTEDVERYQUICK, INTERRUPTEDULTRAQUICK
	}

	public static final Map<EnumSet<Chr>, String> ChrMAP = new HashMap<EnumSet<Chr>, String>();
	static {
		ChrMAP.put(EnumSet.of(Chr.UNKNOWN), "");
		ChrMAP.put(EnumSet.of(Chr.FIXED), "F");
		ChrMAP.put(EnumSet.of(Chr.FLASH), "Fl");
		ChrMAP.put(EnumSet.of(Chr.LONGFLASH), "LFl");
		ChrMAP.put(EnumSet.of(Chr.QUICK), "Q");
		ChrMAP.put(EnumSet.of(Chr.VERYQUICK), "VQ");
		ChrMAP.put(EnumSet.of(Chr.ULTRAQUICK), "UQ");
		ChrMAP.put(EnumSet.of(Chr.INTERRUPTEDQUICK), "IQ");
		ChrMAP.put(EnumSet.of(Chr.INTERRUPTEDVERYQUICK), "IVQ");
		ChrMAP.put(EnumSet.of(Chr.INTERRUPTEDULTRAQUICK), "IUQ");
		ChrMAP.put(EnumSet.of(Chr.ISOPHASED), "Iso");
		ChrMAP.put(EnumSet.of(Chr.OCCULTING), "Oc");
		ChrMAP.put(EnumSet.of(Chr.MORSE), "Mo");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING), "Al");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.FIXED), "Al.F");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.FLASH), "Al.Fl");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.FIXED, Chr.FLASH), "F.Al.Fl");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.LONGFLASH), "Al.LFl");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.ISOPHASED), "Al.Iso");
		ChrMAP.put(EnumSet.of(Chr.ALTERNATING, Chr.OCCULTING), "Al.Oc");
		ChrMAP.put(EnumSet.of(Chr.FIXED, Chr.FLASH), "FFl");
		ChrMAP.put(EnumSet.of(Chr.FIXED, Chr.LONGFLASH), "FLFl");
		ChrMAP.put(EnumSet.of(Chr.OCCULTING, Chr.FLASH), "OcFl");
		ChrMAP.put(EnumSet.of(Chr.FLASH, Chr.LONGFLASH), "FlLFl");
		ChrMAP.put(EnumSet.of(Chr.QUICK, Chr.LONGFLASH), "Q+LFl");
		ChrMAP.put(EnumSet.of(Chr.VERYQUICK, Chr.LONGFLASH), "VQ+LFl");
		ChrMAP.put(EnumSet.of(Chr.ULTRAQUICK, Chr.LONGFLASH), "UQ+LFl");
	}

	private ArrayList<Chr> lightCharacter = new ArrayList<Chr>();

	public Chr getLightChar(int i) {
		if (i < lightCharacter.size())
			return lightCharacter.get(i);
		else
			return Chr.UNKNOWN;
	}

	public void setLightChar(Chr chr) {
		lightCharacter.clear();
		lightCharacter.add(chr);
	}

	public void setLightChar(int i, Chr chr) {
		if (lightCharacter.size() > i)
			lightCharacter.set(i, chr);
	}

	public void addLightChar(int i, Chr chr) {
		if (lightCharacter.size() >= i)
			lightCharacter.add(i, chr);
	}

	public void addLightChar(Chr chr) {
		lightCharacter.add(chr);
	}

	public void subLightChar(int i) {
		if (lightCharacter.size() > i)
			lightCharacter.remove(i);
	}

	private ArrayList<String> lightGroup = new ArrayList<String>();

	public String getLightGroup(int i) {
		if (i < lightGroup.size())
			return lightGroup.get(i);
		else
			return "";
	}

	public void setLightGroup(String str) {
		lightGroup.clear();
		lightGroup.add(str);
	}

	public void setLightGroup(int i, String str) {
		if (lightGroup.size() > i)
			lightGroup.set(i, str);
	}

	public void addLightGroup(int i, String str) {
		if (lightGroup.size() >= i)
			lightGroup.add(i, str);
	}

	public void addLightGroup(String str) {
		lightGroup.add(str);
	}

	public void subLightGroup(int i) {
		if (lightGroup.size() > i)
			lightGroup.remove(i);
	}

	private ArrayList<String> lightSequence = new ArrayList<String>();

	public String getLightSeq(int i) {
		if (i < lightSequence.size())
			return lightSequence.get(i);
		else
			return "";
	}

	public void setLightSeq(String str) {
		lightSequence.clear();
		lightSequence.add(str);
	}

	public void setLightSeq(int i, String str) {
		if (lightSequence.size() > i)
			lightSequence.set(i, str);
	}

	public void addLightSeq(int i, String str) {
		if (lightSequence.size() >= i)
			lightSequence.add(i, str);
	}

	public void addLightSeq(String str) {
		lightSequence.add(str);
	}

	public void subLightSeq(int i) {
		if (lightSequence.size() > i)
			lightSequence.remove(i);
	}

	private ArrayList<String> lightPeriod = new ArrayList<String>();

	public String getLightPeriod(int i) {
		if (i < lightPeriod.size())
			return lightPeriod.get(i);
		else
			return "";
	}

	public void setLightPeriod(String str) {
		lightPeriod.clear();
		lightGroup.add(validDecimal(str));
	}

	public void setLightPeriod(int i, String str) {
		if (lightPeriod.size() > i)
			lightPeriod.set(i, validDecimal(str));
	}

	public void addLightPeriod(int i, String str) {
		if (lightPeriod.size() >= i)
			lightPeriod.add(i, validDecimal(str));
	}

	public void addLightPeriod(String str) {
		lightPeriod.add(validDecimal(str));
	}

	public void subLightPeriod(int i) {
		if (lightPeriod.size() > i)
			lightPeriod.remove(i);
	}

	private ArrayList<String> lightHeight = new ArrayList<String>();

	public String getLightHeight(int i) {
		if (i < lightHeight.size())
			return lightHeight.get(i);
		else
			return "";
	}

	public void setLightHeight(String str) {
		lightHeight.clear();
		lightHeight.add(validDecimal(str));
	}

	public void setLightHeight(int i, String str) {
		if (lightHeight.size() > i)
			lightHeight.set(i, validDecimal(str));
	}

	public void addLightHeight(int i, String str) {
		if (lightHeight.size() >= i)
			lightHeight.add(i, validDecimal(str));
	}

	public void addLightHeight(String str) {
		lightHeight.add(validDecimal(str));
	}

	public void subLightHeight(int i) {
		if (lightHeight.size() > i)
			lightHeight.remove(i);
	}

	private ArrayList<String> lightRange = new ArrayList<String>();

	public String getLightRange(int i) {
		if (i < lightRange.size())
			return lightRange.get(i);
		else
			return "";
	}

	public void setLightRange(String str) {
		lightRange.clear();
		lightRange.add(validDecimal(str));
	}

	public void setLightRange(int i, String str) {
		if (lightRange.size() > i)
			lightRange.set(i, validDecimal(str));
	}

	public void addLightRange(int i, String str) {
		if (lightRange.size() >= i)
			lightRange.add(i, validDecimal(str));
	}

	public void addLightRange(String str) {
		lightRange.add(validDecimal(str));
	}

	public void subLightRange(int i) {
		if (lightRange.size() > i)
			lightRange.remove(i);
	}

	private ArrayList<String> lightSector1 = new ArrayList<String>();

	public String getLightSector1(int i) {
		if (i < lightSector1.size())
			return lightSector1.get(i);
		else
			return "";
	}

	public void setLightSector1(String str) {
		lightSector1.clear();
		lightSector1.add(validDecimal(str));
	}

	public void setLightSector1(int i, String str) {
		if (lightSector1.size() > i)
			lightSector1.set(i, validDecimal(str));
	}

	public void addLightSector1(int i, String str) {
		if (lightSector1.size() >= i)
			lightSector1.add(i, validDecimal(str));
	}

	public void addLightSector1(String str) {
		lightSector1.add(validDecimal(str));
	}

	public void subLightSector1(int i) {
		if (lightSector1.size() > i)
			lightSector1.remove(i);
	}

	private ArrayList<String> lightSector2 = new ArrayList<String>();

	public String getLightSector2(int i) {
		if (i < lightSector2.size())
			return lightSector2.get(i);
		else
			return "";
	}

	public void setLightSector2(String str) {
		lightSector2.clear();
		lightSector2.add(validDecimal(str));
	}

	public void setLightSector2(int i, String str) {
		if (lightSector2.size() > i)
			lightSector2.set(i, validDecimal(str));
	}

	public void addLightSector2(int i, String str) {
		if (lightSector2.size() >= i)
			lightSector2.add(i, validDecimal(str));
	}

	public void addLightSector2(String str) {
		lightSector2.add(validDecimal(str));
	}

	public void subLightSector2(int i) {
		if (lightSector2.size() > i)
			lightSector2.remove(i);
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

	private Pat bodyPattern = Pat.NONE;
	private Pat topPattern = Pat.NONE;

	public Pat getPattern(Ent ent) {
		switch (ent) {
		case BODY:
		case BUOY:
		case BEACON:
		case FLOAT:
			return bodyPattern;
		case TOPMARK:
			return topPattern;
		}
		return Pat.NONE;
	}

	public void setPattern(Ent ent, Pat pat) {
		switch (ent) {
		case BODY:
		case BUOY:
		case BEACON:
		case FLOAT:
			bodyPattern = pat;
			break;
		case TOPMARK:
			topPattern = pat;
			break;
		}
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
		TopSTR.put(Top.WEST, "2 cones points together");
		TopSTR.put(Top.SPHERES2, "2 spheres");
		TopSTR.put(Top.BOARD, "board");
		TopSTR.put(Top.DIAMOND, "diamond");
		TopSTR.put(Top.CIRCLE, "circle");
		TopSTR.put(Top.TRIANGLE, "triangle, point up");
		TopSTR.put(Top.TRIANGLE_INV, "triangle, point down");
		TopSTR.put(Top.SQUARE, "square");
	}

	private Top topShape = Top.NONE;

	public boolean hasTopmark() {
		return (topShape != Top.NONE);
	}

	public Top getTopmark() {
		return topShape;
	}

	public void setTopmark(Top top) {
		topShape = top;
	}

	private boolean Radar = false;

	public boolean hasRadar() {
		return Radar;
	}

	public void setRadar(boolean radar) {
		Radar = radar;
	}

	private boolean Racon = false;

	public boolean hasRacon() {
		return Racon;
	}

	public void setRacon(boolean racon) {
		Racon = racon;
	}

	public enum Rtb {
		NONE, REFLECTOR, RACON, RAMARK, LEADING
	}

	private Rtb RaType = Rtb.NONE;

	public Rtb getRaType() {
		return RaType;
	}

	public void setRaType(Rtb type) {
		RaType = type;
	}

	private String RaconGroup = "";

	public String getRaconGroup() {
		return RaconGroup;
	}

	public void setRaconGroup(String grp) {
		RaconGroup = grp;
	}

	private String RaconSequence = "";

	public String getRaconSequence() {
		return RaconSequence;
	}

	public void setRaconSequence(String seq) {
		RaconSequence = seq;
	}

	private String RaconPeriod = "";

	public String getRaconPeriod() {
		return RaconPeriod;
	}

	public void setRaconPeriod(String per) {
		RaconPeriod = validDecimal(per);
	}

	private String RaconRange = "";

	public String getRaconRange() {
		return RaconRange;
	}

	public void setRaconRange(String rng) {
		RaconRange = validDecimal(rng);
	}

	private String RaconSector1 = "";

	public String getRaconSector1() {
		return RaconSector1;
	}

	public void setRaconSector1(String sec) {
		RaconSector1 = validDecimal(sec);
	}

	private String RaconSector2 = "";

	public String getRaconSector2() {
		return RaconSector2;
	}

	public void setRaconSector2(String sec) {
		RaconSector2 = validDecimal(sec);
	}

	private boolean FogSignal = false;

	public boolean hasFog() {
		return FogSignal;
	}

	public void setFog(boolean fog) {
		FogSignal = fog;
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

	private Fog FogSound = Fog.NONE;

	public Fog getFogSound() {
		return FogSound;
	}

	public void setFogSound(Fog sound) {
		FogSound = sound;
	}

	private String FogGroup = "";

	public String getFogGroup() {
		return FogGroup;
	}

	public void setFogGroup(String grp) {
		FogGroup = grp;
	}

	private String FogSequence = "";

	public String getFogSequence() {
		return FogSequence;
	}

	public void setFogSequence(String seq) {
		FogSequence = seq;
	}

	private String FogRange = "";

	public String getFogRange() {
		return FogRange;
	}

	public void setFogRange(String rng) {
		FogRange = validDecimal(rng);
	}

	private String FogPeriod = "";

	public String getFogPeriod() {
		return FogPeriod;
	}

	public void setFogPeriod(String per) {
		FogPeriod = validDecimal(per);
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

	public enum Vis {
		UNKNOWN, CONSP, NCONS, REFL
	}

	public static final EnumMap<Vis, String> VisSTR = new EnumMap<Vis, String>(Vis.class);
	static {
		VisSTR.put(Vis.CONSP, "conspicuous");
		VisSTR.put(Vis.NCONS, "not_conspicuous");
		VisSTR.put(Vis.REFL, "reflector");
	}

	private Vis visibility = Vis.UNKNOWN;

	public Vis getVis() {
		return visibility;
	}

	public void setVis(Vis vis) {
		visibility = vis;
	}

	private Vis reflectivity = Vis.UNKNOWN;

	public Vis getRvis() {
		return reflectivity;
	}

	public void setRvis(Vis vis) {
		reflectivity = vis;
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

	public boolean isValid() {
		switch (getObject()) {
		case BCNCAR:
		case BCNLAT:
		case BOYCAR:
		case BOYLAT:
			if ((getCategory() != Cat.NONE) && (getShape() != Shp.UNKNOWN))
				return true;
			break;
		case BCNISD:
		case BCNSAW:
		case BCNSPP:
		case BOYISD:
		case BOYSAW:
		case BOYSPP:
			if (getShape() != Shp.UNKNOWN)
				return true;
			break;
		case FLTCAR:
		case FLTISD:
		case FLTLAT:
		case FLTSAW:
		case FLTSPP:
			if (getColour(Ent.BODY, 0) != Col.UNKNOWN)
				return true;
			break;
		case LITMAJ:
		case LITMIN:
		case LITFLT:
		case LITVES:
		case LNDMRK:
		case MORFAC:
		case SISTAW:
		case SISTAT:
			return true;
		default:
			return false;
		}
		return false;
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
				setPattern(Ent.BODY, Pat.NONE);
				for (Pat pat : PatSTR.keySet()) {
					if (PatSTR.get(pat).equals(str)) {
						setPattern(Ent.BODY, pat);
					}
				}
			}
		}

		if (getObject() == Obj.LITFLT) {
			switch (getColour(Ent.BODY, 0)) {
			case RED:
				if ((getColour(Ent.BODY, 1) == Col.WHITE) && (getColour(Ent.BODY, 2) == Col.UNKNOWN)) {
					setObject(Obj.FLTSAW);
					setCategory(Cat.NONE);
				} else if (getColour(Ent.BODY, 1) == Col.UNKNOWN) {
					setObject(Obj.FLTLAT);
					if (getRegion() == Reg.B) {
						setCategory(Cat.LAM_STBD);
					} else {
						setCategory(Cat.LAM_PORT);
					}
				} else if ((getColour(Ent.BODY, 1) == Col.GREEN) && (getColour(Ent.BODY, 2) == Col.RED)) {
					setObject(Obj.FLTLAT);
					if (getRegion() == Reg.B) {
						setCategory(Cat.LAM_PSTBD);
					} else {
						setCategory(Cat.LAM_PPORT);
					}
				} else if ((getColour(Ent.BODY, 1) == Col.WHITE) && (getColour(Ent.BODY, 2) == Col.RED)) {
					setObject(Obj.FLTLAT);
					setCategory(Cat.LAM_PORT);
				} else {
					setObject(Obj.FLTSPP);
					setCategory(Cat.NONE);
				}
				break;
			case GREEN:
				if (getColour(Ent.BODY, 1) == Col.UNKNOWN) {
					setObject(Obj.FLTLAT);
					if (getRegion() == Reg.B) {
						setCategory(Cat.LAM_PORT);
					} else {
						setCategory(Cat.LAM_STBD);
					}
				} else if ((getColour(Ent.BODY, 1) == Col.RED) && (getColour(Ent.BODY, 2) == Col.GREEN)) {
					setObject(Obj.FLTLAT);
					if (getRegion() == Reg.B) {
						setCategory(Cat.LAM_PPORT);
					} else {
						setCategory(Cat.LAM_PSTBD);
					}
				} else if ((getColour(Ent.BODY, 1) == Col.WHITE) && (getColour(Ent.BODY, 2) == Col.GREEN)) {
					setObject(Obj.FLTLAT);
					setCategory(Cat.LAM_STBD);
				} else {
					setObject(Obj.FLTSPP);
					setCategory(Cat.NONE);
				}
				break;
			case YELLOW:
				if (getColour(Ent.BODY, 1) == Col.BLACK) {
					setObject(Obj.FLTCAR);
					if (getColour(Ent.BODY, 2) == Col.YELLOW) {
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
				if (getColour(Ent.BODY, 1) == Col.RED) {
					setObject(Obj.FLTISD);
					setCategory(Cat.NONE);
				} else if (getColour(Ent.BODY, 1) == Col.YELLOW) {
					if (getColour(Ent.BODY, 2) == Col.BLACK) {
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
				setObject(Obj.FLTSPP);
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
					if (getColour(Ent.BODY, 0) == Col.RED) {
						if (getColour(Ent.BODY, 1) == Col.WHITE)
							setRegion(Reg.C);
						else
							setRegion(Reg.A);
					}
					if (getColour(Ent.BODY, 0) == Col.GREEN)
						setRegion(Reg.B);
					break;
				case LAM_PPORT:
					if (getColour(Ent.BODY, 0) == Col.RED) {
						if (getColour(Ent.BODY, 3) == Col.GREEN)
							setRegion(Reg.C);
						else
							setRegion(Reg.A);
					}
					if (getColour(Ent.BODY, 0) == Col.GREEN)
						setRegion(Reg.B);
					break;
				case LAM_STBD:
					if (getColour(Ent.BODY, 0) == Col.GREEN) {
						if (getColour(Ent.BODY, 1) == Col.WHITE)
							setRegion(Reg.C);
						else
							setRegion(Reg.A);
					}
					if (getColour(Ent.BODY, 0) == Col.RED)
						setRegion(Reg.B);
					break;
				case LAM_PSTBD:
					if (getColour(Ent.BODY, 0) == Col.GREEN)
						setRegion(Reg.A);
					if (getColour(Ent.BODY, 0) == Col.RED) {
						if (getColour(Ent.BODY, 3) == Col.GREEN)
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
			setColour(Ent.TOPMARK, Col.UNKNOWN);
			for (Col col : ColSTR.keySet()) {
				if (ColSTR.get(col).equals(str)) {
					setColour(Ent.TOPMARK, col);
				}
			}
		}
		if (keys.containsKey("seamark:topmark:colour_pattern")) {
			str = keys.get("seamark:topmark:colour_pattern");
			setPattern(Ent.TOPMARK, Pat.NONE);
			for (Pat pat : PatSTR.keySet()) {
				if (PatSTR.get(pat).equals(str)) {
					setPattern(Ent.TOPMARK, pat);
				}
			}
		}

		if (keys.containsKey("seamark:fog_signal")) {
			str = keys.get("seamark:fog_signal");
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
		if (keys.containsKey("seamark:visibility")) {
			str = keys.get("seamark:visibility");
			setVis(Vis.UNKNOWN);
			for (Vis vis : VisSTR.keySet()) {
				if (VisSTR.get(vis).equals(str)) {
					setVis(vis);
				}
			}
		}
		if (keys.containsKey("seamark:reflectivity")) {
			str = keys.get("seamark:reflectivity");
			setRvis(Vis.UNKNOWN);
			for (Vis vis : VisSTR.keySet()) {
				if (VisSTR.get(vis).equals(str)) {
					setRvis(vis);
				}
			}
		}

		dlg.panelMain.syncPanel();

		paintlock = false;
		paintSign();
	}

	public void paintSign() {

		if (paintlock)
			return;

		dlg.panelMain.shapeIcon.setIcon(null);
		dlg.panelMain.lightIcon.setIcon(null);
		dlg.panelMain.topIcon.setIcon(null);
		dlg.panelMain.radarIcon.setIcon(null);
		dlg.panelMain.fogIcon.setIcon(null);
		dlg.panelMain.colLabel.setText("");

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
						dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource(imgStr)));
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
			for (Col col : topColour) {
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

		if (hasFog()) {
			dlg.panelMain.fogIcon.setIcon(new ImageIcon(getClass().getResource("/images/Fog_Signal.png")));
		}
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
					String str = CatSTR.get(category);
					if (str != null)
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":category", str));
					if ((getShape() != Shp.BUOY) && (getShape() != Shp.BEACON))
						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":shape", ShpSTR.get(getShape())));
				}

				if ((getColour(Ent.BODY, 0) != Col.UNKNOWN) && getShape() != Shp.PERCH) {
					String str = ColSTR.get(getColour(Ent.BODY, 0));
					for (int i = 1; bodyColour.size() > i; i++) {
						str += (";" + ColSTR.get(getColour(Ent.BODY, i)));
					}
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour", str));
				}

				if (getPattern(Ent.BODY) != Pat.NONE) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour_pattern", PatSTR
							.get(getPattern(Ent.BODY))));
				}

				if ((GrpMAP.get(object) == Grp.LAT) && (getShape() != Shp.PERCH) || (getObject() == Obj.FLTLAT)) {
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
			}
			if (hasTopmark()) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:topmark:shape", TopSTR.get(getTopmark())));
				if (getPattern(Ent.TOPMARK) != Pat.NONE)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:topmark:colour_pattern", PatSTR
							.get(getPattern(Ent.TOPMARK))));
				if (getColour(Ent.TOPMARK, 0) != Col.UNKNOWN) {
					String str = ColSTR.get(getColour(Ent.TOPMARK, 0));
					for (int i = 1; topColour.size() > i; i++) {
						str += (";" + ColSTR.get(getColour(Ent.TOPMARK, i)));
					}
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:topmark:colour", str));
				}
			}
			if (hasFog()) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal", FogSTR.get(getFogSound())));
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
			if (!getInfo().isEmpty()) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:information", getInfo()));
			}
			if (!getSource().isEmpty()) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:source", getSource()));
			}
			if (!getHeight().isEmpty()) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:height", getHeight()));
			}
			if (!getElevation().isEmpty()) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:elevation", getElevation()));
			}
			if (getStatus() != Sts.UNKNOWN) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:status", StsSTR.get(getStatus())));
			}
			if (getConstr() != Cns.UNKNOWN) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:construction", CnsSTR.get(getConstr())));
			}
			if (getVis() != Vis.UNKNOWN) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:visibility", VisSTR.get(getVis())));
			}
			if (getRvis() != Vis.UNKNOWN) {
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:reflectivity", VisSTR.get(getRvis())));
			}
		}
	}

}
