/**
 * Terracer: A JOSM Plugin for terraced houses.
 *
 * Copyright 2009 CloudMade Ltd.
 *
 * Released under the GPLv2, see LICENSE file for details.
 */
package terracer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;
import org.openstreetmap.josm.tools.GBC;


/**
 * The HouseNumberInputDialog is the layout of the house number input logic.
 * Created with the Eclipse Visual Editor.
 *
 *  This dialog is concerned with the layout, all logic goes into the
 *  HouseNumberinputHandler class.
 *
 * @author casualwalker
 *
 */
public class HouseNumberInputDialog extends ExtendedDialog {
    /*
    final static String MIN_NUMBER = "plugin.terracer.lowest_number";
    final static String MAX_NUMBER = "plugin.terracer.highest_number";
    final static String INTERPOLATION = "plugin.terracer.interpolation_mode";
    */
    final static String HANDLE_RELATION = "plugins.terracer.handle_relation";
    final static String DELETE_OUTLINE = "plugins.terracer.delete_outline";
    final static String INTERPOLATION = "plugins.terracer.interpolation";

    //final private Way street;
    final private String streetName;
    final private String buildingType;
    final private boolean relationExists;
    final ArrayList<Node> housenumbers;

    protected static final String DEFAULT_MESSAGE = tr("Enter housenumbers or amount of segments");
    private static final long serialVersionUID = 1L;
    private Container jContentPane;
    private JPanel inputPanel;
    private JLabel loLabel;
    JTextField lo;
    private JLabel hiLabel;
    JTextField hi;
    private JLabel numbersLabel;
    JTextField numbers;
    private JLabel streetLabel;
    AutoCompletingComboBox streetComboBox;
    private JLabel buildingLabel;
    AutoCompletingComboBox buildingComboBox;
    private JLabel segmentsLabel;
    JTextField segments;
    JTextArea messageLabel;
    private JLabel interpolationLabel;
    Choice interpolation;
    JCheckBox handleRelationCheckBox;
    JCheckBox deleteOutlineCheckBox;

    HouseNumberInputHandler inputHandler;

