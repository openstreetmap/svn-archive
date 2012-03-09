//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * 
 * 				Informations on the time of a measurement
 * 			
 * 
 * <p>Classe Java pour MeasurementTimeType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="MeasurementTimeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="measurementTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="measurementPeriod" type="{http://www.trident.org/schema/trident}TimePeriodType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeasurementTimeType", propOrder = {
    "measurementTime",
    "measurementPeriod"
})
public class MeasurementTimeType {

    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar measurementTime;
    protected TimePeriodType measurementPeriod;

    /**
     * Obtient la valeur de la propriété measurementTime.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMeasurementTime() {
        return measurementTime;
    }

    /**
     * Définit la valeur de la propriété measurementTime.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMeasurementTime(XMLGregorianCalendar value) {
        this.measurementTime = value;
    }

    /**
     * Obtient la valeur de la propriété measurementPeriod.
     * 
     * @return
     *     possible object is
     *     {@link TimePeriodType }
     *     
     */
    public TimePeriodType getMeasurementPeriod() {
        return measurementPeriod;
    }

    /**
     * Définit la valeur de la propriété measurementPeriod.
     * 
     * @param value
     *     allowed object is
     *     {@link TimePeriodType }
     *     
     */
    public void setMeasurementPeriod(TimePeriodType value) {
        this.measurementPeriod = value;
    }

}
