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
 * <p>Classe Java pour VehicleJourneyStatusValueType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="VehicleJourneyStatusValueType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Normal"/>
 *     &lt;enumeration value="Delayed"/>
 *     &lt;enumeration value="Cancelled"/>
 *     &lt;enumeration value="Rerouted"/>
 *     &lt;enumeration value="NotStopping"/>
 *     &lt;enumeration value="Early"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "VehicleJourneyStatusValueType")
@XmlEnum
public enum VehicleJourneyStatusValueType {

    @XmlEnumValue("Normal")
    NORMAL("Normal"),
    @XmlEnumValue("Delayed")
    DELAYED("Delayed"),
    @XmlEnumValue("Cancelled")
    CANCELLED("Cancelled"),
    @XmlEnumValue("Rerouted")
    REROUTED("Rerouted"),
    @XmlEnumValue("NotStopping")
    NOT_STOPPING("NotStopping"),
    @XmlEnumValue("Early")
    EARLY("Early");
    private final String value;

    VehicleJourneyStatusValueType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VehicleJourneyStatusValueType fromValue(String v) {
        for (VehicleJourneyStatusValueType c: VehicleJourneyStatusValueType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
