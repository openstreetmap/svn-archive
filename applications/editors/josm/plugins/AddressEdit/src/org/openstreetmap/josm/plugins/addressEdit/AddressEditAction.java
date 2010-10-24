package org.openstreetmap.josm.plugins.addressEdit;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.tools.Shortcut;
import static org.openstreetmap.josm.tools.I18n.tr;

public class AddressEditAction extends JosmAction implements
		SelectionChangedListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AddressEditAction() {
		super(tr("Address Editor"), "addressedit_24", tr("Handy Address Editing Functions"),
                Shortcut.registerShortcut("tools:AddressEdit", tr("Tool: {0}", tr("Address Edit")),
                        KeyEvent.VK_A, Shortcut.GROUP_MENU,
                        InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), false);
        setEnabled(false);
        DataSet.addSelectionListener(this);
	}

	@Override
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
		AddressVisitor addrVisitor = new AddressVisitor();
		
		for (OsmPrimitive osm : newSelection) {
            osm.visit(addrVisitor);
        }
		/* This code is abused to generate tag utility code
		for (String tag : addrVisitor.getTags()) {
			String methodName = tag.replaceAll(":", "");
			methodName = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
			System.out.println(String.format("/** Check if OSM primitive has a tag '%s'. /\npublic static boolean is%s(OsmPrimitive osmPrimitive) {\n return osmPrimitive != null ? osmPrimitive.hasKey(%s_TAG) : false;\n}\n",
					tag,
					methodName, 
					tag.toUpperCase().replaceAll(":", "_")));
		}
		
		for (String tag : addrVisitor.getTags()) {
			System.out.println(String.format("public static final String %s_TAG = \"%s\";", tag.toUpperCase().replaceAll(":", "_"), tag));
		}*/
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

	}

}
