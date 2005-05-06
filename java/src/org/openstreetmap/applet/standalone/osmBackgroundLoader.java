
import java.util.*;
import java.awt.Color;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.*;

import org.openstreetmap.client.*;
import org.openstreetmap.util.gpspoint;


public class osmBackgroundLoader extends Thread
{
  OMGraphicList graphics;
  osmPointsLayer osmPL;
  LatLonPoint llTopLeft;
  LatLonPoint llBotRight;
  osmServerClient osc;
  
  public osmBackgroundLoader(osmServerClient o, OMGraphicList list, osmPointsLayer op, LatLonPoint tl, LatLonPoint br)
  {
    graphics = list;
    osmPL = op;
    llTopLeft = tl;
    llBotRight = br;
    osc = o;

  } // osmPointsLayer


  public void run()
  {
    System.out.println("background loader started!");

    OMCircle omc;

    long lLastTime = System.currentTimeMillis();


    Vector v = new Vector();
    

    v = osc.getPoints(llTopLeft, llBotRight);

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

      graphics.add(omc);

      if(lLastTime + 1000 < System.currentTimeMillis() )
      {
        lLastTime = System.currentTimeMillis();
        osmPL.fireBackgroundRedraw();

      }
    }

    osmPL.fireBackgroundRedraw();
  }
} // osmBackGroundLoader
