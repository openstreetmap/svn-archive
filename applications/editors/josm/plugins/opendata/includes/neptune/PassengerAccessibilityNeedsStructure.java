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
 * Type for accessibility needs. Records the requirementrs of a passenger that may affect chocie of facilities
 * 
 * <p>Classe Java pour PassengerAccessibilityNeedsStructure complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="PassengerAccessibilityNeedsStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UserNeed" type="{http://www.ifopt.org.uk/acsb}UserNeedStructure" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="AccompaniedByCarer" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PassengerAccessibilityNeedsStructure", namespace = "http://www.ifopt.org.uk/acsb", propOrder = {
    "userNeed",
    "accompaniedByCarer"
})
public class PassengerAccessibilityNeedsStructure {

    @XmlElement(name = "UserNeed")
    protected List<UserNeedStructure> userNeed;
    @XmlElement(name = "AccompaniedByCarer")
    protected Boolean accompaniedByCarer;

    /**
     * Gets the value of the userNeed property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userNeed property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserNeed().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UserNeedStructure }
     * 
     * 
     */
    public List<UserNeedStructure> getUserNeed() {
        if (userNeed == null) {
            userNeed = new ArrayList<UserNeedStructure>();
        }
        return this.userNeed;
    }

    /**
     * Obtient la valeur de la propriété accompaniedByCarer.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAccompaniedByCarer() {
        return accompaniedByCarer;
    }

    /**
     * Définit la valeur de la propriété accompaniedByCarer.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAccompaniedByCarer(Boolean value) {
        this.accompaniedByCarer = value;
    }

}
