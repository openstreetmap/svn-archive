package org.openstreetmap.josm.plugins.validator;

import java.util.*;
import java.util.Map.Entry;

import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.*;

import org.openstreetmap.josm.plugins.validator.util.Bag;
import org.openstreetmap.josm.plugins.validator.util.MultipleNameVisitor;

/**
 * A panel that displays the error tree. The selection manager
 * respects clicks into the selection list. Ctrl-click will remove entries from
 * the list while single click will make the clicked entry the only selection.
 *
 * @author frsantos
 */

public class ErrorTreePanel extends JTree
{
	/** Serializable ID */
	private static final long serialVersionUID = 2952292777351992696L;

	/**
	 * The validation data.
	 */
	protected DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());

	/** The list of errors shown in the tree */
	private List<TestError> errors;
	/**
	 * Constructor
	 * @param errors The list of errors
	 */
	public ErrorTreePanel(List<TestError> errors)
	{
		ToolTipManager.sharedInstance().registerComponent(this);
		this.setModel(treeModel);
		this.setRootVisible(false);
		this.setShowsRootHandles(true);
		this.expandRow(0);
		this.setVisibleRowCount(8);
		this.setCellRenderer(new ErrorTreeRenderer());
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		setErrorList(errors);
	}

	public String getToolTipText(MouseEvent e) {
		String res = null;
		TreePath path = getPathForLocation(e.getX(), e.getY());
		if (path != null)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
			Object nodeInfo = node.getUserObject();

			if (nodeInfo instanceof TestError)
			{
				TestError error = (TestError)nodeInfo;
				MultipleNameVisitor v = new MultipleNameVisitor();
				v.visit(error.getPrimitives());
				res = "<html>" + v.getText() + "<br>" + error.getMessage();
				String d = error.getDescription();
				if(d != null)
					res += "<br>" + d;
				res += "</html>";
			}
			else
				res = node.toString();
		}
		return res;
	}

	/** Constructor */
	public ErrorTreePanel()
	{
		this(null);
	}

	@Override
	public void setVisible(boolean v)
	{
		if (v)
			buildTree();
		else 
			treeModel.setRoot(new DefaultMutableTreeNode());
		super.setVisible(v);
	}

	/**
	 * Builds the errors tree
	 */
	public void buildTree()
	{
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

		if( errors == null || errors.isEmpty() )
		{
			treeModel.setRoot(rootNode);
			return;
		}

		// Remember the currently expanded rows
		Set<Object> oldSelectedRows = new HashSet<Object>();
		Enumeration<TreePath> expanded = getExpandedDescendants( new TreePath(getRoot()) );
		if( expanded != null )
		{
			while( expanded.hasMoreElements() )
			{
				TreePath path = expanded.nextElement();
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent(); 
				Object userObject = node.getUserObject();
				if( userObject instanceof Severity )
					oldSelectedRows.add(userObject);
				else if (userObject instanceof String)
				{
					String msg = (String)userObject;
					msg = msg.substring(0, msg.lastIndexOf(" ("));
					oldSelectedRows.add(msg);
				}
			}
		}

		Map<Severity, Bag<String, TestError>> errorTree = new HashMap<Severity, Bag<String, TestError>>();
		for(Severity s : Severity.values())
		{
			errorTree.put(s, new Bag<String, TestError>(20));
		}

		for(TestError e : errors)
		{
			errorTree.get(e.getSeverity()).add(e.getMessage(), e);
		}

		List<TreePath> expandedPaths = new ArrayList<TreePath>();
		for(Severity s : Severity.values())
		{
			Bag<String, TestError> severityErrors = errorTree.get(s);
			if( severityErrors.isEmpty() )
				continue;

			// Severity node
			DefaultMutableTreeNode severityNode = new DefaultMutableTreeNode(s);
			rootNode.add(severityNode);

			if( oldSelectedRows.contains(s))
				expandedPaths.add( new TreePath( new Object[] {rootNode, severityNode} ) );

			for(Entry<String, List<TestError>> msgErrors : severityErrors.entrySet()  )
			{
				// Message node
				List<TestError> errors = msgErrors.getValue();
				String msg = msgErrors.getKey() + " (" + errors.size() + ")";
				DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(msg);
				severityNode.add(messageNode);

				if( oldSelectedRows.contains(msgErrors.getKey()))
					 expandedPaths.add( new TreePath( new Object[] {rootNode, severityNode, messageNode} ) );

				for (TestError error : errors) 
				{
					// Error node
					DefaultMutableTreeNode errorNode = new DefaultMutableTreeNode(error);
					messageNode.add(errorNode);
				}
			}
		}

		treeModel.setRoot(rootNode);
		for( TreePath path : expandedPaths)
		{
			this.expandPath(path);
		}
	}

	/**
	 * Sets the errors list used by a data layer
	 * @param errors The error list that is used by a data layer
	 */
	public void setErrorList(List<TestError> errors)
	{
		this.errors = errors;
		if( isVisible() )
			buildTree();
	}

	/**
	 * Clears the current error list and adds these errors to it
	 * @param errors The validation errors
	 */
	public void setErrors(List<TestError> newerrors)
	{
		if(errors == null)
			return;
		errors.clear();
		for(TestError error : newerrors)
		{
			if(!error.getIgnored())
				errors.add(error);
		}
		if( isVisible() )
			buildTree();
	}

	/**
	 * Returns the errors of the tree
	 * @return  the errors of the tree
	 */
	public List<TestError> getErrors()
	{
		return errors != null ? errors : Collections.<TestError>emptyList();
	}

	/**
	 * Updates the current errors list
	 * @param errors The validation errors
	 */
	public void resetErrors()
	{
		List<TestError> e = new ArrayList<TestError>(errors);
		setErrors(e);
	}

	/**
	 * Expands all tree
	 */
	@SuppressWarnings("unchecked")
	public void expandAll()
	{
		DefaultMutableTreeNode root = getRoot();

		int row = 0;
		Enumeration<DefaultMutableTreeNode> children = root.breadthFirstEnumeration();
		while( children.hasMoreElements() )
		{
			children.nextElement();
			expandRow(row++);
		}
	}

	/**
	 * Returns the root node model.
	 * @return The root node model
	 */
	public DefaultMutableTreeNode getRoot()
	{
		return (DefaultMutableTreeNode) treeModel.getRoot();
	}
}
