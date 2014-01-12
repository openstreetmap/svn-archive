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
 * <p>Classe Java pour LocationTypeType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="LocationTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BusStopPoint"/>
 *     &lt;enumeration value="AriportStopPoint"/>
 *     &lt;enumeration value="TramStopPoint"/>
 *     &lt;enumeration value="MetroStopPoint"/>
 *     &lt;enumeration value="RailwayStopPoint"/>
 *     &lt;enumeration value="RoadJunction"/>
 *     &lt;enumeration value="Mixed"/>
 *     &lt;enumeration value="Address"/>
 *     &lt;enumeration value="IntermediateRoadPoint"/>
 *     &lt;enumeration value="StopArea"/>
 *     &lt;enumeration value="PointOfInterest"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LocationTypeType")
@XmlEnum
public enum LocationTypeType {

    @XmlEnumValue("BusStopPoint")
    BUS_STOP_POINT("BusStopPoint"),
    @XmlEnumValue("AriportStopPoint")
    ARIPORT_STOP_POINT("AriportStopPoint"),
    @XmlEnumValue("TramStopPoint")
    TRAM_STOP_POINT("TramStopPoint"),
    @XmlEnumValue("MetroStopPoint")
    METRO_STOP_POINT("MetroStopPoint"),
    @XmlEnumValue("RailwayStopPoint")
    RAILWAY_STOP_POINT("RailwayStopPoint"),
    @XmlEnumValue("RoadJunction")
    ROAD_JUNCTION("RoadJunction"),
    @XmlEnumValue("Mixed")
    MIXED("Mixed"),
    @XmlEnumValue("Address")
    ADDRESS("Address"),
    @XmlEnumValue("IntermediateRoadPoint")
    INTERMEDIATE_ROAD_POINT("IntermediateRoadPoint"),
    @XmlEnumValue("StopArea")
    STOP_AREA("StopArea"),
    @XmlEnumValue("PointOfInterest")
    POINT_OF_INTEREST("PointOfInterest");
    private final String value;

    LocationTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LocationTypeType fromValue(String v) {
        for (LocationTypeType c: LocationTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
