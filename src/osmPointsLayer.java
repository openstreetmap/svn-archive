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

  protected OMGraphicList graphics;

  Projection proj;
  
  public osmPointsLayer()
  {  
    super();
    graphics = new OMGraphicList(2000);
    createGraphics(graphics);

  } // osmPointsLayer



  public void setProperties(String prefix, java.util.Properties props) {

    super.setProperties(prefix, props);

  } // setProperties



  public void projectionChanged (ProjectionEvent e) {

    System.out.println("projection changed");
    
    proj = e.getProjection();
  
    // probably better to empty the list rather than create a new one?
    graphics = new OMGraphicList(2000);
    
    createGraphics(graphics);

    graphics.generate(e.getProjection());

    repaint();

  } // projectionChanged



  public void paint (Graphics g) {

    graphics.render(g);

  } // paint



  protected void createGraphics (OMGraphicList list)
  {
    // NOTE: all this is very non-optimized...

    OMCircle omc;

//    Projection proj = getProjection(); 

    if( proj != null )
    {
      osmServerClient osc = new osmServerClient();

      Vector v = osc.getPoints(proj.getUpperLeft(),
          proj.getLowerRight());

      Enumeration e = v.elements();

      while( e.hasMoreElements() )
      {
        gpspoint p = (gpspoint)e.nextElement();

        omc = new OMCircle( p.getLatitude(),
            p.getLongitude(),
            5f,
            com.bbn.openmap.proj.Length.METER
            );
        omc.setLinePaint(Color.black);
        omc.setFillPaint(OMGraphic.clear);

        list.add(omc);
      }

    }
  }





} // osmPointsLayer
