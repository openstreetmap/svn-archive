//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 		General description of a logical location (point, area, link,network,line....)
 * 		This type is an abstract type
 * 			
 * 
 * <p>Classe Java pour LogicalLocationType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="LogicalLocationType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.trident.org/schema/trident}TridentObjectType">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LogicalLocationType")
@XmlSeeAlso({
    TransportNetworkType.class,
    LineType.class,
    LocationType.class
})
public abstract class LogicalLocationType
    extends TridentObjectType
{


}
