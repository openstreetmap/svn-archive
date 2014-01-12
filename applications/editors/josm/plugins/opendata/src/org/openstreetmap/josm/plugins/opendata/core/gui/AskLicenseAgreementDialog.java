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
package org.openstreetmap.josm.plugins.opendata.core.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.opendata.core.licenses.License;

public class AskLicenseAgreementDialog extends ViewLicenseDialog {
	
	public AskLicenseAgreementDialog(License license) throws IOException {
		super(license, Main.parent, tr("License Agreement"), new String[] {tr("Accept"), "", tr("Refuse")});
		
        setToolTipTexts(new String[] {
                tr("I understand and accept these terms and conditions"),
                tr("View the full text of this license"),
                tr("I refuse these terms and conditions. Cancel download.")});
	}
}
