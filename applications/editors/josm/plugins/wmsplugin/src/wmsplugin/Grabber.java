package wmsplugin;
import java.io.IOException;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.projection.Projection;

public interface Grabber {
	public GeorefImage grab(Bounds bounds,
		Projection proj, double pixelPerDegree)
		throws IOException;
}
