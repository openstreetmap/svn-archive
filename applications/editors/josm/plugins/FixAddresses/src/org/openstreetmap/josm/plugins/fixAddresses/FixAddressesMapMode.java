/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.fixAddresses;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;


@SuppressWarnings("serial")
public class FixAddressesMapMode extends MapMode {

	public FixAddressesMapMode(MapFrame mapFrame) {
		super(tr("Fix addresses"), "incompleteaddress_24",
				tr("Show dialog with incomplete addresses"),
				mapFrame,
				Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}


}
