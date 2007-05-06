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
 * A small tool dialog for displaying the current errors. The selection manager
 * respects clicks into the selection list. Ctrl-click will remove entries from
 * the list while single click will make the clicked entry the only selection.
 *
 * @author frsantos
 */
public class ValidatorDialog extends ToggleDialog implements ActionListener
{
    /** Serializable ID */
    private static final long serialVersionUID = 2952292777351992696L;

	/**
     * The display tree.
     */
    protected ErrorTreePanel tree;

    /** 
     * The fix button
     */
    private JButton fixButton;
    
    /** 
     * The select button
     */
    private JButton selectButton;
    
    /** Last selected element */
    private DefaultMutableTreeNode lastSelectedNode = null;

    /**
     * Constructor
     */
    public ValidatorDialog() 
    {
        super(tr("Validation errors"), "validator", tr("Open the validation window."), KeyEvent.VK_V, 150);
        
        tree = new ErrorTreePanel();
		tree.addMouseListener(new ClickWatch());
		tree.addTreeSelectionListener(new SelectionWatch());

		add(new JScrollPane(tree), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1,2));

        selectButton = Util.createButton("Select", "mapmode/selection/select", "Set the selected elements on the map to the selected items in the list above.", this);
        selectButton.setEnabled(false);
        buttonPanel.add(selectButton); 
        //add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(Util.createButton("Validate", "dialogs/refresh", "Validate the data.", this)); 
        // add(buttonPanel, BorderLayout.SOUTH);
        fixButton = Util.createButton("Fix", "dialogs/fix", "Fix the selected errors.", this);
        fixButton.setEnabled(false);
        buttonPanel.add(fixButton); 
        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override 
    public void setVisible(boolean v) 
    {
        if( tree != null )
            tree.setVisible(v);
		if( action != null && action.button != null )
			action.button.setSelected(v);
		super.setVisible(v);
	}
    
    
	/**
	 * Fix selected errors
	 * @param e 
	 */
	@SuppressWarnings("unchecked")
	private void fixErrors(ActionEvent e) 
	{
        TreePath[] selectionPaths = tree.getSelectionPaths();
        if( selectionPaths == null )
            return;
        
        Bag<String, Command> commands = new Bag<String, Command>();
        Set<DefaultMutableTreeNode> processedNodes = new HashSet<DefaultMutableTreeNode>();
        for( TreePath path : selectionPaths )
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
        	if( node == null )
        		continue;
            
    		Enumeration<DefaultMutableTreeNode> children = node.breadthFirstEnumeration();
    		while( children.hasMoreElements() )
    		{
        		DefaultMutableTreeNode childNode = children.nextElement();
                if( processedNodes.contains(childNode) )
                    continue;
                
                processedNodes.add(childNode);
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
        if( tree == null )
            return;
        
        Collection<OsmPrimitive> sel = new HashSet<OsmPrimitive>(40);

        TreePath[] selectedPaths = tree.getSelectionPaths();
        if( selectedPaths == null)
            return;
        
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
	 * @param errors The errors to display
	 */
	public void refresh(List<TestError> errors)
	{
        tree.setErrors(errors);
		tree.buildTree();
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
        if( lastSelectedNode != null && !lastSelectedNode.equals(node) )
        {
            Enumeration<DefaultMutableTreeNode> children = lastSelectedNode.breadthFirstEnumeration();
            while( children.hasMoreElements() )
            {
                DefaultMutableTreeNode childNode = children.nextElement();
                Object nodeInfo = childNode.getUserObject();
                if( nodeInfo instanceof TestError)
                {
                    TestError error = (TestError)nodeInfo;
                    error.setSelected(false);
                }
            }  
        }
        
        lastSelectedNode = node;
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
                error.setSelected(true);
                
    			hasFixes = hasFixes || error.isFixable();
    			if( addSelected )
    			{
        			sel.addAll( error.getPrimitives() );
    			}
    		}
		}
        selectButton.setEnabled(true);
		
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
            selectButton.setEnabled(false);
            
			boolean isDblClick = e.getClickCount() > 1;
            Collection<OsmPrimitive> sel = isDblClick ? new HashSet<OsmPrimitive>(40) : null;
			
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
            selectButton.setEnabled(false);
        	
			if(e.getSource() instanceof JScrollPane)
            {
                System.out.println(e.getSource());
                return;
            }
        	
			boolean hasFixes = setSelection(null, false);
	        fixButton.setEnabled(hasFixes);
            Main.map.repaint();            
		}
	}
}
