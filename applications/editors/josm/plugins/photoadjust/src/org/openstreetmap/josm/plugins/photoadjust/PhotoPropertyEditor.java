// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.photoadjust;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.ImageData;
import org.openstreetmap.josm.data.ImageData.ImageDataUpdateListener;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.coor.conversion.CoordinateFormatManager;
import org.openstreetmap.josm.data.coor.conversion.LatLonParser;
import org.openstreetmap.josm.data.gpx.GpxImageEntry;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.dialogs.LatLonDialog;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.gui.widgets.JosmTextField;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Simple editor for photo GPS data.
 */
public class PhotoPropertyEditor {

    /**
     * This class is not intended to be instantiated.  Throw an exception if
     * it is.
     */
    private PhotoPropertyEditor() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Add photo property editor to edit menu.
     */
    public static void init() {
        MainMenu.add(MainApplication.getMenu().editMenu, new PropertyEditorAction());
    }

    /**
     * Action if the menu entry is selected.
     */
    private static class PropertyEditorAction extends JosmAction implements LayerChangeListener, ImageDataUpdateListener {
        public PropertyEditorAction() {
            super(tr("Edit photo GPS data"),    // String name
                    (String)null,                         // String iconName
                    tr("Edit GPS data of selected photo."), // String tooltip
                    null,                                 // Shortcut shortcut
                    true,                                 // boolean registerInToolbar
                    "photoadjust/propertyeditor", // String toolbarId
                    false                          // boolean installAdapters
                    );
            this.installAdapters();
        }

        @Override
        protected void installAdapters() {
            MainApplication.getLayerManager().addLayerChangeListener(this);
            initEnabledState();
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            final ImageData data = getLayerWithOneSelectedImage().get().getImageData();
            final ImageEntry photo = data.getSelectedImages().get(0);

            StringBuilder title =
                    new StringBuilder(tr("Edit Photo GPS Data"));
            if (photo.getFile() != null) {
                title.append(" - ");
                title.append(photo.getFile().getName());
            }
            PropertyEditorDialog dialog =
                    new PropertyEditorDialog(title.toString(), photo, data);
            if (dialog.getValue() == 1) {
                dialog.updateImageTmp();
                // There are cases where isNewGpsData is not set but there
                // is still new data, e.g. if the EXIF data was re-read
                // from the image file.
                photo.applyTmp();
            } else {
                photo.discardTmp();
            }
            data.notifyImageUpdate();
        }

        /**
         * Check if there is a selected image.
         *
         * @return {@code true} if the image viewer exists and there is an
         *         image shown, {@code false} otherwise.
         */
        private static boolean enabled() {
            return getLayerWithOneSelectedImage().isPresent();
        }

        private static Optional<GeoImageLayer> getLayerWithOneSelectedImage() {
            List<GeoImageLayer> list = MainApplication.getLayerManager().getLayersOfType(GeoImageLayer.class);
            return list.stream().filter(l -> l.getImageData().getSelectedImages().size() == 1).findFirst();
        }

        @Override
        protected void updateEnabledState() {
            setEnabled(enabled());
        }

        @Override
        public void layerAdded(LayerAddEvent e) {
            Layer layer = e.getAddedLayer();
            if (layer instanceof GeoImageLayer) {
                ((GeoImageLayer) layer).getImageData().addImageDataUpdateListener(this);
            }
        }

        @Override
        public void layerRemoving(LayerRemoveEvent e) {
            Layer layer = e.getRemovedLayer();

            if (layer instanceof GeoImageLayer) {
                ((GeoImageLayer) layer).getImageData().removeImageDataUpdateListener(this);
            }
            this.updateEnabledState();
        }

        @Override
        public void layerOrderChanged(LayerOrderChangeEvent e) {
            // ignored
        }

        @Override
        public void imageDataUpdated(ImageData data) {
            // ignored
        }

        @Override
        public void selectedImageChanged(ImageData data) {
            this.updateEnabledState();
        }
    }

    /**
     * The actual photo property editor dialog.
     */
    private static class PropertyEditorDialog extends ExtendedDialog {
        private final JosmTextField coords = new JosmTextField(24);
        private final JosmTextField altitude = new JosmTextField();
        private final JosmTextField speed = new JosmTextField();
        private final JosmTextField direction = new JosmTextField();
        // Image that is to be updated.
        private final ImageEntry image;
        private final ImageData data;
        // Image as it was when the dialog was opened.
        private final GpxImageEntry imgOrig;
        private static final Color BG_COLOR_ERROR = new Color(255, 224, 224);

