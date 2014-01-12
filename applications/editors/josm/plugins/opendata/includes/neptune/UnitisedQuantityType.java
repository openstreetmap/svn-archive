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
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * Value with its type 
 * for road trafic_
 * 			
 * 
 * <p>Classe Java pour UnitisedQuantityType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="UnitisedQuantityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="unit" type="{http://www.trident.org/schema/trident}UnitType"/>
 *         &lt;element name="accuracy" type="{http://www.trident.org/schema/trident}AccuracyType" minOccurs="0"/>
 *         &lt;element name="measurementTime" type="{http://www.trident.org/schema/trident}MeasurementTimeType" minOccurs="0"/>
 *         &lt;element name="measurementLocation" type="{http://www.trident.org/schema/trident}LocationType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnitisedQuantityType", propOrder = {
    "value",
    "unit",
    "accuracy",
    "measurementTime",
    "measurementLocation"
})
public class UnitisedQuantityType {

    @XmlElement(required = true)
    protected BigDecimal value;
    @XmlElement(required = true)
    protected UnitType unit;
    protected AccuracyType accuracy;
    protected MeasurementTimeType measurementTime;
    protected LocationType measurementLocation;

    /**
     * Obtient la valeur de la propriété value.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Définit la valeur de la propriété value.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /**
     * Obtient la valeur de la propriété unit.
     * 
     * @return
     *     possible object is
     *     {@link UnitType }
     *     
     */
    public UnitType getUnit() {
        return unit;
    }

    /**
     * Définit la valeur de la propriété unit.
     * 
     * @param value
     *     allowed object is
     *     {@link UnitType }
     *     
     */
    public void setUnit(UnitType value) {
        this.unit = value;
    }

    /**
     * Obtient la valeur de la propriété accuracy.
     * 
     * @return
     *     possible object is
     *     {@link AccuracyType }
     *     
     */
    public AccuracyType getAccuracy() {
        return accuracy;
    }

    /**
     * Définit la valeur de la propriété accuracy.
     * 
     * @param value
     *     allowed object is
     *     {@link AccuracyType }
     *     
     */
    public void setAccuracy(AccuracyType value) {
        this.accuracy = value;
    }

    /**
     * Obtient la valeur de la propriété measurementTime.
     * 
     * @return
     *     possible object is
     *     {@link MeasurementTimeType }
     *     
     */
    public MeasurementTimeType getMeasurementTime() {
        return measurementTime;
    }

    /**
     * Définit la valeur de la propriété measurementTime.
     * 
     * @param value
     *     allowed object is
     *     {@link MeasurementTimeType }
     *     
     */
    public void setMeasurementTime(MeasurementTimeType value) {
        this.measurementTime = value;
    }

    /**
     * Obtient la valeur de la propriété measurementLocation.
     * 
     * @return
     *     possible object is
     *     {@link LocationType }
     *     
     */
    public LocationType getMeasurementLocation() {
        return measurementLocation;
    }

    /**
     * Définit la valeur de la propriété measurementLocation.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationType }
     *     
     */
    public void setMeasurementLocation(LocationType value) {
        this.measurementLocation = value;
    }

}
