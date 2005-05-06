/* Based on osmAppletLoginWindow 

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


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.lang.*;
import java.awt.event.*;


public class WaypointDialogue extends JDialog implements ActionListener
{

  JTextField nameField;
  JComboBox typeComboBox;
  boolean ok=false;
  

  
  public WaypointDialogue(String name,String type)
  { 
	super((JFrame)null,true);
	nameField=new JTextField(20);
	nameField.setText(name);
	typeComboBox=new JComboBox(LookAndFeel.getWaypointTypes());
	typeComboBox.setSelectedItem(type);



    // text boxes

    JPanel textPane = new JPanel();

    textPane.setLayout(new GridLayout(2,1));

    JPanel namePane = new JPanel();

    namePane.setLayout(new FlowLayout(FlowLayout.RIGHT));

    namePane.add(new JLabel("Name:", JLabel.RIGHT));

    namePane.add(nameField);

    JPanel typePane = new JPanel();

    typePane.setLayout(new FlowLayout(FlowLayout.RIGHT));

    typePane.add(new JLabel("Type:", JLabel.RIGHT));

    typePane.add(typeComboBox);


    textPane.add(namePane);
    textPane.add(typePane);

    // buttons

    JPanel buttons = new JPanel();

    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

    buttons.add(Box.createHorizontalGlue());
    
    JButton bCancel = new JButton("Cancel");
    bCancel.setActionCommand("Cancel");
    bCancel.addActionListener(this);

    JButton bOK = new JButton("OK");
    bOK.setActionCommand("OK");
    bOK.addActionListener(this);


    buttons.add(bCancel);
    buttons.add(bOK);


    JPanel main = new JPanel();

    main.setLayout(new BorderLayout());

    main.add(textPane, BorderLayout.CENTER);

    main.add(buttons, BorderLayout.SOUTH);


    this.getContentPane().add(main);


    this.pack();

    this.setTitle("Edit waypoint type");
    this.setResizable(false);
    this.setVisible(true);

  } // WaypointDialogue



  public void actionPerformed(ActionEvent e)
  {

    if( e.getActionCommand().equals("Cancel") )
    {
		ok=false;
    } 

	else if( e.getActionCommand().equals("OK") )
    {
		ok=true;
	}
	this.setVisible(false);

  } // actionPerformed

  public boolean okPressed()
  {
	return ok;
  }

  public String getType()
  {
	return (String)typeComboBox.getSelectedItem();
  }

  public String getName()
  {
	return nameField.getText();
  }	

} // WaypointDialogue
