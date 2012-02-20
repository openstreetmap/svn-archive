// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.projection.Lambert;
import org.openstreetmap.josm.data.projection.LambertCC9Zones;
import org.openstreetmap.josm.data.projection.UTM_France_DOM;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.GBC;

public class MenuActionNewLocation extends JosmAction {

    private static final long serialVersionUID = 1L;

    private static final String departements[] = {
        "", tr("(optional)"),
        "001", "01 - Ain",                 "002", "02 - Aisne",              "003", "03 - Allier",                "004", "04 - Alpes de Haute-Provence", "005", "05 - Hautes-Alpes",
        "006", "06 - Alpes-Maritimes",     "007", "07 - Ard\u00eache",       "008", "08 - Ardennes",              "009", "09 - Ari\u00e8ge",             "010", "10 - Aube",
        "011", "11 - Aude",                "012", "12 - Aveyron",            "013", "13 - Bouches-du-Rh\u00f4ne", "014", "14 - Calvados",                "015", "15 - Cantal",
        "016", "16 - Charente",            "017", "17 - Charente-Maritime",  "018", "18 - Cher",                  "019", "19 - Corr\u00e8ze",
        "02A", "2A - Corse-du-Sud",        "02B", "2B - Haute-Corse",
        "021", "21 - C\u00f4te-d'Or",      "022", "22 - C\u00f4tes d'Armor", "023", "23 - Creuse",                "024", "24 - Dordogne",                "025", "25 - Doubs",
        "026", "26 - Dr\u00f4me",          "027", "27 - Eure",               "028", "28 - Eure-et-Loir",          "029", "29 - Finist\u00e8re",          "030", "30 - Gard",
        "031", "31 - Haute-Garonne",       "032", "32 - Gers",               "033", "33 - Gironde",               "034", "34 - H\u00e9rault",            "035", "35 - Ille-et-Vilaine",
        "036", "36 - Indre",               "037", "37 - Indre-et-Loire",     "038", "38 - Is\u00e8re",            "039", "39 - Jura",                    "040", "40 - Landes",
        "041", "41 - Loir-et-Cher",        "042", "42 - Loire",              "043", "43 - Haute-Loire",           "044", "44 - Loire-Atlantique",        "045", "45 - Loiret",
        "046", "46 - Lot",                 "047", "47 - Lot-et-Garonne",     "048", "48 - Loz\u00e8re",           "049", "49 - Maine-et-Loire",          "050", "50 - Manche",
        "051", "51 - Marne",               "052", "52 - Haute-Marne",        "053", "53 - Mayenne",               "054", "54 - Meurthe-et-Moselle",      "055", "55 - Meuse",
        "056", "56 - Morbihan",            "057", "57 - Moselle",            "058", "58 - Ni\u00e8vre",           "059", "59 - Nord",                    "060", "60 - Oise",
        "061", "61 - Orne",                "062", "62 - Pas-de-Calais",      "063", "63 - Puy-de-D\u00f4me",      "064", "64 - Pyr\u00e9n\u00e9es-Atlantiques", "065", "65 - Hautes-Pyr\u00e9n\u00e9es",
        "066", "66 - Pyr\u00e9n\u00e9es-Orientales", "067", "67 - Bas-Rhin", "068", "68 - Haut-Rhin",             "069", "69 - Rh\u00f4ne",              "070", "70 - Haute-Sa\u00f4ne",
        "071", "71 - Sa\u00f4ne-et-Loire", "072", "72 - Sarthe",             "073", "73 - Savoie",                "074", "74 - Haute-Savoie",            "075", "75 - Paris",
        "076", "76 - Seine-Maritime",      "077", "77 - Seine-et-Marne",     "078", "78 - Yvelines",              "079", "79 - Deux-S\u00e8vres",        "080", "80 - Somme",
        "081", "81 - Tarn",                "082", "82 - Tarn-et-Garonne",    "083", "83 - Var",                   "084", "84 - Vaucluse",                "085", "85 - Vend\u00e9e",
        "086", "86 - Vienne",              "087", "87 - Haute-Vienne",       "088", "88 - Vosges",                "089", "89 - Yonne",                   "090", "90 - Territoire de Belfort",
        "091", "91 - Essonne",             "092", "92 - Hauts-de-Seine",     "093", "93 - Seine-Saint-Denis",     "094", "94 - Val-de-Marne",            "095", "95 - Val-d'Oise",
        "971", "971 - Guadeloupe",         "972", "972 - Martinique",        "973", "973 - Guyane",               "974", "974 - R\u00e9union"
    };

