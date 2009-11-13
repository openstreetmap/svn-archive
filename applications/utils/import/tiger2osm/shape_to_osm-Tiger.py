#!/usr/bin/python
# Tiger road data to OSM conversion script
# Creates Karlsruhe-style address ways beside the main way
# based on the Massachusetts GIS script by christopher schmidt

#BUGS:
# On very tight curves, a loop may be generated in the address way.
# It would be nice if the ends of the address ways were not pulled back from dead ends

VERSION="0.3"
# Version 0.3 is optimized for the tiger road conversion

# Tag Source  = iSource + _import_v + version + _ + date and time
iSource="tiger"
# Tag Attribution = iAttrib
iAttrib="tiger"

# Ways that include these mtfccs should not be uploaded
# H1100 Connector
# H3010 Stream/River
# H3013 Braided Stream
# H3020 Canal, Ditch or Aqueduct
# L4130 Point-to-Point Line
# L4140 Property/Parcel Line (Including PLSS)
# P0001 Nonvisible Linear Legal/Statistical Boundary
# P0002 Perennial Shoreline
# P0003 Intermittent Shoreline
# P0004 Other non-visible bounding Edge (e.g., Census water boundary, boundary of an areal feature)


ignoremtfcc = [ "H1100", "H3010", "H3013", "H3020", "L4130", "L4140", "P0001", "P0002", "P0003", "P0004" ]

#Files will be split when longer than this number of nodes
maxNodes = 300000

# Set the maximum length of a way (in nodes) before it is split into
# shorter ways
Max_Waylength = 500

# Sets the distance that the address ways should be from the main way, in feet.
address_distance = 30

# Sets the distance that the ends of the address ways should be pulled back from the ends of the main way, in feet
address_pullback = 45

try:
    from osgeo import ogr
    from osgeo import osr
except:
    import ogr
    import osr

# ====================================
# Edit parse_shp_for_osm section to fit your data!
# change poFeature.GetField("    ") to contain only the shape column names for the data you want
# and  tags["   "] to match the osm tag names you wish to use for that data.
# some tags will require changing a number to a meaningful value like the Highway tag.  See the metadata for the meaning of these tags.
# For any measurements be sure to check the unit value of the original data, and convert if needed to the expected unit for osm.
# ====================================

# Long name, short name, ISO-3166-1 alpha-2
# from http://www.census.gov/geo/www/ansi/statetables.html
fipscodes = {
    '01' : ('Alabama', 'AL', 'US'),
    '02' : ('Alaska', 'AK', 'US'),
    '04' : ('Arizona', 'AZ', 'US'),
    '05' : ('Arkansas', 'AR', 'US'),
    '06' : ('California', 'CA', 'US'),
    '08' : ('Colorado', 'CO', 'US'),
    '09' : ('Connecticut', 'CT', 'US'),
    '10' : ('Delaware', 'DE', 'US'),
    '11' : ('District of Columbia', 'DC', 'US'),
    '12' : ('Florida', 'FL', 'US'),
    '13' : ('Georgia', 'GA', 'US'),
    '15' : ('Hawaii', 'HI', 'US'),
    '16' : ('Idaho', 'ID', 'US'),
    '17' : ('Illinois', 'IL', 'US'),
    '18' : ('Indiana', 'IN', 'US'),
    '19' : ('Iowa', 'IA', 'US'),
    '20' : ('Kansas', 'KS', 'US'),
    '21' : ('Kentucky', 'KY', 'US'),
    '22' : ('Louisiana', 'LA', 'US'),
    '23' : ('Maine', 'ME', 'US'),
    '24' : ('Maryland', 'MD', 'US'),
    '25' : ('Massachusetts', 'MA', 'US'),
    '26' : ('Michigan', 'MI', 'US'),
    '27' : ('Minnesota', 'MN', 'US'),
    '28' : ('Mississippi', 'MS', 'US'),
    '29' : ('Missouri', 'MO', 'US'),
    '30' : ('Montana', 'MT', 'US'),
    '31' : ('Nebraska', 'NE', 'US'),
    '32' : ('Nevada', 'NV', 'US'),
    '33' : ('New Hampshire', 'NH', 'US'),
    '34' : ('New Jersey', 'NJ', 'US'),
    '35' : ('New Mexico', 'NM', 'US'),
    '36' : ('New York', 'NY', 'US'),
    '37' : ('North Carolina', 'NC', 'US'),
    '38' : ('North Dakota', 'ND', 'US'),
    '39' : ('Ohio', 'OH', 'US'),
    '40' : ('Oklahoma', 'OK', 'US'),
    '41' : ('Oregon', 'OR', 'US'),
    '42' : ('Pennsylvania', 'PA', 'US'),
    '44' : ('Rhode Island', 'RI', 'US'),
    '45' : ('South Carolina', 'SC', 'US'),
    '46' : ('South Dakota', 'SD', 'US'),
    '47' : ('Tennessee', 'TN', 'US'),
    '48' : ('Texas', 'TX', 'US'),
    '49' : ('Utah', 'UT', 'US'),
    '50' : ('Vermont', 'VT', 'US'),
    '51' : ('Virginia', 'VA', 'US'),
    '53' : ('Washington', 'WA', 'US'),
    '54' : ('West Virginia', 'WV', 'US'),
    '55' : ('Wisconsin', 'WI', 'US'),
    '56' : ('Wyoming', 'WY', 'US'),
    # Outlying areas w/census data
    '60' : ('American Samoa', 'AS', 'AS'),
    '66' : ('Guam', 'GU', 'GU'),
    '69' : ('Commonwealth of the Northern Mariana Islands', 'MP', 'MP'),
    '72' : ('Puerto Rico', 'PR', 'PR'),
    '78' : ('U.S. Virgin Islands', 'VI', 'VI'),
    }

# This was generated with the following:
# wget http://www.census.gov/datamap/fipslist/AllSt.txt
# cat AllSt.txt  | grep '^                [0-9]' | awk "{printf \"'%s' : '%s' ,\\n\", \$1, substr(\$0, 31)}" | > countyfips.py

