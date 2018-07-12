// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.gui.imageinfo;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideUtils;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;

public class WebLinkAction extends AbstractAction {

  private static final long serialVersionUID = -8168227661356480455L;

  final static Logger logger = Logger.getLogger(WebLinkAction.class);

  private URL url;

  public WebLinkAction(final String name, final URL url) {
    super(name, ImageProvider.get("link", ImageSizes.SMALLICON));
    setURL(url);
  }

  /**
   * @param url the url to set
   */
  public final void setURL(URL url) {
    this.url = url;
    setEnabled(url != null);
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    try {
      StreetsideUtils.browse(url);
    } catch (IOException e1) {
      String msg = "Could not open the URL " + url == null ? "‹null›" : url + " in a browser";
      logger.warn(msg, e1);
      new Notification(msg).setIcon(JOptionPane.WARNING_MESSAGE).show();
    }
  }
}