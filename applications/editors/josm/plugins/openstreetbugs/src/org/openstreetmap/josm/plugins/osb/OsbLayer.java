/* Copyright (c) 2008, Henrik Niehaus
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
package org.openstreetmap.josm.plugins.osb;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolTip;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.osb.gui.action.OsbAction;
import org.openstreetmap.josm.plugins.osb.gui.action.PopupFactory;
import org.openstreetmap.josm.tools.ColorHelper;

public class OsbLayer extends Layer implements MouseListener {

    private DataSet data;

    private Collection<? extends OsmPrimitive> selection;

    private JToolTip tooltip = new JToolTip();

    public OsbLayer(DataSet dataSet, String name) {
        super(name);
        this.data = dataSet;
        DataSet.selListeners.add(new SelectionChangedListener() {
            public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
                selection = newSelection;
            }
        });

        // if the map layer has been closed, while we are requesting the osb db,
        // the mapframe is null, so we check that, before installing the mouse listener
        if(Main.map != null && Main.map.mapView != null) { 
            Main.map.mapView.addMouseListener(this);
        }
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Component[] getMenuEntries() {
        return new Component[]{
                new JMenuItem(new LayerListDialog.ShowHideLayerAction(this)),
                new JMenuItem(new LayerListDialog.DeleteLayerAction(this)),
                new JSeparator(),
                new JMenuItem(new RenameLayerAction(null, this)),
                new JSeparator(),
                new JMenuItem(new LayerListPopup.InfoAction(this))};
    }

    @Override
    public String getToolTipText() {
        return tr("Displays OpenStreetBugs issues");
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void mergeFrom(Layer from) {}

    @Override
    public void paint(Graphics g, MapView mv) {
        Object[] nodes = data.nodes.toArray();
        for (int i = 0; i < nodes.length; i++) {
            Node node = (Node) nodes[i];

            // don't paint deleted nodes
            if(node.deleted) {
                continue;
            }

            Point p = mv.getPoint(node);

            ImageIcon icon = OsbPlugin.loadIcon("icon_error16.png");
            if("1".equals(node.get("state"))) {
                icon = OsbPlugin.loadIcon("icon_valid16.png");
            }
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();

            g.drawImage(icon.getImage(), p.x - (width / 2), p.y - (height / 2), new ImageObserver() {
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    return false;
                }
            });


            if(selection != null && selection.contains(node)) {
                // draw description
                String desc = node.get("note");
                if(desc != null) {
                    // format with html
                    StringBuilder sb = new StringBuilder("<html>");
                    //sb.append(desc.replaceAll("\\|", "<br>"));
                    sb.append(desc.replaceAll("<hr />", "<hr>"));
                    sb.append("</html>");
                    desc = sb.toString();

                    // determine tooltip dimensions
                    int tooltipWidth = 0;
                    Rectangle2D fontBounds = null;
                    String[] lines = desc.split("<hr>");
                    for (int j = 0; j < lines.length; j++) {
                        String line = lines[j];
                        fontBounds = g.getFontMetrics().getStringBounds(line, g);
                        tooltipWidth = Math.max(tooltipWidth, (int)fontBounds.getWidth());
                    }

                    // FIXME hiehgt calculations doesn't work with all LAFs
                    int lineCount = lines.length;
                    int HR_SIZE = 10;
                    int tooltipHeight = lineCount * (int)fontBounds.getHeight() + HR_SIZE * (lineCount - 1);

                    // draw description as a tooltip
                    tooltip.setTipText(desc);
                    tooltip.setSize(tooltipWidth+10, tooltipHeight + 6);

                    int tx = p.x + (width / 2) + 5;
                    int ty = (int)(p.y - height / 2);
                    g.translate(tx, ty);
                    tooltip.paint(g);
                    g.translate(-tx, -ty);
                }

                // draw selection border
                g.setColor(ColorHelper.html2color(Main.pref.get("color.selected")));
                g.drawRect(p.x - (width / 2), p.y - (height / 2), 16, 16);
            }
        }
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {}

    @Override
    public Icon getIcon() {
        return OsbPlugin.loadIcon("icon_error16.png");
    }

    private Node getNearestNode(Point p) {
        double snapDistance = 10;
        double minDistanceSq = Double.MAX_VALUE;
        Node minPrimitive = null;
        for (Node n : data.nodes) {
            if (n.deleted || n.incomplete)
                continue;
            Point sp = Main.map.mapView.getPoint(n);
            double dist = p.distanceSq(sp);
            if (minDistanceSq > dist && p.distance(sp) < snapDistance) {
                minDistanceSq = p.distanceSq(sp);
                minPrimitive = n;
            }
            // prefer already selected node when multiple nodes on one point
            else if(minDistanceSq == dist && n.selected && !minPrimitive.selected)
            {
                minPrimitive = n;
            }
        }
        return minPrimitive;
    }

    public void mouseClicked(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            if(Main.map.mapView.getActiveLayer() == this) {
                Node n = (Node) getNearestNode(e.getPoint());
                if(data.nodes.contains(n)) {
                    data.setSelected(n);
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        mayTriggerPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
        mayTriggerPopup(e);
    }

    private void mayTriggerPopup(MouseEvent e) {
        if(e.isPopupTrigger()) {
            if(Main.map.mapView.getActiveLayer() == this) {
                Node n = (Node) getNearestNode(e.getPoint());
                OsbAction.setSelectedNode(n);
                if(data.nodes.contains(n)) {
                    PopupFactory.createPopup(n).show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}
}