county_fips = {
'02000' : 'ALASKA' ,
'02013' : 'Aleutians East, AK' ,
'02016' : 'Aleutians West, AK' ,
'02050' : 'Bethel, AK' ,
'02060' : 'Bristol Bay, AK' ,
'02068' : 'Denali, AK' ,
'02070' : 'Dillingham, AK' ,
'02090' : 'Fairbanks North Star, AK' ,
'02100' : 'Haines, AK' ,
'02110' : 'Juneau, AK' ,
'02122' : 'Kenai Peninsula, AK' ,
'02130' : 'Ketchikan Gateway, AK' ,
'02150' : 'Kodiak Island, AK' ,
'02164' : 'Lake and Peninsula, AK' ,
'02170' : 'Matanuska-Susitna, AK' ,
'02180' : 'Nome, AK' ,
'02185' : 'North Slope, AK' ,
'02188' : 'Northwest Arctic, AK' ,
'02201' : 'Prince of Wales-Outer Ketchikan, AK' ,
'02220' : 'Sitka, AK' ,
'02232' : 'Skagway-Hoonah-Angoon, AK' ,
'02240' : 'Southeast Fairbanks, AK' ,
'02261' : 'Valdez-Cordova, AK' ,
'02270' : 'Wade Hampton, AK' ,
'02280' : 'Wrangell-Petersburg, AK' ,
'02282' : 'Yakutat, AK' ,
'02290' : 'Yukon-Koyukuk, AK' ,
'01000' : 'ALABAMA' ,
'01005' : 'Barbour, AL' ,
'01007' : 'Bibb, AL' ,
'01011' : 'Bullock, AL' ,
'01013' : 'Butler, AL' ,
'01017' : 'Chambers, AL' ,
'01019' : 'Cherokee, AL' ,
'01021' : 'Chilton, AL' ,
'01023' : 'Choctaw, AL' ,
'01025' : 'Clarke, AL' ,
'01027' : 'Clay, AL' ,
'01029' : 'Cleburne, AL' ,
'01031' : 'Coffee, AL' ,
'01035' : 'Conecuh, AL' ,
'01037' : 'Coosa, AL' ,
'01039' : 'Covington, AL' ,
'01041' : 'Crenshaw, AL' ,
'01043' : 'Cullman, AL' ,
'01047' : 'Dallas, AL' ,
'01049' : 'De Kalb, AL' ,
'01053' : 'Escambia, AL' ,
'01057' : 'Fayette, AL' ,
'01059' : 'Franklin, AL' ,
'01061' : 'Geneva, AL' ,
'01063' : 'Greene, AL' ,
'01065' : 'Hale, AL' ,
'01067' : 'Henry, AL' ,
'01071' : 'Jackson, AL' ,
'01075' : 'Lamar, AL' ,
'01081' : 'Lee, AL' ,
'01085' : 'Lowndes, AL' ,
'01087' : 'Macon, AL' ,
'01091' : 'Marengo, AL' ,
'01093' : 'Marion, AL' ,
'01095' : 'Marshall, AL' ,
'01099' : 'Monroe, AL' ,
'01105' : 'Perry, AL' ,
'01107' : 'Pickens, AL' ,
'01109' : 'Pike, AL' ,
'01111' : 'Randolph, AL' ,
'01119' : 'Sumter, AL' ,
'01121' : 'Talladega, AL' ,
'01123' : 'Tallapoosa, AL' ,
'01127' : 'Walker, AL' ,
'01129' : 'Washington, AL' ,
'01131' : 'Wilcox, AL' ,
'01133' : 'Winston, AL' ,
'04001' : 'Apache, AZ' ,
'04003' : 'Cochise, AZ' ,
'04005' : 'Coconino, AZ' ,
'04007' : 'Gila, AZ' ,
'04009' : 'Graham, AZ' ,
'04011' : 'Greenlee, AZ' ,
'04012' : 'La Paz, AZ' ,
'04017' : 'Navajo, AZ' ,
'04023' : 'Santa Cruz, AZ' ,
'04025' : 'Yavapai, AZ' ,
'05000' : 'ARKANSAS' ,
'05001' : 'Arkansas, AR' ,
'05003' : 'Ashley, AR' ,
'05005' : 'Baxter, AR' ,
'05009' : 'Boone, AR' ,
'05011' : 'Bradley, AR' ,
'05013' : 'Calhoun, AR' ,
'05015' : 'Carroll, AR' ,
'05017' : 'Chicot, AR' ,
'05019' : 'Clark, AR' ,
'05021' : 'Clay, AR' ,
'05023' : 'Cleburne, AR' ,
'05025' : 'Cleveland, AR' ,
'05027' : 'Columbia, AR' ,
'05029' : 'Conway, AR' ,
'05031' : 'Craighead, AR' ,
'05037' : 'Cross, AR' ,
'05039' : 'Dallas, AR' ,
'05041' : 'Desha, AR' ,
'05043' : 'Drew, AR' ,
'05047' : 'Franklin, AR' ,
'05049' : 'Fulton, AR' ,
'05051' : 'Garland, AR' ,
'05053' : 'Grant, AR' ,
'05055' : 'Greene, AR' ,
'05057' : 'Hempstead, AR' ,
'05059' : 'Hot Spring, AR' ,
'05061' : 'Howard, AR' ,
'05063' : 'Independence, AR' ,
'05065' : 'Izard, AR' ,
'05067' : 'Jackson, AR' ,
'05071' : 'Johnson, AR' ,
'05073' : 'Lafayette, AR' ,
'05075' : 'Lawrence, AR' ,
'05077' : 'Lee, AR' ,
'05079' : 'Lincoln, AR' ,
'05081' : 'Little River, AR' ,
'05083' : 'Logan, AR' ,
'05087' : 'Madison, AR' ,
'05089' : 'Marion, AR' ,
'05093' : 'Mississippi, AR' ,
'05095' : 'Monroe, AR' ,
'05097' : 'Montgomery, AR' ,
'05099' : 'Nevada, AR' ,
'05101' : 'Newton, AR' ,
'05103' : 'Ouachita, AR' ,
'05105' : 'Perry, AR' ,
'05107' : 'Phillips, AR' ,
'05109' : 'Pike, AR' ,
'05111' : 'Poinsett, AR' ,
'05113' : 'Polk, AR' ,
'05115' : 'Pope, AR' ,
'05117' : 'Prairie, AR' ,
'05121' : 'Randolph, AR' ,
'05123' : 'St. Francis, AR' ,
'05127' : 'Scott, AR' ,
'05129' : 'Searcy, AR' ,
'05133' : 'Sevier, AR' ,
'05135' : 'Sharp, AR' ,
'05137' : 'Stone, AR' ,
'05139' : 'Union, AR' ,
'05141' : 'Van Buren, AR' ,
'05145' : 'White, AR' ,
'05147' : 'Woodruff, AR' ,
'05149' : 'Yell, AR' ,
'06000' : 'CALIFORNIA' ,
'06003' : 'Alpine, CA' ,
'06005' : 'Amador, CA' ,
'06009' : 'Calaveras, CA' ,
'06011' : 'Colusa, CA' ,
'06015' : 'Del Norte, CA' ,
'06021' : 'Glenn, CA' ,
'06023' : 'Humboldt, CA' ,
'06025' : 'Imperial, CA' ,
'06027' : 'Inyo, CA' ,
'06031' : 'Kings, CA' ,
'06033' : 'Lake, CA' ,
'06035' : 'Lassen, CA' ,
'06043' : 'Mariposa, CA' ,
'06045' : 'Mendocino, CA' ,
'06049' : 'Modoc, CA' ,
'06051' : 'Mono, CA' ,
'06057' : 'Nevada, CA' ,
'06063' : 'Plumas, CA' ,
'06069' : 'San Benito, CA' ,
'06091' : 'Sierra, CA' ,
'06093' : 'Siskiyou, CA' ,
'06103' : 'Tehama, CA' ,
'06105' : 'Trinity, CA' ,
'06109' : 'Tuolumne, CA' ,
'08000' : 'COLORADO' ,
'08003' : 'Alamosa, CO' ,
'08007' : 'Archuleta, CO' ,
'08009' : 'Baca, CO' ,
'08011' : 'Bent, CO' ,
'08015' : 'Chaffee, CO' ,
'08017' : 'Cheyenne, CO' ,
'08019' : 'Clear Creek, CO' ,
'08021' : 'Conejos, CO' ,
'08023' : 'Costilla, CO' ,
'08025' : 'Crowley, CO' ,
'08027' : 'Custer, CO' ,
'08029' : 'Delta, CO' ,
'08033' : 'Dolores, CO' ,
'08037' : 'Eagle, CO' ,
'08039' : 'Elbert, CO' ,
'08043' : 'Fremont, CO' ,
'08045' : 'Garfield, CO' ,
'08047' : 'Gilpin, CO' ,
'08049' : 'Grand, CO' ,
'08051' : 'Gunnison, CO' ,
'08053' : 'Hinsdale, CO' ,
'08055' : 'Huerfano, CO' ,
'08057' : 'Jackson, CO' ,
'08061' : 'Kiowa, CO' ,
'08063' : 'Kit Carson, CO' ,
'08065' : 'Lake, CO' ,
'08067' : 'La Plata, CO' ,
'08071' : 'Las Animas, CO' ,
'08073' : 'Lincoln, CO' ,
'08075' : 'Logan, CO' ,
'08077' : 'Mesa, CO' ,
'08079' : 'Mineral, CO' ,
'08081' : 'Moffat, CO' ,
'08083' : 'Montezuma, CO' ,
'08085' : 'Montrose, CO' ,
'08087' : 'Morgan, CO' ,
'08089' : 'Otero, CO' ,
'08091' : 'Ouray, CO' ,
'08093' : 'Park, CO' ,
'08095' : 'Phillips, CO' ,
'08097' : 'Pitkin, CO' ,
'08099' : 'Prowers, CO' ,
'08103' : 'Rio Blanco, CO' ,
'08105' : 'Rio Grande, CO' ,
'08107' : 'Routt, CO' ,
'08109' : 'Saguache, CO' ,
'08111' : 'San Juan, CO' ,
'08113' : 'San Miguel, CO' ,
'08115' : 'Sedgwick, CO' ,
'08117' : 'Summit, CO' ,
'08119' : 'Teller, CO' ,
'08121' : 'Washington, CO' ,
'08125' : 'Yuma, CO' ,
'09000' : 'CONNECTICUT' ,
'09005' : 'Litchfield, CT' ,
'09015' : 'Windham, CT' ,
'11000' : 'DISTRICT OF COLUMBIA' ,
'10000' : 'DELAWARE' ,
'10005' : 'Sussex, DE' ,
'12000' : 'FLORIDA' ,
'12003' : 'Baker, FL' ,
'12007' : 'Bradford, FL' ,
'12013' : 'Calhoun, FL' ,
'12017' : 'Citrus, FL' ,
'12023' : 'Columbia, FL' ,
'12027' : 'De Soto, FL' ,
'12029' : 'Dixie, FL' ,
'12037' : 'Franklin, FL' ,
'12041' : 'Gilchrist, FL' ,
'12043' : 'Glades, FL' ,
'12045' : 'Gulf, FL' ,
'12047' : 'Hamilton, FL' ,
'12049' : 'Hardee, FL' ,
'12051' : 'Hendry, FL' ,
'12055' : 'Highlands, FL' ,
'12059' : 'Holmes, FL' ,
'12061' : 'Indian River, FL' ,
'12063' : 'Jackson, FL' ,
'12065' : 'Jefferson, FL' ,
'12067' : 'Lafayette, FL' ,
'12075' : 'Levy, FL' ,
'12077' : 'Liberty, FL' ,
'12079' : 'Madison, FL' ,
'12087' : 'Monroe, FL' ,
'12093' : 'Okeechobee, FL' ,
'12107' : 'Putnam, FL' ,
'12119' : 'Sumter, FL' ,
'12121' : 'Suwannee, FL' ,
'12123' : 'Taylor, FL' ,
'12125' : 'Union, FL' ,
'12129' : 'Wakulla, FL' ,
'12131' : 'Walton, FL' ,
'12133' : 'Washington, FL' ,
'13000' : 'GEORGIA' ,
'13001' : 'Appling, GA' ,
'13003' : 'Atkinson, GA' ,
'13005' : 'Bacon, GA' ,
'13007' : 'Baker, GA' ,
'13009' : 'Baldwin, GA' ,
'13011' : 'Banks, GA' ,
'13017' : 'Ben Hill, GA' ,
'13019' : 'Berrien, GA' ,
'13023' : 'Bleckley, GA' ,
'13025' : 'Brantley, GA' ,
'13027' : 'Brooks, GA' ,
'13031' : 'Bulloch, GA' ,
'13033' : 'Burke, GA' ,
'13035' : 'Butts, GA' ,
'13037' : 'Calhoun, GA' ,
'13039' : 'Camden, GA' ,
'13043' : 'Candler, GA' ,
'13049' : 'Charlton, GA' ,
'13055' : 'Chattooga, GA' ,
'13061' : 'Clay, GA' ,
'13065' : 'Clinch, GA' ,
'13069' : 'Coffee, GA' ,
'13071' : 'Colquitt, GA' ,
'13075' : 'Cook, GA' ,
'13079' : 'Crawford, GA' ,
'13081' : 'Crisp, GA' ,
'13085' : 'Dawson, GA' ,
'13087' : 'Decatur, GA' ,
'13091' : 'Dodge, GA' ,
'13093' : 'Dooly, GA' ,
'13099' : 'Early, GA' ,
'13101' : 'Echols, GA' ,
'13105' : 'Elbert, GA' ,
'13107' : 'Emanuel, GA' ,
'13109' : 'Evans, GA' ,
'13111' : 'Fannin, GA' ,
'13115' : 'Floyd, GA' ,
'13119' : 'Franklin, GA' ,
'13123' : 'Gilmer, GA' ,
'13125' : 'Glascock, GA' ,
'13127' : 'Glynn, GA' ,
'13129' : 'Gordon, GA' ,
'13131' : 'Grady, GA' ,
'13133' : 'Greene, GA' ,
'13137' : 'Habersham, GA' ,
'13139' : 'Hall, GA' ,
'13141' : 'Hancock, GA' ,
'13143' : 'Haralson, GA' ,
'13147' : 'Hart, GA' ,
'13149' : 'Heard, GA' ,
'13155' : 'Irwin, GA' ,
'13157' : 'Jackson, GA' ,
'13159' : 'Jasper, GA' ,
'13161' : 'Jeff Davis, GA' ,
'13163' : 'Jefferson, GA' ,
'13165' : 'Jenkins, GA' ,
'13167' : 'Johnson, GA' ,
'13171' : 'Lamar, GA' ,
'13173' : 'Lanier, GA' ,
'13175' : 'Laurens, GA' ,
'13179' : 'Liberty, GA' ,
'13181' : 'Lincoln, GA' ,
'13183' : 'Long, GA' ,
'13185' : 'Lowndes, GA' ,
'13187' : 'Lumpkin, GA' ,
'13189' : 'McDuffie, GA' ,
'13191' : 'McIntosh, GA' ,
'13193' : 'Macon, GA' ,
'13197' : 'Marion, GA' ,
'13199' : 'Meriwether, GA' ,
'13201' : 'Miller, GA' ,
'13205' : 'Mitchell, GA' ,
'13207' : 'Monroe, GA' ,
'13209' : 'Montgomery, GA' ,
'13211' : 'Morgan, GA' ,
'13213' : 'Murray, GA' ,
'13221' : 'Oglethorpe, GA' ,
'13229' : 'Pierce, GA' ,
'13231' : 'Pike, GA' ,
'13233' : 'Polk, GA' ,
'13235' : 'Pulaski, GA' ,
'13237' : 'Putnam, GA' ,
'13239' : 'Quitman, GA' ,
'13241' : 'Rabun, GA' ,
'13243' : 'Randolph, GA' ,
'13249' : 'Schley, GA' ,
'13251' : 'Screven, GA' ,
'13253' : 'Seminole, GA' ,
'13257' : 'Stephens, GA' ,
'13259' : 'Stewart, GA' ,
'13261' : 'Sumter, GA' ,
'13263' : 'Talbot, GA' ,
'13265' : 'Taliaferro, GA' ,
'13267' : 'Tattnall, GA' ,
'13269' : 'Taylor, GA' ,
'13271' : 'Telfair, GA' ,
'13273' : 'Terrell, GA' ,
'13275' : 'Thomas, GA' ,
'13277' : 'Tift, GA' ,
'13279' : 'Toombs, GA' ,
'13281' : 'Towns, GA' ,
'13283' : 'Treutlen, GA' ,
'13285' : 'Troup, GA' ,
'13287' : 'Turner, GA' ,
'13291' : 'Union, GA' ,
'13293' : 'Upson, GA' ,
'13299' : 'Ware, GA' ,
'13301' : 'Warren, GA' ,
'13303' : 'Washington, GA' ,
'13305' : 'Wayne, GA' ,
'13307' : 'Webster, GA' ,
'13309' : 'Wheeler, GA' ,
'13311' : 'White, GA' ,
'13313' : 'Whitfield, GA' ,
'13315' : 'Wilcox, GA' ,
'13317' : 'Wilkes, GA' ,
'13319' : 'Wilkinson, GA' ,
'13321' : 'Worth, GA' ,
'15000' : 'HAWAII' ,
'15001' : 'Hawaii, HI' ,
'15005' : 'Kalawao, HI' ,
'15007' : 'Kauai, HI' ,
'15009' : 'Maui, HI' ,
'19000' : 'IOWA' ,
'19001' : 'Adair, IA' ,
'19003' : 'Adams, IA' ,
'19005' : 'Allamakee, IA' ,
'19007' : 'Appanoose, IA' ,
'19009' : 'Audubon, IA' ,
'19011' : 'Benton, IA' ,
'19015' : 'Boone, IA' ,
'19017' : 'Bremer, IA' ,
'19019' : 'Buchanan, IA' ,
'19021' : 'Buena Vista, IA' ,
'19023' : 'Butler, IA' ,
'19025' : 'Calhoun, IA' ,
'19027' : 'Carroll, IA' ,
'19029' : 'Cass, IA' ,
'19031' : 'Cedar, IA' ,
'19033' : 'Cerro Gordo, IA' ,
'19035' : 'Cherokee, IA' ,
'19037' : 'Chickasaw, IA' ,
'19039' : 'Clarke, IA' ,
'19041' : 'Clay, IA' ,
'19043' : 'Clayton, IA' ,
'19045' : 'Clinton, IA' ,
'19047' : 'Crawford, IA' ,
'19051' : 'Davis, IA' ,
'19053' : 'Decatur, IA' ,
'19055' : 'Delaware, IA' ,
'19057' : 'Des Moines, IA' ,
'19059' : 'Dickinson, IA' ,
'19063' : 'Emmet, IA' ,
'19065' : 'Fayette, IA' ,
'19067' : 'Floyd, IA' ,
'19069' : 'Franklin, IA' ,
'19071' : 'Fremont, IA' ,
'19073' : 'Greene, IA' ,
'19075' : 'Grundy, IA' ,
'19077' : 'Guthrie, IA' ,
'19079' : 'Hamilton, IA' ,
'19081' : 'Hancock, IA' ,
'19083' : 'Hardin, IA' ,
'19085' : 'Harrison, IA' ,
'19087' : 'Henry, IA' ,
'19089' : 'Howard, IA' ,
'19091' : 'Humboldt, IA' ,
'19093' : 'Ida, IA' ,
'19095' : 'Iowa, IA' ,
'19097' : 'Jackson, IA' ,
'19099' : 'Jasper, IA' ,
'19101' : 'Jefferson, IA' ,
'19105' : 'Jones, IA' ,
'19107' : 'Keokuk, IA' ,
'19109' : 'Kossuth, IA' ,
'19111' : 'Lee, IA' ,
'19115' : 'Louisa, IA' ,
'19117' : 'Lucas, IA' ,
'19119' : 'Lyon, IA' ,
'19121' : 'Madison, IA' ,
'19123' : 'Mahaska, IA' ,
'19125' : 'Marion, IA' ,
'19127' : 'Marshall, IA' ,
'19129' : 'Mills, IA' ,
'19131' : 'Mitchell, IA' ,
'19133' : 'Monona, IA' ,
'19135' : 'Monroe, IA' ,
'19137' : 'Montgomery, IA' ,
'19139' : 'Muscatine, IA' ,
'19141' : 'O\'Brien, IA' ,
'19143' : 'Osceola, IA' ,
'19145' : 'Page, IA' ,
'19147' : 'Palo Alto, IA' ,
'19149' : 'Plymouth, IA' ,
'19151' : 'Pocahontas, IA' ,
'19157' : 'Poweshiek, IA' ,
'19159' : 'Ringgold, IA' ,
'19161' : 'Sac, IA' ,
'19165' : 'Shelby, IA' ,
'19167' : 'Sioux, IA' ,
'19169' : 'Story, IA' ,
'19171' : 'Tama, IA' ,
'19173' : 'Taylor, IA' ,
'19175' : 'Union, IA' ,
'19177' : 'Van Buren, IA' ,
'19179' : 'Wapello, IA' ,
'19183' : 'Washington, IA' ,
'19185' : 'Wayne, IA' ,
'19187' : 'Webster, IA' ,
'19189' : 'Winnebago, IA' ,
'19191' : 'Winneshiek, IA' ,
'19195' : 'Worth, IA' ,
'19197' : 'Wright, IA' ,
'16000' : 'IDAHO' ,
'16003' : 'Adams, ID' ,
'16005' : 'Bannock, ID' ,
'16007' : 'Bear Lake, ID' ,
'16009' : 'Benewah, ID' ,
'16011' : 'Bingham, ID' ,
'16013' : 'Blaine, ID' ,
'16015' : 'Boise, ID' ,
'16017' : 'Bonner, ID' ,
'16019' : 'Bonneville, ID' ,
'16021' : 'Boundary, ID' ,
'16023' : 'Butte, ID' ,
'16025' : 'Camas, ID' ,
'16029' : 'Caribou, ID' ,
'16031' : 'Cassia, ID' ,
'16033' : 'Clark, ID' ,
'16035' : 'Clearwater, ID' ,
'16037' : 'Custer, ID' ,
'16039' : 'Elmore, ID' ,
'16041' : 'Franklin, ID' ,
'16043' : 'Fremont, ID' ,
'16045' : 'Gem, ID' ,
'16047' : 'Gooding, ID' ,
'16049' : 'Idaho, ID' ,
'16051' : 'Jefferson, ID' ,
'16053' : 'Jerome, ID' ,
'16055' : 'Kootenai, ID' ,
'16057' : 'Latah, ID' ,
'16059' : 'Lemhi, ID' ,
'16061' : 'Lewis, ID' ,
'16063' : 'Lincoln, ID' ,
'16065' : 'Madison, ID' ,
'16067' : 'Minidoka, ID' ,
'16069' : 'Nez Perce, ID' ,
'16071' : 'Oneida, ID' ,
'16073' : 'Owyhee, ID' ,
'16075' : 'Payette, ID' ,
'16077' : 'Power, ID' ,
'16079' : 'Shoshone, ID' ,
'16081' : 'Teton, ID' ,
'16083' : 'Twin Falls, ID' ,
'16085' : 'Valley, ID' ,
'16087' : 'Washington, ID' ,
'17000' : 'ILLINOIS' ,
'17001' : 'Adams, IL' ,
'17003' : 'Alexander, IL' ,
'17005' : 'Bond, IL' ,
'17009' : 'Brown, IL' ,
'17011' : 'Bureau, IL' ,
'17013' : 'Calhoun, IL' ,
'17015' : 'Carroll, IL' ,
'17017' : 'Cass, IL' ,
'17021' : 'Christian, IL' ,
'17023' : 'Clark, IL' ,
'17025' : 'Clay, IL' ,
'17029' : 'Coles, IL' ,
'17033' : 'Crawford, IL' ,
'17035' : 'Cumberland, IL' ,
'17039' : 'De Witt, IL' ,
'17041' : 'Douglas, IL' ,
'17045' : 'Edgar, IL' ,
'17047' : 'Edwards, IL' ,
'17049' : 'Effingham, IL' ,
'17051' : 'Fayette, IL' ,
'17053' : 'Ford, IL' ,
'17055' : 'Franklin, IL' ,
'17057' : 'Fulton, IL' ,
'17059' : 'Gallatin, IL' ,
'17061' : 'Greene, IL' ,
'17065' : 'Hamilton, IL' ,
'17067' : 'Hancock, IL' ,
'17069' : 'Hardin, IL' ,
'17071' : 'Henderson, IL' ,
'17075' : 'Iroquois, IL' ,
'17077' : 'Jackson, IL' ,
'17079' : 'Jasper, IL' ,
'17081' : 'Jefferson, IL' ,
'17085' : 'Jo Daviess, IL' ,
'17087' : 'Johnson, IL' ,
'17095' : 'Knox, IL' ,
'17099' : 'La Salle, IL' ,
'17101' : 'Lawrence, IL' ,
'17103' : 'Lee, IL' ,
'17105' : 'Livingston, IL' ,
'17107' : 'Logan, IL' ,
'17109' : 'McDonough, IL' ,
'17117' : 'Macoupin, IL' ,
'17121' : 'Marion, IL' ,
'17123' : 'Marshall, IL' ,
'17125' : 'Mason, IL' ,
'17127' : 'Massac, IL' ,
'17131' : 'Mercer, IL' ,
'17135' : 'Montgomery, IL' ,
'17137' : 'Morgan, IL' ,
'17139' : 'Moultrie, IL' ,
'17145' : 'Perry, IL' ,
'17147' : 'Piatt, IL' ,
'17149' : 'Pike, IL' ,
'17151' : 'Pope, IL' ,
'17153' : 'Pulaski, IL' ,
'17155' : 'Putnam, IL' ,
'17157' : 'Randolph, IL' ,
'17159' : 'Richland, IL' ,
'17165' : 'Saline, IL' ,
'17169' : 'Schuyler, IL' ,
'17171' : 'Scott, IL' ,
'17173' : 'Shelby, IL' ,
'17175' : 'Stark, IL' ,
'17177' : 'Stephenson, IL' ,
'17181' : 'Union, IL' ,
'17183' : 'Vermilion, IL' ,
'17185' : 'Wabash, IL' ,
'17187' : 'Warren, IL' ,
'17189' : 'Washington, IL' ,
'17191' : 'Wayne, IL' ,
'17193' : 'White, IL' ,
'17195' : 'Whiteside, IL' ,
'17199' : 'Williamson, IL' ,
'18000' : 'INDIANA' ,
'18005' : 'Bartholomew, IN' ,
'18007' : 'Benton, IN' ,
'18009' : 'Blackford, IN' ,
'18013' : 'Brown, IN' ,
'18015' : 'Carroll, IN' ,
'18017' : 'Cass, IN' ,
'18025' : 'Crawford, IN' ,
'18027' : 'Daviess, IN' ,
'18031' : 'Decatur, IN' ,
'18037' : 'Dubois, IN' ,
'18041' : 'Fayette, IN' ,
'18045' : 'Fountain, IN' ,
'18047' : 'Franklin, IN' ,
'18049' : 'Fulton, IN' ,
'18051' : 'Gibson, IN' ,
'18053' : 'Grant, IN' ,
'18055' : 'Greene, IN' ,
'18065' : 'Henry, IN' ,
'18071' : 'Jackson, IN' ,
'18073' : 'Jasper, IN' ,
'18075' : 'Jay, IN' ,
'18077' : 'Jefferson, IN' ,
'18079' : 'Jennings, IN' ,
'18083' : 'Knox, IN' ,
'18085' : 'Kosciusko, IN' ,
'18087' : 'Lagrange, IN' ,
'18091' : 'La Porte, IN' ,
'18093' : 'Lawrence, IN' ,
'18099' : 'Marshall, IN' ,
'18101' : 'Martin, IN' ,
'18103' : 'Miami, IN' ,
'18107' : 'Montgomery, IN' ,
'18111' : 'Newton, IN' ,
'18113' : 'Noble, IN' ,
'18117' : 'Orange, IN' ,
'18119' : 'Owen, IN' ,
'18121' : 'Parke, IN' ,
'18123' : 'Perry, IN' ,
'18125' : 'Pike, IN' ,
'18131' : 'Pulaski, IN' ,
'18133' : 'Putnam, IN' ,
'18135' : 'Randolph, IN' ,
'18137' : 'Ripley, IN' ,
'18139' : 'Rush, IN' ,
'18147' : 'Spencer, IN' ,
'18149' : 'Starke, IN' ,
'18151' : 'Steuben, IN' ,
'18153' : 'Sullivan, IN' ,
'18155' : 'Switzerland, IN' ,
'18161' : 'Union, IN' ,
'18169' : 'Wabash, IN' ,
'18171' : 'Warren, IN' ,
'18175' : 'Washington, IN' ,
'18177' : 'Wayne, IN' ,
'18181' : 'White, IN' ,
'20000' : 'KANSAS' ,
'20001' : 'Allen, KS' ,
'20003' : 'Anderson, KS' ,
'20005' : 'Atchison, KS' ,
'20007' : 'Barber, KS' ,
'20009' : 'Barton, KS' ,
'20011' : 'Bourbon, KS' ,
'20013' : 'Brown, KS' ,
'20017' : 'Chase, KS' ,
'20019' : 'Chautauqua, KS' ,
'20021' : 'Cherokee, KS' ,
'20023' : 'Cheyenne, KS' ,
'20025' : 'Clark, KS' ,
'20027' : 'Clay, KS' ,
'20029' : 'Cloud, KS' ,
'20031' : 'Coffey, KS' ,
'20033' : 'Comanche, KS' ,
'20035' : 'Cowley, KS' ,
'20037' : 'Crawford, KS' ,
'20039' : 'Decatur, KS' ,
'20041' : 'Dickinson, KS' ,
'20043' : 'Doniphan, KS' ,
'20047' : 'Edwards, KS' ,
'20049' : 'Elk, KS' ,
'20051' : 'Ellis, KS' ,
'20053' : 'Ellsworth, KS' ,
'20055' : 'Finney, KS' ,
'20057' : 'Ford, KS' ,
'20059' : 'Franklin, KS' ,
'20061' : 'Geary, KS' ,
'20063' : 'Gove, KS' ,
'20065' : 'Graham, KS' ,
'20067' : 'Grant, KS' ,
'20069' : 'Gray, KS' ,
'20071' : 'Greeley, KS' ,
'20073' : 'Greenwood, KS' ,
'20075' : 'Hamilton, KS' ,
'20077' : 'Harper, KS' ,
'20081' : 'Haskell, KS' ,
'20083' : 'Hodgeman, KS' ,
'20085' : 'Jackson, KS' ,
'20087' : 'Jefferson, KS' ,
'20089' : 'Jewell, KS' ,
'20093' : 'Kearny, KS' ,
'20095' : 'Kingman, KS' ,
'20097' : 'Kiowa, KS' ,
'20099' : 'Labette, KS' ,
'20101' : 'Lane, KS' ,
'20105' : 'Lincoln, KS' ,
'20107' : 'Linn, KS' ,
'20109' : 'Logan, KS' ,
'20111' : 'Lyon, KS' ,
'20113' : 'McPherson, KS' ,
'20115' : 'Marion, KS' ,
'20117' : 'Marshall, KS' ,
'20119' : 'Meade, KS' ,
'20123' : 'Mitchell, KS' ,
'20125' : 'Montgomery, KS' ,
'20127' : 'Morris, KS' ,
'20129' : 'Morton, KS' ,
'20131' : 'Nemaha, KS' ,
'20133' : 'Neosho, KS' ,
'20135' : 'Ness, KS' ,
'20137' : 'Norton, KS' ,
'20139' : 'Osage, KS' ,
'20141' : 'Osborne, KS' ,
'20143' : 'Ottawa, KS' ,
'20145' : 'Pawnee, KS' ,
'20147' : 'Phillips, KS' ,
'20149' : 'Pottawatomie, KS' ,
'20151' : 'Pratt, KS' ,
'20153' : 'Rawlins, KS' ,
'20155' : 'Reno, KS' ,
'20157' : 'Republic, KS' ,
'20159' : 'Rice, KS' ,
'20161' : 'Riley, KS' ,
'20163' : 'Rooks, KS' ,
'20165' : 'Rush, KS' ,
'20167' : 'Russell, KS' ,
'20169' : 'Saline, KS' ,
'20171' : 'Scott, KS' ,
'20175' : 'Seward, KS' ,
'20179' : 'Sheridan, KS' ,
'20181' : 'Sherman, KS' ,
'20183' : 'Smith, KS' ,
'20185' : 'Stafford, KS' ,
'20187' : 'Stanton, KS' ,
'20189' : 'Stevens, KS' ,
'20191' : 'Sumner, KS' ,
'20193' : 'Thomas, KS' ,
'20195' : 'Trego, KS' ,
'20197' : 'Wabaunsee, KS' ,
'20199' : 'Wallace, KS' ,
'20201' : 'Washington, KS' ,
'20203' : 'Wichita, KS' ,
'20205' : 'Wilson, KS' ,
'20207' : 'Woodson, KS' ,
'21000' : 'KENTUCKY' ,
'21001' : 'Adair, KY' ,
'21003' : 'Allen, KY' ,
'21005' : 'Anderson, KY' ,
'21007' : 'Ballard, KY' ,
'21009' : 'Barren, KY' ,
'21011' : 'Bath, KY' ,
'21013' : 'Bell, KY' ,
'21021' : 'Boyle, KY' ,
'21023' : 'Bracken, KY' ,
'21025' : 'Breathitt, KY' ,
'21027' : 'Breckinridge, KY' ,
'21031' : 'Butler, KY' ,
'21033' : 'Caldwell, KY' ,
'21035' : 'Calloway, KY' ,
'21039' : 'Carlisle, KY' ,
'21041' : 'Carroll, KY' ,
'21043' : 'Carter, KY' ,
'21045' : 'Casey, KY' ,
'21051' : 'Clay, KY' ,
'21053' : 'Clinton, KY' ,
'21055' : 'Crittenden, KY' ,
'21057' : 'Cumberland, KY' ,
'21061' : 'Edmonson, KY' ,
'21063' : 'Elliott, KY' ,
'21065' : 'Estill, KY' ,
'21069' : 'Fleming, KY' ,
'21071' : 'Floyd, KY' ,
'21073' : 'Franklin, KY' ,
'21075' : 'Fulton, KY' ,
'21079' : 'Garrard, KY' ,
'21083' : 'Graves, KY' ,
'21085' : 'Grayson, KY' ,
'21087' : 'Green, KY' ,
'21091' : 'Hancock, KY' ,
'21093' : 'Hardin, KY' ,
'21095' : 'Harlan, KY' ,
'21097' : 'Harrison, KY' ,
'21099' : 'Hart, KY' ,
'21103' : 'Henry, KY' ,
'21105' : 'Hickman, KY' ,
'21107' : 'Hopkins, KY' ,
'21109' : 'Jackson, KY' ,
'21115' : 'Johnson, KY' ,
'21119' : 'Knott, KY' ,
'21121' : 'Knox, KY' ,
'21123' : 'Larue, KY' ,
'21125' : 'Laurel, KY' ,
'21127' : 'Lawrence, KY' ,
'21129' : 'Lee, KY' ,
'21131' : 'Leslie, KY' ,
'21133' : 'Letcher, KY' ,
'21135' : 'Lewis, KY' ,
'21137' : 'Lincoln, KY' ,
'21139' : 'Livingston, KY' ,
'21141' : 'Logan, KY' ,
'21143' : 'Lyon, KY' ,
'21145' : 'McCracken, KY' ,
'21147' : 'McCreary, KY' ,
'21149' : 'McLean, KY' ,
'21153' : 'Magoffin, KY' ,
'21155' : 'Marion, KY' ,
'21157' : 'Marshall, KY' ,
'21159' : 'Martin, KY' ,
'21161' : 'Mason, KY' ,
'21163' : 'Meade, KY' ,
'21165' : 'Menifee, KY' ,
'21167' : 'Mercer, KY' ,
'21169' : 'Metcalfe, KY' ,
'21171' : 'Monroe, KY' ,
'21173' : 'Montgomery, KY' ,
'21175' : 'Morgan, KY' ,
'21177' : 'Muhlenberg, KY' ,
'21179' : 'Nelson, KY' ,
'21181' : 'Nicholas, KY' ,
'21183' : 'Ohio, KY' ,
'21187' : 'Owen, KY' ,
'21189' : 'Owsley, KY' ,
'21193' : 'Perry, KY' ,
'21195' : 'Pike, KY' ,
'21197' : 'Powell, KY' ,
'21199' : 'Pulaski, KY' ,
'21201' : 'Robertson, KY' ,
'21203' : 'Rockcastle, KY' ,
'21205' : 'Rowan, KY' ,
'21207' : 'Russell, KY' ,
'21211' : 'Shelby, KY' ,
'21213' : 'Simpson, KY' ,
'21215' : 'Spencer, KY' ,
'21217' : 'Taylor, KY' ,
'21219' : 'Todd, KY' ,
'21221' : 'Trigg, KY' ,
'21223' : 'Trimble, KY' ,
'21225' : 'Union, KY' ,
'21227' : 'Warren, KY' ,
'21229' : 'Washington, KY' ,
'21231' : 'Wayne, KY' ,
'21233' : 'Webster, KY' ,
'21235' : 'Whitley, KY' ,
'21237' : 'Wolfe, KY' ,
'22000' : 'LOUISIANA' ,
'22003' : 'Allen, LA' ,
'22005' : 'Ascension, LA' ,
'22007' : 'Assumption, LA' ,
'22009' : 'Avoyelles, LA' ,
'22011' : 'Beauregard, LA' ,
'22013' : 'Bienville, LA' ,
'22021' : 'Caldwell, LA' ,
'22023' : 'Cameron, LA' ,
'22025' : 'Catahoula, LA' ,
'22027' : 'Claiborne, LA' ,
'22029' : 'Concordia, LA' ,
'22031' : 'De Soto, LA' ,
'22035' : 'East Carroll, LA' ,
'22037' : 'East Feliciana, LA' ,
'22039' : 'Evangeline, LA' ,
'22041' : 'Franklin, LA' ,
'22043' : 'Grant, LA' ,
'22045' : 'Iberia, LA' ,
'22047' : 'Iberville, LA' ,
'22049' : 'Jackson, LA' ,
'22053' : 'Jefferson Davis, LA' ,
'22059' : 'La Salle, LA' ,
'22061' : 'Lincoln, LA' ,
'22065' : 'Madison, LA' ,
'22067' : 'Morehouse, LA' ,
'22069' : 'Natchitoches, LA' ,
'22077' : 'Pointe Coupee, LA' ,
'22081' : 'Red River, LA' ,
'22083' : 'Richland, LA' ,
'22085' : 'Sabine, LA' ,
'22091' : 'St. Helena, LA' ,
'22101' : 'St. Mary, LA' ,
'22105' : 'Tangipahoa, LA' ,
'22107' : 'Tensas, LA' ,
'22111' : 'Union, LA' ,
'22113' : 'Vermilion, LA' ,
'22115' : 'Vernon, LA' ,
'22117' : 'Washington, LA' ,
'22123' : 'West Carroll, LA' ,
'22125' : 'West Feliciana, LA' ,
'22127' : 'Winn, LA' ,
'25000' : 'MASSACHUSETTS' ,
'25007' : 'Dukes, MA' ,
'25011' : 'Franklin, MA' ,
'25019' : 'Nantucket, MA' ,
'24000' : 'MARYLAND' ,
'24011' : 'Caroline, MD' ,
'24019' : 'Dorchester, MD' ,
'24023' : 'Garrett, MD' ,
'24029' : 'Kent, MD' ,
'24037' : 'St. Mary\'s, MD' ,
'24039' : 'Somerset, MD' ,
'24041' : 'Talbot, MD' ,
'24045' : 'Wicomico, MD' ,
'24047' : 'Worcester, MD' ,
'23000' : 'MAINE' ,
'23003' : 'Aroostook, ME' ,
'23007' : 'Franklin, ME' ,
'23009' : 'Hancock, ME' ,
'23011' : 'Kennebec, ME' ,
'23013' : 'Knox, ME' ,
'23015' : 'Lincoln, ME' ,
'23017' : 'Oxford, ME' ,
'23021' : 'Piscataquis, ME' ,
'23023' : 'Sagadahoc, ME' ,
'23025' : 'Somerset, ME' ,
'23027' : 'Waldo, ME' ,
'23029' : 'Washington, ME' ,
'23031' : 'York, ME' ,
'26000' : 'MICHIGAN' ,
'26001' : 'Alcona, MI' ,
'26003' : 'Alger, MI' ,
'26007' : 'Alpena, MI' ,
'26009' : 'Antrim, MI' ,
'26011' : 'Arenac, MI' ,
'26013' : 'Baraga, MI' ,
'26015' : 'Barry, MI' ,
'26019' : 'Benzie, MI' ,
'26023' : 'Branch, MI' ,
'26027' : 'Cass, MI' ,
'26029' : 'Charlevoix, MI' ,
'26031' : 'Cheboygan, MI' ,
'26033' : 'Chippewa, MI' ,
'26035' : 'Clare, MI' ,
'26039' : 'Crawford, MI' ,
'26041' : 'Delta, MI' ,
'26043' : 'Dickinson, MI' ,
'26047' : 'Emmet, MI' ,
'26051' : 'Gladwin, MI' ,
'26053' : 'Gogebic, MI' ,
'26055' : 'Grand Traverse, MI' ,
'26057' : 'Gratiot, MI' ,
'26059' : 'Hillsdale, MI' ,
'26061' : 'Houghton, MI' ,
'26063' : 'Huron, MI' ,
'26067' : 'Ionia, MI' ,
'26069' : 'Iosco, MI' ,
'26071' : 'Iron, MI' ,
'26073' : 'Isabella, MI' ,
'26079' : 'Kalkaska, MI' ,
'26083' : 'Keweenaw, MI' ,
'26085' : 'Lake, MI' ,
'26089' : 'Leelanau, MI' ,
'26095' : 'Luce, MI' ,
'26097' : 'Mackinac, MI' ,
'26101' : 'Manistee, MI' ,
'26103' : 'Marquette, MI' ,
'26105' : 'Mason, MI' ,
'26107' : 'Mecosta, MI' ,
'26109' : 'Menominee, MI' ,
'26113' : 'Missaukee, MI' ,
'26117' : 'Montcalm, MI' ,
'26119' : 'Montmorency, MI' ,
'26123' : 'Newaygo, MI' ,
'26127' : 'Oceana, MI' ,
'26129' : 'Ogemaw, MI' ,
'26131' : 'Ontonagon, MI' ,
'26133' : 'Osceola, MI' ,
'26135' : 'Oscoda, MI' ,
'26137' : 'Otsego, MI' ,
'26141' : 'Presque Isle, MI' ,
'26143' : 'Roscommon, MI' ,
'26149' : 'St. Joseph, MI' ,
'26151' : 'Sanilac, MI' ,
'26153' : 'Schoolcraft, MI' ,
'26155' : 'Shiawassee, MI' ,
'26157' : 'Tuscola, MI' ,
'26165' : 'Wexford, MI' ,
'27000' : 'MINNESOTA' ,
'27001' : 'Aitkin, MN' ,
'27005' : 'Becker, MN' ,
'27007' : 'Beltrami, MN' ,
'27011' : 'Big Stone, MN' ,
'27013' : 'Blue Earth, MN' ,
'27015' : 'Brown, MN' ,
'27017' : 'Carlton, MN' ,
'27021' : 'Cass, MN' ,
'27023' : 'Chippewa, MN' ,
'27029' : 'Clearwater, MN' ,
'27031' : 'Cook, MN' ,
'27033' : 'Cottonwood, MN' ,
'27035' : 'Crow Wing, MN' ,
'27039' : 'Dodge, MN' ,
'27041' : 'Douglas, MN' ,
'27043' : 'Faribault, MN' ,
'27045' : 'Fillmore, MN' ,
'27047' : 'Freeborn, MN' ,
'27049' : 'Goodhue, MN' ,
'27051' : 'Grant, MN' ,
'27057' : 'Hubbard, MN' ,
'27061' : 'Itasca, MN' ,
'27063' : 'Jackson, MN' ,
'27065' : 'Kanabec, MN' ,
'27067' : 'Kandiyohi, MN' ,
'27069' : 'Kittson, MN' ,
'27071' : 'Koochiching, MN' ,
'27073' : 'Lac qui Parle, MN' ,
'27075' : 'Lake, MN' ,
'27077' : 'Lake of the Woods, MN' ,
'27079' : 'Le Sueur, MN' ,
'27081' : 'Lincoln, MN' ,
'27083' : 'Lyon, MN' ,
'27085' : 'McLeod, MN' ,
'27087' : 'Mahnomen, MN' ,
'27089' : 'Marshall, MN' ,
'27091' : 'Martin, MN' ,
'27093' : 'Meeker, MN' ,
'27095' : 'Mille Lacs, MN' ,
'27097' : 'Morrison, MN' ,
'27099' : 'Mower, MN' ,
'27101' : 'Murray, MN' ,
'27103' : 'Nicollet, MN' ,
'27105' : 'Nobles, MN' ,
'27107' : 'Norman, MN' ,
'27111' : 'Otter Tail, MN' ,
'27113' : 'Pennington, MN' ,
'27115' : 'Pine, MN' ,
'27117' : 'Pipestone, MN' ,
'27121' : 'Pope, MN' ,
'27125' : 'Red Lake, MN' ,
'27127' : 'Redwood, MN' ,
'27129' : 'Renville, MN' ,
'27131' : 'Rice, MN' ,
'27133' : 'Rock, MN' ,
'27135' : 'Roseau, MN' ,
'27143' : 'Sibley, MN' ,
'27147' : 'Steele, MN' ,
'27149' : 'Stevens, MN' ,
'27151' : 'Swift, MN' ,
'27153' : 'Todd, MN' ,
'27155' : 'Traverse, MN' ,
'27157' : 'Wabasha, MN' ,
'27159' : 'Wadena, MN' ,
'27161' : 'Waseca, MN' ,
'27165' : 'Watonwan, MN' ,
'27167' : 'Wilkin, MN' ,
'27169' : 'Winona, MN' ,
'27173' : 'Yellow Medicine, MN' ,
'29000' : 'MISSOURI' ,
'29001' : 'Adair, MO' ,
'29005' : 'Atchison, MO' ,
'29007' : 'Audrain, MO' ,
'29009' : 'Barry, MO' ,
'29011' : 'Barton, MO' ,
'29013' : 'Bates, MO' ,
'29015' : 'Benton, MO' ,
'29017' : 'Bollinger, MO' ,
'29023' : 'Butler, MO' ,
'29025' : 'Caldwell, MO' ,
'29027' : 'Callaway, MO' ,
'29029' : 'Camden, MO' ,
'29031' : 'Cape Girardeau, MO' ,
'29033' : 'Carroll, MO' ,
'29035' : 'Carter, MO' ,
'29039' : 'Cedar, MO' ,
'29041' : 'Chariton, MO' ,
'29045' : 'Clark, MO' ,
'29051' : 'Cole, MO' ,
'29053' : 'Cooper, MO' ,
'29055' : 'Crawford, MO' ,
'29057' : 'Dade, MO' ,
'29059' : 'Dallas, MO' ,
'29061' : 'Daviess, MO' ,
'29063' : 'De Kalb, MO' ,
'29065' : 'Dent, MO' ,
'29067' : 'Douglas, MO' ,
'29069' : 'Dunklin, MO' ,
'29073' : 'Gasconade, MO' ,
'29075' : 'Gentry, MO' ,
'29079' : 'Grundy, MO' ,
'29081' : 'Harrison, MO' ,
'29083' : 'Henry, MO' ,
'29085' : 'Hickory, MO' ,
'29087' : 'Holt, MO' ,
'29089' : 'Howard, MO' ,
'29091' : 'Howell, MO' ,
'29093' : 'Iron, MO' ,
'29101' : 'Johnson, MO' ,
'29103' : 'Knox, MO' ,
'29105' : 'Laclede, MO' ,
'29109' : 'Lawrence, MO' ,
'29111' : 'Lewis, MO' ,
'29115' : 'Linn, MO' ,
'29117' : 'Livingston, MO' ,
'29119' : 'McDonald, MO' ,
'29121' : 'Macon, MO' ,
'29123' : 'Madison, MO' ,
'29125' : 'Maries, MO' ,
'29127' : 'Marion, MO' ,
'29129' : 'Mercer, MO' ,
'29131' : 'Miller, MO' ,
'29133' : 'Mississippi, MO' ,
'29135' : 'Moniteau, MO' ,
'29137' : 'Monroe, MO' ,
'29139' : 'Montgomery, MO' ,
'29141' : 'Morgan, MO' ,
'29143' : 'New Madrid, MO' ,
'29147' : 'Nodaway, MO' ,
'29149' : 'Oregon, MO' ,
'29151' : 'Osage, MO' ,
'29153' : 'Ozark, MO' ,
'29155' : 'Pemiscot, MO' ,
'29157' : 'Perry, MO' ,
'29159' : 'Pettis, MO' ,
'29161' : 'Phelps, MO' ,
'29163' : 'Pike, MO' ,
'29167' : 'Polk, MO' ,
'29169' : 'Pulaski, MO' ,
'29171' : 'Putnam, MO' ,
'29173' : 'Ralls, MO' ,
'29175' : 'Randolph, MO' ,
'29179' : 'Reynolds, MO' ,
'29181' : 'Ripley, MO' ,
'29185' : 'St. Clair, MO' ,
'29186' : 'Ste. Genevieve, MO' ,
'29187' : 'St. Francois, MO' ,
'29195' : 'Saline, MO' ,
'29197' : 'Schuyler, MO' ,
'29199' : 'Scotland, MO' ,
'29201' : 'Scott, MO' ,
'29203' : 'Shannon, MO' ,
'29205' : 'Shelby, MO' ,
'29207' : 'Stoddard, MO' ,
'29209' : 'Stone, MO' ,
'29211' : 'Sullivan, MO' ,
'29213' : 'Taney, MO' ,
'29215' : 'Texas, MO' ,
'29217' : 'Vernon, MO' ,
'29221' : 'Washington, MO' ,
'29223' : 'Wayne, MO' ,
'29227' : 'Worth, MO' ,
'29229' : 'Wright, MO' ,
'28000' : 'MISSISSIPPI' ,
'28001' : 'Adams, MS' ,
'28003' : 'Alcorn, MS' ,
'28005' : 'Amite, MS' ,
'28007' : 'Attala, MS' ,
'28009' : 'Benton, MS' ,
'28011' : 'Bolivar, MS' ,
'28013' : 'Calhoun, MS' ,
'28015' : 'Carroll, MS' ,
'28017' : 'Chickasaw, MS' ,
'28019' : 'Choctaw, MS' ,
'28021' : 'Claiborne, MS' ,
'28023' : 'Clarke, MS' ,
'28025' : 'Clay, MS' ,
'28027' : 'Coahoma, MS' ,
'28029' : 'Copiah, MS' ,
'28031' : 'Covington, MS' ,
'28035' : 'Forrest, MS' ,
'28037' : 'Franklin, MS' ,
'28039' : 'George, MS' ,
'28041' : 'Greene, MS' ,
'28043' : 'Grenada, MS' ,
'28051' : 'Holmes, MS' ,
'28053' : 'Humphreys, MS' ,
'28055' : 'Issaquena, MS' ,
'28057' : 'Itawamba, MS' ,
'28061' : 'Jasper, MS' ,
'28063' : 'Jefferson, MS' ,
'28065' : 'Jefferson Davis, MS' ,
'28067' : 'Jones, MS' ,
'28069' : 'Kemper, MS' ,
'28071' : 'Lafayette, MS' ,
'28073' : 'Lamar, MS' ,
'28075' : 'Lauderdale, MS' ,
'28077' : 'Lawrence, MS' ,
'28079' : 'Leake, MS' ,
'28081' : 'Lee, MS' ,
'28083' : 'Leflore, MS' ,
'28085' : 'Lincoln, MS' ,
'28087' : 'Lowndes, MS' ,
'28091' : 'Marion, MS' ,
'28093' : 'Marshall, MS' ,
'28095' : 'Monroe, MS' ,
'28097' : 'Montgomery, MS' ,
'28099' : 'Neshoba, MS' ,
'28101' : 'Newton, MS' ,
'28103' : 'Noxubee, MS' ,
'28105' : 'Oktibbeha, MS' ,
'28107' : 'Panola, MS' ,
'28109' : 'Pearl River, MS' ,
'28111' : 'Perry, MS' ,
'28113' : 'Pike, MS' ,
'28115' : 'Pontotoc, MS' ,
'28117' : 'Prentiss, MS' ,
'28119' : 'Quitman, MS' ,
'28123' : 'Scott, MS' ,
'28125' : 'Sharkey, MS' ,
'28127' : 'Simpson, MS' ,
'28129' : 'Smith, MS' ,
'28131' : 'Stone, MS' ,
'28133' : 'Sunflower, MS' ,
'28135' : 'Tallahatchie, MS' ,
'28137' : 'Tate, MS' ,
'28139' : 'Tippah, MS' ,
'28141' : 'Tishomingo, MS' ,
'28143' : 'Tunica, MS' ,
'28145' : 'Union, MS' ,
'28147' : 'Walthall, MS' ,
'28149' : 'Warren, MS' ,
'28151' : 'Washington, MS' ,
'28153' : 'Wayne, MS' ,
'28155' : 'Webster, MS' ,
'28157' : 'Wilkinson, MS' ,
'28159' : 'Winston, MS' ,
'28161' : 'Yalobusha, MS' ,
'28163' : 'Yazoo, MS' ,
'30000' : 'MONTANA' ,
'30001' : 'Beaverhead, MT' ,
'30003' : 'Big Horn, MT' ,
'30005' : 'Blaine, MT' ,
'30007' : 'Broadwater, MT' ,
'30009' : 'Carbon, MT' ,
'30011' : 'Carter, MT' ,
'30015' : 'Chouteau, MT' ,
'30017' : 'Custer, MT' ,
'30019' : 'Daniels, MT' ,
'30021' : 'Dawson, MT' ,
'30023' : 'Deer Lodge, MT' ,
'30025' : 'Fallon, MT' ,
'30027' : 'Fergus, MT' ,
'30029' : 'Flathead, MT' ,
'30031' : 'Gallatin, MT' ,
'30033' : 'Garfield, MT' ,
'30035' : 'Glacier, MT' ,
'30037' : 'Golden Valley, MT' ,
'30039' : 'Granite, MT' ,
'30041' : 'Hill, MT' ,
'30043' : 'Jefferson, MT' ,
'30045' : 'Judith Basin, MT' ,
'30047' : 'Lake, MT' ,
'30049' : 'Lewis and Clark, MT' ,
'30051' : 'Liberty, MT' ,
'30053' : 'Lincoln, MT' ,
'30055' : 'McCone, MT' ,
'30057' : 'Madison, MT' ,
'30059' : 'Meagher, MT' ,
'30061' : 'Mineral, MT' ,
'30063' : 'Missoula, MT' ,
'30065' : 'Musselshell, MT' ,
'30067' : 'Park, MT' ,
'30069' : 'Petroleum, MT' ,
'30071' : 'Phillips, MT' ,
'30073' : 'Pondera, MT' ,
'30075' : 'Powder River, MT' ,
'30077' : 'Powell, MT' ,
'30079' : 'Prairie, MT' ,
'30081' : 'Ravalli, MT' ,
'30083' : 'Richland, MT' ,
'30085' : 'Roosevelt, MT' ,
'30087' : 'Rosebud, MT' ,
'30089' : 'Sanders, MT' ,
'30091' : 'Sheridan, MT' ,
'30093' : 'Silver Bow, MT' ,
'30095' : 'Stillwater, MT' ,
'30097' : 'Sweet Grass, MT' ,
'30099' : 'Teton, MT' ,
'30101' : 'Toole, MT' ,
'30103' : 'Treasure, MT' ,
'30105' : 'Valley, MT' ,
'30107' : 'Wheatland, MT' ,
'30109' : 'Wibaux, MT' ,
'30113' : 'Yellowstone National Park, MT' ,
'37000' : 'NORTH CAROLINA' ,
'37005' : 'Alleghany, NC' ,
'37007' : 'Anson, NC' ,
'37009' : 'Ashe, NC' ,
'37011' : 'Avery, NC' ,
'37013' : 'Beaufort, NC' ,
'37015' : 'Bertie, NC' ,
'37017' : 'Bladen, NC' ,
'37019' : 'Brunswick, NC' ,
'37029' : 'Camden, NC' ,
'37031' : 'Carteret, NC' ,
'37033' : 'Caswell, NC' ,
'37039' : 'Cherokee, NC' ,
'37041' : 'Chowan, NC' ,
'37043' : 'Clay, NC' ,
'37045' : 'Cleveland, NC' ,
'37047' : 'Columbus, NC' ,
'37049' : 'Craven, NC' ,
'37055' : 'Dare, NC' ,
'37061' : 'Duplin, NC' ,
'37073' : 'Gates, NC' ,
'37075' : 'Graham, NC' ,
'37077' : 'Granville, NC' ,
'37079' : 'Greene, NC' ,
'37083' : 'Halifax, NC' ,
'37085' : 'Harnett, NC' ,
'37087' : 'Haywood, NC' ,
'37089' : 'Henderson, NC' ,
'37091' : 'Hertford, NC' ,
'37093' : 'Hoke, NC' ,
'37095' : 'Hyde, NC' ,
'37097' : 'Iredell, NC' ,
'37099' : 'Jackson, NC' ,
'37103' : 'Jones, NC' ,
'37105' : 'Lee, NC' ,
'37107' : 'Lenoir, NC' ,
'37111' : 'McDowell, NC' ,
'37113' : 'Macon, NC' ,
'37117' : 'Martin, NC' ,
'37121' : 'Mitchell, NC' ,
'37123' : 'Montgomery, NC' ,
'37125' : 'Moore, NC' ,
'37131' : 'Northampton, NC' ,
'37137' : 'Pamlico, NC' ,
'37139' : 'Pasquotank, NC' ,
'37141' : 'Pender, NC' ,
'37143' : 'Perquimans, NC' ,
'37145' : 'Person, NC' ,
'37149' : 'Polk, NC' ,
'37153' : 'Richmond, NC' ,
'37155' : 'Robeson, NC' ,
'37157' : 'Rockingham, NC' ,
'37161' : 'Rutherford, NC' ,
'37163' : 'Sampson, NC' ,
'37165' : 'Scotland, NC' ,
'37167' : 'Stanly, NC' ,
'37171' : 'Surry, NC' ,
'37173' : 'Swain, NC' ,
'37175' : 'Transylvania, NC' ,
'37177' : 'Tyrrell, NC' ,
'37181' : 'Vance, NC' ,
'37185' : 'Warren, NC' ,
'37187' : 'Washington, NC' ,
'37189' : 'Watauga, NC' ,
'37193' : 'Wilkes, NC' ,
'37195' : 'Wilson, NC' ,
'37199' : 'Yancey, NC' ,
'38000' : 'NORTH DAKOTA' ,
'38001' : 'Adams, ND' ,
'38003' : 'Barnes, ND' ,
'38005' : 'Benson, ND' ,
'38007' : 'Billings, ND' ,
'38009' : 'Bottineau, ND' ,
'38011' : 'Bowman, ND' ,
'38013' : 'Burke, ND' ,
'38019' : 'Cavalier, ND' ,
'38021' : 'Dickey, ND' ,
'38023' : 'Divide, ND' ,
'38025' : 'Dunn, ND' ,
'38027' : 'Eddy, ND' ,
'38029' : 'Emmons, ND' ,
'38031' : 'Foster, ND' ,
'38033' : 'Golden Valley, ND' ,
'38037' : 'Grant, ND' ,
'38039' : 'Griggs, ND' ,
'38041' : 'Hettinger, ND' ,
'38043' : 'Kidder, ND' ,
'38045' : 'La Moure, ND' ,
'38047' : 'Logan, ND' ,
'38049' : 'McHenry, ND' ,
'38051' : 'McIntosh, ND' ,
'38053' : 'McKenzie, ND' ,
'38055' : 'McLean, ND' ,
'38057' : 'Mercer, ND' ,
'38061' : 'Mountrail, ND' ,
'38063' : 'Nelson, ND' ,
'38065' : 'Oliver, ND' ,
'38067' : 'Pembina, ND' ,
'38069' : 'Pierce, ND' ,
'38071' : 'Ramsey, ND' ,
'38073' : 'Ransom, ND' ,
'38075' : 'Renville, ND' ,
'38077' : 'Richland, ND' ,
'38079' : 'Rolette, ND' ,
'38081' : 'Sargent, ND' ,
'38083' : 'Sheridan, ND' ,
'38085' : 'Sioux, ND' ,
'38087' : 'Slope, ND' ,
'38089' : 'Stark, ND' ,
'38091' : 'Steele, ND' ,
'38093' : 'Stutsman, ND' ,
'38095' : 'Towner, ND' ,
'38097' : 'Traill, ND' ,
'38099' : 'Walsh, ND' ,
'38101' : 'Ward, ND' ,
'38103' : 'Wells, ND' ,
'38105' : 'Williams, ND' ,
'31000' : 'NEBRASKA' ,
'31001' : 'Adams, NE' ,
'31003' : 'Antelope, NE' ,
'31005' : 'Arthur, NE' ,
'31007' : 'Banner, NE' ,
'31009' : 'Blaine, NE' ,
'31011' : 'Boone, NE' ,
'31013' : 'Box Butte, NE' ,
'31015' : 'Boyd, NE' ,
'31017' : 'Brown, NE' ,
'31019' : 'Buffalo, NE' ,
'31021' : 'Burt, NE' ,
'31023' : 'Butler, NE' ,
'31027' : 'Cedar, NE' ,
'31029' : 'Chase, NE' ,
'31031' : 'Cherry, NE' ,
'31033' : 'Cheyenne, NE' ,
'31035' : 'Clay, NE' ,
'31037' : 'Colfax, NE' ,
'31039' : 'Cuming, NE' ,
'31041' : 'Custer, NE' ,
'31045' : 'Dawes, NE' ,
'31047' : 'Dawson, NE' ,
'31049' : 'Deuel, NE' ,
'31051' : 'Dixon, NE' ,
'31053' : 'Dodge, NE' ,
'31057' : 'Dundy, NE' ,
'31059' : 'Fillmore, NE' ,
'31061' : 'Franklin, NE' ,
'31063' : 'Frontier, NE' ,
'31065' : 'Furnas, NE' ,
'31067' : 'Gage, NE' ,
'31069' : 'Garden, NE' ,
'31071' : 'Garfield, NE' ,
'31073' : 'Gosper, NE' ,
'31075' : 'Grant, NE' ,
'31077' : 'Greeley, NE' ,
'31079' : 'Hall, NE' ,
'31081' : 'Hamilton, NE' ,
'31083' : 'Harlan, NE' ,
'31085' : 'Hayes, NE' ,
'31087' : 'Hitchcock, NE' ,
'31089' : 'Holt, NE' ,
'31091' : 'Hooker, NE' ,
'31093' : 'Howard, NE' ,
'31095' : 'Jefferson, NE' ,
'31097' : 'Johnson, NE' ,
'31099' : 'Kearney, NE' ,
'31101' : 'Keith, NE' ,
'31103' : 'Keya Paha, NE' ,
'31105' : 'Kimball, NE' ,
'31107' : 'Knox, NE' ,
'31111' : 'Lincoln, NE' ,
'31113' : 'Logan, NE' ,
'31115' : 'Loup, NE' ,
'31117' : 'McPherson, NE' ,
'31119' : 'Madison, NE' ,
'31121' : 'Merrick, NE' ,
'31123' : 'Morrill, NE' ,
'31125' : 'Nance, NE' ,
'31127' : 'Nemaha, NE' ,
'31129' : 'Nuckolls, NE' ,
'31131' : 'Otoe, NE' ,
'31133' : 'Pawnee, NE' ,
'31135' : 'Perkins, NE' ,
'31137' : 'Phelps, NE' ,
'31139' : 'Pierce, NE' ,
'31141' : 'Platte, NE' ,
'31143' : 'Polk, NE' ,
'31145' : 'Red Willow, NE' ,
'31147' : 'Richardson, NE' ,
'31149' : 'Rock, NE' ,
'31151' : 'Saline, NE' ,
'31155' : 'Saunders, NE' ,
'31157' : 'Scotts Bluff, NE' ,
'31159' : 'Seward, NE' ,
'31161' : 'Sheridan, NE' ,
'31163' : 'Sherman, NE' ,
'31165' : 'Sioux, NE' ,
'31167' : 'Stanton, NE' ,
'31169' : 'Thayer, NE' ,
'31171' : 'Thomas, NE' ,
'31173' : 'Thurston, NE' ,
'31175' : 'Valley, NE' ,
'31179' : 'Wayne, NE' ,
'31181' : 'Webster, NE' ,
'31183' : 'Wheeler, NE' ,
'31185' : 'York, NE' ,
'33000' : 'NEW HAMPSHIRE' ,
'33001' : 'Belknap, NH' ,
'33003' : 'Carroll, NH' ,
'33005' : 'Cheshire, NH' ,
'33007' : 'Coos, NH' ,
'33009' : 'Grafton, NH' ,
'33013' : 'Merrimack, NH' ,
'33019' : 'Sullivan, NH' ,
'34000' : 'NEW JERSEY' ,
'35000' : 'NEW MEXICO' ,
'35003' : 'Catron, NM' ,
'35005' : 'Chaves, NM' ,
'35006' : 'Cibola, NM' ,
'35007' : 'Colfax, NM' ,
'35009' : 'Curry, NM' ,
'35011' : 'De Baca, NM' ,
'35015' : 'Eddy, NM' ,
'35017' : 'Grant, NM' ,
'35019' : 'Guadalupe, NM' ,
'35021' : 'Harding, NM' ,
'35023' : 'Hidalgo, NM' ,
'35025' : 'Lea, NM' ,
'35027' : 'Lincoln, NM' ,
'35029' : 'Luna, NM' ,
'35031' : 'McKinley, NM' ,
'35033' : 'Mora, NM' ,
'35035' : 'Otero, NM' ,
'35037' : 'Quay, NM' ,
'35039' : 'Rio Arriba, NM' ,
'35041' : 'Roosevelt, NM' ,
'35045' : 'San Juan, NM' ,
'35047' : 'San Miguel, NM' ,
'35051' : 'Sierra, NM' ,
'35053' : 'Socorro, NM' ,
'35055' : 'Taos, NM' ,
'35057' : 'Torrance, NM' ,
'35059' : 'Union, NM' ,
'32000' : 'NEVADA' ,
'32001' : 'Churchill, NV' ,
'32005' : 'Douglas, NV' ,
'32007' : 'Elko, NV' ,
'32009' : 'Esmeralda, NV' ,
'32011' : 'Eureka, NV' ,
'32013' : 'Humboldt, NV' ,
'32015' : 'Lander, NV' ,
'32017' : 'Lincoln, NV' ,
'32019' : 'Lyon, NV' ,
'32021' : 'Mineral, NV' ,
'32027' : 'Pershing, NV' ,
'32029' : 'Storey, NV' ,
'32033' : 'White Pine, NV' ,
'32510' : 'Carson City city, NV' ,
'36000' : 'NEW YORK' ,
'36003' : 'Allegany, NY' ,
'36009' : 'Cattaraugus, NY' ,
'36017' : 'Chenango, NY' ,
'36019' : 'Clinton, NY' ,
'36021' : 'Columbia, NY' ,
'36023' : 'Cortland, NY' ,
'36025' : 'Delaware, NY' ,
'36031' : 'Essex, NY' ,
'36033' : 'Franklin, NY' ,
'36035' : 'Fulton, NY' ,
'36039' : 'Greene, NY' ,
'36041' : 'Hamilton, NY' ,
'36045' : 'Jefferson, NY' ,
'36049' : 'Lewis, NY' ,
'36077' : 'Otsego, NY' ,
'36089' : 'St. Lawrence, NY' ,
'36097' : 'Schuyler, NY' ,
'36099' : 'Seneca, NY' ,
'36101' : 'Steuben, NY' ,
'36105' : 'Sullivan, NY' ,
'36109' : 'Tompkins, NY' ,
'36111' : 'Ulster, NY' ,
'36121' : 'Wyoming, NY' ,
'36123' : 'Yates, NY' ,
'39000' : 'OHIO' ,
'39001' : 'Adams, OH' ,
'39005' : 'Ashland, OH' ,
'39009' : 'Athens, OH' ,
'39021' : 'Champaign, OH' ,
'39027' : 'Clinton, OH' ,
'39031' : 'Coshocton, OH' ,
'39037' : 'Darke, OH' ,
'39039' : 'Defiance, OH' ,
'39043' : 'Erie, OH' ,
'39047' : 'Fayette, OH' ,
'39053' : 'Gallia, OH' ,
'39059' : 'Guernsey, OH' ,
'39063' : 'Hancock, OH' ,
'39065' : 'Hardin, OH' ,
'39067' : 'Harrison, OH' ,
'39069' : 'Henry, OH' ,
'39071' : 'Highland, OH' ,
'39073' : 'Hocking, OH' ,
'39075' : 'Holmes, OH' ,
'39077' : 'Huron, OH' ,
'39079' : 'Jackson, OH' ,
'39083' : 'Knox, OH' ,
'39091' : 'Logan, OH' ,
'39101' : 'Marion, OH' ,
'39105' : 'Meigs, OH' ,
'39107' : 'Mercer, OH' ,
'39111' : 'Monroe, OH' ,
'39115' : 'Morgan, OH' ,
'39117' : 'Morrow, OH' ,
'39119' : 'Muskingum, OH' ,
'39121' : 'Noble, OH' ,
'39123' : 'Ottawa, OH' ,
'39125' : 'Paulding, OH' ,
'39127' : 'Perry, OH' ,
'39131' : 'Pike, OH' ,
'39135' : 'Preble, OH' ,
'39137' : 'Putnam, OH' ,
'39141' : 'Ross, OH' ,
'39143' : 'Sandusky, OH' ,
'39145' : 'Scioto, OH' ,
'39147' : 'Seneca, OH' ,
'39149' : 'Shelby, OH' ,
'39157' : 'Tuscarawas, OH' ,
'39159' : 'Union, OH' ,
'39161' : 'Van Wert, OH' ,
'39163' : 'Vinton, OH' ,
'39169' : 'Wayne, OH' ,
'39171' : 'Williams, OH' ,
'39175' : 'Wyandot, OH' ,
'40000' : 'OKLAHOMA' ,
'40001' : 'Adair, OK' ,
'40003' : 'Alfalfa, OK' ,
'40005' : 'Atoka, OK' ,
'40007' : 'Beaver, OK' ,
'40009' : 'Beckham, OK' ,
'40011' : 'Blaine, OK' ,
'40013' : 'Bryan, OK' ,
'40015' : 'Caddo, OK' ,
'40019' : 'Carter, OK' ,
'40021' : 'Cherokee, OK' ,
'40023' : 'Choctaw, OK' ,
'40025' : 'Cimarron, OK' ,
'40029' : 'Coal, OK' ,
'40033' : 'Cotton, OK' ,
'40035' : 'Craig, OK' ,
'40039' : 'Custer, OK' ,
'40041' : 'Delaware, OK' ,
'40043' : 'Dewey, OK' ,
'40045' : 'Ellis, OK' ,
'40049' : 'Garvin, OK' ,
'40051' : 'Grady, OK' ,
'40053' : 'Grant, OK' ,
'40055' : 'Greer, OK' ,
'40057' : 'Harmon, OK' ,
'40059' : 'Harper, OK' ,
'40061' : 'Haskell, OK' ,
'40063' : 'Hughes, OK' ,
'40065' : 'Jackson, OK' ,
'40067' : 'Jefferson, OK' ,
'40069' : 'Johnston, OK' ,
'40071' : 'Kay, OK' ,
'40073' : 'Kingfisher, OK' ,
'40075' : 'Kiowa, OK' ,
'40077' : 'Latimer, OK' ,
'40079' : 'Le Flore, OK' ,
'40081' : 'Lincoln, OK' ,
'40085' : 'Love, OK' ,
'40089' : 'McCurtain, OK' ,
'40091' : 'McIntosh, OK' ,
'40093' : 'Major, OK' ,
'40095' : 'Marshall, OK' ,
'40097' : 'Mayes, OK' ,
'40099' : 'Murray, OK' ,
'40101' : 'Muskogee, OK' ,
'40103' : 'Noble, OK' ,
'40105' : 'Nowata, OK' ,
'40107' : 'Okfuskee, OK' ,
'40111' : 'Okmulgee, OK' ,
'40115' : 'Ottawa, OK' ,
'40117' : 'Pawnee, OK' ,
'40119' : 'Payne, OK' ,
'40121' : 'Pittsburg, OK' ,
'40123' : 'Pontotoc, OK' ,
'40127' : 'Pushmataha, OK' ,
'40129' : 'Roger Mills, OK' ,
'40133' : 'Seminole, OK' ,
'40137' : 'Stephens, OK' ,
'40139' : 'Texas, OK' ,
'40141' : 'Tillman, OK' ,
'40147' : 'Washington, OK' ,
'40149' : 'Washita, OK' ,
'40151' : 'Woods, OK' ,
'40153' : 'Woodward, OK' ,
'41000' : 'OREGON' ,
'41001' : 'Baker, OR' ,
'41003' : 'Benton, OR' ,
'41007' : 'Clatsop, OR' ,
'41011' : 'Coos, OR' ,
'41013' : 'Crook, OR' ,
'41015' : 'Curry, OR' ,
'41017' : 'Deschutes, OR' ,
'41019' : 'Douglas, OR' ,
'41021' : 'Gilliam, OR' ,
'41023' : 'Grant, OR' ,
'41025' : 'Harney, OR' ,
'41027' : 'Hood River, OR' ,
'41031' : 'Jefferson, OR' ,
'41033' : 'Josephine, OR' ,
'41035' : 'Klamath, OR' ,
'41037' : 'Lake, OR' ,
'41041' : 'Lincoln, OR' ,
'41043' : 'Linn, OR' ,
'41045' : 'Malheur, OR' ,
'41049' : 'Morrow, OR' ,
'41055' : 'Sherman, OR' ,
'41057' : 'Tillamook, OR' ,
'41059' : 'Umatilla, OR' ,
'41061' : 'Union, OR' ,
'41063' : 'Wallowa, OR' ,
'41065' : 'Wasco, OR' ,
'41069' : 'Wheeler, OR' ,
'42000' : 'PENNSYLVANIA' ,
'42001' : 'Adams, PA' ,
'42005' : 'Armstrong, PA' ,
'42009' : 'Bedford, PA' ,
'42015' : 'Bradford, PA' ,
'42023' : 'Cameron, PA' ,
'42031' : 'Clarion, PA' ,
'42033' : 'Clearfield, PA' ,
'42035' : 'Clinton, PA' ,
'42039' : 'Crawford, PA' ,
'42047' : 'Elk, PA' ,
'42053' : 'Forest, PA' ,
'42055' : 'Franklin, PA' ,
'42057' : 'Fulton, PA' ,
'42059' : 'Greene, PA' ,
'42061' : 'Huntingdon, PA' ,
'42063' : 'Indiana, PA' ,
'42065' : 'Jefferson, PA' ,
'42067' : 'Juniata, PA' ,
'42073' : 'Lawrence, PA' ,
'42083' : 'McKean, PA' ,
'42087' : 'Mifflin, PA' ,
'42089' : 'Monroe, PA' ,
'42093' : 'Montour, PA' ,
'42097' : 'Northumberland, PA' ,
'42105' : 'Potter, PA' ,
'42107' : 'Schuylkill, PA' ,
'42109' : 'Snyder, PA' ,
'42113' : 'Sullivan, PA' ,
'42115' : 'Susquehanna, PA' ,
'42117' : 'Tioga, PA' ,
'42119' : 'Union, PA' ,
'42121' : 'Venango, PA' ,
'42123' : 'Warren, PA' ,
'42127' : 'Wayne, PA' ,
'44000' : 'RHODE ISLAND' ,
'44005' : 'Newport, RI' ,
'45000' : 'SOUTH CAROLINA' ,
'45001' : 'Abbeville, SC' ,
'45005' : 'Allendale, SC' ,
'45009' : 'Bamberg, SC' ,
'45011' : 'Barnwell, SC' ,
'45013' : 'Beaufort, SC' ,
'45017' : 'Calhoun, SC' ,
'45023' : 'Chester, SC' ,
'45025' : 'Chesterfield, SC' ,
'45027' : 'Clarendon, SC' ,
'45029' : 'Colleton, SC' ,
'45031' : 'Darlington, SC' ,
'45033' : 'Dillon, SC' ,
'45039' : 'Fairfield, SC' ,
'45043' : 'Georgetown, SC' ,
'45047' : 'Greenwood, SC' ,
'45049' : 'Hampton, SC' ,
'45053' : 'Jasper, SC' ,
'45055' : 'Kershaw, SC' ,
'45057' : 'Lancaster, SC' ,
'45059' : 'Laurens, SC' ,
'45061' : 'Lee, SC' ,
'45065' : 'McCormick, SC' ,
'45067' : 'Marion, SC' ,
'45069' : 'Marlboro, SC' ,
'45071' : 'Newberry, SC' ,
'45073' : 'Oconee, SC' ,
'45075' : 'Orangeburg, SC' ,
'45081' : 'Saluda, SC' ,
'45087' : 'Union, SC' ,
'45089' : 'Williamsburg, SC' ,
'46000' : 'SOUTH DAKOTA' ,
'46003' : 'Aurora, SD' ,
'46005' : 'Beadle, SD' ,
'46007' : 'Bennett, SD' ,
'46009' : 'Bon Homme, SD' ,
'46011' : 'Brookings, SD' ,
'46013' : 'Brown, SD' ,
'46015' : 'Brule, SD' ,
'46017' : 'Buffalo, SD' ,
'46019' : 'Butte, SD' ,
'46021' : 'Campbell, SD' ,
'46023' : 'Charles Mix, SD' ,
'46025' : 'Clark, SD' ,
'46027' : 'Clay, SD' ,
'46029' : 'Codington, SD' ,
'46031' : 'Corson, SD' ,
'46033' : 'Custer, SD' ,
'46035' : 'Davison, SD' ,
'46037' : 'Day, SD' ,
'46039' : 'Deuel, SD' ,
'46041' : 'Dewey, SD' ,
'46043' : 'Douglas, SD' ,
'46045' : 'Edmunds, SD' ,
'46047' : 'Fall River, SD' ,
'46049' : 'Faulk, SD' ,
'46051' : 'Grant, SD' ,
'46053' : 'Gregory, SD' ,
'46055' : 'Haakon, SD' ,
'46057' : 'Hamlin, SD' ,
'46059' : 'Hand, SD' ,
'46061' : 'Hanson, SD' ,
'46063' : 'Harding, SD' ,
'46065' : 'Hughes, SD' ,
'46067' : 'Hutchinson, SD' ,
'46069' : 'Hyde, SD' ,
'46071' : 'Jackson, SD' ,
'46073' : 'Jerauld, SD' ,
'46075' : 'Jones, SD' ,
'46077' : 'Kingsbury, SD' ,
'46079' : 'Lake, SD' ,
'46081' : 'Lawrence, SD' ,
'46085' : 'Lyman, SD' ,
'46087' : 'McCook, SD' ,
'46089' : 'McPherson, SD' ,
'46091' : 'Marshall, SD' ,
'46093' : 'Meade, SD' ,
'46095' : 'Mellette, SD' ,
'46097' : 'Miner, SD' ,
'46101' : 'Moody, SD' ,
'46105' : 'Perkins, SD' ,
'46107' : 'Potter, SD' ,
'46109' : 'Roberts, SD' ,
'46111' : 'Sanborn, SD' ,
'46113' : 'Shannon, SD' ,
'46115' : 'Spink, SD' ,
'46117' : 'Stanley, SD' ,
'46119' : 'Sully, SD' ,
'46121' : 'Todd, SD' ,
'46123' : 'Tripp, SD' ,
'46125' : 'Turner, SD' ,
'46127' : 'Union, SD' ,
'46129' : 'Walworth, SD' ,
'46135' : 'Yankton, SD' ,
'46137' : 'Ziebach, SD' ,
'47000' : 'TENNESSEE' ,
'47003' : 'Bedford, TN' ,
'47005' : 'Benton, TN' ,
'47007' : 'Bledsoe, TN' ,
'47011' : 'Bradley, TN' ,
'47013' : 'Campbell, TN' ,
'47015' : 'Cannon, TN' ,
'47017' : 'Carroll, TN' ,
'47023' : 'Chester, TN' ,
'47025' : 'Claiborne, TN' ,
'47027' : 'Clay, TN' ,
'47029' : 'Cocke, TN' ,
'47031' : 'Coffee, TN' ,
'47033' : 'Crockett, TN' ,
'47035' : 'Cumberland, TN' ,
'47039' : 'Decatur, TN' ,
'47041' : 'DeKalb, TN' ,
'47045' : 'Dyer, TN' ,
'47049' : 'Fentress, TN' ,
'47051' : 'Franklin, TN' ,
'47053' : 'Gibson, TN' ,
'47055' : 'Giles, TN' ,
'47057' : 'Grainger, TN' ,
'47059' : 'Greene, TN' ,
'47061' : 'Grundy, TN' ,
'47063' : 'Hamblen, TN' ,
'47067' : 'Hancock, TN' ,
'47069' : 'Hardeman, TN' ,
'47071' : 'Hardin, TN' ,
'47075' : 'Haywood, TN' ,
'47077' : 'Henderson, TN' ,
'47079' : 'Henry, TN' ,
'47081' : 'Hickman, TN' ,
'47083' : 'Houston, TN' ,
'47085' : 'Humphreys, TN' ,
'47087' : 'Jackson, TN' ,
'47089' : 'Jefferson, TN' ,
'47091' : 'Johnson, TN' ,
'47095' : 'Lake, TN' ,
'47097' : 'Lauderdale, TN' ,
'47099' : 'Lawrence, TN' ,
'47101' : 'Lewis, TN' ,
'47103' : 'Lincoln, TN' ,
'47107' : 'McMinn, TN' ,
'47109' : 'McNairy, TN' ,
'47111' : 'Macon, TN' ,
'47113' : 'Madison, TN' ,
'47115' : 'Marion, TN' ,
'47117' : 'Marshall, TN' ,
'47119' : 'Maury, TN' ,
'47121' : 'Meigs, TN' ,
'47123' : 'Monroe, TN' ,
'47127' : 'Moore, TN' ,
'47129' : 'Morgan, TN' ,
'47131' : 'Obion, TN' ,
'47133' : 'Overton, TN' ,
'47135' : 'Perry, TN' ,
'47137' : 'Pickett, TN' ,
'47139' : 'Polk, TN' ,
'47141' : 'Putnam, TN' ,
'47143' : 'Rhea, TN' ,
'47145' : 'Roane, TN' ,
'47151' : 'Scott, TN' ,
'47153' : 'Sequatchie, TN' ,
'47159' : 'Smith, TN' ,
'47161' : 'Stewart, TN' ,
'47169' : 'Trousdale, TN' ,
'47175' : 'Van Buren, TN' ,
'47177' : 'Warren, TN' ,
'47181' : 'Wayne, TN' ,
'47183' : 'Weakley, TN' ,
'47185' : 'White, TN' ,
'48000' : 'TEXAS' ,
'48001' : 'Anderson, TX' ,
'48003' : 'Andrews, TX' ,
'48005' : 'Angelina, TX' ,
'48007' : 'Aransas, TX' ,
'48011' : 'Armstrong, TX' ,
'48013' : 'Atascosa, TX' ,
'48015' : 'Austin, TX' ,
'48017' : 'Bailey, TX' ,
'48019' : 'Bandera, TX' ,
'48023' : 'Baylor, TX' ,
'48025' : 'Bee, TX' ,
'48031' : 'Blanco, TX' ,
'48033' : 'Borden, TX' ,
'48035' : 'Bosque, TX' ,
'48043' : 'Brewster, TX' ,
'48045' : 'Briscoe, TX' ,
'48047' : 'Brooks, TX' ,
'48049' : 'Brown, TX' ,
'48051' : 'Burleson, TX' ,
'48053' : 'Burnet, TX' ,
'48057' : 'Calhoun, TX' ,
'48059' : 'Callahan, TX' ,
'48063' : 'Camp, TX' ,
'48065' : 'Carson, TX' ,
'48067' : 'Cass, TX' ,
'48069' : 'Castro, TX' ,
'48073' : 'Cherokee, TX' ,
'48075' : 'Childress, TX' ,
'48077' : 'Clay, TX' ,
'48079' : 'Cochran, TX' ,
'48081' : 'Coke, TX' ,
'48083' : 'Coleman, TX' ,
'48087' : 'Collingsworth, TX' ,
'48089' : 'Colorado, TX' ,
'48093' : 'Comanche, TX' ,
'48095' : 'Concho, TX' ,
'48097' : 'Cooke, TX' ,
'48101' : 'Cottle, TX' ,
'48103' : 'Crane, TX' ,
'48105' : 'Crockett, TX' ,
'48107' : 'Crosby, TX' ,
'48109' : 'Culberson, TX' ,
'48111' : 'Dallam, TX' ,
'48115' : 'Dawson, TX' ,
'48117' : 'Deaf Smith, TX' ,
'48119' : 'Delta, TX' ,
'48123' : 'De Witt, TX' ,
'48125' : 'Dickens, TX' ,
'48127' : 'Dimmit, TX' ,
'48129' : 'Donley, TX' ,
'48131' : 'Duval, TX' ,
'48133' : 'Eastland, TX' ,
'48137' : 'Edwards, TX' ,
'48143' : 'Erath, TX' ,
'48145' : 'Falls, TX' ,
'48147' : 'Fannin, TX' ,
'48149' : 'Fayette, TX' ,
'48151' : 'Fisher, TX' ,
'48153' : 'Floyd, TX' ,
'48155' : 'Foard, TX' ,
'48159' : 'Franklin, TX' ,
'48161' : 'Freestone, TX' ,
'48163' : 'Frio, TX' ,
'48165' : 'Gaines, TX' ,
'48169' : 'Garza, TX' ,
'48171' : 'Gillespie, TX' ,
'48173' : 'Glasscock, TX' ,
'48175' : 'Goliad, TX' ,
'48177' : 'Gonzales, TX' ,
'48179' : 'Gray, TX' ,
'48185' : 'Grimes, TX' ,
'48189' : 'Hale, TX' ,
'48191' : 'Hall, TX' ,
'48193' : 'Hamilton, TX' ,
'48195' : 'Hansford, TX' ,
'48197' : 'Hardeman, TX' ,
'48205' : 'Hartley, TX' ,
'48207' : 'Haskell, TX' ,
'48211' : 'Hemphill, TX' ,
'48217' : 'Hill, TX' ,
'48219' : 'Hockley, TX' ,
'48223' : 'Hopkins, TX' ,
'48225' : 'Houston, TX' ,
'48227' : 'Howard, TX' ,
'48229' : 'Hudspeth, TX' ,
'48233' : 'Hutchinson, TX' ,
'48235' : 'Irion, TX' ,
'48237' : 'Jack, TX' ,
'48239' : 'Jackson, TX' ,
'48241' : 'Jasper, TX' ,
'48243' : 'Jeff Davis, TX' ,
'48247' : 'Jim Hogg, TX' ,
'48249' : 'Jim Wells, TX' ,
'48253' : 'Jones, TX' ,
'48255' : 'Karnes, TX' ,
'48259' : 'Kendall, TX' ,
'48261' : 'Kenedy, TX' ,
'48263' : 'Kent, TX' ,
'48265' : 'Kerr, TX' ,
'48267' : 'Kimble, TX' ,
'48269' : 'King, TX' ,
'48271' : 'Kinney, TX' ,
'48273' : 'Kleberg, TX' ,
'48275' : 'Knox, TX' ,
'48277' : 'Lamar, TX' ,
'48279' : 'Lamb, TX' ,
'48281' : 'Lampasas, TX' ,
'48283' : 'La Salle, TX' ,
'48285' : 'Lavaca, TX' ,
'48287' : 'Lee, TX' ,
'48289' : 'Leon, TX' ,
'48293' : 'Limestone, TX' ,
'48295' : 'Lipscomb, TX' ,
'48297' : 'Live Oak, TX' ,
'48299' : 'Llano, TX' ,
'48301' : 'Loving, TX' ,
'48305' : 'Lynn, TX' ,
'48307' : 'McCulloch, TX' ,
'48311' : 'McMullen, TX' ,
'48313' : 'Madison, TX' ,
'48315' : 'Marion, TX' ,
'48317' : 'Martin, TX' ,
'48319' : 'Mason, TX' ,
'48321' : 'Matagorda, TX' ,
'48323' : 'Maverick, TX' ,
'48325' : 'Medina, TX' ,
'48327' : 'Menard, TX' ,
'48331' : 'Milam, TX' ,
'48333' : 'Mills, TX' ,
'48335' : 'Mitchell, TX' ,
'48337' : 'Montague, TX' ,
'48341' : 'Moore, TX' ,
'48343' : 'Morris, TX' ,
'48345' : 'Motley, TX' ,
'48347' : 'Nacogdoches, TX' ,
'48349' : 'Navarro, TX' ,
'48351' : 'Newton, TX' ,
'48353' : 'Nolan, TX' ,
'48357' : 'Ochiltree, TX' ,
'48359' : 'Oldham, TX' ,
'48363' : 'Palo Pinto, TX' ,
'48365' : 'Panola, TX' ,
'48369' : 'Parmer, TX' ,
'48371' : 'Pecos, TX' ,
'48373' : 'Polk, TX' ,
'48377' : 'Presidio, TX' ,
'48379' : 'Rains, TX' ,
'48383' : 'Reagan, TX' ,
'48385' : 'Real, TX' ,
'48387' : 'Red River, TX' ,
'48389' : 'Reeves, TX' ,
'48391' : 'Refugio, TX' ,
'48393' : 'Roberts, TX' ,
'48395' : 'Robertson, TX' ,
'48399' : 'Runnels, TX' ,
'48401' : 'Rusk, TX' ,
'48403' : 'Sabine, TX' ,
'48405' : 'San Augustine, TX' ,
'48407' : 'San Jacinto, TX' ,
'48411' : 'San Saba, TX' ,
'48413' : 'Schleicher, TX' ,
'48415' : 'Scurry, TX' ,
'48417' : 'Shackelford, TX' ,
'48419' : 'Shelby, TX' ,
'48421' : 'Sherman, TX' ,
'48425' : 'Somervell, TX' ,
'48427' : 'Starr, TX' ,
'48429' : 'Stephens, TX' ,
'48431' : 'Sterling, TX' ,
'48433' : 'Stonewall, TX' ,
'48435' : 'Sutton, TX' ,
'48437' : 'Swisher, TX' ,
'48443' : 'Terrell, TX' ,
'48445' : 'Terry, TX' ,
'48447' : 'Throckmorton, TX' ,
'48449' : 'Titus, TX' ,
'48455' : 'Trinity, TX' ,
'48457' : 'Tyler, TX' ,
'48461' : 'Upton, TX' ,
'48463' : 'Uvalde, TX' ,
'48465' : 'Val Verde, TX' ,
'48467' : 'Van Zandt, TX' ,
'48471' : 'Walker, TX' ,
'48475' : 'Ward, TX' ,
'48477' : 'Washington, TX' ,
'48481' : 'Wharton, TX' ,
'48483' : 'Wheeler, TX' ,
'48487' : 'Wilbarger, TX' ,
'48489' : 'Willacy, TX' ,
'48495' : 'Winkler, TX' ,
'48497' : 'Wise, TX' ,
'48499' : 'Wood, TX' ,
'48501' : 'Yoakum, TX' ,
'48503' : 'Young, TX' ,
'48505' : 'Zapata, TX' ,
'48507' : 'Zavala, TX' ,
'49000' : 'UTAH' ,
'49001' : 'Beaver, UT' ,
'49003' : 'Box Elder, UT' ,
'49005' : 'Cache, UT' ,
'49007' : 'Carbon, UT' ,
'49009' : 'Daggett, UT' ,
'49013' : 'Duchesne, UT' ,
'49015' : 'Emery, UT' ,
'49017' : 'Garfield, UT' ,
'49019' : 'Grand, UT' ,
'49021' : 'Iron, UT' ,
'49023' : 'Juab, UT' ,
'49025' : 'Kane, UT' ,
'49027' : 'Millard, UT' ,
'49029' : 'Morgan, UT' ,
'49031' : 'Piute, UT' ,
'49033' : 'Rich, UT' ,
'49037' : 'San Juan, UT' ,
'49039' : 'Sanpete, UT' ,
'49041' : 'Sevier, UT' ,
'49043' : 'Summit, UT' ,
'49045' : 'Tooele, UT' ,
'49047' : 'Uintah, UT' ,
'49051' : 'Wasatch, UT' ,
'49053' : 'Washington, UT' ,
'49055' : 'Wayne, UT' ,
'51000' : 'VIRGINIA' ,
'51001' : 'Accomack, VA' ,
'51005' : 'Alleghany, VA' ,
'51007' : 'Amelia, VA' ,
'51011' : 'Appomattox, VA' ,
'51015' : 'Augusta, VA' ,
'51017' : 'Bath, VA' ,
'51021' : 'Bland, VA' ,
'51025' : 'Brunswick, VA' ,
'51027' : 'Buchanan, VA' ,
'51029' : 'Buckingham, VA' ,
'51033' : 'Caroline, VA' ,
'51035' : 'Carroll, VA' ,
'51037' : 'Charlotte, VA' ,
'51045' : 'Craig, VA' ,
'51049' : 'Cumberland, VA' ,
'51051' : 'Dickenson, VA' ,
'51057' : 'Essex, VA' ,
'51063' : 'Floyd, VA' ,
'51067' : 'Franklin, VA' ,
'51069' : 'Frederick, VA' ,
'51071' : 'Giles, VA' ,
'51077' : 'Grayson, VA' ,
'51081' : 'Greensville, VA' ,
'51083' : 'Halifax, VA' ,
'51089' : 'Henry, VA' ,
'51091' : 'Highland, VA' ,
'51097' : 'King and Queen, VA' ,
'51101' : 'King William, VA' ,
'51103' : 'Lancaster, VA' ,
'51105' : 'Lee, VA' ,
'51109' : 'Louisa, VA' ,
'51111' : 'Lunenburg, VA' ,
'51113' : 'Madison, VA' ,
'51117' : 'Mecklenburg, VA' ,
'51119' : 'Middlesex, VA' ,
'51121' : 'Montgomery, VA' ,
'51125' : 'Nelson, VA' ,
'51131' : 'Northampton, VA' ,
'51133' : 'Northumberland, VA' ,
'51135' : 'Nottoway, VA' ,
'51137' : 'Orange, VA' ,
'51139' : 'Page, VA' ,
'51141' : 'Patrick, VA' ,
'51147' : 'Prince Edward, VA' ,
'51155' : 'Pulaski, VA' ,
'51157' : 'Rappahannock, VA' ,
'51159' : 'Richmond, VA' ,
'51163' : 'Rockbridge, VA' ,
'51165' : 'Rockingham, VA' ,
'51167' : 'Russell, VA' ,
'51171' : 'Shenandoah, VA' ,
'51173' : 'Smyth, VA' ,
'51175' : 'Southampton, VA' ,
'51181' : 'Surry, VA' ,
'51183' : 'Sussex, VA' ,
'51185' : 'Tazewell, VA' ,
'51193' : 'Westmoreland, VA' ,
'51195' : 'Wise, VA' ,
'51197' : 'Wythe, VA' ,
'51530' : 'Buena Vista, VA' ,
'51560' : 'Clifton Forge, VA' ,
'51580' : 'Covington, VA' ,
'51595' : 'Emporia, VA' ,
'51620' : 'Franklin, VA' ,
'51640' : 'Galax, VA' ,
'51660' : 'Harrisonburg, VA' ,
'51678' : 'Lexington, VA' ,
'51690' : 'Martinsville, VA' ,
'51720' : 'Norton, VA' ,
'51750' : 'Radford, VA' ,
'51780' : 'South Boston, VA' ,
'51790' : 'Staunton, VA' ,
'51820' : 'Waynesboro, VA' ,
'51840' : 'Winchester, VA' ,
'50000' : 'VERMONT' ,
'50001' : 'Addison, VT' ,
'50003' : 'Bennington, VT' ,
'50005' : 'Caledonia, VT' ,
'50009' : 'Essex, VT' ,
'50015' : 'Lamoille, VT' ,
'50017' : 'Orange, VT' ,
'50019' : 'Orleans, VT' ,
'50021' : 'Rutland, VT' ,
'50023' : 'Washington, VT' ,
'50025' : 'Windham, VT' ,
'50027' : 'Windsor, VT' ,
'53000' : 'WASHINGTON' ,
'53001' : 'Adams, WA' ,
'53003' : 'Asotin, WA' ,
'53007' : 'Chelan, WA' ,
'53009' : 'Clallam, WA' ,
'53013' : 'Columbia, WA' ,
'53015' : 'Cowlitz, WA' ,
'53017' : 'Douglas, WA' ,
'53019' : 'Ferry, WA' ,
'53023' : 'Garfield, WA' ,
'53025' : 'Grant, WA' ,
'53027' : 'Grays Harbor, WA' ,
'53031' : 'Jefferson, WA' ,
'53037' : 'Kittitas, WA' ,
'53039' : 'Klickitat, WA' ,
'53041' : 'Lewis, WA' ,
'53043' : 'Lincoln, WA' ,
'53045' : 'Mason, WA' ,
'53047' : 'Okanogan, WA' ,
'53049' : 'Pacific, WA' ,
'53051' : 'Pend Oreille, WA' ,
'53055' : 'San Juan, WA' ,
'53057' : 'Skagit, WA' ,
'53059' : 'Skamania, WA' ,
'53065' : 'Stevens, WA' ,
'53069' : 'Wahkiakum, WA' ,
'53071' : 'Walla Walla, WA' ,
'53075' : 'Whitman, WA' ,
'55000' : 'WISCONSIN' ,
'55001' : 'Adams, WI' ,
'55003' : 'Ashland, WI' ,
'55005' : 'Barron, WI' ,
'55007' : 'Bayfield, WI' ,
'55011' : 'Buffalo, WI' ,
'55013' : 'Burnett, WI' ,
'55019' : 'Clark, WI' ,
'55021' : 'Columbia, WI' ,
'55023' : 'Crawford, WI' ,
'55027' : 'Dodge, WI' ,
'55029' : 'Door, WI' ,
'55033' : 'Dunn, WI' ,
'55037' : 'Florence, WI' ,
'55039' : 'Fond du Lac, WI' ,
'55041' : 'Forest, WI' ,
'55043' : 'Grant, WI' ,
'55045' : 'Green, WI' ,
'55047' : 'Green Lake, WI' ,
'55049' : 'Iowa, WI' ,
'55051' : 'Iron, WI' ,
'55053' : 'Jackson, WI' ,
'55055' : 'Jefferson, WI' ,
'55057' : 'Juneau, WI' ,
'55061' : 'Kewaunee, WI' ,
'55065' : 'Lafayette, WI' ,
'55067' : 'Langlade, WI' ,
'55069' : 'Lincoln, WI' ,
'55071' : 'Manitowoc, WI' ,
'55075' : 'Marinette, WI' ,
'55077' : 'Marquette, WI' ,
'55078' : 'Menominee, WI' ,
'55081' : 'Monroe, WI' ,
'55083' : 'Oconto, WI' ,
'55085' : 'Oneida, WI' ,
'55091' : 'Pepin, WI' ,
'55095' : 'Polk, WI' ,
'55097' : 'Portage, WI' ,
'55099' : 'Price, WI' ,
'55103' : 'Richland, WI' ,
'55107' : 'Rusk, WI' ,
'55111' : 'Sauk, WI' ,
'55113' : 'Sawyer, WI' ,
'55115' : 'Shawano, WI' ,
'55119' : 'Taylor, WI' ,
'55121' : 'Trempealeau, WI' ,
'55123' : 'Vernon, WI' ,
'55125' : 'Vilas, WI' ,
'55127' : 'Walworth, WI' ,
'55129' : 'Washburn, WI' ,
'55135' : 'Waupaca, WI' ,
'55137' : 'Waushara, WI' ,
'55141' : 'Wood, WI' ,
'54000' : 'WEST VIRGINIA' ,
'54001' : 'Barbour, WV' ,
'54005' : 'Boone, WV' ,
'54007' : 'Braxton, WV' ,
'54013' : 'Calhoun, WV' ,
'54015' : 'Clay, WV' ,
'54017' : 'Doddridge, WV' ,
'54019' : 'Fayette, WV' ,
'54021' : 'Gilmer, WV' ,
'54023' : 'Grant, WV' ,
'54025' : 'Greenbrier, WV' ,
'54027' : 'Hampshire, WV' ,
'54031' : 'Hardy, WV' ,
'54033' : 'Harrison, WV' ,
'54035' : 'Jackson, WV' ,
'54041' : 'Lewis, WV' ,
'54043' : 'Lincoln, WV' ,
'54045' : 'Logan, WV' ,
'54047' : 'McDowell, WV' ,
'54049' : 'Marion, WV' ,
'54053' : 'Mason, WV' ,
'54055' : 'Mercer, WV' ,
'54059' : 'Mingo, WV' ,
'54061' : 'Monongalia, WV' ,
'54063' : 'Monroe, WV' ,
'54065' : 'Morgan, WV' ,
'54067' : 'Nicholas, WV' ,
'54071' : 'Pendleton, WV' ,
'54073' : 'Pleasants, WV' ,
'54075' : 'Pocahontas, WV' ,
'54077' : 'Preston, WV' ,
'54081' : 'Raleigh, WV' ,
'54083' : 'Randolph, WV' ,
'54085' : 'Ritchie, WV' ,
'54087' : 'Roane, WV' ,
'54089' : 'Summers, WV' ,
'54091' : 'Taylor, WV' ,
'54093' : 'Tucker, WV' ,
'54095' : 'Tyler, WV' ,
'54097' : 'Upshur, WV' ,
'54101' : 'Webster, WV' ,
'54103' : 'Wetzel, WV' ,
'54105' : 'Wirt, WV' ,
'54109' : 'Wyoming, WV' ,
'56000' : 'WYOMING' ,
'56001' : 'Albany, WY' ,
'56003' : 'Big Horn, WY' ,
'56005' : 'Campbell, WY' ,
'56007' : 'Carbon, WY' ,
'56009' : 'Converse, WY' ,
'56011' : 'Crook, WY' ,
'56013' : 'Fremont, WY' ,
'56015' : 'Goshen, WY' ,
'56017' : 'Hot Springs, WY' ,
'56019' : 'Johnson, WY' ,
'56023' : 'Lincoln, WY' ,
'56027' : 'Niobrara, WY' ,
'56029' : 'Park, WY' ,
'56031' : 'Platte, WY' ,
'56033' : 'Sheridan, WY' ,
'56035' : 'Sublette, WY' ,
'56037' : 'Sweetwater, WY' ,
'56039' : 'Teton, WY' ,
'56041' : 'Uinta, WY' ,
'56043' : 'Washakie, WY' ,
'56045' : 'Weston, WY' ,
}

