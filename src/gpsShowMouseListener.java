import java.awt.*;
import java.awt.event.*;

public class gpsShowMouseListener implements MouseListener{

  public void mousePressed(MouseEvent e){
    System.out.println("1");
  }

  public void mouseReleased(MouseEvent e){

    System.out.println("2");
  }

  public void mouseEntered(MouseEvent e){

    System.out.println("3");
  }

  public void mouseExited(MouseEvent e){

    System.out.println("4");
  }

  public void mouseClicked(MouseEvent e){

    System.out.println("5");
  }

} // gpsShowmouseListener
