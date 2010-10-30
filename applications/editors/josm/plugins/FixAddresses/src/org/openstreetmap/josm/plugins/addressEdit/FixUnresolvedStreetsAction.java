package org.openstreetmap.josm.plugins.addressEdit;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditDialog;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Action to find and fix addresses without (valid) streets.
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */

public class FixUnresolvedStreetsAction extends JosmAction implements SelectionChangedListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AddressEditContainer addressEditContainer;
	private Collection<? extends OsmPrimitive> newSelection;

	public FixUnresolvedStreetsAction() {
		super(tr("Fix street addresses"), "fixstreets_24",
				tr("Find and fix addresses without (valid) streets."), Shortcut
				.registerShortcut("tools:AddressEdit", tr("Tool: {0}",
						tr("Address Edit")), KeyEvent.VK_A,
						Shortcut.GROUP_MENU, InputEvent.ALT_DOWN_MASK
						| InputEvent.SHIFT_DOWN_MASK), false);
		setEnabled(false);
		
		addressEditContainer = new AddressEditContainer();
		DataSet.addSelectionListener(this);		
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.data.SelectionChangedListener#selectionChanged(java.util.Collection)
	 */
	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
		/* this changes the dialog contents, so we do nothing here
		this.newSelection = newSelection;
		if (addressEditContainer != null) {
			addressEditContainer.invalidate(newSelection);
		}*/
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {		
		if (addressEditContainer != null) {
			addressEditContainer.attachToDataSet(newSelection);
			try {
				AddressEditDialog dlg = new AddressEditDialog(addressEditContainer);
				dlg.setVisible(true);
			} finally {
				addressEditContainer.detachFromDataSet();
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.actions.JosmAction#updateEnabledState()
	 */
	@Override
	protected void updateEnabledState() {
		setEnabled(getCurrentDataSet() != null);
	}

	@Override
	protected void updateEnabledState(
			Collection<? extends OsmPrimitive> selection) {
		// Enable button if there is either a selection or at least a data set
		setEnabled(	selection != null && !selection.isEmpty() || 
					(getCurrentDataSet() != null));
	}

	/* This code is abused to generate tag utility code */

	@SuppressWarnings("unused")
	private void generateTagCode(AddressEditContainer addrVisitor) {
		
		for (String tag : addrVisitor.getTags()) {
			String methodName = createMethodName(tag);
			System.out
			.println(String
					.format(
							"/** Check if OSM primitive has a tag '%s'.\n * @param osmPrimitive The OSM entity to check.*/\npublic static boolean has%sTag(OsmPrimitive osmPrimitive) {\n return osmPrimitive != null ? osmPrimitive.hasKey(%s_TAG) : false;\n}\n",
							tag, methodName, tag.toUpperCase()
							.replaceAll(":", "_")));
			System.out
			.println(String
					.format(
							"/** Gets the value of tag '%s'.\n * @param osmPrimitive The OSM entity to check.*/\npublic static String get%sValue(OsmPrimitive osmPrimitive) {\n return osmPrimitive != null ? osmPrimitive.get(%s_TAG) : null;\n}\n",
							tag, methodName, tag.toUpperCase()
							.replaceAll(":", "_")));
		}

		for (String tag : addrVisitor.getTags()) {
			System.out.println(String.format(
					"public static final String %s_TAG = \"%s\";", tag
					.toUpperCase().replaceAll(":", "_"), tag));
		}
	}

	private String createMethodName(String osmName) {
		StringBuffer result = new StringBuffer(osmName.length());
		boolean nextUp = false;
		for (int i = 0; i < osmName.length(); i++) {
			char c = osmName.charAt(i);
			if (i == 0) {
				result.append(Character.toUpperCase(c));
				continue;
			}
			if (c == '_' || c == ':') {
				nextUp = true;
			} else {
				if (nextUp) {
					result.append(Character.toUpperCase(c));
					nextUp = false;
				} else {
					result.append(c);
				}
			}
		}

		return result.toString();
	}
}
