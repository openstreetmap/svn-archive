/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import livegps.LiveGpsData;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.XmlObjectParser;
import org.xml.sax.SAXException;

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
    private Set<String>hotKeys;

    public SurveyorComponent() {
        super();
        hotKeys = new HashSet<String>();
        setLayout(new BorderLayout());
        streetLabel = new JLabel(tr("Way: "));
        float fontSize = Float.parseFloat(Main.pref.get(SurveyorPlugin.PREF_KEY_STREET_NAME_FONT_SIZE, "35"));
        Main.pref.put(SurveyorPlugin.PREF_KEY_STREET_NAME_FONT_SIZE, String.valueOf(fontSize));
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
        if(width > 0 && height > 0) {
            super.setPreferredSize(new Dimension(width, height));
        }
    }

    /**
     * Set the width as a string.
     * @param widthString the width of the component.
     */
    public void setHeight(String heightString) {
        height = Integer.parseInt(heightString);
        if(width > 0 && height > 0) {
            super.setPreferredSize(new Dimension(width, height));
        }
    }

    public void setGridSize(int rows, int cols) {
        setLayout(new GridLayout(rows, cols));
    }

    public void addButton(ButtonDescription description) {
        if(description.getHotkey() != "" &&  hotKeys.contains(description.getHotkey())) {
            JOptionPane.showMessageDialog(Main.parent, tr("Duplicate hotkey for button ''{0}'' - button will be ignored!",description.getLabel()));
        } else {
            if(rows == 0 && columns == 0) {
                setColumns("4");
            }
            description.setGpsDataSource(this);
            buttonPanel.add(description.createComponent());
            hotKeys.add(description.getHotkey());
        }
    }



    public static void main(String[] args) {


        // parse xml file and create component from it:
        Reader in = new InputStreamReader(SurveyorComponent.class.getClassLoader().getResourceAsStream("surveyor.xml"));
        XmlObjectParser parser = new XmlObjectParser();
        parser.mapOnStart("surveyor", SurveyorComponent.class);
        parser.map("button", ButtonDescription.class);
        parser.map("action", SurveyorActionDescription.class);

        SurveyorComponent surveyorComponent = null;
        try {
            parser.start(in);
            List<SurveyorActionDescription> actions = new ArrayList<SurveyorActionDescription>();
            while(parser.hasNext()) {
                Object object = parser.next();
                if (object instanceof SurveyorComponent) {
                    System.out.println("SurveyorComponent " + object);
                    surveyorComponent = (SurveyorComponent) object;
                } else if (object instanceof ButtonDescription) {
                    System.out.println("ButtonDescription " + object);
                    ((ButtonDescription)object).setActions(actions);
                    surveyorComponent.addButton(((ButtonDescription)object));
                    actions.clear();
                } else if (object instanceof SurveyorActionDescription) {
                    System.out.println("SurveyorActionDescription " + object);
                    actions.add((SurveyorActionDescription)object);
                } else {
                    System.err.println("unknown " + object);
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        }

//        SurveyorComponent surveyorComponent = new SurveyorComponent();
//        surveyorComponent.setGridSize(3,3);
//        surveyorComponent.addButton(new ButtonDescription("Tunnel", "T", "images/symbols/tunnel.png", "ConsolePrinterAction", ButtonType.SINGLE));
//        surveyorComponent.addButton(new ButtonDescription("Bridge", "B", null, "ConsolePrinterAction", ButtonType.TOGGLE));
//        surveyorComponent.addButton(new ButtonDescription("Motorway", "M", null, "ConsolePrinterAction", null));
//        surveyorComponent.addButton(new ButtonDescription("Primary", "P", null, "ConsolePrinterAction", null));
//        surveyorComponent.addButton(new ButtonDescription("Secondary", "S", null, "ConsolePrinterAction", null));
//        surveyorComponent.addButton(new ButtonDescription("Unclassified", "U", null, "ConsolePrinterAction", null));
//        surveyorComponent.addButton(new ButtonDescription("Residential", "R", null, "ConsolePrinterAction", null));
//        surveyorComponent.addButton(new ButtonDescription("Parking", "P", "images/symbols/parking.png", "ConsolePrinterAction", null));

        JFrame frame = new JFrame();
        frame.add(surveyorComponent);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if("gpsdata".equals(evt.getPropertyName())) {
            gpsData = (LiveGpsData) evt.getNewValue();
            streetLabel.setText(tr("Way: ") + gpsData.getWayInfo());
        }

    }

    /* (non-Javadoc)
     * @see at.dallermassl.josm.plugin.surveyor.GpsDataSource#getGpsData()
     */
    public LiveGpsData getGpsData() {
        return gpsData;
    }

}
