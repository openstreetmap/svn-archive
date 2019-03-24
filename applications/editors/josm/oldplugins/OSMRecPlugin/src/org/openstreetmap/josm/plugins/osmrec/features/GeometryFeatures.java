// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.features;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.plugins.osmrec.container.OSMWay;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import de.bwaldvogel.liblinear.FeatureNode;

/**
 * Constructs the geometry feature nodes for liblinear.
 *
 * @author imis-nkarag
 */
public class GeometryFeatures {

    private int id; //= 1422; //pass this as a param from main
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private static final int NUMBER_OF_AREA_FEATURES = 25;
    private static final int NUMBER_OF_POINTS = 13;
    private static final int NUMBER_OF_MEAN = 23; //for boolean intervals
    private static final int NUMBER_OF_VARIANCE = 37; //for boolean intervals

    public GeometryFeatures(int id) {
        this.id = id;
    }

    public void createGeometryFeatures(OSMWay wayNode) {

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////  geometry Features ///////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // geometry type feature //
        String geometryType = wayNode.getGeometry().getGeometryType();
        switch (geometryType) {
        //the IDs are unique for each geometry type
        case "LineString":
            wayNode.getFeatureNodeList().add(new FeatureNode(id, 1));
            id += 4;
            break;
        case "Polygon":
            wayNode.getFeatureNodeList().add(new FeatureNode(id+1, 1));
            id += 4;
            break;
        case "LinearRing":
            wayNode.getFeatureNodeList().add(new FeatureNode(id+2, 1));
            id += 4;
            break;
        case "Point":
            wayNode.getFeatureNodeList().add(new FeatureNode(id+3, 1));
            id += 4;
            break;
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // rectangle geometry shape feature //
        //id 1426
        if (wayNode.getGeometry().isRectangle()) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id, 1.0));
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // number of points of geometry feature //
        id++; //1427
        int numberOfPoints = wayNode.getGeometry().getNumPoints();
        numberOfPointsFeature(numberOfPoints, wayNode);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // area of geometry feature //
        //id 1440
        double area = wayNode.getGeometry().getArea();

