package org.openstreetmap.josm.plugins.photoadjust;

//import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.dialogs.LatLonDialog;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.gui.layer.geoimage.ImageViewerDialog;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.gui.widgets.JosmTextField;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Simple editor for photo GPS data.
 */
public class PhotoPropertyEditor {

    public PhotoPropertyEditor() {
        MainMenu.add(Main.main.menu.editMenu, new PropertyEditorAction());
    }

    /**
     * Update the geo image layer and the image viewer.
     *
     * @param layer GeoImageLayer of the photo.
     * @param photo The photo that is updated.
     */
    private static void updateLayer(GeoImageLayer layer, ImageEntry photo) {
        layer.updateBufferAndRepaint();
        ImageViewerDialog.showImage(layer, photo);
    }

    /**
     * Action if the menu entry is selected.
     */
    private static class PropertyEditorAction extends JosmAction {
        public PropertyEditorAction() {
            super(tr("Edit photo GPS data"),	// String name
                  (String)null,			// String iconName
                  tr("Edit GPS data of selected photo."), // String tooltip
                  null,				// Shortcut shortcut
                  true,				// boolean registerInToolbar
                  "photoadjust/propertyeditor", // String toolbarId
                  true				// boolean installAdapters
                  );
            //putValue("help", ht("/Action/..."));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                final ImageEntry photo = ImageViewerDialog.getCurrentImage();
                final GeoImageLayer layer = ImageViewerDialog.getCurrentLayer();
                if (photo == null) {
                    throw new AssertionError("No image selected.");
                }
                StringBuilder title =
                    new StringBuilder(tr("Edit Photo GPS Data"));
                if (photo.getFile() != null) {
                    title.append(" - ");
                    title.append(photo.getFile().getName());
                }
                PropertyEditorDialog dialog =
                    new PropertyEditorDialog(title.toString(), photo, layer);
                if (dialog.getValue() == 1) {
                    dialog.updateImageTmp();
                    photo.applyTmp();
                } else {
                    photo.discardTmp();
                }
                updateLayer(layer, photo);
            } catch (AssertionError err) {
                JOptionPane.showMessageDialog(Main.parent,
                                              tr("Please select an image first."),
                                              tr("No image selected"),
                                              JOptionPane.INFORMATION_MESSAGE);
                return;
            }
       }

        /**
         * Check if there is a selected image.
         *
         * @return {@code true} if the image viewer exists and there is an
         *         image shown, {@code false} otherwise.
         */
        private boolean enabled() {
            try {
                //return ImageViewerDialog.getInstance().hasImage();
                ImageViewerDialog.getInstance().hasImage();
                return true;
            } catch (AssertionError err) {
                return false;
            }
        }

