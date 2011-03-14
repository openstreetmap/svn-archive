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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;


/**
 * Defines the JPanel which displays the <code>MapdustBug</code> object
 * description.
 *
 * @author Bea
 *
 */
public class MapdustDescriptionPanel extends JPanel {

    /** The serial version UID */
    private static final long serialVersionUID = -4246874841836269643L;

    /**
     * Builds a <code>MapdustDescriptionPanel</code> object
     *
     * @param description The description of the bug
     */
    public MapdustDescriptionPanel(String description) {
        setLayout(new BorderLayout());
        String name = "Description ";
        setName(name);
        addComponents(description);
    }

    /**
     * Updates the components of the <code>MapdustDescriptionPanel</code> based
     * on the given parameter
     *
     * @param description The description
     */
    public void updateComponents(String description) {
        removeAll();
        addComponents(description);
    }

    /**
     * Adds the components to the <code>MapdustDescriptionPanel</code>.
     *
     * @param description the description
     */
    private void addComponents(String description) {
        if (description != null && !description.isEmpty()) {
            JTextArea txtDescription = new JTextArea(description);
            txtDescription.setLineWrap(true);
            txtDescription.setFont(new Font("Times New Roman", Font.BOLD, 12));
            txtDescription.setEditable(false);
            JScrollPane cmpDescription = ComponentUtil.createJScrollPane(
                    txtDescription, null, Color.white, true, true);
            cmpDescription.setPreferredSize(new Dimension(100, 100));
            add(cmpDescription, BorderLayout.CENTER);
        }
    }

}