        public PropertyEditorDialog(String title, final ImageEntry image,
                final ImageData data) {
            super(MainApplication.getMainFrame(), title, tr("Ok"), tr("Cancel"));
            this.image = image;
            this.data = data;
            imgOrig = new ImageEntry(image);
            setButtonIcons("ok", "cancel");
            final JPanel content = new JPanel(new GridBagLayout());
            //content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            if (image.hasThumbnail() || image.getFile() != null) {
                final JLabel header = new JLabel(image.getFile() != null
                                                 ? image.getFile().getName()
                                                 : "");
                if (!image.hasThumbnail()) {
                    image.loadThumbnail();
                }
                if (image.hasThumbnail()) {
                    header.setIcon(new ImageIcon(image.getThumbnail()));
                }
                content.add(header, GBC.eol().fill().weight(1.0, 1.0));
            }

            content.add(new JSeparator(),
                        GBC.eol().fill(GBC.HORIZONTAL).insets(0, 5, 0, 5));
            content.add(new JLabel(tr("(Empty values delete the according fields.)")),
                        GBC.eol());

            // Coordinates.
            coords.setHint(tr("coordinates"));
            //coords.setEditable(false);
            LatLonInputVerifier coordsVerif = new LatLonInputVerifier();
            coords.getDocument().addDocumentListener(coordsVerif);
            coords.setToolTipText(tr("Latitude and longitude"));
            content.add(new JLabel(tr("Coordinates:")),
                        GBC.std().insets(0, 0, 5, 0));
            content.add(coords, GBC.std().fill(GBC.HORIZONTAL));

            final JButton editCoordBtn = new JButton(new EditCoordAction());
            content.add(editCoordBtn, GBC.eol());

            // Altitude/elevation.
            altitude.setHint(tr("altitude"));
            DoubleInputVerifier altVerif = new DoubleInputVerifier(altitude) {
                @Override public void updateValue(Double value) {
                    image.getTmp().setElevation(value);
                    data.notifyImageUpdate();
                }
            };
            altitude.getDocument().addDocumentListener(altVerif);
            content.add(new JLabel(tr("Altitude:")),
                        GBC.std().insets(0, 0, 5, 0));
            content.add(altitude, GBC.std().fill(GBC.HORIZONTAL));
            content.add(new JLabel(/* unit: meter */ tr("m")), GBC.eol());

            // Speed.
            speed.setHint(tr("speed"));
            DoubleInputVerifier speedVerif = new DoubleInputVerifier(speed) {
                @Override public void updateValue(Double value) {
                    image.getTmp().setSpeed(value);
                    data.notifyImageUpdate();
                }
            };
            speedVerif.setMinMax(0.0, null);
            speed.getDocument().addDocumentListener(speedVerif);
            speed.setToolTipText(tr("positive number or empty"));
            content.add(new JLabel(tr("Speed:")),
                        GBC.std().insets(0, 0, 5, 0));
            content.add(speed, GBC.std().fill(GBC.HORIZONTAL));
            content.add(new JLabel(tr("km/h")), GBC.eol());

            // Image direction.
            direction.setHint(tr("direction"));
            DoubleInputVerifier dirVerif = new DoubleInputVerifier(direction) {
                @Override public void updateValue(Double value) {
                    image.getTmp().setExifImgDir(value);
                    data.notifyImageUpdate();
                }
            };
            dirVerif.setMinMax(-360.0, 360.0);
            direction.getDocument().addDocumentListener(dirVerif);
            direction.setToolTipText(tr("range -360.0 .. 360.0, or empty"));
            content.add(new JLabel(tr("Direction:")),
                        GBC.std().insets(0, 0, 5, 0));
            content.add(direction, GBC.std().fill(GBC.HORIZONTAL));
            content.add(new JLabel(/* unit: degree (angle) */ tr("\u00b0")),
                        GBC.eol());

            setInitialValues();

            // Button row.
            final JPanel buttonsPanel = new JPanel(new GridBagLayout());

            // Undo.
            final JButton undoButton = new JButton(new UndoAction());
            buttonsPanel.add(undoButton, GBC.std().insets(2, 2, 2, 2));

