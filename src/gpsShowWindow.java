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

  Image offScreenBuffer;

  public gpsShowWindow() {


    System.out.println("gpsShow instantiated...");
    
    this.getContentPane().setLayout(new BorderLayout());

    //JLabel instructions = new JLabel("<html>hello</html>");

    //this.getContentPane().add(instructions, BorderLayout.NORTH);


    this.setTitle("gpsShow");

    gpsShowMouseListener gsml = new gpsShowMouseListener(this);
    this.addMouseMotionListener(gsml);
    this.addMouseListener(gsml);
    this.setSize(600,600);
//    this.pack();


    System.out.println("Grabbing stuff from SQL");

    gpsPoints = new SQLTempPointsReader().getPoints();
      
    System.out.println("Grabbing AffineTransform...");

    at = new gpsCoord().getTransform(gpsPoints, 600);
    
    setVisible(true);

    System.out.println("all done...");
    
    
  }//end constructor


  public void translate(int x, int y)
  {
    //System.out.println("asked to translate by " + x + "," + y);
    at.preConcatenate(AffineTransform.getTranslateInstance(x,y));

    repaint();
  } // translate

 
  
  
  public void scale(int x, int y)
  {
    //System.out.println("asked to scale by " + x + "," + y);

    at.preConcatenate(
        AffineTransform.getScaleInstance(1+ ((double)y)/100 ,1+ ((double)y)/100));

    repaint();
  } // scale


  public void update(Graphics g)
  {
    Graphics gr; 
    
    if (offScreenBuffer==null ||
        (! (offScreenBuffer.getWidth(this) == this.size().width
            && offScreenBuffer.getHeight(this) == this.size().height)))
    {
      offScreenBuffer = this.createImage(size().width, size().height);
    }

    gr = offScreenBuffer.getGraphics();

    paint(gr);
    
    g.drawImage(offScreenBuffer, 0, 0, this);
  }

  public void paint(Graphics g)
  {
    super.paint(g);

    if( gpsPoints != null )
    {

      g.setColor(Color.white);

      g.fillRect(0,0,600,600);

      g.setColor(Color.black);
      
      //     System.out.println(at);

      Enumeration e = gpsPoints.elements();

      Point2D.Float p1 = new Point2D.Float();
      Point2D.Float p2 = new Point2D.Float();

      while( e.hasMoreElements() )
      {
        gpspoint p = (gpspoint)e.nextElement();

        p1.setLocation(p.getLongitude(), p.getLatitude());

        at.transform(p1,p2);

        g.drawLine(300 + (int)p2.getX(),
            300 + (int)p2.getY(),
            300 + (int)p2.getX(),
            300 + (int)p2.getY());

      }

    }
  }

} // 
