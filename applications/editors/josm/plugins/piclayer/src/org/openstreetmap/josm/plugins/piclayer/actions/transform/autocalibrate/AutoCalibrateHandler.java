package org.openstreetmap.josm.plugins.piclayer.actions.transform.autocalibrate;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.openstreetmap.josm.actions.OpenFileAction;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.coor.conversion.CoordinateFormatManager;
import org.openstreetmap.josm.data.coor.conversion.ICoordinateFormat;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapViewState.MapViewPoint;
import org.openstreetmap.josm.gui.help.HelpBrowser;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.affine.MovePointAction;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.autocalibrate.helper.ObservableArrayList;
import org.openstreetmap.josm.plugins.piclayer.gui.autocalibrate.CalibrationErrorView;
import org.openstreetmap.josm.plugins.piclayer.gui.autocalibrate.CalibrationWindow;
import org.openstreetmap.josm.plugins.piclayer.gui.autocalibrate.ReferenceOptionView;
import org.openstreetmap.josm.plugins.piclayer.gui.autocalibrate.ResultCheckView;
import org.openstreetmap.josm.plugins.piclayer.gui.autocalibrate.SelectLayerView;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;
import org.openstreetmap.josm.tools.Logging;

/**
 * Class handling connection between {@link AutoCalibratePictureAction} and GUIs.
 * Info at https://wiki.openstreetmap.org/wiki/User:Rebsc
 * @author rebsc
 *
 */
public class AutoCalibrateHandler {

	private PicLayerAbstract currentPicLayer;
	private CalibrationWindow mainWindow;
	private CalibrationErrorView errorView;
	private ReferenceOptionView refOptionView;
	private File referenceFile;
	private Layer referenceLayer;
	private AutoCalibrator calibrator;
	private ObservableArrayList<Point2D> originPointList;		// points set on picture to calibrate, scaled in LatLon
	private ObservableArrayList<Point2D> referencePointList;	// points of reference data, scaled in LatLon
	private double distance1To2;	// in meter
	private double distance2To3;	// in meter



	public AutoCalibrateHandler(){
		this.originPointList = new ObservableArrayList<>(3);
		this.referencePointList = new ObservableArrayList<>(3);
		this.distance1To2 = 0.0;
		this.distance2To3 = 0.0;
		this.referenceFile = null;
		this.referenceLayer = null;
		this.currentPicLayer = null;
		this.mainWindow = new CalibrationWindow();
		addListenerToMainView();
		this.calibrator = new AutoCalibrator();
	}

	/**
	 * Method to call calibrating method for given image.
	 */
	private void callCalibrator(){
		if(currentPicLayer != null && originPointList.size() > 0 && referencePointList.size() > 0
				&& distance1To2 != 0.0 && distance2To3 != 0.0) {
					calibrator.setCurrentLayer(currentPicLayer);
					calibrator.setStartPositions(this.originPointList);
					calibrator.setEndPositions(this.referencePointList);
					calibrator.setDistance1To2(distance1To2);
					calibrator.setDistance2To3(distance2To3);
					calibrator.calibrate();
		}
		else	calibrator.showErrorView(CalibrationErrorView.CALIBRATION_ERROR);
	}


	// MAIN WINDOW LISTENER

	/**
	* Method adds listener to main view
	*/
	private void addListenerToMainView() {
		if(this.mainWindow != null) {
			this.mainWindow.addHelpButtonListener(new HelpButtonListener());
			this.mainWindow.addEdgePointButtonListener(new EdgePointsButtonListener());
			this.mainWindow.addDistance1FieldListener(new TextField1Listener());
			this.mainWindow.addDistance2FieldListener(new TextField2Listener());
			this.mainWindow.addOpenFileButtonListener(new OpenFileButtonListener());
			this.mainWindow.addSelectLayerButtonListener(new SelectLayerButtonListener());
			this.mainWindow.addReferencePointButtonListener(new RefPointsButtonListener());
			this.mainWindow.addCancelButtonListener(new CancelButtonListener());
			this.mainWindow.addRunButtonListener(new RunButtonListener());
			this.mainWindow.addFrameWindowListener(getToolWindowListener());
		}
	}

