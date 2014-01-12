//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * La strucutre d'échange d'une ligne de transport.
 * Cette strucuture contient la totalité des données qui décrivent la ligne.
 * 
 * <p>Classe Java pour ChouettePTNetworkType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ChouettePTNetworkType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PTNetwork" type="{http://www.trident.org/schema/trident}PTNetworkType"/>
 *         &lt;element name="GroupOfLine" type="{http://www.trident.org/schema/trident}GroupOfLineType" minOccurs="0"/>
 *         &lt;element name="Company" type="{http://www.trident.org/schema/trident}CompanyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ChouetteArea">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="StopArea" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{http://www.trident.org/schema/trident}StopAreaType">
 *                           &lt;sequence>
 *                             &lt;element name="StopAreaExtension" type="{http://www.trident.org/schema/trident}StopAreaExtension" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="AreaCentroid" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{http://www.trident.org/schema/trident}PlaceType">
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ConnectionLink" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.trident.org/schema/trident}ConnectionLinkType">
 *                 &lt;sequence>
 *                   &lt;element name="ConnectionLinkExtension" type="{http://www.trident.org/schema/trident}ConnectionLinkExtensionType" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Timetable" type="{http://www.trident.org/schema/trident}TimetableType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="TimeSlot" type="{http://www.trident.org/schema/trident}TimeSlotType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ChouetteLineDescription">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Line">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{http://www.trident.org/schema/trident}LineType">
 *                           &lt;sequence>
 *                             &lt;element name="LineExtension" type="{http://www.trident.org/schema/trident}LineExtensionType" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="ChouetteRoute" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{http://www.trident.org/schema/trident}RouteType">
 *                           &lt;sequence>
 *                             &lt;element name="RouteExtension" type="{http://www.trident.org/schema/trident}RouteExtension" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="StopPoint" maxOccurs="unbounded" minOccurs="2">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;extension base="{http://www.trident.org/schema/trident}StopPointType">
 *                         &lt;/extension>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                   &lt;element name="ITL" type="{http://www.trident.org/schema/trident}ITLType" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="PtLink" type="{http://www.trident.org/schema/trident}PTLinkType" maxOccurs="unbounded"/>
 *                   &lt;element name="JourneyPattern" type="{http://www.trident.org/schema/trident}JourneyPatternType" maxOccurs="unbounded" minOccurs="0"/>
 *                   &lt;element name="VehicleJourney" type="{http://www.trident.org/schema/trident}VehicleJourneyType" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Facility" type="{http://www.trident.org/schema/trident}ChouetteFacilityType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="AccessPoint" type="{http://www.trident.org/schema/trident}PTAccessPointType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="AccessLink" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://www.trident.org/schema/trident}PTAccessLinkType">
 *                 &lt;sequence>
 *                   &lt;element name="ConnectionLinkExtension" type="{http://www.trident.org/schema/trident}ConnectionLinkExtensionType" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChouettePTNetworkType", propOrder = {
    "ptNetwork",
    "groupOfLine",
    "company",
    "chouetteArea",
    "connectionLink",
    "timetable",
    "timeSlot",
    "chouetteLineDescription",
    "facility",
    "accessPoint",
    "accessLink"
})
public class ChouettePTNetworkType {

    @XmlElement(name = "PTNetwork", required = true)
    protected PTNetworkType ptNetwork;
    @XmlElement(name = "GroupOfLine")
    protected GroupOfLineType groupOfLine;
    @XmlElement(name = "Company")
    protected List<CompanyType> company;
    @XmlElement(name = "ChouetteArea", required = true)
    protected ChouettePTNetworkType.ChouetteArea chouetteArea;
    @XmlElement(name = "ConnectionLink")
    protected List<ChouettePTNetworkType.ConnectionLink> connectionLink;
    @XmlElement(name = "Timetable")
    protected List<TimetableType> timetable;
    @XmlElement(name = "TimeSlot")
    protected List<TimeSlotType> timeSlot;
    @XmlElement(name = "ChouetteLineDescription", required = true)
    protected ChouettePTNetworkType.ChouetteLineDescription chouetteLineDescription;
    @XmlElement(name = "Facility")
    protected List<ChouetteFacilityType> facility;
    @XmlElement(name = "AccessPoint")
    protected List<PTAccessPointType> accessPoint;
    @XmlElement(name = "AccessLink")
    protected List<ChouettePTNetworkType.AccessLink> accessLink;

    /**
     * Obtient la valeur de la propriété ptNetwork.
     * 
     * @return
     *     possible object is
     *     {@link PTNetworkType }
     *     
     */
    public PTNetworkType getPTNetwork() {
        return ptNetwork;
    }

    /**
     * Définit la valeur de la propriété ptNetwork.
     * 
     * @param value
     *     allowed object is
     *     {@link PTNetworkType }
     *     
     */
    public void setPTNetwork(PTNetworkType value) {
        this.ptNetwork = value;
    }

    /**
     * Obtient la valeur de la propriété groupOfLine.
     * 
     * @return
     *     possible object is
     *     {@link GroupOfLineType }
     *     
     */
    public GroupOfLineType getGroupOfLine() {
        return groupOfLine;
    }

    /**
     * Définit la valeur de la propriété groupOfLine.
     * 
     * @param value
     *     allowed object is
     *     {@link GroupOfLineType }
     *     
     */
    public void setGroupOfLine(GroupOfLineType value) {
        this.groupOfLine = value;
    }

    /**
     * Gets the value of the company property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the company property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCompany().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CompanyType }
     * 
     * 
     */
    public List<CompanyType> getCompany() {
        if (company == null) {
            company = new ArrayList<CompanyType>();
        }
        return this.company;
    }

    /**
     * Obtient la valeur de la propriété chouetteArea.
     * 
     * @return
     *     possible object is
     *     {@link ChouettePTNetworkType.ChouetteArea }
     *     
     */
    public ChouettePTNetworkType.ChouetteArea getChouetteArea() {
        return chouetteArea;
    }

    /**
     * Définit la valeur de la propriété chouetteArea.
     * 
     * @param value
     *     allowed object is
     *     {@link ChouettePTNetworkType.ChouetteArea }
     *     
     */
    public void setChouetteArea(ChouettePTNetworkType.ChouetteArea value) {
        this.chouetteArea = value;
    }

    /**
     * Gets the value of the connectionLink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connectionLink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConnectionLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ChouettePTNetworkType.ConnectionLink }
     * 
     * 
     */
    public List<ChouettePTNetworkType.ConnectionLink> getConnectionLink() {
        if (connectionLink == null) {
            connectionLink = new ArrayList<ChouettePTNetworkType.ConnectionLink>();
        }
        return this.connectionLink;
    }

    /**
     * Gets the value of the timetable property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the timetable property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTimetable().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TimetableType }
     * 
     * 
     */
    public List<TimetableType> getTimetable() {
        if (timetable == null) {
            timetable = new ArrayList<TimetableType>();
        }
        return this.timetable;
    }

    /**
     * Gets the value of the timeSlot property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the timeSlot property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTimeSlot().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TimeSlotType }
     * 
     * 
     */
    public List<TimeSlotType> getTimeSlot() {
        if (timeSlot == null) {
            timeSlot = new ArrayList<TimeSlotType>();
        }
        return this.timeSlot;
    }

    /**
     * Obtient la valeur de la propriété chouetteLineDescription.
     * 
     * @return
     *     possible object is
     *     {@link ChouettePTNetworkType.ChouetteLineDescription }
     *     
     */
    public ChouettePTNetworkType.ChouetteLineDescription getChouetteLineDescription() {
        return chouetteLineDescription;
    }

    /**
     * Définit la valeur de la propriété chouetteLineDescription.
     * 
     * @param value
     *     allowed object is
     *     {@link ChouettePTNetworkType.ChouetteLineDescription }
     *     
     */
    public void setChouetteLineDescription(ChouettePTNetworkType.ChouetteLineDescription value) {
        this.chouetteLineDescription = value;
    }

    /**
     * Gets the value of the facility property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the facility property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFacility().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ChouetteFacilityType }
     * 
     * 
     */
    public List<ChouetteFacilityType> getFacility() {
        if (facility == null) {
            facility = new ArrayList<ChouetteFacilityType>();
        }
        return this.facility;
    }

    /**
     * Gets the value of the accessPoint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accessPoint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccessPoint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PTAccessPointType }
     * 
     * 
     */
    public List<PTAccessPointType> getAccessPoint() {
        if (accessPoint == null) {
            accessPoint = new ArrayList<PTAccessPointType>();
        }
        return this.accessPoint;
    }

    /**
     * Gets the value of the accessLink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accessLink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccessLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ChouettePTNetworkType.AccessLink }
     * 
     * 
     */
    public List<ChouettePTNetworkType.AccessLink> getAccessLink() {
        if (accessLink == null) {
            accessLink = new ArrayList<ChouettePTNetworkType.AccessLink>();
        }
        return this.accessLink;
    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.trident.org/schema/trident}PTAccessLinkType">
     *       &lt;sequence>
     *         &lt;element name="ConnectionLinkExtension" type="{http://www.trident.org/schema/trident}ConnectionLinkExtensionType" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class AccessLink
        extends PTAccessLinkType
    {


    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="StopArea" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{http://www.trident.org/schema/trident}StopAreaType">
     *                 &lt;sequence>
     *                   &lt;element name="StopAreaExtension" type="{http://www.trident.org/schema/trident}StopAreaExtension" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/extension>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="AreaCentroid" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{http://www.trident.org/schema/trident}PlaceType">
     *               &lt;/extension>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "stopArea",
        "areaCentroid"
    })
    public static class ChouetteArea {

        @XmlElement(name = "StopArea")
        protected List<ChouettePTNetworkType.ChouetteArea.StopArea> stopArea;
        @XmlElement(name = "AreaCentroid")
        protected List<ChouettePTNetworkType.ChouetteArea.AreaCentroid> areaCentroid;

        /**
         * Gets the value of the stopArea property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the stopArea property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getStopArea().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ChouettePTNetworkType.ChouetteArea.StopArea }
         * 
         * 
         */
        public List<ChouettePTNetworkType.ChouetteArea.StopArea> getStopArea() {
            if (stopArea == null) {
                stopArea = new ArrayList<ChouettePTNetworkType.ChouetteArea.StopArea>();
            }
            return this.stopArea;
        }

        /**
         * Gets the value of the areaCentroid property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the areaCentroid property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getAreaCentroid().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ChouettePTNetworkType.ChouetteArea.AreaCentroid }
         * 
         * 
         */
        public List<ChouettePTNetworkType.ChouetteArea.AreaCentroid> getAreaCentroid() {
            if (areaCentroid == null) {
                areaCentroid = new ArrayList<ChouettePTNetworkType.ChouetteArea.AreaCentroid>();
            }
            return this.areaCentroid;
        }


        /**
         * <p>Classe Java pour anonymous complex type.
         * 
         * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;extension base="{http://www.trident.org/schema/trident}PlaceType">
         *     &lt;/extension>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class AreaCentroid
            extends PlaceType
        {


        }


        /**
         * <p>Classe Java pour anonymous complex type.
         * 
         * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;extension base="{http://www.trident.org/schema/trident}StopAreaType">
         *       &lt;sequence>
         *         &lt;element name="StopAreaExtension" type="{http://www.trident.org/schema/trident}StopAreaExtension" minOccurs="0"/>
         *       &lt;/sequence>
         *     &lt;/extension>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "stopAreaExtension"
        })
        public static class StopArea
            extends StopAreaType
        {

            @XmlElement(name = "StopAreaExtension")
            protected StopAreaExtension stopAreaExtension;

            /**
             * Obtient la valeur de la propriété stopAreaExtension.
             * 
             * @return
             *     possible object is
             *     {@link StopAreaExtension }
             *     
             */
            public StopAreaExtension getStopAreaExtension() {
                return stopAreaExtension;
            }

            /**
             * Définit la valeur de la propriété stopAreaExtension.
             * 
             * @param value
             *     allowed object is
             *     {@link StopAreaExtension }
             *     
             */
            public void setStopAreaExtension(StopAreaExtension value) {
                this.stopAreaExtension = value;
            }

        }

    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="Line">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{http://www.trident.org/schema/trident}LineType">
     *                 &lt;sequence>
     *                   &lt;element name="LineExtension" type="{http://www.trident.org/schema/trident}LineExtensionType" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/extension>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="ChouetteRoute" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{http://www.trident.org/schema/trident}RouteType">
     *                 &lt;sequence>
     *                   &lt;element name="RouteExtension" type="{http://www.trident.org/schema/trident}RouteExtension" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/extension>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="StopPoint" maxOccurs="unbounded" minOccurs="2">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;extension base="{http://www.trident.org/schema/trident}StopPointType">
     *               &lt;/extension>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *         &lt;element name="ITL" type="{http://www.trident.org/schema/trident}ITLType" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="PtLink" type="{http://www.trident.org/schema/trident}PTLinkType" maxOccurs="unbounded"/>
     *         &lt;element name="JourneyPattern" type="{http://www.trident.org/schema/trident}JourneyPatternType" maxOccurs="unbounded" minOccurs="0"/>
     *         &lt;element name="VehicleJourney" type="{http://www.trident.org/schema/trident}VehicleJourneyType" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "line",
        "chouetteRoute",
        "stopPoint",
        "itl",
        "ptLink",
        "journeyPattern",
        "vehicleJourney"
    })
    public static class ChouetteLineDescription {

        @XmlElement(name = "Line", required = true)
        protected ChouettePTNetworkType.ChouetteLineDescription.Line line;
        @XmlElement(name = "ChouetteRoute", required = true)
        protected List<ChouettePTNetworkType.ChouetteLineDescription.ChouetteRoute> chouetteRoute;
        @XmlElement(name = "StopPoint", required = true)
        protected List<ChouettePTNetworkType.ChouetteLineDescription.StopPoint> stopPoint;
        @XmlElement(name = "ITL")
        protected List<ITLType> itl;
        @XmlElement(name = "PtLink", required = true)
        protected List<PTLinkType> ptLink;
        @XmlElement(name = "JourneyPattern")
        protected List<JourneyPatternType> journeyPattern;
        @XmlElement(name = "VehicleJourney")
        protected List<VehicleJourneyType> vehicleJourney;

        /**
         * Obtient la valeur de la propriété line.
         * 
         * @return
         *     possible object is
         *     {@link ChouettePTNetworkType.ChouetteLineDescription.Line }
         *     
         */
        public ChouettePTNetworkType.ChouetteLineDescription.Line getLine() {
            return line;
        }

        /**
         * Définit la valeur de la propriété line.
         * 
         * @param value
         *     allowed object is
         *     {@link ChouettePTNetworkType.ChouetteLineDescription.Line }
         *     
         */
        public void setLine(ChouettePTNetworkType.ChouetteLineDescription.Line value) {
            this.line = value;
        }

        /**
         * Gets the value of the chouetteRoute property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the chouetteRoute property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getChouetteRoute().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ChouettePTNetworkType.ChouetteLineDescription.ChouetteRoute }
         * 
         * 
         */
        public List<ChouettePTNetworkType.ChouetteLineDescription.ChouetteRoute> getChouetteRoute() {
            if (chouetteRoute == null) {
                chouetteRoute = new ArrayList<ChouettePTNetworkType.ChouetteLineDescription.ChouetteRoute>();
            }
            return this.chouetteRoute;
        }

        /**
         * Gets the value of the stopPoint property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the stopPoint property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getStopPoint().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ChouettePTNetworkType.ChouetteLineDescription.StopPoint }
         * 
         * 
         */
        public List<ChouettePTNetworkType.ChouetteLineDescription.StopPoint> getStopPoint() {
            if (stopPoint == null) {
                stopPoint = new ArrayList<ChouettePTNetworkType.ChouetteLineDescription.StopPoint>();
            }
            return this.stopPoint;
        }

        /**
         * Gets the value of the itl property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the itl property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getITL().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ITLType }
         * 
         * 
         */
        public List<ITLType> getITL() {
            if (itl == null) {
                itl = new ArrayList<ITLType>();
            }
            return this.itl;
        }

        /**
         * Gets the value of the ptLink property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the ptLink property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getPtLink().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link PTLinkType }
         * 
         * 
         */
        public List<PTLinkType> getPtLink() {
            if (ptLink == null) {
                ptLink = new ArrayList<PTLinkType>();
            }
            return this.ptLink;
        }

        /**
         * Gets the value of the journeyPattern property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the journeyPattern property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getJourneyPattern().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link JourneyPatternType }
         * 
         * 
         */
        public List<JourneyPatternType> getJourneyPattern() {
            if (journeyPattern == null) {
                journeyPattern = new ArrayList<JourneyPatternType>();
            }
            return this.journeyPattern;
        }

        /**
         * Gets the value of the vehicleJourney property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the vehicleJourney property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getVehicleJourney().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link VehicleJourneyType }
         * 
         * 
         */
        public List<VehicleJourneyType> getVehicleJourney() {
            if (vehicleJourney == null) {
                vehicleJourney = new ArrayList<VehicleJourneyType>();
            }
            return this.vehicleJourney;
        }


        /**
         * <p>Classe Java pour anonymous complex type.
         * 
         * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;extension base="{http://www.trident.org/schema/trident}RouteType">
         *       &lt;sequence>
         *         &lt;element name="RouteExtension" type="{http://www.trident.org/schema/trident}RouteExtension" minOccurs="0"/>
         *       &lt;/sequence>
         *     &lt;/extension>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "routeExtension"
        })
        public static class ChouetteRoute
            extends RouteType
        {

            @XmlElement(name = "RouteExtension")
            protected RouteExtension routeExtension;

            /**
             * Obtient la valeur de la propriété routeExtension.
             * 
             * @return
             *     possible object is
             *     {@link RouteExtension }
             *     
             */
            public RouteExtension getRouteExtension() {
                return routeExtension;
            }

            /**
             * Définit la valeur de la propriété routeExtension.
             * 
             * @param value
             *     allowed object is
             *     {@link RouteExtension }
             *     
             */
            public void setRouteExtension(RouteExtension value) {
                this.routeExtension = value;
            }

        }


        /**
         * <p>Classe Java pour anonymous complex type.
         * 
         * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;extension base="{http://www.trident.org/schema/trident}LineType">
         *       &lt;sequence>
         *         &lt;element name="LineExtension" type="{http://www.trident.org/schema/trident}LineExtensionType" minOccurs="0"/>
         *       &lt;/sequence>
         *     &lt;/extension>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "lineExtension"
        })
        public static class Line
            extends LineType
        {

            @XmlElement(name = "LineExtension")
            protected LineExtensionType lineExtension;

            /**
             * Obtient la valeur de la propriété lineExtension.
             * 
             * @return
             *     possible object is
             *     {@link LineExtensionType }
             *     
             */
            public LineExtensionType getLineExtension() {
                return lineExtension;
            }

            /**
             * Définit la valeur de la propriété lineExtension.
             * 
             * @param value
             *     allowed object is
             *     {@link LineExtensionType }
             *     
             */
            public void setLineExtension(LineExtensionType value) {
                this.lineExtension = value;
            }

        }


        /**
         * <p>Classe Java pour anonymous complex type.
         * 
         * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;extension base="{http://www.trident.org/schema/trident}StopPointType">
         *     &lt;/extension>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class StopPoint
            extends StopPointType
        {


        }

    }


    /**
     * <p>Classe Java pour anonymous complex type.
     * 
     * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://www.trident.org/schema/trident}ConnectionLinkType">
     *       &lt;sequence>
     *         &lt;element name="ConnectionLinkExtension" type="{http://www.trident.org/schema/trident}ConnectionLinkExtensionType" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "connectionLinkExtension"
    })
    public static class ConnectionLink
        extends ConnectionLinkType
    {

        @XmlElement(name = "ConnectionLinkExtension")
        protected ConnectionLinkExtensionType connectionLinkExtension;

        /**
         * Obtient la valeur de la propriété connectionLinkExtension.
         * 
         * @return
         *     possible object is
         *     {@link ConnectionLinkExtensionType }
         *     
         */
        public ConnectionLinkExtensionType getConnectionLinkExtension() {
            return connectionLinkExtension;
        }

        /**
         * Définit la valeur de la propriété connectionLinkExtension.
         * 
         * @param value
         *     allowed object is
         *     {@link ConnectionLinkExtensionType }
         *     
         */
        public void setConnectionLinkExtension(ConnectionLinkExtensionType value) {
            this.connectionLinkExtension = value;
        }

    }

}
