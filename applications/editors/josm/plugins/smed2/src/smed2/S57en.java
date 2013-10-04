package smed2;

import java.util.EnumMap;

import s57.S57obj.Obj;
import s57.S57att.Att;
import s57.S57val.*;

public class S57en {

	private static final EnumMap<Obj, String> ObjEN = new EnumMap<Obj, String>(Obj.class);
	static {
		ObjEN.put(Obj.UNKOBJ, "Unkown object");	ObjEN.put(Obj.ADMARE, "Administrative area");	ObjEN.put(Obj.AIRARE, "Airfield");	ObjEN.put(Obj.ACHBRT, "Anchor berth");
		ObjEN.put(Obj.ACHARE, "Anchorage"); ObjEN.put(Obj.BCNCAR, "Cardinal beacon");	ObjEN.put(Obj.BCNISD, "Isolated danger beacon	");
		ObjEN.put(Obj.BCNLAT, "Lateral beacon");	ObjEN.put(Obj.BCNSAW, "Safe water beacon"); ObjEN.put(Obj.BCNSPP, "Special purpose beacon");
		ObjEN.put(Obj.BERTHS, "Berth"); ObjEN.put(Obj.BRIDGE, "Bridge"); ObjEN.put(Obj.BUISGL, "Building"); ObjEN.put(Obj.BUAARE, "Built-up area");
		ObjEN.put(Obj.BOYCAR, "Cardinal buoy"); ObjEN.put(Obj.BOYINB, "Installation buoy");	ObjEN.put(Obj.BOYISD, "Isolated danger buoy");
		ObjEN.put(Obj.BOYLAT, "Lateral buoy");	ObjEN.put(Obj.BOYSAW, "Safe water buoy"); ObjEN.put(Obj.BOYSPP, "Special purpose buoy");
		ObjEN.put(Obj.CBLARE, "Cable area");	ObjEN.put(Obj.CBLOHD, "Overhead cable");	ObjEN.put(Obj.CBLSUB, "Submarine cable"); ObjEN.put(Obj.CANALS, "Canal");
		ObjEN.put(Obj.CANBNK, "Canal bank");	ObjEN.put(Obj.CTSARE, "Cargo area");	ObjEN.put(Obj.CAUSWY, "Causeway");	ObjEN.put(Obj.CTNARE, "Caution area");
		ObjEN.put(Obj.CHKPNT, "Checkpoint");	ObjEN.put(Obj.CGUSTA, "Coastguard station");	ObjEN.put(Obj.COALNE, "Coastline"); ObjEN.put(Obj.CONZNE, "Contiguous zone");
		ObjEN.put(Obj.COSARE, "Continental shelf"); ObjEN.put(Obj.CTRPNT, "Control point");	ObjEN.put(Obj.CONVYR, "Conveyor");	ObjEN.put(Obj.CRANES, "Crane");
		ObjEN.put(Obj.CURENT, "Current"); ObjEN.put(Obj.CUSZNE, "Custom zone");	ObjEN.put(Obj.DAMCON, "Dam"); ObjEN.put(Obj.DAYMAR, "Daymark");
		ObjEN.put(Obj.DWRTCL, "Deep water route centreline"); ObjEN.put(Obj.DWRTPT, "Deep water route"); ObjEN.put(Obj.DEPARE, "Depth area");
		ObjEN.put(Obj.DEPCNT, "Depth contour"); ObjEN.put(Obj.DISMAR, "Distance mark");	ObjEN.put(Obj.DOCARE, "Dock");	ObjEN.put(Obj.DRGARE, "Dredged area");
		ObjEN.put(Obj.DRYDOC, "Dry dock");	ObjEN.put(Obj.DMPGRD, "Dumping ground");	ObjEN.put(Obj.DYKCON, "Dyke");	ObjEN.put(Obj.EXEZNE, "Exclusive economic zone");
		ObjEN.put(Obj.FAIRWY, "Fairway"); ObjEN.put(Obj.FNCLNE, "Fenceline/Wall"); ObjEN.put(Obj.FERYRT, "Ferry route");	ObjEN.put(Obj.FSHZNE, "Fishery zone");
		ObjEN.put(Obj.FSHFAC, "Fishing facility");	ObjEN.put(Obj.FSHGRD, "Fishing ground");	ObjEN.put(Obj.FLODOC, "Floating dock"); ObjEN.put(Obj.FOGSIG, "Fog signal");
		ObjEN.put(Obj.FORSTC, "Fortified structure"); ObjEN.put(Obj.FRPARE, "Free port area"); ObjEN.put(Obj.GATCON, "Gate"); ObjEN.put(Obj.GRIDRN, "Gridiron");
		ObjEN.put(Obj.HRBARE, "Harbour area");	ObjEN.put(Obj.HRBFAC, "Harbour"); ObjEN.put(Obj.HULKES, "Hulk"); ObjEN.put(Obj.ICEARE, "Ice area");
		ObjEN.put(Obj.ICNARE, "Incineration zone"); ObjEN.put(Obj.ISTZNE, "Inshore traffic zone"); ObjEN.put(Obj.LAKARE, "Lake"); ObjEN.put(Obj.LAKSHR, "Lake shore");
		ObjEN.put(Obj.LNDARE, "Land area"); ObjEN.put(Obj.LNDELV, "Land elevation"); ObjEN.put(Obj.LNDRGN, "Land region");	ObjEN.put(Obj.LNDMRK, "Landmark");
		ObjEN.put(Obj.LIGHTS, "Light"); ObjEN.put(Obj.LITFLT, "Light float");	ObjEN.put(Obj.LITVES, "Light vessel");	ObjEN.put(Obj.LOCMAG, "Local magnetic anomaly");
		ObjEN.put(Obj.LOKBSN, "Lock basin");	ObjEN.put(Obj.LOGPON, "Log pond");	ObjEN.put(Obj.MAGVAR, "Magnetic variation");	ObjEN.put(Obj.MARCUL, "Marine farm");
		ObjEN.put(Obj.MIPARE, "Military practice area"); ObjEN.put(Obj.MORFAC, "Mooring");	ObjEN.put(Obj.NAVLNE, "Navigation line"); ObjEN.put(Obj.OBSTRN, "Obstruction");
		ObjEN.put(Obj.OFSPLF, "Offshore platform");	ObjEN.put(Obj.OSPARE, "Offshore production area"); ObjEN.put(Obj.OILBAR, "Oil barrier");	ObjEN.put(Obj.PILPNT, "Pile");
		ObjEN.put(Obj.PILBOP, "Pilot boarding place");	ObjEN.put(Obj.PIPARE, "Pipeline area"); ObjEN.put(Obj.PIPOHD, "Overhead pipeline");	ObjEN.put(Obj.PIPSOL, "Submarine pipeline");
		ObjEN.put(Obj.PONTON, "Pontoon"); ObjEN.put(Obj.PRCARE, "Precautionary area"); ObjEN.put(Obj.PRDARE, "Land production area");ObjEN.put(Obj.PYLONS, "Pylon");
		ObjEN.put(Obj.RADLNE, "Radar line");	ObjEN.put(Obj.RADRNG, "Radar range"); ObjEN.put(Obj.RADRFL, "Radar reflector");	ObjEN.put(Obj.RADSTA, "Radar station");
		ObjEN.put(Obj.RTPBCN, "Radar transponder"); ObjEN.put(Obj.RDOCAL, "Calling-in point"); ObjEN.put(Obj.RDOSTA, "Radio station");	ObjEN.put(Obj.RAILWY, "Railway");
		ObjEN.put(Obj.RAPIDS, "Rapids");	ObjEN.put(Obj.RCRTCL, "Recommended route centreline");	ObjEN.put(Obj.RECTRC, "Recommended track");
		ObjEN.put(Obj.RCTLPT, "Recommended traffic lane");	ObjEN.put(Obj.RSCSTA, "Rescue station");	ObjEN.put(Obj.RESARE, "Restricted area");
		ObjEN.put(Obj.RETRFL, "Retro reflector"); ObjEN.put(Obj.RIVERS, "River");	ObjEN.put(Obj.RIVBNK, "River bank");	ObjEN.put(Obj.ROADWY, "Road");
		ObjEN.put(Obj.RUNWAY, "Runway");	ObjEN.put(Obj.SNDWAV, "Sand waves");	ObjEN.put(Obj.SEAARE, "Sea area");	ObjEN.put(Obj.SPLARE, "Seaplane landing area");
		ObjEN.put(Obj.SBDARE, "Seabed area"); ObjEN.put(Obj.SLCONS, "Shoreline construction"); ObjEN.put(Obj.SISTAT, "Traffic signal station");
		ObjEN.put(Obj.SISTAW, "Warning signal station");	ObjEN.put(Obj.SILTNK, "Tank/Silo");	ObjEN.put(Obj.SLOTOP, "Slope topline"); ObjEN.put(Obj.SLOGRD, "Sloping ground");
		ObjEN.put(Obj.SMCFAC, "Small craft facility");	ObjEN.put(Obj.SOUNDG, "Sounding");	ObjEN.put(Obj.SPRING, "Spring");	ObjEN.put(Obj.SQUARE, "Square");
		ObjEN.put(Obj.STSLNE, "Territorial baseline");	ObjEN.put(Obj.SUBTLN, "Submarine transit lane");	ObjEN.put(Obj.SWPARE, "Swept area");
		ObjEN.put(Obj.TESARE, "Territorial area");	ObjEN.put(Obj.TS_PRH, "");	ObjEN.put(Obj.TS_PNH, "");	ObjEN.put(Obj.TS_PAD, "");	ObjEN.put(Obj.TS_TIS, "");
		ObjEN.put(Obj.T_HMON, "");	ObjEN.put(Obj.T_NHMN, "");	ObjEN.put(Obj.T_TIMS, "");	ObjEN.put(Obj.TIDEWY, "Tideway"); ObjEN.put(Obj.TOPMAR, "Topmark");
		ObjEN.put(Obj.TSELNE, "Separation line"); ObjEN.put(Obj.TSSBND, "Separation boundary");	ObjEN.put(Obj.TSSCRS, "Separation crossing");
		ObjEN.put(Obj.TSSLPT, "Separation lane"); ObjEN.put(Obj.TSSRON, "Separation roundabout");	ObjEN.put(Obj.TSEZNE, "Separation zone"); ObjEN.put(Obj.TUNNEL, "Tunnel");
		ObjEN.put(Obj.TWRTPT, "Two-way route"); ObjEN.put(Obj.UWTROC, "Rock"); ObjEN.put(Obj.UNSARE, "Unsurveyed area");	ObjEN.put(Obj.VEGATN, "Vegetation");
		ObjEN.put(Obj.WATTUR, "Water turbulence");	ObjEN.put(Obj.WATFAL, "Waterfall"); ObjEN.put(Obj.WEDKLP, "Weed"); ObjEN.put(Obj.WRECKS, "Wreck");
		ObjEN.put(Obj.TS_FEB, "Tidal stream");	ObjEN.put(Obj.NOTMRK, "Notice");	ObjEN.put(Obj.WTWAXS, "Waterway axis"); ObjEN.put(Obj.WTWPRF, "Waterway profile");
		ObjEN.put(Obj.BRGARE, "Bridge area"); ObjEN.put(Obj.BUNSTA, "Bunker station");	ObjEN.put(Obj.COMARE, "Communication area");	ObjEN.put(Obj.HRBBSN, "Harbour basin");
		ObjEN.put(Obj.LOKARE, "Lock area"); ObjEN.put(Obj.LKBSPT, "Lock basin part"); ObjEN.put(Obj.PRTARE, "Port area");	ObjEN.put(Obj.BCNWTW, "Waterway beacon");
		ObjEN.put(Obj.BOYWTW, "Waterway buoy"); ObjEN.put(Obj.REFDMP, "Refuse dump"); ObjEN.put(Obj.RTPLPT, "Route planning point"); ObjEN.put(Obj.TERMNL, "Terminal");
		ObjEN.put(Obj.TRNBSN, "Turning basin"); ObjEN.put(Obj.WTWARE, "Waterway area"); ObjEN.put(Obj.WTWGAG, "Waterway gauge"); ObjEN.put(Obj.TISDGE, "Time schedule");
		ObjEN.put(Obj.VEHTRF, "Vehicle transfer"); ObjEN.put(Obj.EXCNST, "Exceptional structure"); ObjEN.put(Obj.LG_SDM, ""); ObjEN.put(Obj.LG_VSP, "");
		ObjEN.put(Obj.LITMAJ, "Major light"); ObjEN.put(Obj.LITMIN, "Minor light");
	}

