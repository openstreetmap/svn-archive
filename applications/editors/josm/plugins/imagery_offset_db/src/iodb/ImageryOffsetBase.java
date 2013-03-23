package iodb;

import java.util.Date;
import java.util.Map;
import org.openstreetmap.josm.data.coor.CoordinateFormat;
import org.openstreetmap.josm.data.coor.LatLon;

/**
 * Stores one offset record. It is the superclass for {@link ImageryOffset}
 * and {@link CalibrationObject} classes and contains common fields
 * like position, author and description.
 * 
 * @author Zverik
 * @license WTFPL
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
    protected boolean flagged;
    
    /**
     * Initialize object with the basic information. It's offset location, author, date
     * and description.
     */
    public void setBasicInfo( LatLon position, String author, String description, Date date ) {
        this.position = position;
        this.author = author;
        this.description = description;
        this.date = date;
        this.abandonDate = null;
        this.flagged = false;
    }

    public void setId( long id ) {
        this.offsetId = id;
    }

    public long getId() {
        return offsetId;
    }

    /**
     * Mark the offset as deprecated. Though there is no exact field for "isDeprecated",
     * it is deduced from abandonDate, author and reason being not null.
     */
    public void setDeprecated(Date abandonDate, String author, String reason) {
        this.abandonDate = abandonDate;
        this.abandonAuthor = author;
        this.abandonReason = reason;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged( boolean flagged ) {
        this.flagged = flagged;
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
    
    /**
     * Check that {@link #getAbandonDate()} is not null. Note that
     * is doesn't say anything about abandonAuthor or abandonReason.
     */
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
