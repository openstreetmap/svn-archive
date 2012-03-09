//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * 
 * 		The physical (spatial) possibility for a passenger
 * 		to access or leave the PT network.
 * 			
 * 
 * <p>Classe Java pour PTAccessPointType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="PTAccessPointType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}PointType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="In"/>
 *               &lt;enumeration value="Out"/>
 *               &lt;enumeration value="InOut"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="openingTime" type="{http://www.w3.org/2001/XMLSchema}time" minOccurs="0"/>
 *         &lt;element name="closingTime" type="{http://www.w3.org/2001/XMLSchema}time" minOccurs="0"/>
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
@XmlType(name = "PTAccessPointType", propOrder = {
    "name",
    "type",
    "openingTime",
    "closingTime",
    "mobilityRestrictedSuitability",
    "stairsAvailability",
    "liftAvailability",
    "comment"
})
public class PTAccessPointType
    extends PointType
{

    protected String name;
    protected String type;
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar openingTime;
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar closingTime;
    protected Boolean mobilityRestrictedSuitability;
    protected Boolean stairsAvailability;
    protected Boolean liftAvailability;
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
     * Obtient la valeur de la propriété type.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Définit la valeur de la propriété type.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Obtient la valeur de la propriété openingTime.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOpeningTime() {
        return openingTime;
    }

    /**
     * Définit la valeur de la propriété openingTime.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOpeningTime(XMLGregorianCalendar value) {
        this.openingTime = value;
    }

    /**
     * Obtient la valeur de la propriété closingTime.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getClosingTime() {
        return closingTime;
    }

    /**
     * Définit la valeur de la propriété closingTime.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setClosingTime(XMLGregorianCalendar value) {
        this.closingTime = value;
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
