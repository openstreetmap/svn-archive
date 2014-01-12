// License: WTFPL
package geochat;

import java.util.Date;
import org.openstreetmap.josm.data.coor.LatLon;

/**
 * One message.
 * 
 * @author zverik
 */
public class ChatMessage implements Comparable<ChatMessage> {
    private LatLon pos;
    private Date time;
    private String author;
    private String recipient;
    private String message;
    private long id;
    private boolean priv;
    private boolean incoming;

    public ChatMessage( long id, LatLon pos, String author, boolean incoming, String message, Date time ) {
        this.id = id;
        this.author = author;
        this.message = message;
        this.pos = pos;
        this.time = time;
        this.incoming = incoming;
        this.priv = false;
        this.recipient = null;
    }

    public void setRecipient( String recipient ) {
        this.recipient = recipient;
    }

    public void setPrivate( boolean priv ) {
        this.priv = priv;
    }
    
    public String getAuthor() {
        return author;
    }

    /**
     * Is only set when the message is not incoming, that is, author is the current user.
     */
    public String getRecipient() {
        return recipient;
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

    public boolean isIncoming() {
        return incoming;
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

    public int compareTo(ChatMessage o) {
        long otherId = o.id;
        return otherId < id ? 1 : otherId == id ? 0 : 1;
    }
}
