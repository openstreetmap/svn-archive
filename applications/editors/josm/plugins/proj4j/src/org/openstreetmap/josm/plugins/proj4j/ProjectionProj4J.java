//License: GPL. For details, see README file.
package org.openstreetmap.josm.plugins.proj4j;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.event.DocumentEvent;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JLabel;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.tools.GBC;

/* TODO:
 *      * Improve GUI layout
 *      * Rather than reading an auxiliary file containing projection information,
 *          add it to Proj4J directly
 *      * Allow user to input proj.4 settings explicitly, or load from a file
 */
public class ProjectionProj4J implements org.openstreetmap.josm.data.projection.Projection, org.openstreetmap.josm.data.projection.ProjectionSubPrefs {

    private String crsCode = "EPSG:4326";
    private String filterText = "";
    private org.osgeo.proj4j.CoordinateReferenceSystem proj4jCRS;
    private org.osgeo.proj4j.CoordinateReferenceSystem wgs84CRS;
    private org.osgeo.proj4j.CoordinateTransform transformToWGS84;
    private org.osgeo.proj4j.CoordinateTransform transformFromWGS84;
    private JTable table;
    private JTextField filterTextField;
    private JLabel selectedLabel;
    private CRSTableModel model;
    private TableRowSorter<CRSTableModel> sorter;
    private ActionListener actionListener;

    public ProjectionProj4J() {
        super();

        try {
            model = new CRSTableModel();
        } catch (IOException e) {
            // TODO: show message if IOException is thrown, though shouldn't
            // since projections.txt is included in the JAR
        }

        sorter = new TableRowSorter<CRSTableModel>(model);

        setupTransformations();
    }

