// License: GPL. For details, see LICENSE file.

package pdfimport;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class GuiPanel extends JPanel{

    public GuiPanel() {
        super();
    }

    public GuiPanel(LayoutManager layout) {
        super(layout);
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (Component c : this.getComponents()) {
            c.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }
}
