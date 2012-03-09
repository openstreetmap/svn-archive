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
 * 		An airport
 * 			
 * 
 * <p>Classe Java pour AirportStopPointType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="AirportStopPointType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}StopPointType">
 *       &lt;sequence>
 *         &lt;element name="terminalIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="gateIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AirportStopPointType", propOrder = {
    "terminalIdentifier",
    "gateIdentifier"
})
public class AirportStopPointType
    extends StopPointType
{

    protected String terminalIdentifier;
    protected String gateIdentifier;

    /**
     * Obtient la valeur de la propriété terminalIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTerminalIdentifier() {
        return terminalIdentifier;
    }

    /**
     * Définit la valeur de la propriété terminalIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerminalIdentifier(String value) {
        this.terminalIdentifier = value;
    }

    /**
     * Obtient la valeur de la propriété gateIdentifier.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGateIdentifier() {
        return gateIdentifier;
    }

    /**
     * Définit la valeur de la propriété gateIdentifier.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGateIdentifier(String value) {
        this.gateIdentifier = value;
    }

}
