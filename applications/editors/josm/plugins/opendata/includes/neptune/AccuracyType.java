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
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				Accuracy of a measure
 * 			
 * 
 * <p>Classe Java pour AccuracyType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="AccuracyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="standardDeviation" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="accuracy" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="dataClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="accuracyRange" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccuracyType", propOrder = {
    "standardDeviation",
    "accuracy",
    "dataClass",
    "accuracyRange"
})
public class AccuracyType {

    protected BigDecimal standardDeviation;
    protected BigDecimal accuracy;
    protected String dataClass;
    protected String accuracyRange;

    /**
     * Obtient la valeur de la propriété standardDeviation.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * Définit la valeur de la propriété standardDeviation.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setStandardDeviation(BigDecimal value) {
        this.standardDeviation = value;
    }

    /**
     * Obtient la valeur de la propriété accuracy.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAccuracy() {
        return accuracy;
    }

    /**
     * Définit la valeur de la propriété accuracy.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAccuracy(BigDecimal value) {
        this.accuracy = value;
    }

    /**
     * Obtient la valeur de la propriété dataClass.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataClass() {
        return dataClass;
    }

    /**
     * Définit la valeur de la propriété dataClass.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataClass(String value) {
        this.dataClass = value;
    }

    /**
     * Obtient la valeur de la propriété accuracyRange.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccuracyRange() {
        return accuracyRange;
    }

    /**
     * Définit la valeur de la propriété accuracyRange.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccuracyRange(String value) {
        this.accuracyRange = value;
    }

}
