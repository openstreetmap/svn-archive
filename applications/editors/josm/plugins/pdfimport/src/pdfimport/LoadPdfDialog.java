// License: GPL. For details, see LICENSE file.
package pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.UploadPolicy;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.io.importexport.OsmExporter;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.progress.ProgressRenderer;
import org.openstreetmap.josm.gui.progress.swing.SwingRenderingProgressMonitor;
import org.openstreetmap.josm.tools.Logging;

import pdfimport.pdfbox.PdfBoxParser;

public class LoadPdfDialog extends JFrame {

	public static class MainButtons {
		public JButton okButton;
		public JButton cancelButton;
		public JButton showButton;
		public JButton saveButton;
		public JPanel panel;

		public MainButtons() {
		}

		void build(LoadPdfDialog loadPdfDialog) {
			/*
			 * build the dialog Window from components
			 */
			okButton = new JButton(tr("Import"));
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					loadPdfDialog.importAction();
				}
			});
			saveButton = new JButton(tr("Save"));
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					loadPdfDialog.saveAction();
				}
			});

			showButton = new JButton(tr("Show target"));
			showButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					loadPdfDialog.showAction();
				}
			});

			cancelButton = new JButton(tr("Cancel"));
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					loadPdfDialog.cancelAction();
				}
			});

			panel = new JPanel(new FlowLayout());
			panel.add(cancelButton);
			panel.add(showButton);
			panel.add(okButton);
			panel.add(saveButton);
			showButton.setVisible(Preferences.isLegacyActions());
			saveButton.setVisible(Preferences.isLegacyActions());
		}
	}


	private static class Config {
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
		private JPanel panel;

		public Config() {
			build();
		}

		public JComponent getComponent() {
			return panel;
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

			splitOnSingleSegmentCheck = new GuiFieldBool(tr("Single segments", Preferences.isLayerSegment()));
			splitOnOrthogonalCheck = new GuiFieldBool(tr("Orthogonal shapes", Preferences.isLayerOrtho()));

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

	static class LoadProgressRenderer implements ProgressRenderer {
		private final JProgressBar pBar;
		private String title = "";

		LoadProgressRenderer(JProgressBar pb) {
			this.pBar = pb;
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

	private File pdfFile;
	private final FilePlacement18 placement = new FilePlacement18();

	private PathOptimizer pdfData;
	private OsmDataLayer dataLayer;

	private final JButton loadFileButton = new JButton(tr("Load preview ..."));

	private final JProgressBar loadProgress = new JProgressBar();
;
	private OsmDataLayer newLayer;

	private LoadProgressRenderer progressRenderer;

	public LoadPdfDialog() {
		buildGUI();
		removeLayer();
		if (Preferences.getGuiMode() == Preferences.GuiMode.Simple) {
			loadFileButton.setVisible(false);
			configPanel.panel.setVisible(false);
			actionPanel.saveButton.setVisible(false);
			actionPanel.showButton.setVisible(false);
			setSize(new Dimension(380, 350));
			if (!loadAction()) {
				cancelAction();
				return;
			}
		} else {
			setSize(new Dimension(450, 600));
		}
		setAlwaysOnTop(true);
		setVisible(true);
	}

	Component placementPanel = placement.getGui();
	MainButtons actionPanel = new MainButtons();
	Config configPanel = new Config();

	private void buildGUI() {
		/*
		 * build the GUI from Components
		 */
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new java.awt.Insets(0, 0, 0, 0);

		actionPanel.build(this);

		loadFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadAction();
			}
		});

		progressRenderer = new LoadProgressRenderer(loadProgress);

		JPanel panel = new JPanel(new GridBagLayout());

		panel.add(configPanel.getComponent(), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(loadFileButton, c);
		c.fill = GridBagConstraints.BOTH;
		panel.add(placementPanel, c);
		panel.add(actionPanel.panel, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(this.loadProgress, c);

		setContentPane(panel);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancelAction();
			}
		});
		placement.setDependsOnValid(actionPanel.okButton);

		/*
		 * TODO: Make okButton to default Button of Dialog, make cancelButton to react on ESC-Key
		 */
//		SwingUtilities.getRootPane(panel).setDefaultButton(actionPanel.okButton);
	}

	 private boolean loadAction() {
		 /*
		  * perform load PDF file to preview
		  * TODO: load preview to previous placement, involves reverse transform
		  */
		final File newFileName = this.chooseFile();

		if (newFileName == null) {
			return false;
		}
		Logging.debug("PdfImport: Load Preview");
		this.removeLayer();

		this.loadFileButton.setEnabled(false);

		this.runAsBackgroundTask(new Runnable() {
			@Override
			public void run() {
				// async part
				LoadPdfDialog.this.loadProgress.setVisible(true);
				SwingRenderingProgressMonitor monitor = new SwingRenderingProgressMonitor(progressRenderer);
				monitor.beginTask("Loading file", 1000);
				pdfData = loadPDF(newFileName, monitor.createSubTaskMonitor(500, false));
				OsmBuilder.Mode mode = LoadPdfDialog.this.configPanel.debugModeCheck.getValue()
						? OsmBuilder.Mode.Debug
						: OsmBuilder.Mode.Draft;

				if (pdfData != null) {
					LoadPdfDialog.this.newLayer = LoadPdfDialog.this.makeLayer(
							tr("PDF preview: ") + newFileName.getName(), new FilePlacement(), mode,
							monitor.createSubTaskMonitor(500, false));
				}

				monitor.finishTask();
				progressRenderer.finish();
			}
		}, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				// sync part
				LoadPdfDialog.this.pdfFile = newFileName;
				if (pdfData != null) {
					LoadPdfDialog.this.placeLayer(newLayer, new FilePlacement());
					try {
						LoadPdfDialog.this.placement.load(newFileName);
					} catch (IOException e) {
						// Dont care
					} finally {
						LoadPdfDialog.this.placement.verify();
					}
					LoadPdfDialog.this.newLayer = null;
					LoadPdfDialog.this.loadFileButton.setEnabled(true);
					LoadPdfDialog.this.placementPanel.setEnabled(true);
					LoadPdfDialog.this.actionPanel.panel.setEnabled(true);
					LoadPdfDialog.this.actionPanel.showButton.setEnabled(true);
					LoadPdfDialog.this.actionPanel.saveButton.setEnabled(true);
					LoadPdfDialog.this.actionPanel.okButton.setEnabled(true);
					LoadPdfDialog.this.loadProgress.setVisible(false);
				}
			}
		});
		return true;
	}

	private void importAction() {

		if (!placement.isValid()) return;
		try {
			placement.save(pdfFile);
		} catch (IOException e) {
			e.toString();
		}
		removeLayer();

		runAsBackgroundTask(new Runnable() {
			@Override
			public void run() {
				// async part
				LoadPdfDialog.this.loadProgress.setVisible(true);
				SwingRenderingProgressMonitor monitor = new SwingRenderingProgressMonitor(progressRenderer);
				LoadPdfDialog.this.newLayer = LoadPdfDialog.this.makeLayer(tr("PDF: ") + pdfFile.getName(), placement,
						OsmBuilder.Mode.Final, monitor);
				progressRenderer.finish();
			}
		}, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// sync part
				// rebuild layer with latest projection
				LoadPdfDialog.this.placeLayer(newLayer, placement);
				LoadPdfDialog.this.setVisible(false);
			}
		});
	}

	private void saveAction() {
		/*
		 * perform save preview layer to file
		 * TODO: is this action valueable? Can be easily performed from main menu
		 */

		if (!placement.isValid()) return;

		final java.io.File file = this.chooseSaveFile();

		if (file == null) {
			return;
		}

		this.removeLayer();

		this.runAsBackgroundTask(new Runnable() {
			@Override
			public void run() {
				// async part
				SwingRenderingProgressMonitor monitor = new SwingRenderingProgressMonitor(progressRenderer);
				LoadPdfDialog.this.saveLayer(file, placement, monitor);
				progressRenderer.finish();
			}
		}, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// sync part
				LoadPdfDialog.this.setVisible(false);
			}
		});
	}

	private void showAction() {
		/*
		 * perform show action
		 * TODO: is this action valuable? User can do it easy from OSM Main Menu
		 */
		if (!placement.isValid()) return;

		// zoom to new location
		MainApplication.getMap().mapView.zoomTo(placement.getWorldBounds(pdfData));
		MainApplication.getMap().repaint();
	}

	private void cancelAction() {
		/*
		 * perform cancel action
		 */
		removeLayer();
		setVisible(false);
	}

	// Implementation methods

	private static JFileChooser loadChooser = null;

	private java.io.File chooseFile() {
		// get PDF file to load
		if (loadChooser == null) {
			loadChooser = new JFileChooser(Preferences.getLoadDir());
			loadChooser.setAcceptAllFileFilterUsed(false);
			loadChooser.setMultiSelectionEnabled(false);
			loadChooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(java.io.File pathname) {
					return pathname.isDirectory() || pathname.getName().endsWith(".pdf");
				}

				@Override
				public String getDescription() {
					return tr("PDF files");
				}
			});
		} else {
			loadChooser.rescanCurrentDirectory();
		}
		int result = loadChooser.showDialog(this, tr("Import PDF"));
		if (result != JFileChooser.APPROVE_OPTION) {
			return null;
		} else {
			Preferences.setLoadDir(loadChooser.getSelectedFile().getParentFile().getAbsolutePath());
			return loadChooser.getSelectedFile();
		}
	}

	private java.io.File chooseSaveFile() {
		// get file name
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(true);
		fc.setMultiSelectionEnabled(false);
		fc.setFileFilter(new FileFilter() {
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
		} else {
			return fc.getSelectedFile();
		}
	}

	private void runAsBackgroundTask(final Runnable task, final ActionListener after) {
		/*
		 * run @task in background (asychronosly , run @after when @task has finished
		 */
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				task.run();

				SwingUtilities.invokeLater(new Runnable() {
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
		/*
		 * postprocess load PDF-file according to options
		 */

		monitor.beginTask("", 100);
		monitor.setTicks(0);
		monitor.setCustomText(tr("Preparing"));

		double nodesTolerance = 0.0;
		Color color = null;
		int maxPaths = Integer.MAX_VALUE;

		if (configPanel.mergeCloseNodesCheck.getValue() && configPanel.mergeCloseNodesTolerance.isDataValid()) {
				nodesTolerance = configPanel.mergeCloseNodesTolerance.getValue();
		}

		if (configPanel.colorFilterCheck.getValue()) {
			try {
				String colString = this.configPanel.colorFilterColor.getText().replace("#", "");
				color = new Color(Integer.parseInt(colString, 16));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(Main.parent, tr("Could not parse color"));
				return null;
			}
		}

		if (configPanel.limitPathCountCheck.getValue() && configPanel.limitPathCount.isDataValid()) {
				maxPaths = configPanel.limitPathCount.getValue();
		}

		monitor.setTicks(10);
		monitor.setCustomText(tr("Parsing file"));

		PathOptimizer data = new PathOptimizer(nodesTolerance, color, configPanel.splitOnColorChangeCheck.getValue());

		try {
			PdfBoxParser parser = new PdfBoxParser(data);
			parser.parse(fileName, maxPaths, monitor.createSubTaskMonitor(80, false));

		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(Main.parent, tr("File not found."));
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Main.parent, tr("Error while parsing: {0}", e.getMessage()));
			return null;
		}

		monitor.setTicks(80);

		if (configPanel.removeParallelSegmentsCheck.getValue() && configPanel.removeParallelSegmentsTolerance.isDataValid()) {
				double tolerance = configPanel.removeParallelSegmentsTolerance.getValue();
				monitor.setCustomText(tr("Removing parallel segments"));
		}

		if (nodesTolerance > 0.0) {
			monitor.setTicks(83);
			monitor.setCustomText(tr("Joining nodes"));
			data.mergeNodes();
		}

		monitor.setTicks(85);
		monitor.setCustomText(tr("Joining adjacent segments"));
		data.mergeSegments();

		if (configPanel.removeSmallObjectsCheck.getValue() && configPanel.removeSmallObjectsSize.isDataValid()) {
				double tolerance = configPanel.removeSmallObjectsSize.getValue();
				monitor.setTicks(90);
				monitor.setCustomText(tr("Removing small objects"));

				data.removeSmallObjects(tolerance);

		}

		if (configPanel.removeLargeObjectsCheck.getValue() && configPanel.removeLargeObjectsSize.isDataValid()) {
				double tolerance = configPanel.removeLargeObjectsSize.getValue();
				monitor.setTicks(90);
				monitor.setCustomText(tr("Removing large objects"));
				data.removeLargeObjects(tolerance);
		}

		monitor.setTicks(95);
		monitor.setCustomText(tr("Finalizing layers"));
		data.splitLayersByPathKind(configPanel.splitOnShapeClosedCheck.getValue(),
				configPanel.splitOnSingleSegmentCheck.getValue(),
				configPanel.splitOnOrthogonalCheck.getValue());
		data.finish();

		monitor.finishTask();
		return data;
	}

	private OsmDataLayer makeLayer(String name, FilePlacement placement, OsmBuilder.Mode mode,
			ProgressMonitor monitor) {
		/*
		 * create a layer from data
		 */
		monitor.beginTask(tr("Building JOSM layer"), 100);
		OsmBuilder builder = new OsmBuilder(placement);
		DataSet data = builder.build(pdfData.getLayers(), mode, monitor.createSubTaskMonitor(50, false));
		data.setUploadPolicy(UploadPolicy.BLOCKED);
		monitor.setTicks(50);
		monitor.setCustomText(tr("Postprocessing layer"));
		OsmDataLayer result = new OsmDataLayer(data, name, null);
		result.setUploadDiscouraged(true);
		result.setBackgroundLayer(true);
		result.onPostLoadFromFile();

		monitor.finishTask();
		return result;
	}

	private void placeLayer(OsmDataLayer _layer, FilePlacement placement) {
		/*
		 *
		 */
		removeLayer();
		dataLayer = _layer;
		MainApplication.getLayerManager().addLayer(dataLayer);
		MainApplication.getMap().mapView.zoomTo(placement.getWorldBounds(pdfData));
	}

	private void removeLayer() {
		/*
		 * remove preview layer
		 */
		if (dataLayer != null) {
			MainApplication.getLayerManager().removeLayer(dataLayer);
			dataLayer.data.clear(); // saves memory
			dataLayer = null;
		}
		// No layer ==> no actions
		actionPanel.showButton.setEnabled(false);
		actionPanel.saveButton.setEnabled(false);
		actionPanel.okButton.setEnabled(false);
		placementPanel.setEnabled(false);

	}

	private void saveLayer(java.io.File file, FilePlacement placement, ProgressMonitor monitor) {
		/*
		 * save layer to file
		 * TODO: is this methode valuable? Functionality can easily performed from Main-Menu
		 */
		monitor.beginTask(tr("Saving to file."), 1000);

		OsmBuilder builder = new OsmBuilder(placement);
		DataSet data = builder.build(this.pdfData.getLayers(), OsmBuilder.Mode.Final,
				monitor.createSubTaskMonitor(500, false));
		OsmDataLayer layer = new OsmDataLayer(data, file.getName(), file);

		monitor.setCustomText(tr(" Writing to file"));
		monitor.setTicks(500);

		OsmExporter exporter = new OsmExporter();

		try {
			exporter.exportData(file, layer);
		} catch (IOException e) {
			Logging.error(e);
		}

		monitor.finishTask();
	}

}
