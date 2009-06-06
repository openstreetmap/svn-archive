package com.innovant.josm.jrt.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.Main;


/**
 * This class holds information about a routing profile.
 *
 * A routing profile specifies the type of vehicle that will go through the route
 * and the conditions with respect to the traversal of different types of edges
 *
 * For instance, a pedestrian can traverse streets in both directions, walk through
 * pedestrian ways and almost all types of ways except motorways, climb steps, and
 * can ignore turn restrictions, while a handicapped person would have the same profile
 * except for climbing steps. A car can drive at the maximum allowed speed of the way,
 * and can not use cycleways nor pedestrian ways, while a bicycle can, but its maximum
 * speed for any type of way would be around 50km/h.
 *
 * When combined with public transit data, information of which types of transport modes
 * are allowed for the vehicle can be stored in the profile. For instance, bicycles are
 * usually allowed to travel on board of trains, trams and subways.
 *
 * @author juangui
 *
 */
public class RoutingProfile {
    /**
     * logger
     */
    static Logger logger = Logger.getLogger(RoutingProfile.class);
    /**
     * True if oneway is used for routing (i.e. for cars).
     */
    private boolean useOneway;

    /**
     * True if turn restrictions are used for routing (i.e. for cars).
     */
    private boolean useRestrictions;

    /**
     * True if maximum allowed speed of ways is considered for routing (i.e. for cars).
     */
    private boolean useMaxAllowedSpeed;

    /**
     * Name of the routing profile, for identification issues (i.e. "pedestrian").
     */
    private String name;

    /**
     * Holds traverse speed for each type of way, using the type as key.
     * A speed of zero means that this type of way cannot be traversed.
     */
    private Map<String,Double> waySpeeds;



    /**
     * Holds permission of use for each type of transport mode, using the mode as key.
     */
    private Map<String,Boolean> allowedModes;

    /**
     * Constructor
     * @param name The name for the routing profile. Please use a name that is
     * self descriptive, i.e., something that an application user would
     * understand (like "pedestrian", "motorbike", "bicycle", etc.)
     */
    public RoutingProfile(String name) {
        logger.debug("Init RoutingProfile with name: "+name);
        this.name = name;
        waySpeeds=new HashMap<String,Double>();
        Map<String,String> prefs=Main.pref.getAllPrefix("routing.profile."+name+".speed");
        for(String key:prefs.keySet()){
            waySpeeds.put((key.split("\\.")[4]), Double.valueOf(prefs.get(key)));
        }
        for (String key:waySpeeds.keySet())
            logger.debug(key+ "-- speed: "+waySpeeds.get(key));
        logger.debug("End init RoutingProfile with name: "+name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setOnewayUse(boolean useOneway) {
        this.useOneway = useOneway;
    }

    public boolean isOnewayUsed() {
        return useOneway;
    }

    public void setRestrictionsUse(boolean useRestrictions) {
        this.useRestrictions = useRestrictions;
    }

    public boolean isRestrictionsUsed() {
        return useRestrictions;
    }

    public void setMaxAllowedSpeedUse(boolean useMaxAllowedSpeed) {
        this.useMaxAllowedSpeed = useMaxAllowedSpeed;
    }

    public boolean isMaxAllowedSpeedUsed() {
        return useMaxAllowedSpeed;
    }

    public void setWayTypeSpeed(String type, double speed) {
        waySpeeds.put(type, speed);
    }

    public void setTransportModePermission(String mode, boolean permission) {
        allowedModes.put(mode, permission);
    }

    /**
     * Return whether the driving profile specifies that a particular type of way
     * can be traversed
     * @param type Key for the way type
     * @return True if the way type can be traversed
     */
    public boolean isWayTypeAllowed(String type) {
        if (waySpeeds.get(type) != 0.0)
            return true;
        return false;
    }

    /**
     * Return whether the driving profile specifies that a particular type of transport
     * mode can be used
     * @param mode Key for the way type
     * @return True if the way type can be traversed
     */
    public boolean isTransportModeAllowed(String mode) {
        return allowedModes.get(mode);
    }

    public double getSpeed(String key){
        if(!waySpeeds.containsKey(key)) return 0.0;
        return waySpeeds.get(key);
    }

    public Map<String, Double> getWaySpeeds() {
        return waySpeeds;
    }

    public void setWaySpeeds(Map<String, Double> waySpeeds) {
        this.waySpeeds = waySpeeds;
    }
}
