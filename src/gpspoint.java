import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;

public class gpspoint{

  private float m_lat;
  private float m_long;
  private float m_altitude;
  private long m_time;

  private boolean bHighlighted = false;
  
  private gpspoint gpsPrevious;
  private gpspoint gpsNext;
  
  public gpspoint(float x,
                  float y,
                  float a,
                  long millis)
  {
    
    m_lat = x;
    m_long = y;
    m_altitude = a;
    m_time = millis;

  } // gpspoint

  
  public gpspoint(String x,
                  String y,
                  String a,
                  String millis)
  {

    m_lat = Float.parseFloat(x);
    m_long = Float.parseFloat(y);
    m_altitude = Float.parseFloat(a);
    m_time = Long.parseLong(millis);
  } // gpspoint

  

  public void setHighlight(boolean bYesNo)
  {

      bHighlighted = bYesNo;
      
  } // setHighlight

  public boolean getHighlight()
  {

    return bHighlighted;

  } // getHighlight

  public float getLongitude()
  {
    return m_long;
  } // getLongitude

  public float getLatitude()
  {
    return m_lat;
  } // getLatitude

  public float getAltitude()
  {
    return m_altitude;
  } // getAltitude
  
  public long getTime()
  {
    return m_time;
  } // getTime



  public void setGPSPrevious(gpspoint somePoint)
  {
    gpsPrevious = somePoint;
    
  } // setGPSPrevious


  public void setGPSNext(gpspoint somePoint)
  {
    gpsNext = somePoint;

  } // getGPSPrevious


  public gpspoint getGPSPrevious()
  {
    return gpsPrevious;
    
  }


  public gpspoint getGPSNext()
  {
    
    return gpsNext;
    
  } // gpspoint
  
  
  public String toString()
  {
    return m_lat + " " + m_long + " " + m_altitude + " " + m_time;

  } // toString


  public void paintPoint(Graphics g,
                         int nOffsetX,
                         int nOffsetY,
                         AffineTransform at)
  {

    //FIXME combing offsets and the transform
    //
    
    Point2D.Float p1 = new Point2D.Float();
    Point2D.Float p2 = new Point2D.Float();


    p1.setLocation(getLongitude(), getLatitude());

    at.transform(p1,p2);

    if(bHighlighted)
    {
      g.setColor(Color.red);
      
      g.fillOval(nOffsetX + (int)p2.getX() - 10,
                 nOffsetY + (int)p2.getY() - 10,
                 20,
                 20);


      g.setColor(Color.black);

    }

    g.drawLine(nOffsetX + (int)p2.getX(),
               nOffsetY + (int)p2.getY(),
               nOffsetX + (int)p2.getX(),
               nOffsetY + (int)p2.getY());


  } // paintPoint

} // gpspoint
