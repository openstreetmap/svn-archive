package org.openstreetmap.josm.plugins.ImportImagePlugin;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * UI-Dialog which provides:
 * - general and spatial information about the georeferenced image
 * - a possiblitly to change the source reference system of the image
 * 
 * 
 * @author Christoph Beekmans, Fabian Kowitz, Anna Robaszkiewicz, Oliver Kuhn, Martin Ulitzny
 *
 */
public class LayerPropertiesDialog extends JFrame{
    
    private Vector<String> supportedCRS;
    private ImageLayer imageLayer;

    private JPanel mainPanel = null;
    private JPanel jPanel = null;
    private JPanel buttonPanel = null;
    private JTabbedPane jTabbedPane = null;
    private JPanel infoPanel = null;
    private JPanel crsPanel = null;
    private JButton okButton = null;
    private JLabel layerNameLabel = null;
    private JLabel layerNameValueLabel = null;
    private JLabel imageFileLabel = null;
    private JLabel imageFileValueLabel = null;
    private JLabel sizeLabel = null;
    private JLabel sizeValueLabel = null;
    private JLabel crsLabel = null;
    private JLabel crsValueLabel = null;
    private JLabel extentLabel = null;
    private JLabel defaultCRSDescriptorLabel = null;
    private JLabel defaultCRSLabel = null;
    private JTextField searchField = null;
    private JScrollPane crsListScrollPane = null;
    private JList crsJList = null;
    private JButton useDefaultCRSButton = null;
    private JButton applySelectedCRSButton = null;
    private JButton setSelectedCRSAsDefaultButton = null;
    private JLabel searchFieldLabel = null;
    private JCheckBox eastingFirstCheckBox = null;
    private JLabel eastingFirstLabel = null;
    private JLabel tabDescriptionLabel = null;
    private JLabel upperLeftLabel = null;
    private JLabel lowerLeftLabel = null;
    private JLabel upperRightLabel = null;
    private JLabel lowerRightLabel = null;
    private JLabel upperLeftValueLabel = null;
    private JLabel upperRightValueLabel = null;
    private JLabel lowerLeftValueLabel = null;
    private JLabel lowerRightValueLabel = null;
    private JLabel currentCRSLabel = null;
    private JLabel currentCRSValueLabel = null;

    /**
     * This method initializes 
     * 
     */
    public LayerPropertiesDialog(ImageLayer imageLayer, Vector<String> supportedCRS) {
        super(imageLayer.getName());
        this.supportedCRS = supportedCRS;
        this.imageLayer = imageLayer;
        initialize();
    }
    
