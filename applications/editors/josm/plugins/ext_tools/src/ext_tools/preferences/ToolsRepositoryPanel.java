package ext_tools.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ext_tools.ExtTool;
import ext_tools.ToolsInformation;

public class ToolsRepositoryPanel extends JPanel {

    ToolsInformation tools;

    public ToolsRepositoryPanel(ToolsInformation tools) {
        super(new GridBagLayout());
        this.tools = tools;
    }

    public void refresh() {
        removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 5, 2, 5);

        for (final ExtTool tool : tools.getToolsList()) {
            gbc.gridx = 0;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;

            final JCheckBox cbTool = new JCheckBox(tool.getName());
            cbTool.setSelected(tool.isEnabled());
            cbTool.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tool.setEnabled(cbTool.isSelected());
                }
            });
            add(cbTool, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.EAST;

            final JButton bEdit = new JButton(tr("Install"));
            bEdit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    JDialog dlg = new EditToolDialog(tool);
                    dlg.setVisible(true);
                    dlg.dispose();
                    refresh();
                }
            });
            add(bEdit, gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.WEST;

            JLabel lbl = new JLabel(tool.cmdline);
            add(lbl, gbc);

            gbc.gridy++;
        }
        gbc.gridy++;

        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JPanel(), gbc);
        revalidate();
        repaint();
    }

}
