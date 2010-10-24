package org.openstreetmap.josm.plugins.addressEdit;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.progress.PleaseWaitProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressMonitor.CancelListener;
import org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditDialog;
import org.openstreetmap.josm.plugins.addressEdit.gui.AddressEditModel;
import org.openstreetmap.josm.tools.Shortcut;

public class AddressEditAction extends JosmAction implements
SelectionChangedListener, CancelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AddressEditModel addressModel;
	private boolean isCanceled = false;

	public AddressEditAction() {
		super(tr("Address Editor"), "addressedit_24",
				tr("Handy Address Editing Functions"), Shortcut
				.registerShortcut("tools:AddressEdit", tr("Tool: {0}",
						tr("Address Edit")), KeyEvent.VK_A,
						Shortcut.GROUP_MENU, InputEvent.ALT_DOWN_MASK
						| InputEvent.SHIFT_DOWN_MASK), false);
		setEnabled(false);
		DataSet.addSelectionListener(this);
	}

	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
		synchronized (this) {
			collectAddressesAndStreets(newSelection);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		collectAddressesAndStreets(Main.main.getCurrentDataSet()
				.allPrimitives());

		if (addressModel != null) {
			AddressEditDialog dlg = new AddressEditDialog(addressModel);
			dlg.setVisible(true);
		}
	}

	private void collectAddressesAndStreets(
			final Collection<? extends OsmPrimitive> osmData) {
		if (osmData == null || osmData.isEmpty())
			return;

		final AddressVisitor addrVisitor = new AddressVisitor();

		//final PleaseWaitProgressMonitor monitor = new PleaseWaitProgressMonitor(tr("Prepare OSM data..."));
		// int ticks = osmData.size();



		try {
			for (OsmPrimitive osm : osmData) {
				osm.visit(addrVisitor);

				if (isCanceled) {
					addrVisitor.clearData(); // free visitor data
					return;
				}
			}
			//monitor.worked(1);

			// generateTagCode(addrVisitor);
			//monitor.setCustomText(tr("Resolving addresses..."));
			addrVisitor.resolveAddresses();
			//monitor.worked(1);


		} finally {
			//monitor.close();
		}

		addressModel = new AddressEditModel(
				addrVisitor.getStreetList(), 
				addrVisitor.getUnresolvedItems());
	}

	@Override
	protected void updateEnabledState() {
		setEnabled(getCurrentDataSet() != null);
	}

	@Override
	protected void updateEnabledState(
			Collection<? extends OsmPrimitive> selection) {
		setEnabled(selection != null && !selection.isEmpty());
	}

	/* ----------------------------------------- */

	private void generateTagCode(AddressVisitor addrVisitor) {
		/* This code is abused to generate tag utility code */
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

	@Override
	public void operationCanceled() {
		isCanceled = true;
	}
}
