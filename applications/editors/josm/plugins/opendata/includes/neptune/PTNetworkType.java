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
 * PT Network description, and link to all the entry point
 * for this network in the Data Model.
 * 			
 * 
 * <p>Classe Java pour PTNetworkType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="PTNetworkType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}TransportNetworkType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="registration" type="{http://www.trident.org/schema/trident}RegistrationType" minOccurs="0"/>
 *         &lt;element name="sourceName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sourceIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sourceType" type="{http://www.trident.org/schema/trident}SourceTypeType" minOccurs="0"/>
 *         &lt;element name="lineId" type="{http://www.trident.org/schema/trident}TridentIdType" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "PTNetworkType", propOrder = {
    "name",
    "registration",
    "sourceName",
    "sourceIdentifier",
    "sourceType",
    "lineId",
    "comment"
})
public class PTNetworkType
    extends TransportNetworkType
{

    @XmlElement(required = true)
    protected String name;
    protected RegistrationType registration;
    protected String sourceName;
    protected String sourceIdentifier;
    protected SourceTypeType sourceType;
    protected List<String> lineId;
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
     * Obtient la valeur de la propriété sourceName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Définit la valeur de la propriété sourceName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceName(String value) {
        this.sourceName = value;
    }

    /**
     * Obtient la valeur de la propriété sourceIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    /**
     * Définit la valeur de la propriété sourceIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceIdentifier(String value) {
        this.sourceIdentifier = value;
    }

    /**
     * Obtient la valeur de la propriété sourceType.
     * 
     * @return
     *     possible object is
     *     {@link SourceTypeType }
     *     
     */
    public SourceTypeType getSourceType() {
        return sourceType;
    }

    /**
     * Définit la valeur de la propriété sourceType.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceTypeType }
     *     
     */
    public void setSourceType(SourceTypeType value) {
        this.sourceType = value;
    }

    /**
     * Gets the value of the lineId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lineId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLineId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getLineId() {
        if (lineId == null) {
            lineId = new ArrayList<String>();
        }
        return this.lineId;
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
