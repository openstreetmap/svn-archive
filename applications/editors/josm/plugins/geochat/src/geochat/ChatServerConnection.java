// License: WTFPL. For details, see LICENSE file.
package geochat;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.coor.conversion.DecimalDegreesCoordinateFormat;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

/**
 * This class holds all the chat data and periodically polls the server.
 *
 * @author zverik
 */
final class ChatServerConnection {
    public static final String TOKEN_PREFIX = "=";
    private static final String TOKEN_PATTERN = "^[a-zA-Z0-9]{10}$";

    private int userId;
    private String userName;
    private static ChatServerConnection instance;
    private Set<ChatServerConnectionListener> listeners;
    private LogRequest requestThread;

    private ChatServerConnection() {
        userId = 0;
        userName = null;
        listeners = new HashSet<>();
        requestThread = new LogRequest();
        new Thread(requestThread).start();
    }

    public static ChatServerConnection getInstance() {
        if (instance == null)
            instance = new ChatServerConnection();
        return instance;
    }

    public void addListener(ChatServerConnectionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ChatServerConnectionListener listener) {
        listeners.remove(listener);
    }

    public boolean isActive() {
        return isLoggedIn() && getPosition() != null;
    }

    public boolean isLoggedIn() {
        return userId > 0;
    }

    public String getUserName() {
        return userName;
    }

    /**
     * Test that userId is still active, log out otherwise.
     */
    public void checkLogin() {
        autoLogin(null);
    }

    /**
     * Test that userId is still active, if not, tries to login with given user name.
     * Does not autologin, if userName is null, obviously.
     */
    public void autoLogin(final String userName) {
        final int uid = Config.getPref().getInt("geochat.lastuid", 0);
        if (uid <= 0) {
            if (userName != null && userName.length() > 1)
                login(userName);
        } else {
            String query = "whoami&uid=" + uid;
            JsonQueryUtil.queryAsync(query, new JsonQueryCallback() {
                @Override
                public void processJson(JsonObject json) {
                    if (json != null && json.get("name") != null)
                        login(uid, json.getString("name"));
                    else if (userName != null && userName.length() > 1)
                        login(userName);
                }
            });
        }
    }

