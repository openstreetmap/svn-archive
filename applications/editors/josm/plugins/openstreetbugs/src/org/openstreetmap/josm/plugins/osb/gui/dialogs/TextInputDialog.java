/* Copyright (c) 2008, Henrik Niehaus
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its 
 *    contributors may be used to endorse or promote products derived from this 
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.osb.gui.dialogs;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.openstreetmap.josm.gui.widgets.ComboBoxHistory;
import org.openstreetmap.josm.gui.widgets.HistoryChangedListener;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class TextInputDialog extends JDialog {
    private JLabel lblIcon;
    private JButton btnCancel;
    private JButton btnOk;
    private JPanel pnlButtons;
    private HistoryComboBox input;
    private JLabel lblText;
    private JPanel pnlMain;
    
    private String value = null;
    
    private TextInputDialog() {
        initGUI();
        initListeners();
    }

    private void initListeners() {
        input.getEditor().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });
        
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });
        
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void okPressed() {
        value = input.getText();
        input.addCurrentItemToHistory();
        dispose();
    }

    private void initGUI() {
        getContentPane().setLayout(new BorderLayout());
        {
            lblIcon = new JLabel();
            lblIcon.setVerticalAlignment(SwingConstants.TOP);
            lblIcon.setVerticalTextPosition(SwingConstants.TOP);
            lblIcon.setPreferredSize(new java.awt.Dimension(22, 109));
            lblIcon.setMinimumSize(new java.awt.Dimension(22, 100));
            JPanel pnlIcon = new JPanel(new GridBagLayout());
            pnlIcon.add(lblIcon, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
            getContentPane().add(pnlIcon, BorderLayout.WEST);
        }
        pnlMain = new JPanel();
        GridBagLayout pnlMainLayout = new GridBagLayout();
        getContentPane().add(pnlMain, BorderLayout.CENTER);
        pnlMain.setLayout(pnlMainLayout);
        pnlMain.setPreferredSize(new java.awt.Dimension(487, 132));
        {
            lblText = new JLabel();
            pnlMain.add(lblText, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        }
        {
            input = new HistoryComboBox();
            pnlMain.add(input, new GridBagConstraints(1, 1, 1, 1, 0.9, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 10), 0, 0));
            input.setSize(503, 22);
        }
        {
            pnlButtons = new JPanel();
            FlowLayout pnlButtonsLayout = new FlowLayout();
            pnlButtonsLayout.setAlignment(FlowLayout.RIGHT);
            pnlMain.add(pnlButtons, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            pnlButtons.setLayout(pnlButtonsLayout);
            {
                btnOk = new JButton();
                pnlButtons.add(btnOk);
                btnOk.setText(tr("OK"));
                btnOk.setPreferredSize(new java.awt.Dimension(100, 25));
            }
            {
                btnCancel = new JButton();
                pnlButtons.add(btnCancel);
                btnCancel.setText(tr("Cancel"));
                btnCancel.setPreferredSize(new java.awt.Dimension(100, 25));
            }
        }
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    public static String showDialog(JComponent parent, String title, String text, List<String> history, HistoryChangedListener l) {
        return showDialog(parent, title, text, null, history, l);
    }
    
    /**
     * Opens a text input dialog and returns the entered text
     * @return the entered text or null if the cancel button has been pressed;
     */
    public static String showDialog(JComponent parent, String title, String description, Icon icon, List<String> history, HistoryChangedListener l) {
        TextInputDialog tid = new TextInputDialog();
        tid.setTitle(title);
        tid.setSize(new Dimension(550, 180));
        tid.setDescription(description);
        tid.setHistory(history);
        tid.addHistoryChangedListener(l);
        tid.setModal(true);
        tid.setIcon(icon);

        // center tid on parent comp;
        Point p = new Point(0,0);
        SwingUtilities.convertPointToScreen(p, parent);
        int x = (int) (p.getX() + (double)(parent.getWidth() - tid.getWidth()) / 2);
        int y = (int) (p.getY() +  (double)(parent.getHeight() - tid.getHeight()) / 2);
        tid.setLocation(x, y);
        
        //tid.pack();
        tid.setVisible(true);
        return tid.getValue();
    }

    private String getValue() {
        return this.value;
    }
    
    public void setDescription(String text) {
        lblText.setText(text);
    }
    
    public void setHistory(List<String> history) {
        input.setHistory(history);
        input.setText("");
        value = null;
    }
    
    public void addHistoryChangedListener(HistoryChangedListener l) {
        ((ComboBoxHistory)input.getModel()).addHistoryChangedListener(l);
    }
    
    public void setIcon(Icon icon) {
        lblIcon.setIcon(icon);
    }
}