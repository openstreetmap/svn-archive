package iodb;

import java.util.Date;
import org.openstreetmap.josm.data.coor.LatLon;

/**
 * Stores one imagery offset record.
 * 
 * @author zverik
 */
public class ImageryOffsetBase {
    private LatLon position;
    private Date date;
    private String author;
    private String description;
    private Date abandonDate;
    
    public void setBasicInfo( LatLon position, String author, String description, Date date ) {
        this.position = position;
        this.author = author;
        this.description = description;
        this.date = date;
        this.abandonDate = null;
    }

    public void setAbandonDate(Date abandonDate) {
        this.abandonDate = abandonDate;
    }

    public Date getAbandonDate() {
        return abandonDate;
    }
    
    public boolean isAbandoned() {
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

    public LatLon getPosition() {
        return position;
    }
}
