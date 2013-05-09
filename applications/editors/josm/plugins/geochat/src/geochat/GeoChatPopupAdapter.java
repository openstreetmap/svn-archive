package geochat;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import org.openstreetmap.josm.Main;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 *
 * @author zverik
 */
class GeoChatPopupAdapter extends MouseAdapter {
    private GeoChatPanel panel;

    public GeoChatPopupAdapter( GeoChatPanel panel ) {
        this.panel = panel;
    }

    @Override
    public void mousePressed( MouseEvent e ) {
        check(e);
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        check(e);
    }

    private void check( MouseEvent e ) {
        if( e.isPopupTrigger() ) {
            createPopupMenu().show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private JPopupMenu createPopupMenu() {
        JMenu userMenu = new JMenu(tr("Private chat"));
        for( String user : panel.users.keySet() ) {
            if( !panel.chatPanes.hasUser(user) )
                userMenu.add(new PrivateChatAction(user));
        }

        JPopupMenu menu = new JPopupMenu();
        if( panel.chatPanes.hasSelectedText() )
            menu.add(new CopyTextAction());
        menu.add(new JCheckBoxMenuItem(new ToggleUserLayerAction()));
        if( userMenu.getItemCount() > 0 )
            menu.add(userMenu);
        if( panel.chatPanes.getRecipient() != null )
            menu.add(new CloseTabAction());
//        menu.add(new ClearPaneAction());
        menu.add(new LogoutAction());
        return menu;
    }

    private class PrivateChatAction extends AbstractAction {
        private String userName;

        public PrivateChatAction( String userName ) {
            super(userName);
            this.userName = userName;
        }

        public void actionPerformed( ActionEvent e ) {
            if( !panel.chatPanes.hasUser(userName) ) {
                panel.chatPanes.createChatPane(userName);
            }
        }
    }

    private class CloseTabAction extends AbstractAction {
        public CloseTabAction() {
            super(tr("Close tab"));
//            putValue(SMALL_ICON, ImageProvider.get("help"));
        }

        public void actionPerformed( ActionEvent e ) {
            panel.chatPanes.closeSelectedPrivatePane();
        }
    }

    private class LogoutAction extends AbstractAction {
        public LogoutAction() {
            super(tr("Logout"));
//            putValue(SMALL_ICON, ImageProvider.get("help"));
        }

        public void actionPerformed( ActionEvent e ) {
            panel.logout();
        }
    }

    private class ClearPaneAction extends AbstractAction {
        public ClearPaneAction() {
            super(tr("Clear log"));
//            putValue(SMALL_ICON, ImageProvider.get("help"));
        }

        public void actionPerformed( ActionEvent e ) {
            panel.chatPanes.clearActiveChatPane();
        }
    }

    private class ToggleUserLayerAction extends AbstractAction {
        public ToggleUserLayerAction() {
            super(tr("Show users on map"));
//            putValue(SMALL_ICON, ImageProvider.get("help"));
            putValue(SELECTED_KEY, Boolean.valueOf(panel.userLayerActive));
        }

        public void actionPerformed( ActionEvent e ) {
            if( Main.map == null || Main.map.mapView == null )
                return;
            boolean wasAdded = Main.map.mapView.addTemporaryLayer(panel);
            if( !wasAdded )
                Main.map.mapView.removeTemporaryLayer(panel);
            putValue(SELECTED_KEY, Boolean.valueOf(panel.userLayerActive));
            Main.map.mapView.repaint();
        }
    }

    private class CopyTextAction extends AbstractAction {
        public CopyTextAction() {
            super(tr("Copy"));
//            putValue(SMALL_ICON, ImageProvider.get("help"));
        }

        public void actionPerformed( ActionEvent e ) {
            panel.chatPanes.copySelectedText();
        }
    }
}
