// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.preferences.BooleanProperty;
import org.openstreetmap.josm.data.preferences.IntegerProperty;
import org.openstreetmap.josm.data.tagging.ac.AutoCompletionItem;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.mappaint.MapPaintStyles;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.gui.widgets.PopupMenuLauncher;
import org.openstreetmap.josm.io.XmlWriter;
import org.openstreetmap.josm.plugins.osmrec.container.OSMWay;
import org.openstreetmap.josm.plugins.osmrec.core.TrainWorker;
import org.openstreetmap.josm.plugins.osmrec.extractor.LanguageDetector;
import org.openstreetmap.josm.plugins.osmrec.extractor.SampleModelsExtractor;
import org.openstreetmap.josm.plugins.osmrec.features.ClassFeatures;
import org.openstreetmap.josm.plugins.osmrec.features.GeometryFeatures;
import org.openstreetmap.josm.plugins.osmrec.features.OSMClassification;
import org.openstreetmap.josm.plugins.osmrec.features.TextualFeatures;
import org.openstreetmap.josm.plugins.osmrec.parsers.Mapper;
import org.openstreetmap.josm.plugins.osmrec.parsers.OSMParser;
import org.openstreetmap.josm.plugins.osmrec.parsers.Ontology;
import org.openstreetmap.josm.plugins.osmrec.parsers.TextualStatistics;
import org.openstreetmap.josm.plugins.osmrec.personalization.UserDataExtractAndTrainWorker;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.Utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;

/**
 * Modified PropertiesDialog(since 5633) to serve the plugin functionality.
 *
 * @author imis-nkarag
 */
class OSMRecPluginHelper {

    private final DefaultTableModel tagData;
    private static Collection<String> fileHistory;
    private Map<File, Double> filesAndWeights = new HashMap<>();
    private boolean useCombinedModel;

    //the most recent file from history.
    //all necessary files will reside in a directory of this file
    private static String MAIN_PATH;
    private static String MODEL_PATH;
    private static String TEXTUAL_LIST_PATH;
    private static Map<String, List<String>> indirectClasses;
    private static Map<String, Integer> indirectClassesWithIDs;
    private static LanguageDetector languageDetector;
    private static String bestModelPath;
    private boolean modelWithClasses;
    private final String modelWithClassesPath;
    private boolean useCustomSVMModel = false;
    private String customSVMModelPath;
    private final String combinedModelClasses;

    // Selection that we are editing by using both dialogs
    Collection<OsmPrimitive> sel;

    private String changedKey;