def fipsstate(fips,countyfp):
    tags = {}

    if not fips:
        tags['is_in'] = 'USA'
        tags['is_in:country'] = 'USA'
        tags['is_in:country_code'] = 'US'
        return tags

    if fips not in fipscodes:
        raise KeyError, 'missing FIPS code', fips

    state, statecode, isocode = fipscodes[fips]
    county_fips_code = fips + "" + countyfp
    county = county_fips[county_fips_code]
    tags["tiger:county"] = county
    tags["is_in:county"] = county

    tags["is_in"] =  'USA, '+state
    tags["is_in:state"] =  state
    tags["is_in:state_code"] = statecode
    tags["is_in:country_code"] = isocode

    if isocode == 'US':
        tags["is_in:iso_3166_2"] =  isocode+':'+statecode
        tags["is_in:country"] = "USA"

    else:
        # Reasonable to specify both here
        tags["is_in:country"] = 'USA;'+state

    return tags

def parse_shp_for_osm( filename ):
    #ogr.RegisterAll()

    dr = ogr.GetDriverByName("ESRI Shapefile")
    poDS = dr.Open( filename )

    if poDS == None:
        raise "Open failed."

    poLayer = poDS.GetLayer( 0 )

    poLayer.ResetReading()

    ret = []

    poFeature = poLayer.GetNextFeature()
    while poFeature:
        tags = {}
        
        # WAY ID
        tags[iSource + ":way_id"] = int( poFeature.GetField("TLID") )
        
	# FEATURE IDENTIFICATION
        mtfcc = poFeature.GetField("MTFCC");
        if mtfcc != None:

	    if mtfcc == "L4010":	#Pipeline
		tags["man_made"] = "pipeline"
	    if mtfcc == "L4020":	#Powerline
		tags["power"] = "line"
	    if mtfcc == "L4031":	#Aerial Tramway/Ski Lift
		tags["aerialway"] = "cable_car"
	    if mtfcc == "L4110":	#Fence Line
		tags["barrier"] = "fence"
	    if mtfcc == "L4125":	#Cliff/Escarpment
		tags["natural"] = "cliff"
	    if mtfcc == "L4165":	#Ferry Crossing
		tags["route"] = "ferry"
	    if mtfcc == "R1011":	#Railroad Feature (Main, Spur, or Yard)
		tags["railway"] = "rail"
		ttyp = poFeature.GetField("TTYP")
		if ttyp != None:
		    if ttyp == "S":
		        tags["service"] = "spur"
		    if ttyp == "Y":
		        tags["service"] = "yard"
	            tags["tiger:ttyp"] = ttyp
	    if mtfcc == "R1051":	#Carline, Streetcar Track, Monorail, Other Mass Transit Rail)
		tags["railway"] = "light_rail"
	    if mtfcc == "R1052":	#Cog Rail Line, Incline Rail Line, Tram
		tags["railway"] = "incline"
	    if mtfcc == "S1100":
		tags["highway"] = "primary"
	    if mtfcc == "S1200":
		tags["highway"] = "secondary"
	    if mtfcc == "S1400":
		tags["highway"] = "residential"
	    if mtfcc == "S1500":
		tags["highway"] = "track"
	    if mtfcc == "S1630":	#Ramp
		tags["highway"] = "motorway_link"
	    if mtfcc == "S1640":	#Service Drive usually along a limited access highway
		tags["highway"] = "service"
	    if mtfcc == "S1710":	#Walkway/Pedestrian Trail
		tags["highway"] = "path"
	    if mtfcc == "S1720":
		tags["highway"] = "steps"
	    if mtfcc == "S1730":	#Alley
		tags["highway"] = "service"
		tags["service"] = "alley"
	    if mtfcc == "S1740":	#Private Road for service vehicles (logging, oil, fields, ranches, etc.)
		tags["highway"] = "service"
		tags["access"] = "private"
	    if mtfcc == "S1750":	#Private Driveway
		tags["highway"] = "service"
		tags["access"] = "private"
		tags["service"] = "driveway"
	    if mtfcc == "S1780":	#Parking Lot Road
		tags["highway"] = "service"
		tags["service"] = "parking_aisle"
	    if mtfcc == "S1820":	#Bike Path or Trail
		tags["highway"] = "cycleway"
	    if mtfcc == "S1830":	#Bridle Path
		tags["highway"] = "bridleway"
	    tags["tiger:mtfcc"] = mtfcc

        # FEATURE NAME
        if poFeature.GetField("FULLNAME"):
            #capitalizes the first letter of each word
            name = poFeature.GetField( "FULLNAME" )
            tags["name"] = name

	    #Attempt to guess highway grade
	    if name[0:2] == "I-":
		tags["highway"] = "motorway"
	    if name[0:3] == "US ":
		tags["highway"] = "primary"
	    if name[0:3] == "US-":
		tags["highway"] = "primary"
	    if name[0:3] == "Hwy":
		if tags["highway"] != "primary":
		    tags["highway"] = "secondary"

        divroad = poFeature.GetField("DIVROAD")
        if divroad != None:
	    if divroad == "Y" and tags["highway"] == "residential":
                tags["highway"] = "tertiary"
            tags["tiger:separated"] = divroad

        statefp = poFeature.GetField("STATEFP")
        countyfp = poFeature.GetField("COUNTYFP")
        if (statefp != None) and (countyfp != None):
           tags.update( fipsstate(statefp, countyfp) )

        tlid = poFeature.GetField("TLID")
        if tlid != None:
            tags["tiger:tlid"] = tlid

        lfromadd = poFeature.GetField("LFROMADD")
        if lfromadd != None:
            tags["tiger:lfromadd"] = lfromadd

        rfromadd = poFeature.GetField("RFROMADD")
        if rfromadd != None:
            tags["tiger:rfromadd"] = rfromadd

        ltoadd = poFeature.GetField("LTOADD")
        if ltoadd != None:
            tags["tiger:ltoadd"] = ltoadd

        rtoadd = poFeature.GetField("RTOADD")
        if rtoadd != None:
            tags["tiger:rtoadd"] = rtoadd

        zipl = poFeature.GetField("ZIPL")
        if zipl != None:
            tags["tiger:zip_left"] = zipl

        zipr = poFeature.GetField("ZIPR")
        if zipr != None:
            tags["tiger:zip_right"] = zipr

        if mtfcc not in ignoremtfcc:
            # COPY DOWN THE GEOMETRY
            geom = []
            
            rawgeom = poFeature.GetGeometryRef()
            for i in range( rawgeom.GetPointCount() ):
                geom.append( (rawgeom.GetX(i), rawgeom.GetY(i)) )
    
            ret.append( (geom, tags) )
        poFeature = poLayer.GetNextFeature()
        
    return ret


