/*
 * UploadDataGui.java
 *
 * Created on August 17, 2008, 6:56 PM
 * Copyright by Subhodip Biswas
 * This program is free software and licensed under GPL.
 */

package org.openstreetmap.josm.plugins.DirectUpload;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.io.GpxWriter;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.tools.Base64;
import org.openstreetmap.josm.tools.GBC;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 *
 * @author  subhodip
 */
public class UploadDataGui extends javax.swing.JFrame {
    private JTextArea OutputDisplay = new JTextArea();
    private JTextField descriptionField = new JTextField();
    private JTextField tagsField = new JTextField();
    private JCheckBox publicCheckbox = new JCheckBox();
    private JButton OkButton = new JButton();

    public static final String API_VERSION = "0.5";
    private static final String BOUNDARY = "----------------------------d10f7aa230e8";
    private static final String LINE_END = "\r\n";

    private String datename = new SimpleDateFormat("yyMMddHHmmss").format(new Date());

    /** Creates new form UploadDataGui */
    public UploadDataGui() {
        setTitle(tr("Upload Traces"));
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        setPreferredSize(new Dimension(350,200));

        // Display Center Screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = getPreferredSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2), screenSize.height / 2 - (labelSize.height / 2));

        OutputDisplay.setBackground(UIManager.getColor("Panel.background"));
        OutputDisplay.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        OutputDisplay.setEditable(false);
        OutputDisplay.setFont(new JLabel().getFont());
        OutputDisplay.setLineWrap(true);
        OutputDisplay.setWrapStyleWord(true);

        JScrollPane jScrollPane1 = new JScrollPane();
        jScrollPane1.setViewportView(OutputDisplay);

        OkButton.setText(tr("Upload GPX track"));
        OkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OkButtonActionPerformed(evt);
            }
        });

        JButton CancelButton = new JButton(tr("Cancel"));
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });

        publicCheckbox.setText(tr("Public"));
        publicCheckbox.setToolTipText(tr("Selected makes your trace public in openstreetmap.org"));

        JLabel descriptionLabel = new JLabel(tr("Description"));
        descriptionField.setToolTipText("Please enter Description about your trace.");

        JLabel tagsLabel = new JLabel(tr("Tags"));
        tagsField.setToolTipText("Please enter tags about your trace.");

        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());

        p.add(OutputDisplay, GBC.eol().fill());

        p.add(tagsLabel, GBC.std().insets(0,10,0,0));
        p.add(tagsField, GBC.eol().fill(GBC.HORIZONTAL));

        p.add(descriptionLabel, GBC.std().insets(0,10,0,0));
        p.add(descriptionField, GBC.eol().fill(GBC.HORIZONTAL));

        p.add(publicCheckbox, GBC.eol());

        p.add(CancelButton, GBC.std());
        p.add(OkButton, GBC.eol().fill(GBC.HORIZONTAL));

        getContentPane().setLayout(new GridBagLayout());
        getContentPane().add(p, GBC.eol().insets(10,10,10,10).fill());

        pack();

        // If no GPX layer is selected, select one for the user if there is only one GPX layer
        if(Main.map != null && Main.map.mapView != null) {
            MapView mv=Main.map.mapView;
            if(!(mv.getActiveLayer() instanceof GpxLayer)) {
                Layer lastLayer=null;
                int layerCount=0;
                for (Layer l : mv.getAllLayers()) {
                    if(l instanceof GpxLayer) {
                        lastLayer = l;
                        layerCount++;
                    }
                }
                if(layerCount == 1) mv.setActiveLayer(lastLayer);
            }

            if(mv.getActiveLayer() instanceof GpxLayer) {
                GpxData data=((GpxLayer)Main.map.mapView.getActiveLayer()).data;
                descriptionField.setText(data.storageFile.getName().replaceAll("[&?/\\\\]"," ").replaceAll("(\\.[^.]*)$",""));
            }
        }

        boolean x=checkForGPXLayer();
    }

    public void upload(String username, String password, String description, String tags, Boolean isPublic, GpxData gpxData) throws IOException {
        if(checkForErrors(username, password, description, gpxData))
            return;
				
				OkButton.setEnabled(false);
				
        description = description.replaceAll("[&?/\\\\]"," ");
        tags = tags.replaceAll("[&?/\\\\.,;]"," ");
        
        Main.pleaseWaitDlg.progress.setValue(0);
        Main.pleaseWaitDlg.setIndeterminate(true); 
        Main.pleaseWaitDlg.currentAction.setText(tr("Connecting..."));
        // We don't support cancellation yet, so do not advertise it
        Main.pleaseWaitDlg.cancel.setEnabled(false);

        try {
            URL url = new URL("http://www.openstreetmap.org/api/" + API_VERSION + "/gpx/create");
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            connect.setConnectTimeout(15000);
            connect.setRequestMethod("POST");
            connect.setDoOutput(true);
            connect.addRequestProperty("Authorization", "Basic " + Base64.encode(username + ":" + password));
            connect.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            connect.addRequestProperty("Connection", "close"); // counterpart of keep-alive
            connect.addRequestProperty("Expect", "");
            connect.connect();
            
            Main.pleaseWaitDlg.currentAction.setText(tr("Uploading GPX track..."));
            DataOutputStream out  = new DataOutputStream(new BufferedOutputStream(connect.getOutputStream()));            
            writeContentDispositionGpxData(out, "file", gpxData);
            writeContentDisposition(out, "description", description);
            writeContentDisposition(out, "tags", (tags != null && tags.length() > 0) ? tags : "");
            writeContentDisposition(out, "public", isPublic ? "1" : "0");
            out.writeBytes("--" + BOUNDARY + "--" + LINE_END);
            out.flush();
            
            String returnMsg = connect.getResponseMessage();
            boolean success = returnMsg.equals("OK");
            OutputDisplay.setText(success ? tr("GPX upload was successful") : returnMsg);

            if (connect.getResponseCode() != 200) {
                if (connect.getHeaderField("Error") != null)
                    returnMsg += "\n" + connect.getHeaderField("Error");
            }
            out.close();
            connect.disconnect();
            
            OkButton.setEnabled(!success);
            
        } catch(UnsupportedEncodingException ignore) {
        } catch (MalformedURLException e) {
            OutputDisplay.setText(tr("Error while uploading"));
            e.printStackTrace();
        }
    }
    
    private boolean checkForErrors(String username, String password, String description, GpxData gpxData) {
        String errors="";
        if(description == null || description.length() == 0)
            errors += tr("No description provided. Please provide some description.");

        if(gpxData == null)
            errors += tr("No GPX layer selected. Cannot upload a trace.");

        if(username == null || username.length()==0)
            errors += tr("No username provided.");

        if(password == null || password.length()==0)
            errors += tr("No password provided.");

        OutputDisplay.setText(errors);
        return errors.length() > 0;
    }

    private boolean checkForGPXLayer() {
        if(Main.map == null || Main.map.mapView == null || Main.map.mapView.getActiveLayer() == null || !(Main.map.mapView.getActiveLayer() instanceof GpxLayer)) {
            OutputDisplay.setText(tr("No GPX layer selected. Cannot upload a trace."));
            return true;
        }
        return false;
    }

    private void OkButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if(checkForGPXLayer()) return;

        PleaseWaitRunnable uploadTask = new PleaseWaitRunnable(tr("Uploading GPX Track")){
            @Override protected void realRun() throws IOException {
                  setAlwaysOnTop(false);
                  upload(Main.pref.get("osm-server.username"),
                         Main.pref.get("osm-server.password"),
                         descriptionField.getText(),
                         tagsField.getText(),
                         publicCheckbox.isSelected(),
                         ((GpxLayer)Main.map.mapView.getActiveLayer()).data
                  );
            }
            @Override protected void finish() {
                setAlwaysOnTop(true);
            }
            @Override protected void cancel() {
            }
        };
        
        Main.worker.execute(uploadTask);
    }

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void writeContentDisposition(DataOutputStream out, String name, String value) throws IOException {
        out.writeBytes("--" + BOUNDARY + LINE_END);
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + LINE_END);
        out.writeBytes(LINE_END);
        // DataOutputStream's "writeUTF" method is unsuitable here because it adds two
        // char length bytes in front
        byte[] temp=value.getBytes("UTF-8");
        out.write(temp, 0, temp.length);
        out.writeBytes(LINE_END);
    }

    private void writeContentDispositionGpxData(DataOutputStream out, String name, GpxData gpxData) throws IOException {
        out.writeBytes("--" + BOUNDARY + LINE_END);
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + datename +".gpx" + "\"" + LINE_END);
        out.writeBytes("Content-Type: application/octet-stream" + LINE_END);
        out.writeBytes(LINE_END);
        new GpxWriter(out).write(gpxData);
        out.flush();
        out.writeBytes(LINE_END);
    }
}
