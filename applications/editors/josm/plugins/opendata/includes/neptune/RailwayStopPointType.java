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
 * 		An Railwaystop point
 * 			
 * 
 * <p>Classe Java pour RailwayStopPointType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="RailwayStopPointType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}StopPointType">
 *       &lt;sequence>
 *         &lt;element name="stationInternalDivision" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "RailwayStopPointType", propOrder = {
    "stationInternalDivision",
    "platformIdentifier"
})
public class RailwayStopPointType
    extends StopPointType
{

    protected String stationInternalDivision;
    protected String platformIdentifier;

    /**
     * Obtient la valeur de la propriété stationInternalDivision.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStationInternalDivision() {
        return stationInternalDivision;
    }

    /**
     * Définit la valeur de la propriété stationInternalDivision.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStationInternalDivision(String value) {
        this.stationInternalDivision = value;
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