            // Reload.
            final JButton reloadButton = new JButton(new ReloadAction());
            buttonsPanel.add(reloadButton, GBC.std().insets(2, 2, 2, 2));

            // // Apply.
            // final JButton applyButton = new JButton(new ApplyAction());
            // // fill(VERTICAL) to make the button the same height than the
            // // other buttons.  This is needed because the apply icon is
            // // smaller.
            // buttonsPanel.add(applyButton,
            //                  GBC.std().insets(2, 2, 2, 2).fill(GBC.VERTICAL));

            // Add buttons to form.
            content.add(buttonsPanel, GBC.eol().insets(0, 5, 0, 5));

            // The false in the next line makes it a dynamic width, but the
            // scroll bars are gone...
            setContent(content, false);
            showDialog();
        }

        /**
         * Initialize the dialog with image data.  Uses the image the dialog
         * was started with.
         */
        private void setInitialValues() {
            image.discardTmp();
            setInitialValues(image);
        }

        /**
         * Convert an image position into a display string.
         *
         * @param pos Coordinates of image position.
         * @return Image position as text.
         */
        private static String posToText(LatLon pos) {
            // See josm.gui.dialogs.LatLonDialog.setCoordinates().
            return
                pos == null ? "" :
                CoordinateFormatManager.getDefaultFormat().latToString(pos) +
                ' ' +
                CoordinateFormatManager.getDefaultFormat().lonToString(pos);
        }

        /**
         * Initialize the dialog with image data.  The image can be specified.
         *
         * @param image Use the data of this image.
         */
        private void setInitialValues(ImageEntry image) {
            if (image.getPos() != null) {
                //coords.setText(image.getPos().toDisplayString());
                coords.setText(posToText(image.getPos()));
            } else {
                coords.setText(null);
            }
            if (image.getElevation() != null) {
                altitude.setText(image.getElevation().toString());
            } else {
                altitude.setText(null);
            }
            if (image.getSpeed() != null) {
                speed.setText(image.getSpeed().toString());
            } else {
                speed.setText(null);
            }
            if (image.getExifImgDir() != null) {
                direction.setText(image.getExifImgDir().toString());
            } else {
                direction.setText(null);
            }
        }

