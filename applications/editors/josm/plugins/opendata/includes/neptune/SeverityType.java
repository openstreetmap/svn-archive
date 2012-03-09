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
 * <p>Classe Java pour SeverityType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="SeverityType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ExtremelySevere"/>
 *     &lt;enumeration value="VerySevere"/>
 *     &lt;enumeration value="Severe"/>
 *     &lt;enumeration value="LowSeverity"/>
 *     &lt;enumeration value="LowestSeverity"/>
 *     &lt;enumeration value="NotProvided"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SeverityType")
@XmlEnum
public enum SeverityType {

    @XmlEnumValue("ExtremelySevere")
    EXTREMELY_SEVERE("ExtremelySevere"),
    @XmlEnumValue("VerySevere")
    VERY_SEVERE("VerySevere"),
    @XmlEnumValue("Severe")
    SEVERE("Severe"),
    @XmlEnumValue("LowSeverity")
    LOW_SEVERITY("LowSeverity"),
    @XmlEnumValue("LowestSeverity")
    LOWEST_SEVERITY("LowestSeverity"),
    @XmlEnumValue("NotProvided")
    NOT_PROVIDED("NotProvided");
    private final String value;

    SeverityType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SeverityType fromValue(String v) {
        for (SeverityType c: SeverityType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
