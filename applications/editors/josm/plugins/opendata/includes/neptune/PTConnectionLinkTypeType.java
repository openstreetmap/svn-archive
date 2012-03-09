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
 * <p>Classe Java pour PTConnectionLinkTypeType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="PTConnectionLinkTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Underground"/>
 *     &lt;enumeration value="Mixed"/>
 *     &lt;enumeration value="Overground"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PTConnectionLinkTypeType")
@XmlEnum
public enum PTConnectionLinkTypeType {

    @XmlEnumValue("Underground")
    UNDERGROUND("Underground"),
    @XmlEnumValue("Mixed")
    MIXED("Mixed"),
    @XmlEnumValue("Overground")
    OVERGROUND("Overground");
    private final String value;

    PTConnectionLinkTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PTConnectionLinkTypeType fromValue(String v) {
        for (PTConnectionLinkTypeType c: PTConnectionLinkTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
