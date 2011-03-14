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
package org.openstreetmap.josm.plugins.mapdust;


import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToolTip;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.mapdust.gui.MapdustGUI;
import org.openstreetmap.josm.plugins.mapdust.service.value.MapdustBug;
import org.openstreetmap.josm.tools.ImageProvider;


/**
 * Defines the MapDust JOSM layer main functionality.
 *
 * @author Bea
 */
public class MapdustLayer extends Layer {

    /** The <code>MapdustGUI</code> object */
    private MapdustGUI mapdustGUI;

    /** The list of <code>MapdustBugList</code> objects */
    private List<MapdustBug> mapdustBugList;

    /** The selected <code>MapdustBug</code> object */
    private MapdustBug bugSelected;

    /**
     * Builds a <code>MapdustLayer</code> object based on the given parameters.
     *
     * @param name The name of the layer
     * @param mapdustGUI The <code>MapdustGUI</code> object
     * @param mapdustBugList The list of <code>MapdustBug</code> objects
     */
    public MapdustLayer(String name, MapdustGUI mapdustGUI,
            List<MapdustBug> mapdustBugList) {
        super(name);
        this.mapdustGUI = mapdustGUI;
        this.mapdustBugList = mapdustBugList;
        this.bugSelected = null;
    }

    /**
     * Returns the icon of the MapDust layer.
     *
     * @return icon
     */
    @Override
    public Icon getIcon() {
        Icon layerIcon = ImageProvider.get("dialogs/mapdust_icon16.png");
        return layerIcon;
    }

    /**
     * Returns the info components of the MapDust layer.
     *
     * @return object
     */
    @Override
    public Object getInfoComponent() {
        String infoComponent = "Shows the Mapdust bug reporter issues.";
        return tr(infoComponent);
    }

    /**
     * Returns the menu entries of the MapDust layer.
     *
     * @return an array of <code>Action</code> objects.
     */
    @Override
    public Action[] getMenuEntries() {
        Action[] menuEntries = new Action[6];
        menuEntries[0] =
                LayerListDialog.getInstance().createShowHideLayerAction();
        menuEntries[1] =
                LayerListDialog.getInstance().createDeleteLayerAction();
        menuEntries[2] = SeparatorLayerAction.INSTANCE;
        menuEntries[3] = new RenameLayerAction(null, this);
        menuEntries[4] = SeparatorLayerAction.INSTANCE;
        menuEntries[5] = new LayerListPopup.InfoAction(this);
        return menuEntries;
    }

    /**
     * Returns the text of the tool tip of the MapDust layer.
     *
     * @return the tooltip text
     */
    @Override
    public String getToolTipText() {
        String toolTipText = "Shows Mapdust bug reporter issues.";
        return tr(toolTipText);
    }

    /**
     * Returns the image icon based on the given status and type.
     *
     * @param iconType The type of the bug
     * @param status The status of the bug
     * @param bugType The type of the bug
     * @return A <code>ImageIcom</code> object
     */
    private ImageIcon getImageIcon(String iconType, String status,
            String bugType) {
        String iconName = "bugs/" + iconType + "/";
        iconName += status.toLowerCase() + "_";
        iconName += bugType;
        iconName += ".png";
        ImageIcon icon = ImageProvider.get(iconName);
        return icon;
    }

