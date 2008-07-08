<?xml version="1.0" encoding="UTF-8"?>
<!--
==============================================================================

Osmarender 6.0 Alpha 6 
    with - orig area generation 
         - one node way filtered out
         - filtered out missing multipolygon relation members from areas
         - filtered out missing node ref from ways

==============================================================================

Copyright (C) 2006-2007  Etienne Cherdlu, Jochen Topf

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

==============================================================================
-->
<xsl:stylesheet
  xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:xi="http://www.w3.org/2001/XInclude"
  xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
  xmlns:cc="http://web.resource.org/cc/"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:exslt="http://exslt.org/common"
  xmlns:msxsl="urn:schemas-microsoft-com:xslt"
  exclude-result-prefixes="exslt msxsl" 
  version="1.0">

  <xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="UTF-8"/>

  <!-- This msxsl script extension fools msxsl into interpreting exslt extensions as msxsl ones, so 
       we can write code using exslt extensions even though msxsl only recognises the msxsl extension 
       namespace.  Thanks to David Carlisle for this: http://dpcarlisle.blogspot.com/2007/05/exslt-node-set-function.html -->
  <msxsl:script language="JScript" implements-prefix="exslt">
    this['node-set'] =  function (x) {
    return x;
    }
  </msxsl:script>

  <xsl:param name="osmfile" select="/rules/@data"/>
  <xsl:param name="title" select="/rules/@title"/>

  <xsl:param name="scale" select="/rules/@scale"/>
  <xsl:param name="symbolScale" select="/rules/@symbolScale"/>
  <xsl:param name='textAttenuation' select='/rules/@textAttenuation'/>
  <xsl:param name="withOSMLayers" select="/rules/@withOSMLayers"/>
  <xsl:param name="svgBaseProfile" select="/rules/@svgBaseProfile"/>
  <xsl:param name="symbolsDir" select="/rules/@symbolsDir"/>

  <xsl:param name="showGrid" select="/rules/@showGrid"/>
  <xsl:param name="showBorder" select="/rules/@showBorder"/>
  <xsl:param name="showScale" select="/rules/@showScale"/>
  <xsl:param name="showLicense" select="/rules/@showLicense"/>

  <xsl:param name="showRelationRoute" select="/rules/@showRelationRoute"/>

  <xsl:key name="nodeById" match="/osm/node" use="@id"/>
  <xsl:key name="wayById" match="/osm/way" use="@id"/>
  <xsl:key name="wayByNode" match="/osm/way" use="nd/@ref"/>
  <xsl:key name="relationByWay" match="/osm/relation" use="member/@ref"/>

  <xsl:variable name="data" select="document($osmfile)"/>

  <!-- Use a web-service (if available) to get the current date -->
  <xsl:variable name="now" select="document('http://xobjex.com/service/date.xsl')" />
  <xsl:variable name="date">
    <xsl:choose>
      <xsl:when test="$now">
        <xsl:value-of select="substring($now/date/utc/@stamp,1,10)" />
        <!-- Assumes 4 digit year -->
      </xsl:when>
      <xsl:otherwise>2007-01-01</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="year">
    <xsl:choose>
      <xsl:when test="$now">
        <xsl:value-of select="$now/date/utc/year" />
      </xsl:when>
      <xsl:otherwise>2007</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- extra height for marginalia at top -->
  <xsl:variable name="marginaliaTopHeight">
    <xsl:choose>
      <xsl:when test="$title != ''">40</xsl:when>
      <xsl:when test="($title = '') and ($showBorder = 'yes')">1.5</xsl:when>
      <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- extra height for marginalia at bottom -->
  <xsl:variable name="marginaliaBottomHeight">
    <xsl:choose>
      <xsl:when test="($showScale = 'yes') or ($showLicense = 'yes')">45</xsl:when>
      <xsl:when test="($showScale != 'yes') and ($showLicense != 'yes') and ($showBorder = 'yes')">1.5</xsl:when>
      <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- extra width for border -->
  <xsl:variable name="extraWidth">
    <xsl:choose>
      <xsl:when test="$showBorder = 'yes'">3</xsl:when>
      <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- extra height for border -->
  <xsl:variable name="extraHeight">
    <xsl:choose>
      <xsl:when test="($title = '') and ($showBorder = 'yes')">3</xsl:when>
      <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

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
      <xsl:when test="/rules/bounds">
        <xsl:value-of select="/rules/bounds/@minlat"/>
      </xsl:when>
      <xsl:when test="$data/osm/bounds">
        <xsl:value-of select="$data/osm/bounds/@minlat"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$bllat"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="bottomLeftLongitude">
    <xsl:choose>
      <xsl:when test="/rules/bounds">
        <xsl:value-of select="/rules/bounds/@minlon"/>
      </xsl:when>
      <xsl:when test="$data/osm/bounds">
        <xsl:value-of select="$data/osm/bounds/@minlon"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$bllon"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="topRightLatitude">
    <xsl:choose>
      <xsl:when test="/rules/bounds">
        <xsl:value-of select="/rules/bounds/@maxlat"/>
      </xsl:when>
      <xsl:when test="$data/osm/bounds">
        <xsl:value-of select="$data/osm/bounds/@maxlat"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$trlat"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="topRightLongitude">
    <xsl:choose>
      <xsl:when test="/rules/bounds">
        <xsl:value-of select="/rules/bounds/@maxlon"/>
      </xsl:when>
      <xsl:when test="$data/osm/bounds">
        <xsl:value-of select="$data/osm/bounds/@maxlon"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$trlon"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- Derive the latitude of the middle of the map -->
  <xsl:variable name="middleLatitude" select="($topRightLatitude + $bottomLeftLatitude) div 2.0"/>
  <!--woohoo lets do trigonometry in xslt -->
  <!--convert latitude to radians -->
  <xsl:variable name="latr" select="$middleLatitude * 3.1415926 div 180.0"/>
  <!--taylor series: two terms is 1% error at lat<68 and 10% error lat<83. we probably need polar projection by then -->
  <xsl:variable name="coslat" select="1 - ($latr * $latr) div 2 + ($latr * $latr * $latr * $latr) div 24"/>
  <xsl:variable name="projection" select="1 div $coslat"/>

  <xsl:variable name="dataWidth" select="(number($topRightLongitude)-number($bottomLeftLongitude))*10000*$scale"/>
  <xsl:variable name="dataHeight" select="(number($topRightLatitude)-number($bottomLeftLatitude))*10000*$scale*$projection"/>
  <xsl:variable name="km" select="(0.0089928*$scale*10000*$projection)"/>

  <xsl:variable name="documentWidth">
    <xsl:choose>
      <xsl:when test="$dataWidth &gt; (number(/rules/@minimumMapWidth) * $km)">
        <xsl:value-of select="$dataWidth"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="number(/rules/@minimumMapWidth) * $km"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="documentHeight">
    <xsl:choose>
      <xsl:when test="$dataHeight &gt; (number(/rules/@minimumMapHeight) * $km)">
        <xsl:value-of select="$dataHeight"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="number(/rules/@minimumMapHeight) * $km"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="width" select="($documentWidth div 2) + ($dataWidth div 2)"/>
  <xsl:variable name="height" select="($documentHeight div 2) + ($dataHeight div 2)"/>



  <!-- Main template -->
  <xsl:template match="/rules">

    <!-- Include an external css stylesheet if one was specified in the rules file -->
    <xsl:if test="@xml-stylesheet">
      <xsl:processing-instruction name="xml-stylesheet">
        href="<xsl:value-of select="@xml-stylesheet"/>" type="text/css"
      </xsl:processing-instruction>
    </xsl:if>

    <xsl:variable name="svgWidth" select="$documentWidth + $extraWidth"/>
    <xsl:variable name="svgHeight" select="$documentHeight + $marginaliaTopHeight + $marginaliaBottomHeight"/>

    <svg id="main"
  version="1.1"
  baseProfile="{$svgBaseProfile}"
  width="{$svgWidth}px"
  height="{$svgHeight}px"
  preserveAspectRatio="none"
  viewBox="{-$extraWidth div 2} {-$extraHeight div 2} {$svgWidth} {$svgHeight}">
      <xsl:if test="/rules/@interactive='yes'">
        <xsl:attribute name="onscroll">fnOnScroll(evt)</xsl:attribute>
        <xsl:attribute name="onzoom">fnOnZoom(evt)</xsl:attribute>
        <xsl:attribute name="onload">fnOnLoad(evt)</xsl:attribute>
        <xsl:attribute name="onmousedown">fnOnMouseDown(evt)</xsl:attribute>
        <xsl:attribute name="onmousemove">fnOnMouseMove(evt)</xsl:attribute>
        <xsl:attribute name="onmouseup">fnOnMouseUp(evt)</xsl:attribute>
      </xsl:if>

      <xsl:call-template name="metadata"/>

      <!-- Include javaScript functions for all the dynamic stuff -->
      <xsl:if test="/rules/@interactive='yes'">
        <xsl:call-template name="javaScript"/>
      </xsl:if>


      <defs id="defs-rulefile">
        <!-- Get any <defs> and styles from the rules file -->
        <xsl:copy-of select="defs/*"/>
      </defs>


      <xsl:if test="$symbolsDir != ''">
        <!-- Get all symbols mentioned in the rules file from the symbolsDir -->
        <defs id="defs-symbols">
          <xsl:for-each select="/rules//symbol/@ref">
            <xsl:copy-of select="document(concat($symbolsDir,'/', ., '.svg'))/svg:svg/svg:defs/svg:symbol"/>
          </xsl:for-each>
        </defs>
      </xsl:if>

      <!-- Pre-generate named path definitions for all ways -->
      <xsl:variable name="allWays" select="$data/osm/way"/>
      <defs id="defs-ways">
        <xsl:for-each select="$allWays">
          <xsl:call-template name="generateWayPaths"/>
        </xsl:for-each>
      </defs>

      <!-- Clipping rectangle for map -->
      <clipPath id="map-clipping">
        <rect id="map-clipping-rect" x="0px" y="0px" height="{$documentHeight}px" width="{$documentWidth}px"/>
      </clipPath>

      <g id="map" clip-path="url(#map-clipping)" inkscape:groupmode="layer" inkscape:label="Map" transform="translate(0,{$marginaliaTopHeight})">
        <!-- Draw a nice background layer -->
        <rect id="background" x="0px" y="0px" height="{$documentHeight}px" width="{$documentWidth}px" class="map-background"/>

        <!-- Process all the rules drawing all map features -->
        <xsl:call-template name="processRules"/>
      </g>

      <!-- Draw map decoration -->
      <g id="map-decoration" inkscape:groupmode="layer" inkscape:label="Map decoration" transform="translate(0,{$marginaliaTopHeight})">
        <!-- Draw a grid if required -->
        <xsl:if test="$showGrid='yes'">
          <xsl:call-template name="drawGrid"/>
        </xsl:if>

        <!-- Draw a border if required -->
        <xsl:if test="$showBorder='yes'">
          <xsl:call-template name="drawBorder"/>
        </xsl:if>
      </g>

      <!-- Draw map marginalia -->
      <xsl:if test="($title != '') or ($showScale = 'yes') or ($showLicense = 'yes')">
        <g id="marginalia" inkscape:groupmode="layer" inkscape:label="Marginalia">
          <!-- Draw the title -->
          <xsl:if test="$title!=''">
            <xsl:call-template name="drawTitle">
              <xsl:with-param name="title" select="$title"/>
            </xsl:call-template>
          </xsl:if>

          <xsl:if test="($showScale = 'yes') or ($showLicense = 'yes')">
            <g id="marginalia-bottom" inkscape:groupmode="layer" inkscape:label="Marginalia (Bottom)" transform="translate(0,{$marginaliaTopHeight})">
              <!-- Draw background for marginalia at bottom -->
              <rect id="marginalia-background" x="0px" y="{$documentHeight + 5}px" height="40px" width="{$documentWidth}px" class="map-marginalia-background"/>

              <!-- Draw the scale in the bottom left corner -->
              <xsl:if test="$showScale='yes'">
                <xsl:call-template name="drawScale"/>
              </xsl:if>

              <!-- Draw Creative commons license -->
              <xsl:if test="$showLicense='yes'">
                <xsl:call-template name="in-image-license">
                  <xsl:with-param name="dx" select="$documentWidth"/>
                  <xsl:with-param name="dy" select="$documentHeight"/>
                </xsl:call-template>
              </xsl:if>
            </g>
          </xsl:if>
        </g>
      </xsl:if>

      <!-- Draw labels and controls that are in a static position -->
      <g id="staticElements" transform="scale(1) translate(0,0)">
        <!-- Draw the +/- zoom controls -->
        <xsl:if test="/rules/@interactive='yes'">
          <xsl:call-template name="zoomControl"/>
        </xsl:if>
      </g>
    </svg>

  </xsl:template>

  <!-- Path Fragment Drawing -->
  <xsl:template name="drawPath">
    <xsl:param name='instruction' />
    <xsl:param name='pathId'/>
    <xsl:param name='extraClasses'/>

    <xsl:variable name="maskId" select="concat('mask_',$pathId)"/>

    <xsl:call-template name='generateMask'>
      <xsl:with-param name='instruction' select='$instruction'/>
      <xsl:with-param name='pathId' select='$pathId'/>
      <xsl:with-param name='maskId' select='$maskId'/>
    </xsl:call-template>

    <use xlink:href="#{$pathId}">
      <!-- Copy all attributes from instruction -->
      <xsl:apply-templates select="$instruction/@*" mode="copyAttributes" />
      <!-- Add in any extra classes -->
      <xsl:attribute name="class">
        <xsl:value-of select='$instruction/@class'/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$extraClasses"/>
      </xsl:attribute>
      <!-- If there is a mask class then include the mask attribute -->
      <xsl:if test='$instruction/@mask-class'>
        <xsl:attribute name="mask">url(#<xsl:value-of select="$maskId"/>)</xsl:attribute>
      </xsl:if>
      <xsl:call-template name="getSvgAttributesFromOsmTags"/>
    </use>
  </xsl:template>


  <xsl:template name='generateMask'>
    <xsl:param name='instruction' />
    <xsl:param name='pathId'/>
    <xsl:param name='maskId'/>

    <!-- If the instruction has a mask class -->
    <xsl:if test='$instruction/@mask-class'>
      <mask id="{$maskId}" maskUnits="userSpaceOnUse">
        <use xlink:href="#{$pathId}" class="{$instruction/@mask-class} osmarender-stroke-linecap-round osmarender-mask-black" />
        <!-- Required for Inkscape bug -->
        <use xlink:href="#{$pathId}" class="{$instruction/@class} osmarender-mask-white" />
        <use xlink:href="#{$pathId}" class="{$instruction/@mask-class} osmarender-stroke-linecap-round osmarender-mask-black" />
      </mask>
    </xsl:if>
  </xsl:template>



  <!-- Draw a line for the current <way> element using the formatting of the current <line> instruction -->
  <xsl:template name="drawWay">
    <xsl:param name="instruction"/>
    <xsl:param name="way"/>
    <!-- The current way element if applicable -->
    <xsl:param name="layer"/>

    <xsl:variable name="extraClasses">
      <xsl:if test="$instruction/@suppress-markers-tag != ''">
        <xsl:variable name="suppressMarkersTag" select="$instruction/@suppress-markers-tag" />
        <xsl:variable name="firstNode" select="key('nodeById',$way/nd[1]/@ref)"/>
        <xsl:variable name="firstNodeMarkerGroupConnectionCount"
                      select="count(key('wayByNode',$firstNode/@id)/tag[@k=$suppressMarkersTag and ( @v = 'yes' or @v = 'true' )])" />
        <xsl:variable name="lastNode" select="key('nodeById',$way/nd[last()]/@ref)"/>
        <xsl:variable name="lastNodeMarkerGroupConnectionCount"
                      select="count(key('wayByNode',$lastNode/@id)/tag[@k=$suppressMarkersTag and ( @v = 'yes' or @v = 'true' )])" />
       
        <xsl:if test="$firstNodeMarkerGroupConnectionCount > 1">osmarender-no-marker-start</xsl:if>
        <xsl:if test="$lastNodeMarkerGroupConnectionCount > 1"> osmarender-no-marker-end</xsl:if>
      </xsl:if>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$instruction/@smart-linecap='no'">
        <xsl:call-template name='drawPath'>
          <xsl:with-param name='pathId' select="concat('way_normal_',$way/@id)"/>
          <xsl:with-param name='instruction' select='$instruction'/>
          <xsl:with-param name="extraClasses" select='$extraClasses'/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="drawWayWithSmartLinecaps">
          <xsl:with-param name="instruction" select="$instruction"/>
          <xsl:with-param name="way" select="$way"/>
          <xsl:with-param name="layer" select="$layer"/>
          <xsl:with-param name="extraClasses" select='$extraClasses'/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="drawWayWithSmartLinecaps">
    <xsl:param name="instruction"/>
    <xsl:param name="way"/>
    <!-- The current way element if applicable -->
    <xsl:param name="layer"/>
    <xsl:param name="extraClasses"/>

    <!-- The first half of the first segment and the last half of the last segment are treated differently from the main
			part of the way path.  The main part is always rendered with a butt line-cap.  Each end fragment is rendered with
			either a round line-cap, if it connects to some other path, or with its default line-cap if it is not connected
			to anything.  That way, cul-de-sacs etc are terminated with round, square or butt as specified in the style for the
			way. -->

    <!-- First draw the middle section of the way with round linejoins and butt linecaps -->
    <xsl:if test="count($way/nd) &gt; 1">
      <xsl:call-template name='drawPath'>
        <xsl:with-param name='pathId' select="concat('way_mid_',$way/@id)"/>
        <xsl:with-param name='instruction' select='$instruction'/>
        <xsl:with-param name='extraClasses'>osmarender-stroke-linecap-butt osmarender-no-marker-start osmarender-no-marker-end</xsl:with-param>
      </xsl:call-template>
    </xsl:if>


    <!-- For the first half segment in the way, count the number of segments that link to the from-node of this segment.
			Also count links where the layer tag is less than the layer of this way, if there are links on a lower layer then
			we can safely draw a butt line-cap because the lower layer will already have a round line-cap. -->
    <!-- Process the first segment in the way -->
    <xsl:variable name="firstNode" select="key('nodeById',$way/nd[1]/@ref)"/>

    <!-- Count the number of segments connecting to the from node. If there is only one (the current segment) then draw a default line.  -->
    <xsl:variable name="firstNodeConnectionCount" select="count(key('wayByNode',$firstNode/@id))" />

    <!-- Count the number of connectors at a layer lower than the current layer -->
    <xsl:variable name="firstNodeLowerLayerConnectionCount" select="
			count(key('wayByNode',$firstNode/@id)/tag[@k='layer' and @v &lt; $layer]) +
			count(key('wayByNode',$firstNode/@id)[count(tag[@k='layer'])=0 and $layer &gt; 0])
			" />
    <xsl:choose>
      <xsl:when test="$firstNodeConnectionCount=1">
        <xsl:call-template name='drawPath'>
          <xsl:with-param name='pathId' select="concat('way_start_',$way/@id)"/>
          <xsl:with-param name='instruction' select='$instruction'/>
          <xsl:with-param name="extraClasses"><xsl:value-of select="$extraClasses"/> osmarender-no-marker-end</xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$firstNodeLowerLayerConnectionCount>0">
        <xsl:call-template name='drawPath'>
          <xsl:with-param name='pathId' select="concat('way_start_',$way/@id)"/>
          <xsl:with-param name='instruction' select='$instruction'/>
          <xsl:with-param name="extraClasses"><xsl:value-of select="$extraClasses"/> osmarender-stroke-linecap-butt osmarender-no-marker-end</xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name='drawPath'>
          <xsl:with-param name='pathId' select="concat('way_start_',$way/@id)"/>
          <xsl:with-param name='instruction' select='$instruction'/>
          <xsl:with-param name="extraClasses"><xsl:value-of select="$extraClasses"/>  osmarender-stroke-linecap-round osmarender-no-marker-end</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>

    </xsl:choose>


    <!-- Process the last segment in the way -->
    <xsl:variable name="lastNode" select="key('nodeById',$way/nd[last()]/@ref)"/>

    <!-- Count the number of segments connecting to the last node. If there is only one (the current segment) then draw
		     a default line.  -->
    <xsl:variable name="lastNodeConnectionCount" select="count(key('wayByNode',$lastNode/@id))" />

    <!-- Count the number of connectors at a layer lower than the current layer -->
    <xsl:variable name="lastNodeLowerLayerConnectionCount" select="
			count(key('wayByNode',$lastNode/@id)/tag[@k='layer' and @v &lt; $layer]) +
			count(key('wayByNode',$lastNode/@id)[count(tag[@k='layer'])=0 and $layer &gt; 0])
			" />


    <xsl:choose>
      <xsl:when test="$lastNodeConnectionCount=1">
        <xsl:call-template name='drawPath'>
          <xsl:with-param name='pathId' select="concat('way_end_',$way/@id)"/>
          <xsl:with-param name='instruction' select='$instruction'/>
          <xsl:with-param name="extraClasses"><xsl:value-of select="$extraClasses"/> osmarender-no-marker-start</xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$lastNodeLowerLayerConnectionCount>0">
        <xsl:call-template name='drawPath'>
          <xsl:with-param name='pathId' select="concat('way_end_',$way/@id)"/>
          <xsl:with-param name='instruction' select='$instruction'/>
          <xsl:with-param name="extraClasses"><xsl:value-of select="$extraClasses"/> osmarender-stroke-linecap-butt osmarender-no-marker-start</xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name='drawPath'>
          <xsl:with-param name='pathId' select="concat('way_end_',$way/@id)"/>
          <xsl:with-param name='instruction' select='$instruction'/>
          <xsl:with-param name="extraClasses"><xsl:value-of select="$extraClasses"/> osmarender-stroke-linecap-round osmarender-no-marker-start</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>

    </xsl:choose>

  </xsl:template>


  <!-- Draw a circle for the current <node> element using the formatting of the current <circle> instruction -->
  <xsl:template name="drawCircle">
    <xsl:param name="instruction"/>

    <xsl:variable name="x" select="($width)-((($topRightLongitude)-(@lon))*10000*$scale)"/>
    <xsl:variable name="y" select="($height)+((($bottomLeftLatitude)-(@lat))*10000*$scale*$projection)"/>

    <circle cx="{$x}" cy="{$y}">
      <xsl:apply-templates select="$instruction/@*" mode="copyAttributes"/>
      <!-- Copy all the svg attributes from the <circle> instruction -->
    </circle>

  </xsl:template>


  <!-- Draw a symbol for the current <node> element using the formatting of the current <symbol> instruction -->
  <xsl:template name="drawSymbol">
    <xsl:param name="instruction"/>

    <xsl:variable name="x" select="($width)-((($topRightLongitude)-(@lon))*10000*$scale)"/>
    <xsl:variable name="y" select="($height)+((($bottomLeftLatitude)-(@lat))*10000*$scale*$projection)"/>

    <g transform="translate({$x},{$y}) scale({$symbolScale})">
      <use>
        <xsl:if test="$instruction/@ref">
          <xsl:attribute name="xlink:href">
            <xsl:value-of select="concat('#symbol-', $instruction/@ref)"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="$instruction/@*" mode="copyAttributes"/>
	<!-- Copy all the attributes from the <symbol> instruction -->
      </use>
    </g>
  </xsl:template>


  <!-- Render the appropriate attribute of the current <node> element using the formatting of the current <text> instruction -->
  <xsl:template name="renderText">
    <xsl:param name="instruction"/>

    <xsl:variable name="x" select="($width)-((($topRightLongitude)-(@lon))*10000*$scale)"/>
    <xsl:variable name="y" select="($height)+((($bottomLeftLatitude)-(@lat))*10000*$scale*$projection)"/>

    <text>
      <xsl:apply-templates select="$instruction/@*" mode="copyAttributes"/>
      <xsl:attribute name="x">
        <xsl:value-of select="$x"/>
      </xsl:attribute>
      <xsl:attribute name="y">
        <xsl:value-of select="$y"/>
      </xsl:attribute>
      <xsl:call-template name="getSvgAttributesFromOsmTags"/>
      <xsl:value-of select="tag[@k=$instruction/@k]/@v"/>
    </text>
  </xsl:template>


  <!-- Render the appropriate attribute of the current <segment> element using the formatting of the current <textPath> instruction -->
  <xsl:template name="renderTextPath">
    <xsl:param name="instruction"/>
    <xsl:param name="pathId"/>
    <xsl:param name="pathDirection"/>
    <xsl:param name='text'/>

    <xsl:variable name='pathLengthSquared'>
      <xsl:call-template name='getPathLength'>
        <xsl:with-param name='pathLengthMultiplier'>
          <!-- This factor is used to adjust the path-length for comparison with text along a path to determine whether it will fit. -->
          <xsl:choose>
            <xsl:when test='$instruction/@textAttenuation'>
              <xsl:value-of select='$instruction/@textAttenuation'/>
            </xsl:when>
            <xsl:when test='string($textAttenuation)'>
              <xsl:value-of select='$textAttenuation'/>
            </xsl:when>
            <xsl:otherwise>99999999</xsl:otherwise>
          </xsl:choose>
        </xsl:with-param>
        <xsl:with-param name='nodes' select='nd'/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name='textLength' select='string-length($text)' />
    <xsl:variable name='textLengthSquared100' select='($textLength)*($textLength)' />
    <xsl:variable name='textLengthSquared90' select='($textLength *.9)*($textLength*.9)' />
    <xsl:variable name='textLengthSquared80' select='($textLength *.8)*($textLength*.8)' />
    <xsl:variable name='textLengthSquared70' select='($textLength *.7)*($textLength*.7)' />

    <xsl:choose>
      <xsl:when test='($pathLengthSquared) > $textLengthSquared100'>
        <text>
          <xsl:apply-templates select="$instruction/@*" mode="renderTextPath-text"/>
          <textPath xlink:href="#{$pathId}">
            <xsl:apply-templates select="$instruction/@*" mode="renderTextPath-textPath"/>
            <xsl:call-template name="getSvgAttributesFromOsmTags"/>
            <xsl:value-of select="$text"/>
          </textPath>
        </text>
      </xsl:when>
      <xsl:when test='($pathLengthSquared) > ($textLengthSquared90)'>
        <text>
          <xsl:apply-templates select="$instruction/@*" mode="renderTextPath-text"/>
          <textPath xlink:href="#{$pathId}">
            <xsl:attribute name='font-size'>90%</xsl:attribute>
            <xsl:apply-templates select="$instruction/@*" mode="renderTextPath-textPath"/>
            <xsl:call-template name="getSvgAttributesFromOsmTags"/>
            <xsl:value-of select="$text"/>
          </textPath>
        </text>
      </xsl:when>
      <xsl:when test='($pathLengthSquared) > ($textLengthSquared80)'>
        <text>
          <xsl:apply-templates select="$instruction/@*" mode="renderTextPath-text"/>
          <textPath xlink:href="#{$pathId}">
            <xsl:attribute name='font-size'>80%</xsl:attribute>
            <xsl:apply-templates select="$instruction/@*" mode="renderTextPath-textPath"/>
            <xsl:call-template name="getSvgAttributesFromOsmTags"/>
            <xsl:value-of select="$text"/>
          </textPath>
        </text>
      </xsl:when>
      <xsl:when test='($pathLengthSquared) > ($textLengthSquared70)'>
        <text>
          <xsl:apply-templates select="$instruction/@*" mode="renderTextPath-text"/>
          <textPath xlink:href="#{$pathId}">
            <xsl:attribute name='font-size'>70%</xsl:attribute>
            <xsl:apply-templates select="$instruction/@*" mode="renderTextPath-textPath"/>
            <xsl:call-template name="getSvgAttributesFromOsmTags"/>
            <xsl:value-of select="$text"/>
          </textPath>
        </text>
      </xsl:when>
      <xsl:otherwise />
      <!-- Otherwise don't render the text -->
    </xsl:choose>
  </xsl:template>


  <xsl:template name='getPathLength'>
    <xsl:param name='sumLon' select='number("0")' />
    <!-- initialise sum to zero -->
    <xsl:param name='sumLat' select='number("0")' />
    <!-- initialise sum to zero -->
    <xsl:param name='nodes'/>
    <xsl:param name='pathLengthMultiplier'/>
    <xsl:choose>
      <xsl:when test='$nodes[1] and $nodes[2]'>
        <xsl:variable name='fromNode' select='key("nodeById",$nodes[1]/@ref)'/>
        <xsl:variable name='toNode' select='key("nodeById",$nodes[2]/@ref)'/>
        <xsl:variable name='lengthLon' select='($fromNode/@lon)-($toNode/@lon)'/>
        <xsl:variable name='absLengthLon'>
          <xsl:choose>
            <xsl:when test='$lengthLon &lt; 0'>
              <xsl:value-of select='$lengthLon * -1'/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select='$lengthLon'/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name='lengthLat' select='($fromNode/@lat)-($toNode/@lat)'/>
        <xsl:variable name='absLengthLat'>
          <xsl:choose>
            <xsl:when test='$lengthLat &lt; 0'>
              <xsl:value-of select='$lengthLat * -1'/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select='$lengthLat'/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:call-template name='getPathLength'>
          <xsl:with-param name='sumLon' select='$sumLon+$absLengthLon'/>
          <xsl:with-param name='sumLat' select='$sumLat+$absLengthLat'/>
          <xsl:with-param name='nodes' select='$nodes[position()!=1]'/>
          <xsl:with-param name='pathLengthMultiplier' select='$pathLengthMultiplier'/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <!-- Add the square of the total horizontal length to the square of the total vertical length to get the square of
				     the total way length.  We don't have a sqrt() function so leave it squared.
				     Multiply by 1,000 so that we are usually dealing with a values greater than 1.  Squares of values between 0 and 1
				     are smaller and so not very useful.
				     Multiply the latitude component by $projection to adjust for Mercator projection issues. 
				     -->
        <xsl:value-of select='(
					(($sumLon*1000*$pathLengthMultiplier)*($sumLon*1000*$pathLengthMultiplier))+
					(($sumLat*1000*$pathLengthMultiplier*$projection)*($sumLat*1000*$pathLengthMultiplier*$projection))
					)'/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Suppress the following attributes, allow everything else -->
  <xsl:template match="@startOffset|@method|@spacing|@lengthAdjust|@textLength|@k" mode="renderTextPath-text" />

  <xsl:template match="@*" mode="renderTextPath-text">
    <xsl:copy/>
  </xsl:template>


  <!-- Allow the following attributes, suppress everything else -->
  <xsl:template match="@startOffset|@method|@spacing|@lengthAdjust|@textLength" mode="renderTextPath-textPath">
    <xsl:copy/>
  </xsl:template>

  <xsl:template match="@*" mode="renderTextPath-textPath" />


  <!-- If there are any tags like <tag k="svg:font-size" v="5"/> then add these as attributes of the svg output -->
  <xsl:template name="getSvgAttributesFromOsmTags">
    <xsl:for-each select="tag[contains(@k,'svg:')]">
      <xsl:attribute name="{substring-after(@k,'svg:')}">
        <xsl:value-of select="@v"/>
      </xsl:attribute>
    </xsl:for-each>
  </xsl:template>


  <xsl:template name="renderArea">
    <xsl:param name="instruction"/>
    <xsl:param name="pathId"/>

    <use xlink:href="#{$pathId}">
      <xsl:apply-templates select="$instruction/@*" mode="copyAttributes"/>
    </use>
  </xsl:template>


  <!-- Templates to process line, circle, text, etc. instructions -->
  <!-- Each template is passed a variable containing the set of elements that need to
         be processed.  The set of elements is already determined by the rules, so
         these templates don't need to know anything about the rules context they are in. -->

  <!-- Process a <line> instruction -->
  <xsl:template match="line">
    <xsl:param name="elements"/>
    <xsl:param name="layer"/>

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name="instruction" select="."/>

    <g>
      <xsl:apply-templates select="@*" mode="copyAttributes" />
      <!-- Add all the svg attributes of the <line> instruction to the <g> element -->

      <!-- For each way -->
      <xsl:apply-templates select="$elements" mode="line">
        <xsl:with-param name="instruction" select="$instruction"/>
        <xsl:with-param name="layer" select="$layer"/>
      </xsl:apply-templates>

    </g>
  </xsl:template>


  <!-- Suppress output of any unhandled elements -->
  <xsl:template match="*" mode="line"/>


  <!-- Draw lines for a way  -->
  <xsl:template match="way" mode="line">
    <xsl:param name="instruction"/>
    <xsl:param name="layer"/>

    <!-- The current <way> element -->
    <xsl:variable name="way" select="."/>

    <!-- DODI: !!!WORKAROUND!!! skip one node ways-->
    <xsl:if test="count($way/nd) &gt; 1">
      <xsl:call-template name="drawWay">
        <xsl:with-param name="instruction" select="$instruction"/>
        <xsl:with-param name="way" select="$way"/>
        <xsl:with-param name="layer" select="$layer"/>
      </xsl:call-template>
    </xsl:if >
  </xsl:template>


  <!-- Draw lines for a relation -->
  <xsl:template match="relation" mode="line">
    <xsl:param name="instruction"/>
    <xsl:param name="layer"/>

    <xsl:variable name="relation" select="@id"/>

    <xsl:if test="(tag[@k='type']/@v='route') and ($showRelationRoute!='~|no')">
      <!-- Draw lines for a RelationRoute -->
      <xsl:for-each select="$data/osm/relation[@id=$relation]/member[@type='way']">
        <xsl:variable name="wayid" select="@ref"/>

        <xsl:for-each select="$data/osm/way[@id=$wayid]">
          <!-- The current <way> element -->
          <xsl:variable name="way" select="."/>

          <!-- DODI: !!!WORKAROUND!!! skip one node ways-->
          <xsl:if test="count($way/nd) &gt; 1">
            <xsl:call-template name="drawWay">
              <xsl:with-param name="instruction" select="$instruction"/>
              <xsl:with-param name="way" select="$way"/>
              <xsl:with-param name="layer" select="$layer"/>
            </xsl:call-template>
          </xsl:if >
        </xsl:for-each >
      </xsl:for-each >
    </xsl:if>

    <!-- Handle other types of Relations if necessary -->

  </xsl:template>


  <!-- Process an <area> instruction -->
  <xsl:template match="area">
    <xsl:param name="elements"/>

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name="instruction" select="."/>

    <g>
      <xsl:apply-templates select="@*" mode="copyAttributes"/>
      <!-- Add all the svg attributes of the <line> instruction to the <g> element -->

      <!-- For each way -->
      <xsl:apply-templates select="$elements" mode="area">
        <xsl:with-param name="instruction" select="$instruction"/>
      </xsl:apply-templates>
    </g>
  </xsl:template>


  <!-- Discard anything that is not matched by a more specific template -->
  <xsl:template match="*" mode="area"/>


  <!-- Draw area for a <way> -->
  <xsl:template match="way" mode="area">
    <xsl:param name="instruction"/>

    <!-- DODI:  removed because duplicate definition generated if area referenced 2 or more times -->
    <!-- DODI:  reenabled because of "duplicate point detection in lines2curves.pl " -->
    <!-- <xsl:call-template name="generateAreaPath"/> -->

    <xsl:variable name="pathArea">
      <xsl:call-template name="generateAreaPath"/>
    </xsl:variable>

    <!-- DODI: do now draw empty ways/areas-->
    <xsl:if test ="$pathArea!=''">
      <path id="area_{@id}" d="{$pathArea}"/>
      <xsl:call-template name="renderArea">
        <xsl:with-param name="instruction" select="$instruction"/>
        <xsl:with-param name="pathId" select="concat('area_',@id)"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <!-- Process <circle> instruction -->
  <xsl:template match="circle">
    <xsl:param name="elements"/>
    <xsl:param name="layer"/>

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name="instruction" select="."/>

    <!-- For each circle -->
    <xsl:apply-templates select="$elements" mode="circle">
      <xsl:with-param name="instruction" select="$instruction"/>
      <xsl:with-param name="layer" select="$layer"/>
      <xsl:with-param name="elements" select="$elements"/>
    </xsl:apply-templates>
  </xsl:template>


  <!-- Suppress output of any unhandled elements -->
  <xsl:template match="*" mode="circle"/>


  <!-- Draw circle for a node -->
  <xsl:template match="node" mode="circle">
    <xsl:param name="instruction"/>
    <xsl:param name="elements"/>

    <xsl:for-each select="$elements[name()='node']">
      <xsl:call-template name="drawCircle">
        <xsl:with-param name="instruction" select="$instruction"/>
      </xsl:call-template>
    </xsl:for-each>

  </xsl:template>


  <!-- Draw circle for a relation -->
  <xsl:template match="relation" mode="circle">
    <xsl:param name="instruction"/>
    <xsl:param name="layer"/>

    <xsl:variable name="relation" select="@id"/>

    <xsl:if test="(tag[@k='type']/@v='route') and ($showRelationRoute!='~|no')">
      <!-- Draw Circles for a RelationRoute Stop -->
      <xsl:for-each select="$data/osm/relation[@id=$relation]/member[@type='node']">
        <xsl:variable name="nodeid" select="@ref"/>

        <xsl:for-each select="$data/osm/node[@id=$nodeid]">
          <xsl:call-template name="drawCircle">
            <xsl:with-param name="instruction" select="$instruction"/>
            <xsl:with-param name="node" select="@id"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:if>

    <!-- Handle other types of Relations if necessary -->

  </xsl:template>


  <!-- Process a <symbol> instruction -->
  <xsl:template match="symbol">
    <xsl:param name="elements"/>

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name="instruction" select="."/>

    <xsl:for-each select="$elements[name()='node']">
      <xsl:call-template name="drawSymbol">
        <xsl:with-param name="instruction" select="$instruction"/>
      </xsl:call-template>
    </xsl:for-each>

  </xsl:template>

  <!-- wayMarker instruction.  Draws a marker on a node that is perpendicular to a way that passes through the node.
       If more than one way passes through the node then the result is a bit unspecified.  -->
  <xsl:template match="wayMarker">
    <xsl:param name="elements"/>
    
    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name="instruction" select="."/>
    
    <g>
      <!-- Add all the svg attributes of the <wayMarker> instruction to the <g> element -->
      <xsl:apply-templates select="@*" mode="copyAttributes" />
      
      <!-- Process each matched node in turn -->
      <xsl:for-each select="$elements[name()='node']">
	<xsl:variable name='nodeId' select="@id" />
	
	<xsl:variable name='way' select="key('wayByNode', @id)" />
	<xsl:variable name='previousNode' select="key('nodeById', $way/nd[@ref=$nodeId]/preceding-sibling::nd[1]/@ref)" />
	<xsl:variable name='nextNode' select="key('nodeById', $way/nd[@ref=$nodeId]/following-sibling::nd[1]/@ref)" />
	
	<xsl:variable name='path'>
	  <xsl:choose>
	    <xsl:when test='$previousNode and $nextNode'>
	      <xsl:call-template name="moveToNode">
		<xsl:with-param name="node" select="$previousNode"/>
	      </xsl:call-template>
	      <xsl:call-template name="lineToNode">
		<xsl:with-param name="node" select="."/>
	      </xsl:call-template>
	      <xsl:call-template name="lineToNode">
		<xsl:with-param name="node" select="$nextNode"/>
	      </xsl:call-template>
	    </xsl:when>

	    <xsl:when test='$previousNode'>
	      <xsl:call-template name="moveToNode">
		<xsl:with-param name="node" select="$previousNode"/>
	      </xsl:call-template>
	      <xsl:call-template name="lineToNode">
		<xsl:with-param name="node" select="."/>
	      </xsl:call-template>
	      <xsl:call-template name="lineToNode">
		<xsl:with-param name="node" select="."/>
	      </xsl:call-template>
	    </xsl:when>

	    <xsl:when test='$nextNode'>
	      <xsl:call-template name="moveToNode">
		<xsl:with-param name="node" select="."/>
	      </xsl:call-template>
	      <xsl:call-template name="lineToNode">
		<xsl:with-param name="node" select="$nextNode"/>
	      </xsl:call-template>
	      <xsl:call-template name="lineToNode">
		<xsl:with-param name="node" select="$nextNode"/>
	      </xsl:call-template>
	    </xsl:when>
	  </xsl:choose>
	</xsl:variable>
	
	<path id="nodePath_{@id}" d="{$path}"/>
	
	<use xlink:href="#nodePath_{@id}">
	  <xsl:apply-templates select="$instruction/@*" mode="copyAttributes" />
	</use>
      </xsl:for-each>
    </g>
    
  </xsl:template>

  <!-- Process an <areaText> instruction -->
  <xsl:template match="areaText">
    <xsl:param name="elements"/>

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name="instruction" select="."/>

    <!-- Select all <way> elements that have a key that matches the k attribute of the text instruction -->
    <xsl:apply-templates select="$elements[name()='way'][tag[@k=$instruction/@k]]" mode="areaTextPath">
      <xsl:with-param name="instruction" select="$instruction"/>
    </xsl:apply-templates>
  </xsl:template>


  <xsl:template match="*" mode="areaTextPath"/>


  <xsl:template match="way" mode="areaTextPath">
    <xsl:param name="instruction"/>

    <!-- The current <way> element -->
    <xsl:variable name="way" select="."/>

    <xsl:call-template name="renderAreaText">
      <xsl:with-param name="instruction" select="$instruction"/>
      <xsl:with-param name="pathId" select="concat('way_normal_',@id)"/>
    </xsl:call-template>

  </xsl:template>


  <xsl:template name="renderAreaText">
    <xsl:param name="instruction"/>

    <xsl:variable name='center'>
      <xsl:call-template name="areaCenter">
	<xsl:with-param name="element" select="." />
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="centerLon" select="substring-before($center, ',')" />
    <xsl:variable name="centerLat" select="substring-after($center, ',')" />

    <xsl:variable name="x" select="($width)-((($topRightLongitude)-($centerLon))*10000*$scale)"/>
    <xsl:variable name="y" select="($height)+((($bottomLeftLatitude)-($centerLat))*10000*$scale*$projection)"/>

    <text>
      <xsl:apply-templates select="$instruction/@*" mode="copyAttributes"/>
      <xsl:attribute name="x">
        <xsl:value-of select="$x"/>
      </xsl:attribute>
      <xsl:attribute name="y">
        <xsl:value-of select="$y"/>
      </xsl:attribute>
      <xsl:call-template name="getSvgAttributesFromOsmTags"/>
      <xsl:value-of select="tag[@k=$instruction/@k]/@v"/>
    </text>
  </xsl:template>

  <!-- Process an <areaSymbol> instruction -->
  <xsl:template match="areaSymbol">
    <xsl:param name="elements"/>

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name="instruction" select="."/>

    <!-- Select all <way> elements -->
    <xsl:apply-templates select="$elements[name()='way']" mode="areaSymbolPath">
      <xsl:with-param name="instruction" select="$instruction"/>
    </xsl:apply-templates>
  </xsl:template>


  <xsl:template match="*" mode="areaSymbolPath"/>


  <xsl:template match="way" mode="areaSymbolPath">
    <xsl:param name="instruction"/>

    <!-- The current <way> element -->
    <xsl:variable name="way" select="."/>

    <xsl:call-template name="renderAreaSymbol">
      <xsl:with-param name="instruction" select="$instruction"/>
      <xsl:with-param name="pathId" select="concat('way_normal_',@id)"/>
    </xsl:call-template>

  </xsl:template>


  <xsl:template name="renderAreaSymbol">
    <xsl:param name="instruction"/>

    <xsl:variable name='center'>
      <xsl:call-template name="areaCenter">
	<xsl:with-param name="element" select="." />
      </xsl:call-template>
    </xsl:variable>

    <xsl:message>
      areaCenter: <xsl:value-of select="$center" />
    </xsl:message>

    <xsl:variable name="centerLon" select="substring-before($center, ',')" />
    <xsl:variable name="centerLat" select="substring-after($center, ',')" />

    <xsl:variable name="x" select="($width)-((($topRightLongitude)-($centerLon))*10000*$scale)"/>
    <xsl:variable name="y" select="($height)+((($bottomLeftLatitude)-($centerLat))*10000*$scale*$projection)"/>

    <g transform="translate({$x},{$y}) scale({$symbolScale})">
      <use>
        <xsl:if test="$instruction/@ref">
          <xsl:attribute name="xlink:href">
            <xsl:value-of select="concat('#symbol-', $instruction/@ref)"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="$instruction/@*" mode="copyAttributes"/>
        <!-- Copy all the attributes from the <symbol> instruction -->
      </use>
    </g>
  </xsl:template>

  <!--
      areaCenter: Find a good center point for label/icon placement inside of polygon.
      Algorithm is described at http://bob.cakebox.net/poly-center.php
  -->
  <xsl:template name="areaCenter">
    <xsl:param name="element" />

    <!-- Get multipolygon relation for areas with holes -->
    <xsl:variable name='holerelation' select="key('relationByWay',$element/@id)[tag[@k='type' and @v='multipolygon']]"/>

    <!-- A semicolon-separated list of x,y coordinate pairs of points lying halfway into the polygon at angles to the vertex -->
    <xsl:variable name="points">
      <xsl:call-template name="areacenterPointsInside">
	<xsl:with-param name="element" select="$element" />
	<xsl:with-param name="holerelation" select="$holerelation" />
      </xsl:call-template>
    </xsl:variable>

    <!-- x,y calculated by a simple average over all x/y's in points -->
    <xsl:variable name="mediumpoint">
      <xsl:call-template name="areacenterMediumOfPoints">
	<xsl:with-param name="points" select="$points" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="mediumpoint_x" select="substring-before($mediumpoint, ',')" />
    <xsl:variable name="mediumpoint_y" select="substring-before(substring-after($mediumpoint, ','), ',')" />
    <xsl:variable name="medium_dist" select="substring-after(substring-after($mediumpoint, ','), ',')" />

    <!-- Find out if mediumpoint is inside or outside the polygon -->
    <xsl:variable name="intersection">
      <xsl:call-template name="areacenterNearestIntersectionInside">
	<xsl:with-param name="x" select="$mediumpoint_x" />
	<xsl:with-param name="y" select="$mediumpoint_y" />
	<xsl:with-param name="edgestart" select="$element/nd[1]" />
	<xsl:with-param name="linepoint_x" select="$mediumpoint_x" />
	<xsl:with-param name="linepoint_y" select="$mediumpoint_y + 1" />
	<xsl:with-param name="holerelation" select="$holerelation" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="intersection_count" select="substring-before($intersection, ';')" />

    <xsl:variable name="nearestEdge">
      <xsl:call-template name="areacenterNearestEdge">
	<xsl:with-param name="x" select="$mediumpoint_x" />
	<xsl:with-param name="y" select="$mediumpoint_y" />
	<xsl:with-param name="edgestart" select="$element/nd[1]" />
	<xsl:with-param name="holerelation" select="$holerelation" />
      </xsl:call-template>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$intersection_count mod 2 = 0 or $nearestEdge div 2 * 1.20 &gt; $medium_dist">
	<!-- Find the best point in $points to use -->
	<xsl:call-template name="areacenterBestPoint">
	  <xsl:with-param name="points" select="$points" />
	  <xsl:with-param name="x" select="$mediumpoint_x" />
	  <xsl:with-param name="y" select="$mediumpoint_y" />
	  <xsl:with-param name="medium_dist" select="$medium_dist" />
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="$mediumpoint_x"/>,<xsl:value-of select="$mediumpoint_y"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Returns a semicolon-separated list of x,y pairs -->
  <xsl:template name="areacenterPointsInside">
    <xsl:param name="element" />
    <xsl:param name="holerelation" />

    <!-- iterate over every vertex except the first one, which is also the last -->
    <xsl:for-each select="$element/nd[position() &gt; 1]">
      <xsl:variable name="vertex" select="." />
      <xsl:variable name="prev" select="$vertex/preceding-sibling::nd[1]" />
      <xsl:variable name="nextId">
	<xsl:choose>
	  <xsl:when test="position() &lt; last()">
	    <xsl:value-of select="$vertex/following-sibling::nd[1]/@ref" />
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:value-of select="$vertex/../nd[2]/@ref" />
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:variable>
      <xsl:variable name="next" select="$vertex/../nd[@ref=$nextId]" />

      <!-- Angle at between $prev and $next in $vertex -->
      <xsl:variable name="angle">
	<xsl:call-template name="angleThroughPoints">
	  <xsl:with-param name="from" select="key('nodeById', $prev/@ref)" />
	  <xsl:with-param name="through" select="key('nodeById', $vertex/@ref)" />
	  <xsl:with-param name="to" select="key('nodeById', $next/@ref)" />
	</xsl:call-template>
      </xsl:variable>

      <!-- Calculate a point on the line going through $vertex at $angle -->
      <xsl:variable name="linepoint">
	<xsl:call-template name="areacenterLinepoint">
	  <xsl:with-param name="point" select="key('nodeById', $vertex/@ref)" />
	  <xsl:with-param name="angle" select="$angle" />
	</xsl:call-template>
      </xsl:variable>
      <xsl:variable name="linepoint_x" select="substring-before($linepoint, ',')" />
      <xsl:variable name="linepoint_y" select="substring-after($linepoint, ',')" />

      <!-- Find the nearest intersection between the line vertex-linepoint and the nearest edge inwards into the polygon -->
      <xsl:variable name="intersection">
	<xsl:call-template name="areacenterNearestIntersectionInside">
	  <xsl:with-param name="x" select="key('nodeById', $vertex/@ref)/@lon" />
	  <xsl:with-param name="y" select="key('nodeById', $vertex/@ref)/@lat" />
	  <xsl:with-param name="edgestart" select="../nd[1]" />
	  <xsl:with-param name="linepoint_x" select="$linepoint_x" />
	  <xsl:with-param name="linepoint_y" select="$linepoint_y" />
	  <xsl:with-param name="holerelation" select="$holerelation" />
	</xsl:call-template>
      </xsl:variable>
      <xsl:variable name="intersection_count" select="substring-before($intersection, ';')" />
      <xsl:variable name="intersection_data">
	<xsl:choose>
	  <xsl:when test="$intersection_count mod 2 != 0">
	    <xsl:value-of select="substring-before(substring-after($intersection, ';'), ';')" />
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:value-of select="substring-after(substring-after($intersection, ';'), ';')" />
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:variable>
      <xsl:variable name="intersection_x" select="substring-before($intersection_data, ',')" />
      <xsl:variable name="intersection_y" select="substring-before(substring-after($intersection_data, ','), ',')" />
      <xsl:variable name="intersection_dist" select="substring-before(substring-after(substring-after($intersection_data, ','), ','), ',')" />

      <xsl:variable name="point_x" select="key('nodeById', $vertex/@ref)/@lon + ( $intersection_x - key('nodeById', $vertex/@ref)/@lon ) div 2" />
      <xsl:variable name="point_y" select="key('nodeById', $vertex/@ref)/@lat + ( $intersection_y - key('nodeById', $vertex/@ref)/@lat ) div 2" />
      
      <xsl:if test="($point_x &lt;= 0 or $point_x &gt; 0)  and ($point_y &lt;= 0 or $point_y &gt; 0)"> <!-- Only return anything if we actually have a result -->
	<!-- Note: this will produce trailing semicolon, which is nice as it simplifies looping over this later -->
	<xsl:value-of select="$point_x" />,<xsl:value-of select="$point_y" />,<xsl:value-of select="$intersection_dist" />;
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- Calculate the angle between $from and $to in $through. Returns answer in radians -->
  <xsl:template name="angleThroughPoints">
    <xsl:param name="from" />
    <xsl:param name="through" />
    <xsl:param name="to" />

    <xsl:variable name="from_x" select="($from/@lon) - ($through/@lon)" />
    <xsl:variable name="from_y" select="$from/@lat - $through/@lat" />
    <xsl:variable name="to_x" select="$to/@lon - $through/@lon" />
    <xsl:variable name="to_y" select="$to/@lat - $through/@lat" />

    <xsl:variable name="from_angle_">
      <xsl:call-template name="atan2">
	<xsl:with-param name="x" select="$from_x" />
	<xsl:with-param name="y" select="$from_y" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="from_angle" select="$from_angle_ + $pi" />
    <xsl:variable name="to_angle_">
      <xsl:call-template name="atan2">
	<xsl:with-param name="x" select="$to_x" />
	<xsl:with-param name="y" select="$to_y" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="to_angle" select="$to_angle_ + $pi" />

    <xsl:variable name="min_angle">
      <xsl:choose>
	<xsl:when test="$from_angle &gt; $to_angle">
	  <xsl:value-of select="$to_angle" />
	</xsl:when>
	<xsl:otherwise>
	  <xsl:value-of select="$from_angle" />
	</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="max_angle">
      <xsl:choose>
	<xsl:when test="$from_angle &gt; $to_angle">
	  <xsl:value-of select="$from_angle" />
	</xsl:when>
	<xsl:otherwise>
	  <xsl:value-of select="$to_angle" />
	</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:value-of select="$min_angle + ($max_angle - $min_angle) div 2" />
  </xsl:template>

  <!-- atan2 implementation from http://lists.fourthought.com/pipermail/exslt/2007-March/001540.html -->
  <xsl:template name="atan2">
    <xsl:param name="y"/>
    <xsl:param name="x"/>
    <!-- http://lists.apple.com/archives/PerfOptimization-dev/2005/Jan/msg00051.html -->
    <xsl:variable name="PI"    select="number(3.1415926535897)"/>
    <xsl:variable name="PIBY2" select="$PI div 2.0"/>
    <xsl:choose>
      <xsl:when test="$x = 0.0">
        <xsl:choose>
          <xsl:when test="($y &gt; 0.0)">
            <xsl:value-of select="$PIBY2"/>
          </xsl:when>
          <xsl:when test="($y &lt; 0.0)">
            <xsl:value-of select="-$PIBY2"/>
          </xsl:when>
          <xsl:otherwise>
            <!-- Error: Degenerate x == y == 0.0 -->
            <xsl:value-of select="number(NaN)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="z" select="$y div $x"/>
        <xsl:variable name="absZ">
          <!-- inline abs function -->
          <xsl:choose>
            <xsl:when test="$z &lt; 0.0">
              <xsl:value-of select="- number($z)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="number($z)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="($absZ &lt; 1.0)">
            <xsl:variable name="f1Z" select="$z div (1.0 + 0.28*$z*$z)"/>
            <xsl:choose>
              <xsl:when test="($x &lt; 0.0) and ($y &lt; 0.0)">
                <xsl:value-of select="$f1Z - $PI"/>
              </xsl:when>
              <xsl:when test="($x &lt; 0.0)">
                <xsl:value-of select="$f1Z + $PI"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$f1Z"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="f2Z" select="$PIBY2 - ($z div ($z*$z +
0.28))"/>
            <xsl:choose>
              <xsl:when test="($y &lt; 0.0)">
                <xsl:value-of select="$f2Z - $PI"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$f2Z"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Find a point on the line going through $point at $angle that's guaranteed to be outside the polygon -->
  <xsl:template name="areacenterLinepoint">
    <xsl:param name="point" />
    <xsl:param name="angle" />

    <xsl:variable name="cos_angle">
      <xsl:call-template name="cos">
	<xsl:with-param name="angle" select="$angle"/>
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:variable name="sin_angle">
      <xsl:call-template name="sin">
	<xsl:with-param name="angle" select="$angle"/>
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:value-of select="$point/@lon + $cos_angle"/>, <xsl:value-of select="$point/@lat + $sin_angle"/>
  </xsl:template>
  
  <!-- Constants for trig templates -->
  <xsl:variable name="pi" select="3.1415926535897"/>
  <xsl:variable name="halfPi" select="$pi div 2"/>
  <xsl:variable name="twicePi" select="$pi*2"/>

  <xsl:template name="sin">
    <xsl:param name="angle" />
    <xsl:param name="precision" select="0.00000001"/>

    <xsl:variable name="y">
      <xsl:choose>
        <xsl:when test="not(0 &lt;= $angle and $twicePi > $angle)">
          <xsl:call-template name="cutIntervals">
            <xsl:with-param name="length" select="$twicePi"/>
            <xsl:with-param name="angle" select="$angle"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$angle"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:call-template name="sineIter">
      <xsl:with-param name="angle2" select="$y*$y"/>
      <xsl:with-param name="res" select="$y"/>
      <xsl:with-param name="elem" select="$y"/>
      <xsl:with-param name="n" select="1"/>
      <xsl:with-param name="precision" select="$precision" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="sineIter">
    <xsl:param name="angle2" />
    <xsl:param name="res" />
    <xsl:param name="elem" />
    <xsl:param name="n" />
    <xsl:param name="precision"/>

    <xsl:variable name="nextN" select="$n+2" />
    <xsl:variable name="newElem" select="-$elem*$angle2 div ($nextN*($nextN - 1))" />
    <xsl:variable name="newResult" select="$res + $newElem" />
    <xsl:variable name="diffResult" select="$newResult - $res" />

    <xsl:choose>
      <xsl:when test="$diffResult > $precision or $diffResult &lt; -$precision">
        <xsl:call-template name="sineIter">
          <xsl:with-param name="angle2" select="$angle2" />
          <xsl:with-param name="res" select="$newResult" />
          <xsl:with-param name="elem" select="$newElem" />
          <xsl:with-param name="n" select="$nextN" />
          <xsl:with-param name="precision" select="$precision" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$newResult"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="cutIntervals">
    <xsl:param name="length"/>
    <xsl:param name="angle"/>

    <xsl:variable name="vsign">
      <xsl:choose>
        <xsl:when test="$angle >= 0">1</xsl:when>
        <xsl:otherwise>-1</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="vdiff" select="$length*floor($angle div $length) -$angle"/> 
    <xsl:choose>
      <xsl:when test="$vdiff*$angle > 0">
        <xsl:value-of select="$vsign*$vdiff"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="-$vsign*$vdiff"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="cos">
    <xsl:param name="angle" />
    <xsl:param name="precision" select="0.00000001"/>

    <xsl:call-template name="sin">
      <xsl:with-param name="angle" select="$halfPi - $angle" />
      <xsl:with-param name="precision" select="$precision" />
    </xsl:call-template>
  </xsl:template>

  <!-- Find the nearest intersection into the polygon along the line ($x,$y)-$linepoint.
       Can also be used for ray-casting point-in-polygon checking -->
  <xsl:template name="areacenterNearestIntersectionInside">
    <xsl:param name="x" />
    <xsl:param name="y" />
    <xsl:param name="edgestart" />
    <xsl:param name="linepoint_x" />
    <xsl:param name="linepoint_y" />
    <xsl:param name="holerelation" />
    <xsl:param name="intersectioncount_on" select="0" /><!-- Number of intersections. Only counts those on segment (x,y)-linepoint -->
    <xsl:param name="nearest_on_x" />
    <xsl:param name="nearest_on_y" />
    <xsl:param name="nearest_on_dist" select="'NaN'" />
    <xsl:param name="nearest_off_x" />
    <xsl:param name="nearest_off_y" />
    <xsl:param name="nearest_off_dist" select="'NaN'" />

    <xsl:choose>
      <!-- If there are no more vertices we don't have a second point for the edge, and are finished -->
      <xsl:when test="$edgestart/following-sibling::nd[1]">
	<xsl:variable name="edgeend" select="$edgestart/following-sibling::nd[1]" />
	<!-- Get the intersection point between the line ($x,$y)-$linepoint and $edgestart-$edgeend -->
	<xsl:variable name="intersection">
	  <xsl:choose>
	    <xsl:when test="( $x = key('nodeById', $edgestart/@ref)/@lon and $y = key('nodeById', $edgestart/@ref)/@lat ) or
			    ( $x = key('nodeById', $edgeend/@ref)/@lon and $y = key('nodeById', $edgeend/@ref)/@lat )">
	      <!-- (x,y) is one of the points in edge, skip -->
	      NoIntersection
	    </xsl:when>
	    <xsl:otherwise>      
	      <xsl:call-template name="areacenterLinesIntersection">
		<xsl:with-param name="x1" select="$x" />
		<xsl:with-param name="y1" select="$y" />
		<xsl:with-param name="x2" select="$linepoint_x" />
		<xsl:with-param name="y2" select="$linepoint_y" />
		<xsl:with-param name="x3" select="key('nodeById', $edgestart/@ref)/@lon" />
		<xsl:with-param name="y3" select="key('nodeById', $edgestart/@ref)/@lat" />
		<xsl:with-param name="x4" select="key('nodeById', $edgeend/@ref)/@lon" />
		<xsl:with-param name="y4" select="key('nodeById', $edgeend/@ref)/@lat" />
	      </xsl:call-template>
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:variable>

	<!-- Haul ix, iy, ua and ub out of the csv -->
	<xsl:variable name="ix" select="substring-before($intersection, ',')" />
	<xsl:variable name="iy" select="substring-before(substring-after($intersection, ','), ',')" />
	<xsl:variable name="ua" select="substring-before(substring-after(substring-after($intersection, ','), ','), ',')" />
	<xsl:variable name="ub" select="substring-after(substring-after(substring-after($intersection, ','), ','), ',')" />

	<!-- A) Is there actually an intersection? B) Is it on edge? -->
	<xsl:choose>
	  <xsl:when test="$intersection != 'NoIntersection' and $ub &gt; 0 and $ub &lt;= 1">
	    <xsl:variable name="distance">
	      <xsl:call-template name="areacenterPointDistance">
		<xsl:with-param name="x1" select="$x" />
		<xsl:with-param name="y1" select="$y" />
		<xsl:with-param name="x2" select="$ix" />
		<xsl:with-param name="y2" select="$iy" />
	      </xsl:call-template>
	    </xsl:variable>

	    <!-- Is intersection on the segment ($x,$y)-$linepoint, or on the other side of ($x,$y)? -->
	    <xsl:variable name="isOnSegment">
	      <xsl:if test="$ua &gt;= 0">Yes</xsl:if>
	    </xsl:variable>
	    
	    <xsl:variable name="isNewNearestOn">
	      <xsl:if test="$isOnSegment = 'Yes' and ( $nearest_on_dist = 'NaN' or $distance &lt; $nearest_on_dist )">Yes</xsl:if>
	    </xsl:variable>
	    
	    <xsl:variable name="isNewNearestOff">
	      <xsl:if test="$isOnSegment != 'Yes' and ( $nearest_off_dist = 'NaN' or $distance &lt; $nearest_off_dist )">Yes</xsl:if>
	    </xsl:variable>

	    <xsl:call-template name="areacenterNearestIntersectionInside">
	      <xsl:with-param name="x" select="$x" />
	      <xsl:with-param name="y" select="$y" />
	      <xsl:with-param name="linepoint_x" select="$linepoint_x" />
	      <xsl:with-param name="linepoint_y" select="$linepoint_y" />
	      <xsl:with-param name="edgestart" select="$edgeend" />
	      <xsl:with-param name="holerelation" select="$holerelation" />
	      <xsl:with-param name="intersectioncount_on" select="$intersectioncount_on + number(boolean($isOnSegment = 'Yes'))" />
	      <xsl:with-param name="nearest_on_dist"> <xsl:choose>
		<xsl:when test="$isNewNearestOn = 'Yes'"> <xsl:value-of select="$distance" /> </xsl:when>
		<xsl:otherwise> <xsl:value-of select="$nearest_on_dist" /> </xsl:otherwise>
	      </xsl:choose> </xsl:with-param>
	      <xsl:with-param name="nearest_on_x"> <xsl:choose>
		<xsl:when test="$isNewNearestOn = 'Yes'"> <xsl:value-of select="$ix" /> </xsl:when>
		<xsl:otherwise> <xsl:value-of select="$nearest_on_x" /> </xsl:otherwise>
	      </xsl:choose> </xsl:with-param>
	      <xsl:with-param name="nearest_on_y"> <xsl:choose>
		<xsl:when test="$isNewNearestOn = 'Yes'"> <xsl:value-of select="$iy" /> </xsl:when>
		<xsl:otherwise> <xsl:value-of select="$nearest_on_y" /> </xsl:otherwise>
	      </xsl:choose> </xsl:with-param>
	      <xsl:with-param name="nearest_off_dist"> <xsl:choose>
		<xsl:when test="$isNewNearestOff = 'Yes'"> <xsl:value-of select="$distance" /> </xsl:when>
		<xsl:otherwise> <xsl:value-of select="$nearest_off_dist" /> </xsl:otherwise>
	      </xsl:choose> </xsl:with-param>
	      <xsl:with-param name="nearest_off_x"> <xsl:choose>
		<xsl:when test="$isNewNearestOff = 'Yes'"> <xsl:value-of select="$ix" /> </xsl:when>
		<xsl:otherwise> <xsl:value-of select="$nearest_off_x" /> </xsl:otherwise>
	      </xsl:choose> </xsl:with-param>
	      <xsl:with-param name="nearest_off_y"> <xsl:choose>
		<xsl:when test="$isNewNearestOff = 'Yes'"> <xsl:value-of select="$iy" /> </xsl:when>
		<xsl:otherwise> <xsl:value-of select="$nearest_off_y" /> </xsl:otherwise>
	      </xsl:choose> </xsl:with-param>
	    </xsl:call-template>
	  </xsl:when>
	  <!-- No intersection, just go on to next edge -->
	  <xsl:otherwise>
	    <xsl:call-template name="areacenterNearestIntersectionInside">
	      <xsl:with-param name="x" select="$x" />
	      <xsl:with-param name="y" select="$y" />
	      <xsl:with-param name="linepoint_x" select="$linepoint_x" />
	      <xsl:with-param name="linepoint_y" select="$linepoint_y" />
	      <xsl:with-param name="edgestart" select="$edgeend" />
	      <xsl:with-param name="holerelation" select="$holerelation" />
	      <xsl:with-param name="intersectioncount_on" select="$intersectioncount_on" />
	      <xsl:with-param name="nearest_on_dist" select="$nearest_on_dist" />
	      <xsl:with-param name="nearest_on_x" select="$nearest_on_x" />
	      <xsl:with-param name="nearest_on_y" select="$nearest_on_y" />
	      <xsl:with-param name="nearest_off_dist" select="$nearest_off_dist" />
	      <xsl:with-param name="nearest_off_x" select="$nearest_off_x" />
	      <xsl:with-param name="nearest_off_y" select="$nearest_off_y" />
	    </xsl:call-template>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>
      <!-- Is there a hole in the polygon, and were we working on the outer one? Then we start edge detection against the hole. -->
      <xsl:when test="$holerelation and
		      $holerelation/member[@ref = $edgestart/../@id][@role='outer']">
	<xsl:variable name="nextnode" select="key('wayById', $holerelation/member[@type='way'][@role='inner'][1]/@ref)/nd[1]"/>
	<xsl:call-template name="areacenterNearestIntersectionInside">
	  <xsl:with-param name="x" select="$x" />
	  <xsl:with-param name="y" select="$y" />
	  <xsl:with-param name="linepoint_x" select="$linepoint_x" />
	  <xsl:with-param name="linepoint_y" select="$linepoint_y" />
	  <xsl:with-param name="edgestart" select="$nextnode" />
	  <xsl:with-param name="holerelation" select="$holerelation" />
	  <xsl:with-param name="intersectioncount_on" select="$intersectioncount_on" />
	  <xsl:with-param name="nearest_on_dist" select="$nearest_on_dist" />
	  <xsl:with-param name="nearest_on_x" select="$nearest_on_x" />
	  <xsl:with-param name="nearest_on_y" select="$nearest_on_y" />
	  <xsl:with-param name="nearest_off_dist" select="$nearest_off_dist" />
	  <xsl:with-param name="nearest_off_x" select="$nearest_off_x" />
	  <xsl:with-param name="nearest_off_y" select="$nearest_off_y" />
	</xsl:call-template>
      </xsl:when>
      <!-- Is there a hole in the polygon, and were we working working on one of the inner ones? Then go to the next hole, if there is one -->
      <xsl:when test="$holerelation and
		      $holerelation/member[@ref = $edgestart/../@id][@type='way'][@role='inner']/following-sibling::member[@role='inner']">
	<xsl:variable name="nextnode" select="key('wayById', $holerelation/member[@ref = $edgestart/../@id][@type='way'][@role='inner']/following-sibling::member[@role='inner']/@ref)/nd[1]"/>
	<xsl:call-template name="areacenterNearestIntersectionInside">
	  <xsl:with-param name="x" select="$x" />
	  <xsl:with-param name="y" select="$y" />
	  <xsl:with-param name="linepoint_x" select="$linepoint_x" />
	  <xsl:with-param name="linepoint_y" select="$linepoint_y" />
	  <xsl:with-param name="edgestart" select="$nextnode" />
	  <xsl:with-param name="holerelation" select="$holerelation" />
	  <xsl:with-param name="intersectioncount_on" select="$intersectioncount_on" />
	  <xsl:with-param name="nearest_on_dist" select="$nearest_on_dist" />
	  <xsl:with-param name="nearest_on_x" select="$nearest_on_x" />
	  <xsl:with-param name="nearest_on_y" select="$nearest_on_y" />
	  <xsl:with-param name="nearest_off_dist" select="$nearest_off_dist" />
	  <xsl:with-param name="nearest_off_x" select="$nearest_off_x" />
	  <xsl:with-param name="nearest_off_y" select="$nearest_off_y" />
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<!-- No more edges, return data -->
	<xsl:value-of select="$intersectioncount_on" />;
	<xsl:value-of select="$nearest_on_x"/>,<xsl:value-of select="$nearest_on_y"/>,<xsl:value-of select="$nearest_on_dist"/>;
	<xsl:value-of select="$nearest_off_x"/>,<xsl:value-of select="$nearest_off_y"/>,<xsl:value-of select="$nearest_off_dist"/>;
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Find the distance to the edge nearest (x,y) -->
  <xsl:template name="areacenterNearestEdge">
    <xsl:param name="x" />
    <xsl:param name="y" />
    <xsl:param name="edgestart" />
    <xsl:param name="holerelation" />
    <xsl:param name="nearest_dist" select="'NaN'" />

    <xsl:choose>
      <!-- If there are no more vertices we don't have a second point for the edge, and are finished -->
      <xsl:when test="$edgestart/following-sibling::nd[1]">
	<xsl:variable name="edgeend" select="$edgestart/following-sibling::nd[1]" />

	<xsl:variable name="distance">
	  <xsl:call-template name="areacenterDistancePointSegment">
	    <xsl:with-param name="x" select="$x" />
	    <xsl:with-param name="y" select="$y" />
	    <xsl:with-param name="x1" select="key('nodeById', $edgestart/@ref)/@lon" />
	    <xsl:with-param name="y1" select="key('nodeById', $edgestart/@ref)/@lat" />
	    <xsl:with-param name="x2" select="key('nodeById', $edgeend/@ref)/@lon" />
	    <xsl:with-param name="y2" select="key('nodeById', $edgeend/@ref)/@lat" />
	  </xsl:call-template>
	</xsl:variable>

	<!-- Did we get a valid distance?
	     There is some code in DistancePointSegment that can return NaN in some cases -->
	<xsl:choose>
	  <xsl:when test="string(number($distance)) != 'NaN'">
	    <xsl:call-template name="areacenterNearestEdge">
	      <xsl:with-param name="x" select="$x" />
	      <xsl:with-param name="y" select="$y" />
	      <xsl:with-param name="edgestart" select="$edgeend" />
	      <xsl:with-param name="holerelation" select="$holerelation" />
	      <xsl:with-param name="nearest_dist"> <xsl:choose>
		<xsl:when test="$nearest_dist = 'NaN' or $distance &lt; $nearest_dist"> <xsl:value-of select="$distance" /> </xsl:when>
		<xsl:otherwise> <xsl:value-of select="$nearest_dist" /> </xsl:otherwise>
	      </xsl:choose> </xsl:with-param>
	    </xsl:call-template>
	  </xsl:when>

	  <xsl:otherwise>
	    <xsl:call-template name="areacenterNearestEdge">
	      <xsl:with-param name="x" select="$x" />
	      <xsl:with-param name="y" select="$y" />
	      <xsl:with-param name="edgestart" select="$edgeend" />
	      <xsl:with-param name="holerelation" select="$holerelation" />
	      <xsl:with-param name="nearest_dist" select="$nearest_dist" />
	    </xsl:call-template>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:when>
      <!-- Is there a hole in the polygon, and were we working on the outer one? Then we start edge detection against the hole. -->
      <xsl:when test="$holerelation and
		      $holerelation/member[@ref = $edgestart/../@id][@role='outer']">
	<xsl:variable name="nextnode" select="key('wayById', $holerelation/member[@type='way'][@role='inner'][1]/@ref)/nd[1]"/>
	<xsl:call-template name="areacenterNearestEdge">
	  <xsl:with-param name="x" select="$x" />
	  <xsl:with-param name="y" select="$y" />
	  <xsl:with-param name="edgestart" select="$nextnode" />
	  <xsl:with-param name="holerelation" select="$holerelation" />
	  <xsl:with-param name="nearest_dist" select="$nearest_dist" />
	</xsl:call-template>
      </xsl:when>
      <!-- Is there a hole in the polygon, and were we working working on one of the inner ones? Then go to the next hole, if there is one -->
      <xsl:when test="$holerelation and
		      $holerelation/member[@ref = $edgestart/../@id][@type='way'][@role='inner']/following-sibling::member[@role='inner']">
	<xsl:variable name="nextnode" select="key('wayById', $holerelation/member[@ref = $edgestart/../@id][@type='way'][@role='inner']/following-sibling::member[@role='inner']/@ref)/nd[1]"/>
	<xsl:call-template name="areacenterNearestEdge">
	  <xsl:with-param name="x" select="$x" />
	  <xsl:with-param name="y" select="$y" />
	  <xsl:with-param name="edgestart" select="$nextnode" />
	  <xsl:with-param name="holerelation" select="$holerelation" />
	  <xsl:with-param name="nearest_dist" select="$nearest_dist" />
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<!-- No more edges, return data -->
	<xsl:value-of select="$nearest_dist" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Find the distance between the point (x,y) and the segment x1,y1 -> x2,y2 -->
  <!-- Based on http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/ and the
       Delphi example by Graham O'Brien -->
  <xsl:template name="areacenterDistancePointSegment">
    <xsl:param name="x" />
    <xsl:param name="y" />
    <xsl:param name="x1" />
    <xsl:param name="y1" />
    <xsl:param name="x2" />
    <xsl:param name="y2" />

    <!-- Constants -->
    <xsl:variable name="EPS" select="0.000001" />
    <xsl:variable name="EPSEPS" select="$EPS * $EPS" />

    <!-- The line magnitude, squared -->
    <xsl:variable name="sqLineMagnitude" select="($x2 - $x1) * ($x2 - $x1) + ($y2 - $y1) * ($y2 - $y1)" />

    <xsl:choose>
      <xsl:when test="sqLineMagnitude &lt; $EPSEPS">
	NaN
      </xsl:when>
      <xsl:otherwise>
	<xsl:variable name="u" select="( ($x - $x1)*($x2 - $x1) + ($y - $y1)*($y2 - $y1) ) div sqLineMagnitude" />

	<xsl:variable name="result">
	  <xsl:choose>
	    <xsl:when test="u &lt; $EPS or u &gt; 1">
	      <!-- Closest point in not on segment, return shortest distance to an endpoint -->
	      <xsl:variable name="dist1" select="($x1 - $x) * ($x1 - $x) + ($y1 - $y) * ($y1 - $y)" />
	      <xsl:variable name="dist2" select="($x2 - $x) * ($x2 - $x) + ($y2 - $y) * ($y2 - $y)" />
	      
	      <!-- min($dist1, $dist2) -->
	      <xsl:choose>
		<xsl:when test="$dist1 &lt; $dist2">
		  <xsl:value-of select="$dist1" />
		</xsl:when>
		<xsl:otherwise>
		  <xsl:value-of select="$dist2" />
		</xsl:otherwise>
	      </xsl:choose>
	      
	    </xsl:when>
	    <xsl:otherwise>
	      <xsl:variable name="ix" select="$x1 + $u * ($x2 - $x1)" />
	      <xsl:variable name="iy" select="$y1 + $u * ($y2 - $y1)" />
	      <xsl:value-of select="($ix - $x) * ($ix - $x) + ($iy - $y) * ($iy - $y)" />
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:variable>

	<!-- Finally return the square root of the result, as we were working with squared distances -->
	<xsl:call-template name="sqrt">
	  <xsl:with-param name="num" select="$result" />
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!--
      Finds intersection point between lines x1,y1 -> x2,y2 and x3,y3 -> x4,y4.
      Returns a comma-separated list of x,y,ua,ub or NoIntersection if the lines do not intersect
  -->
  <xsl:template name="areacenterLinesIntersection">
    <xsl:param name="x1" />
    <xsl:param name="y1" />
    <xsl:param name="x2" />
    <xsl:param name="y2" />
    <xsl:param name="x3" />
    <xsl:param name="y3" />
    <xsl:param name="x4" />
    <xsl:param name="y4" />

    <xsl:variable name="denom" select="(( $y4 - $y3 ) * ( $x2 - $x1 )) -
				       (( $x4 - $x3 ) * ( $y2 - $y1 ))" />
    <xsl:variable name="nume_a" select="(( $x4 - $x3 ) * ( $y1 - $y3 )) -
					(( $y4 - $y3 ) * ( $x1 - $x3 ))" />
    <xsl:variable name="nume_b" select="(( $x2 - $x1 ) * ( $y1 - $y3 )) -
					(( $y2 - $y1 ) * ( $x1 - $x3 ))" />

    <xsl:choose>
      <xsl:when test="$denom = 0">
	NoIntersection
      </xsl:when>
      <xsl:otherwise>
	<xsl:variable name="ua" select="$nume_a div $denom" />
	<xsl:variable name="ub" select="$nume_b div $denom" />

	<!-- x,y,ua,ub -->
	<xsl:value-of select="$x1 + $ua * ($x2 - $x1)" />,<xsl:value-of select="$y1 + $ua * ($y2 - $y1)" />,<xsl:value-of select="$ua" />,<xsl:value-of select="$ub" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Distance between two points -->
  <xsl:template name="areacenterPointDistance">
    <xsl:param name="x1" />
    <xsl:param name="y1" />
    <xsl:param name="x2" />
    <xsl:param name="y2" />

    <!-- sqrt( ($x2 - $x1)**2 + ($y2 - $y1)**2 ) -->
    <xsl:call-template name="sqrt">
      <xsl:with-param name="num" select="($x2*$x2 - $x2*$x1 - $x1*$x2 + $x1*$x1) + ($y2*$y2 - $y2*$y1 - $y1*$y2 + $y1*$y1)" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="sqrt">
    <xsl:param name="num" select="0"/>  <!-- The number you want to find the
					     square root of -->
    <xsl:param name="try" select="1"/>  <!-- The current 'try'.  This is used
					     internally. -->
    <xsl:param name="iter" select="1"/> <!-- The current iteration, checked
					     against maxiter to limit loop count -->
    <xsl:param name="maxiter" select="10"/>  <!-- Set this up to insure
