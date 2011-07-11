/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstreetmap.josm.plugins.fastdraw;

import javax.swing.JOptionPane;
import java.text.NumberFormat;
import java.text.ParseException;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.ExtendedDialog;
import javax.swing.GroupLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import static org.openstreetmap.josm.tools.I18n.tr;

public class FastDrawConfigDialog extends ExtendedDialog {

    public FastDrawConfigDialog(FDSettings settings) {
        super(Main.parent,tr("FastDraw configuration"),new String[] {tr("Ok"), tr("Cancel")});
        JPanel all = new JPanel();
        GroupLayout layout = new GroupLayout(all);
        all.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        JLabel label1=new JLabel(tr("Epsilon multiplier"));
        JLabel label2=new JLabel(tr("Starting Epsilon"));
        JLabel label3=new JLabel(tr("Max points count per 1 km"));
        JFormattedTextField text1=new JFormattedTextField(NumberFormat.getInstance());
        JFormattedTextField text2=new  JFormattedTextField(NumberFormat.getInstance());
        JFormattedTextField text3=new  JFormattedTextField(NumberFormat.getInstance());
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(label1)
                    .addComponent(label2)
                    .addComponent(label3))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(text1)
                    .addComponent(text2)
                    .addComponent(text3)
                )
                );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(label1)
                    .addComponent(text1))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(label2)
                    .addComponent(text2))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(label3)
                    .addComponent(text3))
                );
        
        text1.setValue(settings.epsilonMult);
        text2.setValue(settings.startingEps);
        text3.setValue(settings.maxPointsPerKm);
        
        ExtendedDialog dialog = new ExtendedDialog(Main.parent,
                tr("FastDraw settings"),
                new String[] {tr("Ok"), tr("Cancel")}
        );
        setContent(all, false);
        setButtonIcons(new String[] {"ok.png", "cancel.png"});
        setToolTipTexts(new String[] {
                tr("Save settings"),
                tr("Cancel")
        });
        setDefaultButton(1);
        //configureContextsensitiveHelp("/Action/DownloadObject", true /* show help button */);
        showDialog();
        if (dialog.getValue() == 0) {
            try {
            settings.epsilonMult=NumberFormat.getInstance().parse(text1.getText()).doubleValue();
            settings.startingEps=NumberFormat.getInstance().parse(text2.getText()).doubleValue();
            settings.maxPointsPerKm=NumberFormat.getInstance().parse(text3.getText()).doubleValue();
            settings.savePrefs();
            } catch (ParseException e) {
              JOptionPane.showMessageDialog(Main.parent,
                  tr("Can not read settings"));
            }
        }
            
    }
    
}
