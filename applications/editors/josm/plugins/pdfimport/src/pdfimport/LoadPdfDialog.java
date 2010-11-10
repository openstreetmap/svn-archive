package pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.io.OsmExporter;

import pdfimport.pdfbox.PdfBoxParser;

public class LoadPdfDialog extends JFrame {

	private File fileName;
	private PathOptimizer data;
	private final FilePlacement placement;
	private OsmDataLayer layer;

	/**
	 * Combobox with all projections available
	 */
	private JComboBox projectionCombo;
	private JTextField minXField;
	private JTextField minYField;
	private JTextField minEastField;
	private JTextField minNorthField;
	private JButton getMinButton;
	private JButton okButton;
	private JButton cancelButton;
	private JButton getMaxButton;
	private JTextField maxNorthField;
	private JTextField maxEastField;
	private JTextField maxYField;
	private JTextField maxXField;
	private JButton loadFileButton;
	private JButton showButton;
	private JButton saveButton;
	private JCheckBox debugModeCheck;
	private JCheckBox mergeCloseNodesCheck;
	private JTextField mergeCloseNodesTolerance;
	private JCheckBox removeSmallObjectsCheck;
	private JTextField removeSmallObjectsSize;
	private JTextField colorFilterColor;
	private JCheckBox colorFilterCheck;
	private JCheckBox removeParallelSegmentsCheck;
	private JTextField removeParallelSegmentsTolerance;
	private JCheckBox removeLargeObjectsCheck;
	private JTextField removeLargeObjectsSize;

	public LoadPdfDialog() {

		this.placement = new FilePlacement();

		this.buildGUI();
		this.addListeners();
		this.removeLayer();
	}