# ====================================
# to do read .prj file for this data
# Change the Projcs_wkt to match your datas prj file.
# ====================================
projcs_wkt = \
"""GEOGCS["GCS_North_American_1983",
	DATUM["D_North_American_1983",
	SPHEROID["GRS_1980",6378137,298.257222101]],
	PRIMEM["Greenwich",0],
	UNIT["Degree",0.017453292519943295]]"""

from_proj = osr.SpatialReference()
from_proj.ImportFromWkt( projcs_wkt )

# output to WGS84
to_proj = osr.SpatialReference()
to_proj.SetWellKnownGeogCS( "EPSG:4326" )

tr = osr.CoordinateTransformation( from_proj, to_proj )

import math
def length(segment, nodelist):
    '''Returns the length (in feet) of a segment'''
    first = True
    distance = 0
    lat_feet = 364613  #The approximate number of feet in one degree of latitude
    for point in segment:
        pointid, (lat, lon) = nodelist[ round_point( point ) ]
        if first:
            first = False
	else:
	    #The approximate number of feet in one degree of longitute
            lrad = math.radians(lat)
            lon_feet = 365527.822 * math.cos(lrad) - 306.75853 * math.cos(3 * lrad) + 0.3937 * math.cos(5 * lrad)
	    distance += math.sqrt(((lat - previous[0])*lat_feet)**2 + ((lon - previous[1])*lon_feet)**2)
	previous = (lat, lon)
    return distance

