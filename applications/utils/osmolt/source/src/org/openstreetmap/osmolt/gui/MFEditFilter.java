package org.openstreetmap.osmolt.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import net.miginfocom.swing.MigLayout;
import org.jdom.*;
import org.openstreetmap.osmolt.Filter;
import org.openstreetmap.osmolt.Osmolt;
import org.openstreetmap.osmolt.Filter.RestrictionType;

public class MFEditFilter extends JPanel implements TreeSelectionListener {
  
  /**
   * 
   */
  private static final long serialVersionUID = 4182341870393810410L;
  
  OsmoltGui mainframe = new OsmoltGui();
  
  JButton bt_apply;
  
  JButton bt_reset;
  
  private JButton addLogicalButton;
  
  private JButton addRestrictionButton;
  
  private JButton removeButton;
  
  private JScrollPane scrollPane;
  
  private JRadioButton rb_and;
  
  private JRadioButton rb_or;
  
  private JRadioButton rb_type_equal;
  
  private JRadioButton rb_type_amongothers;
  
  private JRadioButton rb_type_regex;
  
  private JRadioButton rb_type_anything;
  
  private JCheckBox cb_negation;
  
  JPanel filterpanel;
  
  JPanel restrictionpanel;
  
  JPanel editElementPanel;
  
  JList selectionList;
  
  DefaultListModel selectionListModel = new DefaultListModel();
  
  // TODO auswahl => felder füllen
  
  private JLabel lb_Info;
  
  private JTextField tf_key;
  
  private JTextField tf_value;
  
  Element currentElement = null;
  
  // the current mapelement
  Element currentEntry = null;
  
  // the current Filter
  Element currentFilter = null;
  
  Element rootLogical;
  
  MapFeatures mapFeatures;
  
  MFGuiAccess gui;
  
