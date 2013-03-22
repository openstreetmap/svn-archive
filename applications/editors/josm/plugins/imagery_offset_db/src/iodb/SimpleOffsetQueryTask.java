package iodb;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * A task to query the imagery offset server and process the response.
 *
 * @author Zverik
 * @license WTFPL
 */
class SimpleOffsetQueryTask extends PleaseWaitRunnable {
    private String query;
    private String errorMessage;
    private String title;
    protected boolean cancelled;
    private QuerySuccessListener listener;

    /**
     * Initialize the task.
     * @param query A query string, usually starting with an action word and a question mark.
     * @param title A title for the progress monitor.
     */
    public SimpleOffsetQueryTask( String query, String title ) {
        super(tr("Uploading"));
        this.query = query;
        this.title = title;
        cancelled = false;
    }

    /**
     * In case a query was not specified when the object was constructed,
     * it can be set with this method.
     * @see #SimpleOffsetQueryTask(java.lang.String, java.lang.String) 
     */
    public void setQuery( String query ) {
        this.query = query;
    }

    /**
     * Install a listener for successful responses. There can be only one.
     */
    public void setListener( QuerySuccessListener listener ) {
        this.listener = listener;
    }

    /**
     * Remove a listener for successful responses.
     */
    public void removeListener() {
        this.listener = null;
    }

    /**
     * The main method: calls {@link #doQuery(java.lang.String)} and processes exceptions.
     */
    @Override
    protected void realRun() {
        getProgressMonitor().indeterminateSubTask(title);
        try {
            errorMessage = null;
            doQuery(query);
        } catch( UploadException e ) {
            errorMessage = tr("Server has rejected the request") + ":\n" + e.getMessage();
        } catch( IOException e ) {
            errorMessage = tr("Unable to connect to the server") + "\n" + e.getMessage();
        }
    }

    /**
     * Sends a request to the imagery offset server. Processes exceptions and
     * return codes, calls {@link #processResponse(java.io.InputStream)} on success.
     * @param query
     * @throws iodb.SimpleOffsetQueryTask.UploadException
     * @throws IOException 
     */
    private void doQuery( String query ) throws UploadException, IOException {
        try {
            String serverURL = Main.pref.get("iodb.server.url", "http://offsets.textual.ru/");
            URL url = new URL(serverURL + query);
            Main.info("IODB URL = " + url); // todo: remove in release
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            if( connection.getResponseCode() != 200 ) {
                throw new IOException("HTTP Response code " + connection.getResponseCode() + " (" + connection.getResponseMessage() + ")");
            }
            InputStream inp = connection.getInputStream();
            if( inp == null )
                throw new IOException("Empty response");
            try {
                if( !cancelled )
                    processResponse(inp);
            } finally {
                connection.disconnect();
            }
        } catch( MalformedURLException ex ) {
            throw new IOException("Malformed URL: " + ex.getMessage());
        }
    }

    /**
     * Doesn't actually cancel, just raises a flag.
     */
    @Override
    protected void cancel() {
        cancelled = true;
    }

    /**
     * Is called after {@link #realRun()}. Either displays an error message
     * or notifies a listener of success.
     */
    @Override
    protected void finish() {
        if( errorMessage != null ) {
            JOptionPane.showMessageDialog(Main.parent, errorMessage, tr("Imagery Offset"), JOptionPane.ERROR_MESSAGE);
        } else if( listener != null ) {
            listener.queryPassed();
        }
    }

    /**
     * Parse the response input stream and determine whether an operation
     * was successful or not.
     * @throws iodb.SimpleOffsetQueryTask.UploadException Thrown if an error message was found.
     */
    protected void processResponse( InputStream inp ) throws UploadException {
        String response = "";
        if( inp != null ) {
            Scanner sc = new Scanner(inp).useDelimiter("\\A");
            response = sc.hasNext() ? sc.next() : "";
        }
        Pattern p = Pattern.compile("<(\\w+)>([^<]+)</\\1>");
        Matcher m = p.matcher(response);
        if( m.find() ) {
            if( m.group(1).equals("error") ) {
                throw new UploadException(m.group(2));
            }
        } else {
            throw new UploadException("No response");
        }
    }

    /**
     * A placeholder exception for error messages.
     */
    public static class UploadException extends Exception {
        public UploadException( String message ) {
            super(message);
        }
    }
}
