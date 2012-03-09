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
 * <p>Classe Java pour SourceTypeType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="SourceTypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AutomobileClubPatrol"/>
 *     &lt;enumeration value="SpotterAircraft"/>
 *     &lt;enumeration value="BreakdownService"/>
 *     &lt;enumeration value="CameraObservation"/>
 *     &lt;enumeration value="EmergencyServicePatrol"/>
 *     &lt;enumeration value="FreightVehicleOperator"/>
 *     &lt;enumeration value="InfraredMonitoringStation"/>
 *     &lt;enumeration value="InductionLoopMonitoringStation"/>
 *     &lt;enumeration value="MicrowaveMonitoringStation"/>
 *     &lt;enumeration value="MobileTelephoneCaller"/>
 *     &lt;enumeration value="OtherInformation"/>
 *     &lt;enumeration value="OtherOfficialVehicle"/>
 *     &lt;enumeration value="PolicePatrol"/>
 *     &lt;enumeration value="PublicAndPrivateUtilities"/>
 *     &lt;enumeration value="RoadAuthorities"/>
 *     &lt;enumeration value="RegisteredMotoristObserver"/>
 *     &lt;enumeration value="RoadsideTelephoneCaller"/>
 *     &lt;enumeration value="TrafficMonitoringStation"/>
 *     &lt;enumeration value="TransitOperator"/>
 *     &lt;enumeration value="VideoProcessingMonitoringStation"/>
 *     &lt;enumeration value="VehicleProbeMeasurement"/>
 *     &lt;enumeration value="PublicTransport"/>
 *     &lt;enumeration value="PassengerTransportCoordinatingAuthority"/>
 *     &lt;enumeration value="TravelInformationServiceProvider"/>
 *     &lt;enumeration value="TravelAgency"/>
 *     &lt;enumeration value="IndividualSubjectOfTravelItinerary"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SourceTypeType")
@XmlEnum
public enum SourceTypeType {

    @XmlEnumValue("AutomobileClubPatrol")
    AUTOMOBILE_CLUB_PATROL("AutomobileClubPatrol"),
    @XmlEnumValue("SpotterAircraft")
    SPOTTER_AIRCRAFT("SpotterAircraft"),
    @XmlEnumValue("BreakdownService")
    BREAKDOWN_SERVICE("BreakdownService"),
    @XmlEnumValue("CameraObservation")
    CAMERA_OBSERVATION("CameraObservation"),
    @XmlEnumValue("EmergencyServicePatrol")
    EMERGENCY_SERVICE_PATROL("EmergencyServicePatrol"),
    @XmlEnumValue("FreightVehicleOperator")
    FREIGHT_VEHICLE_OPERATOR("FreightVehicleOperator"),
    @XmlEnumValue("InfraredMonitoringStation")
    INFRARED_MONITORING_STATION("InfraredMonitoringStation"),
    @XmlEnumValue("InductionLoopMonitoringStation")
    INDUCTION_LOOP_MONITORING_STATION("InductionLoopMonitoringStation"),
    @XmlEnumValue("MicrowaveMonitoringStation")
    MICROWAVE_MONITORING_STATION("MicrowaveMonitoringStation"),
    @XmlEnumValue("MobileTelephoneCaller")
    MOBILE_TELEPHONE_CALLER("MobileTelephoneCaller"),
    @XmlEnumValue("OtherInformation")
    OTHER_INFORMATION("OtherInformation"),
    @XmlEnumValue("OtherOfficialVehicle")
    OTHER_OFFICIAL_VEHICLE("OtherOfficialVehicle"),
    @XmlEnumValue("PolicePatrol")
    POLICE_PATROL("PolicePatrol"),
    @XmlEnumValue("PublicAndPrivateUtilities")
    PUBLIC_AND_PRIVATE_UTILITIES("PublicAndPrivateUtilities"),
    @XmlEnumValue("RoadAuthorities")
    ROAD_AUTHORITIES("RoadAuthorities"),
    @XmlEnumValue("RegisteredMotoristObserver")
    REGISTERED_MOTORIST_OBSERVER("RegisteredMotoristObserver"),
    @XmlEnumValue("RoadsideTelephoneCaller")
    ROADSIDE_TELEPHONE_CALLER("RoadsideTelephoneCaller"),
    @XmlEnumValue("TrafficMonitoringStation")
    TRAFFIC_MONITORING_STATION("TrafficMonitoringStation"),
    @XmlEnumValue("TransitOperator")
    TRANSIT_OPERATOR("TransitOperator"),
    @XmlEnumValue("VideoProcessingMonitoringStation")
    VIDEO_PROCESSING_MONITORING_STATION("VideoProcessingMonitoringStation"),
    @XmlEnumValue("VehicleProbeMeasurement")
    VEHICLE_PROBE_MEASUREMENT("VehicleProbeMeasurement"),
    @XmlEnumValue("PublicTransport")
    PUBLIC_TRANSPORT("PublicTransport"),
    @XmlEnumValue("PassengerTransportCoordinatingAuthority")
    PASSENGER_TRANSPORT_COORDINATING_AUTHORITY("PassengerTransportCoordinatingAuthority"),
    @XmlEnumValue("TravelInformationServiceProvider")
    TRAVEL_INFORMATION_SERVICE_PROVIDER("TravelInformationServiceProvider"),
    @XmlEnumValue("TravelAgency")
    TRAVEL_AGENCY("TravelAgency"),
    @XmlEnumValue("IndividualSubjectOfTravelItinerary")
    INDIVIDUAL_SUBJECT_OF_TRAVEL_ITINERARY("IndividualSubjectOfTravelItinerary");
    private final String value;

    SourceTypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SourceTypeType fromValue(String v) {
        for (SourceTypeType c: SourceTypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
