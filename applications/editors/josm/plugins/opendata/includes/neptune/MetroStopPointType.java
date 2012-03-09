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
 * 		An metro stop point
 * 			
 * 
 * <p>Classe Java pour MetroStopPointType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="MetroStopPointType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}StopPointType">
 *       &lt;sequence>
 *         &lt;element name="lineName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lineNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="platformIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ptDirection" type="{http://www.trident.org/schema/trident}PTDirectionType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MetroStopPointType", propOrder = {
    "lineName",
    "lineNumber",
    "platformIdentifier",
    "ptDirection"
})
public class MetroStopPointType
    extends StopPointType
{

    protected String lineName;
    protected String lineNumber;
    protected String platformIdentifier;
    protected PTDirectionType ptDirection;

    /**
     * Obtient la valeur de la propriété lineName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineName() {
        return lineName;
    }

    /**
     * Définit la valeur de la propriété lineName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineName(String value) {
        this.lineName = value;
    }

    /**
     * Obtient la valeur de la propriété lineNumber.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineNumber() {
        return lineNumber;
    }

    /**
     * Définit la valeur de la propriété lineNumber.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineNumber(String value) {
        this.lineNumber = value;
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

}
