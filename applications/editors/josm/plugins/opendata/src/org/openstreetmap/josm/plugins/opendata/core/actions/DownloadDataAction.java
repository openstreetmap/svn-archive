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
package org.openstreetmap.josm.plugins.opendata.core.actions;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Action;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.CheckParameterUtil;

public class DownloadDataAction extends JosmAction {

	private final URL url;
	
	public DownloadDataAction(String name, URL url) {
		super(false);
		CheckParameterUtil.ensureParameterNotNull(name, "name");
		CheckParameterUtil.ensureParameterNotNull(url, "url");
		putValue(Action.NAME, name);
		putValue("toolbar", "opendata_download_"+name.toLowerCase().replace(" ", "_"));
		this.url = url;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Main.main.menu.openLocation.openUrl(true, url.toString());
	}
}