  public MFEditFilter(MapFeatures mapFeatures, MFGuiAccess gui) {
    try {
      UIManager.setLookAndFeel(gui.getLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    this.mapFeatures = mapFeatures;
    this.gui = gui;
    
    rb_and = new JRadioButton("AND");
    rb_or = new JRadioButton("OR");
    
    rb_type_equal = new JRadioButton(gui.translate("type_equal"));
    rb_type_amongothers = new JRadioButton(gui.translate("type_amongothers"));
    rb_type_regex = new JRadioButton(gui.translate("type_regex"));
    rb_type_anything = new JRadioButton(gui.translate("type_anything"));
    
    rb_type_anything.addChangeListener(new ChangeListener() {
      
      public void stateChanged(ChangeEvent e) {
        if (rb_type_anything.isSelected())
          tf_value.setText("*");
        
      }
      
    });
    
    cb_negation = new JCheckBox(gui.translate("editfilter_cb_exclude"));
    filterpanel = new JPanel();
    restrictionpanel = new JPanel(new GridLayout(2, 2));
    lb_Info = new JLabel("");
    tf_key = new JTextField("", 20);
    tf_value = new JTextField("", 20);
    editElementPanel = new JPanel();
    
    JPanel selectionPanel = new JPanel();
    selectionPanel.setLayout(new BorderLayout());
    
    selectionPanel.setSize(600, 100);
    
    // DefaultMutableTreeNode root = new DefaultMutableTreeNode(element);
    // root.getUserObjectPath();
    
    scrollPane = new JScrollPane();
    
    selectionPanel.add(scrollPane, BorderLayout.CENTER);
    selectionList = new JList();
    selectionPanel.add(selectionList, BorderLayout.CENTER);
    selectionList.setModel(selectionListModel);
    selectionList.addListSelectionListener(new ListSelectionListener() {
      int lastingIndex = 0;
      
      private Element getSelectedFilter(Element element) {
        if (lastingIndex != 0) {
          List children = element.getChildren();
          for (Iterator iter = children.iterator(); iter.hasNext();) {
            Element child = (Element) iter.next();
            lastingIndex--;
            Element e = getSelectedFilter(child);
            if (e != null)
              return e;
          }
          return null;
        } else
          return element;
      }
      
      public void valueChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting() == false) {
          
          if (selectionList.getSelectedIndex() == -1) {
            currentElement = null;
            
          } else {
            lastingIndex = selectionList.getSelectedIndex();
            // System.out.println(lastingIndex);
            currentElement = getSelectedFilter(rootLogical);
            // System.out.println(domString(currentElement, 0));
            filledit();
          }
        }
        
      }
    });
    
    updateTree();
    JPanel selectioncontrollPanel = new JPanel();
    
    selectionPanel.add(selectioncontrollPanel, BorderLayout.SOUTH);
    
    selectioncontrollPanel.setLayout(new MigLayout());
    
    addLogicalButton = new JButton(gui.translate("btn_addLogical"));
    addLogicalButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addLogical();
      }
    });
    
    addRestrictionButton = new JButton(gui.translate("btn_addRestriction"));
    addRestrictionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addRestriction();
      }
    });
    
    removeButton = new JButton(gui.translate("btn_delete"));
    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        remove();
      }
    });
    
    selectioncontrollPanel.add(addLogicalButton, "");
    selectioncontrollPanel.add(addRestrictionButton, "");
    selectioncontrollPanel.add(removeButton, "");
    
    ButtonGroup g = new ButtonGroup();
    
    lb_Info.setText(gui.translate("str_hint_editfilter_restriction"));
    
    ButtonGroup bg_restriction = new ButtonGroup();
    bg_restriction.add(rb_type_equal);
    bg_restriction.add(rb_type_amongothers);
    bg_restriction.add(rb_type_regex);
    bg_restriction.add(rb_type_anything);
    
    restrictionpanel.setLayout(new MigLayout());
    
    restrictionpanel.add(new JLabel("Key"));
    restrictionpanel.add(tf_key, "wrap");
    restrictionpanel.add(new JLabel("Value"));
    restrictionpanel.add(tf_value, "wrap");
    restrictionpanel.add(new JLabel(gui.translate("type")), "wrap");
    
    restrictionpanel.add(rb_type_equal, "wrap,span 2");
    restrictionpanel.add(rb_type_amongothers, "wrap,span 2");
    restrictionpanel.add(rb_type_regex, "wrap,span 2");
    restrictionpanel.add(rb_type_anything, "wrap,span 2");
    
    ButtonGroup bg_filter = new ButtonGroup();
    bg_filter.add(rb_and);
    bg_filter.add(rb_or);
    
    filterpanel.setLayout(new MigLayout());
    filterpanel.add(rb_and);
    filterpanel.add(rb_or);
    
    filterpanel.setVisible(false);
    
    bt_apply = new JButton(gui.translate("apply"));
    bt_reset = new JButton(gui.translate("reset"));
    
    bt_apply.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        apply();
      }
    });
    
    bt_reset = new JButton(gui.translate("reset"));
    
    bt_reset.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        filledit();
      }
    });
    
    // editElementPanel.setLayout(new MigLayout(( "debug, inset
    // 20"),"[para]0[][100lp, fill][60lp][95lp, fill]", ""));
    editElementPanel.setLayout(new MigLayout());
    editElementPanel.add(lb_Info, "span 2,wrap");
    editElementPanel.add(restrictionpanel, "span 2,wrap, hidemode 1");
    editElementPanel.add(filterpanel, "span 2,wrap, hidemode 1");
    editElementPanel.add(cb_negation, "span 2,wrap");
    editElementPanel.add(bt_apply, "");
    editElementPanel.add(bt_reset, "");
    
    setLayout(new BorderLayout());
    add(new JTextField(gui.translate("editfilter_title")));
    add(selectionPanel, BorderLayout.CENTER);
    add(editElementPanel, BorderLayout.EAST);
    
  }
  
  protected void setrestriction() {
    restrictionpanel.setVisible(true);
    filterpanel.setVisible(false);
    tf_key.setText("");
    tf_value.setText("");
  }
  
  protected void setfilter() {
    restrictionpanel.setVisible(false);
    filterpanel.setVisible(true);
  }
  
  protected void remove() {
    
    List l = rootLogical.getChildren();
    Element mainRestriction = (Element) l.get(0);
    if (currentElement != rootLogical) {
      if (currentElement != mainRestriction) {
        
        if (JOptionPane.showConfirmDialog(null, gui.translate("editfilter_ques_removeNode"), "remove",
            JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
          currentElement.detach();
          updateTree();
        }
        
      } else
        gui.printTranslatedError("editfilter_hint_cantremoveMainrestriction");
    } else
      gui.printTranslatedError("editfilter_hint_cantremove");
  }
  
  /**
   * fügt eine neue Einschränkung hinzu
   */
  protected void addRestriction() {
    if (currentElement == null)
      currentElement = rootLogical;
    // System.out.println(MapFeatures.domString(currentElement, 0));
    if (!currentElement.getName().equals("restriction")) {
      Element elm = new Element("restriction");
      elm.setAttribute("osmKey", "x");
      elm.setAttribute("osmValue", "x");
      elm.setAttribute("type", "equal");
      currentElement.addContent(elm);
      updateTree();
    } else
      gui.printTranslatedError("editfilter_hint_cantAdd");
  }
  
  /**
   * fügt eine neue logische Operation hinzu
   */
  protected void addLogical() {
    if (currentElement == null)
      currentElement = rootLogical;
    // System.out.println(MapFeatures.domString(currentElement, 0));
    if (!currentElement.getName().equals("restriction")) {
      Element elm = new Element("logical");
      elm.setAttribute("type", "and");
      currentElement.addContent(elm);
      updateTree();
    } else
      gui.printTranslatedError("editfilter_hint_cantAdd");
  }
  
  private String getElementsummary(Element element) {
    if (element.getName() == "logical") {
      
      // root
      // if (element.getAttribute("name") != null
      // && element.getAttribute("name").getValue() == "root")
      // return "if";
      // and / or
      if (element.getAttribute("type") != null)
        return element.getAttributeValue("type");
      
    } else if (element.getName() == "restriction") {
      String s = "";
      if ((element.getAttribute("osmKey") != null) && (element.getAttribute("osmKey").getValue() != ""))
        s += element.getAttribute("osmKey").getValue();
      else
        s += "*";
      
      Filter.RestrictionType type = Filter.makeRestrictionTypefromString(element.getAttributeValue("type"));
      
      switch (type) {
        case equal:
          s += " = ";
          break;
        case amongothers:
          s += " ~ ";
          break;
        case regex:
          s += " = regex(";
          break;
        case anything:
          s += " = ";
          break;
        
        default:
          break;
      }
      
      if ((element.getAttribute("osmValue") != null) && (element.getAttribute("osmValue").getValue() != "")
          && (type != Filter.RestrictionType.anything))
        s += element.getAttribute("osmValue").getValue();
      else
        s += "*";
      if (type == Filter.RestrictionType.regex)
        s += ")";
      
      return s;
    }
    return element.getName();
  }
  
  private void AddElementToList(Element element, int indent) {
    selectionListModel.add(selectionListModel.getSize(), getIndent(indent) + getElementsummary(element));
    List children = element.getChildren();
    for (Iterator iter = children.iterator(); iter.hasNext();) {
      Element child = (Element) iter.next();
      
      AddElementToList(child, indent + 1);
    }
    
  }
  
  private void updateTree() {
    int sel = selectionList.getSelectedIndex();
    selectionListModel.clear();
    if (rootLogical != null) {
      AddElementToList(rootLogical, 0);
      selectionList.setSelectedIndex(sel);
    }
    
  }
  
  private String getIndent(int ebene) {
    String s = "";
    for (int i = 0; i < ebene; i++) {
      s += "    ";
    }
    return s;
  }
  
  public void filledit() {
    
    // System.out.println(Osmolt.xmlToString(rootLogical));
    
    if (currentElement != null) {
      
      List l = rootLogical.getChildren();
      Element mainRestriction = (Element) l.get(0);
      
      tf_key.setEditable(false);
      tf_value.setEditable(false);
      tf_key.setEnabled(false);
      tf_value.setEnabled(false);
      bt_apply.setEnabled(false);
      bt_reset.setEnabled(false);
      rb_and.setEnabled(false);
      rb_or.setEnabled(false);
      cb_negation.setEnabled(false);
      rb_type_equal.setEnabled(false);
      rb_type_amongothers.setEnabled(false);
      rb_type_regex.setEnabled(false);
      rb_type_anything.setEnabled(false);
      if (currentElement.equals(mainRestriction)) {
        tf_key.setEditable(true);
        tf_value.setEditable(true);
        tf_key.setEnabled(true);
        tf_value.setEnabled(true);
        bt_apply.setEnabled(true);
        bt_reset.setEnabled(true);
        rb_type_equal.setEnabled(true);
        rb_type_anything.setEnabled(true);
        
      } else if (!currentElement.equals(rootLogical)) {
        tf_key.setEditable(true);
        tf_value.setEditable(true);
        tf_key.setEnabled(true);
        tf_value.setEnabled(true);
        
        bt_apply.setEnabled(true);
        bt_reset.setEnabled(true);
        rb_and.setEnabled(true);
        rb_or.setEnabled(true);
        cb_negation.setEnabled(true);
        rb_type_equal.setEnabled(true);
        rb_type_amongothers.setEnabled(true);
        rb_type_regex.setEnabled(true);
        rb_type_anything.setEnabled(true);
        
      }
      
      Element selElement = currentElement;
      if (selElement.getName().equals("logical")) {
        setfilter();
        String logical = currentElement.getAttribute("type").getValue();
        if (logical.toLowerCase().equals("and"))
          rb_and.setSelected(true);
        else if (logical.toLowerCase().equals("or"))
          rb_or.setSelected(true);
        
        if (currentElement.getAttribute("negation") != null)
          cb_negation.setSelected(true);
        else
          cb_negation.setSelected(false);
      } else if (selElement.getName().equals("restriction")) {
        
        setrestriction();
        tf_key.setText(currentElement.getAttribute("osmKey").getValue());
        tf_value.setText(currentElement.getAttribute("osmValue").getValue());
        String type = currentElement.getAttributeValue("type");
        
        rb_type_equal.setSelected(true);
        
        if (type != null) {
          if (type.equals("amongothers"))
            rb_type_amongothers.setSelected(true);
          else if (type.equals("regex"))
            rb_type_regex.setSelected(true);
          else if (type.equals("anything"))
            rb_type_anything.setSelected(true);
        }
      }
      if (currentElement.getAttribute("negation") != null)
        cb_negation.setSelected(true);
      else
        cb_negation.setSelected(false);
      
    }
  }
  
  public void apply() {
    if (currentElement != null)
      if (currentElement.getName() == "logical") {
        if (rb_and.isSelected())
          currentElement.setAttribute("type", "and");
        else if (rb_or.isSelected())
          currentElement.setAttribute("type", "or");
        else
          gui.printTranslatedError("please select an Operation");
        if (cb_negation.isSelected())
          currentElement.setAttribute("negation", "true");
        else
          currentElement.removeAttribute("negation");
        updateTree();
        
      } else if (currentElement.getName() == "restriction") {
        if (tf_key.getText().isEmpty())
          gui.printTranslatedError("please insert a Key");
        else if (tf_value.getText().isEmpty())
          gui.printTranslatedError("please insert a Value");
        else {
          currentElement.removeContent();
          currentElement.setAttribute("osmKey", tf_key.getText());
          currentElement.setAttribute("osmValue", tf_value.getText());
          
          if (rb_type_equal.isSelected())
            currentElement.setAttribute("type", "equal");
          else if (rb_type_amongothers.isSelected())
            currentElement.setAttribute("type", "amongothers");
          else if (rb_type_regex.isSelected())
            currentElement.setAttribute("type", "regex");
          else if (rb_type_anything.isSelected())
            currentElement.setAttribute("type", "anything");
          else
            currentElement.removeAttribute("type");
          
          if (cb_negation.isSelected())
            currentElement.setAttribute("negation", "true");
          else
            currentElement.removeAttribute("negation");
          updateTree();
        }
      }
    
  }
  
  public void valueChanged(TreeSelectionEvent arg0) {
    Object obj = arg0.getPath().getLastPathComponent();
    if ((obj != null) && (obj.getClass().equals(new Element("test").getClass()))) {
      Element selElement = (Element) obj;
      currentElement = selElement;
      filledit();
    }
  }
  
  void updateGui() {
    currentEntry = gui.getWorkFilter();
    if (currentEntry == null) {
      tf_key.setEditable(false);
      tf_value.setEditable(false);
      setVisible(false);
    } else {
      currentFilter = currentEntry.getChild("filter");
      // setVisible(true);
      tf_value.setEditable(true);
      tf_key.setEditable(true);
      rootLogical = currentFilter.getChild("logical");
      filledit();
      updateTree();
    }
    
  }
  
  public void updateElement(Element child) {
    // TODO Automatisch erstellter Methoden-Stub
    
  }
  
  public void applyChanges() {
    List l = rootLogical.getChildren();
    Element e = (Element) l.get(0);
    
    if (e.getName().equals("restriction")) {
      rootLogical.removeContent(e);
    }
    else{

      e = new Element("restriction");
      e.setAttribute("osmKey", "");
      e.setAttribute("osmValue", "");
    }
    if (rootLogical.getContentSize() != 0)
      rootLogical.setContent(0, e);
    else
      rootLogical.addContent(e);
    updateTree();
    
  }
  
}