        @Override
        protected void updateEnabledState() {
            setEnabled(enabled());
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
        private final GeoImageLayer layer;
        private static final Color BG_COLOR_ERROR = new Color(255,224,224);

        public PropertyEditorDialog(String title, final ImageEntry image,
                                    final GeoImageLayer layer) {
            super(Main.parent, title, new String[] {tr("Ok"), tr("Cancel")});
            this.image = image;
            this.layer = layer;
            setButtonIcons(new String[] {"ok", "cancel"});
            final JPanel content = new JPanel(new GridBagLayout());
            //content.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            if (image.hasThumbnail() || image.getFile() != null) {
                final JLabel header = new JLabel(image.getFile() != null
                                                 ? image.getFile().getName()
                                                 : "");
                //if (!image.hasThumbnail()) {
                //    image.loadThumbnail();
                //}
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
            Action editCoordAction = new AbstractAction(tr("Edit")) {
                @Override public void actionPerformed(ActionEvent evt) {
                    final LatLonDialog dialog 
                        = new LatLonDialog(Main.parent,
                                           tr("Edit Image Coordinates"), null);
                    dialog.setCoordinates(getLatLon());
                    dialog.showDialog();
                    if (dialog.getValue() == 1) {
                        LatLon coordinates = dialog.getCoordinates();
                        if (coordinates != null) {
                            coords.setText(coordinates.toStringCSV(" "));
                        }
                    }
                }
            };
            final JButton editCoordBtn = new JButton(editCoordAction);
            editCoordBtn
                .setToolTipText(tr("Edit coordinates in separate editor"));
            content.add(editCoordBtn, GBC.eol());

            // Altitude/elevation.
            altitude.setHint(tr("altitude"));
            DoubleInputVerifier altVerif = new DoubleInputVerifier(altitude) {
                @Override public void updateValue(Double value) {
                    image.getTmp().setElevation(value);
                    updateLayer(layer, image);
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
                    updateLayer(layer, image);
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
                    updateLayer(layer, image);
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
            Action undoAction = new AbstractAction(tr("Undo")) {
                @Override public void actionPerformed(ActionEvent evt) {
                    setInitialValues();
                }
            };
            final JButton undoButton = new JButton(undoAction);
            undoButton.setToolTipText(tr("Undo changes made in this dialog"));
            undoButton.setIcon(ImageProvider.get("undo"));
            buttonsPanel.add(undoButton, GBC.std().insets(2, 2, 2, 2));

            // Reload.
            Action reloadAction = new AbstractAction(tr("Reload")) {
                @Override public void actionPerformed(ActionEvent evt) {
                    final ImageEntry imgTmp = new ImageEntry(image.getFile());
                    imgTmp.extractExif();
                    setInitialValues(imgTmp);
                }
            };
            final JButton reloadButton = new JButton(reloadAction);
            reloadButton.setToolTipText(tr("Reload GPS data from image file"));
            reloadButton.setIcon(ImageProvider.get("dialogs/refresh"));
            buttonsPanel.add(reloadButton, GBC.std().insets(2, 2, 2, 2));

            // // Apply.
            // Action applyAction = new AbstractAction(tr("Apply")) {
            //     @Override public void actionPerformed(ActionEvent evt) {
            //         updateImageTmp();
            //         updateLayer(layer, image);
            //     }
            // };
            // final JButton applyButton = new JButton(applyAction);
            // applyButton.setToolTipText(tr("Apply changes, keep dialog open"));
            // applyButton.setIcon(ImageProvider.get("apply"));
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
         * Initialize the dialog with image data.  The image can be specified.
         *
         * @param image Use the data of this image.
         */
        private void setInitialValues(ImageEntry image) {
            if (image.getPos() != null) {
                //coords.setText(image.getPos().toDisplayString());
                coords.setText(image.getPos().toStringCSV(" "));
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
        private boolean isDoubleFieldDifferent(JosmTextField txtFld,
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
        private Double getDoubleValue(JosmTextField txtFld) {
            final String text = txtFld.getText();
            if (text == null || text.isEmpty()) {
                return null;
            }
            try {
                return new Double(text);
            } catch(NumberFormatException nfe) {
                return null;
            }
        }

        /**
         * Copy the values from the dialog to the temporary image copy.
         */
        public void updateImageTmp() {
            ImageEntry imgTmp = image.getTmp();
            String text = coords.getText();
            if (text == null || text.isEmpty()) {
                if (imgTmp.getPos() != null) {
                    imgTmp.flagNewGpsData();
                    imgTmp.setPos(null);
                }
            } else {
                if ( imgTmp.getPos() == null
                     || !text.equals(imgTmp.getPos().toStringCSV(" "))) {
                    imgTmp.flagNewGpsData();
                    imgTmp.setPos(getLatLon());
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
                latLon = LatLonDialog.parseLatLon(coords.getText());
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
                latLon = LatLonDialog.parseLatLon(coordsText);
            } catch (IllegalArgumentException exn) {
                latLon = null;
            }
            if ( latLon == null && coordsText != null
                 && !coordsText.isEmpty()) {
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
            if (text == null || text.isEmpty()) {
                isError = false;
            } else {
                try {
                    value = Double.parseDouble(text);
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
                updateLayer(layer, image);
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
    }
}