        if (geometryType.equals("Polygon")) {

            areaFeature(area, wayNode);
            //the id increases by 25 in the areaFeature method
        } else {
            id += 25;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // resembles to a circle feature //
        //id 1465
        if (geometryResemblesCircle(wayNode)) { //this method checks if the shape of the geometry resembles to a circle
            wayNode.getFeatureNodeList().add(new FeatureNode(id, 1.0));
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // mean edge feature //

        id++;
        //TOGGLE COMMENT !! commenting out mean and variance to run the best case

        Coordinate[] nodeGeometries = wayNode.getGeometry().getCoordinates();
        List<Double> edgeLengths = new ArrayList<>();

        if (!wayNode.getGeometry().getGeometryType().toUpperCase().equals("POINT")) {
            for (int i = 0; i < nodeGeometries.length-1; i++) {
                Coordinate[] nodePair = new Coordinate[2];
                nodePair[0] = nodeGeometries[i];
                nodePair[1] = nodeGeometries[i+1];
                LineString tempGeom = geometryFactory.createLineString(nodePair);
                edgeLengths.add(tempGeom.getLength());
            }
        } else {
            edgeLengths.add(0.0);
        }
        double edgeSum = 0;
        for (Double edge : edgeLengths) {
            edgeSum = edgeSum + edge;
        }
        double mean = edgeSum/edgeLengths.size();

        //intervals with boolean values for mean feature

        handleMean(wayNode, mean);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // variance feature//
        double sum = 0;
        for (Double edge : edgeLengths) {
            sum = sum + (edge-mean)*(edge-mean);
        }

        double normalizedVariance = (sum/edgeLengths.size())/(mean*mean); //normalized with square of mean value

        handleVariance(wayNode, normalizedVariance);
        setLastID(id);
    }

    private void handleMean(OSMWay wayNode, double mean) {
        if (mean < 2) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 4) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+1, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 6) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+2, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 8) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+3, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 10) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+4, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 12) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+5, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 14) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+6, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 16) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+7, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 18) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+8, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 20) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+9, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 25) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+10, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 30) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+11, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 35) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+12, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 40) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+13, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 45) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+14, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 50) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+15, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 60) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+16, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 70) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+17, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 80) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+18, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 90) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+19, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 100) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+20, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else if (mean < 200) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+21, 1.0));
            id = id + NUMBER_OF_MEAN;
        } else {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+22, 1.0));
            id = id + NUMBER_OF_MEAN;
        }
    }

    private void handleVariance(OSMWay wayNode, double normalizedVariance) {
        if (normalizedVariance == 0) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.005) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+1, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.01) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+2, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.02) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+3, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.03) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+4, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.04) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+5, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.05) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+6, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.06) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+7, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.07) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+8, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.08) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+9, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.09) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+10, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.1) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+11, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.12) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+12, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.14) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+13, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.16) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+14, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.18) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+15, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.20) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+16, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.22) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+17, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.24) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+18, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.26) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+19, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.28) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+20, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.30) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+21, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.32) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+22, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.34) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+23, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.36) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+24, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.38) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+25, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.40) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+26, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.42) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+27, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.44) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+28, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.46) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+29, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.48) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+30, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.5) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+31, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.6) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+32, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.7) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+33, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.8) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+34, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 0.9) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+35, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else if (normalizedVariance < 1) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+36, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        } else {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+37, 1.0));
            id = id + NUMBER_OF_VARIANCE;
        }
    }

    private void numberOfPointsFeature(int numberOfPoints, OSMWay wayNode) {
        //increase the id after the feature is found for the next portion of the vector.

        if (numberOfPoints < 10) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id, 1.0));
            id += NUMBER_OF_POINTS;
        } else if (numberOfPoints < 20) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+1, 1.0));
            id += NUMBER_OF_POINTS;
        } else if (numberOfPoints < 30) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+2, 1.0));
            id += NUMBER_OF_POINTS;
        } else if (numberOfPoints < 40) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+3, 1.0));
            id += NUMBER_OF_POINTS;
        } else if (numberOfPoints < 50) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+4, 1.0));
            id += NUMBER_OF_POINTS;
        } else if (numberOfPoints < 75) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+5, 1.0));
            id += NUMBER_OF_POINTS;
        } else if (numberOfPoints < 100) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+6, 1.0));
            id += NUMBER_OF_POINTS;
        } else if (numberOfPoints < 150) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+7, 1.0));
            id += NUMBER_OF_POINTS;
        } else if (numberOfPoints < 200) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+8, 1.0));
            id += NUMBER_OF_POINTS;
        } else if (numberOfPoints < 300) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+9, 1.0));
            id += NUMBER_OF_POINTS;
        } else if (numberOfPoints < 500) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+10, 1.0));
            id += NUMBER_OF_POINTS;
        } else if (numberOfPoints < 1000) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+11, 1.0));
            id += NUMBER_OF_POINTS;
        } else {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+12, 1.0));
            id += NUMBER_OF_POINTS;
        }
    }

    private void areaFeature(double area, OSMWay wayNode) {

        if (area < 50) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 100) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+1, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 150) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+2, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 200) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+3, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 250) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+4, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 300) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+5, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 350) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+6, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 400) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+7, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 450) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+8, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 500) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+9, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 750) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+10, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 1000) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+11, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 1250) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+12, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 1500) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+13, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 1750) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+14, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 2000) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+15, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 2250) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+16, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 2500) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+17, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 2750) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+18, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 3000) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+19, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 3500) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+20, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 4000) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+21, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 5000) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+22, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else if (area < 10000) {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+23, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        } else {
            wayNode.getFeatureNodeList().add(new FeatureNode(id+24, 1.0));
            id += NUMBER_OF_AREA_FEATURES;
        }
    }

    private boolean geometryResemblesCircle(OSMWay way) {
        Geometry wayGeometry = way.getGeometry();
        boolean isCircle = false;
        if (wayGeometry.getGeometryType().equals("Polygon") && wayGeometry.getNumPoints() >= 16) {

            List<Geometry> points = way.getNodeGeometries();
            Geometry firstPoint = points.get(0);
            double radius = firstPoint.distance(wayGeometry.getCentroid());

            // buffer around the distance of the first point to centroid
            double radiusBufferSmaller = radius*0.6;
            //the rest of the point-to-centroid distances will be compared with these
            double radiusBufferGreater = radius*1.4;
            isCircle = true;

            for (Geometry point : points) {
                double tempRadius = point.distance(wayGeometry.getCentroid());
                boolean tempIsCircle = (radiusBufferSmaller <= tempRadius) && (tempRadius <= radiusBufferGreater);
                isCircle = isCircle && tempIsCircle; //if any of the points give a false, the method will return false
                //if (!isCircle) {break;}
            }

            double ratio = wayGeometry.getLength() / wayGeometry.getArea();
            boolean tempIsCircle = ratio < 0.06; //arbitary value based on statistic measure of osm instances.
            //The smaller this value, the closer this polygon resembles to a circle
            isCircle = isCircle && tempIsCircle;
        }
        return isCircle;
    }

    private void setLastID(int lastID) {
        this.id = lastID;
    }

    public int getLastID() {
        return id + 1;
    }
}
