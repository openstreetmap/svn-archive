
package org.openstreetmap.applet;

import java.util.*;
import java.lang.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.LatLonPoint;
import java.awt.event.*;

public class osmAppletLineDrawListener extends MapMouseAdapter
{

  osmDisplay osmD;
  osmLineLayer osmLL;
  
  int x1 = 0;
  int y1 = 0;
  LatLonPoint pPressed = new LatLonPoint();
  LatLonPoint pReleased = new LatLonPoint();

  boolean bMouseDown = false;

  boolean bHasMouseBeenDown = false;
  boolean bCatchEvents = false;

  public osmAppletLineDrawListener(osmDisplay od, osmLineLayer oll)
  {
    System.out.println("osmappletLineDrawListener instantiated");
    osmD = od;
    osmLL = oll;

  } // osmAppletMouseListener



  public LatLonPoint getFirst()
  {
    return pPressed;

  } // getTopLeft

  
  public LatLonPoint getSecond()
  {
    return pReleased;
    
  } // getBottomRight
  


  public String[] getMouseModeServiceList()
  {
    System.out.println("asked for service list!!!!!");

    return new String[] { SelectMouseMode.modeID, NavMouseMode.modeID };

  } // getMouseModeServiceList



  public boolean mousePressed(java.awt.event.MouseEvent e)
  {
    if( !bCatchEvents )
    {
      return false;

    }
    bMouseDown = true;

    MapMouseEvent mme = (MapMouseEvent)e;
    LatLonPoint p = mme.getLatLon();


    System.out.println("map pressed at " + p.getLatitude() + "," +  p.getLongitude());

    pPressed = osmLL.findClosestLineEnding(p);
    return true;
  } 



  public void mouseMoved()
  {

  }

  public boolean mouseMoved(java.awt.event.MouseEvent e)
  {
    if( !bCatchEvents )
    {
      return false;

    }

    return true;

  } 

  public boolean mouseClicked(java.awt.event.MouseEvent e)
  {
    if( !bCatchEvents )
    {
      return false;

    }

    return true;

  } 

  public void setMouseListen(boolean bYesNo)
  {
    bCatchEvents = bYesNo;

  } // setMouseListen

  
  public boolean mouseDragged(java.awt.event.MouseEvent e)
  {
  
    if( !bCatchEvents )
    {
      return false;

    }


    if( bMouseDown )
    {
      MapMouseEvent m = (MapMouseEvent)e;
    
      osmD.getSelectLayer().setLine(pPressed, m.getLatLon());

    }

    return true;

  } 
  

  public void mouseEntered(java.awt.event.MouseEvent e)
  {

  } 

  
  public void mouseExited(java.awt.event.MouseEvent e)
  {

  } 

  
 
  public boolean mouseReleased(java.awt.event.MouseEvent e)
  {
  
    if( !bCatchEvents )
    {
      return false;

    }

    bMouseDown = false;
    bHasMouseBeenDown = true;
    
    MapMouseEvent mme = (MapMouseEvent)e;
    pReleased = osmLL.findClosestLineEnding(mme.getLatLon());

    System.out.println("map released at " + pReleased.getLatitude() + "," +  pReleased.getLongitude());

    if( !pPressed.equals(pReleased) )
    {
      osmLL.setLine(pPressed, pReleased);
    }
    
    return true;
    
  }

  
  public boolean hasMouseBeenDown()
  {
    return bHasMouseBeenDown;

  } // hasMouseBeenDown

} // osmAppletMouseListener
