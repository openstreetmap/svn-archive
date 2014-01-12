//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * 
 * Passing time on a stop point
 * 			
 * 
 * <p>Classe Java pour VehicleJourneyAtStopType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="VehicleJourneyAtStopType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="stopPointId" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *         &lt;element name="vehicleJourneyId" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
 *         &lt;element name="connectingServiceId" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;sequence>
 *             &lt;element name="arrivalTime" type="{http://www.w3.org/2001/XMLSchema}time" minOccurs="0"/>
 *             &lt;element name="departureTime" type="{http://www.w3.org/2001/XMLSchema}time"/>
 *             &lt;element name="waitingTime" type="{http://www.w3.org/2001/XMLSchema}time" minOccurs="0"/>
 *           &lt;/sequence>
 *           &lt;sequence>
 *             &lt;element name="elapseDuration" type="{http://www.w3.org/2001/XMLSchema}duration"/>
 *           &lt;/sequence>
 *         &lt;/choice>
 *         &lt;element name="headwayFrequency" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/>
 *         &lt;element name="boardingAlightingPossibility" type="{http://www.trident.org/schema/trident}BoardingAlightingPossibilityType" minOccurs="0"/>
 *         &lt;element name="order" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VehicleJourneyAtStopType", propOrder = {
    "stopPointId",
    "vehicleJourneyId",
    "connectingServiceId",
    "arrivalTime",
    "departureTime",
    "waitingTime",
    "elapseDuration",
    "headwayFrequency",
    "boardingAlightingPossibility",
    "order"
})
public class VehicleJourneyAtStopType {

    @XmlElement(required = true)
    protected String stopPointId;
    protected String vehicleJourneyId;
    protected String connectingServiceId;
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar arrivalTime;
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar departureTime;
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar waitingTime;
    protected Duration elapseDuration;
    protected Duration headwayFrequency;
    protected BoardingAlightingPossibilityType boardingAlightingPossibility;
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger order;

    /**
     * Obtient la valeur de la propriété stopPointId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStopPointId() {
        return stopPointId;
    }

    /**
     * Définit la valeur de la propriété stopPointId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStopPointId(String value) {
        this.stopPointId = value;
    }

    /**
     * Obtient la valeur de la propriété vehicleJourneyId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleJourneyId() {
        return vehicleJourneyId;
    }

    /**
     * Définit la valeur de la propriété vehicleJourneyId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleJourneyId(String value) {
        this.vehicleJourneyId = value;
    }

    /**
     * Obtient la valeur de la propriété connectingServiceId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectingServiceId() {
        return connectingServiceId;
    }

    /**
     * Définit la valeur de la propriété connectingServiceId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectingServiceId(String value) {
        this.connectingServiceId = value;
    }

    /**
     * Obtient la valeur de la propriété arrivalTime.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Définit la valeur de la propriété arrivalTime.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setArrivalTime(XMLGregorianCalendar value) {
        this.arrivalTime = value;
    }

    /**
     * Obtient la valeur de la propriété departureTime.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDepartureTime() {
        return departureTime;
    }

    /**
     * Définit la valeur de la propriété departureTime.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDepartureTime(XMLGregorianCalendar value) {
        this.departureTime = value;
    }

    /**
     * Obtient la valeur de la propriété waitingTime.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getWaitingTime() {
        return waitingTime;
    }

    /**
     * Définit la valeur de la propriété waitingTime.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setWaitingTime(XMLGregorianCalendar value) {
        this.waitingTime = value;
    }

    /**
     * Obtient la valeur de la propriété elapseDuration.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getElapseDuration() {
        return elapseDuration;
    }

    /**
     * Définit la valeur de la propriété elapseDuration.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setElapseDuration(Duration value) {
        this.elapseDuration = value;
    }

    /**
     * Obtient la valeur de la propriété headwayFrequency.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getHeadwayFrequency() {
        return headwayFrequency;
    }

    /**
     * Définit la valeur de la propriété headwayFrequency.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setHeadwayFrequency(Duration value) {
        this.headwayFrequency = value;
    }

    /**
     * Obtient la valeur de la propriété boardingAlightingPossibility.
     * 
     * @return
     *     possible object is
     *     {@link BoardingAlightingPossibilityType }
     *     
     */
    public BoardingAlightingPossibilityType getBoardingAlightingPossibility() {
        return boardingAlightingPossibility;
    }

    /**
     * Définit la valeur de la propriété boardingAlightingPossibility.
     * 
     * @param value
     *     allowed object is
     *     {@link BoardingAlightingPossibilityType }
     *     
     */
    public void setBoardingAlightingPossibility(BoardingAlightingPossibilityType value) {
        this.boardingAlightingPossibility = value;
    }

    /**
     * Obtient la valeur de la propriété order.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getOrder() {
        return order;
    }

    /**
     * Définit la valeur de la propriété order.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setOrder(BigInteger value) {
        this.order = value;
    }

}
