package oseam.seamarks;

import java.util.Map;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;

import oseam.dialogs.OSeaMAction;
import oseam.seamarks.SeaMark;

public class MarkSaw extends SeaMark {
	public MarkSaw(OSeaMAction dia) {
		super(dia);
	}

	public void parseMark() {

		String str;
		Map<String, String> keys;
		keys = dlg.node.getKeys();

		if (!dlg.panelMain.chanButton.isSelected())
			dlg.panelMain.chanButton.doClick();
		if (!dlg.panelMain.panelChan.safeWaterButton.isSelected())
			dlg.panelMain.panelChan.safeWaterButton.doClick();

		if (keys.containsKey("name"))
			setName(keys.get("name"));

		if (keys.containsKey("seamark:name"))
			setName(keys.get("seamark:name"));

		if (keys.containsKey("seamark:buoy_safe_water:name"))
			setName(keys.get("seamark:buoy_safe_water:name"));
		else if (keys.containsKey("seamark:beacon_safe_water:name"))
			setName(keys.get("seamark:beacon_safe_water:name"));
		else if (keys.containsKey("seamark:light_float:name"))
			setName(keys.get("seamark:light_float:name"));

		/*
		 * 
		 * setBuoyIndex(SAFE_WATER); setColour(SeaMark.RED_WHITE);
		 * setLightColour("W");
		 * setRegion(Main.pref.get("tomsplugin.IALA").equals("B"));
		 * 
		 * if (keys.containsKey("seamark:buoy_safe_water:shape")) { str =
		 * keys.get("seamark:buoy_safe_water:shape");
		 * 
		 * if (str.equals("pillar")) setStyleIndex(SAFE_PILLAR); else if
		 * (str.equals("spar")) setStyleIndex(SAFE_SPAR); else if
		 * (str.equals("sphere")) setStyleIndex(SAFE_SPHERE); } else if
		 * ((keys.containsKey("seamark:type")) &&
		 * (keys.get("seamark:type").equals("light_float"))) {
		 * setStyleIndex(SAFE_FLOAT); } else if ((keys.containsKey("seamark:type"))
		 * && (keys.get("seamark:type").equals("beacon_safe_water"))) {
		 * setStyleIndex(SAFE_BEACON); }
		 * 
		 * if (getStyleIndex() >= dlg.cbM01StyleOfMark.getItemCount())
		 * setStyleIndex(0);
		 * 
		 * if (keys.containsKey("seamark:topmark:shape") ||
		 * keys.containsKey("seamark:topmark:colour")) { setTopMark(true); }
		 * 
		 * refreshLights(); parseLights(keys); parseFogRadar(keys);
		 * 
		 * dlg.cbM01StyleOfMark.setSelectedIndex(getStyleIndex());
		 * dlg.tfM01Name.setText(getName());
		 * dlg.cM01TopMark.setSelected(hasTopMark());
		 */}

	/*
	 * public void refreshLights() { dlg.cbM01Kennung.removeAllItems();
	 * dlg.cbM01Kennung.addItem(Messages.getString("SmpDialogAction.212"));
	 * dlg.cbM01Kennung.addItem("Iso"); dlg.cbM01Kennung.addItem("Oc");
	 * dlg.cbM01Kennung.addItem("LFl"); dlg.cbM01Kennung.addItem("Mo");
	 * dlg.cbM01Kennung.setSelectedIndex(0); }
	 * 
	 * public void setLightColour() { super.setLightColour("W"); }
	 */
	public void paintSign() {

		String image = "/images/Safe_Water";

		switch (getShape()) {
		case PILLAR:
			image += "_Pillar";
			break;
		case SPAR:
			image += "_Spar";
			break;
		case SPHERE:
			image += "_Sphere";
			break;
		case BEACON:
			image += "_Beacon";
			break;
		case FLOAT:
			image += "_Float";
			break;
		default:
		}

		if (!image.equals("/images/Safe_Water")) {
			image += ".png";
			dlg.panelMain.shapeIcon.setIcon(new ImageIcon(getClass().getResource(image)));
			if (hasTopMark()) {
				image = "";
				switch (getShape()) {
				case PILLAR:
				case SPAR:
					image = "/images/Top_Sphere_Red_Buoy.png";
					break;
				case SPHERE:
					image = "/images/Top_Sphere_Red_Buoy_Small.png";
					break;
				case BEACON:
					image = "/images/Top_Sphere_Red_Beacon.png";
					break;
				case FLOAT:
					image = "/images/Top_Sphere_Red_Float.png";
					break;
				}
				if (!image.isEmpty())
					dlg.panelMain.topIcon.setIcon(new ImageIcon(getClass().getResource(image)));
			} else
				dlg.panelMain.topIcon.setIcon(null);
		} else {
			dlg.panelMain.shapeIcon.setIcon(null);
			dlg.panelMain.topIcon.setIcon(null);
		}
		super.paintSign();
	}

	public void saveSign() {
		if (dlg.node == null)
			return;
		else
			super.saveSign();

		switch (getShape()) {
		case PILLAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_safe_water"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_safe_water:shape", "pillar"));
			break;
		case SPAR:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_safe_water"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_safe_water:shape", "spar"));
			break;
		case SPHERE:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "buoy_safe_water"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_safe_water:shape", "sphere"));
			break;
		case BEACON:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "beacon_safe_water"));
			break;
		case FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:type", "light_float"));
			break;
		default:
		}

		switch (getShape()) {
		case PILLAR:
		case SPAR:
		case SPHERE:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_safe_water:colour_pattern", "vertical stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:buoy_safe_water:colour", "red;white"));
			break;
		case BEACON:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_safe_water:colour_pattern", "vertical stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:beacon_safe_water:colour", "red;white"));
			break;
		case FLOAT:
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour_pattern", "vertical stripes"));
			Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:light_float:colour", "red;white"));
			break;
		default:
		}
		Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:topmark:shape", "sphere"));
		Main.main.undoRedo.add(new ChangePropertyCommand(dlg.node, "seamark:topmark:colour", "red"));
	}
}
