Readme.txt - canvec-to-osm

*****
THANK YOU FOR READING THIS FILE :)
*****
The canvec-to-osm script is designed to convert the Natural Resources Canada - Geogratis 
dataset called 'CanVec' which contains about 99 features and 397 sub-features each containing
point/line/area and multi-polyon.

The chart which contains the details conversion (OSM tags that match with the shp file fields)
Is available on the GoogleDocs spreadsheet.

All members of the talk-ca discussion list are welcome to have editing access to this chart 
they, just need to ask.

http://spreadsheets.google.com/pub?key=rP0soJiyFhapKZbRLYZ74zA&output=html



We use the GoogleDoc chart instead of the OSM wiki because it's 379 features that all are 
dealt with, and it's easier to maintain.  Also, the final verison of the tags are in the rulesTXT
folder, (zip file) so changes to the chart needs to be the same as the RulesTXT folder
in order for this script to work.



****************************
***** IMPORTANT TO SEE *****
****************************

The purpose of THIS script is to convert the shp files for all the other features accept
for the ones that ae being dealth with with other means.  Namely:
type - 
P - point 
L - line 
A - area

ENTITIES					Type 	CanVec-to-osm 	canvec2osm.py   GeoBaseNHN	GeoBaseNRN  	other  
						
1- Amusement park - (Parc d'amusement)		A	Yes
2- Auto wrecker - (Récupérateur)		A	Yes
3- Blocked passage - (Passage obstrué)		P	some						maybe		some maybe not applicable
4- Botanical garden - (Jardin botanique)	A	Yes
5- Building - (Bâtiment)			PA	some								buildings are sub-divided where the points that contain no other attribute besides building=yes are removed, however all the Areas are available... BUT doesn't totally work, and needs to be manually removed.
6- Camp - (Camp)				P	Yes
7- Campground - (Terrain de camping)		PA	Yes
8- Cemetery - (Cimetière)			PA	yes
9- Chimney - (Cheminée)				P	yes
10- Coastal water - (Eau côtière)		A	yes				not available
11- Contour - (Courbe de niveau)		L	No								contours arent needed in OSM, but can be used to make the garmin maps.  So there not converted
12- Contour imperial - (Courbe de niveau 	L	No								same as above
13- Cross - (Croix)				P	Yes
14- Cut line - (Percée)				L	Yes
15- Designated area - (Aire désignée)		PLA	Yes
16- Domestic waste - (Déchet domestique)	A	Yes
17- Drive-in theatre - (Ciné-parc)		PA	Yes
18- Elevation point - (Point d'élévation)	P	Yes
19- Elevation point imperial - (Point 		P	yes	
20- Esker - (Esker)				L	Yes
21- Exhibition ground - (Terrain d'exposition)	A	Yes
22- Extraction area - (Zone d'extraction)	PA	Yes
23- Ferry connection segment - (Segment de	L	Yes						wasn't used	This wasnt converted when geobase2osm was run, so caution (as always) is needed when copying this feature in.
24- Footbridge - (Passerelle)			L	Yes		
25- Fort - (Fort)				A	Yes	
26- Gas and oil facilities - (Installations 	PA	Yes
27- Glacial debris undifferentiated - (Débris 	A	Yes
28- Golf course - (Terrain de golf)		A	Yes
29- Golf driving range - (Terrain 		PA	Yes
30- Historic site/Point of interest - (Lieu 	P	yes
31- Hydrographic obstacle entity -		PLA	some		please		some				This one is available as converted, but some features are also available
32- Industrial and commercial area - (Zone	PA	Yes
33- Industrial solid depot - (			PA	Yes
34- Island - (Île)				A	Yes		please		yes				the direction of the ways need to be reversed, and is also available in geobaseNHN (but identical data)
35- Junction - (Jonction)			P	some						yes		some are available and listed as 'extra'
36- Landform - (Forme terrestre)		A	yes
37- Lookout - (Belvédère)			PA	yes
38- Lumber yard - (Cour à bois)			A	yes
39- Manmade hydrographic entity - (Entité 	PLA	yes		please		yes				Some are available in geobaseNHN, but there all the same in canvec
40- Marina - (Marina)				p	yes
41- Mine - (Mine)				PA	yes	
42- Mining area - (Zone d'extraction de		P	yes
43- Moraine - (Moraine)				A	yes
44- Named feature - (Entité nommée)		PLA	yes but				some				Because some of the features are available in the other datasets, these are listed as 'extra'
45- Navigational aid - (Repère de navigation)	P	yes
46- NTS50K boundary polygon - (Polygone 	A	yes	 							this is the bounding box that can be used to see where the extents of the area that your looking at.  (not to be uploaded to osm, of course)
47- Palsa bog - (Tourbière de palse)		A	yes
48- Parabolic antenna - (Antenne parabolique)	P	yes
49- Park/Sports field - (Parc/terrain de	A	yes
50- Peat cutting - (Tourbière)			A	yes
51- Permanent snow and ice - (Neige et glace	A	yes
52- Picnic site - (Terrain de pique-nique)	PA	yes
53- Pingo - (Pingo)				P	yes
54- Pipeline - (Pipeline)			L	yes
55- Pipeline (Sewage/liquid waste)		L	yes
56- Pit - (Banc)				A	yes
57- Power transmission line - (Ligne de		L	yes
58- Quarry - (Carrière)				A	yes
59- Railway - (Chemin de fer)			L	yes
60- Residential area - (Zone résidentielle)	A	yes
61- Road segment - (Segment routier)		L       nope						yes		these are available in geobase and converted geobase2osm.py, so its not needed here.
62- Ruins - (Ruines)				PA	yes
63- Runway - (Piste d'envol)			PAq	yes
64- Sand - (Sable)				A	Yes
65- Saturated soil - (Sol saturé)		A	yes
66- Shrine - (Lieu de pèlerinage)		P	yes
67- Silo - (Silo)				P	yes
68- Single line watercourse - (Cours d'eau	L	no		please		yes but				These are available in GeoBaseNHN.. as well as the (LNFLOW which shows the direction of the water) however, other than that it's fine to include with this script. (names are in french and english)
69- Ski centre - (Centre de ski)		P	yes
70- Sports track/Race track - (Piste de course)	LA	yes
71- Stadium - (Stade)				A	yes
72- String bog - (Tourbière réticulée)		A	yes
73- Tank - (Citerne)				PA	yes
74- Toll point - (Poste de péage)		P	yes						yes		These need some work.. 	
75- Tower - (Tour)				p	yes	
76- Trail - (Sentier)				l	yes								some in ontario might also be available fromLIO, but the canvec feature can be imported 1st.
77- Transformer station - (Poste de		PA	yes
78- Transmission line - (Ligne			L	yes
79- Tundra polygon - (Sols polygonaux)		A	yes
80- Tundra pond - (Étang de toundra)		A	yes
81- Turntable - (Plaque tournante)		P	yes
82- Underground reservoir - (Réservoir		pA	yes
83- Valve - (Valve)				P	yes
84- Wall/Fence - (Mur/clôture)			L	yes
85- Waterbody - (Région hydrique)		A	no		please		yes				because it  includes inner/outer, it's best to be dealth with, using python 
86- Well - (Puits)				p	yes	
87- Wetland - (Terre humide)			A	yes
88- Wind-operated device - (Éolienne)		P	yes
89- Wooded area - (Région boisée)		A	no		please						because it includes  inner/outer
90- Zoo - (Zoo)					A	yes



We keep track of the whole import progress with a GoogleDoc chart also, and you can have editing access to it
just by asking on the talk-ca list.

http://spreadsheets.google.com/pub?key=tmY7V02fBT8C8vimCf8ioXg&output=html



This is the readme.txt file for the Natural Resources Canada, 'CanVec' dataset, provided
by GeoGratis.ca, the data is provided with the 'Unristricted End-user licence agreement,
which states that basicially, we can do whatever we want with the data, as long as we include the note that it 'came from Natural Resources Canada', so we have the tag
"attribution=Natural Resources Canada", which gets added on to every point,line, area feature
that is available to use.

This script is a 'DATA CONVERTION' script, and is NOT a 'DATA IMPORT' script.  Th

*******
re: CanVec Code retention

The CanVec code, is used as an indicator for searching for specific geographical features within the Natural Resources Canada dataset.   This  is a way to find all the similar features (each with it's own unique UUID & OSM reference number), and to change the key/value of each as potentiality needed.   For example, if it has been found that there is a better way to represent the map feature canvec:CODE=200041 where currently (example not real) it says natural=water, then it should be natural=stream.   This change should only effect those feature of 200041 as through out the Canada data its referred to as the same thing.   There is also another canvec:CODE which refers to 'lakes', so those tags do NOT need to be changed. (as it's a different feature, with a different CODE)

HOWEVER, over time, once the source canvec data is known that it will no longer change how it lists the data, then this feature could be removed from the OSM database.   But until then, the canvec:CODE is used as a reference back to the database to know exactly where this particular feature came from.   (in example, the roads for GeobaseNRN) would just be 1 of the many CODES, as canvec is a database collection of many different map features, where GeoBase Roads is just roads (and different types of roads features).


*******
re: UUID retention

The UUID or (Universal Unique Identifier) is a alphanumeric code (for example bb02c686968311d99c9f000ea65e52d8) which is attached onto each of the map features, to give it an identification within the source data.

Similarly to OpenStreetMap, where every node, way & polygon also has it's own ID which is automatically generated as each node; way; polygon is created. For example "Node: 452814802".  This is used to give identification to the data, and also to compare old with new data sets.  Ie. Comparing a back-up copy of an OSM file with the current OSM data.

Although the UUID has no DIRECT actual significant use in the OpenStreetMap it does however, value for the source datasets.  

The primary purpose of the UUID is for use in comparison, when deciding on what new data (from the same datasource) should be used to help improve OpenStreetMap, based on new available data.

It is however noted, that any changes in the OSM database when are done after the initial import.   These changes are to be RETAINED, and only with DIRECT consent by the original contributor can they be modified.   OR if the modifications to the map would be a 'significant' map improvement, (just as in a local mapper doesn't need to contact everyone for all changes, as long as the 'spirit of OSM' is retained, that is, to make the map better.

Because of the known fact that a VAST MAJORITY of the geographical area of Canada, will largely remain either totally untouched, or just extra map features added to osm, retaining the UUID, would be of help when looking at new data available from that same source (in this case, Natural Resources Canada)

Although (at this time) the exact method of comparing existing OSM data which has been largely untouched, has not been directly explained with an example.   The decision needs to be made in advance, (as to retain the UUID or not).

SINCE, the ONLY current method (possibility) to compare future datasets, is with a DIRECT COMPARISON of geographic locations of the map features.   Using OpenJUMP AutoMatch feature, this method is possible, where the results are manually reviewed as errors are highlighted.

CONSIDERING that this is a one-to-many ratio (where it's only 1 person who is actually doing the conversion, and only a few people who are doing the vast majority of the 'copying in' and with a large number of people who are viewing the available .osm files and adding features in locally.

THEREFORE, it is decided that the next revision of the (Natural Resources Canada, GeoGratis product of CanVec) for the canvec-to-osm.bat script (as well as the other Natural Resources Canada products from GeoBase products (geobaseNHN-to-osm), and future scripts will include the UUID tags.
 
The tags a prefixed with the source
geobase:UUID=bb02c686968311d99c9f000ea65e52d8
and
canvec:uuid=bb02c686968311d99c9f000ea65e52d8 '

AND THEREFORE, ONCE, it is found that the updates that are available from the datasource are small enough that these 'DIFF' files the list of features to remove, along with the list of features to add), and with confirmation from the datasourse provider, that the updates will be small, THEN the updates can be done manually, and the UUID tags can be removed.

*******

re: survay points and mountain peakes.  

The mountain peaks are found in the named_features set
and do not include the elevation.  So this is a manual process that needs to be done.  
for those survay points that are directly on the named feature, the tag can be added. Making a purge.

e
*******

re: in correct tagging from rules.txt files

If you find any tagging that needs to be fixed, or have questions about why something is tagged
a certain way.  PLEASE ask me acrosscanadatrails@gmail.com and corrections can be made.
With an update to the conversion script.



*******

re: Extra folder

The zip file contains an 'extra' folder, these are files that dont need to be copied, but are
there for reference, and some of it can be copied if you like.
*** these are now prefixed with 'EXTRA_' to indicate that they shouldn't be copied in just yet, and another method is 
in the works

*******

re: Origional shp .zip file

The origional shp .zip file is copied and saved in this zip package, as it's available so then
the canvec script can be run again, as new 'rules.txt' files are made. And so, they can be used
for whatever you like.  Mainly as a 'source' for double-checking the accuracy.

*******

re: what canvec contains / not contains

The script is designed to convert most of the features from the Natural
Resources Canada - CanVec Dataset.  MOST, but not all.  Since other features are available
from other datasets, ie. Geobase some features got omited, in favour of using geobase.
So there are a few things to note.

*******

re: single line watercourse

The water features such as 'waterbody' and 'single line watercourse'
are also available on the GeoBase National Hydrgraphy Dataset NHN.
Using GeoBaseNHN2osm will convert that data.

The feature of 'LN flow' is not available in Canvec, but IS available from geobaseNHN.
Also, the interections of the water flows dont connect. Thus, duplicate nodes are created to form that intersection.


*******
re: contour lines

Features such as Contour Lines, are not included, this is because
the OpenStreetMap project already uses contour lines derrived from NASA
SRTM data.  This data however, will be used, but only for the creation
of Garmin GPS Map. These SHP files are available in the 'extra' folder,
as well as the .osm files for the creation of the osmAtlas which would need it.

*******
re: Roads
As these were imported from GeoBase2osm script for the National
Road Network, they arent converted with this script. .. HOWEVER, if it does get converted,
then it would show up as 'sticks' that are not connected to other roads. .. So importing them
would not help, since manual work is needed to fix that.  The geobase2osm script handles that
already.  Therefore, it's not needed with this conversion script.


*******
re: NTS Boundary polygon

The NTS Boundary polygon. This is helpful to the user as it shows
the 'working area' so then you can download the whole OSM data for that area.
But please DONT upload it to OSM, it's not needed.


*******
re: Simple building nodes

The 'Unknown' and 'Other' Buildings, nodes are not included. This is because
for the OpenStreetMap project, listing the fact that there is a building
is not good enough.  The building needs to have a function for map users to
use it, otherwise its just 'clutter' .  Kindof like indicating where every
tree is in 'wooded area', the landuse area shows if it's residnetial or commercial
and is good enough.


*******
re: named_features.osm file

For the TO_1580009_012_Named_featureRULES.txt file, its listed in the 'extra' folder since
there is extra work needed.
The user needs to only import the following features.
Search for "*=110" which is PARK Conservation area, and copy those.
... but the park boundaries files might become available, if it doesn't have names then sure,
but otherwise it's not needed.

Places:  for some reason, the script likes to add in the 'place' tag, so they need to be removed.
It's a mistery why these get in there.

*=280" which is MTN Mountain natural=peak. remove tag'place=unincorporated_area'

remove the 'canvec:CODE=1580010:120' features which is natural=river and the name of the river
this info will be back when the GeoBaseNHN2osm will be imported.


search 350, and remove the "place=major_municipality" tag.

 '290' (VALL Valley) and remove the 'place=indian_reserve'
.. again it looks at the '90' feature that uses that.

 '280' (MTN Mountain) and remove the 'place=unincorporated_area' tag, which
shows up for some reason (i dont know why it looks at the '80' feater and uses that.

'250' (SHL Shoal) and remove the 'place=major_municipality'
which looks at the '25' that uses that.

'230' (CAPE Cape) and remove the 'place=village' which looks
at the '30' which uses that.

'220' (BAY Bay) and remove the 'place=town' tag that '20' uses.

'200'  remove the place=town that the '20' uses (search *=200)

'150' (LAKE Lake) and remove the 'place=major_municipality'
which looks at the '50' that uses that.

'140' (FALL Falls) and remove the 'place=hamlet' tag which looks
at the '40' that uses that.

115 and remove place=state

130 RIVF River feature remove the 'place=village' which '30' uses.

120 RIV River ... it also uses '20', but is included in the geobaseNHN dataset
as a line.

'110' PARK Conservation area and remove the place=city tag

'80' or 080' UNP Unincorporated area.  odd are that this place tag
has already been imported. so it's best to remove it.

280 - 280 MTN Mountain
 the natural=peak
and
110 PARK Conservation area
 the leisure=nature_reserve, where the boundary tag will get added once the polygon is availabe.

re: 90 IR Indian Reserve w
These features may be included in the geobase dataset, so they should be omited here.

******

re: trails & railways

For the trails and railways, the entire rail network was already done and available in canvec.  

Bridges might not have been mapped by local mappers, so the best way to deal with it is to
is simply slice the ways where the other mapper stopped. And add in the bridges.

For accuracy, there is no guartee of which version is better. That's because the accuracy
of the rail network is about 10meters, where an off the shelf GPS can do better.
The main thing is to only adjust the track to fit where the bridge is, and to download the local GPS
track layer and get a median.

When someone else travels that track they will be able to further define it.

******

re: Islands

The coastal islands have the natural=coastline tag, the ways need to be reversed to go counter-clockwise
otherwise it wont render right.


****
re: 16 tile area

The script handles all 16 tiles in a ###x area, so i will be processing the major cities soon,
and on request.   It saves the origional .shp files, which can later on be used to cross-reference with the
new version that becomes available. As they are in the same directory.



*****
re: Google Docs Scpreadsheet
 
Please use the GoogleDocs spreadsheet, and contact me for access to it. As it is the most organized way
to keep track of the national progress. Since there are THOUSANDS of tiles, keeping track on a master spreadsheet
where everyone has access to it, makes the most sence.   And hey, this spreadsheet could also be used as a speadsheet.
It's a great way for everyone to quickly see what the progress is and where.  

http://spreadsheets.google.com/ccc?key=0Am70fsptsPF2dG1ZN1YwMmZCVDhDOHZpbUNmOGlvWGc&hl=en

******
re: CanVec Map features chart

Since this is the place where all the features are listed, (they are copied in the RULES.txt) identically to
what is listed on the chart (should be, to the best of my ability)

You can view the CanVec Map Features chart
http://spreadsheets.google.com/ccc?key=0Am70fsptsPF2clAwc29KaXlGaGFwS1piUkxZWjc0ekE&hl=en
And help by indicating which features pass the 'community review'.  If you see any errors.
PLEASE contact me to fix, so i can update the internal script.
