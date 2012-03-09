//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;


/**
 * 
 * 		The path between two places covered by any "personal" mean of transport 
 * 			
 * 
 * <p>Classe Java pour ConnectionLinkType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ConnectionLinkType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}GeneralLinkType">
 *       &lt;sequence>
 *         &lt;element name="linkType" type="{http://www.trident.org/schema/trident}ConnectionLinkTypeType" minOccurs="0"/>
 *         &lt;element name="defaultDuration" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/>
 *         &lt;element name="frequentTravellerDuration" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/>
 *         &lt;element name="occasionalTravellerDuration" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/>
 *         &lt;element name="mobilityRestrictedTravellerDuration" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/>
 *         &lt;element name="mobilityRestrictedSuitability" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="stairsAvailability" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="liftAvailability" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
@XmlType(name = "ConnectionLinkType", propOrder = {
    "linkType",
    "defaultDuration",
    "frequentTravellerDuration",
    "occasionalTravellerDuration",
    "mobilityRestrictedTravellerDuration",
    "mobilityRestrictedSuitability",
    "stairsAvailability",
    "liftAvailability",
    "comment"
})
@XmlSeeAlso({
    neptune.ChouettePTNetworkType.ConnectionLink.class,
    PTAccessLinkType.class,
    NonPTAccessLinkType.class
})
public class ConnectionLinkType
    extends GeneralLinkType
{

    protected ConnectionLinkTypeType linkType;
    protected Duration defaultDuration;
    protected Duration frequentTravellerDuration;
    protected Duration occasionalTravellerDuration;
    protected Duration mobilityRestrictedTravellerDuration;
    protected Boolean mobilityRestrictedSuitability;
    protected Boolean stairsAvailability;
    protected Boolean liftAvailability;
    protected String comment;

    /**
     * Obtient la valeur de la propriété linkType.
     * 
     * @return
     *     possible object is
     *     {@link ConnectionLinkTypeType }
     *     
     */
    public ConnectionLinkTypeType getLinkType() {
        return linkType;
    }

    /**
     * Définit la valeur de la propriété linkType.
     * 
     * @param value
     *     allowed object is
     *     {@link ConnectionLinkTypeType }
     *     
     */
    public void setLinkType(ConnectionLinkTypeType value) {
        this.linkType = value;
    }

    /**
     * Obtient la valeur de la propriété defaultDuration.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getDefaultDuration() {
        return defaultDuration;
    }

    /**
     * Définit la valeur de la propriété defaultDuration.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setDefaultDuration(Duration value) {
        this.defaultDuration = value;
    }

    /**
     * Obtient la valeur de la propriété frequentTravellerDuration.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getFrequentTravellerDuration() {
        return frequentTravellerDuration;
    }

    /**
     * Définit la valeur de la propriété frequentTravellerDuration.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setFrequentTravellerDuration(Duration value) {
        this.frequentTravellerDuration = value;
    }

    /**
     * Obtient la valeur de la propriété occasionalTravellerDuration.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getOccasionalTravellerDuration() {
        return occasionalTravellerDuration;
    }

    /**
     * Définit la valeur de la propriété occasionalTravellerDuration.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setOccasionalTravellerDuration(Duration value) {
        this.occasionalTravellerDuration = value;
    }

    /**
     * Obtient la valeur de la propriété mobilityRestrictedTravellerDuration.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getMobilityRestrictedTravellerDuration() {
        return mobilityRestrictedTravellerDuration;
    }

    /**
     * Définit la valeur de la propriété mobilityRestrictedTravellerDuration.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setMobilityRestrictedTravellerDuration(Duration value) {
        this.mobilityRestrictedTravellerDuration = value;
    }

    /**
     * Obtient la valeur de la propriété mobilityRestrictedSuitability.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMobilityRestrictedSuitability() {
        return mobilityRestrictedSuitability;
    }

    /**
     * Définit la valeur de la propriété mobilityRestrictedSuitability.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMobilityRestrictedSuitability(Boolean value) {
        this.mobilityRestrictedSuitability = value;
    }

    /**
     * Obtient la valeur de la propriété stairsAvailability.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isStairsAvailability() {
        return stairsAvailability;
    }

    /**
     * Définit la valeur de la propriété stairsAvailability.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setStairsAvailability(Boolean value) {
        this.stairsAvailability = value;
    }

    /**
     * Obtient la valeur de la propriété liftAvailability.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLiftAvailability() {
        return liftAvailability;
    }

    /**
     * Définit la valeur de la propriété liftAvailability.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLiftAvailability(Boolean value) {
        this.liftAvailability = value;
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

}
