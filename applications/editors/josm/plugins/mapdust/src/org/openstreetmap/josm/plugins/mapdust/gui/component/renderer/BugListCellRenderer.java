/* Copyright (c) 2010, skobbler GmbH
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
package org.openstreetmap.josm.plugins.mapdust.gui.component.renderer;


import java.awt.Component;
import java.awt.Font;
import java.text.DateFormat;
import java.util.Locale;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.tools.ImageProvider;


/**
 * Cell renderer for the list of <code>MapdustBug</code> objects.
 *
 * @author Bea
 */
public class BugListCellRenderer extends DefaultListCellRenderer {

    /** The serial version UID */
    private static final long serialVersionUID = -5888587819204364046L;

    /**
     * Returns the cell renderer component of the MapDust bug list.
     *
     * @param list The MapDust bug list
     * @param value The selected object
     * @param index The selected index
     * @param isSelected Specifies if the item is selected or not
     * @param hasFocus Specifies if the item has focus or not
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean hasFocus) {
        JLabel label =(JLabel) super.getListCellRendererComponent(list, value,
                index,isSelected, hasFocus);
        if (value instanceof MapdustBug) {
            /* show the MapdustBug in the list */
            MapdustBug mapdustBug = (MapdustBug) value;
            String iconPath = "bugs/normal/";
            iconPath += mapdustBug.getStatus().getValue().toLowerCase();
            iconPath += "_";
            iconPath += mapdustBug.getType().getKey() + ".png";
            ImageIcon icon = ImageProvider.get(iconPath);
            icon.setDescription(mapdustBug.getType().getValue());
            label.setIcon(ImageProvider.get(iconPath));
            String text = "" + mapdustBug.getId() + ": ";
            text += mapdustBug.getType().getValue();
            if (mapdustBug.getAddress() != null) {
                String addressStr=mapdustBug.getAddress().toString();
                if (!addressStr.trim().isEmpty()) {
                    text += " (" + mapdustBug.getAddress().toString() + " )";
                }
            }
            DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT,
                    Locale.getDefault());
            text +=" last modified on ";
            text += df.format(mapdustBug.getDateUpdated());
            label.setText(text);
            label.setFont(new Font("Times New Roman", Font.BOLD, 12));
            label.setSize(200, 20);
        }
        if (value instanceof String){
            /* show default text in the list */
            String text=(String)value;
            label.setText(text);
            label.setFont(new Font("Times New Roman", Font.BOLD, 12));
        }
        return (label);
    }

}
