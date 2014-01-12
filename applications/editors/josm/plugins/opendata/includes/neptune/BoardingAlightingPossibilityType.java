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
 * <p>Classe Java pour BoardingAlightingPossibilityType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="BoardingAlightingPossibilityType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BoardAndAlight"/>
 *     &lt;enumeration value="AlightOnly"/>
 *     &lt;enumeration value="BoardOnly"/>
 *     &lt;enumeration value="NeitherBoardOrAlight"/>
 *     &lt;enumeration value="BoardAndAlightOnRequest"/>
 *     &lt;enumeration value="AlightOnRequest"/>
 *     &lt;enumeration value="BoardOnRequest"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BoardingAlightingPossibilityType")
@XmlEnum
public enum BoardingAlightingPossibilityType {

    @XmlEnumValue("BoardAndAlight")
    BOARD_AND_ALIGHT("BoardAndAlight"),
    @XmlEnumValue("AlightOnly")
    ALIGHT_ONLY("AlightOnly"),
    @XmlEnumValue("BoardOnly")
    BOARD_ONLY("BoardOnly"),
    @XmlEnumValue("NeitherBoardOrAlight")
    NEITHER_BOARD_OR_ALIGHT("NeitherBoardOrAlight"),
    @XmlEnumValue("BoardAndAlightOnRequest")
    BOARD_AND_ALIGHT_ON_REQUEST("BoardAndAlightOnRequest"),
    @XmlEnumValue("AlightOnRequest")
    ALIGHT_ON_REQUEST("AlightOnRequest"),
    @XmlEnumValue("BoardOnRequest")
    BOARD_ON_REQUEST("BoardOnRequest");
    private final String value;

    BoardingAlightingPossibilityType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BoardingAlightingPossibilityType fromValue(String v) {
        for (BoardingAlightingPossibilityType c: BoardingAlightingPossibilityType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
