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


public class osmPointsLayer extends Layer
{

  osmServerClient osc;;
  protected OMGraphicList graphics;
  private boolean bStartingUp = true;
  osmAppletMouseListener osmAML;
  Projection proj;

  public osmPointsLayer(osmDisplay od)
  {
    super();
    osc = od.getServerClient();

    
    osmAML = new osmAppletMouseListener(od, this);
    graphics = new OMGraphicList(10000);
    createGraphics(graphics);

  } // osmPointsLayer

  
  public void setStartingUp(boolean bYesNo)
  {
    bStartingUp = bYesNo;

  } // setStartingUp


  public void setProperties(String prefix, java.util.Properties props) {

    super.setProperties(prefix, props);

  } // setProperties


  public void projectionChanged (ProjectionEvent e) {

    //Projection proj = setProjection(e);
    System.out.println("projection changed to ");

    //if (proj != null) {

      proj = e.getProjection();

      System.out.println("scale is " + proj.getScale());
      System.out.println("center is " + proj.getCenter());
      
      graphics.clear();

      createGraphics(graphics);

      graphics.generate(proj);

      repaint();
    //}
    fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
  } // projectionChanged



  public void paint (Graphics g) {

    graphics.render(g);

  } // paint


  public void select(LatLonPoint a, LatLonPoint b)
  {
    Iterator i = graphics.iterator();

    while( i.hasNext() )
    {
      OMCircle omc = (OMCircle)i.next();

      LatLonPoint llp = omc.getLatLon();

      if( 
          llp.getLatitude() < a.getLatitude() &&
          llp.getLatitude() > b.getLatitude() &&
          llp.getLongitude() > a.getLongitude() &&
          llp.getLongitude() < b.getLongitude()
        )
      {

        omc.select();
      }
      else
      {
        omc.deselect();

      }



      //System.out.println(llp);



    }

    repaint();

  } // selectArea


  public void setMouseListen(boolean bYesNo)
  {
    osmAML.setMouseListen(bYesNo);

  } // setMouseListen


  public synchronized void deleteSelectedPoints()
  {
    if( !osmAML.hasMouseBeenDown() )
    {
      // mouse hasnt been down yet

      return;

    }

    LatLonPoint a = osmAML.getTopLeft();
    LatLonPoint b = osmAML.getBottomRight();

    Iterator i = graphics.iterator();

    Vector v = new Vector();

    while( i.hasNext() )
    {
      OMCircle omc = (OMCircle)i.next();

      if( omc.isSelected() )
      {
        LatLonPoint p = omc.getLatLon();


        v.add(omc);
      }

    }

    if(!
        osc.deletePointsInArea(
          (double)a.getLongitude(),
          (double)a.getLatitude(),
          (double)b.getLongitude(),
          (double)b.getLatitude())
      )
    {

      System.out.println("something went screwy dropping points");
      return;

    }



    Enumeration e = v.elements();

    while(e.hasMoreElements())
    {
      OMCircle omc = (OMCircle)e.nextElement();

      graphics.remove(omc);

    }

    repaint();

  } // deleteSelectedPoints




  protected void createGraphics (OMGraphicList list)
  {
    // NOTE: all this is very non-optimized...

    OMCircle omc;

    //    Projection proj = getProjection(); 

    if( proj != null )
    {

      Vector v = new Vector();

      if( !bStartingUp )
      {
        v = osc.getPoints(proj.getUpperLeft(),
            proj.getLowerRight());
      }

      Enumeration e = v.elements();

      while( e.hasMoreElements() )
      {
        gpspoint p = (gpspoint)e.nextElement();

        omc = new OMCircle( p.getLatitude(),
            p.getLongitude(),
            5f,
            com.bbn.openmap.proj.Length.METER
            );

        omc.setLinePaint(Color.gray);
        omc.setSelectPaint(Color.red);
        omc.setFillPaint(OMGraphic.clear);

        list.add(omc);
      }

    }
  }

  public MapMouseListener getMapMouseListener() {

    System.out.println("asked for maplistener");
    return osmAML;

  }

} // osmPointsLayer
