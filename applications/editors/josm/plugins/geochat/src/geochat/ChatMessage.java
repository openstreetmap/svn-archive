package geochat;

import java.util.Date;
import org.openstreetmap.josm.data.coor.LatLon;

/**
 * One message.
 * 
 * @author zverik
 */
public class ChatMessage implements Comparable {
    private LatLon pos;
    private Date time;
    private String author;
    private String message;
    private long id;
    private boolean priv;

    public ChatMessage( long id, LatLon pos, String author, String message, Date time ) {
        this.id = id;
        this.author = author;
        this.message = message;
        this.pos = pos;
        this.time = time;
        this.priv = false;
    }

    public void setPrivate( boolean priv ) {
        this.priv = priv;
    }
    
    public String getAuthor() {
        return author;
    }

    public long getId() {
        return id;
    }

    public LatLon getPosition() {
        return pos;
    }

    public String getMessage() {
        return message;
    }
    
    public boolean isPrivate() {
        return priv;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChatMessage other = (ChatMessage) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    public int compareTo(Object o) {
        long otherId = ((ChatMessage)o).id;
        return otherId < id ? 1 : otherId == id ? 0 : 1;
    }
}
