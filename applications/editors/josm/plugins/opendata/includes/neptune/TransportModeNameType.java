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
 * <p>Classe Java pour TransportModeNameType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="TransportModeNameType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Air"/>
 *     &lt;enumeration value="Train"/>
 *     &lt;enumeration value="LongDistanceTrain"/>
 *     &lt;enumeration value="LongDistanceTrain_2"/>
 *     &lt;enumeration value="LocalTrain"/>
 *     &lt;enumeration value="RapidTransit"/>
 *     &lt;enumeration value="Metro"/>
 *     &lt;enumeration value="Tramway"/>
 *     &lt;enumeration value="Coach"/>
 *     &lt;enumeration value="Bus"/>
 *     &lt;enumeration value="Ferry"/>
 *     &lt;enumeration value="Waterborne"/>
 *     &lt;enumeration value="PrivateVehicle"/>
 *     &lt;enumeration value="Walk"/>
 *     &lt;enumeration value="Trolleybus"/>
 *     &lt;enumeration value="Bicycle"/>
 *     &lt;enumeration value="Shuttle"/>
 *     &lt;enumeration value="Taxi"/>
 *     &lt;enumeration value="VAL"/>
 *     &lt;enumeration value="Other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TransportModeNameType")
@XmlEnum
public enum TransportModeNameType {

    @XmlEnumValue("Air")
    AIR("Air"),
    @XmlEnumValue("Train")
    TRAIN("Train"),
    @XmlEnumValue("LongDistanceTrain")
    LONG_DISTANCE_TRAIN("LongDistanceTrain"),
    @XmlEnumValue("LongDistanceTrain_2")
    LONG_DISTANCE_TRAIN_2("LongDistanceTrain_2"),
    @XmlEnumValue("LocalTrain")
    LOCAL_TRAIN("LocalTrain"),
    @XmlEnumValue("RapidTransit")
    RAPID_TRANSIT("RapidTransit"),
    @XmlEnumValue("Metro")
    METRO("Metro"),
    @XmlEnumValue("Tramway")
    TRAMWAY("Tramway"),
    @XmlEnumValue("Coach")
    COACH("Coach"),
    @XmlEnumValue("Bus")
    BUS("Bus"),
    @XmlEnumValue("Ferry")
    FERRY("Ferry"),
    @XmlEnumValue("Waterborne")
    WATERBORNE("Waterborne"),
    @XmlEnumValue("PrivateVehicle")
    PRIVATE_VEHICLE("PrivateVehicle"),
    @XmlEnumValue("Walk")
    WALK("Walk"),
    @XmlEnumValue("Trolleybus")
    TROLLEYBUS("Trolleybus"),
    @XmlEnumValue("Bicycle")
    BICYCLE("Bicycle"),
    @XmlEnumValue("Shuttle")
    SHUTTLE("Shuttle"),
    @XmlEnumValue("Taxi")
    TAXI("Taxi"),
    VAL("VAL"),
    @XmlEnumValue("Other")
    OTHER("Other");
    private final String value;

    TransportModeNameType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TransportModeNameType fromValue(String v) {
        for (TransportModeNameType c: TransportModeNameType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
