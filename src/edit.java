import java.awt.event.*;
import java.awt.*;

class MyCanvas extends Canvas {

  public void paint(Graphics g) {

  }
}

class Gui extends Frame implements WindowListener {

  Graphics pic;

  /* Constructor */

  public Gui(String text) {
    super(text);	
    setBackground(new Color(200,200,200));
    addWindowListener(this);

    MyCanvas pic = new MyCanvas();
    add(pic); 
  }	

  /* Window Closed */

  public void windowClosed(WindowEvent event) {
  }

  /* Window Deiconified */

  public void windowDeiconified(WindowEvent event) {
  }

  /* Window  Iconified */

  public void windowIconified(WindowEvent event) {
  }

  /* Window Activated */

  public void windowActivated(WindowEvent event) {
  }

  /* Window Deactivated */

  public void windowDeactivated(WindowEvent event) {
  }

  /* Window Opened */

  public void windowOpened(WindowEvent event) {
  }

  /* Window Closing */

  public void windowClosing(WindowEvent event) {
    System.exit(0);
  } 
}



class edit {

  /* Main  method */    

  public static void main(String[] args) {
    Gui screen = new Gui("GUI Example 1");

    screen.setSize(300,300);
    screen.setVisible(true);
  }
}
