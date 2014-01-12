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
 * A line is a set of Route _.
 * 			
 * 
 * <p>Classe Java pour LineType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="LineType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}LogicalLocationType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="number" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="publishedName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transportModeName" type="{http://www.trident.org/schema/trident}TransportModeNameType" minOccurs="0"/>
 *         &lt;element name="lineEnd" type="{http://www.trident.org/schema/trident}TridentIdType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="routeId" type="{http://www.trident.org/schema/trident}TridentIdType" maxOccurs="unbounded"/>
 *         &lt;element name="registration" type="{http://www.trident.org/schema/trident}RegistrationType" minOccurs="0"/>
 *         &lt;element name="ptNetworkIdShortcut" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
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
@XmlType(name = "LineType", propOrder = {
    "name",
    "number",
    "publishedName",
    "transportModeName",
    "lineEnd",
    "routeId",
    "registration",
    "ptNetworkIdShortcut",
    "comment"
})
@XmlSeeAlso({
    neptune.ChouettePTNetworkType.ChouetteLineDescription.Line.class
})
public class LineType
    extends LogicalLocationType
{

    protected String name;
    protected String number;
    protected String publishedName;
    protected TransportModeNameType transportModeName;
    protected List<String> lineEnd;
    @XmlElement(required = true)
    protected List<String> routeId;
    protected RegistrationType registration;
    protected String ptNetworkIdShortcut;
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
     * Obtient la valeur de la propriété transportModeName.
     * 
     * @return
     *     possible object is
     *     {@link TransportModeNameType }
     *     
     */
    public TransportModeNameType getTransportModeName() {
        return transportModeName;
    }

    /**
     * Définit la valeur de la propriété transportModeName.
     * 
     * @param value
     *     allowed object is
     *     {@link TransportModeNameType }
     *     
     */
    public void setTransportModeName(TransportModeNameType value) {
        this.transportModeName = value;
    }

    /**
     * Gets the value of the lineEnd property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lineEnd property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLineEnd().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLineEnd() {
        if (lineEnd == null) {
            lineEnd = new ArrayList<String>();
        }
        return this.lineEnd;
    }

    /**
     * Gets the value of the routeId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the routeId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRouteId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getRouteId() {
        if (routeId == null) {
            routeId = new ArrayList<String>();
        }
        return this.routeId;
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
     * Obtient la valeur de la propriété ptNetworkIdShortcut.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPtNetworkIdShortcut() {
        return ptNetworkIdShortcut;
    }

    /**
     * Définit la valeur de la propriété ptNetworkIdShortcut.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPtNetworkIdShortcut(String value) {
        this.ptNetworkIdShortcut = value;
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