	private static final EnumMap<Att, String> AttEN = new EnumMap<Att, String>(Att.class);
	static {
		AttEN.put(Att.UNKATT, "Unknown attribute"); AttEN.put(Att.AGENCY, "Agency"); AttEN.put(Att.BCNSHP, "Beacon shape"); AttEN.put(Att.BUISHP, "Building shape");
		AttEN.put(Att.BOYSHP, "Buoy shape"); AttEN.put(Att.BURDEP, "Buried depth"); AttEN.put(Att.CALSGN, "Callsign"); AttEN.put(Att.CATAIR, "Airfield category");
		AttEN.put(Att.CATACH, "Anchorage category"); AttEN.put(Att.CATBRG, "Bridge category"); AttEN.put(Att.CATBUA, "Built-up area category");
		AttEN.put(Att.CATCBL, "Cable category"); AttEN.put(Att.CATCAN, "Canal category"); AttEN.put(Att.CATCAM, "Cardinal mark category"); AttEN.put(Att.CATCHP, "Checkpoint category");
		AttEN.put(Att.CATCOA, "Coastline category"); AttEN.put(Att.CATCTR, "Control point category"); AttEN.put(Att.CATCON, "Conveyor category"); AttEN.put(Att.CATCRN, "Crane category");
		AttEN.put(Att.CATDAM, "Dam category"); AttEN.put(Att.CATDIS, "Distance mark category"); AttEN.put(Att.CATDOC, "Dock category"); AttEN.put(Att.CATDPG, "Dumping ground category");
		AttEN.put(Att.CATFNC, "Fenceline category"); AttEN.put(Att.CATFRY, "Ferry category"); AttEN.put(Att.CATFIF, "Fishing facility category"); AttEN.put(Att.CATFOG, "Fog signal category");
		AttEN.put(Att.CATFOR, "Fortified structure category"); AttEN.put(Att.CATGAT, "Gate category"); AttEN.put(Att.CATHAF, "Harbour category"); AttEN.put(Att.CATHLK, "Hulk category");
		AttEN.put(Att.CATICE, "Ice category"); AttEN.put(Att.CATINB, "Installation buoy category"); AttEN.put(Att.CATLND, "Land region category"); AttEN.put(Att.CATLMK, "Landmark category");
		AttEN.put(Att.CATLAM, "Lateral mark category"); AttEN.put(Att.CATLIT, "Light category"); AttEN.put(Att.CATMFA, "Marine farm category");
		AttEN.put(Att.CATMPA, "Military practice area category"); AttEN.put(Att.CATMOR, "Morring category"); AttEN.put(Att.CATNAV, "Navigation line category");
		AttEN.put(Att.CATOBS, "Obstruction category"); AttEN.put(Att.CATOFP, "Offshore platform category"); AttEN.put(Att.CATOLB, "Oil barrier category");
		AttEN.put(Att.CATPLE, "Pile category"); AttEN.put(Att.CATPIL, "Pilot boarding category"); AttEN.put(Att.CATPIP, "Pipeline category"); AttEN.put(Att.CATPRA, "Production area category");
		AttEN.put(Att.CATPYL, "Pylon category"); AttEN.put(Att.CATRAS, "Radar station category"); AttEN.put(Att.CATRTB, "Radar transponder category");
		AttEN.put(Att.CATROS, "Radio station category"); AttEN.put(Att.CATTRK, "Recommended track category"); AttEN.put(Att.CATRSC, "Rescue station category");
		AttEN.put(Att.CATREA, "Restricted area category"); AttEN.put(Att.CATROD, "Road category"); AttEN.put(Att.CATRUN, "Runway category"); AttEN.put(Att.CATSEA, "Sea area category");
		AttEN.put(Att.CATSLC, "Shoreline construction category"); AttEN.put(Att.CATSIT, "Traffic signal station category"); AttEN.put(Att.CATSIW, "Warning signal station category");
		AttEN.put(Att.CATSIL, "Silo/tank category"); AttEN.put(Att.CATSLO, "Slope category"); AttEN.put(Att.CATSCF, "Small craft facility category");
		AttEN.put(Att.CATSPM, "Special purpose mark category"); AttEN.put(Att.CATTSS, "Traffic separation scheme category"); AttEN.put(Att.CATVEG, "Vegetation category");
		AttEN.put(Att.CATWAT, "Water turbulence category"); AttEN.put(Att.CATWED, "Weed category"); AttEN.put(Att.CATWRK, "Wreck category"); AttEN.put(Att.CATZOC, "Zone of confidence category");
		AttEN.put(Att.COLOUR, "Colour"); AttEN.put(Att.COLPAT, "Colour pattern"); AttEN.put(Att.COMCHA, "VHF channel"); AttEN.put(Att.CONDTN, "Condition");
		AttEN.put(Att.CONRAD, "Radar reflectivity"); AttEN.put(Att.CONVIS, "Visual conspicuity"); AttEN.put(Att.CURVEL, "Current velocity"); AttEN.put(Att.DATEND, "End date");
		AttEN.put(Att.DATSTA, "Start date"); AttEN.put(Att.DRVAL1, "Minimum depth"); AttEN.put(Att.DRVAL2, "Maximum depth"); AttEN.put(Att.DUNITS, "Depth units");
		AttEN.put(Att.ELEVAT, "Elevation"); AttEN.put(Att.ESTRNG, "Estimated range"); AttEN.put(Att.EXCLIT, "Light exhibition"); AttEN.put(Att.EXPSOU, "Light exposition");
		AttEN.put(Att.FUNCTN, "Function"); AttEN.put(Att.HEIGHT, "Height"); AttEN.put(Att.HUNITS, "Height/length units"); AttEN.put(Att.HORACC, "Horizontal accuracy");
		AttEN.put(Att.HORCLR, "Horizontal clearance"); AttEN.put(Att.HORLEN, "Horizontal length"); AttEN.put(Att.HORWID, "Horizontal width"); AttEN.put(Att.ICEFAC, "Ice factor");
		AttEN.put(Att.INFORM, "Information"); AttEN.put(Att.JRSDTN, "Jurisdiction"); AttEN.put(Att.LIFCAP, "Maximum load"); AttEN.put(Att.LITCHR, "Light character");
		AttEN.put(Att.LITVIS, "Light visibility"); AttEN.put(Att.MARSYS, "Buoyage system");	AttEN.put(Att.MLTYLT, "Multiplicity of lights"); AttEN.put(Att.NATION, "Nationality");
		AttEN.put(Att.NATCON, "Nature of construction"); AttEN.put(Att.NATSUR, "Nature of surface"); AttEN.put(Att.NATQUA, "Nature of surface qualification");
		AttEN.put(Att.NMDATE, "Notice to mariners date"); AttEN.put(Att.OBJNAM, "Object name"); AttEN.put(Att.ORIENT, "Orientation"); AttEN.put(Att.PEREND, "End date");
		AttEN.put(Att.PERSTA, "Start date"); AttEN.put(Att.PICREP, "Pictorial representation"); AttEN.put(Att.PILDST, "Pilot district");	AttEN.put(Att.PRCTRY, "Producing country");
		AttEN.put(Att.PRODCT, "Product"); AttEN.put(Att.PUBREF, "Publication reference"); AttEN.put(Att.QUASOU, "Quality of sounding");	AttEN.put(Att.RADWAL, "Radar wavelength");
		AttEN.put(Att.RADIUS, "Radius"); AttEN.put(Att.RECDAT, "Recording date"); AttEN.put(Att.RECIND, "Recording indication");	AttEN.put(Att.RYRMGV, "Magnetic variation reference year");
		AttEN.put(Att.RESTRN, "Restriction"); AttEN.put(Att.SECTR1, "First sector limit"); AttEN.put(Att.SECTR2, "Second sector limit");	AttEN.put(Att.SHIPAM, "Shift parameters");
		AttEN.put(Att.SIGFRQ, "Signal frequency"); AttEN.put(Att.SIGGEN, "Signal generation"); AttEN.put(Att.SIGGRP, "Signal group"); AttEN.put(Att.SIGPER, "Signal period");
		AttEN.put(Att.SIGSEQ, "Signal sequence"); AttEN.put(Att.SOUACC, "Sounding accuracy"); AttEN.put(Att.SDISMX, "Maximum sounding distance");	AttEN.put(Att.SDISMN, "Minimum sounding distance");
		AttEN.put(Att.SORDAT, "Source date"); AttEN.put(Att.SORIND, "Source"); AttEN.put(Att.STATUS, "Status");	AttEN.put(Att.SURATH, "Survey authority"); AttEN.put(Att.SUREND, "Survey end date");
		AttEN.put(Att.SURSTA, "Survey start date"); AttEN.put(Att.SURTYP, "Survey type");	AttEN.put(Att.TECSOU, "Sounding technique"); AttEN.put(Att.TXTDSC, "Textual description");
		AttEN.put(Att.TIMEND, "End time"); AttEN.put(Att.TIMSTA, "Start time");	AttEN.put(Att.TOPSHP, "Topmark/daymark shape"); AttEN.put(Att.TRAFIC, "Traffic flow");
		AttEN.put(Att.VALACM, "Magnetic variation annual change"); AttEN.put(Att.VALDCO, "Value of depth contour"); AttEN.put(Att.VALLMA, "Value of local magnetic anomaly");
		AttEN.put(Att.VALMAG, "Value of magnetic variation"); AttEN.put(Att.VALMXR, "Maximum range"); AttEN.put(Att.VALNMR, "Nominal range");	AttEN.put(Att.VALSOU, "Value of sounding");
		AttEN.put(Att.VERACC, "Vertical accuracy"); AttEN.put(Att.VERCLR, "Vertical clearance");	AttEN.put(Att.VERCCL, "Vertical clearance, closed");
		AttEN.put(Att.VERCOP, "Vertical clearance, open"); AttEN.put(Att.VERCSA, "Vertical clearance, safe"); AttEN.put(Att.VERDAT, "Vertical datum"); AttEN.put(Att.VERLEN, "Vertical length");
		AttEN.put(Att.WATLEV, "Water level effect"); AttEN.put(Att.CAT_TS, "Tidal stream category");	AttEN.put(Att.PUNITS, "Positional units"); AttEN.put(Att.NINFOM, "National information");
		AttEN.put(Att.NOBJNM, "National name"); AttEN.put(Att.NPLDST, "National pilot district"); AttEN.put(Att.NTXTDS, "National textual description"); AttEN.put(Att.HORDAT, "Horizontal datum");
		AttEN.put(Att.POSACC, "Positional accuracy"); AttEN.put(Att.QUAPOS, "Quality of position"); AttEN.put(Att.ADDMRK, "Notice mark addition"); AttEN.put(Att.BNKWTW, "Side of Waterway");
		AttEN.put(Att.CATBNK, "Waterway bank category");	AttEN.put(Att.CATNMK, "Notice mark category"); AttEN.put(Att.CLSDNG, "Dangerous cargo class"); AttEN.put(Att.DIRIMP, "Direction of impact");
		AttEN.put(Att.DISBK1, "First distance from notice");	AttEN.put(Att.DISBK2, "Second distance from notice");AttEN.put(Att.DISIPU, "Upstream distance of impact");
		AttEN.put(Att.DISIPD, "Dwonstream distance of impact"); AttEN.put(Att.ELEVA1, "Minimum elevation"); AttEN.put(Att.ELEVA2, "Maximum elevation"); AttEN.put(Att.FNCTNM, "Notice mark function");
		AttEN.put(Att.WTWDIS, "Waterway distance"); AttEN.put(Att.BUNVES, "Bunker vessel availibility");	AttEN.put(Att.CATBRT, "Berth category"); AttEN.put(Att.CATBUN, "Bunker station category");
		AttEN.put(Att.CATCCL, "CEMT class category"); AttEN.put(Att.CATHBR, "Harbour area category");	AttEN.put(Att.CATRFD, "Refuse dump category"); AttEN.put(Att.CATTML, "Terminal category");
		AttEN.put(Att.COMCTN, "Communication"); AttEN.put(Att.HORCLL, "Horizontal clearance length");	AttEN.put(Att.HORCLW, "Horizontal clearance width");
		AttEN.put(Att.TRSHGD, "Transshipping goods"); AttEN.put(Att.UNLOCD, "UN location code"); AttEN.put(Att.CATGAG, "Waterway gauge category");	AttEN.put(Att.HIGWAT, "Value at high water");
		AttEN.put(Att.HIGNAM, "Name of high water level"); AttEN.put(Att.LOWWAT, "Value at low water"); AttEN.put(Att.LOWNAM, "Name of lowwater level");
		AttEN.put(Att.MEAWAT, "Value at mean water level"); AttEN.put(Att.MEANAM, "Name of mean water level"); AttEN.put(Att.OTHWAT, "Value at local water level");
		AttEN.put(Att.OTHNAM, "Name of local water level");	AttEN.put(Att.REFLEV, "Reference gravitational level"); AttEN.put(Att.SDRLEV, "Name of sounding reference level");
		AttEN.put(Att.VCRLEV, "Name of vertical datum level"); AttEN.put(Att.CATVTR, "Vehicle transfer category");	AttEN.put(Att.CATTAB, "Time and behaviour category");
		AttEN.put(Att.SCHREF, "Time schedule reference"); AttEN.put(Att.USESHP, "Use of ship"); AttEN.put(Att.CURVHW, "high water current velocity");
		AttEN.put(Att.CURVLW, "low water current velocity"); AttEN.put(Att.CURVMW, "mean level current velocity"); AttEN.put(Att.CURVOW, "local level current velocity");
		AttEN.put(Att.APTREF, "Average passing time reference"); AttEN.put(Att.CATEXS, "Exceptional structure category"); AttEN.put(Att.CATWWM, "Waterway mark category");
		AttEN.put(Att.SHPTYP, "Ship type"); AttEN.put(Att.UPDMSG, "Update message"); AttEN.put(Att.LITRAD, "Light sector radius");
	}
	
}
