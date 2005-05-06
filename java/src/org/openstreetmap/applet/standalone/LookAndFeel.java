import java.util.HashMap;
import java.awt.Color;
import javax.swing.ImageIcon;

class LookAndFeel
{
	static HashMap segmentColours, waypointIcons;
   
	static
	{

		segmentColours = new HashMap();
		waypointIcons = new HashMap();

		segmentColours.put("track",Color.gray);
		segmentColours.put("footpath",Color.green);
		segmentColours.put("cycle path",Color.magenta);
		segmentColours.put("bridleway",Color.orange);
		segmentColours.put("byway",Color.red);
		segmentColours.put("minor road",Color.black);
		segmentColours.put("B road",Color.black);
		segmentColours.put("A road",Color.black);

		waypointIcons.put("waypoint",new ImageIcon("waypoint.png"));
		waypointIcons.put("pub",new ImageIcon("pub.png"));
		waypointIcons.put("church",new ImageIcon("church.png"));
		waypointIcons.put("viewpoint",new ImageIcon("viewpoint.png"));
		waypointIcons.put("farm",new ImageIcon("farm.png"));
		waypointIcons.put("summit",new ImageIcon("peak.png"));
		waypointIcons.put("hamlet",new ImageIcon("place.png"));
		waypointIcons.put("village",new ImageIcon("place.png"));
		waypointIcons.put("small town",new ImageIcon("place.png"));
		waypointIcons.put("large town",new ImageIcon("place.png"));
		waypointIcons.put("car park",new ImageIcon("carpark.png"));
		waypointIcons.put("station",new ImageIcon("station.png"));
		waypointIcons.put("mast",new ImageIcon("mast.png"));
		waypointIcons.put("locality",new ImageIcon("waypoint.png"));
		waypointIcons.put("point of interest",
						new ImageIcon("interest.png"));
		waypointIcons.put("suburb",new ImageIcon("place.png"));
		waypointIcons.put("caution",new ImageIcon("caution.png"));
		waypointIcons.put("amenity",new ImageIcon("amenity.png"));
		waypointIcons.put("campsite",new ImageIcon("campsite.png"));
		waypointIcons.put("restaurant",new ImageIcon("restaurant.png"));
		waypointIcons.put("bridge",new ImageIcon("bridge.png"));
		waypointIcons.put("tunnel",new ImageIcon("tunnel.png"));
		waypointIcons.put("tea shop",new ImageIcon("teashop.png"));
		waypointIcons.put("country park",new ImageIcon("park.png"));
		waypointIcons.put("industrial area",
						new ImageIcon("industry.png"));

	}

	public static Color getColour(String type)
	{
		return (Color)segmentColours.get(type);
	}

	public static ImageIcon getImageIcon(String type)
	{
		return (ImageIcon)waypointIcons.get(type);
	}

	public static Object[] getWaypointTypes()
	{
		return waypointIcons.keySet().toArray();
	}
	public static Object[] getTrackTypes()
	{
		return segmentColours.keySet().toArray();
	}
}
