package org.openstreetmap.josm.plugins.JunctionChecker;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author  joerg
 */
public class JunctionCheckDialog extends ToggleDialog implements SelectionChangedListener{

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
		JPanel spinnerpanel = new JPanel(new GridLayout(1,2));
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
				putValue(SMALL_ICON, ImageProvider.get("dialogs", "digraphcreation"));
			}
			@Override
			public void actionPerformed(ActionEvent e) {
					DigraphCreationTask dct = new DigraphCreationTask(plugin, digraphsealcb.isSelected(), sccCB.isSelected());
					Main.worker.submit(dct);
					setActivateJunctionCheckOrSearch(true);
			}
		});
		checkJunctionButton = new SideButton(new AbstractAction() {
			{
				putValue(NAME, marktr("Check "));
				putValue(SHORT_DESCRIPTION, tr("check the subust for junction properties"));
				putValue(SMALL_ICON, ImageProvider.get("dialogs", "junctioncheck"));
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				PrepareJunctionCheckorSearch pjc = new PrepareJunctionCheckorSearch(plugin, smodel.getNumber().intValue(), produceRelation.isSelected());
				pjc.prepareJunctionCheck();
			}
		});
		checkJunctionButton.setEnabled(false);
		searchJunctionButton = new SideButton(new AbstractAction() {
			{
				putValue(NAME, marktr("Search "));
				putValue(SHORT_DESCRIPTION, tr("search for junctions in the channel subset"));
				putValue(SMALL_ICON, ImageProvider.get("dialogs", "junctionsearch"));
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				PrepareJunctionCheckorSearch pjc = new PrepareJunctionCheckorSearch(plugin, smodel.getNumber().intValue(), produceRelation.isSelected());
				pjc.prepareJunctionSearch();
			}
		});
		searchJunctionButton.setEnabled(false);

		createLayout(centerPanel, false, Arrays.asList(new SideButton[] {
			createDigraphButton, checkJunctionButton, searchJunctionButton
		}));
	}


	/**
	 * (de)aktiviert Buttons zum JunctionCheck oder Suche
	 * @param activate
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
	 * @param activate
	 */
	public void setActivateCreateDigraph(boolean activate) {
		createDigraphButton.setEnabled(activate);
		digraphsealcb.setEnabled(activate);
		sccCB.setEnabled(activate);
	}

	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {

	}
}
