// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.surveyor;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.spi.preferences.Config;

import livegps.LiveGpsData;

/**
 * @author cdaller
 *
 */
public class SurveyorComponent extends JComponent implements PropertyChangeListener, GpsDataSource {
    private static final long serialVersionUID = 4539838472057529042L;
    private LiveGpsData gpsData;
    private int rows = 0;
    private int columns = 0;
    private int width = 0;
    private int height = 0;
    private JLabel streetLabel;
    private JPanel buttonPanel;
    private Set<String> hotKeys;

    public SurveyorComponent() {
        super();
        hotKeys = new HashSet<>();
        setLayout(new BorderLayout());
        streetLabel = new JLabel(tr("Way: "));
        float fontSize = Float.parseFloat(Config.getPref().get(SurveyorPlugin.PREF_KEY_STREET_NAME_FONT_SIZE, "35"));
        Config.getPref().put(SurveyorPlugin.PREF_KEY_STREET_NAME_FONT_SIZE, String.valueOf(fontSize));
        streetLabel.setFont(streetLabel.getFont().deriveFont(35f));
        add(streetLabel, BorderLayout.NORTH);
        buttonPanel = new JPanel();
        add(buttonPanel, BorderLayout.CENTER);
    }

    /**
     * Set the number of rows as a string (callback method from xml parser).
     * @param rowsString the row string.
     */
    public void setRows(String rowsString) {
        rows = Integer.parseInt(rowsString);
        buttonPanel.setLayout(new GridLayout(rows, columns));
    }

    /**
     * Set the number of columns as a string (callback method from xml parser).
     * @param columnsString the column string.
     */
    public void setColumns(String columnsString) {
        System.out.println("setting columns to " +columnsString);
        columns = Integer.parseInt(columnsString);
        buttonPanel.setLayout(new GridLayout(rows, columns));
    }

    /**
     * Set the width as a string.
     * @param widthString the width of the component.
     */
    public void setWidth(String widthString) {
        width = Integer.parseInt(widthString);
        if (width > 0 && height > 0) {
            super.setPreferredSize(new Dimension(width, height));
        }
    }

    /**
     * Set the width as a string.
     * @param widthString the width of the component.
     */
    public void setHeight(String heightString) {
        height = Integer.parseInt(heightString);
        if (width > 0 && height > 0) {
            super.setPreferredSize(new Dimension(width, height));
        }
    }

    public void setGridSize(int rows, int cols) {
        setLayout(new GridLayout(rows, cols));
    }

    public void addButton(ButtonDescription description) {
        if (description.getHotkey() != "" && hotKeys.contains(description.getHotkey())) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), 
                    tr("Duplicate hotkey for button ''{0}'' - button will be ignored!", description.getLabel()));
        } else {
            if (rows == 0 && columns == 0) {
                setColumns("4");
            }
            description.setGpsDataSource(this);
            buttonPanel.add(description.createComponent());
            hotKeys.add(description.getHotkey());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("gpsdata".equals(evt.getPropertyName())) {
            gpsData = (LiveGpsData) evt.getNewValue();
            streetLabel.setText(tr("Way: ") + gpsData.getWayInfo());
        }
    }

    @Override
    public LiveGpsData getGpsData() {
        return gpsData;
    }
}
