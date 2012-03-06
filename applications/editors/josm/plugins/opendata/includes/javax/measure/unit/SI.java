/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.unit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.measure.converter.RationalConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;

/**
 * <p> This class contains SI (Système International d'Unités) base units,
 *     and derived units.</p>
 *     
 * <p> It also defines the 20 SI prefixes used to form decimal multiples and
 *     submultiples of SI units. For example:[code]
 *     import static org.jscience.physics.units.SI.*; // Static import.
 *     ...
 *     Unit<Pressure> HECTO_PASCAL = HECTO(PASCAL);
 *     Unit<Length> KILO_METER = KILO(METER);
 *     [/code]</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, August 26, 2006
 * @see <a href="http://en.wikipedia.org/wiki/SI">Wikipedia: SI</a>
 * @see <a href="http://en.wikipedia.org/wiki/SI_prefix">Wikipedia: SI prefix</a>
 */
public final class SI extends SystemOfUnits {

    /**
     * Holds collection of SI units.
     */
    private static HashSet<Unit<?>> UNITS = new HashSet<Unit<?>>();

    /**
     * Default constructor (prevents this class from being instantiated).
     */
    private SI() {
    }

    ////////////////
    // BASE UNITS //
    ////////////////

    /**
     * The base unit for length quantities (<code>m</code>).
     * One meter was redefined in 1983 as the distance traveled by light in
     * a vacuum in 1/299,792,458 of a second.
     */
    public static final BaseUnit<Length> METRE = si(new BaseUnit<Length>("m"));

    /**
     * Equivalent to {@link #METRE} (American spelling).
     */
    public static final Unit<Length> METER = METRE;

    /**
     * The base unit for duration quantities (<code>s</code>).
     * It is defined as the duration of 9,192,631,770 cycles of radiation
     * corresponding to the transition between two hyperfine levels of
     * the ground state of cesium (1967 Standard).
     */
    public static final BaseUnit<Duration> SECOND = si(new BaseUnit<Duration>(
            "s"));

    ////////////////////////////////
    // SI DERIVED ALTERNATE UNITS //
    ////////////////////////////////

    /**
     * The unit for plane angle quantities (<code>rad</code>).
     * One radian is the angle between two radii of a circle such that the
     * length of the arc between them is equal to the radius.
     */
    public static final AlternateUnit<Angle> RADIAN = si(new AlternateUnit<Angle>(
            "rad", Unit.ONE));

    /////////////////
    // SI PREFIXES //
    /////////////////

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>-3</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e-3)</code>.
     */
    public static <Q extends Quantity> Unit<Q> MILLI(Unit<Q> unit) {
        return unit.transform(Em3);
    }

    /////////////////////
    // Collection View //
    /////////////////////

    /**
     * Returns a read only view over theunits defined in this class.
     *
     * @return the collection of SI units.
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
    private static <U extends Unit<?>> U si(U unit) {
        UNITS.add(unit);
        return unit;
    }

    // Holds prefix converters (optimization).

    static final RationalConverter Em3 = new RationalConverter(1, 1000L);
}