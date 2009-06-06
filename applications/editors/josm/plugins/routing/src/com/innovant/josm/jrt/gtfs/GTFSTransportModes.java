package com.innovant.josm.jrt.gtfs;

/**
 * Constants for parsing GTFS and to use in Routing Profiles
 * @author juangui
 * TODO Using integers is suitable to parse gtfs feeds but
 * Routing Profile keys should be Strings
 */
public class GTFSTransportModes {

    /**
     * 0 - Tram, Streetcar, Light rail. Any light rail or street level system within
     *     a metropolitan area.
     */
    public static final int TRAM = 0;
    public static final int STREETCAR = 0;
    public static final int LIGHT_RAIL = 0;

    /**
     * 1 - Subway, Metro. Any underground rail system within a metropolitan area.
     */
    public static final int SUBWAY = 1;
    public static final int METRO = 1;

    /**
     * 2 - Rail. Used for intercity or long-distance travel.
     */
    public static final int RAIL = 2;

    /**
     * 3 - Bus. Used for short- and long-distance bus routes.
     */
    public static final int BUS = 3;

    /**
     * 4 - Ferry. Used for short- and long-distance boat service.
     */
    public static final int FERRY = 4;

    /**
     * 5 - Cable car. Used for street-level cable cars where the cable runs beneath the car.
     */
    public static final int CABLE_CAR = 5;

    /**
     * 6 - Gondola, Suspended cable car. Typically used for aerial cable cars where
     *     the car is suspended from the cable.
     */
    public static final int GONDOLA = 6;
    public static final int SUSPENDED_CABLE_CAR = 6;

    /**
     * 7 - Funicular. Any rail system designed for steep inclines.
     */
    public static final int FUNICULAR = 7;

}
