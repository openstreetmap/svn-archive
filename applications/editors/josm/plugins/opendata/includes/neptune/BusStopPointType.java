//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		An bus sopt point
 * 			
 * 
 * <p>Classe Java pour BusStopPointType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="BusStopPointType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}StopPointType">
 *       &lt;sequence>
 *         &lt;element name="ptDirection" type="{http://www.trident.org/schema/trident}PTDirectionType" minOccurs="0"/>
 *         &lt;element name="streetName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="streetNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="platformIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BusStopPointType", propOrder = {
    "ptDirection",
    "streetName",
    "streetNumber",
    "platformIdentifier"
})
public class BusStopPointType
    extends StopPointType
{

    protected PTDirectionType ptDirection;
    protected String streetName;
    protected String streetNumber;
    protected String platformIdentifier;

    /**
     * Obtient la valeur de la propriété ptDirection.
     * 
     * @return
     *     possible object is
     *     {@link PTDirectionType }
     *     
     */
    public PTDirectionType getPtDirection() {
        return ptDirection;
    }

    /**
     * Définit la valeur de la propriété ptDirection.
     * 
     * @param value
     *     allowed object is
     *     {@link PTDirectionType }
     *     
     */
    public void setPtDirection(PTDirectionType value) {
        this.ptDirection = value;
    }

    /**
     * Obtient la valeur de la propriété streetName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStreetName() {
        return streetName;
    }

    /**
     * Définit la valeur de la propriété streetName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStreetName(String value) {
        this.streetName = value;
    }

    /**
     * Obtient la valeur de la propriété streetNumber.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStreetNumber() {
        return streetNumber;
    }

    /**
     * Définit la valeur de la propriété streetNumber.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStreetNumber(String value) {
        this.streetNumber = value;
    }

    /**
     * Obtient la valeur de la propriété platformIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlatformIdentifier() {
        return platformIdentifier;
    }

    /**
     * Définit la valeur de la propriété platformIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlatformIdentifier(String value) {
        this.platformIdentifier = value;
    }

}