	private void addListeners() {

		this.loadFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFilePressed();
			}
		});

		this.okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okPressed();
			}
		});

		this.saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				savePressed();
			}
		});

		this.showButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPressed();
			}
		});

		this.cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelPressed();
			}
		});

		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e) {
				cancelPressed();
			}
		});

		this.getMinButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getMinPressed();
			}
		});

		this.getMaxButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getMaxPressed();
			}
		});

	}

	private void buildGUI() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;c.gridwidth = 1;c.weightx =1; c.weighty = 1; c.fill = GridBagConstraints.BOTH;

		this.projectionCombo = new JComboBox();
		this.projectionCombo.addItem("Select projection...");
		for (Projection p: Projection.allProjections) {
			this.projectionCombo.addItem(p);
		}

		this.loadFileButton = new JButton(tr("Load file..."));
		this.okButton = new JButton(tr("Place"));
		this.saveButton = new JButton(tr("Save"));
		this.showButton = new JButton(tr("Show target"));
		this.cancelButton = new JButton(tr("Discard"));

		this.minXField = new JTextField(""+this.placement.minX);
		this.minYField = new JTextField(""+this.placement.minY);
		this.minEastField = new JTextField(""+this.placement.minEast);
		this.minNorthField = new JTextField(""+this.placement.minNorth);
		this.getMinButton = new JButton(tr("Take X and Y from selected node"));

		this.maxXField = new JTextField(""+this.placement.maxX);
		this.maxYField = new JTextField(""+this.placement.maxY);
		this.maxEastField = new JTextField(""+this.placement.maxEast);
		this.maxNorthField = new JTextField(""+this.placement.maxNorth);
		this.getMaxButton = new JButton(tr("Take X and Y from selected node"));

		this.debugModeCheck = new JCheckBox(tr("Debug info"));
		this.mergeCloseNodesCheck = new JCheckBox(tr("Merge close nodes"));
		this.mergeCloseNodesTolerance = new JTextField("1e-3");

		this.removeSmallObjectsCheck = new JCheckBox(tr("Remove objects smaller than"));
		this.removeSmallObjectsSize = new JTextField("1");

		this.removeLargeObjectsCheck = new JCheckBox(tr("Remove objects larger than"));
		this.removeLargeObjectsSize = new JTextField("10");


		this.colorFilterCheck = new JCheckBox(tr("Only this color"));
		this.colorFilterColor = new JTextField("#000000");

		this.removeParallelSegmentsCheck = new JCheckBox(tr("Remove parallel lines"));
		this.removeParallelSegmentsTolerance = new JTextField("3");


		JPanel configPanel = new JPanel(new GridBagLayout());
		configPanel.setBorder(BorderFactory.createTitledBorder(tr("Import settings")));
		c.gridx = 0; c.gridy = 0; c.gridwidth = 1;
		configPanel.add(this.mergeCloseNodesCheck, c);
		c.gridx = 1; c.gridy = 0; c.gridwidth = 1; c.anchor = GridBagConstraints.NORTHEAST;
		configPanel.add(new JLabel("Tolerance :"), c);
		c.gridx = 2; c.gridy = 0; c.gridwidth = 1; c.anchor = GridBagConstraints.NORTHWEST;
		configPanel.add(this.mergeCloseNodesTolerance, c);

		c.gridx = 0; c.gridy = 1; c.gridwidth = 1;
		configPanel.add(this.removeSmallObjectsCheck, c);
		c.gridx = 1; c.gridy = 1; c.gridwidth = 1; c.anchor = GridBagConstraints.NORTHEAST;
		configPanel.add(new JLabel("Tolerance :"), c);
		c.gridx = 2; c.gridy = 1; c.gridwidth = 1; c.anchor = GridBagConstraints.NORTHWEST;
		configPanel.add(this.removeSmallObjectsSize, c);

		c.gridx = 0; c.gridy = 2; c.gridwidth = 1;
		configPanel.add(this.removeLargeObjectsCheck, c);
		c.gridx = 1; c.gridy = 2; c.gridwidth = 1; c.anchor = GridBagConstraints.NORTHEAST;
		configPanel.add(new JLabel("Tolerance :"), c);
		c.gridx = 2; c.gridy = 2; c.gridwidth = 1; c.anchor = GridBagConstraints.NORTHWEST;
		configPanel.add(this.removeLargeObjectsSize, c);

		c.gridx = 0; c.gridy = 3; c.gridwidth = 1;
		configPanel.add(this.removeParallelSegmentsCheck, c);
		c.gridx = 1; c.gridy = 3; c.gridwidth = 1; c.anchor = GridBagConstraints.NORTHEAST;
		configPanel.add(new JLabel("Max distance :"), c);
		c.gridx = 2; c.gridy = 3; c.gridwidth = 1; c.anchor = GridBagConstraints.NORTHWEST;
		configPanel.add(this.removeParallelSegmentsTolerance, c);

		c.gridx = 0; c.gridy = 4; c.gridwidth = 1;
		configPanel.add(this.colorFilterCheck, c);
		c.gridx = 2; c.gridy = 4; c.gridwidth = 1;
		configPanel.add(this.colorFilterColor, c);

		c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
		configPanel.add(this.debugModeCheck, c);


		JPanel selectFilePanel = new JPanel(new GridBagLayout());
		selectFilePanel.setBorder(BorderFactory.createTitledBorder(tr("Load file")));
		c.gridx = 0; c.gridy = 0; c.gridwidth = 1;
		selectFilePanel.add(this.loadFileButton, c);


		JPanel projectionPanel = new JPanel(new GridBagLayout());
		projectionPanel.setBorder(BorderFactory.createTitledBorder(tr("Bind to coordinates")));

		c.gridx = 0; c.gridy = 0; c.gridwidth = 1;
		projectionPanel.add(new JLabel(tr("Projection:")), c);
		c.gridx = 1; c.gridy = 0; c.gridwidth = 1;
		projectionPanel.add(this.projectionCombo);

		c.gridx = 0; c.gridy = 1; c.gridwidth = 2;
		projectionPanel.add(new JLabel(tr("Bottom left (min) corner:")), c);
		c.gridx = 0; c.gridy = 2; c.gridwidth = 1;
		projectionPanel.add(new JLabel(tr("PDF X and Y")), c);
		c.gridx = 1; c.gridy = 2; c.gridwidth = 1;
		projectionPanel.add(new JLabel(tr("East and North")), c);
		c.gridx = 0; c.gridy = 3; c.gridwidth = 1;
		projectionPanel.add(this.minXField, c);
		c.gridx = 0; c.gridy = 4; c.gridwidth = 1;
		projectionPanel.add(this.minYField, c);
		c.gridx = 1; c.gridy = 3; c.gridwidth = 1;
		projectionPanel.add(this.minEastField, c);
		c.gridx = 1; c.gridy = 4; c.gridwidth = 1;
		projectionPanel.add(this.minNorthField, c);
		c.gridx = 0; c.gridy = 5; c.gridwidth = 1;
		projectionPanel.add(this.getMinButton, c);


		c.gridx = 0; c.gridy = 6; c.gridwidth = 2;
		projectionPanel.add(new JLabel(tr("Top right (max) corner:")), c);
		c.gridx = 0; c.gridy = 7; c.gridwidth = 1;
		projectionPanel.add(new JLabel(tr("PDF X and Y")), c);
		c.gridx = 1; c.gridy = 7; c.gridwidth = 1;
		projectionPanel.add(new JLabel(tr("East and North")), c);
		c.gridx = 0; c.gridy = 8; c.gridwidth = 1;
		projectionPanel.add(this.maxXField, c);
		c.gridx = 0; c.gridy = 9; c.gridwidth = 1;
		projectionPanel.add(this.maxYField, c);
		c.gridx = 1; c.gridy = 8; c.gridwidth = 1;
		projectionPanel.add(this.maxEastField, c);
		c.gridx = 1; c.gridy = 9; c.gridwidth = 1;
		projectionPanel.add(this.maxNorthField, c);
		c.gridx = 0; c.gridy = 10; c.gridwidth = 1;
		projectionPanel.add(this.getMaxButton, c);

		JPanel okCancelPanel = new JPanel(new GridLayout(1,3));
		okCancelPanel.add(this.cancelButton);
		okCancelPanel.add(this.showButton);
		okCancelPanel.add(this.okButton);
		okCancelPanel.add(this.saveButton);

		JPanel panel = new JPanel(new GridBagLayout());
		c.gridx = 0; c.gridy = 0; c.gridwidth = 1;
		panel.add(configPanel, c);
		c.gridx = 0; c.gridy = 1; c.gridwidth = 1;
		panel.add(selectFilePanel, c);
		c.gridx = 0; c.gridy = 2; c.gridwidth = 1;
		panel.add(projectionPanel, c);
		c.gridx = 0; c.gridy = 3; c.gridwidth = 1;
		panel.add(okCancelPanel, c);

		this.setSize(400, 520);
		this.setContentPane(panel);
	}


	private void loadFilePressed() {
		final File newFileName = this.chooseFile();

		if (newFileName == null) {
			return;
		}

		this.removeLayer();

		this.loadFileButton.setEnabled(false);
		this.loadFileButton.setText(tr("Loading..."));

		this.runAsBackgroundTask(
				new Runnable() {
					public void run() {
						data = loadPDF(newFileName);
					}
				},
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						if (data!= null) {
							OsmBuilder.Mode mode = LoadPdfDialog.this.debugModeCheck.isSelected() ? OsmBuilder.Mode.Debug: OsmBuilder.Mode.Draft;
							LoadPdfDialog.this.fileName = newFileName;
							LoadPdfDialog.this.makeLayer(tr("PDF file preview"), new FilePlacement(), mode);
							LoadPdfDialog.this.loadFileButton.setText(tr("Loaded"));
							LoadPdfDialog.this.loadFileButton.setEnabled(true);
							LoadPdfDialog.this.loadPlacement();
							LoadPdfDialog.this.setPlacement();
						}
					}
				});
	}

	
	private boolean preparePlacement()
	{
		boolean ok = this.parsePlacement();
		if (!ok){
			JOptionPane.showMessageDialog(Main.parent, tr("Problems parsing placement."));
			return false;
		}
		
		String transformError = this.placement.prepareTransform();
		if (transformError != null){
			JOptionPane.showMessageDialog(Main.parent, transformError);			
		}		

		this.savePlacement();
		
		return true;
	}

	private void okPressed() {

		if (!preparePlacement()) {
			return;
		}

		//rebuild layer with latest projection
		this.makeLayer(tr("Imported PDF: ") + this.fileName, this.placement, OsmBuilder.Mode.Final);

		this.setVisible(false);
	}

	private void savePressed() {

		if (!preparePlacement()) {
			return;
		}		

		java.io.File file = this.chooseSaveFile();

		if (file == null){
			return;
		}		
		
		//rebuild layer with latest projection
		this.removeLayer();


		this.saveLayer(file);
		this.setVisible(false);
	}


	private void showPressed() {

		boolean ok = this.parsePlacement();
		if (!ok){
			return;
		}

		OsmBuilder builder = new OsmBuilder(this.placement);

		//zoom to new location
		Main.map.mapView.zoomTo(builder.getWorldBounds(this.data));
		Main.map.repaint();
	}

	private void cancelPressed() {
		this.removeLayer();
		this.setVisible(false);
	}


	private void getMinPressed() {
		EastNorth en = this.getSelectedCoor();

		if (en != null) {
			this.minXField.setText(Double.toString(en.east()));
			this.minYField.setText(Double.toString(en.north()));
		}
	}

	private void getMaxPressed() {
		EastNorth en = this.getSelectedCoor();

		if (en != null) {
			this.maxXField.setText(Double.toString(en.east()));
			this.maxYField.setText(Double.toString(en.north()));
		}
	}

	// Implementation methods

	private EastNorth getSelectedCoor() {
		Collection<OsmPrimitive> selected = Main.main.getCurrentDataSet().getSelected();

		if (selected.size() != 1 || !(selected.iterator().next() instanceof Node)){
			JOptionPane.showMessageDialog(Main.parent, tr("Please select exactly one node."));
			return null;
		}

		LatLon ll = ((Node)selected.iterator().next()).getCoor();
		FilePlacement pl = new FilePlacement();
		return pl.reverseTransform(ll);
	}


	private java.io.File chooseFile() {
		//get file name
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.setMultiSelectionEnabled(false);
		fc.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(java.io.File pathname) {
				return pathname.isDirectory() || pathname.getName().endsWith(".pdf");
			}
			@Override
			public String getDescription() {
				return tr("PDF files");
			}
		});
		int result = fc.showOpenDialog(Main.parent);

		if (result != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		else
		{
			return fc.getSelectedFile();
		}
	}

	private java.io.File chooseSaveFile() {
		//get file name
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(true);
		fc.setMultiSelectionEnabled(false);
		fc.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(java.io.File pathname) {
				return pathname.isDirectory() || pathname.getName().endsWith(".osm");
			}
			@Override
			public String getDescription() {
				return tr("OSM files");
			}
		});
		int result = fc.showOpenDialog(Main.parent);

		if (result != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		else
		{
			return fc.getSelectedFile();
		}
	}

	private void runAsBackgroundTask(final Runnable task, final ActionListener after) {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Thread t = new Thread(new Runnable()
		{
			public void run() {
				task.run();

				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						after.actionPerformed(null);
					}
				});
			}
		});
		t.start();
	}

	private PathOptimizer loadPDF(File fileName) {

		PathOptimizer data = new PathOptimizer();

		try {
			PdfBoxParser parser = new PdfBoxParser(data);
			parser.parse(fileName);

		} catch (FileNotFoundException e1) {
			JOptionPane
			.showMessageDialog(
					Main.parent,
					tr("File not found."));
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane
			.showMessageDialog(
					Main.parent,
					tr("Error while parsing: {0}", e.getMessage()));
			return null;
		}

		if (this.colorFilterCheck.isSelected()) {
			try {
				String colString = this.colorFilterColor.getText().replace("#", "");
				Color color = new Color(Integer.parseInt(colString, 16));
				data.filterByColor(color);
			}
			catch (Exception e) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Could not parse color"));
				return null;
			}
		}


		if (this.removeParallelSegmentsCheck.isSelected()) {
			try {
				double tolerance = Double.parseDouble(this.removeParallelSegmentsTolerance.getText());
				data.removeParallelLines(tolerance);
			}
			catch (Exception e) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Max distance is not a number"));
				return null;
			}
		}

		if (this.mergeCloseNodesCheck.isSelected()) {
			try {
				double tolerance = Double.parseDouble(this.mergeCloseNodesTolerance.getText());
				data.mergeNodes(tolerance);
			}
			catch (Exception e) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Tolerance is not a number"));
				return null;
			}
		}

		data.mergeSegments();

		if (this.removeSmallObjectsCheck.isSelected()) {
			try {
				double tolerance = Double.parseDouble(this.removeSmallObjectsSize.getText());
				data.removeSmallObjects(tolerance);
			}
			catch (Exception e) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Tolerance is not a number"));
				return null;
			}
		}


		if (this.removeLargeObjectsCheck.isSelected()) {
			try {
				double tolerance = Double.parseDouble(this.removeLargeObjectsSize.getText());
				data.removeLargeObjects(tolerance);
			}
			catch (Exception e) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Tolerance is not a number"));
				return null;
			}
		}

		data.splitLayersByPathKind();
		data.finish();
		return data;
	}



	private boolean parsePlacement() {
		Object selectedProjection = this.projectionCombo.getSelectedItem();

		if (!(selectedProjection instanceof Projection))
		{
			JOptionPane.showMessageDialog(Main.parent, tr("Please set a projection."));
			return false;
		}

		this.placement.projection = (Projection)this.projectionCombo.getSelectedItem();

		try
		{
			this.placement.setPdfBounds(
					Double.parseDouble(this.minXField.getText()),
					Double.parseDouble(this.minYField.getText()),
					Double.parseDouble(this.maxXField.getText()),
					Double.parseDouble(this.maxYField.getText()));
			this.placement.setEastNorthBounds(
					Double.parseDouble(this.minEastField.getText()),
					Double.parseDouble(this.minNorthField.getText()),
					Double.parseDouble(this.maxEastField.getText()),
					Double.parseDouble(this.maxNorthField.getText()));
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(Main.parent, tr("Could not parse numbers. Please check."));
			return false;
		}

		return true;
	}

	private void setPlacement() {

		this.projectionCombo.setSelectedItem(placement.projection);
		this.minXField.setText(Double.toString(placement.minX));
		this.maxXField.setText(Double.toString(placement.maxX));
		this.minYField.setText(Double.toString(placement.minY));
		this.maxYField.setText(Double.toString(placement.maxY));
		this.minEastField.setText(Double.toString(placement.minEast));
		this.maxEastField.setText(Double.toString(placement.maxEast));
		this.minNorthField.setText(Double.toString(placement.minNorth));
		this.maxNorthField.setText(Double.toString(placement.maxNorth));
	}


	private void loadPlacement() {
		//load saved transformation
		File propertiesFile = new File(fileName.getAbsoluteFile()+ ".placement");
		try {

			if (propertiesFile.exists()){
				Properties p = new Properties();
				p.load(new FileInputStream(propertiesFile));
				this.placement.fromProperties(p);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void savePlacement(){
		//load saved transformation
		File propertiesFile = new File(fileName.getAbsoluteFile()+ ".placement");
		try {
			Properties p = this.placement.toProperties();
			p.store(new FileOutputStream(propertiesFile), "PDF file placement on OSM");
		} catch (Exception e){
			e.printStackTrace();
		}
	}


	private void makeLayer(String name, FilePlacement placement, OsmBuilder.Mode mode) {
		this.removeLayer();

		if (placement == null) {
			return;
		}

		OsmBuilder builder = new OsmBuilder(placement);

		DataSet data = builder.build(this.data.getLayers(), mode);
		this.layer = new OsmDataLayer(data, name, null);

		// Commit
		this.layer.onPostLoadFromFile();
		Main.main.addLayer(this.layer);
		Main.map.mapView.zoomTo(builder.getWorldBounds(this.data));

		this.okButton.setEnabled(true);
		this.showButton.setEnabled(true);
	}

	private void removeLayer() {
		if (this.layer != null) {
			Main.main.removeLayer(this.layer);
			this.layer.data.clear(); //saves memory
			this.layer = null;
		}

		this.okButton.setEnabled(false);
		this.showButton.setEnabled(false);
	}

	private void saveLayer(java.io.File file) {		
		OsmBuilder builder = new OsmBuilder(this.placement);
		DataSet data = builder.build(this.data.getLayers(), OsmBuilder.Mode.Final);
		OsmDataLayer layer = new OsmDataLayer(data, file.getName(), file);

		OsmExporter exporter = new OsmExporter();

		try {
			exporter.exportData(file, layer);
		}
		catch(IOException e){
			//TODO:
		}
	}

}
