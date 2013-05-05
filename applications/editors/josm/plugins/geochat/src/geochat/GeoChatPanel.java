package geochat;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
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
import org.openstreetmap.josm.tools.ImageProvider;

/**
 *
 * @author zverik
 */
public class GeoChatPanel extends ToggleDialog implements ChatServerConnectionListener, MapViewPaintable {
    private static final String PUBLIC_PANE = "Public Pane";

    private JTextField input;
    private JTabbedPane tabs;
    private JComponent noData;
    private JPanel loginPanel;
    private JPanel gcPanel;
    private ChatServerConnection connection;
    private Map<String, LatLon> users;
    private Map<String, ChatLogEntry> chatPanes;
    
    public GeoChatPanel() {
        super(tr("GeoChat"), "geochat", tr("Open GeoChat panel"), null, 200, true);

        noData = new JLabel(tr("Zoom in to see messages"), SwingConstants.CENTER);

        chatPanes = new HashMap<String, ChatLogEntry>();
        tabs = new JTabbedPane();
        createChatPane(null);

        tabs.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed( MouseEvent e ) { check(e); }
            @Override public void mouseReleased( MouseEvent e ) { check(e); }

            private void check( MouseEvent e ) {
                if( e.isPopupTrigger() ) {
                    createPopupMenu().show(tabs, e.getX(), e.getY());
                }
            }
        });

        input = new JPanelTextField() {
            @Override
            protected void processEnter( String text ) {
                connection.postMessage(text, getRecipient());
            }

            @Override
            protected String autoComplete( String word ) {
                return word;
            }
        };

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
        nameField.setText(userName);

        JButton loginButton = new JButton(tr("Login"));
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                connection.login(nameField.getText());
            }
        });
        nameField.setPreferredSize(new Dimension(nameField.getPreferredSize().width, loginButton.getPreferredSize().height));

