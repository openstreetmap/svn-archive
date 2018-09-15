// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.MainApplication;

public class FilePlacement18 extends FilePlacement {
    /*
     * Handle plasement of a pdf File
     * provide transformation, load & save, GUI-componennt
     * superseeds (legacy) class FilePlacement
     */

    public class PlacementPanel {
        private class CoorFields {
            /*
             * Coordinte Fields
             * keep Fields representing a Coordinate together
             * functionality: allow entering coordinate in the first field and transfer Y-Part to the second field
             * TODO: encapsulate to Componente
             */
            private GuiFieldDouble x;
            private GuiFieldDouble y;

            private void checkCoords(GuiFieldDouble x, GuiFieldDouble y) {
                int splitpos = 0;
                String val2 = x.getText().trim();
                if ((splitpos = val2.indexOf(';')) >= 0) {
                    // Split a coordinate into its parts for ease of data entry
                    y.setText(val2.substring(splitpos + 1).trim());
                    x.setText(val2.substring(0, splitpos).trim());
                }
                y.checker.check(y);
                x.checker.check(x);
            }

            @SuppressWarnings("unused")
            private CoorFields() {
            }

            CoorFields(GuiFieldDouble x, GuiFieldDouble y) {
                this.x = x;
                this.y = y;
                x.addFocusListener(new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        checkCoords(x, y);
                    }

                    @Override
                    public void focusGained(FocusEvent e) {
                    }
                });
            }
            public void SetCoor(EastNorth en) {
                x.requestFocusInWindow();        // make shure focus-lost events will be triggered later
                x.setValue(en.getX());
                y.requestFocusInWindow();
                y.setValue(en.getY());
            }
            public EastNorth getCorr() {
                return new EastNorth(x.getValue(),y.getValue());
            }
        }

        private GuiFieldDouble minXField;
        private GuiFieldDouble minYField;
        private GuiFieldDouble minEastField;
        private GuiFieldDouble minNorthField;
        private JButton getMinButton;
        private JButton getMaxButton;
        private GuiFieldDouble maxNorthField;
        private GuiFieldDouble maxEastField;
        private GuiFieldDouble maxYField;
        private GuiFieldDouble maxXField;
        private GuiPanel panel;
        private JLabel hintField;
        private CoorFields pdfMin;
        private CoorFields pdfMax;
        private CoorFields worldMin;
        private CoorFields worldMax;
        private GuiProjections projectionChooser;
        private FilePlacement18 fc = null;        // reference to enclosing FilePlacement
        private JComponent dependsOnValid = null;

        public PlacementPanel(FilePlacement18 parrent) {
            if (parrent==null) throw new IllegalArgumentException();
            fc=parrent;
        }

        private PlacementPanel () {

        }

        private class Monitor implements FocusListener {

            private PlacementPanel target=null;

            public Monitor(PlacementPanel home) {
                target=home;
            }

            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    target.Verify();
                } catch (Exception ee) {
                }
            }
        }

        void load() {
            /*
             * load GUI from setting
             */
            try {
                minXField.setValue(fc.minX);
                maxXField.setValue(fc.maxX);
                minYField.setValue(fc.minY);
                maxYField.setValue(fc.maxY);
                minEastField.setValue(fc.minEast);
                maxEastField.setValue(fc.maxEast);
                minNorthField.setValue(fc.minNorth);
                maxNorthField.setValue(fc.maxNorth);
                projectionChooser.setProjection(fc.projection);
            } finally {
                Verify();
            }
        }

        void build() {
/*
 * Construct Objects
 *
 */
            Monitor monitor = new Monitor(this);
            projectionChooser = new GuiProjections();

            pdfMin = new CoorFields(minXField = new GuiFieldDouble(0), minYField = new GuiFieldDouble(0));
            minXField.setToolTipText(tr("X-value of bottom left reference point"));
            minXField.addFocusListener(monitor);
            minYField.setToolTipText(tr("Y-value of bottom left reference point"));
            minYField.addFocusListener(monitor);

//
            worldMin = new CoorFields(minEastField = new GuiFieldDouble(0), minNorthField = new GuiFieldDouble(0));
            minEastField.setToolTipText(tr("East-value of bottom left reference point"));
            minEastField.addFocusListener(monitor);
            minNorthField.setToolTipText(tr("North-value of bottom left reference point"));
            minNorthField.addFocusListener(monitor);
//
            getMinButton = new JButton(tr("Get from Preview"));
            getMinButton.setToolTipText(tr("Select a single node from preview"));
            getMinButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setFromCoor(pdfMin);
                }
            });
