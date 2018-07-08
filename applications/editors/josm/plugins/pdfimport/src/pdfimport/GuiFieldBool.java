// License: GPL. For details, see LICENSE file.

package pdfimport;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GuiFieldBool extends JCheckBox {
	private JComponent companion;
	private boolean value;

	public JComponent getCompanion() {
		return companion;
	}

	public void setCompanion(JComponent c) {
		companion = c;
		if (companion != null) companion.setEnabled(isSelected());
	}

	public boolean getValue() {
		return super.isSelected();
	}

	public void setValue(boolean value) {
		this.value = value;
		super.setSelected(value);
	}

	public GuiFieldBool() {
		super();
		addChangeListener(new Monitor());
	}

	public GuiFieldBool(Action a) {
		super(a);
		addChangeListener(new Monitor());
	}

	public GuiFieldBool(Icon icon, boolean selected) {
		super(icon, selected);
		addChangeListener(new Monitor());
	}

	public GuiFieldBool(Icon icon) {
		super(icon);
		addChangeListener(new Monitor());
	}

	public GuiFieldBool(String text, boolean selected) {
		super(text, selected);
		addChangeListener(new Monitor());
	}

	public GuiFieldBool(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		addChangeListener(new Monitor());
	}

	public GuiFieldBool(String text, Icon icon) {
		super(text, icon);
		addChangeListener(new Monitor());
	}

	public GuiFieldBool(String text) {
		super(text);
		addChangeListener(new Monitor());
	}

	private class Monitor implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			GuiFieldBool o = (GuiFieldBool) e.getSource();
			value = o.isSelected();
			if (o.companion != null) o.companion.setEnabled(value);
		}
	}
}
