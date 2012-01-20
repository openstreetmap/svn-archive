package org.openstreetmap.josm.plugins.housenumbertool;

import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;

/**
 * @author Oliver Raupach 09.01.2012 <http://www.oliver-raupach.de>
 */
public class TagDialog extends JDialog
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
   private SimpleTagTableModel model;
   private String pluginDir;
   private AutoCompletionManager acm;
   private OsmPrimitive selection;

   public static final String TEMPLATE_DATA = "/template.data";
  
   private JTextField country;
   private JTextField city;
   private JTextField postcode;
   private JTextField street;
   private JTextField housnumber;
   private JCheckBox buildingEnabled;
   private JCheckBox countryEnabled;
   private JCheckBox cityEnabled;
   private JCheckBox zipEnabled;
   private JCheckBox streetEnabled;
   private JCheckBox housenumberEnabled;
   private JTextField state;
   private JCheckBox stateEnabled;

   public TagDialog(String pluginDir, OsmPrimitive p_selection)
   {
      super();
      this.pluginDir = pluginDir;
      this.selection = p_selection;

      model = new SimpleTagTableModel();
      acm = selection.getDataSet().getAutoCompletionManager();

      Dto dto = loadDto();

      setModalityType(ModalityType.APPLICATION_MODAL);
      setLayout(new BorderLayout(5, 5));

      JPanel centerPanel = new JPanel();
      JPanel editPanel = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();

      buildingEnabled = new JCheckBox(TAG_BUILDING);
      buildingEnabled.setSelected(dto.isSaveBuilding());
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 0;
      editPanel.add(buildingEnabled, c);

      JTextField building = new JTextField();
      building.setPreferredSize(new Dimension(200, 20));
      building.setText("yes");
      building.setEditable(false);
      c.gridx = 1;
      c.gridy = 0;
      c.weightx = 1;
      editPanel.add(building, c);

      // country
      countryEnabled = new JCheckBox(TAG_ADDR_COUNTRY);
      countryEnabled.setSelected(dto.isSaveCountry());
      c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 1;
      c.weightx = 0;
      editPanel.add(countryEnabled, c);

      country = new JTextField();
      country.addFocusListener(new MyFocusListener(acm, model, TAG_ADDR_COUNTRY));
      country.setPreferredSize(new Dimension(200, 20));
      country.setText(dto.getCountry());
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 1;
      editPanel.add(country, c);
      
      // state
      stateEnabled = new JCheckBox(TAG_ADDR_STATE);
      stateEnabled.setSelected(dto.isSaveState());
      c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 2;
      c.weightx = 0;
      editPanel.add(stateEnabled, c);

      state = new JTextField();
      state.addFocusListener(new MyFocusListener(acm, model, TAG_ADDR_STATE));
      state.setPreferredSize(new Dimension(200, 20));
      state.setText(dto.getState());
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 1;
      c.gridy = 2;
      c.weightx = 1;
      editPanel.add(state, c);

      // city
      cityEnabled = new JCheckBox(TAG_ADDR_CITY);
      cityEnabled.setSelected(dto.isSaveCity());
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 3;
      c.weightx = 0;
      editPanel.add(cityEnabled, c);

      city = new JTextField();
      city.addFocusListener(new MyFocusListener(acm, model, TAG_ADDR_CITY));
      city.setPreferredSize(new Dimension(200, 20));
      city.setText(dto.getCity());
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 1;
      c.gridy = 3;
      c.weightx = 1;
      editPanel.add(city, c);

      // postcode 
      zipEnabled = new JCheckBox(TAG_ADDR_POSTCODE);
      zipEnabled.setSelected(dto.isSavePostcode());
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 4;
      c.weightx = 0;
      editPanel.add(zipEnabled, c);

      postcode = new JTextField();
      postcode.addFocusListener(new MyFocusListener(acm, model, TAG_ADDR_POSTCODE));
      postcode.setPreferredSize(new Dimension(200, 20));
      postcode.setText(dto.getPostcode());
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 1;
      c.gridy = 4;
      c.weightx = 1;
      editPanel.add(postcode, c);

      // street
      streetEnabled = new JCheckBox(TAG_ADDR_STREET);
      streetEnabled.setSelected(dto.isSaveStreet());
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 5;
      c.weightx = 0;
      editPanel.add(streetEnabled, c);

      street = new JTextField();
      street.setPreferredSize(new Dimension(200, 20));
      street.addFocusListener(new MyFocusListener(acm, model, TAG_ADDR_STREET));
      street.setText(dto.getStreet());
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 1;
      c.gridy = 5;
      c.weightx = 1;
      editPanel.add(street, c);

      // housenumber
      housenumberEnabled = new JCheckBox(TAG_ADDR_HOUSENUMBER);
      housenumberEnabled.setSelected(dto.isSaveHousenumber());
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 0;
      c.gridy = 6;
      c.weightx = 0;
      editPanel.add(housenumberEnabled, c);

      housnumber = new JTextField();
      housnumber.addFocusListener(new MyFocusListener(acm, model, TAG_ADDR_HOUSENUMBER));
      housnumber.setPreferredSize(new Dimension(200, 20));
      housnumber.setText(dto.getHousenumber());
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 1;
      c.gridy = 6;
      c.weightx = 1;
      editPanel.add(housnumber, c);

      JPanel panelEast = new JPanel(new GridLayout(0, 1));
      JTable table = new JTable(model);
      table.setPreferredScrollableViewportSize(new Dimension(300, 200));
      table.addMouseListener(new MouseListener()
      {

         @Override
         public void mouseReleased(MouseEvent e)
         {
             JTable target = (JTable) e.getSource();
             int row = target.getSelectedRow();
             String selectedValue = (String) model.getValueAt(row, 0);
             String tag = model.getDisplayTag();

             if (tag.equals(TAG_ADDR_COUNTRY))
             {
                country.setText(selectedValue);
             }
             if (tag.equals(TAG_ADDR_STATE))
             {
                state.setText(selectedValue);
             }
             else if (tag.equals(TAG_ADDR_CITY))
             {
                city.setText(selectedValue);
             }
             else if (tag.equals(TAG_ADDR_POSTCODE))
             {
                postcode.setText(selectedValue);
             }
             else if (tag.equals(TAG_ADDR_HOUSENUMBER))
             {
                housnumber.setText(selectedValue);
             }
             else if (tag.equals(TAG_ADDR_STREET))
             {
                street.setText(selectedValue);
             }        	 
         }

         @Override
         public void mousePressed(MouseEvent e)
         {
         }

         @Override
         public void mouseExited(MouseEvent e)
         {
         }

         @Override
         public void mouseEntered(MouseEvent e)
         {
         }

         @Override
         public void mouseClicked(MouseEvent e)
         {

         }
      });

      JScrollPane scrollPane = new JScrollPane(table);
      table.setFillsViewportHeight(true);

      panelEast.add(scrollPane);

      JButton btnOk = new JButton("OK");
      btnOk.addActionListener(new OkDialogListener(this, pluginDir)
      {

         @Override
         public void actionPerformed(ActionEvent e)
         {
            Dto dto = new Dto();
            dto.setSaveBuilding(buildingEnabled.isSelected());
            dto.setSaveCity(cityEnabled.isSelected());
            dto.setSaveCountry(countryEnabled.isSelected());
            dto.setSaveState(stateEnabled.isSelected());
            dto.setSaveHousenumber(housenumberEnabled.isSelected());
            dto.setSavePostcode(zipEnabled.isSelected());
            dto.setSaveStreet(streetEnabled.isSelected());

            dto.setCity(city.getText());
            dto.setCountry(country.getText());
            dto.setHousenumber(housnumber.getText());
            dto.setPostcode(postcode.getText());
            dto.setStreet(street.getText());
            dto.setState(state.getText());
            
            updateJOSMSelection(selection, dto);
            saveDto(dto);
            tagDialog.setVisible(false);
         }
      });

      JButton btnCancel = new JButton("Cancel");
      btnCancel.addActionListener(new CancelDialogListener(this));

      JPanel bp = new JPanel();
      bp.add(btnOk);
      bp.add(btnCancel);
 
      centerPanel.add(editPanel);
      getContentPane().add(new TopPanel(), BorderLayout.NORTH);
      getContentPane().add(centerPanel, BorderLayout.CENTER);
      getContentPane().add(panelEast, BorderLayout.EAST);
      getContentPane().add(bp, BorderLayout.PAGE_END);

      ActionListener listener = new ActionListener() {
          public final void actionPerformed(ActionEvent e) {
              setVisible(false);
              dispose();
          }
      };
      KeyStroke keyStrokeESC = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
      getRootPane().registerKeyboardAction(listener, keyStrokeESC,  JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      
      pack();

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

   private Dto loadDto()
   {
      Dto dto = new Dto();

      try
      {
         File fileName = new File(pluginDir + TagDialog.TEMPLATE_DATA);
         if (fileName.exists())
         {
            FileInputStream file = new FileInputStream(fileName);
            ObjectInputStream o = new ObjectInputStream(file);

            dto = (Dto) o.readObject();
            o.close();
         }

      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, ex.getMessage());
      }

      return dto;

   }

}

