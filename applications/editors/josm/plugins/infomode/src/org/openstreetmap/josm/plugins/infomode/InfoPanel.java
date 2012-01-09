package org.openstreetmap.josm.plugins.infomode;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import org.openstreetmap.josm.data.gpx.GpxTrackSegment;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.data.gpx.WayPoint;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.OpenBrowser;

import static org.openstreetmap.josm.tools.I18n.tr;

class InfoPanel extends JPanel {
    
    private Collection<GpxTrack> tracks;
    private GpxTrack trk;
    private DateFormat df;

    private JLabel label1=new JLabel();
    private JLabel label2=new JLabel();
    private JLabel label3=new JLabel();
    private JLabel label4=new JLabel();
    private JLabel label5=new JLabel();
    private JLabel label6=new JLabel();
    private JButton but1 = new JButton(tr("Delete this"));
    private JButton but2 = new JButton(tr("Delete this&older"));
    
    public InfoPanel() {
        super(new GridBagLayout());
        df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT, Locale.getDefault()); 
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(label1, GBC.eol().insets(10,0,0,0));
        add(label2, GBC.eol().insets(10,0,0,0));
        add(label3, GBC.eol().insets(10,0,0,0));
        add(label4, GBC.eol().insets(10,0,0,0));
        add(label5, GBC.eol().insets(10,0,0,0));
        add(label6, GBC.eol().insets(10,0,0,0));
        add(but1, GBC.std().insets(10,5,0,0));
        add(but2, GBC.eop().insets(10,5,0,0));
        // lightweight hyperlink
        label6.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) { OpenBrowser.displayUrl(label6.getText());  }
            public void mousePressed(MouseEvent e) { }
            public void mouseReleased(MouseEvent e) { }
            public void mouseEntered(MouseEvent e) { }
            public void mouseExited(MouseEvent e) { }
        });
        but1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tracks!=null) tracks.remove(trk);
                Main.map.mapView.repaint();
            }
        });
        but2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            if (tracks==null) return;
            Set<GpxTrack> toRemove = new HashSet<GpxTrack>();
            
            double tm=-1;
            for (GpxTrackSegment seg : trk.getSegments()) {
                    for (WayPoint S : seg.getWayPoints()) {
                        if (S.time>tm) {tm=S.time;}
                    }
                }
            
            for (GpxTrack track : tracks) {
                boolean f=true;
                sg: for (GpxTrackSegment seg : track.getSegments()) {
                    for (WayPoint S : seg.getWayPoints()) {
                        if (S.time>tm) {f=false; break sg;}
                    }
                }
                if (f) toRemove.add(track);
            }
            tracks.removeAll(toRemove);
            Main.map.mapView.repaint();
            }
        });
        

    }

    void setData(WayPoint wp, GpxTrack trk, double vel, Collection<GpxTrack> tracks) {
        this.tracks=tracks;
        this.trk=trk;
        if (wp.time==0.0) { label1.setText(tr("No timestamp"));
            but2.setVisible(false);
        } else {
            label1.setText(df.format(wp.getTime()));
            but2.setVisible(true);
        }
        if (vel>0) label2.setText(String.format("%.1f "+tr("km/h"), vel));
              else label2.setText(null);
        String s = (String) trk.getAttributes().get("name");
        if (s!=null) label3.setText(tr("Track name: ")+s); 
                else label3.setText(null);
        s = (String) trk.getAttributes().get("desc");
        label4.setText(s);
        s = (String) wp.attr.get("ele");
        String s1="";
        try {s1 = String.format("H=%3.1f   ", Double.parseDouble(s));} catch (Exception e) { }
        s1=s1+"L="+(int)trk.length();
        label5.setText(s1);
        if (trk.getAttributes().containsKey("url")) {
           label6.setText(trk.getAttributes().get("url").toString());
        } else label6.setText(null);
    }
    
}
