//    JOSM Imagery XML Bounds plugin.
//    Copyright (C) 2011 Don-vip
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
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.imagery.ImageryInfo;
import org.openstreetmap.josm.gui.preferences.imagery.ImageryPreference.ImageryProvidersPanel.ImageryDefaultLayerTableModel;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsConstants;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsLayer;
import org.openstreetmap.josm.plugins.imageryxmlbounds.data.XmlBoundsConverter;

@SuppressWarnings("serial")
public class EditEntriesAction extends JosmAction implements XmlBoundsConstants, ListSelectionListener {

	protected static final String ACTION_NAME = tr("Edit");

	private final JTable defaultTable;
	private final ImageryDefaultLayerTableModel defaultModel;

	private final List<ImageryInfo> entries;

	public EditEntriesAction(JTable defaultTable, ImageryDefaultLayerTableModel defaultModel) {
        putValue(SHORT_DESCRIPTION, tr("edit bounds for selected defaults"));
        putValue(NAME, ACTION_NAME);
        try {
        	putValue(SMALL_ICON, XML_ICON_24);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        this.defaultModel = defaultModel;
        this.defaultTable = defaultTable;
        this.defaultTable.getSelectionModel().addListSelectionListener(this);
        this.entries = new ArrayList<ImageryInfo>();
    	setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final XmlBoundsLayer layer = new XmlBoundsLayer(
				XmlBoundsConverter.convertImageryEntries(entries));
        final Runnable uiStuff = new Runnable() {
            @Override
            public void run() {
                Main.main.addLayer(layer);
                layer.onPostLoadFromFile();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            uiStuff.run();
        } else {
            SwingUtilities.invokeLater(uiStuff);
        }
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			entries.clear();
			for (int row : defaultTable.getSelectedRows()) {
				ImageryInfo info = defaultModel.getRow(row);
				if (info != null && info.getBounds() != null) {
					entries.add(info);
				}
			}
			setEnabled(!entries.isEmpty());
		}
	}
}
