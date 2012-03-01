/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstreetmap.josm.plugins.conflation;

public interface ConflationListChangedListener {

    /**
     * Informs the listener that the conflation list has changed.
     *
     * @param list The new list.
     */
    public void conflationListChanged(ConflationCandidateList list);
}