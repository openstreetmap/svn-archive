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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * Extension de ligne pour l'accessibilité aux PMR
 * 
 * <p>Classe Java pour LineExtensionType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="LineExtensionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="stableId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mobilityRestrictedSuitability" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="accessibilitySuitabilityDetails" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence maxOccurs="unbounded">
 *                   &lt;group ref="{http://www.ifopt.org.uk/acsb}UserNeedGroup"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LineExtensionType", propOrder = {
    "stableId",
    "mobilityRestrictedSuitability",
    "accessibilitySuitabilityDetails"
})
public class LineExtensionType {

    protected String stableId;
    protected Boolean mobilityRestrictedSuitability;
    protected LineExtensionType.AccessibilitySuitabilityDetails accessibilitySuitabilityDetails;

    /**
     * Obtient la valeur de la propriété stableId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStableId() {
        return stableId;
    }

    /**
     * Définit la valeur de la propriété stableId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStableId(String value) {
        this.stableId = value;
    }

    /**
     * Obtient la valeur de la propriété mobilityRestrictedSuitability.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMobilityRestrictedSuitability() {
        return mobilityRestrictedSuitability;
    }

    /**
     * Définit la valeur de la propriété mobilityRestrictedSuitability.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMobilityRestrictedSuitability(Boolean value) {
        this.mobilityRestrictedSuitability = value;
    }

    /**
     * Obtient la valeur de la propriété accessibilitySuitabilityDetails.
     * 
     * @return
     *     possible object is
     *     {@link LineExtensionType.AccessibilitySuitabilityDetails }
     *     
     */
    public LineExtensionType.AccessibilitySuitabilityDetails getAccessibilitySuitabilityDetails() {
        return accessibilitySuitabilityDetails;
    }

    /**
     * Définit la valeur de la propriété accessibilitySuitabilityDetails.
     * 
     * @param value
     *     allowed object is
     *     {@link LineExtensionType.AccessibilitySuitabilityDetails }
     *     
     */
    public void setAccessibilitySuitabilityDetails(LineExtensionType.AccessibilitySuitabilityDetails value) {
        this.accessibilitySuitabilityDetails = value;
    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence maxOccurs="unbounded">
     *         &lt;group ref="{http://www.ifopt.org.uk/acsb}UserNeedGroup"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "mobilityNeedOrPsychosensoryNeedOrMedicalNeed"
    })
    public static class AccessibilitySuitabilityDetails {

        @XmlElements({
            @XmlElement(name = "MobilityNeed", namespace = "http://www.ifopt.org.uk/acsb", type = MobilityEnumeration.class),
            @XmlElement(name = "PsychosensoryNeed", namespace = "http://www.ifopt.org.uk/acsb", type = PyschosensoryNeedEnumeration.class),
            @XmlElement(name = "MedicalNeed", namespace = "http://www.ifopt.org.uk/acsb", type = MedicalNeedEnumeration.class),
            @XmlElement(name = "EncumbranceNeed", namespace = "http://www.ifopt.org.uk/acsb", type = EncumbranceEnumeration.class)
        })
        protected List<Object> mobilityNeedOrPsychosensoryNeedOrMedicalNeed;

        /**
         * Gets the value of the mobilityNeedOrPsychosensoryNeedOrMedicalNeed property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the mobilityNeedOrPsychosensoryNeedOrMedicalNeed property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMobilityNeedOrPsychosensoryNeedOrMedicalNeed().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link MobilityEnumeration }
         * {@link PyschosensoryNeedEnumeration }
         * {@link MedicalNeedEnumeration }
         * {@link EncumbranceEnumeration }
         * 
         * 
         */
        public List<Object> getMobilityNeedOrPsychosensoryNeedOrMedicalNeed() {
            if (mobilityNeedOrPsychosensoryNeedOrMedicalNeed == null) {
                mobilityNeedOrPsychosensoryNeedOrMedicalNeed = new ArrayList<Object>();
            }
            return this.mobilityNeedOrPsychosensoryNeedOrMedicalNeed;
        }

    }

}
