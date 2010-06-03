package touchscreenhelper;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class TouchScreenHelperPlugin extends Plugin {
	public TouchScreenHelperPlugin(PluginInformation info) {
		super(info);
	}
	@Override public void mapFrameInitialized(MapFrame oldFrame,
	    MapFrame newFrame) {
		if (oldFrame == null && newFrame != null) {
			Main.map.addMapMode(new IconToggleButton(new BrowseAction(Main.map)));
		}
	}
}
