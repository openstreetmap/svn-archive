// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.housenumbertool;

import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.tagging.ac.AutoCompletionItem;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

/**
 * @author Oliver Raupach 09.01.2012
 * @author Victor Kropp 10.03.2012
 */
public class TagDialog extends ExtendedDialog {
    private static final String APPLY_CHANGES = tr("Apply Changes");
    private static final String TAG_STREET_OR_PLACE = tr("Use tag ''addr:street'' or ''addr:place''");

    private static final String TAG_BUILDING = "building";
    private static final String TAG_SOURCE = "source";
    private static final String TAG_ADDR_COUNTRY = "addr:country";
    private static final String TAG_ADDR_STATE = "addr:state";
    private static final String TAG_ADDR_CITY = "addr:city";
    private static final String TAG_ADDR_POSTCODE = "addr:postcode";
    private static final String TAG_ADDR_HOUSENUMBER = "addr:housenumber";
    private static final String TAG_ADDR_STREET = "addr:street";
    private static final String TAG_ADDR_PLACE = "addr:place";
    private static final String TAG_ADDR_SUBURB = "addr:suburb";

    private static final String[] BUILDING_STRINGS = {
        "yes", "apartments", "chapel", "church", "commercial", "dormitory", "hotel", "house", "residential", "terrace",  
        "industrial", "retail", "warehouse", "cathedral",  "civic", "hospital", "school", "train_station", "transportation", 
        "university", "public", "bridge", "bunker", "cabin", "construction", "farm_auxiliary", "garage", "garages", 
        "greenhouse", "hangar", "hut", "roof", "shed", "stable"};

    private static final int FPS_MIN = -10;
    private static final int FPS_MAX =  10;

    private static final Logger LOGGER = Logger.getLogger(TagDialog.class.getName());

    private File pluginDir;
    private AutoCompletionManager acm;
    private OsmPrimitive selection;

    private static final String TEMPLATE_DATA = "/template.data";

    private AutoCompletingComboBox source;
    private AutoCompletingComboBox country;
    private AutoCompletingComboBox state;
    private AutoCompletingComboBox suburb;
    private AutoCompletingComboBox city;
    private AutoCompletingComboBox postcode;
    private AutoCompletingComboBox street;
    private JTextField housnumber;
    private JCheckBox buildingEnabled;
    private JCheckBox sourceEnabled;
    private JCheckBox countryEnabled;
    private JCheckBox stateEnabled;
    private JCheckBox cityEnabled;
    private JCheckBox suburbEnabled;
    private JCheckBox zipEnabled;
    private JCheckBox streetEnabled;
    private JCheckBox housenumberEnabled;
    private JSlider housenumberChangeSequence;
    private JComboBox<String> building;
    private JRadioButton streetRadio;
    private JRadioButton placeRadio;

    /**
     * Constructs a new {@code TagDialog}.
     * @param pluginDir plugin directory
     * @param selection selected primitive
     */
    public TagDialog(File pluginDir, OsmPrimitive selection) {
        super(MainApplication.getMainFrame(), tr("House Number Editor"), new String[] { tr("OK"), tr("Cancel") }, true);
        this.pluginDir = pluginDir;
        this.selection = selection;

        JPanel editPanel = createContentPane();

        setPreferredSize(new Dimension(900, 500));
        setMinimumSize(new Dimension(900, 500));

        setContent(editPanel);
        setButtonIcons("ok", "cancel");
        setDefaultButton(1);
        setupDialog();
        getRootPane().setDefaultButton(defaultButton);

        // middle of the screen
        setLocationRelativeTo(null);

        SwingUtilities.invokeLater(() -> {
            housnumber.requestFocus();
            housnumber.selectAll();
        });
    }

