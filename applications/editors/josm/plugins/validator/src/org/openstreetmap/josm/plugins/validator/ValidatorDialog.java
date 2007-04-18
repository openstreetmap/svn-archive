package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
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
     * The fix button
     */
	private JButton fixButton;
    

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
		tree.addMouseListener(new ClickWatch());
		tree.addTreeSelectionListener(new SelectionWatch());
		tree.setCellRenderer(new ErrorTreeRenderer());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		add(new JScrollPane(tree), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1,2));

        buttonPanel.add(Util.createButton("Select", "mapmode/selection/select", "Set the selected elements on the map to the selected items in the list above.", this)); 
        add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(Util.createButton("Validate", "dialogs/refresh", "Validate the data.", this)); 
        add(buttonPanel, BorderLayout.SOUTH);
        fixButton = Util.createButton("Fix", "dialogs/fix", "Fix the selected errors.", this);
        fixButton.setEnabled(false);
        buttonPanel.add(fixButton); 
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
	 * Fix selected errors
	 * @param e 
	 */
	@SuppressWarnings("unchecked")
	private void fixErrors(ActionEvent e) 
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
    	if( node == null )
    		return;

        Bag<String, Command> commands = new Bag<String, Command>();

		Enumeration<DefaultMutableTreeNode> children = node.breadthFirstEnumeration();
		while( children.hasMoreElements() )
		{
    		DefaultMutableTreeNode childNode = children.nextElement();
    		Object nodeInfo = childNode.getUserObject();
    		if( nodeInfo instanceof TestError)
    		{
    			TestError error = (TestError)nodeInfo;
    			Command fixCommand = error.getFix();
    			if( fixCommand != null )
    			{
    				commands.add(error.getMessage(), fixCommand);
    			}
    		}
		}
		
		Command fixCommand = null;
		if( commands.size() == 0 )
			return;
		else if( commands.size() > 1 )
		{
			List<Command> allComands = new ArrayList<Command>(50);
			for( Entry<String, List<Command>> errorType : commands.entrySet())
			{
				String description = errorType.getKey();
				List<Command> errorCommands = errorType.getValue();
				allComands.add( new SequenceCommand("Fix " + description, errorCommands) );
			}
			
			fixCommand = new SequenceCommand("Fix errors", allComands);
		}
		else 
		{
			for( Entry<String, List<Command>> errorType : commands.entrySet())
			{
				String description = errorType.getKey();
				List<Command> errorCommands = errorType.getValue();
				fixCommand = new SequenceCommand("Fix " + description, errorCommands);
			}
		}
		
		Main.main.editLayer().add( fixCommand );
		Main.map.repaint();
		Main.ds.fireSelectionChanged(Main.ds.getSelected());
		       
    	OSMValidatorPlugin.getPlugin().validateAction.doValidate(e, false);
	}	
	
    /**
     * Sets the selection of the map to the current selected items.
     */
    @SuppressWarnings("unchecked")
    private void setSelectedItems() 
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
		String actionCommand = e.getActionCommand();
		if( actionCommand.equals("Select"))
			setSelectedItems();
		else if( actionCommand.equals("Validate"))
	    	OSMValidatorPlugin.getPlugin().validateAction.actionPerformed(e);
		else if( actionCommand.equals("Fix"))
	    	fixErrors(e); 
	}

	/**
	 * Refresh the error messages display
	 */
	public void refresh()
	{
		buildTree();
	}
	
    /**
     * Checks for fixes in selected element and, if needed, adds to the sel parameter all selected elements
     * @param sel The collection where to add all selected elements
     * @param addSelected if true, add all selected elements to collection
     * @return whether the selected elements has any fix
     */
    @SuppressWarnings("unchecked")
	private boolean setSelection(Collection<OsmPrimitive> sel, boolean addSelected)
	{
		boolean hasFixes = false;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
    	if( node == null ) 
    		return hasFixes;

		Enumeration<DefaultMutableTreeNode> children = node.breadthFirstEnumeration();
		while( children.hasMoreElements() )
		{
    		DefaultMutableTreeNode childNode = children.nextElement();
    		Object nodeInfo = childNode.getUserObject();
    		if( nodeInfo instanceof TestError)
    		{
    			TestError error = (TestError)nodeInfo;
    			hasFixes = hasFixes || error.isFixable();
    			if( addSelected )
    			{
        			sel.addAll( error.getPrimitives() );
    			}
    		}
		}
		
		return hasFixes;
	}
    
	/**
	 * Watches for clicks.
	 */
	public class ClickWatch extends MouseAdapter 
	{
        @Override 
		public void mouseClicked(MouseEvent e) 
		{
	        fixButton.setEnabled(false);
        	
			if(e.getSource() instanceof JScrollPane)
				return;
        	
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        	if( node == null )
        		return;

	        Collection<OsmPrimitive> sel = new HashSet<OsmPrimitive>(40);
			boolean isDblClick = e.getClickCount() > 1;
			
			boolean hasFixes = setSelection(sel, isDblClick);
	        fixButton.setEnabled(hasFixes);
	        
	        if( isDblClick)
			{
	    		Main.ds.setSelected(sel);
			}
		}
	}
	
	/**
	 * Watches for tree selection.
	 */
	public class SelectionWatch implements TreeSelectionListener 
	{
        @SuppressWarnings("unchecked")
		public void valueChanged(TreeSelectionEvent e) 
		{
	        fixButton.setEnabled(false);
        	
			if(e.getSource() instanceof JScrollPane)
				return;
        	
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        	if( node == null )
        		return;

			boolean hasFixes = setSelection(null, false);
	        fixButton.setEnabled(hasFixes);
		}
	}
}
