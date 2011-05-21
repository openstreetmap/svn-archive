// License: GPL. Copyright 2011 by Josh Doe and others
package org.openstreetmap.josm.plugins.conflation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 *
 * @author Josh Doe <josh@joshdoe.com>
 */
public class ConflationOptionsDialog extends JDialog {
    private boolean canceled = false;
    private ConflationOptionsPanel panel;

    public ConflationOptionsDialog(Component parent, List<OsmDataLayer> layers) {
        super(JOptionPane.getFrameForComponent(parent),tr("Conflation Options"), ModalityType.MODELESS);
        getContentPane().setLayout(new BorderLayout());
        panel = new ConflationOptionsPanel(this, layers);
        getContentPane().add(panel, BorderLayout.CENTER);

        pack();

        // make dialog respond to ESCAPE
        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "escape");
        getRootPane().getActionMap().put("escape", new CancelAction());

        // make dialog respond to F1
        // TODO: set help context, whatever that means
        //HelpUtil.setHelpContext(getRootPane(), help);
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

    class CancelAction extends AbstractAction {
        public CancelAction() {
            putValue(NAME, tr("Cancel"));
            putValue(SHORT_DESCRIPTION, tr("Close the dialog, do not perform conflation"));
            putValue(SMALL_ICON, ImageProvider.get("cancel"));
        }

        public void actionPerformed(ActionEvent e) {
            setCanceled(true);
            setVisible(false);
        }
    }

    public ConflationOptionsPanel getPanel() {
        return panel;
    }
}
