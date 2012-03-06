/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.util;

import org.opengis.annotation.Extension;


/**
 * Monitor the progress of some lengthly operation, and allows cancelation.
 * This interface makes no assumption about the output device. Additionnaly, this
 * interface provides support for non-fatal warning and exception reports.
 * <p>
 * All implementations should be multi-thread safe, even the ones that provide
 * feedback to a user interface thread.
 * <p>
 * Usage example:
 * <blockquote><pre>
 * float scale = 100f / maximumCount;
 * listener.started();
 * for (int counter=0; counter&lt;maximumCount; counter++) {
 *     if (listener.isCanceled()) {
 *         break;
 *     }
 *     listener.progress(scale * counter);
 *     try {
 *         // Do some work...
 *     } catch (NonFatalException e) {
 *         listener.exceptionOccurred(e);
 *     }
 * }
 * listener.complete();
 * </pre></blockquote>
 *
 * @since  GeoAPI 2.1
 * @author Martin Desruisseaux
 * @author Jody Garnet
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/opengis/src/main/java/org/opengis/util/ProgressListener.java $
 */
@Extension
public interface ProgressListener {

    /**
     * Notifies this listener that the operation begins.
     */
    void started();

    /**
     * Notifies this listener of progress in the lengthly operation. Progress are reported
     * as a value between 0 and 100 inclusive. Values out of bounds will be clamped.
     *
     * @param percent The progress as a value between 0 and 100 inclusive.
     *
     * @todo Should be renamed setProgress(float) for consistency with getProgress().
     */
    void progress(float percent);

    /**
     * Notifies this listener that the operation has finished. The progress indicator will
     * shows 100% or disappears, at implementor choice. If warning messages were pending,
     * they will be displayed now.
     */
    void complete();

    /**
     * Returns {@code true} if this job is cancelled.
     *
     * @return {@code true} if this job is cancelled.
     */
    boolean isCanceled();

    /**
     * Reports an exception. This method may prints the stack trace to the {@linkplain System#err
     * standard error stream} or display it in a dialog box, at implementor choice.
     *
     * @param exception The exception to report.
     */
    void exceptionOccurred(Throwable exception);
}