    Comparator<AutoCompletionItem> defaultACItemComparator = new Comparator<AutoCompletionItem>() {
        @Override
        public int compare(AutoCompletionItem o1, AutoCompletionItem o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.getValue(), o2.getValue());
        }
    };

    private String lastAddKey = null;
    private String lastAddValue = null;

    public static final int DEFAULT_LRU_TAGS_NUMBER = 5;
    public static final int MAX_LRU_TAGS_NUMBER = 30;

    // LRU cache for recently added tags (http://java-planet.blogspot.com/2005/08/how-to-set-up-simple-lru-cache-using.html)
    private final Map<Tag, Void> recentTags = new LinkedHashMap<Tag, Void>(MAX_LRU_TAGS_NUMBER+1, 1.1f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Tag, Void> eldest) {
            return size() > MAX_LRU_TAGS_NUMBER;
        }
    };

    OSMRecPluginHelper(DefaultTableModel propertyData, Map<String, Map<String, Integer>> valueCount) {
        this.tagData = propertyData;
        fileHistory = Config.getPref().getList("file-open.history");
        if (!fileHistory.isEmpty()) {
            MAIN_PATH = (String) fileHistory.toArray()[0];
        } else {
            MAIN_PATH = System.getProperty("user.home");
        }
        MODEL_PATH = new File(MAIN_PATH).getParentFile() + "/OSMRec_models";
        TEXTUAL_LIST_PATH = MODEL_PATH + "/textualList.txt";
        combinedModelClasses = MODEL_PATH + "/combinedModel.1";
        bestModelPath = MODEL_PATH + "/best_model";
        customSVMModelPath = bestModelPath;
        modelWithClassesPath = MODEL_PATH + "/model_with_classes";
        languageDetector = LanguageDetector.getInstance(MODEL_PATH + "/profiles");

        SampleModelsExtractor sampleModelsExtractor = new SampleModelsExtractor();

        sampleModelsExtractor.extractSampleSVMmodel("best_model", bestModelPath);
        sampleModelsExtractor.extractSampleSVMmodel("model_with_classes", modelWithClassesPath);
    }

    /**
     * Open the add selection dialog and add a new key/value to the table (and to the dataset, of course).
     */
    public void addTag() {
        changedKey = null;
        sel = OsmDataManager.getInstance().getInProgressSelection();
        if (sel == null || sel.isEmpty()) return;

        final AddTagsDialog addDialog = new AddTagsDialog();

        addDialog.showDialog();

        addDialog.destroyActions();
        if (addDialog.getValue() == 1)
            addDialog.performTagAdding();
        else
            addDialog.undoAllTagsAdding();
    }

    /**
     * Edit the value in the tags table row.
     * @param row The row of the table from which the value is edited.
     * @param focusOnKey Determines if the initial focus should be set on key instead of value
     * @since 5653
     */
    public void editTag(final int row, boolean focusOnKey) {
        changedKey = null;
        sel = OsmDataManager.getInstance().getInProgressSelection();
        String key = "";

        Map<String, Integer> dumPar = new HashMap<>();
        dumPar.put(" ", -1);
        final TrainingDialog editDialog = new TrainingDialog(key, row,
                dumPar, focusOnKey);
        editDialog.showDialog();
    }

    /**
     * If during last editProperty call user changed the key name, this key will be returned
     * Elsewhere, returns null.
     * @return The modified key, or {@code null}
     */
    public String getChangedKey() {
        return changedKey;
    }

    /**
     * For a given key k, return a list of keys which are used as keys for
     * auto-completing values to increase the search space.
     * @param key the key k
     * @return a list of keys
     */
    private static List<String> getAutocompletionKeys(String key) {
        if ("name".equals(key) || "addr:street".equals(key))
            return Arrays.asList("addr:street", "name");
        else
            return Arrays.asList(key);
    }

    /**
     * Load recently used tags from preferences if needed.
     */
    public void loadTagsIfNeeded() {
        if (PROPERTY_REMEMBER_TAGS.get() && recentTags.isEmpty()) {
            recentTags.clear();
            Collection<String> c = Config.getPref().getList("properties.recent-tags");
            Iterator<String> it = c.iterator();
            String key, value;
            while (it.hasNext()) {
                key = it.next();
                value = it.next();
                recentTags.put(new Tag(key, value), null);
            }
        }
    }

    /**
     * Store recently used tags in preferences if needed.
     */
    public void saveTagsIfNeeded() {
        if (PROPERTY_REMEMBER_TAGS.get() && !recentTags.isEmpty()) {
            List<String> c = new ArrayList<>(recentTags.size()*2);
            for (Tag t: recentTags.keySet()) {
                c.add(t.getKey());
                c.add(t.getValue());
            }
            Config.getPref().putList("properties.recent-tags", c);
        }
    }

    /**
     * Warns user about a key being overwritten.
     * @param action The action done by the user. Must state what key is changed
     * @param togglePref  The preference to save the checkbox state to
     * @return {@code true} if the user accepts to overwrite key, {@code false} otherwise
     */
    private boolean warnOverwriteKey(String action, String togglePref) {
        ExtendedDialog ed = new ExtendedDialog(
                MainApplication.getMainFrame(),
                tr("Overwrite key"),
                new String[]{tr("Replace"), tr("Cancel")});
        ed.setButtonIcons(new String[]{"purge", "cancel"});
        ed.setContent(action+"\n"+ tr("The new key is already used, overwrite values?"));
        ed.setCancelButton(2);
        ed.toggleEnable(togglePref);
        ed.showDialog();

        return ed.getValue() == 1;
    }

    public final class TrainingDialog extends AbstractTagsDialog {

        private static final int FIELD_COLUMNS = 4;
        private final JTextField inputFileField;
        private final JLabel inputFileLabel;
        private final JTextField topKField;
        private final JTextField cParameterField;
        private final JTextField frequencyField;

        private final JButton fileBrowseButton;
        private final JButton acceptConfigButton;
        private JRadioButton frequencyButton;
        private JRadioButton topKButton;
        private JCheckBox cParameterCheckBox;
        private final JButton resetConfigButton;
        private String inputFileValue;
        private Double cParameterValue = 0.0;
        private Integer topKvalue = 0;
        private Integer frequencyValue = 0;
        private boolean crossValidateFlag;
        private final JButton startTrainingButton;
        private final JLabel cErrorMessageLabel;
        private final JLabel topKErrorMessageLabel;
        private final JLabel inputFileErrorMessageLabel;
        private final JLabel frequencyErrorMessageLabel;
        private final JProgressBar trainingProgressBar;
        private final JRadioButton byAreaRadioButton;
        private final JRadioButton byTimeRadioButton;
        private final JLabel userNameLabel;
        private final JTextField userNameField;
        private final JTextField daysField;
        private final JLabel daysLabel;
        private final JCheckBox trainFromUserCheckBox;
        private final JPanel userHistoryPanel;
        private Integer daysValue;
        private String usernameValue;
        private TrainWorker trainWorker;
        private UserDataExtractAndTrainWorker userDataExtractAndTrainWorker;

        private TrainingDialog(String key, int row, Map<String, Integer> map, final boolean initialFocusOnKey) {
            super(MainApplication.getMainFrame(), tr("Training process configuration"), new String[] {tr("Cancel")});

            setButtonIcons(new String[] {"ok", "cancel"});
            setCancelButton(2);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));   //6,6
            JPanel configPanel = new JPanel(new BorderLayout(10, 10));  //6,6    //at NORTH of mainPanel
            JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));    //NORTH at config panel
            JPanel paramPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));    //WEST at config panel //config panel has EAST free

            JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));    //SOUTH at config panel
            userHistoryPanel = new JPanel(); //CENTER of config

            trainingProgressBar = new JProgressBar(0, 100);

            ButtonGroup paramGroup = new ButtonGroup();
            ButtonGroup userGroup = new ButtonGroup();
            inputFileLabel = new JLabel();
            inputFileField = new JTextField();
            cParameterField = new JTextField();
            topKField = new JTextField();
            frequencyField = new JTextField();

            cParameterCheckBox = new JCheckBox("Define C parameter");
            topKButton = new JRadioButton("Top-K terms");
            frequencyButton = new JRadioButton("Max-Frequency");
            fileBrowseButton = new JButton();
            acceptConfigButton = new JButton("Accept Configuration");
            resetConfigButton = new JButton("Reset Configuration/Cancel training");
            startTrainingButton = new JButton("Train Model");

            inputFileErrorMessageLabel = new JLabel("");
            cErrorMessageLabel = new JLabel("");
            topKErrorMessageLabel = new JLabel("");
            frequencyErrorMessageLabel = new JLabel("");

            trainFromUserCheckBox = new JCheckBox("Train Model From User");
            byAreaRadioButton = new JRadioButton("By Area");
            byTimeRadioButton = new JRadioButton("By Time");
            userNameLabel = new JLabel("Username:");
            userNameField = new JTextField();

            daysLabel = new JLabel("Days: ");
            daysField = new JTextField();

            cParameterCheckBox.setSelected(true);
            userHistoryPanel.setEnabled(false);
            byAreaRadioButton.setEnabled(false);
            byAreaRadioButton.setSelected(true);
            byTimeRadioButton.setEnabled(false);
            userNameLabel.setEnabled(false);
            userNameField.setEnabled(false);
            daysLabel.setEnabled(false);
            daysField.setEnabled(false);
            userNameField.setColumns(FIELD_COLUMNS);
            daysField.setColumns(FIELD_COLUMNS);

            Collection<String> fileHistory = Config.getPref().getList("file-open.history");
            if (!fileHistory.isEmpty()) {
                inputFileField.setText(MAIN_PATH);
            }

            fileBrowseButton.setText("...");
            inputFileLabel.setText("OSM filepath: ");
            inputFileErrorMessageLabel.setForeground(Color.RED);
            inputFileErrorMessageLabel.setText("");
            topKField.setText("50");
            frequencyField.setText("200");
            cParameterField.setText("0.01");

            cParameterField.setColumns(FIELD_COLUMNS);
            cParameterField.setEditable(false);
            cParameterField.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            cErrorMessageLabel.setForeground(Color.RED);
            cErrorMessageLabel.setMinimumSize(new Dimension(150, 10));

            topKButton.setSelected(true);
            topKField.setColumns(FIELD_COLUMNS);
            topKField.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            topKErrorMessageLabel.setForeground(Color.RED);

            frequencyField.setEditable(false);
            frequencyField.setColumns(FIELD_COLUMNS);
            frequencyField.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            frequencyErrorMessageLabel.setForeground(Color.RED);

            fileBrowseButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    inputFileChooserButtonActionPerformed(evt);
                }
            });
            topKButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (topKButton.isSelected()) {
                        frequencyField.setEditable(false);
                        topKField.setEditable(true);
                    } else {
                        frequencyField.setEditable(true);
                        topKField.setEditable(false);
                    }
                }
            });
            frequencyButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (frequencyButton.isSelected()) {
                        topKField.setEditable(false);
                        frequencyField.setEditable(true);
                    } else {
                        topKField.setEditable(true);
                        frequencyField.setEditable(false);
                    }
                }
            });
            cParameterCheckBox.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (cParameterCheckBox.isSelected()) {
                        cParameterField.setEditable(true);
                    } else {
                        cParameterField.setEditable(false);
                    }
                }
            });
            acceptConfigButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    acceptConfigButtonActionPerformed(evt);
                }
            });
            resetConfigButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    resetConfigButtonActionPerformed();
                }
            });

            startTrainingButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (!acceptConfigButton.isEnabled()) {
                        startTraining();
                    } else {
                        System.out.println("Set values first!");
                    }
                }
            });

            trainFromUserCheckBox.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (trainFromUserCheckBox.isSelected()) {
                        userHistoryPanel.setEnabled(true);
                        byAreaRadioButton.setEnabled(true);
                        byTimeRadioButton.setEnabled(true);
                        userNameLabel.setEnabled(true);
                        userNameField.setEnabled(true);
                        daysLabel.setEnabled(true);
                        daysField.setEnabled(true);
                    } else {
                        userHistoryPanel.setEnabled(false);
                        byAreaRadioButton.setEnabled(false);
                        byTimeRadioButton.setEnabled(false);
                        userNameLabel.setEnabled(false);
                        userNameField.setEnabled(false);
                        daysLabel.setEnabled(false);
                        daysField.setEnabled(false);
                    }
                }
            });

            byAreaRadioButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (byAreaRadioButton.isSelected()) {
                        daysField.setEditable(false);
                    } else {
                        daysField.setEditable(true);
                    }
                }
            });

            byTimeRadioButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (byTimeRadioButton.isSelected()) {
                        daysField.setEditable(true);
                    } else {
                        daysField.setEditable(false);
                    }
                }
            });

            //grouplayout for input panel
            buildInputPanelGroupLayout(inputPanel);

            //grouplayout for param panel
            buildParamPanelGroupLayout(paramPanel);

            inputPanel.add(inputFileLabel);
            inputPanel.add(inputFileField);
            inputPanel.add(fileBrowseButton);
            inputPanel.add(inputFileErrorMessageLabel);

            paramGroup.add(topKButton);
            paramGroup.add(frequencyButton);

            paramPanel.add(cParameterCheckBox);
            paramPanel.add(cParameterField);
            paramPanel.add(cErrorMessageLabel);
            paramPanel.add(topKButton);
            paramPanel.add(topKField);
            paramPanel.add(topKErrorMessageLabel);
            paramPanel.add(frequencyButton);
            paramPanel.add(frequencyField);
            paramPanel.add(frequencyErrorMessageLabel);

            southPanel.add(acceptConfigButton);
            southPanel.add(resetConfigButton);
            southPanel.add(trainFromUserCheckBox);

            userGroup.add(byAreaRadioButton);
            userGroup.add(byTimeRadioButton);
            userHistoryPanel.add(byAreaRadioButton);
            userHistoryPanel.add(byTimeRadioButton);
            userHistoryPanel.add(daysLabel);
            userHistoryPanel.add(daysField);
            userHistoryPanel.add(userNameLabel);
            userHistoryPanel.add(userNameField);

            //grouplayout for user history panel
            /*
                userNameLabel       userField
                arearadiobutton
                timeradiobutton     daysLabel   daysField
             */
            buildUserHistoryPanelGroupLayout();

            configPanel.add(inputPanel, BorderLayout.NORTH);
            configPanel.add(userHistoryPanel, BorderLayout.EAST);
            configPanel.add(paramPanel, BorderLayout.WEST);
            configPanel.add(southPanel, BorderLayout.SOUTH);

            userHistoryPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                    .createEtchedBorder(EtchedBorder.LOWERED, Color.GRAY, Color.WHITE), "Train by user History"));
            paramPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                    .createEtchedBorder(EtchedBorder.LOWERED, Color.GRAY, Color.WHITE), "SVM Configuration"));
            inputPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.GRAY, Color.WHITE));
            configPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.GRAY, Color.WHITE));

            mainPanel.add(configPanel, BorderLayout.NORTH);
            mainPanel.add(startTrainingButton, BorderLayout.CENTER);
            mainPanel.add(trainingProgressBar, BorderLayout.SOUTH);

            AutoCompletionManager autocomplete = AutoCompletionManager.of(MainApplication.getLayerManager().getEditLayer().data);
            List<AutoCompletionItem> keyList = new ArrayList<>(autocomplete.getTagKeys());
            Collections.sort(keyList, defaultACItemComparator);

            setContent(mainPanel, false);
        }

        private void buildInputPanelGroupLayout(JPanel inputPanel) {
            GroupLayout inputGroupLayout = new GroupLayout(inputPanel);
            inputPanel.setLayout(inputGroupLayout);
            inputGroupLayout.setAutoCreateGaps(true);
            inputGroupLayout.setAutoCreateContainerGaps(true);

            GroupLayout.SequentialGroup inputHorGroup = inputGroupLayout.createSequentialGroup();
            inputHorGroup.addGroup(inputGroupLayout.createParallelGroup().addComponent(inputFileLabel).
                    addComponent(inputFileErrorMessageLabel));
            inputHorGroup.addGroup(inputGroupLayout.createParallelGroup().addComponent(inputFileField));
            inputHorGroup.addGroup(inputGroupLayout.createParallelGroup().addComponent(fileBrowseButton));
            inputGroupLayout.setHorizontalGroup(inputHorGroup);

            GroupLayout.SequentialGroup inputVerGroup = inputGroupLayout.createSequentialGroup();
            inputVerGroup.addGroup(inputGroupLayout.createParallelGroup(Alignment.LEADING).addComponent(inputFileLabel).
                    addComponent(inputFileField).addComponent(fileBrowseButton));
            inputVerGroup.addGroup(inputGroupLayout.createParallelGroup(Alignment.LEADING).
                    addComponent(inputFileErrorMessageLabel));
            inputGroupLayout.setVerticalGroup(inputVerGroup);
        }

        private void buildParamPanelGroupLayout(JPanel paramPanel) {
            GroupLayout paramGroupLayout = new GroupLayout(paramPanel);
            paramPanel.setLayout(paramGroupLayout);
            paramGroupLayout.setAutoCreateGaps(true);
            paramGroupLayout.setAutoCreateContainerGaps(true);

            GroupLayout.SequentialGroup paramHorGroup = paramGroupLayout.createSequentialGroup();
            paramHorGroup.addGroup(paramGroupLayout.createParallelGroup().addComponent(topKButton).
                    addComponent(frequencyButton).addComponent(cParameterCheckBox));
            paramHorGroup.addGroup(paramGroupLayout.createParallelGroup().addComponent(cParameterField).
                    addComponent(topKField).addComponent(frequencyField));
            paramHorGroup.addGroup(paramGroupLayout.createParallelGroup().addComponent(cErrorMessageLabel).
                    addComponent(topKErrorMessageLabel).addComponent(frequencyErrorMessageLabel));
            paramGroupLayout.setHorizontalGroup(paramHorGroup);

            GroupLayout.SequentialGroup paramVerGroup = paramGroupLayout.createSequentialGroup();
            paramVerGroup.addGroup(paramGroupLayout.createParallelGroup(Alignment.BASELINE).
                    addComponent(cParameterCheckBox).addComponent(cParameterField).addComponent(cErrorMessageLabel));
            paramVerGroup.addGroup(paramGroupLayout.createParallelGroup(Alignment.BASELINE).addComponent(topKButton).
                    addComponent(topKField).addComponent(topKErrorMessageLabel));
            paramVerGroup.addGroup(paramGroupLayout.createParallelGroup(Alignment.BASELINE).
                    addComponent(frequencyButton).addComponent(frequencyField).addComponent(frequencyErrorMessageLabel));
            paramGroupLayout.setVerticalGroup(paramVerGroup);
        }

        private void buildUserHistoryPanelGroupLayout() {
            GroupLayout userHistoryGroupLayout = new GroupLayout(userHistoryPanel);
            userHistoryPanel.setLayout(userHistoryGroupLayout);
            userHistoryGroupLayout.setAutoCreateGaps(true);
            userHistoryGroupLayout.setAutoCreateContainerGaps(true);
            userHistoryGroupLayout.linkSize(SwingConstants.HORIZONTAL, userNameField, daysLabel, daysField);

            GroupLayout.SequentialGroup userHistoryHorGroup = userHistoryGroupLayout.createSequentialGroup();

            userHistoryHorGroup.addGroup(userHistoryGroupLayout.createParallelGroup().addComponent(userNameLabel)
                    .addComponent(byAreaRadioButton).addComponent(byTimeRadioButton));
            userHistoryHorGroup.addGroup(userHistoryGroupLayout.createParallelGroup().addComponent(userNameField)
                    .addComponent(daysLabel));
            userHistoryHorGroup.addGroup(userHistoryGroupLayout.createParallelGroup().addComponent(daysField));
            userHistoryGroupLayout.setHorizontalGroup(userHistoryHorGroup);

            GroupLayout.SequentialGroup userHistoryVerGroup = userHistoryGroupLayout.createSequentialGroup();
            userHistoryVerGroup.addGroup(userHistoryGroupLayout.createParallelGroup(Alignment.BASELINE).
                    addComponent(userNameLabel).addComponent(userNameField));
            userHistoryVerGroup.addGroup(userHistoryGroupLayout.createParallelGroup(Alignment.BASELINE).
                    addComponent(byAreaRadioButton));
            userHistoryVerGroup.addGroup(userHistoryGroupLayout.createParallelGroup(Alignment.BASELINE).
                    addComponent(byTimeRadioButton).addComponent(daysLabel).addComponent(daysField));
            userHistoryGroupLayout.setVerticalGroup(userHistoryVerGroup);
        }

        private void inputFileChooserButtonActionPerformed(ActionEvent evt) {
            try {
                final File file = new File(inputFileField.getText());
                final JFileChooser fileChooser = new JFileChooser(file);

                final int returnVal = fileChooser.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    inputFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            } catch (RuntimeException ex) {
                Logging.warn(ex);
            }
        }

        private void acceptConfigButtonActionPerformed(ActionEvent evt) {
            //parse values
            inputFileValue = inputFileField.getText();

            if (!new File(inputFileValue).exists()) {
                inputFileErrorMessageLabel.setText("OSM file does not exist");
                resetConfigButtonActionPerformed();
                return;
            }

            if (cParameterCheckBox.isSelected()) {
                String c = cParameterField.getText();
                try {
                    cParameterValue = Double.parseDouble(c.replace(",", "."));
                    cErrorMessageLabel.setText("");
                } catch (NumberFormatException ex) {
                    cErrorMessageLabel.setText("Must be a number!");
                    System.out.println("c must be a number!" + ex); //make empty textLabel beside c param to notify errors
                    resetConfigButtonActionPerformed();
                    return;
                }
                crossValidateFlag = false;
            } else {
                crossValidateFlag = true;
            }

            if (topKButton.isSelected()) {
                String k = topKField.getText();
                try {
                    topKvalue = Integer.parseInt(k);
                    topKErrorMessageLabel.setText("");
                } catch (NumberFormatException ex) {
                    topKErrorMessageLabel.setText("Must be an Integer!");
                    resetConfigButtonActionPerformed();
                    return;
                }
            } else {
                String f = frequencyField.getText();
                try {
                    frequencyValue = Integer.parseInt(f);
                    frequencyErrorMessageLabel.setText("");
                } catch (NumberFormatException ex) {
                    frequencyErrorMessageLabel.setText("Must be an Integer!");
                    resetConfigButtonActionPerformed();
                    return;
                }
            }

            if (trainFromUserCheckBox.isSelected()) {
                usernameValue = userNameField.getText();
                if (byTimeRadioButton.isSelected()) {
                    try {
                        daysValue = Integer.parseInt(daysField.getText());
                    } catch (NumberFormatException ex) {
                        daysField.setText("Integer!");
                        Logging.warn(ex);
                    }
                }

                userHistoryPanel.setEnabled(false);
                byAreaRadioButton.setEnabled(false);
                byTimeRadioButton.setEnabled(false);
                userNameLabel.setEnabled(false);
                userNameField.setEnabled(false);
                daysLabel.setEnabled(false);
                daysField.setEnabled(false);
            }

            System.out.println("Running configuration:" + "\nC parameter: " + cParameterValue +" \ntopK: " + topKvalue
                    + "\nMax Frequency: " + frequencyValue + "\nCross Validate?: " + crossValidateFlag);

            trainFromUserCheckBox.setEnabled(false);
            inputFileField.setEditable(false);
            cParameterField.setEditable(false);
            topKField.setEditable(false);
            frequencyField.setEditable(false);
            cParameterCheckBox.setEnabled(false);
            topKButton.setEnabled(false);
            frequencyButton.setEnabled(false);
            acceptConfigButton.setEnabled(false);
            fileBrowseButton.setEnabled(false);
        }

        private void resetConfigButtonActionPerformed() {
            if (trainWorker != null) {
                try {
                    trainWorker.cancel(true);
                } catch (CancellationException ex) {
                    startTrainingButton.setEnabled(true);
                    System.out.println(ex);
                }
            }
            if (userDataExtractAndTrainWorker != null) {
                try {
                    userDataExtractAndTrainWorker.cancel(true);
                } catch (CancellationException ex) {
                    startTrainingButton.setEnabled(true);
                    System.out.println(ex);
                }
            }
            inputFileField.setEditable(true);
            cParameterField.setEditable(true);
            topKField.setEditable(true);
            frequencyField.setEditable(true);
            cParameterCheckBox.setEnabled(true);
            topKButton.setEnabled(true);
            frequencyButton.setEnabled(true);
            acceptConfigButton.setEnabled(true);
            fileBrowseButton.setEnabled(true);
            trainFromUserCheckBox.setEnabled(true);

            if (trainFromUserCheckBox.isSelected()) {
                userHistoryPanel.setEnabled(true);
                byAreaRadioButton.setEnabled(true);
                byTimeRadioButton.setEnabled(true);
                userNameLabel.setEnabled(true);
                userNameField.setEnabled(true);
                daysLabel.setEnabled(true);
                daysField.setEnabled(true);
            }
        }

        private void startTraining() {
            startTrainingButton.setEnabled(false);

            if (trainFromUserCheckBox.isSelected()) { //if user training. train by area or days
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        userDataExtractAndTrainWorker = new UserDataExtractAndTrainWorker(inputFileValue, usernameValue, daysValue,
                                byAreaRadioButton.isSelected(), crossValidateFlag, cParameterValue, topKvalue, frequencyValue,
                                topKButton.isSelected(), languageDetector);

                        userDataExtractAndTrainWorker.addPropertyChangeListener(new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                if ("progress".equals(evt.getPropertyName())) {
                                    int progress = (Integer) evt.getNewValue();
                                    trainingProgressBar.setValue(progress);
                                    if (progress == 100) {
                                        startTrainingButton.setEnabled(true);
                                    }
                                }
                            }
                        });

                        try {
                            System.out.println("executing userDataExtractAndTrainWorker Thread..");
                            userDataExtractAndTrainWorker.execute();
                        } catch (Exception ex) {
                            Logger.getLogger(OSMRecPluginHelper.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            } else {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        trainWorker = new TrainWorker(inputFileValue, crossValidateFlag, cParameterValue, topKvalue, frequencyValue,
                                topKButton.isSelected(), languageDetector);

                        trainWorker.addPropertyChangeListener(new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                if ("progress".equals(evt.getPropertyName())) {
                                    int progress = (Integer) evt.getNewValue();
                                    trainingProgressBar.setValue(progress);
                                    if (progress == 100) {
                                        startTrainingButton.setEnabled(true);
                                    }
                                }
                            }
                        });

                        try {
                            trainWorker.execute();
                        } catch (Exception ex) {
                            Logger.getLogger(OSMRecPluginHelper.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
        }
    }

    public static final BooleanProperty PROPERTY_FIX_TAG_LOCALE =
            new BooleanProperty("properties.fix-tag-combobox-locale", false);
    public static final BooleanProperty PROPERTY_REMEMBER_TAGS =
            new BooleanProperty("properties.remember-recently-added-tags", true);
    public static final IntegerProperty PROPERTY_RECENT_TAGS_NUMBER =
            new IntegerProperty("properties.recently-added-tags", DEFAULT_LRU_TAGS_NUMBER);

    abstract static class AbstractTagsDialog extends ExtendedDialog {
        AutoCompletingComboBox keys;
        AutoCompletingComboBox values;

        AbstractTagsDialog(Component parent, String title, String[] buttonTexts) {
            super(parent, title, buttonTexts);
            addMouseListener(new PopupMenuLauncher(popupMenu));
        }

        @Override
        public void setupDialog() {
            super.setupDialog();
            final Dimension size = getSize();
            // Set resizable only in width
            setMinimumSize(size);
            setPreferredSize(size);
            // setMaximumSize does not work, and never worked, but still it seems not to bother Oracle to fix this 10-year-old bug
            // https://bugs.openjdk.java.net/browse/JDK-6200438
            // https://bugs.openjdk.java.net/browse/JDK-6464548

            setRememberWindowGeometry(getClass().getName() + ".geometry",
                    WindowGeometry.centerInWindow(MainApplication.getMainFrame(), size));
        }

        @Override
        public void setVisible(boolean visible) {
            // Do not want dialog to be resizable in height, as its size may increase each time because of the recently added tags
            // So need to modify the stored geometry (size part only) in order to use the automatic positioning mechanism
            if (visible) {
                WindowGeometry geometry = initWindowGeometry();
                Dimension storedSize = geometry.getSize();
                Dimension size = getSize();
                if (!storedSize.equals(size)) {
                    if (storedSize.width < size.width) {
                        storedSize.width = size.width;
                    }
                    if (storedSize.height != size.height) {
                        storedSize.height = size.height;
                    }
                    rememberWindowGeometry(geometry);
                }
                if (keys != null) {
                    keys.setFixedLocale(PROPERTY_FIX_TAG_LOCALE.get());
                }
            }
            super.setVisible(visible);
        }

        /**
         * Create a focus handling adapter and apply in to the editor component of value
         * autocompletion box.
         * @param autocomplete Manager handling the autocompletion
         * @param comparator Class to decide what values are offered on autocompletion
         * @return The created adapter
         */
        protected FocusAdapter addFocusAdapter(final AutoCompletionManager autocomplete, final Comparator<AutoCompletionItem> comparator) {
            // get the combo box' editor component
            JTextComponent editor = (JTextComponent) values.getEditor().getEditorComponent();
            // Refresh the values model when focus is gained
            FocusAdapter focus = new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    String key = keys.getEditor().getItem().toString();

                    List<AutoCompletionItem> valueList = new ArrayList<>(autocomplete.getTagValues(getAutocompletionKeys(key)));
                    Collections.sort(valueList, comparator);

                    values.setPossibleAcItems(valueList);
                    values.getEditor().selectAll();
                }
            };
            editor.addFocusListener(focus);
            return focus;
        }

        protected JPopupMenu popupMenu = new JPopupMenu() {
            JCheckBoxMenuItem fixTagLanguageCb = new JCheckBoxMenuItem(
                    new AbstractAction(tr("Use English language for tag by default")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            boolean sel = ((JCheckBoxMenuItem) e.getSource()).getState();
                            PROPERTY_FIX_TAG_LOCALE.put(sel);
                        }
                    });
            {
                add(fixTagLanguageCb);
                fixTagLanguageCb.setState(PROPERTY_FIX_TAG_LOCALE.get());
            }
        };
    }

    class ModelSettingsDialog extends JPanel {

        private final JLabel chooseModelLabel;
        private final JButton chooseModelButton;
        private final JTextField chooseModelTextField;

        private final DefaultListModel<String> combinationDefaultListModel = new DefaultListModel<>();
        private final JList<String> modelCombinationList = new JList<>(combinationDefaultListModel);
        private final JPanel modelCombinationPanel;
        private final JPanel weightsPanel;
        private final JCheckBox useModelCombinationCheckbox;
        private final JButton acceptWeightsButton;
        private final JButton resetWeightsButton;
        private final JButton removeSelectedModelButton;
        private Map<JTextField, String> weightFieldsAndPaths = new HashMap<>();
        private final Map<String, Double> normalizedPathsAndWeights = new HashMap<>();
        private final JOptionPane pane;
        private final JDialog dlg;
        private final JPanel mainPanel;
        private final JPanel singleSelectionPanel;
        private final JPanel setResetWeightsPanel;
        private final JScrollPane combinationScrollPane;
        private final JScrollPane singleSelectionScrollPane;
        private final TitledBorder modelTitle;
        private final TitledBorder weightTitle;
        private final TitledBorder combineTitle;
        private final Dimension singleSelectionDimension;
        private final Dimension modelCombinationDimension;
        private final Dimension mainPanelDimension;

        ModelSettingsDialog(Collection<OsmPrimitive> sel1, final AddTagsDialog addDialog) {

            loadPreviousCombinedSVMModel();
            singleSelectionDimension = new Dimension(470, 70);
            modelCombinationDimension = new Dimension(450, 250);
            mainPanelDimension = new Dimension(600, 350);

            //------- <NORTH of main> ---------//
            mainPanel = new JPanel(new BorderLayout(10, 10));
            singleSelectionPanel = new JPanel(new BorderLayout(10, 10));
            setResetWeightsPanel = new JPanel();

            chooseModelLabel = new JLabel("Choose a Model:");
            chooseModelTextField = new JTextField();
            chooseModelButton = new JButton("...");
            chooseModelTextField.setText(MODEL_PATH);

            singleSelectionPanel.add(chooseModelLabel, BorderLayout.NORTH);
            singleSelectionPanel.add(chooseModelTextField, BorderLayout.WEST);
            singleSelectionPanel.add(chooseModelButton, BorderLayout.EAST);

            singleSelectionScrollPane = new JScrollPane(singleSelectionPanel);
            singleSelectionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            singleSelectionScrollPane.setPreferredSize(singleSelectionDimension);

            //------- </NORTH of main> ---------//

            //------- <WEST of main> ---------//
            modelCombinationList.setFixedCellHeight(20);
            modelCombinationList.setEnabled(false);
            modelCombinationPanel = new JPanel(new BorderLayout(10, 10));

            weightsPanel = new JPanel();
            weightsPanel.setLayout(new BoxLayout(weightsPanel, BoxLayout.Y_AXIS));
            weightsPanel.setEnabled(false);


            acceptWeightsButton = new JButton("Set Weights/Normalize");
            resetWeightsButton = new JButton("Reset Weights");
            removeSelectedModelButton = new JButton("Remove Selected");
            setResetWeightsPanel.add(acceptWeightsButton);
            setResetWeightsPanel.add(resetWeightsButton);
            setResetWeightsPanel.add(removeSelectedModelButton);
            removeSelectedModelButton.setEnabled(false);
            acceptWeightsButton.setEnabled(false);
            resetWeightsButton.setEnabled(false);

            modelCombinationPanel.add(modelCombinationList, BorderLayout.CENTER);
            modelCombinationPanel.add(weightsPanel, BorderLayout.EAST);
            modelCombinationPanel.add(setResetWeightsPanel, BorderLayout.SOUTH);

            combinationScrollPane = new JScrollPane(modelCombinationPanel);

            combinationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            combinationScrollPane.setPreferredSize(modelCombinationDimension);   //new Dimension(450, 250) // w/h

            //------- </WEST of main> ---------//

            //------- <SOUTH of main> ---------//
            useModelCombinationCheckbox = new JCheckBox("Combine different models?");

            //------- </SOUTH of main> ---------//

            //------- <Borders> ---------//
            modelTitle = BorderFactory.createTitledBorder("Models");
            weightTitle = BorderFactory.createTitledBorder("W");
            combineTitle = BorderFactory.createTitledBorder("Combine Models");
            modelCombinationList.setBorder(modelTitle);
            weightsPanel.setBorder(weightTitle);

            for (Entry<JTextField, String> entry : weightFieldsAndPaths.entrySet()) {
                combinationDefaultListModel.addElement(entry.getValue());

                JTextField weightTextField = new JTextField("0.00");
                weightTextField.setMaximumSize(new Dimension(80, 20));
                weightsPanel.add(entry.getKey());
            }

            //modelCombinationPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.GRAY, Color.WHITE));
            modelCombinationPanel.setBorder(combineTitle);
            singleSelectionPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.GRAY, Color.WHITE));
            //------- </Borders> ---------//

            chooseModelButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    modelChooserButtonActionPerformed(evt);
                }
            });

            useModelCombinationCheckbox.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    userCombinationCheckboxActionPerformed(evt);
                }
            });

            acceptWeightsButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    acceptWeightsButtonActionPerformed(evt);
                }
            });

            resetWeightsButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    resetWeightsButtonActionPerformed(evt);
                }
            });

            removeSelectedModelButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    removeSelectedModelButtonActionPerformed(evt);
                }
            });
            mainPanel.add(singleSelectionScrollPane, BorderLayout.NORTH);
            mainPanel.add(combinationScrollPane, BorderLayout.CENTER);
            mainPanel.add(useModelCombinationCheckbox, BorderLayout.SOUTH);

            mainPanel.setPreferredSize(mainPanelDimension);

            this.add(mainPanel);

            pane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
                @Override
                public void setValue(Object newValue) {
                    super.setValue(newValue);
                    if (newValue instanceof Integer && (int) newValue == 0 && useModelCombinationCheckbox.isSelected()) {
                        System.out.println("model settings button value: " + newValue);
                        System.out.println("\nUSE COMBINED MODEL\n");
                        useCombinedModel = true;
                        useCustomSVMModel = false;

                        addDialog.loadSVMmodel();
                        addDialog.createOSMObject(sel);
                        saveCombinedModel();
                        dlg.setVisible(false);
                    } else if (newValue instanceof Integer && (int) newValue == -1 && useModelCombinationCheckbox.isSelected()) {
                        System.out.println("model settings button value: " + newValue);
                        useCombinedModel = false;
                        useCustomSVMModel = false;
                        System.out.println("Use combined model");

                        addDialog.loadSVMmodel();
                        addDialog.createOSMObject(sel);
                        dlg.setVisible(false);
                    } else if (newValue instanceof Integer && (int) newValue == 0 && !useModelCombinationCheckbox.isSelected()) {
                        System.out.println("model settings button value: " + newValue);
                        System.out.println("Don t use combined model, use custom model");
                        useCombinedModel = false;
                        useCustomSVMModel = true;
                        addDialog.loadSVMmodel();
                        addDialog.createOSMObject(sel);
                        dlg.setVisible(false);
                    } else if (newValue instanceof Integer && (int) newValue == -1 && !useModelCombinationCheckbox.isSelected()) {
                        System.out.println("model settings button value: " + newValue);
                        System.out.println("Don t use combined model, use custom model");
                        useCombinedModel = false;
                        useCustomSVMModel = false;
                        addDialog.loadSVMmodel();
                        addDialog.createOSMObject(sel);
                        dlg.setVisible(false);
                    } else if (newValue == null || newValue.equals("uninitializedValue")) {
                        System.out.println("uninitializedValue, do nothing");
                    }
                }
            };

            dlg = pane.createDialog(MainApplication.getMainFrame(), tr("Model Settings"));
            dlg.setVisible(true);
        }

        public void makeVisible(boolean visible) {
            dlg.setVisible(true);
        }

        private void modelChooserButtonActionPerformed(ActionEvent evt) {

            try {
                final File file = new File(chooseModelTextField.getText());
                final JFileChooser fileChooser = new JFileChooser(file);

                final int returnVal = fileChooser.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    chooseModelTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    useCustomSVMModel = true;
                    customSVMModelPath = fileChooser.getSelectedFile().getAbsolutePath();
                }

                if (useModelCombinationCheckbox.isSelected()) {
                    String svmModelPath = fileChooser.getSelectedFile().getAbsolutePath();
                    String svmModelText;
                    if (System.getProperty("os.name").contains("ux")) {
                        if (svmModelPath.contains("/")) {
                            svmModelText = svmModelPath.substring(svmModelPath.lastIndexOf("/"));
                        } else {
                            svmModelText = svmModelPath;
                        }
                    } else {
                        if (svmModelPath.contains("\\")) {
                            svmModelText = svmModelPath.substring(svmModelPath.lastIndexOf("\\"));
                        } else {
                            svmModelText = svmModelPath;
                        }
                    }
                    combinationDefaultListModel.addElement(svmModelText);
                    JTextField weightTextField = new JTextField("0.00");
                    weightFieldsAndPaths.put(weightTextField, svmModelPath);
                    System.out.println("weights size: " + weightFieldsAndPaths.size());

                    weightTextField.setMaximumSize(new Dimension(80, 20));
                    weightsPanel.add(weightTextField);
                    //add additional textbox
                }
            } catch (RuntimeException ex) {
                Logging.warn(ex);
            }
        }

        private void userCombinationCheckboxActionPerformed(java.awt.event.ActionEvent evt) {

            if (useModelCombinationCheckbox.isSelected()) {
                useCombinedModel = true;
                useCustomSVMModel = false; //reseting the selected custom SVM model only here
                removeSelectedModelButton.setEnabled(true);
                acceptWeightsButton.setEnabled(true);
                resetWeightsButton.setEnabled(true);

                chooseModelTextField.setEnabled(false);
                modelCombinationList.setEnabled(true);
                weightsPanel.setEnabled(true);
                Component[] weightPanelComponents = weightsPanel.getComponents();
                for (Component weightPanelComponent : weightPanelComponents) {
                    weightPanelComponent.setEnabled(true);
                }
            } else {
                useCombinedModel = false;
                useCustomSVMModel = true;
                removeSelectedModelButton.setEnabled(false);
                acceptWeightsButton.setEnabled(false);
                resetWeightsButton.setEnabled(false);

                chooseModelTextField.setEnabled(true);
                modelCombinationList.setEnabled(false);
                weightsPanel.setEnabled(false);
                Component[] weightPanelComponents = weightsPanel.getComponents();
                for (Component weightPanelComponent : weightPanelComponents) {
                    weightPanelComponent.setEnabled(false);
                }
            }
        }

        private void acceptWeightsButtonActionPerformed(ActionEvent evt) {
            int weightsCount = 0;
            removeSelectedModelButton.setEnabled(false);
            double weightSum = 0;
            for (JTextField weightField : weightFieldsAndPaths.keySet()) {
                if (weightField.getText().equals("")) {
                    weightField.setText("0.00");
                }

                try {
                    //TODO replace "," with "." to parse doubles with commas
                    Double weightValue = Double.parseDouble(weightField.getText());

                    weightValue = Math.abs(weightValue);
                    weightSum += weightValue;
                } catch (NumberFormatException ex) {
                    Logging.warn(ex);
                }
                weightsCount++;
            }

            if (!filesAndWeights.isEmpty()) {
                filesAndWeights.clear();
            }

            for (JTextField weightField : weightFieldsAndPaths.keySet()) {
                try {
                    Double weightValue = Double.parseDouble(weightField.getText());

                    weightValue = Math.abs(weightValue)/weightSum; //normalize

                    if (weightSum == 0) {
                        weightValue = 1.0/weightsCount;
                    }

                    weightField.setText(new DecimalFormat("#.##").format(weightValue));
                    normalizedPathsAndWeights.put(weightFieldsAndPaths.get(weightField), weightValue);
                    filesAndWeights.put(new File(weightFieldsAndPaths.get(weightField)), weightValue);
                    System.out.println("normalized: " + weightFieldsAndPaths.get(weightField) + "->" + weightValue);
                    weightField.setEnabled(false);

                } catch (NumberFormatException ex) {
                    Logging.warn(ex);
                }
            }

            useCombinedModel = true;
            useCustomSVMModel = false;
        }

        private void resetWeightsButtonActionPerformed(ActionEvent evt) {
            removeSelectedModelButton.setEnabled(true);
            for (JTextField weightField : weightFieldsAndPaths.keySet()) {
                weightField.setEnabled(true);
            }
        }

        private void removeSelectedModelButtonActionPerformed(ActionEvent evt) {
            int index = modelCombinationList.getSelectedIndex();
            String modelToBeRemoved = combinationDefaultListModel.get(index);
            combinationDefaultListModel.remove(index);
            System.out.println("model to be removed: " + modelToBeRemoved);

            Iterator<Entry<JTextField, String>> it = weightFieldsAndPaths.entrySet().iterator();
            while (it.hasNext()) {
                Entry<JTextField, String> en = it.next();
                if (en.getValue().equals(modelToBeRemoved)) {
                    it.remove();
                }
            }
            System.out.println("model to be removed: " + modelToBeRemoved);

            weightsPanel.remove(index);
            weightsPanel.revalidate();
            weightsPanel.repaint();
        }

        @SuppressWarnings("unchecked")
        private void loadPreviousCombinedSVMModel() {
            File combinedModelClassesFile = new File(combinedModelClasses);

            if (combinedModelClassesFile.exists()) {
                FileInputStream fileIn = null;
                ObjectInputStream in = null;
                try {
                    fileIn = new FileInputStream(combinedModelClassesFile);
                    in = new ObjectInputStream(fileIn);
                    weightFieldsAndPaths = (Map<JTextField, String>) in.readObject();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(OSMRecPluginHelper.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(OSMRecPluginHelper.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                        if (fileIn != null) {
                            fileIn.close();
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(OSMRecPluginHelper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                try {
                    combinedModelClassesFile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(OSMRecPluginHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        private void saveCombinedModel() {
            try (FileOutputStream fileOut = new FileOutputStream(combinedModelClasses);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(weightFieldsAndPaths);
            } catch (IOException e) {
                System.out.println("serialize error" + e);
            }
        }
    }

    class AddTagsDialog extends AbstractTagsDialog {
        List<JosmAction> recentTagsActions = new ArrayList<>();

        // Counter of added commands for possible undo
        private int commandCount;
        private final JLabel recommendedClassesLabel;
        private final JButton modelSettingsButton;
        private final JButton addAndContinueButton;
        private final DefaultListModel<String> model;
        private final JList<String> categoryList;
        private Model modelSVM;
        private int modelSVMLabelSize;
        private int[] modelSVMLabels;
        private Map<String, String> mappings;
        private Map<String, Integer> mapperWithIDs;
        private Map<Integer, String> idsWithMappings;
        private List<String> textualList = new ArrayList<>();
        private final JCheckBox useTagsCheckBox;
        private ModelSettingsDialog modelSettingsDialog;
        private static final int RECOMMENDATIONS_SIZE = 10;

        AddTagsDialog() {
            super(MainApplication.getMainFrame(), tr("Add value?"), new String[] {tr("OK"), tr("Cancel")});
            setButtonIcons(new String[] {"ok", "cancel"});
            setCancelButton(2);
            configureContextsensitiveHelp("/Dialog/AddValue", true /* show help button */);
            final AddTagsDialog addTagsDialog = this;

            loadOntology();
            //if the user did not train a model by running the training process
            //the list does not exist in a file and so we load the default list from the jar.

            System.out.println("path for textual: " + TEXTUAL_LIST_PATH);
            File textualListFile = new File(TEXTUAL_LIST_PATH);
            if (textualListFile.exists()) {
                loadTextualList(textualListFile);
            } else {
                loadDefaultTextualList();
            }

            //if training process has not been performed, we use two sample SVM models, extracted from the jar

            JPanel splitPanel = new JPanel(new BorderLayout(10, 10));
            JPanel mainPanel = new JPanel(new GridBagLayout()); //original panel, will be wrapped by the splitPanel
            JPanel recommendPanel = new JPanel(new BorderLayout(10, 10));   //will contain listPanel, action panel
            JPanel listPanel = new JPanel(new BorderLayout(10, 10)); //class recommend label, recommendation list
            JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); //model selection buttons or configuration

            addAndContinueButton = new JButton("Add and continue");
            modelSettingsButton = new JButton("Model Settings");
            useTagsCheckBox = new JCheckBox("Predict using tags");
            recommendedClassesLabel = new JLabel("Recommended Classes:");

            addAndContinueButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    String selectedClass = categoryList.getSelectedValue();
                    addAndContinueButtonActionPerformed(evt, selectedClass);

                    //reconstruct vector for instance and use the model that was trained with classes here

                    List<OsmPrimitive> osmPrimitiveSelection = new ArrayList<>(sel);
                    OsmPrimitive s;

                    //get a simple selection
                    if (!osmPrimitiveSelection.isEmpty()) {
                        s = osmPrimitiveSelection.get(0);
                        if (s.getInterestingTags().isEmpty()) {
                            //load original model
                            modelWithClasses = false;
                            loadSVMmodel();
                            createOSMObject(sel); //create object without class features
                        } else {
                            //recommend using tags: set the checkbox selected to avoid confusing the user
                            useTagsCheckBox.setSelected(true);

                            if (useTagsCheckBox.isSelected()) {
                                //load model with classes
                                modelWithClasses = true;
                                loadSVMmodel();
                                createOSMObject(sel); //create object including class features
                            } else {
                                modelWithClasses = false;
                                loadSVMmodel();
                                createOSMObject(sel); //create object including class features
                            }
                        }
                    }
                }
            });

            modelSettingsButton.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (modelSettingsDialog == null) {
                        System.out.println("new modelSettingsDialog");
                        modelSettingsDialog = new ModelSettingsDialog(sel, addTagsDialog);
                    } else {
                        System.out.println("set modelSettingsDialog visible");
                        modelSettingsDialog.makeVisible(true);
                    }
                }
            });

            useTagsCheckBox.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    List<OsmPrimitive> osmPrimitiveSelection = new ArrayList<>(sel);
                    OsmPrimitive s;
                    if (!osmPrimitiveSelection.isEmpty()) {
                        s = osmPrimitiveSelection.get(0);
                        if (s.getInterestingTags().isEmpty()) {
                            //load original model
                            modelWithClasses = false;
                            loadSVMmodel();
                            createOSMObject(sel); //create object without class features
                        } else {
                            //useTagsCheckBox
                            if (useTagsCheckBox.isSelected()) {
                                //load model with classes
                                modelWithClasses = true;
                                loadSVMmodel();
                                createOSMObject(sel); //create object including class features
                            } else {
                                modelWithClasses = false;
                                loadSVMmodel();
                                createOSMObject(sel); //create object including class features
                            }
                        }
                    }
                }
            });

            keys = new AutoCompletingComboBox();
            values = new AutoCompletingComboBox();

            mainPanel.add(new JLabel("<html>"+trn("This will change up to {0} object.",
                    "This will change up to {0} objects.", sel.size(), sel.size())
            +"<br><br>"+tr("Please select a key")), GBC.eol().fill(GBC.HORIZONTAL));

            AutoCompletionManager autocomplete = AutoCompletionManager.of(MainApplication.getLayerManager().getEditLayer().data);
            List<AutoCompletionItem> keyList = new ArrayList<>(autocomplete.getTagKeys());

            AutoCompletionItem itemToSelect = null;
            // remove the object's tag keys from the list
            Iterator<AutoCompletionItem> iter = keyList.iterator();
            while (iter.hasNext()) {
                AutoCompletionItem item = iter.next();
                if (item.getValue().equals(lastAddKey)) {
                    itemToSelect = item;
                }
                for (int i = 0; i < tagData.getRowCount(); ++i) {
                    if (item.getValue().equals(tagData.getValueAt(i, 0))) {
                        if (itemToSelect == item) {
                            itemToSelect = null;
                        }
                        iter.remove();
                        break;
                    }
                }
            }

            Collections.sort(keyList, defaultACItemComparator);
            keys.setPossibleAcItems(keyList);
            keys.setEditable(true);

            mainPanel.add(keys, GBC.eop().fill());
            mainPanel.add(new JLabel(tr("Please select a value")), GBC.eol());

            model = new DefaultListModel<>();

            parseTagsMappedToClasses();

            List<OsmPrimitive> osmPrimitiveSelection = new ArrayList<>(sel);
            OsmPrimitive s;
            //get a simple selection
            if (!osmPrimitiveSelection.isEmpty()) {
                s = osmPrimitiveSelection.get(0);
                File modelDirectory = new File(MODEL_PATH);
                String modelWithClassesPath = modelDirectory.getAbsolutePath() + "/model_with_classes";
                File modelWithClassesFile = new File(modelWithClassesPath);
                if (s.getInterestingTags().isEmpty() || !modelWithClassesFile.exists()) {
                    modelWithClasses = false;
                    loadSVMmodel(); //load original model
                    createOSMObject(sel); //create object without class features
                } else {
                    //recommend using tags: set the checkbox selected to avoid confusing the user
                    useTagsCheckBox.setSelected(true);
                    modelWithClasses = true;
                    loadSVMmodel(); //load model with classes
                    createOSMObject(sel); //create object including class features
                }
            }

            categoryList = new JList<>(model);

            ListSelectionListener listSelectionListener = new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent listSelectionEvent) {
                    if (!listSelectionEvent.getValueIsAdjusting()) { //This prevents double events

                        String selectedClass = categoryList.getSelectedValue();

                        if (selectedClass != null) { //null check, because the model is cleared after a new recommendation
                            //tags become unselected
                            if ((selectedClass.indexOf(" ")+1) > 0) {
                                //add both key + value in tags
                                String keyTag = selectedClass.substring(0, selectedClass.indexOf(" "));
                                String valueTag = selectedClass.substring(selectedClass.indexOf(" ")+1, selectedClass.length());
                                keys.setSelectedItem(keyTag); //adding selected tags to textBoxes
                                values.setSelectedItem(valueTag);
                            } else {
                                //value does not have a value, add the key tag only
                                String keyTag = selectedClass; //test it
                                keys.setSelectedItem(keyTag);
                                values.setSelectedItem("");
                            }
                        }
                    }
                }
            };

            categoryList.addListSelectionListener(listSelectionListener);
            categoryList.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.GRAY, Color.WHITE));
            categoryList.setModel(model);

            values.setEditable(true);
            mainPanel.add(values, GBC.eop().fill());
            if (itemToSelect != null) {
                keys.setSelectedItem(itemToSelect);
                if (lastAddValue != null) {
                    values.setSelectedItem(lastAddValue);
                }
            }

            FocusAdapter focus = addFocusAdapter(autocomplete, defaultACItemComparator);
            // fire focus event in advance or otherwise the popup list will be too small at first
            focus.focusGained(null);

            int recentTagsToShow = PROPERTY_RECENT_TAGS_NUMBER.get();
            if (recentTagsToShow > MAX_LRU_TAGS_NUMBER) {
                recentTagsToShow = MAX_LRU_TAGS_NUMBER;
            }

            suggestRecentlyAddedTags(mainPanel, recentTagsToShow, focus);

            mainPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.GRAY, Color.WHITE));
            listPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.GRAY, Color.WHITE));
            splitPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.GRAY, Color.WHITE));

            listPanel.add(recommendedClassesLabel, BorderLayout.NORTH);
            listPanel.add(categoryList, BorderLayout.SOUTH);
            actionsPanel.add(addAndContinueButton);
            actionsPanel.add(modelSettingsButton);
            actionsPanel.add(useTagsCheckBox);

            recommendPanel.add(actionsPanel, BorderLayout.WEST);
            recommendPanel.add(listPanel, BorderLayout.NORTH);

            splitPanel.add(mainPanel, BorderLayout.WEST);
            splitPanel.add(recommendPanel, BorderLayout.EAST);

            setContent(splitPanel, false);

            popupMenu.add(new AbstractAction(tr("Set number of recently added tags")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectNumberOfTags();
                }
            });
            JCheckBoxMenuItem rememberLastTags = new JCheckBoxMenuItem(
                    new AbstractAction(tr("Remember last used tags after a restart")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            boolean sel = ((JCheckBoxMenuItem) e.getSource()).getState();
                            PROPERTY_REMEMBER_TAGS.put(sel);
                            if (sel) saveTagsIfNeeded();
                        }
                    });
            rememberLastTags.setState(PROPERTY_REMEMBER_TAGS.get());
            popupMenu.add(rememberLastTags);
        }

        private void addAndContinueButtonActionPerformed(ActionEvent evt, String selectedClass) {
            performTagAdding();
        }

        private void selectNumberOfTags() {
            String s = JOptionPane.showInputDialog(this, tr("Please enter the number of recently added tags to display"));
            if (s != null) try {
                int v = Integer.parseInt(s);
                if (v >= 0 && v <= MAX_LRU_TAGS_NUMBER) {
                    PROPERTY_RECENT_TAGS_NUMBER.put(v);
                    return;
                }
            } catch (NumberFormatException ex) {
                Logging.warn(ex);
            }
            JOptionPane.showMessageDialog(this, tr("Please enter integer number between 0 and {0}", MAX_LRU_TAGS_NUMBER));
        }

        private void suggestRecentlyAddedTags(JPanel mainPanel, int tagsToShow, final FocusAdapter focus) {
            if (!(tagsToShow > 0 && !recentTags.isEmpty()))
                return;

            mainPanel.add(new JLabel(tr("Recently added tags")), GBC.eol());

            int count = 1;
            // We store the maximum number (9) of recent tags to allow dynamic change of number of tags shown in the preferences.
            // This implies to iterate in descending order,
            // as the oldest elements will only be removed after we reach the maximum numbern and not the number of tags to show.
            // However, as Set does not allow to iterate in descending order,
            // we need to copy its elements into a List we can access in reverse order.
            List<Tag> tags = new LinkedList<>(recentTags.keySet());
            for (int i = tags.size()-1; i >= 0 && count <= tagsToShow; i--, count++) {
                final Tag t = tags.get(i);
                // Create action for reusing the tag, with keyboard shortcut Ctrl+(1-5)
                String scKey = "properties:recent:"+count;
                String scsKey = "properties:recent:shift:"+count;
                Shortcut sc = Shortcut.registerShortcut(scKey, tr("Choose recent tag {0}", count), KeyEvent.VK_0+count, Shortcut.CTRL);
                final JosmAction action = new JosmAction(scKey, null, tr("Use this tag again"), sc, false) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        keys.setSelectedItem(t.getKey());
                        // fix #7951, #8298 - update list of values before setting value (?)
                        focus.focusGained(null);
                        values.setSelectedItem(t.getValue());
                    }
                };
                Shortcut scs = Shortcut.registerShortcut(scsKey, tr("Apply recent tag {0}", count), KeyEvent.VK_0+count, Shortcut.CTRL_SHIFT);
                final JosmAction actionShift = new JosmAction(scsKey, null, tr("Use this tag again"), scs, false) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        action.actionPerformed(null);
                        performTagAdding();
                    }
                };
                recentTagsActions.add(action);
                recentTagsActions.add(actionShift);
                disableTagIfNeeded(t, action);
                // Find and display icon
                ImageIcon icon = MapPaintStyles.getNodeIcon(t, false); // Filters deprecated icon
                if (icon == null) {
                    // If no icon found in map style look at presets
                    Map<String, String> map = new HashMap<>();
                    map.put(t.getKey(), t.getValue());
                    // If still nothing display an empty icon
                    if (icon == null) {
                        icon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
                    }
                }
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.ipadx = 5;
                mainPanel.add(new JLabel(action.isEnabled() ? icon : GuiHelper.getDisabledIcon(icon)), gbc);
                // Create tag label
                final String color = action.isEnabled() ? "" : "; color:gray";
                final JLabel tagLabel = new JLabel("<html>"
                        + "<style>td{border:1px solid gray; font-weight:normal"+color+"}</style>"
                        + "<table><tr><td>" + XmlWriter.encode(t.toString(), true) + "</td></tr></table></html>");
                if (action.isEnabled()) {
                    // Register action
                    mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(sc.getKeyStroke(), scKey);
                    mainPanel.getActionMap().put(scKey, action);
                    mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(scs.getKeyStroke(), scsKey);
                    mainPanel.getActionMap().put(scsKey, actionShift);
                    // Make the tag label clickable and set tooltip to the action description (this displays also the keyboard shortcut)
                    tagLabel.setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
                    tagLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    tagLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            action.actionPerformed(null);
                            // add tags and close window on double-click

                            if (e.getClickCount() > 1) {
                                buttonAction(0, null); // emulate OK click and close the dialog
                            }
                            // add tags on Shift-Click
                            if (e.isShiftDown()) {
                                performTagAdding();
                            }
                        }
                    });
                } else {
                    // Disable tag label
                    tagLabel.setEnabled(false);
                    // Explain in the tooltip why
                    tagLabel.setToolTipText(tr("The key ''{0}'' is already used", t.getKey()));
                }
                // Finally add label to the resulting panel
                JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                tagPanel.add(tagLabel);
                mainPanel.add(tagPanel, GBC.eol().fill(GBC.HORIZONTAL));
            }
        }

        public void destroyActions() {
            for (JosmAction action : recentTagsActions) {
                action.destroy();
            }
        }

        /**
         * Read tags from comboboxes and add it to all selected objects
         */
        public final void performTagAdding() {
            String key = Utils.removeWhiteSpaces(keys.getEditor().getItem().toString());
            String value = Utils.removeWhiteSpaces(values.getEditor().getItem().toString());
            if (key.isEmpty() || value.isEmpty()) return;
            for (OsmPrimitive osm: sel) {
                String val = osm.get(key);
                if (val != null && !val.equals(value)) {
                    if (!warnOverwriteKey(tr("You changed the value of ''{0}'' from ''{1}'' to ''{2}''.", key, val, value),
                            "overwriteAddKey"))
                        return;
                    break;
                }
            }
            lastAddKey = key;
            lastAddValue = value;
            recentTags.put(new Tag(key, value), null);
            AutoCompletionManager.rememberUserInput(key, value, false);
            commandCount++;
            UndoRedoHandler.getInstance().add(new ChangePropertyCommand(sel, key, value));
            changedKey = key;
        }

        public void undoAllTagsAdding() {
            UndoRedoHandler.getInstance().undo(commandCount);
        }

        private void disableTagIfNeeded(final Tag t, final JosmAction action) {
            // Disable action if its key is already set on the object (the key being absent from the keys list for this reason
            // performing this action leads to autocomplete to the next key (see #7671 comments)
            for (int j = 0; j < tagData.getRowCount(); ++j) {
                if (t.getKey().equals(tagData.getValueAt(j, 0))) {
                    action.setEnabled(false);
                    break;
                }
            }
        }

        private void loadSVMmodel() {
            File modelDirectory = new File(MODEL_PATH);
            File modelFile;
            if (useCombinedModel) {
                if (filesAndWeights.isEmpty()) {
                    System.out.println("No models selected! Loading defaults..");
                    if (modelWithClasses) {
                        System.out.println("Using default/last model with classes: " + modelDirectory.getAbsolutePath() + "/model_with_classes");
                        modelFile = new File(modelDirectory.getAbsolutePath() + "/model_with_classes");
                        try {
                            System.out.println("try to load model: " + modelFile.getAbsolutePath());
                            modelSVM = Model.load(modelFile);
                            System.out.println("model loaded!");

                        } catch (IOException ex) {
                            Logger.getLogger(TrainWorker.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        modelSVMLabelSize = modelSVM.getLabels().length;
                        modelSVMLabels = modelSVM.getLabels();
                    } else {
                        System.out.println("Using default/last model without classes: " + modelDirectory.getAbsolutePath() + "/best_model");
                        modelFile = new File(modelDirectory.getAbsolutePath() + "/best_model");
                        try {
                            System.out.println("try to load model: " + modelFile.getAbsolutePath());
                            modelSVM = Model.load(modelFile);
                            System.out.println("model loaded!");

                        } catch (IOException ex) {
                            Logger.getLogger(TrainWorker.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        modelSVMLabelSize = modelSVM.getLabels().length;
                        modelSVMLabels = modelSVM.getLabels();
                    }
                }
                if (modelWithClasses) { //check filenames to define if model with classes is selected
                    System.out.println("Using combined model with classes");
                    useCombinedSVMmodels(sel, true);
                } else {
                    System.out.println("Using combined model without classes");
                    useCombinedSVMmodels(sel, false);
                }
            } else if (useCustomSVMModel) {
                System.out.println("custom path: " + customSVMModelPath);
                File checkExistance = new File(customSVMModelPath);
                if (checkExistance.exists() && checkExistance.isFile()) {
                    if (modelWithClasses) {
                        System.out.println("Using custom model with classes: ");
                        if (customSVMModelPath.endsWith(".0")) {
                            String customSVMModelPathWithClasses = customSVMModelPath.substring(0, customSVMModelPath.length() - 2) + ".1";

                            modelFile = new File(customSVMModelPathWithClasses);
                            System.out.println(customSVMModelPathWithClasses);
                        } else {
                            modelFile = new File(customSVMModelPath);
                        }
                    } else {
                        System.out.println("Using custom model without classes");
                        if (customSVMModelPath.endsWith(".1")) {
                            String customSVMModelPathWithoutClasses = customSVMModelPath.substring(0, customSVMModelPath.length() - 2) + ".0";
                            modelFile = new File(customSVMModelPathWithoutClasses);
                            System.out.println(customSVMModelPathWithoutClasses);
                        } else {
                            modelFile = new File(customSVMModelPath);
                        }
                    }
                    try {
                        System.out.println("try to load model: " + modelFile.getAbsolutePath());
                        modelSVM = Model.load(modelFile);
                        System.out.println("model loaded!");

                    } catch (IOException ex) {
                        Logger.getLogger(TrainWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    modelSVMLabelSize = modelSVM.getLabels().length;
                    modelSVMLabels = modelSVM.getLabels();

                } else {
                    //user chose to use a custom model, but did not provide a path to a model:
                    if (modelWithClasses) {
                        System.out.println("Using default/last model with classes");
                        modelFile = new File(modelDirectory.getAbsolutePath() + "/model_with_classes");
                    } else {
                        System.out.println("Using default/last model without classes");
                        modelFile = new File(modelDirectory.getAbsolutePath() + "/best_model");
                    }

                    try {
                        System.out.println("try to load model: " + modelFile.getAbsolutePath());
                        modelSVM = Model.load(modelFile);
                        System.out.println("model loaded!");

                    } catch (IOException ex) {
                        Logger.getLogger(TrainWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    modelSVMLabelSize = modelSVM.getLabels().length;
                    modelSVMLabels = modelSVM.getLabels();

                }
            } else {
                if (modelWithClasses) {
                    System.out.println("Using default/last model with classes");
                    modelFile = new File(modelDirectory.getAbsolutePath() + "/model_with_classes");
                } else {
                    System.out.println("Using default/last model without classes");
                    modelFile = new File(modelDirectory.getAbsolutePath() + "/best_model");
                }

                try {
                    System.out.println("try to load model: " + modelFile.getAbsolutePath());
                    modelSVM = Model.load(modelFile);
                    System.out.println("model loaded!");

                } catch (IOException ex) {
                    Logger.getLogger(TrainWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
                modelSVMLabelSize = modelSVM.getLabels().length;
                modelSVMLabels = modelSVM.getLabels();
            }
        }

        private void useCombinedSVMmodels(Collection<OsmPrimitive> sel, boolean useClassFeatures) {
            System.out.println("The system will combine " + filesAndWeights.size() + " SVM models.");

            MathTransform transform = null;
            GeometryFactory geometryFactory = new GeometryFactory();
            CoordinateReferenceSystem sourceCRS = DefaultGeographicCRS.WGS84;
            CoordinateReferenceSystem targetCRS = DefaultGeocentricCRS.CARTESIAN;
            try {
                transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            } catch (FactoryException ex) {
                Logger.getLogger(OSMRecPluginHelper.class.getName()).log(Level.SEVERE, null, ex);
            }

            OSMWay selectedInstance;
            List<OsmPrimitive> osmPrimitiveSelection = new ArrayList<>(sel);
            OsmPrimitive s;

            //get a simple selection
            if (!osmPrimitiveSelection.isEmpty()) {
                s = osmPrimitiveSelection.get(0);
            } else {
                return;
            }

            selectedInstance = new OSMWay();
            for (Way selectedWay : s.getDataSet().getSelectedWays()) {
                List<Node> selectedWayNodes = selectedWay.getNodes();
                for (Node node : selectedWayNodes) {
                    node.getCoor();
                    if (node.isLatLonKnown()) {
                        double lat = node.getCoor().lat();
                        double lon = node.getCoor().lon();

                        Coordinate sourceCoordinate = new Coordinate(lon, lat);
                        Coordinate targetGeometry = null;
                        try {
                            targetGeometry = JTS.transform(sourceCoordinate, null, transform);
                        } catch (MismatchedDimensionException | TransformException ex) {
                            Logger.getLogger(OSMParser.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        Geometry geom = geometryFactory.createPoint(new Coordinate(targetGeometry));
                        selectedInstance.addNodeGeometry(geom);
                    }
                }
            }
            Geometry fullGeom = geometryFactory.buildGeometry(selectedInstance.getNodeGeometries());
            if ((selectedInstance.getNodeGeometries().size() > 3) &&
                    selectedInstance.getNodeGeometries().get(0).equals(selectedInstance.getNodeGeometries()
                            .get(selectedInstance.getNodeGeometries().size()-1))) {
                //checks if the beginning and ending node are the same and the number of nodes are more than 3.
                //the nodes must be more than 3, because jts does not allow a construction of a linear ring with less points.
                LinearRing linear = geometryFactory.createLinearRing(fullGeom.getCoordinates());
                Polygon poly = new Polygon(linear, null, geometryFactory);
                selectedInstance.setGeometry(poly);

                System.out.println("\n\npolygon");
            } else if (selectedInstance.getNodeGeometries().size() > 1) {
                //it is an open geometry with more than one nodes, make it linestring
                System.out.println("\n\nlinestring");
                LineString lineString = geometryFactory.createLineString(fullGeom.getCoordinates());
                selectedInstance.setGeometry(lineString);
            } else { //we assume all the rest geometries are points
                System.out.println("\n\npoint");
                Point point = geometryFactory.createPoint(fullGeom.getCoordinate());
                selectedInstance.setGeometry(point);
            }

            Map<String, String> selectedTags = s.getInterestingTags();
            selectedInstance.setAllTags(selectedTags);

            //construct vector
            if (selectedInstance != null) {
                int id;

                OSMClassification classifier = new OSMClassification();
                classifier.calculateClasses(selectedInstance, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);

                if (useClassFeatures) {
                    ClassFeatures classFeatures = new ClassFeatures();
                    classFeatures.createClassFeatures(selectedInstance, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
                    id = 1422;
                } else {
                    id = 1;
                }

                GeometryFeatures geometryFeatures = new GeometryFeatures(id);
                geometryFeatures.createGeometryFeatures(selectedInstance);
                id = geometryFeatures.getLastID();
                TextualFeatures textualFeatures = new TextualFeatures(id, textualList, languageDetector);
                textualFeatures.createTextualFeatures(selectedInstance);

                List<FeatureNode> featureNodeList = selectedInstance.getFeatureNodeList();

                FeatureNode[] featureNodeArray = new FeatureNode[featureNodeList.size()];

                int i = 0;
                for (FeatureNode featureNode : featureNodeList) {
                    featureNodeArray[i] = featureNode;
                    i++;
                }
                FeatureNode[] testInstance2 = featureNodeArray;

                //compute prediction list for every model
                int[] ranks = new int[10];

                for (int l = 0; l < 10; l++) {
                    ranks[l] = 10-l; //init from 10 to 1
                }

                Map<String, Double> scoreMap = new HashMap<>();

                Map<File, Double> alignedFilesAndWeights = getAlignedModels(filesAndWeights);

                for (File modelFile : alignedFilesAndWeights.keySet()) {

                    try {
                        modelSVM = Model.load(modelFile);
                    } catch (IOException ex) {
                        Logger.getLogger(TrainWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    modelSVMLabelSize = modelSVM.getLabels().length;
                    modelSVMLabels = modelSVM.getLabels();

                    Map<Integer, Integer> mapLabelsToIDs = new HashMap<>();
                    for (int h = 0; h < modelSVMLabelSize; h++) {
                        mapLabelsToIDs.put(modelSVMLabels[h], h);
                    }
                    double[] scores = new double[modelSVMLabelSize];
                    Linear.predictValues(modelSVM, testInstance2, scores);

                    Map<Double, Integer> scoresValues = new HashMap<>();
                    for (int h = 0; h < scores.length; h++) {
                        scoresValues.put(scores[h], h);
                    }

                    Arrays.sort(scores);
                    int predicted1 = modelSVMLabels[scoresValues.get(scores[scores.length-1])];
                    int predicted2 = modelSVMLabels[scoresValues.get(scores[scores.length-2])];
                    int predicted3 = modelSVMLabels[scoresValues.get(scores[scores.length-3])];
                    int predicted4 = modelSVMLabels[scoresValues.get(scores[scores.length-4])];
                    int predicted5 = modelSVMLabels[scoresValues.get(scores[scores.length-5])];
                    int predicted6 = modelSVMLabels[scoresValues.get(scores[scores.length-6])];
                    int predicted7 = modelSVMLabels[scoresValues.get(scores[scores.length-7])];
                    int predicted8 = modelSVMLabels[scoresValues.get(scores[scores.length-8])];
                    int predicted9 = modelSVMLabels[scoresValues.get(scores[scores.length-9])];
                    int predicted10 = modelSVMLabels[scoresValues.get(scores[scores.length-10])];

                    String[] predictedTags = new String[10];
                    for (Map.Entry<String, Integer> entry : mapperWithIDs.entrySet()) {

                        if (entry.getValue().equals(predicted1)) {
                            predictedTags[0] = entry.getKey();
                        } else if (entry.getValue().equals(predicted2)) {
                            predictedTags[1] = entry.getKey();
                        } else if (entry.getValue().equals(predicted3)) {
                            predictedTags[2] = entry.getKey();
                        } else if (entry.getValue().equals(predicted4)) {
                            predictedTags[3] = entry.getKey();
                        } else if (entry.getValue().equals(predicted5)) {
                            predictedTags[4] = entry.getKey();
                        } else if (entry.getValue().equals(predicted6)) {
                            predictedTags[5] = entry.getKey();
                        } else if (entry.getValue().equals(predicted7)) {
                            predictedTags[6] = entry.getKey();
                        } else if (entry.getValue().equals(predicted8)) {
                            predictedTags[7] = entry.getKey();
                        } else if (entry.getValue().equals(predicted9)) {
                            predictedTags[8] = entry.getKey();
                        } else if (entry.getValue().equals(predicted10)) {
                            predictedTags[9] = entry.getKey();
                        }
                    }
                    //clearing model, to add the new computed classes in jlist
                    model.clear();
                    for (Map.Entry<String, String> tag : mappings.entrySet()) {

                        for (int k = 0; k < 10; k++) {
                            if (tag.getValue().equals(predictedTags[k])) {
                                predictedTags[k] = tag.getKey();
                                model.addElement(tag.getKey());
                            }
                        }
                    }
                    System.out.println("combined, predicted classes: " + Arrays.toString(predictedTags));

                    for (int r = 0; r < ranks.length; r++) {
                        String predictedTag = predictedTags[r];
                        Double currentWeight = alignedFilesAndWeights.get(modelFile);
                        double finalRank = ranks[r]*currentWeight;

                        if (scoreMap.containsKey(predictedTag)) {
                            Double scoreToAdd = scoreMap.get(predictedTag);
                            scoreMap.put(predictedTag, finalRank+scoreToAdd);
                        } else {
                            scoreMap.put(predictedTag, finalRank);
                        }
                        //add final weight - predicted tag
                    }
                } //files iter
                model.clear();
                List<Double> scoresList = new ArrayList<>(scoreMap.values());
                Collections.sort(scoresList, Collections.reverseOrder());

                for (Double sco : scoresList) {
                    if (model.size() > 9) {
                        break;
                    }
                    for (Map.Entry<String, Double> scoreEntry : scoreMap.entrySet()) {
                        if (scoreEntry.getValue().equals(sco)) {
                            model.addElement(scoreEntry.getKey());
                        }
                    }
                }
            }
        }

        private void createOSMObject(Collection<OsmPrimitive> sel) {

            MathTransform transform = null;
            GeometryFactory geometryFactory = new GeometryFactory();
            CoordinateReferenceSystem sourceCRS = DefaultGeographicCRS.WGS84;
            CoordinateReferenceSystem targetCRS = DefaultGeocentricCRS.CARTESIAN;
            try {
                transform = CRS.findMathTransform(sourceCRS, targetCRS, true);

            } catch (FactoryException ex) {
                Logger.getLogger(OSMRecPluginHelper.class.getName()).log(Level.SEVERE, null, ex);
            }

            //fire an error to the user if he has multiple selection from map

            //we consider simple (one instance) selection, so we get the first of the sel list

            OSMWay selectedInstance;
            List<OsmPrimitive> osmPrimitiveSelection = new ArrayList<>(sel);
            OsmPrimitive s;

            //get a simple selection
            if (!osmPrimitiveSelection.isEmpty()) {
                s = osmPrimitiveSelection.get(0);
            } else {
                return;
            }

            selectedInstance = new OSMWay();
            for (Way selectedWay : s.getDataSet().getSelectedWays()) {
                List<Node> selectedWayNodes = selectedWay.getNodes();
                for (Node node : selectedWayNodes) {
                    node.getCoor();
                    if (node.isLatLonKnown()) {
                        double lat = node.getCoor().lat();
                        double lon = node.getCoor().lon();

                        Coordinate sourceCoordinate = new Coordinate(lon, lat);
                        Coordinate targetGeometry = null;
                        try {
                            targetGeometry = JTS.transform(sourceCoordinate, null, transform);
                        } catch (MismatchedDimensionException | TransformException ex) {
                            Logger.getLogger(OSMParser.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        Geometry geom = geometryFactory.createPoint(new Coordinate(targetGeometry));
                        selectedInstance.addNodeGeometry(geom);
                    }
                }
            }
            Geometry fullGeom = geometryFactory.buildGeometry(selectedInstance.getNodeGeometries());

            System.out.println("number of nodes: " + selectedInstance.getNodeGeometries().size());

            if ((selectedInstance.getNodeGeometries().size() > 3) &&
                    selectedInstance.getNodeGeometries().get(0).equals(selectedInstance.getNodeGeometries()
                            .get(selectedInstance.getNodeGeometries().size()-1))) {
                //checks if the beginning and ending node are the same and the number of nodes are more than 3.
                //the nodes must be more than 3, because jts does not allow a construction of a linear ring with less points.
                LinearRing linear = geometryFactory.createLinearRing(fullGeom.getCoordinates());
                Polygon poly = new Polygon(linear, null, geometryFactory);
                selectedInstance.setGeometry(poly);

                System.out.println("\n\npolygon");
            } else if (selectedInstance.getNodeGeometries().size() > 1) {
                //it is an open geometry with more than one nodes, make it linestring
                System.out.println("\n\nlinestring");
                LineString lineString = geometryFactory.createLineString(fullGeom.getCoordinates());
                selectedInstance.setGeometry(lineString);
            } else { //we assume all the rest geometries are points
                System.out.println("\n\npoint");
                Point point = geometryFactory.createPoint(fullGeom.getCoordinate());
                selectedInstance.setGeometry(point);
            }

            Map<String, String> selectedTags = s.getInterestingTags();
            selectedInstance.setAllTags(selectedTags);

            //construct vector here
            if (selectedInstance != null) {
                int id;
                if (mappings == null) {
                    System.out.println("null mappings ERROR");
                }

                OSMClassification classifier = new OSMClassification();
                classifier.calculateClasses(selectedInstance, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);

                if (modelWithClasses) {
                    ClassFeatures classFeatures = new ClassFeatures();
                    classFeatures.createClassFeatures(selectedInstance, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
                    id = 1422;
                } else {
                    id = 1;
                }

                GeometryFeatures geometryFeatures = new GeometryFeatures(id);
                geometryFeatures.createGeometryFeatures(selectedInstance);
                id = geometryFeatures.getLastID();
                TextualFeatures textualFeatures = new TextualFeatures(id, textualList, languageDetector);
                textualFeatures.createTextualFeatures(selectedInstance);

                List<FeatureNode> featureNodeList = selectedInstance.getFeatureNodeList();
                System.out.println(featureNodeList);

                FeatureNode[] featureNodeArray = new FeatureNode[featureNodeList.size()];

                int i = 0;
                for (FeatureNode featureNode : featureNodeList) {
                    featureNodeArray[i] = featureNode;
                    i++;
                }
                FeatureNode[] testInstance2 = featureNodeArray;

                Map<Integer, Integer> mapLabelsToIDs = new HashMap<>();
                for (int h = 0; h < modelSVMLabelSize; h++) {
                    mapLabelsToIDs.put(modelSVMLabels[h], h);
                }

                double[] scores = new double[modelSVMLabelSize];
                Linear.predictValues(modelSVM, testInstance2, scores);

                Map<Double, Integer> scoresValues = new HashMap<>();
                for (int h = 0; h < scores.length; h++) {
                    scoresValues.put(scores[h], h);
                }

                Arrays.sort(scores);

                int[] preds = new int[RECOMMENDATIONS_SIZE];
                for (int p = 0; p < RECOMMENDATIONS_SIZE; p++) {
                    preds[p] = modelSVMLabels[scoresValues.get(scores[scores.length-(p+1)])];
                }
                String[] predictedTags2 = new String[RECOMMENDATIONS_SIZE];

                for (int p = 0; p < RECOMMENDATIONS_SIZE; p++) {
                    if (idsWithMappings.containsKey(preds[p])) {
                        predictedTags2[p] = idsWithMappings.get(preds[p]);
                    }
                }

                //clearing model, to add the new computed classes in jlist
                model.clear();
                for (Map.Entry<String, String> tag : mappings.entrySet()) {
                    for (int k = 0; k < 10; k++) {
                        if (tag.getValue().equals(predictedTags2[k])) {
                            predictedTags2[k] = tag.getKey();
                            model.addElement(tag.getKey());
                        }
                    }
                }
                System.out.println("Optimized - create OSMObject, predicted classes: " + Arrays.toString(predictedTags2));
            }
        }

        private void parseTagsMappedToClasses() {

            InputStream tagsToClassesMapping = TrainWorker.class.getResourceAsStream("/resources/files/Map");
            Mapper mapper = new Mapper();
            try {
                mapper.parseFile(tagsToClassesMapping);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(Mapper.class.getName()).log(Level.SEVERE, null, ex);
            }
            mappings = mapper.getMappings();
            mapperWithIDs = mapper.getMappingsWithIDs();
            idsWithMappings = mapper.getIDsWithMappings();
        }

        private void loadTextualList(File textualListFile) {

            Scanner input = null;

            try {
                input = new Scanner(textualListFile, "UTF-8");
            } catch (FileNotFoundException ex) {
                Logging.warn(ex);
            }
            while (input.hasNext()) {
                String nextLine = input.nextLine();
                textualList.add(nextLine);
            }
            System.out.println("Textual List parsed from file successfully." + textualList);
        }

        private void loadDefaultTextualList() {

            InputStream textualListStream = TrainWorker.class.getResourceAsStream("/resources/files/textualList.txt");
            TextualStatistics textualStatistics = new TextualStatistics();
            textualStatistics.parseTextualList(textualListStream);
            textualList = textualStatistics.getTextualList();
            System.out.println("Default Textual List parsed from file successfully." + textualList);
        }

        private void loadOntology() {
            InputStream ontologyStream = TrainWorker.class.getResourceAsStream("/resources/files/owl.xml");
            Ontology ontology = new Ontology(ontologyStream);
            indirectClasses = ontology.getIndirectClasses();
            indirectClassesWithIDs = ontology.getIndirectClassesIDs();
        }

        private Map<File, Double> getAlignedModels(Map<File, Double> filesAndWeights) {
            Map<File, Double> alignedFilesAndWeights = new HashMap<>();
            if (modelWithClasses) {
                for (Entry<File, Double> entry : filesAndWeights.entrySet()) {
                    String absolutePath = entry.getKey().getAbsolutePath();
                    if (absolutePath.endsWith(".0")) {
                        String newPath = absolutePath.substring(0, absolutePath.length()-2) + ".1";
                        File alignedFile = new File(newPath);
                        if (alignedFile.exists()) {
                            alignedFilesAndWeights.put(alignedFile, entry.getValue());
                        }
                    } else {
                        alignedFilesAndWeights.put(entry.getKey(), entry.getValue());
                    }
                }
            } else {
                for (Entry<File, Double> entry : filesAndWeights.entrySet()) {
                    String absolutePath = entry.getKey().getAbsolutePath();
                    if (absolutePath.endsWith(".1")) {
                        String newPath = absolutePath.substring(0, absolutePath.length()-2) + ".0";
                        File alignedFile = new File(newPath);
                        if (alignedFile.exists()) {
                            alignedFilesAndWeights.put(alignedFile, entry.getValue());
                        }
                    } else {
                        alignedFilesAndWeights.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            return alignedFilesAndWeights;
        }
    }
}
