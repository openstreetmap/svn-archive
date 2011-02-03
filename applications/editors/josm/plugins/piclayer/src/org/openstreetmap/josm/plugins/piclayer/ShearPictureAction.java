/***************************************************************************
 *                                                                         *
 *   Copyright (C) 2011 Patrick "Petschge" Kilian, based on code           *
 *   (c) 2009 by Tomasz Stelmach                                           *
 *   http://www.stelmach-online.net/                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package org.openstreetmap.josm.plugins.piclayer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.data.coor.EastNorth;

//TODO: Move/Rotate/Scale/Shear action classes are similar. Do the redesign!

/**
 * This class handles the input during shearing of the picture.
 */
public class ShearPictureAction extends MapMode implements MouseListener, MouseMotionListener
{
    // Action ongoing?
    private boolean mb_dragging = false;

    // Last mouse position
    private EastNorth m_prevEastNorth;

    // The layer we're working on
    private PicLayerAbstract m_currentLayer = null;

    /**
     * Constructor
     */
    public ShearPictureAction(MapFrame frame) {
        super(tr("PicLayer shear"), "shear", tr("Drag to shear the picture"), frame, ImageProvider.getCursor("crosshair", null));
    }

    @Override
    public void enterMode() {
        super.enterMode();
        Main.map.mapView.addMouseListener(this);
        Main.map.mapView.addMouseMotionListener(this);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        Main.map.mapView.removeMouseMotionListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {

        // If everything is OK, we start dragging/moving the picture
        if ( Main.map.mapView.getActiveLayer() instanceof PicLayerAbstract ) {
            m_currentLayer = (PicLayerAbstract)Main.map.mapView.getActiveLayer();

            if ( m_currentLayer != null && e.getButton() == MouseEvent.BUTTON1 ) {
                mb_dragging = true;
                m_prevEastNorth=Main.map.mapView.getEastNorth(e.getX(),e.getY());
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Picture moving is ongoing
        if(mb_dragging) {
            EastNorth eastNorth = Main.map.mapView.getEastNorth(e.getX(),e.getY());
            m_currentLayer.shearPictureBy(
                1000* (eastNorth.east()-m_prevEastNorth.east()),
                1000* (eastNorth.north()-m_prevEastNorth.north())
            );
            m_prevEastNorth = eastNorth;
            Main.map.mapView.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Stop moving
        mb_dragging = false;
    }

}
