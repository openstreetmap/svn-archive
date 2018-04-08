// License: GPL. For details, see LICENSE file.
package ru.rodsoft.openstreetmap.josm.plugins.customizepublictransportstop;

/**
 * Interface of operation of stop area customizing
 * 
 * @author Rodion Scherbakov
 */
public interface IStopAreaCustomizer {
    /**
     * Perform operation of customizing of stop area
     * 
     * @param stopArea Stop area
     * @return Stop area after customizing
     */
    StopArea performCustomizing(StopArea stopArea);
}
