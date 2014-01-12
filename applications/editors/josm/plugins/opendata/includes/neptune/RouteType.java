//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		An ordered list of Stop Points on wich Journey 
 * 		pattern are applied
 * 			
 * 
 * <p>Classe Java pour RouteType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="RouteType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}TridentObjectType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="publishedName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="number" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="direction" type="{http://www.trident.org/schema/trident}PTDirectionType" minOccurs="0"/>
 *         &lt;element name="ptLinkId" type="{http://www.trident.org/schema/trident}TridentIdType" maxOccurs="unbounded"/>
 *         &lt;element name="journeyPatternId" type="{http://www.trident.org/schema/trident}TridentIdType" maxOccurs="unbounded"/>
 *         &lt;element name="wayBackRouteId" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
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
@XmlType(name = "RouteType", propOrder = {
    "name",
    "publishedName",
    "number",
    "direction",
    "ptLinkId",
    "journeyPatternId",
    "wayBackRouteId",
    "comment"
})
@XmlSeeAlso({
    neptune.ChouettePTNetworkType.ChouetteLineDescription.ChouetteRoute.class
})
public class RouteType
    extends TridentObjectType
{

    protected String name;
    protected String publishedName;
    protected String number;
    protected PTDirectionType direction;
    @XmlElement(required = true)
    protected List<String> ptLinkId;
    @XmlElement(required = true)
    protected List<String> journeyPatternId;
    protected String wayBackRouteId;
    protected String comment;

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
     * Obtient la valeur de la propriété publishedName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublishedName() {
        return publishedName;
    }

    /**
     * Définit la valeur de la propriété publishedName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublishedName(String value) {
        this.publishedName = value;
    }

    /**
     * Obtient la valeur de la propriété number.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumber() {
        return number;
    }

    /**
     * Définit la valeur de la propriété number.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumber(String value) {
        this.number = value;
    }

    /**
     * Obtient la valeur de la propriété direction.
     * 
     * @return
     *     possible object is
     *     {@link PTDirectionType }
     *     
     */
    public PTDirectionType getDirection() {
        return direction;
    }

    /**
     * Définit la valeur de la propriété direction.
     * 
     * @param value
     *     allowed object is
     *     {@link PTDirectionType }
     *     
     */
    public void setDirection(PTDirectionType value) {
        this.direction = value;
    }

    /**
     * Gets the value of the ptLinkId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ptLinkId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPtLinkId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getPtLinkId() {
        if (ptLinkId == null) {
            ptLinkId = new ArrayList<String>();
        }
        return this.ptLinkId;
    }

    /**
     * Gets the value of the journeyPatternId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the journeyPatternId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJourneyPatternId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getJourneyPatternId() {
        if (journeyPatternId == null) {
            journeyPatternId = new ArrayList<String>();
        }
        return this.journeyPatternId;
    }

    /**
     * Obtient la valeur de la propriété wayBackRouteId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWayBackRouteId() {
        return wayBackRouteId;
    }

    /**
     * Définit la valeur de la propriété wayBackRouteId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWayBackRouteId(String value) {
        this.wayBackRouteId = value;
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
