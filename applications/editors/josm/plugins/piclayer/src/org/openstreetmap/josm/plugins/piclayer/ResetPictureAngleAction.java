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
import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * Action for resetting properties of an image.
 * 
 * TODO Four almost identical classes. Refactoring needed.
 */
public class ResetPictureAngleAction extends JosmAction {

	// Owner layer of the action
	PicLayerAbstract m_owner = null;
	
	/**
	 * Constructor
	 */
	public ResetPictureAngleAction( PicLayerAbstract owner ) {
		super("Angle", null, "Resets picture rotation", null, false);
		// Remember the owner...
		m_owner = owner;
	}
	
	/**
	 * Action handler
	 */
	public void actionPerformed(ActionEvent arg0) {
		// Reset
		m_owner.resetAngle();
		// Redraw
        Main.map.mapView.repaint();
	}
}
