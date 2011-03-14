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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import org.openstreetmap.josm.plugins.mapdust.gui.value.MapdustAction;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustComment;
import org.openstreetmap.josm.tools.ImageProvider;


/**
 * Cell renderer for the <code>MapdustAction</code> objects.
 *
 * @author Bea
 * @version $Revision$
 */
public class ActionListCellRenderer extends DefaultListCellRenderer {

    /** The serial version UID */
    private static final long serialVersionUID = 7552949107018269769L;

    /**
     * Returns the cell renderer component of the action list.
     *
     * @param list The action list
     * @param value The selected object
     * @param index The selected index
     * @param isSelected Specifies if the item is selected or not
     * @param hasFocus Specifies if the item has focus or not
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean hasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                index, isSelected, hasFocus);
        if (value instanceof MapdustAction) {
            MapdustAction mapdustAction = (MapdustAction) value;
            String text = "";
            MapdustBug bug = mapdustAction.getMapdustBug();
            MapdustComment comment = mapdustAction.getMapdustComment();
            if (comment == null) {
                /* created a new bug */
                bug = mapdustAction.getMapdustBug();
                text += bug.getType().getValue() + ":";
                text += bug.getDescription() + " ";
                text += " created by ";
                text += bug.getNickname();
            } else {
                if (mapdustAction.getNewStatus() != null) {
                    switch (mapdustAction.getNewStatus()) {
                        case 1:
                            /* bug was re-opened */
                            text += bug.getId() + ":";
                            text += bug.getType().getValue() + " ";
                            text += " re-opened by ";
                            text += comment.getNickname();
                            break;
                        case 2:
                            /* bug was fixed */
                            text += bug.getId() + ":";
                            text += bug.getType().getValue() + " ";
                            text += " fixed by ";
                            text += comment.getNickname();
                            break;
                        case 3:
                            /* bug was invalidated */
                            text += bug.getId() + ":";
                            text += bug.getType().getValue() + " ";
                            text += " invalidated by ";
                            text += comment.getNickname();
                            break;
                    }

                } else {
                    /* new comment */
                    text += bug.getId() + ":";
                    text += bug.getType().getValue() + " ";
                    text += " commented by ";
                    text += comment.getNickname();
                }
            }
            /* set the text and icon */
            label.setText(text);
            label.setIcon(ImageProvider.get(mapdustAction.getIconPath()));
            label.setFont(new Font("Times New Roman", Font.BOLD, 12));
        }
        return (label);
    }

}
