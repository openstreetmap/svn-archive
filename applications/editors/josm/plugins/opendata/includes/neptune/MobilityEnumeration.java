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
 * <p>Classe Java pour MobilityEnumeration.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="MobilityEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="wheelchair"/>
 *     &lt;enumeration value="assistedWheelchair"/>
 *     &lt;enumeration value="motorizedWheelchair"/>
 *     &lt;enumeration value="walkingFrame"/>
 *     &lt;enumeration value="restrictedMobility"/>
 *     &lt;enumeration value="otherMobilityNeed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MobilityEnumeration", namespace = "http://www.ifopt.org.uk/acsb")
@XmlEnum
public enum MobilityEnumeration {

    @XmlEnumValue("wheelchair")
    WHEELCHAIR("wheelchair"),
    @XmlEnumValue("assistedWheelchair")
    ASSISTED_WHEELCHAIR("assistedWheelchair"),
    @XmlEnumValue("motorizedWheelchair")
    MOTORIZED_WHEELCHAIR("motorizedWheelchair"),
    @XmlEnumValue("walkingFrame")
    WALKING_FRAME("walkingFrame"),
    @XmlEnumValue("restrictedMobility")
    RESTRICTED_MOBILITY("restrictedMobility"),
    @XmlEnumValue("otherMobilityNeed")
    OTHER_MOBILITY_NEED("otherMobilityNeed");
    private final String value;

    MobilityEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MobilityEnumeration fromValue(String v) {
        for (MobilityEnumeration c: MobilityEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
