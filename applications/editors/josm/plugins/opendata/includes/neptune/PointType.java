//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		General point used to build any kind of point
 * 			
 * 
 * <p>Classe Java pour PointType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="PointType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}LocationType">
 *       &lt;sequence>
 *         &lt;element name="longitude" type="{http://www.trident.org/schema/trident}LongitudeType"/>
 *         &lt;element name="latitude" type="{http://www.trident.org/schema/trident}LatitudeType"/>
 *         &lt;element name="longLatType" type="{http://www.trident.org/schema/trident}LongLatTypeType"/>
 *         &lt;element name="address" type="{http://www.trident.org/schema/trident}AddressType" minOccurs="0"/>
 *         &lt;element name="projectedPoint" type="{http://www.trident.org/schema/trident}ProjectedPointType" minOccurs="0"/>
 *         &lt;element name="containedIn" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PointType", propOrder = {
    "longitude",
    "latitude",
    "longLatType",
    "address",
    "projectedPoint",
    "containedIn"
})
@XmlSeeAlso({
    PlaceType.class,
    PTAccessPointType.class,
    StopPointType.class,
    RoadPointType.class,
    NonPTAccessLinkendType.class
})
public abstract class PointType
    extends LocationType
{

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
