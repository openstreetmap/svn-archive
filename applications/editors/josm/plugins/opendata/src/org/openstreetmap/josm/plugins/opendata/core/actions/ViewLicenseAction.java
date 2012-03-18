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
import java.io.IOException;

import javax.swing.Action;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.gui.ViewLicenseDialog;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;
import org.openstreetmap.josm.tools.CheckParameterUtil;

public class ViewLicenseAction extends JosmAction implements OdConstants {

	private final License license;
	
	public ViewLicenseAction(License license, String title, String description) {
		super(title, null, description, null, false);
		CheckParameterUtil.ensureParameterNotNull(license, "license");
		this.license = license;
        putValue(Action.SMALL_ICON, OdUtils.getImageIcon(ICON_AGREEMENT_24));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			new ViewLicenseDialog(license).showDialog();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
