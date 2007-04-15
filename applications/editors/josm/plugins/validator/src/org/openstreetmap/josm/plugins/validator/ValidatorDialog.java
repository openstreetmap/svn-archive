package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.validator.util.Bag;
import org.openstreetmap.josm.plugins.validator.util.Util;

/**
 * A small tool dialog for displaying the current selection. The selection manager
 * respects clicks into the selection list. Ctrl-click will remove entries from
 * the list while single click will make the clicked entry the only selection.
 *
 * @author imi
 */
public class ValidatorDialog extends ToggleDialog implements ActionListener
{
    /** Serializable ID */
    private static final long serialVersionUID = 2952292777351992696L;

    /**
     * The validation data.
     */
	private DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());

	/**
     * The display tree.
     */
    protected JTree tree = new JTree(treeModel);
    

    /**
     * Constructor
     */
    public ValidatorDialog() 
    {
        super(tr("Validation errors"), "validator", tr("Open the validation window."), KeyEvent.VK_V, 150);
        
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.expandRow(0);
		tree.setVisibleRowCount(8);
		tree.addMouseListener(new DblClickWatch());
		tree.setCellRenderer(new ErrorTreeRenderer());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		add(new JScrollPane(tree), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1,2));

        buttonPanel.add(Util.createButton("Select", "mapmode/selection/select", "Set the selected elements on the map to the selected items in the list above.", this)); 
        add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(Util.createButton("Validate", "dialogs/refresh", "Validate the data.", this)); 
        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override 
    public void setVisible(boolean v) {
		if (v)
			buildTree();
		else if (tree != null)
			treeModel.setRoot(new DefaultMutableTreeNode());
		if( action != null && action.button != null )
			action.button.setSelected(v);
		super.setVisible(v);
	}
    
    
	/**
	 * Builds the errors tree
	 */
	private void buildTree() 
	{
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

		List<TestError> errorList = OSMValidatorPlugin.getPlugin().errors;
		if( errorList == null || errorList.isEmpty() )
		{
			treeModel.setRoot(rootNode);
			return;
		}
		
		Map<Severity, Bag<String, TestError>> errorTree = new HashMap<Severity, Bag<String, TestError>>();
		for(Severity s : Severity.values())
		{
			errorTree.put(s, new Bag<String, TestError>(20));
		}
		
		for(TestError e : errorList)
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
		tree.scrollRowToVisible(treeModel.getChildCount(rootNode)-1);
	}
	
    /**
     * Sets the selection of the map to the current selected items.
     */
    @SuppressWarnings("unchecked")
    public void setSelectedItems() 
    {
        Collection<OsmPrimitive> sel = new HashSet<OsmPrimitive>(40);

        TreePath[] selectedPaths = tree.getSelectionPaths();
        for( TreePath path : selectedPaths)
        {
        	DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
    		Enumeration<DefaultMutableTreeNode> children = node.breadthFirstEnumeration();
    		while( children.hasMoreElements() )
    		{
        		DefaultMutableTreeNode childNode = children.nextElement();
        		Object nodeInfo = childNode.getUserObject();
        		if( nodeInfo instanceof TestError)
        		{
        			TestError error = (TestError)nodeInfo;
        			sel.addAll( error.getPrimitives() );
        		}
    		}
        }
        Main.ds.setSelected(sel);
    }

	public void actionPerformed(ActionEvent e) 
	{
		if( e.getActionCommand().equals("Select"))
			setSelectedItems();
		else if( e.getActionCommand().equals("Validate"))
	    	OSMValidatorPlugin.getPlugin().validateAction.actionPerformed(e);
	}
	
	/**
	 * Refresh the error messages display
	 */
	public void refresh()
	{
		buildTree();
	}
	
	/**
	 * Watches for double clicks and from editing or new property, depending on the
	 * location, the click was.
	 * @author imi
	 */
	public class DblClickWatch extends MouseAdapter 
	{
        @SuppressWarnings("unchecked")
        @Override 
		public void mouseClicked(MouseEvent e) 
		{
			if (e.getClickCount() < 2 || e.getSource() instanceof JScrollPane)
				return;
			else 
			{
		        Collection<OsmPrimitive> sel = new HashSet<OsmPrimitive>(40);

	        	DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
	    		Enumeration<DefaultMutableTreeNode> children = node.breadthFirstEnumeration();
	    		while( children.hasMoreElements() )
	    		{
	        		DefaultMutableTreeNode childNode = children.nextElement();
	        		Object nodeInfo = childNode.getUserObject();
	        		if( nodeInfo instanceof TestError)
	        		{
	        			TestError error = (TestError)nodeInfo;
	        			sel.addAll( error.getPrimitives() );
	        		}
	    		}
	    		Main.ds.setSelected(sel);
			}
		}
	}
}
