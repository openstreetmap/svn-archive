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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.net.URI;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.util.Configuration;
import org.openstreetmap.josm.tools.OpenBrowser;


/**
 * Defines the JPanel which displays the Help.
 *
 * @author Bea
 */
public class MapdustHelpPanel extends JPanel implements HyperlinkListener {

    /** The serial version UID */
    private static final long serialVersionUID = 8366853437915060878L;

    /**
     * Builds a <code>MapdustDescriptionPanel</code> object
     */
    public MapdustHelpPanel() {
        setLayout(new BorderLayout());
        String name = "Help";
        setName(name);
        String txt = buildText();
        JEditorPane txtHelp = new JEditorPane("text/html", "");
        txtHelp.setEditorKit(new HTMLEditorKit());
        txtHelp.setEditable(false);
        txtHelp.setText(txt);
        txtHelp.addHyperlinkListener(this);
        JScrollPane cmpDescription = ComponentUtil.createJScrollPane(txtHelp,
                null, Color.white, true, true);
        cmpDescription.setPreferredSize(new Dimension(100, 100));
        add(cmpDescription, BorderLayout.CENTER);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String url = Configuration.getInstance().getMapdustWiki();
            try {
                OpenBrowser.displayUrl(new URI(url));
            } catch (Exception e) {
                String errorMessage = "Error opening the MapDust wiki page";
                JOptionPane.showMessageDialog(Main.parent, tr(errorMessage),
                        tr("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Builds the text of the Help panel. This text contains general information
     * related to the MapDust plugin.
     *
     * @return a string containing the text which will be displayed on the Help
     * tab
     */
    private String buildText() {
        Integer version = Integer.decode(Main.pref.get("mapdust.version"));
        Integer localVersion = Integer.decode(Main.pref.get("mapdust.localVersion"));
        String txt = "<html>";
        txt += "<font style='font-size:10px' face='Times New Roman'>";
        txt += "<b>You are using MapDust version ";
        txt += "<i style='color:red;font-size:10px'>";
        if (version <= localVersion) {
            txt += version + "</i>.</b><br>";
        } else {
            txt += localVersion + "</i>. There is an update available. ";
            txt += "Please update to version ";
            txt += "<i style='color:red;font-size:10px'>" + version;
            txt += "</i> to benefit from the latest improvements.</b><br>";
        }
        txt += "<b>To add bugs on the map you need to activate ";
        txt += "the MapDust layer in the Layer List Dialog.";
        txt += "Click <a href='' target='_blank'>here</a> for more help.";
        txt += "</b></font></html>";
        return txt;
    }

}