def addressways(waylist, nodelist, first_id):
    id = first_id
    awaylist = {}
    lat_feet = 364613  #The approximate number of feet in one degree of latitude
    distance = float(address_distance)
    ret = []
    ret.append( "<?xml version='1.0' encoding='UTF-8'?>" )
    ret.append( "<osm version='0.6' generator='shape_to_osm.py'>" )

    for waykey, segments in waylist.iteritems():
        waykey = dict(waykey)
        rsegments = []
        lsegments = []
        for segment in segments:
            lsegment = []
            rsegment = []
            lastpoint = None

	    #Don't pull back the ends of very short ways too much
	    seglength = length(segment, nodelist)
	    if seglength < float(address_pullback) * 3.0:
		pullback = seglength / 3.0
	    else:
                pullback = float(address_pullback)
            if "tiger:lfromadd" in waykey:
                lfromadd = waykey["tiger:lfromadd"]
            else:
                lfromadd = None
            if "tiger:ltoadd" in waykey:
                ltoadd = waykey["tiger:ltoadd"]
            else:
                ltoadd = None
            if "tiger:rfromadd" in waykey:
                rfromadd = waykey["tiger:rfromadd"]
            else: 
                rfromadd = None
            if "tiger:rtoadd" in waykey:
                rtoadd = waykey["tiger:rtoadd"]
            else:
		rtoadd = None
            if rfromadd != None and rtoadd != None:
                right = True
	    else:
		right = False
            if lfromadd != None and ltoadd != None:
                left = True
	    else:
		left = False
            if left or right:
		first = True
                firstpointid, firstpoint = nodelist[ round_point( segment[0] ) ]

                finalpointid, finalpoint = nodelist[ round_point( segment[len(segment) - 1] ) ]
                for point in segment:
                    pointid, (lat, lon) = nodelist[ round_point( point ) ]

		    #The approximate number of feet in one degree of longitute
                    lrad = math.radians(lat)
                    lon_feet = 365527.822 * math.cos(lrad) - 306.75853 * math.cos(3 * lrad) + 0.3937 * math.cos(5 * lrad)

