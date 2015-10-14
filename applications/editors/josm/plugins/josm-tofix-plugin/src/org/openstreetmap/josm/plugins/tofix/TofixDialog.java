package org.openstreetmap.josm.plugins.tofix;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import javax.swing.AbstractAction;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.JosmUserIdentityManager;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.tofix.bean.AccessToTask;
import org.openstreetmap.josm.plugins.tofix.bean.FixedBean;
import org.openstreetmap.josm.plugins.tofix.bean.ListTaskBean;
import org.openstreetmap.josm.plugins.tofix.bean.TrackBean;
import org.openstreetmap.josm.plugins.tofix.bean.items.Item;
import org.openstreetmap.josm.plugins.tofix.controller.ItemController;
import org.openstreetmap.josm.plugins.tofix.controller.ItemTrackController;
import org.openstreetmap.josm.plugins.tofix.controller.ListTaskController;
import org.openstreetmap.josm.plugins.tofix.util.*;
import org.openstreetmap.josm.plugins.tofix.util.Config;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.OpenBrowser;
import org.openstreetmap.josm.tools.Shortcut;

/**
 *
 * @author ruben
 */
public class TofixDialog extends ToggleDialog implements ActionListener {

    // private final SideButton editButton;
    private final SideButton skipButton;
    private final SideButton fixedButton;
    private final SideButton noterrorButton;
    private Shortcut skipShortcut = null;
    private Shortcut fixedShortcut = null;
    private Shortcut noterrorButtonShortcut = null;
    JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 5, 1);

    //size to download
    double zise = 0.001; //per default

    AccessToTask mainAccessToTask = null;
    // Task list
    ListTaskBean listTaskBean = null;
    ListTaskController listTaskController = new ListTaskController();

    //Item
    Item item = new Item();
    ItemController itemController = new ItemController();

    // To-Fix layer
    MapView mv = Main.map.mapView;

    ItemTrackController itemTrackController = new ItemTrackController();

    JTabbedPane TabbedPanel = new javax.swing.JTabbedPane();

    JPanel jcontenTasks = new JPanel(new GridLayout(2, 1));
    JPanel valuePanel = new JPanel(new GridLayout(1, 1));

    JPanel jcontenConfig = new JPanel(new GridLayout(2, 1));
    JPanel panelslide = new JPanel(new GridLayout(1, 1));

    JosmUserIdentityManager josmUserIdentityManager = JosmUserIdentityManager.getInstance();

    TofixTask tofixTask = new TofixTask();
    UploadAction upload = new UploadAction();

    public TofixDialog() {

        super(tr("To-fix"), "icontofix", tr("Open to-fix window."),
                Shortcut.registerShortcut("Tool:To-fix", tr("Toggle: {0}", tr("Tool:To-fix")),
                        KeyEvent.VK_T, Shortcut.ALT_CTRL_SHIFT), 170);

        // "Skip" button
        skipButton = new SideButton(new AbstractAction() {
            {
                putValue(NAME, tr("Skip"));
                putValue(SMALL_ICON, ImageProvider.get("mapmode", "skip.png"));
                putValue(SHORT_DESCRIPTION, tr("Skip Error"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                skip();
            }
        });
        skipButton.setEnabled(false);

        // "Fixed" button
        fixedButton = new SideButton(new AbstractAction() {
            {
                putValue(NAME, tr("Fixed"));
                putValue(SMALL_ICON, ImageProvider.get("mapmode", "fixed.png"));
                putValue(SHORT_DESCRIPTION, tr("Fixed Error"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                upload.setCustomized_comment("#to-fix:" + mainAccessToTask.getTask_id());
                upload.actionPerformed(e);
                fixed();
            }
        });

        fixedButton.setEnabled(false);

        // "Not a error" button
        noterrorButton = new SideButton(new AbstractAction() {
            {
                putValue(NAME, tr("Not an error"));
                putValue(SMALL_ICON, ImageProvider.get("mapmode", "noterror.png"));
                putValue(SHORT_DESCRIPTION, tr("Not an error"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                noterror();
            }
        });

        noterrorButton.setEnabled(false);

        //add tittle for To-fix task
        JLabel title_tasks = new javax.swing.JLabel();
        title_tasks.setText("<html><a href=\"\">List of tasks</a></html>");
        title_tasks.setCursor(new Cursor(Cursor.HAND_CURSOR));
        title_tasks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                OpenBrowser.displayUrl(Config.URL_TOFIX);
            }
        });
        jcontenTasks.add(title_tasks);

        // JComboBox for each task
        ArrayList<String> tasksList = new ArrayList<String>();
        tasksList.add("Select a task ...");

        if (Status.isInternetReachable()) { //checkout  internet connection
            listTaskBean = listTaskController.getListTasksBean();
            for (int i = 0; i < listTaskBean.getTasks().size(); i++) {
                tasksList.add(listTaskBean.getTasks().get(i).getTitle());
            }
            JComboBox jcomboBox = new JComboBox(tasksList.toArray());
            valuePanel.add(jcomboBox);
            jcomboBox.addActionListener(this);

            jcontenTasks.add(valuePanel);

            //add title to download
            jcontenConfig.add(new Label(tr("Set download area (m²)")));

            //Add Slider to download
            slider.setMinorTickSpacing(2);
            slider.setMajorTickSpacing(5);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            //slider.setLabelTable((slider.createStandardLabels(1)));
            Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
            table.put(1, new JLabel(tr("~.02")));
            table.put(3, new JLabel("~.20"));
            table.put(5, new JLabel("~.40"));
            slider.setLabelTable(table);

            slider.addChangeListener(new javax.swing.event.ChangeListener() {
                public void stateChanged(javax.swing.event.ChangeEvent evt) {
                    zise = (slider.getValue() * 0.001);
                }
            });
            panelslide.add(slider);
            jcontenConfig.add(panelslide);

            //PANEL TASKS
            valuePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
            //jcontenTasks.setBorder(javax.swing.BorderFactory.createEtchedBorder());
            panelslide.setBorder(javax.swing.BorderFactory.createEtchedBorder());

            TabbedPanel.addTab("Tasks", jcontenTasks);
            TabbedPanel.addTab("Config", jcontenConfig);

            //add panels in JOSM
            createLayout(TabbedPanel, false, Arrays.asList(new SideButton[]{
                skipButton, noterrorButton, fixedButton
            }));

            if (!Status.server()) {
                jcomboBox.setEnabled(false);
                skipButton.setEnabled(false);
                fixedButton.setEnabled(false);
                noterrorButton.setEnabled(false);
            } else {
                // Request data
                mainAccessToTask = new AccessToTask("mixedlayer", "keepright", false);//start mixedlayer task by default
                //Shortcuts
                skipShortcut = Shortcut.registerShortcut("tofix:skip", tr("tofix:Skip item"), KeyEvent.VK_S, Shortcut.ALT_SHIFT);
                Main.registerActionShortcut(new Skip_key_Action(), skipShortcut);

                fixedShortcut = Shortcut.registerShortcut("tofix:fixed", tr("tofix:Fixed item"), KeyEvent.VK_F, Shortcut.ALT_SHIFT);
                Main.registerActionShortcut(new Fixed_key_Action(), fixedShortcut);

                noterrorButtonShortcut = Shortcut.registerShortcut("tofix:noterror", tr("tofix:Not a Error item"), KeyEvent.VK_N, Shortcut.ALT_SHIFT);
                Main.registerActionShortcut(new NotError_key_Action(), noterrorButtonShortcut);
            }
        }
    }

    public class Skip_key_Action extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            skip();
        }
    }

    public class Fixed_key_Action extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            upload.actionPerformed(e);
            fixed();
        }
    }

    public class NotError_key_Action extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            noterror();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox) e.getSource();
        if (cb.getSelectedIndex() != 0) {
            mainAccessToTask.setTask_name(listTaskBean.getTasks().get(cb.getSelectedIndex() - 1).getTitle());
            mainAccessToTask.setTask_id(listTaskBean.getTasks().get(cb.getSelectedIndex() - 1).getId());
            mainAccessToTask.setTask_source(listTaskBean.getTasks().get(cb.getSelectedIndex() - 1).getSource());
            get_new_item();
            skipButton.setEnabled(true);
            fixedButton.setEnabled(true);
            noterrorButton.setEnabled(true);
        } else {
            skipButton.setEnabled(false);
            fixedButton.setEnabled(false);
            noterrorButton.setEnabled(false);
        }
    }

    public void edit() {
        if (mainAccessToTask.isAccess()) {
            TrackBean trackBean = new TrackBean();
            trackBean.getAttributes().setAction("edit");
            trackBean.getAttributes().setEditor("josm");
            trackBean.getAttributes().setUser(josmUserIdentityManager.getUserName());
            trackBean.getAttributes().setKey(mainAccessToTask.getKey());
            itemTrackController.send_track_edit(mainAccessToTask.getTrack_url(), trackBean);
        }
    }

    public void skip() {
        if (mainAccessToTask.isAccess()) {
            TrackBean trackBean = new TrackBean();
            trackBean.getAttributes().setAction("skip");
            trackBean.getAttributes().setEditor("josm");
            trackBean.getAttributes().setUser(josmUserIdentityManager.getUserName());
            trackBean.getAttributes().setKey(mainAccessToTask.getKey());
            itemTrackController.send_track_skip(mainAccessToTask.getTrack_url(), trackBean);
        }
        get_new_item();
    }

    public void fixed() {
        if (mainAccessToTask.isAccess()) {
            FixedBean fixedBean = new FixedBean();
            fixedBean.setUser(josmUserIdentityManager.getUserName());
            fixedBean.setKey(mainAccessToTask.getKey());
            itemTrackController.send_track_fix(mainAccessToTask.getFixed_url(), fixedBean);
        }
        get_new_item();
    }

    public void noterror() {
        if (mainAccessToTask.isAccess()) {

            FixedBean NoterrorBean = new FixedBean();
            NoterrorBean.setUser(josmUserIdentityManager.getUserName());
            NoterrorBean.setKey(mainAccessToTask.getKey());
            itemTrackController.send_track_noterror(mainAccessToTask.getNoterror_url(), NoterrorBean);

        }
        get_new_item();
    }

    private void get_new_item() {
        item.setStatus(0);
        itemController.setAccessToTask(mainAccessToTask);
        item = itemController.getItem();
        switch (item.getStatus()) {
            case 200:
                mainAccessToTask.setAccess(true);
                mainAccessToTask = tofixTask.work(item, mainAccessToTask, zise);
                edit();
                break;
            case 410:
                mainAccessToTask.setAccess(false);
                tofixTask.task_complete(item, mainAccessToTask);
                break;
            case 503:
                mainAccessToTask.setAccess(false);
                JOptionPane.showMessageDialog(Main.panel, tr("Maintenance server"), tr("Warning"), JOptionPane.WARNING_MESSAGE);
                break;
            case 520:
                mainAccessToTask.setAccess(false);
                JLabel text = new javax.swing.JLabel();
                text.setText(tr("<html>Something went wrong, please update the plugin or report an issue at <a href=\"\">josm-tofix-plugin/issues</a></html>"));
                text.setCursor(new Cursor(Cursor.HAND_CURSOR));
                text.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        OpenBrowser.displayUrl(Config.URL_TOFIX_ISSUES);
                    }
                });
                JOptionPane.showMessageDialog(Main.panel, text, tr("Warning"), JOptionPane.WARNING_MESSAGE);
                break;
            default:
                mainAccessToTask.setAccess(false);
                JOptionPane.showMessageDialog(Main.panel, tr("Something went wrong, try again"), tr("Warning"), JOptionPane.WARNING_MESSAGE);
        }
    }

}
