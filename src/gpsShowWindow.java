import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


public class gpsShowWindow extends JFrame{

  Vector gpsPoints;

  AffineTransform at = new AffineTransform();

  public gpsShowWindow() {


    System.out.println("gpsShow instantiated...");
    
    this.getContentPane().setLayout(new BorderLayout());

    JLabel instructions = new JLabel("<html>hello</html>");

    this.getContentPane().add(instructions, BorderLayout.NORTH);


    this.setTitle("gpsShow");

    this.addMouseListener(new gpsShowMouseListener());
    this.pack();


    System.out.println("Grabbing stuff from SQL");

    gpsPoints = new SQLTempPointsReader().getPoints();
      
    System.out.println("Grabbing AffineTransform...");

    at = new gpsCoord().getTransform(gpsPoints, 600);
    
    setVisible(true);

    System.out.println("all done...");
    
    
  }//end constructor



  public void paint(Graphics g)
  {
    super.paint(g);

    if( gpsPoints != null )
    {
      Enumeration e = gpsPoints.elements();

      Point2D.Float p1 = new Point2D.Float();
      Point2D.Float p2 = new Point2D.Float();

      while( e.hasMoreElements() )
      {
        gpspoint p = (gpspoint)e.nextElement();

        p1.setLocation(p.getLongitude(), p.getLatitude());

        at.transform(p1,p2);
        g.drawLine((int)p2.getX(),
                   (int)p2.getY(),
                   (int)p2.getX(),
                   (int)p2.getY());
                   

//        System.out.println("drawing point " + p2.getX() + "," + p2.getY());
      }

    }
  }

} // 
