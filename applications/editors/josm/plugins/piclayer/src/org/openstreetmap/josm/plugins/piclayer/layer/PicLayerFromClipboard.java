// License: GPL. For details, see LICENSE file.
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
        if (t == null) {
            throw new IOException(tr("Nothing in clipboard"));
        }

        // TODO: Why is it so slow?
        // Try to make it an image data
        try {
            if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                image = (Image) t.getTransferData(DataFlavor.imageFlavor);
            } else {
                throw new IOException(tr("The clipboard data is not an image"));
            }
        } catch (UnsupportedFlavorException e) {
            throw new IOException(e.getMessage());
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