#Calculate the points of the offset ways
                    if lastpoint != None:
		        #Skip points too close to start
			if math.sqrt((lat * lat_feet - firstpoint[0] * lat_feet)**2 + (lon * lon_feet - firstpoint[1] * lon_feet)**2) < pullback:
			    #Preserve very short ways (but will be rendered backwards)
			    if pointid != finalpointid:
			        continue
		        #Skip points too close to end
			if math.sqrt((lat * lat_feet - finalpoint[0] * lat_feet)**2 + (lon * lon_feet - finalpoint[1] * lon_feet)**2) < pullback:
			    #Preserve very short ways (but will be rendered backwards)
			    if (pointid != firstpointid) and (pointid != finalpointid):
			        continue

                        X = (lon - lastpoint[1]) * lon_feet
		        Y = (lat - lastpoint[0]) * lat_feet
                        if Y != 0:
		            theta = math.pi/2 - math.atan( X / Y)
		            Xp = math.sin(theta) * distance
		            Yp = math.cos(theta) * distance
                        else:
                            Xp = 0
			    if X > 0:
                                Yp = -distance
			    else:
                                Yp = distance

			if Y > 0:
			    Xp = -Xp
			else:
			    Yp = -Yp
				
			if first:
			    first = False
			    dX =  - (Yp * (pullback / distance)) / lon_feet #Pull back the first point
			    dY = (Xp * (pullback / distance)) / lat_feet
			    if left:
                                lpoint = (lastpoint[0] + (Yp / lat_feet) - dY, lastpoint[1] + (Xp / lon_feet) - dX)
                                lsegment.append( (id, lpoint) )
			        id += 1
			    if right:
                                rpoint = (lastpoint[0] - (Yp / lat_feet) - dY, lastpoint[1] - (Xp / lon_feet) - dX)
                                rsegment.append( (id, rpoint) )
			        id += 1

			else:
			    #round the curves
			    if delta[1] != 0:
			        theta = abs(math.atan(delta[0] / delta[1]))
			    else:
				theta = math.pi / 2
			    if Xp != 0:
				theta = theta - abs(math.atan(Yp / Xp))
			    else: theta = theta - math.pi / 2
			    r = 1 + abs(math.tan(theta/2))
			    if left:
				lpoint = (lastpoint[0] + (Yp + delta[0]) * r / (lat_feet * 2), lastpoint[1] + (Xp + delta[1]) * r / (lon_feet * 2))
                                lsegment.append( (id, lpoint) )
                                id += 1
			    if right:
                                rpoint = (lastpoint[0] - (Yp + delta[0]) * r / (lat_feet * 2), lastpoint[1] - (Xp + delta[1]) * r / (lon_feet * 2))
				
                                rsegment.append( (id, rpoint) )
                                id += 1

                        delta = (Yp, Xp)

                    lastpoint = (lat, lon)


