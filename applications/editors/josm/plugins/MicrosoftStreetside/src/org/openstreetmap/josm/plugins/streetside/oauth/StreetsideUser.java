// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.streetside.oauth;


import java.util.Map;

import org.openstreetmap.josm.plugins.streetside.utils.StreetsideProperties;

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
   // users are not currently supported in Streetside
   return null;
}

/**
* @return A HashMap object containing the images_policy and images_hash
*         strings.
*/
public static synchronized Map<String, String> getSecrets() {
  // secrets are not currently supported in Streetside
  return null;
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