    public MenuActionNewLocation() {
        super(tr("Change location"), "cadastre_small", tr("Set a new location for the next request"), null, false, "cadastrefr/newlocation", true);
    }

    public void actionPerformed(ActionEvent e) {
        WMSLayer wmsLayer = addNewLayer(new ArrayList<WMSLayer>());
        if (wmsLayer != null)
            DownloadWMSVectorImage.download(wmsLayer);
    }

    public WMSLayer addNewLayer(ArrayList<WMSLayer> existingLayers) {
        String location = "";
        String codeDepartement = "";
        String codeCommune = "";
        JLabel labelSectionNewLocation = new JLabel(tr("Add a new municipality layer"));
        JPanel p = new JPanel(new GridBagLayout());
        JLabel labelLocation = new JLabel(tr("Commune"));
        final JTextField inputTown = new JTextField( Main.pref.get("cadastrewms.location") );
        inputTown.setToolTipText(tr("<html>Enter the town,village or city name.<br>"
                + "Use the syntax and punctuation known by www.cadastre.gouv.fr .</html>"));
        JLabel labelDepartement =  new JLabel(tr("Departement"));
        final JComboBox inputDepartement = new JComboBox();
        for (int i=1; i<departements.length; i+=2) {
            inputDepartement.addItem(departements[i]);
        }
        inputDepartement.setToolTipText(tr("<html>Departement number (optional)</html>"));
        if (!Main.pref.get("cadastrewms.codeDepartement").equals("")) {
            for (int i=0; i < departements.length; i=i+2)
                if (departements[i].equals(Main.pref.get("cadastrewms.codeDepartement")))
                    inputDepartement.setSelectedIndex(i/2);
        }
        p.add(labelSectionNewLocation, GBC.eol());
        p.add(labelLocation, GBC.std().insets(10, 0, 0, 0));
        p.add(inputTown, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));
        p.add(labelDepartement, GBC.std().insets(10, 0, 0, 0));
        p.add(inputDepartement, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));
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
            codeDepartement = departements[inputDepartement.getSelectedIndex()*2];
            Main.pref.put("cadastrewms.location", location);
            Main.pref.put("cadastrewms.codeCommune", codeCommune);
            Main.pref.put("cadastrewms.codeDepartement", codeDepartement);
            if (Main.map != null) {
                for (Layer l : Main.map.mapView.getAllLayers()) {
                    if (l instanceof WMSLayer && l.getName().equalsIgnoreCase(location)) {
                        return null;
                    }
                }
            }
            // add the layer if it doesn't exist
            int zone = -1;
            if (Main.getProjection() instanceof LambertCC9Zones)
                zone = ((LambertCC9Zones)Main.getProjection()).getLayoutZone();
            else if (Main.getProjection() instanceof Lambert)
                zone = ((Lambert)Main.getProjection()).getLayoutZone();
            else if (Main.getProjection() instanceof UTM_France_DOM)
                zone = ((UTM_France_DOM)Main.getProjection()).getCurrentGeodesic();
            wmsLayer = new WMSLayer(location, codeCommune, zone);
            wmsLayer.setDepartement(codeDepartement);
            CadastrePlugin.addWMSLayer(wmsLayer);
            System.out.println("Add new layer with Location:" + inputTown.getText());
        } else if (existingLayers != null && existingLayers.size() > 0 && Main.map.mapView.getActiveLayer() instanceof WMSLayer) {
            wmsLayer = (WMSLayer)Main.map.mapView.getActiveLayer();
        }

        return wmsLayer;
    }

}
