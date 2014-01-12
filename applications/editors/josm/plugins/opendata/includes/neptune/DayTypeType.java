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
 * <p>Classe Java pour DayTypeType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="DayTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="WeekDay"/>
 *     &lt;enumeration value="WeekEnd"/>
 *     &lt;enumeration value="Monday"/>
 *     &lt;enumeration value="Tuesday"/>
 *     &lt;enumeration value="Wednesday"/>
 *     &lt;enumeration value="Thursday"/>
 *     &lt;enumeration value="Friday"/>
 *     &lt;enumeration value="Saturday"/>
 *     &lt;enumeration value="Sunday"/>
 *     &lt;enumeration value="SchoolHolliday"/>
 *     &lt;enumeration value="PublicHolliday"/>
 *     &lt;enumeration value="MarketDay"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DayTypeType")
@XmlEnum
public enum DayTypeType {

    @XmlEnumValue("WeekDay")
    WEEK_DAY("WeekDay"),
    @XmlEnumValue("WeekEnd")
    WEEK_END("WeekEnd"),
    @XmlEnumValue("Monday")
    MONDAY("Monday"),
    @XmlEnumValue("Tuesday")
    TUESDAY("Tuesday"),
    @XmlEnumValue("Wednesday")
    WEDNESDAY("Wednesday"),
    @XmlEnumValue("Thursday")
    THURSDAY("Thursday"),
    @XmlEnumValue("Friday")
    FRIDAY("Friday"),
    @XmlEnumValue("Saturday")
    SATURDAY("Saturday"),
    @XmlEnumValue("Sunday")
    SUNDAY("Sunday"),
    @XmlEnumValue("SchoolHolliday")
    SCHOOL_HOLLIDAY("SchoolHolliday"),
    @XmlEnumValue("PublicHolliday")
    PUBLIC_HOLLIDAY("PublicHolliday"),
    @XmlEnumValue("MarketDay")
    MARKET_DAY("MarketDay");
    private final String value;

    DayTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DayTypeType fromValue(String v) {
        for (DayTypeType c: DayTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
