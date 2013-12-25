package render;

import java.awt.geom.Point2D;

import s57.S57map.*;

public interface MapContext {
	Point2D getPoint(Snode coord);
	double mile(Feature feature);
}
