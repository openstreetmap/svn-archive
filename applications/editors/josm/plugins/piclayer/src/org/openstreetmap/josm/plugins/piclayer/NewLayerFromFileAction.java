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

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

/**
 * Action responsible for creation of a new layer based on
 * an image file.
 */
public class NewLayerFromFileAction extends JosmAction {
	
	/**
	 * Provides filtering of only image files.
	 */
	private class ImageFileFilter extends FileFilter {

		@Override
		public boolean accept(File f) {
		    
		    String ext3 = ( f.getName().length() > 4 ) ?  f.getName().substring( f.getName().length() - 4 ).toLowerCase() : "";
		    String ext4 = ( f.getName().length() > 5 ) ?  f.getName().substring( f.getName().length() - 5 ).toLowerCase() : "";

		    // TODO: check what is supported by Java :)
		    return ( f.isDirectory() 
		    	||	ext3.equals( ".jpg" )
		    	||	ext4.equals( ".jpeg" )
		    	||	ext3.equals( ".png" )
		    	);
		}


		@Override
		public String getDescription() {
			return tr("Image files");
		}
		
	}
	
	/**
	 * Constructor...
	 */
	public NewLayerFromFileAction() {
		super(tr("New picture layer from file..."), null, null, null, false);
	}

	/**
	 * Action handler
	 */
	public void actionPerformed(ActionEvent arg0) {
		
		// Choose a file
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed( false );
		fc.setFileFilter( new ImageFileFilter() );
		int result = fc.showOpenDialog( Main.parent );
		
		// Create a layer?
		if ( result == JFileChooser.APPROVE_OPTION ) {
			// Create layer from file
			PicLayerFromFile layer = new PicLayerFromFile( fc.getSelectedFile() );
			// Add layer only if successfully initialized
			try {
				layer.initialize();
			}
			catch (IOException e) {
				// Failed
				System.out.println( "NewLayerFromFileAction::actionPerformed - " + e.getMessage() );
				JOptionPane.showMessageDialog(null, e.getMessage() );  
				return;
			}
			// Add layer
			Main.main.addLayer( layer );
		}
		
	}
}
