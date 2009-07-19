/*
 * UploadDataGui.java
 *
 * Created on August 17, 2008, 6:56 PM
 * Copyright by Subhodip Biswas
 * This program is free software and licensed under GPL.
 */

package org.openstreetmap.josm.plugins.DirectUpload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.JMultilineLabel;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.GpxWriter;
import org.openstreetmap.josm.tools.Base64;
import org.openstreetmap.josm.tools.GBC;

/**
 *
 * @author  subhodip, xeen
 */
public class UploadDataGui extends ExtendedDialog {
    // User for log in when uploading trace
    private String username = Main.pref.get("osm-server.username");
    private String password = Main.pref.get("osm-server.password");

    // Fields are declared here for easy access
    // Do not remove the space in JMultilineLabel. Otherwise the label will be empty
    // as we don't know its contents yet and therefore have a height of 0. This will
    // lead to unnecessary scrollbars.
    private JMultilineLabel OutputDisplay = new JMultilineLabel(" ");
    private JTextField descriptionField = new JTextField(50);
    private JTextField tagsField = new JTextField(50);
    private JCheckBox publicCheckbox = new JCheckBox();

    // Constants used when generating upload request
    private static final String API_VERSION = "0.6";
    private static final String BOUNDARY = "----------------------------d10f7aa230e8";
    private static final String LINE_END = "\r\n";
    private static final String uploadTraceText = tr("Upload Trace");

    // Filename and current date. Date will be used as fallback if filename not available
    private String datename = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
    private String filename = "";

    private boolean cancelled = false;

    public UploadDataGui() {
        // Initalizes ExtendedDialog
        super(Main.parent,
                tr("Upload Traces"),
                new String[] {uploadTraceText, tr("Cancel")},
                true
        );
        JPanel content = initComponents();
        autoSelectTrace();

        setupDialog(content, new String[] { "uploadtrace.png", "cancel.png" });

        buttons.get(0).setEnabled(!checkForGPXLayer());
    }

    /**
     * Sets up the dialog window elements
     * @return JPanel with components
     */
    private JPanel initComponents() {
        publicCheckbox.setText(tr("Public"));
        publicCheckbox.setToolTipText(tr("Selected makes your trace public in openstreetmap.org"));

        JLabel descriptionLabel = new JLabel(tr("Description"));
        descriptionField.setToolTipText(tr("Please enter Description about your trace."));

        JLabel tagsLabel = new JLabel(tr("Tags"));
        tagsField.setToolTipText(tr("Please enter tags about your trace."));

        JPanel p = new JPanel(new GridBagLayout());

        OutputDisplay.setMaxWidth(findMaxDialogSize().width-10);
        p.add(OutputDisplay, GBC.eol());

        p.add(tagsLabel, GBC.eol().insets(0,10,0,0));
        p.add(tagsField, GBC.eol().fill(GBC.HORIZONTAL));

        p.add(descriptionLabel, GBC.eol().insets(0,10,0,0));
        p.add(descriptionField, GBC.eol().fill(GBC.HORIZONTAL));

        p.add(publicCheckbox, GBC.eol());

        return p;
    }