class CancelDialogListener implements ActionListener
{

   TagDialog tagDialog;

   public CancelDialogListener(TagDialog tagDialog)
   {
      this.tagDialog = tagDialog;
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      tagDialog.setVisible(false);
   }

}

class OkDialogListener implements ActionListener
{
   static private final Logger logger = Logger.getLogger(OkDialogListener.class.getName());

   TagDialog tagDialog;
   String pluginDir;

   public OkDialogListener(TagDialog tagDialog, String pluginDir)
   {
      this.tagDialog = tagDialog;
      this.pluginDir = pluginDir;
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
   }

   protected void saveDto(Dto dto)
   {

      try
      {
         File path = new File(pluginDir);
         path.mkdirs();

         File fileName = new File(pluginDir + TagDialog.TEMPLATE_DATA);
         FileOutputStream file = new FileOutputStream(fileName);
         ObjectOutputStream o = new ObjectOutputStream(file);
         o.writeObject(dto);
         o.close();
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, ex.getMessage());
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

}

class MyFocusListener implements FocusListener
{
   private String tag;
   private SimpleTagTableModel model;
   private AutoCompletionManager acm;

   public MyFocusListener(AutoCompletionManager acm, SimpleTagTableModel model, String tag)
   {
      this.tag = tag;
      this.acm = acm;
      this.model = model;
   }

   @Override
   public void focusGained(FocusEvent e)
   {
      List<TagData> modelData = new ArrayList<TagData>();
      List<AutoCompletionListItem> values = acm.getValues(tag);
      for (AutoCompletionListItem value : values)
      {
         TagData tagData = new TagData(value.getValue());
         modelData.add(tagData);
         model.setDisplayTag(tag);
      }

      model.setModelData(modelData);
   }

   @Override
   public void focusLost(FocusEvent e)
   {

   }

}

