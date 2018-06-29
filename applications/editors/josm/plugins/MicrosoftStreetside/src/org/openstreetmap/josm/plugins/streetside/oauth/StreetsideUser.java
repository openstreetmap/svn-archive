// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.oauth;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;
import org.openstreetmap.josm.plugins.streetside.utils.StreetsideURL;
import org.openstreetmap.josm.tools.Logging;

/**
* Represents the current logged in user and stores its data.
*
* @author nokutu
*
*/
public final class StreetsideUser {

private static String username;
private static String imagesPolicy;
private static String imagesHash;
/** If the stored token is valid or not. */
private static boolean isTokenValid = true;

private StreetsideUser() {
 // Private constructor to avoid instantiation
}

/**
* @return The username of the logged in user.
*/
public static synchronized String getUsername() {
 //if (!isTokenValid) {
   return null;
 //}
 /*if (username == null) {
   try {
     username = OAuthUtils
         .getWithHeader(StreetsideURL.APIv3.userURL())
         .getString("username");
   } catch (IOException e) {
     logger.warn(I18n.tr("Invalid Streetside token, resetting field", e));
     reset();
   }
 }
 return username;*/
}

/**
* @return A HashMap object containing the images_policy and images_hash
*         strings.
*/
public static synchronized Map<String, String> getSecrets() {
 //if (!isTokenValid)
   return null;
 /*Map<String, String> hash = new HashMap<>();
 try {
   if (imagesHash == null)
     imagesHash = OAuthUtils
         .getWithHeader(StreetsideURL.uploadSecretsURL())
         .getString("images_hash", null);
   hash.put("images_hash", imagesHash);
   if (imagesPolicy == null)
     imagesPolicy = OAuthUtils
         .getWithHeader(StreetsideURL.uploadSecretsURL())
         .getString("images_policy");
 } catch (IOException e) {
   logger.warn(I18n.tr("Invalid Streetside token, resetting field", e));
   reset();
 }
 hash.put("images_policy", imagesPolicy);
 return hash;*/
}

/**
* Resets the MapillaryUser to null values.
*/
public static synchronized void reset() {
 username = null;
 imagesPolicy = null;
 imagesHash = null;
 isTokenValid = false;
 StreetsideProperties.ACCESS_TOKEN.put(StreetsideProperties.ACCESS_TOKEN.getDefaultValue());
}

public static synchronized void setTokenValid(boolean value) {
 isTokenValid = value;
}
}
