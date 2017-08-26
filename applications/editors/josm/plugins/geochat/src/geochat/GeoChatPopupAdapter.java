// License: WTFPL. For details, see LICENSE file.
package geochat;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.openstreetmap.josm.gui.MainApplication;

/**
 *
 * @author zverik
 */
class GeoChatPopupAdapter extends MouseAdapter {
    private GeoChatPanel panel;

    GeoChatPopupAdapter(GeoChatPanel panel) {
        this.panel = panel;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        check(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        check(e);
    }

    private void check(MouseEvent e) {
        if (e.isPopupTrigger()) {
            createPopupMenu().show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private JPopupMenu createPopupMenu() {
        JMenu userMenu = new JMenu(tr("Private chat"));
        for (String user : panel.users.keySet()) {
            if (!panel.chatPanes.hasUser(user))
                userMenu.add(new PrivateChatAction(user));
        }

        JPopupMenu menu = new JPopupMenu();
        if (panel.chatPanes.hasSelectedText())
            menu.add(new CopyTextAction());
        menu.add(new JCheckBoxMenuItem(new ToggleUserLayerAction()));
        if (userMenu.getItemCount() > 0)
            menu.add(userMenu);
        if (panel.chatPanes.getRecipient() != null)
            menu.add(new CloseTabAction());
        menu.add(new LogoutAction());
        return menu;
    }

    private class PrivateChatAction extends AbstractAction {
        private String userName;

        PrivateChatAction(String userName) {
            super(userName);
            this.userName = userName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!panel.chatPanes.hasUser(userName)) {
                panel.chatPanes.createChatPane(userName);
            }
        }
    }

    private class CloseTabAction extends AbstractAction {
        CloseTabAction() {
            super(tr("Close tab"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            panel.chatPanes.closeSelectedPrivatePane();
        }
    }

    private class LogoutAction extends AbstractAction {
        LogoutAction() {
            super(tr("Logout"));
            //            putValue(SMALL_ICON, ImageProvider.get("help"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            panel.logout();
        }
    }

    private class ClearPaneAction extends AbstractAction {
        ClearPaneAction() {
            super(tr("Clear log"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            panel.chatPanes.clearActiveChatPane();
        }
    }

    private class ToggleUserLayerAction extends AbstractAction {
        ToggleUserLayerAction() {
            super(tr("Show users on map"));
            putValue(SELECTED_KEY, panel.userLayerActive);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!MainApplication.isDisplayingMapView())
                return;
            boolean wasAdded = MainApplication.getMap().mapView.addTemporaryLayer(panel);
            if (!wasAdded)
                MainApplication.getMap().mapView.removeTemporaryLayer(panel);
            panel.userLayerActive = wasAdded;
            putValue(SELECTED_KEY, panel.userLayerActive);
            MainApplication.getMap().mapView.repaint();
        }
    }

    private class CopyTextAction extends AbstractAction {
        CopyTextAction() {
            super(tr("Copy"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            panel.chatPanes.copySelectedText();
        }
    }
}
