package org.openstreetmap.josm.plugins.namemanager.dialog;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletingTextField;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionList;
import org.openstreetmap.josm.plugins.namemanager.countryData.Country;
import org.openstreetmap.josm.plugins.namemanager.countryData.CountryDataMemory;
import org.openstreetmap.josm.plugins.namemanager.utils.NameManagerUtils;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Plugin's dialog box. Singleton class.
 * 
 * @author Rafal Jachowicz, Harman/Becker Automotive Systems (master's thesis)
 * 
 */
public class NameManagerDialog extends JDialog {

    private static final String TAG_NAME = "Tag name";
    private static final String TAG_VALUE = "Tag value";
    private static final String ADMINISTRATIVE = "Administrative";
    private static final String ADD = "Add";
    private static final String EDIT = "Edit";
    private static final String DELETE = "Delete";
    private static final String TAG_BUILDINGS = "Tag buildings";
    private static final String COUNTRY = "Country";
    private static NameManagerDialog dialog;
    private static final int height = 400;
    private static final int width = 300;

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4289136772379693178L;
    private Dimension dimension = new Dimension(width, height);
    private Way selectedWay;
    private List<Way> waysInsideSelectedArea;
    private JTabbedPane tabPanel;
    private JComboBox country;
    private JLabel labelLevel1;
    private JTextField level1;
    private JLabel labelLevel2;
    private JTextField level2;
    private JLabel labelLevel3;
    private JTextField level3;
    private JLabel labelLevel4;
    private JTextField level4;
    private JLabel labelLevel5;
    private JTextField level5;
    private JLabel labelLevel6;
    private JTextField level6;
    private JLabel labelTagNameAM;
    private AutoCompletingTextField tagNameAM;
    private JLabel labelTagNameD;
    private AutoCompletingTextField tagNameD;
    private JLabel labelTagValueAM;
    private AutoCompletingTextField tagValueAM;
    private JCheckBox buildings;
    private JButton addModifyButton;
    private JButton deleteButton;

    private NameManagerDialog() {
        super((JFrame) Main.parent, "Name Manager", true);
        Container cp = this.getContentPane();
        cp.setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu aboutMenu = new JMenu(tr("About"));
        JMenuItem aboutAppButton = new JMenuItem(tr("About Plugin"));
        aboutAppButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                String newline = System.getProperty("line.separator");
                JOptionPane.showMessageDialog(NameManagerDialog.this, "Authors:" + newline + "Rafal Jachowicz, Marek Strassenburg-Kleciak"
                        + newline + "Consultant:" + newline + "Stefanie Otte" + newline + "Powered by:" + newline
                        + "Harman Becker Automotive Systems GmbH" + newline + "Neusoft Technology Solutions GmbH", tr("About"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        aboutMenu.add(aboutAppButton);
        menuBar.add(aboutMenu);
        this.setJMenuBar(menuBar);

        tabPanel = new JTabbedPane();
        tabPanel.setTabPlacement(JTabbedPane.TOP);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton saveButton = new JButton(new SaveAction());
        JButton cancelButton = new JButton(new CancelAction());
        buttonPanel.add(cancelButton, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        JPanel administrativePanel = new JPanel();
        administrativePanel.setLayout(new BoxLayout(administrativePanel, BoxLayout.PAGE_AXIS));
        JPanel levelPanel = new JPanel(new GridLayout(7, 2, 5, 2));
        levelPanel.setAlignmentX(CENTER_ALIGNMENT);
        buildings = new JCheckBox(tr(TAG_BUILDINGS));
        buildings.setSelected(false);
        JLabel labelCountry = new JLabel("  " + tr(COUNTRY));
        country = new JComboBox();
        labelLevel1 = new JLabel("  level1");
        level1 = new JTextField();
        labelLevel2 = new JLabel("  level2");
        level2 = new JTextField();
        labelLevel3 = new JLabel("  level3");
        level3 = new JTextField();
        labelLevel4 = new JLabel("  level4");
        level4 = new JTextField();
        labelLevel5 = new JLabel("  level5");
        level5 = new JTextField();
        labelLevel6 = new JLabel("  level6");
        level6 = new JTextField();

        levelPanel.add(labelCountry);
        levelPanel.add(country);
        levelPanel.add(labelLevel1);
        levelPanel.add(level1);
        levelPanel.add(labelLevel2);
        levelPanel.add(level2);
        levelPanel.add(labelLevel3);
        levelPanel.add(level3);
        levelPanel.add(labelLevel4);
        levelPanel.add(level4);
        levelPanel.add(labelLevel5);
        levelPanel.add(level5);
        levelPanel.add(labelLevel6);
        levelPanel.add(level6);
        administrativePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        administrativePanel.add(buildings);
        administrativePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        administrativePanel.add(levelPanel);
        administrativePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        administrativePanel.add(saveButton);
        administrativePanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel addModifyPanel = new JPanel();
        addModifyPanel.setLayout(new BoxLayout(addModifyPanel, BoxLayout.PAGE_AXIS));
        labelTagNameAM = new JLabel(tr(TAG_NAME));
        tagNameAM = new AutoCompletingTextField();
        AutoCompletionList list = new AutoCompletionList();
        Main.main.getEditLayer().data.getAutoCompletionManager().populateWithKeys(list);
        tagNameAM.setAutoCompletionList(list);
        labelTagValueAM = new JLabel(tr(TAG_VALUE));
        tagValueAM = new AutoCompletingTextField();
        tagNameAM.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent arg0) {
                if (!"".equals(tagNameAM.getText())) {
                    AutoCompletionList list = tagValueAM.getAutoCompletionList();
                    if (list == null) {
                        list = new AutoCompletionList();
                    }
                    list.clear();
                    Main.main.getEditLayer().data.getAutoCompletionManager().populateWithTagValues(list, tagNameAM.getText());
                    tagValueAM.setAutoCompletionList(list);
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent arg0) {
            }
        });
        addModifyButton = new JButton(new AddModifyAction());
        addModifyPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        addModifyPanel.add(labelTagNameAM);
        addModifyPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        addModifyPanel.add(tagNameAM);
        addModifyPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        addModifyPanel.add(labelTagValueAM);
        addModifyPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        addModifyPanel.add(tagValueAM);
        addModifyPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        addModifyPanel.add(addModifyButton);
        addModifyPanel.add(Box.createRigidArea(new Dimension(0, 170)));

        JPanel deletePanel = new JPanel();
        deletePanel.setLayout(new BoxLayout(deletePanel, BoxLayout.PAGE_AXIS));
        labelTagNameD = new JLabel(tr(TAG_NAME));
        tagNameD = new AutoCompletingTextField();
        tagNameD.setAutoCompletionList(list);
        deleteButton = new JButton(new DeleteAction());
        deletePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        deletePanel.add(labelTagNameD);
        deletePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        deletePanel.add(tagNameD);
        deletePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        deletePanel.add(deleteButton);
        deletePanel.add(Box.createRigidArea(new Dimension(0, 228)));

        tabPanel.addTab(tr(ADD) + "/" + tr(EDIT), addModifyPanel);
        tabPanel.addTab(tr(DELETE), deletePanel);
        tabPanel.addTab(tr(ADMINISTRATIVE), administrativePanel);
        cp.add(tabPanel, BorderLayout.CENTER);

        this.setPreferredSize(dimension);
        this.setSize(dimension);
        this.setResizable(false);
    }

    public void setCountryComboBox() {
        Set<String> keySetUnsorted = CountryDataMemory.getCountryCache().keySet();
        List<String> keySet = new ArrayList<String>(keySetUnsorted);
        Collections.sort(keySet);
        country.removeAllItems();
        for (String string : keySet) {
            country.addItem(string);
        }
        String countryName = (String) country.getSelectedItem();
        if (CountryDataMemory.getCountryCache().containsKey(countryName)) {
            Country countryItem = CountryDataMemory.getCountryCache().get(countryName);
            labelLevel1.setText("  " + countryItem.getLevel1());
            if ("  n/a".equals(labelLevel1.getText())) {
                level1.setEnabled(false);
            } else {
                level1.setEnabled(true);
            }
            labelLevel2.setText("  " + countryItem.getLevel2());
            if ("  n/a".equals(labelLevel2.getText())) {
                level2.setEnabled(false);
            } else {
                level2.setEnabled(true);
            }
            labelLevel3.setText("  " + countryItem.getLevel3());
            if ("  n/a".equals(labelLevel3.getText())) {
                level3.setEnabled(false);
            } else {
                level3.setEnabled(true);
            }
            labelLevel4.setText("  " + countryItem.getLevel4());
            if ("  n/a".equals(labelLevel4.getText())) {
                level4.setEnabled(false);
            } else {
                level4.setEnabled(true);
            }
            labelLevel5.setText("  " + countryItem.getLevel5());
            if ("  n/a".equals(labelLevel5.getText())) {
                level5.setEnabled(false);
            } else {
                level5.setEnabled(true);
            }
            labelLevel6.setText("  " + countryItem.getLevel6());
            if ("  n/a".equals(labelLevel6.getText())) {
                level6.setEnabled(false);
            } else {
                level6.setEnabled(true);
            }
        }
        country.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String countryName = (String) country.getSelectedItem();
                if (CountryDataMemory.getCountryCache().containsKey(countryName)) {
                    Country countryItem = CountryDataMemory.getCountryCache().get(countryName);
                    labelLevel1.setText("  " + countryItem.getLevel1());
                    if ("  n/a".equals(labelLevel1.getText())) {
                        level1.setEnabled(false);
                    } else {
                        level1.setEnabled(true);
                    }
                    labelLevel2.setText("  " + countryItem.getLevel2());
                    if ("  n/a".equals(labelLevel2.getText())) {
                        level2.setEnabled(false);
                    } else {
                        level2.setEnabled(true);
                    }
                    labelLevel3.setText("  " + countryItem.getLevel3());
                    if ("  n/a".equals(labelLevel3.getText())) {
                        level3.setEnabled(false);
                    } else {
                        level3.setEnabled(true);
                    }
                    labelLevel4.setText("  " + countryItem.getLevel4());
                    if ("  n/a".equals(labelLevel4.getText())) {
                        level4.setEnabled(false);
                    } else {
                        level4.setEnabled(true);
                    }
                    labelLevel5.setText("  " + countryItem.getLevel5());
                    if ("  n/a".equals(labelLevel5.getText())) {
                        level5.setEnabled(false);
                    } else {
                        level5.setEnabled(true);
                    }
                    labelLevel6.setText("  " + countryItem.getLevel6());
                    if ("  n/a".equals(labelLevel6.getText())) {
                        level6.setEnabled(false);
                    } else {
                        level6.setEnabled(true);
                    }
                }
            }
        });
    }

    /**
     * Gets reference to the singleton object.
     * 
     * @return Singleton object of the {@link LaneManagerDialog} class.
     */
    public static NameManagerDialog getInstance() {
        if (dialog == null) {
            dialog = new NameManagerDialog();
            centerDialog(dialog);
        }
        dialog.buildings.setSelected(false);
        return dialog;
    }

    /**
     * This method centers the dialog window on the screen.
     * 
     * @param dialog
     *            is the {@link LaneManagerDialog} object
     */
    private static void centerDialog(NameManagerDialog dialog) {
        Window window = NameManagerUtils.getTopWindow();
        if (window == null)
            return;

        GraphicsConfiguration gc = window.getGraphicsConfiguration();
        if (gc != null) {
            Rectangle screenBounds = gc.getBounds();
            Rectangle windowBounds = window.getBounds();
            Dimension d = dialog.getPreferredSize();

            Point p = new Point();
            if (d.width <= windowBounds.width && d.height <= windowBounds.height) {
                p.x = windowBounds.x + ((windowBounds.width - d.width) / 2);
                p.y = windowBounds.y + ((windowBounds.height - d.height) / 2);
            } else {
                p.x = screenBounds.x + ((screenBounds.width - d.width) / 2);
                p.y = screenBounds.y + ((screenBounds.height - d.height) / 2);
            }
            dialog.setLocation(p);
        }
    }

    public Way getSelectedWay() {
        return selectedWay;
    }

    public void setSelectedWay(Way selectedWay) {
        this.selectedWay = selectedWay;
    }

    @SuppressWarnings("serial")
    class AddModifyAction extends AbstractAction {

        public AddModifyAction() {
            putValue(Action.NAME, tr(ADD) + "/" + tr(EDIT));
            ImageIcon addModifyIcon = ImageProvider.get("", "addnode");
            if (addModifyIcon != null) {
                putValue(Action.SMALL_ICON, addModifyIcon);
            }
        }

        public void actionPerformed(ActionEvent arg0) {
            if (selectedWay != null && waysInsideSelectedArea != null && !waysInsideSelectedArea.isEmpty()) {
                if (tabPanel.getSelectedIndex() == 0) {
                    for (Way way : waysInsideSelectedArea) {
                        if (!"".equals(tagNameAM.getText()) && !"".equals(tagValueAM.getText())) {
                            way.put(tagNameAM.getText(), tagValueAM.getText());
                            way.save();
                        }
                    }
                }
            }
            setVisible(false);
        }
    }

    @SuppressWarnings("serial")
    class DeleteAction extends AbstractAction {

        public DeleteAction() {
            putValue(Action.NAME, tr(DELETE));
            ImageIcon deleteIcon = ImageProvider.get("", "purge");
            if (deleteIcon != null) {
                putValue(Action.SMALL_ICON, deleteIcon);
            }
        }

        public void actionPerformed(ActionEvent arg0) {
            if (selectedWay != null && waysInsideSelectedArea != null && !waysInsideSelectedArea.isEmpty()) {
                if (tabPanel.getSelectedIndex() == 1) {
                    for (Way way : waysInsideSelectedArea) {
                        if (!"".equals(tagNameD.getText())) {
                            way.remove(tagNameD.getText());
                            way.save();
                        }
                    }
                }
            }
            setVisible(false);
        }
    }

    @SuppressWarnings("serial")
    class CancelAction extends AbstractAction {

        public CancelAction() {
            putValue(Action.NAME, tr("Cancel"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
            ImageIcon cancelIcon = ImageProvider.get("", "cancel");
            if (cancelIcon != null) {
                putValue(Action.SMALL_ICON, cancelIcon);
            }
        }

        public void actionPerformed(ActionEvent arg0) {
            setVisible(false);
        }
    }

    @SuppressWarnings("serial")
    class SaveAction extends AbstractAction {

        public SaveAction() {
            putValue(Action.NAME, tr("Save"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
            ImageIcon saveIcon = ImageProvider.get("", "save");
            if (saveIcon != null) {
                putValue(Action.SMALL_ICON, saveIcon);
            }
        }

        public void actionPerformed(ActionEvent arg0) {
            if (selectedWay != null && waysInsideSelectedArea != null && !waysInsideSelectedArea.isEmpty()) {
                String countryName = (String) country.getSelectedItem();
                Country countryItem = null;
                if (CountryDataMemory.getCountryCache().containsKey(countryName)) {
                    countryItem = CountryDataMemory.getCountryCache().get(countryName);
                }
                for (Way way : waysInsideSelectedArea) {
                    if (tabPanel.getSelectedIndex() == 2) {
                        if (way.get("building") != null && !buildings.isSelected()) {
                            continue;
                        }
                        way.put("country", countryName);
                        if (countryItem != null) {
                            way.put("level1", level1.getText());
                            way.put("level2", level2.getText());
                            way.put("level3", level3.getText());
                            way.put("level4", level4.getText());
                            way.put("level5", level5.getText());
                            way.put("level6", level6.getText());
                        }
                    }
                    way.save();
                }
            }
            setVisible(false);
        }
    }

    public List<Way> getWaysInsideSelectedArea() {
        return waysInsideSelectedArea;
    }

    public void setWaysInsideSelectedArea(List<Way> waysInsideSelectedArea) {
        this.waysInsideSelectedArea = waysInsideSelectedArea;
    }

}
