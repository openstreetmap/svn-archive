package org.openstreetmap.josm.plugins.graphview.plugin.dialogs;

import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.MAX_INCLINE_DOWN;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.MAX_INCLINE_UP;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.MAX_TRACKTYPE;
import static org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes.SURFACE_BLACKLIST;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.plugins.graphview.core.access.AccessType;
import org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyType;
import org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.PreferenceAccessParameters;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.VehiclePropertyStringParser.PropertyValueSyntaxException;

public class AccessParameterDialog extends JDialog {

	public static interface BookmarkAction {
		public void execute(String name, PreferenceAccessParameters parameters);
	}

	/**
	 * map that contains all float value vehicle properties (as those can be treated uniformly)
	 * and their labels
	 */
	private static final Map<VehiclePropertyType<Float>, String> FLOAT_PROPERTIES;

	static {
		FLOAT_PROPERTIES = new LinkedHashMap<VehiclePropertyType<Float>, String>();
		FLOAT_PROPERTIES.put(VehiclePropertyTypes.HEIGHT, "height (m)");
		FLOAT_PROPERTIES.put(VehiclePropertyTypes.WIDTH, "width (m)");
		FLOAT_PROPERTIES.put(VehiclePropertyTypes.LENGTH, "length (m)");
		FLOAT_PROPERTIES.put(VehiclePropertyTypes.SPEED, "speed (km/h)");
		FLOAT_PROPERTIES.put(VehiclePropertyTypes.WEIGHT, "weight (t)");
		FLOAT_PROPERTIES.put(VehiclePropertyTypes.AXLELOAD, "axleload (t)");
	}

	private static final Collection<Character> FORBIDDEN_CHARS =
		Arrays.asList(',', ';', '{', '}', '=', '|');

	private class BookmarkNamePanel extends JPanel {

		private final JTextField bookmarkNameTextField;

		public BookmarkNamePanel(String initialName) {
			super();
			this.setBorder(BorderFactory.createTitledBorder("bookmark name"));

			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

			bookmarkNameTextField = new JTextField(initialName);
			this.add(bookmarkNameTextField);

		}

		public String getBookmarkName() {

			String name = bookmarkNameTextField.getText();

			if (existingBookmarkNames.contains(name)) {
				JOptionPane.showMessageDialog(this, "Bookmark name already exists!");
				return null;
			}

			for (char nameChar : name.toCharArray()) {
				if (FORBIDDEN_CHARS.contains(nameChar)) {
					JOptionPane.showMessageDialog(this, "Bookmark name must not contain '" +
							nameChar + "'!");
					return null;
				}
			}

			return name;
		}
	}

	private static class AccessClassPanel extends JPanel {

		private final JTextField accessClassTextField;

		public AccessClassPanel(PreferenceAccessParameters initialParameters) {
			super();
			this.setBorder(BorderFactory.createTitledBorder("access class"));

			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

			accessClassTextField = new JTextField(initialParameters.getAccessClass());
			this.add(accessClassTextField);

		}

		public String getAccessClass() {

			String name = accessClassTextField.getText();

			for (char nameChar : name.toCharArray()) {
				if (FORBIDDEN_CHARS.contains(nameChar)) {
					JOptionPane.showMessageDialog(this, "Access class must not contain '" +
							nameChar + "'!");
					return null;
				}
			}

			return name;
		}
	}

	private static class AccessTypesPanel extends JPanel {

		private static final int COLS = 4;

		private final Map<AccessType, JCheckBox> accessTypeCheckBoxes =
			new EnumMap<AccessType, JCheckBox>(AccessType.class);

		public AccessTypesPanel(PreferenceAccessParameters initialParameters) {
			super();
			this.setBorder(BorderFactory.createTitledBorder("access types"));

			this.setLayout(
					new GridLayout(((COLS-1 + AccessType.values().length) / COLS), COLS));

			for (AccessType accessType : AccessType.values()) {
				JCheckBox checkBox = new JCheckBox(accessType.toString());
				checkBox.setSelected(initialParameters.getAccessTypeUsable(accessType));
				accessTypeCheckBoxes.put(accessType, checkBox);
				this.add(checkBox);
			}

		}

		public Collection<AccessType> getUsableAccessTypes() {

			Collection<AccessType> usableAccessTypes = new LinkedList<AccessType>();

			for (AccessType accessType : AccessType.values()) {
				if (accessTypeCheckBoxes.get(accessType).isSelected()) {
					usableAccessTypes.add(accessType);
				}
			}

			return usableAccessTypes;
		}
	}

	private static class VehiclePropertiesPanel extends JPanel {

		private static final int COLS = 2;

		private final Map<VehiclePropertyType<Float>, JTextField> floatPropertyTextFields =
			new HashMap<VehiclePropertyType<Float>, JTextField>();

