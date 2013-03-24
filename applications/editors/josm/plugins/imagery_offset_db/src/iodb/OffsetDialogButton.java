package iodb;

import java.awt.*;
import javax.swing.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * A button which shows offset information. Must be spectacular, since it's the only
 * non-JOptionPane GUI in the plugin.
 * 
 * @author Zverik
 * @license WTFPL
 */
public class OffsetDialogButton extends JButton {
    
    private ImageryOffsetBase offset;

    private JLabel distanceLabel;
    private DirectionIcon directionArrow;

    /**
     * Initialize the button with an offset. Calculated all relevant values.
     * @param offset An offset to display on the button.
     */
    public OffsetDialogButton( ImageryOffsetBase offset ) {
        this.offset = offset;
        layoutComponents();
    }

    /**
     * Returns the offset associated with this button.
     */
    public ImageryOffsetBase getOffset() {
        return offset;
    }

    /**
     * Update arrow for the offset location.
     */
    public void updateLocation() {
        LatLon center = ImageryOffsetTools.getMapCenter();
        directionArrow.updateIcon(center);
        double distance = center.greatCircleDistance(offset.getPosition());
        distanceLabel.setText(ImageryOffsetTools.formatDistance(distance));
    }

    /**
     * Adds a layout to this button and places labels, images and icons.
     */
    private void layoutComponents() {
        String authorAndDate = offset.isDeprecated()
                ? tr("Deprecated by {0} on {1}\n", offset.getAbandonAuthor(),
                OffsetInfoAction.DATE_FORMAT.format(offset.getAbandonDate()))
                : tr("Created by {0} on {1}\n", offset.getAuthor(),
                OffsetInfoAction.DATE_FORMAT.format(offset.getDate()));
        JLabel authorAndDateLabel = new JLabel(authorAndDate);
        Font authorFont = new Font(authorAndDateLabel.getFont().getName(), Font.ITALIC, authorAndDateLabel.getFont().getSize());
        authorAndDateLabel.setFont(authorFont);

        directionArrow = new DirectionIcon(offset.getPosition());
        distanceLabel = new JLabel("", directionArrow, SwingConstants.RIGHT);
        distanceLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        Font distanceFont = new Font(distanceLabel.getFont().getName(), Font.PLAIN, distanceLabel.getFont().getSize());
        distanceLabel.setFont(distanceFont);
        updateLocation();

        String description = offset.isDeprecated() ? offset.getAbandonReason() : offset.getDescription();
        description = description.replace("<", "&lt;").replace(">", "&gt;");
        JLabel descriptionLabel = new JLabel("<html><div style=\"width: 300px;\">"+description+"</div></html>");
        Font descriptionFont = new Font(descriptionLabel.getFont().getName(), Font.BOLD, descriptionLabel.getFont().getSize());
        descriptionLabel.setFont(descriptionFont);

        double offsetDistance = offset instanceof ImageryOffset
                ? ((ImageryOffset)offset).getImageryPos().greatCircleDistance(offset.getPosition()) : 0.0;
        JLabel offsetLabel = new JLabel(offsetDistance > 1 ? ImageryOffsetTools.formatDistance(offsetDistance) : "",
                new OffsetIcon(offset), SwingConstants.CENTER);
        Font offsetFont = new Font(offsetLabel.getFont().getName(), Font.PLAIN, offsetLabel.getFont().getSize() - 2);
        offsetLabel.setFont(offsetFont);
        offsetLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        offsetLabel.setVerticalTextPosition(SwingConstants.BOTTOM);

        Box topLine = new Box(BoxLayout.X_AXIS);
        topLine.add(authorAndDateLabel);
        topLine.add(Box.createHorizontalGlue());
        topLine.add(Box.createHorizontalStrut(10));
        topLine.add(distanceLabel);

        JPanel p = new JPanel(new BorderLayout(10, 5));
        p.setOpaque(false);
        p.add(topLine, BorderLayout.NORTH);
        p.add(offsetLabel, BorderLayout.WEST);
        p.add(descriptionLabel, BorderLayout.CENTER);
        add(p);
    }

    /**
     * Calculates length and direction for two points in the imagery offset object.
     * @see #getLengthAndDirection(iodb.ImageryOffset, double, double) 
     */
    private double[] getLengthAndDirection( ImageryOffset offset ) {
        ImageryLayer layer = ImageryOffsetTools.getTopImageryLayer();
        double[] dxy = layer == null ? new double[] {0.0, 0.0} : new double[] {layer.getDx(), layer.getDy()};
        return getLengthAndDirection(offset, dxy[0], dxy[1]);
    }

