package org.openstreetmap.josm.plugins.piclayer.gui.autocalibrate;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;

public class SelectLayerView {

	private String[] labels;
	private JList<?> list;
	private JFrame frame;
	private JScrollPane scrollPane;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	private Container contentPane;


	public SelectLayerView(){
		labels = new String[10];
		getLayerNames();
		list = new JList<Object>(labels);

		frame = new JFrame("Layer Selector");
	    frame.setSize(400, 200);
	    frame.setLocationRelativeTo(frame.getOwner());

		contentPane = frame.getContentPane();

		setScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);

		setButtonBar();
	    setOKButton();
	    setCancelButton();
	    contentPane.add(buttonBar, BorderLayout.SOUTH);
	}

	private void getLayerNames() {
		java.util.List<Layer> layer = MainApplication.getLayerManager().getLayers();
		for(int i=0; i<layer.size(); i++)	labels[i] = layer.get(i).getName();
	}

	public void setVisible(boolean value) {
		frame.setVisible(value);
	}

	public JFrame getFrame() {
		return this.frame;
	}

	public JList<?> getList(){
		return this.list;
	}

	// COMPONENTS

	private void setScrollPane() {
		scrollPane = new JScrollPane(list);
	}

	private void setButtonBar() {
		buttonBar = new JPanel();
		buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
	    buttonBar.setLayout(new GridBagLayout());
	    ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
	    ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};
	}

	private void setOKButton() {
		okButton = new JButton();
		okButton.setText(tr("OK"));
	    buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
	          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	          new Insets(0, 0, 0, 5), 0, 0));
	}

	private void setCancelButton() {
		cancelButton = new JButton();
		cancelButton.setText(tr("Cancel"));
	    buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
	          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
	          new Insets(0, 0, 0, 0), 0, 0));
	}

	// LISTENER

	public void setOkButtonListener(ActionListener l) {
	    this.okButton.addActionListener(l);
	}

	public void setCancelButtonListener(ActionListener l) {
	    this.cancelButton.addActionListener(l);
	}

}
