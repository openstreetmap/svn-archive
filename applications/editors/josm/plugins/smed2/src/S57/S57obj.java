package S57;

import java.util.EnumMap;

public class S57obj {
	
	public enum Obj {
		UNKOBJ, ADMARE, AIRARE, ACHBRT, ACHARE, BCNCAR, BCNISD, BCNLAT, BCNSAW, BCNSPP, BERTHS, BRIDGE, BUISGL, BUAARE, BOYCAR, BOYINB,
		BOYISD, BOYLAT, BOYSAW, BOYSPP, CBLARE, CBLOHD, CBLSUB, CANALS, CANBNK, CTSARE, CAUSWY, CTNARE, CHKPNT, CGUSTA, COALNE, CONZNE,
		COSARE, CTRPNT, CONVYR, CRANES, CURENT, CUSZNE, DAMCON, DAYMAR, DWRTCL, DWRTPT, DEPARE, DEPCNT, DISMAR, DOCARE, DRGARE, DRYDOC,
		DMPGRD, DYKCON, EXEZNE, FAIRWY, FNCLNE, FERYRT, FSHZNE, FSHFAC, FSHGRD, FLODOC, FOGSIG, FORSTC, FRPARE, GATCON, GRIDRN, HRBARE,
		HRBFAC, HULKES, ICEARE, ICNARE, ISTZNE, LAKARE, LAKSHR, LNDARE, LNDELV, LNDRGN, LNDMRK, LIGHTS, LITFLT, LITVES, LOCMAG, LOKBSN,
		LOGPON, MAGVAR, MARCUL, MIPARE, MORFAC, NAVLNE, OBSTRN, OFSPLF, OSPARE, OILBAR, PILPNT, PILBOP, PIPARE, PIPOHD, PIPSOL, PONTON,
		PRCARE, PRDARE, PYLONS, RADLNE, RADRNG, RADRFL, RADSTA, RTPBCN, RDOCAL, RDOSTA, RAILWY, RAPIDS, RCRTCL, RECTRC, RCTLPT, RSCSTA,
		RESARE, RETRFL, RIVERS, RIVBNK, ROADWY, RUNWAY, SNDWAV, SEAARE, SPLARE, SBDARE, SLCONS, SISTAT, SISTAW, SILTNK, SLOTOP, SLOGRD,
		SMCFAC, SOUNDG, SPRING, SQUARE, STSLNE, SUBTLN, SWPARE, TESARE, TS_PRH, TS_PNH, TS_PAD, TS_TIS, T_HMON, T_NHMN, T_TIMS, TIDEWY,
		TOPMAR, TSELNE, TSSBND, TSSCRS, TSSLPT, TSSRON, TSEZNE, TUNNEL, TWRTPT, UWTROC, UNSARE, VEGATN, WATTUR, WATFAL, WEDKLP, WRECKS,
		TS_FEB, M_ACCY, M_CSCL, M_COVR, M_HDAT, M_HOPA, M_NPUB, M_NSYS, M_PROD, M_QUAL, M_SDAT, M_SREL, M_UNIT, M_VDAT, C_AGGR, C_ASSO,
		C_STAC, $AREAS, $LINES, $CSYMB, $COMPS, $TEXTS, NOTMRK, WTWAXS, WTWPRF, BRGARE, BUNSTA, COMARE, HRBBSN, LOKARE, LKBSPT, PRTARE,
		BCNWTW, BOYWTW, REFDMP, RTPLPT, TERMNL, TRNBSN, WTWARE, WTWGAG, TISDGE, VEHTRF, EXCNST, LG_SDM, LG_VSP, LITMIN, LITMAJ
	}

