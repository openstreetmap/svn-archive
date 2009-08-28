/***************************************************************************
 *   Copyright (C) 2009 by Tomasz Stelmach                                 *
 *   http://www.stelmach-online.net/                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package org.openstreetmap.josm.plugins.piclayer;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.openstreetmap.josm.actions.JosmAction;

/**
 * Help class. Basically copied from WMSPlugin.
 */
public class HelpAction extends JosmAction {

	/**
	 * Constructor
	 */
	public HelpAction() {
		super( "Help", null, null, null, false);

	}

	/**
	 * Shows help window.
	 */
	public void actionPerformed(ActionEvent e) {
          String helptext = 
            "This plugin is meant for showing images as a background of the data being edited."
            + "It offers two ways of loading the data - from a file and from the clipboard."
            + "This is an early version and is not end-user-proof, so please follow the steps exactly in case you want to use it."
            + "Actually... it's barely working at the moment but I already find it very useful :)."
            + "\n\n"
            + "NOTE: Make sure the image is suitable, copyright-wise, if in doubt, don't use."
            + "\n"
            + "Step 1) Load some data from the server so that map is visible."
            + "\n"
            + "Step 2) Go to PicLayer menu and either choose to add a layer from a file or from the clipboard."
            + "\n"
            + "Step 2a) If you chose a file, a file selector will pop-up. Choose the image you want. Of course the format you are using might not be supported yet :)"
            + "\n"
            + "Step 2b) If you chose a clipboard, please wait. For some reason it takes time. To be fixed later."
            + "\n"
            + "NOTE) If something failed, you should get a message box. If not - check the console for messages (Linux only?)"
            + "\n"
            + "Step 3) Once the image is visible you may start positioning it. For that - select the PicLayer in the layers list and activate it (!!!)."
            + "\n"
            + "Step 4) Start aligning the image."
            + "\n"
            + "Step 4a) Move the image by choosing 'Move' from the toolbar and draggin the mouse around with left button pressed."
            + "\n"
            + "Step 4b) Rotate the image by choosing 'Rotate' from the toolbar and draggin the mouse up/down with left button pressed."
            + "\n"
            + "Step 4c) Scale the image by choosing 'Scale' from the toolbar and draggin the mouse up/down with left button pressed."
            + "\n"
            + "Step 4d) You can also choose to scale in just one axis by selecting the proper button."
            + "\n"
            + "NOTE) If it does not work - make sure the right layer is selected AND ACTIVATED."
            + "\n"
            + "Step 5) If you need to reset your changes - you can use the popup menu available under the PicLayer entry in the layer list."
            + "\n"
            + "Step 6) If you want to save the calibraton of the picture you have made, you can also do this using the popup menu."
            + "\n\n"
            + "This plugin is meant to help with mapping from photos (for me it's mostly buildings) for people who simply don't like MetaCarta Rectifier (like me)."
            + "\n\n"
            + "Although I wrote it only to help myself and currently have no intention of extending it beyond what I need, you may still contact me with any suggestions that you think are important: tomasz.stelmach@poczta.onet.pl"
            + "\n\n"
            + "I used wmsplugin and routing plugin code very much during coding. Thank you and I hope you don't mind :)"
       	  ;
        
		JTextPane tp = new JTextPane();
		JScrollPane js = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		          JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		           
		js.getViewport().add(tp);
		JFrame jf = new JFrame("PicLayer Help");
		jf.getContentPane().add(js);
		jf.pack();
		jf.setSize(600,600);
		jf.setVisible(true); 
		tp.setText(helptext);
    }
}
