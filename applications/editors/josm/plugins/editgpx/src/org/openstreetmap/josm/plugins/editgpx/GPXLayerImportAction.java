/**
 * License: GPL. Copyright 2008. Martin Garbe (leo at running-sheep dot com)
 *
 * other source from mesurement plugin written by Raphael Mack
 *
 */
package org.openstreetmap.josm.plugins.editgpx;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.editgpx.data.EditGpxData;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Import GPX data from available layers
 *
 *
 */
class GPXLayerImportAction extends AbstractAction {


    private static final long serialVersionUID = 5794897888911798168L;
    private EditGpxData data;
    public Object importing = new Object(); //used for synchronization

    public GPXLayerImportAction(EditGpxData data) {
        //TODO what is icon at the end?
        super(tr("Import path from GPX layer"), ImageProvider.get("dialogs", "edit"));
        this.data = data;
    }

    /**
     * shows a list of GPX layers. if user selects one the data from this layer is
     * imported.
     */
    public void activateImport() {
        Box panel = Box.createVerticalBox();
        DefaultListModel dModel= new DefaultListModel();

        final JList layerList = new JList(dModel);
        Collection<Layer> data = Main.map.mapView.getAllLayers();
        int layerCnt = 0;

        for (Layer l : data){
            if(l instanceof GpxLayer){
                dModel.addElement(l);
                layerCnt++;
            }
        }
        if(layerCnt > 0){
            layerList.setSelectionInterval(0, layerCnt-1);
            layerList.setCellRenderer(new DefaultListCellRenderer(){
                @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    Layer layer = (Layer)value;
                    JLabel label = (JLabel)super.getListCellRendererComponent(list,
                            layer.getName(), index, isSelected, cellHasFocus);
                    Icon icon = layer.getIcon();
                    label.setIcon(icon);
                    label.setToolTipText(layer.getToolTipText());
                    return label;
                }
            });

            JCheckBox dropFirst = new JCheckBox(tr("Drop existing path"));
            dropFirst.setEnabled(!this.data.getTracks().isEmpty());

            panel.add(layerList);
            panel.add(dropFirst);

            final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION){
                @Override public void selectInitialValue() {
                    layerList.requestFocusInWindow();
                }
            };
            final JDialog dlg = optionPane.createDialog(Main.parent, tr("Import path from GPX layer"));
            dlg.setVisible(true);

            Object answer = optionPane.getValue();
            if (answer == null || answer == JOptionPane.UNINITIALIZED_VALUE ||
                    (answer instanceof Integer && (Integer)answer != JOptionPane.OK_OPTION)) {
                return;
            }

            if (dropFirst.isSelected()) {
                this.data.getTracks().clear();
            }
            synchronized(importing) {
                for (Object o : layerList.getSelectedValues()) {
                    GpxLayer gpx = (GpxLayer )o;
                    this.data.load(gpx.data);
                }
            }
            Main.map.mapView.repaint();

        } else {
            // no gps layer
            JOptionPane.showMessageDialog(Main.parent,tr("No GPX data layer found."));
        }
    }

    /**
     * called when pressing "Import.." from context menu of EditGpx layer
     *
     */
    public void actionPerformed(ActionEvent arg0) {
        activateImport();
    }
}
