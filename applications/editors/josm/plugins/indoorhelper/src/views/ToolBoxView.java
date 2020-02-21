// License: GPL. For details, see LICENSE file.
package views;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.widgets.DisableShortcutsOnFocusGainedTextField;
import org.openstreetmap.josm.gui.widgets.JosmComboBox;

import model.TagCatalog;
import model.TagCatalog.IndoorObject;

/**
*
* This is the main toolbox of the indoorhelper plug-in.
*
* @author egru
* @author rebsc
*
*/
@SuppressWarnings("serial")
public class ToolBoxView extends ToggleDialog {
    private JPanel dialogPanel;
    private JPanel contentPanel;
    private JLabel levelLabel;
    private JCheckBox levelCheckBox;
    private JLabel levelNameLabel;
    private DisableShortcutsOnFocusGainedTextField levelNameField;
    private JLabel repeatOnLabel;
    private DisableShortcutsOnFocusGainedTextField repeatOnField;
    private JLabel objectLabel;
    private JosmComboBox<TagCatalog.IndoorObject> objectBox;
    private JLabel nameLabel;
    private DisableShortcutsOnFocusGainedTextField nameField;
    private JLabel refLabel;
    private DisableShortcutsOnFocusGainedTextField refField;
    private JLabel multiLabel;
    private JButton multiOuterButton;
    private JButton multiInnerButton;
    private JCheckBox multiCheckBox;
    private JPanel buttonBar;
    private JButton applyButton;
    private JSeparator separator1;
    private JSeparator separator2;
    private PresetButton preset1;
    private PresetButton preset2;
    private PresetButton preset3;
    private PresetButton preset4;
    private JButton addLevelButton;
    private JButton helpButton;

   public ToolBoxView() {
       super(tr("Indoor Mapping Helper"), "indoorhelper",
               tr("Toolbox for indoor mapping assistance"), null, 300, true);

       initComponents();
   }

