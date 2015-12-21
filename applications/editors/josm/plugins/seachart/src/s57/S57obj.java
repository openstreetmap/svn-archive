/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package s57;

import java.util.*;

public class S57obj { // S57 Object lookup tables & methods
	
	public enum Obj {
		UNKOBJ, M_COVR, M_NSYS, AIRARE, ACHBRT, ACHARE, BCNCAR, BCNISD, BCNLAT, BCNSAW, BCNSPP, BERTHS, BRIDGE, BUISGL, BUAARE, BOYCAR,
		BOYINB, BOYISD, BOYLAT, BOYSAW, BOYSPP, CBLARE, CBLOHD, CBLSUB, CANALS, CTSARE, CAUSWY, CTNARE, CHKPNT, CGUSTA, COALNE, CONZNE,
		COSARE, CTRPNT, CONVYR, CRANES, CURENT, CUSZNE, DAMCON, DAYMAR, DWRTCL, DWRTPT, DEPARE, DEPCNT, DISMAR, DOCARE, DRGARE, DRYDOC,
		DMPGRD, DYKCON, EXEZNE, FAIRWY, FNCLNE, FERYRT, FSHZNE, FSHFAC, FSHGRD, FLODOC, FOGSIG, FORSTC, FRPARE, GATCON, GRIDRN, HRBARE,
		HRBFAC, HULKES, ICEARE, ICNARE, ISTZNE, LAKARE, LNDARE, LNDELV, LNDRGN, LNDMRK, LIGHTS, LITFLT, LITVES, LOCMAG, LOKBSN, LOGPON,
		MAGVAR, MARCUL, MIPARE, MORFAC, MPAARE, NAVLNE, OBSTRN, OFSPLF, OSPARE, OILBAR, PILPNT, PILBOP, PIPARE, PIPOHD, PIPSOL, PONTON,
		PRCARE, PRDARE, PYLONS, RADLNE, RADRNG, RADRFL, RADSTA, RTPBCN, RDOCAL, RDOSTA, RAILWY, RAPIDS, RCRTCL, RECTRC, RCTLPT, RSCSTA,
		RESARE, RETRFL, RIVERS, ROADWY, RUNWAY, SNDWAV, SEAARE, SPLARE, SBDARE, SLCONS, SISTAT, SISTAW, SILTNK, SLOTOP, SLOGRD, SMCFAC,
		SOUNDG, SPRING, STSLNE, SUBTLN, SWPARE, TESARE, TS_PRH, TS_PNH, TS_PAD, TS_TIS, T_HMON, T_NHMN, T_TIMS, TIDEWY, TOPMAR, TSELNE,
		TSSBND, TSSCRS, TSSLPT, TSSRON, TSEZNE, TUNNEL, TWRTPT, UWTROC, UNSARE, VEGATN, WATTUR, WATFAL, WEDKLP, WRECKS, TS_FEB, NOTMRK,
		WTWAXS, WTWPRF, BUNSTA, COMARE, HRBBSN, LOKARE, LKBSPT, PRTARE, REFDMP, TERMNL, TRNBSN, WTWARE, WTWGAG, TISDGE, VEHTRF, EXCNST,
		LG_SDM, LG_VSP, LITMIN, LITMAJ
	}

