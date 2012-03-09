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
 * <p>Classe Java pour PTDirectionType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="PTDirectionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="North"/>
 *     &lt;enumeration value="NorthEast"/>
 *     &lt;enumeration value="East"/>
 *     &lt;enumeration value="SouthEast"/>
 *     &lt;enumeration value="South"/>
 *     &lt;enumeration value="SouthWest"/>
 *     &lt;enumeration value="West"/>
 *     &lt;enumeration value="NorthWest"/>
 *     &lt;enumeration value="ClockWise"/>
 *     &lt;enumeration value="CounterClockWise"/>
 *     &lt;enumeration value="A"/>
 *     &lt;enumeration value="R"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PTDirectionType")
@XmlEnum
public enum PTDirectionType {

    @XmlEnumValue("North")
    NORTH("North"),
    @XmlEnumValue("NorthEast")
    NORTH_EAST("NorthEast"),
    @XmlEnumValue("East")
    EAST("East"),
    @XmlEnumValue("SouthEast")
    SOUTH_EAST("SouthEast"),
    @XmlEnumValue("South")
    SOUTH("South"),
    @XmlEnumValue("SouthWest")
    SOUTH_WEST("SouthWest"),
    @XmlEnumValue("West")
    WEST("West"),
    @XmlEnumValue("NorthWest")
    NORTH_WEST("NorthWest"),
    @XmlEnumValue("ClockWise")
    CLOCK_WISE("ClockWise"),
    @XmlEnumValue("CounterClockWise")
    COUNTER_CLOCK_WISE("CounterClockWise"),
    A("A"),
    R("R");
    private final String value;

    PTDirectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PTDirectionType fromValue(String v) {
        for (PTDirectionType c: PTDirectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