    /**
     * This method initializes 
     * 
     */
    public LayerPropertiesDialog(Vector<String> supportedCRS) {
        super();
        this.supportedCRS = supportedCRS;
        initialize();
    }
    

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setMinimumSize(new Dimension(404, 485));
        this.setContentPane(getMainPanel());
        this.setPreferredSize(new Dimension(404, 485));
            
    }

    /**
     * This method initializes mainPanel    
     *  
     * @return javax.swing.JPanel   
     */
    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(null);
            mainPanel.add(getJPanel(), null);
            mainPanel.add(getButtonPanel(), null);
        }
        return mainPanel;
    }

    /**
     * This method initializes jPanel   
     *  
     * @return javax.swing.JPanel   
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.gridx = 0;
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.setBounds(new Rectangle(0, 0, 391, 406));
            jPanel.add(getJTabbedPane(), gridBagConstraints);
        }
        return jPanel;
    }

    /**
     * This method initializes buttonPanel  
     *  
     * @return javax.swing.JPanel   
     */
    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.setLayout(null);
            buttonPanel.setBounds(new Rectangle(0, 405, 391, 46));
            buttonPanel.add(getOkButton(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes jTabbedPane  
     *  
     * @return javax.swing.JTabbedPane  
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.addTab("General Information", null, getInfoPanel(), null);
            jTabbedPane.addTab("Source Reference System", null, getCrsPanel(), null);
        }
        return jTabbedPane;
    }

    /**
     * This method initializes infoPanel    
     *  
     * @return javax.swing.JPanel   
     */
    private JPanel getInfoPanel() {
        if (infoPanel == null) {
            lowerRightValueLabel = new JLabel();
            lowerRightValueLabel.setBounds(new Rectangle(210, 315, 134, 16));
            lowerRightValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            lowerRightValueLabel.setText((float)imageLayer.getBbox().getMinX() + ", " + (float)imageLayer.getBbox().getMaxY());
            lowerLeftValueLabel = new JLabel();
            lowerLeftValueLabel.setBounds(new Rectangle(30, 315, 133, 16));
            lowerLeftValueLabel.setHorizontalAlignment(SwingConstants.LEFT);
            lowerLeftValueLabel.setText((float)imageLayer.getBbox().getMinX() + ", " + (float)imageLayer.getBbox().getMinY());
            upperRightValueLabel = new JLabel();
            upperRightValueLabel.setBounds(new Rectangle(210, 255, 138, 16));
            upperRightValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            upperRightValueLabel.setText((float)imageLayer.getBbox().getMaxX() + ", " + (float)imageLayer.getBbox().getMaxY());
            upperLeftValueLabel = new JLabel();
            upperLeftValueLabel.setBounds(new Rectangle(30, 255, 133, 16));
            upperLeftValueLabel.setHorizontalAlignment(SwingConstants.LEFT);
            upperLeftValueLabel.setText((float)imageLayer.getBbox().getMaxX() + ", " + (float)imageLayer.getBbox().getMinY());
            lowerRightLabel = new JLabel();
            lowerRightLabel.setBounds(new Rectangle(287, 344, 74, 16));
            lowerRightLabel.setText("Lower Right");
            upperRightLabel = new JLabel();
            upperRightLabel.setBounds(new Rectangle(285, 225, 91, 16));
            upperRightLabel.setText("Upper Right");
            lowerLeftLabel = new JLabel();
            lowerLeftLabel.setBounds(new Rectangle(15, 345, 92, 16));
            lowerLeftLabel.setText("Lower Left");
            upperLeftLabel = new JLabel();
            upperLeftLabel.setBounds(new Rectangle(15, 224, 91, 16));
            upperLeftLabel.setText("Upper Left");
            extentLabel = new JLabel();
            extentLabel.setBounds(new Rectangle(120, 195, 136, 16));
            extentLabel.setEnabled(false);
            extentLabel.setHorizontalAlignment(SwingConstants.CENTER);
            extentLabel.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
            extentLabel.setText("Extent");
            crsValueLabel = new JLabel();
            crsValueLabel.setBounds(new Rectangle(150, 150, 226, 16));
            
            String crsDescription = "";
            try {
                crsDescription = imageLayer.getBbox().getCoordinateReferenceSystem().getIdentifiers().iterator().next().toString();
            } catch (Exception e) {
            }
            crsValueLabel.setText(crsDescription + "(" + imageLayer.getBbox().getCoordinateReferenceSystem().getName().toString() + ")");
            
            crsLabel = new JLabel();
            crsLabel.setBounds(new Rectangle(15, 150, 118, 16));
            crsLabel.setText("Reference System");
            sizeValueLabel = new JLabel();
            sizeValueLabel.setBounds(new Rectangle(150, 105, 226, 16));
            sizeValueLabel.setText(imageLayer.getImage().getHeight() + " x " + imageLayer.getImage().getWidth());
            sizeLabel = new JLabel();
            sizeLabel.setBounds(new Rectangle(15, 105, 121, 16));
            sizeLabel.setText("Image size");
            imageFileValueLabel = new JLabel();
            imageFileValueLabel.setBounds(new Rectangle(150, 60, 226, 16));
            imageFileValueLabel.setText(imageLayer.getImageFile().getAbsolutePath());
            imageFileValueLabel.setToolTipText(imageLayer.getImageFile().getAbsolutePath());
            imageFileLabel = new JLabel();
            imageFileLabel.setBounds(new Rectangle(15, 60, 121, 16));
            imageFileLabel.setText("Image file");
            layerNameValueLabel = new JLabel();
            layerNameValueLabel.setBounds(new Rectangle(150, 15, 226, 16));
            layerNameValueLabel.setText(imageLayer.getName());
            layerNameLabel = new JLabel();
            layerNameLabel.setBounds(new Rectangle(15, 15, 121, 16));
            layerNameLabel.setText("Layer name");
            infoPanel = new JPanel();
            infoPanel.setLayout(null);
            infoPanel.setFont(new Font("Dialog", Font.BOLD, 12));
            infoPanel.add(layerNameLabel, null);
            infoPanel.add(layerNameValueLabel, null);
            infoPanel.add(imageFileLabel, null);
            infoPanel.add(imageFileValueLabel, null);
            infoPanel.add(sizeLabel, null);
            infoPanel.add(sizeValueLabel, null);
            infoPanel.add(crsLabel, null);
            infoPanel.add(crsValueLabel, null);
            infoPanel.add(extentLabel, null);
            infoPanel.add(upperLeftLabel, null);
            infoPanel.add(lowerLeftLabel, null);
            infoPanel.add(upperRightLabel, null);
            infoPanel.add(lowerRightLabel, null);
            infoPanel.add(upperLeftValueLabel, null);
            infoPanel.add(upperRightValueLabel, null);
            infoPanel.add(lowerLeftValueLabel, null);
            infoPanel.add(lowerRightValueLabel, null);
        }
        return infoPanel;
    }

    /**
     * This method initializes crsPanel 
     *  
     * @return javax.swing.JPanel   
     */
    private JPanel getCrsPanel() {
        if (crsPanel == null) {
            currentCRSValueLabel = new JLabel();
            currentCRSValueLabel.setBounds(new Rectangle(78, 33, 297, 16));
            String crsDescription = "unknown";
            try {
                crsDescription = imageLayer.getSourceRefSys().getIdentifiers().iterator().next().toString();
            } catch (Exception e) {
            }
            currentCRSValueLabel.setText(crsDescription);
            
            currentCRSLabel = new JLabel();
            currentCRSLabel.setBounds(new Rectangle(15, 33, 52, 16));
            currentCRSLabel.setText("Current:");
            tabDescriptionLabel = new JLabel();
            tabDescriptionLabel.setBounds(new Rectangle(15, 9, 361, 16));
            tabDescriptionLabel.setText("Set here the source reference system of the image");
            eastingFirstLabel = new JLabel();
            eastingFirstLabel.setBounds(new Rectangle(315, 210, 76, 46));
            eastingFirstLabel.setHorizontalTextPosition(SwingConstants.TRAILING);
            eastingFirstLabel.setHorizontalAlignment(SwingConstants.CENTER);
            eastingFirstLabel.setText("<html>Easting<br>first</html>");
            searchFieldLabel = new JLabel();
            searchFieldLabel.setBounds(new Rectangle(298, 114, 84, 16));
            searchFieldLabel.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
            searchFieldLabel.setHorizontalTextPosition(SwingConstants.TRAILING);
            searchFieldLabel.setHorizontalAlignment(SwingConstants.CENTER);
            searchFieldLabel.setText("Search");
            defaultCRSLabel = new JLabel();
            defaultCRSLabel.setBounds(new Rectangle(15, 89, 361, 16));
            defaultCRSLabel.setText(PluginOperations.defaultSourceCRSDescription);
            defaultCRSDescriptorLabel = new JLabel();
            defaultCRSDescriptorLabel.setBounds(new Rectangle(15, 63, 226, 16));
            defaultCRSDescriptorLabel.setText("Default Reference System:");
            crsPanel = new JPanel();
            crsPanel.setLayout(null);
            crsPanel.add(defaultCRSDescriptorLabel, null);
            crsPanel.add(defaultCRSLabel, null);
            crsPanel.add(getSearchField(), null);
            crsPanel.add(getCrsListScrollPane(), null);
            crsPanel.add(getUseDefaultCRSButton(), null);
            crsPanel.add(getApplySelectedCRSButton(), null);
            crsPanel.add(getSetSelectedCRSAsDefaultButton(), null);
            crsPanel.add(searchFieldLabel, null);
            crsPanel.add(getEastingFirstCheckBox(), null);
            crsPanel.add(eastingFirstLabel, null);
            crsPanel.add(tabDescriptionLabel, null);
            crsPanel.add(currentCRSLabel, null);
            crsPanel.add(currentCRSValueLabel, null);
        }
        return crsPanel;
    }

    /**
     * This method initializes okButton 
     *  
     * @return javax.swing.JButton  
     */
    private JButton getOkButton() {
        if (okButton == null) {
            okButton = new JButton();
            okButton.setBounds(new Rectangle(134, 5, 136, 31));
            okButton.setText("OK");
            okButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    
                    setVisible(false);
                    dispose();
                }
            });
        }
        return okButton;
    }

    /**
     * This method initializes searchField  
     *  
     * @return javax.swing.JTextField   
     */
    private JTextField getSearchField() {
        if (searchField == null) {
            searchField = new JTextField();
            searchField.setBounds(new Rectangle(13, 111, 282, 20));
            searchField.setToolTipText("Enter keywords or EPSG codes");
            searchField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent e) {
                    
                    for (Iterator iterator = supportedCRS.iterator(); iterator
                            .hasNext();) {
                        String type = (String) iterator.next();
                        if(type.contains(searchField.getText()))
                        {
                            crsJList.setSelectedIndex(supportedCRS.indexOf(type));
                            crsJList.ensureIndexIsVisible(supportedCRS.indexOf(type));
                            break;
                        }
                        
                    }
                }
            });
        }
        return searchField;
    }

    /**
     * This method initializes crsListScrollPane    
     *  
     * @return javax.swing.JScrollPane  
     */
    private JScrollPane getCrsListScrollPane() {
        if (crsListScrollPane == null) {
            crsListScrollPane = new JScrollPane();
            crsListScrollPane.setBounds(new Rectangle(15, 135, 301, 241));
            crsListScrollPane.setViewportView(getCrsJList());
        }
        return crsListScrollPane;
    }

    /**
     * This method initializes crsJList 
     *  
     * @return javax.swing.JList    
     */
    private JList getCrsJList() {
        if (crsJList == null) {
            crsJList = new JList(supportedCRS);
            crsJList.addListSelectionListener(new ListSelectionHandler());
        }
        return crsJList;
    }

    /**
     * This method initializes useDefaultCRSButton  
     *  
     * @return javax.swing.JButton  
     */
    private JButton getUseDefaultCRSButton() {
        if (useDefaultCRSButton == null) {
            useDefaultCRSButton = new JButton();
            useDefaultCRSButton.setBounds(new Rectangle(253, 54, 118, 28));
            useDefaultCRSButton.setText("Apply Default");
            useDefaultCRSButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    
                    try {
                        
                        setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        if(PluginOperations.defaultSourceCRS != null){
                            imageLayer.resample(PluginOperations.defaultSourceCRS);
                        }else
                        {
                            JOptionPane.showMessageDialog(getContentPane(), "<html>No default reference system available.<br>Please select one from the list</html>");
                        }
                        
                    } catch (NoSuchAuthorityCodeException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (FactoryException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (IOException e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                    }
                    finally{
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            });
        }
        return useDefaultCRSButton;
    }

    /**
     * This method initializes applySelectedCRSButton   
     *  
     * @return javax.swing.JButton  
     */
    private JButton getApplySelectedCRSButton() {
        if (applySelectedCRSButton == null) {
            applySelectedCRSButton = new JButton();
            applySelectedCRSButton.setBounds(new Rectangle(315, 135, 69, 61));
            applySelectedCRSButton.setHorizontalAlignment(SwingConstants.CENTER);
            applySelectedCRSButton.setHorizontalTextPosition(SwingConstants.TRAILING);
            applySelectedCRSButton.setText("<html>Apply<br>Selection</html>");
            applySelectedCRSButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    
                    String selection = (String) crsJList.getSelectedValue();
                    String code = selection.substring(selection.indexOf("[-") + 2, selection.indexOf("-]"));
                    
                    CoordinateReferenceSystem newRefSys = null;
                    try {
                        newRefSys = CRS.decode(code, eastingFirstCheckBox.isSelected());
                        
                        setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        
                        imageLayer.resample(newRefSys);

                    } catch (NoSuchAuthorityCodeException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (FactoryException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (IOException e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                    }
                    finally{
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                    
                    
                }
            });
        }
        return applySelectedCRSButton;
    }

    /**
     * This method initializes setSelectedCRSAsDefaultButton    
     *  
     * @return javax.swing.JButton  
     */
    private JButton getSetSelectedCRSAsDefaultButton() {
        if (setSelectedCRSAsDefaultButton == null) {
            setSelectedCRSAsDefaultButton = new JButton();
            setSelectedCRSAsDefaultButton.setBounds(new Rectangle(315, 300, 69, 61));
            setSelectedCRSAsDefaultButton.setText("<html>Set as<br>Default</html>");
            setSelectedCRSAsDefaultButton
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            
                            if(crsJList.getSelectedValue() != null){
                                String selection = (String) crsJList.getSelectedValue();
                                String code = selection.substring(selection.indexOf("[-") + 2, selection.indexOf("-]"));
                                
                                try {
                                    PluginOperations.defaultSourceCRS = CRS.decode(code, eastingFirstCheckBox.isSelected());
                                    PluginOperations.defaultSourceCRSDescription = selection;
                                    
                                    ImportImagePlugin.pluginProps.setProperty("default_crs_eastingfirst", "" + eastingFirstCheckBox.isSelected());
                                    ImportImagePlugin.pluginProps.setProperty("default_crs_srid", code);
                                    FileWriter fileWriter = new FileWriter(new File(ImportImagePlugin.PLUGINPROPERTIES_PATH));
                                    ImportImagePlugin.pluginProps.store(fileWriter, null);
                                    fileWriter.close();
                                    
                                    defaultCRSLabel.setText(selection);
                                    
                                } catch (IOException e2) {
                                    // TODO Auto-generated catch block
                                    e2.printStackTrace();
                                } catch (NoSuchAuthorityCodeException e3) {
                                    // TODO Auto-generated catch block
                                    e3.printStackTrace();
                                } catch (FactoryException e4) {
                                    // TODO Auto-generated catch block
                                    e4.printStackTrace();
                                }
                            }else{
                                JOptionPane.showMessageDialog(getContentPane(), "Please make a selection from the list.");
                            }

                            
                        }
                    });
        }
        return setSelectedCRSAsDefaultButton;
    }
    
    /**
     * This method initializes eastingFirstCheckBox 
     *  
     * @return javax.swing.JCheckBox    
     */
    private JCheckBox getEastingFirstCheckBox() {
        if (eastingFirstCheckBox == null) {
            eastingFirstCheckBox = new JCheckBox();
            eastingFirstCheckBox.setBounds(new Rectangle(345, 255, 21, 21));
            eastingFirstCheckBox.setSelected(true);
        }
        return eastingFirstCheckBox;
    }

    
    
    /**
     * Listener setting text in the search field if selection has changed.
     *
     */
    class ListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if(e.getValueIsAdjusting())
            {
                searchField.setText(supportedCRS.get(e.getLastIndex()));
                searchField.setEditable(true);
            }
        }
    }
}  //  @jve:decl-index=0:visual-constraint="142,39"