        /**
         * Check if the value of a dialog field is different from a value of
         * type {@code Double}.
         *
         * @param txtFld Dialog text field.
         * @param value Double value to compare with.
         * @return {@code true} if the values differ, {@code false} otherwise.
         */
        private static boolean isDoubleFieldDifferent(JosmTextField txtFld,
                                                      Double value) {
            final Double fieldValue = getDoubleValue(txtFld);
            if (fieldValue == null) {
                if (value != null) {
                    return true;
                }
            } else {
                if (value == null ||
                    // Comparison of 'double' so -0.0 is equal to +0.0.
                    fieldValue.doubleValue() != value.doubleValue()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Convert dialog field value to {@code Double}.
         *
         * @param txtFld Dialog text field.
         * @return Dialog field converted to {@code Double}.  {@code null} if
         *         the field is empty or if the field value cannot be
         *         converted to double.
         */
        private static Double getDoubleValue(JosmTextField txtFld) {
            final String text = txtFld.getText();
            if (text == null || text.isEmpty()) {
                return null;
            }
            try {
                return Double.valueOf(text);
            } catch(NumberFormatException nfe) {
                return null;
            }
        }

        /**
         * Copy the values from the dialog to the temporary image copy.
         */
        public void updateImageTmp() {
            GpxImageEntry imgTmp = image.getTmp();

            String text = coords.getText();
            // The position of imgTmp is set in any case because it was
            // modified while the dialog was open.
            if (text == null || text.isEmpty()) {
                imgTmp.setPos(null);
                if (imgOrig.getPos() != null) {
                    imgTmp.flagNewGpsData();
                }
            } else {
                // Coordinates field is not empty.
                imgTmp.setPos(getLatLon());
                // Flag new GPS data if the temporary image is at a different
                // position as the original image.  It doesn't work to compare
                // against the coords text field as that might contain data
                // (e.g. trailing zeros) that do not change the value.  It
                // doesn't work to compare imgTmp.getPos() with getLatLon()
                // because the dialog will round the initial position.
                if (imgOrig.getPos() == null
                    || !posToText(imgOrig.getPos()).equals(
                        posToText(imgTmp.getPos()))) {
                    imgTmp.flagNewGpsData();
                }
            }

            if (isDoubleFieldDifferent(altitude, imgTmp.getElevation())) {
                imgTmp.flagNewGpsData();
                imgTmp.setElevation(getDoubleValue(altitude));
            }

            if (isDoubleFieldDifferent(speed, imgTmp.getSpeed())) {
                imgTmp.flagNewGpsData();
                imgTmp.setSpeed(getDoubleValue(speed));
            }

            if (isDoubleFieldDifferent(direction, imgTmp.getExifImgDir())) {
                imgTmp.flagNewGpsData();
                Double imgDir = getDoubleValue(direction);
                if (imgDir != null) {
                    if (imgDir < 0.0) {
                        imgDir %= 360.0; // >-360.0...-0.0
                        imgDir += 360.0; // >0.0...360.0
                    }
                    if (imgDir >= 360.0) {
                        imgDir %= 360.0;
                    }
                }
                imgTmp.setExifImgDir(imgDir);
            }
        }

        /**
         * Parse coordinate text into LatLon.
         *
         * @return Coordinates, {@code null} if no coordinates set or if they
         *         are not valid.
         */
        private LatLon getLatLon() {
            LatLon latLon;
            try {
                latLon = LatLonParser.parse(coords.getText());
                if (!latLon.isValid()) {
                    latLon = null;
                }
            } catch (IllegalArgumentException exn) {
                latLon = null;
            }
            return latLon;
        }

        /**
         * Parse the coordinate dialog field.  Set the error marker if the
         * field value is not valid.
         *
         * @return Coordinates converted to {@code LatLon}.  {@code null} if
         *         the dialog field is empty or if the field value cannot be
         *         converted.
         */
        protected LatLon parseLatLonUserInput() {
            LatLon latLon;
            final String coordsText = coords.getText();
            try {
                latLon = LatLonParser.parse(coordsText);
            } catch (IllegalArgumentException exn) {
                latLon = null;
            }
            if ((latLon == null && coordsText != null
                 && !coordsText.isEmpty())) {
                setErrorFeedback(coords);
                setOkEnabled(false);
                latLon = null;
            } else {
                clearErrorFeedback(coords);
                setOkEnabled(true);
            }
            return latLon;
        }

        /**
         * Parse a dialog field that displays a value of type {@code Double}.
         * Set the error marker if the field value is not valid.
         *
         * @param txtFld Dialog text field.
         * @param min Minimum value.  Set to {@code null} if there is no
         *        minimum.
         * @param max Maximum value.  Set to {@code null} if there is no
         *        maximum.
         * @return Parsed form value.  {@code null} if
         *         the dialog field is empty, if the field value cannot be
         *         converted, or if the value is not within the limits.
         */
        protected Double parseDoubleUserInput(JosmTextField txtFld,
                                              Double min, Double max) {
            boolean isError = false;
            final String text = txtFld.getText();
            Double value = null;
            if (text != null && !text.isEmpty()) {
                try {
                    value = Double.valueOf(text);
                    if (min != null && value < min) {
                        isError = true;
                    }
                    if (max != null && value > max) {
                        isError = true;
                    }
                } catch(NumberFormatException nfe) {
                    isError = true;
                }
            }
            if (isError) {
                setErrorFeedback(txtFld);
                setOkEnabled(false);
                value = null;
            } else {
                clearErrorFeedback(txtFld);
                setOkEnabled(true);
            }
            return value;
        }

        /**
         * Mark a dialog field as erroneous.
         *
         * @param txtFld Dialog text field.
         */
        protected void setErrorFeedback(JosmTextField txtFld) {
            txtFld.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
            txtFld.setBackground(BG_COLOR_ERROR);
        }

        /**
         * Clear the error marker from a dialog field.
         *
         * @param txtFld Dialog text field.
         */
        protected void clearErrorFeedback(JosmTextField txtFld) {
            txtFld.setBorder(UIManager.getBorder("TextField.border"));
            txtFld.setBackground(UIManager.getColor("TextField.background"));
        }

        /**
         * Enable or disable the OK button.  It gets disabled if one of the
         * dialog fields has an error.
         *
         * @param enabled {@code true} if the OK button is enabled,
         *        {@code false} if not.
         */
        private void setOkEnabled(boolean enabled) {
            if (buttons != null && !buttons.isEmpty()) {
                buttons.get(0).setEnabled(enabled);
            }
        }

        /**
         * Interface for coordinate update.
         */
        public interface UpdateLatLon {
            /**
             * Update of latitude and longitude.
             *
             * @param latLon New latitude/longitude value.
             */
            public void updateLatLon(LatLon latLon);
        }

        /**
         * Verify the coordinates.  Parses the coordinate dialog field, sets
         * or clears the error marker, updates the (temporary) image position,
         * and updates the image layer to reflect the current coordinates.
         */
        class LatLonInputVerifier implements DocumentListener, UpdateLatLon {
            private void doUpdate() {
                updateLatLon(parseLatLonUserInput());
            }

            @Override
            public void updateLatLon(LatLon latLon) {
                image.getTmp().setPos(latLon);
                data.notifyImageUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                doUpdate();
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                doUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent evt) {
                doUpdate();
            }
        }

        /**
         * Interface for update of a {@code Double} field.
         */
        public interface UpdateDoubleValue {
            /**
             * Update the associated value.
             *
             * @param value New double value.
             */
            public void updateValue(Double value);
        }

        /**
         * Verify the dialog field with double value.  Parses the dialog
         * field, sets or clears the error marker, calls the update method.
         * The update method must be defined in the class instance.
         */
        abstract class DoubleInputVerifier implements DocumentListener,
                                                      UpdateDoubleValue {

            private final JosmTextField textField;
            private Double minimum;
            private Double maximum;

            public DoubleInputVerifier(JosmTextField txtFld) {
                textField = txtFld;
            }

            /**
             * Set minimum and maximal value.
             *
             * @param min Minimum value.  Set to {@code null} if there is no
             *        minimum.
             * @param max Maximum value.  Set to {@code null} if there is no
             *        maximum.
             */
            public void setMinMax(Double min, Double max) {
                minimum = min;
                maximum = max;
            }

            private void doUpdate() {
                updateValue(parseDoubleUserInput(textField, minimum, maximum));
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                doUpdate();
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                doUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent evt) {
                doUpdate();
            }
        }

        class EditCoordAction extends AbstractAction {
            EditCoordAction() {
                super(tr("Edit"));
                putValue(SHORT_DESCRIPTION,
                         tr("Edit coordinates in separate editor"));
            }

            @Override public void actionPerformed(ActionEvent evt) {
                final LatLonDialog llDialog
                    = new LatLonDialog(MainApplication.getMainFrame(),
                                       tr("Edit Image Coordinates"), null);
                llDialog.setCoordinates(getLatLon());
                llDialog.showDialog();
                if (llDialog.getValue() == 1) {
                    final LatLon coordinates = llDialog.getCoordinates();
                    if (coordinates != null) {
                        coords.setText(posToText(coordinates));
                    }
                }
            }
        }

        class UndoAction extends AbstractAction {
            UndoAction() {
                super(tr("Undo"));
                new ImageProvider("undo").getResource().attachImageIcon(this);
                putValue(SHORT_DESCRIPTION,
                         tr("Undo changes made in this dialog"));
            }

            @Override public void actionPerformed(ActionEvent evt) {
                setInitialValues();
            }
        }

        class ReloadAction extends AbstractAction {
            ReloadAction() {
                super(tr("Reload"));
                new ImageProvider("dialogs", "refresh").getResource().
                    attachImageIcon(this);
                putValue(SHORT_DESCRIPTION,
                         tr("Reload GPS data from image file"));
            }

            @Override public void actionPerformed(ActionEvent evt) {
                final ImageEntry imgTmp = new ImageEntry(image.getFile());
                imgTmp.extractExif();
                setInitialValues(imgTmp);
            }
        }

        // class ApplyAction extends AbstractAction {
        //     ApplyAction() {
        //         super(tr("Apply"));
        //         new ImageProvider("apply").getResource().
        //             attachImageIcon(this);
        //         putValue(SHORT_DESCRIPTION,
        //                  tr("Apply changes, keep dialog open"));
        //     }
        //
        //     @Override public void actionPerformed(ActionEvent evt) {
        //         updateImageTmp();
        //         updateLayer(layer, image);
        //     }
        // }
    }
}
