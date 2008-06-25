
// This is correct. Don't fully understand the latToY() though! :-)
public class GProjection
{


    
    public static int lonToX(double lon,int zoom)
    {

        return (int) (0.5+Math.floor( (MyMaths.pow(2,zoom+8)*(lon+180)) / 360));
    }

    public static int latToY(double lat,int zoom)
    {
        double f = Math.sin((Math.PI/180)*lat);
        
        int y = (int)(0.5+Math.floor
            (MyMaths.pow(2,zoom+7) + 0.5*MyMaths.log((1+f)/(1-f)) *                                 (-MyMaths.pow(2,zoom+8)/(2*Math.PI))));
        return y;
    } 
	public static String direction (double lon1,double lat1,
									double lon2,double lat2)
	{
		// Put any old zoom in as it doesn't matter
		double v1 = Math.tan(Math.PI/8), 
				v3=Math.tan((Math.PI/8)*3);
		int x1=lonToX(lon1,13), x2=lonToX(lon2,13), y1=latToY(lat1,13), 
			y2=latToY(lat2,13);
		System.out.println("x1=" + x1+ " y1="+y1+ " x2=" + x2 +" y2="+y2);
		double tanAngle =((double)(y1-y2)) / ((double)(x2-x1));
		System.out.println("v1="+v1+" v3="+v3);
		System.out.println("tanAngle=" + tanAngle);
		String dir;
		if(Math.abs(tanAngle)<v1)
		{
		 	dir = (x1<x2) ? "E":"W";
		}
		else if (Math.abs(tanAngle)>v3)
		{
			dir=(y1>y2) ? "N":"S";	
		}
		else
		{
			if(tanAngle>=0)
				dir= (y1>y2) ? "NE" :"SW";
			else
				dir= (y1>y2) ? "NW" :"SE";
		}

		System.out.println("Direction="+dir);
		return dir;
	}
}
