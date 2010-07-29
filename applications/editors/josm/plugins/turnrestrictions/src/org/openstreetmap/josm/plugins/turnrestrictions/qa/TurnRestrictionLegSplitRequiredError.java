package org.openstreetmap.josm.plugins.turnrestrictions.qa;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.Collections;

import javax.swing.AbstractAction;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.actions.SplitWayAction.SplitWayResult;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.DefaultNameFormatter;
import org.openstreetmap.josm.plugins.turnrestrictions.editor.TurnRestrictionLegRole;

/**
 * Represents an error given by the fact that either 'from' or 'to' should
 * be split because they intersect.
 *
 */
public class TurnRestrictionLegSplitRequiredError extends Issue{
	private TurnRestrictionLegRole role;
	private Way from;
	private Way to;
	private Node interesect;

	/**
	 * Create the issue
	 *
	 * @param parent the parent model
	 * @param role the role of the way which should be splitted
	 * @param from the way with role 'from'
	 * @param to the way with role 'to'
	 * @param interesect the node at the intersection
	 */
	public TurnRestrictionLegSplitRequiredError(IssuesModel parent, TurnRestrictionLegRole role, Way from, Way to, Node intersect) {
		super(parent, Severity.ERROR);
		this.role = role;
		this.from = from;
		this.to = to;
		this.interesect = intersect;
		actions.add(new SplitAction());
	}

	@Override
	public String getText() {
		String msg = null;
		switch(role){
		case FROM:
			msg = tr("The OSM way <span class=\"object-name\">{0}</span> with role <tt>{1}</tt> should be split "
				+ "at node <span class=\"object-name\">{2}</span> where it connects to way <span class=\"object-name\">{3}</span>.",
				from.getDisplayName(DefaultNameFormatter.getInstance()),
				role.getOsmRole(),
				interesect.getDisplayName(DefaultNameFormatter.getInstance()),
				to.getDisplayName(DefaultNameFormatter.getInstance())
			);
			break;
		case TO:
			msg = tr("The OSM way <span class=\"object-name\">{0}</span> with role <tt>{1}</tt> should be split "
					+ "at node <span class=\"object-name\">{2}</span> where it connects to way <span class=\"object-name\">{3}</span>.",
					to.getDisplayName(DefaultNameFormatter.getInstance()),
					role.getOsmRole(),
					interesect.getDisplayName(DefaultNameFormatter.getInstance()),
					from.getDisplayName(DefaultNameFormatter.getInstance())
				);
			break;
		}
		return msg;
	}

	class SplitAction extends AbstractAction {
		public SplitAction() {
			putValue(NAME, tr("Split now"));
			putValue(SHORT_DESCRIPTION, tr("Splits the way"));
		}
		public void actionPerformed(ActionEvent e) {
			Way way = null;
			switch(role){
			case FROM: way = from; break;
			case TO: way = to; break;
			}
			SplitWayResult result = SplitWayAction.split(
					parent.getEditorModel().getLayer(),
					way,
					Collections.singletonList(interesect),
					Collections.<OsmPrimitive>emptyList()
			);
			if (result != null){
				Main.main.undoRedo.add(result.getCommand());
			}
		}
	}
}
