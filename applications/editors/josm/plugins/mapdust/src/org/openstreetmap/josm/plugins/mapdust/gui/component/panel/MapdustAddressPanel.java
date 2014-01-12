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


import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JPanel;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.service.value.Address;


/**
 * Defines the JPanel for displaying the address of the <code>MapdustBug</code>
 * object.
 *
 * @author Bea
 *
 */
public class MapdustAddressPanel extends JPanel {

    /** The serial version UID */
    private static final long serialVersionUID = 8388870946800544374L;

    /**
     * Builds a new <code>MapdustBugDetailsPanel</code> object.
     *
     * @param address The address of the MapDust bug
     * @param coordinates The coordinates of the MapDust bug
     */
    public MapdustAddressPanel(Address address, LatLon coordinates) {
        setLayout(new GridLayout(8, 1));
        setBackground(Color.white);
        addComponents(address, coordinates);
    }

    /**
     * Updates the components of the <code>MapdustAddressPanel</code> based on
     * the given parameters.
     *
     * @param address The <code>Address</code> of a MapDust bug
     * @param coordinates The <code>LatLon</code> of a MapDust bug
     */
    public void updateComponents(Address address, LatLon coordinates) {
        removeAll();
        addComponents(address, coordinates);
    }

    /**
     * Creates the components of the panel, and adds to the parent panel.
     *
     * @param address The address of the <code>MapdustBug</code> object
     * @param coordinates The coordinate of the <code>MapdustBug</code> object
     */
    private void addComponents(Address address, LatLon coordinates) {
        /* the font of the label and label value */
        Font fontLabel = new Font("Times New Roman", Font.BOLD, 12);
        Font fontLabelVal = new Font("Times New Roman", Font.PLAIN, 12);

        /* country */
        add(ComponentUtil.createJLabel("Country: ", fontLabel, null, null));
        String country = address != null ? address.getCountryCode() : "";
        add(ComponentUtil.createJLabel(country, fontLabelVal, null, null));

        /* label */
        add(ComponentUtil.createJLabel("City: ", fontLabel, null, null));
        String city = address != null ? address.getCity() : "";
        add(ComponentUtil.createJLabel(city, fontLabelVal, null, null));

        /* statecode */
        add(ComponentUtil.createJLabel("State code: ", fontLabel, null, null));
        String state = address != null ? address.getStateCode() : "";
        add(ComponentUtil.createJLabel(state, fontLabelVal, null, null));

        /* label */
        add(ComponentUtil.createJLabel("Zip code: ", fontLabel, null, null));
        String zip = address != null ? address.getZipCode() : "";
        add(ComponentUtil.createJLabel(zip, fontLabelVal, null, null));

        /* street name */
        add(ComponentUtil.createJLabel("Street: ", fontLabel, null, null));
        String street = address != null ? address.getStreetName() : "";
        add(ComponentUtil.createJLabel(street, fontLabelVal, null, null));

        /* house number */
        add(ComponentUtil.createJLabel("House number: ", fontLabel, null, null));
        String houseNr = address != null ? address.getHouseNumber() : "";
        add(ComponentUtil.createJLabel(houseNr, fontLabelVal, null, null));

        /* latitude */
        add(ComponentUtil.createJLabel("Latitude: ", fontLabel, null, null));
        String lat = coordinates != null ? ("" + coordinates.lat()) : "";
        add(ComponentUtil.createJLabel(lat, fontLabelVal, null, null));

        /* longitude */
        add(ComponentUtil.createJLabel("Longitude: ", fontLabel, null, null));
        String lon = coordinates != null ? ("" + coordinates.lon()) : "";
        add(ComponentUtil.createJLabel(lon, fontLabelVal, null, null));
    }

}
