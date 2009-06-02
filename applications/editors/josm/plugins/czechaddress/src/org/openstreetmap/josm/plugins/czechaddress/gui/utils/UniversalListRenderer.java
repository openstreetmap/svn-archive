/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.josm.plugins.czechaddress.gui.utils;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author Radomír Černoch, radomir.cernoch@gmail.com
 */
public class UniversalListRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                          int index, boolean isSelected, boolean cellHasFocus) {
        
        Component c = super.getListCellRendererComponent(list, value, index,
                                                      isSelected, cellHasFocus);
        setIcon(UniversalRenderer.getIcon(value));
        setText(UniversalRenderer.getText(value));

        return c;
    }
}
