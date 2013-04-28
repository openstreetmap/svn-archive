package geochat;

import java.util.*;
import org.openstreetmap.josm.data.coor.LatLon;

/**
 * An interface for listening for chat events.
 *
 * @author zverik
 */
public interface ChatServerConnectionListener {
    /**
     * User has been logged in.
     * @param userName Name of the logged in user.
     */
    void loggedIn( String userName );
    
    /**
     * User tried to log in, but failed.
     * @param reason Why.
     */
    void notLoggedIn( String reason );

    /**
     * Sending message failed.
     * @param reason Why.
     */
    void messageSendFailed( String reason );

    /**
     * Chat has become active or not.
     * @param active Is the chat active.
     */
    void statusChanged( boolean active );

    /**
     * Received an update to users list.
     * @param users a hash of user names and coordinates.
     */
    void updateUsers( Map<String, LatLon> users );

    /**
     * New messages were received. This would only be called in active state.
     * @param replace if set, remove all old messages.
     * @param messages new messages array.
     */
    void receivedMessages( boolean replace, List<ChatMessage> messages );

    /**
     * New private messages were received. See {@link #receivedMessages(boolean, java.util.List)}.
     * Note that the array of messages can be reset, for example, when user has changed.
     * @param replace if set, remove all old messages.
     * @param messages list of new private messages.
     */
    void receivedPrivateMessages( boolean replace, List<ChatMessage> messages );
}
