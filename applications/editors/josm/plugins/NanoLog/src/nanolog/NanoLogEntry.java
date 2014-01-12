package nanolog;

import java.util.Date;
import org.openstreetmap.josm.data.coor.LatLon;

/**
 * This holds one NanoLog entry.
 * 
 * @author zverik
 */
public class NanoLogEntry {
    private LatLon pos;
    private Date time;
    private String message;
    private int direction;
    private LatLon tmpPos;

    public int getDirection() {
        return direction;
    }

    public String getMessage() {
        return message;
    }

    public LatLon getPos() {
        return tmpPos == null ? pos : tmpPos;
    }

    public Date getTime() {
        return time;
    }
}
