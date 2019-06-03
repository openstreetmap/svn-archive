// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.piclayer.layer.CalibrationFileFilter;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;

/**
 * Action for resetting properties of an image.
 *
 * TODO Four almost identical classes. Refactoring needed.
 */
public class SavePictureCalibrationAction extends JosmAction {

    // Owner layer of the action
    PicLayerAbstract m_owner = null;

    /**
     * Constructor
     * @param owner Owner layer of the action
     */
    public SavePictureCalibrationAction(PicLayerAbstract owner) {
        super(tr("Save Picture Calibration..."), null, tr("Saves calibration data to a file"), null, false);
        // Remember the owner...
        m_owner = owner;
    }

    /**
     * Action handler
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        // Save dialog
        final JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new CalibrationFileFilter());
        fc.setSelectedFile(new File(m_owner.getPicLayerName() + CalibrationFileFilter.EXTENSION));
        int result = fc.showSaveDialog(MainApplication.getMainFrame());

        if (result == JFileChooser.APPROVE_OPTION) {
            // Check file extension and force it to be valid
            File file = fc.getSelectedFile();
            String path = file.getAbsolutePath();
            if (path.length() < CalibrationFileFilter.EXTENSION.length()
                || !path.substring(path.length() - 4).equals(CalibrationFileFilter.EXTENSION)) {
                file = new File(path + CalibrationFileFilter.EXTENSION);
            }

            // Save
            Properties props = new Properties();
            m_owner.saveCalibration(props);
            try {
                props.store(new FileOutputStream(file), "JOSM PicLayer plugin calibration data");
            } catch (Exception e) {
                // Error
                e.printStackTrace();
                JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                        tr("Saving file failed: {0}", e.getMessage()), tr("Problem occurred"), JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
