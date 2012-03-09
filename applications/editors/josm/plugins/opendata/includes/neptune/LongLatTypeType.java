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
 * <p>Classe Java pour LongLatTypeType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="LongLatTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="WGS84"/>
 *     &lt;enumeration value="WGS92"/>
 *     &lt;enumeration value="Standard"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LongLatTypeType")
@XmlEnum
public enum LongLatTypeType {

    @XmlEnumValue("WGS84")
    WGS_84("WGS84"),
    @XmlEnumValue("WGS92")
    WGS_92("WGS92"),
    @XmlEnumValue("Standard")
    STANDARD("Standard");
    private final String value;

    LongLatTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LongLatTypeType fromValue(String v) {
        for (LongLatTypeType c: LongLatTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
