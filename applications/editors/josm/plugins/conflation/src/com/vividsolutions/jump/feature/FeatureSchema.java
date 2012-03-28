
/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.vividsolutions.jump.feature;

import java.util.*;

import com.vividsolutions.jts.util.Assert;


public class FeatureSchema implements Cloneable {
    //<<TODO:QUESTION>> Is this an efficient implementation? Must cast the Integer to
    //an int. [Jon Aquino]
    private HashMap attributeNameToIndexMap = new HashMap();
    private int geometryIndex = -1;
    private int attributeCount = 0;
    private ArrayList attributeNames = new ArrayList();
    private ArrayList attributeTypes = new ArrayList();

    public FeatureSchema() {
    }

    /**
     *@param  attributeName              case-sensitive
     *@throws  IllegalArgumentException  if attributeName is unrecognized
     */
    public int getAttributeIndex(String attributeName) {
        //<<TODO:RECONSIDER>> Attribute names are currently case sensitive.
        //I wonder whether or not this is desirable. [Jon Aquino]
        Integer index = (Integer) attributeNameToIndexMap.get(attributeName);

        if (index == null) {
            throw new IllegalArgumentException("Unrecognized attribute name: " +
                attributeName);
        }

        return index.intValue();
    }

    /**
     * Returns whether this FeatureSchema has an attribute with this name
     * @param attributeName the name to look up
     * @return whether this FeatureSchema has an attribute with this name
     */
    public boolean hasAttribute(String attributeName) {
        return attributeNameToIndexMap.get(attributeName) != null;
    }

    /**
     *@return    the attribute index of the Geometry, or -1 if there is no
     *      Geometry attribute
     */
    public int getGeometryIndex() {
        return geometryIndex;
    }

    public String getAttributeName(int attributeIndex) {
        return (String) attributeNames.get(attributeIndex);
    }

    public AttributeType getAttributeType(int attributeIndex) {
        return (AttributeType) attributeTypes.get(attributeIndex);
    }

    public AttributeType getAttributeType(String attributeName) {
        return getAttributeType(getAttributeIndex(attributeName));
    }

    public int getAttributeCount() {
        return attributeCount;
    }

    /**
     *@param  geometry  true if the attribute is a Geometry
     */
    public void addAttribute(String attributeName, AttributeType attributeType) {
        if (AttributeType.GEOMETRY == attributeType) {
            //<<TODO:QUESTION>> Using Assert from JTS package ok? [Jon Aquino]
            Assert.isTrue(geometryIndex == -1);
            geometryIndex = attributeCount;
        }

        attributeNames.add(attributeName);
        attributeNameToIndexMap.put(attributeName, new Integer(attributeCount));
        attributeTypes.add(attributeType);
        attributeCount++;
    }

    /**
     *@return    true if the two metadatas have the same attribute names with the
     *      same types in the same order
     */
    public boolean equals(Object other) {
        return this.equals(other, false);
    }
    public boolean equals(Object other, boolean orderMatters) {
        if (!(other instanceof FeatureSchema)) {
            return false;
        }

        FeatureSchema otherFeatureSchema = (FeatureSchema) other;

        if (attributeNames.size() != otherFeatureSchema.attributeNames.size()) {
            return false;
        }

        for (int i = 0; i < attributeNames.size(); i++) {
            String attributeName = (String) attributeNames.get(i);
            
            if (!otherFeatureSchema.attributeNames.contains(attributeName)) {
                return false;
            }
            
            if (orderMatters && !otherFeatureSchema.attributeNames.get(i).equals(attributeName)) {
                return false;
            }

            if (getAttributeType(attributeName) != otherFeatureSchema.getAttributeType(
                        attributeName)) {
                return false;
            }
        }

        return true;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            Assert.shouldNeverReachHere();

            return null;
        }
    }
}