    @Override
    public void setupPreferencePanel(JPanel p, ActionListener actionListener) {
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(new GridBagLayout());

        JLabel filterLabel = new JLabel(tr("Filter"), SwingConstants.TRAILING);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GBC.NONE;
        p.add(filterLabel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.fill = GBC.HORIZONTAL;
        filterTextField = new JTextField(filterText, 20);
        filterTextField.getDocument().addDocumentListener(
                new DocumentListener() {

                    public void insertUpdate(DocumentEvent e) {
                        newFilter();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        newFilter();
                    }

                    public void changedUpdate(DocumentEvent e) {
                        newFilter();
                    }
                });
        p.add(filterTextField, c);

        selectedLabel = new JLabel(tr("Selected: {0}", crsCode), SwingConstants.TRAILING);
        c.gridx = 3;
        c.gridy = 0;
        c.fill = GBC.HORIZONTAL;
        p.add(selectedLabel);

        filterLabel.setLabelFor(filterTextField);

        table = new JTable(model);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GBC.BOTH;
        c.gridwidth = 3;
        p.add(scrollPane, c);

        // Update table using filterText
        newFilter();

        // Select and show row containing saved projection/CRS, if visible
        int index = model.findCode(crsCode);
        if (index >= 0) {
            index = table.convertRowIndexToView(index);
            if (index >= 0) {
                table.addRowSelectionInterval(index, index);
                table.scrollRectToVisible(table.getCellRect(index, 0, true));
            }
        }

        // add listener to react to table selections
        table.getSelectionModel().addListSelectionListener(
                new SelectionListener(table));

        this.actionListener = actionListener;
    }

    @Override
    public Collection<String> getPreferences(JPanel p) {
        return Arrays.asList(crsCode, filterText);
    }

    @Override
    public Collection<String> getPreferencesFromCode(String code) {
        // TODO: need to return null if code is not supported
        return Collections.singleton(code);
    }

    @Override
    public void setPreferences(Collection<String> args) {
        if (args != null) {
            String[] array = args.toArray(new String[0]);
            if (array.length > 0) {
                crsCode = array[0];
            }
            if (array.length > 1) {
                filterText = array[1];
            }
            setupTransformations();
        }
    }

    @Override
    public double getDefaultZoomInPPD() {
        //TODO: this needs to be changed per projection
        return 1.01;
    }

    /**
     * @param LatLon WGS84 (in degree)
     * @return xy east/north (in whatever unit the projection uses, m/ft/deg/etc)
     */
    @Override
    public EastNorth latlon2eastNorth(LatLon p) {
        org.osgeo.proj4j.ProjCoordinate pc1 = new org.osgeo.proj4j.ProjCoordinate(p.lon(), p.lat());
        org.osgeo.proj4j.ProjCoordinate pc2 = new org.osgeo.proj4j.ProjCoordinate();
        //System.out.println("From " + pc1.x + " " + pc1.y);
        transformFromWGS84.transform(pc1, pc2);
        //System.out.println("To " + pc2.x + " " + pc2.y);
        return new EastNorth(pc2.x, pc2.y);
    }

    /**
     * @param xy east/north (in whatever unit the projection uses, m/ft/deg/etc)
     * @return LatLon WGS84 (in degree)
     */
    @Override
    public LatLon eastNorth2latlon(EastNorth p) {
        org.osgeo.proj4j.ProjCoordinate pc1 = new org.osgeo.proj4j.ProjCoordinate(p.east(), p.north());
        org.osgeo.proj4j.ProjCoordinate pc2 = new org.osgeo.proj4j.ProjCoordinate();
        //System.out.println("InvFrom " + pc1.x + " " + pc1.y);
        transformToWGS84.transform(pc1, pc2);
        //System.out.println("InvTo " + pc2.x + " " + pc2.y);
        return new LatLon(pc2.y, pc2.x);
    }

    @Override
    public String toString() {
        // TODO: include description in string
        return tr("Proj4J: " + crsCode);
    }

    @Override
    public String toCode() {
        return crsCode;
    }

    @Override
    public String getCacheDirectoryName() {
        return "Proj4J";
    }

    @Override
    public Bounds getWorldBoundsLatLon() {
        org.osgeo.proj4j.proj.Projection proj = proj4jCRS.getProjection();

        // FIXME: Do we need to convert these coords because of possibly differing datums?
        LatLon min = new LatLon(proj.getMinLatitudeDegrees(), proj.getMinLongitudeDegrees());
        LatLon max = new LatLon(proj.getMaxLatitudeDegrees(), proj.getMaxLongitudeDegrees());
        return new Bounds(min, max);
    }

    @Override
    public String[] allCodes() {
        // get all available codes from the model
        return model.getAllCodes();
    }

    private void updateSelectedCode() {
        // if row is selected, set it as the active projection (CRS)
        int index = table.getSelectedRow();
        if (index >= 0) {
            index = table.convertRowIndexToModel(index);
            if (index >= 0) {
                crsCode = model.getCRSEntryAt(index).getName();
                selectedLabel.setText(tr("Selected: {0}", crsCode));
                setupTransformations();
            }
        }
    }

    class SelectionListener implements ListSelectionListener {

        JTable table;

        SelectionListener(JTable table) {
            this.table = table;
        }

        public void valueChanged(ListSelectionEvent e) {
            updateSelectedCode();
        }
    }

    private void setupTransformations() {
        org.osgeo.proj4j.CRSFactory crsFactory =
                new org.osgeo.proj4j.CRSFactory();
        org.osgeo.proj4j.CoordinateTransformFactory transFactory =
                new org.osgeo.proj4j.CoordinateTransformFactory();

        // Create coordinate reference systems for source and target
        proj4jCRS = crsFactory.createFromName(crsCode);
        wgs84CRS = crsFactory.createFromName("EPSG:4326");

        // Create transformations between CRS
        transformToWGS84 = transFactory.createTransform(proj4jCRS, wgs84CRS);
        transformFromWGS84 = transFactory.createTransform(wgs84CRS, proj4jCRS);

        if (actionListener != null) {
            actionListener.actionPerformed(new ActionEvent(selectedLabel, 0, "CRS changed"));
        }
    }

    private void newFilter() {
        RowFilter<CRSTableModel, Object> rf = null;
        filterText = filterTextField.getText();
        try {
            // Create case-insensitive filter using "(?i)"
            rf = RowFilter.regexFilter("(?i)" + filterText);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(rf);
    }

    class CRSEntry {

        public String authority;
        public String code;
        public String description;

        public CRSEntry(String authority, String code, String description) {
            this.code = code;
            this.authority = authority;
            this.description = description;
        }

        public String getName() {
            return authority + ":" + code;
        }

        public String getDescription() {
            return description;
        }
    }

    class CRSTableModel extends AbstractTableModel {

        private String[] columnNames = {tr("Code"), tr("Description")};
        private ArrayList<CRSEntry> crsList = new ArrayList<CRSEntry>();

        public CRSTableModel() throws java.io.IOException {
            // Read projection information from file, (authority, code, description)
            InputStream inStr = getClass().getResourceAsStream("/resources/projections.txt");
            BufferedReader fh = new BufferedReader(new InputStreamReader(inStr));

            String s;
            while ((s = fh.readLine()) != null) {
                String f[] = s.split("\t");
                crsList.add(new CRSEntry(f[0], f[1], f[2]));
            }
            fh.close();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return crsList.size();
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row >= crsList.size()) {
                return "Empty";
            }

            if (col == 0) {
                return crsList.get(row).getName();
            } else {
                return crsList.get(row).getDescription();
            }
        }

        public CRSEntry getCRSEntryAt(int index) {
            return crsList.get(index);
        }

        public String[] getAllCodes() {
            int size = crsList.size();
            String[] codes = new String[size];
            for (int i = 0; i < size; i++) {
                codes[i] = crsList.get(i).getName();
            }
            return codes;
        }

        public int findCode(String code) {
            int size = crsList.size();
            for (int i = 0; i < size; i++) {
                if (code.compareTo(crsList.get(i).getName()) == 0) {
                    return i;
                }
            }
            return -1;
        }
    }
}
