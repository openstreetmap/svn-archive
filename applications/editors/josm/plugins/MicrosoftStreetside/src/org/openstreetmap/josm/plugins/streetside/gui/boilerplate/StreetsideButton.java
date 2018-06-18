// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui.boilerplate;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;

import org.openstreetmap.josm.plugins.streetside.utils.StreetsideColorScheme;


public class StreetsideButton extends JButton {

  private static final long serialVersionUID = -3060978712233067368L;

  public StreetsideButton(final Action action) {
    this(action, false);
  }

  public StreetsideButton(final Action action, boolean slim) {
    super(action);
    setForeground(Color.WHITE);
    setBorder(slim ? BorderFactory.createEmptyBorder(3, 4, 3, 4) : BorderFactory.createEmptyBorder(7, 10, 7, 10));
  }

  @Override
  protected void paintComponent(final Graphics g) {
    if (!isEnabled()) {
      g.setColor(StreetsideColorScheme.TOOLBAR_DARK_GREY);
    } else if (getModel().isPressed()) {
      g.setColor(StreetsideColorScheme.STREETSIDE_BLUE.darker().darker());
    } else if (getModel().isRollover()) {
      g.setColor(StreetsideColorScheme.STREETSIDE_BLUE.darker());
    } else {
      g.setColor(StreetsideColorScheme.STREETSIDE_BLUE);
    }
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
    super.paintComponent(g);
  }

  @Override
  public boolean isContentAreaFilled() {
    return false;
  }
}