    /**
     * @param street If street is not null, we assume, the name of the street to be fixed
     * and just show a label. If street is null, we show a ComboBox/InputField.
     * @param streetName the name of the street, derived from either the
     *        street line or the house numbers which are guaranteed to have the
     *        same name attached (may be null)
     * @param buildingType The value to add for building key
     * @param relationExists If the buildings can be added to an existing relation or not.
     * @param housenumbers a list of house numbers in this outline (may be empty)
     */
    public HouseNumberInputDialog(HouseNumberInputHandler handler, Way street, String streetName, String buildingType, boolean relationExists, ArrayList<Node> housenumbers) {
        super(Main.parent,
                tr("Terrace a house"),
                new String[] { tr("OK"), tr("Cancel")},
                true
        );
        this.inputHandler = handler;
        //this.street = street;
        this.streetName = streetName;
        this.buildingType = buildingType;
        this.relationExists = relationExists;
        this.housenumbers = housenumbers;
        handler.dialog = this;
        JPanel content = getInputPanel();
        setContent(content);
        setButtonIcons(new String[] {"ok.png", "cancel.png" });
        getJContentPane();
        initialize();
        setDefaultButton(1);
        setupDialog();
        getRootPane().setDefaultButton(defaultButton);
        setVisible(true);
        lo.requestFocus();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.lo.addFocusListener(this.inputHandler);
        this.hi.addFocusListener(this.inputHandler);
        this.segments.addFocusListener(this.inputHandler);
        this.interpolation.addItemListener(this.inputHandler);
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private Container getJContentPane() {
        if (jContentPane == null) {
            jContentPane = this.getContentPane();
            jContentPane.setLayout(new BoxLayout(jContentPane, BoxLayout.Y_AXIS));
            jContentPane.add(getInputPanel(), jContentPane);
        }
        return jContentPane;
    }

    /**
     * This method initializes inputPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getInputPanel() {
        if (inputPanel == null) {

            GridBagConstraints c = new GridBagConstraints();

            messageLabel = new JTextArea();
            messageLabel.setText(DEFAULT_MESSAGE);
            messageLabel.setAutoscrolls(true);

            messageLabel.setLineWrap(true);
            messageLabel.setRows(2);
            messageLabel.setBackground(new Color(238, 238, 238));
            messageLabel.setEditable(false);
            messageLabel.setFocusable(false); // Needed so that lowest number can have focus immediately

            interpolationLabel = new JLabel(tr("Interpolation"));
            segmentsLabel = new JLabel(tr("Segments"));
            streetLabel = new JLabel(tr("Street"));
            buildingLabel = new JLabel(tr("Building"));
            loLabel = new JLabel(tr("Lowest Number"));
            loLabel.setPreferredSize(new Dimension(111, 16));
            loLabel.setToolTipText(tr("Lowest housenumber of the terraced house"));
            hiLabel = new JLabel(tr("Highest Number"));
            numbersLabel = new JLabel(tr("List of Numbers"));
            loLabel.setPreferredSize(new Dimension(111, 16));
            final String txt = relationExists ? tr("add to existing associatedStreet relation") : tr("create an associatedStreet relation");

            handleRelationCheckBox = new JCheckBox(txt, Main.pref.getBoolean(HANDLE_RELATION, true));
            deleteOutlineCheckBox = new JCheckBox(tr("delete outline way"), Main.pref.getBoolean(DELETE_OUTLINE, true));

            inputPanel = new JPanel();
            inputPanel.setLayout(new GridBagLayout());
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            inputPanel.add(messageLabel, c);

            inputPanel.add(loLabel, GBC.std().insets(3,3,0,0));
            inputPanel.add(getLo(), GBC.eol().fill(GBC.HORIZONTAL).insets(5,3,0,0));
            inputPanel.add(hiLabel, GBC.std().insets(3,3,0,0));
            inputPanel.add(getHi(), GBC.eol().fill(GBC.HORIZONTAL).insets(5,3,0,0));
            inputPanel.add(numbersLabel, GBC.std().insets(3,3,0,0));
            inputPanel.add(getNumbers(), GBC.eol().fill(GBC.HORIZONTAL).insets(5,3,0,0));
            inputPanel.add(interpolationLabel, GBC.std().insets(3,3,0,0));
            inputPanel.add(getInterpolation(), GBC.eol().insets(5,3,0,0));
            inputPanel.add(segmentsLabel, GBC.std().insets(3,3,0,0));
            inputPanel.add(getSegments(), GBC.eol().fill(GBC.HORIZONTAL).insets(5,3,0,0));
            if (streetName == null) {
                inputPanel.add(streetLabel, GBC.std().insets(3,3,0,0));
                inputPanel.add(getStreet(), GBC.eol().insets(5,3,0,0));
            } else {
                inputPanel.add(new JLabel(tr("Street name: ")+"\""+streetName+"\""), GBC.eol().insets(3,3,0,0));
            }
            if (buildingType == null) {
                inputPanel.add(buildingLabel, GBC.std().insets(3,3,0,0));
                inputPanel.add(getBuilding(), GBC.eol().insets(5,3,0,0));
            } else {
                inputPanel.add(new JLabel(tr("Building: ")+"\""+buildingType+"\""), GBC.eol().insets(3,3,0,0));
            }
            inputPanel.add(handleRelationCheckBox, GBC.eol().insets(3,3,0,0));
            inputPanel.add(deleteOutlineCheckBox, GBC.eol().insets(3,3,0,0));
            
            if (numbers.isVisible())
            {
                loLabel.setVisible(false);
                lo.setVisible(false);
                lo.setEnabled(false);
                hiLabel.setVisible(false);
                hi.setVisible(false);
                hi.setEnabled(false);
                interpolationLabel.setVisible(false);
                interpolation.setVisible(false);
                interpolation.setEnabled(false);
                segments.setText(String.valueOf(housenumbers.size()));
                segments.setEditable(false);
            }
        }
        return inputPanel;
    }

    /**
     * Overrides the default actions. Will not close the window when upload trace is clicked
     */
    @Override protected void buttonAction(int buttonIndex, final ActionEvent evt) {
        //String a = evt.getActionCommand();
        this.inputHandler.actionPerformed(evt);
    }

    /**
     * This method initializes lo
     *
     * @return javax.swing.JTextField
     */
    private JTextField getLo() {
        if (lo == null) {
            lo = new JTextField();
            lo.setText("");
        }
        return lo;
    }

    /**
     * This method initializes hi
     *
     * @return javax.swing.JTextField
     */
    private JTextField getHi() {
        if (hi == null) {
            hi = new JTextField();
            hi.setText("");
        }
        return hi;
    }
    
    /**
     * This method initializes numbers
     *
     * @return javax.swing.JTextField
     */
    private JTextField getNumbers() {
        if (numbers == null) {
            numbers = new JTextField();
            
            Iterator<Node> it = housenumbers.iterator();
            StringBuilder s = new StringBuilder(256);
            if (it.hasNext()) {
                s.append(it.next().get("addr:housenumber"));
                while (it.hasNext())
                    s.append(';').append(it.next().get("addr:housenumber"));
            }
            else {
                numbersLabel.setVisible(false);
                numbers.setVisible(false);
            }
            
            numbers.setText(s.toString());
            numbers.setEditable(false);
        }
        return numbers;
    }

    /**
     * This method initializes street
     *
     * @return AutoCompletingComboBox
     */
    private AutoCompletingComboBox getStreet() {

        if (streetComboBox == null) {
            final TreeSet<String> names = createAutoCompletionInfo();

            streetComboBox = new AutoCompletingComboBox();
            streetComboBox.setPossibleItems(names);
            streetComboBox.setEditable(true);
            streetComboBox.setSelectedItem(null);

        }
        return streetComboBox;
    }
    
    /**
     * This method initializes building
     *
     * @return AutoCompletingComboBox
     */
    private AutoCompletingComboBox getBuilding() {

        if (buildingComboBox == null) {
            final List<AutoCompletionListItem> values = Main.main.getCurrentDataSet().getAutoCompletionManager().getValues("building");

            buildingComboBox = new AutoCompletingComboBox();
            buildingComboBox.setPossibleACItems(values);
            buildingComboBox.setEditable(true);
            buildingComboBox.setSelectedItem("yes");

        }
        return buildingComboBox;
    }

    /**
     * This method initializes segments
     *
     * @return javax.swing.JTextField
     */
    private JTextField getSegments() {
        if (segments == null) {
            segments = new JTextField();
            segments.setText("1");
        }
        return segments;
    }

    /**
     * This method initializes interpolation
     *
     * @return java.awt.Choice
     */
    private Choice getInterpolation() {
        if (interpolation == null) {
            interpolation = new Choice();
            interpolation.add(tr("All"));
            interpolation.add(tr("Even/Odd"));
            if (Main.pref.getInteger(INTERPOLATION, 2) == 1) {
                interpolation.select(tr("All"));
            } else {
                interpolation.select(tr("Even/Odd"));
            }
            //return (dialog.interpolation.getSelectedItem().equals(tr("All"))) ? 1 : 2;
        }
        return interpolation;
    }

    /**
     * Generates a list of all visible names of highways in order to do
     * autocompletion on the road name.
     */
    TreeSet<String> createAutoCompletionInfo() {
        final TreeSet<String> names = new TreeSet<String>();
        for (OsmPrimitive osm : Main.main.getCurrentDataSet()
                .allNonDeletedPrimitives()) {
            if (osm.getKeys() != null && osm.keySet().contains("highway")
                    && osm.keySet().contains("name")) {
                names.add(osm.get("name"));
            }
        }
        return names;
    }
}
