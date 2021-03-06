<?php
session_start();
//header("Content-type: image/svg+xml");
header("Content-type: text/xml");
$bbox = $_GET['bbox'];
$scale =(isset($_GET['scale'])) ? $_GET['scale'] : 1;
echo "<?xml version='1.0' encoding='UTF-8'?>\n";
echo "<?xml-stylesheet type='text/xsl' href='osmarender.xsl'?>\n";
?>

<!-- This file should be used with Osmarender 3.0 -->
<!-- This file implements a sub-set of the items described at http://wiki.openstreetmap.org/index.php/Map_Features -->

<!-- A scale of 0.1 will make fat roads on a small map, a scale of 5 will draw very thin roads on a large scale map -->
<!-- minimumMapWidth/Height is in kilometres -->
<!-- Set javaScript="no" if you want an svg file that contains no javascript.  This is so that you can upload it to Wikipedia etc -->
<?php
echo '<rules xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svg="http://www.w3.org/2000/svg" data="getosm.php?bbox='.$bbox.'" scale="'.$scale.'"'.
' minimumMapWidth="4" minimumMapHeight="3"'.
' showScale="yes" showGrid="yes" showBorder="yes" showAttribution="yes" showLicense="yes" showZoomControls="yes" javaScript="yes">';
?>
	
	<!-- Uncomment this element if you want to explicitly specify the bounding box for a map, or you can add a <bounds> element to
	     your data.osm file, which is probably a better thing to do. -->
	<!--<bounds
	    minlat="51.41"
	    minlon="-0.4344802422025771"
	    maxlat="51.42187795307207"
	    maxlon="-0.3891926802448317" />
	-->

	<!-- Only select nodes and ways that do not have a osmarender:render=no tag -->
	<!-- If you really want to render segments then you will need to add segment to the element list, but please don't - tag the ways instead. -->
	<rule e="node|way" k="osmarender:render" v="~|yes">

		<!-- draw highway shading first -->
		<rule k="highway" v="residential">
			<line class='abutters-residential' /> 
		</rule>
		<rule k="abutters" v="residential">
			<line class='abutters-residential'/> 
		</rule>
		<rule k="abutters" v="retail">
			<line class='abutters-retail'/> 
		</rule>
		<rule k="abutters" v="industrial">
			<line class='abutters-industrial'/> 
		</rule>
		<rule k="abutters" v="commercial">
			<line class='abutters-commercial'/> 
		</rule>
		<rule k="abutters" v="mixed">
			<line class='abutters-mixed'/> 
		</rule>

		<!-- draw waterways -->
		<rule e="way|area" k="waterway" v="riverbank">
		   <area class='waterway-riverbank'/> 
		</rule>
		<rule e="segment|way" k="waterway" v="river">
			<line class='waterway-river-casing'/> 
		</rule>
		<rule e="segment|way" k="waterway" v="canal">
			<line class='waterway-canal-casing'/> 
		</rule>
		<rule e="segment|way" k="waterway" v="drain">
			<line class='waterway-drain-casing'/> 
		</rule>
		<rule e="segment|way" k="waterway" v="river">
			<line class='waterway-river-core'/> 
		</rule>
		<rule e="segment|way" k="waterway" v="canal">
			<line class='waterway-canal-core'/> 
		</rule>
		<rule e="segment|way" k="waterway" v="drain">
			<line class='waterway-drain-core'/> 
		</rule>

		<!-- Draw natural features -->
		<rule e="way|area" k="natural" v="coastline">
		   <area class='natural-water'/> 
		</rule>
		<rule e="way|area" k="natural" v="land">
			<area class='natural-land' /> 
		</rule>
		<rule e="way|area" k="leisure" v="park|playing_fields|garden|pitch|golf_course|common">
			<area class='leisure-park'/> 
		</rule>
		<rule e="way|area" k="landuse" v="forest|wood">
			<area class='landuse-wood'/> 
		</rule>
		<rule e="way|area" k="landuse" v="field">
			<area class='landuse-field'/>
		</rule>
		<rule e="way|area" k="landuse" v="residential">
			<area class='landuse-residential'/> 
		</rule>
		<rule e="way|area" k="landuse" v="retail">
			<area class='landuse-retail'/> 
		</rule>
		<rule e="way|area" k="landuse" v="industrial">
			<area class="landuse-industrial" />
		</rule>
			<rule e="way|area" k="landuse" v="commercial">
		<area class="landuse-commercial" />
		</rule>
		<rule e="way|area" k="natural" v="water|pond|lake">
			<area class='natural-water' /> 
		</rule>
		<rule e="way|area" k="landuse" v="reservoir">
			<area class='natural-water' /> 
		</rule>
		<rule e="way|area" k="landuse" v="basin">
			<area class='natural-water' /> 
		</rule>
		<rule e="way|area" k="landuse" v="cemetery">
			<area class='landuse-cemetery' />
		</rule>

		<!-- Draw man-made areas -->
		<rule e="way|area" k="sport" v="rugby|soccer|cricket">
			<area class='sport'/>
		</rule>
		<rule e="way|area" k="amenity" v="parking">
			<area class='amenity-parking'/> 
		</rule>
		<rule e="way|area" k="tourism" v="attraction">
			<area class='tourism-attraction'/> 
		</rule>
		<rule e="way|area" k="building" v="barn|warehouse|oast_house|tower|castle|monument|hall|shed|store|stadium">
			<area class='building'/> 
		</rule>
		<rule e="way|area" k="building" v="barn|warehouse|oast_house|block|tower|castle|monument|hall|shed|store|stadium">
			<area class='building-block'/> 
		</rule>
		<rule e="way|area" k="building" v="detached|semi|terrace|apartments">
			<area class='building-residential'/> 
		</rule>


		<!-- For debugging this rule draws a one pixel wide trace of *all* segments.  This enables segments that have no
		     tags to be identified. Comment it out to hide the debug trace. --> 
		<!--<rule e="segment" k="~" v="~">  
			<line class='debug'/>
		</rule>-->

		<!-- draw highway casings -->
		<rule e="segment|way" k="highway" v="pedestrian">
			<line class='highway-pedestrian-casing' />
		</rule>
		<rule e="segment|way" k="highway" v="track">
			<line class='highway-track-casing' />
		</rule>
		<rule e="segment|way" k="highway" v="unclassified|residential|minor">
			<line class='highway-unclassified-casing' />
		</rule>
		<rule e="segment|way" k="highway" v="unsurfaced">
			<line class='highway-unsurfaced-casing' />
		</rule>
		<rule e="segment|way" k="highway" v="service">
			<line class='highway-service-casing' />
		</rule>
		<rule e="segment|way" k="highway" v="secondary">
			<line class='highway-secondary-casing' />
		</rule>
		<rule e="segment|way" k="highway" v="primary|primary_link">
			<line class='highway-primary-casing' />
		</rule>
		<rule e="segment|way" k="highway" v="trunk|trunk_link">
			<line class='highway-trunk-casing' />
		</rule>
		<rule e="segment|way" k="highway" v="motorway|motorway_link">
			<line class='highway-motorway-casing' />
		</rule>


		<!-- next draw paths -->
		<rule e="segment|way" k="highway" v="footway|steps">
			<line class='highway-footway' /> 
			<text k="ref" class='highway-footway-ref' dx='2px' dy='-2px' />		
		</rule>
		<rule e="segment|way" k="highway" v="steps">
			<line class='highway-steps' /> 
		</rule>
		<rule e="segment|way" k="highway" v="cycleway">
			<line class='highway-cycleway' /> 
			<text k="ref" class='highway-cycleway-ref' dx='2px' dy='-2px' />
		</rule>
		<rule e="segment|way" k="highway" v="bridleway">
			<line class='highway-bridleway' /> 
			<text k="ref" class='highway-bridleway-ref' dx='2px' dy='-2px' />
		</rule>
		<rule e="way" k="highway" v="byway">
			<line class='highway-byway' /> 
			<text k="ref" class='highway-byway-ref' dx='2px' dy='-2px' />
		</rule>


		<!-- draw highway cores -->
		<rule e="segment|way" k="highway" v="pedestrian">
			<rule k="oneway" v="~">
				<line class='highway-pedestrian-core' />
			</rule>
			<rule k="oneway" v="1|yes|true">
				<line class='highway-pedestrian-core oneway' />
			</rule>
			<rule k="oneway" v="-1">
				<line class='highway-pedestrian-core otherway' />
			</rule>
		</rule>
		<rule e="segment|way" k="highway" v="track">
			<rule k="oneway" v="~">
				<line class='highway-track-core' />
			</rule>
			<rule k="oneway" v="1|yes|true">
				<line class='highway-track-core oneway' />
			</rule>
			<rule k="oneway" v="-1">
				<line class='highway-track-core otherway' />
			</rule>
		</rule>
		<rule e="segment|way" k="highway" v="unclassified|residential|minor">
			<rule k="oneway" v="~">
				<line class='highway-unclassified-core' />	
			</rule>
			<rule k="oneway" v="1|yes|true">
				<line class='highway-unclassified-core oneway' />	
			</rule>
			<rule k="oneway" v="-1">
				<line class='highway-unclassified-core otherway' />		
			</rule>		
		</rule>
		<rule e="segment|way" k="highway" v="unsurfaced">
			<rule k="oneway" v="~">
				<line class='highway-unsurfaced-core' />	
			</rule>
			<rule k="oneway" v="1|yes|true">
				<line class='highway-unsurfaced-core oneway' />	
			</rule>
			<rule k="oneway" v="-1">
				<line class='highway-unsurfaced-core otherway' />		
			</rule>		
		</rule>
		<rule e="segment|way" k="highway" v="service">
			<rule k="oneway" v="~">
				<line class='highway-service-core' />	
			</rule>
			<rule k="oneway" v="1|yes|true">
				<line class='highway-service-core oneway' />	
			</rule>
			<rule k="oneway" v="-1">
				<line class='highway-service-core otherway' />		
			</rule>		
		</rule>
		<rule e="segment|way" k="highway" v="secondary">
			<rule k="oneway" v="~">
				<line class='highway-secondary-core' />	
			</rule>
			<rule k="oneway" v="1|yes|true">
				<line class='highway-secondary-core oneway' />	
			</rule>		
			<rule k="oneway" v="-1">
				<line class='highway-secondary-core otherway' />		
			</rule>		
		</rule>
		<rule e="segment|way" k="highway" v="primary|primary_link">
			<rule k="oneway" v="~">
				<line class='highway-primary-core' />
			</rule>
			<rule k="oneway" v="1|yes|true">
				<line class='highway-primary-core oneway' />	
			</rule>
			<rule k="oneway" v="-1">
				<line class='highway-primary-core otherway' />		
			</rule>		
		</rule>
		<rule e="segment|way" k="highway" v="trunk|trunk_link">
			<rule k="oneway" v="~">
				<line class='highway-trunk-core' />
			</rule>
			<rule k="oneway" v="1|yes|true">
				<line class='highway-trunk-core oneway' />	
			</rule>
			<rule k="oneway" v="-1">
				<line class='highway-trunk-core otherway' />		
			</rule>		
		</rule>
		<rule e="segment|way" k="highway" v="motorway|motorway_link">
			<rule k="oneway" v="~">
				<line class='highway-motorway-core' />
			</rule>
			<rule k="oneway" v="1|yes|true">
				<line class='highway-motorway-core oneway' />	
			</rule>
			<rule k="oneway" v="-1">
				<line class='highway-motorway-core otherway' />		
			</rule>		
		</rule>


		<!-- draw railway lines -->
		<rule e="segment|way" k="railway" v="rail">
			<line class='railway-rail' />
		</rule>


		<!-- Airfields and airports -->
		<rule e="segment|way" k="aeroway" v="runway">
			<line class='aeroway-runway-casing'/>
		</rule>
		<rule e="segment|way" k="aeroway" v="taxiway">
			<line class='aeroway-taxiway-casing'/>
		</rule>
		<rule e="segment|way" k="aeroway" v="runway">
			<line class='aeroway-runway-core'/>
		</rule>
		<rule e="segment|way" k="aeroway" v="taxiway">
			<line class='aeroway-taxiway-core'/>
		</rule>	
		<rule e="node" k="aeroway" v="aerodrome">
			<symbol xlink:href="#airport" width='5px' height='5px' transform='translate(-2.5,-2.5)' />
			<rule k="osmarender:renderName" v="~|yes">
				<text k="name" class='aeroway-aerodrome-caption' dx='4px' dy='2.5px'/>
			</rule>
		</rule>
		<rule e="node" k="aeroway" v="airport">
			<symbol xlink:href="#airport" width='10px' height='10px' transform='translate(-5,-5)' />
			<rule k="osmarender:renderName" v="~|yes">
				<text k="name" class='aeroway-airport-caption' dx='8px' dy='4px' />
			</rule>
		</rule>

		
		<!-- Power Lines and Pylons -->
		<rule e="node" k="power" v="tower">
			<symbol xlink:href="#power-tower" width='1px' height='1px' transform='translate(-.5,-.5)'/>
		</rule>
		<rule e="way" k="power" v="line">
			<line class='power-line'/>
		</rule>	


		<!-- draw non-pysical routes -->
		<rule e="segment|way" k="route" v="ferry">
			<line class='route-ferry' />
		</rule>
		

		<!-- draw places  -->
		<rule e="node" k="place" v="continent">
			<text k="name" class='continent-caption' />
		</rule>
		<rule e="node" k="place" v="country">
			<text k="name" class='country-caption' />
		</rule>
		<rule e="node" k="place" v="state">
			<text k="name" class='state-caption' />
		</rule>
		<rule e="node" k="place" v="region">
			<text k="name" class='region-caption' />
		</rule>
		<rule e="node" k="place" v="county">
			<text k="name" class='county-caption' />
		</rule>
		<rule e="node" k="place" v="city">
			<text k="name" class='city-caption' />
		</rule>
		<rule e="node" k="place" v="town">
			<text k="name" class='town-caption' />
		</rule>
		<rule e="node" k="place" v="village">
			<text k="name" class='village-caption' />
		</rule>
		<rule e="node" k="place" v="hamlet">
			<text k='name' class='hamlet-caption' />
		</rule>
		<rule e="node" k="place" v="farm">
			<text k='name' class='farm-caption' />
		</rule>


		<!-- Draw tourist features -->
		<rule e="node" k="tourism" v="attraction">
			<text k='name' class='tourism-attraction-caption' />
		</rule>
		<rule k="tourism" v="hotel">
			<symbol xlink:href="#hotel" width='4px' height='4px' transform='translate(-2,-2)' />
		</rule>
		<rule e="node" k="tourism" v="hostel">
			<symbol xlink:href="#hostel" width='6px' height='4px' transform='translate(-3,-2)' />
		</rule>		
		<rule e="node" k="tourism" v="camp_site">
			<symbol xlink:href="#campSite" width='4px' height='4px' transform='translate(-1.5,-1.5)' />
		</rule>

		
		<rule e="node" k="railway" v="station">
			<circle r="1.5" class="railway-station" />
			<rule k="osmarender:renderName" v="~|yes">
				<text k="name" class='railway-station-caption' dx='2.5px' dy='1.5px' />
			</rule>
		</rule>


		<!-- Draw amenities -->
		<rule e="node" k="amenity" v="hospital">
			<symbol xlink:href="#hospital" width='5px' height='5px' transform='translate(-2.5,-2.5)' />
		</rule>
		<rule e="node" k="amenity" v="post_office">
			<symbol xlink:href="#postoffice" width='4px' height='2px' transform='translate(-2,-1)' />
		</rule>
		<rule e="node" k="amenity" v="pub">
			<symbol xlink:href="#pub" width='1.75px' height='2.5px' transform='translate(-0.9,-1.25)'/>
			<rule k="osmarender:renderName" v="~|yes">
				<text k='name' class='amenity-pub-caption' dx='1px' dy='0.5px'/>
			</rule>
		</rule>
		<rule e="node" k="amenity" v="place_of_worship">
			<rule e="node" k="denomination" v="~">
				<symbol xlink:href="#church" width='2.5px' height='5px' transform='translate(-1.25,-2.5)' />
			</rule>
			<rule e="node" k="denomination" v="christian|church_of_england">
				<symbol xlink:href="#church" width='2.5px' height='5px' transform='translate(-1.25,-2.5)' />
			</rule>
			<rule e="node" k="denomination" v="jewish">
				<symbol xlink:href="#synagogue" width='4px' height='4px' transform='translate(-2,-2)' />
			</rule>
			<rule e="node" k="denomination" v="muslim">
				<symbol xlink:href="#mosque" width='4px' height='4px' transform='translate(-2,-2)' />
			</rule>
		</rule>
		<rule e="node" k="amenity" v="parking">
			<symbol xlink:href="#parking" width='4px' height='4px' transform='translate(-2,-2)' />
		</rule>
		<rule e="node" k="amenity" v="fuel">
			<symbol xlink:href="#petrolStation" width='2.5px' height='5px' transform='translate(-1.25,-3.5)' />
		</rule>
		<rule k="amenity" v="recycling">
			<symbol xlink:href="#recycling" width='4px' height='4px' transform='translate(-2,-2)'/>
		</rule> 

		<!-- Draw leisure symbols -->
		<rule e="node" k="leisure" v="golf_course">
			<symbol xlink:href="#golfCourse" width='5px' height='10px' transform='translate(-2.5,-5)' />
		</rule>
		<rule e="node" k="leisure" v="slipway">
			<symbol xlink:href="#slipway" width='4px' height='4px' transform='translate(-2,-2)' />
		</rule>

		<!-- Draw street names for all highways -->
		<rule k="osmarender:renderName" v="~|yes">
			<rule e="segment|way" k="highway" v="unclassified|residential|name|pedestrian">
				<text k="name" text-anchor='middle' startOffset='50%' class="highway-unclassified-name" />
			</rule>
			<rule e="way" k="highway" v="unsurfaced">
				<text k="name" text-anchor='middle' startOffset='50%' class="highway-unsurfaced-name" />
			</rule>
			<rule e="segment|way" k="highway" v="service">
				<text k="name" text-anchor='middle' startOffset='50%' class="highway-unclassified-name" />
			</rule>
			<rule e="segment|way" k="highway" v="secondary">
				<text k="name" text-anchor='middle' startOffset='50%' class="highway-secondary-name" />
			</rule>
			<rule e="segment|way" k="highway" v="primary">
				<text k="name" text-anchor='middle' startOffset='50%' class="highway-primary-name" />
			</rule>
			<rule e="segment|way" k="highway" v="trunk">
				<text k="name" text-anchor='middle' startOffset='50%' class="highway-trunk-name" />
			</rule>
			<rule e="segment|way" k="highway" v="motorway">
				<text k="name" text-anchor='middle' startOffset='50%' class="highway-motorway-name" />
			</rule>
		</rule>

		<!-- Draw road numbers for all highways -->
		<rule k="osmarender:renderRef" v="~|yes">
			<rule e="segment|way" k="highway" v="unclassified|residential">
				<text k="ref" class='highway-unclassified-ref' dx='2.5px' dy='-2.5px' />
			</rule>
			<rule e="way" k="highway" v="unsurfaced">
				<text k="ref" class='highway-unsurfaced-ref' dx='2.5px' dy='-2.5px' />
			</rule>
			<rule e="segment|way" k="highway" v="service">
				<text k="ref" class='highway-service-ref' dx='2.5px' dy='-2.5px' />
			</rule>
			<rule e="segment|way" k="highway" v="secondary">
				<text k="ref" class='highway-secondary-ref' dx='2.5px' dy='-2.5px' />
			</rule>
			<rule e="segment|way" k="highway" v="primary">
				<text k="ref" text-anchor='middle' startOffset='60%' class="highway-primary-name" />
			</rule>
			<rule e="segment|way" k="highway" v="trunk">
				<text k="ref" class='highway-trunk-ref' dx='2.5px' dy='-2.5px' />
			</rule>
			<rule e="segment|way" k="highway" v="motorway">
				<text k="ref" class='highway-motorway-ref' dx='2.5px' dy='-2.5px' />
			</rule>
		</rule>

		<rule e="node" k="highway" v="gate">
			<symbol xlink:href="#gate" width='10px' height='5px' transform='translate(-5,-2.5)'/>
		</rule>

		<!--<rule e="segment" node="from|to|any" k="highway" v="gate">
			<symbol xlink:href="#gate" width='10' height='5' transform='translate(-5,-2.5)'/>
		</rule>-->

		<!-- Use the following three rules to display nodes, segments and ways as they would appear in JOSM, overlayed on top of anything else -->
		<!--
		<rule e="segment" k="*" v="*">
			<line class='josm-segment' /> 
		</rule>

		<rule e="way" k="*" v="*">
			<line class='josm-way' /> 
		</rule>

		<rule e="node" k="*" v="*">
			<circle r='0.2' class='josm-node' /> 
		</rule>
		-->

		<!-- Use this rule to highlight tags that you want to get rid of, or change -->
		<!--<rule e="way" k="class|highway|waterway|route" v="~">
				<line class='error'/> 
		</rule>-->
	</rule>



	<!-- SVG Definitions - markers, symbols etc go here -->
	<defs>

		<style type="text/css" xmlns="http://www.w3.org/2000/svg">
			.debug {
			  stroke-width: 0.1px;
			  stroke-linecap: round;
			  stroke: gray;
			  /* marker-end: url(#segment-direction); */
			  }
		
			.error {
			  stroke-width: 2px;
			  stroke-linecap: round;
			  stroke: red;
			  }

			.abutters-residential {
			  stroke-width: 9px;
			  stroke-linecap: round;
			  stroke: #f2f2f2;
			  fill: none;
			  }

			.abutters-retail {
			  stroke-width: 9px;
			  stroke-linecap: round;
			  stroke: #ffebeb;
			  fill: none;
			  }

			.abutters-industrial {
			  stroke-width: 9px;
			  stroke-linecap: round;
			  stroke: #ecd8ff;
			  fill: none;
			  }

			.abutters-commercial {
			  stroke-width: 9px;
			  stroke-linecap: round;
			  stroke: #fcffc9;
			  fill: none;
			  }
				
			.abutters-mixed {
			  stroke-width: 9px;
			  stroke-linecap: round;
			  stroke: #d8feff;
			  fill: none;
			  }

			/* Highways */
			.highway-motorway-casing {
			  stroke-width: 2.5px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #222222;
			  fill: none;
			  }

			.highway-motorway-core {
			  stroke-width: 2px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #809BC0;
			  fill: none;
			  }

			.highway-motorway-name {
			  fill: black;
			  font-family: verdana;
			  font-size: 1.5px;
			  font-weight: normal;
			  baseline-shift: -35%;
			  }

			.highway-motorway-ref {
			  fill: black;
			  stroke: white;
			  stroke-width: .4px;
			  font-family: verdana;
			  font-size: 7px;
			  font-weight: bolder;
			  }			

			.highway-trunk-casing {
			  stroke-width: 2.5px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #222222;
			  fill: none;
			  }

			.highway-trunk-core {
			  stroke-width: 2px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #e46d71;
			  fill: none;
			  }

			.highway-trunk-name {
			  fill: black;
			  font-family: verdana;
			  font-size: 1.5px;
			  font-weight: normal;
			  baseline-shift: -35%;
			  }

			.highway-trunk-ref {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.4px;
			  font-family: verdana;
			  font-size: 6px;
			  font-weight: bolder;
			  }			

			.highway-primary-casing {
			  stroke-width: 2px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #222222;
			  fill: none;
			  }

			.highway-primary-core {
			  stroke-width: 1.5px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #7FC97F;
			  fill: none;
			  }

			.highway-primary-name {
			  fill: black;
			  font-family: verdana;
			  font-size: 1px;
			  font-weight: bolder;
			  stroke: #ffffff;
			  stroke-width: 0px; 
			  baseline-shift: -35%;
			  }

			.highway-primary-ref {
			  fill: black;
			  font-family: verdana;
			  font-size: 1px;
			  font-weight: bolder;
			  stroke: white;
			  stroke-width: 0px;
			  baseline-shift: -35%;
			  }			

			.highway-secondary-casing {
			  stroke-width: 2px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #222222;
			  fill: none;
			  }

			.highway-secondary-core {
			  stroke-width: 1.5px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #FDBF6F;
			  fill: none;
			  }

			.highway-secondary-name {
			  fill: black;
			  font-family: verdana;
			  font-size: 1px;
			  font-weight: bolder;
			  baseline-shift: -35%;
			  }

			.highway-secondary-ref {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.3px;
			  font-family: verdana;
			  font-size: 5px;
			  font-weight: bolder;
			  }			

			.highway-unclassified-casing {
			  stroke-width: 1.5px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round; 
			  fill: none;
			  stroke: #222222;
			  }

			.highway-unclassified-core {
			  stroke-width: 1.2px; 
			  stroke-linecap: butt;
			  stroke-linejoin: round; 
			  stroke: #ffffff;
			  fill: none;
			  }

			.highway-unclassified-name {
			  fill: black;
			  font-family: verdana;
			  font-size: 1px;
			  font-weight: bold;
			  baseline-shift: -35%;
 			  }

			.highway-unclassified-ref {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.3px;
			  font-family: verdana;
			  font-size: 4.5px;
			  font-weight: bolder;
			  }			

			.highway-unsurfaced-casing {
			  stroke-width: 1.5px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round; 
			  fill: none;
			  stroke: #222222;
			  stroke-dasharray: 1px, .5px;
			  }
			  
			.highway-unsurfaced-core {
			  stroke-width: 1.2px; 
			  stroke-linecap: butt;
			  stroke-linejoin: round; 
			  stroke: #ffffff;
			  fill: none;
			  }
			  
			.highway-unsurfaced-name {
			  fill: black;
			  font-family: verdana;
			  font-size: 1px;
			  font-weight: bold;
			  baseline-shift: -35%;
 			  }
 			  
			.highway-unsurfaced-ref {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.3px;
			  font-family: verdana;
			  font-size: 4.5px;
			  font-weight: bolder;
			  }			

			.highway-track-casing {
			  stroke-width: 1.5px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  fill: none;
			  stroke: #d79331;
			  }

			.highway-track-core {
			  stroke-width: 1.2px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  stroke: #ffffff;
			  fill: none;
			  }
			  
			.highway-pedestrian-casing {
			  stroke-width: 1.5px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  fill: none;
			  stroke: #aaaaaa;
			  }

			.highway-pedestrian-core {
			  stroke-width: 1.2px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  stroke: #eeeeee;
			  fill: none;
			  }
			  
			.highway-service-casing {
			  stroke-width: 0.7px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round; 
			  fill: none;
			  stroke: #222222;
			  }

			.highway-service-core {
			  stroke-width: 0.4px; 
			  stroke-linecap: butt;
			  stroke-linejoin: round; 
			  stroke: #ffffff;
			  fill: none;
			  }

			.highway-service-name {
			  fill: black;
			  font-family: verdana;
			  font-size: 0.3px;
			  font-weight: bold;
			  baseline-shift: -35%;
 			  }

			.highway-unclassified-ref {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.3px;
			  font-family: verdana;
			  font-size: 4.5px;
			  font-weight: bolder;
			  }			

			.highway-bridleway {
			  stroke-width: 1px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #70b971;
			  fill: none;
			  }

			.highway-byway {
			  stroke-width: 1px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #ef7771;
			  fill: none;
			  }

			.highway-byway-ref {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.3px;
			  font-family: verdana;
			  font-size: 4px;
			  font-weight: bolder;
			  }			

			.highway-cycleway {
			  stroke-width: 1px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #008102;
			  fill: none;
			  }

			.highway-cycleway-ref {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.3px;
			  font-family: verdana;
			  font-size: 4px;
			  font-weight: bolder;
			  }			

			.highway-footway {
			  stroke-width: 0.5px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #d79331;
			  fill: none;
			  }

			.highway-footway-ref {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.3px;
			  font-family: verdana;
			  font-size: 4px;
			  font-weight: bolder;
			  }			

			.highway-steps {
			  stroke-width: 0.5px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #707070;
			  stroke-dasharray: 0.1px, 0.3px;
			  fill: none;
			  }
				
			/* Aeroways */
			.aeroway-taxiway-core {
			  stroke-width: 1px;
			  stroke-linecap: butt;
			  stroke-linejoin: round; 
			  stroke: #CCCCCC;
			  fill: none;
			  }

			.aeroway-taxiway-casing {
			  stroke-width: 3px;
			  stroke-linecap: butt;
			  stroke-linejoin: round; 
			  stroke: #000000;
			  fill: none;
			  }

			.aeroway-runway-core {
			  stroke-width: 5px;
			  stroke-linecap: butt;
			  stroke-linejoin: round; 
			  stroke: #CCCCCC;
			  fill: none;
			  }

			.aeroway-runway-casing {
			  stroke-width: 7px;
			  stroke-linecap: butt;
			  stroke-linejoin: round; 
			  stroke: #000000;
			  fill: none;
			  }	

			.aeroway-aerodrome-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.3px;
			  font-family: verdana;
			  font-size: 6px;
			  font-weight: bolder;
			  }

			.aeroway-airport-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.3px;
			  font-family: verdana;
			  font-size: 10px;
			  font-weight: bolder;
			  }

			/* Waterways */
			.waterway-riverbank {
			  fill: #89bac6;
			  stroke: #aaaaaa;
			  stroke-width: 0px;
			  }
			
			.waterway-river-casing {
			  stroke-width: 4px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  stroke: #aaaaaa;
			  fill: none;
			  }
				
			.waterway-river-core {
			  stroke-width: 3px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  stroke: #89bac6;
			  fill: none;
			  }

			.waterway-canal-casing {
			  stroke-width: 2px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  stroke: #aaaaaa;
			  fill: none;
			  }
				
			.waterway-canal-core {
			  stroke-width: 1px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  stroke: #89bac6;
			  fill: none;
			  }
			
			.waterway-drain-casing {
			  stroke-width: 1px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  stroke: #aaaaaa;
			  fill: none;
			  }
				
			.waterway-drain-core {
			  stroke-width: 0.5px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  stroke: #89bac6;
			  fill: none;
			  }

			.railway-rail {
			  stroke-width: 1.5px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  stroke: #000000;
			  fill: none;
			  }
			
			.railway-rail-dashes {
			  stroke-width: 1px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  stroke: #ffffff;
			  fill: none;
			  stroke-dasharray: 3px, 3px;
			  stroke-opacity: 1;
			  }
							
			.railway-station {
			  fill: red;
			  stroke: black;
			  stroke-width: 0.5px;
			  }

			.railway-station-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.2px;
			  font-family: verdana;
			  font-size: 4px;
			  font-weight: bolder;
			  }

			.route-ferry {
			  stroke-width: 0.5px;
			  stroke-linecap: butt;
			  stroke-linejoin: round;
			  stroke: #777777;
			  fill: none;
			  }
			
			.point-of-interest {
			  fill: red;
			  stroke: black;
			  stroke-width: 0.5px;
			  }

			.josm-segment {
			  stroke-width: 0.2px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #008000;
			  fill: none;
			  marker-end: url(#segment-direction);
			  }

			.josm-way {
			  stroke-width: 0.2px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #000060;
			  fill: none;
			  }
			
			.josm-node {
			  fill: #ff0000;
			  stroke: none;
			  }


			/* Place names */						
			.continent-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.6px;
			  font-family: verdana;
			  font-size: 20px;
			  font-weight: bolder;
			  }
			  
			.country-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.5px;
			  font-family: verdana;
			  font-size: 18px;
			  font-weight: bolder;
			  }
			  
			.state-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.5px;
			  font-family: verdana;
			  font-size: 16px;
			  font-weight: bolder;
			  }
			  
			.region-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.4px;
			  font-family: verdana;
			  font-size: 14px;
			  font-weight: bolder;
			  }
			  
			.county-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.4px;
			  font-family: verdana;
			  font-size: 12px;
			  font-weight: bolder;
			  }
			  
			.city-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.4px;
			  font-family: verdana;
			  font-size: 10px;
			  font-weight: bolder;
			  }
			  
			.town-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.4px;
			  font-family: verdana;
			  font-size: 8px;
			  font-weight: bolder;
			  }
			  
			.village-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.3px;
			  font-family: verdana;
			  font-size: 6px;
			  font-weight: bolder;
			  }
			  
			.hamlet-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.2px;
			  font-family: verdana;
			  font-size: 4px;
			  font-weight: bolder;
			  }
			  
			.farm-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.1px;
			  font-family: verdana;
			  font-size: 3px;
			  font-weight: bold;
			  }
			  
			.church-caption {
			  fill: black;
			  stroke: white;
			  stroke-width: 0.3px;
			  font-family: verdana;
			  font-size: 5px;
			  font-weight: bold;
			  }
			  
			.natural-water {
			  fill: #89bac6;
			  stroke: #aaaaaa;
			  stroke-width: 0px;
			  }
			  
			.natural-land {
			  fill: #ffffff;
			  stroke: #e0e0e0;
			  stroke-width: 0.1px;
			  }

			.landuse-wood {
			  fill: #84b295;
			  stroke: #6fc18e;
			  stroke-width: 0.2px;
			  }
			  
			.landuse-cemetery {
			  fill: #bde3cb;
			  stroke: #eeeeee;
			  stroke-width: 0.2px;
			  }

			.landuse-field {
			  fill: #c7f1a3;
			  stroke: #6fc13d;
			  stroke-width: 0.2px;
			}

			.landuse-residential {
			  stroke: none;
			  fill: #f2f2f2;
			  }
			  
			.landuse-retail {
			  stroke: none;
			  fill: #ffebeb;
			  }

			.landuse-industrial {
			  fill: #ecd8ff;
			  stroke: #eeeeee;
			  stroke-width: 0.2px;
			  }
			  
			.landuse-commercial {
			  fill: #fcffc9;
			  stroke: #eeeeee;
			  stroke-width: 0.2px;
			  }
			  
			.landuse-retail {
			  fill: #ffebeb;
			  stroke: #eeeeee;
			  stroke-width: 0.2px;
			  }

			.leisure-park {
			  fill: #bde3cb;
			  stroke: #6fc18e;
			  stroke-width: 0.2px;
			  }
			  
			.sport {
			  fill: #bde3cb;
			  stroke: #6fc18e;
			  stroke-width: 0.2px;
			  }
			  
			.amenity-parking {
			  fill: #f7efb7;
			  stroke: #e9dd72;
			  stroke-width: 0.2px;
			  }
			  
			.boundary-areaOfInterest {
			  fill: #f2caea;
			  stroke: #aaaaaa;
			  stroke-width: 0px;
			  }
			  
			.boundary-areaOfInterest-A {
			  fill: #f2caff;
			  stroke: #aaaaaa;
			  stroke-width: 0px;
			  }
			  
			.boundary-areaOfInterest-B {
			  fill: #f2cabb;
			  stroke: #aaaaaa;
			  stroke-width: 0px;
			  }
			  
			.boundary-areaOfInterest-C {
			  fill: #f2cadd;
			  stroke: #aaaaaa;
			  stroke-width: 0px;
			  }
			  
			.tourism-attraction {
			  fill: #f2caea;
			  stroke: #f124cb;
			  stroke-width: 0px;
			  }
			  
			.tourism-attraction-caption {
			  fill: #f124cb;
			  stroke: white;
			  stroke-width: 0px;
			  font-family: verdana;
			  font-size: 3px;
			  font-weight: bolder;
			  }

			.amenity-pub-caption {
			  fill: #e21e2f;
			  stroke: white;
			  stroke-width: 0px;
			  font-family: verdana;
			  font-size: 1px;
			  font-weight: bolder;
			  }

			.building {
			  fill: #dddddd;
			  stroke: #cccccc;
			  stroke-width: 0.2px;
			  }
			  
			.building-block {
			  fill: #a18bd8;
			  stroke: #6a5a8e;
			  stroke-width: 0.2px;
			  }

			.building-residential {
			  fill: #c95e2a;
			  stroke: #80290a;
			  stroke-width: 0.2px;
			  }

			.power-line {
			  stroke-width: 0.1px; 
			  stroke-linecap: butt; 
			  stroke-linejoin: round;
			  stroke: #cccccc;
			  stroke-dasharray: 1px ,1px;
			  fill: none;
			  }

			.oneway {
			  marker-end: url(#triangle);
			  }

			.otherway {
			  marker-start: url(#invertedTriangle);
			  }
				
			.map-grid-line {
			  fill: none;
			  stroke: #8080ff;
			  stroke-opacity: 0.5;
			  }

			.map-border-casing {
			  fill: none;
			  stroke: #8080ff;
			  stroke-width: 3px;
			  stroke-miterlimit: 4;
			  stroke-dasharray: none;
			  stroke-opacity: 1;
			  stroke-linecap: round;
			  }

			.map-border-core {
			  fill: none;
			  fill-opacity: 1;
			  fill-rule: nonzero;
			  stroke: #ffffff;
			  stroke-width: 2px;
			  stroke-miterlimit: 0;
			  stroke-dashoffset: -0.5px;
			  stroke-opacity: 1;
			  }

			.map-scale-casing {
			  fill: none;
			  stroke: #8080ff;
			  stroke-width: 4px;
			  stroke-linecap: butt;
			  }
			
			.map-scale-core {
			  fill: none;
			  stroke: #ffffff;
			  stroke-width: 3px;
			  stroke-linecap: butt;
			  }

			.map-scale-bookend {
			  fill: none;
			  stroke: #8080ff;
			  stroke-width: 1px;
			  stroke-linecap: butt;
			  }
			  
			.map-scale-caption {
			  font-family: verdana;
			  font-size: 10px;
			  fill: #8080ff;
			  }

			.map-background {
			  fill: #fcfcfc;
			  stroke: none;
			  } 

		</style>
	
		<marker 
			id="triangle"
			viewBox="0 0 10 10"
			refX="10px" refY="5px" 
			markerUnits="userSpaceOnUse"
			fill='#a2aee9'
			stroke-width='1px'
			stroke='#000000'
			markerWidth="1px"
			markerHeight="1px"
			orient="auto">
			<path d="M 0,4 L 6,4 L 6,2 L 10,5 L 6,8 L 6,6 L 0,6 z" />
		</marker>

		<marker 
			id="invertedTriangle"
			viewBox="0 0 10 10"
			refX="0px" refY="5px" 
			markerUnits="userSpaceOnUse"
			fill='#a2aee9'
			stroke-width='1px'
			stroke='#000000'
			markerWidth="1px"
			markerHeight="1px"
			orient="auto">
			<path d="M 10,4 L 4,4 L 4,2 L 0,5 L 4,8 L 4,6 L 10,6 z" />
		</marker>	

		<marker 
			id="segment-direction"
			viewBox="0 0 10 10"
			refX="10px" refY="5px" 
			markerUnits="userSpaceOnUse"
			fill='none'
			stroke-width='1px'
			stroke='#008000'
			markerWidth="1px"
			markerHeight="1px"
			orient="auto">
			<path d="M 0,2 L 10,5 L 0,8" />
		</marker>

		<svg:symbol
		  id="church"
		  viewBox="0 0 5 10"
		  fill='#000000'>
			<svg:path d="M 0 10 L 0 5 L 5 5 L 5 10 z M 0 2 L 5 2 L 5 3 L 0 3 z M 2 0 L 2 5 L 3 5 L 3 0 z" />
		</svg:symbol>	

		<svg:symbol
		  id="mosque"
		  viewBox="0 0 120 120" 
		  fill='#00ab00'>
			<svg:path d="M 4,60 C 11,75 60,107 84,73 C 103,40 76,22 50,7 C 76,6 130,35 103,84 C 72,124 8,97 4,60 z M 35,52 C 35,52 20,55 20,55 L 30,43 C 30,43 21,30 21,30 L 35,35 L 45,23 L 45,38 L 60,45 L 45,50 L 45,65 L 35,52 z"/>
		</svg:symbol>
		
		<svg:symbol 
		  id="synagogue" 
		  viewBox="0 0 20 20" 
		  stroke='#0000d0' 
		  fill='none'
		  stroke-width="1.5px"
		  stroke-linecap="butt"
		  stroke-linejoin="miter">
			<svg:path d="M 10,0 L 20,15 L 0,15 L 10,0 z M 10,20 L 0,5 L 20,5 L 10,20 z" />
		</svg:symbol>
		
		<!-- derived from http://www.sodipodi.com/index.php3?section=clipart -->
		<svg:symbol 
		  id="campSite"
		  viewBox="0 0 100 100" 
		  fill='#0000dc'
		  fill-opacity="1">
			<svg:path d="M 35,0 L 50,24 L 65,0 L 80,0 L 60,35 L 100,100 L 0,100 L 40,35 L 20,0 L 35,0 z "/>
		</svg:symbol>

		<svg:symbol 
		  id="gate"
		  viewBox="0 0 10 10"
		  fill='none'
		  stroke-width='0.4px'
		  stroke='#000000'>
			<svg:path d="M 0,7 L 10,7 M 0,6 L 10,6 M 0,5 L 10,5 M 0,4 L 10,4 M 0,3 L 10,3 M 0,7 L 0,3 M 10,7 L 10,3 M 0,7 L 10,3" />
		</svg:symbol>
			
		<svg:symbol
		  id="airport"
		  viewBox="0 0 10 10"
		  fill="black"
		  fill-opacity="1"
		  fill-rule="evenodd"
		  stroke="none">
			<svg:path d="M 9.2,5 C 9.2,4.5 9.8,3.2 10,3 L 9,3 L 8,4 L 5.5,4 L 8,0 L 6,0 L 3,4 C 2,4 1,4.2 0.5,4.5 C 0,5 0,5 0.5,5.5 C 1,5.8 2,6 3,6 L 6,10 L 8,10 L 5.5,6 L 7.8,6 L 9,7 L 10,7 C 9.8,6.8 9.2,5.5 9.2,5 z " />
		</svg:symbol>
		
		<svg:symbol 
		  id="power-tower" 
		  viewBox="0 0 10 10"
		  stroke-width='1px'
		  stroke='#cccccc'>
		  <svg:path d="M 0 0 L 10 10 M 0 10 L 10 0" />
		</svg:symbol>

		<svg:symbol 
		  id="bar"
		  viewBox="0 0 100 100"
		  fill='#000000'
		  stroke-width='0.4px'
		  stroke='#000000'>
			<svg:path d="M 16.8725 9.81954 L 96.3004 9.81954 L 59.4774 46.3164 L 59.4774 94.9796 C 59.575 94.9796 57.9896 100.587 84.2324 102.6 L 84.2324 103.99 L 31.0262 103.99 L 31.0275 102.6 C 56.4414 100.587 54.9906 94.9796 54.9906 94.9796 L 54.9906 46.3164 L 16.8725 9.81954 z " />
		</svg:symbol>

		<!-- derived from http://www.sodipodi.com/index.php3?section=clipart -->
		<svg:symbol 
		  id="petrolStation"
		  viewBox="0 0 100 100"
		  fill='#000000'
		  fill-rule="evenodd"
		  stroke-width="3px">
			<svg:path d="M 22.7283 108.087 C 4.26832 107.546 23.6818 43.3596 32.6686 21.0597 C 33.8491 17.0245 60.28 18.4952 60.0056 19.8857 C 59.0889 25.9148 54.8979 23.2429 52.0142 26.8579 L 51.7464 36.8066 C 48.6085 40.8144 40.2357 34.4677 38.078 42.8773 C 31.3694 92.5727 45.0689 108.819 22.7283 108.087 z M 85.3122 9.52799 L 29.1766 9.52847 C 28.4855 17.5896 -11.559 113.573 22.9292 113.284 C 48.5214 113.073 39.5312 104.08 42.6984 51.03 C 41.8513 49.3228 50.871 48.6585 50.8739 51.4448 L 51.0453 116.604 L 97.6129 116.188 L 97.6129 26.544 C 96.0669 24.2073 93.899 25.2958 90.584 22.394 C 87.7907 19.4131 92.2353 9.52799 85.3122 9.52799 z M 64.0766 35.3236 C 61.5443 36.7258 61.5443 45.2814 64.0766 46.6836 C 68.3819 49.0684 80.2848 49.0684 84.5902 46.6836 C 87.1225 45.2814 87.1225 36.7258 84.5902 35.3236 C 80.2848 32.9393 68.3819 32.9393 64.0766 35.3236 z "/>
		</svg:symbol>	

		<!-- derived from http://www.sodipodi.com/index.php3?section=clipart -->
		<svg:symbol 
		  id="golfCourse"
		  viewBox="0 0 100 100"
		  fill='#000000'
		  fill-rule="evenodd"
		  fill-opacity="1"
		  stroke="none">
			<svg:path d="M 61.6421 25.2514 C 61.6421 25.2514 48.7712 34.4528 48.1727 38.766 C 47.574 43.0787 56.5537 48.8295 56.8529 52.2802 C 57.1522 55.7303 56.5537 87.3594 56.5537 87.3594 C 56.5537 87.3594 37.3978 104.036 36.7993 105.474 C 36.2006 106.912 41.5878 117.55 43.9826 117.263 C 46.3769 116.975 43.3841 109.787 44.2819 108.349 C 45.1798 106.912 64.0363 92.5353 65.2335 90.5221 C 65.5327 91.0979 65.8321 76.7208 65.5327 76.7208 L 66.7305 76.7208 L 66.1319 91.0979 C 66.1319 91.0979 59.2473 108.349 60.1451 113.237 C 60.1451 115.824 70.6212 122.15 72.1176 121 C 73.6145 119.85 68.5261 115.536 68.8254 112.375 C 67.6283 109.212 73.016 97.4233 73.3153 94.2605 C 73.6145 91.0979 73.9138 56.3053 72.7167 51.9927 C 72.7161 48.542 69.424 42.5037 67.9276 40.2035 C 67.6283 37.9029 65.8326 31.2897 65.8326 31.2897 C 65.8326 31.2897 59.547 39.341 59.5465 39.341 C 58.0501 37.9035 68.2268 28.702 68.2268 25.8268 C 68.2268 22.9513 49.9689 9.72452 49.9689 9.72452 C 49.9689 9.72452 25.126 63.2064 25.4254 65.5065 C 25.7246 67.8065 29.9146 72.9824 32.908 70.6823 C 35.9009 68.3822 27.8197 62.9194 27.8197 62.9194 L 49.3703 14.6122 L 52.6624 18.3506 L 58.3494 18.638 L 58.0501 19.5005 C 58.0501 19.5005 51.7645 18.9255 50.5675 19.788 C 49.3703 20.6506 47.574 22.0887 47.574 25.5388 C 47.574 28.9896 52.0638 30.4271 53.5603 30.7146 L 60.8936 24.6764 L 61.6421 25.2514 z "/>
		</svg:symbol>	

		<svg:symbol 
		  id="slipway" 
		  viewBox="0 0 50 45" 
		  fill='#0087ff' 
		  stroke='none' 
		  fill-opacity='0.7'>
			<svg:path d="M 45,33 L 45,45 L 2,45 C 2,45 45,33 45,33 z M 0,35 L 43,22 L 43,26 C 43,26 37,32 26,36 C 15,40 0,35 0,35 z M 3,32 C 3,32 13,0 13,0 L 22,26 L 3,32 z M 16,0 L 42,20 L 25,25 L 16,0 z "/>
		</svg:symbol>

		<svg:symbol 
		  id="pub" 
		  viewBox="0 0 6 9"
		  stroke='none'>
			<svg:path fill="#aa5605" d="M 1.2,9 C 1.2,9 1,3 0.3,1.7 L 5.7,1.7 C 5,3 4.8,9 4.8,9" />
			<svg:path fill="#ffe680" d="M 5.7,1.7 L 0.3,1.7 C 0,1 0,1 0,0 L 6,0 C 6,1 6,1 5.7,1.7 z" />
		</svg:symbol>
		
		<!-- derived from http://www.sodipodi.com/index.php3?section=clipart -->
		<svg:symbol 
		  id="hotel" 
		  viewBox="0 0 90 90"
		  fill="black"
		  fill-opacity="1"
		  stroke="black"
		  stroke-width="1px"
		  stroke-miterlimit="4px">
			<svg:path d="M 0,60 C 0,65 10,65 10,60 L 10,50 L 35,70 L 35,85 C 35,90 45,90 45,85 L 45,70 L 75,70 L 75,85 C 75,90 85,90 85,85 L 85,60 L 40,60 L 5,30 C 9,20 45,20 50,25 L 50,10 C 50,5 40,5 40,10 L 40,15 L 10,15 L 10,10 C 10,5 0,5 0,10 C 0,10 0,60 0,60 z M 10,35 C 15,25 45,25 55,35 L 85,60 C 75,50 40,50 40,60 L 10,35 z "/>
		</svg:symbol>

		<!-- derived from http://www.sodipodi.com/index.php3?section=clipart -->
		<svg:symbol 
		  id="hostel" 
		  viewBox="0 0 12.5 8"
		  fill="#286a9d"
		  fill-opacity="1"
		  fill-rule="nonzero"
		  stroke="none">
			<svg:path d="M 5.5,4 L 9,0 L 12.5,4 L 11.5,4 L 11.5,8 L 10,8 L 10,5 L 8,5 L 8,8 L 6.5,8 L 6.5,4 L 5.5,4 z M 0.5,3.5 C 2,2.5 2.3,1 2.5,0 C 2.7,1 3,2.5 4.5,3.5 L 3.3,3.5 C 3.3,4 4,5 5,6 L 3,6 L 3,8 L 2,8 L 2,6 L 0,6 C 1,5 1.7,4 1.7,3.5 L 0.5,3.5 z M 0,8 L 0,7.5 L 12.5,7.5 L 12.5,8 L 0,8 z " />
		</svg:symbol>

		<svg:symbol 
		  id="recycling"
		  viewBox="0 0 100 100"
		  stroke='none'
		  fill='#00ba00'>
			<svg:path d="M 55.0,37.3 L 72.1,27.0 L 79.8,41.9 C 81.6,50.0 71.5,52.9 63.3,52.4 L 55.0,37.3 z" />
			<svg:path d="M 51.1,47.9 L 42.1,63.8 L 51.1,80.0 L 51.3,73.5 L 59.5,73.5 C 62.5,73.8 66.4,71.8 67.9,69.0 L 78.4,49.5 C 75.0,53.0 70.5,53.9 65.3,53.9 L 51.4,53.9 L 51.1,47.9 z " />
			<svg:path d="M 31.0,28.2 L 13.7,18.2 L 22.9,4.2 C 29.0,-1.3 36.6,6.1 40.1,13.5 L 30.9,28.2 z " />
			<svg:path d="M 42.1,26.5 L 60.4,26.6 L 70.1,10.9 L 64.3,13.8 L 60.3,6.6 C 59.1,3.9 55.5,1.4 52.3,1.5 L 30.2,1.7 C 34.9,3.1 37.9,6.6 40.4,11.1 L 47.2,23.3 L 42.1,26.5 z " />
			<svg:path d="M 0.4,27.4 L 5.8,31.5 L 0.8,40.5 C -1.8,45.3 2.6,49.6 5.3,51.0 C 8.0,52.5 12.2,52.7 16.2,52.7 L 23.3,41.3 L 28.6,44.1 L 19.3,27.2 L 0.4,27.4 z " />
			<svg:path d="M 1.2,49.3 L 12.7,70.1 C 15.0,73.0 19.4,73.7 23.9,73.6 L 36.0,73.6 L 36.0,53.9 L 13.0,53.7 C 9.5,53.9 4.8,53.2 1.2,49.3 z " />
		</svg:symbol>

		<svg:symbol 
		  id="hospital" 
		  viewBox="0 0 15 15" 
		  stroke='red'
		  stroke-width="2px"
		  fill="none">
			<svg:path d="M 12.5,7.5 L 2.5,7.5 L 2.5,7.5 L 12.5,7.5 z M 7.5,2.3 L 7.5,12.5 L 7.5,12.5"/>
			<svg:path stroke-width="1px" d="M 14.5 7.5 A 7 7 0 1 1 0.5,7.5 A 7 7 0 1 1 14.5 7.5 z" />
		</svg:symbol>

		<svg:symbol 
		  id="postoffice" 
		  viewBox="0 0 14 8"
		  fill="none"
		  stroke="red"
		  stroke-width="1.5px">
			<svg:path d="M 0,0 L 14,0 L 14,8 L 0,8 L 0,0 z M 0,0 L 7,4 L 14,0" />
		</svg:symbol>

	</defs>

	
</rules>

