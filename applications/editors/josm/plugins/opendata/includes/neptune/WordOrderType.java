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
 * <p>Classe Java pour WordOrderType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="WordOrderType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="FromTheFirstToTheLastWord"/>
 *     &lt;enumeration value="FromTheLastToTheFirstWord"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "WordOrderType")
@XmlEnum
public enum WordOrderType {

    @XmlEnumValue("FromTheFirstToTheLastWord")
    FROM_THE_FIRST_TO_THE_LAST_WORD("FromTheFirstToTheLastWord"),
    @XmlEnumValue("FromTheLastToTheFirstWord")
    FROM_THE_LAST_TO_THE_FIRST_WORD("FromTheLastToTheFirstWord");
    private final String value;

    WordOrderType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static WordOrderType fromValue(String v) {
        for (WordOrderType c: WordOrderType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
