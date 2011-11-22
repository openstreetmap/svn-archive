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

import org.openstreetmap.josm.gui.MapFrame;

/**
 * This class handles the input during scaling the picture.
 */
@SuppressWarnings("serial")
public class ScaleYPictureAction extends ScalePictureActionAbstract
{
    /*
     * Constructor
     */
    public ScaleYPictureAction(MapFrame frame) {
        super(tr("PicLayer scale Y"), "scale_y", tr("Drag to scale the picture in the Y Axis"), frame);
    }

    public void doTheScale( double scale ) {
        currentLayer.scalePictureBy( 1.0, scale );
    }
}
