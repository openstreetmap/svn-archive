//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * Instance of a Journey Pattern
 * 			
 * 
 * <p>Classe Java pour VehicleJourneyType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="VehicleJourneyType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}TridentObjectType">
 *       &lt;sequence>
 *         &lt;element name="routeId" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *         &lt;element name="journeyPatternId" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
 *         &lt;element name="publishedJourneyName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="publishedJourneyIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transportMode" type="{http://www.trident.org/schema/trident}TransportModeNameType" minOccurs="0"/>
 *         &lt;element name="vehicleTypeIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusValue" type="{http://www.trident.org/schema/trident}ServiceStatusValueType" minOccurs="0"/>
 *         &lt;element name="lineIdShortcut" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
 *         &lt;element name="routeIdShortcut" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
 *         &lt;element name="operatorId" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
 *         &lt;element name="facility" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="number" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="vehicleJourneyAtStop" type="{http://www.trident.org/schema/trident}VehicleJourneyAtStopType" maxOccurs="unbounded" minOccurs="2"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="timeSlotId" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VehicleJourneyType", propOrder = {
    "routeId",
    "journeyPatternId",
    "publishedJourneyName",
    "publishedJourneyIdentifier",
    "transportMode",
    "vehicleTypeIdentifier",
    "statusValue",
    "lineIdShortcut",
    "routeIdShortcut",
    "operatorId",
    "facility",
    "number",
    "vehicleJourneyAtStop",
    "comment",
    "timeSlotId"
})
public class VehicleJourneyType
    extends TridentObjectType
{

    @XmlElement(required = true)
    protected String routeId;
    protected String journeyPatternId;
    protected String publishedJourneyName;
    protected String publishedJourneyIdentifier;
    protected TransportModeNameType transportMode;
    protected String vehicleTypeIdentifier;
    protected ServiceStatusValueType statusValue;
    protected String lineIdShortcut;
    protected String routeIdShortcut;
    protected String operatorId;
    protected String facility;
    protected BigInteger number;
    @XmlElement(required = true)
    protected List<VehicleJourneyAtStopType> vehicleJourneyAtStop;
    protected String comment;
    protected String timeSlotId;

    /**
     * Obtient la valeur de la propriété routeId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Définit la valeur de la propriété routeId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRouteId(String value) {
        this.routeId = value;
    }

    /**
     * Obtient la valeur de la propriété journeyPatternId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJourneyPatternId() {
        return journeyPatternId;
    }

    /**
     * Définit la valeur de la propriété journeyPatternId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJourneyPatternId(String value) {
        this.journeyPatternId = value;
    }

    /**
     * Obtient la valeur de la propriété publishedJourneyName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublishedJourneyName() {
        return publishedJourneyName;
    }

    /**
     * Définit la valeur de la propriété publishedJourneyName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublishedJourneyName(String value) {
        this.publishedJourneyName = value;
    }

    /**
     * Obtient la valeur de la propriété publishedJourneyIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublishedJourneyIdentifier() {
        return publishedJourneyIdentifier;
    }

    /**
     * Définit la valeur de la propriété publishedJourneyIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublishedJourneyIdentifier(String value) {
        this.publishedJourneyIdentifier = value;
    }

    /**
     * Obtient la valeur de la propriété transportMode.
     * 
     * @return
     *     possible object is
     *     {@link TransportModeNameType }
     *     
     */
    public TransportModeNameType getTransportMode() {
        return transportMode;
    }

    /**
     * Définit la valeur de la propriété transportMode.
     * 
     * @param value
     *     allowed object is
     *     {@link TransportModeNameType }
     *     
     */
    public void setTransportMode(TransportModeNameType value) {
        this.transportMode = value;
    }

    /**
     * Obtient la valeur de la propriété vehicleTypeIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleTypeIdentifier() {
        return vehicleTypeIdentifier;
    }

    /**
     * Définit la valeur de la propriété vehicleTypeIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleTypeIdentifier(String value) {
        this.vehicleTypeIdentifier = value;
    }

    /**
     * Obtient la valeur de la propriété statusValue.
     * 
     * @return
     *     possible object is
     *     {@link ServiceStatusValueType }
     *     
     */
    public ServiceStatusValueType getStatusValue() {
        return statusValue;
    }

    /**
     * Définit la valeur de la propriété statusValue.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceStatusValueType }
     *     
     */
    public void setStatusValue(ServiceStatusValueType value) {
        this.statusValue = value;
    }

    /**
     * Obtient la valeur de la propriété lineIdShortcut.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineIdShortcut() {
        return lineIdShortcut;
    }

    /**
     * Définit la valeur de la propriété lineIdShortcut.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineIdShortcut(String value) {
        this.lineIdShortcut = value;
    }

    /**
     * Obtient la valeur de la propriété routeIdShortcut.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRouteIdShortcut() {
        return routeIdShortcut;
    }

    /**
     * Définit la valeur de la propriété routeIdShortcut.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRouteIdShortcut(String value) {
        this.routeIdShortcut = value;
    }

    /**
     * Obtient la valeur de la propriété operatorId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOperatorId() {
        return operatorId;
    }

    /**
     * Définit la valeur de la propriété operatorId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOperatorId(String value) {
        this.operatorId = value;
    }

    /**
     * Obtient la valeur de la propriété facility.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFacility() {
        return facility;
    }

    /**
     * Définit la valeur de la propriété facility.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFacility(String value) {
        this.facility = value;
    }

    /**
     * Obtient la valeur de la propriété number.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumber() {
        return number;
    }

    /**
     * Définit la valeur de la propriété number.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumber(BigInteger value) {
        this.number = value;
    }

    /**
     * Gets the value of the vehicleJourneyAtStop property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vehicleJourneyAtStop property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVehicleJourneyAtStop().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link VehicleJourneyAtStopType }
     * 
     * 
     */
    public List<VehicleJourneyAtStopType> getVehicleJourneyAtStop() {
        if (vehicleJourneyAtStop == null) {
            vehicleJourneyAtStop = new ArrayList<VehicleJourneyAtStopType>();
        }
        return this.vehicleJourneyAtStop;
    }

    /**
     * Obtient la valeur de la propriété comment.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Définit la valeur de la propriété comment.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Obtient la valeur de la propriété timeSlotId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeSlotId() {
        return timeSlotId;
    }

    /**
     * Définit la valeur de la propriété timeSlotId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeSlotId(String value) {
        this.timeSlotId = value;
    }

}
