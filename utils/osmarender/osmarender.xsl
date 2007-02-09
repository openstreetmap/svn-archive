<?xml version='1.0' encoding='UTF-8' ?>
<!DOCTYPE xsl:stylesheet [ <!ENTITY nbsp '&#160;'> ]>

<!-- Osmarender.xsl 3.2 -->

<!--

Copyright (C) 2006  OpenStreetMap Foundation

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA

-->

<!-- Revision history:
     1.0 2006-03-21 Initial version
     1.1 2006-03-23 Remove <html> and <body> tags
     1.2 2006-03-24 Support for ways
     1.3 2006-04-10 width key will override line width
                    Implements nested rules
                    General restructuring
                    Implements <symbol> instruction
		 1.4 2006-04-11 Implements <textPath> instruction for text on Segments and Ways
		 1.5 2006-04-11 Fix bug that generated invalid xsl-stylesheet PI
										Fix bug resulting in superflous white space output
										Fix bug causing dy attribute on <textPath> element rather than <text> element
     1.6 2006-04-12 Fix bug with <text> instructions choking on <segment> and <way> elements in Batik
     2.0 2006-07-07 Implements <area> instruction for areas and ways
                    Fix bug to enable stroke-linecap="butt"
					Implements e attribute for rules, allowing selection by element type
                    Implements v="*" for rules
                    Implements k="*" for rules
                    Implements e="node|segment|way|area" for rules
                    Implements v="rag|tag|bobtail" for rules
                    Implements k="rag|tag|bobtail" for rules
					Generates progress message as each rule is processed
					Elements with tags that have a key starting with svg: will be applied to the corresponding rendered element
					Use of width key (eg <tag k="width" v="5px"/>) desupported in favour of svg:stroke-width (eg <tag k="svg:stroke-width" v="5px"/>
					Use of x-offset and y-offset attributes desupported in favour of dx and dy for <text> instructions and transform='translate(x,y)'
						for <symbol> instructions.
					Implements name_direction='-1' tag on segments and ways to draw street names in the reverse direction.
					Use of <textPath> instruction desupported in favour of <text> instruction which now does the right thing for both segments and ways.				
					Copyright and attribution captions dynamically re-positioned top-left. 	
     3.0 2006-09-23 Fix bug with non-contiguous segments in an area
					Ignore elements with action='delete' for use with locally edited JOSM files
					Apply linked segment optimisation to ways that have name_direction=-1
					Added a switch to make copyright and attribution stuff optional
					Made copyright and attribution stuff smaller
					Fix bug with butt-capped ways that abut each other and caused cracks in roads
					Fix bug with butt-capped ways and name_direction=-1 that caused cracks in roads
					Implements layering using the layer tag.
					Implements approximate mercator projection.
					Implements pan and zoom controls.
					Implement border and 1km square grid.
					Implements osmarender:nameDirection as a preferred alternative to name_direction
					<bounds> element in rules file allows bounding box of map to be specified
					<bounds> element in .osm file allows bounding box of map to be specified
					Improved rules file, lots of new tags, icons and cleaner look and feel
					Rules file does not select segments for rendering by default (this encourages everyone to tag ways) 
					Tested with IE 6.x, Firefox 1.5, xalan, xmlstarlet, xsltproc, Adobe ASV3, Inkscape, Batik-Squiggle
	3.1 2006-09-30	Implements waysegment pseudo-element to enable segments that are part of a way to be selected
					Grid lines are now generated properly
					Rendering of external white border is now improved when bounds are specified
					No external white border when no bounds specified
					Various tweaks to the rules file
    3.2 2006-12-29  Added svg namespace to marker elements (Joto)
                    Added rendering for place=suburb (same as place=village) (Joto)
                    Added optional "osmfile" parameter to XSL stylesheet which can be used to override default filename for OSM data file (Joto)
                    Changed rendering order of motorway|trunk|primary[_link] (Joto)
                    Added icon for amenity=post_box (currently same as post_office) (Joto)
                    Now draws name for highway=track if available (Joto)
                    Added call to copyAttributes template to a use element for ways, dashed railway lines and steps now work properly (Joto)
                    highway=gate was rendered way to large. Icon simplified and made smaller (Joto)
	3.3 2007-02-03  Fixed rendering artifact with areas containing holes (eg islands in lakes) (80n)
	3.4 2007-02-09  Further fix for rendering artifact with areas containing holes, specifically for islands in rivers (80n)

-->

<!-- Osmarender rules files contain two kinds of element; rules and instructions.  Rule elements provide a
     simple selection mechanism.  Instructions define what to do with the elements that match the rules. 
     
     Rules are simple filters based on elements, keys and values (the e, k and v attributes).  For example:
      <rule e="way" k="highway" v="motorway">...</rule> 
     will select all ways that have a key of highway with a value of motorway.
     Rules can be nested to provide a logical "and".  For example:
       <rule e="way" k="highway" v="primary">
         <rule e="way" k="abutters" v="retail">
          ...
         </rule>
       </rule>
     would select all ways that are primary roads *and* abutted by retail premises. 

	 Each filter attribute can contain a | separated list of values.  For example e="node|way" will match all nodes and all ways.  
	 k="highway|waterway" will match all elements with a key of highway or waterway. v="primary|secondary" will match all elements that
	 have key values equal to primary or secondary. k="*" means all keys.  k="~" means no keys.  v="*" means all values. v="~" means no value.
	     
     Instructions define what to do with the elements that match the rules.  Typically, they render the element
     in some way by generating an svg command to draw a line or circle etc.  In most cases the attributes of
     the instruction are copied to the corresponding svg command.  For example:
       <line stroke-width="10"/> 
     will generate a corresponding svg command to draw a line with a width of 10px.
     The following instructions can be used:
       <line>   - draw a line
       <area>   - draw an area
       <circle> - draw a circle
       <text>   - write some text
       <symbol> - draw an icon or image
-->

<xsl:stylesheet 
 version="1.0"
 xmlns="http://www.w3.org/2000/svg"
 xmlns:xlink="http://www.w3.org/1999/xlink"
 xmlns:ev="http://www.w3.org/2001/xml-events"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="UTF-8"/>

    <xsl:param name="osmfile" select="/rules/@data"/>

	<xsl:key name='nodeById' match='/osm/node' use='@id'/>
	<xsl:key name='segmentById' match='/osm/segment' use='@id'/>
	<xsl:key name='segmentByFromNode' match='/osm/segment' use='@from'/>
	<xsl:key name='segmentByToNode' match='/osm/segment' use='@to'/>
	<xsl:key name='wayBySegment' match='/osm/way' use='seg/@id'/>
	
	<xsl:variable name='data' select='document($osmfile)'/>

	<!-- Calculate the size of the bounding box based on the file content -->
	<xsl:variable name="bllat">
		<xsl:for-each select="$data/osm/node/@lat">
			<xsl:sort data-type="number" order="ascending"/>
			<xsl:if test="position()=1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:variable>
	<xsl:variable name="bllon">
		<xsl:for-each select="$data/osm/node/@lon">
			<xsl:sort data-type="number" order="ascending"/>
			<xsl:if test="position()=1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:variable>
	<xsl:variable name="trlat">
		<xsl:for-each select="$data/osm/node/@lat">
			<xsl:sort data-type="number" order="descending"/>
			<xsl:if test="position()=1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:variable>
	<xsl:variable name="trlon">
		<xsl:for-each select="$data/osm/node/@lon">
			<xsl:sort data-type="number" order="descending"/>
			<xsl:if test="position()=1">
				<xsl:value-of select="."/>
			</xsl:if>
		</xsl:for-each>
	</xsl:variable>
	
	<xsl:variable name="bottomLeftLatitude">
		<xsl:choose>
			<xsl:when test='/rules/bounds'>
				<xsl:value-of select='/rules/bounds/@minlat'/>
			</xsl:when>
			<xsl:when test='$data/osm/bounds'>
				<xsl:value-of select='$data/osm/bounds/@request_minlat'/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select='$bllat'/>
			</xsl:otherwise>		
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="bottomLeftLongitude">
		<xsl:choose>
			<xsl:when test='/rules/bounds'>
				<xsl:value-of select='/rules/bounds/@minlon'/>
			</xsl:when>
			<xsl:when test='$data/osm/bounds'>
				<xsl:value-of select='$data/osm/bounds/@request_minlon'/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select='$bllon'/>
			</xsl:otherwise>		
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="topRightLatitude">
		<xsl:choose>
			<xsl:when test='/rules/bounds'>
				<xsl:value-of select='/rules/bounds/@maxlat'/>
			</xsl:when>
			<xsl:when test='$data/osm/bounds'>
				<xsl:value-of select='$data/osm/bounds/@request_maxlat'/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select='$trlat'/>
			</xsl:otherwise>		
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="topRightLongitude">
		<xsl:choose>
			<xsl:when test='/rules/bounds'>
				<xsl:value-of select='/rules/bounds/@maxlon'/>
			</xsl:when>
			<xsl:when test='$data/osm/bounds'>
				<xsl:value-of select='$data/osm/bounds/@request_maxlon'/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select='$trlon'/>
			</xsl:otherwise>		
		</xsl:choose>
	</xsl:variable>

	<xsl:variable name='scale' select='/rules/@scale'/>

	<!-- Derive the latitude of the middle of the map -->
	<xsl:variable name='middleLatitude' select='($topRightLatitude + $bottomLeftLatitude) div 2.0'/>
	<!--woohoo lets do trigonometry in xslt -->
	<!--convert latitude to radians -->
	<xsl:variable name='latr' select='$middleLatitude * 3.1415926 div 180.0' />
	<!--taylor series: two terms is 1% error at lat<68 and 10% error lat<83. we probably need polar projection by then -->
	<xsl:variable name='coslat' select='1 - ($latr * $latr) div 2 + ($latr * $latr * $latr * $latr) div 24' />
	<xsl:variable name='projection' select='1 div $coslat' />

	<xsl:variable name='dataWidth' select='(number($topRightLongitude)-number($bottomLeftLongitude))*10000*$scale' />
	<xsl:variable name='dataHeight' select='(number($topRightLatitude)-number($bottomLeftLatitude))*10000*$scale*$projection' />
	<xsl:variable name='km' select='(0.0089928*$scale*10000*$projection)' />
	<xsl:variable name='documentWidth'>
		<xsl:choose>
			<xsl:when test='$dataWidth &gt; (number(/rules/@minimumMapWidth) * $km)'>
				<xsl:value-of select='$dataWidth'/>
			</xsl:when>
			<xsl:otherwise><xsl:value-of select='number(/rules/@minimumMapWidth) * $km'/></xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name='documentHeight'>
		<xsl:choose>
			<xsl:when test='$dataHeight &gt; (number(/rules/@minimumMapHeight) * $km)'>
				<xsl:value-of select='$dataHeight'/>
			</xsl:when>
			<xsl:otherwise><xsl:value-of select='number(/rules/@minimumMapHeight) * $km'/></xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name='width' select='($documentWidth div 2) + ($dataWidth div 2)'/>
	<xsl:variable name='height' select='($documentHeight div 2) + ($dataHeight div 2)'/>

	<!-- Main template -->
	<xsl:template match="/rules">     

		<!-- Include an external css stylesheet if one was specified in the rules file -->
		<xsl:if test='@xml-stylesheet'>
			<xsl:processing-instruction name='xml-stylesheet'>
				href="<xsl:value-of select='@xml-stylesheet'/>" type="text/css"
			</xsl:processing-instruction>
		</xsl:if>

		<svg
		 id='main'
		 version="1.1"
		 baseProfile="full"
		 height="100%"
		 width="100%">		
			<xsl:if test='/rules/@javaScript="yes"'>
				<xsl:attribute name='onscroll'>fnOnScroll(evt)</xsl:attribute>
				<xsl:attribute name='onzoom'>fnOnZoom(evt)</xsl:attribute>
				<xsl:attribute name='onload'>fnOnLoad(evt)</xsl:attribute>
				<xsl:attribute name='onmousedown'>fnOnMouseDown(evt)</xsl:attribute>
				<xsl:attribute name='onmousemove'>fnOnMouseMove(evt)</xsl:attribute>
				<xsl:attribute name='onmouseup'>fnOnMouseUp(evt)</xsl:attribute>
			</xsl:if>

			<!-- Include javaScript functions for all the dynamic stuff --> 
			<xsl:if test='/rules/@javaScript="yes"'>
				<xsl:call-template name='javaScript'/>
			</xsl:if>

			<defs>				
				<!-- Get any <defs> and styles from the rules file -->
				<xsl:copy-of select='defs/*'/>
			</defs>

			<!-- Pre-generate named path definitions for all ways -->
			<xsl:variable name='allWays' select='$data/osm/way' />
			<defs>
				<xsl:for-each select='$allWays'>
					<xsl:call-template name='generateWayPath'/>
				</xsl:for-each>
			</defs>


			<!-- Draw a nice background layer -->
			<rect x='0px' y='0px' height='{$documentHeight}px' width='{$documentWidth}px' class='map-background'/>


			<!-- Process all the rules, one layer at a time -->
	 			<xsl:apply-templates select='/rules/rule'>
	 				<xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and tag[@k="layer" and @v="-5"]]' />
	 				<xsl:with-param name='layer' select='"-5"' />
	 			</xsl:apply-templates>
	 			<xsl:apply-templates select='/rules/rule'>
	 				<xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and tag[@k="layer" and @v="-4"]]' />
	 				<xsl:with-param name='layer' select='"-4"' />
	 			</xsl:apply-templates>
	 			<xsl:apply-templates select='/rules/rule'>
	 				<xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and tag[@k="layer" and @v="-3"]]' />
	 				<xsl:with-param name='layer' select='"-3"' />
	 			</xsl:apply-templates>
	 			<xsl:apply-templates select='/rules/rule'>
	 				<xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and tag[@k="layer" and @v="-2"]]' />
	 				<xsl:with-param name='layer' select='"-2"' />
	 			</xsl:apply-templates>
	 			<xsl:apply-templates select='/rules/rule'>
	 				<xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and tag[@k="layer" and @v="-1"]]' />
	 				<xsl:with-param name='layer' select='"-1"' />
	 			</xsl:apply-templates>
	 			<xsl:apply-templates select='/rules/rule'>
	 				<xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and count(tag[@k="layer"])=0 or tag[@k="layer" and @v="0"]]' />
	 				<xsl:with-param name='layer' select='"0"' />
	 			</xsl:apply-templates>
	 			<xsl:apply-templates select='/rules/rule'>
	 				<xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and tag[@k="layer" and @v="1"]]' />
	 				<xsl:with-param name='layer' select='"1"' />
	 			</xsl:apply-templates>
	 			<xsl:apply-templates select='/rules/rule'>
	 				<xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and tag[@k="layer" and @v="2"]]' />
	 				<xsl:with-param name='layer' select='"2"' />
	 			</xsl:apply-templates>
	 			<xsl:apply-templates select='/rules/rule'>
	 				<xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and tag[@k="layer" and @v="3"]]' />
	 				<xsl:with-param name='layer' select='"3"' />
	 			</xsl:apply-templates>
	 			<xsl:apply-templates select='/rules/rule'>
	 				<xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and tag[@k="layer" and @v="4"]]' />
	 				<xsl:with-param name='layer' select='"4"' />
	 			</xsl:apply-templates>
	 			<xsl:apply-templates select='/rules/rule'>
	 				<xsl:with-param name='elements' select='$data/osm/*[not(@action="delete") and tag[@k="layer" and @v="5"]]' />
	 				<xsl:with-param name='layer' select='"5"' />
	 			</xsl:apply-templates>
			
	
			<!-- Blank out anything outside the bounding box -->
			<xsl:if test='/rules/bounds or $data/osm/bounds'>
				<xsl:call-template name='eraseOutsideBoundingBox'/>
			</xsl:if>
			
			<!-- Draw a grid if required -->
			<xsl:if test='/rules/@showGrid="yes"'>
				<xsl:call-template name="gridDraw"/>
			</xsl:if>

			<!-- Draw a border if required -->
			<xsl:if test='/rules/@showBorder="yes"'>
				<xsl:call-template name="borderDraw"/>
			</xsl:if>

			<!-- Draw the scale in the bottom left corner -->
			<xsl:if test='/rules/@showScale="yes"'>
				<xsl:call-template name="scaleDraw"/>
			</xsl:if>

			<!-- Draw labels and controls that are in a static position -->
			<g id="staticElements" transform="scale(1) translate(0,0)">
				<!-- Draw the +/- zoom controls -->
				<xsl:if test='/rules/@showZoomControls="yes"'>
					<xsl:call-template name="zoomControl"/>
				</xsl:if>

				<!-- Attribute to OSM -->
				<xsl:if test='/rules/@showAttribution="yes"'>
					<xsl:call-template name="attribution"/>
				</xsl:if>
				
				<!-- Creative commons license -->
				<xsl:if test='/rules/@showLicense="yes"'>
					<xsl:call-template name="license"/>
				</xsl:if>
			</g>
		</svg>

	</xsl:template>


	<!-- ============================================================================= -->
	<!-- Rule processing template                                                      -->
	<!-- ============================================================================= -->

	<!-- For each rule apply line, circle, text, etc templates.  Then apply the rule template recursively for each nested rule --> 
	<xsl:template match='rule'>
		<xsl:param name='elements' />
		<xsl:param name='layer' />

		<!-- This is the rule currently being processed -->
		<xsl:variable name='rule' select='.'/>

		<!-- Make list of elements that this rule should be applied to -->
		<xsl:variable name='eBare'>
			<xsl:choose>
				<xsl:when test='$rule/@e="*"'>node|segment|way|area</xsl:when>
				<xsl:when test='$rule/@e'><xsl:value-of select='$rule/@e'/></xsl:when>
				<xsl:otherwise>node|segment|way|area</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<!-- List of keys that this rule should be applied to -->
		<xsl:variable name='kBare' select='$rule/@k' />

		<!-- List of values that this rule should be applied to -->
		<xsl:variable name='vBare' select='$rule/@v' />

		<!-- Top'n'tail selectors with | for contains usage -->
		<xsl:variable name='e'>|<xsl:value-of select='$eBare'/>|</xsl:variable>
		<xsl:variable name='k'>|<xsl:value-of select='$kBare'/>|</xsl:variable>
		<xsl:variable name='v'>|<xsl:value-of select='$vBare'/>|</xsl:variable>


		<xsl:variable name='selectedElements' select='$elements[contains($e,concat("|",name(),"|"))or (contains($e,"|waysegment|") and name()="segment" and key("wayBySegment",@id))]'/>

		<xsl:choose>
			<xsl:when test='contains($k,"|*|")'>
				<xsl:choose>
					<xsl:when test='contains($v,"|~|")'>
						<xsl:variable name='elementsWithNoTags' select='$selectedElements[count(tag)=0]'/>
						<xsl:call-template name='processElements'>
							<xsl:with-param name='eBare' select='$eBare'/>
							<xsl:with-param name='kBare' select='$kBare'/>
							<xsl:with-param name='vBare' select='$vBare'/>
							<xsl:with-param name='layer' select='$layer'/>
							<xsl:with-param name='elements' select='$elementsWithNoTags'/>
							<xsl:with-param name='rule' select='$rule'/>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test='contains($v,"|*|")'>
						<xsl:variable name='allElements' select='$selectedElements'/>
						<xsl:call-template name='processElements'>
							<xsl:with-param name='eBare' select='$eBare'/>
							<xsl:with-param name='kBare' select='$kBare'/>
							<xsl:with-param name='vBare' select='$vBare'/>
							<xsl:with-param name='layer' select='$layer'/>
							<xsl:with-param name='elements' select='$allElements'/>
							<xsl:with-param name='rule' select='$rule'/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name='allElementsWithValue' select='$selectedElements[tag[contains($v,concat("|",@v,"|"))]]'/>
						<xsl:call-template name='processElements'>
							<xsl:with-param name='eBare' select='$eBare'/>
							<xsl:with-param name='kBare' select='$kBare'/>
							<xsl:with-param name='vBare' select='$vBare'/>
							<xsl:with-param name='layer' select='$layer'/>
							<xsl:with-param name='elements' select='$allElementsWithValue'/>
							<xsl:with-param name='rule' select='$rule'/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test='contains($v,"|~|")'>
				<xsl:variable name='elementsWithoutKey' select='$selectedElements[count(tag[contains($k,concat("|",@k,"|"))])=0]'/>
				<xsl:call-template name='processElements'>
					<xsl:with-param name='eBare' select='$eBare'/>
					<xsl:with-param name='kBare' select='$kBare'/>
					<xsl:with-param name='vBare' select='$vBare'/>
					<xsl:with-param name='layer' select='$layer'/>
					<xsl:with-param name='elements' select='$elementsWithoutKey'/>
					<xsl:with-param name='rule' select='$rule'/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test='contains($v,"|*|")'>
				<xsl:variable name='allElementsWithKey' select='$selectedElements[tag[contains($k,concat("|",@k,"|"))]]'/>
				<xsl:call-template name='processElements'>
					<xsl:with-param name='eBare' select='$eBare'/>
					<xsl:with-param name='kBare' select='$kBare'/>
					<xsl:with-param name='vBare' select='$vBare'/>
					<xsl:with-param name='layer' select='$layer'/>
					<xsl:with-param name='elements' select='$allElementsWithKey'/>
					<xsl:with-param name='rule' select='$rule'/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name='elementsWithKey' select='$selectedElements[tag[contains($k,concat("|",@k,"|")) and contains($v,concat("|",@v,"|"))]]'/>
				<xsl:call-template name='processElements'>
					<xsl:with-param name='eBare' select='$eBare'/>
					<xsl:with-param name='kBare' select='$kBare'/>
					<xsl:with-param name='vBare' select='$vBare'/>
					<xsl:with-param name='layer' select='$layer'/>
					<xsl:with-param name='elements' select='$elementsWithKey'/>
					<xsl:with-param name='rule' select='$rule'/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<!-- Process a set of elements selected by a rule at a specific layer -->
	<xsl:template name='processElements'>
		<xsl:param name='eBare'/>
		<xsl:param name='kBare'/>
		<xsl:param name='vBare'/>
		<xsl:param name='layer'/>
		<xsl:param name='elements'/>
		<xsl:param name='rule'/>
		
		<xsl:if test='$elements'>
			<xsl:message>
