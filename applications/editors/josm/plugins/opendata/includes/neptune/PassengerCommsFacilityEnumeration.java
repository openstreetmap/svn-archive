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
 * <p>Classe Java pour PassengerCommsFacilityEnumeration.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="PassengerCommsFacilityEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="unknown"/>
 *     &lt;enumeration value="faccomms_1"/>
 *     &lt;enumeration value="passengerWifi"/>
 *     &lt;enumeration value="pti23_21"/>
 *     &lt;enumeration value="telephone"/>
 *     &lt;enumeration value="pti23_14"/>
 *     &lt;enumeration value="audioServices"/>
 *     &lt;enumeration value="pti23_15"/>
 *     &lt;enumeration value="videoServices"/>
 *     &lt;enumeration value="pti23_25"/>
 *     &lt;enumeration value="businessServices"/>
 *     &lt;enumeration value="internet"/>
 *     &lt;enumeration value="postoffice"/>
 *     &lt;enumeration value="letterbox"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PassengerCommsFacilityEnumeration", namespace = "http://www.siri.org.uk/siri")
@XmlEnum
public enum PassengerCommsFacilityEnumeration {

    @XmlEnumValue("unknown")
    UNKNOWN("unknown"),
    @XmlEnumValue("faccomms_1")
    FACCOMMS_1("faccomms_1"),
    @XmlEnumValue("passengerWifi")
    PASSENGER_WIFI("passengerWifi"),
    @XmlEnumValue("pti23_21")
    PTI_23_21("pti23_21"),
    @XmlEnumValue("telephone")
    TELEPHONE("telephone"),
    @XmlEnumValue("pti23_14")
    PTI_23_14("pti23_14"),
    @XmlEnumValue("audioServices")
    AUDIO_SERVICES("audioServices"),
    @XmlEnumValue("pti23_15")
    PTI_23_15("pti23_15"),
    @XmlEnumValue("videoServices")
    VIDEO_SERVICES("videoServices"),
    @XmlEnumValue("pti23_25")
    PTI_23_25("pti23_25"),
    @XmlEnumValue("businessServices")
    BUSINESS_SERVICES("businessServices"),
    @XmlEnumValue("internet")
    INTERNET("internet"),
    @XmlEnumValue("postoffice")
    POSTOFFICE("postoffice"),
    @XmlEnumValue("letterbox")
    LETTERBOX("letterbox");
    private final String value;

    PassengerCommsFacilityEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PassengerCommsFacilityEnumeration fromValue(String v) {
        for (PassengerCommsFacilityEnumeration c: PassengerCommsFacilityEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
