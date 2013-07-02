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
package org.openstreetmap.josm.plugins.osb.api;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.net.URLEncoder;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.osb.ConfigKeys;
import org.openstreetmap.josm.plugins.osb.api.util.HttpUtils;

public class EditAction {

    private final String CHARSET = "UTF-8";

    public void execute(Node n, String comment) throws IOException {
        // create the URI for the data download
        String uri = Main.pref.get(ConfigKeys.OSB_API_URI_EDIT);
        String post = new StringBuilder("id=")
            .append(n.get("id"))
            .append("&text=")
            .append(URLEncoder.encode(comment, CHARSET))
            .toString();

        String result = null;
        if(Main.pref.getBoolean(ConfigKeys.OSB_API_DISABLED)) {
            result = "ok";
        } else {
            result = HttpUtils.post(uri, post, CHARSET);
        }

        if("ok".equalsIgnoreCase(result) || "comment added\n".equalsIgnoreCase(result)) {
            String desc = n.get("note");
            desc = desc.concat("<hr />").concat(comment);
            n.put("note", desc);
            Main.map.mapView.repaint();
        } else {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("An error occurred: {0}", new Object[] {result}),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
