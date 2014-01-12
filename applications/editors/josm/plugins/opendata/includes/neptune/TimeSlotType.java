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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Type pour les fréquences horaire
 * 
 * <p>Classe Java pour TimeSlotType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="TimeSlotType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}TridentObjectType">
 *       &lt;sequence>
 *         &lt;element name="beginningSlotTime" type="{http://www.w3.org/2001/XMLSchema}time"/>
 *         &lt;element name="endSlotTime" type="{http://www.w3.org/2001/XMLSchema}time"/>
 *         &lt;element name="firstDepartureTimeInSlot" type="{http://www.w3.org/2001/XMLSchema}time"/>
 *         &lt;element name="lastDepartureTimeInSlot" type="{http://www.w3.org/2001/XMLSchema}time"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TimeSlotType", propOrder = {
    "beginningSlotTime",
    "endSlotTime",
    "firstDepartureTimeInSlot",
    "lastDepartureTimeInSlot"
})
public class TimeSlotType
    extends TridentObjectType
{

    @XmlElement(required = true)
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar beginningSlotTime;
    @XmlElement(required = true)
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar endSlotTime;
    @XmlElement(required = true)
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar firstDepartureTimeInSlot;
    @XmlElement(required = true)
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar lastDepartureTimeInSlot;

    /**
     * Obtient la valeur de la propriété beginningSlotTime.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBeginningSlotTime() {
        return beginningSlotTime;
    }

    /**
     * Définit la valeur de la propriété beginningSlotTime.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBeginningSlotTime(XMLGregorianCalendar value) {
        this.beginningSlotTime = value;
    }

    /**
     * Obtient la valeur de la propriété endSlotTime.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEndSlotTime() {
        return endSlotTime;
    }

    /**
     * Définit la valeur de la propriété endSlotTime.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEndSlotTime(XMLGregorianCalendar value) {
        this.endSlotTime = value;
    }

    /**
     * Obtient la valeur de la propriété firstDepartureTimeInSlot.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFirstDepartureTimeInSlot() {
        return firstDepartureTimeInSlot;
    }

    /**
     * Définit la valeur de la propriété firstDepartureTimeInSlot.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFirstDepartureTimeInSlot(XMLGregorianCalendar value) {
        this.firstDepartureTimeInSlot = value;
    }

    /**
     * Obtient la valeur de la propriété lastDepartureTimeInSlot.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastDepartureTimeInSlot() {
        return lastDepartureTimeInSlot;
    }

    /**
     * Définit la valeur de la propriété lastDepartureTimeInSlot.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastDepartureTimeInSlot(XMLGregorianCalendar value) {
        this.lastDepartureTimeInSlot = value;
    }

}