    /**
     * Waits until {@link #getPosition()} is not null, then calls {@link #autoLogin(java.lang.String)}.
     * If two seconds have passed, stops the waiting. Doesn't wait if userName is empty.
     */
    public void autoLoginWithDelay(final String userName) {
        if (userName == null || userName.length() == 0) {
            checkLogin();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int cnt = 10;
                    while (getPosition() == null && cnt-- > 0) {
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    Logging.warn(e);
                }
                autoLogin(userName);
            }
        }).start();
    }

    public void login(final String userName) {
        if (userName == null)
            throw new IllegalArgumentException("userName is null");
        LatLon pos = getPosition();
        if (pos == null) {
            fireLoginFailed("Zoom level is too low");
            return;
        }
        String token = userName.startsWith(TOKEN_PREFIX) ? userName.substring(TOKEN_PREFIX.length()) : null;
        if (token != null && !token.matches(TOKEN_PATTERN)) {
            fireLoginFailed("Incorrect token format");
            return;
        }

        try {
            String nameAttr = token != null ? "&token=" + token : "&name=" + URLEncoder.encode(userName, "UTF-8");
            String query = "register&lat=" + DecimalDegreesCoordinateFormat.INSTANCE.latToString(pos)
            + "&lon=" + DecimalDegreesCoordinateFormat.INSTANCE.lonToString(pos)
            + nameAttr;
            JsonQueryUtil.queryAsync(query, new JsonQueryCallback() {
                @Override
                public void processJson(JsonObject json) {
                    if (json == null)
                        fireLoginFailed(tr("Could not get server response, check logs"));
                    else if (json.get("error") != null)
                        fireLoginFailed(tr("Failed to login as {0}:", userName) + "\n" + json.getString("error"));
                    else if (json.get("uid") == null)
                        fireLoginFailed(tr("The server did not return user ID"));
                    else {
                        String name = json.get("name") != null ? json.getString("name") : userName;
                        login(json.getInt("uid"), name);
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            Logging.error(e);
        }
    }

    private void login(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        Config.getPref().putInt("geochat.lastuid", userId);
        for (ChatServerConnectionListener listener : listeners) {
            listener.loggedIn(userName);
        }
    }

    private void logoutIntl() {
        ChatServerConnection.this.userId = 0;
        ChatServerConnection.this.userName = null;
        Config.getPref().put("geochat.lastuid", null);
        for (ChatServerConnectionListener listener : listeners) {
            listener.notLoggedIn(null);
        }
    }

    private void fireLoginFailed(String reason) {
        for (ChatServerConnectionListener listener : listeners) {
            listener.notLoggedIn(reason);
        }
    }

    /**
     * Unregister the current user.
     */
    public void logout() {
        if (!isLoggedIn())
            return;
        String query = "logout&uid=" + userId;
        JsonQueryUtil.queryAsync(query, new JsonQueryCallback() {
            @Override
            public void processJson(JsonObject json) {
                if (json != null && json.get("message") != null) {
                    logoutIntl();
                }
            }
        });
    }

    /**
     * Unregister the current user and do not call listeners.
     * Makes synchronous request to the server.
     */
    public void bruteLogout() throws IOException {
        if (isLoggedIn())
            JsonQueryUtil.query("logout&uid=" + userId);
    }

    private void fireMessageFailed(String reason) {
        for (ChatServerConnectionListener listener : listeners) {
            listener.messageSendFailed(reason);
        }
    }

    /**
     * Posts message to the main channel.
     * @param message Message string.
     * @see #postMessage(java.lang.String, java.lang.String)
     */
    public void postMessage(String message) {
        postMessage(message, null);
    }

    /**
     * Posts message to the main channel or to a specific user.
     * Calls listener on fail.
     * @param message Message string.
     * @param targetUser null if sending to everyone, name of user otherwise.
     */
    public void postMessage(String message, String targetUser) {
        if (!isLoggedIn()) {
            fireMessageFailed("Not logged in");
            return;
        }
        LatLon pos = getPosition();
        if (pos == null) {
            fireMessageFailed("Zoom level is too low");
            return;
        }
        try {
            String query = "post&lat=" + DecimalDegreesCoordinateFormat.INSTANCE.latToString(pos)
            + "&lon=" + DecimalDegreesCoordinateFormat.INSTANCE.lonToString(pos)
            + "&uid=" + userId
            + "&message=" + URLEncoder.encode(message, "UTF8");
            if (targetUser != null && targetUser.length() > 0)
                query += "&to=" + URLEncoder.encode(targetUser, "UTF8");
            JsonQueryUtil.queryAsync(query, new JsonQueryCallback() {
                @Override
                public void processJson(JsonObject json) {
                    if (json == null)
                        fireMessageFailed(tr("Could not get server response, check logs"));
                    else if (json.get("error") != null)
                        fireMessageFailed(json.getString("error"));
                }
            });
        } catch (UnsupportedEncodingException e) {
            Logging.error(e);
        }
    }

    /**
     * Returns current coordinates or null if there is no map, or zoom is too low.
     */
    private static LatLon getPosition() {
        if (!MainApplication.isDisplayingMapView())
            return null;
        if (getCurrentZoom() < 10)
            return null;
        Projection proj = ProjectionRegistry.getProjection();
        return proj.eastNorth2latlon(MainApplication.getMap().mapView.getCenter());
    }

    // Following three methods were snatched from TMSLayer
    private static double latToTileY(double lat, int zoom) {
        double l = lat / 180 * Math.PI;
        double pf = Math.log(Math.tan(l) + (1 / Math.cos(l)));
        return Math.pow(2.0, zoom - 1) * (Math.PI - pf) / Math.PI;
    }

    private static double lonToTileX(double lon, int zoom) {
        return Math.pow(2.0, zoom - 3) * (lon + 180.0) / 45.0;
    }

    public static int getCurrentZoom() {
        if (!MainApplication.isDisplayingMapView()) {
            return 1;
        }
        MapView mv = MainApplication.getMap().mapView;
        LatLon topLeft = mv.getLatLon(0, 0);
        LatLon botRight = mv.getLatLon(mv.getWidth(), mv.getHeight());
        double x1 = lonToTileX(topLeft.lon(), 1);
        double y1 = latToTileY(topLeft.lat(), 1);
        double x2 = lonToTileX(botRight.lon(), 1);
        double y2 = latToTileY(botRight.lat(), 1);

        int screenPixels = mv.getWidth() * mv.getHeight();
        double tilePixels = Math.abs((y2 - y1) * (x2 - x1) * 256 * 256);
        if (screenPixels == 0 || tilePixels == 0) {
            return 1;
        }
        double factor = screenPixels / tilePixels;
        double result = Math.log(factor) / Math.log(2) / 2 + 1;
        int intResult = (int) Math.floor(result);
        return intResult;
    }

    private class LogRequest implements Runnable {
        private static final int MAX_JUMP = 20000; // in meters
        private LatLon lastPosition = null;
        private long lastUserId = 0;
        private long lastId = 0;
        private boolean lastStatus = false;
        private boolean stopping = false;

        @Override
        public void run() {
            //            lastId = Config.getPref().getLong("geochat.lastid", 0);
            int interval = Config.getPref().getInt("geochat.interval", 2);
            while (!stopping) {
                process();
                try {
                    Thread.sleep(interval * 1000);
                } catch (InterruptedException e) {
                    stopping = true;
                }
            }
        }

        public void stop() {
            stopping = true;
        }

        public void process() {
            if (!isLoggedIn()) {
                fireStatusChanged(false);
                return;
            }

            LatLon pos = getPosition();
            if (pos == null) {
                fireStatusChanged(false);
                return;
            }
            fireStatusChanged(true);

            final boolean needReset;
            final boolean needFullReset = lastUserId != userId;
            if (needFullReset || (lastPosition != null && pos.greatCircleDistance(lastPosition) > MAX_JUMP)) {
                // reset messages
                lastId = 0;
                //                Config.getPref().put("geochat.lastid", null);
                needReset = true;
            } else
                needReset = false;
            lastUserId = userId;
            lastPosition = pos;

            String query = "get&lat=" + DecimalDegreesCoordinateFormat.INSTANCE.latToString(pos)
            + "&lon=" + DecimalDegreesCoordinateFormat.INSTANCE.lonToString(pos)
            + "&uid=" + userId + "&last=" + lastId;
            JsonObject json;
            try {
                json = JsonQueryUtil.query(query);
            } catch (IOException ex) {
                json = null; // ?
            }
            if (json == null) {
                // do nothing?
                //              fireLoginFailed(tr("Could not get server response, check logs"));
                //              logoutIntl(); // todo: uncomment?
            } else if (json.get("error") != null) {
                fireLoginFailed(tr("Failed to get messages as {0}:", userName) + "\n" + json.getString("error"));
                logoutIntl();
            } else {
                if (json.get("users") != null) {
                    Map<String, LatLon> users = parseUsers(json.getJsonArray("users"));
                    for (ChatServerConnectionListener listener : listeners) {
                        listener.updateUsers(users);
                    }
                }
                if (json.get("messages") != null) {
                    List<ChatMessage> messages = parseMessages(json.getJsonArray("messages"), false);
                    for (ChatMessage m : messages) {
                        if (m.getId() > lastId)
                            lastId = m.getId();
                    }
                    for (ChatServerConnectionListener listener : listeners) {
                        listener.receivedMessages(needReset, messages);
                    }
                }
                if (json.get("private") != null) {
                    List<ChatMessage> messages = parseMessages(json.getJsonArray("private"), true);
                    for (ChatMessage m : messages) {
                        if (m.getId() > lastId)
                            lastId = m.getId();
                    }
                    for (ChatServerConnectionListener listener : listeners) {
                        listener.receivedPrivateMessages(needFullReset, messages);
                    }
                }
            }
            //                    if (lastId > 0 && Config.getPref().getBoolean("geochat.store.lastid", true) )
            //                        Config.getPref().putLong("geochat.lastid", lastId);
        }

        private List<ChatMessage> parseMessages(JsonArray messages, boolean priv) {
            List<ChatMessage> result = new ArrayList<>();
            for (int i = 0; i < messages.size(); i++) {
                try {
                    JsonObject msg = messages.getJsonObject(i);
                    long id = Long.parseLong(msg.getString("id"));
                    double lat = Double.parseDouble(msg.getString("lat"));
                    double lon = Double.parseDouble(msg.getString("lon"));
                    long timeStamp = Long.parseLong(msg.getString("timestamp"));
                    String author = msg.getString("author");
                    String message = msg.getString("message");
                    boolean incoming = msg.getBoolean("incoming");
                    ChatMessage cm = new ChatMessage(id, new LatLon(lat, lon), author,
                            incoming, message, new Date(timeStamp * 1000));
                    cm.setPrivate(priv);
                    if (msg.get("recipient") != null && !incoming)
                        cm.setRecipient(msg.getString("recipient"));
                    result.add(cm);
                } catch (JsonException e) {
                    Logging.trace(e);
                }
            }
            return result;
        }

        private Map<String, LatLon> parseUsers(JsonArray users) {
            Map<String, LatLon> result = new HashMap<>();
            for (int i = 0; i < users.size(); i++) {
                try {
                    JsonObject user = users.getJsonObject(i);
                    String name = user.getString("user");
                    double lat = Double.parseDouble(user.getString("lat"));
                    double lon = Double.parseDouble(user.getString("lon"));
                    result.put(name, new LatLon(lat, lon));
                } catch (JsonException e) {
                    Logging.trace(e);
                }
            }
            return result;
        }

        private void fireStatusChanged(boolean newStatus) {
            if (newStatus == lastStatus)
                return;
            lastStatus = newStatus;
            for (ChatServerConnectionListener listener : listeners) {
                listener.statusChanged(newStatus);
            }
        }
    }
}
