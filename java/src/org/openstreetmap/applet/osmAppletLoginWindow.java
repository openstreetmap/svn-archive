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

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.lang.*;
import java.awt.event.*;

import org.openstreetmap.client.*;

public class osmAppletLoginWindow extends JDialog implements ActionListener
{

  JTextField user = new JTextField(20);
  JPasswordField pass = new JPasswordField(20);
  
  osmServerClient osc;

  
  public osmAppletLoginWindow(JFrame j, boolean bYesNo, osmDisplay od)
  {
    super(j,bYesNo);

    osc = od.getServerClient();

    System.out.println("login window created");


    // text boxes

    JPanel textPane = new JPanel();

    textPane.setLayout(new GridLayout(2,1));

    JPanel userPane = new JPanel();

    userPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

    userPane.add(new JLabel("Email address:", JLabel.RIGHT));

    userPane.add(user);

    JPanel passPane = new JPanel();

    passPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

    passPane.add(new JLabel("Password:", JLabel.RIGHT));

    passPane.add(pass);


    textPane.add(userPane);
    textPane.add(passPane);

    // buttons

    JPanel buttons = new JPanel();

    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

    buttons.add(Box.createHorizontalGlue());
    
    JButton bCancel = new JButton("cancel");
    bCancel.setActionCommand("cancel");
    bCancel.addActionListener(this);

    JButton bLogin = new JButton("Login");
    bLogin.setActionCommand("login");
    bLogin.addActionListener(this);


    buttons.add(bCancel);
    buttons.add(bLogin);


    JPanel main = new JPanel();

    main.setLayout(new BorderLayout());

    main.add(textPane, BorderLayout.CENTER);

    main.add(buttons, BorderLayout.SOUTH);


    this.getContentPane().add(main);


    this.pack();

    this.setTitle("OpenStreetMap Login");
    this.setResizable(false);
    this.setVisible(true);

  } // osmAppletLoginWindow



  public void actionPerformed(ActionEvent e)
  {

    System.out.println(e.getActionCommand());

    if( e.getActionCommand().equals("cancel") )
    {
      this.setVisible(false);

      return;
    } 

    if( e.getActionCommand().equals("login") )
    {
      JOptionPane jp = new JOptionPane();
      if( osc.login(user.getText(), pass.getText()) )
      {
        jp.showMessageDialog((JFrame)null, "Login successful");

        this.setVisible(false);
      }
      else
      {
        jp.showMessageDialog((JFrame)null, "Login failed", "Login message", JOptionPane.ERROR_MESSAGE);
      }
        

    }

  } // actionPerformed

} // osmAppletLoginWindow
