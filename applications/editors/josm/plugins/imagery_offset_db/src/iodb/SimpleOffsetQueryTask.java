package iodb;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.io.OsmTransferException;
import org.xml.sax.SAXException;
import static org.openstreetmap.josm.tools.I18n.tr;

/**
 *
 * @author zverik
 */
class SimpleOffsetQueryTask extends PleaseWaitRunnable {
    private String query;
    private String errorMessage;
    private String title;
    protected boolean cancelled;

    public SimpleOffsetQueryTask( String query, String title ) {
        super(tr("Uploading"));
        this.query = query;
        this.title = title;
        cancelled = false;
    }

    public void setQuery( String query ) {
        this.query = query;
    }

    @Override
    protected void realRun() throws SAXException, IOException, OsmTransferException {
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

    private void doQuery( String query ) throws UploadException, IOException {
        try {
            URL url = new URL(ImageryOffsetTools.getServerURL() + query);
            System.out.println("url=" + url);
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

    @Override
    protected void cancel() {
        cancelled = true;
    }

    @Override
    protected void finish() {
        if( errorMessage != null ) {
            JOptionPane.showMessageDialog(Main.parent, errorMessage, tr("Imagery Offset"), JOptionPane.ERROR_MESSAGE);
        }
    }

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

    public static class UploadException extends Exception {
        public UploadException( String message ) {
            super(message);
        }
    }
}
