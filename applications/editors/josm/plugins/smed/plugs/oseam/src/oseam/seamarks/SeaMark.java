package oseam.seamarks;

import javax.swing.ImageIcon;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.command.ChangePropertyCommand;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.Light;

public class SeaMark {

	public OSeaMAction dlg = null;

	public SeaMark(OSeaMAction dia) {
		dlg = dia;
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
		name = nam;
	}

	public enum Obj {
		UNKNOWN, BCNCAR, BCNISD, BCNLAT, BCNSAW, BCNSPP, BOYCAR, BOYISD, BOYLAT, BOYSAW, BOYSPP, FLTCAR, FLTISD, FLTLAT, FLTSAW, FLTSPP, LITMAJ, LITMIN, LITFLT, LITVES, LNDMRK, MORFAC, SIGSTA
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
		ObjSTR.put(Obj.FLTSPP, "light_float");
		ObjSTR.put(Obj.LITMAJ, "light_major");
		ObjSTR.put(Obj.LITMIN, "light_minor");
		ObjSTR.put(Obj.LITFLT, "light_float");
		ObjSTR.put(Obj.LITVES, "light_vessel");
		ObjSTR.put(Obj.LNDMRK, "landmark");
		ObjSTR.put(Obj.MORFAC, "mooring");
		ObjSTR.put(Obj.SIGSTA, "signal_station_warning");
		ObjSTR.put(Obj.SIGSTA, "signal_station_traffic");
	}

	private Obj object = Obj.UNKNOWN;

	public Obj getObject() {
		return object;
	}

	public void setObject(Obj obj) {
		object = obj;
	}

	public enum Ent {
		BODY, BUOY, BEACON, FLOAT, TOPMARK, DAYMARK, LIGHT, MOORING, STATION
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
		EntMAP.put(Obj.LITVES, Ent.LIGHT);
		EntMAP.put(Obj.LNDMRK, Ent.LIGHT);
		EntMAP.put(Obj.MORFAC, Ent.MOORING);
		EntMAP.put(Obj.SIGSTA, Ent.STATION);
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
		GrpMAP.put(Obj.LITVES, Grp.LIT);
		GrpMAP.put(Obj.LNDMRK, Grp.LIT);
		GrpMAP.put(Obj.MORFAC, Grp.SPP);
		GrpMAP.put(Obj.SIGSTA, Grp.SIS);
	}

	public enum Cat {
		UNKNOWN, LAT_PORT, LAT_STBD, LAT_PREF_PORT, LAT_PREF_STBD, CARD_NORTH, CARD_EAST, CARD_SOUTH, CARD_WEST, LIGHT_HOUSE, LIGHT_MAJOR, LIGHT_MINOR, LIGHT_VESSEL, LIGHT_FLOAT, MOORING_BUOY, SIGNAL_STATION
	}

	public static final EnumMap<Cat, String> CatSTR = new EnumMap<Cat, String>(Cat.class);
	static {
		CatSTR.put(Cat.LAT_PORT, "port");
		CatSTR.put(Cat.LAT_STBD, "starboard");
		CatSTR.put(Cat.LAT_PREF_PORT, "preferred_channel_port");
		CatSTR.put(Cat.LAT_PREF_STBD, "preferred_channel_starboard");
		CatSTR.put(Cat.CARD_NORTH, "north");
		CatSTR.put(Cat.CARD_EAST, "east");
		CatSTR.put(Cat.CARD_SOUTH, "south");
		CatSTR.put(Cat.CARD_WEST, "west");
		CatSTR.put(Cat.MOORING_BUOY, "mooring_buoy");
	}

