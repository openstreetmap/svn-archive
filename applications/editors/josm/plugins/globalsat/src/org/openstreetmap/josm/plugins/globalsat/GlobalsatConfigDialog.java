// License: GPL v2 or later (v3 preferred). Copyright 2008 by Raphael Mack

//TODO: check input for numbers in text fields
package org.openstreetmap.josm.plugins.globalsat;

import static org.openstreetmap.josm.tools.I18n.tr;
import gnu.io.CommPortIdentifier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.kaintoch.gps.globalsat.dg100.Dg100Config;
import org.openstreetmap.josm.Main;


/**
 * Configuration download dialog.
 *
 * @author Raphael Mack <ramack@raphael-mack.de>
 *
 */
public class GlobalsatConfigDialog extends JPanel {


    public class IntegerTextField extends JTextField {

        final static String badchars = "-`~!@#$%^&*()_+=\\|\"':;?/>.<, ";

        public void processKeyEvent(KeyEvent ev) {

            char c = ev.getKeyChar();
            if((Character.isLetter(c) && !ev.isAltDown())
               || badchars.indexOf(c) > -1) {
                ev.consume();
                return;
            }
            super.processKeyEvent(ev);
        }
    }



    // the JOptionPane that contains this dialog. required for the closeDialog() method.
    private JOptionPane optionPane;
    private JCheckBox delete;
    private JComboBox portCombo;
    private JRadioButton formatPosOnly = new JRadioButton(tr("Position only"));
    private JRadioButton formatPosTDS = new JRadioButton(tr("Position, Time, Date, Speed"));
    private JRadioButton formatPosTDSA = new JRadioButton(tr("Position, Time, Date, Speed, Altitude"));

    private JRadioButton aTime = new JRadioButton(tr("A By Time"));
    private JRadioButton aDist = new JRadioButton(tr("A By Distance"));
    private JRadioButton bTime = new JRadioButton(tr("B By Time"));
    private JRadioButton bDist = new JRadioButton(tr("B By Distance"));
    private JRadioButton cTime = new JRadioButton(tr("C By Time"));
    private JRadioButton cDist = new JRadioButton(tr("C By Distance"));

    private JTextField aSeconds = new IntegerTextField();
    private JTextField aMeters = new IntegerTextField();
    private JTextField bSeconds = new IntegerTextField();
    private JTextField bMeters = new IntegerTextField();
    private JTextField cSeconds = new IntegerTextField();
    private JTextField cMeters = new IntegerTextField();

    private JLabel memUsage = new JLabel();

    private JCheckBox disableLogDist, disableLogSpeed;
    private JTextField minLogDist, minLogSpeed;

    private List<CommPortIdentifier> ports = new LinkedList<CommPortIdentifier>();

    private Dg100Config conf;


    public GlobalsatConfigDialog(Dg100Config config) {
        conf = config;
        GridBagConstraints c = new GridBagConstraints();
        System.out.println("read config " + config);

        Dimension xx = aSeconds.getPreferredSize();
        aSeconds.setPreferredSize(new Dimension((int)xx.getWidth() + 50, (int)xx.getHeight()));
        aSeconds.setHorizontalAlignment(JTextField.RIGHT);
        aMeters.setPreferredSize(new Dimension((int)xx.getWidth() + 50, (int)xx.getHeight()));
        aMeters.setHorizontalAlignment(JTextField.RIGHT);
        bSeconds.setPreferredSize(new Dimension((int)xx.getWidth() + 50, (int)xx.getHeight()));
        bSeconds.setHorizontalAlignment(JTextField.RIGHT);
        bMeters.setPreferredSize(new Dimension((int)xx.getWidth() + 50, (int)xx.getHeight()));
        bMeters.setHorizontalAlignment(JTextField.RIGHT);
        cSeconds.setPreferredSize(new Dimension((int)xx.getWidth() + 50, (int)xx.getHeight()));
        cSeconds.setHorizontalAlignment(JTextField.RIGHT);
        cMeters.setPreferredSize(new Dimension((int)xx.getWidth() + 50, (int)xx.getHeight()));
        cMeters.setHorizontalAlignment(JTextField.RIGHT);

        setLayout(new GridBagLayout());

        ButtonGroup logFormat = new ButtonGroup();
        logFormat.add(formatPosOnly);
        logFormat.add(formatPosTDS);
        logFormat.add(formatPosTDSA);

        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BoxLayout(logPanel, BoxLayout.PAGE_AXIS));
        logPanel.add(new JLabel(tr("Data Logging Format")));
        logPanel.add(formatPosOnly);
        logPanel.add(formatPosTDS);
        logPanel.add(formatPosTDSA);

        c.insets = new Insets(4,4,0,4);
        c.gridwidth = 1;
        c.weightx = 1.8;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        add(logPanel);

        disableLogSpeed = new JCheckBox(tr("Disable data logging if speed falls below"));
        c.insets = new Insets(4,4,0,4);
        c.gridwidth = 1;
        c.weightx = 0.8;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy = 1;
        add(disableLogSpeed, c);

        minLogSpeed = new IntegerTextField();
        minLogSpeed.setHorizontalAlignment(JTextField.RIGHT);
        c.insets = new Insets(4,4,0,4);
        c.gridwidth = 1;
        c.weightx = 1.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        add(minLogSpeed, c);

