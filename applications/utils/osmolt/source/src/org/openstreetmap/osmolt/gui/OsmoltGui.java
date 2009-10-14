package org.openstreetmap.osmolt.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

import org.jdom.Attribute;
import org.jdom.Element;
import org.openstreetmap.osmolt.Options;
import org.openstreetmap.osmolt.OutputInterface;
import org.openstreetmap.osmolt.ProcessOSM;
import org.openstreetmap.osmolt.TranslationAccess;
import org.openstreetmap.osmolt.Version;
import org.openstreetmap.osmolt.slippymap.BBox;
import org.openstreetmap.osmolt.slippymap.SlippyMapBBoxChooser;
import org.openstreetmap.osmolt.slippymap.SlippyMapCaller;
import org.openstreetmap.osmolt.slippymap.SlipyyMapSurroundingPane;

/**
 * the Gui of Osmolt
 * 
 * @license GPL. Copyright 2009
 * @author Josias Polchau
 */
public class OsmoltGui implements SlippyMapCaller, OutputInterface, TranslationAccess, MFGuiAccess {
  
  static ProcessOSM processOSM;
  
  /* Background Var */

  static OsmoltGui osmoltGui = new OsmoltGui();
  
  public static MapFeatures mapFeatures = new MapFeatures(osmoltGui);
  
  static Element currentFilter = null;
  
  public static boolean useOption = false;
  
  public static final ResourceBundle bundle = ResourceBundle.getBundle("osmolt");
  
  public static BBox bbox = new BBox();
  
  public static BBox oldbbox = bbox;
  
  /* Gui Vars */

  static JFrame mainFrame;
  
  static JSplitPane splitPane;
  
  static JLabel statusbar;
  
  private static JProgressBar progressBar;
  
  private static JButton btn_StartCalc;
  
  private static JButton btn_ApplyChanges;
  
  private static JButton btn_choseFolder;
  
  private static JTextField tf_OutputFolder;
  
  private static JPanel controlPanel;
  
  public static SlippyMapBBoxChooser slippyMapBBoxChooser;
  
  public static SlipyyMapSurroundingPane slipyyMapSurroundingPane;
  
  static JTabbedPane filterPanel;
  
  static JPanel helpPanel;
  
  public static MFFilterlist filterlistPanel;
  
  public static MFEditFilter editFilterPanel;
  
  public static MFEditMixed editTitelPanel;
  
  public static MFEditMixed editDescriptionPanel;
  
  static MFEditDetails editDetailsPanel;
  
  static JScrollPane helpScroll;
  
  static JFrame progressFrame;
  
  static JLabel progressLable;
  
  static JLabel progressStatusLable;
  
  public static JFileChooser fileChooser;
  
  // public static MapFeaturesGUI mapFeaturesGUI = new
  // MapFeaturesGUI(mapFeatures);
  
  public static String lookAndFeel;
  
  public static void main(String[] args) {
    startGui();
  }
  
