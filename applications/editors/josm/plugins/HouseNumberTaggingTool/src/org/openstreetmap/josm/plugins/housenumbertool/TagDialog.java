package org.openstreetmap.josm.plugins.housenumbertool;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingComboBox;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;

/**
 * @author Oliver Raupach 09.01.2012 <http://www.oliver-raupach.de>
 * @author Victor Kropp 10.03.2012 <http://victor.kropp.name>
 */
public class TagDialog extends ExtendedDialog
{
   public static final String TAG_BUILDING = "building";
   public static final String TAG_ADDR_COUNTRY = "addr:country";
   public static final String TAG_ADDR_STATE = "addr:state";
   public static final String TAG_ADDR_CITY = "addr:city";
   public static final String TAG_ADDR_POSTCODE = "addr:postcode";
   public static final String TAG_ADDR_HOUSENUMBER = "addr:housenumber";
   public static final String TAG_ADDR_STREET = "addr:street";
   /**
    * 
    */
   private static final long serialVersionUID = 6414385452106276923L;
   static private final Logger logger = Logger.getLogger(TagDialog.class.getName());
   private String pluginDir;
   private AutoCompletionManager acm;
   private OsmPrimitive selection;

   public static final String TEMPLATE_DATA = "/template.data";
  
   private AutoCompletingComboBox country;
    private AutoCompletingComboBox state;
   private AutoCompletingComboBox city;
   private AutoCompletingComboBox postcode;
   private AutoCompletingComboBox street;
   private JTextField housnumber;
   private JCheckBox buildingEnabled;
   private JCheckBox countryEnabled;
    private JCheckBox stateEnabled;
   private JCheckBox cityEnabled;
   private JCheckBox zipEnabled;
   private JCheckBox streetEnabled;
   private JCheckBox housenumberEnabled;