        disableLogSpeed.addActionListener(new ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    minLogSpeed.setEnabled(disableLogSpeed.isSelected());
                }
            });

        disableLogDist = new JCheckBox(tr("Disable data logging if distance falls below"));
        c.insets = new Insets(0,4,4,4);
        c.gridwidth = 1;
        c.weightx = 0.8;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 2;
        add(disableLogDist, c);

        minLogDist = new IntegerTextField();
        minLogDist.setHorizontalAlignment(JTextField.RIGHT);
        c.insets = new Insets(0,4,4,4);
        c.gridwidth = 1;
        c.weightx = 1.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        add(minLogDist, c);
        Dimension x = minLogDist.getPreferredSize();
        minLogDist.setPreferredSize(new Dimension((int)x.getWidth() + 50, (int)x.getHeight()));

        disableLogDist.addActionListener(new ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    minLogDist.setEnabled(disableLogDist.isSelected());
                }
            });

        disableLogDist.setSelected(conf.getDisableLogDist());
        disableLogSpeed.setSelected(conf.getDisableLogSpeed());
        minLogDist.setEnabled(disableLogDist.isSelected());
        minLogSpeed.setEnabled(disableLogSpeed.isSelected());

        minLogSpeed.setText("" + conf.getSpeedThres());
        minLogDist.setText("" + conf.getDistThres());

        ButtonGroup group = new ButtonGroup();
        group.add(aTime);
        group.add(aDist);

        c.insets = new Insets(4,4,0,4);
        c.gridwidth = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 3;
        add(aTime, c);
        c.insets = new Insets(0,4,4,4);
        c.gridy = 4;
        add(aDist, c);

        c.insets = new Insets(4,4,0,4);
        c.gridwidth = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 3;
        add(aSeconds, c);
        c.insets = new Insets(0,4,4,4);
        c.gridy = 4;
        add(aMeters, c);

        group = new ButtonGroup();
        group.add(bTime);
        group.add(bDist);

        c.insets = new Insets(4,4,0,4);
        c.gridwidth = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        add(bTime, c);
        c.insets = new Insets(0,4,4,4);
        c.gridy = 6;
        add(bDist, c);

        c.insets = new Insets(4,4,0,4);
        c.gridwidth = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 5;
        add(bSeconds, c);
        c.insets = new Insets(0,4,4,4);
        c.gridy = 6;
        add(bMeters, c);

        group = new ButtonGroup();
        group.add(cTime);
        group.add(cDist);

        c.insets = new Insets(4,4,0,4);
        c.gridwidth = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 7;
        add(cTime, c);
        c.insets = new Insets(0,4,4,4);
        c.gridy = 8;
        add(cDist, c);

        c.insets = new Insets(4,4,0,4);
        c.gridwidth = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 7;
        add(cSeconds, c);
        c.insets = new Insets(0,4,4,4);
        c.gridy = 8;
        add(cMeters, c);

        //        add(new JLabel(tr("Memory Usage:")));
        //        add(memUsage);
        //add ID textbox



        switch(conf.getLogFormat()){
        case 0:
            formatPosOnly.setSelected(true);
            break;
        case 1:
            formatPosTDS.setSelected(true);
            break;
        case 2:
            formatPosTDSA.setSelected(true);
            break;
        default:
            JOptionPane.showMessageDialog(Main.parent, tr("Unknown logFormat"));
        }

        if(conf.getSwATimeOrDist() == 0){
            aTime.setSelected(true);
            aDist.setSelected(false);
        }else{
            aTime.setSelected(false);
            aDist.setSelected(true);
        }

        if(conf.getSwBTimeOrDist() == 0){
            bTime.setSelected(true);
            bDist.setSelected(false);
        }else{
            bTime.setSelected(false);
            bDist.setSelected(true);
        }

        if(conf.getSwCTimeOrDist() == 0){
            cTime.setSelected(true);
            cDist.setSelected(false);
        }else{
            cTime.setSelected(false);
            cDist.setSelected(true);
        }

        aSeconds.setText("" + conf.getSwATime() / 1000);
        aMeters.setText("" + conf.getSwADist());

        bSeconds.setText("" + conf.getSwBTime() / 1000);
        bMeters.setText("" + conf.getSwBDist());

        cSeconds.setText("" + conf.getSwCTime() / 1000);
        cMeters.setText("" + conf.getSwCDist());

    }

    /**
     * Has to be called after this dialog has been added to a JOptionPane.
     * @param optionPane
     */
    public void setOptionPane(JOptionPane optionPane) {
        this.optionPane = optionPane;
    }

    /**
     * Get the selected configuration.
     */
    public Dg100Config getConfig(){
        conf.setDisableLogDist(disableLogDist.isSelected());
        conf.setDisableLogSpeed(disableLogSpeed.isSelected());
        conf.setDistThres(Integer.parseInt(minLogDist.getText()));
        conf.setSpeedThres(Integer.parseInt(minLogSpeed.getText()));

        if(formatPosOnly.isSelected()){
            conf.setLogFormat((byte)0);
        }else if(formatPosTDS.isSelected()){
            conf.setLogFormat((byte)1);
        }else if(formatPosTDSA.isSelected()){
            conf.setLogFormat((byte)2);
        }

        conf.setSwATimeOrDist((byte)(aDist.isSelected() ? 1 : 0));
        conf.setSwBTimeOrDist((byte)(bDist.isSelected() ? 1 : 0));
        conf.setSwCTimeOrDist((byte)(cDist.isSelected() ? 1 : 0));

        conf.setSwATime(Integer.parseInt(aSeconds.getText()) * 1000);
        conf.setSwADist(Integer.parseInt(aMeters.getText()));
        conf.setSwBTime(Integer.parseInt(bSeconds.getText()) * 1000);
        conf.setSwBDist(Integer.parseInt(bMeters.getText()));
        conf.setSwCTime(Integer.parseInt(cSeconds.getText()) * 1000);
        conf.setSwCDist(Integer.parseInt(cMeters.getText()));
        return conf;
    }
}
