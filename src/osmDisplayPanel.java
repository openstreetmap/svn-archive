import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


public class osmDisplayPanel extends JPanel{

  Vector gpsPoints;

  osmApplet osmA;
  
  AffineTransform at = new AffineTransform();

  Image offScreenBuffer;

  public osmDisplayPanel(osmApplet osmApp) {

    osmA = osmApp;

    System.out.println("gpsShow instantiated...");
    

    //JLabel instructions = new JLabel("<html>hello</html>");

    //this.getContentPane().add(instructions, BorderLayout.NORTH);


    gpsShowMouseListener gsml = new gpsShowMouseListener(this);
    this.addMouseMotionListener(gsml);
    this.addMouseListener(gsml);
    this.setSize(600,600);
//    this.pack();


    System.out.println("Grabbing stuff from server");

    gpsPoints = new osmServerClient().getPoints();
      
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
  
  } // update


  
  public void paint(Graphics g)
  {
    super.paint(g);

    int nWindowCentreX = this.getWidth() / 2;
    int nWindowCentreY = this.getHeight() / 2;
    
    if( gpsPoints != null )
    {

      g.setColor(Color.white);

      g.fillRect(0,0,this.getWidth(),this.getHeight());

      g.setColor(Color.black);
      
      //     System.out.println(at);

      Enumeration e = gpsPoints.elements();

      while( e.hasMoreElements() )
      {
        gpspoint p = (gpspoint)e.nextElement();

        p.paintPoint(g, nWindowCentreX, nWindowCentreY, at);
      }

    }

  } // paint

  

  public void mouseClicked(MouseEvent e)
  {

    
    Enumeration en = gpsPoints.elements();

    gpspoint closestPoint = new gpspoint(0,0,0,0);
    double closestDistance = 10000000;
 
    Point2D p1 = new Point2D.Double(e.getX() - (this.getWidth()/2),
                                    e.getY() - (this.getHeight()/2)); 
    Point2D p2 = new Point2D.Double(0,0);
    
    try{
      at.inverseTransform(p1,p2);
    }
    catch(Exception ex)
    {
      System.out.println("ouch " + ex);
      ex.printStackTrace();
      System.exit(-1);
    }
   
    while( en.hasMoreElements() )
    {
      gpspoint p = (gpspoint)en.nextElement();
      
      
      double myDis = p2.distanceSq(
                      new Point2D.Double(p.getLongitude(),
                                         p.getLatitude()));

      
    
      p.setHighlight(false);
      
      if( myDis < closestDistance )
      {

        closestDistance = myDis;

        closestPoint = p;
      }
      
    }    
  
    closestPoint.setHighlight(true);

    osmA.setStatusLabel("closest point is at" +
                        "lat " + closestPoint.getLatitude() +
                        " lon " + closestPoint.getLongitude() +
                        " alt " + closestPoint.getAltitude() +
                        " recorded at " + new Date(closestPoint.getTime()));
                       
    repaint();

  } // mouseClicked


} // 
