// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

/**
 * A geographical coordinate consisting of latitude and longitude.
 *
 * @author Jan Peter Stotz
 *
 */
public class Coordinate implements ICoordinate, Serializable {
    private static final long serialVersionUID = 1L;
    private double x;
    private double y;

    /**
     * Constructs a new {@code Coordinate}.
     * @param lat latitude in degrees
     * @param lon longitude in degrees
     */
    public Coordinate(double lat, double lon) {
        setLat(lat);
        setLon(lon);
    }

    @Override
    public double getLat() {
        return y;
    }

    @Override
    public void setLat(double lat) {
        y = lat;
    }

    @Override
    public double getLon() {
        return x;
    }

    @Override
    public void setLon(double lon) {
        x = lon;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(x);
        out.writeObject(y);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        x = (Double) in.readObject();
        y = (Double) in.readObject();
    }

    @Override
    public String toString() {
        return "Coordinate[" + y + ", " + x + ']';
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;
        Coordinate that = (Coordinate) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0;
    }
}
