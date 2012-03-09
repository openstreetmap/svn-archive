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
 * <p>Classe Java pour ChouetteAreaType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="ChouetteAreaType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Quay"/>
 *     &lt;enumeration value="BoardingPosition"/>
 *     &lt;enumeration value="CommercialStopPoint"/>
 *     &lt;enumeration value="StopPlace"/>
 *     &lt;enumeration value="ITL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ChouetteAreaType")
@XmlEnum
public enum ChouetteAreaType {

    @XmlEnumValue("Quay")
    QUAY("Quay"),
    @XmlEnumValue("BoardingPosition")
    BOARDING_POSITION("BoardingPosition"),
    @XmlEnumValue("CommercialStopPoint")
    COMMERCIAL_STOP_POINT("CommercialStopPoint"),
    @XmlEnumValue("StopPlace")
    STOP_PLACE("StopPlace"),
    ITL("ITL");
    private final String value;

    ChouetteAreaType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ChouetteAreaType fromValue(String v) {
        for (ChouetteAreaType c: ChouetteAreaType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
