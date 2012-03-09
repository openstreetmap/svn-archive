//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour UnitType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="UnitType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DegreesCelsius"/>
 *     &lt;enumeration value="Centimeter"/>
 *     &lt;enumeration value="Degree"/>
 *     &lt;enumeration value="Hour"/>
 *     &lt;enumeration value="Hectopascals"/>
 *     &lt;enumeration value="KilometersPerHour"/>
 *     &lt;enumeration value="Kilometer"/>
 *     &lt;enumeration value="CubicMeter"/>
 *     &lt;enumeration value="MillimetersPerHour"/>
 *     &lt;enumeration value="Millimeter"/>
 *     &lt;enumeration value="Meter"/>
 *     &lt;enumeration value="MetersPerSecond"/>
 *     &lt;enumeration value="Percentage"/>
 *     &lt;enumeration value="Second"/>
 *     &lt;enumeration value="Tonne"/>
 *     &lt;enumeration value="HrMinSec"/>
 *     &lt;enumeration value="PeriodOfTime"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "UnitType")
@XmlEnum
public enum UnitType {

    @XmlEnumValue("DegreesCelsius")
    DEGREES_CELSIUS("DegreesCelsius"),
    @XmlEnumValue("Centimeter")
    CENTIMETER("Centimeter"),
    @XmlEnumValue("Degree")
    DEGREE("Degree"),
    @XmlEnumValue("Hour")
    HOUR("Hour"),
    @XmlEnumValue("Hectopascals")
    HECTOPASCALS("Hectopascals"),
    @XmlEnumValue("KilometersPerHour")
    KILOMETERS_PER_HOUR("KilometersPerHour"),
    @XmlEnumValue("Kilometer")
    KILOMETER("Kilometer"),
    @XmlEnumValue("CubicMeter")
    CUBIC_METER("CubicMeter"),
    @XmlEnumValue("MillimetersPerHour")
    MILLIMETERS_PER_HOUR("MillimetersPerHour"),
    @XmlEnumValue("Millimeter")
    MILLIMETER("Millimeter"),
    @XmlEnumValue("Meter")
    METER("Meter"),
    @XmlEnumValue("MetersPerSecond")
    METERS_PER_SECOND("MetersPerSecond"),
    @XmlEnumValue("Percentage")
    PERCENTAGE("Percentage"),
    @XmlEnumValue("Second")
    SECOND("Second"),
    @XmlEnumValue("Tonne")
    TONNE("Tonne"),
    @XmlEnumValue("HrMinSec")
    HR_MIN_SEC("HrMinSec"),
    @XmlEnumValue("PeriodOfTime")
    PERIOD_OF_TIME("PeriodOfTime");
    private final String value;

    UnitType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UnitType fromValue(String v) {
        for (UnitType c: UnitType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
