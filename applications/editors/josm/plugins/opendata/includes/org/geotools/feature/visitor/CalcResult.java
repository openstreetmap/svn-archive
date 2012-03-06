/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.feature.visitor;



/**
 * Encapsulates the results from a FeatureCalc, and includes methods for
 * obtaining and merging results.
 *
 * @author Cory Horner, Refractions
 *
 * @see FeatureCalc
 * @since 2.2.M2
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/main/src/main/java/org/geotools/feature/visitor/CalcResult.java $
 */
public interface CalcResult {
	/**
	 * The result obtained when a FeatureCalc found no features to visit.
	 * It lets itself merge with any other result by just returning the other result
	 * as the output of the merge
	 */
	public static final CalcResult NULL_RESULT = new AbstractCalcResult() {
		/**
		 * Always compatible
		 */
		public boolean isCompatible(CalcResult targetResults) {
			return true;
		};
		
		/**
		 * Just returns the other result
		 */
		public CalcResult merge(CalcResult resultsToAdd) {
			return resultsToAdd;
		};
	};
	
    /**
     * Returns true if the target results is a compatible type with the current
     * results, with compatible meaning that the two results may be merged.
     *
     * @param targetResults the second CalcResult Object
     *
     * @return true if the targetResults can be merged with the current results
     */
    public boolean isCompatible(CalcResult targetResults);

    /**
     * Actual answer
     *
     * @return the calculation result as a generic object
     */
    public Object getValue();

    /**
     * Access getValue as a string
     *
     * @return the calculation result as a string (or "" if not applicable)
     */
    public String toString();
}
