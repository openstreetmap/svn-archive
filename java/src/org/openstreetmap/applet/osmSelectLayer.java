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

import org.openstreetmap.client.*;

public class osmSelectLayer extends Layer
{

  osmServerClient osc;;
  protected OMGraphicList graphics;

  osmDisplay od;

  int x1 = 0;
  int y1 = 0;
  int x2 = 0;
  int y2 = 0;
  
  boolean bDisplayRect = false;
  boolean bDisplayLine = false;
 
  
  public osmSelectLayer(osmDisplay oDisplay)
  {
    super();

    od = oDisplay;
    graphics = new OMGraphicList(4);
    

  } // osmPointsLayer


  public void setProperties(String prefix, java.util.Properties props) {

    super.setProperties(prefix, props);

  } // setProperties

  

 
  public void setRect(int xa, int ya, int xb, int yb)
  {
    graphics.clear();
    
    OMRect r = new OMRect(xa,ya,xb,yb);
    
    r.generate( getProjection());

    graphics.add(r);

    
    repaint();

  } // setRect

  
 
  public void setLine(LatLonPoint a, LatLonPoint b)
  {
    graphics.clear();
    
    OMLine l = new OMLine(
        a.getLatitude(),
        a.getLongitude(),
        b.getLatitude(),
        b.getLongitude(),
        com.bbn.openmap.omGraphics.geom.BasicGeometry.LINETYPE_STRAIGHT
        );
    
    l.generate( getProjection());

    graphics.add(l);

    repaint();

  } // setLine

  
  

  public void projectionChanged(com.bbn.openmap.event.ProjectionEvent pe) {

    
    Projection proj = setProjection(pe);
    
    System.out.println(proj);
 //   if (proj != null) {

  
    graphics.generate(pe.getProjection());
      
    
    repaint();
   
    // }

    fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
  }


 
  
  public void paint(Graphics g)
  {
    
    graphics.render(g);

  } // paint


} // osmPointsLayer
