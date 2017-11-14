/***************************************************************************
 *   Copyright (C) 2009 by Tomasz Stelmach                                 *
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

package org.openstreetmap.josm.plugins.piclayer.actions.transform;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.piclayer.actions.GenericPicTransformAction;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This class handles the input during rotating the picture.
 */
@SuppressWarnings("serial")
public class RotatePictureAction extends GenericPicTransformAction {

    /**
     * Constructor
     */
    public RotatePictureAction(MapFrame frame) {
        super(tr("PicLayer rotate"), tr("Rotated"), "rotate", tr("Drag to rotate the picture"),
                frame, ImageProvider.getCursor("crosshair", null));
    }

    @Override
    protected void doAction(MouseEvent e) {
        Point2D center = new Point(MainApplication.getMap().mapView.getWidth()/2, MainApplication.getMap().mapView.getHeight()/2);
        double alpha1 = Math.atan2(e.getY() - center.getY(), e.getX() - center.getX());
        double alpha0 = Math.atan2(prevMousePoint.getY() - center.getY(), prevMousePoint.getX() - center.getX());
        currentLayer.rotatePictureBy(alpha1 - alpha0);
    }
}
