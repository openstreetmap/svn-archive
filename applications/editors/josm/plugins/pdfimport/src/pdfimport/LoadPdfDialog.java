package pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.SideButton;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionChoice;
import org.openstreetmap.josm.gui.preferences.projection.ProjectionPreference;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressRenderer;
import org.openstreetmap.josm.gui.progress.SwingRenderingProgressMonitor;
import org.openstreetmap.josm.io.OsmExporter;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.WindowGeometry;

import pdfimport.pdfbox.PdfBoxParser;

public class LoadPdfDialog extends JFrame{

	class LoadProgressRenderer implements ProgressRenderer{
		private final JProgressBar pBar;
		private String title = "";

		public LoadProgressRenderer(JProgressBar pb)
		{
			this.pBar =pb;
			this.pBar.setMinimum(0);
			this.pBar.setValue(0);
			this.pBar.setMaximum(1);
			this.pBar.setString("");
			this.pBar.setStringPainted(true);

		}

		@Override
		public void setCustomText(String message) {
			this.pBar.setString(this.title + message);
		}

		@Override
		public void setIndeterminate(boolean indeterminate) {
			this.pBar.setIndeterminate(indeterminate);
		}

		@Override
		public void setMaximum(int maximum) {
			this.pBar.setMaximum(maximum);
		}

		@Override
		public void setTaskTitle(String taskTitle) {
			this.title = taskTitle;
			this.pBar.setString(this.title);
		}

		@Override
		public void setValue(int value) {
			this.pBar.setValue(value);
		}

		public void finish() {
			this.pBar.setString(tr("Finished"));
			this.pBar.setValue(this.pBar.getMaximum());
		}

	}

	private File fileName;
	private PathOptimizer data;
	private OsmDataLayer layer;

	/**
	 * Combobox with all projections available
	 */
	private JComboBox projectionCombo;
	private JButton projectionPreferencesButton;
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
	private JProgressBar loadProgress;
	protected OsmDataLayer newLayer;

	private LoadProgressRenderer progressRenderer;
	private JCheckBox limitPathCountCheck;
	private JTextField limitPathCount;
	private JCheckBox splitOnColorChangeCheck;
	private JCheckBox splitOnShapeClosedCheck;
	private JCheckBox splitOnSingleSegmentCheck;
	private JCheckBox splitOnOrthogonalCheck;


	public LoadPdfDialog() {
		this.buildGUI();
		FilePlacement pl = new FilePlacement();
		this.setPlacement(pl);
		this.addListeners();
		this.removeLayer();
	}

