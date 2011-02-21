import java.util.Map;


public class Node extends Entity{
	
	double lon, lat;
	
	public Node(long id, double lat, double lon, int version, int changeset, int uid, String user, String timestamp) {
		super(id, version, changeset, uid, user, timestamp);
		this.lat = lat;
		this.lon = lon;
	}
	
}
