//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core.io;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public class OsmDownloader implements OdConstants {
	
	public static final void downloadOapi(String oapiReq) {
		if (oapiReq != null) {
			try {
				String oapiServer = Main.pref.get(PREF_OAPI, DEFAULT_OAPI);
				System.out.println(oapiReq);
				String oapiReqEnc = URLEncoder.encode(oapiReq, UTF8);
				Main.main.menu.openLocation.openUrl(false, oapiServer+"data="+oapiReqEnc);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static final void downloadXapi(Collection<String> xapiReqs) {
		if (xapiReqs != null) {
			String xapiServer = Main.pref.get(PREF_XAPI, DEFAULT_XAPI);
			for (String xapiReq : xapiReqs) {
				Main.main.menu.openLocation.openUrl(false, xapiServer+xapiReq);
			}
		}
	}
}
