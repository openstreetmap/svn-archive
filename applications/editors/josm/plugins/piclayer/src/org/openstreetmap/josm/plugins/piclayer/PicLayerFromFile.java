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

import org.openstreetmap.josm.Main;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
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
        
        // Set the name of the layer as the base name of the file
        setName(m_file.getName());
    }

    protected String getFilePath() {
        return m_file.getAbsolutePath();
    }

    public File getDefaultCalPath() {
        File calFile = new File(m_file + CalibrationFileFilter.EXTENSION);
        return calFile;
    }
    
    @Override
    protected Image createImage() throws IOException {
        // Try to load file
        Image image = null;
        image = ImageIO.read( m_file );
        return image;
    }
    
    @Override
    protected void lookForCalibration() throws IOException {
        // Manage a potential existing calibration file
        File calFile = getDefaultCalPath();
        if ( calFile.exists() ) {
            String prefkey = "piclayer.autoloadcal";
            String policy = Main.pref.get(prefkey, "");
            policy = policy.trim().toLowerCase();
            boolean loadcal = false;

            String msg = tr("A calibration file associated to the picture file was found:")+"\n"+calFile.getName();
            if ( policy.equals("yes") ) {
                loadcal = true;
            }
            else if ( policy.equals("no") ) {
                loadcal = false;
            }
            else if ( policy.equals("ask") ) {
                msg += "\n" + tr("(set  \"{0}\"  to yes/no/ask in the preferences\n"+
                                "to control the autoloading of calibration files)", prefkey);
                msg += "\n" + tr("Do you want to apply it ?");
                int answer = JOptionPane.showConfirmDialog(Main.parent, msg, tr("Load calibration file ?"), JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    loadcal = true;
                }
            }
            else {
                msg += "\n" + tr("It will be applied automatically.");
                msg += "\n" + tr("Also, frow now on, cal files will always be loaded automatically.");
                msg += "\n" + tr("Set  \"{0}\"  to yes/no/ask in the preferences\n"+
                                "to control the autoloading of calibration files.", prefkey);
                // TODO: there should be here a yes/no dialog with a checkbox "do not ask again"
                JOptionPane.showMessageDialog(Main.parent, msg,
                    "Automatic loading of the calibration", JOptionPane.INFORMATION_MESSAGE);
                Main.pref.put(prefkey, "yes");
                loadcal = true;
            }
            if ( loadcal )
                loadCalibration(calFile);
        } else {
            
            // try to find and load world file
            int dotIdx = m_file.getName().lastIndexOf(".");
            if (dotIdx == -1) return;
            String extension = m_file.getName().substring(dotIdx);
            String namepart = m_file.getName().substring(0, dotIdx);
            String[][] imgExtensions = new String[][] {
                { ".jpg", ".jpeg" },
                { ".png" },
                { ".tif", ".tiff" },
                { ".bmp" },
            };
            String[][] wldExtensions = new String[][] {
                { ".wld", ".jgw", ".jpgw" },
                { ".wld", ".pgw", ".pngw" },
                { ".wld", ".tfw", ".tifw" },
                { ".wld", ".bmpw", ".bpw"},
            };
            for (int i=0; i<imgExtensions.length; ++i) {
                if (Arrays.asList(imgExtensions[i]).contains(extension.toLowerCase())) {
                    for (String wldExtension : wldExtensions[i]) {
                        File wldFile = new File(m_file.getParentFile(), namepart+wldExtension);
                        if (wldFile.exists()) {
                            System.out.println("Loading world file: "+wldFile);
                            loadWorldfile(wldFile);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected String getPicLayerName() {
        return m_tooltiptext;
    }
}
