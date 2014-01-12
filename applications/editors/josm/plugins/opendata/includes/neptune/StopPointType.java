//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		StopPoint on a Route of a Line of a PT Network
 * 			
 * 
 * <p>Classe Java pour StopPointType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="StopPointType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}PointType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="lineIdShortcut" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
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
@XmlType(name = "StopPointType", propOrder = {
    "name",
    "lineIdShortcut",
    "ptNetworkIdShortcut",
    "comment"
})
@XmlSeeAlso({
    neptune.ChouettePTNetworkType.ChouetteLineDescription.StopPoint.class,
    BusStopPointType.class,
    MetroStopPointType.class,
    AirportStopPointType.class,
    TramStopPointType.class,
    RailwayStopPointType.class
})
public class StopPointType
    extends PointType
{

    @XmlElement(required = true)
    protected String name;
    protected String lineIdShortcut;
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
