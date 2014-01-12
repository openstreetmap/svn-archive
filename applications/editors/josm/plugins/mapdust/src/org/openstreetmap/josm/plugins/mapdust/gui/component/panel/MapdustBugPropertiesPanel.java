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


import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.mapdust.gui.component.util.ComponentUtil;
import org.openstreetmap.josm.plugins.mapdust.gui.observer.MapdustBugDetailsObserver;
import org.openstreetmap.josm.plugins.mapdust.service.MapdustServiceHandler;
import org.openstreetmap.josm.plugins.mapdust.service.MapdustServiceHandlerException;
import org.openstreetmap.josm.plugins.mapdust.service.value.Address;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustComment;


/**
 * Defines the JPanel for the <code>MapdustBug</code> object properties. The
 * MapDust bug properties panel displays detailed information about a given
 * <code>MapdustBug</code> and it is composed by the following panels:
 * <code>MapdustBugDetailsPanel</code>, <code>MapdustAddressPanel</code>,
 * <code>MapdustDescriptionPanel</code>, <code>MapdustCommentsPanel</code> and
 * <code>MapdustHelpPanel</code>.
 *
 * @author Bea
 */
public class MapdustBugPropertiesPanel extends JPanel implements
        MapdustBugDetailsObserver {

    /** The serial version UID */
    private static final long serialVersionUID = 5320232823004570279L;

    /** The <code>MapdustBugDetailsPanel</code> object */
    private MapdustBugDetailsPanel detailsPanel;

    /** The <code>MapdustAddressPanel</code> object */
    private MapdustAddressPanel addressPanel;

    /** The <code>MapdustDescriptionPanel</code> object */
    private MapdustDescriptionPanel descriptionPanel;

    /** The <code>MapdustCommentsPanel</code> object */
    private MapdustCommentsPanel commentsPanel;

    /** The <code>MapdustHelpPanel</code> object */
    private MapdustHelpPanel helpPanel;

    /** The <code>JScrollPane</code> for the address */
    private JScrollPane cmpAddress;

    /** The <code>JScrollPane</code> for the details */
    private JScrollPane cmpDetails;

    /** The main panel */
    private JTabbedPane mainPanel;

    /**
     * Builds a <code>MapdustBugPropertiesPanel</code> object based on the given
     * argument.
     *
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    public MapdustBugPropertiesPanel(MapdustBug mapdustBug) {
        setLayout(new BorderLayout());
        setName("Bug Details");
        addComponents(mapdustBug);

    }

    /**
     * Displays the details of the given MapDust bug.
     *
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    @Override
    public void showDetails(MapdustBug mapdustBug) {
        MapdustBug selectedBug = mapdustBug;
        if (mapdustBug != null) {
            if (mapdustBug.getNumberOfComments() > 0) {
                Long id = mapdustBug.getId();
                selectedBug = getBug(id);
            }
        }
        int index = -1;
        /* remove components */
        if (mainPanel != null) {
            index = mainPanel.getSelectedIndex();
        }
        /* create the panels */
        createPanels(selectedBug);
        if (index != -1) {
            mainPanel.setSelectedIndex(index);
        }
    }

    /**
     * Creates and adds the components to the main panel.
     *
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    private void addComponents(MapdustBug mapdustBug) {
        MapdustBug selectedBug = mapdustBug;
        if (mapdustBug != null) {
            if (mapdustBug.getNumberOfComments() > 0) {
                Long id = mapdustBug.getId();
                selectedBug = getBug(id);
            }
        }
        createPanels(selectedBug);
    }

    /**
     * Creates the JPanels for displaying the <code>MapdustBug</code> details.
     *
     * @param mapdustBug The <code>MapdustBug</code> object
     */
    private void createPanels(MapdustBug mapdustBug) {
        /* details panel */
        if (cmpDetails == null) {
            detailsPanel = new MapdustBugDetailsPanel(mapdustBug);
            cmpDetails = ComponentUtil.createJScrollPane(detailsPanel,
                    getBounds(), getBackground(), true, true);
            cmpDetails.setPreferredSize(new Dimension(100, 100));
            cmpDetails.setName("Bug Details");
        } else {
            detailsPanel.updateComponents(mapdustBug);
        }
        /* address panel */
        Address address = mapdustBug != null ? mapdustBug.getAddress() : null;
        LatLon coordinates = mapdustBug != null ? mapdustBug.getLatLon() : null;
        if (cmpAddress == null) {
            addressPanel = new MapdustAddressPanel(address, coordinates);
            cmpAddress = ComponentUtil.createJScrollPane(addressPanel,
                    getBounds(), getBackground(), true, true);
            cmpAddress.setName("Address");
            cmpAddress.setPreferredSize(new Dimension(100, 100));
        } else {
            addressPanel.updateComponents(address, coordinates);
        }
        /* description panel */
        String description = mapdustBug != null ? mapdustBug.getDescription()
                : "";
        if (descriptionPanel == null) {
            descriptionPanel = new MapdustDescriptionPanel(description);
        } else {
            descriptionPanel.updateComponents(description);
        }
        /* comments panel */
        MapdustComment[] comments = mapdustBug != null ?
                mapdustBug.getComments() : new MapdustComment[0];
        if (commentsPanel == null) {
            commentsPanel = new MapdustCommentsPanel(comments);
        } else {
            commentsPanel.updateComponents(comments);
            mainPanel.setTitleAt(3, commentsPanel.getName());
        }
        /* the help panel */
        if (helpPanel == null) {
            helpPanel = new MapdustHelpPanel();
        }
        /* creates the main panel */
        if (mainPanel == null) {
            mainPanel = new JTabbedPane();
            mainPanel.setIgnoreRepaint(true);
            mainPanel.add(cmpDetails, 0);
            mainPanel.add(cmpAddress, 1);
            mainPanel.add(descriptionPanel, 2);
            mainPanel.add(commentsPanel, 3);
            mainPanel.add(helpPanel);
            add(mainPanel, BorderLayout.CENTER);
        } else {
            mainPanel.revalidate();
        }
    }

    /**
     * Returns the bug with the given id.
     *
     * @param id The id of the object
     * @return A <code>MapdustBug</code> object
     */
    private MapdustBug getBug(Long id) {
        MapdustBug bug = null;
        try {
            bug = new MapdustServiceHandler().getBug(id, null);
        } catch (MapdustServiceHandlerException e) {
            String errorMessage = "There was a MapDust service error durring ";
            errorMessage += " the MapDust bug retrieve process.";
            JOptionPane.showMessageDialog(Main.parent, tr(errorMessage),
                    tr("Error"), JOptionPane.ERROR_MESSAGE);
        }
        return bug;
    }

}
