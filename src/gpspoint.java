import java.lang.*;
import java.util.*;
import java.awt.*;

public class gpspoint{

  private float m_lat;
  private float m_long;
  private float m_altitude;
  private long m_time;
 
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
  }
  
  public String toString()
  {
    return m_long + " " + m_lat + " " + m_altitude + " " + m_time;

  } // toString
} // gpspoint
