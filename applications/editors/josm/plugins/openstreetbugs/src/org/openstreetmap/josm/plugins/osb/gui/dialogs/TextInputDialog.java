package org.openstreetmap.josm.plugins.osb.gui.dialogs;
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

import org.openstreetmap.josm.plugins.osb.gui.historycombobox.HistoryChangedListener;
import org.openstreetmap.josm.plugins.osb.gui.historycombobox.SuggestingJHistoryComboBox;

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
    private SuggestingJHistoryComboBox input;
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
        BorderLayout thisLayout = new BorderLayout();
        getContentPane().setLayout(thisLayout);
        thisLayout.setHgap(5);
        thisLayout.setVgap(5);
        {
            pnlMain = new JPanel();
            GridBagLayout pnlMainLayout = new GridBagLayout();
            pnlMainLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
            pnlMainLayout.rowHeights = new int[] {7, 7, 7};
            pnlMainLayout.columnWeights = new double[] {0.1, 0.1};
            pnlMainLayout.columnWidths = new int[] {7, 7};
            getContentPane().add(pnlMain, BorderLayout.CENTER);
            pnlMain.setLayout(pnlMainLayout);
            pnlMain.setPreferredSize(new java.awt.Dimension(487, 132));
            {
                lblIcon = new JLabel();
                pnlMain.add(lblIcon, new GridBagConstraints(0, 0, 1, 3, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
                lblIcon.setVerticalAlignment(SwingConstants.TOP);
                lblIcon.setVerticalTextPosition(SwingConstants.TOP);
            }
            {
                lblText = new JLabel();
                pnlMain.add(lblText, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
            }
            {
                input = new SuggestingJHistoryComboBox();
                pnlMain.add(input, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
                input.setSize(503, 22);
            }
            {
                pnlButtons = new JPanel();
                FlowLayout pnlButtonsLayout = new FlowLayout();
                pnlButtonsLayout.setAlignment(FlowLayout.RIGHT);
                pnlMain.add(pnlButtons, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
                pnlButtons.setLayout(pnlButtonsLayout);
                {
                    btnOk = new JButton();
                    pnlButtons.add(btnOk);
                    btnOk.setText("OK");
                    btnOk.setPreferredSize(new java.awt.Dimension(100, 25));
                }
                {
                    btnCancel = new JButton();
                    pnlButtons.add(btnCancel);
                    btnCancel.setText("Cancel");
                    btnCancel.setPreferredSize(new java.awt.Dimension(100, 25));
                }
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
        tid.setSize(new Dimension(500, 180));
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
        input.addHistoryChangedListener(l);
    }
    
    public void setIcon(Icon icon) {
        lblIcon.setIcon(icon);
    }
}