//    JOSM Imagery XML Bounds plugin.
//    Copyright (C) 2011 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import net.boplicity.xmleditor.XmlTextPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsLayer;

/**
 * @author Don-vip
 *
 */
public class ShowBoundsAction extends ComputeBoundsAction {

	private static final long serialVersionUID = 6636454148615649794L;

	public ShowBoundsAction() {
	}
	
	public ShowBoundsAction(XmlBoundsLayer xmlBoundsLayer) {
		super(xmlBoundsLayer);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		XmlTextPane pane = new XmlTextPane();
		Font courierNew = Font.getFont("Courier New"); 
		if (courierNew != null) {
			pane.setFont(courierNew);
		}
		pane.setText(getXml());
		pane.setEditable(false);
		Box box = Box.createVerticalBox();
		JScrollPane scrollPane = new JScrollPane(pane);
        scrollPane.setPreferredSize(new Dimension(1024, 600));
        box.add(scrollPane);
        JOptionPane.showMessageDialog(Main.parent, box, ACTION_NAME, JOptionPane.PLAIN_MESSAGE);
	}
}