//        loginPanel = new JPanel(new BorderLayout());
//        loginPanel.add(nameField, BorderLayout.CENTER);
//        loginPanel.add(loginButton, BorderLayout.EAST);
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.add(nameField, GBC.std().fill(GridBagConstraints.HORIZONTAL).insets(15, 0, 5, 0));
        loginPanel.add(loginButton, GBC.std().fill(GridBagConstraints.NONE).insets(0, 0, 15, 0));

        gcPanel = new JPanel(new BorderLayout());
        gcPanel.add(loginPanel, BorderLayout.CENTER);
        createLayout(gcPanel, false, null);

        users = new TreeMap<String, LatLon>();
        // Start threads
        connection = ChatServerConnection.getInstance();
        connection.addListener(this);
        connection.checkLogin();
    }
    
    private JPopupMenu createPopupMenu() {
        JMenu userMenu = new JMenu(tr("Private chat"));
        for( String user : users.keySet() ) {
            if( !chatPanes.containsKey(user) )
                userMenu.add(new PrivateChatAction(user));
        }

        JPopupMenu menu = new JPopupMenu();
        menu.add(new JCheckBoxMenuItem(new ToggleUserLayerAction()));
        if( userMenu.getComponentCount() > 0 )
            menu.add(userMenu);
        if( getRecipient() != null )
            menu.add(new CloseTabAction());
        menu.add(new ClearPaneAction());
        menu.add(new LogoutAction());
        return menu;
    }

    private void addLineToChatPane( String userName, String line ) {
        if( !chatPanes.containsKey(userName) )
            createChatPane(userName);
        if( !line.startsWith("\n") )
            line = "\n" + line;
        Document doc = chatPanes.get(userName).pane.getDocument();
        try {
            doc.insertString(doc.getLength(), line, null);
        } catch( BadLocationException ex ) {
            // whatever
        }
    }

    private void addLineToPublic( String line ) {
        addLineToChatPane(PUBLIC_PANE, line);
    }

    private void clearPublicChatPane() {
        chatPanes.get(PUBLIC_PANE).pane.setText("");
        showNearbyUsers();
    }

    private void clearChatPane( String userName) {
        if( userName == null || userName.equals(PUBLIC_PANE) )
            clearPublicChatPane();
        else
            chatPanes.get(userName).pane.setText("");
    }

    private ChatLogEntry createChatPane( String userName ) {
        JTextPane chatPane = new JTextPane();
        chatPane.setEditable(false);
        Font font = chatPane.getFont();
        chatPane.setFont(font.deriveFont(font.getSize2D() - 2));
        DefaultCaret caret = (DefaultCaret)chatPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(chatPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        ChatLogEntry entry = new ChatLogEntry();
        entry.pane = chatPane;
        entry.component = scrollPane;
        entry.notify = false;
        entry.userName = userName;
        entry.isPublic = userName == null;
        chatPanes.put(userName == null ? PUBLIC_PANE : userName, entry);

        tabs.addTab(userName == null ? tr("Public") : userName, scrollPane);
        tabs.setSelectedComponent(scrollPane);
        return entry;
    }

    /**
     * Returns key in chatPanes hash map for the currently active
     * chat pane, or null in case of an error.
     */
    private String getActiveChatPane() {
        Component c = tabs.getSelectedComponent();
        if( c == null )
            return null;
        for( String user : chatPanes.keySet() )
            if( c.equals(chatPanes.get(user).component) )
                return user;
        return null;
    }

    private String getRecipient() {
        String user = getActiveChatPane();
        return user == null || user.equals(PUBLIC_PANE) ? null : user;
    }

    private void closeChatPane( String user ) {
        if( user == null || user.equals(PUBLIC_PANE) || !chatPanes.containsKey(user) )
            return;
        tabs.remove(chatPanes.get(user).component);
        chatPanes.remove(user);
    }

    private void closePrivateChatPanes() {
        List<String> entries = new ArrayList<String>(chatPanes.keySet());
        for( String user : entries )
            if( !user.equals(PUBLIC_PANE) )
                closeChatPane(user);
    }

    private String cachedTitle = "";
    private int cachedAlarm = 0;

    @Override
    public void setTitle( String title ) {
        setTitle(title, -1);
    }

    private void setTitleAlarm( int alarmLevel ) {
        setTitle(null, alarmLevel);
    }

    private void setTitle( String title, int alarmLevel ) {
        if( title != null )
            cachedTitle = title;
        if( alarmLevel >= 0 )
            cachedAlarm = alarmLevel;
        String alarm = cachedAlarm <= 0 ? "" : cachedAlarm == 1 ? "* " : "[!] ";
        super.setTitle(alarm + cachedTitle);
    }

    public void paint( Graphics2D g, MapView mv, Bounds bbox ) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setColor(Color.yellow);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));

        int zoom = ChatServerConnection.getCurrentZoom();
        int radius = Math.max(zoom, 1) * 10;
        if( zoom < 14 )
            radius /= 2;

        Font font = g2d.getFont().deriveFont(Math.min(zoom * 2, 8));
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
        tabs.setComponentAt(0, active ? chatPanes.get(PUBLIC_PANE).component : noData);
        repaint();
    }

    public void updateUsers( Map<String, LatLon> newUsers ) {
        for( String uname : this.users.keySet() ) {
            if( !newUsers.containsKey(uname) )
                addLineToPublic(tr("User {0} has left", uname));
        }
        for( String uname : newUsers.keySet() ) {
            if( !this.users.containsKey(uname) )
                addLineToPublic(tr("User {0} is mapping nearby", uname));
        }
        setTitle(trn("GeoChat ({0} user)", "GeoChat ({0} users)", newUsers.size(), newUsers.size()));
        // todo: update users location
        this.users = newUsers;
    }

    private void showNearbyUsers() {
        if( !users.isEmpty() ) {
            StringBuilder sb = new StringBuilder(tr("Users mapping nearby:"));
            boolean first = true;
            for( String user : users.keySet() ) {
                sb.append(first ? " " : ", ");
                sb.append(user);
            }
            addLineToPublic(sb.toString());
        }
    }

    private final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    private void formatMessage( StringBuilder sb, ChatMessage msg ) {
        sb.append("\n");
        sb.append('[').append(TIME_FORMAT.format(msg.getTime())).append("] ");
        sb.append(msg.getAuthor()).append(": ").append(msg.getMessage());
    }

    public void receivedMessages( boolean replace, List<ChatMessage> messages ) {
        if( replace )
            clearPublicChatPane();
        if( !messages.isEmpty() ) {
            StringBuilder sb = new StringBuilder();
            for( ChatMessage msg : messages )
                formatMessage(sb, msg);
            addLineToPublic(sb.toString());
        }
    }

    public void receivedPrivateMessages( boolean replace, List<ChatMessage> messages ) {
        if( replace )
            closePrivateChatPanes();
        for( ChatMessage msg : messages ) {
            StringBuilder sb = new StringBuilder();
            formatMessage(sb, msg);
            addLineToChatPane(msg.getRecipient() != null ? msg.getRecipient() : msg.getAuthor(), sb.toString());
        }
    }

    private class JPanelTextField extends JTextField {
        @Override
        protected void processKeyEvent( KeyEvent e ) {
            if( e.getID() == KeyEvent.KEY_PRESSED ) {
                int code = e.getKeyCode();
                if( code == KeyEvent.VK_ENTER ) {
                    String text = input.getText();
                    if( text.length() > 0 ) {
                        processEnter(text);
                        input.setText("");
                    }
                } else if( code == KeyEvent.VK_TAB ) {
                    autoComplete(""); // todo
                } else if( code == KeyEvent.VK_ESCAPE ) {
                    if( Main.map != null && Main.map.mapView != null )
                        Main.map.mapView.requestFocus();
                }
                // Do not pass other events to JOSM
                if( code != KeyEvent.VK_LEFT && code != KeyEvent.VK_HOME && code != KeyEvent.VK_RIGHT
                        && code != KeyEvent.VK_END && code != KeyEvent.VK_BACK_SPACE && code != KeyEvent.VK_DELETE )
                    e.consume();
            }
            super.processKeyEvent(e);
        }

        protected void processEnter( String text ) { }

        protected String autoComplete( String word ) { return word; }
    }

    private class ChatLogEntry {
        public String userName;
        public boolean isPublic;
        public JTextPane pane;
        public JScrollPane component;
        public boolean notify;
    }

    private class PrivateChatAction extends AbstractAction {
        private String userName;

        public PrivateChatAction( String userName ) {
            super(userName);
            this.userName = userName;
        }

        public void actionPerformed( ActionEvent e ) {
            if( !chatPanes.containsKey(userName) ) {
                ChatLogEntry entry = createChatPane(userName);
            }
        }
    }

    private class CloseTabAction extends AbstractAction {
        public CloseTabAction() {
            super(tr("Close tab"));
            putValue(SMALL_ICON, ImageProvider.get("help"));
        }

        public void actionPerformed( ActionEvent e ) {
            String pane = getActiveChatPane();
            if( pane != null && !pane.equals(PUBLIC_PANE) )
                closeChatPane(pane);
        }
    }

    private class LogoutAction extends AbstractAction {
        public LogoutAction() {
            super(tr("Logout"));
            putValue(SMALL_ICON, ImageProvider.get("help"));
        }

        public void actionPerformed( ActionEvent e ) {
            connection.logout();
        }
    }

    private class ClearPaneAction extends AbstractAction {
        public ClearPaneAction() {
            super(tr("Clear log"));
            putValue(SMALL_ICON, ImageProvider.get("help"));
        }

        public void actionPerformed( ActionEvent e ) {
            clearChatPane(getActiveChatPane());
        }
    }

    private class ToggleUserLayerAction extends AbstractAction {
        public ToggleUserLayerAction() {
            super(tr("Show users on map"));
            putValue(SMALL_ICON, ImageProvider.get("help"));
        }

        public void actionPerformed( ActionEvent e ) {
            if( Main.map == null || Main.map.mapView == null )
                return;
            boolean wasAdded = Main.map.mapView.addTemporaryLayer(GeoChatPanel.this);
            if( !wasAdded )
                Main.map.mapView.removeTemporaryLayer(GeoChatPanel.this);
            Main.map.mapView.repaint();
            if( e.getSource() != null )
                System.out.println("toggle source: " + e.getSource().getClass().getName());
            if( e.getSource() instanceof JCheckBoxMenuItem )
                ((JCheckBoxMenuItem)e.getSource()).setSelected(wasAdded);
        }
    }
}
