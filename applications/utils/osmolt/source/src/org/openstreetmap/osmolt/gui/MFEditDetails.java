package org.openstreetmap.osmolt.gui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

import org.jdom.Element;
import org.openstreetmap.osmolt.Options;

public class MFEditDetails extends JPanel implements ActionListener {
  /**
   * 
   */
  private static final long serialVersionUID = -5528995596451559160L;
   
  MFGuiAccess gui;
  
  private JTextField tf_elm_name = new JTextField("", 30);
  
  private JTextField tf_elm_filename = new JTextField("", 30);
  
  private JTextField tf_elm_image = new JTextField("", 30);
  
  private JTextField tf_elm_imagesize = new JTextField("", 30);
  
  private JTextField tf_elm_imageoffset = new JTextField("", 30);
  
  private ImagePreview panel_imagePreview = new ImagePreview(gui);
  
  // private JTextField tf_elm_imagesizeoffset = new JTextField("", 30);
  
  JFileChooser fileChooser;
  
  JButton btnApply;
  
  JButton btnEditImage;
  
  Element currentFilter = null;
  
  public MFEditDetails(final MFGuiAccess gui) {
    this.gui = gui;
    
    fileChooser = new JFileChooser();
    tf_elm_name.setToolTipText(gui.translate("tool_tf_elm_name"));
    tf_elm_filename.setToolTipText(gui.translate("tool_tf_elm_filename"));
    tf_elm_image.setToolTipText(gui.translate("tool_tf_elm_image"));
    tf_elm_imagesize.setToolTipText(gui.translate("tool_tf_elm_imagesize"));
    tf_elm_imageoffset.setToolTipText(gui.translate("tool_tf_elm_imageoffset"));
    
    tf_elm_name.addActionListener(this);
    tf_elm_filename.addActionListener(this);
    tf_elm_image.addActionListener(this);
    tf_elm_imagesize.addActionListener(this);
    tf_elm_imageoffset.addActionListener(this);
    
    btnEditImage = new JButton(gui.translate("btn_change"));
    
    btnEditImage.addActionListener(new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
        fileChooser.setCurrentDirectory(new File(tf_elm_image.getText()));
        fileChooser.setFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".png")
                || f.getName().toLowerCase().endsWith(".gif") || f.isDirectory();
          }
          
          public String getDescription() {
            return "Pictures";
          }
        });
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          File file = fileChooser.getSelectedFile();
          tf_elm_image.setText(file.getAbsolutePath());
          updateImage();
          updatePosition();
          // mapFeatures.saveFile(file.getAbsolutePath());
          
        }
      }
      
    });
    
    tf_elm_image.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        updateImage();
      }
    });
    
    tf_elm_imagesize.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        updatePosition();
      }
    });
    
    tf_elm_imageoffset.addActionListener(new ActionListener() {
      
      public void actionPerformed(ActionEvent arg0) {
        updatePosition();
      }
    });
    
    setLayout(new MigLayout(""));
    add(new JLabel(gui.translate("lb_Filter_Name")), "");
    add(tf_elm_name, "growx, wrap");
    add(new JLabel(gui.translate("lb_filename")), "");
    add(tf_elm_filename, "growx, wrap");
    add(new JLabel(gui.translate("lb_imagefile")), "");
    add(tf_elm_image, "growx,split 2");
    add(btnEditImage, "growx, wrap");
    
    MigLayout rowLM = new MigLayout("wrap,ins 0", "[]related[]related[]related[]", "");
    
    JPanel imagePanel = new JPanel(rowLM);
    /*
     * 
     * imagePanel.add(new JLabel("Name")); imagePanel.add(new JLabel("Notes"));
     * imagePanel.add(new JTextField("growx"), "growx"); imagePanel.add(new
     * JTextArea("spany,grow", 5, 20), "spany,grow"); imagePanel.add(new
     * JLabel("Phone")); imagePanel.add(new JTextField("growx"), "growx");
     * imagePanel.add(new JLabel("Fax")); imagePanel.add(new
     * JTextField("growx"), "growx");
     */

    imagePanel.add(new JLabel(gui.translate("lb_imagesizeoffset")), "");
    imagePanel.add(tf_elm_imagesize, "growx");
    imagePanel.add(tf_elm_imageoffset, "growx");
    imagePanel.add(panel_imagePreview, "spany");
    imagePanel.add(new JLabel(gui.translate("apply_with_enter-key")), "spanx 3");
    
    add(imagePanel, "grow, span 2");
    
  }
  
  private void updateImage() {
    panel_imagePreview.updateImage(tf_elm_image.getText());
  }
  
  private void updatePosition() {
    int[] offset = { 0, 0 };
    
    int[] size = { 0, 0 };
    try {
      
      String[] sizeStr = tf_elm_imagesize.getText().split(",");
      size[0] = new Integer(sizeStr[0]);
      size[1] = new Integer(sizeStr[1]);
      String[] offsetStr = tf_elm_imageoffset.getText().split(",");
      offset[0] = new Integer(offsetStr[0]);
      offset[1] = new Integer(offsetStr[1]);
    } catch (Exception e) {
      // TODO: handle exception
    }
    panel_imagePreview.updatePosition(size, offset);
    
  }
  
  void changeCurrentFilter(Element filter) {
    currentFilter = filter;
    updateGui();
    System.out.println("set");
    
  }
  
  void applyChanges() {
    currentFilter.setAttribute("name", tf_elm_name.getText());
    currentFilter.setAttribute("filename", tf_elm_filename.getText());
    currentFilter.setAttribute("image", tf_elm_image.getText());
    currentFilter.setAttribute("imageoffset", tf_elm_imageoffset.getText());
    currentFilter.setAttribute("imagesize", tf_elm_imagesize.getText());
  }
  
  void updateGui() {
    
    currentFilter = gui.getWorkFilter();
    if (currentFilter != null) {
      tf_elm_name.setText(currentFilter.getAttributeValue("name"));
      tf_elm_filename.setText(currentFilter.getAttributeValue("filename"));
      tf_elm_image.setText(currentFilter.getAttributeValue("image"));
      tf_elm_imageoffset.setText(currentFilter.getAttributeValue("imageoffset"));
      tf_elm_imagesize.setText(currentFilter.getAttributeValue("imagesize"));
    }
    
    // TODO evtl hier performance herausholen
    btnEditImage.setEnabled(currentFilter != null);
    tf_elm_name.setEditable(currentFilter != null);
    tf_elm_filename.setEditable(currentFilter != null);
    tf_elm_image.setEditable(currentFilter != null);
    tf_elm_imageoffset.setEditable(currentFilter != null);
    tf_elm_imagesize.setEditable(currentFilter != null);
    
    tf_elm_name.setEnabled(currentFilter != null);
    tf_elm_filename.setEnabled(currentFilter != null);
    tf_elm_image.setEnabled(currentFilter != null);
    tf_elm_imageoffset.setEnabled(currentFilter != null);
    tf_elm_imagesize.setEnabled(currentFilter != null);
    updateImage();
    updatePosition();
    
  }
  
  public void actionPerformed(ActionEvent e) {
    applyChanges();
    if (JTextField.class.isInstance(e.getSource())) {
      JTextField tf = (JTextField) e.getSource();
      tf.transferFocus();
    }
  }
}
