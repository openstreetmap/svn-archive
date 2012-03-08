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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.JMultilineLabel;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;
import org.openstreetmap.josm.io.GpxWriter;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.UrlLabel;

/**
 *
 * @author  subhodip, xeen, ax
 */
public class UploadDataGui extends ExtendedDialog {
    
    /**
     * This enum contains the possible values for the visibility field and their
     * explanation. Provides some methods for easier handling.
     */
    private enum visibility {
        PRIVATE      (tr("Private (only shared as anonymous, unordered points)")),
        PUBLIC       (tr("Public (shown in trace list and as anonymous, unordered points)")),
        TRACKABLE    (tr("Trackable (only shared as anonymous, ordered points with timestamps)")),
        IDENTIFIABLE (tr("Identifiable (shown in trace list and as identifiable, ordered points with timestamps)"));

        public final String description;
        visibility(String description) {
            this.description = description;
        }

        /**
         * "Converts" a given description into the actual enum. Returns null if no matching description
         * is found.
         * @param desc The description to look for
         * @return visibility or null
         */
        public static visibility desc2visi(Object desc) {
            for (visibility v : visibility.values()) {
                if(desc.equals(v.description))
                    return v;
            }
            return null;
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }

    // Fields are declared here for easy access
    // Do not remove the space in JMultilineLabel. Otherwise the label will be empty
    // as we don't know its contents yet and therefore have a height of 0. This will
    // lead to unnecessary scrollbars.
    private JMultilineLabel OutputDisplay = new JMultilineLabel(" ");
    private HistoryComboBox descriptionField;
    private HistoryComboBox tagsField;
    private JComboBox visibilityCombo;

    // Constants used when generating upload request
    private static final String API_VERSION = "0.6";
    private static final String BOUNDARY = "----------------------------d10f7aa230e8";
    private static final String LINE_END = "\r\n";
    private static final String uploadTraceText = tr("Upload Trace");

    private boolean canceled = false;
    
    public UploadDataGui() {
        // Initalizes ExtendedDialog
        super(Main.parent,
                tr("Upload Traces"),
                new String[] {uploadTraceText, tr("Cancel")},
                true
        );
        JPanel content = initComponents();
        GpxData gpxData = UploadOsmConnection.getInstance().autoSelectTrace();
        initTitleAndDescriptionFromGpxData(gpxData);    // this is changing some dialog elements, so it (probably) must be before the following  
        setContent(content);
        setButtonIcons(new String[] { "uploadtrace.png", "cancel.png" });
        setupDialog();

        buttons.get(0).setEnabled(gpxData != null);
    }

    /**
     * Sets up the dialog window elements
     * @return JPanel with components
     */
    private JPanel initComponents() {
        // visibilty
        JLabel visibilityLabel = new JLabel(tr("Visibility"));
        visibilityLabel.setToolTipText(tr("Defines the visibility of your trace for other OSM users."));
        
        visibilityCombo = new JComboBox();
        visibilityCombo.setEditable(false);
        for(visibility v : visibility.values()) {
            visibilityCombo.addItem(v.description);
        }
        visibilityCombo.setSelectedItem(visibility.valueOf(Main.pref.get("directupload.visibility.last-used", visibility.PRIVATE.name())).description);
        UrlLabel visiUrl = new UrlLabel(tr("http://wiki.openstreetmap.org/wiki/Visibility_of_GPS_traces"), tr("(What does that mean?)"), 2);

        // description
        JLabel descriptionLabel = new JLabel(tr("Description"));
        descriptionField = new HistoryComboBox();
        descriptionField.setToolTipText(tr("Please enter Description about your trace."));
        
        List<String> descHistory = new LinkedList<String>(Main.pref.getCollection("directupload.description.history", new LinkedList<String>()));
        // we have to reverse the history, because ComboBoxHistory will reverse it againin addElement()
        // XXX this should be handled in HistoryComboBox
        Collections.reverse(descHistory);
        descriptionField.setPossibleItems(descHistory);

        // tags
        JLabel tagsLabel = new JLabel(tr("Tags (comma delimited)"));
        tagsField = new HistoryComboBox();
        tagsField.setToolTipText(tr("Please enter tags about your trace."));

        List<String> tagsHistory = new LinkedList<String>(Main.pref.getCollection("directupload.tags.history", new LinkedList<String>()));
        // we have to reverse the history, because ComboBoxHistory will reverse it againin addElement()
        // XXX this should be handled in HistoryComboBox
        Collections.reverse(tagsHistory);
        tagsField.setPossibleItems(tagsHistory);

        JPanel p = new JPanel(new GridBagLayout());

        OutputDisplay.setMaxWidth(findMaxDialogSize().width-10);
        p.add(OutputDisplay, GBC.eol());

        p.add(tagsLabel, GBC.eol().insets(0,10,0,0));
        p.add(tagsField, GBC.eol().fill(GBC.HORIZONTAL));

        p.add(descriptionLabel, GBC.eol().insets(0,10,0,0));
        p.add(descriptionField, GBC.eol().fill(GBC.HORIZONTAL));

        p.add(visibilityLabel, GBC.std().insets(0,10,0,0));
        p.add(visiUrl, GBC.eol().insets(5,10,0,0));
        p.add(visibilityCombo, GBC.eol());

        return p;
    }

