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

  public osmLineLayer(osmDisplay oDisplay)
  {

    super();

    od = oDisplay;
    oLDL = new osmAppletLineDrawListener(od,this); 

    graphics = new OMGraphicList(4);

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

      graphics.generate(pe.getProjection());
      
      repaint();
    }

    fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
  }



  /*
     public void projectionChanged (ProjectionEvent e) {

     graphics.generate(e.getProjection());

     repaint();

     } // projectionChanged

   */


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

  public void setLine(LatLonPoint a, LatLonPoint b)
  {
    System.out.println("adding line  "+
        +a.getLatitude()+","
        +a.getLongitude() + " "
        +b.getLatitude() + ","
        +b.getLongitude());



    OMLine l = new OMLine(
        a.getLatitude(),
        a.getLongitude(),
        b.getLatitude(),
        b.getLongitude(),

        com.bbn.openmap.omGraphics.geom.BasicGeometry.LINETYPE_STRAIGHT
        );


    graphics.add( l);

    graphics.generate( getProjection(), true);


    repaint();

    System.out.println(graphics.size());

    od.paintBean();

  } // setLine


} // osmPointsLayer
