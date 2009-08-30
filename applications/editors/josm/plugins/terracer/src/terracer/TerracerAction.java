/**
 * Terracer: A JOSM Plugin for terraced houses.
 *
 * Copyright 2009 CloudMade Ltd.
 *
 * Released under the GPLv2, see LICENSE file for details.
 */
package terracer;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.Choice;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.AutoCompleteComboBox;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Pair;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Terraces a quadrilateral, closed way into a series of quadrilateral,
 * closed ways.
 *
 * At present it only works on quadrilaterals, but there is no reason
 * why it couldn't be extended to work with other shapes too. The
 * algorithm employed is naive, but it works in the simple case.
 *
 * @author zere
 */
public final class TerracerAction extends JosmAction {

	// smsms1 asked for the last value to be remembered to make it easier to do
	// repeated terraces. this is the easiest, but not necessarily nicest, way.
	//private static String lastSelectedValue = "";

	public TerracerAction() {
		super(tr("Terrace a building"),
				"terrace",
				tr("Creates individual buildings from a long building."),
				Shortcut.registerShortcut("tools:Terracer",
						tr("Tool: {0}", tr("Terrace a building")),
						KeyEvent.VK_T, Shortcut.GROUP_EDIT,
						Shortcut.SHIFT_DEFAULT),
						true);
	}

