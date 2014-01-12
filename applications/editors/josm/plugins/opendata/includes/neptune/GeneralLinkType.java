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
 * 		A General Link Between two Points (or object inheriting 
 * from Point)
 * 			
 * 
 * <p>Classe Java pour GeneralLinkType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="GeneralLinkType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}LocationType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="startOfLink" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *         &lt;element name="endOfLink" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *         &lt;element name="linkDistance" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GeneralLinkType", propOrder = {
    "name",
    "startOfLink",
    "endOfLink",
    "linkDistance"
})
@XmlSeeAlso({
    CarType.class,
    PTLinkType.class,
    ConnectionLinkType.class
})
public class GeneralLinkType
    extends LocationType
{

    protected String name;
    @XmlElement(required = true)
    protected String startOfLink;
    @XmlElement(required = true)
    protected String endOfLink;
    protected BigDecimal linkDistance;

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
     * Obtient la valeur de la propriété startOfLink.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartOfLink() {
        return startOfLink;
    }

    /**
     * Définit la valeur de la propriété startOfLink.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartOfLink(String value) {
        this.startOfLink = value;
    }

    /**
     * Obtient la valeur de la propriété endOfLink.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndOfLink() {
        return endOfLink;
    }

    /**
     * Définit la valeur de la propriété endOfLink.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndOfLink(String value) {
        this.endOfLink = value;
    }

    /**
     * Obtient la valeur de la propriété linkDistance.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getLinkDistance() {
        return linkDistance;
    }

    /**
     * Définit la valeur de la propriété linkDistance.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setLinkDistance(BigDecimal value) {
        this.linkDistance = value;
    }

}