	private void addListeners() {

		this.projectionCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateProjectionPrefButton();
			}

		});
		this.projectionPreferencesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showProjectionPreferences();
			}
		});

		this.loadFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadFilePressed();
			}
		});

		this.okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed();
			}
		});

		this.saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				savePressed();
			}
		});

		this.showButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showPressed();
			}
		});

		this.cancelButton.addActionListener(new ActionListener() {
			@Override
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
			@Override
			public void actionPerformed(ActionEvent e) {
				getMinPressed();
			}
		});

		this.getMaxButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getMaxPressed();
			}
		});

	}

	private void buildGUI() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;c.gridwidth = 1;c.weightx =1; c.weighty = 1; c.fill = GridBagConstraints.BOTH;

		this.projectionCombo = new JComboBox();
		for (ProjectionChoice p: ProjectionPreference.getProjectionChoices()) {
			this.projectionCombo.addItem(p);
		}

		this.projectionPreferencesButton = new JButton(tr("Prefs"));
		updateProjectionPrefButton();

		this.loadFileButton = new JButton(tr("Load file..."));
		this.okButton = new JButton(tr("Place"));
		this.saveButton = new JButton(tr("Save"));
		this.showButton = new JButton(tr("Show target"));
		this.cancelButton = new JButton(tr("Discard"));
		this.loadProgress = new JProgressBar();
		this.progressRenderer = new LoadProgressRenderer(this.loadProgress);

		this.minXField = new JTextField("");
		this.minYField = new JTextField("");
		this.minEastField = new JTextField("");
		this.minNorthField = new JTextField("");
		this.getMinButton = new JButton(tr("Take X and Y from selected node"));

		this.maxXField = new JTextField("");
		this.maxYField = new JTextField("");
		this.maxEastField = new JTextField("");
		this.maxNorthField = new JTextField("");
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

		this.limitPathCountCheck = new JCheckBox(tr("Take only first X paths"));
		this.limitPathCount = new JTextField("10000");

		this.splitOnColorChangeCheck = new JCheckBox(tr("Color/width change"));
		this.splitOnShapeClosedCheck = new JCheckBox(tr("Shape closed"));
		this.splitOnSingleSegmentCheck = new JCheckBox(tr("Single segments"));
		this.splitOnOrthogonalCheck = new JCheckBox(tr("Orthogonal shapes"));

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


		c.gridx = 0; c.gridy = 4; c.gridwidth = 2;
		configPanel.add(this.limitPathCountCheck, c);
		c.gridx = 2; c.gridy = 4; c.gridwidth = 1;
		configPanel.add(this.limitPathCount, c);

		c.gridx = 0; c.gridy = 5; c.gridwidth = 1;
		configPanel.add(this.colorFilterCheck, c);
		c.gridx = 2; c.gridy = 5; c.gridwidth = 1;
		configPanel.add(this.colorFilterColor, c);

		c.gridx = 0; c.gridy = 6; c.gridwidth = 2;
		configPanel.add(this.debugModeCheck, c);


		c.gridx = 0; c.gridy = 7; c.gridwidth = 1;
		configPanel.add(new JLabel(tr("Introduce separate layers for:")), c);
		c.gridx = 1; c.gridy = 7; c.gridwidth = 1;
		configPanel.add(this.splitOnShapeClosedCheck, c);
		c.gridx = 2; c.gridy = 7; c.gridwidth = 1;
		configPanel.add(this.splitOnSingleSegmentCheck, c);
		c.gridx = 1; c.gridy = 8; c.gridwidth = 1;
		configPanel.add(this.splitOnColorChangeCheck, c);
		c.gridx = 2; c.gridy = 8; c.gridwidth = 1;
		configPanel.add(this.splitOnOrthogonalCheck, c);


		JPanel projectionPanel = new JPanel(new GridBagLayout());
		projectionPanel.setBorder(BorderFactory.createTitledBorder(tr("Bind to coordinates")));

		JPanel projectionSubPanel = new JPanel();
		projectionSubPanel.setLayout(new BoxLayout(projectionSubPanel, BoxLayout.X_AXIS));

		projectionSubPanel.add(new JLabel(tr("Projection:")));
		projectionSubPanel.add(this.projectionCombo);
		projectionSubPanel.add(this.projectionPreferencesButton);
		c.gridx = 0; c.gridy = 0; c.gridwidth = 3;
		projectionPanel.add(projectionSubPanel, c);

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
		panel.add(loadFileButton, c);
		c.gridx = 0; c.gridy = 2; c.gridwidth = 1;
		panel.add(projectionPanel, c);
		c.gridx = 0; c.gridy = 3; c.gridwidth = 1;
		panel.add(okCancelPanel, c);
		c.gridx = 0; c.gridy = 4; c.gridwidth = 1;
		panel.add(this.loadProgress, c);


		this.setSize(450, 550);
		this.setContentPane(panel);
	}

	private class ProjectionSubPrefsDialog extends JDialog {
		private final ProjectionChoice projPref;
		private OKAction actOK;
		private CancelAction actCancel;
		private JPanel projPrefPanel;

		public ProjectionSubPrefsDialog(Component parent, ProjectionChoice pr) {
			super(JOptionPane.getFrameForComponent(parent), ModalityType.DOCUMENT_MODAL);

			projPref = pr;

			setTitle(tr("Projection Preferences"));
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);

			build();
		}

		protected void makeButtonRespondToEnter(SideButton btn) {
			btn.setFocusable(true);
			btn.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "enter");
			btn.getActionMap().put("enter", btn.getAction());
		}

		protected JPanel buildInputForm() {
			return projPref.getPreferencePanel(null);
		}

		protected JPanel buildButtonRow() {
			JPanel pnl = new JPanel(new FlowLayout());

			actOK = new OKAction();
			actCancel = new CancelAction();

			SideButton btn;
			pnl.add(btn = new SideButton(actOK));
			makeButtonRespondToEnter(btn);
			pnl.add(btn = new SideButton(actCancel));
			makeButtonRespondToEnter(btn);
			return pnl;
		}

		protected void build() {
			projPrefPanel = buildInputForm();
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(projPrefPanel, BorderLayout.CENTER);
			getContentPane().add(buildButtonRow(), BorderLayout.SOUTH);
			pack();

			// make dialog respond to ESCAPE
			getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
			getRootPane().getActionMap().put("escape", actCancel);
		}

		class OKAction extends AbstractAction {
			public OKAction() {
			putValue(NAME, tr("OK"));
			putValue(SHORT_DESCRIPTION, tr("Close the dialog and apply projection preferences"));
			putValue(SMALL_ICON, ImageProvider.get("ok"));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
			projPref.setPreferences(projPref.getPreferences(projPrefPanel));
			setVisible(false);
			}
		}

		class CancelAction extends AbstractAction {
			public CancelAction() {
			putValue(NAME, tr("Cancel"));
			putValue(SHORT_DESCRIPTION, tr("Close the dialog, discard projection preference changes"));
			putValue(SMALL_ICON, ImageProvider.get("cancel"));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
			setVisible(false);
			}
		}

		@Override
		public void setVisible(boolean visible) {
			if (visible) {
			new WindowGeometry(
				getClass().getName() + ".geometry",
				WindowGeometry.centerOnScreen(new Dimension(400, 300))).applySafe(this);
			} else {
			new WindowGeometry(this).remember(getClass().getName() + ".geometry");
			}
			super.setVisible(visible);
		}
	}

	private void updateProjectionPrefButton() {
//		ProjectionChoice proj = (ProjectionChoice) projectionCombo.getSelectedItem();

		//TODO
		// Enable/disable pref button
//		if(!(proj instanceof ProjectionSubPrefs)) {
//			projectionPreferencesButton.setEnabled(false);
//		} else {
			projectionPreferencesButton.setEnabled(true);
//		}
	}

	private void showProjectionPreferences() {
		ProjectionChoice proj = (ProjectionChoice) projectionCombo.getSelectedItem();

		ProjectionSubPrefsDialog dlg = new ProjectionSubPrefsDialog(this, proj);
		dlg.setVisible(true);

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
					@Override
					public void run() {
						//async part
						SwingRenderingProgressMonitor monitor = new SwingRenderingProgressMonitor(progressRenderer);
						monitor.beginTask("Loading file", 1000);
						data = loadPDF(newFileName, monitor.createSubTaskMonitor(500, false));
						OsmBuilder.Mode mode = LoadPdfDialog.this.debugModeCheck.isSelected() ? OsmBuilder.Mode.Debug: OsmBuilder.Mode.Draft;

						if (data!= null) {
							LoadPdfDialog.this.newLayer = LoadPdfDialog.this.makeLayer(tr("PDF file preview"), new FilePlacement(), mode, monitor.createSubTaskMonitor(500, false));
						}

						monitor.finishTask();
						progressRenderer.finish();
					}
				},
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						//sync part
						if (data!= null) {
							LoadPdfDialog.this.placeLayer(newLayer, new FilePlacement());
							fileName = newFileName;
							newLayer = null;
							LoadPdfDialog.this.loadFileButton.setText(tr("Loaded"));
							LoadPdfDialog.this.loadFileButton.setEnabled(true);
							FilePlacement placement =  LoadPdfDialog.this.loadPlacement();
							LoadPdfDialog.this.setPlacement(placement);
						}
					}
				});
	}


	private FilePlacement preparePlacement()
	{
		FilePlacement placement = this.parsePlacement();
		if (placement == null){
			return null;
		}

		String transformError = placement.prepareTransform();
		if (transformError != null){
			JOptionPane.showMessageDialog(Main.parent, transformError);
			return null;
		}

		this.savePlacement(placement);

		return placement;
	}

	private void okPressed() {

		final FilePlacement placement = preparePlacement();
		if (placement == null) {
			return;
		}

		this.removeLayer();

		this.runAsBackgroundTask(
				new Runnable() {
					@Override
					public void run() {
						//async part
						SwingRenderingProgressMonitor monitor = new SwingRenderingProgressMonitor(progressRenderer);
						LoadPdfDialog.this.newLayer = LoadPdfDialog.this.makeLayer(tr("Imported PDF: ") + fileName, placement, OsmBuilder.Mode.Final, monitor);
						progressRenderer.finish();
					}
				},
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						//sync part
						//rebuild layer with latest projection
						LoadPdfDialog.this.placeLayer(newLayer, placement);
						LoadPdfDialog.this.setVisible(false);
					}
				});
	}

	private void savePressed() {

		final FilePlacement placement = preparePlacement();
		if (placement == null) {
			return;
		}

		final java.io.File file = this.chooseSaveFile();

		if (file == null){
			return;
		}

		this.removeLayer();

		this.runAsBackgroundTask(
				new Runnable() {
					@Override
					public void run() {
						//async part
						SwingRenderingProgressMonitor monitor = new SwingRenderingProgressMonitor(progressRenderer);
						LoadPdfDialog.this.saveLayer(file, placement, monitor);
						progressRenderer.finish();
					}
				},
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						//sync part
						LoadPdfDialog.this.setVisible(false);
					}
				});
	}


	private void showPressed() {

		FilePlacement placement = preparePlacement();
		if (placement == null) {
			return;
		}

		//zoom to new location
		Main.map.mapView.zoomTo(placement.getWorldBounds(this.data));
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
		fc.setSelectedFile(this.fileName);
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
			@Override
			public void run() {
				task.run();

				SwingUtilities.invokeLater(new Runnable(){
					@Override
					public void run() {
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						after.actionPerformed(null);
					}
				});
			}
		});
		t.start();
	}

	private PathOptimizer loadPDF(File fileName, ProgressMonitor monitor) {

		monitor.beginTask("", 100);
		monitor.setTicks(0);
		monitor.setCustomText(tr("Preparing"));

		double nodesTolerance = 0.0;
		Color color = null;
		int maxPaths = Integer.MAX_VALUE;

		if (this.mergeCloseNodesCheck.isSelected()) {
			try {
				nodesTolerance = Double.parseDouble(this.mergeCloseNodesTolerance.getText());
			}
			catch (Exception e) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Tolerance is not a number"));
				return null;
			}
		}

		if (this.colorFilterCheck.isSelected()) {
			try {
				String colString = this.colorFilterColor.getText().replace("#", "");
				color = new Color(Integer.parseInt(colString, 16));
			}
			catch (Exception e) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Could not parse color"));
				return null;
			}
		}

		if (this.limitPathCountCheck.isSelected()) {
			try {
				maxPaths = Integer.parseInt(this.limitPathCount.getText());
			}
			catch (Exception e) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Could not parse max path count"));
				return null;
			}
		}


		monitor.setTicks(10);
		monitor.setCustomText(tr("Parsing file"));

		PathOptimizer data = new PathOptimizer(nodesTolerance, color, this.splitOnColorChangeCheck.isSelected());

		try {
			PdfBoxParser parser = new PdfBoxParser(data);
			parser.parse(fileName, maxPaths, monitor.createSubTaskMonitor(80, false));

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

		monitor.setTicks(80);

		if (this.removeParallelSegmentsCheck.isSelected()) {
			try {
				double tolerance = Double.parseDouble(this.removeParallelSegmentsTolerance.getText());
				monitor.setCustomText(tr("Removing parallel segments"));
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

		if (nodesTolerance > 0.0) {
			monitor.setTicks(83);
			monitor.setCustomText(tr("Joining nodes"));
			data.mergeNodes();
		}

		monitor.setTicks(85);
		monitor.setCustomText(tr("Joining adjacent segments"));
		data.mergeSegments();

		if (this.removeSmallObjectsCheck.isSelected()) {
			try {
				double tolerance = Double.parseDouble(this.removeSmallObjectsSize.getText());
				monitor.setTicks(90);
				monitor.setCustomText(tr("Removing small objects"));

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
				monitor.setTicks(90);
				monitor.setCustomText(tr("Removing large objects"));
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

		monitor.setTicks(95);
		monitor.setCustomText(tr("Finalizing layers"));
		data.splitLayersByPathKind(this.splitOnShapeClosedCheck.isSelected(), this.splitOnSingleSegmentCheck.isSelected(), this.splitOnOrthogonalCheck.isSelected());
		data.finish();

		monitor.finishTask();
		return data;
	}



	private FilePlacement parsePlacement() {
		ProjectionChoice selectedProjection = (ProjectionChoice) this.projectionCombo.getSelectedItem();

		if (selectedProjection == null)
		{
			JOptionPane.showMessageDialog(Main.parent, tr("Please set a projection."));
			return null;
		}

		FilePlacement placement = new FilePlacement();

		placement.projection = selectedProjection.getProjection();

		try
		{
			placement.setPdfBounds(
					Double.parseDouble(this.minXField.getText()),
					Double.parseDouble(this.minYField.getText()),
					Double.parseDouble(this.maxXField.getText()),
					Double.parseDouble(this.maxYField.getText()));
			placement.setEastNorthBounds(
					Double.parseDouble(this.minEastField.getText()),
					Double.parseDouble(this.minNorthField.getText()),
					Double.parseDouble(this.maxEastField.getText()),
					Double.parseDouble(this.maxNorthField.getText()));
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(Main.parent, tr("Could not parse numbers. Please check."));
			return null;
		}

		return placement;
	}

	private void setPlacement(FilePlacement placement) {

		if (placement == null) {
			//use default values.
			placement = new FilePlacement();
		}

		if (placement.projection != null) {
			String projectionCode = placement.projection.toCode();
			BIG_LOOP:
			for (ProjectionChoice projectionChoice: ProjectionPreference.getProjectionChoices()) {
				for (String code: projectionChoice.allCodes()) {
					if (code.equals(projectionCode)) {
						projectionChoice.getPreferencesFromCode(projectionCode);
						this.projectionCombo.setSelectedItem(projectionChoice);
						break BIG_LOOP;
					}
				}
			}
		}

		this.minXField.setText(Double.toString(placement.minX));
		this.maxXField.setText(Double.toString(placement.maxX));
		this.minYField.setText(Double.toString(placement.minY));
		this.maxYField.setText(Double.toString(placement.maxY));
		this.minEastField.setText(Double.toString(placement.minEast));
		this.maxEastField.setText(Double.toString(placement.maxEast));
		this.minNorthField.setText(Double.toString(placement.minNorth));
		this.maxNorthField.setText(Double.toString(placement.maxNorth));
	}


	private FilePlacement loadPlacement() {
		FilePlacement pl = null;
		//load saved transformation
		File propertiesFile = new File(fileName.getAbsoluteFile()+ ".placement");
		try {

			if (propertiesFile.exists()){
				pl = new FilePlacement();
				Properties p = new Properties();
				p.load(new FileInputStream(propertiesFile));
				pl.fromProperties(p);
			}
		}catch (Exception e){
			pl = null;
			e.printStackTrace();
		}

		return pl;
	}

	private void savePlacement(FilePlacement pl){
		//load saved transformation
		File propertiesFile = new File(fileName.getAbsoluteFile()+ ".placement");
		try {
			Properties p = pl.toProperties();
			p.store(new FileOutputStream(propertiesFile), "PDF file placement on OSM");
		} catch (Exception e){
			e.printStackTrace();
		}
	}


	private OsmDataLayer makeLayer(String name, FilePlacement placement, OsmBuilder.Mode mode, ProgressMonitor monitor) {
		monitor.beginTask(tr("Building JOSM layer"), 100);
		OsmBuilder builder = new OsmBuilder(placement);
		DataSet data = builder.build(this.data.getLayers(), mode, monitor.createSubTaskMonitor(50, false));
		monitor.setTicks(50);
		monitor.setCustomText(tr("Postprocessing layer"));
		OsmDataLayer result = new OsmDataLayer(data, name, null);
		result.onPostLoadFromFile();

		monitor.finishTask();
		return result;
	}

	private void placeLayer(OsmDataLayer _layer, FilePlacement placement) {
		this.removeLayer();
		this.layer = _layer;
		Main.main.addLayer(this.layer);
		Main.map.mapView.zoomTo(placement.getWorldBounds(this.data));
	}

	private void removeLayer() {
		if (this.layer != null) {
			Main.main.removeLayer(this.layer);
			this.layer.data.clear(); //saves memory
			this.layer = null;
		}
	}

	private void saveLayer(java.io.File file, FilePlacement placement, ProgressMonitor monitor) {
		monitor.beginTask(tr("Saving to file."), 1000);

		OsmBuilder builder = new OsmBuilder(placement);
		DataSet data = builder.build(this.data.getLayers(), OsmBuilder.Mode.Final, monitor.createSubTaskMonitor(500, false));
		OsmDataLayer layer = new OsmDataLayer(data, file.getName(), file);

		monitor.setCustomText(tr(" Writing to file"));
		monitor.setTicks(500);

		OsmExporter exporter = new OsmExporter();

		try {
			exporter.exportData(file, layer);
		}
		catch(IOException e){
			//TODO:
		}

		monitor.finishTask();
	}

}
