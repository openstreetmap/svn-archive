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
 * <p>Classe Java pour NuisanceFacilityEnumeration.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="NuisanceFacilityEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="unknown"/>
 *     &lt;enumeration value="smoking"/>
 *     &lt;enumeration value="noSmoking"/>
 *     &lt;enumeration value="mobilePhoneUseZone"/>
 *     &lt;enumeration value="mobilePhoneFreeZone"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "NuisanceFacilityEnumeration", namespace = "http://www.siri.org.uk/siri")
@XmlEnum
public enum NuisanceFacilityEnumeration {

    @XmlEnumValue("unknown")
    UNKNOWN("unknown"),
    @XmlEnumValue("smoking")
    SMOKING("smoking"),
    @XmlEnumValue("noSmoking")
    NO_SMOKING("noSmoking"),
    @XmlEnumValue("mobilePhoneUseZone")
    MOBILE_PHONE_USE_ZONE("mobilePhoneUseZone"),
    @XmlEnumValue("mobilePhoneFreeZone")
    MOBILE_PHONE_FREE_ZONE("mobilePhoneFreeZone");
    private final String value;

    NuisanceFacilityEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NuisanceFacilityEnumeration fromValue(String v) {
        for (NuisanceFacilityEnumeration c: NuisanceFacilityEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
