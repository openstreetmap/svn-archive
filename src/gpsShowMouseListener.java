import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;


public class gpsShowMouseListener extends MouseInputAdapter{

  gpsShowWindow gsw;
  
  int lastX = 0;
  int lastY = 0;
  
  public gpsShowMouseListener(gpsShowWindow g)
  {
    
    gsw = g;
    

  } // gpsShowMouseListener
  

  public void mouseDragged(MouseEvent e)
  {

    if( e.isControlDown() )
    {
      gsw.scale(e.getX() - lastX, e.getY() - lastY);

    }
    else
    {

      gsw.translate(e.getX() - lastX, e.getY() - lastY);
    }
      
    
    lastX = e.getX();
    lastY = e.getY();
    
    
      

  } // mouseDragged
    

  
  public void mousePressed(MouseEvent e){

    lastX = e.getX();
    lastY = e.getY();
  
  }

  public void mouseReleased(MouseEvent e){

  }

  public void mouseEntered(MouseEvent e){

  }

  public void mouseExited(MouseEvent e){

  }

  public void mouseClicked(MouseEvent e){

  }

} // gpsShowmouseListener
