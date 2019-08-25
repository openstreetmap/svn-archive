package org.openstreetmap.josm.plugins.piclayer.actions.transform.autocalibrate;

import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapViewState.MapViewPoint;
import org.openstreetmap.josm.plugins.piclayer.PicLayerPlugin;
import org.openstreetmap.josm.plugins.piclayer.actions.transform.autocalibrate.helper.GeoLine;
import org.openstreetmap.josm.plugins.piclayer.gui.autocalibrate.CalibrationErrorView;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;
import org.openstreetmap.josm.tools.Logging;


/**
 * Class for image calibration.
 * Info at https://wiki.openstreetmap.org/wiki/User:Rebsc
 * @author rebsc
 *
 */
public class AutoCalibrator {

	private PicLayerAbstract currentLayer;
	private List<Point2D> startPositions;	// raw data - LatLon scale
	private List<Point2D> endPositions;		// raw data - LatLon scale
	private double distance1To2;	// in meter
	private double distance2To3;	// in meter



	public AutoCalibrator() {
		this.currentLayer = null;
		this.startPositions = new ArrayList<>(3);
		this.endPositions = new ArrayList<>(3);
		this.distance1To2 = 0.0;
		this.distance2To3 = 0.0;
	}

	/**
	 * Calibrator - collects raw data for calibration
	 * @param abstractLayer {@link PicLayerAbstract} - currentLayer
	 * @param startPoints representing points set on the image with using {@link PicLayerPlugin} actions, raw data - LatLon scale
	 * @param endPoints representing points set on the image with using {@link PicLayerPlugin} actions, raw data - LatLon scale
	 * @param distance12 distance from point 1 to point 2 in startPoints - entered with using PicLayerPlugin actions. Scaled in meter.
	 * @param distance23 distance from point 2 to point 3 in startPoints - entered with using PicLayerPlugin actions. Scaled in meter.
	 */
	public AutoCalibrator(PicLayerAbstract abstractLayer, List<Point2D> startPoints, List<Point2D> endPoints,
			double distance12, double distance23) {
		this.currentLayer = abstractLayer;
		this.startPositions = startPoints;
		this.endPositions = endPoints;
		this.distance1To2 = distance12;
		this.distance2To3 = distance23;
	}


	/**
	 * Calibrates Image with given data.
	 * Sets start points to end points and corrects end points by passed distances between points.
	 */
	public void calibrate() {
		// get start / end points
		List<Point2D> startPointList = currentLayer.getTransformer().getOriginPoints();				// in current layer scale
		List<Point2D> endPointList = correctedPoints(endPositions, distance1To2, distance2To3);		// in lat/lon scale - translation follows

		// calibrate
		if(currentLayer != null && startPointList != null && endPointList != null
				&& startPointList.size() == 3 && endPointList.size() == 3
				&& distance1To2 != 0.0 && distance2To3 != 0.0) {

			Point2D tsPoint;		// transformed start point
			Point2D tePoint;		// transformed end point
			int index;

			// move all points to final state position
			for(Point2D endPos : endPointList) {
				// get translated start point suitable to end point
				index = endPointList.indexOf(endPos);
				tsPoint = startPointList.get(index);

				// transform end point into current layer scale
				tePoint = translatePointToCurrentScale(endPos);

				// move start point to end point
				currentLayer.getTransformer().updatePair(tsPoint, tePoint);
			}

			// check if image got too distorted after calibration, if, reset and show error.
			// Input start positions (lat/lon), corrected end positions (lat/lon)
			if(!checkCalibration(startPositions, endPointList)) {
				currentLayer.getTransformer().resetCalibration();
				showErrorView(CalibrationErrorView.DIMENSION_ERROR);
			}
		} else {
			// calibration failed
			showErrorView(CalibrationErrorView.CALIBRATION_ERROR);
		}

	}

	/**
	 * Compare side ratios before/after calibration
	 * @param list with ratios to compare to other list
	 * @param compareList with ratios to compare to other list
	 * @return true if ratios equals in range of (+-)0.5, else false
	 */
	private boolean checkCalibration(List<Point2D> list, List<Point2D> compareList) {
		if(list.size() != 3 || compareList.size() != 3)	return false;

		// check site ratios before and after calibration
		double dist12 = new GeoLine(list.get(0), list.get(1)).getDistance();
		double dist23 = new GeoLine(list.get(1), list.get(2)).getDistance();
		double dist13 = new GeoLine(list.get(0), list.get(2)).getDistance();
		double[] startRatio = {1, dist23/dist12,  dist13/dist12};
		double compDist12 = new GeoLine(compareList.get(0), compareList.get(1)).getDistance();
		double compDist23 = new GeoLine(compareList.get(1), compareList.get(2)).getDistance();
		double compDist13 = new GeoLine(compareList.get(0), compareList.get(2)).getDistance();
		double[] compRatio = {1, compDist23/compDist12,  compDist13/compDist12};
		double epsilon = 0.5;

		if(compRatio[1] >= startRatio[1]-epsilon && compRatio[1] <= startRatio[1]+epsilon
				&& compRatio[2] >= startRatio[2]-epsilon && compRatio[2] <= startRatio[2]+ epsilon) {
			return true;
		}
		return false;
	}