	private static final EnumMap<Obj, Integer> ObjS57 = new EnumMap<>(Obj.class);
	static {
		ObjS57.put(Obj.UNKOBJ,0);
		ObjS57.put(Obj.AIRARE,2); ObjS57.put(Obj.ACHBRT,3); ObjS57.put(Obj.ACHARE,4); ObjS57.put(Obj.BCNCAR,5); ObjS57.put(Obj.BCNISD,6);
		ObjS57.put(Obj.BCNLAT,7); ObjS57.put(Obj.BCNSAW,8); ObjS57.put(Obj.BCNSPP,9); ObjS57.put(Obj.BERTHS,10); ObjS57.put(Obj.BRIDGE,11);
		ObjS57.put(Obj.BUISGL,12); ObjS57.put(Obj.BUAARE,13); ObjS57.put(Obj.BOYCAR,14); ObjS57.put(Obj.BOYINB,15); ObjS57.put(Obj.BOYISD,16);
		ObjS57.put(Obj.BOYLAT,17); ObjS57.put(Obj.BOYSAW,18); ObjS57.put(Obj.BOYSPP,19); ObjS57.put(Obj.CBLARE,20); ObjS57.put(Obj.CBLOHD,21);
		ObjS57.put(Obj.CBLSUB,22); ObjS57.put(Obj.CANALS,23); ObjS57.put(Obj.CTSARE,25); ObjS57.put(Obj.CAUSWY,26); ObjS57.put(Obj.CTNARE,27);
		ObjS57.put(Obj.CHKPNT,28); ObjS57.put(Obj.CGUSTA,29); ObjS57.put(Obj.COALNE,30); ObjS57.put(Obj.CONZNE,31); ObjS57.put(Obj.COSARE,32);
		ObjS57.put(Obj.CTRPNT,33); ObjS57.put(Obj.CONVYR,34); ObjS57.put(Obj.CRANES,35); ObjS57.put(Obj.CURENT,36); ObjS57.put(Obj.CUSZNE,37);
		ObjS57.put(Obj.DAMCON,38); ObjS57.put(Obj.DAYMAR,39); ObjS57.put(Obj.DWRTCL,40); ObjS57.put(Obj.DWRTPT,41); ObjS57.put(Obj.DEPARE,42);
		ObjS57.put(Obj.DEPCNT,43); ObjS57.put(Obj.DISMAR,44); ObjS57.put(Obj.DOCARE,45); ObjS57.put(Obj.DRGARE,46); ObjS57.put(Obj.DRYDOC,47);
		ObjS57.put(Obj.DMPGRD,48); ObjS57.put(Obj.DYKCON,49); ObjS57.put(Obj.EXEZNE,50); ObjS57.put(Obj.FAIRWY,51); ObjS57.put(Obj.FNCLNE,52);
		ObjS57.put(Obj.FERYRT,53); ObjS57.put(Obj.FSHZNE,54); ObjS57.put(Obj.FSHFAC,55); ObjS57.put(Obj.FSHGRD,56); ObjS57.put(Obj.FLODOC,57);
		ObjS57.put(Obj.FOGSIG,58); ObjS57.put(Obj.FORSTC,59); ObjS57.put(Obj.FRPARE,60); ObjS57.put(Obj.GATCON,61); ObjS57.put(Obj.GRIDRN,62);
		ObjS57.put(Obj.HRBARE,63); ObjS57.put(Obj.HRBFAC,64); ObjS57.put(Obj.HULKES,65); ObjS57.put(Obj.ICEARE,66); ObjS57.put(Obj.ICNARE,67);
		ObjS57.put(Obj.ISTZNE,68); ObjS57.put(Obj.LAKARE,69); ObjS57.put(Obj.LNDARE,71); ObjS57.put(Obj.LNDELV,72); ObjS57.put(Obj.LNDRGN,73);
		ObjS57.put(Obj.LNDMRK,74); ObjS57.put(Obj.LIGHTS,75); ObjS57.put(Obj.LITFLT,76); ObjS57.put(Obj.LITVES,77); ObjS57.put(Obj.LOCMAG,78);
		ObjS57.put(Obj.LOKBSN,79); ObjS57.put(Obj.LOGPON,80); ObjS57.put(Obj.MAGVAR,81); ObjS57.put(Obj.MARCUL,82); ObjS57.put(Obj.MIPARE,83);
		ObjS57.put(Obj.MORFAC,84); ObjS57.put(Obj.NAVLNE,85); ObjS57.put(Obj.OBSTRN,86); ObjS57.put(Obj.OFSPLF,87); ObjS57.put(Obj.OSPARE,88);
		ObjS57.put(Obj.OILBAR,89); ObjS57.put(Obj.PILPNT,90); ObjS57.put(Obj.PILBOP,91); ObjS57.put(Obj.PIPARE,92); ObjS57.put(Obj.PIPOHD,93);
		ObjS57.put(Obj.PIPSOL,94); ObjS57.put(Obj.PONTON,95); ObjS57.put(Obj.PRCARE,96); ObjS57.put(Obj.PRDARE,97); ObjS57.put(Obj.PYLONS,98);
		ObjS57.put(Obj.RADLNE,99); ObjS57.put(Obj.RADRNG,100); ObjS57.put(Obj.RADRFL,101); ObjS57.put(Obj.RADSTA,102); ObjS57.put(Obj.RTPBCN,103);
		ObjS57.put(Obj.RDOCAL,104); ObjS57.put(Obj.RDOSTA,105); ObjS57.put(Obj.RAILWY,106);	ObjS57.put(Obj.RAPIDS,107);	ObjS57.put(Obj.RCRTCL,108);
		ObjS57.put(Obj.RECTRC,109); ObjS57.put(Obj.RCTLPT,110); ObjS57.put(Obj.RSCSTA,111);	ObjS57.put(Obj.RESARE,112);	ObjS57.put(Obj.RETRFL,113);
		ObjS57.put(Obj.RIVERS,114); ObjS57.put(Obj.ROADWY,116);	ObjS57.put(Obj.RUNWAY,117);	ObjS57.put(Obj.SNDWAV,118);	ObjS57.put(Obj.SEAARE,119);
		ObjS57.put(Obj.SPLARE,120); ObjS57.put(Obj.SBDARE,121);	ObjS57.put(Obj.SLCONS,122);	ObjS57.put(Obj.SISTAT,123);	ObjS57.put(Obj.SISTAW,124);
		ObjS57.put(Obj.SILTNK,125); ObjS57.put(Obj.SLOTOP,126);	ObjS57.put(Obj.SLOGRD,127);	ObjS57.put(Obj.SMCFAC,128);	ObjS57.put(Obj.SOUNDG,129);
		ObjS57.put(Obj.SPRING,130); ObjS57.put(Obj.STSLNE,132);	ObjS57.put(Obj.SUBTLN,133);	ObjS57.put(Obj.SWPARE,134); ObjS57.put(Obj.TESARE,135);
		ObjS57.put(Obj.TS_PRH,136);	ObjS57.put(Obj.TS_PNH,137); ObjS57.put(Obj.TS_PAD,138);	ObjS57.put(Obj.TS_TIS,139); ObjS57.put(Obj.T_HMON,140);
		ObjS57.put(Obj.T_NHMN,141);	ObjS57.put(Obj.T_TIMS,142);	ObjS57.put(Obj.TIDEWY,143);	ObjS57.put(Obj.TOPMAR,144); ObjS57.put(Obj.TSELNE,145);
		ObjS57.put(Obj.TSSBND,146);	ObjS57.put(Obj.TSSCRS,147);	ObjS57.put(Obj.TSSLPT,148);	ObjS57.put(Obj.TSSRON,149); ObjS57.put(Obj.TSEZNE,150);
		ObjS57.put(Obj.TUNNEL,151);	ObjS57.put(Obj.TWRTPT,152);	ObjS57.put(Obj.UWTROC,153);	ObjS57.put(Obj.UNSARE,154); ObjS57.put(Obj.VEGATN,155);
		ObjS57.put(Obj.WATTUR,156);	ObjS57.put(Obj.WATFAL,157);	ObjS57.put(Obj.WEDKLP,158);	ObjS57.put(Obj.WRECKS,159); ObjS57.put(Obj.TS_FEB,160);
		ObjS57.put(Obj.MPAARE,199); ObjS57.put(Obj.M_COVR,302); ObjS57.put(Obj.M_NSYS,306); ObjS57.put(Obj.LITMAJ,74); ObjS57.put(Obj.LITMIN,90);
	}

