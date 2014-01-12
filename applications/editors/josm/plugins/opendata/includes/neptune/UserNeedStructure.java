//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Type for of a specific need
 * 
 * <p>Classe Java pour UserNeedStructure complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="UserNeedStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://www.ifopt.org.uk/acsb}UserNeedGroup"/>
 *         &lt;element name="Excluded" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="NeedRanking" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="Extensions" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserNeedStructure", namespace = "http://www.ifopt.org.uk/acsb", propOrder = {
    "mobilityNeed",
    "psychosensoryNeed",
    "medicalNeed",
    "encumbranceNeed",
    "excluded",
    "needRanking",
    "extensions"
})
public class UserNeedStructure {

    @XmlElement(name = "MobilityNeed")
    protected MobilityEnumeration mobilityNeed;
    @XmlElement(name = "PsychosensoryNeed")
    protected PyschosensoryNeedEnumeration psychosensoryNeed;
    @XmlElement(name = "MedicalNeed")
    protected MedicalNeedEnumeration medicalNeed;
    @XmlElement(name = "EncumbranceNeed")
    protected EncumbranceEnumeration encumbranceNeed;
    @XmlElement(name = "Excluded")
    protected Boolean excluded;
    @XmlElement(name = "NeedRanking")
    protected BigInteger needRanking;
    @XmlElement(name = "Extensions")
    protected Object extensions;

    /**
     * Obtient la valeur de la propriété mobilityNeed.
     * 
     * @return
     *     possible object is
     *     {@link MobilityEnumeration }
     *     
     */
    public MobilityEnumeration getMobilityNeed() {
        return mobilityNeed;
    }

    /**
     * Définit la valeur de la propriété mobilityNeed.
     * 
     * @param value
     *     allowed object is
     *     {@link MobilityEnumeration }
     *     
     */
    public void setMobilityNeed(MobilityEnumeration value) {
        this.mobilityNeed = value;
    }

    /**
     * Obtient la valeur de la propriété psychosensoryNeed.
     * 
     * @return
     *     possible object is
     *     {@link PyschosensoryNeedEnumeration }
     *     
     */
    public PyschosensoryNeedEnumeration getPsychosensoryNeed() {
        return psychosensoryNeed;
    }

    /**
     * Définit la valeur de la propriété psychosensoryNeed.
     * 
     * @param value
     *     allowed object is
     *     {@link PyschosensoryNeedEnumeration }
     *     
     */
    public void setPsychosensoryNeed(PyschosensoryNeedEnumeration value) {
        this.psychosensoryNeed = value;
    }

    /**
     * Obtient la valeur de la propriété medicalNeed.
     * 
     * @return
     *     possible object is
     *     {@link MedicalNeedEnumeration }
     *     
     */
    public MedicalNeedEnumeration getMedicalNeed() {
        return medicalNeed;
    }

    /**
     * Définit la valeur de la propriété medicalNeed.
     * 
     * @param value
     *     allowed object is
     *     {@link MedicalNeedEnumeration }
     *     
     */
    public void setMedicalNeed(MedicalNeedEnumeration value) {
        this.medicalNeed = value;
    }

    /**
     * Obtient la valeur de la propriété encumbranceNeed.
     * 
     * @return
     *     possible object is
     *     {@link EncumbranceEnumeration }
     *     
     */
    public EncumbranceEnumeration getEncumbranceNeed() {
        return encumbranceNeed;
    }

    /**
     * Définit la valeur de la propriété encumbranceNeed.
     * 
     * @param value
     *     allowed object is
     *     {@link EncumbranceEnumeration }
     *     
     */
    public void setEncumbranceNeed(EncumbranceEnumeration value) {
        this.encumbranceNeed = value;
    }

    /**
     * Obtient la valeur de la propriété excluded.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isExcluded() {
        return excluded;
    }

    /**
     * Définit la valeur de la propriété excluded.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setExcluded(Boolean value) {
        this.excluded = value;
    }

    /**
     * Obtient la valeur de la propriété needRanking.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNeedRanking() {
        return needRanking;
    }

    /**
     * Définit la valeur de la propriété needRanking.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNeedRanking(BigInteger value) {
        this.needRanking = value;
    }

    /**
     * Obtient la valeur de la propriété extensions.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getExtensions() {
        return extensions;
    }

    /**
     * Définit la valeur de la propriété extensions.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setExtensions(Object value) {
        this.extensions = value;
    }

}
