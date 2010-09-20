package smed.list;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class CheckBoxJList extends JList implements ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("rawtypes")
	HashSet selectionCache = new HashSet();

	 public CheckBoxJList() {
		 super();
		 
		 setCellRenderer(new CellRenderer());
		 addListSelectionListener (this);		 
	 }


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void valueChanged(ListSelectionEvent lse) {
		
		if (! lse.getValueIsAdjusting()) {
			removeListSelectionListener (this);

			 // remember everything selected as a result of this action
	         HashSet newSelections = new HashSet();
	         int size = getModel().getSize();
	         for (int i=0; i<size; i++) {
	        	 if (getSelectionModel().isSelectedIndex(i)) newSelections.add (new Integer(i));;	        	 
	         }
	         
	         // turn on everything that was previously selected
	         Iterator it = selectionCache.iterator();
	         while (it.hasNext()) {
	        	 int index = ((Integer) it.next()).intValue();
	        	 getSelectionModel().addSelectionInterval(index, index);
	         }
	         
	         // add or remove the delta
	         it = newSelections.iterator();
	         while (it.hasNext()) {
	        	 Integer nextInt = (Integer) it.next();
	        	 int index = nextInt.intValue();
	             
	        	 if (selectionCache.contains (nextInt)) getSelectionModel().removeSelectionInterval (index, index);
	        	 else getSelectionModel().addSelectionInterval (index, index);
	         }
	         
	         // save selections for next time
	         selectionCache.clear();
	         for (int i=0; i<size; i++) {
	        	 if (getSelectionModel().isSelectedIndex(i)) {
	        		 selectionCache.add (new Integer(i));
	        	 }
	         }
	         
	         addListSelectionListener (this);
	         
		}
		
	}
}

class CellRenderer extends JComponent implements ListCellRenderer {
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DefaultListCellRenderer defaultComp;
	 JCheckBox checkbox;

	 static Color listForeground, listBackground;
	 static {
	        UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
	        listForeground =  uid.getColor ("List.foreground");
	 }
	 
	 
	 public CellRenderer() {
		 setLayout (new BorderLayout());
		 defaultComp = new DefaultListCellRenderer();
		 checkbox = new JCheckBox();
		 add (checkbox, BorderLayout.WEST);
         add (defaultComp, BorderLayout.CENTER);
	 }
	
	 @Override
	 public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

		// JCheckBox checkbox = (JCheckBox) value;
		defaultComp.getListCellRendererComponent (list, value, index, isSelected, cellHasFocus);

		checkbox.setSelected (isSelected);
		Component[] comps = getComponents();
		for (int i=0; i<comps.length; i++) {
            comps[i].setForeground (listForeground);
            comps[i].setBackground (listBackground);
        }
		
		return this;
	}
	
}
