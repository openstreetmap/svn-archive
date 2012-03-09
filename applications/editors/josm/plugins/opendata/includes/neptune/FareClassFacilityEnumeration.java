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
 * <p>Classe Java pour FareClassFacilityEnumeration.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="FareClassFacilityEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="unknown"/>
 *     &lt;enumeration value="pti23_0"/>
 *     &lt;enumeration value="unknown_2"/>
 *     &lt;enumeration value="pti23_6"/>
 *     &lt;enumeration value="firstClass"/>
 *     &lt;enumeration value="pti23_7"/>
 *     &lt;enumeration value="secondClass"/>
 *     &lt;enumeration value="pti23_8"/>
 *     &lt;enumeration value="thirdClass"/>
 *     &lt;enumeration value="pti23_9"/>
 *     &lt;enumeration value="economyClass"/>
 *     &lt;enumeration value="pti23_10"/>
 *     &lt;enumeration value="businessClass"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FareClassFacilityEnumeration", namespace = "http://www.siri.org.uk/siri")
@XmlEnum
public enum FareClassFacilityEnumeration {

    @XmlEnumValue("unknown")
    UNKNOWN("unknown"),
    @XmlEnumValue("pti23_0")
    PTI_23_0("pti23_0"),
    @XmlEnumValue("unknown_2")
    UNKNOWN_2("unknown_2"),
    @XmlEnumValue("pti23_6")
    PTI_23_6("pti23_6"),
    @XmlEnumValue("firstClass")
    FIRST_CLASS("firstClass"),
    @XmlEnumValue("pti23_7")
    PTI_23_7("pti23_7"),
    @XmlEnumValue("secondClass")
    SECOND_CLASS("secondClass"),
    @XmlEnumValue("pti23_8")
    PTI_23_8("pti23_8"),
    @XmlEnumValue("thirdClass")
    THIRD_CLASS("thirdClass"),
    @XmlEnumValue("pti23_9")
    PTI_23_9("pti23_9"),
    @XmlEnumValue("economyClass")
    ECONOMY_CLASS("economyClass"),
    @XmlEnumValue("pti23_10")
    PTI_23_10("pti23_10"),
    @XmlEnumValue("businessClass")
    BUSINESS_CLASS("businessClass");
    private final String value;

    FareClassFacilityEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FareClassFacilityEnumeration fromValue(String v) {
        for (FareClassFacilityEnumeration c: FareClassFacilityEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
