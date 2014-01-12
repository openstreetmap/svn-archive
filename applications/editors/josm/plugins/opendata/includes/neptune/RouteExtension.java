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
 * Extension d'itinéraire qui en précise le sens, aller ou retour
 * 
 * <p>Classe Java pour RouteExtension complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="RouteExtension">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="wayBack" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RouteExtension", propOrder = {
    "wayBack"
})
public class RouteExtension {

    @XmlElement(required = true)
    protected String wayBack;

    /**
     * Obtient la valeur de la propriété wayBack.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWayBack() {
        return wayBack;
    }

    /**
     * Définit la valeur de la propriété wayBack.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWayBack(String value) {
        this.wayBack = value;
    }

}
