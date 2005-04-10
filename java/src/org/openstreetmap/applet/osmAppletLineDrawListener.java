
package org.openstreetmap.applet;

import java.util.*;
import java.lang.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.LatLonPoint;
import java.awt.event.*;

public class osmAppletLineDrawListener extends MapMouseAdapter
{
  public static final int MODE_ADD_NODE = 0;
  public static final int MODE_MOVE_NODE = 1;
  public static final int MODE_DELETE_NODE = 2;
  public static final int MODE_NEW_LINE = 3;

  private int CURRENT_MODE = 0;
  private osmDisplay osmD;
  private osmLineLayer osmLL;
  
  private boolean bMouseDown = false;

  private boolean bHasMouseBeenDown = false;
  private boolean bCatchEvents = false;

  public osmAppletLineDrawListener(osmDisplay od, osmLineLayer oll)
  {
    System.out.println("osmappletLineDrawListener instantiated");
    osmD = od;
    osmLL = oll;

  } // osmAppletMouseListener

  public void setMode(int nMode)
  {
    CURRENT_MODE = nMode;

  }


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


    System.out.println("map pressed at " + p.getLatitude() + "," +  p.getLongitude() + " in mode " + CURRENT_MODE);

    if( CURRENT_MODE == MODE_MOVE_NODE) 
    {

      osmLL.moveNode(p);
      

    }

    if( CURRENT_MODE == MODE_NEW_LINE)
    {
      if(osmLL.newLine(p))
      {
        osmD.getSelectLayer().setLineStart(p);
      }

    }

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


    if( bMouseDown && CURRENT_MODE == MODE_MOVE_NODE)
    {
      MapMouseEvent m = (MapMouseEvent)e;

      osmD.getSelectLayer().setNode(m.getLatLon());

    }

    if( bMouseDown && CURRENT_MODE == MODE_NEW_LINE)
    {
     
      MapMouseEvent m = (MapMouseEvent)e;

      osmD.getSelectLayer().setLine(m.getLatLon());
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

    switch(CURRENT_MODE)
    {
      case MODE_ADD_NODE:
        osmLL.addNode(mme.getLatLon());
        break;

      case MODE_MOVE_NODE:
        osmLL.moveNode(mme.getLatLon());
        osmD.getSelectLayer().clearLayer();
        break;

      case MODE_DELETE_NODE:
        osmLL.deleteNode(mme.getLatLon());
        break;

      case MODE_NEW_LINE:
        osmLL.newLine(mme.getLatLon());
        osmD.getSelectLayer().clearLayer();
        break;

    }

    return true;

  }


  public boolean hasMouseBeenDown()
  {
    return bHasMouseBeenDown;

  } // hasMouseBeenDown

} // osmAppletMouseListener
