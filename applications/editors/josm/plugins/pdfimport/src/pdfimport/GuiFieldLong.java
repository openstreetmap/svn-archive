// License: GPL. For details, see LICENSE file.

package pdfimport;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;


public class GuiFieldLong extends GuiFieldString {

	private long value;
	public CheckLong checker;

	public GuiFieldLong() {
		super();
		addFocusListener(checker = new CheckLong());
		setValue(0);
	}

	@SuppressWarnings("unused")
	private GuiFieldLong(String text) {
		super(text);
		addFocusListener(checker = new CheckLong());
	}

	public GuiFieldLong(long v) {
		super();
		addFocusListener(checker = new CheckLong());
		setValue(v);
	}

	public void setValue(long v) {
		super.setText(Long.toString(v));
		value=v;
		this.checker.check(this);
	}

	public long getValue() throws NumberFormatException {
		if (!dataValid) throw new NumberFormatException();
		return value;
	}

	public class CheckLong implements FocusListener {

		@Override
		public void focusGained(FocusEvent e) {
		}

		@Override
		public void focusLost(FocusEvent event) {
			check((GuiFieldLong) event.getSource());
		}

		public void check(GuiFieldLong field) {
			try {
				value = Long.valueOf(field.getText());
				dataValid = true;
				field.setBorder(defaultBorder);
			} catch (NumberFormatException e) {
				field.setBorder(BorderFactory.createLineBorder(Color.red));
				dataValid = false;
			}
		}
	}
}