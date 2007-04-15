package waypoints; 

import java.awt.Component;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;

public class WaypointPlugin extends Plugin  {


	public WaypointPlugin() {
		JMenuItem waypointItem = new JMenuItem(new WaypointOpenAction());
		int index = findFirstSeparator();
		Main.main.menu.fileMenu.add(waypointItem,index<0 ? 0: index);
	}

	private int findFirstSeparator()
	{
		Component[] components = Main.main.menu.fileMenu.getMenuComponents();
		for(int count=0; count<components.length; count++)
		{
			if(components[count] instanceof JSeparator)
				return count;
		}
		return -1;
	}
}
