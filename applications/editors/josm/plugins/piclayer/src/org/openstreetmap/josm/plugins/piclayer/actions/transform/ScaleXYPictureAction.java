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
 * This class handles the input during scaling the picture.
 */
@SuppressWarnings("serial")
public class ScaleXYPictureAction extends GenericPicTransformAction {

    /**
     * Constructor
     */
    public ScaleXYPictureAction(MapFrame frame) {
        super(tr("PicLayer scale"), tr("Scaled"), "scale", tr("Drag to scale the picture in the X and Y Axis"),
                frame, ImageProvider.getCursor("crosshair", null));
    }

    @Override
    protected void doAction(MouseEvent e) {
        double centerX = MainApplication.getMap().mapView.getWidth()/2;
        double centerY = MainApplication.getMap().mapView.getHeight()/2;
        double d0 = Math.max(prevMousePoint.distance(centerX, centerY), 10);
        Point2D mousePoint = new Point(e.getX(), e.getY());
        double d = mousePoint.distance(centerX, centerY);
        double scale = Math.max(d / d0, 0.9);
        currentLayer.scalePictureBy(scale, scale);
    }
}
