// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.oauth;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.tools.I18n;

/**
* Listens to the OAuth port (8763) in order to get the access token and sends
* back a simple reply.
*
* @author nokutu
*
*/
public class OAuthPortListener extends Thread {
public static final int PORT = 8763;

final static Logger logger = Logger.getLogger(OAuthPortListener.class);

protected static final String RESPONSE = String.format(
   "<!DOCTYPE html><html><head><meta charset=\"utf8\"><title>%s</title></head><body>%s</body></html>",
   I18n.tr("Streetside login"),
   I18n.tr("Login successful, return to JOSM.")
);
private final StreetsideLoginListener callback;

public OAuthPortListener(StreetsideLoginListener loginCallback) {
 callback = loginCallback;
}

@Override
public void run() {
 try (
     ServerSocket serverSocket = new ServerSocket(PORT);
     Socket clientSocket = serverSocket.accept();
     PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
     Scanner in = new Scanner(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"))
 ) {
   String s;
   String accessToken = null;
   while (in.hasNextLine()) {
     s = in.nextLine();
     Matcher tokenMatcher = Pattern.compile("^.*&access_token=([^&]+)&.*$").matcher('&'+s+'&');
     if (tokenMatcher.matches()) {
       accessToken = tokenMatcher.group(1);
       break;
     } else if (s.contains("keep-alive")) {
       break;
     }
   }

   writeContent(out);
   out.flush();

   StreetsideUser.reset();

   logger.info(I18n.tr("Successful login with Streetside, the access token is: {0}", accessToken));
   // Saves the access token in preferences.
   StreetsideUser.setTokenValid(true);
   if (Main.main != null) {
     StreetsideProperties.ACCESS_TOKEN.put(accessToken);
     String username = StreetsideUser.getUsername();
     logger.info(I18n.tr("The username is: {0}", username));
     if (callback != null) {
       callback.onLogin(username);
     }
   }
 } catch (BindException e) {
   logger.warn(e);
 } catch (IOException e) {
   logger.error(e);
 }
}

private static void writeContent(PrintWriter out) {
 out.println("HTTP/1.1 200 OK");
 out.println("Content-Length: " + RESPONSE.length());
 out.println("Content-Type: text/html" + "\r\n\r\n");
 out.println(RESPONSE);
}
}
