/**
 * 
 */
package terracer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openstreetmap.josm.data.osm.Way;

/**
 * The Class HouseNumberInputHandler contains all the logic
 * behind the house number input dialog.
 * 
 * From a refactoring viewpoint, this class is indeed more interested in the fields
 * of the HouseNumberInputDialog. This is desired design, as the HouseNumberInputDialog 
 * is already cluttered with auto-generated layout code.  
 * 
 * @author casualwalker
 */
public class HouseNumberInputHandler implements ChangeListener, ItemListener,
		ActionListener, FocusListener {

	TerracerAction terracerAction;
	Way way;
	HouseNumberInputDialog dialog;

	public HouseNumberInputHandler(TerracerAction terracerAction, Way way,
			String title) {
		this.terracerAction = terracerAction;
		this.way = way;
		dialog = new HouseNumberInputDialog(null);
		dialog.addHandler(this);

		dialog.setVisible(true);
		dialog.setTitle(title);

	}

	private void validateInput() {
		boolean isOk = true;
		StringBuffer message = new StringBuffer();

		isOk = isOk && checkNumberOrder(message);
		isOk = isOk && checkSegmentsFromHousenumber(message);
		isOk = isOk && checkSegments(message);
		isOk = isOk
				&& checkNumberStringField(dialog.lo, "Lowest number", message);
		isOk = isOk
				&& checkNumberStringField(dialog.hi, "Highest number", message);
		isOk = isOk
				&& checkNumberStringField(dialog.segments, "Segments", message);

		if (isOk) {
			dialog.okButton.setEnabled(true);
			dialog.messageLabel.setForeground(Color.black);
			dialog.messageLabel.setText(HouseNumberInputDialog.DEFAULT_MESSAGE);

		} else {
			dialog.okButton.setEnabled(false);
			dialog.messageLabel.setForeground(Color.red);
			dialog.messageLabel.setText(message.toString());
		}
	}

	private boolean checkNumberOrder(StringBuffer message) {
		if (numberFrom() != null && numberTo() != null) {
			if (numberFrom().intValue() > numberTo().intValue()) {
				appendMessageNewLine(message);
				message
						.append("Lowest housenumber cannot be higher than highest housenumber");
				return false;
			}
		}
		return true;
	}

	private boolean checkSegmentsFromHousenumber(StringBuffer message) {
		dialog.segments.setEditable(true);

		if (numberFrom() != null && numberTo() != null) {
			int segments = numberTo().intValue() - numberFrom().intValue();

			if (segments % stepSize() != 0) {
				appendMessageNewLine(message);
				message.append("Housenumbers do not match odd/even setting");
				return false;
			}

			int steps = segments / stepSize();
			dialog.segments.setText(String.valueOf(steps));
			dialog.segments.setEditable(false);

		}
		return true;
	}

	private boolean checkSegments(StringBuffer message) {
		if (segments() == null || segments().intValue() < 1) {
			appendMessageNewLine(message);
			message.append("Segment must be a number greater 1");
			return false;

		}
		return true;
	}

	/**
	 * Check, if a string field contains a positive integer. 
	 * 
	 * @param field the field
	 * @param label the label
	 * @param message the message
	 * 
	 * @return true, if successful
	 */
	private boolean checkNumberStringField(JTextField field, String label,
			StringBuffer message) {
		String content = field.getText();
		if (content != null && !content.isEmpty()) {
			try {
				int i = Integer.parseInt(content);
				if (i < 0) {
					appendMessageNewLine(message);
					message.append(label + " must be greater than 0");
					return false;
				}
			} catch (NumberFormatException e) {
				appendMessageNewLine(message);
				message.append(label + " is not a number");
				return false;
			}

		}
		return true;
	}

	/**
	 * @param message
	 */
	private void appendMessageNewLine(StringBuffer message) {
		if (message.length() > 0) {
			message.append("\n");
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		validateInput();

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		validateInput();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() instanceof JButton) {
			JButton button = (JButton) e.getSource();
			if ("OK".equals(button.getName())) {
				terracerAction.terraceBuilding(way, segments(), numberFrom(),
						numberTo(), stepSize(), streetName());

				this.dialog.dispose();
			} else if ("CANCEL".equals(button.getName())) {
				this.dialog.dispose();
			}
		} else {
			validateInput();
		}

	}

	public int stepSize() {
		return (dialog.interpolation.getSelectedItem() == tr("All")) ? 1 : 2;
	}

	public Integer segments() {
		try {
			return Integer.parseInt(dialog.segments.getText());
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	public Integer numberFrom() {
		try {
			return Integer.parseInt(dialog.lo.getText());
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	public Integer numberTo() {
		try {
			return Integer.parseInt(dialog.hi.getText());
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	public String streetName() {
		// Object selected = street.getSelectedItem();
		Object selected = dialog.street.getSelectedItem();
		if (selected == null) {
			return null;
		} else {
			String name = selected.toString();
			if (name.length() == 0) {
				return null;
			} else {
				return name;
			}
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		validateInput();
	}

	@Override
	public void focusLost(FocusEvent e) {
		validateInput();
	}

	/**
	 * Indicates, if house numbers should be used (instead of segments).
	 * 
	 * @return true, if if both number values are set to a number.
	 */
	private boolean useHouseNumbers() {
		return numberFrom() != null && numberTo() != null;

	}
}
