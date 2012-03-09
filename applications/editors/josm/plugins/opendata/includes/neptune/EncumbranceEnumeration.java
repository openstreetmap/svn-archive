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
 * <p>Classe Java pour EncumbranceEnumeration.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="EncumbranceEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="luggageEncumbered"/>
 *     &lt;enumeration value="pushchair"/>
 *     &lt;enumeration value="baggageTrolley"/>
 *     &lt;enumeration value="oversizeBaggage"/>
 *     &lt;enumeration value="guideDog"/>
 *     &lt;enumeration value="otherAnimal"/>
 *     &lt;enumeration value="otherEncumbrance"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "EncumbranceEnumeration", namespace = "http://www.ifopt.org.uk/acsb")
@XmlEnum
public enum EncumbranceEnumeration {

    @XmlEnumValue("luggageEncumbered")
    LUGGAGE_ENCUMBERED("luggageEncumbered"),
    @XmlEnumValue("pushchair")
    PUSHCHAIR("pushchair"),
    @XmlEnumValue("baggageTrolley")
    BAGGAGE_TROLLEY("baggageTrolley"),
    @XmlEnumValue("oversizeBaggage")
    OVERSIZE_BAGGAGE("oversizeBaggage"),
    @XmlEnumValue("guideDog")
    GUIDE_DOG("guideDog"),
    @XmlEnumValue("otherAnimal")
    OTHER_ANIMAL("otherAnimal"),
    @XmlEnumValue("otherEncumbrance")
    OTHER_ENCUMBRANCE("otherEncumbrance");
    private final String value;

    EncumbranceEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EncumbranceEnumeration fromValue(String v) {
        for (EncumbranceEnumeration c: EncumbranceEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
