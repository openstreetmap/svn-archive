/*
Copyright (C) 2004 Stephen Coast (steve@fractalus.com)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/


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

    JPanel zoomButtons = new JPanel();
    zoomButtons.setLayout(new BorderLayout());
    
    add(zoomButtons);
    
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
    zoomButtons.add(bZoomout, BorderLayout.SOUTH);

    imageURL = osmAppletButtons.class.getResource("zoomin.png");
    JButton bZoomin = new JButton( new ImageIcon(imageURL));
    bZoomin.setActionCommand("zoomin");
    bZoomin.addActionListener(this);
    zoomButtons.add(bZoomin, BorderLayout.NORTH);

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
