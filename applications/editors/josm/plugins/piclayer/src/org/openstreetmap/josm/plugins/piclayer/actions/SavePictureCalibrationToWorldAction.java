// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.piclayer.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;

/**
 * Action to export World file Calibration.
 */
public class SavePictureCalibrationToWorldAction extends JosmAction {

    // Owner layer of the action
    PicLayerAbstract m_owner = null;

    public SavePictureCalibrationToWorldAction(PicLayerAbstract owner) {
        super(tr("Export World file Calibration..."), null, tr("Saves calibration data to a world file"), null, false);
        // Remember the owner...
        m_owner = owner;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        double[] values = new double[6];
        m_owner.saveWorldFile(values);

        String picFilename = m_owner.getPicLayerName();
        String picFilenameNoext = picFilename;
        String ext = null;
        int dotPos = picFilename.lastIndexOf(".");
        if (dotPos > 0) {
            ext = picFilename.substring(dotPos+1);
            picFilenameNoext = picFilename.substring(0, dotPos);
        }
        String wext;
        if (ext == null) {
            wext = picFilenameNoext + ".wld";
        } else {
            switch (ext) {
                case "jpg": wext = "jgw"; break;
                case "jpeg": wext = "jpgw"; break;
                case "png": wext = "pgw"; break;
                case "bmp": wext = "bpw"; break;
                case "tif": wext = "tfw"; break;
                case "tiff": wext = "tifw"; break;
                default: wext = "wld";
            }
        }

        // Save dialog
        final JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed(true);
        fc.setSelectedFile(new File(picFilenameNoext + "." + wext));
        int result = fc.showSaveDialog(Main.parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            // Check file extension and force it to be valid
            File file = fc.getSelectedFile();
            String path = file.getAbsolutePath();
            if (!path.contains(".")) {
                // no extension given, add a reasonable one
                file = new File(path + "." + wext);
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < 6; i++) {
                    bw.write(Double.toString(values[i]));
                    if (i < 5) {
                        bw.newLine();
                    }
                }
            } catch (IOException e) {
                // Error
                e.printStackTrace();
                JOptionPane.showMessageDialog(Main.parent,
                        tr("Saving file failed: {0}", e.getMessage()), tr("Problem occurred"), JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