	private static final EnumMap<Obj, Integer> ObjS57 = new EnumMap<Obj, Integer>(Obj.class);
	static {
		ObjS57.put(Obj.UNKOBJ,0); ObjS57.put(Obj.ADMARE,1); ObjS57.put(Obj.AIRARE,2); ObjS57.put(Obj.ACHBRT,3); ObjS57.put(Obj.ACHARE,4);
		ObjS57.put(Obj.BCNCAR,5); ObjS57.put(Obj.BCNISD,6); ObjS57.put(Obj.BCNLAT,7); ObjS57.put(Obj.BCNSAW,8); ObjS57.put(Obj.BCNSPP,9);
		ObjS57.put(Obj.BERTHS,10); ObjS57.put(Obj.BRIDGE,11); ObjS57.put(Obj.BUISGL,12); ObjS57.put(Obj.BUAARE,13); ObjS57.put(Obj.BOYCAR,14);
		ObjS57.put(Obj.BOYINB,15); ObjS57.put(Obj.BOYISD,16); ObjS57.put(Obj.BOYLAT,17); ObjS57.put(Obj.BOYSAW,18); ObjS57.put(Obj.BOYSPP,19);
		ObjS57.put(Obj.CBLARE,20); ObjS57.put(Obj.CBLOHD,21); ObjS57.put(Obj.CBLSUB,22); ObjS57.put(Obj.CANALS,23);	ObjS57.put(Obj.CANBNK,24);
		ObjS57.put(Obj.CTSARE,25); ObjS57.put(Obj.CAUSWY,26);	ObjS57.put(Obj.CTNARE,27); ObjS57.put(Obj.CHKPNT,28); ObjS57.put(Obj.CGUSTA,29);
		ObjS57.put(Obj.COALNE,30); ObjS57.put(Obj.CONZNE,31); ObjS57.put(Obj.COSARE,32); ObjS57.put(Obj.CTRPNT,33); ObjS57.put(Obj.CONVYR,34);
		ObjS57.put(Obj.CRANES,35); ObjS57.put(Obj.CURENT,36); ObjS57.put(Obj.CUSZNE,37); ObjS57.put(Obj.DAMCON,38);	ObjS57.put(Obj.DAYMAR,39);
		ObjS57.put(Obj.DWRTCL,40); ObjS57.put(Obj.DWRTPT,41); ObjS57.put(Obj.DEPARE,42); ObjS57.put(Obj.DEPCNT,43);	ObjS57.put(Obj.DISMAR,44);
		ObjS57.put(Obj.DOCARE,45); ObjS57.put(Obj.DRGARE,46); ObjS57.put(Obj.DRYDOC,47); ObjS57.put(Obj.DMPGRD,48); ObjS57.put(Obj.DYKCON,49);
		ObjS57.put(Obj.EXEZNE,50); ObjS57.put(Obj.FAIRWY,51); ObjS57.put(Obj.FNCLNE,52); ObjS57.put(Obj.FERYRT,53); ObjS57.put(Obj.FSHZNE,54);
		ObjS57.put(Obj.FSHFAC,55); ObjS57.put(Obj.FSHGRD,56); ObjS57.put(Obj.FLODOC,57); ObjS57.put(Obj.FOGSIG,58); ObjS57.put(Obj.FORSTC,59);
		ObjS57.put(Obj.FRPARE,60); ObjS57.put(Obj.GATCON,61); ObjS57.put(Obj.GRIDRN,62); ObjS57.put(Obj.HRBARE,63); ObjS57.put(Obj.HRBFAC,64);
		ObjS57.put(Obj.HULKES,65); ObjS57.put(Obj.ICEARE,66); ObjS57.put(Obj.ICNARE,67); ObjS57.put(Obj.ISTZNE,68); ObjS57.put(Obj.LAKARE,69);
		ObjS57.put(Obj.LAKSHR,70); ObjS57.put(Obj.LNDARE,71);	ObjS57.put(Obj.LNDELV,72); ObjS57.put(Obj.LNDRGN,73); ObjS57.put(Obj.LNDMRK,74);
		ObjS57.put(Obj.LIGHTS,75); ObjS57.put(Obj.LITFLT,76); ObjS57.put(Obj.LITVES,77); ObjS57.put(Obj.LOCMAG,78); ObjS57.put(Obj.LOKBSN,79);
		ObjS57.put(Obj.LOGPON,80); ObjS57.put(Obj.MAGVAR,81); ObjS57.put(Obj.MARCUL,82); ObjS57.put(Obj.MIPARE,83); ObjS57.put(Obj.MORFAC,84);
		ObjS57.put(Obj.NAVLNE,85); ObjS57.put(Obj.OBSTRN,86); ObjS57.put(Obj.OFSPLF,87); ObjS57.put(Obj.OSPARE,88); ObjS57.put(Obj.OILBAR,89);
		ObjS57.put(Obj.PILPNT,90); ObjS57.put(Obj.PILBOP,91);	ObjS57.put(Obj.PIPARE,92); ObjS57.put(Obj.PIPOHD,93); ObjS57.put(Obj.PIPSOL,94);
		ObjS57.put(Obj.PONTON,95); ObjS57.put(Obj.PRCARE,96); ObjS57.put(Obj.PRDARE,97); ObjS57.put(Obj.PYLONS,98); ObjS57.put(Obj.RADLNE,99);
		ObjS57.put(Obj.RADRNG,100); ObjS57.put(Obj.RADRFL,101); ObjS57.put(Obj.RADSTA,102); ObjS57.put(Obj.RTPBCN,103);	ObjS57.put(Obj.RDOCAL,104);
		ObjS57.put(Obj.RDOSTA,105);	ObjS57.put(Obj.RAILWY,106);	ObjS57.put(Obj.RAPIDS,107);	ObjS57.put(Obj.RCRTCL,108);	ObjS57.put(Obj.RECTRC,109);
		ObjS57.put(Obj.RCTLPT,110);	ObjS57.put(Obj.RSCSTA,111);	ObjS57.put(Obj.RESARE,112);	ObjS57.put(Obj.RETRFL,113);	ObjS57.put(Obj.RIVERS,114);
		ObjS57.put(Obj.RIVBNK,115);	ObjS57.put(Obj.ROADWY,116);	ObjS57.put(Obj.RUNWAY,117);	ObjS57.put(Obj.SNDWAV,118);	ObjS57.put(Obj.SEAARE,119);
		ObjS57.put(Obj.SPLARE,120);	ObjS57.put(Obj.SBDARE,121);	ObjS57.put(Obj.SLCONS,122);	ObjS57.put(Obj.SISTAT,123);	ObjS57.put(Obj.SISTAW,124);
		ObjS57.put(Obj.SILTNK,125);	ObjS57.put(Obj.SLOTOP,126);	ObjS57.put(Obj.SLOGRD,127);	ObjS57.put(Obj.SMCFAC,128);	ObjS57.put(Obj.SOUNDG,129);
		ObjS57.put(Obj.SPRING,130);	ObjS57.put(Obj.SQUARE,131);	ObjS57.put(Obj.STSLNE,132);	ObjS57.put(Obj.SUBTLN,133);	ObjS57.put(Obj.SWPARE,134);
		ObjS57.put(Obj.TESARE,135); ObjS57.put(Obj.TS_PRH,136);	ObjS57.put(Obj.TS_PNH,137); ObjS57.put(Obj.TS_PAD,138);	ObjS57.put(Obj.TS_TIS,139);
		ObjS57.put(Obj.T_HMON,140);	ObjS57.put(Obj.T_NHMN,141);	ObjS57.put(Obj.T_TIMS,142);	ObjS57.put(Obj.TIDEWY,143);	ObjS57.put(Obj.TOPMAR,144);
		ObjS57.put(Obj.TSELNE,145);	ObjS57.put(Obj.TSSBND,146);	ObjS57.put(Obj.TSSCRS,147);	ObjS57.put(Obj.TSSLPT,148);	ObjS57.put(Obj.TSSRON,149);
		ObjS57.put(Obj.TSEZNE,150);	ObjS57.put(Obj.TUNNEL,151);	ObjS57.put(Obj.TWRTPT,152);	ObjS57.put(Obj.UWTROC,153);	ObjS57.put(Obj.UNSARE,154);
		ObjS57.put(Obj.VEGATN,155);	ObjS57.put(Obj.WATTUR,156);	ObjS57.put(Obj.WATFAL,157);	ObjS57.put(Obj.WEDKLP,158);	ObjS57.put(Obj.WRECKS,159);
		ObjS57.put(Obj.TS_FEB,160);	ObjS57.put(Obj.M_ACCY,300);	ObjS57.put(Obj.M_CSCL,301);	ObjS57.put(Obj.M_COVR,302);	ObjS57.put(Obj.M_HDAT,303);
		ObjS57.put(Obj.M_HOPA,304);	ObjS57.put(Obj.M_NPUB,305);	ObjS57.put(Obj.M_NSYS,306);	ObjS57.put(Obj.M_PROD,307);	ObjS57.put(Obj.M_QUAL,308);
		ObjS57.put(Obj.M_SDAT,309);	ObjS57.put(Obj.M_SREL,310);	ObjS57.put(Obj.M_UNIT,311);	ObjS57.put(Obj.M_VDAT,312);	ObjS57.put(Obj.C_AGGR,400);
		ObjS57.put(Obj.C_ASSO,401);	ObjS57.put(Obj.C_STAC,402);	ObjS57.put(Obj.$AREAS,500);	ObjS57.put(Obj.$LINES,501);	ObjS57.put(Obj.$CSYMB,502);
		ObjS57.put(Obj.$COMPS,503);	ObjS57.put(Obj.$TEXTS,504);
	}
	private static final EnumMap<Obj, Integer> ObjIENC = new EnumMap<Obj, Integer>(Obj.class);
	static {
		ObjIENC.put(Obj.UNKOBJ, 0);	ObjIENC.put(Obj.ACHBRT, 17000);	ObjIENC.put(Obj.ACHARE, 17001);	ObjIENC.put(Obj.CANBNK, 17002);	ObjIENC.put(Obj.DEPARE, 17003);
		ObjIENC.put(Obj.DISMAR, 17004);	ObjIENC.put(Obj.RESARE, 17005);	ObjIENC.put(Obj.RIVBNK, 17006);	ObjIENC.put(Obj.SISTAT, 17007);	ObjIENC.put(Obj.SISTAW, 17008);
		ObjIENC.put(Obj.TOPMAR, 17009);	ObjIENC.put(Obj.BERTHS, 17010);	ObjIENC.put(Obj.BRIDGE, 17011);	ObjIENC.put(Obj.CBLOHD, 17012);	ObjIENC.put(Obj.FERYRT, 17013);
		ObjIENC.put(Obj.HRBARE, 17014);	ObjIENC.put(Obj.HRBFAC, 17015);	ObjIENC.put(Obj.LOKBSN, 17016);	ObjIENC.put(Obj.RDOCAL, 17017);	ObjIENC.put(Obj.M_NSYS, 17018);
		ObjIENC.put(Obj.CURENT, 17019);	ObjIENC.put(Obj.HULKES, 17020);	ObjIENC.put(Obj.PONTON, 17021);	ObjIENC.put(Obj.M_SDAT, 17022);	ObjIENC.put(Obj.M_VDAT, 17023);
		ObjIENC.put(Obj.PIPOHD, 17024);	ObjIENC.put(Obj.FLODOC, 17025);	ObjIENC.put(Obj.CHKPNT, 17027);	ObjIENC.put(Obj.BCNLAT, 17028);	ObjIENC.put(Obj.BOYLAT, 17029);
		ObjIENC.put(Obj.CRANES, 17030);	ObjIENC.put(Obj.GATCON, 17031);	ObjIENC.put(Obj.SLCONS, 17032);	ObjIENC.put(Obj.UWTROC, 17033);	ObjIENC.put(Obj.CONVYR, 17034);
		ObjIENC.put(Obj.NOTMRK, 17050);	ObjIENC.put(Obj.WTWAXS, 17051);	ObjIENC.put(Obj.WTWPRF, 17052);	ObjIENC.put(Obj.BRGARE, 17053);	ObjIENC.put(Obj.BUNSTA, 17054);
		ObjIENC.put(Obj.COMARE, 17055);	ObjIENC.put(Obj.HRBBSN, 17056);	ObjIENC.put(Obj.LOKARE, 17057);	ObjIENC.put(Obj.LKBSPT, 17058);	ObjIENC.put(Obj.PRTARE, 17059);
		ObjIENC.put(Obj.BCNWTW, 17060);	ObjIENC.put(Obj.BOYWTW, 17061);	ObjIENC.put(Obj.REFDMP, 17062);	ObjIENC.put(Obj.RTPLPT, 17063);	ObjIENC.put(Obj.TERMNL, 17064);
		ObjIENC.put(Obj.TRNBSN, 17065);	ObjIENC.put(Obj.WTWARE, 17066);	ObjIENC.put(Obj.WTWGAG, 17067);	ObjIENC.put(Obj.TISDGE, 17068);	ObjIENC.put(Obj.VEHTRF, 17069);
		ObjIENC.put(Obj.EXCNST, 17070);	ObjIENC.put(Obj.LG_SDM, 18001);	ObjIENC.put(Obj.LG_VSP, 18002);
	}

