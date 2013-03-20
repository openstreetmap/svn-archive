package iodb;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * A button which shows offset information.
 * 
 * @author zverik
 */
public class OffsetDialogButton extends JButton {
    
    private ImageryOffsetBase offset;
    private double offsetLength;
    private double distance;
    private double direction;

    public OffsetDialogButton( ImageryOffsetBase offset ) {
        super();
        setMinimumSize(new Dimension(500, 10));
        setMaximumSize(new Dimension(500, 150));
        setText("<html>"
                + Math.round(offset.getPosition().greatCircleDistance(ImageryOffsetTools.getMapCenter())) + " m: "
                + offset.getDescription() + "</html>");
        setIcon(new OffsetIcon(offset));
        this.offset = offset;

        offsetLength = offset instanceof ImageryOffset ? getLengthAndDirection((ImageryOffset)offset)[0] : 0.0;
        // todo: layout, info, map distance and direction
    }

    public ImageryOffsetBase getOffset() {
        return offset;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = 500;
        size.height = 70;
        return size;
    }

    private double[] getLengthAndDirection( ImageryOffset offset ) {
        ImageryLayer layer = ImageryOffsetTools.getTopImageryLayer();
        double[] dxy = layer == null ? new double[] {0.0, 0.0} : new double[] {layer.getDx(), layer.getDy()};
        return ImageryOffsetTools.getLengthAndDirection((ImageryOffset)offset, dxy[0], dxy[1]);
    }

    class OffsetIcon implements Icon {
        private boolean isDeprecated;
        private boolean isCalibration;
        private double direction;
        private double length;
        private ImageIcon background;

        public OffsetIcon( ImageryOffsetBase offset ) {
            isDeprecated = offset.isDeprecated();
            isCalibration = offset instanceof CalibrationObject;
            if( offset instanceof ImageryOffset ) {
                background = ImageProvider.get("offset");
                ImageryLayer layer = ImageryOffsetTools.getTopImageryLayer();
                double[] dxy = layer == null ? new double[] {0.0, 0.0} : new double[] { layer.getDx(), layer.getDy() };
                double[] ld = ImageryOffsetTools.getLengthAndDirection((ImageryOffset)offset, dxy[0], dxy[1]);
                length = ld[0];
                direction = ld[1];
            } else {
                background = ImageProvider.get("calibration");
            }
        }

        public void paintIcon( Component c, Graphics g, int x, int y ) {
            background.paintIcon(c, g, x, y);
            // todo: draw an arrow
            // todo: apply deprecation
        }

        public int getIconWidth() {
            return background.getIconWidth();
        }

        public int getIconHeight() {
            return background.getIconHeight();
        }
    }
}
