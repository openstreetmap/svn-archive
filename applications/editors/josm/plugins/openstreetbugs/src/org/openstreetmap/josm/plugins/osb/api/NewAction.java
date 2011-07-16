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

import java.awt.Point;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.osb.ConfigKeys;
import org.openstreetmap.josm.plugins.osb.api.util.HttpUtils;

public class NewAction {

    private final String CHARSET = "UTF-8";

    public Node execute(Point p, String text) throws IOException {
        // where has the issue been added
        LatLon latlon = Main.map.mapView.getLatLon(p.x, p.y);

        // create the URI for the data download
        String uri = Main.pref.get(ConfigKeys.OSB_API_URI_NEW);

        String post = new StringBuilder("lon=")
            .append(latlon.lon())
            .append("&lat=")
            .append(latlon.lat())
            .append("&text=")
            .append(URLEncoder.encode(text, CHARSET))
            .toString();

        String result = null;
        if(Main.pref.getBoolean(ConfigKeys.OSB_API_DISABLED)) {
            result = "ok 12345";
        } else {
            result = HttpUtils.post(uri, null, post, CHARSET);
        }

        Pattern resultPattern = Pattern.compile("ok\\s+(\\d+)\\s*");
        Matcher m = resultPattern.matcher(result);
        String id = "-1";
        if(m.matches()) {
            id = m.group(1);
        } else {
            throw new RuntimeException(tr("Couldn''t create new bug. Result: {0}", result));
        }

        Node osmNode = new Node(latlon);
        osmNode.put("id", id);
        osmNode.put("note", text);
        osmNode.put("openstreetbug", "FIXME");
        osmNode.put("state", "0");
        return osmNode;
    }
}