Processing &lt;rule e="<xsl:value-of select='$eBare'/>" k="<xsl:value-of select='$kBare'/>" v="<xsl:value-of select='$vBare'/>" &gt; 
Matched by <xsl:value-of select='count($elements)'/> elements for layer <xsl:value-of select='$layer'/>.
			</xsl:message>

			<xsl:apply-templates select='*'>
				<xsl:with-param name='layer' select='$layer' />
				<xsl:with-param name='elements' select='$elements' />
				<xsl:with-param name='rule' select='$rule'/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>


	<!-- ============================================================================= -->
	<!-- Templates to process line, circle, text, etc instructions                     -->
	<!-- ============================================================================= -->
	<!-- Each template is passed a variable containing the set of elements that need to
	     be processed.  The set of elements is already determined by the rules, so 
	     these templates don't need to know anything about the rules context they are in. -->

	<!-- Process a <line> instruction -->
	<xsl:template match='line'>
		<xsl:param name='elements' />
		<xsl:param name='layer' />

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.'/>

		<g>
			<xsl:apply-templates select='@*' mode='copyAttributes'/> <!-- Add all the svg attributes of the <line> instruction to the <g> element -->

			<!-- For each segment and way -->
			<xsl:apply-templates select='$elements' mode='line'>
				<xsl:with-param name='instruction' select='$instruction' />
				<xsl:with-param name='layer' select='$layer' />
			</xsl:apply-templates>

		</g>
	</xsl:template>


	<!-- Suppress output of any unhandled elements -->
	<xsl:template match='*' mode='line'/>
	
	
	<!-- Draw lines for a segment -->
	<xsl:template match='segment' mode='line'>
		<xsl:param name='instruction' />

		<xsl:call-template name='drawLine'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='segment' select='.'/>
		</xsl:call-template>

	</xsl:template>


	<!-- Draw lines for a way (draw all the segments that belong to the way) -->
	<xsl:template match='way' mode='line'>
		<xsl:param name='instruction' />
		<xsl:param name='layer' />

		<!-- The current <way> element -->
		<xsl:variable name='way' select='.' />
		
		<xsl:call-template name='drawWay'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='way' select='$way'/>
			<xsl:with-param name='layer' select='$layer' />
		</xsl:call-template>

	</xsl:template>
	

	<!-- Process an <area> instruction -->
	<xsl:template match='area'>
		<xsl:param name='elements' />

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.'/>

		<g>
			<xsl:apply-templates select='@*' mode='copyAttributes'/> <!-- Add all the svg attributes of the <line> instruction to the <g> element -->

			<!-- For each segment and way -->
			<xsl:apply-templates select='$elements' mode='area'>
				<xsl:with-param name='instruction' select='$instruction' />
			</xsl:apply-templates>

		</g>
	</xsl:template>


	<!-- Suppress output of any unhandled elements -->
	<xsl:template match='*' mode='area'/>
	
	
	<!-- Draw area for a <way> or an <area> -->
	<xsl:template match='way|area' mode='area'>
		<xsl:param name='instruction' />

		<xsl:call-template name='generateAreaPath' />

		<xsl:call-template name='renderArea'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='pathId' select='concat("area_",@id)'/>
		</xsl:call-template>

	</xsl:template>


	<!-- Process circle instruction -->
	<xsl:template match='circle'>
		<xsl:param name='elements'/>

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.' />

		<xsl:for-each select='$elements[name()="node"]'>
			<xsl:call-template name='drawCircle'>
				<xsl:with-param name='instruction' select='$instruction'/>
			</xsl:call-template>					
		</xsl:for-each>
	</xsl:template>


	<!-- Process a symbol instruction -->
	<xsl:template match='symbol'>
		<xsl:param name='elements'/>

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.' />

		<xsl:for-each select='$elements[name()="node"]'>
			<xsl:call-template name='drawSymbol'>
				<xsl:with-param name='instruction' select='$instruction'/>
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>	


	<!-- Process a <text> instruction -->
	<xsl:template match='text'>
		<xsl:param name='elements'/>

		<!-- This is the instruction that is currently being processed -->
		<xsl:variable name='instruction' select='.' />
		
		<!-- Select all <node> elements that have a key that matches the k attribute of the text instruction -->
		<xsl:for-each select='$elements[name()="node"][tag[@k=$instruction/@k]]'>
				<xsl:call-template name='renderText'>
					<xsl:with-param name='instruction' select='$instruction'/>
				</xsl:call-template>					
		</xsl:for-each>

		<!-- Select all <segment> and <way> elements that have a key that matches the k attribute of the text instruction -->
		<xsl:apply-templates select='$elements[name()="segment" or name()="way"][tag[@k=$instruction/@k]]' mode='textPath'>
			<xsl:with-param name='instruction' select='$instruction' />
		</xsl:apply-templates>
	</xsl:template>


	<!-- Suppress output of any unhandled elements -->
	<xsl:template match='*' mode='textPath'/>


	<!-- Render textPaths for a segment -->
	<xsl:template match='segment' mode='textPath'>
		<xsl:param name='instruction' />
		
		<!-- The current <segment> element -->
		<xsl:variable name='segment' select='.' />

		<!-- Generate the path for the segment -->
		<!-- Text on segments should be relatively uncommon so only generate a <path> when one is needed -->
		<xsl:call-template name='generateSegmentPath' />

		<xsl:call-template name='renderTextPath'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='pathId' select='concat("segment_",@id)'/>
		</xsl:call-template>

	</xsl:template>


	<!-- Render textPaths for a way -->
	<xsl:template match='way' mode='textPath'>
		<xsl:param name='instruction' />

		<!-- The current <way> element -->
		<xsl:variable name='way' select='.' />

		<xsl:call-template name='renderTextPath'>
			<xsl:with-param name='instruction' select='$instruction'/>
			<xsl:with-param name='pathId' select='concat("way_",@id)'/>
		</xsl:call-template>

	</xsl:template>


	<!-- Generate a way path for the current segment -->
	<xsl:template name='generateSegmentPath'>
		<xsl:variable name='pathData'>
			<xsl:choose>
				<xsl:when test='tag[@k="name_direction"]/@v="-1" or tag[@k="osmarender:nameDirection"]/@v="-1"'>
					<xsl:call-template name='segmentMoveToEnd'/>
					<xsl:call-template name='segmentLineToStart'/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name='segmentMoveToStart'/>
					<xsl:call-template name='segmentLineToEnd'/>			
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<path id="segment_{@id}" d="{$pathData}"/>

	</xsl:template>


	<!-- Generate a way path for the current way element -->
	<xsl:template name='generateWayPath'>
		
		<!-- Generate the path for the way -->
		<xsl:variable name='pathData'>
			<xsl:choose>
				<xsl:when test='tag[@k="name_direction"]/@v="-1" or tag[@k="osmarender:nameDirection"]/@v="-1"'>
					<xsl:for-each select='seg'>
						<xsl:sort select='position()' data-type='number' order='descending'/>
						<xsl:variable name='segmentId' select='@id'/>
						<xsl:variable name='linkedSegment' select='key("segmentById",following-sibling::seg[1]/@id)/@from=key("segmentById",@id)/@to'/>
						<xsl:for-each select='key("segmentById",$segmentId)'>
							<xsl:if test='not($linkedSegment)'>
								<xsl:call-template name='segmentMoveToEnd'/>
							</xsl:if>
								<xsl:call-template name='segmentLineToStart'/>
						</xsl:for-each>
					</xsl:for-each>				
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select='seg[key("segmentById",@id)]'>
						<xsl:variable name='segmentId' select='@id'/>
						<xsl:variable name='linkedSegment' select='key("segmentById",@id)/@from=key("segmentById",preceding-sibling::seg[1]/@id)/@to'/>
						<xsl:for-each select='key("segmentById",$segmentId)'>
							<xsl:if test='not($linkedSegment)'>
								<xsl:call-template name='segmentMoveToStart'/>				
							</xsl:if>
								<xsl:call-template name='segmentLineToEnd'/>							
						</xsl:for-each>
					</xsl:for-each>				
				</xsl:otherwise>			
			</xsl:choose>
		</xsl:variable>

		<path id="way_{@id}" d="{$pathData}"/>

	</xsl:template>


	<!-- Generate an area path for the current way or area element -->
	<xsl:template name='generateAreaPath'>

		<!-- Generate the path for the area -->
		<xsl:variable name='pathData'>
			<xsl:for-each select='seg[key("segmentById",@id)]'>
				<xsl:variable name='segmentId' select='@id'/>
				<xsl:variable name='currentSegmentToNodeId' select='key("segmentById",@id)/@to' />
				<xsl:variable name='currentSegmentFromNodeId' select='key("segmentById",@id)/@from' />
				<xsl:variable name='previousSegmentToNodeId' select='key("segmentById",preceding-sibling::seg[1]/@id)/@to' />
				
				<!-- The linkedSegment flag indicates whether the previous segment is connected to the current segment.  If it isn't
				     then we will need to draw an additional line (segmentLineToStart) from the end of the previous segment to the
				     start of the current segment. 
				-->
				<xsl:variable name='linkedSegment' select='key("segmentById",@id)/@from=$previousSegmentToNodeId'/>
		
				<!--  Now we count the number of segments in this way that have a to node that is equal to the current segment's from node.
				      We do this to find out if the current segment is connected from some other segment in the way.  If it is, and it
				      is not linked to the current segment then we can assume we have the start of a new sub-path.  In this case we shouldn't
				      draw an additional line between the end of the previous segment and the start of the current segment.
				-->
				<xsl:variable name='connectedSegmentCount' select='count(../*[key("segmentById",@id)/@to=$currentSegmentFromNodeId])' />
				
				<xsl:variable name='segmentSequence' select='position()'/>
				<xsl:for-each select='key("segmentById",$segmentId)'>
					<xsl:choose>
						<!-- If this is the start of the way then we always have to move to the start of the segment. -->
						<xsl:when test='$segmentSequence=1'>
							<xsl:call-template name='segmentMoveToStart'/>				
						</xsl:when>
						<!-- If the segment is "connected" to another segment (at the from end) but is not linked to the
							 previous segment, then start a new sub-path -->
						<xsl:when test='$connectedSegmentCount>0 and not($linkedSegment)'>
							<xsl:text>Z</xsl:text>
							<xsl:call-template name='segmentMoveToStart'/>				
						</xsl:when>
						<!-- If the previous segment is not linked to this one we need to draw an artificial line -->
						<xsl:when test='not($linkedSegment)'>
							<xsl:call-template name='segmentLineToStart'/>				
						</xsl:when>
					</xsl:choose>
					<xsl:call-template name='segmentLineToEnd'/>
				</xsl:for-each>
			</xsl:for-each>
			<xsl:text>Z</xsl:text>
		</xsl:variable>

		<path id="area_{@id}" d="{$pathData}"/>

	</xsl:template>


	<!-- Generate a MoveTo command for a segment start -->
	<xsl:template name='segmentMoveToStart'>
		<xsl:variable name='from' select='@from'/>
		<xsl:variable name='fromNode' select='key("nodeById",$from)'/>

		<xsl:variable name='x1' select='($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)' />
		<xsl:variable name='y1' select='($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)'/>
		<xsl:text>M</xsl:text>
		<xsl:value-of select='$x1'/>
		<xsl:text> </xsl:text>
		<xsl:value-of select='$y1'/>
	</xsl:template>

		
	<!-- Generate a LineTo command for a segment start -->
	<xsl:template name='segmentLineToStart'>
		<xsl:variable name='from' select='@from'/>
		<xsl:variable name='fromNode' select='key("nodeById",$from)'/>

		<xsl:variable name='x1' select='($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)' />
		<xsl:variable name='y1' select='($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)'/>
		<xsl:text>L</xsl:text>
		<xsl:value-of select='$x1'/>
		<xsl:text> </xsl:text>
		<xsl:value-of select='$y1'/>
	</xsl:template>


	<!-- Generate a MoveTo command for a segment end -->
	<xsl:template name='segmentMoveToEnd'>
		<xsl:variable name='to' select='@to'/>
		<xsl:variable name='toNode' select='key("nodeById",$to)'/>

		<xsl:variable name='x2' select='($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)'/>
		<xsl:variable name='y2' select='($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)'/>
		<xsl:text>M</xsl:text>
		<xsl:value-of select='$x2'/>
		<xsl:text> </xsl:text>
		<xsl:value-of select='$y2'/>
	</xsl:template>


	<!-- Generate a LineTo command for a segment end -->
	<xsl:template name='segmentLineToEnd'>
		<xsl:variable name='to' select='@to'/>
		<xsl:variable name='toNode' select='key("nodeById",$to)'/>

		<xsl:variable name='x2' select='($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)'/>
		<xsl:variable name='y2' select='($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)'/>
		<xsl:text>L</xsl:text>
		<xsl:value-of select='$x2'/>
		<xsl:text> </xsl:text>
		<xsl:value-of select='$y2'/>
	</xsl:template>
	
	
	<!-- ============================================================================= -->
	<!-- Drawing templates                                                             -->
	<!-- ============================================================================= -->

	<!-- Draw a line for the current <segment> element using the formatting of the current <line> instruction -->
	<xsl:template name='drawLine'>
		<xsl:param name='instruction'/>
		<xsl:param name='segment'/> <!-- The current segment element -->
		<xsl:param name='way'/>  <!-- The current way element if applicable -->

		<xsl:variable name='from' select='@from'/>
		<xsl:variable name='to' select='@to'/>
		<xsl:variable name='fromNode' select='key("nodeById",$from)'/>
		<xsl:variable name='toNode' select='key("nodeById",$to)'/>
		<xsl:variable name='fromNodeContinuation' select='(count(key("segmentByFromNode",$fromNode/@id))+count(key("segmentByToNode",$fromNode/@id)))>1' />
		<xsl:variable name='toNodeContinuation' select='(count(key("segmentByFromNode",$toNode/@id))+count(key("segmentByToNode",$toNode/@id)))>1' />

		<xsl:variable name='x1' select='($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)' />
		<xsl:variable name='y1' select='($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)' />
		<xsl:variable name='x2' select='($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)' />
		<xsl:variable name='y2' select='($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)' />

		<!-- If this is not the end of a path then draw a stub line with a rounded linecap at the from-node end -->
		<xsl:if test='$fromNodeContinuation'>
			<xsl:call-template name='drawSegmentFragment'>
				<xsl:with-param name='x1' select='$x1'/>
				<xsl:with-param name='y1' select='$y1'/>
				<xsl:with-param name='x2' select='number($x1)+((number($x2)-number($x1)) div 10)'/>
				<xsl:with-param name='y2' select='number($y1)+((number($y2)-number($y1)) div 10)'/>
			</xsl:call-template>
		</xsl:if>

		<!-- If this is not the end of a path then draw a stub line with a rounded linecap at the to-node end -->
		<xsl:if test='$toNodeContinuation'>
			<xsl:call-template name='drawSegmentFragment'>
				<xsl:with-param name='x1' select='number($x2)-((number($x2)-number($x1)) div 10)'/>
				<xsl:with-param name='y1' select='number($y2)-((number($y2)-number($y1)) div 10)'/>
				<xsl:with-param name='x2' select='$x2'/>
				<xsl:with-param name='y2' select='$y2'/>
			</xsl:call-template>
		</xsl:if>

		<line>
			<xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
			<xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
			<xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
			<xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
			<xsl:call-template name='getSvgAttributesFromOsmTags'/>
		</line>

	</xsl:template>


	<xsl:template name='tags'>
		<xsl:text>"</xsl:text>
			<xsl:text>Segment Id = </xsl:text>
			<xsl:value-of select='@id'/>
			<xsl:text>\n</xsl:text>
			<xsl:text>From node = </xsl:text>
			<xsl:value-of select='@from'/>
			<xsl:text>\n</xsl:text>
			<xsl:text>To node = </xsl:text>
			<xsl:value-of select='@to'/>
			<xsl:text>\n</xsl:text>
			<xsl:for-each select='tag'>
				<xsl:value-of select='@k'/>
				<xsl:text>=</xsl:text>
				<xsl:value-of select='@v'/>
				<xsl:text>\n</xsl:text>
			</xsl:for-each>
		<xsl:text>"</xsl:text>
	</xsl:template>


	<!-- Draw some part of a segment with round line-caps and no start or end markers -->
	<xsl:template name='drawSegmentFragment'>
		<xsl:param name='x1'/>
		<xsl:param name='x2'/>
		<xsl:param name='y1'/>
		<xsl:param name='y2'/>
			<line>
				<xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
				<xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
				<xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
				<xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
				<!-- add the rounded linecap attribute --> 
				<xsl:attribute name='stroke-linecap'>round</xsl:attribute>
				<!-- suppress any markers else these could be drawn in the wrong place -->
				<xsl:attribute name='marker-start'>none</xsl:attribute>
				<xsl:attribute name='marker-end'>none</xsl:attribute>
				<xsl:call-template name='getSvgAttributesFromOsmTags'/>
			</line>
	</xsl:template>


	<!-- Draw a line for the current <way> element using the formatting of the current <line> instruction -->	
	<xsl:template name='drawWay'>
		<xsl:param name='instruction'/>
		<xsl:param name='way'/>  <!-- The current way element if applicable -->
		<xsl:param name='layer'/>

		<!-- For the first and last segments in the way if the start or end is a continuation, then draw a round-capped stub segment
				that is 1/10th the length of the segment and without any markers.  TODO: do this for all sub-paths within the path.
		     Count the number of segments that link to the from node of this segment.  Only count them if they belong to a way that
		     has a layer tag that is greater than the layer of this way.  If there are any such segments then draw rounded
		     end fragments. --> 
		<!-- Process the first segment in the way -->
		<xsl:variable name='firstSegment' select='key("segmentById",$way/seg[1]/@id)'/>
		<xsl:variable name='firstSegmentFromNode' select='key("nodeById",$firstSegment/@from)'/>
		<xsl:variable name='firstSegmentToNode' select='key("nodeById",$firstSegment/@to)'/>
		<xsl:variable name='firstSegmentInboundLayerCount' select='count(key("wayBySegment",key("segmentByToNode",$firstSegmentFromNode/@id)/@id)/tag[@k="layer" and @v &gt;= $layer])' />
		<xsl:variable name='firstSegmentInboundNoLayerCount' select='count(key("wayBySegment",key("segmentByToNode",$firstSegmentFromNode/@id)/@id)[count(tag[@k="layer"])=0 and $layer &lt; 1])' />
		<xsl:variable name='firstSegmentOutboundLayerCount' select='count(key("wayBySegment",key("segmentByFromNode",$firstSegmentFromNode/@id)/@id)/tag[@k="layer" and @v &gt;= $layer])' />
		<xsl:variable name='firstSegmentOutboundNoLayerCount' select='count(key("wayBySegment",key("segmentByFromNode",$firstSegmentFromNode/@id)/@id)[count(tag[@k="layer"])=0 and $layer &lt; 1])' />
		<xsl:variable name='firstSegmentLayerCount' select='($firstSegmentInboundLayerCount+$firstSegmentInboundNoLayerCount+$firstSegmentOutboundLayerCount+$firstSegmentOutboundNoLayerCount)>1' />
		
		<xsl:if test='$firstSegmentLayerCount'>
			<xsl:variable name='x1' select='($width)-((($topRightLongitude)-($firstSegmentFromNode/@lon))*10000*$scale)' />
			<xsl:variable name='y1' select='($height)+((($bottomLeftLatitude)-($firstSegmentFromNode/@lat))*10000*$scale*$projection)' />
			<xsl:variable name='x2' select='($width)-((($topRightLongitude)-($firstSegmentToNode/@lon))*10000*$scale)' />
			<xsl:variable name='y2' select='($height)+((($bottomLeftLatitude)-($firstSegmentToNode/@lat))*10000*$scale*$projection)' />
			<xsl:call-template name='drawSegmentFragment'>
				<xsl:with-param name='x1' select='$x1'/>
				<xsl:with-param name='y1' select='$y1'/>
				<xsl:with-param name='x2' select='number($x1)+((number($x2)-number($x1)) div 10)'/>
				<xsl:with-param name='y2' select='number($y1)+((number($y2)-number($y1)) div 10)'/>
			</xsl:call-template>
		</xsl:if>

		<!-- Process the last segment in the way -->
		<xsl:variable name='lastSegment' select='key("segmentById",$way/seg[last()]/@id)'/>
		<xsl:variable name='lastSegmentFromNode' select='key("nodeById",$lastSegment/@from)'/>
		<xsl:variable name='lastSegmentToNode' select='key("nodeById",$lastSegment/@to)'/>
		<xsl:variable name='lastSegmentToNodeLayer' select='(count(key("segmentByFromNode",$lastSegmentToNode/@id)[@k="layer" and @v &gt; $layer])+count(key("segmentByToNode",$lastSegmentToNode/@id)[@k="layer" and @v &gt; $layer]))>0' />
		<xsl:variable name='lastSegmentInboundLayerCount' select='count(key("wayBySegment",key("segmentByToNode",$lastSegmentToNode/@id)/@id)/tag[@k="layer" and @v &gt;= $layer])' />
		<xsl:variable name='lastSegmentInboundNoLayerCount' select='count(key("wayBySegment",key("segmentByToNode",$lastSegmentToNode/@id)/@id)[count(tag[@k="layer"])=0 and $layer &lt; 1])' />
		<xsl:variable name='lastSegmentOutboundLayerCount' select='count(key("wayBySegment",key("segmentByFromNode",$lastSegmentToNode/@id)/@id)/tag[@k="layer" and @v &gt;= $layer])' />
		<xsl:variable name='lastSegmentOutboundNoLayerCount' select='count(key("wayBySegment",key("segmentByFromNode",$lastSegmentToNode/@id)/@id)[count(tag[@k="layer"])=0 and $layer &lt; 1])' />
		<xsl:variable name='lastSegmentLayerCount' select='($lastSegmentInboundLayerCount+$lastSegmentInboundNoLayerCount+$lastSegmentOutboundLayerCount+$lastSegmentOutboundNoLayerCount)>1' />

		<xsl:if test='$lastSegmentLayerCount'>
			<xsl:variable name='x1' select='($width)-((($topRightLongitude)-($lastSegmentFromNode/@lon))*10000*$scale)' />
			<xsl:variable name='y1' select='($height)+((($bottomLeftLatitude)-($lastSegmentFromNode/@lat))*10000*$scale*$projection)' />
			<xsl:variable name='x2' select='($width)-((($topRightLongitude)-($lastSegmentToNode/@lon))*10000*$scale)' />
			<xsl:variable name='y2' select='($height)+((($bottomLeftLatitude)-($lastSegmentToNode/@lat))*10000*$scale*$projection)' />
			<xsl:call-template name='drawSegmentFragment'>
				<xsl:with-param name='x1' select='number($x2)-((number($x2)-number($x1)) div 10)'/>
				<xsl:with-param name='y1' select='number($y2)-((number($y2)-number($y1)) div 10)'/>
				<xsl:with-param name='x2' select='$x2'/>
				<xsl:with-param name='y2' select='$y2'/>
			</xsl:call-template>
		</xsl:if>

		<!-- Now draw the way itself -->
		<use xlink:href='#way_{$way/@id}'>
			<xsl:apply-templates select='$instruction/@*' mode='copyAttributes' />
		</use>

	</xsl:template>


	<!-- Draw a circle for the current <node> element using the formatting of the current <circle> instruction -->
	<xsl:template name='drawCircle'>
		<xsl:param name='instruction'/>

		<xsl:variable name='x' select='($width)-((($topRightLongitude)-(@lon))*10000*$scale)' />
		<xsl:variable name='y' select='($height)+((($bottomLeftLatitude)-(@lat))*10000*$scale*$projection)'/>

		<circle r='1' cx='{$x}' cy='{$y}'>
			<xsl:apply-templates select='$instruction/@*' mode='copyAttributes' /> <!-- Copy all the svg attributes from the <circle> instruction -->		
		</circle>
		
	</xsl:template>

	
	<!-- Draw a symbol for the current <node> element using the formatting of the current <symbol> instruction -->
	<xsl:template name='drawSymbol'>
		<xsl:param name='instruction'/>

		<xsl:variable name='x' select='($width)-((($topRightLongitude)-(@lon))*10000*$scale)' />
		<xsl:variable name='y' select='($height)+((($bottomLeftLatitude)-(@lat))*10000*$scale*$projection)'/>

		<use x='{$x}' y='{$y}'>
			<xsl:apply-templates select='$instruction/@*' mode='copyAttributes'/> <!-- Copy all the attributes from the <symbol> instruction -->		
		</use>
	</xsl:template>


	<!-- Render the appropriate attribute of the current <node> element using the formatting of the current <text> instruction -->
	<xsl:template name='renderText'>
		<xsl:param name='instruction'/>
		
		<xsl:variable name='x' select='($width)-((($topRightLongitude)-(@lon))*10000*$scale)' />
		<xsl:variable name='y' select='($height)+((($bottomLeftLatitude)-(@lat))*10000*$scale*$projection)'/>

		<text>
			<xsl:apply-templates select='$instruction/@*' mode='copyAttributes'/>		
			<xsl:attribute name='x'><xsl:value-of select='$x'/></xsl:attribute>
			<xsl:attribute name='y'><xsl:value-of select='$y'/></xsl:attribute>
			<xsl:call-template name='getSvgAttributesFromOsmTags'/>
			<xsl:value-of select='tag[@k=$instruction/@k]/@v'/>
	  </text>
	</xsl:template>


	<!-- Render the appropriate attribute of the current <segment> element using the formatting of the current <textPath> instruction -->
	<xsl:template name='renderTextPath'>
		<xsl:param name='instruction'/>
		<xsl:param name='pathId'/>
		<text>
			<xsl:apply-templates select='$instruction/@*' mode='renderTextPath-text'/>
			<textPath xlink:href="#{$pathId}">
				<xsl:apply-templates select='$instruction/@*' mode='renderTextPath-textPath'/>
				<xsl:call-template name='getSvgAttributesFromOsmTags'/>
				<xsl:value-of select='tag[@k=$instruction/@k]/@v'/>
			</textPath>
	  </text>
	</xsl:template>


	<!-- Suppress the following attributes, allow everything else -->
	<xsl:template match="@startOffset|@method|@spacing|@lengthAdjust|@textLength|@k" mode='renderTextPath-text'>
	</xsl:template>

	<xsl:template match="@*" mode='renderTextPath-text'>
		<xsl:copy/>
	</xsl:template>


	<!-- Allow the following attributes, suppress everything else -->
	<xsl:template match="@startOffset|@method|@spacing|@lengthAdjust|@textLength" mode='renderTextPath-textPath'>
		<xsl:copy/>
	</xsl:template>

	<xsl:template match="@*" mode='renderTextPath-textPath'>
	</xsl:template>


	<!-- Render the appropriate attribute of the current <way> element using the formatting of the current <area> instruction -->
	<xsl:template name='renderArea'>
		<xsl:param name='instruction'/>
		<xsl:param name='pathId'/>
		
		<use xlink:href="#{$pathId}">
			<xsl:apply-templates select='$instruction/@*' mode='copyAttributes' />
		</use>
	</xsl:template>


	<!-- Copy all attributes  -->
	<xsl:template match='@*' mode='copyAttributes'>
		<xsl:copy/>
	</xsl:template>
	
	<!-- If there are any tags like <tag k="svg:font-size" v="5"/> then add these as attributes of the svg output --> 
	<xsl:template name='getSvgAttributesFromOsmTags'>
		<xsl:for-each select='tag[contains(@k,"svg:")]'>
			<xsl:attribute name='{substring-after(@k,"svg:")}'><xsl:value-of select='@v'/></xsl:attribute>
		</xsl:for-each>	
	</xsl:template>
	
	
	<!-- ============================================================================= -->
	<!-- Fairly static stuff                                                           -->
	<!-- ============================================================================= -->

	<!-- Draw an approximate scale in the bottom left corner of the map -->
	<xsl:template name='scaleDraw'>
		<xsl:variable name='x1' select='20' />
		<xsl:variable name='y1' select='round(($documentHeight)+((($bottomLeftLatitude)-(number($bottomLeftLatitude)))*10000*$scale*$projection))-20'/>
		<xsl:variable name='x2' select='$x1+$km'/>
		<xsl:variable name='y2' select='$y1'/>
		

		<line class='map-scale-casing'>
			<xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
			<xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
			<xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
			<xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
		</line>
		
		<line class='map-scale-core' stroke-dasharray='{($km div 10)}'>
			<xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
			<xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
			<xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
			<xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
		</line>

		<line class='map-scale-bookend'>
			<xsl:attribute name='x1'><xsl:value-of select='number($x1)'/></xsl:attribute>
			<xsl:attribute name='y1'><xsl:value-of select='number($y1)+2'/></xsl:attribute>
			<xsl:attribute name='x2'><xsl:value-of select='number($x1)'/></xsl:attribute>
			<xsl:attribute name='y2'><xsl:value-of select='number($y1)-10'/></xsl:attribute>
		</line>

		<line class='map-scale-bookend'>
			<xsl:attribute name='x1'><xsl:value-of select='number($x2)'/></xsl:attribute>
			<xsl:attribute name='y1'><xsl:value-of select='number($y2)+2'/></xsl:attribute>
			<xsl:attribute name='x2'><xsl:value-of select='number($x2)'/></xsl:attribute>
			<xsl:attribute name='y2'><xsl:value-of select='number($y2)-10'/></xsl:attribute>
		</line>

		<text class='map-scale-caption'>
			<xsl:attribute name='x'><xsl:value-of select='$x1'/></xsl:attribute>
			<xsl:attribute name='y'><xsl:value-of select='number($y1)-10'/></xsl:attribute>
			0
		</text>

		<text class='map-scale-caption'>
			<xsl:attribute name='x'><xsl:value-of select='$x2'/></xsl:attribute>
			<xsl:attribute name='y'><xsl:value-of select='number($y2)-10'/></xsl:attribute>
			1km
		</text>

	</xsl:template>


	<xsl:template name='eraseOutsideBoundingBox'>
		<xsl:variable name='topMargin' select='(number($trlat)-number($topRightLatitude))*10000*$scale*$projection'/>
		<xsl:variable name='leftMargin' select='(number($bottomLeftLongitude)-number($bllon))*10000*$scale'/>
		<xsl:variable name='rightMargin' select='(number($trlon)-number($topRightLongitude))*10000*$scale'/>
		<xsl:variable name='bottomMargin' select='(number($bottomLeftLatitude)-number($bllat))*10000*$scale*$projection'/>
		<g fill="white" stroke="none">
			<rect x='{0-$leftMargin}px' y='{0-$topMargin}px' width='{$documentWidth+$leftMargin}px' height='{$topMargin}px'/>
			<rect x='{$documentWidth}px' y='{0-$topMargin}px' width='{$rightMargin}px' height='{$topMargin+$documentHeight}px'/>
			<rect x='{0-$leftMargin}px' y='0px' width='{$leftMargin}px' height='{$documentHeight+$bottomMargin}px'/>
			<rect x='0px' y='{$documentHeight}px' width='{$documentWidth+$rightMargin}px' height='{$bottomMargin}px'/>
		</g>
	</xsl:template>


	<!-- Draw a grid over the map in 1km increments -->
	<xsl:template name='gridDraw'>
		<xsl:call-template name='gridDrawHorizontals'>
			<xsl:with-param name='line' select='"1"'/>
		</xsl:call-template>
		<xsl:call-template name='gridDrawVerticals'>
			<xsl:with-param name='line' select='"1"'/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name='gridDrawHorizontals'>
		<xsl:param name='line'/>
		<xsl:if test='($line*$km) &lt; $documentHeight'>
			<line x1='0px' y1='{$line*$km}px' x2='{$documentWidth}px' y2='{$line*$km}px' class='map-grid-line'/>
			<xsl:call-template name='gridDrawHorizontals'>
				<xsl:with-param name='line' select='$line+1'/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template name='gridDrawVerticals'>
		<xsl:param name='line'/>
		<xsl:if test='($line*$km) &lt; $documentWidth'>
			<line x1='{$line*$km}px' y1='0px' x2='{$line*$km}px' y2='{$documentHeight}px' class='map-grid-line'/>
			<xsl:call-template name='gridDrawVerticals'>
				<xsl:with-param name='line' select='$line+1'/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>


	<!-- Draw map border -->
	<xsl:template name='borderDraw'>
		<line x1="0" y1="0" x2="0" y2="{$documentHeight}" class='map-border-casing' stroke-dasharray="{($km div 10) - 1},1" /> <!-- dasharray can be overridden in stylesheet -->
		<line x1="0" y1="0" x2="{$documentWidth}" y2="0" class='map-border-casing' stroke-dasharray="{($km div 10) - 1},1" /> <!-- dasharray can be overridden in stylesheet -->
		<line x1="0" y1="{$documentHeight}" x2="{$documentWidth}" y2="{$documentHeight}" class='map-border-casing' stroke-dasharray="{($km div 10) - 1},1" /> <!-- dasharray can be overridden in stylesheet -->
		<line x1="{$documentWidth}" y1="0" x2="{$documentWidth}" y2="{$documentHeight}" class='map-border-casing' stroke-dasharray="{($km div 10) - 1},1" /> <!-- dasharray can be overridden in stylesheet -->

		<line x1="0" y1="0" x2="0" y2="{$documentHeight}" class='map-border-core' stroke-dasharray="{($km div 10) - 1},1" /> <!-- dasharray can be overridden in stylesheet -->
		<line x1="0" y1="0" x2="{$documentWidth}" y2="0" class='map-border-core' stroke-dasharray="{($km div 10) - 1},1" /> <!-- dasharray can be overridden in stylesheet -->
		<line x1="0" y1="{$documentHeight}" x2="{$documentWidth}" y2="{$documentHeight}" class='map-border-core' stroke-dasharray="{($km div 10) - 1},1" /> <!-- dasharray can be overridden in stylesheet -->
		<line x1="{$documentWidth}" y1="0" x2="{$documentWidth}" y2="{$documentHeight}" class='map-border-core' stroke-dasharray="{($km div 10) - 1},1" /> <!-- dasharray can be overridden in stylesheet -->
	</xsl:template>


	<!-- Draw zoom controls -->
	<xsl:template name='zoomControl'>
		<defs>

			<style type='text/css'>
			  .fancyButton {
			    stroke: #8080ff;
			    stroke-width: 2px;
			    fill: #fefefe;
			    }
			  .fancyButton:hover {
			    stroke: red;
			    }
			</style>

			<filter id="fancyButton" filterUnits="userSpaceOnUse" x="0" y="0" width="200" height="350">
				<feGaussianBlur in="SourceAlpha" stdDeviation="4" result="blur"/>
				<feOffset in="blur" dx="2" dy="2" result="offsetBlur"/>
				<feSpecularLighting in="blur" surfaceScale="5" specularConstant=".75" 
                          specularExponent="20" lighting-color="white"  
                          result="specOut">
					<fePointLight x="-5000" y="-10000" z="7000"/>
				</feSpecularLighting>
				<feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut"/>
				<feComposite in="SourceGraphic" in2="specOut" operator="arithmetic" 
                   k1="0" k2="1" k3="1" k4="0" result="litPaint"/>
				<feMerge>
					<feMergeNode in="offsetBlur"/>
					<feMergeNode in="litPaint"/>
				</feMerge>
			</filter>
			<symbol id="panDown" viewBox="0 0 19 19" class='fancyButton'>
				<path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
				<path d="M 9.5,5 L 9.5,14"/>
			</symbol>
			<symbol id="panUp" viewBox="0 0 19 19" class='fancyButton'>
				<path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
				<path d="M 9.5,5 L 9.5,14"/>
			</symbol>
			<symbol id="panLeft" viewBox="0 0 19 19" class='fancyButton'>
				<path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
				<path d="M 5,9.5 L 14,9.5"/>
			</symbol>
			<symbol id="panRight" viewBox="0 0 19 19" class='fancyButton'>
				<path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
				<path d="M 5,9.5 L 14,9.5"/>
			</symbol>
			<symbol id="zoomIn" viewBox="0 0 19 19" class='fancyButton'>
				<path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
				<path d="M 5,9.5 L 14,9.5 M 9.5,5 L 9.5,14"/>
			</symbol>
			<symbol id="zoomOut" viewBox="0 0 19 19" class='fancyButton'>
				<path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z" />
				<path d="M 5,9.5 L 14,9.5"/>
			</symbol>
			
		</defs>
		
		<g id='gPanDown' filter='url(#fancyButton)'>
			<xsl:if test='/rules/@javaScript="yes"'>
				<xsl:attribute name="onclick">fnPan("down")</xsl:attribute>
			</xsl:if>
			<use x="18px" y="60px" xlink:href="#panDown" width='14px' height='14px' />
			
		</g>
		<g id='gPanRight' filter='url(#fancyButton)'>
			<xsl:if test='/rules/@javaScript="yes"'>
				<xsl:attribute name="onclick">fnPan("right")</xsl:attribute>
			</xsl:if>
			<use x="8px" y="70px" xlink:href="#panRight" width='14px' height='14px' />
		</g>	
		<g id='gPanLeft' filter='url(#fancyButton)'>
			<xsl:if test='/rules/@javaScript="yes"'>
				<xsl:attribute name="onclick">fnPan("left")</xsl:attribute>
			</xsl:if>
			<use x="28px" y="70px" xlink:href="#panLeft" width='14px' height='14px' />
		</g>	
		<g id='gPanUp' filter='url(#fancyButton)'>
			<xsl:if test='/rules/@javaScript="yes"'>
				<xsl:attribute name="onclick">fnPan("up")</xsl:attribute>
			</xsl:if>
			<use x="18px" y="80px" xlink:href="#panUp" width='14px' height='14px' />
		</g>	