#Add in the last node
	        dX =  - (Yp * (pullback / distance)) / lon_feet
	        dY = (Xp * (pullback / distance)) / lat_feet
		if left:
                    lpoint = (lastpoint[0] + (Yp + delta[0]) / (lat_feet * 2) + dY, lastpoint[1] + (Xp + delta[1]) / (lon_feet * 2) + dX )
                    lsegment.append( (id, lpoint) )
                    id += 1
		if right:
                    rpoint = (lastpoint[0] - Yp / lat_feet + dY, lastpoint[1] - Xp / lon_feet + dX)
                    rsegment.append( (id, rpoint) )
                    id += 1

#Generate the tags for ways and nodes
		rtags = []
		ltags = []
		tags = []
    		if "tiger:zip_right" in waykey:
		    zipr = waykey["tiger:zip_right"]
                    rtags.append( "<tag k=\"addr:postcode\" v=\"%s\" />" % zipr )
		if "tiger:zip_left" in waykey:
		    zipl = waykey["tiger:zip_left"]
                    ltags.append( "<tag k=\"addr:postcode\" v=\"%s\" />" % zipl )
                if "name" in waykey:
                    name = waykey["name"]
                    tags.append( "<tag k=\"addr:street\" v=\"%s\" />" % name )
		if "is_in:state" in waykey:
		    state = waykey["is_in:state"]
                    tags.append( "<tag k=\"addr:state\" v=\"%s\" />" % state )
		if "tiger:county" in waykey:
		    county = waykey["tiger:county"]
                    tags.append( "<tag k=\"addr:county\" v=\"%s\" />" % county )
		if "is_in:country_code" in waykey:
		    country = waykey["is_in:country_code"]
                    tags.append( "<tag k=\"addr:country\" v=\"%s\" />" % country )
		if "tiger:separated" in waykey:
		    separated = waykey["tiger:separated"]
		else:
		    separated = "N"
		ltags.extend(tags)
		rtags.extend(tags)

