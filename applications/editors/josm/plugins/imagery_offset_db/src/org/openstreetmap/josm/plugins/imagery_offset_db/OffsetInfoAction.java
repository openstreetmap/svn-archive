// License: WTFPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imagery_offset_db;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.date.DateUtils;

/**
 * Display an information box for an offset.
 *
 * @author Zverik
 * @license WTFPL
 */
public class OffsetInfoAction extends AbstractAction {

    ImageryOffsetBase offset;

    /**
     * Initializes the action with an offset object.
     * Calls {@link #getInformationObject(ImageryOffsetBase)}.
     * @param offset offset object
     */
    public OffsetInfoAction(ImageryOffsetBase offset) {
        super(tr("Offset Information"));
        putValue(SMALL_ICON, ImageProvider.get("info"));
        this.offset = offset;
        setEnabled(offset != null);
    }

    /**
     * Shows a dialog with the pre-constructed message. Allows a user
     * to report the given offset.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object info = offset == null ? null : getInformationObject(offset);
        if (offset.isFlagged())
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), info, ImageryOffsetTools.DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE);
        else {
            int result = JOptionPane.showOptionDialog(MainApplication.getMainFrame(), info, ImageryOffsetTools.DIALOG_TITLE,
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new String[] {"OK", tr("Report this offset")}, null);
            if (result == 1) {
                // ask for a reason
                Object reason = JOptionPane.showInputDialog(MainApplication.getMainFrame(),
                        tr("You are to notify moderators of this offset. Why?"),
                        ImageryOffsetTools.DIALOG_TITLE, JOptionPane.PLAIN_MESSAGE);
                if (reason != null && reason.toString().length() > 0) {
                    try {
                        String query = "report?id=" + offset.getId()
                        + "&reason=" + URLEncoder.encode(reason.toString(), "UTF8");
                        SimpleOffsetQueryTask reportTask =
                                new SimpleOffsetQueryTask(query, tr("Reporting the offset..."));
                        MainApplication.worker.submit(reportTask);
                    } catch (UnsupportedEncodingException ex) {
                        Logging.error(ex);
                    }
                }
            }
        }
    }

    /**
     * Constructs a string with all information about the given offset.
     * @param offset offset object
     * @return string with all information about the given offset
     */
    public static Object getInformationObject(ImageryOffsetBase offset) {
        StringBuilder sb = new StringBuilder();
        if (offset instanceof ImageryOffset) {
            double odist = ((ImageryOffset) offset).getImageryPos().greatCircleDistance(offset.getPosition());
            if (odist < 1e-2) odist = 0.0;
            sb.append(tr("An imagery offset of {0}", ImageryOffsetTools.formatDistance(odist))).append('\n');
            sb.append(tr("Imagery ID")).append(": ").append(((ImageryOffset) offset).getImagery()).append('\n');
        } else {
            sb.append(tr("A calibration geometry of {0} nodes", ((CalibrationObject) offset).getGeometry().length)).append('\n');
        }

        double dist = ImageryOffsetTools.getMapCenter().greatCircleDistance(offset.getPosition());
        sb.append(dist < 50 ? tr("Determined right here") : tr("Determined {0} away",
                ImageryOffsetTools.formatDistance(dist)));

        sb.append("\n\n");
        sb.append(tr("Created by {0} on {1}", offset.getAuthor(),
                DateUtils.formatDate(offset.getDate(), DateFormat.DEFAULT))).append('\n');
        sb.append(tr("Description")).append(": ").append(offset.getDescription());

        if (offset.isDeprecated()) {
            sb.append("\n\n");
            sb.append(tr("Deprecated by {0} on {1}", offset.getAbandonAuthor(),
                    DateUtils.formatDate(offset.getAbandonDate(), DateFormat.DEFAULT))).append('\n');
            sb.append(tr("Reason")).append(": ").append(offset.getAbandonReason());
        }

        if (offset.isFlagged()) {
            sb.append("\n\n").append(tr("This entry has been reported."));
        }

        return sb.toString();
    }
}
