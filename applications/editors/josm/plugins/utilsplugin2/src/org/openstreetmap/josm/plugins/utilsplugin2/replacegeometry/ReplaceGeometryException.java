// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.utilsplugin2.replacegeometry;

/**
 * Exception thrown when replace geometry fails.
 * @author joshdoe
 */
public class ReplaceGeometryException extends RuntimeException {
    public ReplaceGeometryException(String message) {
        super(message);
    }
}
