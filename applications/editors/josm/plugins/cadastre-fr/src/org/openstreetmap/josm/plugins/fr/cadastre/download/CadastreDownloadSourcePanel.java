// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.download;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.NamedColorProperty;
import org.openstreetmap.josm.gui.download.AbstractDownloadSourcePanel;
import org.openstreetmap.josm.gui.download.DownloadDialog;
import org.openstreetmap.josm.gui.download.DownloadSettings;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * The GUI representation of the OSM download source.
 * @since 12652
 */
public class CadastreDownloadSourcePanel extends AbstractDownloadSourcePanel<CadastreDownloadData> {
    /**
     * The simple name for the {@link CadastreDownloadSourcePanel}
     */
    public static final String SIMPLE_NAME = "cadastredownloadpanel";

    /**
     * The color that is used for background painting.
     */
    public static final NamedColorProperty BG_COLOR = new NamedColorProperty(marktr("cadastre download panel"), new Color(255, 204, 51));

    private final JCheckBox cbDownloadBuilding;
    private final JCheckBox cbDownloadAddress;
    private final JCheckBox cbDownloadSymbol;
    private final JCheckBox cbDownloadWater;
    private final JCheckBox cbDownloadParcel;
    private final JCheckBox cbDownloadParcelNum;
    private final JCheckBox cbDownloadSection;
    private final JCheckBox cbDownloadLocality;
    private final JCheckBox cbDownloadCommune;
    private final JLabel sizeCheck = new JLabel();

    private static final BooleanProperty DOWNLOAD_BUILDING = new BooleanProperty("download.cadastrefr.building", true);
    private static final BooleanProperty DOWNLOAD_ADDRESS = new BooleanProperty("download.cadastrefr.address", true);
    private static final BooleanProperty DOWNLOAD_SYMBOL = new BooleanProperty("download.cadastrefr.symbol", true);
    private static final BooleanProperty DOWNLOAD_WATER = new BooleanProperty("download.cadastrefr.water", false);
    private static final BooleanProperty DOWNLOAD_PARCEL = new BooleanProperty("download.cadastrefr.parcel", false);
    private static final BooleanProperty DOWNLOAD_PARCEL_NUM = new BooleanProperty("download.cadastrefr.parcelnum", false);
    private static final BooleanProperty DOWNLOAD_SECTION = new BooleanProperty("download.cadastrefr.section", false);
    private static final BooleanProperty DOWNLOAD_LOCALITY = new BooleanProperty("download.cadastrefr.locality", false);
    private static final BooleanProperty DOWNLOAD_COMMUNE = new BooleanProperty("download.cadastrefr.commune", false);

    private static JCheckBox createCheckBox(String text, BooleanProperty property, String tooltip) {
        JCheckBox cb = new JCheckBox(text, property.get());
        cb.setOpaque(false);
        cb.setToolTipText(tooltip);
        return cb;
    }

    /**
     * Creates a new {@link CadastreDownloadSourcePanel}.
     * @param ds The cadastre download source the panel is for.
     */
    public CadastreDownloadSourcePanel(CadastreDownloadSource ds) {
        super(ds);
        setBackground(BG_COLOR.get());
        setLayout(new GridBagLayout());

        // size check depends on selected data source
        final ChangeListener checkboxChangeListener = e ->
                DownloadDialog.getInstance().getSelectedDownloadArea().ifPresent(this::updateSizeCheck);

        // adding the download tasks
        add(new JLabel(tr("Objects:")), GBC.std().insets(5, 5, 1, 5).anchor(GBC.CENTER));
        cbDownloadBuilding = createCheckBox(tr("building"), DOWNLOAD_BUILDING,
                tr("Select to download buildings in the selected download area."));
        cbDownloadAddress = createCheckBox(tr("address"), DOWNLOAD_ADDRESS,
                tr("Select to download addresses in the selected download area."));
        cbDownloadSymbol = createCheckBox(tr("symbol"), DOWNLOAD_SYMBOL,
                tr("Select to download symbols in the selected download area."));
        cbDownloadWater = createCheckBox(tr("water"), DOWNLOAD_WATER,
                tr("Select to download water bodies in the selected download area."));
        cbDownloadParcel = createCheckBox(tr("parcel"), DOWNLOAD_PARCEL,
                tr("Select to download cadastral parcels in the selected download area."));
        cbDownloadParcelNum = createCheckBox(tr("parcel number"), DOWNLOAD_PARCEL_NUM,
                tr("Select to download cadastral parcel numbers in the selected download area."));
        cbDownloadSection = createCheckBox(tr("section"), DOWNLOAD_SECTION,
                tr("Select to download cadastral sections in the selected download area."));
        cbDownloadLocality = createCheckBox(tr("locality"), DOWNLOAD_LOCALITY,
                tr("Select to download localities in the selected download area."));
        cbDownloadCommune = createCheckBox(tr("commune"), DOWNLOAD_COMMUNE,
                tr("Select to download municipality boundary in the selected download area."));

        for (JCheckBox cb : Arrays.asList(cbDownloadBuilding, cbDownloadAddress, cbDownloadSymbol, cbDownloadWater,
                cbDownloadParcel, cbDownloadParcelNum, cbDownloadSection, cbDownloadLocality, cbDownloadCommune)) {
            cb.getModel().addChangeListener(checkboxChangeListener);
            add(cb, GBC.std().insets(1, 5, 1, 5));
        }

        Font labelFont = sizeCheck.getFont();
        sizeCheck.setFont(labelFont.deriveFont(Font.PLAIN, labelFont.getSize()));

        setMinimumSize(new Dimension(450, 115));
    }

