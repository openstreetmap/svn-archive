// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.I18n;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Preference settings for the French Cadastre plugin
 *
 * @author Pieren <pieren3@gmail.com>
 */
public class CadastrePreferenceSetting implements PreferenceSetting {

    static final int TRANS_MIN = 1;
    static final int TRANS_MAX = 10;
    private JSlider sliderTrans = new JSlider(JSlider.HORIZONTAL, TRANS_MIN, TRANS_MAX, TRANS_MAX);

    private JTextField sourcing = new JTextField(20);

    private JCheckBox alterColors = new JCheckBox(tr("Replace original background by JOSM background color."));

    private JCheckBox reversGrey = new JCheckBox(tr("Reverse grey colors (for black backgrounds)."));

    private JCheckBox transparency = new JCheckBox(tr("Set background transparent."));

    private JCheckBox drawBoundaries = new JCheckBox(tr("Draw boundaries of downloaded data."));

    private JComboBox imageInterpolationMethod = new JComboBox();

    private JCheckBox disableImageCropping = new JCheckBox(tr("Disable image cropping during georeferencing."));

    private JCheckBox enableTableauAssemblage = new JCheckBox(tr("Use \"Tableau d''assemblage\""));

    private JCheckBox autoFirstLayer = new JCheckBox(tr("Select first WMS layer in list."));

    private JCheckBox dontUseRelation = new JCheckBox(tr("Don''t use relation for addresses (but \"addr:street\" on elements)."));

    private JRadioButton grabMultiplier1 = new JRadioButton("", true);

    private JRadioButton grabMultiplier2 = new JRadioButton("", true);

    private JRadioButton grabMultiplier3 = new JRadioButton("", true);

    private JRadioButton grabMultiplier4 = new JRadioButton("", true);

    private JRadioButton crosspiece1 = new JRadioButton(tr("off"));

    private JRadioButton crosspiece2 = new JRadioButton(tr("25 m"));

    private JRadioButton crosspiece3 = new JRadioButton(tr("50 m"));

    private JRadioButton crosspiece4 = new JRadioButton(tr("100 m"));

    private JRadioButton grabRes1 = new JRadioButton(tr("high"));

    private JRadioButton grabRes2 = new JRadioButton(tr("medium"));

    private JRadioButton grabRes3 = new JRadioButton(tr("low"));

    private JCheckBox layerLS3 = new JCheckBox(tr("water"));
    private JCheckBox layerLS2 = new JCheckBox(tr("building"));
    private JCheckBox layerLS1 = new JCheckBox(tr("symbol"));
    private JCheckBox layerParcel = new JCheckBox(tr("parcel"));
    private JCheckBox layerLabel = new JCheckBox(tr("parcel number"));
    private JCheckBox layerNumero = new JCheckBox(tr("address"));
    private JCheckBox layerLieudit = new JCheckBox(tr("locality"));
    private JCheckBox layerSection = new JCheckBox(tr("section"));
    private JCheckBox layerCommune = new JCheckBox(tr("commune"));

    static final int DEFAULT_SQUARE_SIZE = 100;
    private JTextField grabMultiplier4Size = new JTextField(5);

    private JCheckBox enableCache = new JCheckBox(tr("Enable automatic caching."));

    static final int DEFAULT_CACHE_SIZE = 0; // disabled by default
    JLabel jLabelCacheSize = new JLabel(tr("Max. cache size (in MB)"));
    private JTextField cacheSize = new JTextField(20);

    static final String DEFAULT_RASTER_DIVIDER = "5";
    private JTextField rasterDivider = new JTextField(10);

    static final int DEFAULT_CROSSPIECES = 0;

    public void addGui(final PreferenceTabbedPane gui) {

        String description = tr("A special handler of the French cadastre wms at www.cadastre.gouv.fr" + "<BR><BR>"
                + "Please read the Terms and Conditions of Use here (in French): <br>"
                + "<a href=\"http://www.cadastre.gouv.fr/scpc/html/CU_01_ConditionsGenerales_fr.html\"> "
                + "http://www.cadastre.gouv.fr/scpc/html/CU_01_ConditionsGenerales_fr.html</a> <BR>"
                + "before any upload of data created by this plugin.");
        JPanel cadastrewmsMast = gui.createPreferenceTab("cadastrewms.gif", I18n.tr("French cadastre WMS"), description);

        JPanel cadastrewms = new JPanel(new GridBagLayout());
        cadastrewms.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        // option to automatically set the source tag when uploading
        sourcing.setText(CadastrePlugin.source);
        sourcing.setToolTipText(tr("<html>Value of key \"source\" when autosourcing is enabled</html>"));
        JLabel jLabelSource = new JLabel(tr("Source"));
        cadastrewms.add(jLabelSource, GBC.eop().insets(0, 0, 0, 0));
        cadastrewms.add(sourcing, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 0, 0, 5));