	/**
	 * Corrects points with given distances. Calculates new points on lines
	 * between given points at given distances.
	 * @param points need to be corrected
	 * @param distance12 distance between point 1 and point 2 in meter
	 * @param distance23 distance between point 2 and point 3 in meter
	 * @return corrected points
	 */
	private List<Point2D> correctedPoints(List<Point2D> points, double distance12, double distance23){
		if(points != null && points.size() == 3) {
			List<Point2D> correctedList = new ArrayList<>();

			// get line between point1 and point2
			GeoLine line12 = new GeoLine(points.get(0), points.get(1));
			// get line between point2 and point3
			GeoLine line23 = new GeoLine(points.get(1), points.get(2));

			correctedList.add(points.get(0));	// anchor
			correctedList.add(line12.pointOnLine(distance12));	// point on line12 at distance12 from start on
			// get lat/lon offset of line12 point to origin point2
			double lonOffset = line12.pointOnLine(distance12).getX() - points.get(1).getX();
			double latOffset = line12.pointOnLine(distance12).getY() - points.get(1).getY();
			// get point on line23 and add offset - to not deform the image
			Point2D pointOnLine23 = line23.pointOnLine(distance23);
			Point2D correctedPointOnLine23 = new Point2D.Double(pointOnLine23.getX()+ lonOffset, pointOnLine23.getY() + latOffset);
			correctedList.add(correctedPointOnLine23);	// point on line23 at distance23 from start on corrected with offset from point on line12

			return correctedList;
		}
		return null;
	}

	/**
	 * Method to translate {@code Point2D} to current layer scale.
	 * @param point to translate in LatLon
	 * @return translated point in current layer scale
	 */
	private Point2D translatePointToCurrentScale(Point2D point) {
		Point2D translatedPoint = null;
		LatLon ll;				// LatLon object from raw Point2D
		MapViewPoint en;		// MapViewPoint object from LatLon(ll) scaled in EastNorth(en)

		// put raw Point2D endPos into LatLon and transform LatLon into MapViewPoint (EastNorth)
		ll = new LatLon(point.getY(), point.getX());
		en = MainApplication.getMap().mapView.getState().getPointFor(ll);

		// transform EastNorth into current layer scale
		try {
			translatedPoint = currentLayer.transformPoint(new Point2D.Double(en.getInViewX(), en.getInViewY()));
		} catch (NoninvertibleTransformException e) {
			Logging.error(e);
		}

		return translatedPoint;
	}

	/**
	 * Shows error view
	 * @param msg error msg
	 */
	public void showErrorView(String msg) {
		AutoCalibrateHandler handler = new AutoCalibrateHandler();
		handler.getErrorView().show(msg);
	}


	// GETTER / SETTER

	public void setCurrentLayer(PicLayerAbstract currentLayer) {
		this.currentLayer = currentLayer;
	}

	/**
	 * Set start positions scaled in Lat/Lon
	 * @param startPositions calibration start positions
	 */
	public void setStartPositions(List<Point2D> startPositions) {
		this.startPositions = startPositions;
	}

	/**
	 * Set end positions scaled in Lat/Lon
	 * @param endPositions calibration end positions
	 */
	public void setEndPositions(List<Point2D> endPositions) {
		this.endPositions = endPositions;
	}

	/**
	 * Set distance from point 1 to point 2 in start positions.
	 * Scaled in meter.
	 * @param distance12 distance from point 1 to point 2 in start positions. Scaled in meter.
	 */
	public void setDistance1To2(double distance12) {
		this.distance1To2 = distance12;
	}

	/**
	 * Set distance from point 2 to point 3 in start positions.
	 * Scaled in meter.
	 * @param distance23 distance from point 1 to point 2 in start positions. Scaled in meter.
	 */
	public void setDistance2To3(double distance23) {
		this.distance2To3 = distance23;
	}
}