		public VehiclePropertiesPanel(PreferenceAccessParameters initialParameters) {
			super();
			this.setBorder(BorderFactory.createTitledBorder("vehicle properties"));

			this.setLayout(new GridLayout(((COLS-1 + FLOAT_PROPERTIES.size()) / COLS),
					2*COLS));

			for (VehiclePropertyType<Float> vehicleProperty : FLOAT_PROPERTIES.keySet()) {

				JLabel label = new JLabel(FLOAT_PROPERTIES.get(vehicleProperty));
				this.add(label);

				JTextField textField = new JTextField();

				String vehiclePropertyString =
					initialParameters.getVehiclePropertyString(vehicleProperty);
				if (vehiclePropertyString != null) {
					textField.setText(vehiclePropertyString);
				}

				floatPropertyTextFields.put(vehicleProperty, textField);
				this.add(textField);
			}

		}

		public Map<VehiclePropertyType<?>, String> getVehiclePropertyStrings() {

			Map<VehiclePropertyType<?>, String> vehiclePropertyStrings =
				new HashMap<VehiclePropertyType<?>, String>();

			for (VehiclePropertyType<Float> vehicleProperty : floatPropertyTextFields.keySet()) {
				String textFieldContent = floatPropertyTextFields.get(vehicleProperty).getText();
				if (textFieldContent.trim().length() > 0) {
					vehiclePropertyStrings.put(vehicleProperty, textFieldContent);
				}
			}

			return vehiclePropertyStrings;
		}
	}

	private static class RoadQualityPanel extends JPanel {

		private JTextField inclineUpTextField;
		private JTextField inclineDownTextField;
		private JTextField surfaceTextField;
		private JTextField tracktypeTextField;

		public RoadQualityPanel(PreferenceAccessParameters initialParameters) {
			super();
			this.setBorder(BorderFactory.createTitledBorder("road requirements"));


			this.setLayout(new GridLayout(4, 2));

			/* incline up */
			{
				JLabel inclineUpLabel = new JLabel("max. incline up (%, pos.)");
				inclineUpLabel.setToolTipText("maximum incline the vehicle can go up");
				this.add(inclineUpLabel);

				inclineUpTextField = new JTextField();

				String vehiclePropertyString =
					initialParameters.getVehiclePropertyString(MAX_INCLINE_UP);
				if (vehiclePropertyString != null) {
					inclineUpTextField.setText(vehiclePropertyString);
				}
				inclineUpTextField.setToolTipText("maximum incline the vehicle can go up");

				this.add(inclineUpTextField);
			}

			/* incline down */
			{
				JLabel inclineDownLabel = new JLabel("max. incline down (%, pos.)");
				inclineDownLabel.setToolTipText("maximum incline the vehicle can go down");
				this.add(inclineDownLabel);

				inclineDownTextField = new JTextField();

				String vehiclePropertyString =
					initialParameters.getVehiclePropertyString(MAX_INCLINE_DOWN);
				if (vehiclePropertyString != null) {
					inclineDownTextField.setText(vehiclePropertyString);
				}
				inclineDownTextField.setToolTipText("maximum incline the vehicle can go down");

				this.add(inclineDownTextField);
			}

			/* surface */
			{
				JLabel surfaceLabel = new JLabel("surface blacklist");
				surfaceLabel.setToolTipText("list of surfaces the vehicle cannot use, "
						+ "values are separated by semicolons (;)");
				this.add(surfaceLabel);

				surfaceTextField = new JTextField();

				String vehiclePropertyString =
					initialParameters.getVehiclePropertyString(SURFACE_BLACKLIST);

				if (vehiclePropertyString != null) {
					surfaceTextField.setText(vehiclePropertyString);
				}

				surfaceTextField.setToolTipText("list of surfaces the vehicle cannot use, "
						+ "values are separated by semicolons (;)");

				this.add(surfaceTextField);
			}

			/* tracktype */
			{
				JLabel tracktypeLabel = new JLabel("max. tracktype grade");
				tracktypeLabel.setToolTipText("worst tracktype (1-5) the vehicle can still use,"
						+ " 0 for none");
				this.add(tracktypeLabel);

				tracktypeTextField = new JTextField();

				String vehiclePropertyString =
					initialParameters.getVehiclePropertyString(MAX_TRACKTYPE);
				if (vehiclePropertyString != null) {
					tracktypeTextField.setText(vehiclePropertyString);
				}
				tracktypeTextField.setToolTipText("worst tracktype (1-5) the vehicle can still use,"
						+ " 0 for none");

				this.add(tracktypeTextField);
			}

		}

