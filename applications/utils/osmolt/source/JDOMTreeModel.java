import org.jdom.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Vector;


public class JDOMTreeModel implements TreeModel {

	private boolean showforefathers;

	private Vector treeModelListeners = new Vector();

	private Element rootElement;

	/** Creates a new instance of JDOMTreeModel */
	public JDOMTreeModel(Element element) {
		this.rootElement = element;
	}

	/**
	 * Used to toggle between show ancestors/show descendant and to change the
	 * root of the tree.
	 */
	public void showAncestor(boolean b, Object newRoot) {
		showforefathers = b;
		Element oldElement = rootElement;
		if (newRoot != null) {
			rootElement = (Element) newRoot;
		}
		fireTreeStructureChanged(oldElement);
	}

	//  Fire events
	// =============

	/**
	 * The only event raised by this model is TreeStructureChanged with the root
	 * as path, i.e. the whole tree has changed.
	 */
	protected void fireTreeStructureChanged(Element oldRoot) {
		int len = treeModelListeners.size();
		TreeModelEvent e = new TreeModelEvent(this, new Object[] { oldRoot });
		for (int i = 0; i < len; i++) {
			((TreeModelListener) treeModelListeners.elementAt(i))
					.treeStructureChanged(e);
		}
	}

	/**
	 * Adds a listener for the TreeModelEvent posted after the tree changes.
	 */
	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.addElement(l);
	}

	/**
	 * Returns the child of parent at index index in the parent's child array.
	 */
	public Object getChild(Object parent, int index) {
		Element parentElement = (Element) parent;
		if (showforefathers) {

			return parentElement.getParentElement();
		}
		return parentElement.getChildren().get(index);
	}

	/**
	 * Returns the number of children of parent.
	 */
	public int getChildCount(Object parent) {
		Element parentElement = (Element) parent;
		if (showforefathers) {
			int count = 0;
			if (parentElement.getParentElement() != null) {
				count++;
			}
			return count;
		}
		if (parentElement.getChildren() == null)
			return 0;
		return parentElement.getChildren().size();
	}

	/**
	 * Returns the index of child in parent.
	 */
	public int getIndexOfChild(Object parent, Object child) {
		Element element = (Element) parent;
		if (showforefathers) {
			int count = 0;
			if (element.getParentElement() != null) {
				count++;
				if (element == element.getParentElement())
					return 0;
				return -1;
			}
		}
		return element.getChildren().indexOf(child);
	}

	/**
	 * Returns the root of the tree.
	 */
	public Object getRoot() {
		return rootElement;
	}

	/**
	 * Returns true if node is a leaf.
	 */
	public boolean isLeaf(Object node) {
		Element element = (Element) node;
		if (showforefathers) {
			return element.getParentElement() == null;
		}
		return element.getChildren() == null;
	}

	/**
	 * Removes a listener previously added with addTreeModelListener().
	 */
	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.removeElement(l);
	}

	/**
	 * Messaged when the user has altered the value for the item identified by
	 * path to newValue. Not used by this model.
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		System.out.println("*** valueForPathChanged : " + path + " --> "
				+ newValue);
	}

}
