// License: GPL. For details, see LICENSE file.
package org.insignificant.josm.plugins.imagewaypoint;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;

final class ImageComponent extends JComponent {
    private static final long serialVersionUID = -5207198660736375133L;

    private Image image;

    ImageComponent() {
        this.image = null;
    }

    @Override
    public void paint(final Graphics g) {
        if (null == this.image || 0 >= this.image.getWidth(null)
            || 0 >= this.image.getHeight(null)) {
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getSize().width, this.getSize().height);
        } else {
        final int maxWidth = this.getSize().width;
        final int maxHeight = this.getSize().height;
        final int imageWidth = this.image.getWidth(null);
        final int imageHeight = this.image.getHeight(null);

        final double aspect = 1.0 * imageWidth / imageHeight;

        // what's the width if the height is 100%?
        final int widthIfHeightIsMax = (int) (aspect * maxHeight);

        // now find the real width and height
        final int resizedWidth;
        final int resizedHeight;
        if (widthIfHeightIsMax > maxWidth) {
            // oops - burst the width - so width should be the max, and
            // work out the resulting height
            resizedWidth = maxWidth;
            resizedHeight = (int) (resizedWidth / aspect);
        } else {
            // that'll do...
            resizedWidth = widthIfHeightIsMax;
            resizedHeight = maxHeight;
        }

        g.drawImage(this.image,
            (maxWidth - resizedWidth) / 2,
            (maxHeight - resizedHeight) / 2,
            resizedWidth,
            resizedHeight,
            Color.black,
            null);
        }
    }

    public void setImage(final Image image) {
        this.image = image;
        this.repaint();
    }
}
