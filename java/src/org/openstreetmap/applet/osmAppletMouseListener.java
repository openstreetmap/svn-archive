/*
   Copyright (C) 2004 Stephen Coast (steve@fractalus.com)

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */

package org.openstreetmap.applet;

import java.util.*;
import java.lang.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.LatLonPoint;
import java.awt.event.*;

public class osmAppletMouseListener extends MapMouseAdapter
{

  osmDisplay osmD;
  osmPointsLayer osmPL;
  osmSelectLayer selectLayer;
  int x1 = 0;
  int y1 = 0;
  LatLonPoint pPressed = new LatLonPoint();
  LatLonPoint pReleased = new LatLonPoint();

  boolean bMouseDown = false;

  boolean bHasMouseBeenDown = false;
  boolean bCatchEvents = false;

  public osmAppletMouseListener(osmDisplay od, osmPointsLayer opl)
  {
    System.out.println("osmappletmouselistener instantiated");
    osmD = od;
    osmPL = opl;

  } // osmAppletMouseListener



  public LatLonPoint getTopLeft()
  {
    return pPressed;

  } // getTopLeft

  
  public LatLonPoint getBottomRight()
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

    x1 = e.getX();
    y1 = e.getY();
    MapMouseEvent mme = (MapMouseEvent)e;
    LatLonPoint p = mme.getLatLon();

    pPressed = p;

    System.out.println("map pressed at " + p.getLatitude() + "," +  p.getLongitude());

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
    System.out.println("select points mouse listener told " + bYesNo);
    bCatchEvents = bYesNo;

  } // setMouseListen


  public boolean mouseDragged(java.awt.event.MouseEvent e)
  {
    if( !bCatchEvents )
    {
      return false;

    }

    System.out.println("mouse dragged with mousedown:" + bMouseDown);

    if( bMouseDown )
    {

      osmD.getSelectLayer().setRect(x1,y1,e.getX(), e.getY());

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
    pReleased = mme.getLatLon();

    System.out.println("map released at " + pReleased.getLatitude() + "," +  pReleased.getLongitude());

    System.out.println("calling osmPL.select...");
    osmPL.select(pPressed, pReleased);

    return true;

  } 

  
  public boolean hasMouseBeenDown()
  {
    return bHasMouseBeenDown;

  } // hasMouseBeenDown
  




} // osmAppletMouseListener