	/**
	 * Checks that the selection is OK. If not, displays error message. If so
	 * calls to terraceBuilding(), which does all the real work.
	 */
	public void actionPerformed(ActionEvent e) {
		Collection<OsmPrimitive> sel = Main.main.getCurrentDataSet().getSelected();
		boolean badSelect = false;

		if (sel.size() == 1) {
			OsmPrimitive prim = sel.iterator().next();

			if (prim instanceof Way) {
				Way way = (Way)prim;

				if ((way.getNodesCount() >= 5) &&
						way.isClosed()) {
					// first ask the user how many buildings to terrace into
					HouseNumberDialog dialog = new HouseNumberDialog();
					final JOptionPane optionPane = new JOptionPane(dialog, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

					String title = trn("Change {0} object", "Change {0} objects", sel.size(), sel.size());
					if(sel.size() == 0)
						title = tr("Nothing selected!");

					optionPane.createDialog(Main.parent, title).setVisible(true);
					Object answerObj = optionPane.getValue();
					if (answerObj != null &&
							answerObj != JOptionPane.UNINITIALIZED_VALUE &&
							(answerObj instanceof Integer &&
									(Integer)answerObj == JOptionPane.OK_OPTION)) {

						// call out to the method which does the actual
						// terracing.
						terraceBuilding(way,
								dialog.numberFrom(),
								dialog.numberTo(),
								dialog.stepSize(),
								dialog.streetName());

					}
				} else {
					badSelect = true;
				}
			} else {
				badSelect = true;
			}
		} else {
			badSelect = true;
		}

		if (badSelect) {
			JOptionPane.showMessageDialog(Main.parent,
					tr("Select a single, closed way of at least four nodes."));
		}
	}

	/**
	 * Terraces a single, closed, quadrilateral way.
	 *
	 * Any node must be adjacent to both a short and long edge, we naively
	 * choose the longest edge and its opposite and interpolate along them
	 * linearly to produce new nodes. Those nodes are then assembled into
	 * closed, quadrilateral ways and left in the selection.
	 *
	 * @param w The closed, quadrilateral way to terrace.
	 */
	private void terraceBuilding(Way w, int from, int to, int step, String streetName) {
		final int nb = 1 + (to - from) / step;

		// now find which is the longest side connecting the first node
		Pair<Way,Way> interp = findFrontAndBack(w);

		final double frontLength = wayLength(interp.a);
		final double backLength = wayLength(interp.b);

		// new nodes array to hold all intermediate nodes
		Node[][] new_nodes = new Node[2][nb + 1];

		Collection<Command> commands = new LinkedList<Command>();
		Collection<Way> ways = new LinkedList<Way>();

		// create intermediate nodes by interpolating.
		for (int i = 0; i <= nb; ++i) {
			new_nodes[0][i] = interpolateAlong(interp.a, frontLength * (i) / (nb));
			new_nodes[1][i] = interpolateAlong(interp.b, backLength * (i) / (nb));
			commands.add(new AddCommand(new_nodes[0][i]));
			commands.add(new AddCommand(new_nodes[1][i]));
		}

		// create a new relation for addressing
		Relation relatedStreet = new Relation();
		relatedStreet.put("type", "relatedStreet");
		if (streetName != null) {
			relatedStreet.put("name", streetName);
		}
		// note that we don't actually add the street member to the relation, as
		// the name isn't unambiguous and it could cause confusion if the editor were
		// to automatically select one which wasn't the one the user intended.

		// assemble new quadrilateral, closed ways
		for (int i = 0; i < nb; ++i) {
			Way terr = new Way();
			// Using Way.nodes.add rather than Way.addNode because the latter doesn't
			// exist in older versions of JOSM.
			terr.addNode(new_nodes[0][i]);
			terr.addNode(new_nodes[0][i+1]);
			terr.addNode(new_nodes[1][i+1]);
			terr.addNode(new_nodes[1][i]);
			terr.addNode(new_nodes[0][i]);
			terr.put("addr:housenumber", "" + (from + i * step));
			terr.put("building", "yes");
			if (streetName != null) {
				terr.put("addr:street", streetName);
			}
			relatedStreet.members.add(new RelationMember("house", terr));
			ways.add(terr);
			commands.add(new AddCommand(terr));
		}

		commands.add(new AddCommand(relatedStreet));

		Main.main.undoRedo.add(new SequenceCommand(tr("Terrace"), commands));
		Main.main.getCurrentDataSet().setSelected(ways);
	}

	/**
	 * Creates a node at a certain distance along a way, as calculated by the
	 * great circle distance.
	 *
	 * Note that this really isn't an efficient way to do this and leads to
	 * O(N^2) running time for the main algorithm, but its simple and easy
	 * to understand, and probably won't matter for reasonable-sized ways.
	 *
	 * @param w The way to interpolate.
	 * @param l The length at which to place the node.
	 * @return A node at a distance l along w from the first point.
	 */
	private Node interpolateAlong(Way w, double l) {
		Node n = null;
		for (Pair<Node,Node> p : w.getNodePairs(false)) {
			final double seg_length = p.a.getCoor().greatCircleDistance(p.b.getCoor());
			if (l <= seg_length) {
				n = interpolateNode(p.a, p.b, l / seg_length);
				break;
			} else {
				l -= seg_length;
			}
		}
		if (n == null) {
			// sometimes there is a small overshoot due to numerical roundoff, so we just
			// set these cases to be equal to the last node. its not pretty, but it works ;-)
			n = w.getNode(w.getNodesCount() - 1);
		}
		return n;
	}

	/**
	 * Calculates the great circle length of a way by summing the great circle
	 * distance of each pair of nodes.
	 *
	 * @param w The way to calculate length of.
	 * @return The length of the way.
	 */
	private double wayLength(Way w) {
		double length = 0.0;
		for (Pair<Node,Node> p : w.getNodePairs(false)) {
			length += p.a.getCoor().greatCircleDistance(p.b.getCoor());
		}
		return length;
	}

	/**
	 * Given a way, try and find a definite front and back by looking at the
	 * segments to find the "sides". Sides are assumed to be single segments
	 * which cannot be contiguous.
	 *
	 * @param w The way to analyse.
	 * @return A pair of ways (front, back) pointing in the same directions.
	 */
	private Pair<Way, Way> findFrontAndBack(Way w) {
		// calculate the "side-ness" score for each segment of the way
		double[] sideness = calculateSideness(w);

		// find the largest two sidenesses which are not contiguous
		int[] indexes = sortedIndexes(sideness);
		int side1 = indexes[0];
		int side2 = indexes[1];
		// if side2 is contiguous with side1 then look further down the
		// list. we know there are at least 4 sides, as anything smaller
		// than a quadrilateral would have been rejected at an earlier
		// stage.
		if (Math.abs(side1 - side2) < 2) {
			side2 = indexes[2];
		}
		if (Math.abs(side1 - side2) < 2) {
			side2 = indexes[3];
		}

		// if the second side has a shorter length and an approximately equal
		// sideness then its better to choose the shorter, as with quadrilaterals
		// created using the orthogonalise tool the sideness will be about the
		// same for all sides.
		if (sideLength(w, side1) > sideLength(w, side1 + 1) &&
			Math.abs(sideness[side1] - sideness[side1 + 1]) < 0.001) {
			side1 = side1 + 1;
			side2 = (side2 + 1) % (w.getNodesCount() - 1);
		}

		// swap side1 and side2 into sorted order.
		if (side1 > side2) {
			// i can't believe i have to write swap() myself - surely java standard
			// library has this somewhere??!!?ONE!
			int tmp = side2;
			side2 = side1;
			side1 = tmp;
		}

		Way front = new Way();
		Way back = new Way();
		for (int i = side2 + 1; i < w.getNodesCount() - 1; ++i) {
			front.addNode(w.getNode(i));
		}
		for (int i = 0; i <= side1; ++i) {
			front.addNode(w.getNode(i));
		}
		// add the back in reverse order so that the front and back ways point
		// in the same direction.
		for (int i = side2; i > side1; --i) {
			back.addNode(w.getNode(i));
		}

		return new Pair<Way, Way>(front, back);
	}

	/**
	 * Calculate the length of a side (from node i to i+1) in a way. This assumes that
	 * the way is closed, but I only ever call it for buildings.
	 */
	private double sideLength(Way w, int i) {
		Node a = w.getNode(i);
		Node b = w.getNode((i+1) % (w.getNodesCount() - 1));
		return a.getCoor().greatCircleDistance(b.getCoor());
	}

	/**
	 * Given an array of doubles (but this could made generic very easily) sort
	 * into order and return the array of indexes such that, for a returned array
	 * x, a[x[i]] is sorted for ascending index i.
	 *
	 * This isn't efficient at all, but should be fine for the small arrays we're
	 * expecting. If this gets slow - replace it with some more efficient algorithm.
	 *
	 * @param a The array to sort.
	 * @return An array of indexes, the same size as the input, such that a[x[i]]
	 * is in sorted order.
	 */
	private int[] sortedIndexes(final double[] a) {
		class SortWithIndex implements Comparable<SortWithIndex> {
			public double x;
			public int i;
			public SortWithIndex(double a, int b) {
				x = a; i = b;
			}
			public int compareTo(SortWithIndex o) {
				return Double.compare(x, o.x);
			};
		}

		final int length = a.length;
		ArrayList<SortWithIndex> sortable = new ArrayList<SortWithIndex>(length);
		for (int i = 0; i < length; ++i) {
			sortable.add(new SortWithIndex(a[i], i));
		}
		Collections.sort(sortable);

		int[] indexes = new int[length];
		for (int i = 0; i < length; ++i) {
			indexes[i] = sortable.get(i).i;
		}

		return indexes;
	}

	/**
	 * Calculate "sideness" metric for each segment in a way.
	 */
	private double[] calculateSideness(Way w) {
		final int length = w.getNodesCount() - 1;
		double[] sideness = new double[length];

		sideness[0] = calculateSideness(
				w.getNode(length - 1), w.getNode(0),
				w.getNode(1), w.getNode(2));
		for (int i = 1; i < length - 1; ++i) {
			sideness[i] = calculateSideness(
					w.getNode(i-1), w.getNode(i),
					w.getNode(i+1), w.getNode(i+2));
		}
		sideness[length-1] = calculateSideness(
				w.getNode(length - 2), w.getNode(length - 1),
				w.getNode(length), w.getNode(1));

		return sideness;
	}

	/**
	 * Calculate sideness of a single segment given the nodes which make up that
	 * segment and its previous and next segments in order. Sideness is calculated
	 * for the segment b-c.
	 */
	private double calculateSideness(Node a, Node b, Node c, Node d) {
		final double ndx = b.getCoor().getX() - a.getCoor().getX();
		final double pdx = d.getCoor().getX() - c.getCoor().getX();
		final double ndy = b.getCoor().getY() - a.getCoor().getY();
		final double pdy = d.getCoor().getY() - c.getCoor().getY();

		return (ndx * pdx + ndy * pdy) /
		Math.sqrt((ndx * ndx + ndy * ndy) * (pdx * pdx + pdy * pdy));
	}

	/**
	 * Dialog box to allow users to input housenumbers in a nice way.
	 */
	class HouseNumberDialog extends JPanel {
		private SpinnerNumberModel lo, hi;
		private JSpinner clo, chi;
		private Choice step;
		private AutoCompleteComboBox street;

		public HouseNumberDialog() {
			super(new GridBagLayout());
			lo = new SpinnerNumberModel(1, 1, 1, 1);
			hi = new SpinnerNumberModel(1, 1, null, 1);
			step = new Choice();
			step.add(tr("All"));
			step.add(tr("Even"));
			step.add(tr("Odd"));
			clo = new JSpinner(lo);
			chi = new JSpinner(hi);

			lo.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					hi.setMinimum((Integer)lo.getNumber());
				}
			});
			hi.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					lo.setMaximum((Integer)hi.getNumber());
				}
			});
			step.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (step.getSelectedItem() == tr("All")) {
						hi.setStepSize(1);
						lo.setStepSize(1);
					} else {
						int odd_or_even = 0;
						int min = 0;

						if (step.getSelectedItem() == tr("Even")) {
							odd_or_even = 0;
							min = 2;
						} else {
							odd_or_even = 1;
							min = 1;
						}

						if ((lo.getNumber().intValue() & 1) != odd_or_even) {
							int nextval = lo.getNumber().intValue() - 1;
							lo.setValue((nextval > min) ? nextval : min);
						}
						if ((hi.getNumber().intValue() & 1) != odd_or_even) {
							int nextval = hi.getNumber().intValue() - 1;
							hi.setValue((nextval > min) ? nextval : min);
						}
						lo.setMinimum(min);
						hi.setStepSize(2);
						lo.setStepSize(2);
					}
				}
			});

			final TreeSet<String> names = createAutoCompletionInfo();

			street = new AutoCompleteComboBox();
			street.setPossibleItems(names);
			street.setEditable(true);
			street.setSelectedItem(null);

			JFormattedTextField x;
			x = ((JSpinner.DefaultEditor)clo.getEditor()).getTextField();
			x.setColumns(5);
			x = ((JSpinner.DefaultEditor)chi.getEditor()).getTextField();
			x.setColumns(5);
			addLabelled(tr("Highest number") + ": ",   chi);
			addLabelled(tr("Lowest number") + ": ", clo);
			addLabelled(tr("Interpolation") + ": ", step);
			addLabelled(tr("Street name") + " (" + tr("Optional") + "): ", street);
		}

		private void addLabelled(String str, Component c) {
			JLabel label = new JLabel(str);
			add(label, GBC.std());
			label.setLabelFor(c);
			add(c, GBC.eol());
		}

		public int numberFrom() {
			return lo.getNumber().intValue();
		}

		public int numberTo() {
			return hi.getNumber().intValue();
		}

		public int stepSize() {
			return (step.getSelectedItem() == tr("All")) ? 1 : 2;
		}

		public String streetName() {
			Object selected = street.getSelectedItem();
			if (selected == null) {
				return null;
			} else {
				String name = selected.toString();
				if (name.length() == 0) {
					return null;
				} else {
					return name;
				}
			}
		}
	}

	/**
	 * Generates a list of all visible names of highways in order to do
	 * autocompletion on the road name.
	 */
	private TreeSet<String> createAutoCompletionInfo() {
		final TreeSet<String> names = new TreeSet<String>();
		for (OsmPrimitive osm : Main.main.getCurrentDataSet().allNonDeletedPrimitives()) {
			if (osm.getKeys() != null &&
					osm.keySet().contains("highway") &&
					osm.keySet().contains("name")) {
				names.add(osm.get("name"));
			}
		}
		return names;
	}

	/**
	 * Creates a new node at the interpolated position between the argument
	 * nodes. Interpolates linearly in Lat/Lon coordinates.
	 *
	 * @param a First node, at which f=0.
	 * @param b Last node, at which f=1.
	 * @param f Fractional position between first and last nodes.
	 * @return A new node at the interpolated position.
	 */
	private Node interpolateNode(Node a, Node b, double f) {
		Node n = new Node(interpolateLatLon(a, b, f));
		return n;
	}

	/**
	 * Calculates the interpolated position between the argument nodes. Interpolates
	 * linearly in Lat/Lon coordinates.
	 *
	 * @param a First node, at which f=0.
	 * @param b Last node, at which f=1.
	 * @param f Fractional position between first and last nodes.
	 * @return The interpolated position.
	 */
	private LatLon interpolateLatLon(Node a, Node b, double f) {
		// this isn't quite right - we should probably be interpolating
		// screen coordinates rather than lat/lon, but it doesn't seem to
		// make a great deal of difference at the scale of most terraces.
		return new LatLon(a.getCoor().lat() * (1.0 - f) + b.getCoor().lat() * f,
				a.getCoor().lon() * (1.0 - f) + b.getCoor().lon() * f);
	}
}
