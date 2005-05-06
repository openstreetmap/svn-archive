
import java.util.Vector;
import java.io.PrintWriter;

class Waypoints
{
	Vector waypoints;

	public Waypoints() { waypoints = new Vector(); }
	public void addWaypoint(Waypoint wp) { waypoints.add(wp);}
//	void toGPX(std::ostream&);
	public int size() { return waypoints.size(); }
	public boolean alterWaypoint(int idx, String newName, String newType)
	{
		if(idx>=0 && idx<waypoints.size())
		{
			Waypoint wp=(Waypoint)waypoints.elementAt(idx);
			wp.alter(newName, newType);
			return true;
		}
		return false;
	}

	public Waypoint getWaypoint(int i) 
	{ 
		if(i<0 || i>=waypoints.size())
			return null;	
	
 		return (Waypoint)waypoints.elementAt(i);
	}

	public void toGPX(PrintWriter pw)
	{
		for(int count=0; count<waypoints.size(); count++)
		{
			Waypoint wp=(Waypoint)waypoints.elementAt(count);

			pw.println( "<wpt lat=\"" + wp.getLat() + 
				"\" lon=\"" + wp.getLon()+ "\">");
			pw.println("<name>"+wp.getName()+"</name>");
			pw.println("<type>"+wp.getType()+"</type>");
			pw.println("</wpt>");
		}
	}
}

