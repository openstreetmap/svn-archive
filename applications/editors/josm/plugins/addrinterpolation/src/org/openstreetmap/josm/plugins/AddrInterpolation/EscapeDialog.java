// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.AddrInterpolation;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

public class EscapeDialog extends JDialog {
    
    public EscapeDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }
    
    @Override
    protected JRootPane createRootPane() {
        ActionListener escapeActionListener = actionEvent -> dispose();
        JRootPane rootPane = new JRootPane();
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        rootPane.registerKeyboardAction(escapeActionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        return rootPane;
    }
}