    private JPanel createContentPane() {
        acm = AutoCompletionManager.of(selection.getDataSet());

        Dto dto = loadDto();

        JPanel editPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel labelNewValues = new JLabel();
        Font newLabelFont = new Font(labelNewValues.getFont().getName(), Font.BOLD, labelNewValues.getFont().getSize());
        labelNewValues.setFont(newLabelFont);
        labelNewValues.setText(tr("New values:"));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(0, 5, 10, 5);
        editPanel.add(labelNewValues, c);

        JLabel labelExistingValues = new JLabel();
        labelExistingValues.setFont(newLabelFont);
        labelExistingValues.setText(tr("Existing values:"));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 5;
        c.gridy = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(0, 5, 10, 5);
        editPanel.add(labelExistingValues, c);

        JButton getAllButton = new JButton("<<");
        getAllButton.setPreferredSize(new Dimension(60, 24));
        getAllButton.setToolTipText(tr("Accept all existing values"));
        getAllButton.addActionListener(actionEvent -> acceptAllExistingValues());
        GridBagConstraints buttonContstraints = new GridBagConstraints();
        buttonContstraints.fill = GridBagConstraints.NONE;
        buttonContstraints.gridx = 6;
        buttonContstraints.gridy = 0;
        buttonContstraints.weightx = 0;
        buttonContstraints.gridwidth = 1;
        buttonContstraints.anchor = GridBagConstraints.EAST;
        buttonContstraints.insets = new Insets(0, 5, 10, 5);
        editPanel.add(getAllButton, buttonContstraints);

        // building
        buildingEnabled = new JCheckBox(TAG_BUILDING);
        buildingEnabled.setFocusable(false);
        buildingEnabled.setSelected(dto.isSaveBuilding());
        buildingEnabled.setToolTipText(APPLY_CHANGES);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(buildingEnabled, c);

        Arrays.sort(BUILDING_STRINGS);
        building = new JComboBox<>(BUILDING_STRINGS);
        building.setSelectedItem(dto.getBuilding());
        building.setMaximumRowCount(50);
        c.gridx = 3;
        c.gridy = 1;
        c.weightx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(building, c);

        JButton getBuildingButton = new JButton("<");
        getBuildingButton.setPreferredSize(new Dimension(45, 24));
        getBuildingButton.setToolTipText(tr("Accept existing value"));
        getBuildingButton.addActionListener(actionEvent -> building.setSelectedItem(selection.get(TAG_BUILDING)));
        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        c.gridy = 1;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(getBuildingButton, c);

        JTextField existingBuilding = new JTextField();
        existingBuilding.setText(selection.get(TAG_BUILDING));
        existingBuilding.setPreferredSize(new Dimension(200, 24));
        existingBuilding.setEditable(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 5;
        c.gridy = 1;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(existingBuilding, c);

        // source
        sourceEnabled = new JCheckBox(TAG_SOURCE);
        sourceEnabled.setFocusable(false);
        sourceEnabled.setSelected(dto.isSaveBuilding());
        sourceEnabled.setToolTipText(APPLY_CHANGES);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(sourceEnabled, c);

        source = new AutoCompletingComboBox();
        source.setPossibleAcItems(acm.getTagValues(TAG_SOURCE));
        source.setPreferredSize(new Dimension(200, 24));
        source.setEditable(true);
        source.setSelectedItem(dto.getSource());
        c.gridx = 3;
        c.gridy = 2;
        c.weightx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(source, c);

        JButton getSourceButton = new JButton("<");
        getSourceButton.setPreferredSize(new Dimension(45, 24));
        getSourceButton.setToolTipText(tr("Accept existing value"));
        getSourceButton.addActionListener(actionEvent -> source.setSelectedItem(selection.get(TAG_SOURCE)));
        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        c.gridy = 2;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(getSourceButton, c);

        JTextField existingSource = new JTextField();
        existingSource.setText(selection.get(TAG_SOURCE));
        existingSource.setPreferredSize(new Dimension(200, 24));
        existingSource.setEditable(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 5;
        c.gridy = 2;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(existingSource, c);

        // country
        countryEnabled = new JCheckBox(TAG_ADDR_COUNTRY);
        countryEnabled.setFocusable(false);
        countryEnabled.setSelected(dto.isSaveCountry());
        countryEnabled.setToolTipText(APPLY_CHANGES);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(countryEnabled, c);

        country = new AutoCompletingComboBox();
        country.setPossibleAcItems(acm.getTagValues(TAG_ADDR_COUNTRY));
        country.setPreferredSize(new Dimension(200, 24));
        country.setEditable(true);
        country.setSelectedItem(dto.getCountry());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(country, c);

        JButton getCountryButton = new JButton("<");
        getCountryButton.setPreferredSize(new Dimension(45, 24));
        getCountryButton.setToolTipText(tr("Accept existing value"));
        getCountryButton.addActionListener(actionEvent -> country.setSelectedItem(selection.get(TAG_ADDR_COUNTRY)));
        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        c.gridy = 3;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(getCountryButton, c);

        JTextField existingCountry = new JTextField();
        existingCountry.setText(selection.get(TAG_ADDR_COUNTRY));
        existingCountry.setPreferredSize(new Dimension(200, 24));
        existingCountry.setEditable(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 5;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(existingCountry, c);

        // state
        stateEnabled = new JCheckBox(TAG_ADDR_STATE);
        stateEnabled.setFocusable(false);
        stateEnabled.setSelected(dto.isSaveState());
        stateEnabled.setToolTipText(APPLY_CHANGES);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(stateEnabled, c);

        state = new AutoCompletingComboBox();
        state.setPossibleAcItems(acm.getTagValues(TAG_ADDR_STATE));
        state.setPreferredSize(new Dimension(200, 24));
        state.setEditable(true);
        state.setSelectedItem(dto.getState());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(state, c);

        JButton getStateButton = new JButton("<");
        getStateButton.setPreferredSize(new Dimension(45, 24));
        getStateButton.setToolTipText(tr("Accept existing value"));
        getStateButton.addActionListener(actionEvent -> state.setSelectedItem(selection.get(TAG_ADDR_STATE)));
        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        c.gridy = 4;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(getStateButton, c);

        JTextField existingState= new JTextField();
        existingState.setText(selection.get(TAG_ADDR_STATE));
        existingState.setPreferredSize(new Dimension(200, 24));
        existingState.setEditable(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 5;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(existingState, c);

        // suburb
        suburbEnabled = new JCheckBox(TAG_ADDR_SUBURB);
        suburbEnabled.setFocusable(false);
        suburbEnabled.setSelected(dto.isSaveSuburb());
        suburbEnabled.setToolTipText(APPLY_CHANGES);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(suburbEnabled, c);

        suburb = new AutoCompletingComboBox();
        suburb.setPossibleAcItems(acm.getTagValues(TAG_ADDR_SUBURB));
        suburb.setPreferredSize(new Dimension(200, 24));
        suburb.setEditable(true);
        suburb.setSelectedItem(dto.getSuburb());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(suburb, c);

        JButton getSuburbButton = new JButton("<");
        getSuburbButton.setPreferredSize(new Dimension(45, 24));
        getSuburbButton.setToolTipText(tr("Accept existing value"));
        getSuburbButton.addActionListener(actionEvent -> suburb.setSelectedItem(selection.get(TAG_ADDR_SUBURB)));
        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        c.gridy = 5;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(getSuburbButton, c);

        JTextField existingSuburb = new JTextField();
        existingSuburb.setText(selection.get(TAG_ADDR_SUBURB));
        existingSuburb.setPreferredSize(new Dimension(200, 24));
        existingSuburb.setEditable(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 5;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(existingSuburb, c);

        // city
        cityEnabled = new JCheckBox(TAG_ADDR_CITY);
        cityEnabled.setFocusable(false);
        cityEnabled.setSelected(dto.isSaveCity());
        cityEnabled.setToolTipText(APPLY_CHANGES);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 6;
        c.weightx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(cityEnabled, c);

        city = new AutoCompletingComboBox();
        city.setPossibleAcItems(acm.getTagValues(TAG_ADDR_CITY));
        city.setPreferredSize(new Dimension(200, 24));
        city.setEditable(true);
        city.setSelectedItem(dto.getCity());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 6;
        c.weightx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(city, c);

        JButton getCityButton = new JButton("<");
        getCityButton.setPreferredSize(new Dimension(45, 24));
        getCityButton.setToolTipText(tr("Accept existing value"));
        getCityButton.addActionListener(actionEvent -> city.setSelectedItem(selection.get(TAG_ADDR_CITY)));
        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        c.gridy = 6;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(getCityButton, c);

        JTextField existingCity = new JTextField();
        existingCity.setText(selection.get(TAG_ADDR_CITY));
        existingCity.setPreferredSize(new Dimension(200, 24));
        existingCity.setEditable(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 5;
        c.gridy = 6;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(existingCity, c);

        // postcode
        zipEnabled = new JCheckBox(TAG_ADDR_POSTCODE);
        zipEnabled.setFocusable(false);
        zipEnabled.setSelected(dto.isSavePostcode());
        zipEnabled.setToolTipText(APPLY_CHANGES);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 7;
        c.weightx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(zipEnabled, c);

        postcode = new AutoCompletingComboBox();
        postcode.setPossibleAcItems(acm.getTagValues(TAG_ADDR_POSTCODE));
        postcode.setPreferredSize(new Dimension(200, 24));
        postcode.setEditable(true);
        postcode.setSelectedItem(dto.getPostcode());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 7;
        c.weightx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(postcode, c);

        JButton getPostcodeButton = new JButton("<");
        getPostcodeButton.setPreferredSize(new Dimension(45, 24));
        getPostcodeButton.setToolTipText(tr("Accept existing value"));
        getPostcodeButton.addActionListener(actionEvent -> postcode.setSelectedItem(selection.get(TAG_ADDR_POSTCODE)));
        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        c.gridy = 7;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(getPostcodeButton, c);

        JTextField existingPostcode = new JTextField();
        existingPostcode.setText(selection.get(TAG_ADDR_POSTCODE));
        existingPostcode.setPreferredSize(new Dimension(200, 24));
        existingPostcode.setEditable(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 5;
        c.gridy = 7;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(existingPostcode, c);

        // street
        streetEnabled = new JCheckBox();
        streetEnabled.setFocusable(false);
        streetEnabled.setSelected(dto.isSaveStreet());
        streetEnabled.setToolTipText(APPLY_CHANGES);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 8;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(streetEnabled, c);

        streetRadio = new JRadioButton(TAG_ADDR_STREET);
        streetRadio.setToolTipText(TAG_STREET_OR_PLACE);
        streetRadio.setSelected(dto.isTagStreet());
        streetRadio.addItemListener(new RadioChangeListener());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 8;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(streetRadio, c);

        placeRadio = new JRadioButton("addr:place");
        placeRadio.setToolTipText(TAG_STREET_OR_PLACE);
        placeRadio.setSelected(!dto.isTagStreet());
        placeRadio.addItemListener(new RadioChangeListener());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 8;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(placeRadio, c);

        ButtonGroup g = new ButtonGroup();
        g.add(streetRadio);
        g.add(placeRadio);

        street = new AutoCompletingComboBox();
        if (dto.isTagStreet()) {
            street.setPossibleItems(getPossibleStreets());
        } else {
            street.setPossibleAcItems(acm.getTagValues(TAG_ADDR_PLACE));
        }
        street.setPreferredSize(new Dimension(200, 24));
        street.setEditable(true);
        street.setSelectedItem(dto.getStreet());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 8;
        c.weightx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(street, c);

        JButton getStreetButton = new JButton("<");
        getStreetButton.setPreferredSize(new Dimension(45, 24));
        getStreetButton.setToolTipText(tr("Accept existing value"));
        getStreetButton.addActionListener(actionEvent -> updateStreetOrPlaceValues());
        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        c.gridy = 8;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(getStreetButton, c);

        JTextField streetOrPlace = new JTextField();
        streetOrPlace.setText(getStreetOrPlaceTag());
        streetOrPlace.setPreferredSize(new Dimension(50, 24));
        streetOrPlace.setEditable(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 5;
        c.gridy = 8;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(streetOrPlace, c);

        JTextField existingStreet = new JTextField();
        existingStreet.setText(getStreetOrPlaceValue());
        existingStreet.setPreferredSize(new Dimension(100, 24));
        existingStreet.setEditable(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 6;
        c.gridy = 8;
        c.weightx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(existingStreet, c);


        // housenumber
        housenumberEnabled = new JCheckBox(TAG_ADDR_HOUSENUMBER);
        housenumberEnabled.setFocusable(false);
        housenumberEnabled.setSelected(dto.isSaveHousenumber());
        housenumberEnabled.setToolTipText(APPLY_CHANGES);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 9;
        c.weightx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(housenumberEnabled, c);

        housnumber = new JTextField();
        housnumber.setPreferredSize(new Dimension(200, 24));

        String number = HouseNumberHelper.incrementHouseNumber(dto.getHousenumber(), dto.getHousenumberChangeValue());
        if (number != null) {
            housnumber.setText(number);
        }

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 3;
        c.gridy = 9;
        c.weightx = 1;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(housnumber, c);

        JButton getHousenumberButton = new JButton("<");
        getHousenumberButton.setPreferredSize(new Dimension(45, 24));
        getHousenumberButton.setToolTipText(tr("Accept existing value"));
        getHousenumberButton.addActionListener(actionEvent -> housnumber.setText(selection.get(TAG_ADDR_HOUSENUMBER)));
        c.fill = GridBagConstraints.NONE;
        c.gridx = 4;
        c.gridy = 9;
        c.weightx = 0;
        c.gridwidth = 1;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(getHousenumberButton, c);

        JTextField existingHousenumber = new JTextField();
        existingHousenumber.setText(selection.get(TAG_ADDR_HOUSENUMBER));
        existingHousenumber.setPreferredSize(new Dimension(200, 24));
        existingHousenumber.setEditable(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 5;
        c.gridy = 9;
        c.weightx = 1;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(existingHousenumber, c);

        // increment
        JLabel seqLabel = new JLabel(tr("House number increment:"));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 10;
        c.weightx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(5, 5, 0, 5);
        editPanel.add(seqLabel, c);

        housenumberChangeSequence = new JSlider(JSlider.HORIZONTAL, FPS_MIN, FPS_MAX, dto.getHousenumberChangeValue());
        housenumberChangeSequence.setPaintTicks(true);
        housenumberChangeSequence.setMajorTickSpacing(1);
        housenumberChangeSequence.setMinorTickSpacing(1);
        housenumberChangeSequence.setPaintLabels(true);
        housenumberChangeSequence.setSnapToTicks(true);
        c.gridx = 3;
        c.gridy = 10;
        c.weightx = 1;
        c.gridwidth = 4;
        c.insets = new Insets(20, 5, 10, 5);
        editPanel.add(housenumberChangeSequence, c);

        return editPanel;
    }

    private void acceptAllExistingValues() {
        updateStreetOrPlaceValues();
        building.setSelectedItem(selection.get(TAG_BUILDING));
        source.setSelectedItem(selection.get(TAG_SOURCE));
        country.setSelectedItem(selection.get(TAG_ADDR_COUNTRY));
        state.setSelectedItem(selection.get(TAG_ADDR_STATE));
        suburb.setSelectedItem(selection.get(TAG_ADDR_SUBURB));
        city.setSelectedItem(selection.get(TAG_ADDR_CITY));
        postcode.setSelectedItem(selection.get(TAG_ADDR_POSTCODE));
        housnumber.setText(selection.get(TAG_ADDR_HOUSENUMBER));
    }

    private void updateStreetOrPlaceValues() {
        if (selection.hasTag(TAG_ADDR_PLACE)) {
            placeRadio.setSelected(true);
            street.setSelectedItem(selection.get(TAG_ADDR_PLACE));
        }else {
            streetRadio.setSelected(true);
            street.setSelectedItem(selection.get(TAG_ADDR_STREET));
        }
    }

    private String getStreetOrPlaceValue() {
        if (selection.hasTag(TAG_ADDR_PLACE)) {
            return selection.get(TAG_ADDR_PLACE);
        } else if (selection.hasTag(TAG_ADDR_STREET)) {
            return selection.get(TAG_ADDR_STREET);
        } else {
            return "";
        }
    }

    private String getStreetOrPlaceTag() {
        if (selection.hasTag(TAG_ADDR_PLACE)) {
            return TAG_ADDR_PLACE;
        } else if (selection.hasTag(TAG_ADDR_STREET)) {
            return TAG_ADDR_STREET;
        } else {
            return "";
        }
    }

    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        if (buttonIndex == 0) {
            Dto dto = new Dto();
            dto.setSaveBuilding(buildingEnabled.isSelected());
            dto.setSaveSource(sourceEnabled.isSelected());
            dto.setSaveCity(cityEnabled.isSelected());
            dto.setSaveCountry(countryEnabled.isSelected());
            dto.setSaveState(stateEnabled.isSelected());
            dto.setSaveHousenumber(housenumberEnabled.isSelected());
            dto.setSavePostcode(zipEnabled.isSelected());
            dto.setSaveStreet(streetEnabled.isSelected());
            dto.setTagStreet(streetRadio.isSelected());
            dto.setSaveSuburb(suburbEnabled.isSelected());

            dto.setBuilding((String) building.getSelectedItem());
            dto.setSource(getAutoCompletingComboBoxValue(source));
            dto.setCity(getAutoCompletingComboBoxValue(city));
            dto.setCountry(getAutoCompletingComboBoxValue(country));
            dto.setHousenumber(housnumber.getText());
            dto.setPostcode(getAutoCompletingComboBoxValue(postcode));
            dto.setStreet(getAutoCompletingComboBoxValue(street));
            dto.setState(getAutoCompletingComboBoxValue(state));
            dto.setSuburb(getAutoCompletingComboBoxValue(suburb));
            dto.setHousenumberChangeValue(housenumberChangeSequence.getValue());

            updateJOSMSelection(selection, dto);
            saveDto(dto);
        }
        setVisible(false);
    }

    private String getAutoCompletingComboBoxValue(AutoCompletingComboBox box) {
        Object item = box.getSelectedItem();
        if (item != null) {
            if (item instanceof String) {
                return (String) item;
            }
            if (item instanceof AutoCompletionItem) {
                return ((AutoCompletionItem) item).getValue();
            }
            return item.toString();
        } else {
            return "";
        }
    }

    protected void saveDto(Dto dto) {
        File path = pluginDir;
        File fileName = new File(pluginDir + TagDialog.TEMPLATE_DATA);

        try {
            path.mkdirs();
            try (
                FileOutputStream file = new FileOutputStream(fileName);
                ObjectOutputStream o = new ObjectOutputStream(file)
            ) {
                o.writeObject(dto);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            fileName.delete();
        }
    }

    protected void updateJOSMSelection(OsmPrimitive selection, Dto dto) {
        List<Command> commands = new ArrayList<>();

        if (dto.isSaveBuilding()) {
            String value = selection.get(TagDialog.TAG_BUILDING);
            if (value == null || (value != null && !value.equals(dto.getBuilding()))) {
                ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_BUILDING, dto.getBuilding());
                commands.add(command);
            }
        }

        if (dto.isSaveSource()) {
            String value = selection.get(TagDialog.TAG_SOURCE);
            if (value == null || (value != null && !value.equals(dto.getSource()))) {
                ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_SOURCE, dto.getSource());
                commands.add(command);
            }
        }

        if (dto.isSaveCity()) {
            String value = selection.get(TagDialog.TAG_ADDR_CITY);
            if (value == null || (value != null && !value.equals(dto.getCity()))) {
                ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_CITY, dto.getCity());
                commands.add(command);
            }
        }

        if (dto.isSaveCountry())  {
            String value = selection.get(TagDialog.TAG_ADDR_COUNTRY);
            if (value == null || (value != null && !value.equals(dto.getCountry()))) {
                ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_COUNTRY, dto.getCountry());
                commands.add(command);
            }
        }

        if (dto.isSaveSuburb())  {
            String value = selection.get(TagDialog.TAG_ADDR_SUBURB);
            if (value == null || (value != null && !value.equals(dto.getSuburb()))) {
                ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_SUBURB, dto.getSuburb());
                commands.add(command);
            }
        }

        if (dto.isSaveHousenumber())  {
            String value = selection.get(TagDialog.TAG_ADDR_HOUSENUMBER);
            if (value == null || (value != null && !value.equals(dto.getHousenumber()))) {
                ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_HOUSENUMBER, dto.getHousenumber());
                commands.add(command);
            }
        }

        if (dto.isSavePostcode()) {
            String value = selection.get(TagDialog.TAG_ADDR_POSTCODE);
            if (value == null || (value != null && !value.equals(dto.getPostcode()))) {
                ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_POSTCODE, dto.getPostcode());
                commands.add(command);
            }
        }

        if (dto.isSaveStreet()) {
            if (dto.isTagStreet()) {
                String value = selection.get(TagDialog.TAG_ADDR_STREET);
                if (value == null || (value != null && !value.equals(dto.getStreet()))) {
                    ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_STREET, dto.getStreet());
                    commands.add(command);

                    // remove old place-tag
                    if (selection.get(TagDialog.TAG_ADDR_PLACE) != null) {
                        ChangePropertyCommand command2 = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_PLACE, null);
                        commands.add(command2);
                    }
                }
            } else {
                String value = selection.get(TagDialog.TAG_ADDR_PLACE);
                if (value == null || (value != null && !value.equals(dto.getStreet()))) {
                    ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_PLACE, dto.getStreet());
                    commands.add(command);

                    // remove old place-tag
                    if (selection.get(TagDialog.TAG_ADDR_STREET) != null) {
                        ChangePropertyCommand command2 = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_STREET, null);
                        commands.add(command2);
                    }
                }
            }
        }

