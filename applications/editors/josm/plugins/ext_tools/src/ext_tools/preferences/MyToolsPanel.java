package ext_tools.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ext_tools.ExtTool;
import ext_tools.ToolsInformation;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Utils;

public class MyToolsPanel extends JPanel {
	private final transient ToolsInformation tools;

    public MyToolsPanel(ToolsInformation tools) {
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
            cbTool.addActionListener(e -> tool.setEnabled(cbTool.isSelected()));
            add(cbTool, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0;
            gbc.anchor = GridBagConstraints.EAST;

            final JButton bEdit = new JButton(tr("Edit"));
            bEdit.addActionListener(e -> {
			    JDialog dlg = new EditToolDialog(tool);
			    dlg.setVisible(true);
			    dlg.dispose();
			    refresh();
			});
            add(bEdit, gbc);

            gbc.gridx = 2;
            final JButton bDel = new JButton("X");
            bDel.addActionListener(e -> {
			    if (JOptionPane.showConfirmDialog(MainApplication.getMainFrame(),
			            tr("Delete tool \"{0}\"?", tool.name),
			            tr("Are you sure?"),
			            JOptionPane.YES_NO_OPTION,
			            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
			    {
			        tools.removeTool(tool);
			        refresh();
			    }
			});
            add(bDel, gbc);

            gbc.gridy++;
        }
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        final JButton bNew = new JButton(tr("New tool..."));
        bNew.addActionListener(e -> {
		    ExtTool tool = new ExtTool();
		    JDialog dlg = new EditToolDialog(tool);
		    dlg.setVisible(true);
		    dlg.dispose();
		    if (!Utils.isStripEmpty(tool.name)) {
		        tools.addTool(tool);
		        tool.setEnabled(true);
		    }
		    refresh();
		});
        add(bNew, gbc);
        gbc.gridy++;

        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JPanel(), gbc);
        revalidate();
        repaint();
    }

}
