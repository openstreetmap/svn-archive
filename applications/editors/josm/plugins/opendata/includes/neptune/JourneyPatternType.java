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
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		Basically, JourneyPattern are some ordered list of 
 * Stop Points, but these StopPoints have to be linked 
 * together (by couples)
 * 			
 * 
 * <p>Classe Java pour JourneyPatternType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="JourneyPatternType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}TridentObjectType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="publishedName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="routeId" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *         &lt;element name="origin" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
 *         &lt;element name="destination" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
 *         &lt;element name="stopPointList" type="{http://www.trident.org/schema/trident}TridentIdType" maxOccurs="unbounded" minOccurs="2"/>
 *         &lt;element name="registration" type="{http://www.trident.org/schema/trident}RegistrationType" minOccurs="0"/>
 *         &lt;element name="lineIdShortcut" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
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
@XmlType(name = "JourneyPatternType", propOrder = {
    "name",
    "publishedName",
    "routeId",
    "origin",
    "destination",
    "stopPointList",
    "registration",
    "lineIdShortcut",
    "comment"
})
public class JourneyPatternType
    extends TridentObjectType
{

    protected String name;
    protected String publishedName;
    @XmlElement(required = true)
    protected String routeId;
    protected String origin;
    protected String destination;
    @XmlElement(required = true)
    protected List<String> stopPointList;
    protected RegistrationType registration;
    protected String lineIdShortcut;
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
     * Obtient la valeur de la propriété origin.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Définit la valeur de la propriété origin.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /**
     * Obtient la valeur de la propriété destination.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Définit la valeur de la propriété destination.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestination(String value) {
        this.destination = value;
    }

    /**
     * Gets the value of the stopPointList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stopPointList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStopPointList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getStopPointList() {
        if (stopPointList == null) {
            stopPointList = new ArrayList<String>();
        }
        return this.stopPointList;
    }

    /**
     * Obtient la valeur de la propriété registration.
     * 
     * @return
     *     possible object is
     *     {@link RegistrationType }
     *     
     */
    public RegistrationType getRegistration() {
        return registration;
    }

    /**
     * Définit la valeur de la propriété registration.
     * 
     * @param value
     *     allowed object is
     *     {@link RegistrationType }
     *     
     */
    public void setRegistration(RegistrationType value) {
        this.registration = value;
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
