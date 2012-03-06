/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.unit;

import static javax.measure.unit.SI.RADIAN;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;

/**
 * <p> This class contains units that are not part of the International
 *     System of Units, that is, they are outside the SI, but are important
 *     and widely used.</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, August 26, 2007
 */
public final class NonSI extends SystemOfUnits {

    /**
     * Holds collection of NonSI units.
     */
    private static HashSet<Unit<?>> UNITS = new HashSet<Unit<?>>();

    /**
     * Default constructor (prevents this class from being instantiated).
     */
    private NonSI() {
    }

    //////////////
    // Duration //
    //////////////

    /**
     * A unit of duration equal to <code>60 s</code>
     * (standard name <code>min</code>).
     */
    public static final Unit<Duration> MINUTE = nonSI(SI.SECOND.times(60));

    /**
     * A unit of duration equal to <code>60 {@link #MINUTE}</code>
     * (standard name <code>h</code>).
     */
    public static final Unit<Duration> HOUR = nonSI(MINUTE.times(60));

    /**
     * A unit of duration equal to <code>24 {@link #HOUR}</code>
     * (standard name <code>d</code>).
     */
    public static final Unit<Duration> DAY = nonSI(HOUR.times(24));

    ///////////
    // Angle //
    ///////////

    /**
     * A unit of angle equal to a full circle or <code>2<i>&pi;</i> 
     * {@link SI#RADIAN}</code> (standard name <code>rev</code>).
     */
    public static final Unit<Angle> REVOLUTION = nonSI(RADIAN.times(2.0 * Math.PI));

    /**
     * A unit of angle equal to <code>1/360 {@link #REVOLUTION}</code>
     * (standard name <code>°</code>).
     */
    public static final Unit<Angle> DEGREE_ANGLE = nonSI(REVOLUTION.divide(360));

    /**
     * A unit of angle equal to <code>1/60 {@link #DEGREE_ANGLE}</code>
     * (standard name <code>′</code>).
     */
    public static final Unit<Angle> MINUTE_ANGLE = nonSI(DEGREE_ANGLE.divide(60));

    /**
     *  A unit of angle equal to <code>1/60 {@link #MINUTE_ANGLE}</code>
     * (standard name <code>"</code>).
     */
    public static final Unit<Angle> SECOND_ANGLE = nonSI(MINUTE_ANGLE.divide(60));

    /////////////////////
    // Collection View //
    /////////////////////
    
    /**
     * Returns a read only view over the units defined in this class.
     *
     * @return the collection of NonSI units.
     */
    public Set<Unit<?>> getUnits() {
        return Collections.unmodifiableSet(UNITS);
    }

    /**
     * Adds a new unit to the collection.
     *
     * @param  unit the unit being added.
     * @return <code>unit</code>.
     */
    private static <U extends Unit<?>> U nonSI(U unit) {
        UNITS.add(unit);
        return unit;
    }

}