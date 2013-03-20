package iodb;

import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Download a list of imagery offsets for the current position, let user choose which one to use.
 * 
 * @author zverik
 */
public class OffsetInfoAction extends AbstractAction {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private Object info;
    
    public OffsetInfoAction( ImageryOffsetBase offset ) {
        super(tr("Offset Information"));
        putValue(SMALL_ICON, ImageProvider.get("info"));
        if( offset != null )
            this.info = getInformationObject(offset);
        setEnabled(offset != null);
    }

    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(Main.parent, info, ImageryOffsetTools.DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE);
    }

    public static Object getInformationObject( ImageryOffsetBase offset ) {
        StringBuilder sb = new StringBuilder();
        if( offset instanceof ImageryOffset ) {
            double[] ld = getLengthAndDirection((ImageryOffset)offset);
            sb.append(tr("An imagery offset of {0} m to {1}", ld[0], explainDirection(ld[1]))).append('\n');
            sb.append("Imagery ID: ").append(((ImageryOffset)offset).getImagery());
        } else
            sb.append(tr("A calibration {0}", getGeometryType((CalibrationObject)offset)));
        sb.append("\n\nCreated by ").append(offset.getAuthor());
        sb.append(" on ").append(DATE_FORMAT.format(offset.getDate())).append("\n");
        sb.append("Description: ").append(offset.getDescription());
        if( offset.isDeprecated() ) {
            sb.append("\n\nThis geometry was marked obsolete\n");
            sb.append("by ").append(offset.getAbandonAuthor());
            sb.append(" on ").append(DATE_FORMAT.format(offset.getAbandonDate())).append("\n");
            sb.append("Reason: ").append(offset.getAbandonReason());
        }
        return sb.toString();
    }

    public static String getGeometryType( CalibrationObject obj ) {
        if( obj.getGeometry() == null )
            return tr("nothing");
        int n = obj.getGeometry().length;
        if( n == 1 )
            return tr("point");
        else if( n > 1 && !obj.getGeometry()[0].equals(obj.getGeometry()[n - 1]) )
            return tr("path ({0} nodes)", n);
        else if( n > 1 && obj.getGeometry()[0].equals(obj.getGeometry()[n - 1]) )
            return tr("polygon ({0} nodes)", n - 1);
        else
            return "geometry";
    }

    public static double[] getLengthAndDirection( ImageryOffset offset ) {
        return getLengthAndDirection(offset, 0.0, 0.0);
    }

    public static double[] getLengthAndDirection( ImageryOffset offset, double dx, double dy ) {
        double length = 0.0;
        double direction = 0.0;
        // todo: calculate length and angular direction
        return new double[] { length, direction };
    }

    public static String explainDirection( double dir ) {
        return "nowhere"; // todo
    }
}
