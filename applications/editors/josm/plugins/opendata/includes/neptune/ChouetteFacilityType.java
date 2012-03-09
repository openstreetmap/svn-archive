//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Description des equipement situés dans les zones d'arrêt ou les vehicules (via la ligne)
 * 
 * <p>Classe Java pour ChouetteFacilityType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ChouetteFacilityType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}TridentObjectType">
 *       &lt;sequence>
 *         &lt;element name="facilityLocation" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="longitude" type="{http://www.trident.org/schema/trident}LongitudeType"/>
 *                   &lt;element name="latitude" type="{http://www.trident.org/schema/trident}LatitudeType"/>
 *                   &lt;element name="longLatType" type="{http://www.trident.org/schema/trident}LongLatTypeType"/>
 *                   &lt;element name="address" type="{http://www.trident.org/schema/trident}AddressType" minOccurs="0"/>
 *                   &lt;element name="projectedPoint" type="{http://www.trident.org/schema/trident}ProjectedPointType" minOccurs="0"/>
 *                   &lt;element name="containedIn" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;choice>
 *           &lt;element name="stopAreaId" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *           &lt;element name="lineId" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *           &lt;element name="connectionLinkId" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *           &lt;element name="stopPointId" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *         &lt;/choice>
 *         &lt;element name="facilityFeature" type="{http://www.siri.org.uk/siri}AllFacilitiesFeatureStructure" maxOccurs="unbounded"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="freeAccess" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChouetteFacilityType", propOrder = {
    "facilityLocation",
    "stopAreaId",
    "lineId",
    "connectionLinkId",
    "stopPointId",
    "facilityFeature",
    "name",
    "description",
    "freeAccess",
    "comment"
})
public class ChouetteFacilityType
    extends TridentObjectType
{

    protected ChouetteFacilityType.FacilityLocation facilityLocation;
    protected String stopAreaId;
    protected String lineId;
    protected String connectionLinkId;
    protected String stopPointId;
    @XmlElement(required = true)
    protected List<AllFacilitiesFeatureStructure> facilityFeature;
    protected String name;
    protected String description;
    protected Boolean freeAccess;
    protected String comment;

    /**
     * Obtient la valeur de la propriété facilityLocation.
     * 
     * @return
     *     possible object is
     *     {@link ChouetteFacilityType.FacilityLocation }
     *     
     */
    public ChouetteFacilityType.FacilityLocation getFacilityLocation() {
        return facilityLocation;
    }

    /**
     * Définit la valeur de la propriété facilityLocation.
     * 
     * @param value
     *     allowed object is
     *     {@link ChouetteFacilityType.FacilityLocation }
     *     
     */
    public void setFacilityLocation(ChouetteFacilityType.FacilityLocation value) {
        this.facilityLocation = value;
    }

    /**
     * Obtient la valeur de la propriété stopAreaId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStopAreaId() {
        return stopAreaId;
    }

    /**
     * Définit la valeur de la propriété stopAreaId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStopAreaId(String value) {
        this.stopAreaId = value;
    }

    /**
     * Obtient la valeur de la propriété lineId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineId() {
        return lineId;
    }

    /**
     * Définit la valeur de la propriété lineId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineId(String value) {
        this.lineId = value;
    }

    /**
     * Obtient la valeur de la propriété connectionLinkId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectionLinkId() {
        return connectionLinkId;
    }

    /**
     * Définit la valeur de la propriété connectionLinkId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConnectionLinkId(String value) {
        this.connectionLinkId = value;
    }

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
     * Gets the value of the facilityFeature property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the facilityFeature property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFacilityFeature().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AllFacilitiesFeatureStructure }
     * 
     * 
     */
    public List<AllFacilitiesFeatureStructure> getFacilityFeature() {
        if (facilityFeature == null) {
            facilityFeature = new ArrayList<AllFacilitiesFeatureStructure>();
        }
        return this.facilityFeature;
    }

    /**
     * Obtient la valeur de la propriété name.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Définit la valeur de la propriété name.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Obtient la valeur de la propriété description.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Définit la valeur de la propriété description.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Obtient la valeur de la propriété freeAccess.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFreeAccess() {
        return freeAccess;
    }

    /**
     * Définit la valeur de la propriété freeAccess.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFreeAccess(Boolean value) {
        this.freeAccess = value;
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
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="longitude" type="{http://www.trident.org/schema/trident}LongitudeType"/>
     *         &lt;element name="latitude" type="{http://www.trident.org/schema/trident}LatitudeType"/>
     *         &lt;element name="longLatType" type="{http://www.trident.org/schema/trident}LongLatTypeType"/>
     *         &lt;element name="address" type="{http://www.trident.org/schema/trident}AddressType" minOccurs="0"/>
     *         &lt;element name="projectedPoint" type="{http://www.trident.org/schema/trident}ProjectedPointType" minOccurs="0"/>
     *         &lt;element name="containedIn" type="{http://www.trident.org/schema/trident}TridentIdType"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "longitude",
        "latitude",
        "longLatType",
        "address",
        "projectedPoint",
        "containedIn"
    })
    public static class FacilityLocation {

        @XmlElement(required = true)
        protected BigDecimal longitude;
        @XmlElement(required = true)
        protected BigDecimal latitude;
        @XmlElement(required = true)
        protected LongLatTypeType longLatType;
        protected AddressType address;
        protected ProjectedPointType projectedPoint;
        @XmlElement(required = true)
        protected String containedIn;

        /**
         * Obtient la valeur de la propriété longitude.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getLongitude() {
            return longitude;
        }

        /**
         * Définit la valeur de la propriété longitude.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setLongitude(BigDecimal value) {
            this.longitude = value;
        }

        /**
         * Obtient la valeur de la propriété latitude.
         * 
         * @return
         *     possible object is
         *     {@link BigDecimal }
         *     
         */
        public BigDecimal getLatitude() {
            return latitude;
        }

        /**
         * Définit la valeur de la propriété latitude.
         * 
         * @param value
         *     allowed object is
         *     {@link BigDecimal }
         *     
         */
        public void setLatitude(BigDecimal value) {
            this.latitude = value;
        }

        /**
         * Obtient la valeur de la propriété longLatType.
         * 
         * @return
         *     possible object is
         *     {@link LongLatTypeType }
         *     
         */
        public LongLatTypeType getLongLatType() {
            return longLatType;
        }

        /**
         * Définit la valeur de la propriété longLatType.
         * 
         * @param value
         *     allowed object is
         *     {@link LongLatTypeType }
         *     
         */
        public void setLongLatType(LongLatTypeType value) {
            this.longLatType = value;
        }

        /**
         * Obtient la valeur de la propriété address.
         * 
         * @return
         *     possible object is
         *     {@link AddressType }
         *     
         */
        public AddressType getAddress() {
            return address;
        }

        /**
         * Définit la valeur de la propriété address.
         * 
         * @param value
         *     allowed object is
         *     {@link AddressType }
         *     
         */
        public void setAddress(AddressType value) {
            this.address = value;
        }

        /**
         * Obtient la valeur de la propriété projectedPoint.
         * 
         * @return
         *     possible object is
         *     {@link ProjectedPointType }
         *     
         */
        public ProjectedPointType getProjectedPoint() {
            return projectedPoint;
        }

        /**
         * Définit la valeur de la propriété projectedPoint.
         * 
         * @param value
         *     allowed object is
         *     {@link ProjectedPointType }
         *     
         */
        public void setProjectedPoint(ProjectedPointType value) {
            this.projectedPoint = value;
        }

        /**
         * Obtient la valeur de la propriété containedIn.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getContainedIn() {
            return containedIn;
        }

        /**
         * Définit la valeur de la propriété containedIn.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setContainedIn(String value) {
            this.containedIn = value;
        }

    }

}
