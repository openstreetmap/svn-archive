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


public class osmLineLayer extends Layer
{
  private int nodeSelected = -1;

  private osmServerClient osc;
  protected OMGraphicList nodeGraphics;
  protected OMGraphicList lineGraphics;
  private osmAppletLineDrawListener oLDL;
  private osmDisplay od;
  private boolean bStartingUp = false;
  private Hashtable htNodes = new Hashtable();

  public osmAppletLineDrawListener getMouseListener()
  {
    return oLDL;
  } // getMouseListener

  public osmLineLayer(osmDisplay oDisplay)
  {

    super();

    od = oDisplay;
    osc = od.getServerClient();
    oLDL = new osmAppletLineDrawListener(od,this); 

    nodeGraphics = new OMGraphicList(4);
    lineGraphics = new OMGraphicList(4);

    //createGraphics();

    //graphics.add( new OMLine(51.526394f,-0.14697807f,51.529114f,-0.15060599f,
    //   com.bbn.openmap.omGraphics.geom.BasicGeometry.LINETYPE_STRAIGHT
    //   ));

  } // osmPointsLayer


  public void setProperties(String prefix, java.util.Properties props)
  {

    super.setProperties(prefix, props);

  } // setProperties


  public void projectionChanged(com.bbn.openmap.event.ProjectionEvent pe)
  {


    Projection proj = setProjection(pe);


    System.out.println("proj change on line layer to" + proj);

    if (proj != null) {

      System.out.println("projection changed...");
      createGraphicsAndRepaint();

    }


    fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
  }



  protected void createGraphicsAndRepaint()
  {
    System.out.println("line createGraphics called");
    // NOTE: all this is very non-optimized...

    nodeGraphics.clear();
    lineGraphics.clear();

    osmStreetSegment oml;

    Projection proj = getProjection(); 

    htNodes = new Hashtable();
    Vector v = new Vector();

    if(  !od.startingUp() )
    {
      LatLonPoint a = proj.getUpperLeft();
      LatLonPoint b = proj.getLowerRight();

      System.out.println(a);
      System.out.println(b);

      htNodes = osc.getNodes(a,b);

      v = osc.getLines(htNodes);
    }
    // do lines

    Enumeration e = v.elements();

    while( e.hasMoreElements() )
    {
      OMLine l = (OMLine)e.nextElement();

      lineGraphics.add(l);

    }

    // do nodes

    e = htNodes.elements();

    System.out.println("reading streets...");

    while( e.hasMoreElements() )
    {
      Node n = (Node)e.nextElement();
      System.out.println("got a node: " + n);

      nodeGraphics.add( n );

    }

    nodeGraphics.generate(getProjection());
    lineGraphics.generate(getProjection());

    repaint();

  } // createGraphics



  public void paint (Graphics g)
  {

    lineGraphics.render(g);
    nodeGraphics.render(g);

  } // paint


  public void setMouseListen(boolean bYesNo)
  {
    oLDL.setMouseListen(bYesNo);

  } // setMouseListen


  public MapMouseListener getMapMouseListener() {

    System.out.println("asked for maplistener");
    return oLDL;

  }

  /*
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
*/



public void addNode(LatLonPoint p)
{
  if( !od.checkLogin() )
  {
    // not logged in

    return;

  }


  System.out.println("trying to add node  " +p.getLatitude() + "," + p.getLongitude());



  int uid = osc.addNode(
      (double)p.getLatitude(),
      (double)p.getLongitude()
      );

  if( uid != -1 )
  {
    Node n = new Node(uid, (double)p.getLatitude(), (double)p.getLongitude());

    htNodes.put("" + n.getUID(), n);

    nodeGraphics.add( n );

    nodeGraphics.generate( getProjection(), true);


    repaint();

    od.paintBean();
  }
} // addNode



public void moveNode(LatLonPoint p)
{
  int x = (int)getProjection().forward(p).getX();
  int y = (int)getProjection().forward(p).getY();

  if( nodeSelected != -1)
  {
    System.out.println("moving node..." + nodeSelected);
    // move it


    if( osc.moveNode(
          nodeSelected,
          (double)p.getLatitude(),
          (double)p.getLongitude() ) )
    {

      createGraphicsAndRepaint();

    }

    nodeSelected = -1;


  }
  else
  {
    // find a node to move!
    OMCircle g = (OMCircle)nodeGraphics.findClosest(x,y,10);

    if( g != null)
    {
      nodeSelected = ((Node)g).getUID();
      System.out.println("selected a node! " + nodeSelected);
    }

  }

} // moveNode



public void deleteNode(LatLonPoint p)
{

  //FIXME : put up a 'r u sure?' dialog

  int x = (int)getProjection().forward(p).getX();
  int y = (int)getProjection().forward(p).getY();


  OMCircle g = (OMCircle)nodeGraphics.findClosest(x,y,10);

  if( g != null)    
  {
    osc.deleteNode(((Node)g).getUID()); 
  }

  createGraphicsAndRepaint();

} // deleteNode



public boolean newLine(LatLonPoint p)
{

  int x = (int)getProjection().forward(p).getX();
  int y = (int)getProjection().forward(p).getY();

  if( nodeSelected != -1)
  {
    System.out.println("linking node..." + nodeSelected);
    // move it

    OMCircle g = (OMCircle)nodeGraphics.findClosest(x,y,10);

    if( g != null)
    {
      int n = ((Node)g).getUID();

      if(n != -1 && n != nodeSelected)
      {

        int nLineUID = osc.newLine(
            nodeSelected,
            n);

        if( nLineUID != -1)
        {

          createGraphicsAndRepaint();

        }
      }

    }
    nodeSelected = -1;

    return false;


  }
  else
  {
    // find a node to move!
    OMCircle g = (OMCircle)nodeGraphics.findClosest(x,y,10);

    if( g != null)
    {
      nodeSelected = ((Node)g).getUID();
      System.out.println("selected a node! " + nodeSelected);
      return true;
    }

  }


  return false;

}



} // osmLineLayer
