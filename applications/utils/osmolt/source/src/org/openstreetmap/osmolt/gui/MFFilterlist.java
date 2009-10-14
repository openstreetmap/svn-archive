package org.openstreetmap.osmolt.gui;

// Imports
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jdom.Element;
import net.miginfocom.swing.MigLayout;

public class MFFilterlist extends JPanel implements ActionListener, ListSelectionListener {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private JList listbox;
  
  private Vector<String> listData;
  
  private JButton addButton;
  
  private JButton removeButton;
  
  private JScrollPane scrollPane;
  
  MapFeatures mapFeatures;
  
  MFGuiAccess mapFeaturesGUI;
  
  // Constructor of main frame
  public MFFilterlist(MapFeatures mapFeatures, MFGuiAccess mapFeaturesGUI) {
    try {
      UIManager.setLookAndFeel(mapFeaturesGUI.getLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    this.mapFeatures = mapFeatures;
    this.mapFeaturesGUI = mapFeaturesGUI;
    
    setLayout(new MigLayout("fill"));
    // setLayout(new MigLayout(( "debug, inset
    // 20"),"[para]0[][100lp,fill][60lp][95lp, fill]", ""));
    
    // Create the data model for this example
    listData = new Vector<String>();
    
    // Create a new listbox control
    listbox = new JList(listData);
    listbox.addListSelectionListener(this);
    
    // Add the listbox to a scrolling pane
    scrollPane = new JScrollPane();
    scrollPane.getViewport().add(listbox);
    
    add(scrollPane, "grow");
    
    // Create a panel to hold all other components
    JPanel controlpanel = new JPanel(new MigLayout());
    // Create some function buttons
    addButton = new JButton(mapFeaturesGUI.translate("btn_add"));
    controlpanel.add(addButton);
    addButton.addActionListener(this);
    
    removeButton = new JButton(mapFeaturesGUI.translate("btn_delete"));
    controlpanel.add(removeButton);
    removeButton.addActionListener(this);
    add(controlpanel, "south");
    // TODO übersetzen
    add(new JLabel("Filter"), "north");
    
  }
  
  // Handler for list selection changes
  public void valueChanged(ListSelectionEvent event) {
    // See if this is a listbox selection and the
    // event stream has settled
    if (event.getSource() == listbox && !event.getValueIsAdjusting()) {
      // Get the current selection and place it in the
      // edit field
      String stringValue = (String) listbox.getSelectedValue();
      if (stringValue != null)
        mapFeaturesGUI.setWorkFilter(mapFeatures.getElementByName(stringValue));
    }
  }
  
  // Handler for button presses
  public void actionPerformed(ActionEvent event) {
    if (event.getSource() == addButton) {
      String name = JOptionPane.showInputDialog(null, new String("Name"));
      if ((name != null) && (!name.equals("")))
        mapFeaturesGUI.setWorkFilter(mapFeatures.addEntry(name));
      updateGui();
    }
    
    if (event.getSource() == removeButton) {
      // Get the current selection
      Element e = mapFeaturesGUI.getWorkFilter();
      if (e != null) {
        
        // remove this item to the list and refresh
        
        Element p = e.getParentElement();
        int i = p.indexOf(e);
        p.removeContent(e);
        List children = p.getChildren();
        if (children.size() > i)
          mapFeaturesGUI.setWorkFilter((Element) children.get(i));
        else
          mapFeaturesGUI.setWorkFilter(null);
        mapFeatures.updatesknowenTyes();
        mapFeaturesGUI.updateGui();
      }
    }
  }
  
  public void updateGui() {
    
    listData = new Vector<String>();
    ArrayList<String> names = mapFeatures.getNames();
    for (int i = 0; i < names.size(); i++) {
      String stringValue = names.get(i);
      listData.addElement(stringValue);
    }
    // System.out.println(listData);
    listbox.setListData(listData);
    scrollPane.revalidate();
    scrollPane.repaint();
    
  }
}