   public TagDialog(String pluginDir, OsmPrimitive p_selection)
   {
       super(Main.parent,
               tr("House Number Editor"),
               new String[] { tr("OK"), tr("Cancel")},
               true
       );
      this.pluginDir = pluginDir;
      this.selection = p_selection;

       JPanel editPanel = createContentPane();


      setContent(editPanel);
       setButtonIcons(new String[] {"ok.png", "cancel.png" });
       setDefaultButton(1);
       setupDialog();
       getRootPane().setDefaultButton(defaultButton);

      // middle of the screen
      setLocationRelativeTo(null);

      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            housnumber.requestFocus();
            housnumber.selectAll();
         }
      });
   }

    private JPanel createContentPane() {
        acm = selection.getDataSet().getAutoCompletionManager();

        Dto dto = loadDto();

        JPanel editPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        buildingEnabled = new JCheckBox(TAG_BUILDING);
        buildingEnabled.setFocusable(false);
        buildingEnabled.setSelected(dto.isSaveBuilding());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        editPanel.add(buildingEnabled, c);

        JTextField building = new JTextField();
        building.setPreferredSize(new Dimension(200, 24));
        building.setText("yes");
        building.setEditable(false);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        editPanel.add(building, c);

        // country
        countryEnabled = new JCheckBox(TAG_ADDR_COUNTRY);
        countryEnabled.setFocusable(false);
        countryEnabled.setSelected(dto.isSaveCountry());
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        editPanel.add(countryEnabled, c);

        country = new AutoCompletingComboBox();
        country.setPossibleACItems(acm.getValues(TAG_ADDR_COUNTRY));
        country.setPreferredSize(new Dimension(200, 24));
        country.setEditable(true);
        country.setSelectedItem(dto.getCountry());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        editPanel.add(country, c);

        // state
        stateEnabled = new JCheckBox(TAG_ADDR_STATE);
        stateEnabled.setFocusable(false);
        stateEnabled.setSelected(dto.isSaveState());
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        editPanel.add(stateEnabled, c);

        state = new AutoCompletingComboBox();
        state.setPossibleACItems(acm.getValues(TAG_ADDR_STATE));
        state.setPreferredSize(new Dimension(200, 24));
        state.setEditable(true);
        state.setSelectedItem(dto.getState());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1;
        editPanel.add(state, c);

        // city
        cityEnabled = new JCheckBox(TAG_ADDR_CITY);
        cityEnabled.setFocusable(false);
        cityEnabled.setSelected(dto.isSaveCity());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        editPanel.add(cityEnabled, c);

        city = new AutoCompletingComboBox();
        city.setPossibleACItems(acm.getValues(TAG_ADDR_CITY));
        city.setPreferredSize(new Dimension(200, 24));
        city.setEditable(true);
        city.setSelectedItem(dto.getCity());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1;
        editPanel.add(city, c);

        // postcode
        zipEnabled = new JCheckBox(TAG_ADDR_POSTCODE);
        zipEnabled.setFocusable(false);
        zipEnabled.setSelected(dto.isSavePostcode());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 0;
        editPanel.add(zipEnabled, c);

        postcode = new AutoCompletingComboBox();
        postcode.setPossibleACItems(acm.getValues(TAG_ADDR_POSTCODE));
        postcode.setPreferredSize(new Dimension(200, 24));
        postcode.setEditable(true);
        postcode.setSelectedItem(dto.getPostcode());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 1;
        editPanel.add(postcode, c);

        // street
        streetEnabled = new JCheckBox(TAG_ADDR_STREET);
        streetEnabled.setFocusable(false);
        streetEnabled.setSelected(dto.isSaveStreet());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 0;
        editPanel.add(streetEnabled, c);

        street = new AutoCompletingComboBox();
        street.setPossibleItems(getPossibleStreets());
        street.setPreferredSize(new Dimension(200, 24));
        street.setEditable(true);
        street.setSelectedItem(dto.getStreet());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 5;
        c.weightx = 1;
        editPanel.add(street, c);

        // housenumber
        housenumberEnabled = new JCheckBox(TAG_ADDR_HOUSENUMBER);
        housenumberEnabled.setFocusable(false);
        housenumberEnabled.setSelected(dto.isSaveHousenumber());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 6;
        c.weightx = 0;
        editPanel.add(housenumberEnabled, c);

        housnumber = new JTextField();
        housnumber.setPreferredSize(new Dimension(200, 24));
        int number = 0;
        try {
            number = Integer.valueOf(dto.getHousenumber()) + 2;
        } catch (NumberFormatException e) {
        }
        if (number > 0)
           housnumber.setText(String.valueOf(number));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 6;
        c.weightx = 1;
        editPanel.add(housnumber, c);
        return editPanel;
    }

    @Override
    protected void buttonAction(int buttonIndex, ActionEvent evt) {
        if (buttonIndex == 0) {
            Dto dto = new Dto();
            dto.setSaveBuilding(buildingEnabled.isSelected());
            dto.setSaveCity(cityEnabled.isSelected());
            dto.setSaveCountry(countryEnabled.isSelected());
            dto.setSaveState(stateEnabled.isSelected());
            dto.setSaveHousenumber(housenumberEnabled.isSelected());
            dto.setSavePostcode(zipEnabled.isSelected());
            dto.setSaveStreet(streetEnabled.isSelected());

            dto.setCity(getAutoCompletingComboBoxValue(city));
            dto.setCountry(getAutoCompletingComboBoxValue(country));
            dto.setHousenumber(housnumber.getText());
            dto.setPostcode(getAutoCompletingComboBoxValue(postcode));
            dto.setStreet(getAutoCompletingComboBoxValue(street));
            dto.setState(getAutoCompletingComboBoxValue(state));

            updateJOSMSelection(selection, dto);
            saveDto(dto);
        }
        setVisible(false);
    }

    private String getAutoCompletingComboBoxValue(AutoCompletingComboBox box) {
    	Object item = box.getSelectedItem();
    	if ( item != null)
    	{
    		if (item instanceof String)
    		{
    			return (String) item;
    		}
    		if (item instanceof AutoCompletionListItem)
    		{
    			return ((AutoCompletionListItem) item).getValue();
    		}
    		return item.toString();
    	}
    	else
    	{
    		return "";
    	}
    }

    protected void saveDto(Dto dto)
    {
    	 File path = new File(pluginDir);
    	 File fileName = new File(pluginDir + TagDialog.TEMPLATE_DATA);
    	 
       try
       {
         
          path.mkdirs();

         
          FileOutputStream file = new FileOutputStream(fileName);
          ObjectOutputStream o = new ObjectOutputStream(file);
          o.writeObject(dto);
          o.close();
       }
       catch (Exception ex)
       {
          logger.log(Level.SEVERE, ex.getMessage());
          
          if (fileName != null)
          {  
        	  fileName.delete();
          }
       }
    }

    protected void updateJOSMSelection(OsmPrimitive selection, Dto dto)
    {
       ArrayList<Command> commands = new ArrayList<Command>();

       if (dto.isSaveBuilding())
       {
          String value = selection.get(TagDialog.TAG_BUILDING);
          if (value == null || (value != null && !value.equals("yes")))
          {
             ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_BUILDING, "yes");
             commands.add(command);
          }
       }

       if (dto.isSaveCity())
       {
          String value = selection.get(TagDialog.TAG_ADDR_CITY);
          if (value == null || (value != null && !value.equals(dto.getCity())))
          {
             ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_CITY, dto.getCity());
             commands.add(command);
          }
       }

       if (dto.isSaveCountry())
       {
          String value = selection.get(TagDialog.TAG_ADDR_COUNTRY);
          if (value == null || (value != null && !value.equals(dto.getCountry())))
          {
             ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_COUNTRY, dto.getCountry());
             commands.add(command);
          }
       }

       if (dto.isSaveHousenumber())
       {
          String value = selection.get(TagDialog.TAG_ADDR_HOUSENUMBER);
          if (value == null || (value != null && !value.equals(dto.getHousenumber())))
          {
             ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_HOUSENUMBER, dto.getHousenumber());
             commands.add(command);
          }
       }

       if (dto.isSavePostcode())
       {
          String value = selection.get(TagDialog.TAG_ADDR_POSTCODE);
          if (value == null || (value != null && !value.equals(dto.getPostcode())))
          {
             ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_POSTCODE, dto.getPostcode());
             commands.add(command);
          }
       }

       if (dto.isSaveStreet())
       {
          String value = selection.get(TagDialog.TAG_ADDR_STREET);
          if (value == null || (value != null && !value.equals(dto.getStreet())))
          {
             ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_STREET, dto.getStreet());
             commands.add(command);
          }
       }

       if (dto.isSaveState())
       {
          String value = selection.get(TagDialog.TAG_ADDR_STATE);
          if (value == null || (value != null && !value.equals(dto.getState())))
          {
             ChangePropertyCommand command = new ChangePropertyCommand(selection, TagDialog.TAG_ADDR_STATE, dto.getState());
             commands.add(command);
          }
       }

       if (commands.size() > 0)
       {
          SequenceCommand sequenceCommand = new SequenceCommand(trn("Updating properties of up to {0} object", "Updating properties of up to {0} objects", commands.size(), commands.size()), commands);

          // executes the commands and adds them to the undo/redo chains
          Main.main.undoRedo.add(sequenceCommand);
       }
    }

    private Collection<String> getPossibleStreets() {
        /**
         * Generates a list of all visible names of highways in order to do
         * autocompletion on the road name.
         */
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

   private Dto loadDto()
   {
      Dto dto = new Dto();
      File fileName = new File(pluginDir + TagDialog.TEMPLATE_DATA);
      
      try
      {
        
         if (fileName.exists())
         {
            FileInputStream file = new FileInputStream(fileName);
            ObjectInputStream o = new ObjectInputStream(file);

            dto = (Dto) o.readObject();
            o.close();
         }
         else
         {
        	 dto.setCity(selection.get(TagDialog.TAG_ADDR_CITY));
        	 dto.setCountry(selection.get(TagDialog.TAG_ADDR_COUNTRY)); 
        	 dto.setHousenumber(selection.get(TagDialog.TAG_ADDR_HOUSENUMBER));
        	 dto.setPostcode(selection.get(TagDialog.TAG_ADDR_POSTCODE));
        	 dto.setStreet(selection.get(TagDialog.TAG_ADDR_STREET));
        	 dto.setState(selection.get(TagDialog.TAG_ADDR_STATE));
         }

      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, ex.getMessage());
         if (fileName != null)
         {  
       	  	fileName.delete();
         }
      }

      return dto;

   }
}
