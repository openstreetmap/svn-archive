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

    OMPoly poly;

    // H
    poly = new OMPoly(
        new float [] {
          10f, -150f, 35f, -150f,
         35f, -145f, 25f, -145f,
         25f, -135f, 35f, -135f,
         35f, -130f, 10f, -130f,
         10f, -135f, 20f, -135f,
         20f, -145f, 10f, -145f,
         10f, -150f
        },
        OMGraphic.DECIMAL_DEGREES,
        OMGraphic.LINETYPE_RHUMB, 32);
    poly.setLinePaint(Color.black);
    poly.setFillPaint(Color.green);
    list.add(poly);


  }











} // osmPointsLayer
