public class OSM_segment {
    int ID;
    double lat,lon;
    String name;
    String creator;

    public void set_ID(int ID_in) {
	ID = ID_in;
    }

    public void set_lat(double lat_in) {
	lat = lat_in;
    }

    public void set_lon(double lon_in) {
	lon = lon_in;
    }

    public void set_name(String name_in) {
	//System.out.println("segment set name="+name_in);
	name = name_in;
    }
    
    public int get_ID() {
	return ID;
    }

    public double get_lat() {
	return lat;
    }

    public double get_lon() {
	return lon;
    }

    public String get_name() {
	//System.out.println("segment get name:"+name);
	return name;
    }

    public void dump(int code) {
	System.err.println("SEGMENT ID: "+ ID);
	System.err.println("SEGMENT code: "+ code);
    }
}
