package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.projection.Lambert;
import org.openstreetmap.josm.data.projection.LambertCC9Zones;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.GBC;

public class MenuActionNewLocation extends JosmAction {

    private static final long serialVersionUID = 1L;

    public MenuActionNewLocation() {
        super(tr("Change location"), "cadastre_small", tr("Set a new location for the next request"), null, false);
    }

    public void actionPerformed(ActionEvent e) {
        WMSLayer wmsLayer = addNewLayer(new ArrayList<WMSLayer>());
        if (wmsLayer != null)
            DownloadWMSVectorImage.download(wmsLayer);
    }

    public WMSLayer addNewLayer(ArrayList<WMSLayer> existingLayers) {
        /*if (Main.map == null) {
            JOptionPane.showMessageDialog(Main.parent,
                    tr("Open a layer first (GPX, OSM, cache)"));
            return null;
        } else {*/
            String location = "";
            String codeDepartement = "";
            String codeCommune = "";
            boolean resetCookie = false;
            JLabel labelSectionNewLocation = new JLabel(tr("Add a new layer"));
            JPanel p = new JPanel(new GridBagLayout());
            JLabel labelLocation = new JLabel(tr("Location"));
            final JTextField inputTown = new JTextField( Main.pref.get("cadastrewms.location") );
            inputTown.setToolTipText(tr("<html>Enter the town,village or city name.<br>"
                    + "Use the syntax and punctuation known by www.cadastre.gouv.fr .</html>"));

            p.add(labelSectionNewLocation, GBC.eol());
            p.add(labelLocation, GBC.std().insets(10, 0, 0, 0));
            p.add(inputTown, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));
            JOptionPane pane = new JOptionPane(p, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null) {
                private static final long serialVersionUID = 1L;

                @Override
                public void selectInitialValue() {
                    inputTown.requestFocusInWindow();
                    inputTown.selectAll();
                }
            };
            pane.createDialog(Main.parent, tr("Add new layer")).setVisible(true);
            if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(pane.getValue()))
                return null;

            WMSLayer wmsLayer = null;
            if (!inputTown.getText().equals("")) {
                location = inputTown.getText().toUpperCase();
                resetCookie = true;
                Main.pref.put("cadastrewms.location", location);
                Main.pref.put("cadastrewms.codeCommune", codeCommune);
                if (Main.map != null) {
                    for (Layer l : Main.map.mapView.getAllLayers()) {
                        if (l instanceof WMSLayer && l.getName().equalsIgnoreCase(location + codeDepartement)) {
                            return null;
                        }
                    }
                }
                // add the layer if it doesn't exist
                if (Main.proj instanceof LambertCC9Zones)
                    wmsLayer = new WMSLayer(location, codeCommune, LambertCC9Zones.layoutZone);
                else
                    wmsLayer = new WMSLayer(location, codeCommune, Lambert.layoutZone);
                Main.main.addLayer(wmsLayer);
                System.out.println("Add new layer with Location:" + inputTown.getText());
            } else if (existingLayers != null && existingLayers.size() > 0 && Main.map.mapView.getActiveLayer() instanceof WMSLayer) {
                wmsLayer = (WMSLayer)Main.map.mapView.getActiveLayer();
                resetCookie = true;
            }

            if (resetCookie)
                CadastrePlugin.cadastreGrabber.getWmsInterface().resetCookieIfNewLayer(wmsLayer.getName());
            return wmsLayer;
        //}
    }

}
