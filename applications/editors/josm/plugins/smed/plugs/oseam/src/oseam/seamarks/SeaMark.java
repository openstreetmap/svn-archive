package oseam.seamarks;

import javax.swing.ImageIcon;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.command.ChangePropertyCommand;

import oseam.Messages;
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

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String nam) {
		name = nam;
	}

	public enum Obj {
		UNKNOWN, BCNCAR, BCNISD, BCNLAT, BCNSAW, BCNSPP, BOYCAR, BOYISD, BOYLAT, BOYSAW, BOYSPP, LITMAJ, LITMIN, LITFLT, LITVES, LNDMRK, MORFAC, SIGSTA
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
		LAT, CAR, SAW, ISD, SPP, FLT, LIT, SIS
	}

	public static final EnumMap<Obj, Grp> GrpMAP = new EnumMap<Obj, Grp>(Obj.class);
	static {
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
		ShpMAP.put(Shp.SPHERE, "sphere");
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

	public void setShape(Shp styl) {
		shape = styl;
	}

	public enum Col {
		UNKNOWN, WHITE, RED, ORANGE, AMBER, YELLOW, GREEN, BLUE, VIOLET, BLACK, RED_GREEN_RED, GREEN_RED_GREEN, RED_WHITE, BLACK_YELLOW, BLACK_YELLOW_BLACK, YELLOW_BLACK, YELLOW_BLACK_YELLOW, BLACK_RED_BLACK
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
		ColMAP.put(Col.RED_GREEN_RED, "red;green;red");
		ColMAP.put(Col.GREEN_RED_GREEN, "green;red;green");
		ColMAP.put(Col.RED_WHITE, "red;white");
		ColMAP.put(Col.BLACK_YELLOW, "black;yellow");
		ColMAP.put(Col.BLACK_YELLOW_BLACK, "black;yellow;black");
		ColMAP.put(Col.YELLOW_BLACK, "yellow;black");
		ColMAP.put(Col.YELLOW_BLACK_YELLOW, "yellow;black;yellow");
		ColMAP.put(Col.BLACK_RED_BLACK, "black;red;black");
	}

	private Col bodyColour = Col.UNKNOWN;

	public Col getColour(Ent ent) {
		switch (ent) {
		case BODY:
		case BUOY:
		case BEACON:
		case FLOAT:
			return bodyColour;
		case TOPMARK:
			return topColour;
		case DAYMARK:
			return dayColour;
//		case LIGHT:
//			return light.getLightColour();
		}
		return Col.UNKNOWN;
	}

	public void setColour(Ent ent, Col col) {
		switch (ent) {
		case BODY:
		case BUOY:
		case BEACON:
		case FLOAT:
			bodyColour = col;
			break;
		case TOPMARK:
			topColour = col;
			break;
		case DAYMARK:
			dayColour = col;
			break;
//		case LIGHT:
//			light.setLightColour(col);
//			break;
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
	private Col topColour = Col.UNKNOWN;

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
	private Col dayColour = Col.UNKNOWN;

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
		region = Main.pref.get("tomsplugin.IALA").equals("A") ? Reg.A : Reg.B;
		paintlock = true;
		dlg.panelMain.clearSelections();
		dlg.manager.showVisualMessage("");
		String typeStr = "";
		String colStr = "";
		String str = "";

		Map<String, String> keys = node.getKeys();

		if (keys.containsKey("seamark:type"))
			typeStr = keys.get("seamark:type");

		for (Obj obj : ObjMAP.keySet())
			if (ObjMAP.get(obj).equals(typeStr))
				setObject(obj);

		if (typeStr.equals("light_float") || typeStr.equals("") || keys.containsKey("seamark:light_float:colour")
		    || keys.containsKey("seamark:light_float:colour_pattern")) {
			if (keys.containsKey("seamark:light_float:colour_pattern")) {
				setObject(Obj.LITFLT);
				typeStr = "light_float";
			}
			if (keys.containsKey("seamark:light_float:colour")) {
				colStr = keys.get("seamark:light_float:colour");
				if (colStr.equals("red") || colStr.equals("green") || colStr.equals("red;green;red") || colStr.equals("green;red;green")) {
					setObject(Obj.BOYLAT);
				} else if (colStr.equals("black;yellow") || colStr.equals("black;yellow;black") || colStr.equals("yellow;black")
				    || colStr.equals("yellow;black;yellow")) {
					setObject(Obj.BOYCAR);
				} else if (colStr.equals("black;red;black")) {
					setObject(Obj.BOYISD);
				} else if (colStr.equals("red;white")) {
					setObject(Obj.BOYSAW);
				} else if (colStr.equals("yellow")) {
					setObject(Obj.BOYSPP);
				} else
					setObject(Obj.LITFLT);
				typeStr = "light_float";
			}
			if (typeStr.equals("")) {
				if (keys.containsKey("seamark:buoy_lateral:category") || keys.containsKey("seamark:buoy_lateral:shape")
				    || keys.containsKey("seamark:buoy_lateral:colour")) {
					setObject(Obj.BOYLAT);
					typeStr = "buoy_lateral";
				} else if (keys.containsKey("seamark:beacon_lateral:category") || keys.containsKey("seamark:beacon_lateral:shape")
				    || keys.containsKey("seamark:beacon_lateral:colour")) {
					setObject(Obj.BCNLAT);
					typeStr = "beacon_lateral";
				} else if (keys.containsKey("seamark:buoy_cardinal:category") || keys.containsKey("seamark:buoy_cardinal:shape")
				    || keys.containsKey("seamark:buoy_cardinal:colour")) {
					setObject(Obj.BOYCAR);
					typeStr = "buoy_cardinal";
				} else if (keys.containsKey("seamark:beacon_cardinal:category") || keys.containsKey("seamark:beacon_cardinal:shape")
				    || keys.containsKey("seamark:beacon_cardinal:colour")) {
					setObject(Obj.BCNCAR);
					typeStr = "beacon_cardinal";
				} else if (keys.containsKey("seamark:buoy_isolated_danger:category")
				    || keys.containsKey("seamark:buoy_isolated_danger:shape") || keys.containsKey("seamark:buoy_isolated_danger:colour")) {
					setObject(Obj.BOYISD);
					typeStr = "buoy_isolated_danger";
				} else if (keys.containsKey("seamark:beacon_isolated_danger:category")
				    || keys.containsKey("seamark:beacon_isolated_danger:shape")
				    || keys.containsKey("seamark:beacon_isolated_danger:colour")) {
					setObject(Obj.BCNISD);
					typeStr = "beacon_isolated_danger";
				} else if (keys.containsKey("seamark:buoy_safe_water:category") || keys.containsKey("seamark:buoy_safe_water:shape")
				    || keys.containsKey("seamark:buoy_safe_water:colour")) {
					setObject(Obj.BOYSAW);
					typeStr = "buoy_safe_water";
				} else if (keys.containsKey("seamark:beacon_safe_water:category") || keys.containsKey("seamark:beacon_safe_water:shape")
				    || keys.containsKey("seamark:beacon_safe_water:colour")) {
					setObject(Obj.BCNSAW);
					typeStr = "beacon_safe_water";
				} else if (keys.containsKey("seamark:buoy_special_purpose:category")
				    || keys.containsKey("seamark:buoy_special_purpose:shape") || keys.containsKey("seamark:buoy_special_purpose:colour")) {
					setObject(Obj.BOYSPP);
					typeStr = "buoy_special_purpose";
				} else if (keys.containsKey("seamark:beacon_special_purpose:category")
				    || keys.containsKey("seamark:beacon_special_purpose:shape")
				    || keys.containsKey("seamark:beacon_special_purpose:colour")) {
					setObject(Obj.BCNSPP);
					typeStr = "beacon_special_purpose";
				}
			}
		}

		if (getObject() == Obj.UNKNOWN) {
			dlg.manager.showVisualMessage(Messages.getString("NoMark"));
			return;
		}

		if (keys.containsKey("seamark:" + typeStr + ":colour"))
			colStr = keys.get("seamark:" + typeStr + ":colour");
		for (Col col : ColMAP.keySet())
			if (ColMAP.get(col).equals(colStr)) setColour(Ent.BODY, col);

		if (keys.containsKey("seamark:" + typeStr + ":name")) {
			dlg.panelMain.nameBox.setText(keys.get("seamark:" + typeStr + ":name"));
			dlg.panelMain.nameBox.postActionEvent();
		} else if (keys.containsKey("seamark:name")) {
			dlg.panelMain.nameBox.setText(keys.get("seamark:name"));
			dlg.panelMain.nameBox.postActionEvent();
		} else if (keys.containsKey("name")) {
			dlg.panelMain.nameBox.setText(keys.get("name"));
			dlg.panelMain.nameBox.postActionEvent();
		} else
			dlg.panelMain.nameBox.setText("");

		switch (GrpMAP.get(getObject())) {
		case LAT:
			dlg.panelMain.chanButton.doClick();
			switch (getObject()) {
			case BCNLAT:
				if (keys.containsKey("seamark:beacon_lateral:category"))
					str = keys.get("seamark:beacon_lateral:category");
				break;
			case BOYLAT:
				if (keys.containsKey("seamark:buoy_lateral:category"))
					str = keys.get("seamark:buoy_lateral:category");
				else if (typeStr.equals("light_float")) {
					if (region == Reg.A) {
						if (colStr.equals("red"))
							str = "port";
						else if (colStr.equals("green"))
							str = "starboard";
						else if (colStr.equals("red;green;red"))
							str = "preferred_channel_port";
						else if (colStr.equals("green;red;green"))
							str = "preferred_channel_starboard";
					} else {
						if (colStr.equals("green"))
							str = "port";
						else if (colStr.equals("red"))
							str = "starboard";
						else if (colStr.equals("green;red;green"))
							str = "preferred_channel_port";
						else if (colStr.equals("red;green;red"))
							str = "preferred_channel_starboard";
					}
				}
				break;
			}
			if (str.equals("port")) {
				setCategory(Cat.LAT_PORT);
				dlg.panelMain.panelChan.portButton.doClick();
			} else if (str.equals("starboard")) {
				setCategory(Cat.LAT_STBD);
				dlg.panelMain.panelChan.stbdButton.doClick();
			} else if (str.equals("preferred_channel_port")) {
				setCategory(Cat.LAT_PREF_PORT);
				dlg.panelMain.panelChan.prefPortButton.doClick();
			} else if (str.equals("preferred_channel_starboard")) {
				setCategory(Cat.LAT_PREF_STBD);
				dlg.panelMain.panelChan.prefStbdButton.doClick();
			}
			break;
		case CAR:
			dlg.panelMain.hazButton.doClick();
			switch (getObject()) {
			case BCNCAR:
				if (keys.containsKey("seamark:beacon_cardinal:category"))
					str = keys.get("seamark:beacon_cardinal:category");
				break;
			case BOYCAR:
				if (keys.containsKey("seamark:buoy_cardinal:category"))
					str = keys.get("seamark:buoy_cardinal:category");
				else if (typeStr.equals("light_float")) {
					if (colStr.equals("black;yellow"))
						str = "north";
					else if (colStr.equals("yellow;black"))
						str = "south";
					else if (colStr.equals("black;yellow;black"))
						str = "east";
					else if (colStr.equals("yellow;black;yellow"))
						str = "west";
				}
				break;
			}
			if (str.equals("north")) {
				setCategory(Cat.CARD_NORTH);
				dlg.panelMain.panelHaz.northButton.doClick();
			} else if (str.equals("south")) {
				setCategory(Cat.CARD_SOUTH);
				dlg.panelMain.panelHaz.southButton.doClick();
			} else if (str.equals("east")) {
				setCategory(Cat.CARD_EAST);
				dlg.panelMain.panelHaz.eastButton.doClick();
			} else if (str.equals("west")) {
				setCategory(Cat.CARD_WEST);
				dlg.panelMain.panelHaz.westButton.doClick();
			}
			break;
		case SAW:
			dlg.panelMain.chanButton.doClick();
			dlg.panelMain.panelChan.safeWaterButton.doClick();
			break;
		case ISD:
			dlg.panelMain.hazButton.doClick();
			dlg.panelMain.panelHaz.isolButton.doClick();
			break;
		case SPP:
			dlg.panelMain.specButton.doClick();
			break;
		case SIS:
			dlg.panelMain.lightsButton.doClick();
			dlg.panelMain.panelLights.stationButton.doClick();
			break;
		case LIT:
			dlg.panelMain.lightsButton.doClick();
			break;
		}

		if (keys.containsKey("seamark:" + typeStr + ":shape"))
			str = keys.get("seamark:" + typeStr + ":shape");
		else if (typeStr.equals("light_float"))
			str = "float";
		else str = "";
		if ((str.isEmpty() && (EntMAP.get(getObject()) == Ent.BEACON)) || str.equals("stake"))
			str = "beacon";
		if (!str.isEmpty()) {
			for (Shp shp : ShpMAP.keySet()) {
				if (ShpMAP.get(shp).equals(str)) {
					switch (GrpMAP.get(getObject())) {
					case LAT:
						switch (getCategory()) {
						case LAT_PORT:
						case LAT_PREF_PORT:
							if (dlg.panelMain.panelChan.panelPort.shapes.get(shp) != null)
								dlg.panelMain.panelChan.panelPort.shapes.get(shp).doClick();
							break;
						case LAT_STBD:
						case LAT_PREF_STBD:
							if (dlg.panelMain.panelChan.panelStbd.shapes.get(shp) != null)
								dlg.panelMain.panelChan.panelStbd.shapes.get(shp).doClick();
							break;
						}
						break;
					case SAW:
						if (dlg.panelMain.panelChan.panelSaw.shapes.get(shp) != null)
							dlg.panelMain.panelChan.panelSaw.shapes.get(shp).doClick();
						break;
					case CAR:
					case ISD:
						if (dlg.panelMain.panelHaz.shapes.get(shp) != null)
							dlg.panelMain.panelHaz.shapes.get(shp).doClick();
						break;
					case SPP:
						if (dlg.panelMain.panelSpec.shapes.get(shp) != null)
							dlg.panelMain.panelSpec.shapes.get(shp).doClick();
						break;
					}
					break;
				}
			}
		}

		if (keys.containsKey("seamark:topmark:shape")) {
			str = keys.get("seamark:topmark:shape");
		}
/*
		for (Map.Entry<String, String> entry : keys.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().trim();
			if (key.contains("seamark:light:")) {
				light.setFired(true);
				int index = 0;
				key = key.substring(14);
				if (key.matches("^\\d:.*")) {
					index = key.charAt(0) - '0';
					key = key.substring(2);
				} else if (key.matches("^\\d$")) {
					index = key.charAt(0) - '0';
					String values[] = value.split(":");
					if (values[0].equals("red"))
						lightColour[index] = Col.RED;
					else if (values[0].equals("green"))
						lightColour[index] = Col.GREEN;
					else if (values[0].equals("white"))
						lightColour[index] = Col.WHITE;
					if (values.length > 1)
						Bearing1[index] = values[1];
					if (values.length > 2)
						Bearing2[index] = values[2];
				} else {
					index = 0;
				}
				if (index != 0)
					setSectored(true);
				if (key.equals("colour")) {
					if (value.equals("red"))
						lightColour[index] = Col.RED;
					else if (value.equals("green"))
						lightColour[index] = Col.GREEN;
					else if (value.equals("white"))
						lightColour[index] = Col.WHITE;
				} else if (key.equals("character")) {
					LightChar[index] = value;
				} else if (key.equals("group")) {
					LightGroup[index] = value;
				} else if (key.equals("period")) {
					LightPeriod[index] = value;
				} else if (key.equals("height")) {
					Height[index] = value;
				} else if (key.equals("range")) {
					Range[index] = value;
				}
			}
		}
*/
		if (keys.containsKey("seamark:fog_signal") || keys.containsKey("seamark:fog_signal:category")
		    || keys.containsKey("seamark:fog_signal:group") || keys.containsKey("seamark:fog_signal:period")) {
			setFog(true);
			if (keys.containsKey("seamark:fog_signal:category")) {
				str = keys.get("seamark:fog_signal:category");
				if (str.equals("horn"))
					setFogSound(Fog.HORN);
				else if (str.equals("siren"))
					setFogSound(Fog.SIREN);
				else if (str.equals("diaphone"))
					setFogSound(Fog.DIA);
				else if (str.equals("bell"))
					setFogSound(Fog.BELL);
				else if (str.equals("whis"))
					setFogSound(Fog.WHIS);
				else if (str.equals("gong"))
					setFogSound(Fog.GONG);
				else if (str.equals("explosive"))
					setFogSound(Fog.EXPLOS);
				else
					setFogSound(Fog.UNKNOWN);
			}
			if (keys.containsKey("seamark:fog_signal:group"))
				setFogGroup(keys.get("seamark:fog_signal:group"));
			if (keys.containsKey("seamark:fog_signal:period"))
				setFogPeriod(keys.get("seamark:fog_signal:period"));
		}

		if (keys.containsKey("seamark:radar_transponder") || keys.containsKey("seamark:radar_transponder:category")
		    || keys.containsKey("seamark:radar_transponder:group")) {
			setRacon(true);
			if (keys.containsKey("seamark:radar_transponder:category")) {
				str = keys.get("seamark:radar_transponder:category");
				if (str.equals("racon"))
					setRaType(Rtb.RACON);
				else if (str.equals("ramark"))
					setRaType(Rtb.RAMARK);
				else if (str.equals("leading"))
					setRaType(Rtb.LEADING);
				else
					setRaType(Rtb.UNKNOWN);
			}
			if (keys.containsKey("seamark:radar_transponder:group"))
				setRaconGroup(keys.get("seamark:radar_transponder:group"));
		} else if (keys.containsKey("seamark:radar_reflector"))
			setRadar(true);
		paintlock = false;
		paintSign();
	}

	public void paintSign() {

		if (paintlock)
			return;

		String imgStr = "/images/";
		String shpStr = ShpMAP.get(shape);
		if (shpStr == null)
			dlg.panelMain.shapeIcon.setIcon(null);
		else {
			imgStr += shpStr;
			if (shpStr.equals("perch")) {
				imgStr += (getCategory() == Cat.LAT_PORT ? "_port" : "_stbd");
			} else {
				String colStr = ColMAP.get(bodyColour);
				if (colStr != null)
					imgStr += ("_" + colStr);
			}
			imgStr += ".png";
			if (getClass().getResource(imgStr) == null)
				System.out.println("Body:" + imgStr);
			else
				dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource(imgStr)));
		}

		if (topShape == Top.NONE)
			dlg.panelMain.topIcon.setIcon(null);
		else {
			imgStr = "/images/top_";
			switch (getShape()) {
			case CAN:
			case CONE:
			case SPHERE:
			case BARREL:
				imgStr += "il_";
				break;
			case PILLAR:
			case SPAR:
				imgStr += "ih_";
				break;
			case FLOAT:
			case SUPER:
				imgStr += "vl_";
				break;
			case BEACON:
			case TOWER:
				imgStr += "vh_";
				break;
			default:
				imgStr = null;
				dlg.panelMain.topIcon.setIcon(null);
				break;
			}
			if (imgStr != null) {
				imgStr += (TopMAP.get(topShape) + "_");
				String colStr = ColMAP.get(topColour);
				if (colStr != null)
					imgStr += colStr;
				imgStr += ".png";
				if (getClass().getResource(imgStr) == null)
					System.out.println("Top:" + imgStr);
				else
					dlg.panelMain.topIcon.setIcon(new ImageIcon(getClass().getResource(imgStr)));
			}
		}
		/*
		 * if (hasRadar()) { dlg.panelMain.radarIcon.setIcon(new
		 * ImageIcon(getClass().getResource("/images/Radar_Reflector_355.png"))); }
		 * else if (hasRacon()) { dlg.panelMain.radarIcon.setIcon(new
		 * ImageIcon(getClass().getResource("/images/Radar_Station.png"))); // if
		 * (getRaType() != 0) { // String c = (String)
		 * dlg.cbM01Racon.getSelectedItem(); // if ((getRaType() == RATYPE_RACON) &&
		 * !getRaconGroup().isEmpty()) // c += ("(" + getRaconGroup() + ")"); //
		 * dlg.lM01RadarMarkeys.setText(c); // } }
		 * 
		 * if (hasFog()) { dlg.panelMain.fogIcon.setIcon(new
		 * ImageIcon(getClass().getResource("/images/Fog_Signal.png"))); // if
		 * (getFogSound() != 0) { // String c = (String)
		 * dlg.cbM01Fog.getSelectedItem(); // if (!getFogGroup().isEmpty()) // c +=
		 * ("(" + getFogGroup() + ")"); // if (!getFogPeriod().isEmpty()) // c +=
		 * (" " + getFogPeriod() + "s"); // dlg.lM01FogMarkeys.setText(c); // } }
		 * 
		 * if (isFired()) { String lp, c; String tmp = null; int i1;
		 * 
		 * Col col = getColour(Ent.LIGHT); if (col == Col.WHITE) {
		 * dlg.panelMain.lightIcon.setIcon(new
		 * ImageIcon(getClass().getResource("/images/Light_White_120.png"))); } else
		 * if (col == Col.RED) { dlg.panelMain.lightIcon.setIcon(new
		 * ImageIcon(getClass().getResource("/images/Light_Red_120.png"))); } else
		 * if (col == Col.GREEN) { dlg.panelMain.lightIcon.setIcon(new
		 * ImageIcon(getClass().getResource("/images/Light_Green_120.png"))); } else
		 * { dlg.panelMain.lightIcon.setIcon(new
		 * ImageIcon(getClass().getResource("/images/Light_Magenta_120.png"))); }
		 * 
		 * c = getLightChar(); if (c.contains("+")) { i1 = c.indexOf("+"); tmp =
		 * c.substring(i1, c.length()); c = c.substring(0, i1); if
		 * (!getLightGroup().isEmpty()) { c = c + "(" + getLightGroup() + ")"; } if
		 * (tmp != null) c = c + tmp; } else if (!getLightGroup().isEmpty()) c = c +
		 * "(" + getLightGroup() + ")"; c = c + " " + getLightColour(); lp =
		 * getLightPeriod(); if (!lp.isEmpty()) c = c + " " + lp + "s"; }
		 */
	}

	public void saveSign(Node node) {

		Main.pref.put("tomsplugin.IALA", getRegion() == Reg.A ? "A" : "B");

//		for (String str : node.getKeys().keySet()) {
//			if (str.contains("seamark"))
//				if (!str.equals("seamark")) {
//					Main.main.undoRedo.add(new ChangePropertyCommand(node, str, null));
//				}
//		}
		
		if (!name.isEmpty())
			Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:name", name));

		String objStr = ObjMAP.get(object);
		if (objStr != null) {
			Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:type", objStr));

			String str = CatMAP.get(category);
			if (str != null)
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":category", str));

			Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":shape", ShpMAP.get(shape)));

			str = ColMAP.get(bodyColour);
			if (str != null)
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour", str));

			switch (bodyColour) {
			case RED_GREEN_RED:
			case GREEN_RED_GREEN:
			case BLACK_YELLOW:
			case BLACK_YELLOW_BLACK:
			case YELLOW_BLACK:
			case YELLOW_BLACK_YELLOW:
			case BLACK_RED_BLACK:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour_pattern", "horizontal stripes"));
				break;
			case RED_WHITE:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:" + objStr + ":colour_pattern", "vertical stripes"));
				break;
			}
		}

		String str = TopMAP.get(topShape);
		if (str != null) {
			Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:topmark:shape", str));

			str = ColMAP.get(topColour);
			if (str != null)
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:topmark:colour", str));
		}

		str = DayMAP.get(dayShape);
		if (str != null) {
			Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:daymark:shape", str));

			str = ColMAP.get(dayColour);
			if (str != null)
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:daymark:colour", str));
		}
