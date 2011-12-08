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

package org.openstreetmap.josm.plugins.piclayer.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.piclayer.layer.CalibrationFileFilter;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;

/**
 * Action for resetting properties of an image.
 *
 * TODO Four almost identical classes. Refactoring needed.
 */
@SuppressWarnings("serial")
public class SavePictureCalibrationAction extends JosmAction {

    // Owner layer of the action
    PicLayerAbstract m_owner = null;

    /**
     * Constructor
     */
    public SavePictureCalibrationAction( PicLayerAbstract owner ) {
        super(tr("Save Picture Calibration..."), null, tr("Saves calibration data to a file"), null, false);
        // Remember the owner...
        m_owner = owner;
    }

    /**
     * Action handler
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        // Save dialog
        final JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed( true );
        fc.setFileFilter( new CalibrationFileFilter() );
        fc.setSelectedFile( new File(m_owner.getPicLayerName() + CalibrationFileFilter.EXTENSION));
        int result = fc.showSaveDialog( Main.parent );

        if ( result == JFileChooser.APPROVE_OPTION ) {
            // Check file extension and force it to be valid
            File file = fc.getSelectedFile();
            String path = file.getAbsolutePath();
            if ( path.length() < CalibrationFileFilter.EXTENSION.length()
                || !path.substring( path.length() - 4 ).equals(CalibrationFileFilter.EXTENSION)) {
                file = new File( path + CalibrationFileFilter.EXTENSION );
            }

            // Save
            Properties props = new Properties();
            m_owner.saveCalibration(props);
            try {
                props.store(new FileOutputStream(file), "JOSM PicLayer plugin calibration data");
            } catch (Exception e) {
                // Error
                e.printStackTrace();
                JOptionPane.showMessageDialog(Main.parent , tr("Saving file failed: {0}", e.getMessage()));
            }
        }
    }
}
