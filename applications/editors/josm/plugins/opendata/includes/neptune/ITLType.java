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
import javax.xml.bind.annotation.XmlType;


/**
 * Interdiction de trafic local
 * 
 * <p>Classe Java pour ITLType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ITLType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="areaId" type="{http://www.trident.org/schema/trident}TridentIdType"/>
 *         &lt;element name="lineIdShortCut" type="{http://www.trident.org/schema/trident}TridentIdType" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ITLType", propOrder = {
    "areaId",
    "lineIdShortCut",
    "name"
})
public class ITLType {

    @XmlElement(required = true)
    protected String areaId;
    protected String lineIdShortCut;
    @XmlElement(required = true)
    protected String name;

    /**
     * Obtient la valeur de la propriété areaId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAreaId() {
        return areaId;
    }

    /**
     * Définit la valeur de la propriété areaId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAreaId(String value) {
        this.areaId = value;
    }

    /**
     * Obtient la valeur de la propriété lineIdShortCut.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineIdShortCut() {
        return lineIdShortCut;
    }

    /**
     * Définit la valeur de la propriété lineIdShortCut.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineIdShortCut(String value) {
        this.lineIdShortCut = value;
    }

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

}