/*
		Col colour;
		if (isFired()) {
			if ((colour = lightColour[0]) != Col.UNKNOWN)
				if (colour == Col.RED) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:colour", "red"));
				} else if (colour.equals("G")) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:colour", "green"));
				} else if (colour.equals("W")) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:colour", "white"));
				}
			if (!LightPeriod[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:period", LightPeriod[0]));
			if (!LightChar[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:character", LightChar[0]));
			if (!LightGroup[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:group", LightGroup[0]));
			if (!Height[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:height", Height[0]));
			if (!Range[0].isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:range", Range[0]));
			for (int i = 1; i < 10; i++) {
				if ((colour = lightColour[i]) != Col.UNKNOWN)
//					if (colour.equals("R")) {
//						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i + ":colour", "red"));
//						if ((Bearing1[i] != null) && (Bearing2[i] != null) && (Radius[i] != null))
//							Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i, "red:" + Bearing1[i] + ":"
//							    + Bearing2[i] + ":" + Radius[i]));
//					} else if (colour.equals("G")) {
//						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i + ":colour", "green"));
//						if ((Bearing1[i] != null) && (Bearing2[i] != null) && (Radius[i] != null))
//							Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i, "green:" + Bearing1[i] + ":"
//							    + Bearing2[i] + ":" + Radius[i]));
//					} else if (colour.equals("W")) {
//						Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i + ":colour", "white"));
//						if ((Bearing1[i] != null) && (Bearing2[i] != null) && (Radius[i] != null))
//							Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i, "white:" + Bearing1[i] + ":"
//							    + Bearing2[i] + ":" + Radius[i]));
//					}
				if (LightPeriod[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i + ":period", LightPeriod[i]));
				if (LightChar[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i + ":character", LightChar[i]));
				if (LightGroup[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i + ":group", LightGroup[i]));
				if (Height[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i + ":height", Height[i]));
				if (Range[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i + ":range", Range[i]));
				if (Bearing1[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i + ":sector_start", Bearing1[i]));
				if (Bearing2[i] != null)
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:light:" + i + ":sector_end", Bearing2[i]));
			}
		}*/
		if (hasRadar()) {
			Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_reflector", "yes"));
		}
		if (hasRacon()) {
			switch (RaType) {
			case RACON:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder:category", "racon"));
				if (!getRaconGroup().isEmpty())
					Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder:group", getRaconGroup()));
				break;
			case RAMARK:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder:category", "ramark"));
				break;
			case LEADING:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder:category", "leading"));
				break;
			default:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:radar_transponder", "yes"));
			}
		}
		if (hasFog()) {
			switch (getFogSound()) {
			case HORN:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:category", "horn"));
				break;
			case SIREN:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:category", "siren"));
				break;
			case DIA:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:category", "diaphone"));
				break;
			case BELL:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:category", "bell"));
				break;
			case WHIS:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:category", "whistle"));
				break;
			case GONG:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:category", "gong"));
				break;
			case EXPLOS:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:category", "explosive"));
				break;
			default:
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal", "yes"));
			}
			if (!getFogGroup().isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:group", getFogGroup()));
			if (!getFogPeriod().isEmpty())
				Main.main.undoRedo.add(new ChangePropertyCommand(node, "seamark:fog_signal:period", getFogPeriod()));
		}
	}

}
