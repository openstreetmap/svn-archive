import java.util.*;
import java.lang.*;
import java.awt.geom.*;

public class gpsCoord
{
  
  public gpsCoord()
  {

  }
  
  
  public AffineTransform getTransform(Vector gpsPoints,
                                      int screenSize)
  {

    Enumeration e = gpsPoints.elements();

    gpspoint p = (gpspoint)e.nextElement();
    
    float minLon = p.getLongitude();
    float maxLon = p.getLongitude();

    float minLat = p.getLatitude();
    float maxLat = p.getLatitude();
    

    while(e.hasMoreElements())
    {

      p = (gpspoint)e.nextElement();

      
      if( p.getLongitude() > maxLon )
      {
        maxLon = p.getLongitude();
      }

      if( p.getLongitude() < minLon )
      {
        minLon = p.getLongitude();
      }

      
      
      if( p.getLatitude() > maxLat )
      {
        maxLat = p.getLatitude();
      }

      if( p.getLatitude() < minLat )
      {
        minLat = p.getLatitude();
      }

    }


    AffineTransform t = new AffineTransform();
   

    // translate to the middle of the data

    t.translate( ((float)screenSize /2) -  ((maxLon + minLon) / 2),
                 ((float)screenSize /2) -  ((maxLat + minLat) / 2));



    t.scale(3, -70);

    

    return t;
    // flip the y as lat increases upward not downward
    
//    t = t.getScaleInstance(1,-1); 


    

      
    // lat is y, lon is x

    

  } // gpsPoints

} // gpsCoord
