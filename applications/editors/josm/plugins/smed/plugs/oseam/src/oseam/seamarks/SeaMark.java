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

	public static final EnumMap<Obj, String> ObjMAP = new EnumMap<Obj, String>(Obj.class);
	static {
		ObjMAP.put(Obj.UNKNOWN, "");
		ObjMAP.put(Obj.BCNCAR, "beacon_cardinal");
		ObjMAP.put(Obj.BCNISD, "beacon_isolated_danger");
		ObjMAP.put(Obj.BCNLAT, "beacon_lateral");
		ObjMAP.put(Obj.BCNSAW, "beacon_safe_water");
		ObjMAP.put(Obj.BCNSPP, "beacon_special_purpose");
		ObjMAP.put(Obj.BOYCAR, "buoy_cardinal");
		ObjMAP.put(Obj.BOYISD, "buoy_isolated_danger");
		ObjMAP.put(Obj.BOYLAT, "buoy_lateral");
		ObjMAP.put(Obj.BOYSAW, "buoy_safe_water");
		ObjMAP.put(Obj.BOYSPP, "buoy_special_purpose");
		ObjMAP.put(Obj.FLTCAR, "light_float");
		ObjMAP.put(Obj.FLTLAT, "light_float");
		ObjMAP.put(Obj.FLTSAW, "light_float");
		ObjMAP.put(Obj.FLTSPP, "light_float");
		ObjMAP.put(Obj.LITMAJ, "light_major");
		ObjMAP.put(Obj.LITMIN, "light_minor");
		ObjMAP.put(Obj.LITFLT, "light_float");
		ObjMAP.put(Obj.LITVES, "light_vessel");
		ObjMAP.put(Obj.LNDMRK, "landmark");
		ObjMAP.put(Obj.MORFAC, "mooring");
		ObjMAP.put(Obj.SIGSTA, "signal_station_warning");
		ObjMAP.put(Obj.SIGSTA, "signal_station_traffic");
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

	public static final EnumMap<Cat, String> CatMAP = new EnumMap<Cat, String>(Cat.class);
	static {
		CatMAP.put(Cat.UNKNOWN, "");
		CatMAP.put(Cat.LAT_PORT, "port");
		CatMAP.put(Cat.LAT_STBD, "starboard");
		CatMAP.put(Cat.LAT_PREF_PORT, "preferred_channel_port");
		CatMAP.put(Cat.LAT_PREF_STBD, "preferred_channel_starboard");
		CatMAP.put(Cat.CARD_NORTH, "north");
		CatMAP.put(Cat.CARD_EAST, "east");
		CatMAP.put(Cat.CARD_SOUTH, "south");
		CatMAP.put(Cat.CARD_WEST, "west");
		CatMAP.put(Cat.MOORING_BUOY, "mooring_buoy");
	}

	private Cat category = Cat.UNKNOWN;

	public Cat getCategory() {
		return category;
	}

	public void setCategory(Cat cat) {
		category = cat;
	}

	public enum Shp {
		UNKNOWN, PILLAR, SPAR, CAN, CONE, SPHERE, BARREL, FLOAT, SUPER, BEACON, TOWER, STAKE, PERCH
	}

	public static final EnumMap<Shp, String> ShpMAP = new EnumMap<Shp, String>(Shp.class);
	static {
		ShpMAP.put(Shp.UNKNOWN, "");
		ShpMAP.put(Shp.PILLAR, "pillar");
		ShpMAP.put(Shp.SPAR, "spar");
		ShpMAP.put(Shp.CAN, "can");
		ShpMAP.put(Shp.CONE, "conical");
		ShpMAP.put(Shp.SPHERE, "spherical");
		ShpMAP.put(Shp.BARREL, "barrel");
		ShpMAP.put(Shp.FLOAT, "float");
		ShpMAP.put(Shp.SUPER, "super-buoy");
		ShpMAP.put(Shp.BEACON, "beacon");
		ShpMAP.put(Shp.TOWER, "tower");
		ShpMAP.put(Shp.STAKE, "stake");
		ShpMAP.put(Shp.PERCH, "perch");
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

	public static final EnumMap<Col, String> ColMAP = new EnumMap<Col, String>(Col.class);
	static {
		ColMAP.put(Col.UNKNOWN, "");
		ColMAP.put(Col.WHITE, "white");
		ColMAP.put(Col.RED, "red");
		ColMAP.put(Col.ORANGE, "orange");
		ColMAP.put(Col.AMBER, "amber");
		ColMAP.put(Col.YELLOW, "yellow");
		ColMAP.put(Col.GREEN, "green");
		ColMAP.put(Col.BLUE, "blue");
		ColMAP.put(Col.VIOLET, "violet");
		ColMAP.put(Col.BLACK, "black");
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
		PatMAP.put(Pat.NONE, "");
		PatMAP.put(Pat.HORIZ, "horizontal_stripes");
		PatMAP.put(Pat.VERT, "vertical_stripes");
		PatMAP.put(Pat.DIAG, "diagonal_stripes");
		PatMAP.put(Pat.SQUARE, "squared");
		PatMAP.put(Pat.BORDER, "border_stripe");
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

	public static final EnumMap<Top, String> TopMAP = new EnumMap<Top, String>(Top.class);
	static {
		TopMAP.put(Top.NONE, "");
		TopMAP.put(Top.CAN, "cylinder");
		TopMAP.put(Top.CONE, "cylinder");
		TopMAP.put(Top.SPHERE, "SPHERE");
		TopMAP.put(Top.X_SHAPE, "X-SHAPE");
		TopMAP.put(Top.NORTH, "2 cones up");
		TopMAP.put(Top.SOUTH, "2 cones down");
		TopMAP.put(Top.EAST, "2 cones base together");
		TopMAP.put(Top.WEST, "2 cones points together");
		TopMAP.put(Top.SPHERES2, "2 spheres");
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

	public static final EnumMap<Day, String> DayMAP = new EnumMap<Day, String>(Day.class);
	static {
		DayMAP.put(Day.NONE, "");
		DayMAP.put(Day.BOARD, "board");
		DayMAP.put(Day.DIAMOND, "diamond");
		DayMAP.put(Day.CIRCLE, "circle");
		DayMAP.put(Day.TRIANGLE, "triangle, point up");
		DayMAP.put(Day.TRIANGLE_INV, "triangle, point down");
		DayMAP.put(Day.SQUARE, "square");
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
		for (Obj obj : ObjMAP.keySet()) {
			if (ObjMAP.get(obj).equals(str)) {
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
		for (Obj obj : ObjMAP.keySet()) {
			if (keys.containsKey("seamark:" + ObjMAP.get(obj) + ":name")) {
				str = keys.get("seamark:" + ObjMAP.get(obj) + ":name");
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

		for (Obj obj : ObjMAP.keySet()) {
			if (keys.containsKey("seamark:" + ObjMAP.get(obj) + ":category")) {
				str = keys.get("seamark:" + ObjMAP.get(obj) + ":category");
				setCategory(Cat.UNKNOWN);
				for (Cat cat : CatMAP.keySet()) {
					if (CatMAP.get(cat).equals(str)) {
						setCategory(cat);
					}
				}
			}
		}

		for (Obj obj : ObjMAP.keySet()) {
			if (keys.containsKey("seamark:" + ObjMAP.get(obj) + ":shape")) {
				str = keys.get("seamark:" + ObjMAP.get(obj) + ":shape");
				setShape(Shp.UNKNOWN);
				for (Shp shp : ShpMAP.keySet()) {
					if (ShpMAP.get(shp).equals(str)) {
						setShape(shp);
					}
				}
			}
		}
		if (getShape() == Shp.UNKNOWN) {
			if (EntMAP.get(getObject()) == Ent.BEACON)
				setShape(Shp.BEACON);
			if (EntMAP.get(getObject()) == Ent.FLOAT)
				setShape(Shp.FLOAT);
		}

		for (Obj obj : ObjMAP.keySet()) {
			if (keys.containsKey("seamark:" + ObjMAP.get(obj) + ":colour")) {
				str = keys.get("seamark:" + ObjMAP.get(obj) + ":colour");
				bodyColour.clear();
				for (String item : str.split(";")) {
					for (Col col : ColMAP.keySet()) {
						if (ColMAP.get(col).equals(item)) {
							bodyColour.add(col);
						}
					}
				}
			}
		}

		for (Obj obj : ObjMAP.keySet()) {
			if (keys.containsKey("seamark:" + ObjMAP.get(obj) + ":colour_pattern")) {
				str = keys.get("seamark:" + ObjMAP.get(obj) + ":colour_pattern");
				setPattern(Ent.BODY, Pat.NONE);
				for (Pat pat : PatMAP.keySet()) {
					if (PatMAP.get(pat).equals(str)) {
						setPattern(Ent.BODY, pat);
					}
				}
			}
		}

		for (Obj obj : ObjMAP.keySet()) {
			if (keys.containsKey("seamark:" + ObjMAP.get(obj) + ":system")) {
				str = keys.get("seamark:" + ObjMAP.get(obj) + ":system");
				if (str.equals("iala-a"))
					setRegion(Reg.A);
				else if (str.equals("iala-b"))
					setRegion(Reg.B);
				else setRegion(Reg.C);
			} else if (GrpMAP.get(object) == Grp.LAT) {
				if (getCategory() != Cat.UNKNOWN) {
					switch (getCategory()) {
					case LAT_PORT:
					case LAT_PREF_PORT:
						if (getColour(Ent.BODY, 0) == Col.RED) {
							setRegion(Reg.A);
							if (getColour(Ent.BODY, 1) == Col.WHITE)
								setRegion(Reg.C);
						}
						if (getColour(Ent.BODY, 0) == Col.GREEN)
							setRegion(Reg.B);
						break;
					case LAT_STBD:
					case LAT_PREF_STBD:
						if (getColour(Ent.BODY, 0) == Col.GREEN) {
							setRegion(Reg.A);
							if (getColour(Ent.BODY, 1) == Col.WHITE)
								setRegion(Reg.C);
						}
						if (getColour(Ent.BODY, 0) == Col.RED)
							setRegion(Reg.B);
						break;
					}
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
		case ISD:
			dlg.panelMain.hazButton.doClick();
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
					dlg.panelMain.panelChan.safeWaterButton.doClick();
				else if (getColour(Ent.FLOAT, 1) == Col.GREEN)
					if (getRegion().equals("B"))
						dlg.panelMain.panelChan.prefStbdButton.doClick();
					else
						dlg.panelMain.panelChan.prefPortButton.doClick();
				if (getRegion().equals("B"))
					dlg.panelMain.panelChan.stbdButton.doClick();
				else
					dlg.panelMain.panelChan.portButton.doClick();
				break;
			case GREEN:
				dlg.panelMain.chanButton.doClick();
				if (getColour(Ent.FLOAT, 1) == Col.RED)
					if (getRegion().equals("B"))
						dlg.panelMain.panelChan.prefPortButton.doClick();
					else
						dlg.panelMain.panelChan.prefStbdButton.doClick();
				if (getRegion().equals("B"))
					dlg.panelMain.panelChan.portButton.doClick();
				else
					dlg.panelMain.panelChan.stbdButton.doClick();
				break;
			case BLACK:
				dlg.panelMain.hazButton.doClick();
				break;
			case YELLOW:
				if (getColour(Ent.FLOAT, 1) == Col.BLACK)
					dlg.panelMain.hazButton.doClick();
				else
					dlg.panelMain.specButton.doClick();
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
		case FLOAT:
			imgStr += "Float";
			break;
		case BEACON:
			imgStr += "Beacon";
			break;
		case SUPER:
			imgStr += "Float_Major";
			break;
		case STAKE:
			imgStr += "Stake";
			break;
		default:
			if (EntMAP.get(getObject()) == Ent.BEACON)
				imgStr += "Beacon";
			if (EntMAP.get(getObject()) == Ent.BUOY)
				imgStr += "Pillar";
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

		Main.pref.put("smedplugin.IALA", getRegion() == Reg.C ? "C" : (getRegion() == Reg.B ? "B" : "A"));

		for (String str : node.getKeys().keySet()) {
			if (str.trim().matches("^seamark:\\S+"))
				Main.main.undoRedo.add(new ChangePropertyCommand(node, str, null));
		}

		if (!getName().isEmpty())
			Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:name", getName()));

		String objStr = ObjMAP.get(object);
		if (objStr != null) {
			Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:type", objStr));

			String str = CatMAP.get(category);
			if (str != null)
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":category", str));

			Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":shape", ShpMAP.get(shape)));

			if (getColour(Ent.BODY, 0) != Col.UNKNOWN) {
				str = ColMAP.get(getColour(Ent.BODY, 0));
				for (int i = 1; bodyColour.size() > i; i++) {
					str += (";" + ColMAP.get(getColour(Ent.BODY, i)));
				}
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour", str));
			}

			if (getPattern(Ent.BODY) != Pat.NONE) {
				str = PatMAP.get(getPattern(Ent.BODY));
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour_pattern", str));
			}
			
			if (GrpMAP.get(object) == Grp.LAT) {
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

			/*
			 * switch (bodyColour) { case RED_GREEN_RED: case GREEN_RED_GREEN: case
			 * BLACK_YELLOW: case BLACK_YELLOW_BLACK: case YELLOW_BLACK: case
			 * YELLOW_BLACK_YELLOW: case BLACK_RED_BLACK: Main.main.undoRedo.add(new
			 * ChangePropertyCommand(node, "seamark:" + objStr + ":colour_pattern",
			 * "horizontal stripes")); break; case RED_WHITE:
			 * Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" +
			 * objStr + ":colour_pattern", "vertical stripes")); break; }
			 */}

		/*
		 * String str = TopMAP.get(topShape); if (str != null) {
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:topmark:shape", str));
		 * 
		 * str = ColMAP.get(topColour); if (str != null) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:topmark:colour", str)); }
		 * 
		 * str = DayMAP.get(dayShape); if (str != null) { Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:daymark:shape", str));
		 * 
		 * str = ColMAP.get(dayColour); if (str != null) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:daymark:colour", str)); } /* Col
		 * colour; if (isFired()) { if ((colour = lightColour[0]) != Col.UNKNOWN) if
		 * (colour == Col.RED) { Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:light:colour", "red")); } else if
		 * (colour.equals("G")) { Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:light:colour", "green")); } else if
		 * (colour.equals("W")) { Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:light:colour", "white")); } if
		 * (!LightPeriod[0].isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:light:period", LightPeriod[0])); if
		 * (!LightChar[0].isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:light:character", LightChar[0])); if
		 * (!LightGroup[0].isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:light:group", LightGroup[0])); if
		 * (!Height[0].isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:light:height", Height[0])); if
		 * (!Range[0].isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:light:range", Range[0])); for (int i
		 * = 1; i < 10; i++) { if ((colour = lightColour[i]) != Col.UNKNOWN) // if
		 * (colour.equals("R")) { // Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:light:" + i + ":colour", "red")); //
		 * if ((Bearing1[i] != null) && (Bearing2[i] != null) && (Radius[i] !=
		 * null)) // Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:light:" + i, "red:" + Bearing1[i] + ":" // + Bearing2[i] + ":" +
		 * Radius[i])); // } else if (colour.equals("G")) { //
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" +
		 * i + ":colour", "green")); // if ((Bearing1[i] != null) && (Bearing2[i] !=
		 * null) && (Radius[i] != null)) // Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:light:" + i, "green:" + Bearing1[i]
		 * + ":" // + Bearing2[i] + ":" + Radius[i])); // } else if
		 * (colour.equals("W")) { // Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:light:" + i + ":colour", "white"));
		 * // if ((Bearing1[i] != null) && (Bearing2[i] != null) && (Radius[i] !=
		 * null)) // Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:light:" + i, "white:" + Bearing1[i] + ":" // + Bearing2[i] + ":"
		 * + Radius[i])); // } if (LightPeriod[i] != null)
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" +
		 * i + ":period", LightPeriod[i])); if (LightChar[i] != null)
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" +
		 * i + ":character", LightChar[i])); if (LightGroup[i] != null)
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" +
		 * i + ":group", LightGroup[i])); if (Height[i] != null)
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" +
		 * i + ":height", Height[i])); if (Range[i] != null)
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" +
		 * i + ":range", Range[i])); if (Bearing1[i] != null)
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" +
		 * i + ":sector_start", Bearing1[i])); if (Bearing2[i] != null)
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" +
		 * i + ":sector_end", Bearing2[i])); } }
		 */
		/*
		 * if (hasRadar()) { Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:radar_reflector", "yes")); } if (hasRacon()) { switch (RaType) {
		 * case RACON: Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:radar_transponder:category", "racon")); if
		 * (!getRaconGroup().isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:radar_transponder:group",
		 * getRaconGroup())); break; case RAMARK: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:radar_transponder:category",
		 * "ramark")); break; case LEADING: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:radar_transponder:category",
		 * "leading")); break; default: Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:radar_transponder", "yes")); } } if
		 * (hasFog()) { switch (getFogSound()) { case HORN:
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:fog_signal:category", "horn")); break; case SIREN:
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:fog_signal:category", "siren")); break; case DIA:
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:fog_signal:category", "diaphone")); break; case BELL:
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:fog_signal:category", "bell")); break; case WHIS:
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:fog_signal:category", "whistle")); break; case GONG:
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:fog_signal:category", "gong")); break; case EXPLOS:
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:fog_signal:category", "explosive")); break; default:
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:fog_signal", "yes")); } if (!getFogGroup().isEmpty())
		 * Main.main.undoRedo.add(new ChangePropertyCommand(node,
		 * "seamark:fog_signal:group", getFogGroup())); if
		 * (!getFogPeriod().isEmpty()) Main.main.undoRedo.add(new
		 * ChangePropertyCommand(node, "seamark:fog_signal:period",
		 * getFogPeriod())); }
		 */}

}
