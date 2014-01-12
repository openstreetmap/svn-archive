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
 * <p>Classe Java pour TicketingFacilityEnumeration.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="TicketingFacilityEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="unknown"/>
 *     &lt;enumeration value="ticketMachines"/>
 *     &lt;enumeration value="ticketOffice"/>
 *     &lt;enumeration value="ticketOnDemandMachines"/>
 *     &lt;enumeration value="ticketSales"/>
 *     &lt;enumeration value="mobileTicketing"/>
 *     &lt;enumeration value="ticketCollection"/>
 *     &lt;enumeration value="centralReservations"/>
 *     &lt;enumeration value="localTickets"/>
 *     &lt;enumeration value="nationalTickets"/>
 *     &lt;enumeration value="internationalTickets"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TicketingFacilityEnumeration", namespace = "http://www.siri.org.uk/siri")
@XmlEnum
public enum TicketingFacilityEnumeration {

    @XmlEnumValue("unknown")
    UNKNOWN("unknown"),
    @XmlEnumValue("ticketMachines")
    TICKET_MACHINES("ticketMachines"),
    @XmlEnumValue("ticketOffice")
    TICKET_OFFICE("ticketOffice"),
    @XmlEnumValue("ticketOnDemandMachines")
    TICKET_ON_DEMAND_MACHINES("ticketOnDemandMachines"),
    @XmlEnumValue("ticketSales")
    TICKET_SALES("ticketSales"),
    @XmlEnumValue("mobileTicketing")
    MOBILE_TICKETING("mobileTicketing"),
    @XmlEnumValue("ticketCollection")
    TICKET_COLLECTION("ticketCollection"),
    @XmlEnumValue("centralReservations")
    CENTRAL_RESERVATIONS("centralReservations"),
    @XmlEnumValue("localTickets")
    LOCAL_TICKETS("localTickets"),
    @XmlEnumValue("nationalTickets")
    NATIONAL_TICKETS("nationalTickets"),
    @XmlEnumValue("internationalTickets")
    INTERNATIONAL_TICKETS("internationalTickets");
    private final String value;

    TicketingFacilityEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TicketingFacilityEnumeration fromValue(String v) {
        for (TicketingFacilityEnumeration c: TicketingFacilityEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
