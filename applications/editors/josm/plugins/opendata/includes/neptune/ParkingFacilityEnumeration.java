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
 * <p>Classe Java pour ParkingFacilityEnumeration.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="ParkingFacilityEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="unknown"/>
 *     &lt;enumeration value="carPark"/>
 *     &lt;enumeration value="parkAndRidePark"/>
 *     &lt;enumeration value="motorcyclePark"/>
 *     &lt;enumeration value="cyclePark"/>
 *     &lt;enumeration value="rentalCarPark"/>
 *     &lt;enumeration value="coachPark"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ParkingFacilityEnumeration", namespace = "http://www.siri.org.uk/siri")
@XmlEnum
public enum ParkingFacilityEnumeration {

    @XmlEnumValue("unknown")
    UNKNOWN("unknown"),
    @XmlEnumValue("carPark")
    CAR_PARK("carPark"),
    @XmlEnumValue("parkAndRidePark")
    PARK_AND_RIDE_PARK("parkAndRidePark"),
    @XmlEnumValue("motorcyclePark")
    MOTORCYCLE_PARK("motorcyclePark"),
    @XmlEnumValue("cyclePark")
    CYCLE_PARK("cyclePark"),
    @XmlEnumValue("rentalCarPark")
    RENTAL_CAR_PARK("rentalCarPark"),
    @XmlEnumValue("coachPark")
    COACH_PARK("coachPark");
    private final String value;

    ParkingFacilityEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ParkingFacilityEnumeration fromValue(String v) {
        for (ParkingFacilityEnumeration c: ParkingFacilityEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
