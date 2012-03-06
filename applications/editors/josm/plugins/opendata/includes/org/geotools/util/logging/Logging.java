/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.util.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.geotools.resources.Classes;


/**
 * A set of utilities method for configuring loggings in GeoTools. <strong>All GeoTools
 * code should fetch their logger through a call to {@link #getLogger(String)}</strong>,
 * not {@link Logger#getLogger(String)}. This is necessary in order to give GeoTools a
 * chance to redirect log events to an other logging framework, for example
 * <A HREF="http://jakarta.apache.org/commons/logging/">commons-logging</A>.
 * <p>
 * <b>Example:</b> In order to redirect every GeoTools log events to Commons-logging,
 * invoke the following once at application startup:
 *
 * <blockquote><code>
 * Logging.{@linkplain #GEOTOOLS}.{@linkplain #setLoggerFactory
 * setLoggerFactory}("org.geotools.util.logging.CommonsLoggerFactory");
 * </code></blockquote>
 *
 * @since 2.4
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/library/metadata/src/main/java/org/geotools/util/logging/Logging.java $
 * @version $Id: Logging.java 37298 2011-05-25 05:16:15Z mbedward $
 * @author Martin Desruisseaux
 */
public final class Logging {

    /**
     * The name of the base package.
     */
    final String name;

    /**
     * Creates an instance for the root logger. This constructor should not be used
     * for anything else than {@link #ALL} construction; use {@link #getLogging} instead.
     */
    private Logging() {
        name = "";
    }

    /**
     * Creates an instance for the specified base logger. This constructor
     * should not be public; use {@link #getLogging} instead.
     *
     * @param parent The parent {@code Logging} instance.
     * @param name   The logger name for the new instance.
     */
    private Logging(final Logging parent, final String name) {
        this.name = name;
        assert name.startsWith(parent.name) : name;
    }

    /**
     * Returns a logger for the specified class. This convenience method invokes
     * {@link #getLogger(String)} with the package name as the logger name.
     *
     * @param  classe The class for which to obtain a logger.
     * @return A logger for the specified class.
     *
     * @since 2.5
     */
    public static Logger getLogger(final Class<?> classe) {
        String name = classe.getName();
        final int separator = name.lastIndexOf('.');
        name = (separator >= 1) ? name.substring(0, separator) : "";
        return getLogger(name);
    }

    /**
     * Returns a logger for the specified name. If a {@linkplain LoggerFactory logger factory} has
     * been set, then this method first {@linkplain LoggerFactory#getLogger ask to the factory}.
     * It gives GeoTools a chance to redirect logging events to
     * <A HREF="http://jakarta.apache.org/commons/logging/">commons-logging</A>
     * or some equivalent framework.
     * <p>
     * If no factory was found or if the factory choose to not redirect the loggings, then this
     * method returns the usual <code>{@linkplain Logger#getLogger Logger.getLogger}(name)</code>.
     *
     * @param  name The logger name.
     * @return A logger for the specified name.
     */
    public static Logger getLogger(final String name) {
        return Logger.getLogger(name);
    }

    /**
     * Invoked when an unexpected error occurs. This method logs a message at the
     * {@link Level#WARNING WARNING} level to the specified logger. The originating
     * class name and method name are inferred from the error stack trace, using the
     * first {@linkplain StackTraceElement stack trace element} for which the class
     * name is inside a package or sub-package of the logger name. For example if
     * the logger name is {@code "org.geotools.image"}, then this method will uses
     * the first stack trace element where the fully qualified class name starts with
     * {@code "org.geotools.image"} or {@code "org.geotools.image.io"}, but not
     * {@code "org.geotools.imageio"}.
     *
     * @param  logger Where to log the error.
     * @param  error  The error that occured.
     * @return {@code true} if the error has been logged, or {@code false} if the logger
     *         doesn't log anything at the {@link Level#WARNING WARNING} level.
     */
    public static boolean unexpectedException(final Logger logger, final Throwable error) {
        return unexpectedException(logger, null, null, error, Level.WARNING);
    }