#Write the nodes of the offset ways
		if right:
                    first = True
                    for i, point in rsegment:
                        if not first:
			    ret.append( "</node>" )
		        ret.append( "<node id='-%d' action='create' visible='true' lat='%f' lon='%f' >" % (i, point[0], point[1] ) )
	    	        if first:
                            ret.append( "<tag k=\"addr:housenumber\" v=\"%s\" />" % rfromadd )
			    ret.extend(rtags)
                            first = False
                    ret.append( "<tag k=\"addr:housenumber\" v=\"%s\" />" % rtoadd )
		    ret.extend(rtags)
		    ret.append( "</node>" )
		if left:
                    first = True
                    for i, point in lsegment:
                        if not first:
			    ret.append( "</node>" )
		        ret.append( "<node id='-%d' action='create' visible='true' lat='%f' lon='%f' >" % (i, point[0], point[1] ) )
	    	        if first:
                            ret.append( "<tag k=\"addr:housenumber\" v=\"%s\" />" % lfromadd )
			    ret.extend(ltags)
                            first = False
                    ret.append( "<tag k=\"addr:housenumber\" v=\"%s\" />" % ltoadd )
		    ret.extend(ltags)
		    ret.append( "</node>" )
		if right:
                    rsegments.append( rsegment )
		if left:
                    lsegments.append( lsegment )
		rtofromint = right	#Do the addresses convert to integers?
		ltofromint = left	#Do the addresses convert to integers?
		if right:
		    try: rfromint = int(rfromadd)
		    except:
		        print("Non integer address: %s" % rfromadd)
		        rtofromint = False
		    try: rtoint = int(rtoadd)
		    except:
		        print("Non integer address: %s" % rtoadd)
		        rtofromint = False
		if left:
		    try: lfromint = int(lfromadd)
		    except:
		        print("Non integer address: %s" % lfromadd)
		        ltofromint = False
		    try: ltoint = int(ltoadd)
		    except:
		        print("Non integer address: %s" % ltoadd)
		        ltofromint = False
    	        import_guid = time.strftime( '%Y%m%d%H%M%S' )
	        if right:
		    ret.append( "<way id='-%d' action='create' visible='true'> " % id)
		    id += 1
                    for rsegment in rsegments:
                        for point in rsegment:
                            ret.append( "<nd ref='-%d' /> " % point[0])

		    if rtofromint:
                        if (rfromint % 2) == 0 and (rtoint % 2) == 0:
			    if separated == "Y":	#Doesn't matter if there is another side
                                ret.append( "<tag k=\"addr:interpolation\" v=\"even\" />" )
			    elif ltofromint and (lfromint % 2) == 1 and (ltoint % 2) == 1:
                                ret.append( "<tag k=\"addr:interpolation\" v=\"even\" />" )
			    else:
                                ret.append( "<tag k=\"addr:interpolation\" v=\"all\" />" )
                        elif (rfromint % 2) == 1 and (rtoint % 2) == 1:
			    if separated == "Y":	#Doesn't matter if there is another side
                                ret.append( "<tag k=\"addr:interpolation\" v=\"odd\" />" )
			    elif ltofromint and (lfromint % 2) == 0 and (ltoint % 2) == 0:
                                ret.append( "<tag k=\"addr:interpolation\" v=\"odd\" />" )
			    else:
                                ret.append( "<tag k=\"addr:interpolation\" v=\"all\" />" )
			else:
                            ret.append( "<tag k=\"addr:interpolation\" v=\"all\" />" )
		    else:
                        ret.append( "<tag k=\"addr:interpolation\" v=\"all\" />" )
		    ret.extend(rtags)
                    ret.append( "<tag k=\"source\" v=\"%s_import_v%s_%s\" />" % (iSource, VERSION, import_guid) )
                    ret.append( "<tag k=\"attribution\" v=\"%s\" />" % (iAttrib) )
                    ret.append( "</way>" )
		if left:
		    ret.append( "<way id='-%d' action='create' visible='true'> " % id)
		    id += 1
                    for lsegment in lsegments:
                        for point in lsegment:
                            ret.append( "<nd ref='-%d' /> " % point[0])
		    if ltofromint:
                        if (lfromint % 2) == 0 and (ltoint % 2) == 0:
			    if separated == "Y":
                                ret.append( "<tag k=\"addr:interpolation\" v=\"even\" />" )
			    elif rtofromint and (rfromint % 2) == 1 and (rtoint % 2) == 1:
                                ret.append( "<tag k=\"addr:interpolation\" v=\"even\" />" )
			    else:
                        	ret.append( "<tag k=\"addr:interpolation\" v=\"all\" />" )

                        elif (lfromint % 2) == 1 and (ltoint % 2) == 1:
			    if separated == "Y":
                                ret.append( "<tag k=\"addr:interpolation\" v=\"odd\" />" )
			    elif rtofromint and (rfromint %2 ) == 0 and (rtoint % 2) == 0:
                                ret.append( "<tag k=\"addr:interpolation\" v=\"odd\" />" )
			    else:
                                ret.append( "<tag k=\"addr:interpolation\" v=\"all\" />" )
			else:
                            ret.append( "<tag k=\"addr:interpolation\" v=\"all\" />" )
		    else:
                        ret.append( "<tag k=\"addr:interpolation\" v=\"all\" />" )
		    ret.extend(ltags)
                    ret.append( "<tag k=\"source\" v=\"%s_import_v%s_%s\" />" % (iSource, VERSION, import_guid) )
                    ret.append( "<tag k=\"attribution\" v=\"%s\" />" % (iAttrib) )
                    ret.append( "</way>" )

    ret.append( "</osm>" )
    return ret

def unproject( point ):
    pt = tr.TransformPoint( point[0], point[1] )
    return (pt[1], pt[0])

def round_point( point, accuracy=8 ):
    return tuple( [ round(x,accuracy) for x in point ] )

def compile_nodelist( parsed_gisdata, first_id=1 ):
    nodelist = {}
    
    i = first_id
    for geom, tags in parsed_gisdata:
        if len( geom )==0:
            continue
        
        for point in geom:
            r_point = round_point( point )
            if r_point not in nodelist:
                nodelist[ r_point ] = (i, unproject( point ))
                i += 1
            
    return (i, nodelist)

def adjacent( left, right ):
    left_left = round_point(left[0])
    left_right = round_point(left[-1])
    right_left = round_point(right[0])
    right_right = round_point(right[-1])
    
    return ( left_left == right_left or
             left_left == right_right or
             left_right == right_left or
             left_right == right_right )
             
def glom( left, right ):
    
    left = list( left )
    right = list( right )
    
    left_left = round_point(left[0])
    left_right = round_point(left[-1])
    right_left = round_point(right[0])
    right_right = round_point(right[-1])
    
    if left_left == right_left:
        left.reverse()
        return left[0:-1] + right
        
    if left_left == right_right:
        return right[0:-1] + left
        
    if left_right == right_left:
        return left[0:-1] + right
        
    if left_right == right_right:
        right.reverse()
        return left[0:-1] + right
        
    raise 'segments are not adjacent'

def glom_once( segments ):
    if len(segments)==0:
        return segments
    
    unsorted = list( segments )
    x = unsorted.pop(0)
    
    while len( unsorted ) > 0:
        n = len( unsorted )
        
        for i in range(0, n):
            y = unsorted[i]
            if adjacent( x, y ):
                y = unsorted.pop(i)
                x = glom( x, y )
                break
                
        # Sorted and unsorted lists have no adjacent segments
        if len( unsorted ) == n:
            break
            
    return x, unsorted
    
def glom_all( segments ):
    unsorted = segments
    chunks = []
    
    while unsorted != []:
        chunk, unsorted = glom_once( unsorted )
        chunks.append( chunk )
        
    return chunks
        
                

def compile_waylist( parsed_gisdata, blank_way_id ):
    waylist = {}
    
    #Group by iSource:way_id
    for geom, tags in parsed_gisdata:
        way_key = tags.copy()
        way_key = ( way_key[iSource + ':way_id'], tuple( [(k,v) for k,v in way_key.iteritems()] ) )
        
        if way_key not in waylist:
            waylist[way_key] = []
            
        waylist[way_key].append( geom )
    
    ret = {}
    for (way_id, way_key), segments in waylist.iteritems():
        
        if way_id != blank_way_id:
            ret[way_key] = glom_all( segments )
        else:
            ret[way_key] = segments
        
    return ret
            

import time
from xml.sax.saxutils import escape
def shape_to_osm( shp_filename, base_filename, blank_way_id ):
    
    import_guid = time.strftime( '%Y%m%d%H%M%S' )

    print "parsing shpfile"
    parsed_features = parse_shp_for_osm( shp_filename )
    
    print "compiling nodelist"
    i, nodelist = compile_nodelist( parsed_features )
    
    print "compiling waylist"
    waylist = compile_waylist( parsed_features, blank_way_id )

    filenumber = 1
    objectcount = 0
    seen = {}

    print "preparing address ways"
    ret = addressways(waylist, nodelist, i)
    osm_filename = "%s%d.osm" % (base_filename, filenumber)
    print "writing %s" %osm_filename
    fp = open( osm_filename, "w" )
    fp.write( "\n".join( ret ) )
    fp.close()
    filenumber += 1

    
    print "constructing osm xml file"
    ret = []
    ret.append( "<?xml version='1.0' encoding='UTF-8'?>" )
    ret.append( "<osm version='0.6' generator='shape_to_osm.py'>" )
    
    for waykey, segments in waylist.iteritems():
        for segment in segments:
	    #write the nodes
            for point in segment:
                id, (lat, lon) = nodelist[ round_point( point ) ]
                if id not in seen:
                    seen[id] = True
		    #write node
                    ret.append( "  <node id='-%d' action='create' visible='true' lat='%f' lon='%f' >" % (id, lat, lon) )
                    ret.append( "  </node>" )
                    objectcount += 1
                else:
                    pass
                    #print "Skipping node %d" %id

	    #write the way
            ret.append( "  <way id='-%d' action='create' visible='true'>" % i )
            
            ids = [ nodelist[ round_point( point ) ][0] for point in segment ]

            count = 0
            for id in ids:
                count += 1
                ret.append( "    <nd ref='-%d' />" % id )
                if (count % Max_Waylength == 0) and (count != len(ids)):	#Split the way
                    for k, v in waykey:
                        ret.append( "    <tag k=\"%s\" v=\"%s\" />" % (k, escape(str(v))) )
                    ret.append( "    <tag k=\"source\" v=\"%s_import_v%s_%s\" />" % (iSource, VERSION, import_guid) )
                    ret.append( "    <tag k=\"attribution\" v=\"%s\" />" % (iAttrib) )
                
                    ret.append( "  </way>" )
                    objectcount += 1
		    i += 1
                    ret.append( "  <way id='-%d' action='create' visible='true'>" % i )
                    ret.append( "    <nd ref='-%d' />" % id )
                
            for k, v in waykey:
                ret.append( "    <tag k=\"%s\" v=\"%s\" />" % (k, escape(str(v))) )
            ret.append( "    <tag k=\"source\" v=\"%s_import_v%s_%s\" />" % (iSource, VERSION, import_guid) )
            ret.append( "    <tag k=\"attribution\" v=\"%s\" />" % (iAttrib) )
                
            ret.append( "  </way>" )
            objectcount += 1
            
            i += 1

            if objectcount > maxNodes:	#Write a file
                ret.append( "</osm>" )
                osm_filename = "%s%d.osm" % (base_filename, filenumber)
                print "writing %s" %osm_filename
                fp = open( osm_filename, "w" )
                fp.write( "\n".join( ret ) )
                fp.close()

                objectcount = 0
                filenumber += 1
                seen = {}
                ret = []
                ret.append( "<?xml version='1.0' encoding='UTF-8'?>" )
                ret.append( "<osm version='0.6' generator='shape_to_osm.py'>" )
        
    ret.append( "</osm>" )
    
    osm_filename = "%s%d.osm" % (base_filename, filenumber)
    print "writing %s" %osm_filename
    fp = open( osm_filename, "w" )
    fp.write( "\n".join( ret ) )
    fp.close()
    
if __name__ == '__main__':
    import sys, os.path
    if len(sys.argv) < 2:
        print "%s filename.shp [filename.osm]" % sys.argv[0]
        sys.exit()
    shape = sys.argv[1]
    if len(sys.argv) > 2:
        osm = sys.argv[2]
    else:
        osm = shape[0:-4] + ".osm" 
    id = "1.shp"
	# Left over from massGIS unknown usage, but works fine hardcoded to "1.shp" which was the valu on a test of the actual mass data,
	#id = os.path.basename(shape).split("_")[-1]
    shape_to_osm( shape, osm, id )
