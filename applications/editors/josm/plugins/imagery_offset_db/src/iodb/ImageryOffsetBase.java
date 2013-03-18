package iodb;

import java.util.Date;
import java.util.Map;
import org.openstreetmap.josm.data.coor.CoordinateFormat;
import org.openstreetmap.josm.data.coor.LatLon;

/**
 * Stores one imagery offset record.
 * 
 * @author zverik
 */
public class ImageryOffsetBase {
    protected long offsetId;
    protected LatLon position;
    protected Date date;
    protected String author;
    protected String description;
    protected Date abandonDate;
    protected String abandonAuthor;
    protected String abandonReason;
    
    public void setBasicInfo( LatLon position, String author, String description, Date date ) {
        this.position = position;
        this.author = author;
        this.description = description;
        this.date = date;
        this.abandonDate = null;
    }

    public void setId( long id ) {
        this.offsetId = id;
    }

    public long getId() {
        return offsetId;
    }

    public void setAbandonDate(Date abandonDate) {
        this.abandonDate = abandonDate;
    }

    public Date getAbandonDate() {
        return abandonDate;
    }

    public String getAbandonAuthor() {
        return abandonAuthor;
    }

    public String getAbandonReason() {
        return abandonReason;
    }
    
    public boolean isDeprecated() {
        return abandonDate != null;
    }

    public String getAuthor() {
        return author;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public LatLon getPosition() {
        return position;
    }
    
    public void putServerParams( Map<String, String> map ) {
        map.put("lat", position.latToString(CoordinateFormat.DECIMAL_DEGREES));
        map.put("lon", position.lonToString(CoordinateFormat.DECIMAL_DEGREES));
        map.put("author", author);
        map.put("description", description);
    }

    @Override
    public String toString() {
        return "ImageryOffsetBase{" + "position=" + position + ", date=" + date + ", author=" + author + ", description=" + description + ", abandonDate=" + abandonDate + '}';
    }
}