    /**
     * Invoked when an unexpected error occurs. This method logs a message at the
     * {@link Level#WARNING WARNING} level to the specified logger. The originating
     * class name and method name can optionnaly be specified. If any of them is
     * {@code null}, then it will be inferred from the error stack trace as in
     * {@link #unexpectedException(Logger, Throwable)}.
     * <p>
     * Explicit value for class and method names are sometime preferred to automatic
     * inference for the following reasons:
     *
     * <ul>
     *   <li><p>Automatic inference is not 100% reliable, since the Java Virtual Machine
     *       is free to omit stack frame in optimized code.</p></li>
     *   <li><p>When an exception occured in a private method used internally by a public
     *       method, we sometime want to log the warning for the public method instead,
     *       since the user is not expected to know anything about the existence of the
     *       private method. If a developper really want to know about the private method,
     *       the stack trace is still available anyway.</p></li>
     * </ul>
     *
     * @param logger  Where to log the error.
     * @param classe  The class where the error occurred, or {@code null}.
     * @param method  The method where the error occurred, or {@code null}.
     * @param error   The error.
     * @return {@code true} if the error has been logged, or {@code false} if the logger
     *         doesn't log anything at the {@link Level#WARNING WARNING} level.
     */
    public static boolean unexpectedException(final Logger logger, final Class<?> classe,
                                              final String method, final Throwable error)
    {
        final String classname = (classe != null) ? classe.getName() : null;
        return unexpectedException(logger, classname, method, error, Level.WARNING);
    }

    /**
     * Invoked when an unexpected error occurs. This method logs a message at the
     * {@link Level#WARNING WARNING} level to the logger for the specified package
     * name. The originating class name and method name can optionnaly be specified.
     * If any of them is {@code null}, then it will be inferred from the error stack
     * trace as in {@link #unexpectedException(Logger, Throwable)}.
     *
     * @param paquet  The package where the error occurred, or {@code null}. This
     *                information is used for fetching an appropriate {@link Logger}
     *                for logging the error.
     * @param classe  The class where the error occurred, or {@code null}.
     * @param method  The method where the error occurred, or {@code null}.
     * @param error   The error.
     * @return {@code true} if the error has been logged, or {@code false} if the logger
     *         doesn't log anything at the {@link Level#WARNING WARNING} level.
     *
     * @deprecated Use one of the other {@code unexpectedException} methods instead.
     */
    public static boolean unexpectedException(final String paquet, final Class<?> classe,
                                              final String method, final Throwable error)
    {
        final Logger logger = (paquet != null) ? getLogger(paquet) : null;
        return unexpectedException(logger, classe, method, error);
    }

    /**
     * Invoked when an unexpected error occurs. This method logs a message at the
     * {@link Level#WARNING WARNING} level to a logger inferred from the given class.
     *
     * @param classe  The class where the error occurred.
     * @param method  The method where the error occurred, or {@code null}.
     * @param error   The error.
     * @return {@code true} if the error has been logged, or {@code false} if the logger
     *         doesn't log anything at the {@link Level#WARNING WARNING} level.
     *
     * @since 2.5
     */
    public static boolean unexpectedException(Class<?> classe, String method, Throwable error) {
        return unexpectedException((Logger) null, classe, method, error);
    }

