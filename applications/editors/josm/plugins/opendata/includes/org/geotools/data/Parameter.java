/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data;

import java.util.Collections;
import java.util.Map;

import org.geotools.referencing.CRS;
import org.opengis.util.InternationalString;

/**
 * A Parameter defines information about a valid process parameter.
 *
 * @author gdavis
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/api/src/main/java/org/geotools/data/Parameter.java $
 */
public class Parameter<T> {
    /**
     * This is the key (ie machine readable text) used to represent this parameter in a
     * java.util.Map.
     * 
     * @param key (or machine readable name) for this parameter.
     */
    public final String key;
    
    /**
     * Human readable description of this parameter.
     */
    public final InternationalString description;
    
    /**
     * Class binding for this parameter.
     * <p>
     * When a value is supplied for this key it should be of the provided type.
     */
    public final Class<T> type;
    
    /** Can the value be missing? Or is null allowed...
     *@return true if a value is required to be both present and non null
     **/
    public final boolean required;
    
    /**
     * Hints for the user interface
     */
    
    /**
     * File extension expected - "shp", "jpg", etc...
     */
    public static final String EXT = "ext";
    
    /**
     * Level or Category of the parameter - "user", "advanced", "program"
     * <p>
     * <ul>
     * <li>user - should be shown to all users and is used every time.<br>
     *     example: user name and password
     * </li>
     * <li>advanced - advanced or expert parameter used in special cases<br>
     *     example: choice between get and post requests for WFS
     * </li>
     * <li>program - intended for programs often tweaking settings for performance<br>
     *     example: JDBC datasource for which it is hard for a user to type in
     * </li>
     * </ul>
     */
    public static final String LEVEL = "level";
    
    /**
     * Refinement of type; such as the FeatureType of a FeatureCollection, or component type of a List.
     * <p>
     * This information is supplied (along with type) to allow a process implementor communicate
     * additional restrictions on the allowed value beyond the strict type.
     * <p>
     * The following keys are understood at this time: LENGTH, FEATURE_TYPE, CRS, ELEMENT
     * .. additional keys will be documented as static final fields over time.
     * <p>
     * Any restrictions mentioned here should be mentioned as part of your
     * parameter description. This metadata is only used to help restrict what
     * the user enters; not all client application will understand and respect
     * these keys - please communicate with your end-user.
     * 
     * @see CRS
     * @see ELEMENT
     * @see FEATURE_TYPE
     * @see IS_PASSWORD
     * @see LENGTH
     * @see MAX
     * @see MIN
     */
    public final Map<String, Object> metadata;

    /**
     * Addition of optional parameters
     * @param key machine readable key for use in a java.util.Map
     * @param type Java class for the expected value
     * @param title Human readable title used for use in a user interface
     * @param description Human readable description
     * @param required true if the value is required
     * @param min Minimum value; or null if not needed
     * @param max Maximum value; or null if not needed
     * @param sample Sample value; may be used as a default in a user interface
     * @param metadata Hints to the user interface (read the javadocs for each metadata key)
     * 
     * @see CRS
     * @see ELEMENT
     * @see FEATURE_TYPE
     * @see IS_PASSWORD
     * @see LENGTH
     * @see MAX
     * @see MIN
     */
    public Parameter(String key, Class<T> type, InternationalString title,
    				 InternationalString description,
                     boolean required, int min, int max, Object sample, 
                     Map<String,Object> metadata) {
        this.key = key;
        this.type = type;
        this.description = description;
        this.required = required;
        this.metadata = metadata == null ? null : Collections.unmodifiableMap(metadata);
    }
}
