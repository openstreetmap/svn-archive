// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tageditor.ac;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * 
 * 
 *
 */
@SuppressWarnings("serial")
public class AutoCompletionListViewer extends JPanel {
	
	static private Logger logger = Logger.getLogger(AutoCompletionListViewer.class.getName());

	/** the table showing the auto completion list entries */
	private JTable table = null;
	
	/** the auto completion list to be displayed */
	private AutoCompletionList autoCompletionList = null;
	
	/** the listeners */
	private ArrayList<IAutoCompletionListListener> listener = null;
	
	
	/**
	 * creates the GUI 
	 */
	protected void createGUI() {
		setBackground(Color.WHITE);
		setLayout(new BorderLayout());
	
		
		table = new JTable();
		
		// the table model
		//
		if (autoCompletionList == null) {
			logger.info("setting model to default model");
			table.setModel(new DefaultTableModel());
		} else {
			logger.info("setting model to " + autoCompletionList);
			table.setModel(autoCompletionList);
		}
		
		// no table header required 
		table.setTableHeader(null);
		
		// set cell renderer 
		//
		table.setDefaultRenderer(Object.class, new AutoCompletionListRenderer());
		
		// embed in a scroll pane 
		JScrollPane p  = new JScrollPane(table);
		p.setBackground(Color.WHITE);
		add(p, BorderLayout.CENTER);
		
		// only single selection allowed
		//
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// fire item change event on double click
		//
		table.addMouseListener(
				new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							int row = table.getSelectedRow();
							String item = autoCompletionList.getFilteredItem(row).getValue();
							fireAutoCompletionListItemSelected(item);
						}
					}					
				}
		);


	}
	
	
	/**
	 * constructor 
	 * 
	 * @param list the auto completion list to be rendered. If null, the list is empty.
	 *  
	 */
	public AutoCompletionListViewer(AutoCompletionList list) {		
		this.autoCompletionList = list;
		createGUI();
		listener = new ArrayList<IAutoCompletionListListener>();
	}
	
	
	/**
	 * constructor 
	 */
	public AutoCompletionListViewer() {
		this.autoCompletionList = null;
		createGUI();
		listener = new ArrayList<IAutoCompletionListListener>();
	}


	/**
	 * 
	 */
	@Override public Dimension getMaximumSize() {	    
	    Dimension d = super.getMaximumSize();
	    d.width = 100;
	    return d;
    }
	
	/**
	 * 
	 */
	@Override public Dimension getPreferredSize() {	    
	    Dimension d = super.getMaximumSize();
	    d.width = 150;
	    return d;
    }


	/**
	 * replies the auto completion list this viewer renders
	 * 
	 * @return the auto completion list; may be null 
	 */
	public AutoCompletionList getAutoCompletionList() {
    	return autoCompletionList;
    }


	/**
	 * sets the auto completion list this viewer renders 
	 * 
	 * @param autoCompletionList  the auto completion list; may be null
	 */
	public void setAutoCompletionList(AutoCompletionList autoCompletionList) {
    	this.autoCompletionList = autoCompletionList;
    	if (autoCompletionList == null) {
    		table.setModel(new DefaultTableModel());
    	} else {
    		table.setModel(autoCompletionList);
    	}
    }


	
	/**
	 * add an {@link IAutoCompletionListListener}
	 * 
	 * @param listener  the listener 
	 */
	public void addAutoCompletionListListener(IAutoCompletionListListener listener) {
		if (listener != null && !this.listener.contains(listener)) {
			synchronized(this.listener) {
				this.listener.add(listener);
			}
		}
	}
	
	/**
	 * removes a {@link IAutoCompletionListListener} 
	 * 
	 * @param listener the listener 
	 */
	public void removeAutoCompletionListListener(IAutoCompletionListListener listener) {
		if (listener != null && this.listener.contains(listener)) {
			synchronized(this.listener) {
				this.listener.remove(listener);
			}
		}
	}
	
	
	/**
	 * notifies listeners about a selected item in the auto completion list  
	 */
	protected void fireAutoCompletionListItemSelected(String item) {
		synchronized(this.listener) {
			for (IAutoCompletionListListener target: listener) {
				target.autoCompletionItemSelected(item);
			}			
		}
	}	
	
	
	public void installKeyAction(Action a) {
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke)a.getValue(AbstractAction.ACCELERATOR_KEY), a.getValue(AbstractAction.NAME));
		getActionMap().put(a.getValue(AbstractAction.NAME), a);

	}
}
