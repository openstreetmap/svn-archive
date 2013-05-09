package geochat;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.JosmUserIdentityManager;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.tools.GBC;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

/**
 * Chat Panel. Contains of one public chat pane and multiple private ones.
 *
 * @author zverik
 */
public class GeoChatPanel extends ToggleDialog implements ChatServerConnectionListener, MapViewPaintable {
    private JTextField input;
    private JTabbedPane tabs;
    private JComponent noData;
    private JPanel loginPanel;
    private JPanel gcPanel;
    private ChatServerConnection connection;
    // those fields should be visible to popup menu actions
    protected Map<String, LatLon> users;
    protected ChatPaneManager chatPanes;
    protected boolean userLayerActive;
    
    public GeoChatPanel() {
        super(tr("GeoChat"), "geochat", tr("Open GeoChat panel"), null, 200, true);

        noData = new JLabel(tr("Zoom in to see messages"), SwingConstants.CENTER);

        tabs = new JTabbedPane();
        tabs.addMouseListener(new GeoChatPopupAdapter(this));
        chatPanes = new ChatPaneManager(this, tabs);

        input = new JPanelTextField() {
            @Override
            protected void processEnter( String text ) {
                connection.postMessage(text, chatPanes.getRecipient());
            }

            @Override
            protected String autoComplete( String word ) {
                return autoCompleteUser(word);
            }
        };

        loginPanel = createLoginPanel();

        gcPanel = new JPanel(new BorderLayout());
        gcPanel.add(loginPanel, BorderLayout.CENTER);
        createLayout(gcPanel, false, null);

        users = new TreeMap<String, LatLon>();
        // Start threads
        connection = ChatServerConnection.getInstance();
        connection.addListener(this);
        connection.checkLogin();
    }

