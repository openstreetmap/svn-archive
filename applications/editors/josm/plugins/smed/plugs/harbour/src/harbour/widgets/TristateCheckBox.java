package harbour.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;

public class TristateCheckBox extends JCheckBox {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static class State {private State() {} }
	public static final State NOT_SELECTED = new State();
	public static final State SELECTED = new State();
	public static final State DONT_CARE = new State();
	
	private TristateDecorator model;
	private TristateCheckBox box;
	
	Icon d = new ImageIcon(getClass().getResource("/images/qm_12x12.png"));
	Icon s = new ImageIcon(getClass().getResource("/images/cm_12x12.png"));
	Icon n = new ImageIcon(getClass().getResource("/images/xm_12x12.png"));
	
	@SuppressWarnings("serial")
	public TristateCheckBox(String text, Icon icon, State initial) {
		super(text,icon);
		
		box = this;
		
		// add a listener for when the mouse is pressed
		super.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				grabFocus();
				model.nextState();
				
				fireItemStateChanged(new ItemEvent(box,
						ItemEvent.ITEM_STATE_CHANGED,
						box,
						box.isSelected() ?  ItemEvent.SELECTED : ItemEvent.DESELECTED));
				
				if(model.getState() == NOT_SELECTED) setIcon(n);
				if(model.getState() == DONT_CARE) setIcon(d);
				if(model.getState() == SELECTED) setIcon(s);
			}
		} );
		
		// reset the keyboard action map
		ActionMap map = new ActionMapUIResource();
		map.put("pressed", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				grabFocus();
				model.nextState();
			}
			
		});
		
		map.put("released", null);
		SwingUtilities.replaceUIActionMap(this, map);
		
		// set the model to the adapted model
		model = new TristateDecorator(getModel());
		setModel(model);
		setState(initial);
	}
	
	public TristateCheckBox(String text, State initial) {
		this(text, null, initial);
	}
	
	public TristateCheckBox(String text) {
		this(text, DONT_CARE);
	}
	
	public TristateCheckBox(){
		this(null);
	}

	/** No one may add mouse listeners, not even Swing! */
	public void addMouseListener(MouseListener l) { }
	
	/**
	 * Set the new state to either SELECTED, NOT_SELECTED or
	 * DONT_CARE. If state == null, it is treated as DONT_CARE
	 */
	public void setState(State state) {
		if(state == NOT_SELECTED)	setIcon(n);
		if(state == DONT_CARE) 	setIcon(d);
		if(state == SELECTED) 	setIcon(s);
		
		fireItemStateChanged(new ItemEvent(this,
				ItemEvent.ITEM_STATE_CHANGED,
				this,
				this.isSelected() ?  ItemEvent.SELECTED : ItemEvent.DESELECTED));
		
		model.setState(state);
		}
	
	/**
	 * Return the current state, which is determined by the
	 * selection status of the model
	 */
	public State getState() { return model.getState(); }
	
	public void setState(boolean b) {
		if(b)	setState(SELECTED);
		else	setState(NOT_SELECTED);
	}
	
	/**
	 * Exactly which Design Pattern is this? Is it an Adapter,
	 * a Proxy or a Decorator? In this case, my vote lies with the
	 * Decorator, because we are extending functionality and
	 * "decorating" the original model with a more powerful model
	 */
	private class TristateDecorator implements ButtonModel {

		private final ButtonModel other;
		
		private TristateDecorator(ButtonModel other) {
			this.other = other;
		}
		
		private void setState(State state) {
			if(state == NOT_SELECTED) {
				other.setArmed(false);
				setPressed(false);
				setSelected(false);
			} else if(state == SELECTED) {
				other.setArmed(false);
				setPressed(false);
				setSelected(true);
			} else { // either "null" or DONT_CARE
				other.setArmed(true);
				setPressed(true);
				setSelected(true);
			}
		}
		
		/**
		 * The current state is embedded in the selection / armed
		 * state of the model.
		 * 
		 * We return the SELECTED state when the checkbox is selected
		 * but not armed, DONT_CARE state when the checkbox is
		 * selected and armed (grey) and NOT_SELECTED when the
		 * checkbox is deselected
		 */
		private State getState() {
			if(isSelected() && !isArmed()) 		return SELECTED;
			else if (isSelected() && isArmed()) return DONT_CARE;
			else 								return NOT_SELECTED;
			}

		/** we rotate between NOT_SELECTED, SELECTED, and DONT_CARE */
		private void nextState() {
			State current = getState();
			
			if(current == NOT_SELECTED) 	setState(DONT_CARE);
			else if(current == SELECTED) 	setState(NOT_SELECTED);
			else if(current == DONT_CARE) 	setState(SELECTED);
		}
		
		@Override
		public Object[] getSelectedObjects() {
			return other.getSelectedObjects();
		}

		@Override
		public void addActionListener(ActionListener l) {
			other.addActionListener(l);
		}

		@Override
		public void addChangeListener(ChangeListener l) {
			other.addChangeListener(l);
		}

		@Override
		public void addItemListener(ItemListener l) {
			other.addItemListener(l);
		}

		@Override
		public String getActionCommand() {
			return other.getActionCommand();
		}

		@Override
		public int getMnemonic() {
			return other.getMnemonic();
		}

		@Override
		public boolean isArmed() {
			return other.isArmed();
		}

		@Override
		public boolean isEnabled() {
			return other.isEnabled();
		}

		@Override
		public boolean isPressed() {
			return other.isPressed();
		}

		@Override
		public boolean isRollover() {
			return other.isRollover();
		}

		@Override
		public boolean isSelected() {
			return other.isSelected();
		}

		@Override
		public void removeActionListener(ActionListener l) {
			other.removeActionListener(l);
		}

		@Override
		public void removeChangeListener(ChangeListener l) {
			other.removeChangeListener(l);
		}

		@Override
		public void removeItemListener(ItemListener l) {
			other.removeItemListener(l);
		}

		@Override
		public void setActionCommand(String s) {
			other.setActionCommand(s);
		}

		/** Filter: No one may change the armed status except us */
		@Override
		public void setArmed(boolean b) {
		}

		@Override
		public void setEnabled(boolean b) {
			setFocusable(b);
			other.setEnabled(b);
		}

		@Override
		public void setGroup(ButtonGroup group) {
			other.setGroup(group);
		}

		@Override
		public void setMnemonic(int key) {
			other.setMnemonic(key);
		}

		@Override
		public void setPressed(boolean b) {
			other.setPressed(b);
		}

		@Override
		public void setRollover(boolean b) {
			other.setRollover(b);
		}

		@Override
		public void setSelected(boolean b) {
			other.setSelected(b);
		}
	}
}
