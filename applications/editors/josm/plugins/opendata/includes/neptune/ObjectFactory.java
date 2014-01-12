//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.5 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2012.03.08 à 06:24:59 PM CET 
//


package neptune;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the neptune package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _AccommodationFacility_QNAME = new QName("http://www.siri.org.uk/siri", "AccommodationFacility");
    private final static QName _ChouetteRemoveLine_QNAME = new QName("http://www.trident.org/schema/trident", "ChouetteRemoveLine");
    private final static QName _RetailFacility_QNAME = new QName("http://www.siri.org.uk/siri", "RetailFacility");
    private final static QName _NuisanceFacility_QNAME = new QName("http://www.siri.org.uk/siri", "NuisanceFacility");
    private final static QName _HireFacility_QNAME = new QName("http://www.siri.org.uk/siri", "HireFacility");
    private final static QName _FareClassFacility_QNAME = new QName("http://www.siri.org.uk/siri", "FareClassFacility");
    private final static QName _ReservedSpaceFacility_QNAME = new QName("http://www.siri.org.uk/siri", "ReservedSpaceFacility");
    private final static QName _PassengerCommsFacility_QNAME = new QName("http://www.siri.org.uk/siri", "PassengerCommsFacility");
    private final static QName _MobilityFacility_QNAME = new QName("http://www.siri.org.uk/siri", "MobilityFacility");
    private final static QName _PassengerInformationFacility_QNAME = new QName("http://www.siri.org.uk/siri", "PassengerInformationFacility");
    private final static QName _TicketingFacility_QNAME = new QName("http://www.siri.org.uk/siri", "TicketingFacility");
    private final static QName _AssistanceFacility_QNAME = new QName("http://www.siri.org.uk/siri", "AssistanceFacility");
    private final static QName _LuggageFacility_QNAME = new QName("http://www.siri.org.uk/siri", "LuggageFacility");
    private final static QName _ParkingFacility_QNAME = new QName("http://www.siri.org.uk/siri", "ParkingFacility");
    private final static QName _AccessFacility_QNAME = new QName("http://www.siri.org.uk/siri", "AccessFacility");
    private final static QName _RefreshmentFacility_QNAME = new QName("http://www.siri.org.uk/siri", "RefreshmentFacility");
    private final static QName _ChouettePTNetwork_QNAME = new QName("http://www.trident.org/schema/trident", "ChouettePTNetwork");
    private final static QName _SanitaryFacility_QNAME = new QName("http://www.siri.org.uk/siri", "SanitaryFacility");
    private final static QName _PTAccessLinkTypeComment_QNAME = new QName("http://www.trident.org/schema/trident", "Comment");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: neptune
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LineExtensionType }
     * 
     */
    public LineExtensionType createLineExtensionType() {
        return new LineExtensionType();
    }

    /**
     * Create an instance of {@link ConnectionLinkExtensionType }
     * 
     */
    public ConnectionLinkExtensionType createConnectionLinkExtensionType() {
        return new ConnectionLinkExtensionType();
    }

    /**
     * Create an instance of {@link StopAreaExtension }
     * 
     */
    public StopAreaExtension createStopAreaExtension() {
        return new StopAreaExtension();
    }

    /**
     * Create an instance of {@link ChouetteFacilityType }
     * 
     */
    public ChouetteFacilityType createChouetteFacilityType() {
        return new ChouetteFacilityType();
    }

    /**
     * Create an instance of {@link ChouettePTNetworkType }
     * 
     */
    public ChouettePTNetworkType createChouettePTNetworkType() {
        return new ChouettePTNetworkType();
    }

    /**
     * Create an instance of {@link ChouettePTNetworkType.ChouetteLineDescription }
     * 
     */
    public ChouettePTNetworkType.ChouetteLineDescription createChouettePTNetworkTypeChouetteLineDescription() {
        return new ChouettePTNetworkType.ChouetteLineDescription();
    }

    /**
     * Create an instance of {@link ChouettePTNetworkType.ChouetteArea }
     * 
     */
    public ChouettePTNetworkType.ChouetteArea createChouettePTNetworkTypeChouetteArea() {
        return new ChouettePTNetworkType.ChouetteArea();
    }

    /**
     * Create an instance of {@link ChouetteRemoveLineType }
     * 
     */
    public ChouetteRemoveLineType createChouetteRemoveLineType() {
        return new ChouetteRemoveLineType();
    }

    /**
     * Create an instance of {@link BusStopPointType }
     * 
     */
    public BusStopPointType createBusStopPointType() {
        return new BusStopPointType();
    }

    /**
     * Create an instance of {@link CompanyType }
     * 
     */
    public CompanyType createCompanyType() {
        return new CompanyType();
    }

    /**
     * Create an instance of {@link GeneralLinkType }
     * 
     */
    public GeneralLinkType createGeneralLinkType() {
        return new GeneralLinkType();
    }

    /**
     * Create an instance of {@link MetroStopPointType }
     * 
     */
    public MetroStopPointType createMetroStopPointType() {
        return new MetroStopPointType();
    }

    /**
     * Create an instance of {@link PostalAddressType }
     * 
     */
    public PostalAddressType createPostalAddressType() {
        return new PostalAddressType();
    }

    /**
     * Create an instance of {@link RoadNetworkType }
     * 
     */
    public RoadNetworkType createRoadNetworkType() {
        return new RoadNetworkType();
    }

    /**
     * Create an instance of {@link PointOfInterestType }
     * 
     */
    public PointOfInterestType createPointOfInterestType() {
        return new PointOfInterestType();
    }

    /**
     * Create an instance of {@link PlaceType }
     * 
     */
    public PlaceType createPlaceType() {
        return new PlaceType();
    }

    /**
     * Create an instance of {@link PeriodType }
     * 
     */
    public PeriodType createPeriodType() {
        return new PeriodType();
    }

    /**
     * Create an instance of {@link TimeSlotType }
     * 
     */
    public TimeSlotType createTimeSlotType() {
        return new TimeSlotType();
    }

    /**
     * Create an instance of {@link GroupOfLineType }
     * 
     */
    public GroupOfLineType createGroupOfLineType() {
        return new GroupOfLineType();
    }

    /**
     * Create an instance of {@link StopPointType }
     * 
     */
    public StopPointType createStopPointType() {
        return new StopPointType();
    }

    /**
     * Create an instance of {@link UnitisedQuantityType }
     * 
     */
    public UnitisedQuantityType createUnitisedQuantityType() {
        return new UnitisedQuantityType();
    }

    /**
     * Create an instance of {@link ObjectReferenceType }
     * 
     */
    public ObjectReferenceType createObjectReferenceType() {
        return new ObjectReferenceType();
    }

    /**
     * Create an instance of {@link RegistrationType }
     * 
     */
    public RegistrationType createRegistrationType() {
        return new RegistrationType();
    }

    /**
     * Create an instance of {@link ConnectingServiceType }
     * 
     */
    public ConnectingServiceType createConnectingServiceType() {
        return new ConnectingServiceType();
    }

    /**
     * Create an instance of {@link TimePeriodType }
     * 
     */
    public TimePeriodType createTimePeriodType() {
        return new TimePeriodType();
    }

    /**
     * Create an instance of {@link AirportStopPointType }
     * 
     */
    public AirportStopPointType createAirportStopPointType() {
        return new AirportStopPointType();
    }

    /**
     * Create an instance of {@link PTAccessLinkType }
     * 
     */
    public PTAccessLinkType createPTAccessLinkType() {
        return new PTAccessLinkType();
    }

    /**
     * Create an instance of {@link PTAccessPointType }
     * 
     */
    public PTAccessPointType createPTAccessPointType() {
        return new PTAccessPointType();
    }

    /**
     * Create an instance of {@link VehicleJourneyAtStopType }
     * 
     */
    public VehicleJourneyAtStopType createVehicleJourneyAtStopType() {
        return new VehicleJourneyAtStopType();
    }

    /**
     * Create an instance of {@link TramStopPointType }
     * 
     */
    public TramStopPointType createTramStopPointType() {
        return new TramStopPointType();
    }

    /**
     * Create an instance of {@link JourneyPatternType }
     * 
     */
    public JourneyPatternType createJourneyPatternType() {
        return new JourneyPatternType();
    }

    /**
     * Create an instance of {@link CarType }
     * 
     */
    public CarType createCarType() {
        return new CarType();
    }

    /**
     * Create an instance of {@link NonPTAccessLinkType }
     * 
     */
    public NonPTAccessLinkType createNonPTAccessLinkType() {
        return new NonPTAccessLinkType();
    }

    /**
     * Create an instance of {@link RoadAddressType }
     * 
     */
    public RoadAddressType createRoadAddressType() {
        return new RoadAddressType();
    }

    /**
     * Create an instance of {@link RailwayStopPointType }
     * 
     */
    public RailwayStopPointType createRailwayStopPointType() {
        return new RailwayStopPointType();
    }

    /**
     * Create an instance of {@link RouteExtension }
     * 
     */
    public RouteExtension createRouteExtension() {
        return new RouteExtension();
    }

    /**
     * Create an instance of {@link TimetableType }
     * 
     */
    public TimetableType createTimetableType() {
        return new TimetableType();
    }

    /**
     * Create an instance of {@link AccuracyType }
     * 
     */
    public AccuracyType createAccuracyType() {
        return new AccuracyType();
    }

    /**
     * Create an instance of {@link ObjectValidityType }
     * 
     */
    public ObjectValidityType createObjectValidityType() {
        return new ObjectValidityType();
    }

    /**
     * Create an instance of {@link ProprietaryIdentifierType }
     * 
     */
    public ProprietaryIdentifierType createProprietaryIdentifierType() {
        return new ProprietaryIdentifierType();
    }

    /**
     * Create an instance of {@link RouteType }
     * 
     */
    public RouteType createRouteType() {
        return new RouteType();
    }

    /**
     * Create an instance of {@link WGS84PositionType }
     * 
     */
    public WGS84PositionType createWGS84PositionType() {
        return new WGS84PositionType();
    }

    /**
     * Create an instance of {@link ITLType }
     * 
     */
    public ITLType createITLType() {
        return new ITLType();
    }

    /**
     * Create an instance of {@link AreaType }
     * 
     */
    public AreaType createAreaType() {
        return new AreaType();
    }

    /**
     * Create an instance of {@link PTLinkType }
     * 
     */
    public PTLinkType createPTLinkType() {
        return new PTLinkType();
    }

    /**
     * Create an instance of {@link PTNetworkType }
     * 
     */
    public PTNetworkType createPTNetworkType() {
        return new PTNetworkType();
    }

    /**
     * Create an instance of {@link RoadPointType }
     * 
     */
    public RoadPointType createRoadPointType() {
        return new RoadPointType();
    }

    /**
     * Create an instance of {@link LineType }
     * 
     */
    public LineType createLineType() {
        return new LineType();
    }

    /**
     * Create an instance of {@link ProjectedPointType }
     * 
     */
    public ProjectedPointType createProjectedPointType() {
        return new ProjectedPointType();
    }

    /**
     * Create an instance of {@link MeasurementTimeType }
     * 
     */
    public MeasurementTimeType createMeasurementTimeType() {
        return new MeasurementTimeType();
    }

    /**
     * Create an instance of {@link VehicleJourneyType }
     * 
     */
    public VehicleJourneyType createVehicleJourneyType() {
        return new VehicleJourneyType();
    }

    /**
     * Create an instance of {@link ConnectionLinkType }
     * 
     */
    public ConnectionLinkType createConnectionLinkType() {
        return new ConnectionLinkType();
    }

    /**
     * Create an instance of {@link NonPTAccessLinkendType }
     * 
     */
    public NonPTAccessLinkendType createNonPTAccessLinkendType() {
        return new NonPTAccessLinkendType();
    }

    /**
     * Create an instance of {@link StopAreaType }
     * 
     */
    public StopAreaType createStopAreaType() {
        return new StopAreaType();
    }

    /**
     * Create an instance of {@link AddressType }
     * 
     */
    public AddressType createAddressType() {
        return new AddressType();
    }

    /**
     * Create an instance of {@link SuitabilityStructure }
     * 
     */
    public SuitabilityStructure createSuitabilityStructure() {
        return new SuitabilityStructure();
    }

    /**
     * Create an instance of {@link PassengerAccessibilityNeedsStructure }
     * 
     */
    public PassengerAccessibilityNeedsStructure createPassengerAccessibilityNeedsStructure() {
        return new PassengerAccessibilityNeedsStructure();
    }

    /**
     * Create an instance of {@link UserNeedStructure }
     * 
     */
    public UserNeedStructure createUserNeedStructure() {
        return new UserNeedStructure();
    }

    /**
     * Create an instance of {@link AllFacilitiesFeatureStructure }
     * 
     */
    public AllFacilitiesFeatureStructure createAllFacilitiesFeatureStructure() {
        return new AllFacilitiesFeatureStructure();
    }

    /**
     * Create an instance of {@link LineExtensionType.AccessibilitySuitabilityDetails }
     * 
     */
    public LineExtensionType.AccessibilitySuitabilityDetails createLineExtensionTypeAccessibilitySuitabilityDetails() {
        return new LineExtensionType.AccessibilitySuitabilityDetails();
    }

    /**
     * Create an instance of {@link ConnectionLinkExtensionType.AccessibilitySuitabilityDetails }
     * 
     */
    public ConnectionLinkExtensionType.AccessibilitySuitabilityDetails createConnectionLinkExtensionTypeAccessibilitySuitabilityDetails() {
        return new ConnectionLinkExtensionType.AccessibilitySuitabilityDetails();
    }

    /**
     * Create an instance of {@link StopAreaExtension.AccessibilitySuitabilityDetails }
     * 
     */
    public StopAreaExtension.AccessibilitySuitabilityDetails createStopAreaExtensionAccessibilitySuitabilityDetails() {
        return new StopAreaExtension.AccessibilitySuitabilityDetails();
    }

    /**
     * Create an instance of {@link ChouetteFacilityType.FacilityLocation }
     * 
     */
    public ChouetteFacilityType.FacilityLocation createChouetteFacilityTypeFacilityLocation() {
        return new ChouetteFacilityType.FacilityLocation();
    }

    /**
     * Create an instance of {@link ChouettePTNetworkType.ConnectionLink }
     * 
     */
    public ChouettePTNetworkType.ConnectionLink createChouettePTNetworkTypeConnectionLink() {
        return new ChouettePTNetworkType.ConnectionLink();
    }

    /**
     * Create an instance of {@link ChouettePTNetworkType.AccessLink }
     * 
     */
    public ChouettePTNetworkType.AccessLink createChouettePTNetworkTypeAccessLink() {
        return new ChouettePTNetworkType.AccessLink();
    }

    /**
     * Create an instance of {@link ChouettePTNetworkType.ChouetteLineDescription.Line }
     * 
     */
    public ChouettePTNetworkType.ChouetteLineDescription.Line createChouettePTNetworkTypeChouetteLineDescriptionLine() {
        return new ChouettePTNetworkType.ChouetteLineDescription.Line();
    }

    /**
     * Create an instance of {@link ChouettePTNetworkType.ChouetteLineDescription.ChouetteRoute }
     * 
     */
    public ChouettePTNetworkType.ChouetteLineDescription.ChouetteRoute createChouettePTNetworkTypeChouetteLineDescriptionChouetteRoute() {
        return new ChouettePTNetworkType.ChouetteLineDescription.ChouetteRoute();
    }

    /**
     * Create an instance of {@link ChouettePTNetworkType.ChouetteLineDescription.StopPoint }
     * 
     */
    public ChouettePTNetworkType.ChouetteLineDescription.StopPoint createChouettePTNetworkTypeChouetteLineDescriptionStopPoint() {
        return new ChouettePTNetworkType.ChouetteLineDescription.StopPoint();
    }

    /**
     * Create an instance of {@link ChouettePTNetworkType.ChouetteArea.StopArea }
     * 
     */
    public ChouettePTNetworkType.ChouetteArea.StopArea createChouettePTNetworkTypeChouetteAreaStopArea() {
        return new ChouettePTNetworkType.ChouetteArea.StopArea();
    }

    /**
     * Create an instance of {@link ChouettePTNetworkType.ChouetteArea.AreaCentroid }
     * 
     */
    public ChouettePTNetworkType.ChouetteArea.AreaCentroid createChouettePTNetworkTypeChouetteAreaAreaCentroid() {
        return new ChouettePTNetworkType.ChouetteArea.AreaCentroid();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AccommodationFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "AccommodationFacility")
    public JAXBElement<AccommodationFacilityEnumeration> createAccommodationFacility(AccommodationFacilityEnumeration value) {
        return new JAXBElement<AccommodationFacilityEnumeration>(_AccommodationFacility_QNAME, AccommodationFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChouetteRemoveLineType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.trident.org/schema/trident", name = "ChouetteRemoveLine")
    public JAXBElement<ChouetteRemoveLineType> createChouetteRemoveLine(ChouetteRemoveLineType value) {
        return new JAXBElement<ChouetteRemoveLineType>(_ChouetteRemoveLine_QNAME, ChouetteRemoveLineType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RetailFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "RetailFacility", defaultValue = "unknown")
    public JAXBElement<RetailFacilityEnumeration> createRetailFacility(RetailFacilityEnumeration value) {
        return new JAXBElement<RetailFacilityEnumeration>(_RetailFacility_QNAME, RetailFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NuisanceFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "NuisanceFacility")
    public JAXBElement<NuisanceFacilityEnumeration> createNuisanceFacility(NuisanceFacilityEnumeration value) {
        return new JAXBElement<NuisanceFacilityEnumeration>(_NuisanceFacility_QNAME, NuisanceFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HireFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "HireFacility")
    public JAXBElement<HireFacilityEnumeration> createHireFacility(HireFacilityEnumeration value) {
        return new JAXBElement<HireFacilityEnumeration>(_HireFacility_QNAME, HireFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FareClassFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "FareClassFacility")
    public JAXBElement<FareClassFacilityEnumeration> createFareClassFacility(FareClassFacilityEnumeration value) {
        return new JAXBElement<FareClassFacilityEnumeration>(_FareClassFacility_QNAME, FareClassFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReservedSpaceFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "ReservedSpaceFacility", defaultValue = "unknown")
    public JAXBElement<ReservedSpaceFacilityEnumeration> createReservedSpaceFacility(ReservedSpaceFacilityEnumeration value) {
        return new JAXBElement<ReservedSpaceFacilityEnumeration>(_ReservedSpaceFacility_QNAME, ReservedSpaceFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PassengerCommsFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "PassengerCommsFacility")
    public JAXBElement<PassengerCommsFacilityEnumeration> createPassengerCommsFacility(PassengerCommsFacilityEnumeration value) {
        return new JAXBElement<PassengerCommsFacilityEnumeration>(_PassengerCommsFacility_QNAME, PassengerCommsFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MobilityFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "MobilityFacility")
    public JAXBElement<MobilityFacilityEnumeration> createMobilityFacility(MobilityFacilityEnumeration value) {
        return new JAXBElement<MobilityFacilityEnumeration>(_MobilityFacility_QNAME, MobilityFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PassengerInformationFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "PassengerInformationFacility")
    public JAXBElement<PassengerInformationFacilityEnumeration> createPassengerInformationFacility(PassengerInformationFacilityEnumeration value) {
        return new JAXBElement<PassengerInformationFacilityEnumeration>(_PassengerInformationFacility_QNAME, PassengerInformationFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TicketingFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "TicketingFacility")
    public JAXBElement<TicketingFacilityEnumeration> createTicketingFacility(TicketingFacilityEnumeration value) {
        return new JAXBElement<TicketingFacilityEnumeration>(_TicketingFacility_QNAME, TicketingFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AssistanceFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "AssistanceFacility")
    public JAXBElement<AssistanceFacilityEnumeration> createAssistanceFacility(AssistanceFacilityEnumeration value) {
        return new JAXBElement<AssistanceFacilityEnumeration>(_AssistanceFacility_QNAME, AssistanceFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LuggageFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "LuggageFacility")
    public JAXBElement<LuggageFacilityEnumeration> createLuggageFacility(LuggageFacilityEnumeration value) {
        return new JAXBElement<LuggageFacilityEnumeration>(_LuggageFacility_QNAME, LuggageFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ParkingFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "ParkingFacility")
    public JAXBElement<ParkingFacilityEnumeration> createParkingFacility(ParkingFacilityEnumeration value) {
        return new JAXBElement<ParkingFacilityEnumeration>(_ParkingFacility_QNAME, ParkingFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AccessFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "AccessFacility")
    public JAXBElement<AccessFacilityEnumeration> createAccessFacility(AccessFacilityEnumeration value) {
        return new JAXBElement<AccessFacilityEnumeration>(_AccessFacility_QNAME, AccessFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RefreshmentFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "RefreshmentFacility")
    public JAXBElement<RefreshmentFacilityEnumeration> createRefreshmentFacility(RefreshmentFacilityEnumeration value) {
        return new JAXBElement<RefreshmentFacilityEnumeration>(_RefreshmentFacility_QNAME, RefreshmentFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ChouettePTNetworkType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.trident.org/schema/trident", name = "ChouettePTNetwork")
    public JAXBElement<ChouettePTNetworkType> createChouettePTNetwork(ChouettePTNetworkType value) {
        return new JAXBElement<ChouettePTNetworkType>(_ChouettePTNetwork_QNAME, ChouettePTNetworkType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SanitaryFacilityEnumeration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.siri.org.uk/siri", name = "SanitaryFacility")
    public JAXBElement<SanitaryFacilityEnumeration> createSanitaryFacility(SanitaryFacilityEnumeration value) {
        return new JAXBElement<SanitaryFacilityEnumeration>(_SanitaryFacility_QNAME, SanitaryFacilityEnumeration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.trident.org/schema/trident", name = "Comment", scope = PTAccessLinkType.class)
    public JAXBElement<String> createPTAccessLinkTypeComment(String value) {
        return new JAXBElement<String>(_PTAccessLinkTypeComment_QNAME, String.class, PTAccessLinkType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.trident.org/schema/trident", name = "Comment", scope = NonPTAccessLinkType.class)
    public JAXBElement<String> createNonPTAccessLinkTypeComment(String value) {
        return new JAXBElement<String>(_PTAccessLinkTypeComment_QNAME, String.class, NonPTAccessLinkType.class, value);
    }

}
