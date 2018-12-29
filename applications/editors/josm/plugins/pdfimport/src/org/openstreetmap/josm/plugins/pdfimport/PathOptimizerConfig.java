/**
 * License: GPL. For details, see LICENSE file.
 */
package org.openstreetmap.josm.plugins.pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * @author Nzara
 *
 */
public class PathOptimizerConfig {
	/*
	 * encapsulate options for Path optimizer
	 * provide GUI
	 */
	public GuiFieldBool debugModeCheck;
	public GuiFieldBool mergeCloseNodesCheck;
	public GuiFieldDouble mergeCloseNodesTolerance;
	public GuiFieldBool removeSmallObjectsCheck;
	public GuiFieldDouble removeSmallObjectsSize;
	public JTextField colorFilterColor;
	public GuiFieldBool colorFilterCheck;
	public GuiFieldBool removeParallelSegmentsCheck;
	public GuiFieldDouble removeParallelSegmentsTolerance;
	public GuiFieldBool removeLargeObjectsCheck;
	public GuiFieldDouble removeLargeObjectsSize;
	public GuiFieldBool limitPathCountCheck;
	public GuiFieldInteger limitPathCount;
	public GuiFieldBool splitOnColorChangeCheck;
	public GuiFieldBool splitOnShapeClosedCheck;
	public GuiFieldBool splitOnSingleSegmentCheck;
	public GuiFieldBool splitOnOrthogonalCheck;
	protected JPanel panel;

	public PathOptimizerConfig() {
		build();
	}

	public JComponent getComponent() {
		return panel;
	}
	
	public boolean save() {
		/*
		 * save to preferences
		 */
		Preferences.setDebugTags(debugModeCheck.getValue());

		Preferences.setMergeNodesValue(mergeCloseNodesTolerance.getValue());
		Preferences.setMergeNodes(mergeCloseNodesCheck.getValue());

		Preferences.setRemoveSmallValue(removeSmallObjectsSize.getValue());
		Preferences.setRemoveSmall(removeSmallObjectsCheck.getValue());

		Preferences.setRemoveLargeValue(removeLargeObjectsSize.getValue());
		Preferences.setRemoveLarge(removeLargeObjectsCheck.getValue());

		Preferences.setLimitColorValue(colorFilterColor.getText());
		Preferences.setLimitColor(colorFilterCheck.getValue());

		Preferences.setRemoveParallelValue(removeParallelSegmentsTolerance.getValue());
		Preferences.setRemoveParallel(removeParallelSegmentsCheck.getValue());

		Preferences.setLimitPathValue(limitPathCount.getValue());
		Preferences.setLimitPath(limitPathCountCheck.getValue());

		Preferences.setLayerAttribChange(splitOnColorChangeCheck.getValue());
		Preferences.setLayerClosed(splitOnShapeClosedCheck.getValue());

		Preferences.setLayerSegment(splitOnSingleSegmentCheck.getValue());
		Preferences.setLayerOrtho(splitOnOrthogonalCheck.getValue());
		
		return true;
	}

