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

    JPanel mp = new JPanel();
    mp.setLayout(new BorderLayout());
    
    add(mp);

    java.net.URL imageURL;
    
    imageURL = osmAppletButtons.class.getResource("left.png");
    JButton bLeft = new JButton( new ImageIcon(imageURL) );
    bLeft.setActionCommand("left");
    bLeft.addActionListener(this);
    mp.add(bLeft, BorderLayout.WEST);

    imageURL = osmAppletButtons.class.getResource("right.png");
    JButton bRight = new JButton(new ImageIcon(imageURL));
    bRight.setActionCommand("right");
    bRight.addActionListener(this);
    mp.add(bRight, BorderLayout.EAST);

    imageURL = osmAppletButtons.class.getResource("up.png");
    JButton bUp = new JButton( new ImageIcon(imageURL));
    bUp.setActionCommand("up");
    bUp.addActionListener(this);
    mp.add(bUp, BorderLayout.NORTH);

    imageURL = osmAppletButtons.class.getResource("down.png");
    JButton bDown = new JButton( new ImageIcon(imageURL));
    bDown.setActionCommand("down");
    bDown.addActionListener(this);
    mp.add(bDown, BorderLayout.SOUTH);

    imageURL = osmAppletButtons.class.getResource("zoomout.png");
    JButton bZoomout = new JButton( new ImageIcon(imageURL));
    bZoomout.setActionCommand("zoomout");
    bZoomout.addActionListener(this);
    add(bZoomout);

    imageURL = osmAppletButtons.class.getResource("zoomin.png");
    JButton bZoomin = new JButton( new ImageIcon(imageURL));
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
