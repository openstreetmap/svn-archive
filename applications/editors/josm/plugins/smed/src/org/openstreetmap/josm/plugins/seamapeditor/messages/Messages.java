// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.seamapeditor.messages;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Messages {
    private static final String BUNDLE_NAME = "resources/msg.messages";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
