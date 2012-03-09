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
 * 		An area made up of a set of Points
 * 			
 * 
 * <p>Classe Java pour AreaType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="AreaType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}LocationType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contains" type="{http://www.trident.org/schema/trident}TridentIdType" maxOccurs="unbounded"/>
 *         &lt;element name="boundaryPoint" type="{http://www.trident.org/schema/trident}TridentIdType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="centroidOfArea" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AreaType", propOrder = {
    "name",
    "contains",
    "boundaryPoint",
    "centroidOfArea"
})
@XmlSeeAlso({
    StopAreaType.class
})
public class AreaType
    extends LocationType
{

    protected String name;
    @XmlElement(required = true)
    protected List<String> contains;
    protected List<String> boundaryPoint;
    protected String centroidOfArea;

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
     * Gets the value of the contains property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contains property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContains().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getContains() {
        if (contains == null) {
            contains = new ArrayList<String>();
        }
        return this.contains;
    }

    /**
     * Gets the value of the boundaryPoint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the boundaryPoint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBoundaryPoint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getBoundaryPoint() {
        if (boundaryPoint == null) {
            boundaryPoint = new ArrayList<String>();
        }
        return this.boundaryPoint;
    }

    /**
     * Obtient la valeur de la propriété centroidOfArea.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCentroidOfArea() {
        return centroidOfArea;
    }

    /**
     * Définit la valeur de la propriété centroidOfArea.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCentroidOfArea(String value) {
        this.centroidOfArea = value;
    }

}