    /**
     * Draw the objects to the given map view. Also draws the MapDust bugs to
     * the map, and the tooltip for the selected MapDust bug.
     *
     * @param g The <code>Graphics2D</code> object
     * @param mv The <code>MapView</code> object
     * @param bounds The <code>Bounds</code> object
     */
    @Override
    public void paint(Graphics2D g, MapView mv, Bounds bounds) {
        JToolTip tooltip = new JToolTip();
        if (mapdustBugList != null) {
            /* draw the current visible bugs */
            for (MapdustBug bug : mapdustBugList) {
                LatLon ll = bug.getLatLon();
                Point p = mv.getPoint(ll);
                /* get the icon */
                String status = bug.getStatus().getValue();
                String type = bug.getType().getKey();
                ImageIcon icon = getImageIcon("normal", status, type);
                int width = icon.getIconWidth();
                int height = icon.getIconHeight();
                /*
                 * need to do drawing 2 times, because in some areas the bug
                 * image is invisible
                 */
                for (int i = 0; i < 2; i++) {
                    g.drawImage(icon.getImage(), p.x - (width / 2), p.y
                            - (height / 2), new ImageObserver() {

                        @Override
                        public boolean imageUpdate(Image img, int infoflags,
                                int x, int y, int width, int height) {
                            return false;
                        }
                    });
                }
            }

            /* draw the selected bug description */
            /* selected by clicking */
            MapdustBug bug1 = getBugSelected();
            /* selected from the list */
            MapdustBug bugSelected = getMapdustGUI().getSelectedBug();
            if (bugSelected == null) {
                if (Main.map.mapView.getActiveLayer() == this) {
                    bugSelected = bug1;
                }
            }
            setBugSelected(bugSelected);
            if (bugSelected != null) {
                LatLon ll = bugSelected.getLatLon();
                Point p = mv.getPoint(ll);
                String status = bugSelected.getStatus().getValue();
                String type = bugSelected.getType().getKey();
                ImageIcon icon = getImageIcon("selected", status, type);
                int width = icon.getIconWidth();
                int height = icon.getIconHeight();
                /* draw the icon */
                g.drawImage(icon.getImage(), p.x - (width / 2), p.y
                        - (height / 2), new ImageObserver() {

                    @Override
                    public boolean imageUpdate(Image img, int infoflags, int x,
                            int y, int width, int height) {
                        return false;
                    }
                });
                /* draw description */
                String text = buildTooltipText(bugSelected);
                tooltip.setTipText(text);
                tooltip.setFont(new Font("Times New Roman", Font.BOLD, 12));
                tooltip.setBackground(Color.WHITE);
                tooltip.setForeground(Color.BLUE);
                tooltip.setLocation(p);
                tooltip.setFocusable(true);
                int tx = p.x + (width / 4);
                int ty = (p.y + height / 4);
                g.translate(tx, ty);
                Dimension d = tooltip.getUI().getPreferredSize(tooltip);
                d.width = Math.min(d.width, (mv.getWidth() * 2 / 3));
                tooltip.setSize(d);
                tooltip.paint(g);
                g.translate(-tx, -ty);
            }
        }
    }

    /**
     * No need to implement this.
     */
    @Override
    public boolean isMergable(Layer layer) {
        return false;
    }

    /**
     * No need to implement this.
     */
    @Override
    public void mergeFrom(Layer layer) {}

    /**
     * Builds the text of the tooltip containing a short description of the
     * given <code>MapdustBug</code> object.
     *
     * @param bug The <code>MapdustBug</code> object
     * @return A string containing the description text
     */
    private String buildTooltipText(MapdustBug bug) {
        DateFormat df =
                DateFormat.getDateInstance(DateFormat.DEFAULT,
                        Locale.getDefault());
        String text = "<html>Type: " + bug.getType().getValue() + "<br/>";
        text += "Status: " + bug.getStatus().getValue() + "<br/>";
        text += "Address: " + bug.getAddress() + " <br/>";
        text += "Created by: " + bug.getNickname() + "<br/>";
        text += "Created on: " + df.format(bug.getDateCreated()) + "<br/>";
        text += "Last modified on: ";
        text += df.format(bug.getDateUpdated()) + "<br/>";
        text += "Comments: " + bug.getNumberOfComments();
        text += "</html>";
        return text;
    }

    /**
     * No need to implement this.
     */
    @Override
    public void visitBoundingBox(BoundingXYVisitor arg0) {}

    /**
     * Returns the <code>MapdustGUI</code> object
     *
     * @return the mapdustGUI
     */
    public MapdustGUI getMapdustGUI() {
        return mapdustGUI;
    }

    /**
     * Sets the <code>MapdustGUI</code> object
     *
     * @param mapdustGUI the mapdustGUI to set
     */
    public void setMapdustGUI(MapdustGUI mapdustGUI) {
        this.mapdustGUI = mapdustGUI;
    }

    /**
     * Returns the list of <code>MapdustBug</code> objects
     *
     * @return the mapdustBugList
     */
    public List<MapdustBug> getMapdustBugList() {
        return mapdustBugList;
    }

    /**
     * Returns the selected bug
     *
     * @return the bugSelected
     */
    public MapdustBug getBugSelected() {
        return bugSelected;
    }

    /**
     * Sets the selected bug
     *
     * @param bugSelected the bugSelected to set
     */
    public void setBugSelected(MapdustBug bugSelected) {
        this.bugSelected = bugSelected;
    }

    /**
     * Sets the list of <code>MapdustBug</code> objects
     *
     * @param mapdustBugList the mapdustBugList to set
     */
    public void setMapdustBugList(List<MapdustBug> mapdustBugList) {
        this.mapdustBugList = mapdustBugList;
    }

}
