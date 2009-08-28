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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;

// TODO: Move/Rotate/Scale action classes are similar. Do the redesign!

/**
 * This class handles the input during scaling the picture.
 */
public abstract class ScalePictureActionAbstract extends MapMode implements MouseListener, MouseMotionListener 
{
	// Scaling ongoing?
	private boolean mb_dragging = false;
	
	// Last mouse position
	private int m_prevY;
	
	// Layer we're working on
	protected PicLayerAbstract m_currentLayer = null;
	
	/**
	 * Constructor
	 */
	public ScalePictureActionAbstract (String name, String icon, String tooltip, MapFrame frame) {
		super(name, icon, tooltip, frame, ImageProvider.getCursor("crosshair", null));
		// TODO Auto-generated constructor stub
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
    	// Start scaling
    	if ( Main.map.mapView.getActiveLayer() instanceof PicLayerAbstract ) {
	        m_currentLayer = (PicLayerAbstract)Main.map.mapView.getActiveLayer();
	        
	        if ( m_currentLayer != null && e.getButton() == MouseEvent.BUTTON1 ) {
	        	mb_dragging = true;
	        	m_prevY = e.getY();
	        }
    	}
    }   
    
    @Override 
    public void mouseDragged(MouseEvent e) {
    	// Scale the picture
        if(mb_dragging) {
            doTheScale( ( e.getY() - m_prevY ) / 500.0 );
            m_prevY = e.getY();
            Main.map.mapView.repaint();
        }
    }
    
    @Override 
    public void mouseReleased(MouseEvent e) {
    	// Stop scaling
    	mb_dragging = false;
    }
    
    /**
    * Does the actual scaling in the inherited class.
    */
    protected abstract void doTheScale( double scale );

}