	private static final EnumMap<Obj, Integer> ObjIENC = new EnumMap<>(Obj.class);
	static {
		ObjIENC.put(Obj.UNKOBJ, 0);
		ObjIENC.put(Obj.ACHBRT, 17000);	ObjIENC.put(Obj.ACHARE, 17001);	ObjIENC.put(Obj.DEPARE, 17003); ObjIENC.put(Obj.DISMAR, 17004); ObjIENC.put(Obj.RESARE, 17005);
		ObjIENC.put(Obj.SISTAT, 17007);	ObjIENC.put(Obj.SISTAW, 17008); ObjIENC.put(Obj.TOPMAR, 17009); ObjIENC.put(Obj.BERTHS, 17010);	ObjIENC.put(Obj.BRIDGE, 17011);
		ObjIENC.put(Obj.CBLOHD, 17012);	ObjIENC.put(Obj.FERYRT, 17013); ObjIENC.put(Obj.HRBARE, 17014); ObjIENC.put(Obj.HRBFAC, 17015);	ObjIENC.put(Obj.LOKBSN, 17016);
		ObjIENC.put(Obj.RDOCAL, 17017); ObjIENC.put(Obj.CURENT, 17019); ObjIENC.put(Obj.HULKES, 17020); ObjIENC.put(Obj.PONTON, 17021);	ObjIENC.put(Obj.PIPOHD, 17024);
		ObjIENC.put(Obj.FLODOC, 17025);	ObjIENC.put(Obj.CHKPNT, 17027); ObjIENC.put(Obj.BCNLAT, 17028); ObjIENC.put(Obj.BOYLAT, 17029);	ObjIENC.put(Obj.CRANES, 17030);
		ObjIENC.put(Obj.GATCON, 17031);	ObjIENC.put(Obj.SLCONS, 17032); ObjIENC.put(Obj.UWTROC, 17033); ObjIENC.put(Obj.CONVYR, 17034);	ObjIENC.put(Obj.NOTMRK, 17050);
		ObjIENC.put(Obj.WTWAXS, 17051);	ObjIENC.put(Obj.WTWPRF, 17052); ObjIENC.put(Obj.BUNSTA, 17054);	ObjIENC.put(Obj.COMARE, 17055); ObjIENC.put(Obj.HRBBSN, 17056);
		ObjIENC.put(Obj.LKBSPT, 17058); ObjIENC.put(Obj.PRTARE, 17059); ObjIENC.put(Obj.REFDMP, 17062); ObjIENC.put(Obj.TERMNL, 17064); ObjIENC.put(Obj.TRNBSN, 17065);
		ObjIENC.put(Obj.WTWARE, 17066); ObjIENC.put(Obj.WTWGAG, 17067); ObjIENC.put(Obj.TISDGE, 17068); ObjIENC.put(Obj.VEHTRF, 17069); ObjIENC.put(Obj.EXCNST, 17070);
		ObjIENC.put(Obj.LG_SDM, 18001);	ObjIENC.put(Obj.LG_VSP, 18002);
	}

