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

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Layer displaying a picture loaded from a file.
 */
public class PicLayerFromFile extends PicLayerAbstract {
	
	// File to load from.
	private File m_file;
	// Tooltip text
	private String m_tooltiptext;

    public PicLayerFromFile( File file ) {
    	// Remember the file
    	m_file = file;
    	// Generate tooltip text
    	m_tooltiptext = m_file.getAbsolutePath();
    }	
    
	@Override
	protected Image createImage() throws IOException {
        // Try to load file
		Image image = null;
		image = ImageIO.read( m_file );
		return image;
	}

	@Override
	protected String getPicLayerName() {
		return m_tooltiptext;
	}	
}