    private void initTitleAndDescriptionFromGpxData(GpxData gpxData) {
      String description, title;
      try {
          description = gpxData.storageFile.getName().replaceAll("[&?/\\\\]"," ").replaceAll("(\\.[^.]*)$","");
          title = tr("Selected track: {0}", gpxData.storageFile.getName());
      }
      catch(Exception e) {
          description = new SimpleDateFormat("yyMMddHHmmss").format(new Date()); 
          title = tr("No GPX layer selected. Cannot upload a trace.");
      }
      OutputDisplay.setText(title);
      descriptionField.setText(description);
    }

    /**
     * This is the actual workhorse that manages the upload.
     * @param String Description of the GPX track being uploaded
     * @param String Tags assosciated with the GPX track being uploaded
     * @param boolean Shall the GPX track be public
     * @param GpxData The GPX Data to upload
     */
    private void upload(String description, String tags, String visi, GpxData gpxData, ProgressMonitor progressMonitor) throws IOException {
        progressMonitor.beginTask(tr("Uploading trace ..."));
        try {
            if (checkForErrors(description, gpxData)) {
                return;
            }

            // Clean description/tags from disallowed chars
            description = description.replaceAll("[&?/\\\\]", " ");
            tags = tags.replaceAll("[&?/\\\\.;]", " ");

            // Set progress dialog to indeterminate while connecting
            progressMonitor.indeterminateSubTask(tr("Connecting..."));

            // Generate data for upload
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writeGpxFile(baos, "file", gpxData);
            writeField(baos, "description", description);
            writeField(baos, "tags", (tags != null && tags.length() > 0) ? tags : "");
            writeField(baos, "visibility", visi);
            writeString(baos, "--" + BOUNDARY + "--" + LINE_END);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            HttpURLConnection conn = setupConnection(baos.size());

            progressMonitor.setTicksCount(baos.size());
            progressMonitor.subTask(null);

            flushToServer(bais, conn.getOutputStream(), progressMonitor);

            if (canceled) {
                conn.disconnect();
                OutputDisplay.setText(tr("Upload canceled"));
                buttons.get(0).setEnabled(true);
                canceled = false;
            }
            else {
                boolean success = finishUpConnection(conn);
                buttons.get(0).setEnabled(!success);
                if (success) {
                    buttons.get(1).setText(tr("Close"));
                }
            }
        }
        catch (Exception e) {
            OutputDisplay.setText(tr("Error while uploading"));
            e.printStackTrace();
        }
        finally {
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

        // Upload URL
        URL url = new URL("http://www.openstreetmap.org/api/" + API_VERSION + "/gpx/create");

        // Set up connection and log in
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setFixedLengthStreamingMode(contentLength);
        c.setConnectTimeout(15000);
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        // unfortunately, addAuth() is protected, so we need to subclass OsmConnection 
        // XXX make addAuth public. 
        UploadOsmConnection.getInstance().addAuthHack(c);

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

                if(canceled)
                    break;
            }
        }
        if(!canceled)
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
     * @param String GPX track description
     * @param GpxData the GPX data to upload
     * @return boolean true if errors have been found
     */
    private boolean checkForErrors(String description, GpxData gpxData) {
        String errors="";
        if(description == null || description.length() == 0)
            errors += tr("No description provided. Please provide some description.");

        if(gpxData == null)
            errors += tr("No GPX layer selected. Cannot upload a trace.");

        OutputDisplay.setText(errors);
        return errors.length() > 0;
    }

    /**
     * This creates the uploadTask that does the actual work and hands it to the main.worker to be executed.
     */
    private void setupUpload() {
        final GpxData gpxData = UploadOsmConnection.getInstance().autoSelectTrace();
        if (gpxData == null) {
            return;
        }

        // Disable Upload button so users can't just upload that track again
        buttons.get(0).setEnabled(false);
        
        // save history
        Main.pref.put("directupload.visibility.last-used", visibility.desc2visi(visibilityCombo.getSelectedItem().toString()).name());
        
        descriptionField.addCurrentItemToHistory();
        Main.pref.putCollection("directupload.description.history", descriptionField.getHistory());

        tagsField.addCurrentItemToHistory();
        Main.pref.putCollection("directupload.tags.history", tagsField.getHistory());

        PleaseWaitRunnable uploadTask = new PleaseWaitRunnable(tr("Uploading GPX Track")){
            @Override protected void realRun() throws IOException {
                  upload(descriptionField.getText(),
                         tagsField.getText(),
                         visibility.desc2visi(visibilityCombo.getSelectedItem()).toString(),
                         gpxData,
                         progressMonitor.createSubTaskMonitor(ProgressMonitor.ALL_TICKS, false)
                  );
            }
            @Override protected void finish() {}
            @Override protected void cancel() {
                canceled = true;
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
        writeString(baos, "filename=\"" + gpxData.storageFile.getName() + "\"");
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
     * Overrides the default actions. Will not close the window when upload trace is clicked
     */
    @Override protected void buttonAction(int buttonIndex, ActionEvent evt) {
        String a = evt.getActionCommand();
        if(uploadTraceText.equals(a))
            setupUpload();
        else
            setVisible(false);
    }
}
