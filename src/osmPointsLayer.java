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

  public osmPointsLayer()
  {  
    super();
    graphics = new OMGraphicList(10);
    createGraphics(graphics);

  } // osmPointsLayer



  public void setProperties(String prefix, java.util.Properties props) {
    super.setProperties(prefix, props);
  }

  public void projectionChanged (ProjectionEvent e) {
    graphics.generate(e.getProjection());
    repaint();
  }



  public void paint (Graphics g) {
    graphics.render(g);
  }


  protected void createGraphics (OMGraphicList list) {
    // NOTE: all this is very non-optimized...

    OMCircle omc;

    // H

    osmServerClient osc = new osmServerClient();

    Vector v = osc.getPoints();

    Enumeration e = v.elements();

    while( list.size() < 1000 &&  e.hasMoreElements() )
    {
      gpspoint p = (gpspoint)e.nextElement();

      omc = new OMCircle( p.getLatitude(),
          p.getLongitude(),
          100000f,
          com.bbn.openmap.proj.Length.METER
          );
      omc.setLinePaint(Color.black);
      omc.setFillPaint(OMGraphic.clear);

      list.add(omc);
    }


  }





} // osmPointsLayer