    private JPanel createLoginPanel() {
        final JTextField nameField = new JPanelTextField() {
            @Override
            protected void processEnter( String text ) {
                connection.login(text);
            }
        };
        String userName = JosmUserIdentityManager.getInstance().getUserName();
        if( userName == null )
            userName = "";
        if( userName.contains("@") )
            userName = userName.substring(0, userName.indexOf('@'));
        userName = userName.replace(' ', '_');
        nameField.setText(userName);

        JButton loginButton = new JButton(tr("Login"));
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                connection.login(nameField.getText());
            }
        });
        nameField.setPreferredSize(new Dimension(nameField.getPreferredSize().width, loginButton.getPreferredSize().height));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(nameField, GBC.std().fill(GridBagConstraints.HORIZONTAL).insets(15, 0, 5, 0));
        panel.add(loginButton, GBC.std().fill(GridBagConstraints.NONE).insets(0, 0, 15, 0));
        return panel;
    }

    protected void logout() {
        connection.logout();
    }

    private String autoCompleteUser( String word ) {
        return word; // todo: write autocomplete
    }

    /**
     * This is implementation of a "temporary layer". It paints circles
     * for all users nearby.
     */
    public void paint( Graphics2D g, MapView mv, Bounds bbox ) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setColor(Color.yellow);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));

        int zoom = ChatServerConnection.getCurrentZoom();
        int radius = Math.max(zoom, 1) * 10;
        if( zoom < 14 )
            radius /= 2;

        Font font = g2d.getFont().deriveFont(Font.BOLD, Math.max(zoom * 2, 8));
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        for( String user : users.keySet() ) {
            Point p = mv.getPoint(users.get(user));
            g2d.setColor(Color.yellow);
            g2d.fillOval(p.x - radius, p.y - radius, radius * 2 + 1, radius * 2 + 1);

            g2d.setColor(Color.black);
            Rectangle2D rect = fm.getStringBounds(user, g2d);
            g2d.drawString(user, p.x - Math.round(rect.getWidth() / 2), p.y);
        }
    }

    /* ================== Notifications in the title ======================= */

    private String cachedTitle = null;
    private int cachedAlarm = 0;

    @Override
    public void setTitle( String title ) {
        setTitle(title, -1);
    }

    private void setTitle( String title, int alarmLevel ) {
        boolean changed = false;
        if( title != null && (cachedTitle == null || !cachedTitle.equals(title)) ) {
            cachedTitle = title;
            changed = true;
        }
        if( alarmLevel >= 0 && cachedAlarm != alarmLevel ) {
            cachedAlarm = alarmLevel;
            changed = true;
        }
        if( changed ) {
            String alarm = cachedAlarm <= 0 ? "" : cachedAlarm == 1 ? "* " : "!!! ";
            super.setTitle(alarm + cachedTitle);
            // todo: title becomes cut off
        }
    }

    protected void updateTitleAlarm() {
        int notifyLevel = chatPanes.getNotifyLevel();
        setTitle(null, isDialogInCollapsedView() ? notifyLevel : Math.min(1, notifyLevel));
    }

    @Override
    protected void setIsCollapsed( boolean val ) {
        super.setIsCollapsed(val);
        chatPanes.setCollapsed(val);
        updateTitleAlarm();
    }

    /* ============ ChatServerConnectionListener methods ============= */

    public void loggedIn( String userName ) {
        if( gcPanel.getComponentCount() == 1 ) {
            gcPanel.remove(0);
            gcPanel.add(tabs, BorderLayout.CENTER);
            gcPanel.add(input, BorderLayout.SOUTH);
        }
    }

    public void notLoggedIn( final String reason ) {
        if( reason != null ) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    JOptionPane.showMessageDialog(Main.parent, reason);
                }
            });
        } else {
            // regular logout
            if( gcPanel.getComponentCount() > 1 ) {
                gcPanel.removeAll();
                gcPanel.add(loginPanel, BorderLayout.CENTER);
            }
        }
    }

    public void messageSendFailed( final String reason ) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(Main.parent, reason);
            }
        });
    }

    public void statusChanged( boolean active ) {
        // only the public tab, because private chats don't rely on coordinates
        tabs.setComponentAt(0, active ? chatPanes.getPublicChatComponent() : noData);
        repaint();
    }

    public void updateUsers( Map<String, LatLon> newUsers ) {
        for( String uname : this.users.keySet() ) {
            if( !newUsers.containsKey(uname) )
                chatPanes.addLineToPublic(tr("User {0} has left", uname));
        }
        for( String uname : newUsers.keySet() ) {
            if( !this.users.containsKey(uname) )
                chatPanes.addLineToPublic(tr("User {0} is mapping nearby", uname));
        }
        setTitle(trn("GeoChat ({0} user)", "GeoChat ({0} users)", newUsers.size(), newUsers.size()));
        this.users = newUsers;
        if( userLayerActive && Main.map.mapView != null )
            Main.map.mapView.repaint();
    }

    private final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    private void formatMessage( StringBuilder sb, ChatMessage msg ) {
        sb.append("\n");
        sb.append('[').append(TIME_FORMAT.format(msg.getTime())).append("] ");
        sb.append(msg.getAuthor()).append(": ").append(msg.getMessage());
    }

    public void receivedMessages( boolean replace, List<ChatMessage> messages ) {
        if( replace )
            chatPanes.clearPublicChatPane();
        if( !messages.isEmpty() ) {
            int alarm = 0;
            StringBuilder sb = new StringBuilder();
            for( ChatMessage msg : messages ) {
                formatMessage(sb, msg);
                if( msg.isIncoming() ) {
                    // todo: alarm=2 for private messages
                    alarm = 1;
                }
            }
            chatPanes.addLineToPublic(sb.toString());
            if( alarm > 0 )
                chatPanes.notify(null, alarm > 1);
        }
    }

    public void receivedPrivateMessages( boolean replace, List<ChatMessage> messages ) {
        if( replace )
            chatPanes.closePrivateChatPanes();
        for( ChatMessage msg : messages ) {
            StringBuilder sb = new StringBuilder();
            formatMessage(sb, msg);
            chatPanes.addLineToChatPane(msg.isIncoming() ? msg.getAuthor() : msg.getRecipient(), sb.toString());
            if( msg.isIncoming() )
                chatPanes.notify(msg.getAuthor(), true);
        }
    }
}
