// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import net.boplicity.xmleditor.XmlTextPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.OsmPrimitiveAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsLayer;

/**
 * @author Don-vip
 *
 */
@SuppressWarnings("serial")
public class ShowBoundsAction extends ComputeBoundsAction implements OsmPrimitiveAction {

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

    @Override
    public void setPrimitives(Collection<? extends OsmPrimitive> primitives) {
        updateOsmPrimitives(primitives);
    }
}