    /**
     * Calculates length and direction for two points in the imagery offset object
     * taking into account an existing imagery layer offset.
     *
     * @see #getLengthAndDirection(iodb.ImageryOffset)
     */
    public static double[] getLengthAndDirection( ImageryOffset offset, double dx, double dy ) {
        Projection proj = Main.getProjection();
        EastNorth pos = proj.latlon2eastNorth(offset.getPosition());
        LatLon correctedCenterLL = proj.eastNorth2latlon(pos.add(-dx, -dy));
        double length = correctedCenterLL.greatCircleDistance(offset.getImageryPos());
        double direction = length < 1e-2 ? 0.0 : correctedCenterLL.heading(offset.getImageryPos());
        if( direction < 0 )
            direction += Math.PI * 2;
        return new double[] {length, direction};
    }

    private static void drawArrow( Graphics g, int cx, int cy, double length, double direction ) {
        int dx = (int)Math.round(Math.sin(direction) * length / 2);
        int dy = (int)Math.round(Math.cos(direction) * length / 2);
        g.drawLine(cx - dx, cy - dy, cx + dx, cy + dy);
        double wingLength = Math.max(length / 3, 4);
        double d1 = direction - Math.PI / 6;
        int dx1 = (int)Math.round(Math.sin(d1) * wingLength);
        int dy1 = (int)Math.round(Math.cos(d1) * wingLength);
        g.drawLine(cx + dx, cy + dy, cx + dx - dx1, cy + dy - dy1);
        double d2 = direction + Math.PI / 6;
        int dx2 = (int)Math.round(Math.sin(d2) * wingLength);
        int dy2 = (int)Math.round(Math.cos(d2) * wingLength);
        g.drawLine(cx + dx, cy + dy, cx + dx - dx2, cy + dy - dy2);
    }

    /**
     * An offset icon. Displays a plain calibration icon for a geometry
     * and an arrow for an imagery offset.
     */
    private class OffsetIcon implements Icon {
        private boolean isDeprecated;
        private boolean isCalibration;
        private double direction = -1.0;
        private double length;
        private ImageIcon background;

        /**
         * Initialize the icon with an offset object. Calculates length and direction
         * of an arrow if they're needed.
         */
        public OffsetIcon( ImageryOffsetBase offset ) {
            isDeprecated = offset.isDeprecated();
            isCalibration = offset instanceof CalibrationObject;
            if( offset instanceof ImageryOffset ) {
                background = ImageProvider.get("offset");
                double[] ld = getLengthAndDirection((ImageryOffset)offset);
                length = ld[0];
                direction = ld[1];
            } else {
                background = ImageProvider.get("calibration");
            }
        }

        /**
         * Paints the base image and adds to it according to the offset.
         */
        public void paintIcon( Component comp, Graphics g, int x, int y ) {
            background.paintIcon(comp, g, x, y);

            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if( !isCalibration ) {
                g2.setColor(Color.black);
                Point c = new Point(x + getIconWidth() / 2, y + getIconHeight() / 2);
                if( length < 1e-2 ) {
                    // no offset
                    g2.fillOval(c.x - 3, c.y - 3, 7, 7);
                } else {
                    // draw an arrow
                    double arrowLength = length < 10 ? getIconWidth() / 2 - 1 : getIconWidth() - 4;
                    g2.setStroke(new BasicStroke(2));
                    drawArrow(g2, c.x, c.y, arrowLength, direction);
                }
            }
            if( isDeprecated ) {
                // big red X
                g2.setColor(Color.red);
                g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2.drawLine(x + 2, y + 2, x + getIconWidth() - 2, y + getIconHeight() - 2);
                g2.drawLine(x + 2, y + getIconHeight() - 2, x + getIconWidth() - 2, y + 2);
            }
        }

        public int getIconWidth() {
            return background.getIconWidth();
        }

        public int getIconHeight() {
            return background.getIconHeight();
        }
    }

    private class DirectionIcon implements Icon {
        private static final int SIZE = 10;

        private LatLon to;
        private double distance;
        private double direction;

        public DirectionIcon( LatLon to ) {
            this.to = to;
        }

        public void updateIcon( LatLon from ) {
            distance = from.greatCircleDistance(to);
            direction = to.heading(from);
        }

        /**
         * Paints the base image and adds to it according to the offset.
         */
        public void paintIcon( Component comp, Graphics g, int x, int y ) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.black);
            Point c = new Point(x + getIconWidth() / 2, y + getIconHeight() / 2);
            if( distance < 1 ) {
                // no offset
                int r = 2;
                g2.fillOval(c.x - r, c.y - r, r * 2 + 1, r * 2 + 1);
            } else {
                // draw an arrow
                g2.setStroke(new BasicStroke(1));
                drawArrow(g2, c.x, c.y, SIZE, direction);
            }
        }

        public int getIconWidth() {
            return SIZE;
        }

        public int getIconHeight() {
            return SIZE;
        }
    }
}
