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
 * <p>Classe Java pour AssistanceFacilityEnumeration.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="AssistanceFacilityEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="unknown"/>
 *     &lt;enumeration value="police"/>
 *     &lt;enumeration value="firstAid"/>
 *     &lt;enumeration value="sosPoint"/>
 *     &lt;enumeration value="specificAssistance"/>
 *     &lt;enumeration value="unaccompaniedMinorAssistance"/>
 *     &lt;enumeration value="boardingAssistance"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AssistanceFacilityEnumeration", namespace = "http://www.siri.org.uk/siri")
@XmlEnum
public enum AssistanceFacilityEnumeration {

    @XmlEnumValue("unknown")
    UNKNOWN("unknown"),
    @XmlEnumValue("police")
    POLICE("police"),
    @XmlEnumValue("firstAid")
    FIRST_AID("firstAid"),
    @XmlEnumValue("sosPoint")
    SOS_POINT("sosPoint"),
    @XmlEnumValue("specificAssistance")
    SPECIFIC_ASSISTANCE("specificAssistance"),
    @XmlEnumValue("unaccompaniedMinorAssistance")
    UNACCOMPANIED_MINOR_ASSISTANCE("unaccompaniedMinorAssistance"),
    @XmlEnumValue("boardingAssistance")
    BOARDING_ASSISTANCE("boardingAssistance");
    private final String value;

    AssistanceFacilityEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AssistanceFacilityEnumeration fromValue(String v) {
        for (AssistanceFacilityEnumeration c: AssistanceFacilityEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
