// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.infomode;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.data.SystemOfMeasurement;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.OpenBrowser;
import org.openstreetmap.josm.tools.date.DateUtils;

class InfoPanel extends JPanel {

    private Collection<GpxTrack> tracks;
    private GpxTrack trk;

    private JLabel label1 = new JLabel();
    private JLabel label2 = new JLabel();
    private JLabel label3 = new JLabel();
    private JLabel label4 = new JLabel();
    private JLabel label5 = new JLabel();
    private JLabel label6 = new JLabel();
    private JButton but1 = new JButton(tr("Delete this"));
    private JButton but2 = new JButton(tr("Delete this&older"));

    InfoPanel() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(label1, GBC.eol().insets(10, 0, 0, 0));
        add(label2, GBC.eol().insets(10, 0, 0, 0));
        add(label3, GBC.eol().insets(10, 0, 0, 0));
        add(label4, GBC.eol().insets(10, 0, 0, 0));
        add(label5, GBC.eol().insets(10, 0, 0, 0));
        add(label6, GBC.eol().insets(10, 0, 0, 0));
        add(but1, GBC.std().insets(10, 5, 0, 0));
        add(but2, GBC.eop().insets(10, 5, 0, 0));
        // lightweight hyperlink
        label6.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String s = label6.getText();
                OpenBrowser.displayUrl(s.substring(9, s.length()-11));
            }
        });
        but1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tracks != null) tracks.remove(trk);
                MainApplication.getMap().mapView.repaint();
            }
        });
        but2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            if (tracks == null) return;
            Set<GpxTrack> toRemove = new HashSet<>();

            double tm = -1;
            for (GpxTrackSegment seg : trk.getSegments()) {
                    for (WayPoint S : seg.getWayPoints()) {
                        if (S.getTime() > tm) {
                            tm = S.getTime();
                        }
                    }
                }

            for (GpxTrack track : tracks) {
                boolean f = true;
                sg: for (GpxTrackSegment seg : track.getSegments()) {
                    for (WayPoint S : seg.getWayPoints()) {
                        if (S.getTime() > tm) {
                            f = false;
                            break sg;
                        }
                    }
                }
                if (f) toRemove.add(track);
            }
            tracks.removeAll(toRemove);
            MainApplication.getMap().mapView.repaint();
            }
        });
    }

    void setData(WayPoint wp, GpxTrack trk, double vel, Collection<GpxTrack> tracks) {
        this.tracks = tracks;
        this.trk = trk;
        if (!wp.hasDate()) {
            label1.setText(tr("No timestamp"));
            but2.setVisible(false);
        } else {
            label1.setText(DateUtils.formatDateTime(wp.getDate(), DateFormat.DEFAULT, DateFormat.DEFAULT));
            but2.setVisible(true);
        }
        if (vel > 0) {
            SystemOfMeasurement som = SystemOfMeasurement.getSystemOfMeasurement();
            label2.setText(String.format("%.1f "+som.speedName, vel * som.speedValue));
        } else {
            label2.setText(null);
        }
        String s = (String) trk.getAttributes().get("name");
        if (s != null)
            label3.setText(tr("Track name: ")+s);
        else label3.setText(null);
        s = (String) trk.getAttributes().get("desc");
        label4.setText(s);
        s = (String) wp.attr.get("ele");
        String s1 = "";
        if (s != null) {
            try {
                s1 = String.format("H=%3.1f   ", Double.parseDouble(s));
            } catch (NumberFormatException e) {
                Logging.warn(e);
            }
        }
        s1 = s1+"L="+(int) trk.length();
        label5.setText(s1);
        if (trk.getAttributes().containsKey("url")) {
           label6.setText(String.format("<html><u>%s</u></html>", trk.getAttributes().get("url").toString()));
        } else label6.setText(null);
    }

}
