package org.openstreetmap.josm.plugins.piclayer.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.FileInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;

@SuppressWarnings("serial")
public class LoadPictureCalibrationFromWorldAction extends JosmAction {

    private PicLayerAbstract layer;

    public LoadPictureCalibrationFromWorldAction(PicLayerAbstract layer) {
        super(tr("Load World File Calibration..."), null, tr("Loads calibration data from a world file"), null, false);

        this.layer = layer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        JFileChooser fc = new JFileChooser();
        fc.setAcceptAllFileFilterUsed( true );
        int result = fc.showOpenDialog(Main.parent );

        if ( result == JFileChooser.APPROVE_OPTION ) {

            // Load
            try {
                layer.loadWorldfile(new FileInputStream(fc.getSelectedFile()));
            } catch (Exception ex) {
                // Error
                ex.printStackTrace();
                JOptionPane.showMessageDialog(Main.parent , tr("Loading file failed: {0}", ex.getMessage()));
            }
        }
    }

}