        if (dto.isSaveState()) {
            String value = selection.get(TagDialog.TAG_ADDR_STATE);
            if (value == null || (value != null && !value.equals(dto.getState())))  {
                ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_STATE, dto.getState());
                commands.add(command);
            }
        }

        if (!commands.isEmpty()) {
            SequenceCommand sequenceCommand = new SequenceCommand(
                 trn("Updating properties of up to {0} object", 
                     "Updating properties of up to {0} objects", commands.size(), commands.size()), commands);

            // executes the commands and adds them to the undo/redo chains
            UndoRedoHandler.getInstance().add(sequenceCommand);
        }
    }

    private Collection<String> getPossibleStreets() {
        /**
         * Generates a list of all visible names of highways in order to do autocompletion on the road name.
         */
        Set<String> names = new TreeSet<>();
        for (OsmPrimitive osm : MainApplication.getLayerManager().getEditDataSet().allNonDeletedPrimitives()) {
            if (osm.getKeys() != null && osm.keySet().contains("highway") && osm.keySet().contains("name")) {
                names.add(osm.get("name"));
            }
        }
        return names;
    }

    private Dto loadDto() {
        Dto dto = new Dto();
        File fileName = new File(pluginDir + TagDialog.TEMPLATE_DATA);

        try {
            if (fileName.exists()) {
                try (
                        FileInputStream file = new FileInputStream(fileName);
                        ObjectInputStream o = new ObjectInputStream(file);
                ) {
                    dto = (Dto) o.readObject();
                }
            } else {
                loadExistingValuesToDto(dto);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            fileName.delete();
            loadExistingValuesToDto(dto);
        }
        return dto;
    }

    private void loadExistingValuesToDto(Dto dto) {
        dto.setCity(selection.get(TagDialog.TAG_ADDR_CITY));
        dto.setCountry(selection.get(TagDialog.TAG_ADDR_COUNTRY));
        dto.setSource(selection.get(TagDialog.TAG_SOURCE));
        dto.setHousenumber(selection.get(TagDialog.TAG_ADDR_HOUSENUMBER));
        dto.setPostcode(selection.get(TagDialog.TAG_ADDR_POSTCODE));
        dto.setStreet(selection.get(TagDialog.TAG_ADDR_STREET));
        dto.setState(selection.get(TagDialog.TAG_ADDR_STATE));
        dto.setSuburb(selection.get(TagDialog.TAG_ADDR_SUBURB));
    }

    class RadioChangeListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (streetRadio.isSelected()) {
                street.setPossibleItems(getPossibleStreets());
            } else {
                street.setPossibleAcItems(acm.getTagValues(TAG_ADDR_PLACE));
            }
        }
    }
}
