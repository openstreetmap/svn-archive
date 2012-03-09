//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour POITypeType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="POITypeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AccommodationEatingAndDrinking"/>
 *     &lt;enumeration value="CommercialServices"/>
 *     &lt;enumeration value="Attraction"/>
 *     &lt;enumeration value="SportAndEntertainment"/>
 *     &lt;enumeration value="EducationAndHealth"/>
 *     &lt;enumeration value="PublicInfrastructure"/>
 *     &lt;enumeration value="ManufacturingAndProduction"/>
 *     &lt;enumeration value="Wholesale"/>
 *     &lt;enumeration value="Retail"/>
 *     &lt;enumeration value="Transport"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "POITypeType")
@XmlEnum
public enum POITypeType {

    @XmlEnumValue("AccommodationEatingAndDrinking")
    ACCOMMODATION_EATING_AND_DRINKING("AccommodationEatingAndDrinking"),
    @XmlEnumValue("CommercialServices")
    COMMERCIAL_SERVICES("CommercialServices"),
    @XmlEnumValue("Attraction")
    ATTRACTION("Attraction"),
    @XmlEnumValue("SportAndEntertainment")
    SPORT_AND_ENTERTAINMENT("SportAndEntertainment"),
    @XmlEnumValue("EducationAndHealth")
    EDUCATION_AND_HEALTH("EducationAndHealth"),
    @XmlEnumValue("PublicInfrastructure")
    PUBLIC_INFRASTRUCTURE("PublicInfrastructure"),
    @XmlEnumValue("ManufacturingAndProduction")
    MANUFACTURING_AND_PRODUCTION("ManufacturingAndProduction"),
    @XmlEnumValue("Wholesale")
    WHOLESALE("Wholesale"),
    @XmlEnumValue("Retail")
    RETAIL("Retail"),
    @XmlEnumValue("Transport")
    TRANSPORT("Transport");
    private final String value;

    POITypeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static POITypeType fromValue(String v) {
        for (POITypeType c: POITypeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
