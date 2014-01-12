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
 * <p>Classe Java pour QualityIndexType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="QualityIndexType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Certain"/>
 *     &lt;enumeration value="VeryReliable"/>
 *     &lt;enumeration value="Reliable"/>
 *     &lt;enumeration value="ProbablyReliable"/>
 *     &lt;enumeration value="Unconfirmed"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "QualityIndexType")
@XmlEnum
public enum QualityIndexType {

    @XmlEnumValue("Certain")
    CERTAIN("Certain"),
    @XmlEnumValue("VeryReliable")
    VERY_RELIABLE("VeryReliable"),
    @XmlEnumValue("Reliable")
    RELIABLE("Reliable"),
    @XmlEnumValue("ProbablyReliable")
    PROBABLY_RELIABLE("ProbablyReliable"),
    @XmlEnumValue("Unconfirmed")
    UNCONFIRMED("Unconfirmed");
    private final String value;

    QualityIndexType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QualityIndexType fromValue(String v) {
        for (QualityIndexType c: QualityIndexType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
