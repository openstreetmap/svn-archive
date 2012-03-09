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
 * <p>Classe Java pour RoadNetworkType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="RoadNetworkType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}TransportNetworkType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="JunctionId" type="{http://www.trident.org/schema/trident}TridentIdType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="RoadElementId" type="{http://www.trident.org/schema/trident}TridentIdType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoadNetworkType", propOrder = {
    "name",
    "junctionId",
    "roadElementId",
    "comment"
})
public class RoadNetworkType
    extends TransportNetworkType
{

    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "JunctionId")
    protected List<String> junctionId;
    @XmlElement(name = "RoadElementId")
    protected List<String> roadElementId;
    @XmlElement(name = "Comment")
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
     * Gets the value of the junctionId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the junctionId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJunctionId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getJunctionId() {
        if (junctionId == null) {
            junctionId = new ArrayList<String>();
        }
        return this.junctionId;
    }

    /**
     * Gets the value of the roadElementId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the roadElementId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRoadElementId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getRoadElementId() {
        if (roadElementId == null) {
            roadElementId = new ArrayList<String>();
        }
        return this.roadElementId;
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
