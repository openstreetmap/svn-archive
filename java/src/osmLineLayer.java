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


import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.event.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.*;


public class osmLineLayer extends Layer
{

  osmServerClient osc;
  protected OMGraphicList graphics;
  osmAppletLineDrawListener oLDL;
  osmDisplay od;
  boolean bStartingUp = false;

  
  public osmLineLayer(osmDisplay oDisplay)
  {

    super();

    od = oDisplay;
    osc = od.getServerClient();
    oLDL = new osmAppletLineDrawListener(od,this); 

    graphics = new OMGraphicList(4);

    createGraphics();

    //graphics.add( new OMLine(51.526394f,-0.14697807f,51.529114f,-0.15060599f,
    //   com.bbn.openmap.omGraphics.geom.BasicGeometry.LINETYPE_STRAIGHT
    //   ));

  } // osmPointsLayer


  public void setProperties(String prefix, java.util.Properties props) {

    super.setProperties(prefix, props);

  } // setProperties


  public void projectionChanged(com.bbn.openmap.event.ProjectionEvent pe) {
    Projection proj = setProjection(pe);
    if (proj != null) {

      createGraphics();

      graphics.generate(pe.getProjection());
      
      repaint();
    }

    fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
  }



  protected void createGraphics()
  {
    // NOTE: all this is very non-optimized...

    graphics.clear();

    osmStreetSegment oml;

    Projection proj = getProjection(); 

    if( proj != null )
    {

      Vector v = new Vector();

      if( proj!= null && !od.startingUp() )
      {
        LatLonPoint a = proj.getUpperLeft();
        LatLonPoint b = proj.getLowerRight();
        
        v = osc.getStreets(a,b);
      }

      Enumeration e = v.elements();

      System.out.println("reading streets...");
      
      while( e.hasMoreElements() )
      {
        
        int id = ((Integer)e.nextElement()).intValue();
        
        float lon1 = (float)((Double)e.nextElement()).doubleValue();
        float lat1 = (float)((Double)e.nextElement()).doubleValue();
        float lon2 = (float)((Double)e.nextElement()).doubleValue();
        float lat2 = (float)((Double)e.nextElement()).doubleValue();

        System.out.println("adding street " + lon1 + "," + lat1 + " " + lon2 + "," + lat2);

        oml = new osmStreetSegment(lat1, lon1, lat2, lon2,
            com.bbn.openmap.omGraphics.geom.BasicGeometry.LINETYPE_STRAIGHT,
            id
            );


        graphics.add(oml);
      }

    }
  } // createGraphics


  public void paint (Graphics g) {

    graphics.render(g);

  } // paint


  public void setMouseListen(boolean bYesNo)
  {
    oLDL.setMouseListen(bYesNo);

  } // setMouseListen


  public MapMouseListener getMapMouseListener() {

    System.out.println("asked for maplistener");
    return oLDL;

  }


  public LatLonPoint findClosestLineEnding(LatLonPoint p)
  {
    // return the nearest endpoint
    // if too far return the given point
    
    Iterator i = graphics.iterator();

    double cx = 0;
    double cy = 0;

    double d = 0.00025;
    
    while(i.hasNext())
    {

      OMLine oml = (OMLine)i.next();

      float pos[] = oml.getLL();
      

      if( distance(pos[0], pos[1], p.getLatitude(), p.getLongitude()) < 
          distance(cx,cy,p.getLatitude(), p.getLongitude()))
      {
        cx = pos[0];
        cy = pos[1];

      }

      if( distance(pos[2], pos[3], p.getLatitude(), p.getLongitude()) <
          distance(cx,cy,p.getLatitude(), p.getLongitude()))
      {
        cx = pos[2];

        cy = pos[3];

      }


    }

    if( cx != 0 && cy != 0 && 
        distance(cx,cy,p.getLatitude(), p.getLongitude()) <d
      )
    {
    
      return new LatLonPoint(cx,cy);

    }

    return p;

  } // findClosestLineEnding




  private int findUidOfLineWithPoint(LatLonPoint p)
  {

    Iterator i = graphics.iterator();

    while( i.hasNext())
    {

      osmStreetSegment oms = (osmStreetSegment)i.next();

      float pos[] = oms.getLL();

      if( (pos[0] == p.getLatitude() && pos[1] == p.getLongitude() )
          || (pos[2] == p.getLatitude() && pos[3] == p.getLongitude() )
        )
      {
        // found a line ending at that point

        return oms.getUid();


      }


    }

    return -1; // not found

  } // findUidOfLineWithPoint


  
  private double distance(double x1, double y1, double x2, double y2)
  {

    double a = Math.sqrt( Math.pow(x1-x2,2) + Math.pow(y1-y2,2));

    return a;
  } // distance




  public void setLine(LatLonPoint a, LatLonPoint b)
  {
    if( !od.checkLogin() )
    {
      // not logged in

      return;

    }


    System.out.println("trying to adding line  "+
        +a.getLatitude()+","
        +a.getLongitude() + " "
        +b.getLatitude() + ","
        +b.getLongitude());

    int uid = findUidOfLineWithPoint(a);

    if(uid == -1)
    {
      uid = findUidOfLineWithPoint(b);

      
    }

    boolean bSQLSuccess = false;

    if(uid == -1)
    {
      // not attached to an existing line, create a new one

      int i = osc.addNewStreet(
          "",
          a.getLatitude(),
          a.getLongitude(),
          b.getLatitude(),
          b.getLongitude()
          );

      if( i!= -1)
      {
        uid = i;
        bSQLSuccess = true;

      }
    }
    else
    {
      // attached to an existing line. yay.

      bSQLSuccess = osc.addStreetSegment(
          uid,
          a.getLatitude(),
          a.getLongitude(),
          b.getLatitude(),
          b.getLongitude()
          );
    }

    if( bSQLSuccess )
    {
      // one of the sql queries worked so add it to our private list
      // if it got added to the database then add it to our list too

      osmStreetSegment l = new osmStreetSegment(
          a.getLatitude(),
          a.getLongitude(),
          b.getLatitude(),
          b.getLongitude(),
          com.bbn.openmap.omGraphics.geom.BasicGeometry.LINETYPE_STRAIGHT,
          uid
          );

      graphics.add( l);

      graphics.generate( getProjection(), true);

      repaint();

      System.out.println(graphics.size());

      od.paintBean();
    }
  } // setLine


} // osmLineLayer
