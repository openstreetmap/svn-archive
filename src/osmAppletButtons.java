import java.lang.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class osmAppletButtons extends JPanel implements ActionListener
{
  osmDisplay od;

  public osmAppletButtons(osmDisplay osmdisp)
  {
    od = osmdisp;

    setLayout(new FlowLayout());

    JButton bLeft = new JButton( new ImageIcon("left.png"));
    bLeft.setActionCommand("left");
    bLeft.addActionListener(this);
    add(bLeft);

    JButton bRight = new JButton("right");
    bRight.setActionCommand("right");
    bRight.addActionListener(this);
    add(bRight);

    JButton bUp = new JButton("up");
    bUp.setActionCommand("up");
    bUp.addActionListener(this);
    add(bUp);

    JButton bDown = new JButton("down");
    bDown.setActionCommand("down");
    bDown.addActionListener(this);
    add(bDown);


    JButton bZoomout = new JButton("zoom out");
    bZoomout.setActionCommand("zoomout");
    bZoomout.addActionListener(this);
    add(bZoomout);

    JButton bZoomin = new JButton("zoom in");
    bZoomin.setActionCommand("zoomin");
    bZoomin.addActionListener(this);
    add(bZoomin);


  } // osmAppletButtons


  public void actionPerformed(ActionEvent e)
  {

    System.out.println(e.getActionCommand());

    if( e.getActionCommand().equals("left") )
    {
      od.left();
      return;
    }  
  
    if( e.getActionCommand().equals("right") )
    {
      od.right();
      return;
    }

    if( e.getActionCommand().equals("up") )
    {
      od.up();
      return;
    }

    if( e.getActionCommand().equals("down") )
    {
      od.down();
      return;
    }


    if( e.getActionCommand().equals("zoomin") )
    {
      od.zoomin();
      return;
    }
    

    if( e.getActionCommand().equals("zoomout") )
    {
      od.zoomout();
      return;
    }


  }
} // osmAppletButtons
