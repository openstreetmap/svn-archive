/**
 * 
 */
package terracer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.widgets.AutoCompleteComboBox;

/**
 * The HouseNumberInputDialog is the layout of the house number input logic.
 * Created with the Eclipse Visual Editor.
 * 
 *  This dialog is concerned with the layout.
 * 
 * @author casualwalker
 *
 */
public class HouseNumberInputDialog extends JDialog {

	protected static final String DEFAULT_MESSAGE = "Enter housenumbers or amount of segments";
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JPanel inputPanel = null;
	private JPanel buttonPanel = null;
	private JLabel loLabel = null;
	JTextField lo = null;
	private JLabel hiLabel = null;
	JTextField hi = null;
	private JLabel streetLabel = null;
	AutoCompleteComboBox street;
	// JTextField street = null;
	private JLabel segmentsLabel = null;
	JTextField segments = null;
	JTextArea messageLabel = null;
	JButton okButton = null;
	JButton cancelButton = null;
	private JLabel interpolationLabel = null;
	Choice interpolation = null;

	/**
	 * @param owner
	 */
	public HouseNumberInputDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setTitle("Terrace a house");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			messageLabel = new JTextArea();
			messageLabel.setText(DEFAULT_MESSAGE);
			messageLabel.setAutoscrolls(true);

			messageLabel.setLineWrap(true);
			messageLabel.setRows(2);
			messageLabel.setBackground(new Color(238, 238, 238));
			messageLabel.setEditable(false);
			jContentPane = new JPanel();
			jContentPane.setLayout(new BoxLayout(getJContentPane(),
					BoxLayout.Y_AXIS));
			jContentPane.add(getInputPanel(), null);
			jContentPane.add(messageLabel, null);
			jContentPane.add(getButtonPanel(), null);

		}
		return jContentPane;
	}

	/**
	 * This method initializes inputPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getInputPanel() {
		if (inputPanel == null) {
			interpolationLabel = new JLabel();
			interpolationLabel.setText("Interpolation");
			segmentsLabel = new JLabel();
			segmentsLabel.setText("Segments");
			streetLabel = new JLabel();
			streetLabel.setText("Street");
			hiLabel = new JLabel();
			hiLabel.setText("Highest Number");
			loLabel = new JLabel();
			loLabel.setText("Lowest Number");
			loLabel.setPreferredSize(new Dimension(111, 16));
			loLabel.setToolTipText("Lowest housenumber of the terraced house");
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(5);
			gridLayout.setColumns(2);
			inputPanel = new JPanel();
			inputPanel.setLayout(gridLayout);
			inputPanel.add(loLabel, null);

			inputPanel.add(getLo(), null);
			inputPanel.add(hiLabel, null);
			inputPanel.add(getHi(), null);
			inputPanel.add(interpolationLabel, null);
			inputPanel.add(getInterpolation(), null);
			inputPanel.add(segmentsLabel, null);
			inputPanel.add(getSegments(), null);
			inputPanel.add(streetLabel, null);
			inputPanel.add(getStreet(), null);
		}
		return inputPanel;
	}

	/**
	 * This method initializes buttonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout());
			buttonPanel.add(getOkButton(), null);
			buttonPanel.add(getCancelButton(), null);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes lo	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getLo() {
		if (lo == null) {
			lo = new JTextField();
			lo.setText("");
		}
		return lo;
	}

	/**
	 * This method initializes hi	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getHi() {
		if (hi == null) {
			hi = new JTextField();
			hi.setText("");
		}
		return hi;
	}

	/**
	 * This method initializes street	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private AutoCompleteComboBox getStreet() {

		if (street == null) {
			final TreeSet<String> names = createAutoCompletionInfo();

			street = new AutoCompleteComboBox();
			street.setPossibleItems(names);
			street.setEditable(true);
			street.setSelectedItem(null);

		}
		return street;
	}

	/**
	 * This method initializes segments	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getSegments() {
		if (segments == null) {
			segments = new JTextField();
			segments.setText("1");
		}
		return segments;
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText("OK");
			okButton.setName("OK");
		}
		return okButton;
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText("Cancel");
			cancelButton.setName("CANCEL");
		}
		return cancelButton;
	}

	/**
	 * This method initializes interpolation	
	 * 	
	 * @return java.awt.Choice	
	 */
	private Choice getInterpolation() {
		if (interpolation == null) {
			interpolation = new Choice();
			interpolation.add(tr("All"));
			interpolation.add(tr("Even/Odd"));
		}
		return interpolation;
	}

	public void addHandler(HouseNumberInputHandler handler) {
		this.hi.addActionListener(handler);
		this.hi.addFocusListener(handler);

		this.lo.addActionListener(handler);
		this.lo.addFocusListener(handler);

		this.segments.addActionListener(handler);
		this.segments.addFocusListener(handler);

		this.okButton.addActionListener(handler);
		this.cancelButton.addActionListener(handler);

		this.interpolation.addItemListener(handler);

	}

	/**
	 * Generates a list of all visible names of highways in order to do
	 * autocompletion on the road name.
	 */
	TreeSet<String> createAutoCompletionInfo() {
		final TreeSet<String> names = new TreeSet<String>();
		for (OsmPrimitive osm : Main.main.getCurrentDataSet()
				.allNonDeletedPrimitives()) {
			if (osm.getKeys() != null && osm.keySet().contains("highway")
					&& osm.keySet().contains("name")) {
				names.add(osm.get("name"));
			}
		}
		return names;
	}

}