	private static final EnumMap<Obj, String> ObjStr = new EnumMap<>(Obj.class);
	static {
		ObjStr.put(Obj.UNKOBJ, "");	ObjStr.put(Obj.AIRARE, "airfield");	ObjStr.put(Obj.ACHBRT, "anchor_berth"); ObjStr.put(Obj.ACHARE, "anchorage");
		ObjStr.put(Obj.BCNCAR, "beacon_cardinal");	ObjStr.put(Obj.BCNISD, "beacon_isolated_danger"); ObjStr.put(Obj.BCNLAT, "beacon_lateral");
		ObjStr.put(Obj.BCNSAW, "beacon_safe_water"); ObjStr.put(Obj.BCNSPP, "beacon_special_purpose"); ObjStr.put(Obj.BERTHS, "berth"); ObjStr.put(Obj.BRIDGE, "bridge");
		ObjStr.put(Obj.BUISGL, "building"); ObjStr.put(Obj.BUAARE, "built-up_area"); ObjStr.put(Obj.BOYCAR, "buoy_cardinal"); ObjStr.put(Obj.BOYINB, "buoy_installation");
		ObjStr.put(Obj.BOYISD, "buoy_isolated_danger"); ObjStr.put(Obj.BOYLAT, "buoy_lateral");	ObjStr.put(Obj.BOYSAW, "buoy_safe_water");
		ObjStr.put(Obj.BOYSPP, "buoy_special_purpose"); ObjStr.put(Obj.CBLARE, "cable_area");	ObjStr.put(Obj.CBLOHD, "cable_overhead");
		ObjStr.put(Obj.CBLSUB, "cable_submarine"); ObjStr.put(Obj.CANALS, "canal"); ObjStr.put(Obj.CTSARE, "cargo_area");	ObjStr.put(Obj.CAUSWY, "causeway");
		ObjStr.put(Obj.CTNARE, "caution_area"); ObjStr.put(Obj.CHKPNT, "checkpoint"); ObjStr.put(Obj.CGUSTA, "coastguard_station");	ObjStr.put(Obj.COALNE, "coastline");
		ObjStr.put(Obj.CONZNE, "contiguous_zone"); ObjStr.put(Obj.COSARE, "continental_shelf"); ObjStr.put(Obj.CTRPNT, "control_point");
		ObjStr.put(Obj.CONVYR, "conveyor");	ObjStr.put(Obj.CRANES, "crane"); ObjStr.put(Obj.CURENT, "current"); ObjStr.put(Obj.CUSZNE, "custom_zone");
		ObjStr.put(Obj.DAMCON, "dam"); ObjStr.put(Obj.DAYMAR, "daymark"); ObjStr.put(Obj.DWRTCL, "deep_water_route_centreline"); ObjStr.put(Obj.DWRTPT, "deep_water_route");
		ObjStr.put(Obj.DEPARE, "depth_area"); ObjStr.put(Obj.DEPCNT, "depth_contour"); ObjStr.put(Obj.DISMAR, "distance_mark"); ObjStr.put(Obj.DOCARE, "dock");
		ObjStr.put(Obj.DRGARE, "dredged_area"); ObjStr.put(Obj.DRYDOC, "dry_dock");	ObjStr.put(Obj.DMPGRD, "dumping_ground");	ObjStr.put(Obj.DYKCON, "dyke");
		ObjStr.put(Obj.EXEZNE, "exclusive_economic_zone"); ObjStr.put(Obj.FAIRWY, "fairway"); ObjStr.put(Obj.FNCLNE, "wall"); ObjStr.put(Obj.FERYRT, "ferry_route");
		ObjStr.put(Obj.FSHZNE, "fishery_zone"); ObjStr.put(Obj.FSHFAC, "fishing_facility");	ObjStr.put(Obj.FSHGRD, "fishing_ground");	ObjStr.put(Obj.FLODOC, "floating_dock");
		ObjStr.put(Obj.FOGSIG, "fog_signal"); ObjStr.put(Obj.FORSTC, "fortified_structure"); ObjStr.put(Obj.FRPARE, "free_port_area"); ObjStr.put(Obj.GATCON, "gate");
		ObjStr.put(Obj.GRIDRN, "gridiron"); ObjStr.put(Obj.HRBARE, "harbour_area");	ObjStr.put(Obj.HRBFAC, "harbour"); ObjStr.put(Obj.HULKES, "hulk");
		ObjStr.put(Obj.ICEARE, "ice_area"); ObjStr.put(Obj.ICNARE, "incineration_zone"); ObjStr.put(Obj.ISTZNE, "inshore_traffic_zone"); ObjStr.put(Obj.LAKARE, "lake");
		ObjStr.put(Obj.LNDARE, "land_area"); ObjStr.put(Obj.LNDELV, "land_elevation"); ObjStr.put(Obj.LNDRGN, "land_region");	ObjStr.put(Obj.LNDMRK, "landmark");
		ObjStr.put(Obj.LIGHTS, "light"); ObjStr.put(Obj.LITFLT, "light_float");	ObjStr.put(Obj.LITVES, "light_vessel");	ObjStr.put(Obj.LOCMAG, "local_magnetic_anomaly");
		ObjStr.put(Obj.LOKBSN, "lock_basin");	ObjStr.put(Obj.LOGPON, "log_pond");	ObjStr.put(Obj.MAGVAR, "magnetic_variation");	ObjStr.put(Obj.MARCUL, "marine_farm");
		ObjStr.put(Obj.MIPARE, "military_area"); ObjStr.put(Obj.MORFAC, "mooring");	ObjStr.put(Obj.NAVLNE, "navigation_line"); ObjStr.put(Obj.OBSTRN, "obstruction");
		ObjStr.put(Obj.OFSPLF, "platform");	ObjStr.put(Obj.OSPARE, "production_area"); ObjStr.put(Obj.OILBAR, "oil_barrier");	ObjStr.put(Obj.PILPNT, "pile");
		ObjStr.put(Obj.PILBOP, "pilot_boarding");	ObjStr.put(Obj.PIPARE, "pipeline_area"); ObjStr.put(Obj.PIPOHD, "pipeline_overhead");	ObjStr.put(Obj.PIPSOL, "pipeline_submarine");
		ObjStr.put(Obj.PONTON, "pontoon"); ObjStr.put(Obj.PRCARE, "precautionary_area"); ObjStr.put(Obj.PRDARE, "land_production_area");ObjStr.put(Obj.PYLONS, "pylon");
		ObjStr.put(Obj.RADLNE, "radar_line");	ObjStr.put(Obj.RADRNG, "radar_range"); ObjStr.put(Obj.RADRFL, "radar_reflector");	ObjStr.put(Obj.RADSTA, "radar_station");
		ObjStr.put(Obj.RTPBCN, "radar_transponder"); ObjStr.put(Obj.RDOCAL, "calling-in_point"); ObjStr.put(Obj.RDOSTA, "radio_station");	ObjStr.put(Obj.RAILWY, "railway");
		ObjStr.put(Obj.RAPIDS, "rapids");	ObjStr.put(Obj.RCRTCL, "recommended_route_centreline");	ObjStr.put(Obj.RECTRC, "recommended_track");
		ObjStr.put(Obj.RCTLPT, "recommended_traffic_lane");	ObjStr.put(Obj.RSCSTA, "rescue_station");	ObjStr.put(Obj.RESARE, "restricted_area");
		ObjStr.put(Obj.RETRFL, "retro_reflector"); ObjStr.put(Obj.RIVERS, "river");	ObjStr.put(Obj.ROADWY, "road"); ObjStr.put(Obj.RUNWAY, "runway");
		ObjStr.put(Obj.SNDWAV, "sand_waves");	ObjStr.put(Obj.SEAARE, "sea_area");	ObjStr.put(Obj.SPLARE, "seaplane_landing_area"); ObjStr.put(Obj.SBDARE, "seabed_area");
		ObjStr.put(Obj.SLCONS, "shoreline_construction"); ObjStr.put(Obj.SISTAT, "signal_station_traffic"); ObjStr.put(Obj.SISTAW, "signal_station_warning");
		ObjStr.put(Obj.SILTNK, "tank");	ObjStr.put(Obj.SLOTOP, "slope_topline"); ObjStr.put(Obj.SLOGRD, "sloping_ground"); ObjStr.put(Obj.SMCFAC, "small_craft_facility");
		ObjStr.put(Obj.SOUNDG, "sounding");	ObjStr.put(Obj.SPRING, "spring"); ObjStr.put(Obj.STSLNE, "territorial_baseline");	ObjStr.put(Obj.SUBTLN, "submarine_transit_lane");
		ObjStr.put(Obj.SWPARE, "swept_area"); ObjStr.put(Obj.TESARE, "territorial_area");	ObjStr.put(Obj.TIDEWY, "tideway"); ObjStr.put(Obj.TOPMAR, "topmark");
		ObjStr.put(Obj.TSELNE, "separation_line"); ObjStr.put(Obj.TSSBND, "separation_boundary");	ObjStr.put(Obj.TSSCRS, "separation_crossing");
		ObjStr.put(Obj.TSSLPT, "separation_lane"); ObjStr.put(Obj.TSSRON, "separation_roundabout");	ObjStr.put(Obj.TSEZNE, "separation_zone"); ObjStr.put(Obj.TUNNEL, "tunnel");
		ObjStr.put(Obj.TWRTPT, "two-way_route"); ObjStr.put(Obj.UWTROC, "rock"); ObjStr.put(Obj.UNSARE, "unsurveyed_area");	ObjStr.put(Obj.VEGATN, "vegetation");
		ObjStr.put(Obj.WATTUR, "water_turbulence");	ObjStr.put(Obj.WATFAL, "waterfall"); ObjStr.put(Obj.WEDKLP, "weed"); ObjStr.put(Obj.WRECKS, "wreck");
		ObjStr.put(Obj.TS_FEB, "tidal_stream");	ObjStr.put(Obj.NOTMRK, "notice");	ObjStr.put(Obj.WTWAXS, "waterway_axis"); ObjStr.put(Obj.WTWPRF, "waterway_profile");
		ObjStr.put(Obj.BUNSTA, "bunker_station");	ObjStr.put(Obj.COMARE, "communication_area");	ObjStr.put(Obj.HRBBSN, "harbour_basin"); ObjStr.put(Obj.LOKARE, "lock_area");
		ObjStr.put(Obj.LKBSPT, "lock_basin_part"); ObjStr.put(Obj.PRTARE, "port_area"); ObjStr.put(Obj.REFDMP, "refuse_dump");
		ObjStr.put(Obj.TERMNL, "terminal"); ObjStr.put(Obj.TRNBSN, "turning_basin"); ObjStr.put(Obj.WTWARE, "waterway_area"); ObjStr.put(Obj.WTWGAG, "waterway_gauge");
		ObjStr.put(Obj.TISDGE, "time_schedule"); ObjStr.put(Obj.VEHTRF, "vehicle_transfer"); ObjStr.put(Obj.EXCNST, "exceptional_structure"); ObjStr.put(Obj.MPAARE, "protected_area");
		ObjStr.put(Obj.LITMAJ, "light_major"); ObjStr.put(Obj.LITMIN, "light_minor"); ObjStr.put(Obj.M_COVR, "coverage"); ObjStr.put(Obj.M_NSYS, "system");
	}
	