	private static final EnumMap<Obj, String> ObjSTR = new EnumMap<Obj, String>(Obj.class);
	static {
		ObjSTR.put(Obj.UNKOBJ, "");	ObjSTR.put(Obj.ADMARE, "administration");	ObjSTR.put(Obj.AIRARE, "airfield");	ObjSTR.put(Obj.ACHBRT, "anchor_berth");
		ObjSTR.put(Obj.ACHARE, "anchorage"); ObjSTR.put(Obj.BCNCAR, "beacon_cardinal");	ObjSTR.put(Obj.BCNISD, "beacon_isolated_danger");
		ObjSTR.put(Obj.BCNLAT, "beacon_lateral");	ObjSTR.put(Obj.BCNSAW, "beacon_safe_water"); ObjSTR.put(Obj.BCNSPP, "beacon_special_purpose");
		ObjSTR.put(Obj.BERTHS, "berth"); ObjSTR.put(Obj.BRIDGE, "bridge"); ObjSTR.put(Obj.BUISGL, "building"); ObjSTR.put(Obj.BUAARE, "built-up_area");
		ObjSTR.put(Obj.BOYCAR, "buoy_cardinal"); ObjSTR.put(Obj.BOYINB, "buoy_installation");	ObjSTR.put(Obj.BOYISD, "buoy_isolated_danger");
		ObjSTR.put(Obj.BOYLAT, "buoy_lateral");	ObjSTR.put(Obj.BOYSAW, "buoy_safe_water"); ObjSTR.put(Obj.BOYSPP, "buoy_special_purpose");
		ObjSTR.put(Obj.CBLARE, "cable_area");	ObjSTR.put(Obj.CBLOHD, "cable_overhead");	ObjSTR.put(Obj.CBLSUB, "cable_submarine"); ObjSTR.put(Obj.CANALS, "canal");
		ObjSTR.put(Obj.CANBNK, "canal_bank");	ObjSTR.put(Obj.CTSARE, "cargo_area");	ObjSTR.put(Obj.CAUSWY, "causeway");	ObjSTR.put(Obj.CTNARE, "caution_area");
		ObjSTR.put(Obj.CHKPNT, "checkpoint");	ObjSTR.put(Obj.CGUSTA, "coastguard_station");	ObjSTR.put(Obj.COALNE, "coastline"); ObjSTR.put(Obj.CONZNE, "contiguous_zone");
		ObjSTR.put(Obj.COSARE, "continental_shelf"); ObjSTR.put(Obj.CTRPNT, "control_point");	ObjSTR.put(Obj.CONVYR, "conveyor");	ObjSTR.put(Obj.CRANES, "crane");
		ObjSTR.put(Obj.CURENT, "current"); ObjSTR.put(Obj.CUSZNE, "custom_zone");	ObjSTR.put(Obj.DAMCON, "dam"); ObjSTR.put(Obj.DAYMAR, "daymark");
		ObjSTR.put(Obj.DWRTCL, "deep_water_route_centreline"); ObjSTR.put(Obj.DWRTPT, "deep_water_route"); ObjSTR.put(Obj.DEPARE, "depth_area");
		ObjSTR.put(Obj.DEPCNT, "depth_contour"); ObjSTR.put(Obj.DISMAR, "distance_mark");	ObjSTR.put(Obj.DOCARE, "dock");	ObjSTR.put(Obj.DRGARE, "dredged_area");
		ObjSTR.put(Obj.DRYDOC, "dry_dock");	ObjSTR.put(Obj.DMPGRD, "dumping_ground");	ObjSTR.put(Obj.DYKCON, "dyke");	ObjSTR.put(Obj.EXEZNE, "exclusive_economic_zone");
		ObjSTR.put(Obj.FAIRWY, "fairway"); ObjSTR.put(Obj.FNCLNE, "wall"); ObjSTR.put(Obj.FERYRT, "ferry_route");	ObjSTR.put(Obj.FSHZNE, "fishery_zone");
		ObjSTR.put(Obj.FSHFAC, "fishing_facility");	ObjSTR.put(Obj.FSHGRD, "fishing_ground");	ObjSTR.put(Obj.FLODOC, "floating_dock"); ObjSTR.put(Obj.FOGSIG, "fog_signal");
		ObjSTR.put(Obj.FORSTC, "fortified_structure"); ObjSTR.put(Obj.FRPARE, "free_port_area"); ObjSTR.put(Obj.GATCON, "gate"); ObjSTR.put(Obj.GRIDRN, "gridiron");
		ObjSTR.put(Obj.HRBARE, "harbour_area");	ObjSTR.put(Obj.HRBFAC, "harbour"); ObjSTR.put(Obj.HULKES, "hulk"); ObjSTR.put(Obj.ICEARE, "ice_area");
		ObjSTR.put(Obj.ICNARE, "incineration_zone"); ObjSTR.put(Obj.ISTZNE, "inshore_traffic_zone"); ObjSTR.put(Obj.LAKARE, "lake"); ObjSTR.put(Obj.LAKSHR, "lake_shore");
		ObjSTR.put(Obj.LNDARE, "land_area"); ObjSTR.put(Obj.LNDELV, "land_elevation"); ObjSTR.put(Obj.LNDRGN, "land_region");	ObjSTR.put(Obj.LNDMRK, "landmark");
		ObjSTR.put(Obj.LIGHTS, "light"); ObjSTR.put(Obj.LITFLT, "light_float");	ObjSTR.put(Obj.LITVES, "light_vessel");	ObjSTR.put(Obj.LOCMAG, "local_magnetic_anomaly");
		ObjSTR.put(Obj.LOKBSN, "lock_basin");	ObjSTR.put(Obj.LOGPON, "log_pond");	ObjSTR.put(Obj.MAGVAR, "magnetic_variation");	ObjSTR.put(Obj.MARCUL, "marine_farm");
		ObjSTR.put(Obj.MIPARE, "military_area"); ObjSTR.put(Obj.MORFAC, "mooring");	ObjSTR.put(Obj.NAVLNE, "navigation_line"); ObjSTR.put(Obj.OBSTRN, "obstruction");
		ObjSTR.put(Obj.OFSPLF, "platform");	ObjSTR.put(Obj.OSPARE, "production_area"); ObjSTR.put(Obj.OILBAR, "oil_barrier");	ObjSTR.put(Obj.PILPNT, "pile");
		ObjSTR.put(Obj.PILBOP, "pilot_boarding");	ObjSTR.put(Obj.PIPARE, "pipeline_area"); ObjSTR.put(Obj.PIPOHD, "pipeline_overhead");	ObjSTR.put(Obj.PIPSOL, "pipeline_submarine");
		ObjSTR.put(Obj.PONTON, "pontoon"); ObjSTR.put(Obj.PRCARE, "precautionary_area"); ObjSTR.put(Obj.PRDARE, "land_production_area");ObjSTR.put(Obj.PYLONS, "pylon");
		ObjSTR.put(Obj.RADLNE, "radar_line");	ObjSTR.put(Obj.RADRNG, "radar_range"); ObjSTR.put(Obj.RADRFL, "radar_reflector");	ObjSTR.put(Obj.RADSTA, "radar_station");
		ObjSTR.put(Obj.RTPBCN, "radar_transponder"); ObjSTR.put(Obj.RDOCAL, "calling-in_point"); ObjSTR.put(Obj.RDOSTA, "radio_station");	ObjSTR.put(Obj.RAILWY, "railway");
		ObjSTR.put(Obj.RAPIDS, "rapids");	ObjSTR.put(Obj.RCRTCL, "recommended_route_centreline");	ObjSTR.put(Obj.RECTRC, "recommended_track");
		ObjSTR.put(Obj.RCTLPT, "recommended_traffic_lane");	ObjSTR.put(Obj.RSCSTA, "rescue_station");	ObjSTR.put(Obj.RESARE, "restricted_area");
		ObjSTR.put(Obj.RETRFL, "retro_reflector"); ObjSTR.put(Obj.RIVERS, "river");	ObjSTR.put(Obj.RIVBNK, "river_bank");	ObjSTR.put(Obj.ROADWY, "road");
		ObjSTR.put(Obj.RUNWAY, "runway");	ObjSTR.put(Obj.SNDWAV, "sand_waves");	ObjSTR.put(Obj.SEAARE, "sea_area");	ObjSTR.put(Obj.SPLARE, "seaplane_landing_area");
		ObjSTR.put(Obj.SBDARE, "seabed_area"); ObjSTR.put(Obj.SLCONS, "shoreline_construction"); ObjSTR.put(Obj.SISTAT, "signal_station_traffic");
		ObjSTR.put(Obj.SISTAW, "signal_station_warning");	ObjSTR.put(Obj.SILTNK, "tank");	ObjSTR.put(Obj.SLOTOP, "slope_topline"); ObjSTR.put(Obj.SLOGRD, "sloping_ground");
		ObjSTR.put(Obj.SMCFAC, "small_craft_facility");	ObjSTR.put(Obj.SOUNDG, "sounding");	ObjSTR.put(Obj.SPRING, "spring");	ObjSTR.put(Obj.SQUARE, "square");
		ObjSTR.put(Obj.STSLNE, "territorial_baseline");	ObjSTR.put(Obj.SUBTLN, "submarine_transit_lane");	ObjSTR.put(Obj.SWPARE, "swept_area");
		ObjSTR.put(Obj.TESARE, "territorial_area");	ObjSTR.put(Obj.TS_PRH, "");	ObjSTR.put(Obj.TS_PNH, "");	ObjSTR.put(Obj.TS_PAD, "");	ObjSTR.put(Obj.TS_TIS, "");
		ObjSTR.put(Obj.T_HMON, "");	ObjSTR.put(Obj.T_NHMN, "");	ObjSTR.put(Obj.T_TIMS, "");	ObjSTR.put(Obj.TIDEWY, "tideway"); ObjSTR.put(Obj.TOPMAR, "topmark");
		ObjSTR.put(Obj.TSELNE, "separation_line"); ObjSTR.put(Obj.TSSBND, "separation_boundary");	ObjSTR.put(Obj.TSSCRS, "separation_crossing");
		ObjSTR.put(Obj.TSSLPT, "separation_lane"); ObjSTR.put(Obj.TSSRON, "separation_roundabout");	ObjSTR.put(Obj.TSEZNE, "separation_zone"); ObjSTR.put(Obj.TUNNEL, "tunnel");
		ObjSTR.put(Obj.TWRTPT, "two-way_route"); ObjSTR.put(Obj.UWTROC, "rock"); ObjSTR.put(Obj.UNSARE, "unsurveyed_area");	ObjSTR.put(Obj.VEGATN, "vegetation");
		ObjSTR.put(Obj.WATTUR, "water_turbulence");	ObjSTR.put(Obj.WATFAL, "waterfall"); ObjSTR.put(Obj.WEDKLP, "weed"); ObjSTR.put(Obj.WRECKS, "wreck");
		ObjSTR.put(Obj.TS_FEB, "tidal_stream");	ObjSTR.put(Obj.M_ACCY, "");	ObjSTR.put(Obj.M_CSCL, "");	ObjSTR.put(Obj.M_COVR, "coverage");	ObjSTR.put(Obj.M_HDAT, "");
		ObjSTR.put(Obj.M_HOPA, "");	ObjSTR.put(Obj.M_NPUB, "");	ObjSTR.put(Obj.M_NSYS, "");	ObjSTR.put(Obj.M_PROD, "");	ObjSTR.put(Obj.M_QUAL, "data_quality");
		ObjSTR.put(Obj.M_SDAT, "");	ObjSTR.put(Obj.M_SREL, "");	ObjSTR.put(Obj.M_UNIT, "");	ObjSTR.put(Obj.M_VDAT, "");	ObjSTR.put(Obj.C_AGGR, "");	ObjSTR.put(Obj.C_ASSO, "");
		ObjSTR.put(Obj.C_STAC, "");	ObjSTR.put(Obj.$AREAS, "");	ObjSTR.put(Obj.$LINES, "");	ObjSTR.put(Obj.$CSYMB, "");	ObjSTR.put(Obj.$COMPS, "");	ObjSTR.put(Obj.$TEXTS, "");
		ObjSTR.put(Obj.NOTMRK, "notice");	ObjSTR.put(Obj.WTWAXS, "waterway_axis"); ObjSTR.put(Obj.WTWPRF, "waterway_profile"); ObjSTR.put(Obj.BRGARE, "bridge_area");
		ObjSTR.put(Obj.BUNSTA, "bunker_station");	ObjSTR.put(Obj.COMARE, "communication_area");	ObjSTR.put(Obj.HRBBSN, "harbour_basin"); ObjSTR.put(Obj.LOKARE, "lock_area");
		ObjSTR.put(Obj.LKBSPT, "lock_basin_part"); ObjSTR.put(Obj.PRTARE, "port_area");	ObjSTR.put(Obj.BCNWTW, "beacon_waterway"); ObjSTR.put(Obj.BOYWTW, "buoy_waterway");
		ObjSTR.put(Obj.REFDMP, "refuse_dump"); ObjSTR.put(Obj.RTPLPT, "route_planning_point"); ObjSTR.put(Obj.TERMNL, "terminal"); ObjSTR.put(Obj.TRNBSN, "turning_basin");
		ObjSTR.put(Obj.WTWARE, "waterway_area"); ObjSTR.put(Obj.WTWGAG, "waterway_gauge"); ObjSTR.put(Obj.TISDGE, "time_schedule");	ObjSTR.put(Obj.VEHTRF, "vehicle_transfer");
		ObjSTR.put(Obj.EXCNST, "exceptional_structure"); ObjSTR.put(Obj.LG_SDM, ""); ObjSTR.put(Obj.LG_VSP, ""); ObjSTR.put(Obj.LITMAJ, "light_major"); ObjSTR.put(Obj.LITMIN, "light_minor");
	}