    /**
     * This function will automatically select a GPX layer if it's the only one.
     * If a GPX layer is found, its filename will be parsed and displayed
     */
    private void autoSelectTrace() {
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
                try {
                    filename = data.storageFile.getName()
                                    .replaceAll("[&?/\\\\]"," ").replaceAll("(\\.[^.]*)$","");
                } catch(Exception e) { }
                descriptionField.setText(getFilename());
                OutputDisplay.setText(tr("Selected track: {0}", getFilename()));
            }
        }
    }

    /**
     * This is the actual workhorse that manages the upload.
     * @param String Description of the GPX track being uploaded
     * @param String Tags assosciated with the GPX track being uploaded
     * @param boolean Shall the GPX track be public
     * @param GpxData The GPX Data to upload
     */
    private void upload(String description, String tags, Boolean isPublic, GpxData gpxData, ProgressMonitor progressMonitor) throws IOException {
    	progressMonitor.beginTask(null);
    	try {
    		if(checkForErrors(username, password, description, gpxData))
    			return;

    		// Clean description/tags from disallowed chars
    		description = description.replaceAll("[&?/\\\\]"," ");
    		tags = tags.replaceAll("[&?/\\\\.,;]"," ");

    		// Set progress dialog to indeterminate while connecting
    		progressMonitor.indeterminateSubTask(tr("Connecting..."));

    		try {
    			// Generate data for upload
    			ByteArrayOutputStream baos  = new ByteArrayOutputStream();
    			writeGpxFile(baos, "file", gpxData);
    			writeField(baos, "description", description);
    			writeField(baos, "tags", (tags != null && tags.length() > 0) ? tags : "");
    			writeField(baos, "public", isPublic ? "1" : "0");
    			writeString(baos, "--" + BOUNDARY + "--" + LINE_END);

    			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    			HttpURLConnection conn = setupConnection(baos.size());

    			progressMonitor.setTicksCount(baos.size());
    			progressMonitor.subTask(null);

    			try {
    				flushToServer(bais, conn.getOutputStream(), progressMonitor);
    			} catch(Exception e) {}

    			if(cancelled) {
    				conn.disconnect();
    				OutputDisplay.setText(tr("Upload cancelled"));
    				buttons.get(0).setEnabled(true);
    				cancelled = false;
    			} else {
    				boolean success = finishUpConnection(conn);
    				buttons.get(0).setEnabled(!success);
    				if(success)
    					buttons.get(1).setText(tr("Close"));
    			}
    		} catch(Exception e) {
    			OutputDisplay.setText(tr("Error while uploading"));
    			e.printStackTrace();
    		}
    	} finally {
    		progressMonitor.finishTask();
    	}
    }

    /**
     * This function sets up the upload URL and logs in using the username and password given
     * in the preferences.
     * @param int The length of the content to be sent to the server
     * @return HttpURLConnection The set up conenction
     */
    private HttpURLConnection setupConnection(int contentLength) throws Exception {
        // Encode username and password
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        String auth = username + ":" + password;
        ByteBuffer bytes = encoder.encode(CharBuffer.wrap(auth));

        // Upload URL
        URL url = new URL("http://www.openstreetmap.org/api/" + API_VERSION + "/gpx/create");

        // Set up connection and log in
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setFixedLengthStreamingMode(contentLength);
        c.setConnectTimeout(15000);
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.addRequestProperty("Authorization", "Basic " + Base64.encode(bytes));
        c.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        c.addRequestProperty("Connection", "close"); // counterpart of keep-alive
        c.addRequestProperty("Expect", "");
        c.connect();

        return c;
    }

    /**
     * This function checks if the given connection finished up fine, closes it and returns the result.
     * It also posts the result (or errors) to OutputDisplay.

     * @param HttpURLConnection The connection to check/finish up
     */
    private boolean finishUpConnection(HttpURLConnection c) throws Exception {
        String returnMsg = c.getResponseMessage();
        boolean success = returnMsg.equals("OK");

        if (c.getResponseCode() != 200) {
            if (c.getHeaderField("Error") != null)
                returnMsg += "\n" + c.getHeaderField("Error");
        }

        OutputDisplay.setText(success
            ? tr("GPX upload was successful")
            : tr("Upload failed. Server returned the following message: ") + returnMsg);

        c.disconnect();
        return success;
    }

    /**
     * Uploads a given InputStream to a given OutputStream and sets progress
     * @param InputSteam
     * @param OutputStream
     */
    private void flushToServer(InputStream in, OutputStream out, ProgressMonitor progressMonitor) throws Exception {
        // Upload in 10 kB chunks
        byte[] buffer = new byte[10000];
        int nread;
        int cur = 0;
        synchronized (in) {
            while ((nread = in.read(buffer, 0, buffer.length)) >= 0) {
                out.write(buffer, 0, nread);
                cur += nread;
                out.flush();
                progressMonitor.worked(nread);
                progressMonitor.subTask(getProgressText(cur, progressMonitor));

                if(cancelled)
                    break;
            }
        }
        if(!cancelled)
            out.flush();
        progressMonitor.subTask("Waiting for server reply...");
        buffer = null;
    }

    /**
     * Generates the output string displayed in the PleaseWaitDialog.
     * @param int Bytes already uploaded
     * @return String Message
     */
    private String getProgressText(int cur, ProgressMonitor progressMonitor) {
        int max = progressMonitor.getTicksCount();
        int percent = Math.round(cur * 100 / max);
        return tr("Uploading GPX track: {0}% ({1} of {2})",
                        percent, formatBytes(cur), formatBytes(max));
    }

    /**
     * Nicely calculates given bytes into MB, kB and B (with units)
     * @param int Bytes
     * @return String
     */
    private String formatBytes(int bytes) {
        return (bytes > 1000 * 1000
                    // Rounds to 2 decimal places
                    ? new DecimalFormat("0.00")
                        .format((double)Math.round(bytes/(1000*10))/100) + " MB"
                    : (bytes > 1000
                        ? Math.round(bytes/1000) + " kB"
                        : bytes + " B"));
    }

    /**
     * Checks for common errors and displays them in OutputDisplay if it finds any.
     * Returns whether errors have been found or not.
     * @param String OSM username
     * @param String OSM password
     * @param String GPX track description
     * @param GpxData the GPX data to upload
     * @return boolean true if errors have been found
     */
    private boolean checkForErrors(String username, String password,
                                   String description, GpxData gpxData) {
        String errors="";
        if(description == null || description.length() == 0)
            errors += tr("No description provided. Please provide some description.");

        if(gpxData == null)
            errors += tr("No GPX layer selected. Cannot upload a trace.");

        if(username == null || username.length() == 0)
            errors += tr("No username provided.");

        if(password == null || password.length() == 0)
            errors += tr("No password provided.");

        OutputDisplay.setText(errors);
        return errors.length() > 0;
    }

    /**
     * Checks if a GPX layer is selected and returns the result. Also writes an error
     * message to OutputDisplay if result is false.
     * @return boolean True, if /no/ GPX layer is selected
     */
    private boolean checkForGPXLayer() {
        if(Main.map == null
                || Main.map.mapView == null
                || Main.map.mapView.getActiveLayer() == null
                || !(Main.map.mapView.getActiveLayer() instanceof GpxLayer)) {
            OutputDisplay.setText(tr("No GPX layer selected. Cannot upload a trace."));
            return true;
        }
        return false;
    }


    /**
     * This creates the uploadTask that does the actual work and hands it to the main.worker to be executed.
     */
    private void setupUpload() {
        if(checkForGPXLayer()) return;

        // Disable Upload button so users can't just upload that track again
        buttons.get(0).setEnabled(false);

        PleaseWaitRunnable uploadTask = new PleaseWaitRunnable(tr("Uploading GPX Track")){
            @Override protected void realRun() throws IOException {
                  upload(descriptionField.getText(),
                         tagsField.getText(),
                         publicCheckbox.isSelected(),
                         ((GpxLayer)Main.map.mapView.getActiveLayer()).data,
                         progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false)
                  );
            }
            @Override protected void finish() {}
            @Override protected void cancel() {
                cancelled = true;
            }
        };

        Main.worker.execute(uploadTask);
    }

    /**
     * Writes textfields (like in webbrowser) to the given ByteArrayOutputStream
     * @param ByteArrayOutputStream
     * @param String The name of the "textbox"
     * @param String The value to write
     */
    private void writeField(ByteArrayOutputStream baos, String name, String value) throws IOException {
        writeBoundary(baos);
        writeString(baos, "Content-Disposition: form-data; name=\"" + name + "\"");
        writeLineEnd(baos);
        writeLineEnd(baos);
        baos.write(value.getBytes("UTF-8"));
        writeLineEnd(baos);
    }

    /**
     * Writes gpxData (= file field in webbrowser) to the given ByteArrayOutputStream
     * @param ByteArrayOutputStream
     * @param String The name of the "upload field"
     * @param GpxData The GPX data to upload
     */
    private void writeGpxFile(ByteArrayOutputStream baos, String name, GpxData gpxData) throws IOException {
        writeBoundary(baos);
        writeString(baos, "Content-Disposition: form-data; name=\"" + name + "\"; ");
        writeString(baos, "filename=\"" + getFilename() + ".gpx" + "\"");
        writeLineEnd(baos);
        writeString(baos, "Content-Type: application/octet-stream");
        writeLineEnd(baos);
        writeLineEnd(baos);
        new GpxWriter(baos).write(gpxData);
        writeLineEnd(baos);
    }

    /**
     * Writes a String to the given ByteArrayOutputStream
     * @param ByteArrayOutputStream
     * @param String
     */
    private void writeString(ByteArrayOutputStream baos, String s) {
        try {
            baos.write(s.getBytes());
        } catch(Exception e) {}
    }

    /**
     * Writes a newline to the given ByteArrayOutputStream
     * @param ByteArrayOutputStream
     */
    private void writeLineEnd(ByteArrayOutputStream baos) {
        writeString(baos, LINE_END);
    }

    /**
     * Writes a boundary line to the given ByteArrayOutputStream
     * @param ByteArrayOutputStream
     */
    private void writeBoundary(ByteArrayOutputStream baos) {
        writeString(baos, "--" + BOUNDARY);
        writeLineEnd(baos);
    }

    /**
     * Returns the filename of the GPX file to be upload. If not available, returns current date
     * as an alternative
     * @param String
     */
    private String getFilename() {
       return filename.equals("") ? datename : filename;
    }

    /**
     * Overrides the default actions. Will not close the window when upload trace is clicked
     */
    @Override protected void buttonAction(ActionEvent evt) {
        String a = evt.getActionCommand();
        if(uploadTraceText.equals(a))
            setupUpload();
        else
            setVisible(false);
    }
}
