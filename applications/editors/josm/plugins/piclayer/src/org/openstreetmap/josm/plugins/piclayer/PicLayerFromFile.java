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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.tools.Utils;
/**
 * Layer displaying a picture loaded from a file.
 */
public class PicLayerFromFile extends PicLayerAbstract {

    // File to load from.
    private File m_file;

    // whether the file is a zip archive
    private boolean isZip;
    // if so, what is the name of the image inside the archive?
    private String imgNameInZip;

    // Tooltip text
    private String m_tooltiptext;

    public PicLayerFromFile( File file ) {
        // Remember the file
        m_file = file;

        if ("zip".equalsIgnoreCase(getFileExtension(file))) {
            isZip = true;
        }

        // Generate tooltip text
        m_tooltiptext = m_file.getAbsolutePath();

        // Set the name of the layer as the base name of the file
        setName(m_file.getName());
    }

    @Override
    protected Image createImage() throws IOException {
        // Try to load file
        Image image = null;

        if (isZip) {
            ZipFile zipFile = null;
            try
            {
                zipFile = new ZipFile(m_file);
                ZipEntry imgEntry = null;
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                String[] supportedImageExtensions = ImageIO.getReaderFormatNames();

                while_loop:
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    for (String extension : supportedImageExtensions) {
                        if (entry.getName().endsWith("." + extension)) {
                            imgEntry = entry;
                            break while_loop;
                        }
                    }
                }
                if (imgEntry != null) {
                    imgNameInZip = imgEntry.getName();
                    InputStream is = null;
                    try {
                        is = zipFile.getInputStream(imgEntry);
                        image = ImageIO.read(is);
                        return image;
                    } finally {
                        Utils.close(is);
                    }
                }
                System.err.println("Warning: no image in zip file found");
                return null;
            } catch (Exception e) {
                System.err.println(tr("Warning: failed to handle zip file ''{0}''. Exception was: {1}", m_file.getName(), e.toString()));
                return null;
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException ex) {
                    }
                }
            }
        } else {
            image = ImageIO.read( m_file );
            return image;
        }
    }

    public enum CalibrationType { CAL, WORLDFILE }

    public static class CalData {
        public String[] imgExt;
        public String[] calExt;
        public CalibrationType type;

        public CalData(String[] imgExt, String[] calExt, CalibrationType type) {
            this.imgExt = imgExt;
            this.calExt = calExt;
            this.type = type;
        }
    }

    @Override
    protected void lookForCalibration() throws IOException {
        // Manage a potential existing calibration file

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

        if (isZip) {
            ZipFile zipFile = null;
            try
            {
                zipFile = new ZipFile(m_file);
                String calFileStr = imgNameInZip + CalibrationFileFilter.EXTENSION;
                ZipEntry calEntry = zipFile.getEntry(calFileStr);
                if (calEntry != null) {
                    if (confirmCalibrationLoading(calFileStr)) {
                        InputStream is = zipFile.getInputStream(calEntry);
                        loadCalibration(is);
                        return;
                    }
                } else {
                    int dotIdx = imgNameInZip.lastIndexOf(".");
                    if (dotIdx == -1) return;
                    String extension = imgNameInZip.substring(dotIdx);
                    String namepart = imgNameInZip.substring(0, dotIdx);
                    for (int i=0; i<imgExtensions.length; ++i) {
                        if (Arrays.asList(imgExtensions[i]).contains(extension.toLowerCase())) {
                            for (String wldExtension : wldExtensions[i]) {
                                String wldName = namepart+wldExtension;
                                ZipEntry wldEntry = zipFile.getEntry(wldName);
                                if (wldEntry != null) {
                                    if (confirmCalibrationLoading(wldName)) {
                                        InputStream is = zipFile.getInputStream(wldEntry);
                                        loadWorldfile(is);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(tr("Warning: failed to handle zip file ''{0}''. Exception was: {1}", m_file.getName(), e.toString()));
                return;
            } finally {
                if (zipFile != null) {
                    try {
                        zipFile.close();
                    } catch (IOException ex) {
                    }
                }
            }
        } else {
            File calFile = new File(m_file + CalibrationFileFilter.EXTENSION);
            if (calFile.exists()) {
                if (confirmCalibrationLoading(calFile.getName())) {
                    loadCalibration(new FileInputStream(calFile));
                }
            } else {
                int dotIdx = m_file.getName().lastIndexOf(".");
                if (dotIdx == -1) return;
                String extension = m_file.getName().substring(dotIdx);
                String namepart = m_file.getName().substring(0, dotIdx);
                for (int i=0; i<imgExtensions.length; ++i) {
                    if (Arrays.asList(imgExtensions[i]).contains(extension.toLowerCase())) {
                        for (String wldExtension : wldExtensions[i]) {
                            File wldFile = new File(m_file.getParentFile(), namepart+wldExtension);
                            if (wldFile.exists()) {
                                loadWorldfile(new FileInputStream(wldFile));
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean confirmCalibrationLoading(String fileName) {
        String prefkey = "piclayer.autoloadcal";
        String policy = Main.pref.get(prefkey, "");
        policy = policy.trim().toLowerCase();
        boolean loadcal = false;

        String msg = tr("A calibration file associated to the picture file was found:")+"\n"+fileName;
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
        return loadcal;
    }

    @Override
    protected String getPicLayerName() {
        return m_tooltiptext;
    }

    /**
     * Get the file extension
     * @param f the file
     * @return everything after the last '.'
     *         the empty string, if there is no extension
     */
    public static String getFileExtension(File f) {
        int dotIdx = f.getName().lastIndexOf('.');
        if (dotIdx == -1) return "";
        return f.getName().substring(dotIdx+1);
    }

}
