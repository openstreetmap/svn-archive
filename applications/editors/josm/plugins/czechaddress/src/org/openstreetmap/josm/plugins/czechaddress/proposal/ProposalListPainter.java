/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.josm.plugins.czechaddress.proposal;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 *
 * @author radek
 */
public class ProposalListPainter extends DefaultListCellRenderer {

    Icon iconAdd = ImageProvider.get("actions", "add.png");
    Icon iconEdit = ImageProvider.get("actions", "edit.png");
    Icon iconRemove = ImageProvider.get("actions", "remove.png");

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        setIcon(null);
        
             if (value instanceof AddKeyValueProposal)    setIcon(iconAdd);
        else if (value instanceof KeyValueChangeProposal) setIcon(iconEdit);
        else if (value instanceof RemoveKeyProposal)      setIcon(iconRemove);

        return c;
    }
    

}
