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
package org.openstreetmap.josm.plugins.piclayer.layer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Filter for the file dialog. Allows only calibration files.
 */
public class CalibrationFileFilter extends FileFilter {

    // Extension used by calibration files
    public static final String EXTENSION = ".cal";

    @Override
    public boolean accept(File f) {
        String ext3 = ( f.getName().length() > 4 ) ?  f.getName().substring( f.getName().length() - 4 ).toLowerCase() : "";

        // TODO: check what is supported by Java :)
        return ( f.isDirectory()
            ||  ext3.equals( EXTENSION )
            );
    }

    @Override
    public String getDescription() {
        return tr("Calibration Files")+ " (*" + EXTENSION + ")";
    }
}