    @Override
    public CadastreDownloadData getData() {
        return new CadastreDownloadData(
                isDownloadWater(), isDownloadBuilding(), isDownloadSymbol(), isDownloadParcel(),
                isDownloadParcelNum(), isDownloadAddress(), isDownloadLocality(), isDownloadSection(), isDownloadCommune());
    }

    @Override
    public void rememberSettings() {
        DOWNLOAD_BUILDING.put(isDownloadBuilding());
        DOWNLOAD_ADDRESS.put(isDownloadAddress());
        DOWNLOAD_SYMBOL.put(isDownloadSymbol());
        DOWNLOAD_WATER.put(isDownloadWater());
        DOWNLOAD_PARCEL.put(isDownloadParcel());
        DOWNLOAD_PARCEL_NUM.put(isDownloadParcelNum());
        DOWNLOAD_SECTION.put(isDownloadSection());
        DOWNLOAD_LOCALITY.put(isDownloadLocality());
        DOWNLOAD_COMMUNE.put(isDownloadCommune());
    }

    @Override
    public void restoreSettings() {
        cbDownloadBuilding.setSelected(DOWNLOAD_BUILDING.get());
        cbDownloadAddress.setSelected(DOWNLOAD_ADDRESS.get());
        cbDownloadSymbol.setSelected(DOWNLOAD_SYMBOL.get());
        cbDownloadWater.setSelected(DOWNLOAD_WATER.get());
        cbDownloadParcel.setSelected(DOWNLOAD_PARCEL.get());
        cbDownloadParcelNum.setSelected(DOWNLOAD_PARCEL_NUM.get());
        cbDownloadSection.setSelected(DOWNLOAD_SECTION.get());
        cbDownloadLocality.setSelected(DOWNLOAD_LOCALITY.get());
        cbDownloadCommune.setSelected(DOWNLOAD_COMMUNE.get());
    }

    @Override
    public boolean checkDownload(DownloadSettings settings) {
        /*
         * It is mandatory to specify the area to download from OSM.
         */
        if (!settings.getDownloadBounds().isPresent()) {
            JOptionPane.showMessageDialog(
                    this.getParent(),
                    tr("Please select a download area first."),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE
            );

            return false;
        }

        /*
         * Checks if the user selected the type of data to download. At least one must be chosen.
         * If none of those are selected, then the corresponding dialog is shown to inform the user.
         */
        if (!isDownloadWater() && !isDownloadBuilding() && !isDownloadSymbol() && !isDownloadParcel() &&
            !isDownloadParcelNum() && !isDownloadAddress() && !isDownloadLocality() && !isDownloadSection() && !isDownloadCommune()) {
            JOptionPane.showMessageDialog(
                    this.getParent(),
                    tr("<html>Nothing is enabled.<br>Please choose something to download.</html>"),
                    tr("Error"),
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        this.rememberSettings();
        return true;
    }

    boolean isDownloadBuilding() {
        return cbDownloadBuilding.isSelected();
    }

    boolean isDownloadAddress() {
        return cbDownloadAddress.isSelected();
    }

    boolean isDownloadSymbol() {
        return cbDownloadSymbol.isSelected();
    }

    boolean isDownloadWater() {
        return cbDownloadWater.isSelected();
    }

    boolean isDownloadParcel() {
        return cbDownloadParcel.isSelected();
    }

    boolean isDownloadParcelNum() {
        return cbDownloadParcelNum.isSelected();
    }

    boolean isDownloadSection() {
        return cbDownloadSection.isSelected();
    }

    boolean isDownloadLocality() {
        return cbDownloadLocality.isSelected();
    }

    boolean isDownloadCommune() {
        return cbDownloadCommune.isSelected();
    }

    @Override
    public Icon getIcon() {
        return ImageProvider.get("cadastre_small");
    }

    @Override
    public void boundingBoxChanged(Bounds bbox) {
        updateSizeCheck(bbox);
    }

    @Override
    public String getSimpleName() {
        return SIMPLE_NAME;
    }

    private void updateSizeCheck(Bounds bbox) {
        if (bbox == null) {
            sizeCheck.setText(tr("No area selected yet"));
            sizeCheck.setForeground(Color.darkGray);
            return;
        }
/*
        boolean isAreaTooLarge = false;
        if (!isDownloadNotes() && !isDownloadOsmData() && !isDownloadGpxData()) {
            isAreaTooLarge = false;
        } else if (isDownloadNotes() && !isDownloadOsmData() && !isDownloadGpxData()) {
            // see max_note_request_area in https://github.com/openstreetmap/openstreetmap-website/blob/master/config/example.application.yml
            isAreaTooLarge = bbox.getArea() > Config.getPref().getDouble("osm-server.max-request-area-notes", 25);
        } else {
            // see max_request_area in https://github.com/openstreetmap/openstreetmap-website/blob/master/config/example.application.yml
            isAreaTooLarge = bbox.getArea() > Config.getPref().getDouble("osm-server.max-request-area", 0.25);
        }

        displaySizeCheckResult(isAreaTooLarge);*/
    }

    private void displaySizeCheckResult(boolean isAreaTooLarge) {
        if (isAreaTooLarge) {
            sizeCheck.setText(tr("Download area too large; will probably be rejected by server"));
            sizeCheck.setForeground(Color.red);
        } else {
            sizeCheck.setText(tr("Download area ok, size probably acceptable to server"));
            sizeCheck.setForeground(Color.darkGray);
        }
    }
}