	private static final HashMap<String, Obj> StrObj = new HashMap<>();
	static {
		for (Map.Entry<Obj, String> entry : ObjStr.entrySet()) {
			if (!entry.getValue().isEmpty())
				StrObj.put(entry.getValue(), entry.getKey());
		}
	}
	
	public static Obj decodeType(long objl) { // Convert S57 feature code to SCM object enumeration
		for (Obj obj : ObjS57.keySet()) {
			if (ObjS57.get(obj) == objl) return obj;
		}
		for (Obj obj : ObjIENC.keySet()) {
			if (ObjIENC.get(obj) == objl) return obj;
		}
		return Obj.UNKOBJ;
	}

	public static long encodeType(Obj type) { // Convert SCM object enumeration to S57 feature code
		if (ObjS57.containsKey(type))
			return ObjS57.get(type);
		else if (ObjIENC.containsKey(type))
			return ObjIENC.get(type);
		return 0;
	}

	public static String stringType(Obj type) { // Convert SCM object enumeration to OSM object string
		String str = ObjStr.get(type);
			return str != null ? str : "";
	}

	public static Obj enumType(String type) { // Convert OSM object string to SCM object enumeration
		if ((type != null) && !type.isEmpty() && (StrObj.containsKey(type)))
			return StrObj.get(type);
		else
			return Obj.UNKOBJ;
	}
	
}
