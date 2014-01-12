package org.openstreetmap.josm.plugins.turnrestrictions.editor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ExceptValueModel is a model for the value of the tag 'except' in a turn
 * restriction. 
 *
 */
public class ExceptValueModel {
    /**
     * The set of standard vehicle types which can be used in the
     * 'except' tag 
     */
    static public final Set<String> STANDARD_VEHICLE_EXCEPTION_VALUES;
    static {
        HashSet<String> s = new HashSet<String>();
        s.add("psv");
        s.add("hgv");
        s.add("bicycle");
        s.add("motorcar");
        STANDARD_VEHICLE_EXCEPTION_VALUES = Collections.unmodifiableSet(s);
    }
    
    /**
     * Replies true, if {@code v} is a standard vehicle type. Replies
     * false if {@code v} is null
     * 
     * @param v the vehicle type. 
     * @return true, if {@code v} is a standard vehicle type.
     */
    static public boolean isStandardVehicleExceptionValue(String v){
        if (v == null) return false;
        v = v.trim().toLowerCase();
        return STANDARD_VEHICLE_EXCEPTION_VALUES.contains(v);
    }
        
    private String value = "";
    private boolean isStandard = true;
    private final Set<String> vehicleExceptions = new HashSet<String>();
    
    protected void parseValue(String value) {
        if (value == null || value.trim().equals("")) value = "";
        this.value = value;
        isStandard = true;
        vehicleExceptions.clear();
        if (value.equals("")) return;
        String[] values = value.split(";");
        for (String v: values){
            v = v.trim().toLowerCase();
            if (isStandardVehicleExceptionValue(v)) {
                vehicleExceptions.add(v);
            } else {
                isStandard = false;
            }
        }
    }
    
    /**
     * Creates a new model for an empty standard value 
     */
    public ExceptValueModel() {}
    
    /**
     * Creates a new model for the tag value {@code value}. 
     * 
     * @param value the tag value
     * @see #parseValue(String)
     */
    public ExceptValueModel(String value){
        if (value == null || value.trim().equals("")) 
            return;
        parseValue(value);
    }

    /**
     * Replies the tag value representing the state of this model.
     * 
     * @return 
     */
    public String getValue() {
        if (isStandard){
            StringBuffer sb = new StringBuffer();
            // we use an ordered list because equals()
            // is based on getValue()
            //
            List<String> values = new ArrayList<String>(vehicleExceptions);
            Collections.sort(values);
            for (String v: values){
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(v);
            }
            return sb.toString();
        } else {
            return value;
        }
    }

    /**
     * Sets the value in this model
     * 
     * @param value
     */
    public void setValue(String value) {
        parseValue(value);
    }

    /**
     * Replies true if this model currently holds a standard 'except' value
     * 
     * @return
     */
    public boolean isStandard() {
        return isStandard;
    }   
    
    /**
     * Tells this model to use standard values only.
     * 
     */
    public void setStandard(boolean isStandard) {
        this.isStandard = isStandard;
    }
    
    /**
     * Replies true if {@code vehicleType} is currently set as exception in this
     * model.
     * 
     * @param vehicleType one of the standard vehicle types from {@see #STANDARD_VEHICLE_EXCEPTION_VALUES}
     * @return true if {@code vehicleType} is currently set as exception in this
     * model.
     * @exception IllegalArgumentException thrown if {@code vehicleType} isn't a standard vehicle type 
     */
    public boolean isVehicleException(String vehicleType) throws IllegalArgumentException{
        if (vehicleType == null) return false;
        if (!isStandardVehicleExceptionValue(vehicleType)) {
            throw new IllegalArgumentException(MessageFormat.format("vehicleType ''{0}'' isn''t a valid standard vehicle type", vehicleType));
        }
        vehicleType = vehicleType.trim().toLowerCase();
        return vehicleExceptions.contains(vehicleType);
    }
    
    /**
     * Sets the {@code vehicleType} as exception in this turn restriction.
     * 
     * @param vehicleType one of the standard vehicle types from {@see #STANDARD_VEHICLE_EXCEPTION_VALUES}
     * @exception IllegalArgumentException thrown if {@code vehicleType} isn't a standard vehicle type 
     */
    public void setVehicleException(String vehicleType) throws IllegalArgumentException{
        if (!isStandardVehicleExceptionValue(vehicleType)) {
            throw new IllegalArgumentException(MessageFormat.format("vehicleType ''{0}'' isn''t a valid standard vehicle type", vehicleType));
        }
        vehicleExceptions.add(vehicleType.trim().toLowerCase());
    }
    

    /**
     * Sets or removes the {@code vehicleType} as exception in this turn restriction, depending
     * on whether {@code setOrRemove} is true or false, respectively.
     * 
     * @param vehicleType one of the standard vehicle types from {@see #STANDARD_VEHICLE_EXCEPTION_VALUES}
     * @param setOrRemove if true, the exception is set; otherwise, it is removed
     * @exception IllegalArgumentException thrown if {@code vehicleType} isn't a standard vehicle type 
     */
    public void setVehicleException(String vehicleType, boolean setOrRemove) throws IllegalArgumentException{
        if (setOrRemove){
            setVehicleException(vehicleType);
        } else {
            removeVehicleException(vehicleType);
        }
    }
    
    /**
     * Removes the {@code vehicleType} as exception in this turn restriction
     * 
     * @param vehicleType one of the standard vehicle types from {@see #STANDARD_VEHICLE_EXCEPTION_VALUES}
     * @exception IllegalArgumentException thrown if {@code vehicleType} isn't a standard vehicle type 
     */
    public void removeVehicleException(String vehicleType) throws IllegalArgumentException{
        if (!isStandardVehicleExceptionValue(vehicleType)) {
            throw new IllegalArgumentException(MessageFormat.format("vehicleType ''{0}'' isn''t a valid standard vehicle type", vehicleType));
        }
        vehicleExceptions.remove(vehicleType.trim().toLowerCase());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getValue().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExceptValueModel other = (ExceptValueModel) obj;
        return getValue().equals(other.getValue());
    }       
}
