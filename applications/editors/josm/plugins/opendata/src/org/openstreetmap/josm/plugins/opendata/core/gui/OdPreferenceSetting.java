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

import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.tools.GBC;

public class OdPreferenceSetting extends DefaultTabPreferenceSetting implements OdConstants {

    //private final JRadioButton rbCC43 = new JRadioButton(tr("CC43"));
    //private final JRadioButton rbWGS84 = new JRadioButton(tr("WGS84"));
    
    private final JTextField oapi = new JTextField();
    private final JTextField xapi = new JTextField();
    
    private final JCheckBox rawData = new JCheckBox(tr("Raw data"));
    
    public final JTabbedPane tabPane = new JTabbedPane();
    public JPanel masterPanel;
    
    private final ModulePreference modulePref = new ModulePreference();

    public OdPreferenceSetting() {
    	super(ICON_CORE_48, tr("OpenData Preferences"),
                tr("A special handler for various Open Data portals<br/><br/>"+
                        "Please read the Terms and Conditions of Use of each portal<br/>"+
                        "before any upload of data loaded by this plugin."));
    }
    
    /**
     * Replies the collection of module site URLs from where module lists can be downloaded
     *
     * @return
     */
    public static final Collection<String> getModuleSites() {
        return Main.pref.getCollection(PREF_MODULES_SITES, Arrays.asList(DEFAULT_MODULE_SITES));
    }

    /**
     * Sets the collection of module site URLs.
     *
     * @param sites the site URLs
     */
	public static void setModuleSites(List<String> sites) {
		Main.pref.putCollection(PREF_MODULES_SITES, sites);
    }
    
    @Override
    public void addGui(PreferenceTabbedPane gui) {
        masterPanel = gui.createPreferenceTab(this);
        modulePref.addGui(gui);
        tabPane.add(createGeneralSettings());
        
        JScrollPane scrollpane = new JScrollPane(tabPane);
        scrollpane.setBorder(BorderFactory.createEmptyBorder( 0, 0, 0, 0 ));
        masterPanel.add(scrollpane, GBC.eol().fill(GBC.BOTH));
    }

	protected JPanel createGeneralSettings() {
        JPanel general = new JPanel(new GridBagLayout());
        general.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        general.setName(tr("General settings"));

        // option to enable raw data
        rawData.setSelected(Main.pref.getBoolean(PREF_RAWDATA, DEFAULT_RAWDATA));
        rawData.setToolTipText(tr("Import only raw data (i.e. do not add/delete tags or replace them by standard OSM tags)"));
        general.add(rawData, GBC.eop().insets(0, 0, 0, 0));
        
        // separator
        general.add(new JSeparator(SwingConstants.HORIZONTAL), GBC.eol().fill(GBC.HORIZONTAL));
        
        // option to select the coordinates to use
/*        JLabel jLabelRes = new JLabel(tr("Coordinates system to read in CSV files:"));
        p.add(jLabelRes, GBC.std().insets(0, 5, 10, 0));
        ButtonGroup bgCoordinates = new ButtonGroup();
        rbCC43.setToolTipText(tr("CC43"));
        rbWGS84.setToolTipText(tr("WGS84"));
        bgCoordinates.add(rbCC43);
        bgCoordinates.add(rbWGS84);
        String currentCoordinates = Main.pref.get(PREF_COORDINATES, VALUE_CC9ZONES);
        if (currentCoordinates.equals(VALUE_WGS84))
        	rbWGS84.setSelected(true);
        else
        	rbCC43.setSelected(true);
        p.add(rbCC43, GBC.std().insets(5, 0, 5, 0));
        p.add(rbWGS84, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 5, 0, 5));*/

        // option to set the Overpass API server
        JLabel jLabelOapi = new JLabel(tr("Overpass API server:"));
        oapi.setText(Main.pref.get(PREF_OAPI, DEFAULT_OAPI));
        oapi.setToolTipText(tr("Overpass API server used to download OSM data"));
        general.add(jLabelOapi, GBC.std().insets(0, 5, 10, 0));
        general.add(oapi, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 5, 200, 5));
        
        // option to set the XAPI server
        JLabel jLabelXapi = new JLabel(tr("XAPI server:"));
        xapi.setText(Main.pref.get(PREF_XAPI, DEFAULT_XAPI));
        xapi.setToolTipText(tr("XAPI server used to download OSM data when Overpass API is not available"));
        general.add(jLabelXapi, GBC.std().insets(0, 5, 10, 0));
        general.add(xapi, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 5, 200, 5));
        
        // end of dialog, scroll bar
        general.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
        
        return general;
    }
    
    @Override
    public boolean ok() {
    	modulePref.ok();
   		//Main.pref.put(PREF_COORDINATES, rbWGS84.isSelected() ? VALUE_WGS84 : VALUE_CC9ZONES);
   		Main.pref.put(PREF_OAPI, oapi.getText());
   		Main.pref.put(PREF_XAPI, xapi.getText());
   		Main.pref.put(PREF_RAWDATA, rawData.isSelected());
        return false;
    }
}
