package org.openstreetmap.josm.plugins.graphview.plugin.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyType;
import org.openstreetmap.josm.plugins.graphview.core.property.VehiclePropertyTypes;
import org.openstreetmap.josm.plugins.graphview.core.util.ValueStringParser;

/**
 * utility class for interpreting Strings as vehicle property values
 */
public final class VehiclePropertyStringParser {

    /** prevents instantiation */
    private VehiclePropertyStringParser() { }

    /**
     * Exception class for syntax errors in property value Strings,
     * the message contains the reason using one of this utility class' public String constants.
     */
    public static class PropertyValueSyntaxException extends Exception {
        private static final long serialVersionUID = 1L;
        public PropertyValueSyntaxException(String message) {
            super(message);
        }
    }

    public static final String ERROR_WEIGHT =
        tr("Weights must be given as positive decimal numbers with unit \"t\" or without unit.");
    public static final String ERROR_LENGTH =
        tr("Lengths must be given as positive decimal numbers with unit \"m\", \"km\", \"mi\"" +
        " or without unit.\nAlternatively, the format FEET'' INCHES\" can be used.");
    public static final String ERROR_SPEED =
        tr("Speeds should be given as numbers without unit or "
        + "as numbers followed by \"mph\".");
    public static final String ERROR_INCLINE =
        tr("Inclines must be given as positive decimal numbers with followed by \"%\".");
    public static final String ERROR_TRACKTYPE =
        tr("Tracktype grades must be given as integers between 0 and 5.");
    public static final String ERROR_SURFACE =
        tr("Surface values must not contain any of the following characters: '','', '' '{' '',  '' '}' '', ''='', ''|''");

    private static final List<Character> FORBIDDEN_SURFACE_CHARS =
        Arrays.asList(',', '{', '}', '=', '|');

    /**
     * returns the value represented by the propertyValueString
     *
     * @throws PropertyValueSyntaxException  if the string has syntax errors that prevent parsing
     * @throws InvalidParameterException     if an unknown property type was passed
     *
     * @param propertyType         type of the property; != null
     * @param propertyValueString  string to parse; != null
     * @return                     property value; != null.
     *                             Guaranteed to be valid according to propertyType's
     *                             {@link VehiclePropertyType#isValidValue(Object)} method.
     */
    public static final <V> V parsePropertyValue(
            VehiclePropertyType<V> propertyType, String propertyValueString)
    throws PropertyValueSyntaxException {

        assert propertyType != null && propertyValueString != null;

        if (propertyType == VehiclePropertyTypes.AXLELOAD
                || propertyType == VehiclePropertyTypes.WEIGHT) {

            Float value = ValueStringParser.parseWeight(propertyValueString);
            if (value != null && propertyType.isValidValue(value)) {
                @SuppressWarnings("unchecked") //V must be float because of propertyType condition
                V result = (V)value;
                return result;
            } else {
                throw new PropertyValueSyntaxException(ERROR_WEIGHT);
            }

        } else if (propertyType == VehiclePropertyTypes.HEIGHT
                || propertyType == VehiclePropertyTypes.LENGTH
                || propertyType == VehiclePropertyTypes.WIDTH) {

            Float value = ValueStringParser.parseMeasure(propertyValueString);
            if (value != null && propertyType.isValidValue(value)) {
                @SuppressWarnings("unchecked") //V must be float because of propertyType condition
                V result = (V)value;
                return result;
            } else {
                throw new PropertyValueSyntaxException(ERROR_LENGTH);
            }

        } else if (propertyType == VehiclePropertyTypes.SPEED) {

            Float value = ValueStringParser.parseSpeed(propertyValueString);
            if (value != null && propertyType.isValidValue(value)) {
                @SuppressWarnings("unchecked") //V must be float because of propertyType condition
                V result = (V)value;
                return result;
            } else {
                throw new PropertyValueSyntaxException(ERROR_SPEED);
            }

        } else if (propertyType == VehiclePropertyTypes.MAX_INCLINE_DOWN
                || propertyType == VehiclePropertyTypes.MAX_INCLINE_UP) {

            Float value = ValueStringParser.parseIncline(propertyValueString);
            if (value != null && propertyType.isValidValue(value)) {
                @SuppressWarnings("unchecked") //V must be float because of propertyType condition
                V result = (V)value;
                return result;
            } else {
                throw new PropertyValueSyntaxException(ERROR_INCLINE);
            }

        } else if (propertyType == VehiclePropertyTypes.MAX_TRACKTYPE) {

            try {
                int value = Integer.parseInt(propertyValueString);
                if (0 <= value && value <= 5) {
                    @SuppressWarnings("unchecked") //V must be int because of propertyType condition
                    V result = (V)(Integer)value;
                    return result;
                }
            } catch (NumberFormatException e) {}

            throw new PropertyValueSyntaxException(ERROR_TRACKTYPE);

        } else if (propertyType == VehiclePropertyTypes.SURFACE_BLACKLIST) {

            String[] surfaces = propertyValueString.split(";\\s*");
            Collection<String> surfaceBlacklist = new ArrayList<String>(surfaces.length);
            for (String surface : surfaces) {
                for (char nameChar : surface.toCharArray()) {
                    if (FORBIDDEN_SURFACE_CHARS.contains(nameChar)) {
                        throw new PropertyValueSyntaxException(ERROR_SURFACE);
                    }
                }
                surfaceBlacklist.add(surface);
            }

            @SuppressWarnings("unchecked") //V must be Collection because of propertyType condition
            V result = (V)surfaceBlacklist;
            return result;

        } else {
            throw new InvalidParameterException("Unknown property type: " + propertyType);
        }

    }

}
