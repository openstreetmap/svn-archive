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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Layer displaying a picture copied from the clipboard.
 */
public class PicLayerFromClipboard extends PicLayerAbstract {

    @Override
    protected Image createImage() throws IOException {
        // Return item
        Image image = null;
        // Access the clipboard
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        // Check result
        if ( t == null ) {
            throw new IOException(tr("Nothing in clipboard"));
        }

        // TODO: Why is it so slow?
        // Try to make it an image data
        try {
            if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                image = (Image)t.getTransferData(DataFlavor.imageFlavor);
            } else {
                throw new IOException(tr("The clipboard data is not an image"));
            }
        } catch (UnsupportedFlavorException e) {
            throw new IOException( e.getMessage() );
        }

        return image;
    }

    @Override
    public String getPicLayerName() {
        return "Clipboard";
    }

    @Override
    protected void lookForCalibration() throws IOException {
    }

}
