// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.DirectUpload;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.josm.data.gpx.GpxConstants;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.gpx.GpxTrack;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.gui.util.GuiHelper;
import org.openstreetmap.josm.gui.widgets.HistoryComboBox;
import org.openstreetmap.josm.gui.widgets.JMultilineLabel;
import org.openstreetmap.josm.gui.widgets.UrlLabel;
import org.openstreetmap.josm.io.GpxWriter;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.io.OsmTransferException;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.HttpClient.Response;
import org.openstreetmap.josm.tools.Logging;

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
    private JMultilineLabel outputDisplay = new JMultilineLabel(" ");
    private HistoryComboBox descriptionField;
    private HistoryComboBox tagsField;
    private JComboBox<String> visibilityCombo;

    // Constants used when generating upload request
    private static final String BOUNDARY = "----------------------------d10f7aa230e8";
    private static final String LINE_END = "\r\n";
    private static final String uploadTraceText = tr("Upload Trace");

    private boolean canceled = false;

    public UploadDataGui() {
        // Initalizes ExtendedDialog
        super(MainApplication.getMainFrame(),
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

        visibilityCombo = new JComboBox<>();
        visibilityCombo.setEditable(false);
        for(visibility v : visibility.values()) {
            visibilityCombo.addItem(v.description);
        }
        visibilityCombo.setSelectedItem(visibility.valueOf(Config.getPref().get("directupload.visibility.last-used", visibility.PRIVATE.name())).description);
        /* I18n: either copy the link verbose or replace it by the translated version from the wiki for already translated languages */
        UrlLabel visiUrl = new UrlLabel(tr("https://wiki.openstreetmap.org/wiki/Visibility_of_GPS_traces"), tr("(What does that mean?)"), 2);

        // description
        JLabel descriptionLabel = new JLabel(tr("Description"));
        descriptionField = new HistoryComboBox();
        descriptionField.setToolTipText(tr("Please enter Description about your trace."));

        List<String> descHistory = new LinkedList<>(Config.getPref().getList("directupload.description.history", new LinkedList<String>()));
        // we have to reverse the history, because ComboBoxHistory will reverse it again in addElement()
        // XXX this should be handled in HistoryComboBox
        Collections.reverse(descHistory);
        descriptionField.setPossibleItems(descHistory);

        // tags
        JLabel tagsLabel = new JLabel(tr("Tags (comma delimited)"));
        tagsField = new HistoryComboBox();
        tagsField.setToolTipText(tr("Please enter tags about your trace."));

        List<String> tagsHistory = new LinkedList<>(Config.getPref().getList("directupload.tags.history", new LinkedList<String>()));
        // we have to reverse the history, because ComboBoxHistory will reverse it againin addElement()
        // XXX this should be handled in HistoryComboBox
        Collections.reverse(tagsHistory);
        tagsField.setPossibleItems(tagsHistory);

        JPanel p = new JPanel(new GridBagLayout());

        outputDisplay.setMaxWidth(findMaxDialogSize().width-10);
        p.add(outputDisplay, GBC.eol());

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
      String description = "", title = " ", tags = "";
      // non-breaking space in title fixes #10275
      if (gpxData != null) {
          GpxTrack firstTrack = gpxData.tracks.isEmpty() ? null : gpxData.tracks.iterator().next();
          Object meta_desc = gpxData.attr.get(GpxConstants.META_DESC);
          if (meta_desc != null) {
              description = meta_desc.toString();
          } else if (firstTrack != null && gpxData.tracks.size() == 1 && firstTrack.get("desc") != null) {
              description = firstTrack.getString("desc");
          } else if (gpxData.storageFile != null) {
              description = gpxData.storageFile.getName().replaceAll("[&?/\\\\]"," ").replaceAll("(\\.[^.]*)$","");
          }
          if (gpxData.storageFile != null) {
              title = tr("Selected track: {0}", gpxData.storageFile.getName());
          }
          Object meta_tags = gpxData.attr.get(GpxConstants.META_KEYWORDS);
          if (meta_tags != null) {
              tags = meta_tags.toString();
          }
      }
      else {
          description = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
          title = tr("No GPX layer selected. Cannot upload a trace.");
      }
      outputDisplay.setText(title);
      descriptionField.setText(description);
      tagsField.setText(tags);
    }

    /**
     * This is the actual workhorse that manages the upload.
     * @param description Description of the GPX track being uploaded
     * @param tags Tags associated with the GPX track being uploaded
     * @param visi Shall the GPX track be public
     * @param gpxData The GPX Data to upload
     * @param progressMonitor Progress monitor
     * @throws IOException if any I/O error occurs
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

            // Generate data for upload
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writeGpxFile(baos, "file", gpxData);
            writeField(baos, "description", description);
            writeField(baos, "tags", (tags != null && tags.length() > 0) ? tags : "");
            writeField(baos, "visibility", visi);
            writeString(baos, "--" + BOUNDARY + "--" + LINE_END);

            HttpClient conn = setupConnection(baos.size());

            // FIXME previous method allowed to see real % progress (each 10 Kb of data)
            //flushToServer(bais, conn.getOutputStream(), progressMonitor);
            Response response = conn.setRequestBody(baos.toByteArray()).connect(progressMonitor.createSubTaskMonitor(1, false));

            if (canceled) {
            	response.disconnect();
                GuiHelper.runInEDT(new Runnable() {
                    @Override public void run() {
                        outputDisplay.setText(tr("Upload canceled"));
                        buttons.get(0).setEnabled(true);
                    }
                });
                canceled = false;
            }
            else {
                final boolean success = finishUpConnection(response);
                GuiHelper.runInEDT(new Runnable() {
                    @Override public void run() {
                        buttons.get(0).setEnabled(!success);
                        if (success) {
                            buttons.get(1).setText(tr("Close"));
                        }
                    }
                });
            }
        }
        catch (Exception e) {
            GuiHelper.runInEDT(new Runnable() {
                @Override public void run() {
                    outputDisplay.setText(tr("Error while uploading"));
                }
            });
            e.printStackTrace();
        }
        finally {
            progressMonitor.finishTask();
        }
    }

    /**
     * This function sets up the upload URL and logs in using the username and password given
     * in the preferences.
     * @param contentLength The length of the content to be sent to the server
     * @return HttpURLConnection The set up conenction
     * @throws MalformedURLException in case of invalid URL
     * @throws OsmTransferException if auth header cannot be added
     */
    private HttpClient setupConnection(int contentLength) throws MalformedURLException, OsmTransferException {

        // Upload URL
        URL url = new URL(OsmApi.getOsmApi().getBaseUrl() + "gpx/create");

        // Set up connection and log in
        HttpClient c = HttpClient.create(url, "POST").setConnectTimeout(15000);
        // unfortunately, addAuth() is protected, so we need to subclass OsmConnection
        // XXX make addAuth public.
        UploadOsmConnection.getInstance().addAuthHack(c);

        c.setHeader("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        c.setHeader("Connection", "close"); // counterpart of keep-alive
        c.setHeader("Expect", "");

        return c;
    }

    /**
     * This function checks if the given connection finished up fine, closes it and returns the result.
     * It also posts the result (or errors) to OutputDisplay.

     * @param c The HTTP connection to check/finish up
     * @return {@code true} for success
     */
    private boolean finishUpConnection(Response c) {
        String returnMsg = c.getResponseMessage();
        final boolean success = returnMsg.equals("OK");

        if (c.getResponseCode() != 200) {
            if (c.getHeaderField("Error") != null)
                returnMsg += "\n" + c.getHeaderField("Error");
        }

        final String returnMsgEDT = returnMsg;

        GuiHelper.runInEDT(new Runnable() {
            @Override public void run() {
                outputDisplay.setText(success
                        ? tr("GPX upload was successful")
                        : tr("Upload failed. Server returned the following message: ") + returnMsgEDT);
            }
        });

        c.disconnect();
        return success;
    }

    /**
     * Uploads a given InputStream to a given OutputStream and sets progress
     * @param InputSteam
     * @param OutputStream
     */
/*    private void flushToServer(InputStream in, OutputStream out, ProgressMonitor progressMonitor) throws Exception {
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
*/
    /**
     * Generates the output string displayed in the PleaseWaitDialog.
     * @param cur Bytes already uploaded
     * @param progressMonitor Progress monitor
     * @return Message
     */
    private String getProgressText(int cur, ProgressMonitor progressMonitor) {
        int max = progressMonitor.getTicksCount();
        int percent = (cur * 100 / max);
        // FIXME method kept because of translated string
        return tr("Uploading GPX track: {0}% ({1} of {2})",
                        percent, formatBytes(cur), formatBytes(max));
    }

    /**
     * Nicely calculates given bytes into MB, kB and B (with units)
     * @param bytes Bytes
     * @return String
     */
    private String formatBytes(int bytes) {
        return (bytes > 1000 * 1000
                    // Rounds to 2 decimal places
                    ? new DecimalFormat("0.00")
                        .format((double)(bytes/(1000*10))/100) + " MB"
                    : (bytes > 1000
                        ? (bytes/1000) + " kB"
                        : bytes + " B"));
    }

    /**
     * Checks for common errors and displays them in OutputDisplay if it finds any.
     * Returns whether errors have been found or not.
     * @param description GPX track description
     * @param gpxData the GPX data to upload
     * @return boolean true if errors have been found
     */
    private boolean checkForErrors(String description, GpxData gpxData) {
        String errors="";
        if(description == null || description.length() == 0)
            errors += tr("No description provided. Please provide some description.");

        if(gpxData == null)
            errors += tr("No GPX layer selected. Cannot upload a trace.");

        final String errorsEDT = errors;

        GuiHelper.runInEDT(new Runnable() {
            @Override public void run() {
                outputDisplay.setText(errorsEDT);
            }
        });

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
        Config.getPref().put("directupload.visibility.last-used", visibility.desc2visi(visibilityCombo.getSelectedItem().toString()).name());

        descriptionField.addCurrentItemToHistory();
        Config.getPref().putList("directupload.description.history", descriptionField.getHistory());

        tagsField.addCurrentItemToHistory();
        Config.getPref().putList("directupload.tags.history", tagsField.getHistory());

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

        MainApplication.worker.execute(uploadTask);
    }

    /**
     * Writes textfields (like in webbrowser) to the given ByteArrayOutputStream
     * @param baos output stream
     * @param name The name of the "textbox"
     * @param value The value to write
     * @throws IOException if any I/O error occurs
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
     * @param baos output stream
     * @param name The name of the "upload field"
     * @param gpxData The GPX data to upload
     * @throws IOException if any I/O error occurs
     */
    private void writeGpxFile(ByteArrayOutputStream baos, String name, GpxData gpxData) throws IOException {
        String filename;

        writeBoundary(baos);
        writeString(baos, "Content-Disposition: form-data; name=\"" + name + "\"; ");
        if (gpxData.storageFile != null)
            filename = gpxData.storageFile.getName();
        else
            filename = "not saved";
        writeString(baos, "filename=\"" + filename + "\"");
        writeLineEnd(baos);
        writeString(baos, "Content-Type: application/octet-stream");
        writeLineEnd(baos);
        writeLineEnd(baos);
        new GpxWriter(baos).write(gpxData);
        writeLineEnd(baos);
    }

    /**
     * Writes a String to the given ByteArrayOutputStream
     * @param baos output stream
     * @param s string
     */
    private void writeString(ByteArrayOutputStream baos, String s) {
        try {
            baos.write(s.getBytes(StandardCharsets.UTF_8));
        } catch(Exception e) {
            Logging.error(e);
        }
    }

    /**
     * Writes a newline to the given ByteArrayOutputStream
     * @param baos output stream
     */
    private void writeLineEnd(ByteArrayOutputStream baos) {
        writeString(baos, LINE_END);
    }

    /**
     * Writes a boundary line to the given ByteArrayOutputStream
     * @param baos output stream
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
