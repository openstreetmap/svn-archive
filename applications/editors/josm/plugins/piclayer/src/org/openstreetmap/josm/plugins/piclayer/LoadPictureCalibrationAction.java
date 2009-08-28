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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * Action for resetting properties of an image.
 * 
 * TODO Four almost identical classes. Refactoring needed.
 */
public class LoadPictureCalibrationAction extends JosmAction {

	// Owner layer of the action
	PicLayerAbstract m_owner = null;
	
	/**
	 * Constructor
	 */
	public LoadPictureCalibrationAction( PicLayerAbstract owner ) {
		super("Load Picture Calibration...", null, "Loads calibration data to a file", null, false);
		// Remember the owner...
		m_owner = owner;
	}
	
	/**
	 * Action handler
	 */
	public void actionPerformed(ActionEvent arg0) {
		// Save dialog
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed( false );
		fc.setFileFilter( new CalibrationFileFilter() );
		fc.setSelectedFile( new File(m_owner.getPicLayerName() + CalibrationFileFilter.EXTENSION));
		int result = fc.showOpenDialog(Main.parent );

		if ( result == JFileChooser.APPROVE_OPTION ) {
					
			// Load	
			try {
				Properties props = new Properties();
				props.load(new FileInputStream(fc.getSelectedFile()));
				m_owner.loadCalibration(props);
			} catch (Exception e) {
				// Error
				e.printStackTrace();
				JOptionPane.showMessageDialog(Main.parent , "Loading file failed: " + e.getMessage());
			}
		}	
	}
}
