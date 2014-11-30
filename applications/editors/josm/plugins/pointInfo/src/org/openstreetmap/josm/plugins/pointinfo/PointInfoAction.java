/**
 *  PointInfo - plugin for JOSM
 *  Marian Kyral
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openstreetmap.josm.plugins.pointinfo;

import static org.openstreetmap.josm.tools.I18n.tr;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
// import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.SwingConstants;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ImageIcon;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.OpenBrowser;
import org.xml.sax.SAXException;

class PointInfoAction extends MapMode implements MouseListener {

    private static final long serialVersionUID = 1L;


    protected boolean cancel;
    protected ruianModule mRuian = new ruianModule();

    private String htmlText = "";
    private String coordinatesText = "";

    public PointInfoAction(MapFrame mapFrame) {
        super(tr("Point info"), "info-sml", tr("Point info."), Shortcut.registerShortcut("tools:pointInfo", tr("Tool: {0}", tr("Point info")), KeyEvent.VK_X, Shortcut.CTRL_SHIFT), mapFrame, getCursor());
    }

    @Override
    public void enterMode() {
        if (!isEnabled()) {
            return;
        }
        super.enterMode();
        Main.map.mapView.setCursor(getCursor());
        Main.map.mapView.addMouseListener(this);

    }

    @Override
    public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
    }

    private static Cursor getCursor() {
        return ImageProvider.getCursor("crosshair", "info-sml");
    }

    protected void infoAsync(Point clickPoint) {
        cancel = false;
        /**
         * Positional data
         */
        final LatLon pos = Main.map.mapView.getLatLon(clickPoint.x, clickPoint.y);

        try {
            PleaseWaitRunnable infoTask = new PleaseWaitRunnable(tr("Connecting server")) {

                @Override
                protected void realRun() throws SAXException {
                    infoSync(pos, progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, true));
                }

                @Override
                protected void finish() {
                  if (htmlText.length() > 0) {

                    // Show result
                    System.out.println("htmlText: " + htmlText);
                    JEditorPane msgLabel = new JEditorPane("text/html", htmlText);
                    msgLabel.setEditable(false);
                    msgLabel.setOpaque(false);
                    msgLabel.addHyperlinkListener(new HyperlinkListener() {
                      public void hyperlinkUpdate(HyperlinkEvent hle) {
                        if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                          if (hle.getURL() == null || hle.getURL().toString().isEmpty()) {
                            return;
                            }
                          System.out.println("URL: "+ hle.getURL());
                          if (! hle.getURL().toString().startsWith("http")) {
                            mRuian.performAction(hle.getURL().toString());
                          } else {
                            String ret = OpenBrowser.displayUrl(hle.getURL().toString());
                            if (ret != null) {
                              PointInfoUtils.showNotification(ret, "error");
                            }
                          }
                        }
                      }
                    });
                    JScrollPane scrollPane = new JScrollPane(msgLabel);
                    Object[] objects = {scrollPane};
                    final ImageIcon icon = new ImageIcon(getClass().getResource("/images/dialogs/info-sml.png"));
                    JOptionPane.showMessageDialog(
                      null, objects, tr("PointInfo") + " " + coordinatesText, JOptionPane.PLAIN_MESSAGE,icon);
                  }
                }

                @Override
                protected void cancel() {
                    PointInfoAction.this.cancel();
                }
            };
            Thread executeInfoThread = new Thread(infoTask);
            executeInfoThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void infoSync(LatLon pos, ProgressMonitor progressMonitor) {

        progressMonitor.beginTask(null, 3);
        try {
              mRuian.prepareData(pos);
              htmlText = mRuian.getHtml();
              coordinatesText = PointInfoUtils.formatCoordinates(pos.lat(), pos.lon());

        } finally {
            progressMonitor.finishTask();
        }
        progressMonitor.invalidate();
        if (htmlText.length() == 0) {
          PointInfoUtils.showNotification(tr("Data not available.")+ "\n(" + pos.toDisplayString() + ")", "warning");
          return;
        }
    }

    public void cancel() {
        cancel = true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!Main.map.mapView.isActiveLayerDrawable()) {
            return;
        }
        requestFocusInMapView();
        updateKeyModifiers(e);
        if (e.getButton() == MouseEvent.BUTTON1) {
          infoAsync(e.getPoint());
        }
    }

    @Override
    protected void updateKeyModifiers(MouseEvent e) {
        ctrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
        alt = (e.getModifiers() & (ActionEvent.ALT_MASK | InputEvent.ALT_GRAPH_MASK)) != 0;
        shift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}