	/**
	* Help button listener
	* @author rebsc
	*
	*/
	private static class HelpButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String topic = "Plugin/PicLayer";
	        // open help browser
	        HelpBrowser.setUrlForHelpTopic(Optional.ofNullable(topic).orElse("/"));
		}
	}

	/**
	* Open file button listener
	* @author rebsc
	*
	*/
	private class OpenFileButtonListener implements ActionListener {
		private JButton openButton;
		private JFileChooser fileChooser;

		@Override
		public void actionPerformed(ActionEvent event) {
			mainWindow.setVisible(false);
			openButton = mainWindow.getOpenButton();
			fileChooser = mainWindow.getFileChooser();

			if (event.getSource() == openButton) {
				int openValue = fileChooser.showOpenDialog(mainWindow);
			    if (openValue == JFileChooser.APPROVE_OPTION) {
			    	referenceFile = fileChooser.getSelectedFile();
			    	addFileInNewLayer(referenceFile);
			    }
			}
			if(referenceFile != null) {
				mainWindow.setReferenceFileNameValue(referenceFile.getName());
				mainWindow.setVisible(true);
			}
			mainWindow.setVisible(true);
		}
	}

	/**
	 * Select layer button listener
	 * @author rebsc
	 *
	 */
	private class SelectLayerButtonListener implements ActionListener {
		private SelectLayerView selector;

		@Override
		public void actionPerformed(ActionEvent event) {
			mainWindow.setVisible(false);

			selector = new SelectLayerView();
			selector.setVisible(true);

			selector.setOkButtonListener(new SelectorOkButtonListener());
			selector.setCancelButtonListener(new SelectorCancelButtonListener());
		}

		private class SelectorCancelButtonListener implements ActionListener {
			@Override
		    public void actionPerformed(ActionEvent e) {
				selector.getFrame().dispatchEvent(new WindowEvent(selector.getFrame(), WindowEvent.WINDOW_CLOSING));
		    }
		}

		private class SelectorOkButtonListener implements ActionListener {
			@Override
		    public void actionPerformed(ActionEvent e) {
				String filename = (String) selector.getList().getSelectedValue();

				if(filename != null) {
					for(Layer l : MainApplication.getLayerManager().getLayers()) {
						if(l.getName().equals(filename)) {
							referenceLayer = l;
							MainApplication.getLayerManager().setActiveLayer(l);
						}
					}
				}

				if(referenceLayer != null) {
					mainWindow.setReferenceFileNameValue(filename);
				}
				else	calibrator.showErrorView(CalibrationErrorView.SELECT_LAYER_ERROR);

				selector.setVisible(false);
				mainWindow.setVisible(true);
		    }
		}
	}

	/**
	 * Cancel button listener
	 * @author rebsc
	 *
	 */
	private class CancelButtonListener implements ActionListener {
		@Override
	    public void actionPerformed(ActionEvent e) {
			reset();
			removeListChangedListener();
	    }
	}

	/**
	 * Run button listener
	 * @author rebsc
	 *
	 */
	private class RunButtonListener implements ActionListener {
		@Override
	    public void actionPerformed(ActionEvent e) {
			// calibrate
			callCalibrator();
			currentPicLayer.clearDrawReferencePoints();
			currentPicLayer.invalidate();
			MainApplication.getLayerManager().setActiveLayer(currentPicLayer);
			mainWindow.setVisible(false);
			// let user check calibration
			ResultCheckView checkView = new ResultCheckView();
			int selectedValue = checkView.showAndChoose();
			if(selectedValue == 1) {
				currentPicLayer.getTransformer().resetCalibration();
				currentPicLayer.invalidate();
			}
	    }
	}

	/**
	 * Edge button listener
	 * @author rebsc
	 *
	 */
	private class EdgePointsButtonListener implements ActionListener {
		@Override
	    public void actionPerformed(ActionEvent e) {
				mainWindow.setVisible(false);
				MainApplication.getLayerManager().setActiveLayer(currentPicLayer);
				// switch to select mode
				MovePointAction selectPointMode = new MovePointAction();
				MainApplication.getMap().selectMapMode(selectPointMode);
		}
	}

	/**
	* Method to get windowListener for main window
	* @return adapter
	*/
	private WindowAdapter getToolWindowListener() {
		WindowAdapter adapter = new WindowAdapter() {
	    	@Override
		    public void windowDeactivated(WindowEvent wEvt) {
	    		((JFrame)wEvt.getSource()).toFront();
		    }

	    	@Override
	    	public void windowClosing(WindowEvent wEvt) {
	    		reset();
	    		removeListChangedListener();
	    	}

		};
		return adapter;
	}

	/**
	 * Origin points list listener
	 * @author rebsc
	 *
	 */
	private class OriginSizePropertyListener implements PropertyChangeListener {
	    @Override
	    public void propertyChange(PropertyChangeEvent event) {
	    	int size = (int) event.getNewValue();
	    	if(currentPicLayer.getTransformer().getLatLonOriginPoints() != null) {
	    		originPointList.clear();
	    		originPointList.addAll(currentPicLayer.getTransformer().getLatLonOriginPoints());
	    		mainWindow.setOriginPoints(originPointList);
	    	}

	        if (size == 3) {
	        	mainWindow.setVisible(true);
	        	currentPicLayer.getTransformer().getLatLonOriginPoints().removePropertyChangeListener(this);
	        }
	    }
	}

	/**
	 * Reference points list listener
	 * @author rebsc
	 *
	 */
	private class RefSizePropertyListener implements PropertyChangeListener {
	    @Override
	    public void propertyChange(PropertyChangeEvent event) {
	    	int size = (int) event.getNewValue();
	        mainWindow.setReferencePoints(referencePointList);

	        if (size == 3) {
	        	mainWindow.setVisible(true);
	        	referencePointList.removePropertyChangeListener(this);
	        }
	    }
	}

	/**
	 * Distance point 1 to point 2 field listener
	 * @author rebsc
	 *
	 */
	private class TextField1Listener implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
			currentPicLayer.setDrawFirstLine(true);
			currentPicLayer.invalidate();
			mainWindow.setDistance1Field("");
		}
		@Override
		public void focusLost(FocusEvent e) {
			currentPicLayer.setDrawFirstLine(false);
			currentPicLayer.invalidate();

			String value = mainWindow.getDistance1FieldText();
			if(validValue(value)) {
				mainWindow.getDistance1Field().selectAll();
				mainWindow.setDistance1Value(value);
				mainWindow.refresh();
				distance1To2 = Double.parseDouble(value);
			}
		}
	}

	/**
	 * Distance point 2 to point 3 field listener
	 * @author rebsc
	 *
	 */
	private class TextField2Listener implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
			currentPicLayer.setDrawSecLine(true);
			currentPicLayer.invalidate();
			mainWindow.setDistance2Field("");
		}
		@Override
		public void focusLost(FocusEvent e) {
			currentPicLayer.setDrawSecLine(false);
			currentPicLayer.invalidate();

			String value = mainWindow.getDistance2FieldText();
			if(validValue(value)) {
				mainWindow.getDistance2Field().selectAll();
				mainWindow.setDistance2Value(value);
				mainWindow.refresh();
				distance2To3 = Double.parseDouble(value);
			}
		}
	}

	/**
	 * Reference add points button listener
	 * @author rebsc
	 *
	 */
	private class RefPointsButtonListener implements ActionListener {
		@Override
	    public void actionPerformed(ActionEvent e) {
			mainWindow.setVisible(false);
			refOptionView = new ReferenceOptionView();
			int selectedValue = refOptionView.showAndChoose();

			if(selectedValue == 0) {	// defined
				MainApplication.getMap().mapView.addMouseListener(new RefDefinedPointsMouseListener());
			}
			else if(selectedValue == 1) {	// manual
				MainApplication.getMap().mapView.addMouseListener(new RefManualPointsMouseListener());
			}
	    }
	}

	/**
	 * Mouse listener for manual reference selection option
	 * @author rebsc
	 *
	 */
	private class RefManualPointsMouseListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			if(referenceFile == null && referenceLayer == null) {
				MainApplication.getMap().mapView.removeMouseListener(this);
				return;
			}

			if(referencePointList.size() < 3) {
				// add point to reference list in lat/lon scale
				LatLon latLonPoint = MainApplication.getMap().mapView.getLatLon(e.getPoint().getX(),e.getPoint().getY());
				ICoordinateFormat mCoord = CoordinateFormatManager.getDefaultFormat();
				double latY = Double.parseDouble(mCoord.latToString(latLonPoint));
				double lonX = Double.parseDouble(mCoord.lonToString(latLonPoint));
				Point2D llPoint = new Point2D.Double(lonX, latY);
				referencePointList.add(llPoint);
				// draw point
				currentPicLayer.setDrawReferencePoints(true, translatePointToPicLayerScale(llPoint));
				currentPicLayer.invalidate();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// do nothing
		}
	}

	/**
	 * Mouse listener for defined reference selection option
	 * @author rebsc
	 *
	 */
	private class RefDefinedPointsMouseListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			if(referenceFile == null && referenceLayer == null) {
				MainApplication.getMap().mapView.removeMouseListener(this);
				return;
			}

			if(referencePointList.size() < 3) {
				// get pressed point in lat/lon
				LatLon latLonPoint = MainApplication.getMap().mapView.getLatLon(e.getPoint().getX(),e.getPoint().getY());
			  	double latY = latLonPoint.getY();
		    	double lonX = latLonPoint.getX();
				Point2D llPoint = new Point2D.Double(lonX, latY);

				// get current data set and find closest point
				Point2D closestPoint = null;
				double shortestDistance = 1000000.0;	// default value
				double tmpDistance;
				DataSet data = MainApplication.getLayerManager().getEditDataSet();

				for(Node node : data.getNodes()) {
					tmpDistance = llPoint.distance(node.lon(), node.lat());

					if(tmpDistance < shortestDistance) {
						closestPoint = new Point2D.Double(node.lon(), node.lat());
						shortestDistance = tmpDistance;
					}
				}

				if(closestPoint != null) {
					// add closest point to reference list
					referencePointList.add(closestPoint);
					// draw point
					currentPicLayer.setDrawReferencePoints(true, translatePointToPicLayerScale(closestPoint));
					currentPicLayer.invalidate();
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// do nothing
		}
	}


	// GETTER / SETTER

	public CalibrationWindow getMainWindow() {
		return this.mainWindow;
	}

	public CalibrationErrorView getErrorView() {
		this.errorView = new CalibrationErrorView();
		return errorView;
	}


	// HELPER

	public void prepare(PicLayerAbstract layer) {
		this.currentPicLayer = layer;
		addListChangedListenerToPointLists();
		// if origin points, add them
		ObservableArrayList<Point2D> list = layer.getTransformer().getLatLonOriginPoints();
		if(list != null && list.size() == 3) {
			this.originPointList = list;
			this.mainWindow.setOriginPoints(originPointList);
		}
		else {
			resetLists();
		}
	}

	private void reset() {
		originPointList = new ObservableArrayList<>(3);
		referencePointList = new ObservableArrayList<>(3);
		distance1To2 = 0.0;
		distance2To3 = 0.0;
		this.referenceFile = null;
		this.referenceLayer = null;
		resetLists();
		currentPicLayer.clearDrawReferencePoints();
		currentPicLayer.invalidate();
		mainWindow.setVisible(false);
		mainWindow = new CalibrationWindow();
		addListenerToMainView();
	}

	private void resetLists() {
		currentPicLayer.getTransformer().clearOriginPoints();
		currentPicLayer.getTransformer().clearLatLonOriginPoints();
	}

	private void addListChangedListenerToPointLists() {
		OriginSizePropertyListener originListener = new OriginSizePropertyListener();
		currentPicLayer.getTransformer().getLatLonOriginPoints().addPropertyChangeListener(originListener);
		RefSizePropertyListener refListener = new RefSizePropertyListener();
		this.referencePointList.addPropertyChangeListener(refListener);
	}

	private void removeListChangedListener() {
		currentPicLayer.getTransformer().getLatLonOriginPoints().removeAllListener();
		referencePointList.removeAllListener();
	}

	/**
	 * Method to translate {@code Point2D} to {@link PicLayerAbstract} scale.
	 * @param point to translate in LatLon
	 * @return translated point in {@link PicLayerAbstract} scale
	 */
	private Point2D translatePointToPicLayerScale(Point2D point) {
		Point2D translatedPoint = null;
		LatLon ll;				// LatLon object from raw Point2D
		MapViewPoint en;		// MapViewPoint object from LatLon(ll) scaled in EastNorth(en)

		// put raw Point2D endPos into LatLon and transform LatLon into MapViewPoint (EastNorth)
		ll = new LatLon(point.getY(), point.getX());
		en = MainApplication.getMap().mapView.getState().getPointFor(ll);

		// transform EastNorth into current layer scale
		try {
			translatedPoint = currentPicLayer.transformPoint(new Point2D.Double(en.getInViewX(), en.getInViewY()));
		} catch (NoninvertibleTransformException e) {
			Logging.error(e);
		}

		return translatedPoint;
	}

	private boolean validValue(String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch (NullPointerException | NumberFormatException ex) {
			return false;
		}
	}

	private void addFileInNewLayer(File file) {
		List<File> files = new ArrayList<>();
		files.add(file);
		OpenFileAction.openFiles(files);
	}
}