against infinite loops -->
    
    <!-- This template was written by Nate Austin using Sir Isaac Newton's
	 method of finding roots -->
    
    <xsl:choose>
      <xsl:when test="$try * $try = $num or $iter &gt; $maxiter">
	<xsl:value-of select="$try"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:call-template name="sqrt">
          <xsl:with-param name="num" select="$num"/>
          <xsl:with-param name="try" select="$try - (($try * $try - $num) div
					     (2 * $try))"/>
          <xsl:with-param name="iter" select="$iter + 1"/>
          <xsl:with-param name="maxiter" select="$maxiter"/>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Returns the medium value of all the points -->
  <xsl:template name="areacenterMediumOfPoints">
    <xsl:param name="points" />
    <xsl:param name="total_x" select="0" />
    <xsl:param name="total_y" select="0" />
    <xsl:param name="total_dist" select="0" />
    <xsl:param name="count" select="0" />

    <xsl:variable name="point" select="substring-before($points, ';')" />

    <xsl:choose>
      <xsl:when test="string-length($point) &gt; 0">
	<xsl:variable name="x" select="substring-before($point, ',')" />
	<xsl:variable name="y" select="substring-before(substring-after($point, ','), ',')" />
	<xsl:variable name="dist" select="substring-after(substring-after($point, ','), ',')" />

	<xsl:call-template name="areacenterMediumOfPoints">
	  <xsl:with-param name="points" select="substring-after($points, ';')" />
	  <xsl:with-param name="total_x" select="$total_x + $x" />
	  <xsl:with-param name="total_y" select="$total_y + $y" />
	  <xsl:with-param name="total_dist" select="$total_dist + $dist" />
	  <xsl:with-param name="count" select="$count + 1" />
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="$total_x div $count" />,<xsl:value-of select="$total_y div $count" />,<xsl:value-of select="$total_dist div $count" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Returns the coordinates of the point that scores highest.
       The score is based on the distance to (x,y),
       the distance between the point and it's vertex,
       and the medium of that distance in all the points -->
  <xsl:template name="areacenterBestPoint">
    <xsl:param name="points" />
    <xsl:param name="x" />
    <xsl:param name="y" />
    <xsl:param name="nearest_x" />
    <xsl:param name="nearest_y" />
    <xsl:param name="medium_dist" />
    <xsl:param name="nearest_score" />
    <xsl:param name="nearest_dist" select="'NaN'" />

    <xsl:variable name="point" select="substring-before($points, ';')" />

    <xsl:choose>
      <xsl:when test="string-length($point) &gt; 0"> 
        <xsl:variable name="point_x" select="substring-before($point, ',')" />
	<xsl:variable name="point_y" select="substring-before(substring-after($point, ','), ',')" />
	<xsl:variable name="point_dist" select="substring-after(substring-after($point, ','), ',')" />
	
	<xsl:variable name="distance">
	  <xsl:call-template name="areacenterPointDistance">
	    <xsl:with-param name="x1" select="$x" />
	    <xsl:with-param name="y1" select="$y" />
	    <xsl:with-param name="x2" select="$point_x" />
	    <xsl:with-param name="y2" select="$point_y" />
	  </xsl:call-template>
	</xsl:variable>

	<xsl:variable name="score" select="0 - $distance + $point_dist + $point_dist - $medium_dist"/>
	<xsl:variable name="isNewNearest" select="$nearest_dist = 'NaN' or $score &gt; $nearest_score" />

	<xsl:call-template name="areacenterBestPoint">
	  <xsl:with-param name="points" select="substring-after($points, ';')" />
	  <xsl:with-param name="x" select="$x" />
	  <xsl:with-param name="y" select="$y" />
	  <xsl:with-param name="medium_dist" select="$medium_dist" />
	  <xsl:with-param name="nearest_dist"><xsl:choose>
	    <xsl:when test="$isNewNearest"><xsl:value-of select="$distance" /></xsl:when>
	    <xsl:otherwise><xsl:value-of select="$nearest_dist" /></xsl:otherwise>
	  </xsl:choose></xsl:with-param>
	  <xsl:with-param name="nearest_x"><xsl:choose>
	    <xsl:when test="$isNewNearest"><xsl:value-of select="$point_x" /></xsl:when>
	    <xsl:otherwise><xsl:value-of select="$nearest_x" /></xsl:otherwise>
	  </xsl:choose></xsl:with-param>
	  <xsl:with-param name="nearest_y"><xsl:choose>
	    <xsl:when test="$isNewNearest"><xsl:value-of select="$point_y" /></xsl:when>
	    <xsl:otherwise><xsl:value-of select="$nearest_y" /></xsl:otherwise>
	  </xsl:choose></xsl:with-param>
	  <xsl:with-param name="nearest_score"><xsl:choose>
	    <xsl:when test="$isNewNearest"><xsl:value-of select="$score" /></xsl:when>
	    <xsl:otherwise><xsl:value-of select="$nearest_score" /></xsl:otherwise>
	  </xsl:choose></xsl:with-param>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="$nearest_x" />, <xsl:value-of select="$nearest_y" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Process a <text> instruction -->
  <xsl:template match="text">
    <xsl:param name="elements"/>

    <!-- This is the instruction that is currently being processed -->
    <xsl:variable name="instruction" select="."/>

    <!-- Select all <node> elements that have a key that matches the k attribute of the text instruction -->
    <xsl:for-each select="$elements[name()='node'][tag[@k=$instruction/@k]]">
      <xsl:call-template name="renderText">
        <xsl:with-param name="instruction" select="$instruction"/>
      </xsl:call-template>
    </xsl:for-each>

    <!-- Select all <way> elements -->
    <xsl:apply-templates select="$elements[name()='way']" mode="textPath">
      <xsl:with-param name="instruction" select="$instruction"/>
    </xsl:apply-templates>
  </xsl:template>


  <!-- Suppress output of any unhandled elements -->
  <xsl:template match="*" mode="textPath"/>


  <!-- Render textPaths for a way -->
  <xsl:template match="way" mode="textPath">
    <xsl:param name="instruction"/>

    <!-- The current <way> element -->
    <xsl:variable name="way" select="."/>

    <!-- DODI: !!!WORKAROUND!!! no text for one node ways-->
    <xsl:if test="count($way/nd) &gt; 1">
      <xsl:variable name='text'>
        <xsl:choose>
          <xsl:when test='$instruction/@k'>
            <xsl:value-of select='tag[@k=$instruction/@k]/@v'/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select='$instruction' mode='textFormat'>
              <xsl:with-param name='way' select='$way'/>
            </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:if test='string($text)'>

        <xsl:variable name="pathDirection">
          <xsl:choose>
            <!-- Manual override, reverse direction -->
            <xsl:when test="tag[@k='name_direction']/@v='-1' or tag[@k='osmarender:nameDirection']/@v='-1'">reverse</xsl:when>
            <!-- Manual override, normal direction -->
            <xsl:when test="tag[@k='name_direction']/@v='1' or tag[@k='osmarender:nameDirection']/@v='1'">normal</xsl:when>
            <!-- Automatic, reverse direction -->
            <xsl:when test="(key('nodeById',$way/nd[1]/@ref)/@lon &gt; key('nodeById',$way/nd[last()]/@ref)/@lon)">reverse</xsl:when>
            <!-- Automatic, normal direction -->
            <xsl:otherwise>normal</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:variable name="wayPath">
          <xsl:choose>
            <!-- Normal -->
            <xsl:when test='$pathDirection="normal"'>
              <xsl:value-of select="concat('way_normal_',@id)"/>
            </xsl:when>
            <!-- Reverse -->
            <xsl:otherwise>
              <xsl:value-of select="concat('way_reverse_',@id)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:call-template name="renderTextPath">
          <xsl:with-param name="instruction" select="$instruction"/>
          <xsl:with-param name="pathId" select="$wayPath"/>
          <xsl:with-param name="pathDirection" select="$pathDirection"/>
          <xsl:with-param name="text" select="$text"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <!-- Process extended form of text instruction -->
  <xsl:template match='text' mode='textFormat'>
    <xsl:param name='way'/>

    <xsl:apply-templates mode='textFormat'>
      <xsl:with-param name='way' select='$way'/>
    </xsl:apply-templates>
  </xsl:template>


  <!-- Substitute a tag in a text instruction -->
  <xsl:template match='text/tag' mode='textFormat'>
    <xsl:param name='way'/>

    <xsl:variable name='key' select='@k'/>
    <xsl:variable name='value'>
      <xsl:choose>
        <xsl:when test='$key="osm:user"'>
          <xsl:value-of select='$way/@user'/>
        </xsl:when>
        <xsl:when test='$key="osm:timestamp"'>
          <xsl:value-of select='$way/@timestamp'/>
        </xsl:when>
        <xsl:when test='$key="osm:id"'>
          <xsl:value-of select='$way/@id'/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select='$way/tag[@k=$key]/@v'/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test='string($value)'>
        <xsl:value-of select='$value'/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select='@default'/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <!-- Generate a way path for the current way element -->
  <xsl:template name="generateWayPaths">
    <!-- DODI: !!!WORKAROUND!!! skip one node ways -->
    <xsl:if test="count(nd) &gt; 1">

      <!-- Generate a normal way path -->
      <xsl:variable name="pathWayNormal">
        <xsl:call-template name="generateWayPathNormal"/>
      </xsl:variable>
      <xsl:if test="$pathWayNormal!=''">
        <path id="way_normal_{@id}" d="{$pathWayNormal}"/>
      </xsl:if>

      <!-- Generate a normal way path as area -->
      <!-- DODI: !!!WORKAROUND!!! added to generate "area for all ways, yes it is very dirty... but -->
      <!-- DODI: removed because of line2curves.pl duplicate node detection problem -->
      <!-- <xsl:variable name="pathArea">
      <xsl:call-template name="generateAreaPath"/>
    </xsl:variable>
    <path id="area_{@id}" d="{$pathArea}"/> -->
      <!-- Generate a reverse way path (if needed) -->
      <xsl:variable name="pathWayReverse">
        <xsl:choose>
          <!-- Manual override, reverse direction -->
          <xsl:when test="tag[@k='name_direction']/@v='-1' or tag[@k='osmarender:nameDirection']/@v='-1'">
            <xsl:call-template name="generateWayPathReverse"/>
          </xsl:when>
          <!-- Manual override, normal direction -->
          <xsl:when test="tag[@k='name_direction']/@v='1' or tag[@k='osmarender:nameDirection']/@v='1'">
            <!-- Generate nothing -->
          </xsl:when>
          <!-- Automatic, reverse direction -->
          <xsl:when test="(key('nodeById',nd[1]/@ref)/@lon &gt; key('nodeById',nd[last()]/@ref)/@lon)">
            <xsl:call-template name="generateWayPathReverse"/>
          </xsl:when>
        </xsl:choose>
      </xsl:variable>
      <xsl:if test="$pathWayReverse!=''">
        <path id="way_reverse_{@id}" d="{$pathWayReverse}"/>
      </xsl:if>

      <!-- Generate the start, middle and end paths needed for smart-linecaps (TM). -->
      <xsl:variable name="pathWayStart">
        <xsl:call-template name="generatePathWayStart"/>
      </xsl:variable>
      <path id="way_start_{@id}" d="{$pathWayStart}"/>

      <xsl:if test="count(nd) &gt; 1">
        <xsl:variable name="pathWayMid">
          <xsl:call-template name="generatePathWayMid"/>
        </xsl:variable>
        <path id="way_mid_{@id}" d="{$pathWayMid}"/>
      </xsl:if>

      <xsl:variable name="pathWayEnd">
        <xsl:call-template name="generatePathWayEnd"/>
      </xsl:variable>
      <path id="way_end_{@id}" d="{$pathWayEnd}"/>
    </xsl:if >
  </xsl:template>


  <!-- Generate a normal way path -->
  <xsl:template name="generateWayPathNormal">
    <xsl:for-each select="nd[key('nodeById',@ref) ]">
      <xsl:choose>
        <xsl:when test="position()=1">
          <xsl:call-template name="moveToNode">
            <xsl:with-param name="node" select="key('nodeById',@ref)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="lineToNode">
            <xsl:with-param name="node" select="key('nodeById',@ref)"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>


  <!-- Generate a reverse way path -->
  <xsl:template name="generateWayPathReverse">
    <xsl:for-each select="nd[key('nodeById',@ref)]">
      <xsl:sort select="position()" data-type="number" order="descending"/>
      <xsl:choose>
        <xsl:when test="position()=1">
          <xsl:call-template name="moveToNode">
            <xsl:with-param name="node" select="key('nodeById',@ref)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="lineToNode">
            <xsl:with-param name="node" select="key('nodeById',@ref)"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>


  <!-- These template generates two paths, one for each end of a way.  The line to the first node is cut in two so that the join
         between the two paths is not at an angle.  -->
  <xsl:template name="generatePathWayStart">
    <xsl:call-template name="moveToNode">
      <xsl:with-param name="node" select="key('nodeById',nd[1]/@ref)"/>
    </xsl:call-template>
    <xsl:call-template name="lineToMidpointPlus">
      <xsl:with-param name="fromNode" select="key('nodeById',nd[1]/@ref)"/>
      <xsl:with-param name="toNode" select="key('nodeById',nd[2]/@ref)"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="generatePathWayEnd">
    <xsl:call-template name="moveToMidpointPlus">
      <xsl:with-param name="fromNode" select="key('nodeById',nd[position()=(last())]/@ref)"/>
      <xsl:with-param name="toNode" select="key('nodeById',nd[position()=last()-1]/@ref)"/>
    </xsl:call-template>
    <xsl:call-template name="lineToNode">
      <xsl:with-param name="node" select="key('nodeById',nd[position()=last()]/@ref)"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="generatePathWayMid">
    <xsl:for-each select="nd[key('nodeById',@ref)]">
      <xsl:choose>
        <xsl:when test="position()=1">
          <xsl:call-template name="moveToMidpointPlus">
            <xsl:with-param name="fromNode" select="key('nodeById',@ref)"/>
            <xsl:with-param name="toNode" select="key('nodeById',following-sibling::nd[1]/@ref)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="position()=last()">
          <xsl:call-template name="lineToMidpointMinus">
            <xsl:with-param name="fromNode" select="key('nodeById',preceding-sibling::nd[1]/@ref)"/>
            <xsl:with-param name="toNode" select="key('nodeById',@ref)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="lineToNode">
            <xsl:with-param name="node" select="key('nodeById',@ref)"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <!-- Generate an area path for the current way or area element -->
  <xsl:template name="generateAreaPath">
    <xsl:variable name='relation' select="key('relationByWay',@id)[tag[@k='type' and @v='multipolygon']]"/>
    <xsl:choose>
      <xsl:when test='$relation'>
	<!-- Handle multipolygons.
	     Draw area only once, draw the outer one first if we know which is it, else just draw the first one -->
	<xsl:variable name='outerway' select="$relation/member[@type='way'][@role='outer']/@ref"/>
	<xsl:variable name='firsrelationmember' select="$relation/member[@type='way'][key('wayById', @ref)][1]/@ref"/> 
        <xsl:if test='( $outerway and $outerway=@id ) or ( not($outerway) and $firsrelationmember=@id )'>
          <xsl:message>
            <xsl:value-of select='$relation/@id'/>
          </xsl:message>
          <xsl:for-each select="$relation/member[@type='way'][key('wayById', @ref)]">
            <xsl:call-template name='generateAreaSubPath'>
              <xsl:with-param name='way' select="key('wayById',@ref)"/>
              <xsl:with-param name='position' select="position()"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:text>Z</xsl:text>
        </xsl:if>

      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name='generateAreaSubPath'>
          <xsl:with-param name='way' select='.'/>
          <xsl:with-param name='position' select="'1'"/>
        </xsl:call-template>
        <xsl:text>Z</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name='generateAreaSubPath'>
    <xsl:param name='way'/>
    <xsl:param name='position'/>

    <xsl:variable name='loop' select='$way/nd[1]/@ref=$way/nd[last()]/@ref'/>
    <xsl:message>
      WayId: <xsl:value-of select='$way/@id'/>
      Loop: <xsl:value-of select='$loop'/>
      Loop: <xsl:value-of select='$way/nd[1]/@ref'/>
      Loop: <xsl:value-of select='$way/nd[last()]/@ref'/>
    </xsl:message>
    <xsl:for-each select="$way/nd[key('nodeById',@ref)]">
      <xsl:choose>
        <xsl:when test="position()=1 and $loop">
          <xsl:if test='not($position=1)'>
            <xsl:text>Z</xsl:text>
          </xsl:if>
          <xsl:call-template name="moveToNode">
            <xsl:with-param name="node" select="key('nodeById',@ref)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="$position=1 and position()=1 and not($loop=1)">
          <xsl:call-template name="moveToNode">
            <xsl:with-param name="node" select="key('nodeById',@ref)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="lineToNode">
            <xsl:with-param name="node" select="key('nodeById',@ref)"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>


  </xsl:template>

  <!-- Generate a MoveTo command for a node -->
  <xsl:template name="moveToNode">
    <xsl:param name='node' />
    <xsl:variable name="x1" select="($width)-((($topRightLongitude)-($node/@lon))*10000*$scale)"/>
    <xsl:variable name="y1" select="($height)+((($bottomLeftLatitude)-($node/@lat))*10000*$scale*$projection)"/>
    <xsl:text>M</xsl:text>
    <xsl:value-of select="$x1"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$y1"/>
  </xsl:template>

  <!-- Generate a LineTo command for a nd -->
  <xsl:template name="lineToNode">
    <xsl:param name='node'/>

    <xsl:variable name="x1" select="($width)-((($topRightLongitude)-($node/@lon))*10000*$scale)"/>
    <xsl:variable name="y1" select="($height)+((($bottomLeftLatitude)-($node/@lat))*10000*$scale*$projection)"/>
    <xsl:text>L</xsl:text>
    <xsl:value-of select="$x1"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$y1"/>
  </xsl:template>

  <xsl:template name="lineToMidpointPlus">
    <xsl:param name='fromNode'/>
    <xsl:param name='toNode'/>

    <xsl:variable name="x1" select="($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)"/>
    <xsl:variable name="y1" select="($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)"/>

    <xsl:variable name="x2" select="($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)"/>
    <xsl:variable name="y2" select="($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)"/>

    <xsl:text>L</xsl:text>
    <xsl:value-of select="$x1+(($x2 - $x1) div 1.9)"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$y1+(($y2 - $y1) div 1.9)"/>
  </xsl:template>

  <xsl:template name="lineToMidpointMinus">
    <xsl:param name='fromNode'/>
    <xsl:param name='toNode'/>

    <xsl:variable name="x1" select="($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)"/>
    <xsl:variable name="y1" select="($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)"/>

    <xsl:variable name="x2" select="($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)"/>
    <xsl:variable name="y2" select="($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)"/>
    <xsl:text>L</xsl:text>
    <xsl:value-of select="$x1+(($x2 - $x1) div 2.1)"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$y1+(($y2 - $y1) div 2.1)"/>
  </xsl:template>


  <xsl:template name="moveToMidpointPlus">
    <xsl:param name='fromNode'/>
    <xsl:param name='toNode'/>

    <xsl:variable name="x1" select="($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)"/>
    <xsl:variable name="y1" select="($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)"/>

    <xsl:variable name="x2" select="($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)"/>
    <xsl:variable name="y2" select="($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)"/>
    <xsl:text>M</xsl:text>
    <xsl:value-of select="$x1+(($x2 - $x1) div 1.9)"/>
    <xsl:text> </xsl:text>
    <xsl:value-of select="$y1+(($y2 - $y1) div 1.9)"/>
  </xsl:template>

  <!-- Some attribute shouldn't be copied -->
  <xsl:template match="@type|@ref|@scale|@smart-linecap" mode="copyAttributes" />

  <!-- Copy all other attributes  -->
  <xsl:template match="@*" mode="copyAttributes">
    <xsl:copy/>
  </xsl:template>


  <!-- Rule processing engine -->

  <!-- 

		Calls all templates inside <rule> tags (including itself, if there are nested rules).

		If the global var withOSMLayers is 'no', we don't care about layers and draw everything
		in one go. This is faster and is sometimes useful. For normal maps you want withOSMLayers
		to be 'yes', which is the default.

	-->
  <xsl:template name="processRules">

    <!-- First select all elements - exclude those marked as deleted by JOSM -->
    <xsl:variable name='elements' select="$data/osm/*[not(@action) or not(@action='delete')]" />

    <xsl:choose>

      <!-- Process all the rules, one layer at a time -->
      <xsl:when test="$withOSMLayers='yes'">
        <xsl:call-template name="processLayer">
          <xsl:with-param name="layer" select="'-5'"/>
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
        <xsl:call-template name="processLayer">
          <xsl:with-param name="layer" select="'-4'"/>
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
        <xsl:call-template name="processLayer">
          <xsl:with-param name="layer" select="'-3'"/>
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
        <xsl:call-template name="processLayer">
          <xsl:with-param name="layer" select="'-2'"/>
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
        <xsl:call-template name="processLayer">
          <xsl:with-param name="layer" select="'-1'"/>
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
        <xsl:call-template name="processLayer">
          <xsl:with-param name="layer" select="'0'"/>
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
        <xsl:call-template name="processLayer">
          <xsl:with-param name="layer" select="'1'"/>
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
        <xsl:call-template name="processLayer">
          <xsl:with-param name="layer" select="'2'"/>
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
        <xsl:call-template name="processLayer">
          <xsl:with-param name="layer" select="'3'"/>
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
        <xsl:call-template name="processLayer">
          <xsl:with-param name="layer" select="'4'"/>
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
        <xsl:call-template name="processLayer">
          <xsl:with-param name="layer" select="'5'"/>
          <xsl:with-param name="elements" select="$elements"/>
        </xsl:call-template>
      </xsl:when>

      <!-- Process all the rules, without looking at the layers -->
      <xsl:otherwise>
        <xsl:apply-templates select="/rules/rule">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="layer" select="'0'"/>
        </xsl:apply-templates>
      </xsl:otherwise>

    </xsl:choose>
  </xsl:template>


  <xsl:template name="processLayer">
    <xsl:param name="layer"/>
    <xsl:param name="elements"/>

    <g inkscape:groupmode="layer" id="layer{$layer}" inkscape:label="Layer {$layer}">
      <xsl:apply-templates select="/rules/rule">
        <xsl:with-param name="elements" select="$elements"/>
        <xsl:with-param name="layer" select="$layer"/>
      </xsl:apply-templates>
    </g>
  </xsl:template>


  <!-- Process a rule at a specific level -->
  <xsl:template match='rule'>
    <xsl:param name="elements"/>
    <xsl:param name="layer"/>

    <!-- If the rule is for a specific layer and we are processing that layer then pass *all* elements 
		     to the rule, otherwise just select the matching elements for this layer. -->
    <xsl:choose>
      <xsl:when test='$layer=@layer'>
        <xsl:call-template name="rule">
          <xsl:with-param name="elements" select="$elements"/>
          <xsl:with-param name="layer" select="$layer"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test='not(@layer)'>
          <xsl:call-template name="rule">
            <xsl:with-param name="elements" select="$elements[
							tag[@k='layer' and @v=$layer]
							or ($layer='0' and count(tag[@k='layer'])=0)
						]"/>
            <xsl:with-param name="layer" select="$layer"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name='rule'>
    <xsl:param name="elements"/>
    <xsl:param name="layer"/>

    <!-- This is the rule currently being processed -->
    <xsl:variable name="rule" select="."/>

    <!-- Make list of elements that this rule should be applied to -->
    <xsl:variable name="eBare">
      <xsl:choose>
        <xsl:when test="$rule/@e='*'">node|way</xsl:when>
        <xsl:when test="$rule/@e">
          <xsl:value-of select="$rule/@e"/>
        </xsl:when>
        <xsl:otherwise>node|way</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- List of keys that this rule should be applied to -->
    <xsl:variable name="kBare" select="$rule/@k"/>

    <!-- List of values that this rule should be applied to -->
    <xsl:variable name="vBare" select="$rule/@v"/>
    <xsl:variable name="sBare" select="$rule/@s"/>

    <!-- Top'n'tail selectors with | for contains usage -->
    <xsl:variable name="e">
      |<xsl:value-of select="$eBare"/>|
    </xsl:variable>
    <xsl:variable name="k">
      |<xsl:value-of select="$kBare"/>|
    </xsl:variable>
    <xsl:variable name="v">
      |<xsl:value-of select="$vBare"/>|
    </xsl:variable>
    <xsl:variable name="s">
      |<xsl:value-of select="$sBare"/>|
    </xsl:variable>

    <xsl:variable
      name="selectedElements"
      select="$elements[contains($e,concat('|',name(),'|'))
            or 
            (contains($e,'|node|') and name()='way' and key('wayByNode',@id))
            ]"/>


    <!-- Patch $s -->
    <xsl:choose>
      <!-- way selector -->
      <xsl:when test="contains($s,'|way|')">
        <xsl:choose>
          <!-- every key -->
          <xsl:when test="contains($k,'|*|')">
            <xsl:choose>
              <!-- every key ,no value defined -->
              <xsl:when test="contains($v,'|~|')">
                <xsl:variable name="elementsWithNoTags" select="$selectedElements[count(key('wayByNode',@id)/tag)=0]"/>
                <xsl:call-template name="processElements">
                  <xsl:with-param name="eBare" select="$eBare"/>
                  <xsl:with-param name="kBare" select="$kBare"/>
                  <xsl:with-param name="vBare" select="$vBare"/>
                  <xsl:with-param name="layer" select="$layer"/>
                  <xsl:with-param name="elements" select="$elementsWithNoTags"/>
                  <xsl:with-param name="rule" select="$rule"/>
                </xsl:call-template>
              </xsl:when>
              <!-- every key ,every value -->
              <xsl:when test="contains($v,'|*|')">
                <xsl:variable name="allElements" select="$selectedElements"/>
                <xsl:call-template name="processElements">
                  <xsl:with-param name="eBare" select="$eBare"/>
                  <xsl:with-param name="kBare" select="$kBare"/>
                  <xsl:with-param name="vBare" select="$vBare"/>
                  <xsl:with-param name="layer" select="$layer"/>
                  <xsl:with-param name="elements" select="$allElements"/>
                  <xsl:with-param name="rule" select="$rule"/>
                </xsl:call-template>
              </xsl:when>
              <!-- every key , selected values -->
              <xsl:otherwise>
                <xsl:variable name="allElementsWithValue" select="$selectedElements[key('wayByNode',@id)/tag[contains($v,concat('|',@v,'|'))]]"/>
                <xsl:call-template name="processElements">
                  <xsl:with-param name="eBare" select="$eBare"/>
                  <xsl:with-param name="kBare" select="$kBare"/>
                  <xsl:with-param name="vBare" select="$vBare"/>
                  <xsl:with-param name="layer" select="$layer"/>
                  <xsl:with-param name="elements" select="$allElementsWithValue"/>
                  <xsl:with-param name="rule" select="$rule"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <!-- no value  -->
          <xsl:when test="contains($v,'|~|')">
            <xsl:variable name="elementsWithoutKey" select="$selectedElements[count(key('wayByNode',@id)/tag[contains($k,concat('|',@k,'|'))])=0]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$elementsWithoutKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:when>
          <!-- every value  -->
          <xsl:when test="contains($v,'|*|')">
            <xsl:variable name="allElementsWithKey" select="$selectedElements[key('wayByNode',@id)/tag[contains($k,concat('|',@k,'|'))]]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$allElementsWithKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:when>

          <!-- defined key and defined value -->
          <xsl:otherwise>
            <xsl:variable name="elementsWithKey" select="$selectedElements[
							key('wayByNode',@id)/tag[
								contains($k,concat('|',@k,'|')) and contains($v,concat('|',@v,'|'))
								]
							]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$elementsWithKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <!-- other selector -->
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="contains($k,'|*|')">
            <xsl:choose>
              <xsl:when test="contains($v,'|~|')">
                <xsl:variable name="elementsWithNoTags" select="$selectedElements[count(tag)=0]"/>
                <xsl:call-template name="processElements">
                  <xsl:with-param name="eBare" select="$eBare"/>
                  <xsl:with-param name="kBare" select="$kBare"/>
                  <xsl:with-param name="vBare" select="$vBare"/>
                  <xsl:with-param name="layer" select="$layer"/>
                  <xsl:with-param name="elements" select="$elementsWithNoTags"/>
                  <xsl:with-param name="rule" select="$rule"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:when test="contains($v,'|*|')">
                <xsl:variable name="allElements" select="$selectedElements"/>
                <xsl:call-template name="processElements">
                  <xsl:with-param name="eBare" select="$eBare"/>
                  <xsl:with-param name="kBare" select="$kBare"/>
                  <xsl:with-param name="vBare" select="$vBare"/>
                  <xsl:with-param name="layer" select="$layer"/>
                  <xsl:with-param name="elements" select="$allElements"/>
                  <xsl:with-param name="rule" select="$rule"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:variable name="allElementsWithValue" select="$selectedElements[tag[contains($v,concat('|',@v,'|'))]]"/>
                <xsl:call-template name="processElements">
                  <xsl:with-param name="eBare" select="$eBare"/>
                  <xsl:with-param name="kBare" select="$kBare"/>
                  <xsl:with-param name="vBare" select="$vBare"/>
                  <xsl:with-param name="layer" select="$layer"/>
                  <xsl:with-param name="elements" select="$allElementsWithValue"/>
                  <xsl:with-param name="rule" select="$rule"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="contains($v,'|~|')">
            <xsl:variable name="elementsWithoutKey" select="$selectedElements[count(tag[contains($k,concat('|',@k,'|'))])=0]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$elementsWithoutKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="contains($v,'|*|')">
            <xsl:variable name="allElementsWithKey" select="$selectedElements[tag[contains($k,concat('|',@k,'|'))]]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$allElementsWithKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="elementsWithKey" select="$selectedElements[tag[contains($k,concat('|',@k,'|')) and contains($v,concat('|',@v,'|'))]]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$elementsWithKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="else">
    <xsl:param name="elements"/>
    <xsl:param name="layer"/>

    <!-- This is the previous rule that is being negated -->
    <!-- TODO: abort if no preceding rule element -->
    <xsl:variable name="rule" select="preceding-sibling::rule[1]"/>

    <!-- Make list of elements that this rule should be applied to -->
    <xsl:variable name="eBare">
      <xsl:choose>
        <xsl:when test="$rule/@e='*'">node|way</xsl:when>
        <xsl:when test="$rule/@e">
          <xsl:value-of select="$rule/@e"/>
        </xsl:when>
        <xsl:otherwise>node|way</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- List of keys that this rule should be applied to -->
    <xsl:variable name="kBare" select="$rule/@k"/>

    <!-- List of values that this rule should be applied to -->
    <xsl:variable name="vBare" select="$rule/@v"/>
    <xsl:variable name="sBare" select="$rule/@s"/>


    <!-- Top'n'tail selectors with | for contains usage -->
    <xsl:variable name="e">
      |<xsl:value-of select="$eBare"/>|
    </xsl:variable>
    <xsl:variable name="k">
      |<xsl:value-of select="$kBare"/>|
    </xsl:variable>
    <xsl:variable name="v">
      |<xsl:value-of select="$vBare"/>|
    </xsl:variable>
    <xsl:variable name="s">
      |<xsl:value-of select="$sBare"/>|
    </xsl:variable>

    <xsl:variable
      name="selectedElements"
      select="$elements[contains($e,concat('|',name(),'|'))
              or 
              (contains($e,'|node|') and name()='way'and key('wayByNode',@id))
              ]"/>

    <!-- Patch $s -->
    <xsl:choose>
      <xsl:when test="contains($s,'|way|')">
        <xsl:choose>
          <xsl:when test="contains($k,'|*|')">
            <xsl:choose>
              <xsl:when test="contains($v,'|~|')">
                <xsl:variable name="elementsWithNoTags" select="$selectedElements[count(key('wayByNode',@id)/tag)!=0]"/>
                <xsl:call-template name="processElements">
                  <xsl:with-param name="eBare" select="$eBare"/>
                  <xsl:with-param name="kBare" select="$kBare"/>
                  <xsl:with-param name="vBare" select="$vBare"/>
                  <xsl:with-param name="layer" select="$layer"/>
                  <xsl:with-param name="elements" select="$elementsWithNoTags"/>
                  <xsl:with-param name="rule" select="$rule"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:when test="contains($v,'|*|')">
                <!-- no-op! -->
              </xsl:when>
              <xsl:otherwise>
                <xsl:variable name="allElementsWithValue" select="$selectedElements[not(key('wayByNode',@id)/tag[contains($v,concat('|',@v,'|'))])]"/>
                <xsl:call-template name="processElements">
                  <xsl:with-param name="eBare" select="$eBare"/>
                  <xsl:with-param name="kBare" select="$kBare"/>
                  <xsl:with-param name="vBare" select="$vBare"/>
                  <xsl:with-param name="layer" select="$layer"/>
                  <xsl:with-param name="elements" select="$allElementsWithValue"/>
                  <xsl:with-param name="rule" select="$rule"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="contains($v,'|~|')">
            <xsl:variable name="elementsWithoutKey" select="$selectedElements[count(key('wayByNode',@id)/tag[contains($k,concat('|',@k,'|'))])!=0]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$elementsWithoutKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="contains($v,'|*|')">
            <xsl:variable name="allElementsWithKey" select="$selectedElements[not(key('wayByNode',@id)/tag[contains($k,concat('|',@k,'|'))])]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$allElementsWithKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="elementsWithKey" select="$selectedElements[not(
                         key('wayByNode',@id)/tag[
                            contains($k,concat('|',@k,'|')) and contains($v,concat('|',@v,'|'))
                            ]
                         )]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$elementsWithKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <xsl:otherwise>
        <!-- not contains $s -->
        <xsl:choose>
          <xsl:when test="contains($k,'|*|')">
            <xsl:choose>
              <xsl:when test="contains($v,'|~|')">
                <xsl:variable name="elementsWithNoTags" select="$selectedElements[count(tag)!=0]"/>
                <xsl:call-template name="processElements">
                  <xsl:with-param name="eBare" select="$eBare"/>
                  <xsl:with-param name="kBare" select="$kBare"/>
                  <xsl:with-param name="vBare" select="$vBare"/>
                  <xsl:with-param name="layer" select="$layer"/>
                  <xsl:with-param name="elements" select="$elementsWithNoTags"/>
                  <xsl:with-param name="rule" select="$rule"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:when test="contains($v,'|*|')">
                <!-- no-op! -->
              </xsl:when>
              <xsl:otherwise>
                <xsl:variable name="allElementsWithValue" select="$selectedElements[not(tag[contains($v,concat('|',@v,'|'))])]"/>
                <xsl:call-template name="processElements">
                  <xsl:with-param name="eBare" select="$eBare"/>
                  <xsl:with-param name="kBare" select="$kBare"/>
                  <xsl:with-param name="vBare" select="$vBare"/>
                  <xsl:with-param name="layer" select="$layer"/>
                  <xsl:with-param name="elements" select="$allElementsWithValue"/>
                  <xsl:with-param name="rule" select="$rule"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="contains($v,'|~|')">
            <xsl:variable name="elementsWithoutKey" select="$selectedElements[count(tag[contains($k,concat('|',@k,'|'))])!=0]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$elementsWithoutKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="contains($v,'|*|')">
            <xsl:variable name="allElementsWithKey" select="$selectedElements[not(tag[contains($k,concat('|',@k,'|'))])]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$allElementsWithKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:variable name="elementsWithKey" select="$selectedElements[not(tag[contains($k,concat('|',@k,'|')) and contains($v,concat('|',@v,'|'))])]"/>
            <xsl:call-template name="processElements">
              <xsl:with-param name="eBare" select="$eBare"/>
              <xsl:with-param name="kBare" select="$kBare"/>
              <xsl:with-param name="vBare" select="$vBare"/>
              <xsl:with-param name="layer" select="$layer"/>
              <xsl:with-param name="elements" select="$elementsWithKey"/>
              <xsl:with-param name="rule" select="$rule"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="processElements">
    <xsl:param name="eBare"/>
    <xsl:param name="kBare"/>
    <xsl:param name="vBare"/>
    <xsl:param name="layer"/>
    <xsl:param name="elements"/>
    <xsl:param name="rule"/>


    <xsl:if test="$elements">
  
      <!-- elementCount is the number of elements we started with (just used for the progress message) -->
      <xsl:variable name="elementCount" select="count($elements)"/>
      <!-- If there's a proximity attribute on the rule then filter elements based on proximity -->
      <xsl:choose>
        <xsl:when test='$rule/@verticalProximity'>
          <xsl:variable name='nearbyElements1'>
            <xsl:call-template name="proximityFilter">
              <xsl:with-param name="elements" select="$elements"/>
              <xsl:with-param name="horizontalProximity" select="$rule/@horizontalProximity div 32"/>
              <xsl:with-param name="verticalProximity" select="$rule/@verticalProximity div 32"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:variable name='nearbyElements2'>
            <xsl:call-template name="proximityFilter">
              <xsl:with-param name="elements" select="exslt:node-set($nearbyElements1)/*"/>
              <xsl:with-param name="horizontalProximity" select="$rule/@horizontalProximity div 16"/>
              <xsl:with-param name="verticalProximity" select="$rule/@verticalProximity div 16"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:variable name='nearbyElements3'>
            <xsl:call-template name="proximityFilter">
              <xsl:with-param name="elements" select="exslt:node-set($nearbyElements2)/*"/>
              <xsl:with-param name="horizontalProximity" select="$rule/@horizontalProximity div 8"/>
              <xsl:with-param name="verticalProximity" select="$rule/@verticalProximity div 8"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:variable name='nearbyElements4'>
            <xsl:call-template name="proximityFilter">
              <xsl:with-param name="elements" select="exslt:node-set($nearbyElements3)/*"/>
              <xsl:with-param name="horizontalProximity" select="$rule/@horizontalProximity div 4"/>
              <xsl:with-param name="verticalProximity" select="$rule/@verticalProximity div 4"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:variable name='nearbyElements5'>
            <xsl:call-template name="proximityFilter">
              <xsl:with-param name="elements" select="exslt:node-set($nearbyElements4)/*"/>
              <xsl:with-param name="horizontalProximity" select="$rule/@horizontalProximity div 2"/>
              <xsl:with-param name="verticalProximity" select="$rule/@verticalProximity div 2"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:variable name='nearbyElementsRtf'>
            <xsl:call-template name="proximityFilter">
              <xsl:with-param name="elements" select="exslt:node-set($nearbyElements5)/*"/>
              <xsl:with-param name="horizontalProximity" select="$rule/@horizontalProximity"/>
              <xsl:with-param name="verticalProximity" select="$rule/@verticalProximity"/>
            </xsl:call-template>
          </xsl:variable>

          <!-- Convert nearbyElements rtf to a node-set -->
          <xsl:variable name="nearbyElements" select="exslt:node-set($nearbyElementsRtf)/*"/>

          <xsl:message>
            Processing &lt;rule e="<xsl:value-of select="$eBare"/>" k="<xsl:value-of select="$kBare"/>" v="<xsl:value-of select="$vBare"/>"
                        horizontalProximity="<xsl:value-of select="$rule/@horizontalProximity"/>" verticalProximity="<xsl:value-of select="$rule/@verticalProximity"/>" &gt;
            Matched by <xsl:value-of select="count($nearbyElements)"/> out of <xsl:value-of select="count($elements)"/> elements for layer <xsl:value-of select="$layer"/>.
          </xsl:message>

          <xsl:apply-templates select="*">
            <xsl:with-param name="layer" select="$layer"/>
            <xsl:with-param name="elements" select="$nearbyElements"/>
            <xsl:with-param name="rule" select="$rule"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>

          <xsl:message>
            Processing &lt;rule e="<xsl:value-of select="$eBare"/>" k="<xsl:value-of select="$kBare"/>" v="<xsl:value-of select="$vBare"/>" &gt;
            Matched by <xsl:value-of select="count($elements)"/> elements for layer <xsl:value-of select="$layer"/>.
          </xsl:message>

          <xsl:apply-templates select="*">
            <xsl:with-param name="layer" select="$layer"/>
            <xsl:with-param name="elements" select="$elements"/>
            <xsl:with-param name="rule" select="$rule"/>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>


  <!-- Select elements that are not within the specified distance from any other element -->
  <xsl:template name="proximityFilter">
    <xsl:param name="elements"/>
    <xsl:param name="horizontalProximity"/>
    <xsl:param name="verticalProximity"/>
    
    <!-- Offsetting the rectangle to the right gives better results when there are a solitary pair of adjacent elements.  
         One will get selected but the other won't.  Without the offset neither will get selected.  -->
    <xsl:variable name="topOffset" select="90  + $verticalProximity"/>
    <xsl:variable name="bottomOffset" select="90  - $verticalProximity"/>
    <xsl:variable name="leftOffset" select="180 - ($horizontalProximity * 0.5)"/>
    <xsl:variable name="rightOffset" select="180 + ($horizontalProximity * 1.5)"/>

    <!-- Test each element to see if it is near any other element -->
    <xsl:for-each select="$elements">
      <xsl:variable name="id" select="@id"/>
      <xsl:variable name="top"    select="@lat + $topOffset"/>
      <xsl:variable name="bottom" select="@lat + $bottomOffset"/>
      <xsl:variable name="left"   select="@lon + $leftOffset"/>
      <xsl:variable name="right"  select="@lon + $rightOffset"/>
      <!-- Iterate through all of the elements currently selected and if there are no elements other 
           than the current element in the rectangle then select this element -->
      <xsl:if test="not($elements[not(@id=$id) 
                                  and (@lon+180) &lt; $right
                                  and (@lon+180) &gt; $left 
                                  and (@lat+90)  &lt; $top 
                                  and (@lat+90)  &gt; $bottom
                                  ]
                        )">
        <xsl:copy-of select="."/>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>


  <!-- Draw SVG layers -->
  <xsl:template match="layer">
    <xsl:param name="elements"/>
    <xsl:param name="layer"/>
    <xsl:param name="rule"/>

    <xsl:message>
      Processing SVG layer: <xsl:value-of select="@name"/> (at OSM layer <xsl:value-of select="$layer"/>)
    </xsl:message>

    <xsl:variable name="opacity">
      <xsl:if test="@opacity">
        <xsl:value-of select="concat('opacity:',@opacity,';')"/>
      </xsl:if>
    </xsl:variable>

    <xsl:variable name="display">
      <xsl:if test="(@display='none') or (@display='off')">
        <xsl:text>display:none;</xsl:text>
      </xsl:if>
    </xsl:variable>

    <g inkscape:groupmode="layer" id="{@name}-{$layer}" inkscape:label="{@name}">
      <xsl:if test="concat($opacity,$display)!=''">
        <xsl:attribute name="style">
          <xsl:value-of select="concat($opacity,$display)"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="*">
        <xsl:with-param name="layer" select="$layer"/>
        <xsl:with-param name="elements" select="$elements"/>
      </xsl:apply-templates>
    </g>

  </xsl:template>


  <!-- Draw map border -->
  <xsl:template name="drawBorder">
    <!-- dasharray definitions here can be overridden in stylesheet -->
    <g id="border" inkscape:groupmode="layer" inkscape:label="Map Border">
      <line id="border-left-casing" x1="0" y1="0" x2="0" y2="{$documentHeight}" class="map-border-casing" stroke-dasharray="{($km div 10) - 1},1"/>
      <line id="border-top-casing" x1="0" y1="0" x2="{$documentWidth}" y2="0" class="map-border-casing" stroke-dasharray="{($km div 10) - 1},1"/>
      <line id="border-bottom-casing" x1="0" y1="{$documentHeight}" x2="{$documentWidth}" y2="{$documentHeight}" class="map-border-casing" stroke-dasharray="{($km div 10) - 1},1"/>
      <line id="border-right-casing" x1="{$documentWidth}" y1="0" x2="{$documentWidth}" y2="{$documentHeight}" class="map-border-casing" stroke-dasharray="{($km div 10) - 1},1"/>

      <line id="border-left-core" x1="0" y1="0" x2="0" y2="{$documentHeight}" class="map-border-core" stroke-dasharray="{($km div 10) - 1},1"/>
      <line id="border-top-core" x1="0" y1="0" x2="{$documentWidth}" y2="0" class="map-border-core" stroke-dasharray="{($km div 10) - 1},1"/>
      <line id="border-bottom-core" x1="0" y1="{$documentHeight}" x2="{$documentWidth}" y2="{$documentHeight}" class="map-border-core" stroke-dasharray="{($km div 10) - 1},1"/>
      <line id="border-right-core" x1="{$documentWidth}" y1="0" x2="{$documentWidth}" y2="{$documentHeight}" class="map-border-core" stroke-dasharray="{($km div 10) - 1},1"/>
    </g>
  </xsl:template>


  <!-- Draw a grid over the map in 1km increments -->
  <xsl:template name="drawGrid">
    <g id="grid" inkscape:groupmode="layer" inkscape:label="Grid">
      <xsl:call-template name="drawGridHorizontals">
        <xsl:with-param name="line" select="'1'"/>
      </xsl:call-template>
      <xsl:call-template name="drawGridVerticals">
        <xsl:with-param name="line" select="'1'"/>
      </xsl:call-template>
    </g>
  </xsl:template>


  <xsl:template name="drawGridHorizontals">
    <xsl:param name="line"/>
    <xsl:if test="($line*$km) &lt; $documentHeight">
      <line id="grid-hori-{$line}" x1="0px" y1="{$line*$km}px" x2="{$documentWidth}px" y2="{$line*$km}px" class="map-grid-line"/>
      <xsl:call-template name="drawGridHorizontals">
        <xsl:with-param name="line" select="$line+1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <xsl:template name="drawGridVerticals">
    <xsl:param name="line"/>
    <xsl:if test="($line*$km) &lt; $documentWidth">
      <line id="grid-vert-{$line}" x1="{$line*$km}px" y1="0px" x2="{$line*$km}px" y2="{$documentHeight}px" class="map-grid-line"/>
      <xsl:call-template name="drawGridVerticals">
        <xsl:with-param name="line" select="$line+1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>


  <!-- Draw map title -->
  <xsl:template name="drawTitle">
    <xsl:param name="title"/>

    <xsl:variable name="x" select="$documentWidth div 2"/>
    <xsl:variable name="y" select="30"/>

    <g id="marginalia-title" inkscape:groupmode="layer" inkscape:label="Title">
      <rect id="marginalia-title-background" x="0px" y="0px" height="{$marginaliaTopHeight - 5}px" width="{$documentWidth}px" class="map-title-background"/>
      <text id="marginalia-title-text" class="map-title" x="{$x}" y="{$y}">
        <xsl:value-of select="$title"/>
      </text>
    </g>
  </xsl:template>


  <!-- Draw an approximate scale in the bottom left corner of the map -->
  <xsl:template name="drawScale">
    <xsl:variable name="x1" select="14"/>
    <xsl:variable name="y1" select="round(($documentHeight)+((($bottomLeftLatitude)-(number($bottomLeftLatitude)))*10000*$scale*$projection))+28"/>
    <xsl:variable name="x2" select="$x1+$km"/>
    <xsl:variable name="y2" select="$y1"/>

    <g id="marginalia-scale" inkscape:groupmode="layer" inkscape:label="Scale">
      <line id="marginalia-scale-casing" class="map-scale-casing" x1="{$x1}" y1="{$y1}" x2="{$x2}" y2="{$y2}"/>

      <line id="marginalia-scale-core" class="map-scale-core" stroke-dasharray="{($km div 10)}" x1="{$x1}" y1="{$y1}" x2="{$x2}" y2="{$y2}"/>

      <line id="marginalia-scale-bookend-from" class="map-scale-bookend" x1="{$x1}" y1="{$y1 + 2}" x2="{$x1}" y2="{$y1 - 10}"/>

      <line id="marginalia-scale-bookend-to" class="map-scale-bookend" x1="{$x2}" y1="{$y2 + 2}" x2="{$x2}" y2="{$y2 - 10}"/>

      <text id="marginalia-scale-text-from" class="map-scale-caption" x="{$x1}" y="{$y1 - 10}">0</text>

      <text id="marginalia-scale-text-to" class="map-scale-caption" x="{$x2}" y="{$y2 - 10}">1km</text>
    </g>
  </xsl:template>


  <!-- Create a comment in SVG source code and RDF description of license -->
  <xsl:template name="metadata">

    <xsl:comment>

      Copyright (c) <xsl:value-of select="$year"/> OpenStreetMap
      www.openstreetmap.org
      This work is licensed under the
      Creative Commons Attribution-ShareAlike 2.0 License.
      http://creativecommons.org/licenses/by-sa/2.0/

    </xsl:comment>
    <metadata id="metadata">
      <rdf:RDF xmlns="http://web.resource.org/cc/">
        <cc:Work rdf:about="">
          <cc:license rdf:resource="http://creativecommons.org/licenses/by-sa/2.0/"/>
          <dc:format>image/svg+xml</dc:format>
          <dc:type rdf:resource="http://purl.org/dc/dcmitype/StillImage"/>
          <dc:title>
            <xsl:value-of select="$title"/>
          </dc:title>
          <dc:date>
            <xsl:value-of select="$date"/>
          </dc:date>
          <dc:source>http://www.openstreetmap.org/</dc:source>
        </cc:Work>
        <cc:License rdf:about="http://creativecommons.org/licenses/by-sa/2.0/">
          <cc:permits rdf:resource="http://web.resource.org/cc/Reproduction"/>
          <cc:permits rdf:resource="http://web.resource.org/cc/Distribution"/>
          <cc:permits rdf:resource="http://web.resource.org/cc/DerivativeWorks"/>
          <cc:requires rdf:resource="http://web.resource.org/cc/Notice"/>
          <cc:requires rdf:resource="http://web.resource.org/cc/Attribution"/>
          <cc:requires rdf:resource="http://web.resource.org/cc/ShareAlike"/>
        </cc:License>
      </rdf:RDF>
    </metadata>
  </xsl:template>

  <!-- Create a license logo and description in the image -->
  <xsl:template name="in-image-license">
    <xsl:param name="dx"/>
    <xsl:param name="dy"/>

    <g id="license" inkscape:groupmode="layer" inkscape:label="Copyright" transform="translate({$dx},{$dy})">
      <style type="text/css">
        <![CDATA[
                .license-text {
                    text-anchor: start;
                    font-family: "DejaVu Sans",sans-serif;
                    font-size: 6px;
                    fill: black;
                }
            ]]>
      </style>
      <a id="license-cc-logo-link" xlink:href="http://creativecommons.org/licenses/by-sa/2.0/">
        <g id="license-cc-logo" transform="scale(0.5,0.5) translate(-604,-49)">
          <path id="path3817_2_" nodetypes="ccccccc" d="M
                    182.23532,75.39014 L 296.29928,75.59326 C
                    297.89303,75.59326 299.31686,75.35644 299.31686,78.77344 L
                    299.17721,116.34033 L 179.3569,116.34033 L
                    179.3569,78.63379 C 179.3569,76.94922 179.51999,75.39014
                    182.23532,75.39014 z " style="fill:#aab2ab"/>
          <g id="g5908_2_" transform="matrix(0.872921,0,0,0.872921,50.12536,143.2144)">
            <path id="path5906_2_" type="arc" cx="296.35416"
            cy="264.3577" ry="22.939548" rx="22.939548" d="M
                        187.20944,-55.6792 C 187.21502,-46.99896
                        180.18158,-39.95825 171.50134,-39.95212 C
                        162.82113,-39.94708 155.77929,-46.97998
                        155.77426,-55.66016 C 155.77426,-55.66687
                        155.77426,-55.67249 155.77426,-55.6792 C
                        155.76922,-64.36054 162.80209,-71.40125
                        171.48233,-71.40631 C 180.16367,-71.41193
                        187.20441,-64.37842 187.20944,-55.69824 C
                        187.20944,-55.69263 187.20944,-55.68591
                        187.20944,-55.6792 z " style="fill:white"/>
            <g id="g5706_2_" transform="translate(-289.6157,99.0653)">
              <path id="path5708_2_" d="M 473.88455,-167.54724 C
                            477.36996,-164.06128 479.11294,-159.79333
                            479.11294,-154.74451 C 479.11294,-149.69513
                            477.40014,-145.47303 473.9746,-142.07715 C
                            470.33929,-138.50055 466.04281,-136.71283
                            461.08513,-136.71283 C 456.18736,-136.71283
                            451.96526,-138.48544 448.42003,-142.03238 C
                            444.87419,-145.57819 443.10158,-149.81537
                            443.10158,-154.74451 C 443.10158,-159.6731
                            444.87419,-163.94049 448.42003,-167.54724 C
                            451.87523,-171.03375 456.09728,-172.77618
                            461.08513,-172.77618 C 466.13342,-172.77618
                            470.39914,-171.03375 473.88455,-167.54724 z M
                            450.76657,-165.20239 C 447.81982,-162.22601
                            446.34701,-158.7395 446.34701,-154.74005 C
                            446.34701,-150.7417 447.80529,-147.28485
                            450.72125,-144.36938 C 453.63778,-141.45288
                            457.10974,-139.99462 461.1383,-139.99462 C
                            465.16683,-139.99462 468.66848,-141.46743
                            471.64486,-144.41363 C 474.47076,-147.14947
                            475.88427,-150.59069 475.88427,-154.74005 C
                            475.88427,-158.85809 474.44781,-162.35297
                            471.57659,-165.22479 C 468.70595,-168.09546
                            465.22671,-169.53131 461.1383,-169.53131 C
                            457.04993,-169.53131 453.59192,-168.08813
                            450.76657,-165.20239 z M 458.52106,-156.49927 C
                            458.07074,-157.4809 457.39673,-157.9715
                            456.49781,-157.9715 C 454.90867,-157.9715
                            454.11439,-156.90198 454.11439,-154.763 C
                            454.11439,-152.62341 454.90867,-151.55389
                            456.49781,-151.55389 C 457.54719,-151.55389
                            458.29676,-152.07519 458.74647,-153.11901 L
                            460.94923,-151.94598 C 459.8993,-150.0805
                            458.32417,-149.14697 456.22374,-149.14697 C
                            454.60384,-149.14697 453.30611,-149.64367
                            452.33168,-150.63653 C 451.35561,-151.62994
                            450.86894,-152.99926 450.86894,-154.7445 C
                            450.86894,-156.46008 451.37123,-157.82159
                            452.37642,-158.83013 C 453.38161,-159.83806
                            454.63347,-160.34264 456.13423,-160.34264 C
                            458.35435,-160.34264 459.94407,-159.46776
                            460.90504,-157.71978 L 458.52106,-156.49927 z M
                            468.8844,-156.49927 C 468.43353,-157.4809
                            467.77292,-157.9715 466.90201,-157.9715 C
                            465.28095,-157.9715 464.46988,-156.90198
                            464.46988,-154.763 C 464.46988,-152.62341
                            465.28095,-151.55389 466.90201,-151.55389 C
                            467.95304,-151.55389 468.68918,-152.07519
                            469.10925,-153.11901 L 471.36126,-151.94598 C
                            470.31301,-150.0805 468.74007,-149.14697
                            466.64358,-149.14697 C 465.02587,-149.14697
                            463.73095,-149.64367 462.75711,-150.63653 C
                            461.78494,-151.62994 461.29773,-152.99926
                            461.29773,-154.7445 C 461.29773,-156.46008
                            461.79221,-157.82159 462.78061,-158.83013 C
                            463.76843,-159.83806 465.02588,-160.34264
                            466.55408,-160.34264 C 468.77027,-160.34264
                            470.35776,-159.46776 471.3154,-157.71978 L
                            468.8844,-156.49927 z "/>
            </g>
          </g>
          <path d="M 297.29639,74.91064 L 181.06688,74.91064 C
                    179.8203,74.91064 178.80614,75.92529 178.80614,77.17187 L
                    178.80614,116.66748 C 178.80614,116.94922
                    179.03466,117.17822 179.31639,117.17822 L
                    299.04639,117.17822 C 299.32813,117.17822
                    299.55713,116.94922 299.55713,116.66748 L
                    299.55713,77.17188 C 299.55713,75.92529 298.54297,74.91064
                    297.29639,74.91064 z M 181.06688,75.93213 L
                    297.29639,75.93213 C 297.97998,75.93213 298.53565,76.48828
                    298.53565,77.17188 C 298.53565,77.17188 298.53565,93.09131
                    298.53565,104.59034 L 215.4619,104.59034 C
                    212.41698,110.09571 206.55077,113.83399 199.81835,113.83399
                    C 193.083,113.83399 187.21825,110.09913 184.1748,104.59034
                    L 179.82666,104.59034 C 179.82666,93.09132
                    179.82666,77.17188 179.82666,77.17188 C 179.82664,76.48828
                    180.38329,75.93213 181.06688,75.93213 z " id="frame"/>
          <g enable-background="new" id="g2821">
            <path d="M 265.60986,112.8833 C 265.68994,113.03906
                        265.79736,113.16504 265.93115,113.26172 C
                        266.06494,113.35791 266.22119,113.42969
                        266.40088,113.47608 C 266.58154,113.52296
                        266.76807,113.54639 266.96045,113.54639 C
                        267.09033,113.54639 267.22998,113.53565
                        267.3794,113.51368 C 267.52784,113.4922
                        267.66749,113.44972 267.79835,113.3877 C
                        267.92823,113.32569 268.03761,113.23975
                        268.12355,113.13086 C 268.21144,113.02197
                        268.25441,112.88379 268.25441,112.71533 C
                        268.25441,112.53515 268.19679,112.38916
                        268.08156,112.27685 C 267.9673,112.16455
                        267.81594,112.07177 267.62941,111.99658 C
                        267.44386,111.92236 267.23195,111.85693
                        266.9966,111.80078 C 266.76027,111.74463
                        266.52101,111.68262 266.27883,111.61377 C
                        266.02981,111.55176 265.78762,111.47559
                        265.55129,111.38525 C 265.31594,111.29541
                        265.10402,111.17822 264.9175,111.03515 C
                        264.73098,110.89208 264.58059,110.71337
                        264.46535,110.49853 C 264.35109,110.28369
                        264.29347,110.02392 264.29347,109.71923 C
                        264.29347,109.37646 264.36671,109.07958
                        264.51222,108.82763 C 264.6587,108.57568
                        264.85011,108.36572 265.08644,108.19726 C
                        265.32179,108.02929 265.58937,107.90478
                        265.8882,107.82372 C 266.18605,107.74315
                        266.48488,107.70263 266.78273,107.70263 C
                        267.13136,107.70263 267.46535,107.74169
                        267.78566,107.81982 C 268.105,107.89746
                        268.39015,108.02392 268.6382,108.19824 C
                        268.88722,108.37256 269.08449,108.59521
                        269.23097,108.86621 C 269.37648,109.13721
                        269.44972,109.46582 269.44972,109.85156 L
                        268.02784,109.85156 C 268.01514,109.65234
                        267.97315,109.4873 267.90284,109.35693 C
                        267.83155,109.22607 267.73682,109.12353
                        267.61964,109.04834 C 267.50148,108.97412
                        267.36671,108.9209 267.21534,108.89014 C
                        267.063,108.85889 266.89796,108.84326
                        266.71827,108.84326 C 266.60108,108.84326
                        266.48292,108.85596 266.36573,108.88037 C
                        266.24757,108.90576 266.14112,108.94922
                        266.04542,109.01123 C 265.94874,109.07373
                        265.86964,109.15137 265.80812,109.24463 C
                        265.7466,109.33838 265.71535,109.45654
                        265.71535,109.59961 C 265.71535,109.73047
                        265.73976,109.83643 265.78957,109.91699 C
                        265.83937,109.99804 265.93801,110.07275
                        266.08352,110.14111 C 266.22903,110.20947
                        266.43118,110.27832 266.68899,110.34668 C
                        266.9468,110.41504 267.28372,110.50244
                        267.70071,110.60791 C 267.82473,110.63281
                        267.99661,110.67822 268.21731,110.74365 C
                        268.43801,110.80908 268.65676,110.91308
                        268.87454,111.05615 C 269.09231,111.1997
                        269.27981,111.39111 269.43899,111.63037 C
                        269.59719,111.87012 269.67629,112.17676
                        269.67629,112.55029 C 269.67629,112.85547
                        269.61672,113.13867 269.49856,113.3999 C
                        269.3804,113.66162 269.20461,113.8872
                        268.97122,114.07666 C 268.73782,114.26709
                        268.44876,114.41455 268.10403,114.52051 C
                        267.75833,114.62647 267.35794,114.6792
                        266.90481,114.6792 C 266.53762,114.6792
                        266.18118,114.63379 265.83547,114.54346 C
                        265.49074,114.45313 265.18508,114.31104
                        264.92043,114.11768 C 264.65676,113.92432
                        264.4468,113.67774 264.29055,113.37891 C
                        264.13528,113.07959 264.06106,112.7251
                        264.06692,112.31397 L 265.4888,112.31397 C
                        265.48877,112.53809 265.52881,112.72803
                        265.60986,112.8833 z " id="path2823"
            style="fill:white"/>
            <path d="M 273.8667,107.8667 L
                        276.35986,114.53076 L 274.8374,114.53076 L
                        274.33349,113.04638 L 271.84033,113.04638 L
                        271.31787,114.53076 L 269.84326,114.53076 L
                        272.36377,107.8667 L 273.8667,107.8667 z M
                        273.95068,111.95264 L 273.11084,109.50928 L
                        273.09229,109.50928 L 272.22315,111.95264 L
                        273.95068,111.95264 z " id="path2825"
            style="fill:white"/>
          </g>
          <g enable-background="new" id="g2827">
            <path d="M 239.17821,107.8667 C 239.49559,107.8667
                        239.78563,107.89502 240.04735,107.95068 C
                        240.30907,108.00683 240.53368,108.09863
                        240.72118,108.22607 C 240.9077,108.35351
                        241.05321,108.52295 241.15575,108.73437 C
                        241.25829,108.94579 241.31005,109.20703
                        241.31005,109.51806 C 241.31005,109.854
                        241.23388,110.13329 241.08056,110.35742 C
                        240.92822,110.58154 240.70165,110.76465
                        240.40283,110.90771 C 240.81494,111.02587
                        241.12256,111.23291 241.32568,111.5288 C
                        241.5288,111.82469 241.63037,112.18114
                        241.63037,112.59814 C 241.63037,112.93408
                        241.56494,113.22509 241.43408,113.47119 C
                        241.30322,113.7168 241.12646,113.91748
                        240.90576,114.07324 C 240.68408,114.229
                        240.43115,114.34424 240.14795,114.41845 C
                        239.86377,114.49365 239.57275,114.53075
                        239.27295,114.53075 L 236.03662,114.53075 L
                        236.03662,107.86669 L 239.17821,107.86669 L
                        239.17821,107.8667 z M 238.99071,110.56201 C
                        239.25243,110.56201 239.46727,110.5 239.63622,110.37597
                        C 239.80419,110.25146 239.88817,110.05029
                        239.88817,109.77099 C 239.88817,109.61572
                        239.85985,109.48828 239.80419,109.38915 C
                        239.74755,109.28954 239.67333,109.21239
                        239.57958,109.15624 C 239.48583,109.10058
                        239.37841,109.06151 239.25731,109.04003 C
                        239.13524,109.01806 239.00926,109.00732
                        238.8784,109.00732 L 237.50535,109.00732 L
                        237.50535,110.56201 L 238.99071,110.56201 z M
                        239.07664,113.39014 C 239.22019,113.39014
                        239.35691,113.37647 239.48777,113.34815 C
                        239.61863,113.32032 239.73484,113.27344
                        239.83445,113.2085 C 239.93406,113.14307
                        240.01316,113.0542 240.07273,112.94239 C
                        240.1323,112.83058 240.1616,112.68751
                        240.1616,112.51319 C 240.1616,112.17139
                        240.06492,111.92725 239.87156,111.78126 C
                        239.6782,111.63527 239.42234,111.56202
                        239.10496,111.56202 L 237.50535,111.56202 L
                        237.50535,113.39014 L 239.07664,113.39014 z "
            id="path2829" style="fill:white"/>
            <path d="M 241.88914,107.8667 L 243.53269,107.8667 L
                        245.09324,110.49854 L 246.64402,107.8667 L
                        248.27781,107.8667 L 245.80418,111.97315 L
                        245.80418,114.53077 L 244.33543,114.53077 L
                        244.33543,111.93604 L 241.88914,107.8667 z "
            id="path2831" style="fill:white"/>
          </g>
          <g id="g6316_1_" transform="matrix(0.624995,0,0,0.624995,391.2294,176.9332)">
            <path id="path6318_1_" type="arc" cx="475.97119"
            cy="252.08646" ry="29.209877" rx="29.209877" d="M
                        -175.0083,-139.1153 C -175.00204,-129.7035
                        -182.62555,-122.06751 -192.03812,-122.06049 C
                        -201.44913,-122.05341 -209.08512,-129.67774
                        -209.09293,-139.09028 C -209.09293,-139.09809
                        -209.09293,-139.10749 -209.09293,-139.1153 C
                        -209.09919,-148.52784 -201.47413,-156.1623
                        -192.06311,-156.17011 C -182.65054,-156.17713
                        -175.01456,-148.55207 -175.0083,-139.14026 C
                        -175.0083,-139.13092 -175.0083,-139.1239
                        -175.0083,-139.1153 z " style="fill:white"/>
            <g id="g6320_1_" transform="translate(-23.9521,-89.72962)">
              <path id="path6322_1_" d="M -168.2204,-68.05536 C
                            -173.39234,-68.05536 -177.76892,-66.25067
                            -181.35175,-62.64203 C -185.02836,-58.90759
                            -186.86588,-54.48883 -186.86588,-49.38568 C
                            -186.86588,-44.28253 -185.02836,-39.89416
                            -181.35175,-36.22308 C -177.67673,-32.55114
                            -173.29859,-30.71521 -168.2204,-30.71521 C
                            -163.07974,-30.71521 -158.62503,-32.56677
                            -154.85312,-36.26996 C -151.30307,-39.78558
                            -149.52652,-44.15827 -149.52652,-49.38568 C
                            -149.52652,-54.6123 -151.33432,-59.03265
                            -154.94843,-62.64203 C -158.5625,-66.25067
                            -162.98599,-68.05536 -168.2204,-68.05536 z M
                            -168.17352,-64.69519 C -163.936,-64.69519
                            -160.33752,-63.20221 -157.37655,-60.21466 C
                            -154.38748,-57.25836 -152.89214,-53.64899
                            -152.89214,-49.38568 C -152.89214,-45.09186
                            -154.35466,-41.52856 -157.28438,-38.69653 C
                            -160.36876,-35.64727 -163.99849,-34.12304
                            -168.17351,-34.12304 C -172.34856,-34.12304
                            -175.94701,-35.63244 -178.96892,-38.64965 C
                            -181.9908,-41.66918 -183.50176,-45.24657
                            -183.50176,-49.38567 C -183.50176,-53.52398
                            -181.97518,-57.13414 -178.92205,-60.21465 C
                            -175.9939,-63.20221 -172.41107,-64.69519
                            -168.17352,-64.69519 z "/>
              <path id="path6324_1_" d="M -176.49548,-52.02087 C
                            -175.75171,-56.71856 -172.44387,-59.22949
                            -168.30008,-59.22949 C -162.33911,-59.22949
                            -158.70783,-54.90448 -158.70783,-49.1372 C
                            -158.70783,-43.50982 -162.57194,-39.13793
                            -168.39383,-39.13793 C -172.39856,-39.13793
                            -175.98297,-41.60277 -176.63611,-46.43877 L
                            -171.93292,-46.43877 C -171.7923,-43.92778
                            -170.1626,-43.04418 -167.83447,-43.04418 C
                            -165.1813,-43.04418 -163.4563,-45.50908
                            -163.4563,-49.27709 C -163.4563,-53.22942
                            -164.94693,-55.32244 -167.74228,-55.32244 C
                            -169.79074,-55.32244 -171.55948,-54.57787
                            -171.93292,-52.02087 L -170.56418,-52.02789 L
                            -174.26734,-48.32629 L -177.96894,-52.02789 L
                            -176.49548,-52.02087 z "/>
            </g>
          </g>
          <g id="g2838">
            <circle cx="242.56226" cy="90.224609" r="10.8064" id="circle2840" style="fill:white"/>
            <g id="g2842">
              <path d="M 245.68994,87.09766 C 245.68994,86.68116
                            245.35205,86.34424 244.93603,86.34424 L
                            240.16357,86.34424 C 239.74755,86.34424
                            239.40966,86.68115 239.40966,87.09766 L
                            239.40966,91.87061 L 240.74071,91.87061 L
                            240.74071,97.52295 L 244.3579,97.52295 L
                            244.3579,91.87061 L 245.68993,91.87061 L
                            245.68993,87.09766 L 245.68994,87.09766 z "
              id="path2844"/>
              <circle cx="242.5498" cy="84.083008" r="1.63232" id="circle2846"/>
            </g>
            <path clip-rule="evenodd" d="M 242.53467,78.31836 C
                        239.30322,78.31836 236.56641,79.4458 234.32715,81.70215
                        C 232.0293,84.03516 230.88086,86.79736
                        230.88086,89.98633 C 230.88086,93.1753
                        232.0293,95.91846 234.32715,98.21338 C
                        236.625,100.50781 239.36133,101.65527
                        242.53467,101.65527 C 245.74756,101.65527
                        248.53272,100.49853 250.88819,98.18359 C
                        253.10889,95.98681 254.21827,93.2539 254.21827,89.98632
                        C 254.21827,86.71874 253.08936,83.95751
                        250.83057,81.70214 C 248.57178,79.4458
                        245.80615,78.31836 242.53467,78.31836 z M
                        242.56396,80.41797 C 245.2124,80.41797
                        247.46142,81.35156 249.31103,83.21875 C
                        251.18115,85.06592 252.11572,87.32227
                        252.11572,89.98633 C 252.11572,92.66992
                        251.20068,94.89746 249.36963,96.66699 C
                        247.4419,98.57275 245.17334,99.52539 242.56397,99.52539
                        C 239.9546,99.52539 237.70557,98.58252
                        235.81739,96.6958 C 233.92774,94.80957
                        232.98389,92.57324 232.98389,89.98633 C
                        232.98389,87.3999 233.93799,85.14404 235.84619,83.21875
                        C 237.67676,81.35156 239.9165,80.41797
                        242.56396,80.41797 z " id="path2848"
            style="fill-rule:evenodd"/>
          </g>
        </g>
      </a>
      <a id="license-osm-link" xlink:href="http://www.openstreetmap.org/">
        <g transform="translate(-210,10)" id="license-osm-text">
          <text class="license-text" dx="0" dy="0">
            Copyright © <xsl:value-of select="$year"/> OpenStreetMap (openstreetmap.org)
          </text>
        </g>
      </a>
      <a id="license-cc-text-link" xlink:href="http://creativecommons.org/licenses/by-sa/2.0/">
        <g transform="translate(-150,18)" id="license-cc-text">
          <text class="license-text" dx="0" dy="0">This work is licensed under the Creative</text>
          <text class="license-text" dx="0" dy="8">Commons Attribution-ShareAlike 2.0 License.</text>
          <text class="license-text" dx="0" dy="16">http://creativecommons.org/licenses/by-sa/2.0/</text>
        </g>
      </a>
    </g>
  </xsl:template>


  <!-- Draw zoom controls -->
  <xsl:template name="zoomControl">
    <defs>

      <style type="text/css">
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
        <feSpecularLighting in="blur" surfaceScale="5" specularConstant=".75" specularExponent="20" lighting-color="white" result="specOut">
          <fePointLight x="-5000" y="-10000" z="7000"/>
        </feSpecularLighting>
        <feComposite in="specOut" in2="SourceAlpha" operator="in" result="specOut"/>
        <feComposite in="SourceGraphic" in2="specOut" operator="arithmetic" k1="0" k2="1" k3="1" k4="0" result="litPaint"/>
        <feMerge>
          <feMergeNode in="offsetBlur"/>
          <feMergeNode in="litPaint"/>
        </feMerge>
      </filter>
      <symbol id="panDown" viewBox="0 0 19 19" class="fancyButton">
        <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z"/>
        <path d="M 9.5,5 L 9.5,14"/>
      </symbol>
      <symbol id="panUp" viewBox="0 0 19 19" class="fancyButton">
        <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z"/>
        <path d="M 9.5,5 L 9.5,14"/>
      </symbol>
      <symbol id="panLeft" viewBox="0 0 19 19" class="fancyButton">
        <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z"/>
        <path d="M 5,9.5 L 14,9.5"/>
      </symbol>
      <symbol id="panRight" viewBox="0 0 19 19" class="fancyButton">
        <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z"/>
        <path d="M 5,9.5 L 14,9.5"/>
      </symbol>
      <symbol id="zoomIn" viewBox="0 0 19 19" class="fancyButton">
        <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z"/>
        <path d="M 5,9.5 L 14,9.5 M 9.5,5 L 9.5,14"/>
      </symbol>
      <symbol id="zoomOut" viewBox="0 0 19 19" class="fancyButton">
        <path d="M 17 9.5 A 7 7 0 1 1 2,9.5 A 7 7 0 1 1 17 9.5 z"/>
        <path d="M 5,9.5 L 14,9.5"/>
      </symbol>

    </defs>

    <g id="gPanDown" filter="url(#fancyButton)" onclick="fnPan('down')">
      <use x="18px" y="60px" xlink:href="#panDown" width="14px" height="14px"/>
    </g>
    <g id="gPanRight" filter="url(#fancyButton)" onclick="fnPan('right')">
      <use x="8px" y="70px" xlink:href="#panRight" width="14px" height="14px"/>
    </g>
    <g id="gPanLeft" filter="url(#fancyButton)" onclick="fnPan('left')">
      <use x="28px" y="70px" xlink:href="#panLeft" width="14px" height="14px"/>
    </g>
    <g id="gPanUp" filter="url(#fancyButton)" onclick="fnPan('up')">
      <use x="18px" y="80px" xlink:href="#panUp" width="14px" height="14px"/>
    </g>

    <xsl:variable name="x1" select="25"/>
    <xsl:variable name="y1" select="105"/>
    <xsl:variable name="x2" select="25"/>
    <xsl:variable name="y2" select="300"/>

    <line style="stroke-width: 10; stroke-linecap: butt; stroke: #8080ff;">
      <xsl:attribute name="x1">
        <xsl:value-of select="$x1"/>
      </xsl:attribute>
      <xsl:attribute name="y1">
        <xsl:value-of select="$y1"/>
      </xsl:attribute>
      <xsl:attribute name="x2">
        <xsl:value-of select="$x2"/>
      </xsl:attribute>
      <xsl:attribute name="y2">
        <xsl:value-of select="$y2"/>
      </xsl:attribute>
    </line>

    <line style="stroke-width: 8; stroke-linecap: butt; stroke: white; stroke-dasharray: 10,1;">
      <xsl:attribute name="x1">
        <xsl:value-of select="$x1"/>
      </xsl:attribute>
      <xsl:attribute name="y1">
        <xsl:value-of select="$y1"/>
      </xsl:attribute>
      <xsl:attribute name="x2">
        <xsl:value-of select="$x2"/>
      </xsl:attribute>
      <xsl:attribute name="y2">
        <xsl:value-of select="$y2"/>
      </xsl:attribute>
    </line>

    <!-- Need to use onmousedown because onclick is interfered with by the onmousedown handler for panning -->
    <g id="gZoomIn" filter="url(#fancyButton)" onmousedown="fnZoom('in')">
      <use x="15.5px" y="100px" xlink:href="#zoomIn" width="19px" height="19px"/>
    </g>

    <!-- Need to use onmousedown because onclick is interfered with by the onmousedown handler for panning -->
    <g id="gZoomOut" filter="url(#fancyButton)" onmousedown="fnZoom('out')">
      <use x="15.5px" y="288px" xlink:href="#zoomOut" width="19px" height="19px"/>
    </g>
  </xsl:template>

  <xsl:template name="javaScript">
    <script>
      /*

      Osmarender

      interactive.js

      */

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
      if (evt.newScale == undefined) throw 'bad interface'
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


    </script>
  </xsl:template>

</xsl:stylesheet>