   private void initComponents() {
       dialogPanel = new JPanel();
       contentPanel = new JPanel();
       levelLabel = new JLabel();
       levelCheckBox = new JCheckBox();
       levelNameLabel = new JLabel();
       levelNameField = new DisableShortcutsOnFocusGainedTextField();
       repeatOnLabel = new JLabel();
       repeatOnField = new DisableShortcutsOnFocusGainedTextField();
       objectLabel = new JLabel();
       objectBox = new JosmComboBox<>(TagCatalog.IndoorObject.values());
       nameLabel = new JLabel();
       nameField = new DisableShortcutsOnFocusGainedTextField();
       refLabel = new JLabel();
       refField = new DisableShortcutsOnFocusGainedTextField();
       multiLabel = new JLabel();
       multiOuterButton = new JButton();
       multiInnerButton = new JButton();
       multiCheckBox = new JCheckBox();
       buttonBar = new JPanel();
       applyButton = new JButton();
       separator1 = new JSeparator();
       separator2 = new JSeparator();
       preset1 = new PresetButton(IndoorObject.ROOM);
       preset2 = new PresetButton(IndoorObject.STEPS);
       preset3 = new PresetButton(IndoorObject.CONCRETE_WALL);
       preset4 = new PresetButton(IndoorObject.GLASS_WALL);
       addLevelButton = new JButton();
       helpButton = new JButton();

       //======== this ========
       //Container contentPane = this.get;
       //contentPane.setLayout(new BorderLayout());

       //======== dialogPane ========

       dialogPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
       dialogPanel.setLayout(new BorderLayout());

       //======== contentPanel ========

       contentPanel.setLayout(new GridBagLayout());
       ((GridBagLayout) contentPanel.getLayout()).columnWidths = new int[] {
               0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
       ((GridBagLayout) contentPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
       ((GridBagLayout) contentPanel.getLayout()).columnWeights = new double[] {
               0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
               0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
       ((GridBagLayout) contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

       //---- addLevelButton ----
       addLevelButton.setText(tr("Insert level"));
       addLevelButton.setToolTipText(tr("Add a new level to layer."));
       addLevelButton.setEnabled(false);
       contentPanel.add(addLevelButton, new GridBagConstraints(12, 1, 3, 1, 0.0, 1.0,
               GridBagConstraints.CENTER, GridBagConstraints.BOTH,
               new Insets(0, 0, 5, 30), 0, 0));

       //---- helpButton ----
       helpButton.setText(tr("help"));
       helpButton.setToolTipText(tr("Show Help-Browser."));
       helpButton.setBackground(Color.LIGHT_GRAY);
       helpButton.setEnabled(false);
       contentPanel.add(helpButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 0), 0, 0));

       //---- levelNameLabel ----
       levelNameLabel.setText(tr("Level name"));
       contentPanel.add(levelNameLabel, new GridBagConstraints(0, 1, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

       //---- levelNameField ----
       levelNameField.setEnabled(false);
       levelNameField.setToolTipText(tr("Sets optional name tag for a level."));
       contentPanel.add(levelNameField, new GridBagConstraints(3, 1, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 30), 0, 0));

       //---- levelLabel ----
       levelLabel.setText(tr("Working level: NONE"));
       levelLabel.setToolTipText(tr("Shows the current working level."));
       contentPanel.add(levelLabel, new GridBagConstraints(6, 1, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

       //---- levelCheckBox ----
       levelCheckBox.setToolTipText(tr("Deactivate automatic level tagging."));
       contentPanel.add(levelCheckBox, new GridBagConstraints(9, 1, 1, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));
       contentPanel.add(separator1, new GridBagConstraints(0, 2, 0, 1, 0.0, 0.0,
               GridBagConstraints.CENTER, GridBagConstraints.BOTH,
               new Insets(0, 0, 5, 5), 0, 0));

       //---- objectLabel ----
       objectLabel.setText(tr("Object"));
       contentPanel.add(objectLabel, new GridBagConstraints(0, 3, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

       //---- objectBox ----
       objectBox.setEnabled(false);
       objectBox.setPrototypeDisplayValue(IndoorObject.CONCRETE_WALL);
       objectBox.setToolTipText(tr("The object preset you want to tag."));
       contentPanel.add(objectBox, new GridBagConstraints(3, 3, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 30), 0, 0));

       //---- nameLabel ----
       nameLabel.setText(tr("Name"));
       contentPanel.add(nameLabel, new GridBagConstraints(0, 4, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

       //---- nameField ----
       nameField.setEnabled(false);
       nameField.addFocusListener(new FocusListener() {

           @Override
           public void focusLost(FocusEvent e) {}

           @Override
           public void focusGained(FocusEvent e) {
               nameField.selectAll();
           }
       });
       nameField.setToolTipText(tr("Sets the name tag."));
       contentPanel.add(nameField, new GridBagConstraints(3, 4, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 30), 0, 0));

       //---- refLabel ----
       refLabel.setText(tr("Reference"));
       contentPanel.add(refLabel, new GridBagConstraints(0, 5, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

       //---- refField ----
       refField.setEnabled(false);
       refField.addFocusListener(new FocusListener() {

           @Override
           public void focusLost(FocusEvent e) {}

           @Override
           public void focusGained(FocusEvent e) {
               refField.selectAll();
           }
       });
       refField.setToolTipText(tr("Sets the referance tag."));
       contentPanel.add(refField, new GridBagConstraints(3, 5, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 30), 0, 0));

       //---- repeatOnLabel ----
       repeatOnLabel.setText(tr("Repeat on"));
       contentPanel.add(repeatOnLabel, new GridBagConstraints(0, 6, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

       //---- repeatOnField ----
       repeatOnField.setEnabled(false);
       repeatOnField.addFocusListener(new FocusListener() {

           @Override
           public void focusLost(FocusEvent e) {}

           @Override
           public void focusGained(FocusEvent e) {
               repeatOnField.selectAll();
           }
       });
       repeatOnField.setToolTipText(
               tr("Sets the repeat on tag when highway objects are selected. Please tag like this: -3-4 or -2--3 or 5-6 ."));
       contentPanel.add(repeatOnField, new GridBagConstraints(3, 6, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 30), 0, 0));
       contentPanel.add(separator2, new GridBagConstraints(0, 7, 0, 1, 0.0, 1.0,
               GridBagConstraints.CENTER, GridBagConstraints.BOTH,
               new Insets(0, 0, 5, 5), 0, 0));

       //---- preset1 ----
       preset1.setEnabled(false);
       contentPanel.add(preset1, new GridBagConstraints(6, 3, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));
       //---- preset2 ----
       preset2.setEnabled(false);
       contentPanel.add(preset2, new GridBagConstraints(6, 4, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));
       //---- preset3 ----
       preset3.setEnabled(false);
       contentPanel.add(preset3, new GridBagConstraints(6, 5, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

       //---- preset4 ----
       preset4.setEnabled(false);
       contentPanel.add(preset4, new GridBagConstraints(6, 6, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

       //---- multiLabel ----
       multiLabel.setText(tr("Multipolygon"));
       contentPanel.add(multiLabel, new GridBagConstraints(0, 8, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

       //---- multiOuterButton ----
       multiOuterButton.setText(tr("OUTER"));
       multiOuterButton.setToolTipText(tr("Creation-Tool for multipolygon with role: outer. To finish press the spacebar."));
       multiOuterButton.setEnabled(false);
       contentPanel.add(multiOuterButton, new GridBagConstraints(3, 8, 3, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 30), 0, 0));

       //---- multiInnerButton ----
       multiInnerButton.setText(tr("INNER"));
       multiInnerButton.setToolTipText(
               tr("Creation-Tool for multipolygons with role: inner. To finish press spacebar. To add to relation select \"outer\" and press enter."));
       multiInnerButton.setEnabled(false);
       contentPanel.add(multiInnerButton, new GridBagConstraints(6, 8, 1, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

       //---- multiCheckBox ----
       multiCheckBox.setToolTipText(tr("Deactivate multipolygon function."));
       contentPanel.add(multiCheckBox, new GridBagConstraints(9, 8, 1, 1, 0.0, 1.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 5, 5), 0, 0));

       dialogPanel.add(contentPanel, BorderLayout.CENTER);

       //======== buttonBar ========
       buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
       buttonBar.setLayout(new GridBagLayout());
       ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] {0, 80};
       ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0};

       //---- applyButton ----
       applyButton.setText(tr("Apply"));
       applyButton.setToolTipText(tr("Add selected tags and/or relations to obeject."));
       applyButton.setEnabled(false);
       buttonBar.add(applyButton, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
           GridBagConstraints.CENTER, GridBagConstraints.BOTH,
           new Insets(0, 0, 0, 0), 0, 0));

       dialogPanel.add(buttonBar, BorderLayout.SOUTH);

       this.createLayout(dialogPanel, true, null);
   }

   /**
    * Enables or disables the interactive UI elements of {@link #ToolBoxView}.
    *
    * @param enabled set this true for enabled elements
    */
   public void setAllUiElementsEnabled(boolean enabled) {
       this.applyButton.setEnabled(enabled);
       this.levelCheckBox.setEnabled(enabled);
       this.helpButton.setEnabled(enabled);
       this.objectBox.setEnabled(enabled);
       this.levelNameField.setEnabled(enabled);
       this.nameField.setEnabled(enabled);
       this.refField.setEnabled(enabled);
       this.levelNameField.setEnabled(enabled);
       this.repeatOnField.setEnabled(enabled);
       this.multiOuterButton.setEnabled(enabled);
       this.multiInnerButton.setEnabled(enabled);
       this.multiCheckBox.setEnabled(enabled);
       this.helpButton.setEnabled(enabled);
       this.addLevelButton.setEnabled(enabled);
       this.preset1.setEnabled(enabled);
       this.preset2.setEnabled(enabled);
       this.preset3.setEnabled(enabled);
       this.preset4.setEnabled(enabled);

       if (enabled == false) {
           resetUiElements();
       }
   }

   /**
    * Enables or disables the interactive text box elements {@link #nameField} and {@link #refField}.
    *
    * @param enabled set this true for enabled elements
    */
   public void setNRUiElementsEnabled(boolean enabled) {
       this.nameField.setEnabled(enabled);
       this.refField.setEnabled(enabled);

   }

   /**
    * Enables or disables the interactive text box element {@link #repeatOnField}.
    * @param enabled set this true for enabled elements
    */
   public void setROUiElementsEnabled(boolean enabled) {
       this.repeatOnField.setEnabled(enabled);
   }

   /**
    * Enables or disables the interactive text box element {@link #levelNameField} and {@link #addLevelButton}.
    * @param enabled set this true for enabled elements
    */
   public void setLVLUiElementsEnabled(boolean enabled) {
       this.levelNameField.setEnabled(enabled);
       this.addLevelButton.setEnabled(enabled);

   }

   /**
    * Enables or disables the interactive ComboBoxes {@link #multiOuterButton} and {@link #multiInnerButton}.
    * @param enabled set this true for enabled elements
    */
   public void setMultiUiElementsEnabled(boolean enabled) {
       this.multiOuterButton.setEnabled(enabled);
       this.multiInnerButton.setEnabled(enabled);

       if (enabled == false) resetUiElements();
   }

   /**
    * Resets the view by making the UI elements disabled.
    */
   public void reset() {
       this.setAllUiElementsEnabled(false);
   }

   /**
    * Getter for the selected {@link IndoorObject} in the {@link #objectBox}.
    *
    * @return the selected indoor object in the object ComboBox.
    */
   public IndoorObject getSelectedObject() {
       return (IndoorObject) this.objectBox.getSelectedItem();
   }

   /**
    * Getter for the level name field.
    *
    * @return the {@link String} of the {@link #levelNameField}
    */
   public String getLevelNameText() {
       return this.levelNameField.getText();
   }

   /**
    * Setter for the {@link #levelNameField}.
    *
    * @param name the String for the {@link #levelNameField}
    */
   public void setLevelNameText(String name) {
       this.levelNameField.setText(name);
   }

   /**
    * Getter for the  {@link #nameField}.
    *
    * @return String of the name text field
    */
   public String getNameText() {
       return this.nameField.getText();
   }

   /**
    * Setter for the current level value tag {@link #levelLabel}.
    *
    * @author rebsc
    * @param levelTag level value as String
    */
   public void setLevelLabel(String levelTag) {
       if (getLevelCheckBoxStatus() == false) {
          if (!levelTag.equals("")) {
              this.levelLabel.setText(tr("Working level: {0}", levelTag));
          } else {
              this.levelLabel.setText(tr("Working level: NONE"));
          }
       } else {
           this.levelLabel.setText(tr("Working level: NONE"));
       }
   }

   /**
    * Getter for the {@link #levelCheckBox} status.
    *
    * @return boolean which tells if box is selected or not.
    */
   public boolean getLevelCheckBoxStatus() {
       return this.levelCheckBox.isSelected();
   }

   /**
    * Getter for the {@link #refField}.
    *
    * @return String of the ref text field
    */
   public String getRefText() {
       return this.refField.getText();
   }

   /**
    * Getter for the repeat on TextField.
    * @return String of the repeat on text field
    */
   public String getRepeatOnText() {
       return this.repeatOnField.getText();
   }


   /**
    * Clears the text boxes and sets an empty String.
    */
   public void resetUiElements() {
       this.nameField.setText("");
       this.levelNameField.setText("");
       this.refField.setText("");
       this.repeatOnField.setText("");
       this.levelNameField.setText("");
   }

   /**
    * Set the listener for the {@link #applyButton}.
    *
    * @param l the listener to set
    */
   public void setApplyButtonListener(ActionListener l) {
       this.applyButton.addActionListener(l);
   }

   /**
    * Set the listener for {@link #levelCheckBox}.
    * @param l the listener to set
    */
   public void setLevelCheckBoxListener(ItemListener l) {
       this.levelCheckBox.addItemListener(l);
   }

   /**
    * Set the listener for {@link #helpButton}.
    * @param l the listener to set
    */
   public void setHelpButtonListener(ActionListener l) {
       this.helpButton.addActionListener(l);
   }

   /**
    * Set the listener for {@link #addLevelButton}.
    * @param l the listener to set
    */
   public void setAddLevelButtonListener(ActionListener l) {
       this.addLevelButton.addActionListener(l);
   }


   /**
    * Set the listener for {@link #objectBox}.
    *
    * @param l the listener to set
    */
   public void setObjectItemListener(ItemListener l) {
       this.objectBox.addItemListener(l);
   }

   /**
    * Set the listener for the {@link #multiOuterButton}.
    *
    * @param l the listener to set
    */
   public void setOuterButtonListener(ActionListener l) {
       this.multiOuterButton.addActionListener(l);
   }

   /**
    * Set the listener for the {@link #multiInnerButton}.
    *
    * @param l the listener to set
    */
   public void setInnerButtonListener(ActionListener l) {
       this.multiInnerButton.addActionListener(l);
   }

   /**
    * Set the listener for the {@link #multiCheckBox}.
    *
    * @param l the listener to set
    */
   public void setMultiCheckBoxListener(ItemListener l) {
       this.multiCheckBox.addItemListener(l);
   }

   public void setPresetButtons(List<IndoorObject> objects) {
       this.preset1.setIndoorObject(objects.get(0));
       this.preset2.setIndoorObject(objects.get(1));
       this.preset3.setIndoorObject(objects.get(2));
       this.preset4.setIndoorObject(objects.get(3));
   }

   public void setPreset1Listener(ActionListener l) {
       this.preset1.addActionListener(l);
   }

   public void setPreset2Listener(ActionListener l) {
       this.preset2.addActionListener(l);
   }

   public void setPreset3Listener(ActionListener l) {
       this.preset3.addActionListener(l);
   }

   public void setPreset4Listener(ActionListener l) {
       this.preset4.addActionListener(l);
   }

   public IndoorObject getPreset1() {
       return preset1.getIndoorObject();
   }

   public IndoorObject getPreset2() {
       return preset2.getIndoorObject();
   }

   public IndoorObject getPreset3() {
       return preset3.getIndoorObject();
   }

   public IndoorObject getPreset4() {
       return preset4.getIndoorObject();
   }
}
