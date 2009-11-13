// This software is released into the Public Domain.
// See copying.txt for details.
package org.openstreetmap.osmosis.core.xml.v0_6;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisConstants;
import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.ChangeContainer;
import org.openstreetmap.osmosis.core.task.v0_6.ChangeSink;
import org.openstreetmap.osmosis.core.xml.v0_6.impl.OsmChangeWriter;


/**
 * An OSM change sink for uploading all data to an OpenStreetMap server.
 *
 * @author Marcus Wolschon Marcus@Wolscon.biz
 */
public class XmlChangeUploader implements ChangeSink {

    /**
     * Our logger for debug and error -output.
     */
    private static final Logger LOG = Logger.getLogger(
            XmlChangeUploader.class.getName());

    /**
     * Default-value for {@link DownloadingDataSet#APIBASEURSETTING}.
     */
    private static final String DEFAULTAPIBASEURL =
             "http://api.openstreetmap.org/api/0.6";

    /**
     * The baseURL defaults to {@value #DEFAULTAPIBASEURL}.
     */
    private String myBaseURL;

    /**
     * the user-name to use.
     */
    private String myUserName;

    /**
     * the password to use.
     */
    private String myPassword;

    /**
     * Comment to add to the Changeset.
     */
    private String myComment;

    /**
     * Used to generate the XML-content of an osc-file.
     */
    private OsmChangeWriter myChangeWriter;

    /**
     * The ID of the changeset we opened on the server.
     */
    private int myChangesetNumber = -1;

    /**
     * We cache the changeset here to replace the
     * changeset-id later.
     */
    private StringWriter myChangesetBuffer = new StringWriter();

    /**
     * Creates a new instance.
     * The baseURL defaults to {@value #DEFAULTAPIBASEURL}.
     * @param aBaseURL may be null
     * @param aUserName the user-name to use
     * @param aPassword the password to use
     * @param aComment the comment to set in the changeset
     */
    public XmlChangeUploader(final String aBaseURL,
                             final String aUserName,
                             final String aPassword,
                             final String aComment) {

        if (aBaseURL == null) {
            this.myBaseURL = DEFAULTAPIBASEURL;
        } else {
            this.myBaseURL = aBaseURL;
        }
        if (aUserName == null) {
            throw new IllegalArgumentException("null username given");
        }
        this.myUserName = aUserName;
        if (myPassword == null) {
            throw new IllegalArgumentException("null password given");
        }
        if (aComment == null) {
            this.myComment = "";
        } else {
            this.myComment = aComment;
        }
        this.myPassword = aPassword;

        this.myChangeWriter = new OsmChangeWriter("osmChange", 0);
    }

    /**
     * Open the changeset if it is not yet open.
     * @throws IOException if we cannot contact the server.
     */
    protected final void initialize() throws IOException {
        if (myChangesetNumber == -1) {
            URL url = new URL(this.myBaseURL + "/changeset/create");
            System.err.println("DEBUG: URL= " + url.toString());
            HttpURLConnection httpCon = (HttpURLConnection)
                                      url.openConnection();

            // we do not use Authenticator.setDefault()
            // here to stay thread-safe.
            httpCon.setRequestProperty("Authorization", "Basic "
                    + (new sun.misc.BASE64Encoder()).encode(
                            (this.myUserName + ":"
                           + this.myPassword).getBytes()));


            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(
                httpCon.getOutputStream());
            out.write("<osm version=\"0.6\" generator=\"Osmosis "
                    + OsmosisConstants.VERSION + "\">\n"
                    + "\t<changeset>\n");
            out.write("\t\t<tag k=\"created_by\" v=\"Osmosis\"/>\n");
            out.write("\t\t<tag k=\"comment\" v=\""
                           + this.myComment + "\"/>\n");
            out.write("\t</changeset>\n</osm>");
            out.close();

            Reader in = new InputStreamReader(httpCon.getInputStream());
            char[] buffer = new char[Byte.MAX_VALUE];
            int len = in.read(buffer);
            int changeset = Integer.parseInt(new String(buffer, 0, len));

            LOG.info("opened changeset with ID: " + changeset);
            this.myChangesetNumber = changeset;

            this.myChangeWriter.begin();
            this.myChangeWriter.setWriter(this.myChangesetBuffer);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void process(final ChangeContainer changeContainer) {
        try {
            initialize();

            myChangeWriter.process(changeContainer);
        } catch (IOException e) {
            throw new OsmosisRuntimeException(
                    "Cannot open changeset on server", e);
        }
    }

    /**
     * close the changeset on the server.
     */
    public final void complete() {
        try {
            uploadChangeBuffer();
            closeChangeset();
        } catch (Exception e) {
            throw new OsmosisRuntimeException(
                    "cannot upload or close changeset.", e);
        }
    }

    /**
     * Upload the buffered changes to the server,
     * replacing the changeset-number.
     * @throws IOException if we cannot contact the server
     */
    private void uploadChangeBuffer() throws IOException {
        URL url = new URL(this.myBaseURL + "/changeset/"
                        + this.myChangesetNumber + "/upload");
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);

        // we do not use Authenticator.setDefault() here to stay thread-safe.
        httpCon.setRequestProperty("Authorization", "Basic "
                + (new sun.misc.BASE64Encoder()).encode(
                        (this.myUserName + ":"
                       + this.myPassword).getBytes()));

        OutputStream out = httpCon.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out);
        String changeSet = this.myChangesetBuffer.getBuffer().toString();
        System.out.println("changeset we got uploading:\n" + changeSet);
        String modified = changeSet.replaceAll("changeset=\"[0-9]*\"",
                                               "changeset=\""
                                             + this.myChangesetNumber + "\"");
        System.out.println("changeset we are uploading:\n" + modified);
        writer.write(modified);
        writer.close();
        //TODO: check response-code
        LOG.fine("response-code to changeset: "
                + httpCon.getResponseCode());
    }

    /**
     * Close the changeset on the server,
     * commiting the change.
     * @throws IOException if we cannot contact the server
     */
    private void closeChangeset() throws IOException {
        URL url = new URL(this.myBaseURL + "/changeset/"
                + this.myChangesetNumber + "/close");
        System.err.println("DEBUG: URL= " + url.toString());
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);

        // we do not use Authenticator.setDefault() here to stay thread-safe.
        httpCon.setRequestProperty("Authorization", "Basic "
                + (new sun.misc.BASE64Encoder()).encode(
                        (this.myUserName + ":"
                       + this.myPassword).getBytes()));

        httpCon.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        httpCon.connect();
        System.out.println("response-code to closing of changeset: "
                + httpCon.getResponseCode());
        this.myChangesetNumber = -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void release() {
        if (this.myChangesetNumber != -1) {
            try {
                closeChangeset();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Cannot close changeset.", e);
            }
        }
    }

}