  public static void startGui() {
    
    // mainFrame:
    // ._______________________
    // |........|..............|
    // |..opt...|.....Map......|
    // |........|..............|
    // |________|______________| <- JSplitPane
    // |........filters........|
    // |_______________________|
    // 
    //    
    //    
    //    
    //    
    //    
    //    
    //    
    //    
    //    
    
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        osmoltGui.printError(e);
      }
    });
    
    try {
      try {
        Class.forName("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        lookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
      } catch (ClassNotFoundException e) {
        lookAndFeel = UIManager.getSystemLookAndFeelClassName();
      } catch (Exception e) {
        
        try {
          System.out.println(e);
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        } catch (Exception b) {
          osmoltGui.printError(b);
        }
      }
      fileChooser = new JFileChooser();
      
      slipyyMapSurroundingPane = new SlipyyMapSurroundingPane();
      slipyyMapSurroundingPane.setLayout(new MigLayout("fill"));
      
      slippyMapBBoxChooser = new SlippyMapBBoxChooser(osmoltGui);
      
      slipyyMapSurroundingPane.add(slippyMapBBoxChooser);
      
      /* start making the panel */

      filterlistPanel = new MFFilterlist(mapFeatures, osmoltGui);
      editFilterPanel = new MFEditFilter(mapFeatures, osmoltGui);
      
      progressBar = new JProgressBar(0, 100);
      editTitelPanel = new MFEditMixed("titel", mapFeatures, osmoltGui);
      editDescriptionPanel = new MFEditMixed("description", mapFeatures, osmoltGui);
      editDetailsPanel = new MFEditDetails(osmoltGui);
      
      helpPanel = new JPanel(new MigLayout());
      helpPanel.add(new JLabel(osmoltGui.translate("help_text")));
      
      helpScroll = new JScrollPane(helpPanel);
      
      // JPanel Logoanzeige = new Logo("logo.png", "");
      
      /*
       * Controlpanel
       */

      /*
       * Start Calculation Button
       */

      btn_StartCalc = new JButton(osmoltGui.translate("btn_startCalculation"));
      btn_StartCalc.setToolTipText(osmoltGui.translate("tip_startCalculation"));
      
      btn_StartCalc.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent e) {
          Attribute a;
          if (((a = mapFeatures.data.getAttribute("output")) != null) && (a.getValue() != ""))
            startCalculation();
          else
            osmoltGui.printTranslatedError("err_no_outputfile");
          
        }
        
      });
      
      /*
       * Save Button
       */

      JButton btnSave = new JButton("", new ImageIcon(osmoltGui.getClass().getResource("/images/save_s.png")));
      btnSave.setToolTipText(osmoltGui.translate("control_saveMFfile"));
      btnSave.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fileChooser.setDialogTitle(osmoltGui.translate("save"));
          fileChooser.setApproveButtonText(osmoltGui.translate("save"));
          fileChooser.setCurrentDirectory(new File(Options.std_MF_file));
          
          fileChooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
              return f.getName().toLowerCase().endsWith(".xml") || f.isDirectory();
            }
            
            public String getDescription() {
              return "Filter (*.xml)";
            }
          });
          
          if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            mapFeatures.data.setAttribute("bbox", bbox.toLink());
            if (file.exists()) {
              if (JOptionPane.showConfirmDialog(null, osmoltGui.translate("qu_File_overwrite"), osmoltGui
                  .translate("qu_head_File_overwrite"), JOptionPane.YES_NO_CANCEL_OPTION) == 0)
                mapFeatures.saveFile(file.getAbsolutePath());
              
              if (OsmoltGui.useOption)
                Options.std_MF_file = file.getAbsolutePath();
              if (OsmoltGui.useOption)
                Options.saveOptions();
            } else
              mapFeatures.saveFile(file.getAbsolutePath());
            if (OsmoltGui.useOption)
              Options.std_MF_file = file.getAbsolutePath();
            if (OsmoltGui.useOption)
              Options.saveOptions();
          }
        }
      });
      
      /*
       * Open Button
       */
      JButton btnOpen = new JButton("", new ImageIcon(osmoltGui.getClass().getResource("/images/open_s.png")));
      btnOpen.setToolTipText(osmoltGui.translate("control_openMFfile"));
      btnOpen.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fileChooser.setDialogTitle(osmoltGui.translate("open"));
          fileChooser.setApproveButtonText(osmoltGui.translate("open"));
          fileChooser.setCurrentDirectory(new File(Options.std_MF_file));
          fileChooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
              return f.getName().toLowerCase().endsWith(".xml") || f.isDirectory();
            }
            
            public String getDescription() {
              return "XML Files (*.xml)";
            }
          });
          if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file.exists()) {
              String mfFile = file.getAbsolutePath();
              mapFeatures.openFile(mfFile);
              updateFields();
              // BBOX auslesen
              if (mapFeatures.data.getAttribute("bbox") != null) {
                String bboxString = mapFeatures.data.getAttribute("bbox").getValue();
                try {
                  bbox.setCoordinates(bboxString);
                } catch (Exception bboxerror) {
                  osmoltGui.printTranslatedError("err_correct_file");
                }
                
                if (OsmoltGui.useOption)
                  Options.std_MF_file = mfFile;
                if (OsmoltGui.useOption)
                  Options.saveOptions();
                
              }
              mapFeatures.updatesknowenTyes();
              filterlistPanel.updateGui();
              slippyMapBBoxChooser.boundingBoxChanged();
            } else {
              osmoltGui.printTranslatedError("err_correct_file");
            }
          }
        }
      });
      
      /*
       * apply Button
       */
      btn_ApplyChanges = new JButton(osmoltGui.translate("control_applyFilter"));
      
      btn_ApplyChanges.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent e) {
          osmoltGui.applyChanges();
        }
        
      });
      
      /*
       * Folder Button
       */
      btn_choseFolder = new JButton(new ImageIcon(osmoltGui.getClass().getResource(
          "/images/gnome-colors/document-save-as.png")));
      btn_choseFolder.setToolTipText(osmoltGui.translate("control_choseFolder"));
      btn_choseFolder.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent e) {
          JFileChooser chooser = new JFileChooser();
          chooser.setApproveButtonText(osmoltGui.translate("choose"));
          Attribute openfolder = mapFeatures.data.getAttribute("output");
          if ((openfolder != null) && (openfolder.getValue() != ""))
            chooser.setCurrentDirectory(new java.io.File(openfolder.getValue()));
          else
            chooser.setCurrentDirectory(new java.io.File("."));
          chooser.setDialogTitle(osmoltGui.translate("choose_folder"));
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          //
          // disable the "All files" option.
          //
          chooser.setAcceptAllFileFilterUsed(false);
          //    
          if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            // System.out.println("getCurrentDirectory(): " +
            // chooser.getCurrentDirectory());
            // System.out.println("getSelectedFile() : " +
            // chooser.getSelectedFile());
            String folder = chooser.getSelectedFile().getAbsolutePath();
            mapFeatures.data.setAttribute("output", folder);
            updateFields();
          }
        }
        
      });
      
      //
      // controlPanel
      // ..._____________
      // ..|......|......|
      // ..|.menu.|filter|
      // ..|......|.list.|
      // ..|______|______|
      // ..|.............|
      // ..|.editDetails.|
      // ..|_____________|
      //
      //
      tf_OutputFolder = new JTextField();
      tf_OutputFolder.setEditable(false);
      tf_OutputFolder.setBackground(new Color(210, 210, 210));
      
      JPanel menuPanel = new JPanel(new MigLayout("top,left"));
      
      menuPanel.add(btnSave, "");
      menuPanel.add(btnOpen, "");
      menuPanel.add(btn_choseFolder, "wrap");
      menuPanel.add(btn_StartCalc, "span,growx");
      
      controlPanel = new JPanel();
      controlPanel.setLayout(new MigLayout("fill"));
      controlPanel.add(filterlistPanel, "wrap,grow");
      controlPanel.add(tf_OutputFolder, "grow,south");
      controlPanel.add(btn_ApplyChanges, "south");
      controlPanel.add(editDetailsPanel, "south");
      controlPanel.add(tf_OutputFolder, "south");
      controlPanel.add(menuPanel, "west");
      
      filterPanel = new JTabbedPane(JTabbedPane.LEFT);
      
      filterPanel.setMaximumSize(new Dimension(10000, 230));
      filterPanel.setMinimumSize(new Dimension(700, 230));
      filterPanel.addTab("Einzelheiten", helpScroll);
      filterPanel.addTab(osmoltGui.translate("editfilter_titel_editfilter"), editFilterPanel);
      filterPanel.addTab(osmoltGui.translate("editfilter_titel_edittitel"), editTitelPanel);
      filterPanel.addTab(osmoltGui.translate("editfilter_titel_editdescription"), editDescriptionPanel);
      
      JSplitPane upperPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, controlPanel, slipyyMapSurroundingPane);
      upperPanel.setDividerLocation(400);
      
      splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, upperPanel, filterPanel);
      splitPane.setDividerLocation(500);
      
      mainFrame = new JFrame();
      
      mainFrame.setTitle(osmoltGui.translate("GuiTitle"));
      mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      mainFrame.setLayout(new BorderLayout());
      mainFrame.add(splitPane);
      //mainFrame.add(statusbar = new JLabel(" "), BorderLayout.SOUTH);
      
      mainFrame.setMinimumSize(new Dimension(800, 600));
      mainFrame.setVisible(true);
      mainFrame.pack();
      osmoltGui.updateGui();
      
      // automatisches laden einer datei
      if (false){
        mapFeatures.openFile("/home/josias/Osmolt Karten/doener/hamburg/doener.xml");
        updateFields();
        // BBOX auslesen
        if (mapFeatures.data.getAttribute("bbox") != null) {
          String bboxString = mapFeatures.data.getAttribute("bbox").getValue();
          try {
            bbox.setCoordinates(bboxString);
          } catch (Exception bboxerror) {
            osmoltGui.printTranslatedError("err_correct_file");
          }
        }
        mapFeatures.updatesknowenTyes();
        filterlistPanel.updateGui();
        slippyMapBBoxChooser.boundingBoxChanged();}
      // System.exit(0);
      updateFields();
     // throw new Exception("test");
      
    } catch (Exception e) {
      osmoltGui.printError(e);
    }
  }
  
  static void startCalculation() {
    processOSM = new ProcessOSM(bbox, mapFeatures, osmoltGui, osmoltGui);
    processOSM.start();
  }
  
  public void updateGui() {
    if (currentFilter == null) {
      filterPanel.setEnabled(false);
      filterPanel.setSelectedIndex(0);
      btn_ApplyChanges.setEnabled(false);
    } else {
      btn_ApplyChanges.setEnabled(true);
      filterPanel.setEnabled(true);
    }
    filterlistPanel.updateGui();
    editDetailsPanel.updateGui();
    editFilterPanel.updateGui();
    editTitelPanel.updateGui();
    editDescriptionPanel.updateGui();
    
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        filterPanel.repaint();
      }
    });
  }
  
  public void loadFilter() {
    
  }
  
  public void applyChanges() {
    editDetailsPanel.applyChanges();
    editFilterPanel.applyChanges();
    updateGui();
  }
  
  public void addSlipyyMapPane(SlippyMapBBoxChooser SlipyyMapPane) {
    slippyMapBBoxChooser = SlipyyMapPane;
  }
  
  public JPanel getSlipyyMapSurroundingPane() {
    return slipyyMapSurroundingPane;
  }
  
  public BBox getBBox() {
    return bbox;
  }
  
  public Dimension getSlippyMapCurrentSize() {
    return slippyMapBBoxChooser.getSize();
  }
  
  public Dimension getSlippyMapSurroundingSize() {
    return slipyyMapSurroundingPane.getSize();
  }
  
  public boolean isSetBBox() {
    return (bbox.minlat != 0) && (bbox.minlon != 0) && (bbox.maxlat != 0) && (bbox.maxlon != 0);
  }
  
  public void setBoundingBox(double minlon, double minlat, double maxlon, double maxlat) {
    bbox.minlat = minlat;
    bbox.minlon = minlon;
    bbox.maxlat = maxlat;
    bbox.maxlon = maxlon;
    
  }
  
  public void setSlippyMapSize(int x, int y, int w, int h) {
    slippyMapBBoxChooser.setBounds(x, y, w, h);
  }
  
  public void setStatusbar(int value) {
    progressBar.setValue(value);
    progressFrame.repaint();
    
  }
  
  public void processAdd() {
    progressBar.setValue(progressBar.getValue() + 1);
    progressFrame.repaint();
    progressBar.setIndeterminate(false);
    
  }
  
  public void processSetName(String s) {
    progressLable.setText(s);
    progressFrame.repaint();
    
  }
  
  public void processSetStatus(String s) {
    progressStatusLable.setText(s);
    progressFrame.repaint();
    
  }
  
  public void processSetPercent(int percent) {
    
    progressBar.setValue(percent);
    progressFrame.repaint();
  }
  
  public void processStart() {
    progressBar.setIndeterminate(true);
    progressBar.setValue(0);
    progressFrame.repaint();
  }
  
  public void processStop() {
    
    progressBar.setValue(0);
    progressFrame.repaint();
  }
  
  public JFileChooser getFileChooser() {
    return fileChooser;
  }
  
  private static void updateFields() {
    String folder = mapFeatures.data.getAttributeValue("output");
    if (folder != null)
      tf_OutputFolder.setText("output: " + folder);
    else
      tf_OutputFolder.setText("output: not set");
    
  }
  
  public void boundingBoxChanged(SlippyMapBBoxChooser chooser) {
    // TODO Automatisch erstellter Methoden-Stub
    // brauch ich das noch?
  }
  
  public void printError(String error) {
    
    JOptionPane.showMessageDialog(mainFrame, error + " ", "Error", JOptionPane.ERROR_MESSAGE);
    
  }
  
  String lastDebugclass = "";
  
  public void printDebugMessage(String classname, String message) {
    System.out.println((lastDebugclass != classname) ? (classname + "\n\t") : "\t" + message.replaceAll("\n", "\n\t"));
    lastDebugclass = classname;
  }
  
  /**
   * print stacktrace and ask to send to developer
   * 
   * @param error
   */
  public void printError(Throwable error) {
    ErrorReporter reporter=new ErrorReporter(this,this,error);
    reporter.setVisible(true);
    reporter.pack();
  }
  public void printMessage(String message) {
    
    JOptionPane.showMessageDialog(mainFrame, message + " ", "Massage", JOptionPane.INFORMATION_MESSAGE);
    
  }
  
  public void printWarning(String warning) {
    JOptionPane.showMessageDialog(mainFrame, warning + " ", "Massage", JOptionPane.WARNING_MESSAGE);
  }
  
  public void setWorkFilter(Element filter) {
    currentFilter = filter;
    updateGui();
  }
  
  public Element getWorkFilter() {
    return currentFilter;
    
  }
  
  public String getLookAndFeelClassName() {
    return lookAndFeel;
  }
  
  public void printTranslatedError(String error) {
    printError(translate(error));
    
  }
  
  public void printTranslatedMessage(String message) {
    printMessage(translate(message));
    
  }
  
  public void printTranslatedWarning(String warning) {
    printWarning(translate(warning));
    
  }
  
  public String translate(String s) {
    try {
      return OsmoltGui.bundle.getString(s);
    } catch (MissingResourceException e) {
      System.out.println("not translated: " + s);
      return s;
    }
  }
  
  public void osmoltStart() {
    progressFrame = new JFrame(osmoltGui.translate("in_progress"));
    progressLable = new JLabel(osmoltGui.translate("in_progress"));
    progressStatusLable = new JLabel("");
    progressFrame.setLayout(new MigLayout("fill"));
    progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    
    progressFrame.add(progressLable, "");
    progressFrame.add(progressBar, "growx");
    progressFrame.setMinimumSize(new Dimension(600, 100));
    JButton btnAbort = new JButton(translate("abort"));
    progressFrame.add(btnAbort, "wrap");
    progressFrame.add(progressStatusLable, "south");
    
    btnAbort.addActionListener(new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
        processSetStatus(translate("aborting"));
        processOSM.interrupt();
        while (processOSM.isAlive())
          try {
            wait(100);
          } catch (Exception e1) {
            // do nothing
          }
        progressFrame.setVisible(false);
        osmoltEnd();
      }
      
    });
    progressFrame.setVisible(true);
    btn_StartCalc.setEnabled(false);
  }
  
  public void osmoltEnd() {
    progressFrame.setVisible(false);
    btn_StartCalc.setEnabled(true);
  }
  
}