		public Map<VehiclePropertyType<?>, String> getVehiclePropertyStrings() {

			Map<VehiclePropertyType<?>, String> vehiclePropertyStrings =
				new HashMap<VehiclePropertyType<?>, String>();

			String incUpString = inclineUpTextField.getText();
			if (incUpString.trim().length() > 0) {
				vehiclePropertyStrings.put(MAX_INCLINE_UP, incUpString);
			}

			String incDownString = inclineDownTextField.getText();
			if (incDownString.trim().length() > 0) {
				vehiclePropertyStrings.put(MAX_INCLINE_DOWN, incDownString);
			}

			String surfaceString = surfaceTextField.getText();
			if (surfaceString.trim().length() > 0) {
				vehiclePropertyStrings.put(SURFACE_BLACKLIST, surfaceString);
			}

			String tracktypeString = tracktypeTextField.getText();
			if (tracktypeString.trim().length() > 0) {
				vehiclePropertyStrings.put(MAX_TRACKTYPE, tracktypeString);
			}

			return vehiclePropertyStrings;
		}
	}

	private class OkCancelPanel extends JPanel {

		public OkCancelPanel() {

			new BoxLayout(this, BoxLayout.X_AXIS);

			JButton okButton = new JButton(existingBookmark?"Change bookmark":"Create bookmark");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String bookmarkName = bookmarkNamePanel.getBookmarkName();
					if (bookmarkName != null) {
						PreferenceAccessParameters parameters = getAccessParameters();
						if (parameters != null) {
							okAction.execute(bookmarkName, parameters);
							AccessParameterDialog.this.dispose();
						}
					}
				}
			});
			this.add(okButton);

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AccessParameterDialog.this.dispose();
				}
			});
			this.add(cancelButton);

		}

	}

	private boolean existingBookmark = false;
	private final Collection<String> existingBookmarkNames;

	private final BookmarkAction okAction;

	private final BookmarkNamePanel bookmarkNamePanel;
	private final AccessClassPanel accessClassPanel;
	private final AccessTypesPanel accessTypesPanel;
	private final VehiclePropertiesPanel vehiclePropertiesPanel;
	private final RoadQualityPanel roadQualityPanel;

	public AccessParameterDialog(final Frame owner, boolean existingBookmark, String initialName,
			Collection<String> existingBookmarkNames,
			PreferenceAccessParameters initialAccessParameters, BookmarkAction okAction) {
		super(owner, "edit access parameters", true);

		this.existingBookmark = existingBookmark;
		this.existingBookmarkNames = existingBookmarkNames;
		this.okAction = okAction;

		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;

		bookmarkNamePanel = new BookmarkNamePanel(initialName);
		gbc.gridy = 0;
		layout.setConstraints(bookmarkNamePanel, gbc);
		this.add(bookmarkNamePanel);

		accessClassPanel = new AccessClassPanel(initialAccessParameters);
		gbc.gridy = 1;
		layout.setConstraints(accessClassPanel, gbc);
		this.add(accessClassPanel);

		accessTypesPanel = new AccessTypesPanel(initialAccessParameters);
		gbc.gridy = 2;
		layout.setConstraints(accessTypesPanel, gbc);
		this.add(accessTypesPanel);

		vehiclePropertiesPanel = new VehiclePropertiesPanel(initialAccessParameters);
		gbc.gridy = 3;
		layout.setConstraints(vehiclePropertiesPanel, gbc);
		this.add(vehiclePropertiesPanel);

		roadQualityPanel = new RoadQualityPanel(initialAccessParameters);
		gbc.gridy = 4;
		layout.setConstraints(roadQualityPanel, gbc);
		this.add(roadQualityPanel);

		JPanel okCancelPanel = new OkCancelPanel();
		gbc.gridy = 5;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		layout.setConstraints(okCancelPanel, gbc);
		this.add(okCancelPanel);

		this.pack();
	}

	private PreferenceAccessParameters getAccessParameters() {

		String accessClass = accessClassPanel.getAccessClass();
		Collection<AccessType> usableAccessTypes = accessTypesPanel.getUsableAccessTypes();
		Map<VehiclePropertyType<?>, String> vehiclePropertyStrings =
			vehiclePropertiesPanel.getVehiclePropertyStrings();
		Map<VehiclePropertyType<?>, String> additionalVehiclePropertyStrings =
			roadQualityPanel.getVehiclePropertyStrings();

		if (accessClass != null && usableAccessTypes != null && vehiclePropertyStrings != null
				&& additionalVehiclePropertyStrings != null) {

			vehiclePropertyStrings.putAll(additionalVehiclePropertyStrings);

			try {
				return new PreferenceAccessParameters(accessClass, usableAccessTypes, vehiclePropertyStrings);
			} catch (PropertyValueSyntaxException e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
				return null;
			}

		} else {
			return null;
		}
	}

}