	private void build() {

		debugModeCheck = new GuiFieldBool(tr("Debug info"), Preferences.isDebugTags());

		mergeCloseNodesTolerance = new GuiFieldDouble(Preferences.getMergeNodesValue());
		mergeCloseNodesCheck = new GuiFieldBool(tr("Merge close nodes"), Preferences.isMergeNodes());
		mergeCloseNodesCheck.setCompanion(mergeCloseNodesTolerance);

		removeSmallObjectsSize = new GuiFieldDouble(Preferences.getRemoveSmallValue());
		removeSmallObjectsCheck = new GuiFieldBool(tr("Remove objects smaller than"),Preferences.isRemoveSmall());
		removeSmallObjectsCheck.setCompanion(removeSmallObjectsSize);

		removeLargeObjectsSize = new GuiFieldDouble((Preferences.getRemoveLargeValue()));
		removeLargeObjectsCheck = new GuiFieldBool(tr("Remove objects larger than"),Preferences.isRemoveLarge());
		removeLargeObjectsCheck.setCompanion(removeLargeObjectsSize);

		colorFilterColor = new GuiFieldHex(Preferences.getLimitColorValue());
		colorFilterCheck = new GuiFieldBool(tr("Only this color"), Preferences.isLimitColor());
		colorFilterCheck.setCompanion(colorFilterColor);

		removeParallelSegmentsTolerance = new GuiFieldDouble((Preferences.getRemoveParallelValue()));
		removeParallelSegmentsCheck = new GuiFieldBool(tr("Remove parallel lines"),Preferences.isRemoveParallel());
		removeParallelSegmentsCheck.setCompanion(removeParallelSegmentsTolerance);

		limitPathCount = new GuiFieldInteger((Preferences.getLimitPathValue()));
		limitPathCountCheck = new GuiFieldBool(tr("Take only first X paths"),Preferences.isLimitPath());
		limitPathCountCheck.setCompanion(limitPathCount);

		splitOnColorChangeCheck = new GuiFieldBool(tr("Color/width change"),Preferences.isLayerAttribChange());
		splitOnShapeClosedCheck = new GuiFieldBool(tr("Shape closed"), Preferences.isLayerClosed());

		splitOnSingleSegmentCheck = new GuiFieldBool(tr("Single segments"), Preferences.isLayerSegment());
		splitOnOrthogonalCheck = new GuiFieldBool(tr("Orthogonal shapes"), Preferences.isLayerOrtho());

		panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(tr("Import settings")));

		GridBagConstraints cBasic = new GridBagConstraints();
		cBasic.gridx = GridBagConstraints.RELATIVE;
		cBasic.gridy = GridBagConstraints.RELATIVE;
		cBasic.insets = new Insets(0, 0, 0, 4);
		cBasic.anchor = GridBagConstraints.LINE_START;
		cBasic.fill = GridBagConstraints.HORIZONTAL;
		cBasic.gridheight = 1;
		cBasic.gridwidth = 1;
		cBasic.ipadx = 0;
		cBasic.ipady = 0;
		cBasic.weightx = 0.0;
		cBasic.weighty = 0.0;

		GridBagConstraints cLeft = (GridBagConstraints) cBasic.clone();
		cLeft.gridx = 0;

		GridBagConstraints cMiddle = (GridBagConstraints) cBasic.clone();
		cMiddle.gridx = 1;
		cMiddle.anchor = GridBagConstraints.LINE_END;

		GridBagConstraints cRight = (GridBagConstraints) cBasic.clone();
		cRight.gridx = 2;

		panel.add(mergeCloseNodesCheck, cLeft);
		panel.add(new JLabel(tr("Tolerance:"),SwingConstants.RIGHT), cMiddle);
		panel.add(mergeCloseNodesTolerance, cRight);

		panel.add(removeSmallObjectsCheck, cLeft);
		panel.add(new JLabel(tr("Tolerance:"),SwingConstants.RIGHT), cMiddle);
		panel.add(removeSmallObjectsSize, cRight);

		panel.add(removeLargeObjectsCheck, cLeft);
		panel.add(new JLabel(tr("Tolerance:"),SwingConstants.RIGHT), cMiddle);
		panel.add(removeLargeObjectsSize, cRight);

		panel.add(removeParallelSegmentsCheck, cLeft);
		panel.add(new JLabel(tr("Max distance:"),SwingConstants.RIGHT), cMiddle);
		panel.add(removeParallelSegmentsTolerance, cRight);

		panel.add(limitPathCountCheck, cLeft);
		panel.add(limitPathCount, cRight);

		panel.add(colorFilterCheck, cLeft);
		panel.add(colorFilterColor, cRight);

		panel.add(debugModeCheck, cLeft);

		cLeft.gridy = 8; panel.add(new JLabel(tr("Introduce separate layers for:")), cLeft);
		cMiddle.gridy = 8; panel.add(splitOnShapeClosedCheck, cMiddle);
		cRight.gridy = 8; panel.add(splitOnSingleSegmentCheck, cRight);
		cMiddle.gridy = 9; panel.add(splitOnColorChangeCheck, cMiddle);
		cRight.gridy = 9;panel.add(splitOnOrthogonalCheck, cRight);
	}
}
