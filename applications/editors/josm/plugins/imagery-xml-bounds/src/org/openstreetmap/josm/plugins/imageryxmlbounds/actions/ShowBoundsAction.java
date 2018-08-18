// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.actions.IPrimitiveAction;
import org.openstreetmap.josm.data.osm.IPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsLayer;

import net.boplicity.xmleditor.XmlTextPane;

/**
 * Show XML bounds.
 * @author Don-vip
 */
public class ShowBoundsAction extends ComputeBoundsAction implements IPrimitiveAction {

    /**
     * Constructs a new {@code ShowBoundsAction}.
     */
    public ShowBoundsAction() {
    }

    /**
     * Constructs a new {@code ShowBoundsAction}.
     * @param xmlBoundsLayer XML bounds layer
     */
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
        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), box, ACTION_NAME, JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    public void setPrimitives(Collection<? extends IPrimitive> primitives) {
        updateOsmPrimitives(primitives);
    }
}
