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

package org.openstreetmap.applet;

import java.lang.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import org.openstreetmap.applet.*;

public class osmAppletButtons extends JPanel implements ActionListener
{
  private osmDisplay od;
  private osmAppletLineDrawListener osmLDL;

  
  public osmAppletButtons(osmDisplay osmdisp, osmAppletLineDrawListener oLDL)
  {
    
    od = osmdisp;
    osmLDL = oLDL;

    setLayout(new GridLayout(1,10));

    JPanel mp = new JPanel();
    mp.setLayout(new BorderLayout());
    
    
    JPanel zoomButtons = new JPanel();
    zoomButtons.setLayout(new BorderLayout());
    

   
    java.net.URL imageURL;
    
    imageURL = osmAppletButtons.class.getResource("/left.png");
    JButton bLeft = new JButton( new ImageIcon(imageURL) );
    bLeft.setActionCommand("left");
    bLeft.addActionListener(this);
    mp.add(bLeft, BorderLayout.WEST);

    imageURL = osmAppletButtons.class.getResource("/right.png");
    JButton bRight = new JButton(new ImageIcon(imageURL));
    bRight.setActionCommand("right");
    bRight.addActionListener(this);
    mp.add(bRight, BorderLayout.EAST);

    imageURL = osmAppletButtons.class.getResource("/up.png");
    JButton bUp = new JButton( new ImageIcon(imageURL));
    bUp.setActionCommand("up");
    bUp.addActionListener(this);
    mp.add(bUp, BorderLayout.NORTH);

    imageURL = osmAppletButtons.class.getResource("/down.png");
    JButton bDown = new JButton( new ImageIcon(imageURL));
    bDown.setActionCommand("down");
    bDown.addActionListener(this);
    mp.add(bDown, BorderLayout.SOUTH);

    imageURL = osmAppletButtons.class.getResource("/zoomout.png");
    JButton bZoomout = new JButton( new ImageIcon(imageURL));
    bZoomout.setActionCommand("zoomout");
    bZoomout.addActionListener(this);
    zoomButtons.add(bZoomout, BorderLayout.SOUTH);

    imageURL = osmAppletButtons.class.getResource("/zoomin.png");
    JButton bZoomin = new JButton( new ImageIcon(imageURL));
    bZoomin.setActionCommand("zoomin");
    bZoomin.addActionListener(this);
    zoomButtons.add(bZoomin, BorderLayout.NORTH);


    JPanel navPanel = new JPanel();

    navPanel.add(mp);
    navPanel.add(zoomButtons);

    add(navPanel);

    JTabbedPane tabbedPane = new JTabbedPane();
  
    tabbedPane.addChangeListener(new osmAppletModeListener(od));
    
    JPanel editPoints = new JPanel();
    JPanel editLines = new JPanel();

    ButtonGroup buttonGroup = new ButtonGroup();

    
    imageURL = osmAppletButtons.class.getResource("/move-node.png");
    JToggleButton moveNode = new JToggleButton( new ImageIcon(imageURL));
    moveNode.setActionCommand("LINE_MOVE_NODE");
    moveNode.setToolTipText("Move a node");
    moveNode.addActionListener(this);

    imageURL = osmAppletButtons.class.getResource("/new-node.png");
    JToggleButton addNode = new JToggleButton( new ImageIcon(imageURL), true );
    addNode.setActionCommand("LINE_ADD_NODE");
    addNode.setToolTipText("Add a node");
    addNode.addActionListener(this);

    imageURL = osmAppletButtons.class.getResource("/del-node.png");
    JToggleButton deleteNode = new JToggleButton( new ImageIcon(imageURL));
    deleteNode.setActionCommand("LINE_DELETE_NODE");
    deleteNode.setToolTipText("Delete a node");
    deleteNode.addActionListener(this);

    imageURL = osmAppletButtons.class.getResource("/new-line.png");
    JToggleButton newLine = new JToggleButton( new ImageIcon(imageURL));
    newLine.setActionCommand("LINE_NEW_LINE");
    newLine.setToolTipText("Join two nodes to form a street segment");
    newLine.addActionListener(this);

    imageURL = osmAppletButtons.class.getResource("/del-line.png");
    JToggleButton deleteLine = new JToggleButton( new ImageIcon(imageURL));
    deleteLine.setActionCommand("LINE_NEW_LINE");
    deleteLine.setToolTipText("Break the join between two nodes to delete a street segment");
    deleteLine.addActionListener(this);
   

    buttonGroup.add(moveNode);
    buttonGroup.add(addNode);
    buttonGroup.add(deleteNode);
    buttonGroup.add(newLine);
    buttonGroup.add(deleteLine);

    editLines.add(moveNode);
    editLines.add(addNode);
    editLines.add(deleteNode);
    editLines.add(newLine);
    editLines.add(deleteLine);

    JPanel editAreas = new JPanel();


    JPanel loginTools = new JPanel();
    
    JButton bLogin = new JButton("Login");
    bLogin.setActionCommand("login");
    bLogin.addActionListener(this);
    loginTools.add(bLogin);

    
    tabbedPane.add("Points", editPoints);
    tabbedPane.add("Lines", editLines);
    tabbedPane.add("Areas", editAreas);
    tabbedPane.add("Server", loginTools);

    tabbedPane.setSelectedIndex(1);

    add(tabbedPane);
    //    add(loginButtons);

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

    if( e.getActionCommand().equals("login") )
    {
      osmAppletLoginWindow loginWindow = new osmAppletLoginWindow((JFrame)null,true,od);

      return;
    }


    if( e.getActionCommand().equals("LINE_ADD_NODE"))
    {

      osmLDL.setMode(osmAppletLineDrawListener.MODE_ADD_NODE);
      return;

    }

    if( e.getActionCommand().equals("LINE_MOVE_NODE"))
    {

      osmLDL.setMode(osmAppletLineDrawListener.MODE_MOVE_NODE);
      return;

    }
 
    if( e.getActionCommand().equals("LINE_DELETE_NODE"))
    {

      osmLDL.setMode(osmAppletLineDrawListener.MODE_DELETE_NODE);
      return;

    }
 
    
    if( e.getActionCommand().equals("LINE_NEW_LINE"))
    {

      osmLDL.setMode(osmAppletLineDrawListener.MODE_NEW_LINE);
      return;

    }
 

  }

} // osmAppletButtons
