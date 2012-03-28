package com.vividsolutions.jump.feature;

import java.util.*;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Subclasses need to implement only the four remaining Feature methods:
 * #getAttribute, #setAttribute, #getAttributes, #setAttributes
 */
public abstract class AbstractBasicFeature implements Feature {
    
    private FeatureSchema schema;
    private int id;
    /**
     * A low-level accessor that is not normally used.
     */    
    public void setSchema(FeatureSchema schema) {
        this.schema = schema;
    }            

    /**
     *  Creates a new Feature based on the given metadata.
     *
     *@param  featureSchema  the metadata containing information on
     *      each column
     */
    public AbstractBasicFeature(FeatureSchema featureSchema) {
        id = FeatureUtil.nextID();
        this.schema = featureSchema;
    }

    /**
     * Returns a number that uniquely identifies this feature. This number is not
     * persistent.
     * @return n, where this feature is the nth Feature created by this application
     */
    public int getID() {
        return id;
    }

    /**
     *  Sets the specified attribute.
     *
     *@param  attributeName  the name of the attribute to set
     *@param  newAttribute   the new attribute
     */
    public void setAttribute(String attributeName, Object newAttribute) {
        setAttribute(schema.getAttributeIndex(attributeName),
            newAttribute);
    }

    /**
     *  Convenience method for setting the spatial attribute. JUMP Workbench
     * PlugIns and CursorTools should not use this method directly, but should use an
     * EditTransaction, so that the proper events are fired.
     *
     *@param  geometry  the new spatial attribute
     */
    public void setGeometry(Geometry geometry) {
        setAttribute(schema.getGeometryIndex(), geometry);
    }

    /**
     *  Returns the specified attribute.
     *
     *@param  name  the name of the attribute to get
     *@return the attribute
     */
    public Object getAttribute(String name) {
        return getAttribute(schema.getAttributeIndex(name));
    }

    //<<TODO:DOC>>Update JavaDoc -- the attribute need not be a String [Jon Aquino]

    /**
     *  Returns a String attribute. The attribute at the given index must be a
     *  String.
     *
     *@param  attributeIndex  the array index of the attribute
     *@return                 the String attribute at the given index
     */
    public String getString(int attributeIndex) {
        // return (String) attributes[attributeIndex];
        //Dave B changed this so you can convert Integers->Strings
        //Automatic conversion of integers to strings is a bit hack-like.
        //Instead one should do #getAttribute followed by #toString.
        //#getString should be strict: it should throw an Exception if it is used
        //on a non-String attribute. [Jon Aquino]
        Object result = getAttribute(attributeIndex);

        //We used to eat ArrayOutOfBoundsExceptions here. I've removed this behaviour
        //because ArrayOutOfBoundsExceptions are bugs and should be exposed. [Jon Aquino]        

        //Is it valid for an attribute to be null? If not, we should put an
        //Assert here [Jon Aquino]
        if (result != null) {
            return result.toString();
        } else {
            return "";
        }
    }

    /**
     *  Returns a integer attribute.
     *
     *@param  attributeIndex the index of the attribute to retrieve
     *@return                the integer attribute with the given name
     */
    public int getInteger(int attributeIndex) {
        return ((Integer) getAttribute(attributeIndex)).intValue();
    }

    /**
     *  Returns a double attribute.
     *
     *@param  attributeIndex the index of the attribute to retrieve
     *@return                the double attribute with the given name
     */
    public double getDouble(int attributeIndex) {
        return ((Double) getAttribute(attributeIndex)).doubleValue();
    }

    //<<TODO:DOC>>Update JavaDoc -- the attribute need not be a String [Jon Aquino]

    /**
     *  Returns a String attribute. The attribute with the given name must be a
     *  String.
     *
     *@param  attributeName  the name of the attribute to retrieve
     *@return                the String attribute with the given name
     */
    public String getString(String attributeName) {
        return getString(schema.getAttributeIndex(attributeName));
    }

    /**
     *  Convenience method for returning the spatial attribute.
     *
     *@return    the feature's spatial attribute
     */
    public Geometry getGeometry() {
        return (Geometry) getAttribute(schema.getGeometryIndex());
    }

    /**
     *  Returns the feature's metadata
     *
     *@return    the metadata describing the names and types of the attributes
     */
    public FeatureSchema getSchema() {
        return schema;
    }

    /**
     * Clones this Feature. The geometry will also be cloned.
     * @return a new Feature with the same attributes as this Feature
     */
    public Object clone() {
        return clone(true);
    }

    /**
     * Clones this Feature.
     * @param deep whether or not to clone the geometry
     * @return a new Feature with the same attributes as this Feature
     */
    public Feature clone(boolean deep) {
        Feature clone = new BasicFeature(schema);

        for (int i = 0; i < schema.getAttributeCount(); i++) {
            if (schema.getAttributeType(i) == AttributeType.GEOMETRY) {
                clone.setAttribute(i,
                    deep ? getGeometry().clone() : getGeometry());
            } else {
                clone.setAttribute(i, getAttribute(i));
            }
        }

        return clone;
    }

    public int compareTo(Object o) {
        return getGeometry().compareTo(((Feature) o).getGeometry());
    }
}
