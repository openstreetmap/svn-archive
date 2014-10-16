package org.openstreetmap.josm.plugins.trustosm.gui.dialogs;

import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.ImageProvider;

public class JCollapsiblePanel extends JPanel {

    private boolean expanded;
    JPanel contentPanel_;
    HeaderPanel headerPanel_;

    private class HeaderPanel extends JPanel {
        JLabel title;

        public HeaderPanel(String text) {

            setLayout(new GridBagLayout());
            title = new JLabel(text,ImageProvider.get("misc", "minimized"),SwingConstants.LEADING);
            add(title,GBC.eol());


            addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            expanded = !expanded;

                            if (contentPanel_.isShowing()) {
                                contentPanel_.setVisible(false);
                                title.setIcon(ImageProvider.get("misc", "minimized"));
                            }
                            else {
                                contentPanel_.setVisible(true);
                                title.setIcon(ImageProvider.get("misc", "normal"));
                            }
                            validate();

                            headerPanel_.repaint();
                        }
                    }
            );

        }

    }

    public JCollapsiblePanel(String text, JPanel panel) {
        super(new GridBagLayout());
        expanded = false;
        headerPanel_ = new HeaderPanel(text);
        contentPanel_ = panel;
        add(headerPanel_, GBC.eol());
        add(contentPanel_, GBC.eol());
        contentPanel_.setVisible(false);
    }

}