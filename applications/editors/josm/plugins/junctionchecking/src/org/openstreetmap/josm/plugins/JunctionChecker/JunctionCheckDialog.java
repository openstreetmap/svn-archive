// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.JunctionChecker;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author  joerg
 */
public class JunctionCheckDialog extends ToggleDialog {

    private final JunctionCheckerPlugin plugin;
    /** Serializable ID */
    private static final long serialVersionUID = 2952292777351992696L;
    private final SideButton checkJunctionButton;
    private final SideButton createDigraphButton;
    private final SideButton searchJunctionButton;
    private final JCheckBox digraphsealcb;
    private final JCheckBox produceRelation;
    private final JCheckBox sccCB;
    private final JSpinner nways;
    private final SpinnerNumberModel smodel;
    private final JLabel nwayslabel;

    public JunctionCheckDialog(JunctionCheckerPlugin junctionCheckerGuiPlugin) {
        super(tr("JunctionChecking"), "junctionchecker", tr("Open the junctionchecking window."),
                Shortcut.registerShortcut("subwindow:junctionchecker", tr("Toggle: {0}", tr("junctions")),
                        KeyEvent.VK_J, Shortcut.ALT_SHIFT), 150);
        plugin = junctionCheckerGuiPlugin;
        //das Digraph Create Panel
        JPanel digraphPanel = new JPanel(new GridLayout(1, 2));
        digraphPanel.setBorder(new TitledBorder(tr("Channel-Digraph creation")));
        digraphsealcb = new JCheckBox(tr("seal Channel Digraph"));
        digraphsealcb.setSelected(false);
        //digraphPanel.add(digraphsealcb);

        sccCB = new JCheckBox(tr("calculate strong connected channels"));
        sccCB.setSelected(true);
        digraphPanel.add(sccCB);

        //das Panel zum Junctionchecken
        JPanel jcPanel = new JPanel(new GridLayout(4, 1));
        jcPanel.setBorder(new TitledBorder(tr("Junctionchecking/junctions searching")));

        //Elemente für Grad-Auswahl der Kreuzung
        JPanel spinnerpanel = new JPanel(new GridLayout(1, 2));
        smodel = new SpinnerNumberModel(3, 1, 20, 1);
        nways = new JSpinner(smodel);
        nwayslabel = new JLabel(tr("order of junction (n):"));
        nwayslabel.setEnabled(false);
        spinnerpanel.add(nwayslabel);
        spinnerpanel.add(nways);

        //Elemente zur OSM-Relationen-Erzeugung
        produceRelation = new JCheckBox(tr("produce OSM-Relations: junction"));
        produceRelation.setToolTipText(tr("if enabled the plugin produces osm-relations from the junction subgraphs"));
        produceRelation.setSelected(true);
        produceRelation.setEnabled(false);
        jcPanel.add(produceRelation);
        jcPanel.add(spinnerpanel);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(digraphPanel);
        centerPanel.add(jcPanel);

        // ButtonPanel
        createDigraphButton = new SideButton(new AbstractAction() {
            {
                putValue(NAME, marktr("Create"));
                putValue(SHORT_DESCRIPTION, tr("create the channel digraph"));
                new ImageProvider("dialogs", "digraphcreation").getResource().attachImageIcon(this, true);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                MainApplication.worker.submit(new DigraphCreationTask(plugin, digraphsealcb.isSelected(), sccCB.isSelected()));
                setActivateJunctionCheckOrSearch(true);
            }
        });
        checkJunctionButton = new SideButton(new AbstractAction() {
            {
                putValue(NAME, marktr("Check "));
                putValue(SHORT_DESCRIPTION, tr("check the subset for junction properties"));
                new ImageProvider("dialogs", "junctioncheck").getResource().attachImageIcon(this, true);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                new PrepareJunctionCheckorSearch(plugin, smodel.getNumber().intValue(), produceRelation.isSelected()).prepareJunctionCheck();
            }
        });
        checkJunctionButton.setEnabled(false);
        searchJunctionButton = new SideButton(new AbstractAction() {
            {
                putValue(NAME, marktr("Search "));
                putValue(SHORT_DESCRIPTION, tr("search for junctions in the channel subset"));
                new ImageProvider("dialogs", "junctionsearch").getResource().attachImageIcon(this, true);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                new PrepareJunctionCheckorSearch(plugin, smodel.getNumber().intValue(), produceRelation.isSelected()).prepareJunctionSearch();
            }
        });
        searchJunctionButton.setEnabled(false);

        createLayout(centerPanel, false, Arrays.asList(new SideButton[] {
            createDigraphButton, checkJunctionButton, searchJunctionButton
        }));
    }

    /**
     * (de)aktiviert Buttons zum JunctionCheck oder Suche
     */
    public void setActivateJunctionCheckOrSearch(boolean activate) {
        checkJunctionButton.setEnabled(activate);
        nways.setEnabled(activate);
        nwayslabel.setEnabled(activate);
        produceRelation.setEnabled(activate);
        searchJunctionButton.setEnabled(activate);
    }

    /**
     * (de)aktiviert Buttons zur Channel Digraph Erstellung
     */
    public void setActivateCreateDigraph(final boolean activate) {
        GuiHelper.runInEDTAndWait(new Runnable() {
            @Override
            public void run() {
                createDigraphButton.setEnabled(activate);
                digraphsealcb.setEnabled(activate);
                sccCB.setEnabled(activate);
            }
        });
    }
}
