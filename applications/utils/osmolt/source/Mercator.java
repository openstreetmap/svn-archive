public class Mercator {
    public static Double[] merc(double x, double y) {
        return new Double[] {mercX(x), mercY(y)};
    }

    private static double  mercX(double lon) {
        return lon*20037508.34 / 180;
    }
    
    private static double mercY(double lat) {
    	return (Math.log( Math.tan( (90 + lat) * Math.PI / 360) ) / (Math.PI / 180) )* 20037508.34 / 180;
    }
}