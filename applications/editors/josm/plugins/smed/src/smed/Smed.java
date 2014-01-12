package smed;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class Smed extends Plugin {

	SmedAction dialog = new SmedAction();
	
	public Smed(PluginInformation info) {
		super(info);
		MainMenu.add(Main.main.menu.toolsMenu, dialog);
	}
	
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (newFrame == null) {
			dialog.closeDialog();
		}
	}
}