	private Cat category = Cat.UNKNOWN;

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
		UNKNOWN, WHITE, RED, ORANGE, AMBER, YELLOW, GREEN, BLUE, VIOLET, BLACK,
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
	}

	private ArrayList<Col> bodyColour = new ArrayList<Col>();
	private ArrayList<Col> topColour = new ArrayList<Col>();
	private ArrayList<Col> dayColour = new ArrayList<Col>();
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
		case DAYMARK:
			if (i < dayColour.size())
				return dayColour.get(i);
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
		case DAYMARK:
			dayColour.clear();
			dayColour.add(col);
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
			bodyColour.set(i, col);
			break;
		case TOPMARK:
			topColour.set(i, col);
			break;
		case DAYMARK:
			dayColour.set(i, col);
			break;
		case LIGHT:
			lightColour.set(i, col);
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
		case DAYMARK:
			dayColour.add(col);
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
			bodyColour.remove(i);
			break;
		case TOPMARK:
			topColour.remove(i);
			break;
		case DAYMARK:
			dayColour.remove(i);
			break;
		case LIGHT:
			lightColour.remove(i);
			break;
		}
	}

	public enum Pat {
		NONE, HORIZ, VERT, DIAG, SQUARE, BORDER
	}

	public static final EnumMap<Pat, String> PatMAP = new EnumMap<Pat, String>(Pat.class);
	static {
		PatMAP.put(Pat.HORIZ, "horizontal stripes");
		PatMAP.put(Pat.VERT, "vertical stripes");
		PatMAP.put(Pat.DIAG, "diagonal stripes");
		PatMAP.put(Pat.SQUARE, "squared");
		PatMAP.put(Pat.BORDER, "border stripe");
	}

	private Pat bodyPattern = Pat.NONE;
	private Pat topPattern = Pat.NONE;
	private Pat dayPattern = Pat.NONE;

	public Pat getPattern(Ent ent) {
		switch (ent) {
		case BODY:
		case BUOY:
		case BEACON:
		case FLOAT:
			return bodyPattern;
		case TOPMARK:
			return topPattern;
		case DAYMARK:
			return dayPattern;
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
		case DAYMARK:
			dayPattern = pat;
			break;
		}
	}

	public enum Top {
		NONE, CAN, CONE, SPHERE, X_SHAPE, NORTH, SOUTH, EAST, WEST, SPHERES2
	}

	public static final EnumMap<Top, String> TopSTR = new EnumMap<Top, String>(Top.class);
	static {
		TopSTR.put(Top.CAN, "cylinder");
		TopSTR.put(Top.CONE, "cylinder");
		TopSTR.put(Top.SPHERE, "SPHERE");
		TopSTR.put(Top.X_SHAPE, "X-SHAPE");
		TopSTR.put(Top.NORTH, "2 cones up");
		TopSTR.put(Top.SOUTH, "2 cones down");
		TopSTR.put(Top.EAST, "2 cones base together");
		TopSTR.put(Top.WEST, "2 cones points together");
		TopSTR.put(Top.SPHERES2, "2 spheres");
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

	public enum Day {
		NONE, BOARD, DIAMOND, CIRCLE, TRIANGLE, TRIANGLE_INV, SQUARE
	}

	public static final EnumMap<Day, String> DaySTR = new EnumMap<Day, String>(Day.class);
	static {
		DaySTR.put(Day.BOARD, "board");
		DaySTR.put(Day.DIAMOND, "diamond");
		DaySTR.put(Day.CIRCLE, "circle");
		DaySTR.put(Day.TRIANGLE, "triangle, point up");
		DaySTR.put(Day.TRIANGLE_INV, "triangle, point down");
		DaySTR.put(Day.SQUARE, "square");
	}

	private Day dayShape = Day.NONE;

	public boolean hasDaymark() {
		return (dayShape != Day.NONE);
	}

	public Day getDaymark() {
		return dayShape;
	}

	public void setDaymark(Day day) {
		dayShape = day;
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
		UNKNOWN, RACON, RAMARK, LEADING
	}

	private Rtb RaType = Rtb.UNKNOWN;

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

	public void setRaconGroup(String raconGroup) {
		RaconGroup = raconGroup;
	}

	private boolean FogSignal = false;

	public boolean hasFog() {
		return FogSignal;
	}

	public void setFog(boolean fog) {
		FogSignal = fog;
	}

	public enum Fog {
		UNKNOWN, HORN, SIREN, DIA, BELL, WHIS, GONG, EXPLOS
	}

	private Fog FogSound = Fog.UNKNOWN;

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

	public void setFogGroup(String group) {
		FogGroup = group;
	}

	private String FogPeriod = "";

	public String getFogPeriod() {
		return FogPeriod;
	}

	public void setFogPeriod(String period) {
		FogPeriod = period;
	}

	public Light light = new Light(dlg);

	private boolean paintlock = false;

	public void parseMark(Node node) {
		region = Main.pref.get("smedplugin.IALA").equals("C") ? Reg.C : (Main.pref.get("smedplugin.IALA").equals("B") ? Reg.B : Reg.A);
		dlg.panelMain.clearSelections();
		dlg.manager.showVisualMessage("");
		String str = "";

		Map<String, String> keys = node.getKeys();

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
				setCategory(Cat.UNKNOWN);
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
				for (Pat pat : PatMAP.keySet()) {
					if (PatMAP.get(pat).equals(str)) {
						setPattern(Ent.BODY, pat);
					}
				}
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
				case LAT_PORT:
					if (getColour(Ent.BODY, 0) == Col.RED) {
						if (getColour(Ent.BODY, 1) == Col.WHITE)
							setRegion(Reg.C);
						else
							setRegion(Reg.A);
					}
					if (getColour(Ent.BODY, 0) == Col.GREEN)
						setRegion(Reg.B);
					break;
				case LAT_PREF_PORT:
					if (getColour(Ent.BODY, 0) == Col.RED) {
						if (getColour(Ent.BODY, 3) == Col.GREEN)
							setRegion(Reg.C);
						else
							setRegion(Reg.A);
					}
					if (getColour(Ent.BODY, 0) == Col.GREEN)
						setRegion(Reg.B);
					break;
				case LAT_STBD:
					if (getColour(Ent.BODY, 0) == Col.GREEN) {
						if (getColour(Ent.BODY, 1) == Col.WHITE)
							setRegion(Reg.C);
						else
							setRegion(Reg.A);
					}
					if (getColour(Ent.BODY, 0) == Col.RED)
						setRegion(Reg.B);
					break;
				case LAT_PREF_STBD:
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

		switch (GrpMAP.get(getObject())) {
		case NUL:
			dlg.panelMain.clearSelections();
			break;
		case LAT:
			dlg.panelMain.chanButton.doClick();
			switch (getCategory()) {
			case LAT_PORT:
				dlg.panelMain.panelChan.portButton.doClick();
				break;
			case LAT_STBD:
				dlg.panelMain.panelChan.stbdButton.doClick();
				break;
			case LAT_PREF_PORT:
				dlg.panelMain.panelChan.prefPortButton.doClick();
				break;
			case LAT_PREF_STBD:
				dlg.panelMain.panelChan.prefStbdButton.doClick();
				break;
			}
			break;
		case SAW:
			dlg.panelMain.chanButton.doClick();
			dlg.panelMain.panelChan.safeWaterButton.doClick();
			break;
		case CAR:
			dlg.panelMain.hazButton.doClick();
			switch (getCategory()) {
			case CARD_NORTH:
				dlg.panelMain.panelHaz.northButton.doClick();
				break;
			case CARD_SOUTH:
				dlg.panelMain.panelHaz.southButton.doClick();
				break;
			case CARD_EAST:
				dlg.panelMain.panelHaz.eastButton.doClick();
				break;
			case CARD_WEST:
				dlg.panelMain.panelHaz.westButton.doClick();
				break;
			}
			break;
		case ISD:
			dlg.panelMain.hazButton.doClick();
			dlg.panelMain.panelHaz.isolButton.doClick();
			break;
		case SPP:
			dlg.panelMain.specButton.doClick();
			break;
		case LIT:
			dlg.panelMain.lightsButton.doClick();
			break;
		case FLT:
			switch (getColour(Ent.FLOAT, 0)) {
			case RED:
				dlg.panelMain.chanButton.doClick();
				if (getColour(Ent.FLOAT, 1) == Col.WHITE)
					if (getColour(Ent.FLOAT, 2) == Col.RED) {
						setRegion(Reg.C);
						dlg.panelMain.panelChan.portButton.doClick();
					} else {
						dlg.panelMain.panelChan.safeWaterButton.doClick();
					}
				else if (getColour(Ent.FLOAT, 1) == Col.GREEN) {
					if (getColour(Ent.FLOAT, 3) == Col.GREEN) {
						setRegion(Reg.C);
					}
					if (getRegion().equals("B")) {
						dlg.panelMain.panelChan.prefStbdButton.doClick();
					} else {
						dlg.panelMain.panelChan.prefPortButton.doClick();
					}
				} else {
					if (getRegion().equals("B"))
						dlg.panelMain.panelChan.stbdButton.doClick();
					else
						dlg.panelMain.panelChan.portButton.doClick();
				}
				break;
			case GREEN:
				dlg.panelMain.chanButton.doClick();
				if (getColour(Ent.FLOAT, 1) == Col.RED) {
					if (getRegion().equals("B")) {
						dlg.panelMain.panelChan.prefPortButton.doClick();
					} else {
						dlg.panelMain.panelChan.prefStbdButton.doClick();
					}
				} else if (getColour(Ent.FLOAT, 1) == Col.WHITE) {
					setRegion(Reg.C);
					dlg.panelMain.panelChan.stbdButton.doClick();
				} else {
					if (getRegion().equals("B")) {
						dlg.panelMain.panelChan.portButton.doClick();
					} else {
						dlg.panelMain.panelChan.stbdButton.doClick();
					}
				}
				break;
			case BLACK:
				dlg.panelMain.hazButton.doClick();
				switch (getColour(Ent.FLOAT, 1)) {
				case YELLOW:
					if (getColour(Ent.FLOAT, 2) == Col.BLACK)
						dlg.panelMain.panelHaz.eastButton.doClick();
					else
						dlg.panelMain.panelHaz.northButton.doClick();
					break;
				case RED:
					dlg.panelMain.panelHaz.isolButton.doClick();
					break;
				}
				break;
			case YELLOW:
				if (getColour(Ent.FLOAT, 1) == Col.BLACK) {
					dlg.panelMain.hazButton.doClick();
					if (getColour(Ent.FLOAT, 2) == Col.YELLOW)
						dlg.panelMain.panelHaz.westButton.doClick();
					else
						dlg.panelMain.panelHaz.southButton.doClick();
				} else {
					dlg.panelMain.specButton.doClick();
				}
				break;
			}
			break;
		}

	}

	public void paintSign() {

		if (paintlock)
			return;

		dlg.panelMain.shapeIcon.setIcon(null);
		dlg.panelMain.lightIcon.setIcon(null);
		dlg.panelMain.topIcon.setIcon(null);
		dlg.panelMain.radarIcon.setIcon(null);
		dlg.panelMain.fogIcon.setIcon(null);

		String imgStr = "/images/";
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
			imgStr += "Float_Major";
			break;
		case STAKE:
		case POLE:
		case POST:
			imgStr += "Stake";
			break;
		}
		if (!imgStr.equals("/images/")) {
			for (Col col : bodyColour) {
				switch (col) {
				case WHITE:
					imgStr += "_White";
					break;
				case RED:
					imgStr += "_Red";
					break;
				case ORANGE:
					imgStr += "_Orange";
					break;
				case AMBER:
					imgStr += "_Amber";
					break;
				case YELLOW:
					imgStr += "_Yellow";
					break;
				case GREEN:
					imgStr += "_Green";
					break;
				case BLUE:
					imgStr += "_Blue";
					break;
				case VIOLET:
					imgStr += "_Violet";
					break;
				case BLACK:
					imgStr += "_Black";
					break;
				}
			}
		}
		if (getShape() == Shp.PERCH) {
			if (getCategory() == Cat.LAT_PORT) {
				imgStr = "/images/Perch_Port";
			} else {
				imgStr = "/images/Perch_Starboard";
			}
		}
		if (!imgStr.equals("/images/")) {
			imgStr += ".png";
			if (getClass().getResource(imgStr) == null) {
				System.out.println("Missing image: " + imgStr);
				return;
			} else {
				dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource(imgStr)));
			}
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
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour_pattern", PatMAP
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
		}
	}

}
