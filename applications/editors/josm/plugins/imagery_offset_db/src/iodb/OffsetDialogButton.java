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
        this.offset = offset;
        setMinimumSize(new Dimension(500, 10));
        setMaximumSize(new Dimension(500, 150));
        setRelevantText();
        setIcon(new OffsetIcon(offset));

        offsetLength = offset instanceof ImageryOffset ? getLengthAndDirection((ImageryOffset)offset)[0] : 0.0;
        // todo: layout, info, map distance and direction
    }

    /**
     * Update text on the button. This method is to be deleted by release.
     */
    public void setRelevantText() {
        setText("<html>"
                + ImageryOffsetTools.formatDistance(offset.getPosition().greatCircleDistance(ImageryOffsetTools.getMapCenter()))
                + ": " + offset.getDescription() + "</html>");
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

    /**
     * Update arrow for the offset location.
     */
    public void updateLocation() {
        // map was moved, update arrow.
        setRelevantText();
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
