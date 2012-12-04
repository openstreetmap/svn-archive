package oseam;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

import oseam.OSeaMAction;

public class OSeaM extends Plugin {

	public OSeaM(PluginInformation info) {
		super(info);
        MainMenu.add(Main.main.menu.toolsMenu, new OSeaMAction());
	}

}