    /**
     * Implementation of {@link #unexpectedException(Logger, Class, String, Throwable)}.
     *
     * @param logger  Where to log the error, or {@code null}.
     * @param classe  The fully qualified class name where the error occurred, or {@code null}.
     * @param method  The method where the error occurred, or {@code null}.
     * @param error   The error.
     * @param level   The logging level.
     * @return {@code true} if the error has been logged, or {@code false} if the logger
     *         doesn't log anything at the specified level.
     */
    private static boolean unexpectedException(Logger logger, String classe, String method,
                                               final Throwable error, final Level level)
    {
        /*
         * Checks if loggable, inferring the logger from the classe name if needed.
         */
        if (error == null) {
            return false;
        }
        if (logger == null && classe != null) {
            final int separator = classe.lastIndexOf('.');
            final String paquet = (separator >= 1) ? classe.substring(0, separator-1) : "";
            logger = getLogger(paquet);
        }
        if (logger != null && !logger.isLoggable(level)) {
            return false;
        }
        /*
         * Loggeable, so complete the null argument from the stack trace if we can.
         */
        if (logger==null || classe==null || method==null) {
            String paquet = (logger != null) ? logger.getName() : null;
            final StackTraceElement[] elements = error.getStackTrace();
            for (int i=0; i<elements.length; i++) {
                /*
                 * Searchs for the first stack trace element with a classname matching the
                 * expected one. We compare preferably against the name of the class given
                 * in argument, or against the logger name (taken as the package name) otherwise.
                 */
                final StackTraceElement element = elements[i];
                final String classname = element.getClassName();
                if (classe != null) {
                    if (!classname.equals(classe)) {
                        continue;
                    }
                } else if (paquet != null) {
                    if (!classname.startsWith(paquet)) {
                        continue;
                    }
                    final int length = paquet.length();
                    if (classname.length() > length) {
                        // We expect '.' but we accept also '$' or end of string.
                        final char separator = classname.charAt(length);
                        if (Character.isJavaIdentifierPart(separator)) {
                            continue;
                        }
                    }
                }
                /*
                 * Now that we have a stack trace element from the expected class (or any
                 * element if we don't know the class), make sure that we have the right method.
                 */
                final String methodName = element.getMethodName();
                if (method != null && !methodName.equals(method)) {
                    continue;
                }
                /*
                 * Now computes every values that are null, and stop the loop.
                 */
                if (paquet == null) {
                    final int separator = classname.lastIndexOf('.');
                    paquet = (separator >= 1) ? classname.substring(0, separator-1) : "";
                    logger = getLogger(paquet);
                    if (!logger.isLoggable(level)) {
                        return false;
                    }
                }
                if (classe == null) {
                    classe = classname;
                }
                if (method == null) {
                    method = methodName;
                }
                break;
            }
            /*
             * The logger may stay null if we have been unable to find a suitable
             * stack trace. Fallback on the global logger.
             *
             * TODO: Use GLOBAL_LOGGER_NAME constant when we will be allowed to target Java 6.
             */
            if (logger == null) {
                logger = getLogger("global");
                if (!logger.isLoggable(level)) {
                    return false;
                }
            }
        }
        /*
         * Now prepare the log message. If we have been unable to figure out a source class and
         * method name, we will fallback on Java logging default mechanism, which may returns a
         * less relevant name than our attempt to use the logger name as the package name.
         */
        final StringBuilder buffer = new StringBuilder(Classes.getShortClassName(error));
        final String message = error.getLocalizedMessage();
        if (message != null) {
            buffer.append(": ").append(message);
        }
        final LogRecord record = new LogRecord(level, buffer.toString());
        if (classe != null) {
            record.setSourceClassName(classe);
        }
        if (method != null) {
            record.setSourceMethodName(method);
        }
        if (level.intValue() > 500) {
            record.setThrown(error);
        }
        record.setLoggerName(logger.getName());
        logger.log(record);
        return true;
    }

    /**
     * Invoked when a recoverable error occurs. This method is similar to
     * {@link #unexpectedException(Logger,Class,String,Throwable) unexpectedException}
     * except that it doesn't log the stack trace and uses a lower logging level.
     *
     * @param logger  Where to log the error.
     * @param classe  The class where the error occurred.
     * @param method  The method name where the error occurred.
     * @param error   The error.
     * @return {@code true} if the error has been logged, or {@code false} if the logger
     *         doesn't log anything at the specified level.
     *
     * @since 2.5
     */
    public static boolean recoverableException(final Logger logger, final Class<?> classe,
                                               final String method, final Throwable error)
    {
        final String classname = (classe != null) ? classe.getName() : null;
        return unexpectedException(logger, classname, method, error, Level.FINE);
    }
}
