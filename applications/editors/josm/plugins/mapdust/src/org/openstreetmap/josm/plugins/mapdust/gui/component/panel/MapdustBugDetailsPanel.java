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


import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.URI;
import java.text.DateFormat;
import java.util.Locale;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustRelevance;
import org.openstreetmap.josm.plugins.mapdust.util.Configuration;
import org.openstreetmap.josm.tools.OpenBrowser;


/**
 * Defines the JPanel for the <code>MapdustBug</code> object details.
 *
 * @author Bea
 */
public class MapdustBugDetailsPanel extends JPanel implements HyperlinkListener {

    /** The serial version UID */
    private static final long serialVersionUID = 65974543949362926L;

    /** The <code>MapdustBug</code> object */
    private MapdustBug bug;

    /**
     * Builds a new <code>MapdustBugDetailsPanel</code> object.
     *
     * @param bug The <code>MapdustBug</code> object
     */
    public MapdustBugDetailsPanel(MapdustBug bug) {
        this.bug = bug;
        setLayout(new GridLayout(8, 2));
        addComponents();
        setBackground(Color.white);
    }

    /**
     * Updates the components of the <code>MapdustBugDetailsPanel</code> based
     * on the given parameter.
     *
     * @param bug The <code>MapdustBug</code>
     */
    public void updateComponents(MapdustBug bug) {
        this.bug = bug;
        removeAll();
        addComponents();
    }

    /**
     * Add the components to the panel.
     */
    private void addComponents() {
        /* the font of the label and label value */
        Font fontLabel = new Font("Times New Roman", Font.BOLD, 12);
        Font fontLabelVal = new Font("Times New Roman", Font.PLAIN, 12);
        /* date formatter */
        DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT,
                Locale.getDefault());

        /* the id */
        add(ComponentUtil.createJLabel("Id: ", fontLabel, null, null));
        String idStr = bug != null ? bug.getId().toString() : "";
        String txt = "<html>";
        txt += "<font style='font-size:10px' face='Times New Roman'>";
        txt += "<a href='' target='_blank'>" + idStr + "</a>";
        txt += "</font></html>";
        JEditorPane txtId = new JEditorPane("text/html", "");
        txtId.setEditorKit(new HTMLEditorKit());
        txtId.setEditable(false);
        txtId.setText(txt);
        txtId.setPreferredSize(new Dimension(100, 20));
        txtId.addHyperlinkListener(this);
        add(txtId);

        /* the type */
        add(ComponentUtil.createJLabel("Type: ", fontLabel, null, null));
        String typeStr = bug != null ? bug.getType().getValue() : "";
        add(ComponentUtil.createJLabel(typeStr, fontLabelVal, null, null));

        /* the status */
        add(ComponentUtil.createJLabel("Status: ", fontLabel, null, null));
        String statusStr = bug != null ? bug.getStatus().getValue() : "";
        add(ComponentUtil.createJLabel(statusStr, fontLabelVal, null, null));

        /* the relevance */
        add(ComponentUtil.createJLabel("Relevance: ", fontLabel, null, null));
        Color color = getRelevanceValueColor(bug);
        String relevanceStr = bug != null ? bug.getRelevance().getName() : "";
        add(ComponentUtil.createJLabel(relevanceStr, fontLabel, null, color));

        /* the source */
        add(ComponentUtil.createJLabel("Source: ", fontLabel, null, null));
        String sourceStr = bug != null ? bug.getSource() : "";
        add(ComponentUtil.createJLabel(sourceStr, fontLabelVal, null, null));

        /* the nickname */
        add(ComponentUtil.createJLabel("Created by: ", fontLabel, null, null));
        String nicknameStr = bug != null ? bug.getNickname() : "";
        add(ComponentUtil.createJLabel(nicknameStr, fontLabelVal, null, null));

        /* the date created */
        add(ComponentUtil.createJLabel("Date created: ", fontLabel, null, null));
        String dateCreatedStr = bug != null ? df.format(bug.getDateCreated())
                : "";
        add(ComponentUtil.createJLabel(dateCreatedStr, fontLabelVal, null,
                null));

        /* the date updated */
        add(ComponentUtil.createJLabel("Date updated: ", fontLabel, null, null));
        String dateUpdatedStr = bug != null ? df.format(bug.getDateUpdated())
                : "";
        add(ComponentUtil.createJLabel(dateUpdatedStr, fontLabelVal, null,
                null));
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String bugDetailsUrl = null;
            if (bug != null) {
                String mapdustSite = Configuration.getInstance().getMapdustBugDetailsUrl();
                bugDetailsUrl = mapdustSite + bug.getId().toString();
            }
            if (bugDetailsUrl != null) {
                try {
                    OpenBrowser.displayUrl(new URI(bugDetailsUrl));
                } catch (Exception e) {
                    String errorMessage = "Error opening the MapDust bug ";
                    errorMessage += "details page";
                    JOptionPane.showMessageDialog(Main.parent,
                            tr(errorMessage), tr("Error"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        }
    }

    /**
     * Returns a <code>Color</code> object based on the <code>MapdustBug</code>
     * relevance level. If the <code>MapdustBug</code> is null, then the
     * returned color will be black.
     *
     * @param bug The <code>MapdustBug</code> object
     * @return The corresponding <code>Color</code>
     */
    private Color getRelevanceValueColor(MapdustBug bug) {
        Color color = Color.BLACK;
        if (bug != null) {
            if (bug.getRelevance().equals(MapdustRelevance.LOW)) {
                color = Color.RED.brighter();
            }
            if (bug.getRelevance().equals(MapdustRelevance.MID_LOW)) {
                color = Color.RED.darker();
            }
            if (bug.getRelevance().equals(MapdustRelevance.MEDIUM)) {
                color = Color.ORANGE;
            }
            if (bug.getRelevance().equals(MapdustRelevance.MID_HIGH)) {
                color = Color.GREEN;
            }
            if (bug.getRelevance().equals(MapdustRelevance.HIGH)) {
                color = (Color.GREEN.darker()).darker();
            }
        }
        return color;
    }

}
