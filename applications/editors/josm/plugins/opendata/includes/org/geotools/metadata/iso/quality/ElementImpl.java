/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.quality;

import java.util.Collection;
import java.util.Collections;

import org.geotools.metadata.iso.MetadataEntity;
import org.opengis.metadata.quality.Element;
import org.opengis.metadata.quality.EvaluationMethodType;
import org.opengis.metadata.quality.Result;
import org.opengis.util.InternationalString;


/**
 * Type of test applied to the data specified by a data quality scope.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/metadata/iso/quality/ElementImpl.java $
 * @version $Id: ElementImpl.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux (IRD)
 * @author Touraïvane
 *
 * @since 2.1
 */
public class ElementImpl extends MetadataEntity implements Element {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3542504624077298894L;

    /**
     * Description of the measure being determined.
     */
    private InternationalString measureDescription;

    /**
     * Type of method used to evaluate quality of the dataset, or {@code null} if unspecified.
     */
    private EvaluationMethodType evaluationMethodType;

    /**
     * Description of the evaluation method.
     */
    private InternationalString evaluationMethodDescription;

    /**
     * Value (or set of values) obtained from applying a data quality measure or the out
     * come of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     */
    private Collection<Result> results;

    /**
     * Constructs an initially empty element.
     */
    public ElementImpl() {
    }

    /**
     * Creates an element initialized to the given result.
     */
    public ElementImpl(final Result result) {
        setResults(Collections.singleton(result));
    }

    /**
     * Returns the description of the measure being determined.
     */
    public InternationalString getMeasureDescription() {
        return measureDescription;
    }

    /**
     * Set the description of the measure being determined.
     */
    public synchronized void setMeasureDescription(final InternationalString newValue)  {
        checkWritePermission();
        measureDescription = newValue;
    }

    /**
     * Returns the type of method used to evaluate quality of the dataset,
     * or {@code null} if unspecified.
     */
    public EvaluationMethodType getEvaluationMethodType() {
        return evaluationMethodType;
    }

    /**
     * Set the ype of method used to evaluate quality of the dataset.
     */
    public synchronized void setEvaluationMethodType(final EvaluationMethodType newValue)  {
        checkWritePermission();
        evaluationMethodType = newValue;
    }

    /**
     * Returns the description of the evaluation method.
     */
    public InternationalString getEvaluationMethodDescription() {
        return evaluationMethodDescription;
    }

    /**
     * Set the description of the evaluation method.
     */
    public synchronized void setEvaluationMethodDescription(final InternationalString newValue)  {
        checkWritePermission();
        evaluationMethodDescription = newValue;
    }

    /**
     * Returns the value (or set of values) obtained from applying a data quality measure or
     * the out come of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     *
     * @since 2.4
     */
    public synchronized Collection<Result> getResults() {
        return results = nonNullCollection(results, Result.class);
    }

    /**
     * Set the value (or set of values) obtained from applying a data quality measure or
     * the out come of evaluating the obtained value (or set of values) against a specified
     * acceptable conformance quality level.
     *
     * @since 2.4
     */
    public synchronized void setResults(final Collection<? extends Result> newValues) {
        results = copyCollection(newValues, results, Result.class);
    }
}