//
            pdfMax = new CoorFields(maxXField = new GuiFieldDouble(1000), maxYField = new GuiFieldDouble(1000));
            maxXField.setToolTipText(tr("X-value of top right reference point"));
            maxXField.addFocusListener(monitor);
            maxYField.setToolTipText(tr("Y-value of top right  reference point"));
            maxYField.addFocusListener(monitor);
//
            worldMax = new CoorFields(maxEastField = new GuiFieldDouble(1),    maxNorthField = new GuiFieldDouble(1));
            maxEastField.setToolTipText(tr("East-value of top right reference point"));
            maxEastField.addFocusListener(monitor);
            maxNorthField.setToolTipText(tr("North-value of top right reference point"));
            maxNorthField.addFocusListener(monitor);
//
            getMaxButton = new JButton(tr("Get from Preview"));
            getMaxButton.setToolTipText(tr("Select a single node from preview"));
            getMaxButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setFromCoor(pdfMax);
                }
            });

//
            hintField = new JLabel(tr("Check Placement"),SwingConstants.CENTER);
            hintField.setForeground(Color.RED);
//
            panel = new GuiPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createTitledBorder(tr("Bind to coordinates")));

            GridBagConstraints cBasic = new GridBagConstraints(GridBagConstraints.RELATIVE, GridBagConstraints.RELATIVE,
                    1, 1, 0.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.LINE_START,
                    new Insets(1, 0, 0, 4), 0, 0);
            cBasic.gridx = GridBagConstraints.RELATIVE;
            cBasic.gridy = GridBagConstraints.RELATIVE;
            cBasic.insets = new Insets(1, 0, 0, 4);
            cBasic.anchor = GridBagConstraints.LINE_START;
            cBasic.fill = GridBagConstraints.HORIZONTAL;
            cBasic.gridheight = 1;
            cBasic.gridwidth = 1;
            cBasic.ipadx = 0;
            cBasic.ipady = 0;
            cBasic.weightx = 0.0;
            cBasic.weighty = 0.0;
            GridBagConstraints cCornerHeading = (GridBagConstraints) cBasic.clone();
            cCornerHeading.gridwidth = GridBagConstraints.REMAINDER;
            cCornerHeading.gridx = 0;
            cCornerHeading.insets = new Insets(3, 0, 0, 0);

            GridBagConstraints cLine = (GridBagConstraints) cBasic.clone();

            GridBagConstraints cGetButton = (GridBagConstraints) cBasic.clone();
            cGetButton.gridx = 1;
            cGetButton.gridwidth = GridBagConstraints.REMAINDER;
            cGetButton.fill = GridBagConstraints.NONE;

            GridBagConstraints c = new GridBagConstraints();
            /*
             * Projection
             */
            panel.add(projectionChooser.getPanel(), cCornerHeading);
            /*
             * Max Corner
             */
            panel.add(new JLabel(tr("Top right (max) corner:"),SwingConstants.CENTER), cCornerHeading);
            c = (GridBagConstraints) cLine.clone();
            c.weightx = 0.0; panel.add(new JLabel(tr("X:"),SwingConstants.RIGHT),c);
            c.weightx = 1.0; panel.add(maxXField, c);
            c.weightx = 0.0; panel.add(new JLabel(tr("East:"),SwingConstants.RIGHT),c);
            c.weightx = 1.0; panel.add(maxEastField, c);

            c.gridy = 4;
            c.weightx = 0.0; panel.add(new JLabel(tr("Y:"),SwingConstants.RIGHT),c);
            c.weightx = 1.0; panel.add(maxYField, c);
            c.weightx = 0.0; panel.add(new JLabel(tr("North:"),SwingConstants.RIGHT),c);
            c.weightx = 1.0; panel.add(maxNorthField, c);
            panel.add(getMaxButton, cGetButton);
            /*
             * Min Corner
             */
            panel.add(new JLabel(tr("Bottom left (min) corner:"),SwingConstants.CENTER), cCornerHeading);
            c = (GridBagConstraints) cLine.clone();
            c.weightx = 0.0; panel.add(new JLabel(tr("X:"),SwingConstants.RIGHT),c);
            c.weightx = 1.0; panel.add(minXField, c);
            c.weightx = 0.0; panel.add(new JLabel(tr("East:"),SwingConstants.RIGHT),c);
            c.weightx = 1.0; panel.add(minEastField, c);

            c.gridy = 8;
            c.weightx = 0.0; panel.add(new JLabel(tr("Y:"),SwingConstants.RIGHT),c);
            c.weightx = 1.0; panel.add(minYField, c);
            c.weightx = 0.0; panel.add(new JLabel(tr("North:"),SwingConstants.RIGHT),c);
            c.weightx = 1.0; panel.add(minNorthField, c);

            panel.add(getMinButton, cGetButton);
            /*
             * Hints
             */
            c.gridx = 0;c.gridy = 11;c.gridwidth = GridBagConstraints.REMAINDER; c.fill = GridBagConstraints.HORIZONTAL;
            panel.add(hintField, cGetButton);
        }

        private EastNorth getSelectedCoor() {
            /*
             * Assumtions:
             *    selection is from preview,
             *    preview has been create without any projection / transformation
             *    TODO: enshure this
             */
            hintField.setText("");
            Collection<OsmPrimitive> selected = MainApplication.getLayerManager().getEditDataSet().getSelected();

            if (selected.size() != 1 || !(selected.iterator().next() instanceof Node)) {
                hintField.setText(tr("Please select exactly one node."));
                return null;
            }

            LatLon ll = ((Node) selected.iterator().next()).getCoor();
//            FilePlacement pl = new FilePlacement();
//            return pl.reverseTransform(ll);
            return new EastNorth(ll.lon() * 1000, ll.lat() * 1000);
        }

        private void Verify() {
            hintField.setText("");
            String Hint = "";
            fc.valid = false;
            FilePlacement placement = fc;
            try {
                placement.projection = projectionChooser.getProjection();
                try {
                    placement.setPdfBounds(minXField.getValue(), minYField.getValue(), maxXField.getValue(),
                            maxYField.getValue());
                    placement.setEastNorthBounds(minEastField.getValue(), minNorthField.getValue(),
                            maxEastField.getValue(), maxNorthField.getValue());
                } catch (Exception e) {
                    Hint = tr("Check numbers");
                    return;
                }
                String transformError = placement.prepareTransform();
                if (transformError != null) {
                    Hint = transformError;
                    return;
                }
                fc.valid = true;
            } finally {
                hintField.setText(Hint);
                if (dependsOnValid != null)
                    dependsOnValid.setEnabled(fc.valid && panel.isEnabled());
            }

            return;
        }

        public void setDependsOnValid(JComponent c) {
            dependsOnValid = c;
        }

        void setFromCoor(CoorFields f) {
            EastNorth en = getSelectedCoor();

            if (en != null) {
                f.SetCoor(en);
            }

        }

    }

    private PlacementPanel panel=null;
    private boolean valid=false;    // the data is consistent and the object ready to use for transformation

    public boolean isValid() {
        /*
         * TODO: compupte it now
         */
        return valid;
    }
    public void setDependsOnValid(JComponent c) {
        panel.setDependsOnValid(c);
    }

    public JPanel getGui() {
        if (panel==null) panel = new PlacementPanel(this);
        if (panel.panel==null) panel.build();
        return panel.panel;
    }

    public FilePlacement18 () {
        panel = new PlacementPanel(this);
    }

    public void load(File baseFile) throws IOException {
        File file = new File(baseFile + ".placement");
        Properties p = new Properties();
        try (FileInputStream s = new FileInputStream(file)){
            p.load(s);
            s.close();
        };
        fromProperties(p);
    }

    public void verify() {
        panel.Verify();
    }

    public void save(File baseFile) throws IOException {
        File file = new File(baseFile + ".placement");
        FileOutputStream s = new FileOutputStream(file);
        Properties p = toProperties();
        p.store(s, "PDF file placement on OSM");
        s.close();
    }

    private Projection getProjection(Properties p, String name) {
        String projectionCode = p.getProperty("Projection", null);
        if (projectionCode != null) {
            return ProjectionInfo.getProjectionByCode(p.getProperty("Projection", null));
        }
        return null;
    }

    private double getDouble(Properties p, String name, double defaultValue) {
        try {
            return Double.parseDouble(p.getProperty(name));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    protected void fromProperties(Properties p) {
        super.fromProperties(p);
        panel.load();
    }

}
