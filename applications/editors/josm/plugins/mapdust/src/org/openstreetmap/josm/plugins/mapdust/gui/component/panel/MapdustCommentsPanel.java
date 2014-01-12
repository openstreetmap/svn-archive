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
package org.openstreetmap.josm.plugins.mapdust.gui.component.panel;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DateFormat;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustComment;


/**
 * Defines the JPanel which displays the comments of a <code>MapdustBug</code>
 * object.
 *
 * @author Bea
 *
 */
public class MapdustCommentsPanel extends JPanel {

    /** The serial version UID */
    private static final long serialVersionUID = 5730420562553912697L;

    /**
     * Builds a <code>MapdustCommentsPanel</code> object based on the given
     * arguments.
     *
     * @param comments The array of <code>MapdustComment</code> object
     */
    public MapdustCommentsPanel(MapdustComment[] comments) {
        setLayout(new BorderLayout());
        addComponents(comments);
    }

    /**
     * Updates the components of the <code>MapdustCommentsPanel</code> based on
     * the given parameter.
     *
     * @param comments The array of <code>MapdustComment</code>s
     */
    public void updateComponents(MapdustComment[] comments) {
        removeAll();
        addComponents(comments);
    }

    /**
     * Adds the components to the panel.
     *
     * @param comments The array of <code>MapdustComment</code> objects
     */
    private void addComponents(MapdustComment[] comments) {
        String name = "Bug Comments (";
        name += comments.length + " )";
        setName(name);
        JTextArea txt = new JTextArea();
        txt.setAutoscrolls(true);
        txt.setEditable(false);
        String line = "";
        DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT,
                Locale.getDefault());
        int index = comments.length;
        for (MapdustComment com : comments) {
            line += "Comment " + index + ":";
            line += "\n";
            line += "  " + com.getCommentText();
            line += "\n ";
            line += " added by " + "[" + com.getNickname() + "]";
            line += " on ";
            line += "[ " + df.format(com.getDateCreated()) + " ";
            line += "] \n\n";
            index--;
        }
        txt.setText(line);
        txt.setFont(new Font("Times New Roman", Font.BOLD, 12));
        txt.setCaretPosition(0);
        JScrollPane sp = ComponentUtil.createJScrollPane(txt, null, Color.white,
                true, true);
        sp.setPreferredSize(new Dimension(100, 100));
        add(sp);
    }

}
