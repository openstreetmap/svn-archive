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

package org.openstreetmap.josm.plugins.piclayer;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;

// TODO: Move/Rotate/Scale action classes are similar. Do the redesign!

/**
 * This class handles the input during scaling the picture.
 */
public class ScaleXPictureAction extends ScalePictureActionAbstract 
{
	/*
	 * Constructor
	 */
	public ScaleXPictureAction(MapFrame frame) {
		super("PicLayer", "scale_x", "Drag to scale the picture in the X Axis", frame);
		// TODO Auto-generated constructor stub
	}

	public void doTheScale( double scale ) {
            m_currentLayer.scalePictureBy( scale, 0.0 );
        }
}
