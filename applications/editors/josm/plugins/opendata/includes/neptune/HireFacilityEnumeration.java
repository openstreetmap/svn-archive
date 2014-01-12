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
 * <p>Classe Java pour HireFacilityEnumeration.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="HireFacilityEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="unknown"/>
 *     &lt;enumeration value="carHire"/>
 *     &lt;enumeration value="motorCycleHire"/>
 *     &lt;enumeration value="cycleHire"/>
 *     &lt;enumeration value="taxi"/>
 *     &lt;enumeration value="recreationDeviceHire"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "HireFacilityEnumeration", namespace = "http://www.siri.org.uk/siri")
@XmlEnum
public enum HireFacilityEnumeration {

    @XmlEnumValue("unknown")
    UNKNOWN("unknown"),
    @XmlEnumValue("carHire")
    CAR_HIRE("carHire"),
    @XmlEnumValue("motorCycleHire")
    MOTOR_CYCLE_HIRE("motorCycleHire"),
    @XmlEnumValue("cycleHire")
    CYCLE_HIRE("cycleHire"),
    @XmlEnumValue("taxi")
    TAXI("taxi"),
    @XmlEnumValue("recreationDeviceHire")
    RECREATION_DEVICE_HIRE("recreationDeviceHire");
    private final String value;

    HireFacilityEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HireFacilityEnumeration fromValue(String v) {
        for (HireFacilityEnumeration c: HireFacilityEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
