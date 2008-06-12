
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

}