	public static String decodeType(Integer type) { // Convert S57 feature code to OSeaM object string
		String str = ObjSTR.get(lookupType(type));
		return str != null ? str : "";
	}

	public static Integer encodeType(String type) { // Convert OSM object string to S57 feature code
		if (type != null) {
			for (Obj obj : ObjSTR.keySet()) {
				if (ObjSTR.get(obj).equals(type)) {
					if (ObjS57.get(obj) != null)
						return ObjS57.get(obj);
					else if (ObjIENC.get(obj) != null)
						return ObjIENC.get(obj);
					else break;
				}
			}
		}
		return 0;
	}

	public static Integer encodeType(Obj type) { // Convert OSM object enumeration to S57 feature code
		if (ObjS57.get(type) != null)
			return ObjS57.get(type);
		else if (ObjIENC.get(type) != null)
			return ObjIENC.get(type);
		return 0;
	}

	public static Obj lookupType(Integer type) {	// Convert S57 feature code to OSeaM object enumeration
		if (type < 10000) {
			for (Obj obj : ObjS57.keySet()) {
				if (ObjS57.get(obj).equals(type)) {
					return obj;
				}
			}
		} else { 
			for (Obj obj : ObjIENC.keySet()) {
				if (ObjIENC.get(obj).equals(type)) {
					return obj;
				}
			}
		}
		return Obj.UNKOBJ;
	}

	public static String stringType(Obj type) { // Convert OSeaM object enumeration to OSeaM object string
		String str = ObjSTR.get(type);
			return str != null ? str : "";
	}

	public static Obj enumType(String type) { // Convert OSeaM object string to OSeaM object enumeration
		for (Obj obj : ObjSTR.keySet()) {
			if (ObjSTR.get(obj).equals(type)) {
				return obj;
			}
		}
		return Obj.UNKOBJ;
	}

}
