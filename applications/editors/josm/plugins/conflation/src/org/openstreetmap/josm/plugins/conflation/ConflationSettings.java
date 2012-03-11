/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstreetmap.josm.plugins.conflation;

import java.util.ArrayList;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 *
 * @author Josh
 */
public class ConflationSettings {
    private ArrayList<OsmPrimitive> subjectSelection;
    private ArrayList<OsmPrimitive> referenceSelection;
    private OsmDataLayer referenceLayer;
    private DataSet subjectDataSet;
    private OsmDataLayer subjectLayer;
    private DataSet referenceDataSet;
    
    public double distanceWeight;
    public double distanceCutoff;
    public String keyString;
    public double stringWeight;
    public double stringCutoff;

    /**
     * @return the subjectSelection
     */
    public ArrayList<OsmPrimitive> getSubjectSelection() {
        return subjectSelection;
    }

    /**
     * @param subjectSelection the subjectSelection to set
     */
    public void setSubjectSelection(ArrayList<OsmPrimitive> subjectSelection) {
        this.subjectSelection = subjectSelection;
    }

    /**
     * @return the referenceSelection
     */
    public ArrayList<OsmPrimitive> getReferenceSelection() {
        return referenceSelection;
    }

    /**
     * @param referenceSelection the referenceSelection to set
     */
    public void setReferenceSelection(ArrayList<OsmPrimitive> referenceSelection) {
        this.referenceSelection = referenceSelection;
    }

    /**
     * @return the referenceLayer
     */
    public OsmDataLayer getReferenceLayer() {
        return referenceLayer;
    }

    /**
     * @param referenceLayer the referenceLayer to set
     */
    public void setReferenceLayer(OsmDataLayer referenceLayer) {
        this.referenceLayer = referenceLayer;
    }

    /**
     * @return the subjectDataSet
     */
    public DataSet getSubjectDataSet() {
        return subjectDataSet;
    }

    /**
     * @param subjectDataSet the subjectDataSet to set
     */
    public void setSubjectDataSet(DataSet subjectDataSet) {
        this.subjectDataSet = subjectDataSet;
    }

    /**
     * @return the subjectLayer
     */
    public OsmDataLayer getSubjectLayer() {
        return subjectLayer;
    }

    /**
     * @param subjectLayer the subjectLayer to set
     */
    public void setSubjectLayer(OsmDataLayer subjectLayer) {
        this.subjectLayer = subjectLayer;
    }

    /**
     * @return the referenceDataSet
     */
    public DataSet getReferenceDataSet() {
        return referenceDataSet;
    }

    /**
     * @param referenceDataSet the referenceDataSet to set
     */
    public void setReferenceDataSet(DataSet referenceDataSet) {
        this.referenceDataSet = referenceDataSet;
    }
}