`		<xsl:variable name='x1' select='25' />
		<xsl:variable name='y1' select='105'/>
		<xsl:variable name='x2' select='25'/>
		<xsl:variable name='y2' select='300'/>
		
		<line style="stroke-width: 10; stroke-linecap: butt; stroke: #8080ff;">
			<xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
			<xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
			<xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
			<xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
		</line>
		
		<line style="stroke-width: 8; stroke-linecap: butt; stroke: white; stroke-dasharray: 10,1;">
			<xsl:attribute name='x1'><xsl:value-of select='$x1'/></xsl:attribute>
			<xsl:attribute name='y1'><xsl:value-of select='$y1'/></xsl:attribute>
			<xsl:attribute name='x2'><xsl:value-of select='$x2'/></xsl:attribute>
			<xsl:attribute name='y2'><xsl:value-of select='$y2'/></xsl:attribute>
		</line>

			
		<g id='gZoomIn' filter='url(#fancyButton)' >
			<xsl:if test='/rules/@javaScript="yes"'>
				<!-- Need to use onmousedown because onclick is interfered with by the onmousedown handler for panning -->
				<xsl:attribute name="onmousedown">fnZoom("in")</xsl:attribute>
			</xsl:if>
			<use x="15.5px" y="100px" xlink:href="#zoomIn" width='19px' height='19px'/>
		</g>

		<g id='gZoomOut' filter='url(#fancyButton)' >
			<xsl:if test='/rules/@javaScript="yes"'>
				<!-- Need to use onmousedown because onclick is interfered with by the onmousedown handler for panning -->
				<xsl:attribute name="onmousedown">fnZoom("out")</xsl:attribute>
			</xsl:if>
			<use x="15.5px" y="288px" xlink:href="#zoomOut" width='19px' height='19px' />
		</g>
	</xsl:template>


	<!-- Draw the copyright and attribution details at the top of the map -->
	<xsl:template name='attribution'>
		<g id='gAttribution'>
			<a xlink:href='http://www.openstreetmap.org'>
				<image 
				  x="10px" 
				  y="10px" 
				  width="75px" 
				  height="25px"
				  xlink:href="Osm_linkage.png">
					<title>Copyright OpenStreetMap 2006</title>
				</image>
				<text font-family='Verdana' font-size='4px' fill='black' x='10' y='40'>
				Copyright 2006, OpenStreetMap.org
				</text>
			</a> 
		</g>
	</xsl:template>
	

	<!-- Draw the license details at the bottom right of the map -->
	<xsl:template name='license'>

		<g id='gLicense'>
			<!--Creative Commons License-->
			<a xlink:href='http://creativecommons.org/licenses/by-sa/2.0/'>
				<image 
				  x="90px" 
				  y="13px" 
				  width="60px" 
				  height="20px"
				  xlink:href="somerights20.png">
					<title>Creative Commons - Some Rights Reserved - Attribution-ShareAlike 2.0</title>
				</image>
				<text font-family='Verdana' font-size='4px' fill='black' x='90px' y='40px'>
				This work is licensed under a Creative
				</text>
				<text font-family='Verdana' font-size='4px' fill='black' x='90px' y='45px'>
				Commons Attribution-ShareAlike 2.0 License.
				</text>
			</a>			 
		</g>
	</xsl:template>
	
	
	<xsl:template name='javaScript'>
 
		<script>  <![CDATA[

			function fnResize() {
				fnResizeElement("gAttribution")
				fnResizeElement("gLicense")
				fnResizeElement("gZoomIn")
				fnResizeElement("gZoomOut")
			}
		

			function fnResizeElement(e) {
				//
				var oSVG,scale,currentTranslateX,currentTranslateY,oe
				//
				oSVG=document.rootElement
				scale=1/oSVG.currentScale
				currentTranslateX=oSVG.currentTranslate.x
				currentTranslateY=oSVG.currentTranslate.y
				oe=document.getElementById(e)
				if (oe) oe.setAttributeNS(null,"transform","scale("+scale+","+scale+") translate("+(-currentTranslateX)+","+(-currentTranslateY)+")")
			}


			function fnToggleImage(osmImage) {
		    var xlink = 'http://www.w3.org/1999/xlink';
 				ogThumbnail=document.getElementById('gThumbnail')
				if (ogThumbnail.getAttributeNS(null,"visibility")=="visible") fnHideImage()
				else {
					ogThumbnail.setAttributeNS(null,"visibility","visible")		
 					oThumbnail=document.getElementById('thumbnail')
					oThumbnail.setAttributeNS(xlink,"href",osmImage)
				}
			}
		
			function fnHideImage() {
 				ogThumbnail=document.getElementById('gThumbnail')
				ogThumbnail.setAttributeNS(null,"visibility","hidden")		
			}
		

			/* The following code originally written by Jonathan Watt (http://jwatt.org/), Aug. 2005 */

			if (!window)
			window = this;


			function fnOnLoad(evt) {
				if (!document) window.document = evt.target.ownerDocument
			}

			/**
			 * Event handlers to change the current user space for the zoom and pan
			 * controls to make them appear to be scale invariant.
			 */


			function fnOnZoom(evt) {
				try {
					if (evt.newScale === undefined) throw 'bad interface'
					// update the transform list that adjusts for zoom and pan
					var tlist = document.getElementById('staticElements').transform.baseVal
					tlist.getItem(0).setScale(1/evt.newScale, 1/evt.newScale)
					tlist.getItem(1).setTranslate(-evt.newTranslate.x, -evt.newTranslate.y)
					}
				catch (e) {
					// work around difficiencies in non-moz implementations (some don't
					// implement the SVGZoomEvent or SVGAnimatedTransform interfaces)
					var de = document.documentElement
					var tform = 'scale(' + 1/de.currentScale + ') ' + 'translate(' + (-de.currentTranslate.x) + ', ' + (-de.currentTranslate.y) + ')'
					document.getElementById('staticElements').setAttributeNS(null, 'transform', tform)
					}
				}


			function fnOnScroll(evt) {
				var ct = document.documentElement.currentTranslate
				try {
					// update the transform list that adjusts for zoom and pan
					var tlist = document.getElementById('staticElements').transform.baseVal
					tlist.getItem(1).setTranslate(-ct.x, -ct.y)
					}
				catch (e) {
					// work around difficiencies in non-moz implementations (some don't
					// implement the SVGAnimatedTransform interface)
					var tform = 'scale(' + 1/document.documentElement.currentScale + ') ' + 'translate(' + (-ct.x) + ', ' + (-ct.y) + ')';
					document.getElementById('staticElements').setAttributeNS(null, 'transform', tform)
					}
				}

			function fnZoom(type) {
				var de = document.documentElement;
				var oldScale = de.currentScale;
				var oldTranslate = { x: de.currentTranslate.x, y: de.currentTranslate.y };
				var s = 2;
				if (type == 'in') {de.currentScale *= 1.5;}
				if (type == 'out') {de.currentScale /= 1.4;}
				// correct currentTranslate so zooming is to the center of the viewport:

				var vp_width, vp_height;
				try {
					vp_width = de.viewport.width;
					vp_height = de.viewport.height;
				}
				catch (e) {
					// work around difficiency in moz ('viewport' property not implemented)
					vp_width = window.innerWidth;
					vp_height = window.innerHeight;
				}
				de.currentTranslate.x = vp_width/2 - ((de.currentScale/oldScale) * (vp_width/2 - oldTranslate.x));
				de.currentTranslate.y = vp_height/2 - ((de.currentScale/oldScale) * (vp_height/2 - oldTranslate.y));

			}


			function fnPan(type) {
				var de = document.documentElement;
				var ct = de.currentTranslate;
				var t = 150;
				if (type == 'right') ct.x += t;
				if (type == 'down') ct.y += t;
				if (type == 'left') ct.x -= t;
				if (type == 'up') ct.y -= t;
			}


			var gCurrentX,gCurrentY
			var gDeltaX,gDeltaY
			var gMouseDown=false
			var gCurrentTranslate=document.documentElement.currentTranslate

			function fnOnMouseDown(evt) {
				gCurrentX=gCurrentTranslate.x
				gCurrentY=gCurrentTranslate.y
				gDeltaX=evt.clientX
				gDeltaY=evt.clientY
				gMouseDown=true
				evt.target.ownerDocument.rootElement.setAttributeNS(null,"cursor","move")
			}


			function fnOnMouseUp(evt) {
				gMouseDown=false
				evt.target.ownerDocument.rootElement.setAttribute("cursor","default")
			}

			function fnOnMouseMove(evt) {
				var id
				if (gMouseDown) {
					gCurrentTranslate.x=gCurrentX+evt.clientX-gDeltaX
					gCurrentTranslate.y=gCurrentY+evt.clientY-gDeltaY
   					}
			}

		]]>  </script>	
	</xsl:template>

</xsl:stylesheet>