        // option to alter the original colors of the wms images
        alterColors.setSelected(Main.pref.getBoolean("cadastrewms.alterColors", false));
        alterColors.setToolTipText(tr("Replace the original white background by the background color defined in JOSM preferences."));
        cadastrewms.add(alterColors, GBC.eop().insets(0, 0, 0, 0));

        // option to reverse the grey colors (to see texts background)
        reversGrey.setSelected(Main.pref.getBoolean("cadastrewms.invertGrey", false));
        reversGrey.setToolTipText(tr("Invert the original black and white colors (and all intermediate greys). Useful for texts on dark backgrounds."));
        cadastrewms.add(reversGrey, GBC.eop().insets(00, 0, 0, 0));

        // option to enable transparency
        transparency.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sliderTrans.setEnabled(transparency.isSelected());
            }
        });
        transparency.setSelected(Main.pref.getBoolean("cadastrewms.backgroundTransparent", false));
        transparency.setToolTipText(tr("Allows multiple layers stacking"));
        cadastrewms.add(transparency, GBC.eop().insets(0, 0, 0, 0));

        // slider for transparency level
        sliderTrans.setSnapToTicks(true);
        sliderTrans.setToolTipText(tr("Set WMS layers transparency. Right is opaque, left is transparent."));
        sliderTrans.setMajorTickSpacing(10);
        sliderTrans.setMinorTickSpacing(1);
        sliderTrans.setValue((int)(Float.parseFloat(Main.pref.get("cadastrewms.brightness", "1.0f"))*10));
        sliderTrans.setPaintTicks(true);
        sliderTrans.setPaintLabels(false);
        sliderTrans.setEnabled(transparency.isSelected());
        cadastrewms.add(sliderTrans, GBC.eol().fill(GBC.HORIZONTAL).insets(20, 0, 250, 0));

        // option to draw boundaries of downloaded data
        drawBoundaries.setSelected(Main.pref.getBoolean("cadastrewms.drawBoundaries", false));
        drawBoundaries.setToolTipText(tr("Draw a rectangle around downloaded data from WMS server."));
        cadastrewms.add(drawBoundaries, GBC.eop().insets(0, 0, 0, 5));

        // option to select the single grabbed image resolution
        JLabel jLabelRes = new JLabel(tr("Image resolution:"));
        cadastrewms.add(jLabelRes, GBC.std().insets(0, 5, 10, 0));
        ButtonGroup bgResolution = new ButtonGroup();
        ActionListener resActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
              AbstractButton button = (AbstractButton) actionEvent.getSource();
              grabMultiplier4Size.setEnabled(button == grabMultiplier4);
            }
          };
        grabRes1.addActionListener( resActionListener);
        grabRes1.setToolTipText(tr("High resolution (1000x800)"));
        grabRes2.addActionListener( resActionListener);
        grabRes2.setToolTipText(tr("Medium resolution (800x600)"));
        grabRes3.addActionListener( resActionListener);
        grabRes3.setToolTipText(tr("Low resolution (600x400)"));
        bgResolution.add(grabRes1);
        bgResolution.add(grabRes2);
        bgResolution.add(grabRes3);
        String currentResolution = Main.pref.get("cadastrewms.resolution", "high");
        if (currentResolution.equals("high"))
            grabRes1.setSelected(true);
        if (currentResolution.equals("medium"))
            grabRes2.setSelected(true);
        if (currentResolution.equals("low"))
            grabRes3.setSelected(true);
        cadastrewms.add(grabRes1, GBC.std().insets(5, 0, 5, 0));
        cadastrewms.add(grabRes2, GBC.std().insets(5, 0, 5, 0));
        cadastrewms.add(grabRes3, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 5, 0, 5));

        // option to select image zooming interpolation method
        JLabel jLabelImageZoomInterpolation = new JLabel(tr("Image filter interpolation:"));
        cadastrewms.add(jLabelImageZoomInterpolation, GBC.std().insets(0, 0, 10, 0));
        imageInterpolationMethod.addItem(tr("Nearest-Neighbor (fastest) [ Default ]"));
        imageInterpolationMethod.addItem(tr("Bilinear (fast)"));
        imageInterpolationMethod.addItem(tr("Bicubic (slow)"));
        String savedImageInterpolationMethod = Main.pref.get("cadastrewms.imageInterpolation", "standard");
        if (savedImageInterpolationMethod.equals("bilinear"))
            imageInterpolationMethod.setSelectedIndex(1);
        else if (savedImageInterpolationMethod.equals("bicubic"))
            imageInterpolationMethod.setSelectedIndex(2);
        else
            imageInterpolationMethod.setSelectedIndex(0);
        cadastrewms.add(imageInterpolationMethod, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 5, 200, 5));

        // separator
        cadastrewms.add(new JSeparator(SwingConstants.HORIZONTAL), GBC.eol().fill(GBC.HORIZONTAL));

        // the vectorized images multiplier
        JLabel jLabelScale = new JLabel(tr("Vector images grab multiplier:"));
        cadastrewms.add(jLabelScale, GBC.std().insets(0, 5, 10, 0));
        ButtonGroup bgGrabMultiplier = new ButtonGroup();
        ActionListener multiplierActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
              AbstractButton button = (AbstractButton) actionEvent.getSource();
              grabMultiplier4Size.setEnabled(button == grabMultiplier4);
            }
          };
        grabMultiplier1.setIcon(ImageProvider.get("preferences", "unsel_box_1"));
        grabMultiplier1.setSelectedIcon(ImageProvider.get("preferences", "sel_box_1"));
        grabMultiplier1.addActionListener( multiplierActionListener);
        grabMultiplier1.setToolTipText(tr("Grab one image full screen"));
        grabMultiplier2.setIcon(ImageProvider.get("preferences", "unsel_box_2"));
        grabMultiplier2.setSelectedIcon(ImageProvider.get("preferences", "sel_box_2"));
        grabMultiplier2.addActionListener( multiplierActionListener);
        grabMultiplier2.setToolTipText(tr("Grab smaller images (higher quality but use more memory)"));
        grabMultiplier3.setIcon(ImageProvider.get("preferences", "unsel_box_3"));
        grabMultiplier3.setSelectedIcon(ImageProvider.get("preferences", "sel_box_3"));
        grabMultiplier3.addActionListener( multiplierActionListener);
        grabMultiplier3.setToolTipText(tr("Grab smaller images (higher quality but use more memory)"));
        grabMultiplier4.setIcon(ImageProvider.get("preferences", "unsel_box_4"));
        grabMultiplier4.setSelectedIcon(ImageProvider.get("preferences", "sel_box_4"));
        grabMultiplier4.addActionListener( multiplierActionListener);
        grabMultiplier4.setToolTipText(tr("Fixed size square (default is 100m)"));
        bgGrabMultiplier.add(grabMultiplier1);
        bgGrabMultiplier.add(grabMultiplier2);
        bgGrabMultiplier.add(grabMultiplier3);
        bgGrabMultiplier.add(grabMultiplier4);
        String currentScale = Main.pref.get("cadastrewms.scale", "1");
        if (currentScale.equals(Scale.X1.value))
            grabMultiplier1.setSelected(true);
        if (currentScale.equals(Scale.X2.value))
            grabMultiplier2.setSelected(true);
        if (currentScale.equals(Scale.X3.value))
            grabMultiplier3.setSelected(true);
        if (currentScale.equals(Scale.SQUARE_100M.value))
            grabMultiplier4.setSelected(true);
        cadastrewms.add(grabMultiplier1, GBC.std().insets(5, 0, 5, 0));
        cadastrewms.add(grabMultiplier2, GBC.std().insets(5, 0, 5, 0));
        cadastrewms.add(grabMultiplier3, GBC.std().insets(5, 0, 5, 0));
        cadastrewms.add(grabMultiplier4, GBC.std().insets(5, 0, 5, 0));
        int squareSize = getNumber("cadastrewms.squareSize", DEFAULT_SQUARE_SIZE);
        grabMultiplier4Size.setText(String.valueOf(squareSize));
        grabMultiplier4Size.setToolTipText(tr("Fixed size (from 25 to 1000 meters)"));
        grabMultiplier4Size.setEnabled(currentScale.equals(Scale.SQUARE_100M.value));
        cadastrewms.add(grabMultiplier4Size, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 5, 0, 5));

        // WMS layers selection
        JLabel jLabelLayers = new JLabel(tr("Layers:"));
        cadastrewms.add(jLabelLayers, GBC.std().insets(0, 5, 10, 0));
        layerLS3.setSelected(Main.pref.getBoolean("cadastrewms.layerWater", true));
        layerLS3.setToolTipText(tr("See, rivers, swimming pools."));
        cadastrewms.add(layerLS3, GBC.std().insets(5, 0, 5, 0));
        layerLS2.setSelected(Main.pref.getBoolean("cadastrewms.layerBuilding", true));
        layerLS2.setToolTipText(tr("Buildings, covers, underground constructions."));
        cadastrewms.add(layerLS2, GBC.std().insets(5, 0, 5, 0));
        layerLS1.setSelected(Main.pref.getBoolean("cadastrewms.layerSymbol", true));
        layerLS1.setToolTipText(tr("Symbols like cristian cross."));
        cadastrewms.add(layerLS1, GBC.std().insets(5, 0, 5, 0));
        layerParcel.setSelected(Main.pref.getBoolean("cadastrewms.layerParcel", true));
        layerParcel.setToolTipText(tr("Parcels."));
        cadastrewms.add(layerParcel, GBC.eop().insets(5, 0, 5, 0));
        layerLabel.setSelected(Main.pref.getBoolean("cadastrewms.layerLabel", true));
        layerLabel.setToolTipText(tr("Parcels numbers, street names."));
        cadastrewms.add(layerLabel, GBC.std().insets(70, 0, 5, 0));
        layerNumero.setSelected(Main.pref.getBoolean("cadastrewms.layerNumero", true));
        layerNumero.setToolTipText(tr("Address, houses numbers."));
        cadastrewms.add(layerNumero, GBC.std().insets(5, 0, 5, 0));
        layerLieudit.setSelected(Main.pref.getBoolean("cadastrewms.layerLieudit", true));
        layerLieudit.setToolTipText(tr("Locality, hamlet, place."));
        cadastrewms.add(layerLieudit, GBC.std().insets(5, 0, 5, 0));
        layerSection.setSelected(Main.pref.getBoolean("cadastrewms.layerSection", true));
        layerSection.setToolTipText(tr("Cadastral sections and subsections."));
        cadastrewms.add(layerSection, GBC.std().insets(5, 0, 5, 0));
        layerCommune.setSelected(Main.pref.getBoolean("cadastrewms.layerCommune", true));
        layerCommune.setToolTipText(tr("Municipality administrative borders."));
        cadastrewms.add(layerCommune, GBC.eop().insets(5, 0, 5, 0));

        // separator
        cadastrewms.add(new JSeparator(SwingConstants.HORIZONTAL), GBC.eol().fill(GBC.HORIZONTAL));

        // for raster images (not vectorized), image grab divider (from 1 to 12)
        String savedRasterDivider = Main.pref.get("cadastrewms.rasterDivider", DEFAULT_RASTER_DIVIDER);
        JLabel jLabelRasterDivider = new JLabel(tr("Raster images grab multiplier:"));
        rasterDivider.setText(savedRasterDivider);
        rasterDivider.setToolTipText("Raster image grab division, from 1 to 12; 12 is very high definition");
        cadastrewms.add(jLabelRasterDivider, GBC.std().insets(0, 5, 10, 0));
        cadastrewms.add(rasterDivider, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 5, 200, 5));
        // option to disable image cropping during raster image georeferencing
        disableImageCropping.setSelected(Main.pref.getBoolean("cadastrewms.noImageCropping", false));
        disableImageCropping.setToolTipText(tr("Disable image cropping during georeferencing."));
        cadastrewms.add(disableImageCropping, GBC.std().insets(0, 0, 10, 0));
        // option to add the "Tableau d'assemblage" in list of sheets to grab
        enableTableauAssemblage.setSelected(Main.pref.getBoolean("cadastrewms.useTA", false));
        enableTableauAssemblage.setToolTipText(tr("Add the \"Tableau(x) d''assemblage\" in the list of cadastre sheets to grab."));
        cadastrewms.add(enableTableauAssemblage, GBC.eop().insets(0, 0, 0, 0));
        // the crosspiece display
        JLabel jLabelCrosspieces = new JLabel(tr("Display crosspieces:"));
        cadastrewms.add(jLabelCrosspieces, GBC.std().insets(0, 0, 10, 0));
        ButtonGroup bgCrosspieces = new ButtonGroup();
        int crosspieces = getNumber("cadastrewms.crosspieces", DEFAULT_CROSSPIECES);
        if (crosspieces == 0) crosspiece1.setSelected(true);
        if (crosspieces == 1) crosspiece2.setSelected(true);
        if (crosspieces == 2) crosspiece3.setSelected(true);
        if (crosspieces == 3) crosspiece4.setSelected(true);
        bgCrosspieces.add(crosspiece1);
        bgCrosspieces.add(crosspiece2);
        bgCrosspieces.add(crosspiece3);
        bgCrosspieces.add(crosspiece4);
        cadastrewms.add(crosspiece1, GBC.std().insets(5, 0, 5, 0));
        cadastrewms.add(crosspiece2, GBC.std().insets(5, 0, 5, 0));
        cadastrewms.add(crosspiece3, GBC.std().insets(5, 0, 5, 0));
        cadastrewms.add(crosspiece4, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 5, 0, 5));

        // separator
        cadastrewms.add(new JSeparator(SwingConstants.HORIZONTAL), GBC.eol().fill(GBC.HORIZONTAL));

        // option to enable automatic caching
        enableCache.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jLabelCacheSize.setEnabled(enableCache.isSelected());
                cacheSize.setEnabled(enableCache.isSelected());
            }
        });
        enableCache.setSelected(Main.pref.getBoolean("cadastrewms.enableCaching", true));
        enableCache.setToolTipText(tr("Allows an automatic caching"));
        cadastrewms.add(enableCache, GBC.eop().insets(0, 0, 0, 0));

        // option to fix the cache size(in MB)
        int size = getNumber("cadastrewms.cacheSize", DEFAULT_CACHE_SIZE);
        cacheSize.setText(String.valueOf(size));
        cacheSize.setToolTipText(tr("Oldest files are automatically deleted when this size is exceeded"));
        cadastrewms.add(jLabelCacheSize, GBC.std().insets(20, 0, 0, 0));
        cadastrewms.add(cacheSize, GBC.eol().fill(GBC.HORIZONTAL).insets(5, 5, 200, 5));

        // separator
        cadastrewms.add(new JSeparator(SwingConstants.HORIZONTAL), GBC.eol().fill(GBC.HORIZONTAL));

        // option to select the first WMS layer
        autoFirstLayer.setSelected(Main.pref.getBoolean("cadastrewms.autoFirstLayer", false));
        autoFirstLayer.setToolTipText(tr("Automatically selects the first WMS layer if multiple layers exist when grabbing."));
        cadastrewms.add(autoFirstLayer, GBC.eop().insets(0, 0, 0, 0));

        // separator
        cadastrewms.add(new JSeparator(SwingConstants.HORIZONTAL), GBC.eol().fill(GBC.HORIZONTAL));

        // option to use or not relations in addresses
        dontUseRelation.setSelected(Main.pref.getBoolean("cadastrewms.addr.dontUseRelation", false));
        dontUseRelation.setToolTipText(tr("Enable this to use the tag \"add:street\" on nodes."));
        cadastrewms.add(dontUseRelation, GBC.eop().insets(0, 0, 0, 0));

        // end of dialog, scroll bar
        cadastrewms.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
        JScrollPane scrollpane = new JScrollPane(cadastrewms);
        scrollpane.setBorder(BorderFactory.createEmptyBorder( 0, 0, 0, 0 ));
        cadastrewmsMast.add(scrollpane, GBC.eol().fill(GBC.BOTH));
    }

    public boolean ok() {
        Main.pref.put("cadastrewms.source", sourcing.getText());
        CadastrePlugin.source = sourcing.getText();
        Main.pref.put("cadastrewms.alterColors", alterColors.isSelected());
        Main.pref.put("cadastrewms.invertGrey", reversGrey.isSelected());
        Main.pref.put("cadastrewms.backgroundTransparent", transparency.isSelected());
        Main.pref.put("cadastrewms.brightness", Float.toString((float)sliderTrans.getValue()/10));
        Main.pref.put("cadastrewms.drawBoundaries", drawBoundaries.isSelected());
        if (grabRes1.isSelected())
            Main.pref.put("cadastrewms.resolution", "high");
        else if (grabRes2.isSelected())
            Main.pref.put("cadastrewms.resolution", "medium");
        else if (grabRes3.isSelected())
            Main.pref.put("cadastrewms.resolution", "low");
        if (imageInterpolationMethod.getSelectedIndex() == 2)
            Main.pref.put("cadastrewms.imageInterpolation", "bicubic");
        else if (imageInterpolationMethod.getSelectedIndex() == 1)
            Main.pref.put("cadastrewms.imageInterpolation", "bilinear");
        else
            Main.pref.put("cadastrewms.imageInterpolation", "standard");
        if (grabMultiplier1.isSelected())
            Main.pref.put("cadastrewms.scale", Scale.X1.toString());
        else if (grabMultiplier2.isSelected())
            Main.pref.put("cadastrewms.scale", Scale.X2.toString());
        else if (grabMultiplier3.isSelected())
            Main.pref.put("cadastrewms.scale", Scale.X3.toString());
        else {
            Main.pref.put("cadastrewms.scale", Scale.SQUARE_100M.toString());
            try {
                int squareSize = Integer.parseInt(grabMultiplier4Size.getText());
                if (squareSize >= 25 && squareSize <= 1000)
                    Main.pref.put("cadastrewms.squareSize", grabMultiplier4Size.getText());
            } catch (NumberFormatException e) { // ignore the last input
            }
        }
        Main.pref.put("cadastrewms.layerWater", layerLS3.isSelected());
        Main.pref.put("cadastrewms.layerBuilding", layerLS2.isSelected());
        Main.pref.put("cadastrewms.layerSymbol", layerLS1.isSelected());
        Main.pref.put("cadastrewms.layerParcel", layerParcel.isSelected());
        Main.pref.put("cadastrewms.layerLabel", layerLabel.isSelected());
        Main.pref.put("cadastrewms.layerNumero", layerNumero.isSelected());
        Main.pref.put("cadastrewms.layerLieudit", layerLieudit.isSelected());
        Main.pref.put("cadastrewms.layerSection", layerSection.isSelected());
        Main.pref.put("cadastrewms.layerCommune", layerCommune.isSelected());
        try {
            int i = Integer.parseInt(rasterDivider.getText());
            if (i > 0 && i < 13)
                Main.pref.put("cadastrewms.rasterDivider", String.valueOf(i));
        } catch (NumberFormatException e) { // ignore the last input
        }
        Main.pref.put("cadastrewms.noImageCropping", disableImageCropping.isSelected());
        Main.pref.put("cadastrewms.useTA", enableTableauAssemblage.isSelected());
        if (crosspiece1.isSelected()) Main.pref.put("cadastrewms.crosspieces", "0");
        else if (crosspiece2.isSelected()) Main.pref.put("cadastrewms.crosspieces", "1");
        else if (crosspiece3.isSelected()) Main.pref.put("cadastrewms.crosspieces", "2");
        else if (crosspiece4.isSelected()) Main.pref.put("cadastrewms.crosspieces", "3");
        Main.pref.put("cadastrewms.enableCaching", enableCache.isSelected());

        // spread data into objects instead of restarting the application
        try {
            CacheControl.cacheSize = Integer.parseInt(cacheSize.getText());
            Main.pref.put("cadastrewms.cacheSize", String.valueOf(CacheControl.cacheSize));
        } catch (NumberFormatException e) { // ignore the last input
        }
        Main.pref.put("cadastrewms.autoFirstLayer", autoFirstLayer.isSelected());
        CacheControl.cacheEnabled = enableCache.isSelected();
        Main.pref.put("cadastrewms.addr.dontUseRelation", dontUseRelation.isSelected());
        CadastrePlugin.refreshConfiguration();
        CadastrePlugin.refreshMenu();

        return false;
    }

    private int getNumber(String pref_parameter, int def_value) {
        try {
            return Integer.parseInt(Main.pref.get(pref_parameter, String.valueOf(def_value)));
        } catch (NumberFormatException e) {
            return def_value;
        }
    }
}
