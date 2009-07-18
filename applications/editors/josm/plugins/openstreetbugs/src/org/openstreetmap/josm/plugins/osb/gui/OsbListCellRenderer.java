/* Copyright (c) 2008, Henrik Niehaus
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.osb.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.osb.OsbPlugin;

public class OsbListCellRenderer implements ListCellRenderer {

    private Color background = Color.WHITE;
    private Color altBackground = new Color(250, 250, 220);

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {

        JLabel label = new JLabel();
        label.setOpaque(true);

        if(isSelected) {
            label.setForeground(UIManager.getColor("List.selectionForeground"));
            label.setBackground(UIManager.getColor("List.selectionBackground"));
        } else {
            label.setForeground(UIManager.getColor("List.foreground"));
            label.setBackground(index % 2 == 0 ? background : altBackground);
        }

        OsbListItem item = (OsbListItem) value;
        Node n = item.getNode();
        Icon icon = null;
        if("0".equals(n.get("state"))) {
            icon = OsbPlugin.loadIcon("icon_error16.png");
        } else if("1".equals(n.get("state"))) {
            icon = OsbPlugin.loadIcon("icon_valid16.png");
        }
        label.setIcon(icon);
        String text = n.get("note");
        if(text.indexOf("<hr />") > 0) {
            text = text.substring(0, text.indexOf("<hr />"));
        }
        label.setText(text);

        Dimension d = label.getPreferredSize();
        d.height += 10;
        label.setPreferredSize(d);

        return label;
    }

}
