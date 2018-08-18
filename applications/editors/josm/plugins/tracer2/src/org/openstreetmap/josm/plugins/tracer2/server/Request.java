// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.tracer2.server;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.HttpClient;

public class Request extends Thread {

    static final String URL = "http://localhost:49243/";

    public Request() {
    }

    /**
     * Send request to the server.
     * @param strUrl request.
     * @return Result text.
     */
    protected String callServer(String strUrl) {
        try {
            return HttpClient.create(new URL(URL + strUrl)).connect().fetchContent();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    tr("Tracer2Server isn''t running. Please start the Server.\nIf you don''t have the server, please download it from\n{0}.",
                            "http://sourceforge.net/projects/tracer2server/"), tr("Error"), JOptionPane.ERROR_MESSAGE);
            return "";
        } catch (Exception e) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Tracer2Server hasn''t found anything.") + "\n",
                    tr("Error"), JOptionPane.ERROR_MESSAGE);
            return "";
        }
    }

    /**
     * Checks errors in response from the server.
     * @param strResponse response from the server.
     * @return Result text.
     */
    protected boolean checkError(String strResponse) {
        String strIdentifier = "&traceError=";
        if (strResponse.contains(strIdentifier)) {
            String strError = strResponse.replaceFirst(strIdentifier, "").trim();
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("Tracer2Server has detected an error.") + "\n" + strError,
                    tr("Error"), JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }

    @Override
    public void run() {
    }
}
