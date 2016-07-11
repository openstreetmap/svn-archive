package nanolog;

import java.util.Date;

import org.openstreetmap.josm.data.coor.LatLon;

/**
 * This holds one NanoLog entry.
 *
 * @author zverik
 */
public class NanoLogEntry implements Comparable<NanoLogEntry> {
    private LatLon pos;
    private Date time;
    private String message;
    private Integer direction;
    private Integer baseDir;
    private LatLon basePos;

    public NanoLogEntry(Date time, String message, LatLon basePos, Integer baseDir) {
        this.basePos = basePos;
        this.baseDir = baseDir;
        this.pos = basePos;
        this.direction = baseDir;
        this.time = time;
        this.message = message;
    }

    public NanoLogEntry(Date time, String message) {
        this(time, message, null, null);
    }

    public Integer getDirection() {
        return direction;
    }

    public String getMessage() {
        return message;
    }

    public LatLon getPos() {
        return pos;
    }

    public void setPos(LatLon pos) {
        this.pos = pos;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public LatLon getBasePos() {
        return basePos;
    }

    public Integer getBaseDir() {
        return baseDir;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public int compareTo(NanoLogEntry t) {
        return time.compareTo(t.time);
    }
}
