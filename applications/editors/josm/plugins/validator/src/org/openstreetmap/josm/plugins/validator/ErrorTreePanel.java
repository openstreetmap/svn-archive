package org.openstreetmap.josm.plugins.validator;

import java.util.*;
import java.util.Map.Entry;

import javax.swing.JTree;
import javax.swing.tree.*;

import org.openstreetmap.josm.plugins.validator.util.Bag;

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
        this.errors = errors;
        this.setModel(treeModel);
		this.setRootVisible(false);
		this.setShowsRootHandles(true);
		this.expandRow(0);
		this.setVisibleRowCount(8);
		this.setCellRenderer(new ErrorTreeRenderer());
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
        buildTree();
    }

    /**
     * Constructor
     */
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
		
		Map<Severity, Bag<String, TestError>> errorTree = new HashMap<Severity, Bag<String, TestError>>();
		for(Severity s : Severity.values())
		{
			errorTree.put(s, new Bag<String, TestError>(20));
		}
		
		for(TestError e : errors)
		{
			errorTree.get(e.getSeverity()).add(e.getMessage(), e);
		}
		
		for(Severity s : Severity.values())
		{
			Bag<String,	TestError> severityErrors = errorTree.get(s);
			if( severityErrors.isEmpty() )
				continue;
			
			// Severity node
			DefaultMutableTreeNode severityNode = new DefaultMutableTreeNode(s);
			rootNode.add(severityNode);
			
			for(Entry<String, List<TestError>> msgErrors : severityErrors.entrySet()  )
			{
				// Message node
				List<TestError> errors = msgErrors.getValue();
				String msg = msgErrors.getKey() + " (" + errors.size() + ")";
				DefaultMutableTreeNode messageNode = new DefaultMutableTreeNode(msg);
				severityNode.add(messageNode);
				
				for (TestError error : errors) 
				{
					// Error node
					DefaultMutableTreeNode errorNode = new DefaultMutableTreeNode(error);
					messageNode.add(errorNode);
				}
			}
		}

		treeModel.setRoot(rootNode);
		this.scrollRowToVisible(treeModel.getChildCount(rootNode)-1);
	}

    /**
     * Set the errors of the tree
     * @param errors
     */
    public void setErrors(List<TestError> errors)
    {
        this.errors = errors;
    }
    
    /**
     * Expands all tree
     */
    @SuppressWarnings("unchecked")
    public void expandAll()
    {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        
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
