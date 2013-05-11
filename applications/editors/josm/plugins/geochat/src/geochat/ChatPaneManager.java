// License: WTFPL
package geochat;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 *
 * @author zverik
 */
class ChatPaneManager {
    private static final String PUBLIC_PANE = "Public Pane";

    private GeoChatPanel panel;
    private JTabbedPane tabs;
    private Map<String, ChatPane> chatPanes;
    private boolean collapsed;

    public ChatPaneManager( GeoChatPanel panel, JTabbedPane tabs ) {
        this.panel = panel;
        this.tabs = tabs;
        this.collapsed = panel.isDialogInCollapsedView();
        chatPanes = new HashMap<String, ChatPane>();
        createChatPane(null);
        tabs.addChangeListener(new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                updateActiveTabStatus();
            }
        });
    }

    public void setCollapsed( boolean collapsed ) {
        this.collapsed = collapsed;
        updateActiveTabStatus();
    }

    public boolean hasUser( String user ) {
        return chatPanes.containsKey(user == null ? PUBLIC_PANE : user);
    }

    public Component getPublicChatComponent() {
        return chatPanes.get(PUBLIC_PANE).component;
    }

    public int getNotifyLevel() {
        int alarm = 0;
        for( ChatPane entry : chatPanes.values() ) {
            if( entry.notify > alarm )
                alarm = entry.notify;
        }
        return alarm;
    }

    public void updateActiveTabStatus() {
        if( tabs.getSelectedIndex() >= 0 )
            ((ChatTabTitleComponent)tabs.getTabComponentAt(tabs.getSelectedIndex())).updateAlarm();
    }

    public void notify( String user, int alarmLevel ) {
        if( alarmLevel <= 0 || !hasUser(user) )
            return;
        ChatPane entry = chatPanes.get(user == null ? PUBLIC_PANE : user);
        entry.notify = alarmLevel;
        int idx = tabs.indexOfComponent(entry.component);
        if( idx >= 0 )
            ((ChatTabTitleComponent)tabs.getTabComponentAt(idx)).updateAlarm();
    }

    public static int MESSAGE_TYPE_DEFAULT = 0;
    public static int MESSAGE_TYPE_INFORMATION = 1;
    public static int MESSAGE_TYPE_ATTENTION = 2;
    private static Color COLOR_ATTENTION = new Color(0, 0, 192);

    private void addLineToChatPane( String userName, String line, int messageType ) {
        if( line.length() == 0 )
            return;
        if( !chatPanes.containsKey(userName) )
            createChatPane(userName);
        if( !line.startsWith("\n") )
            line = "\n" + line;
        Document doc = chatPanes.get(userName).pane.getDocument();
        try {
            SimpleAttributeSet attrs = null;
            if( messageType != MESSAGE_TYPE_DEFAULT ) {
                attrs = new SimpleAttributeSet();
                if( messageType == MESSAGE_TYPE_INFORMATION )
                    StyleConstants.setItalic(attrs, true);
                else if( messageType == MESSAGE_TYPE_ATTENTION )
                    StyleConstants.setForeground(attrs, COLOR_ATTENTION);
            }
            doc.insertString(doc.getLength(), line, attrs);
        } catch( BadLocationException ex ) {
            // whatever
        }
        chatPanes.get(userName).pane.setCaretPosition(doc.getLength());
    }

    public void addLineToChatPane( String userName, String line ) {
        addLineToChatPane(userName, line, MESSAGE_TYPE_DEFAULT);
    }

    public void addLineToPublic( String line ) {
        addLineToChatPane(PUBLIC_PANE, line);
    }

    public void addLineToPublic( String line, int messageType ) {
        addLineToChatPane(PUBLIC_PANE, line, messageType);
    }

    public void clearPublicChatPane() {
        chatPanes.get(PUBLIC_PANE).pane.setText("");
    }

    public void clearChatPane( String userName) {
        if( userName == null || userName.equals(PUBLIC_PANE) )
            clearPublicChatPane();
        else
            chatPanes.get(userName).pane.setText("");
    }

    public void clearActiveChatPane() {
        clearChatPane(getActiveChatPane());
    }

    public ChatPane createChatPane( String userName ) {
        JTextPane chatPane = new JTextPane();
        chatPane.setEditable(false);
        Font font = chatPane.getFont();
        float size = -1.0f; // Main.pref.getInteger("geochat.fontsize", -1); <- we don't need this
        if( size < 8 )
            size += font.getSize2D();
        chatPane.setFont(font.deriveFont(size));
//        DefaultCaret caret = (DefaultCaret)chatPane.getCaret(); // does not work
//        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(chatPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatPane.addMouseListener(new GeoChatPopupAdapter(panel));

        ChatPane entry = new ChatPane();
        entry.pane = chatPane;
        entry.component = scrollPane;
        entry.notify = 0;
        entry.userName = userName;
        entry.isPublic = userName == null;
        chatPanes.put(userName == null ? PUBLIC_PANE : userName, entry);

        tabs.addTab(null, scrollPane);
        tabs.setTabComponentAt(tabs.getTabCount() - 1, new ChatTabTitleComponent(entry));
        tabs.setSelectedComponent(scrollPane);
        return entry;
    }

    /**
     * Returns key in chatPanes hash map for the currently active
     * chat pane, or null in case of an error.
     */
    public String getActiveChatPane() {
        Component c = tabs.getSelectedComponent();
        if( c == null )
            return null;
        for( String user : chatPanes.keySet() )
            if( c.equals(chatPanes.get(user).component) )
                return user;
        return null;
    }

    public String getRecipient() {
        String user = getActiveChatPane();
        return user == null || user.equals(PUBLIC_PANE) ? null : user;
    }

    public void closeChatPane( String user ) {
        if( user == null || user.equals(PUBLIC_PANE) || !chatPanes.containsKey(user) )
            return;
        tabs.remove(chatPanes.get(user).component);
        chatPanes.remove(user);
    }

    public void closeSelectedPrivatePane() {
        String pane = getRecipient();
        if( pane != null )
            closeChatPane(pane);
    }

    public void closePrivateChatPanes() {
        List<String> entries = new ArrayList<String>(chatPanes.keySet());
        for( String user : entries )
            if( !user.equals(PUBLIC_PANE) )
                closeChatPane(user);
    }
    
    public boolean hasSelectedText() {
        String user = getActiveChatPane();
        if( user != null ) {
            JTextPane pane = chatPanes.get(user).pane;
            return pane.getSelectedText() != null;
        }
        return false;
    }

    public void copySelectedText() {
        String user = getActiveChatPane();
        if( user != null )
            chatPanes.get(user).pane.copy();
    }
    

    private class ChatTabTitleComponent extends JLabel {
        private ChatPane entry;

        public ChatTabTitleComponent( ChatPane entry ) {
            super(entry.isPublic ? tr("Public") : entry.userName);
            this.entry = entry;
        }

        private Font normalFont;
        private Font boldFont;

        public void updateAlarm() {
            if( normalFont == null ) {
                // prepare cached fonts
                normalFont = getFont().deriveFont(Font.PLAIN);
                boldFont = getFont().deriveFont(Font.BOLD);
            }
            if( entry.notify > 0 && !collapsed && tabs.getSelectedIndex() == tabs.indexOfComponent(entry.component) )
                entry.notify = 0;
            setFont(entry.notify > 0 ? boldFont : normalFont);
            panel.updateTitleAlarm();
        }
    }

    class ChatPane {
        public String userName;
        public boolean isPublic;
        public JTextPane pane;
        public JScrollPane component;
        public int notify;

    }
}
