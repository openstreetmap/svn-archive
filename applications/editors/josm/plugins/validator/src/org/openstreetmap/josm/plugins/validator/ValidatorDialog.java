package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.DataSet;
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
	private OSMValidatorPlugin plugin;

	/** Serializable ID */
	private static final long serialVersionUID = 2952292777351992696L;

	/** The display tree */
	protected ErrorTreePanel tree;

	public Collection<String> ignoredErrors = new TreeSet<String>();

	/** The fix button */
	private JButton fixButton;

	/** The ignore button */
	private JButton ignoreButton;

	/** The select button */
	private JButton selectButton;

	/** Last selected element */
	private DefaultMutableTreeNode lastSelectedNode = null;

	/**
	 * Constructor
	 */
	public ValidatorDialog(OSMValidatorPlugin plugin) {
		super(tr("Validation errors"), "validator", tr("Open the validation window."), KeyEvent.VK_V, 150);

		this.plugin = plugin;

		tree = new ErrorTreePanel();
		tree.addMouseListener(new ClickWatch());
		tree.addTreeSelectionListener(new SelectionWatch());

		add(new JScrollPane(tree), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new GridLayout(1,3));

		selectButton = Util.createButton(tr("Select"), "select", "mapmode/selection/select",
		tr("Set the selected elements on the map to the selected items in the list above."), this);
		selectButton.setEnabled(false);
		buttonPanel.add(selectButton);
		buttonPanel.add(Util.createButton(tr("Validate"), "validate", "dialogs/refresh", tr("Validate the data."), this));
		fixButton = Util.createButton(tr("Fix"), "fix", "dialogs/fix", tr("Fix the selected errors."), this);
		fixButton.setEnabled(false);
		buttonPanel.add(fixButton);
		if(Main.pref.getBoolean(PreferenceEditor.PREF_USE_IGNORE, true))
		{
			ignoreButton = Util.createButton(tr("Ignore"), "ignore", "dialogs/delete", tr("Ignore the selected errors next time."), this);
			ignoreButton.setEnabled(false);
			buttonPanel.add(ignoreButton);
		}
		else
		{
			ignoreButton = null;
		}
		add(buttonPanel, BorderLayout.SOUTH);
		loadIgnoredErrors();
	}

	private void loadIgnoredErrors() {
		ignoredErrors.clear();
		if(Main.pref.getBoolean(PreferenceEditor.PREF_USE_IGNORE, true))
		{
			try {
				final BufferedReader in = new BufferedReader(new FileReader(Util.getPluginDir() + "ignorederrors"));
				for (String line = in.readLine(); line != null; line = in.readLine()) {
					ignoredErrors.add(line);
				}
			}
			catch (final FileNotFoundException e) {}
			catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveIgnoredErrors() {
		try {
			final PrintWriter out = new PrintWriter(new FileWriter(Util.getPluginDir() + "ignorederrors"), false);
			for (String e : ignoredErrors)
				out.println(e);
			out.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setVisible(boolean v)
	{
		if( tree != null )
			tree.setVisible(v);
		if( action != null && action.button != null )
			action.button.setSelected(v);
		super.setVisible(v);
		Main.map.repaint();
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
						Main.main.undoRedo.add(fixCommand);
						error.setIgnored(true);
					}
				}
			}
		}

		Main.map.repaint();
		tree.resetErrors();
		DataSet.fireSelectionChanged(Main.ds.getSelected());
	}

	/**
	 * Set selected errors to ignore state
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	private void ignoreErrors(ActionEvent e)
	{
		boolean changed = false;
		TreePath[] selectionPaths = tree.getSelectionPaths();
		if( selectionPaths == null )
			return;

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
					String state = error.getIgnoreState();
					if(state != null)
						ignoredErrors.add(state);
					changed = true;
					error.setIgnored(true);
				}
			}
		}
		if(changed)
		{
			tree.resetErrors();
			saveIgnoredErrors();
			Main.map.repaint();
		}
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
		if( actionCommand.equals("select"))
			setSelectedItems();
		else if( actionCommand.equals("validate"))
			plugin.validateAction.actionPerformed(e);
		else if( actionCommand.equals("fix"))
			fixErrors(e);
		else if( actionCommand.equals("ignore"))
			ignoreErrors(e);
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
		if(ignoreButton != null)
			ignoreButton.setEnabled(true);
		
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
			if(ignoreButton != null)
				ignoreButton.setEnabled(false);
			selectButton.setEnabled(false);

			boolean isDblClick = e.getClickCount() > 1;
			Collection<OsmPrimitive> sel = isDblClick ? new HashSet<OsmPrimitive>(40) : null;

			boolean hasFixes = setSelection(sel, isDblClick);
			fixButton.setEnabled(hasFixes);

			if(isDblClick)
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
			if(ignoreButton != null)
				ignoreButton.setEnabled(false);
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
