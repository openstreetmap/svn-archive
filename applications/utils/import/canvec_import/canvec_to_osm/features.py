# canvec-to-osm-features.py
# version 0.1

# Explanation
# - Code refers to the Canvec code (theme + numeric identifier)
# - Geom refers to the geometry type. Some feature types have multiple geometries. Value is a string, which can contain
# - the following values:
#   - 0: point
#   - 1: line
#   - 2: area
# - Class is the feature type name
# - Glom_key is the key by which shp-to-osm connects features together into one bigger feature

# TODO: add "extra", boolean value, indicating what should go to extra. Currently commented out.

# Remarks:
# - Missing underscore: Coastal water

feature_list = [
# Theme: Buildings and structures
{ "code": "BS_201001",  "geom": "0",   "class": "Building_unknown" },
{ "code": "BS_201001",  "geom": "2",   "class": "Building_unknown" },
{ "code": "BS_1250009", "geom": "0",   "class": "Navigational_aid" },
{ "code": "BS_1370009", "geom": "2",   "class": "Residential_area" },
{ "code": "BS_2000009", "geom": "0",   "class": "Parabolic_antenna" },
{ "code": "BS_2010009", "geom": "02",  "class": "Building" },
{ "code": "BS_2060009", "geom": "0",   "class": "Chimney" },
{ "code": "BS_2080009", "geom": "02",  "class": "Tank" },
{ "code": "BS_2120009", "geom": "0",   "class": "Cross" },
{ "code": "BS_2230009", "geom": "1",   "class": "Transmission_line" },
{ "code": "BS_2240009", "geom": "1",   "class": "Wall_Fence" },
{ "code": "BS_2310009", "geom": "1",   "class": "Pipeline_Sewage_liquid_waste" },
{ "code": "BS_2350009", "geom": "0",   "class": "Well" },
{ "code": "BS_2380009", "geom": "02",  "class": "Underground_reservoir" },
{ "code": "BS_2440009", "geom": "0",   "class": "Silo" },
{ "code": "BS_2530009", "geom": "0",   "class": "Tower" },
# Theme: Energy
{ "code": "EN_1120009", "geom": "1",   "class": "Power_transmission_line" },
{ "code": "EN_1180009", "geom": "1",   "class": "Pipeline" },
{ "code": "EN_1340009", "geom": "0",   "class": "Valve" },
{ "code": "EN_1360049", "geom": "02",  "class": "Gas_and_oil_facilities" },
{ "code": "EN_1360059", "geom": "02",  "class": "Transformer_station" },
{ "code": "EN_2170009", "geom": "0",   "class": "Wind-operated_device" },
# Theme: Relief and landforms
#{ "code": "FO_1030009", "geom": "1",   "class": "Contour" },
{ "code": "FO_1080019", "geom": "2",   "class": "Landform" },
{ "code": "FO_1080029", "geom": "1",   "class": "Esker" },
{ "code": "FO_1080039", "geom": "2",   "class": "Glacial_debris_undifferentiated" },
{ "code": "FO_1080049", "geom": "2",   "class": "Moraine" },
{ "code": "FO_1080059", "geom": "2",   "class": "Sand" },
{ "code": "FO_1080069", "geom": "2",   "class": "Tundra_polygon" },
{ "code": "FO_1080079", "geom": "0",   "class": "Pingo" },
{ "code": "FO_1200009", "geom": "0",   "class": "Elevation_point" },
#{ "code": "FO_2570009", "geom": "1",   "class": "Contour_imperial" },
#{ "code": "FO_2610009", "geom": "0",   "class": "Elevation_point_imperial" },
# Theme: Hydrography
{ "code": "HD_1140009", "geom": "2",   "class": "Permanent_snow_and_ice" },
{ "code": "HD_1150009", "geom": "2",   "class": "Coastal water" },
{ "code": "HD_1450009", "geom": "012", "class": "Manmade_hydrographic_entity" },
{ "code": "HD_1460009", "geom": "012", "class": "Hydrographic_obstacle_entity" },
#{ "code": "HD_1470009", "geom": "1",   "class": "Single_line_watercourse_FRENCH_",   "glom_key": "waterway" },
#{ "code": "HD_1470009", "geom": "1",   "class": "Single_line_watercourse",           "glom_key": "waterway" },
#{ "code": "HD_1480009", "geom": "2",   "class": "Waterbody_FRENCH_" },
#{ "code": "HD_1480009", "geom": "2",   "class": "Waterbody_outer" },
#{ "code": "HD_1480009", "geom": "2",   "class": "Waterbody" },
#{ "code": "HD_1490009", "geom": "2",   "class": "Island" },
# Theme: Industrial and commercial areas
{ "code": "IC_1350019", "geom": "2",   "class": "Pit" },
{ "code": "IC_1350029", "geom": "2",   "class": "Quarry" },
{ "code": "IC_1350039", "geom": "02",  "class": "Extraction_area" },
{ "code": "IC_1350049", "geom": "02",  "class": "Mine" },
{ "code": "IC_1350059", "geom": "2",   "class": "Peat_cutting" },
{ "code": "IC_1360019", "geom": "2",   "class": "Domestic_waste" },
{ "code": "IC_1360029", "geom": "02",  "class": "Industrial_solid_depot" },
{ "code": "IC_1360039", "geom": "02",  "class": "Industrial_and_commercial_area" },
{ "code": "IC_2110009", "geom": "2",   "class": "Lumber_yard" },
{ "code": "IC_2360009", "geom": "2",   "class": "Auto_wrecker" },
{ "code": "IC_2600009", "geom": "0",   "class": "Mining_area" },
# Theme: Administrative boundaries
{ "code": "LI_1210009", "geom": "2",   "class": "NTS50K_boundary_polygon" },
# Theme: Places of interest
{ "code": "LX_1000019", "geom": "02",  "class": "Lookout",                           "glom_key": "tourism" },
{ "code": "LX_1000029", "geom": "0",   "class": "Ski_centre",                        "glom_key": "leisure" },
{ "code": "LX_1000039", "geom": "02",  "class": "Cemetery",                          "glom_key": "landuse" },
{ "code": "LX_1000049", "geom": "2",   "class": "Fort",                              "glom_key": "historic" },
{ "code": "LX_1000059", "geom": "012", "class": "Designated_area",                   "glom_key": "landuse" },
{ "code": "LX_1000069", "geom": "0",   "class": "Marina",                            "glom_key": "leisure" },
{ "code": "LX_1000079", "geom": "12",  "class": "Sports_track_Race_track" },
{ "code": "LX_1000089", "geom": "2",   "class": "Golf_course" },
{ "code": "LX_2030009", "geom": "0",   "class": "Camp" },
{ "code": "LX_2070009", "geom": "02",  "class": "Drive-in_theatre" },
{ "code": "LX_2200009", "geom": "2",   "class": "Botanical_garden" },
{ "code": "LX_2210009", "geom": "0",   "class": "Shrine" },
{ "code": "LX_2220009", "geom": "0",   "class": "Historic_site_Point_of_interest" },
{ "code": "LX_2260009", "geom": "2",   "class": "Amusement_park" },
{ "code": "LX_2270009", "geom": "2",   "class": "Park_Sports_field" },
{ "code": "LX_2280009", "geom": "1",   "class": "Footbridge" },
{ "code": "LX_2400009", "geom": "02",  "class": "Ruins" },
{ "code": "LX_2420009", "geom": "1",   "class": "Trail" },
{ "code": "LX_2460009", "geom": "2",   "class": "Stadium" },
{ "code": "LX_2480009", "geom": "2",   "class": "Campground" },
{ "code": "LX_2490009", "geom": "02",  "class": "Picnic_site" },
{ "code": "LX_2500009", "geom": "02",  "class": "Golf_driving_range" },
{ "code": "LX_2510009", "geom": "2",   "class": "Exhibition_ground" },
{ "code": "LX_2560009", "geom": "2",   "class": "Zoo" },
# Theme: Water saturated soils
{ "code": "SS_1320019", "geom": "2",   "class": "Tundra_pond" },
{ "code": "SS_1320029", "geom": "2",   "class": "Palsa_bog" },
{ "code": "SS_1320039", "geom": "2",   "class": "Saturated_soil" },
{ "code": "SS_1320049", "geom": "2",   "class": "Wetland" },
{ "code": "SS_1320059", "geom": "2",   "class": "String_bog" },
# Theme: Toponymy
{ "code": "TO_1580009", "geom": "012", "class": "Named_feature" },
# Theme: Transportation
{ "code": "TR_1020009", "geom": "1",   "class": "Railway",                           "glom_key": "railway" },
{ "code": "TR_1190009", "geom": "02",  "class": "Runway" },
{ "code": "TR_1750009", "geom": "1",   "class": "Ferry_connection_segment" },
#{ "code": "TR_1760009", "geom": "1",   "class": "Road_segment",                      "glom_key": "highway" },
#{ "code": "TR_1770009", "geom": "0",   "class": "Junction" },
{ "code": "TR_1780009", "geom": "0",   "class": "Blocked_passage" },
{ "code": "TR_1790009", "geom": "0",   "class": "Toll_point" },
{ "code": "TR_2320009", "geom": "0",   "class": "Turntable" },
# Theme: Vegetation
{ "code": "VE_1240009", "geom": "2",   "class": "Wooded_area" },
{ "code": "VE_2290009", "geom": "1",   "class": "Cut_line" },
#{ "code": "", "geom": "",   "class": "" },
]

