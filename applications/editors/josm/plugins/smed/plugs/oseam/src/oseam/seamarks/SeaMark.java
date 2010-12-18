package oseam.seamarks;

import java.util.Iterator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.osm.Node;

import oseam.dialogs.OSeaMAction;

abstract public class SeaMark {

	public enum Type {UNKNOWN_TYPE, LATERAL, CARDINAL, SAFE_WATER, ISOLATED_DANGER, SPECIAL_PURPOSE, LIGHT}

	public enum Cat {UNKNOWN_CAT, PORT_HAND, STARBOARD_HAND, PREF_PORT_HAND, PREF_STARBOARD_HAND, CARD_NORTH, CARD_EAST, CARD_SOUTH, CARD_WEST, LIGHT_HOUSE, LIGHT_MAJOR, LIGHT_MINOR, LIGHT_VESSEL}

	public final static boolean IALA_A = false;
	public final static boolean IALA_B = true;

	public enum Styl {UNKNOWN_SHAPE, PILLAR, SPAR, CAN, CONE, SPHERE, BARREL, FLOAT, SUPER, BEACON, TOWER, STAKE, PERCH}

	public enum Col {UNKNOWN_COLOUR, RED, GREEN, RED_GREEN_RED, GREEN_RED_GREEN, RED_WHITE, BLACK_YELLOW, BLACK_YELLOW_BLACK, YELLOW_BLACK, YELLOW_BLACK_YELLOW, BLACK_RED_BLACK, YELLOW}
	
	public final static int WHITE_LIGHT = 1;
	public final static int RED_LIGHT = 2;
	public final static int GREEN_LIGHT = 3;

	/**
	 * Topmark Shapes - correspond to TopMarkIndex
	 */

	public final static int UNKNOWN_TOPMARK = 0;
	public final static int TOP_YELLOW_X = 1;
	public final static int TOP_RED_X = 2;
	public final static int TOP_YELLOW_CAN = 3;
	public final static int TOP_YELLOW_CONE = 4;

	/**
	 * Radar Beacons - correspond to Ratyp Index
	 */

	public final static int UNKNOWN_RATYPE = 0;
	public final static int RATYPE_RACON = 1;
	public final static int RATYPE_RAMARK = 2;
	public final static int RATYPE_LEADING = 3;

	/**
	 * Fog Signals - correspond to FogSound Index
	 */

	public final static int UNKNOWN_FOG = 0;
	public final static int FOG_HORN = 1;
	public final static int FOG_SIREN = 2;
	public final static int FOG_DIA = 3;
	public final static int FOG_BELL = 4;
	public final static int FOG_WHIS = 5;
	public final static int FOG_GONG = 6;
	public final static int FOG_EXPLOS = 7;

	/**
	 * Variables
	 */

	protected OSeaMAction dlg = null;

	public OSeaMAction getDlg() {
		return dlg;
	}

	public void setDlg(OSeaMAction dia) {
		dlg = dia;
	}

	protected SeaMark(OSeaMAction dia, Node node) {
		dlg = dia;
		this.node = node;
	}

	private Node node = null;

	public Node getNode() {
		return node;
	}

	public void setNode(Node nod) {
		node = nod;
	}

	private boolean region = false;

	public boolean getRegion() {
		return region;
	}

	public void setRegion(boolean reg) {
		region = reg;
	}

	private Col colour = Col.UNKNOWN_COLOUR;

	public Col getColour() {
		return colour;
	}

	public void setColour(Col col) {
		colour = col;
	}

	private String errMsg = null;

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String msg) {
		errMsg = msg;
	}

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String nam) {
		name = nam;
	}

	private Cat category = Cat.UNKNOWN_CAT;

	public Cat getCategory() {
		return category;
	}

	public void setCategory(Cat cat) {
		category = cat;
	}

	private Styl shape = Styl.UNKNOWN_SHAPE;

	public Styl getShape() {
		return shape;
	}

	public void setShape(Styl styl) {
		shape = styl;
	}

	private boolean valid = true;

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean val) {
		valid = val;
	}

	private int SectorIndex = 0;

	public int getSectorIndex() {
		return SectorIndex;
	}

	public void setSectorIndex(int sector) {
		SectorIndex = sector;
	}

	private String[] LightColour = new String[10];

	public String getLightColour() {
		if (LightColour[SectorIndex] == null)
			return (LightColour[0]);
		return LightColour[SectorIndex];
	}

	public void setLightColour(String lightColour) {
		LightColour[SectorIndex] = lightColour;
	}

	public abstract void parseMark();
	
	public abstract void paintSign();

	public abstract void saveSign();

	protected void delSeaMarkKeys(Node node) {
		Iterator<String> it = node.getKeys().keySet().iterator();
		String str;

		while (it.hasNext()) {
			str = it.next();

			if (str.contains("seamark") == true)
				if (str.compareTo("seamark") != 0) {
					Main.main.undoRedo.add(new ChangePropertyCommand(node, str, null));
				}
		}
	}

}
