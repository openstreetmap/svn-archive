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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.widgets.AutoCompleteComboBox;
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
public class HouseNumberInputDialog extends JDialog {
    /*
    final static String MIN_NUMBER = "plugin.terracer.lowest_number";
    final static String MAX_NUMBER = "plugin.terracer.highest_number";
    final static String INTERPOLATION = "plugin.terracer.interpolation_mode";
    */
    final static String HANDLE_RELATION = "plugins.terracer.handle_relation";
    final static String DELETE_OUTLINE = "plugins.terracer.delete_outline";

    final private Way street;
    final private boolean relationExists;

    protected static final String DEFAULT_MESSAGE = tr("Enter housenumbers or amount of segments");
    private static final long serialVersionUID = 1L;
    private Container jContentPane;
    private JPanel inputPanel;
    private JPanel buttonPanel;
    private JLabel loLabel;
    JTextField lo;
    private JLabel hiLabel;
    JTextField hi;
    private JLabel streetLabel;
    AutoCompleteComboBox streetComboBox;
    private JLabel segmentsLabel;
    JTextField segments;
    JTextArea messageLabel;
    JButton okButton;
    JButton cancelButton;
    private JLabel interpolationLabel;
    Choice interpolation;
    JCheckBox handleRelationCheckBox;
    JCheckBox deleteOutlineCheckBox;

    /**
     * @param street If street is not null, we assume, the name of the street to be fixed
     * and just show a label. If street is null, we show a ComboBox/InputField.
     * @param relationExists If the buildings can be added to an existing relation or not.
     */
    public HouseNumberInputDialog(Way street, boolean relationExists) {
        super(JOptionPane.getFrameForComponent(Main.parent));
        this.street = street;
        this.relationExists = relationExists;
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setTitle(tr("Terrace a house"));
        getJContentPane();
        this.pack();
        this.setLocationRelativeTo(Main.parent);
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private Container getJContentPane() {
        if (jContentPane == null) {
            messageLabel = new JTextArea();
            messageLabel.setText(DEFAULT_MESSAGE);
            messageLabel.setAutoscrolls(true);

            messageLabel.setLineWrap(true);
            messageLabel.setRows(2);
            messageLabel.setBackground(new Color(238, 238, 238));
            messageLabel.setEditable(false);
            jContentPane = this.getContentPane();
            jContentPane.setLayout(new BoxLayout(jContentPane,
                    BoxLayout.Y_AXIS));
            jContentPane.add(messageLabel, jContentPane);
            jContentPane.add(getInputPanel(), jContentPane);
            jContentPane.add(getButtonPanel(), jContentPane);

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
            interpolationLabel = new JLabel();
            interpolationLabel.setText(tr("Interpolation"));
            segmentsLabel = new JLabel();
            segmentsLabel.setText(tr("Segments"));
            streetLabel = new JLabel();
            streetLabel.setText(tr("Street"));
            hiLabel = new JLabel();
            hiLabel.setText(tr("Highest Number"));
            loLabel = new JLabel();
            loLabel.setText(tr("Lowest Number"));
            loLabel.setPreferredSize(new Dimension(111, 16));
            loLabel.setToolTipText(tr("Lowest housenumber of the terraced house"));
            final String txt = relationExists ? tr("add to existing associatedStreet relation") : tr("create an associatedStreet relation");
            handleRelationCheckBox = new JCheckBox(txt, Main.pref.getBoolean(HANDLE_RELATION, true));
            deleteOutlineCheckBox = new JCheckBox(tr("delete outline way"), Main.pref.getBoolean(DELETE_OUTLINE, true));

            inputPanel = new JPanel();
            inputPanel.setLayout(new GridBagLayout());
            inputPanel.add(loLabel, GBC.std().insets(3,3,0,0));
            inputPanel.add(getLo(), GBC.eol().fill(GBC.HORIZONTAL).insets(5,3,0,0));
            inputPanel.add(hiLabel, GBC.std().insets(3,3,0,0));
            inputPanel.add(getHi(), GBC.eol().fill(GBC.HORIZONTAL).insets(5,3,0,0));
            inputPanel.add(interpolationLabel, GBC.std().insets(3,3,0,0));
            inputPanel.add(getInterpolation(), GBC.eol().insets(5,3,0,0));
            inputPanel.add(segmentsLabel, GBC.std().insets(3,3,0,0));
            inputPanel.add(getSegments(), GBC.eol().fill(GBC.HORIZONTAL).insets(5,3,0,0));
            if (street == null) {
                inputPanel.add(streetLabel, GBC.std().insets(3,3,0,0));
                inputPanel.add(getStreet(), GBC.eol().insets(5,3,0,0));
            } else {
                inputPanel.add(new JLabel(tr("Street name: ")+"\""+street.get("name")+"\""), GBC.eol().insets(3,3,0,0));
            }
            inputPanel.add(handleRelationCheckBox, GBC.eol().insets(3,3,0,0));
            inputPanel.add(deleteOutlineCheckBox, GBC.eol().insets(3,3,0,0));
        }
        return inputPanel;
    }

    /**
     * This method initializes buttonPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());
            buttonPanel.add(getOkButton(), null);
            buttonPanel.add(getCancelButton(), null);
        }
        return buttonPanel;
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
     * This method initializes street
     *
     * @return javax.swing.JTextField
     */
    private AutoCompleteComboBox getStreet() {

        if (streetComboBox == null) {
            final TreeSet<String> names = createAutoCompletionInfo();

            streetComboBox = new AutoCompleteComboBox();
            streetComboBox.setPossibleItems(names);
            streetComboBox.setEditable(true);
            streetComboBox.setSelectedItem(null);

        }
        return streetComboBox;
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
     * This method initializes okButton
     *
     * @return javax.swing.JButton
     */
    private JButton getOkButton() {
        if (okButton == null) {
            okButton = new JButton();
            okButton.setText(tr("OK"));
            okButton.setName("OK");
        }
        return okButton;
    }

    /**
     * This method initializes cancelButton
     *
     * @return javax.swing.JButton
     */
    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton();
            cancelButton.setText(tr("Cancel"));
            cancelButton.setName("CANCEL");
        }
        return cancelButton;
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
        }
        return interpolation;
    }

    /**
     * Registers the handler as a listener to all relevant events.
     *
     * @param handler the handler
     */
    public void addHandler(HouseNumberInputHandler handler) {
        this.hi.addActionListener(handler);
        this.hi.addFocusListener(handler);

        this.lo.addActionListener(handler);
        this.lo.addFocusListener(handler);

        this.segments.addActionListener(handler);
        this.segments.addFocusListener(handler);

        this.okButton.addActionListener(handler);
        this.cancelButton.addActionListener(handler);

        this.interpolation.addItemListener(handler);

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
