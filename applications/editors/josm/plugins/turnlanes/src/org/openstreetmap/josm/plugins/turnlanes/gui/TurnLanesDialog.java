package org.openstreetmap.josm.plugins.turnlanes.gui;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.turnlanes.model.ModelContainer;

public class TurnLanesDialog extends ToggleDialog {
	private final Action editAction = new JosmAction(tr("Edit"), "dialogs/edit",
	    tr("Edit turn relations and lane lengths for selected node."), null, true) {
		
		private static final long serialVersionUID = 4114119073563457706L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final CardLayout cl = (CardLayout) body.getLayout();
			cl.show(body, CARD_EDIT);
			editing = true;
		}
	};
	private final Action validateAction = new JosmAction(tr("Validate"), "dialogs/validator",
	    tr("Validate turn- and lane-length-relations for consistency."), null, true) {
		
		private static final long serialVersionUID = 7510740945725851427L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final CardLayout cl = (CardLayout) body.getLayout();
			cl.show(body, CARD_VALIDATE);
			editing = false;
		}
	};
	
	private static final long serialVersionUID = -1998375221636611358L;
	
	private static final String CARD_EDIT = "EDIT";
	private static final String CARD_VALIDATE = "VALIDATE";
	private static final String CARD_ERROR = "ERROR";
	
	private final JPanel body = new JPanel();
	private final JunctionPane junctionPane = new JunctionPane(null);
	private final JLabel error = new JLabel();
	
	private boolean editing = true;
	
	public TurnLanesDialog() {
		super(tr("Turn Lanes"), "turnlanes.png", tr("Edit turn lanes"), null, 200);
		
		DataSet.addSelectionListener(new SelectionChangedListener() {
			@Override
			public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
				final Collection<OsmPrimitive> s = Collections.unmodifiableCollection(newSelection);
				final List<Node> nodes = OsmPrimitive.getFilteredList(s, Node.class);
				final List<Way> ways = OsmPrimitive.getFilteredList(s, Way.class);
				
				if (nodes.isEmpty()) {
					setJunction(null);
					return;
				}
				
				try {
					setJunction(ModelContainer.create(nodes, ways));
				} catch (RuntimeException e) {
					displayError(e);
					return;
				}
			}
		});
		
		final JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 4, 4));
		final ButtonGroup group = new ButtonGroup();
		final JToggleButton editButton = new JToggleButton(editAction);
		final JToggleButton validateButton = new JToggleButton(validateAction);
		group.add(editButton);
		group.add(validateButton);
		buttonPanel.add(editButton);
		buttonPanel.add(validateButton);
		
		body.setLayout(new CardLayout(4, 4));
		
		add(buttonPanel, BorderLayout.SOUTH);
		add(body, BorderLayout.CENTER);
		
		body.add(junctionPane, CARD_EDIT);
		body.add(new ValidationPanel(), CARD_VALIDATE);
		body.add(error, CARD_ERROR);
		
		editButton.doClick();
	}
	
	void displayError(RuntimeException e) {
		if (editing) {
			e.printStackTrace();
			
			error.setText("<html>An error occured while constructing the model."
			    + " Please run the validator to make sure the data is consistent.<br><br>Error: " + e.getMessage()
			    + "</html>");
			
			final CardLayout cl = (CardLayout) body.getLayout();
			cl.show(body, CARD_ERROR);
		}
	}
	
	void setJunction(ModelContainer mc) {
		if (mc != null && editing) {
			junctionPane.setJunction(new GuiContainer(mc));
			final CardLayout cl = (CardLayout) body.getLayout();
			cl.show(body, CARD_EDIT);
		}
	}
}
