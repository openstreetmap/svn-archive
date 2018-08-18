// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.OpenBrowser;
import org.openstreetmap.josm.tools.Shortcut;
import org.xml.sax.SAXException;

class PointInfoAction extends MapMode implements MouseListener {

    private static final long serialVersionUID = 1L;

    protected boolean cancel;
    protected AbstractPointInfoModule module;

    private String htmlText = "";
    private String coordinatesText = "";

    PointInfoAction() {
        super(tr("Point info"), "info-sml", tr("Point info."),
                Shortcut.registerShortcut("tools:pointInfo", tr("Tool: {0}", tr("Point info")), KeyEvent.VK_X, Shortcut.CTRL_SHIFT),
                getCursor());
    }

    @Override
    public void enterMode() {
        if (!isEnabled()) {
            return;
        }
        super.enterMode();
        MainApplication.getMap().mapView.setCursor(getCursor());
        MainApplication.getMap().mapView.addMouseListener(this);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        MainApplication.getMap().mapView.removeMouseListener(this);
    }

    private static Cursor getCursor() {
        return ImageProvider.getCursor("crosshair", "info-sml");
    }

    protected void infoAsync(Point clickPoint) {
        cancel = false;
        /**
         * Positional data
         */
        final LatLon pos = MainApplication.getMap().mapView.getLatLon(clickPoint.x, clickPoint.y);

        try {
            module = PointInfoPlugin.getModule(pos);
            PleaseWaitRunnable infoTask = new PleaseWaitRunnable(tr("Connecting server")) {
                @Override
                protected void realRun() throws SAXException {
                    infoSync(pos, progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, true));
                }

                @Override
                protected void finish() {
                }

                @Override
                protected void afterFinish() {
                    if (htmlText.length() > 0) {
                        // Show result
                        JEditorPane msgLabel = new JEditorPane("text/html", htmlText);
                        msgLabel.setEditable(false);
                        msgLabel.setOpaque(false);
                        msgLabel.addHyperlinkListener(hle -> {
                            if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                                Logging.info(hle.getURL().toString());
                                if (hle.getURL() == null || hle.getURL().toString().isEmpty()) {
                                    return;
                                }
                                if (!hle.getURL().toString().startsWith("http")) {
                                    module.performAction(hle.getURL().toString());
                                } else {
                                    String ret = OpenBrowser.displayUrl(hle.getURL().toString());
                                    if (ret != null) {
                                        PointInfoUtils.showNotification(ret, "error");
                                    }
                                }
                            }
                        });
                        JScrollPane scrollPane = new JScrollPane(msgLabel);
                        Object[] objects = {scrollPane};
                        final ImageIcon icon = new ImageIcon(getClass().getResource("/images/dialogs/info-sml.png"));
                        JOptionPane.showMessageDialog(
                                null, objects, tr("PointInfo") + " " + coordinatesText, JOptionPane.PLAIN_MESSAGE, icon);
                    }
                }

                @Override
                protected void cancel() {
                    PointInfoAction.this.cancel();
                }
            };
            new Thread(infoTask).start();
        } catch (Exception e) {
            Logging.error(e);
        }
    }

    private void infoSync(LatLon pos, ProgressMonitor progressMonitor) {

        progressMonitor.beginTask(null, 3);
        try {
            module.prepareData(pos);
            htmlText = module.getHtml();
            coordinatesText = PointInfoUtils.formatCoordinates(pos.lat(), pos.lon());

        } finally {
            progressMonitor.finishTask();
        }
        progressMonitor.invalidate();
        if (htmlText.length() == 0) {
            GuiHelper.runInEDTAndWait(
                    () -> PointInfoUtils.showNotification(tr("Data not available.")+ "\n(" + pos.toDisplayString() + ")", "warning"));
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
        if (!MainApplication.getMap().mapView.isActiveLayerDrawable()) {
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

