<?xml version="1.0" encoding="UTF-8"?>
<!--
==============================================================================

Osmarender

==============================================================================

Copyright (C) 2006-2007  OpenStreetMap Foundation

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
<xsl:stylesheet xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" xmlns:cc="http://web.resource.org/cc/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:date="http://exslt.org/dates-and-times" xmlns:set="http://exslt.org/sets" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" extension-element-prefixes="date set">
 
    <xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="UTF-8"/>

    <xsl:param name="osmfile" select="/rules/@data"/>
    <xsl:param name="title" select="/rules/@title"/>

    <xsl:param name="scale" select="/rules/@scale"/>
    <xsl:param name="withOSMLayers" select="/rules/@withOSMLayers"/>
    <xsl:param name="withUntaggedSegments" select="/rules/@withUntaggedSegments"/>
    <xsl:param name="svgBaseProfile" select="/rules/@svgBaseProfile"/>

    <xsl:param name="showGrid" select="/rules/@showGrid"/>
    <xsl:param name="showBorder" select="/rules/@showBorder"/>
    <xsl:param name="showScale" select="/rules/@showScale"/>
    <xsl:param name="showLicense" select="/rules/@showLicense"/>

    <xsl:key name="nodeById" match="/osm/node" use="@id"/>
    <xsl:key name="segmentById" match="/osm/segment" use="@id"/>
    <xsl:key name="segmentByFromNode" match="/osm/segment" use="@from"/>
    <xsl:key name="segmentByToNode" match="/osm/segment" use="@to"/>
    <xsl:key name="wayBySegment" match="/osm/way" use="seg/@id"/>

    <xsl:variable name="data" select="document($osmfile)"/>

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

    <xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="bllat">
        <xsl:for-each select="$data/osm/node/@lat">
            <xsl:sort data-type="number" order="ascending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="bllon">
        <xsl:for-each select="$data/osm/node/@lon">
            <xsl:sort data-type="number" order="ascending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="trlat">
        <xsl:for-each select="$data/osm/node/@lat">
            <xsl:sort data-type="number" order="descending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="trlon">
        <xsl:for-each select="$data/osm/node/@lon">
            <xsl:sort data-type="number" order="descending"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:variable><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="bottomLeftLatitude">
        <xsl:choose>
            <xsl:when test="/rules/bounds">
                <xsl:value-of select="/rules/bounds/@minlat"/>
            </xsl:when>
            <xsl:when test="$data/osm/bounds">
                <xsl:value-of select="$data/osm/bounds/@request_minlat"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$bllat"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="bottomLeftLongitude">
        <xsl:choose>
            <xsl:when test="/rules/bounds">
                <xsl:value-of select="/rules/bounds/@minlon"/>
            </xsl:when>
            <xsl:when test="$data/osm/bounds">
                <xsl:value-of select="$data/osm/bounds/@request_minlon"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$bllon"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="topRightLatitude">
        <xsl:choose>
            <xsl:when test="/rules/bounds">
                <xsl:value-of select="/rules/bounds/@maxlat"/>
            </xsl:when>
            <xsl:when test="$data/osm/bounds">
                <xsl:value-of select="$data/osm/bounds/@request_maxlat"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$trlat"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="topRightLongitude">
        <xsl:choose>
            <xsl:when test="/rules/bounds">
                <xsl:value-of select="/rules/bounds/@maxlon"/>
            </xsl:when>
            <xsl:when test="$data/osm/bounds">
                <xsl:value-of select="$data/osm/bounds/@request_maxlon"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$trlon"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="middleLatitude" select="($topRightLatitude + $bottomLeftLatitude) div 2.0"/><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="latr" select="$middleLatitude * 3.1415926 div 180.0"/><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="coslat" select="1 - ($latr * $latr) div 2 + ($latr * $latr * $latr * $latr) div 24"/><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="projection" select="1 div $coslat"/><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="dataWidth" select="(number($topRightLongitude)-number($bottomLeftLongitude))*10000*$scale"/><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="dataHeight" select="(number($topRightLatitude)-number($bottomLeftLatitude))*10000*$scale*$projection"/><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="km" select="(0.0089928*$scale*10000*$projection)"/><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="documentWidth">
        <xsl:choose>
            <xsl:when test="$dataWidth &gt; (number(/rules/@minimumMapWidth) * $km)">
                <xsl:value-of select="$dataWidth"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="number(/rules/@minimumMapWidth) * $km"/></xsl:otherwise>
        </xsl:choose>
    </xsl:variable><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="documentHeight">
        <xsl:choose>
            <xsl:when test="$dataHeight &gt; (number(/rules/@minimumMapHeight) * $km)">
                <xsl:value-of select="$dataHeight"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="number(/rules/@minimumMapHeight) * $km"/></xsl:otherwise>
        </xsl:choose>
    </xsl:variable><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="width" select="($documentWidth div 2) + ($dataWidth div 2)"/><xsl:variable xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="height" select="($documentHeight div 2) + ($dataHeight div 2)"/>

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

        <svg id="main" version="1.1" baseProfile="{$svgBaseProfile}" width="{$svgWidth}px" height="{$svgHeight}px" viewBox="{-$extraWidth div 2} {-$extraHeight div 2} {$svgWidth} {$svgHeight}">
            <xsl:if test="/rules/@interactive=&quot;yes&quot;">
                <xsl:attribute name="onscroll">fnOnScroll(evt)</xsl:attribute>
                <xsl:attribute name="onzoom">fnOnZoom(evt)</xsl:attribute>
                <xsl:attribute name="onload">fnOnLoad(evt)</xsl:attribute>
                <xsl:attribute name="onmousedown">fnOnMouseDown(evt)</xsl:attribute>
                <xsl:attribute name="onmousemove">fnOnMouseMove(evt)</xsl:attribute>
                <xsl:attribute name="onmouseup">fnOnMouseUp(evt)</xsl:attribute>
            </xsl:if>

            <xsl:call-template name="metadata"/>

            <!-- Include javaScript functions for all the dynamic stuff --> 
            <xsl:if test="/rules/@interactive=&quot;yes&quot;">
                <xsl:call-template name="javaScript"/>
            </xsl:if>

            <defs id="defs-rulefile">
                <!-- Get any <defs> and styles from the rules file -->
                <xsl:copy-of select="defs/*"/>
            </defs>

            <!-- Pre-generate named path definitions for all ways -->
            <xsl:variable name="allWays" select="$data/osm/way"/>
            <defs id="paths-of-ways">
                <xsl:for-each select="$allWays">
                    <xsl:call-template name="generateWayPath"/>
                </xsl:for-each>
            </defs>

            <!-- Clipping rectangle for map -->
            <clipPath id="map-clipping">
                <rect id="map-clipping-rect" x="0px" y="0px" height="{$documentHeight}px" width="{$documentWidth}px"/>
            </clipPath>

            <g id="map" clip-path="url(#map-clipping)" inkscape:groupmode="layer" inkscape:label="Map" transform="translate(0,{$marginaliaTopHeight})">
                <!-- Draw a nice background layer -->
                <rect id="background" x="0px" y="0px" height="{$documentHeight}px" width="{$documentWidth}px" class="map-background"/>

                <!-- If this is set we first draw all untagged segments not belonging to any way -->
                <xsl:if test="$withUntaggedSegments=&quot;yes&quot;">
                    <xsl:call-template name="drawUntaggedSegments"/>
                </xsl:if>

                <!-- Process all the rules drawing all map features -->
                <xsl:call-template name="processRules"/>
            </g>

            <!-- Draw map decoration -->
            <g id="map-decoration" inkscape:groupmode="layer" inkscape:label="Map decoration" transform="translate(0,{$marginaliaTopHeight})">
                <!-- Draw a grid if required -->
                <xsl:if test="$showGrid=&quot;yes&quot;">
                    <xsl:call-template name="gridDraw"/>
                </xsl:if>

                <!-- Draw a border if required -->
                <xsl:if test="$showBorder=&quot;yes&quot;">
                    <xsl:call-template name="borderDraw"/>
                </xsl:if>
            </g>

            <!-- Draw map marginalia -->
            <xsl:if test="($title != '') or ($showScale = 'yes') or ($showLicense = 'yes')">
                <g id="marginalia" inkscape:groupmode="layer" inkscape:label="Marginalia">
                    <!-- Draw the title -->
                    <xsl:if test="$title!=''">
                        <xsl:call-template name="titleDraw">
                            <xsl:with-param name="title" select="$title"/>
                        </xsl:call-template>
                    </xsl:if>

                    <xsl:if test="($showScale = 'yes') or ($showLicense = 'yes')">
                        <g id="marginalia-bottom" inkscape:groupmode="layer" inkscape:label="Marginalia (Bottom)" transform="translate(0,{$marginaliaTopHeight})">
                            <!-- Draw background for marginalia at bottom -->
                            <rect id="marginalia-background" x="0px" y="{$documentHeight + 5}px" height="40px" width="{$documentWidth}px" class="map-marginalia-background"/>

                            <!-- Draw the scale in the bottom left corner -->
                            <xsl:if test="$showScale=&quot;yes&quot;">
                                <xsl:call-template name="scaleDraw"/>
                            </xsl:if>

                            <!-- Draw Creative commons license -->
                            <xsl:if test="$showLicense=&quot;yes&quot;">
                                <xsl:call-template name="in-image-license">
                                    <xsl:with-param name="year" select="2007"/>
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
                <xsl:if test="/rules/@interactive=&quot;yes&quot;">
                    <xsl:call-template name="zoomControl"/>
                </xsl:if>
            </g>
        </svg>

    </xsl:template>

    <!-- include templates from all the other files -->
    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" name="drawLine">
        <xsl:param name="instruction"/>
        <xsl:param name="segment"/> <!-- The current segment element -->
        <xsl:param name="way"/>  <!-- The current way element if applicable -->

        <xsl:variable name="from" select="@from"/>
        <xsl:variable name="to" select="@to"/>
        <xsl:variable name="fromNode" select="key(&quot;nodeById&quot;,$from)"/>
        <xsl:variable name="toNode" select="key(&quot;nodeById&quot;,$to)"/>
        <xsl:variable name="fromNodeContinuation" select="(count(key(&quot;segmentByFromNode&quot;,$fromNode/@id))+count(key(&quot;segmentByToNode&quot;,$fromNode/@id)))&gt;1"/>
        <xsl:variable name="toNodeContinuation" select="(count(key(&quot;segmentByFromNode&quot;,$toNode/@id))+count(key(&quot;segmentByToNode&quot;,$toNode/@id)))&gt;1"/>

        <xsl:variable name="x1" select="($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)"/>
        <xsl:variable name="y1" select="($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)"/>
        <xsl:variable name="x2" select="($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)"/>
        <xsl:variable name="y2" select="($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)"/>

        <!-- If this is not the end of a path then draw a stub line with a rounded linecap at the from-node end -->
        <xsl:if test="$fromNodeContinuation">
            <xsl:call-template name="drawSegmentFragment">
                <xsl:with-param name="x1" select="$x1"/>
                <xsl:with-param name="y1" select="$y1"/>
                <xsl:with-param name="x2" select="number($x1)+((number($x2)-number($x1)) div 10)"/>
                <xsl:with-param name="y2" select="number($y1)+((number($y2)-number($y1)) div 10)"/>
            </xsl:call-template>
        </xsl:if>

        <!-- If this is not the end of a path then draw a stub line with a rounded linecap at the to-node end -->
        <xsl:if test="$toNodeContinuation">
            <xsl:call-template name="drawSegmentFragment">
                <xsl:with-param name="x1" select="number($x2)-((number($x2)-number($x1)) div 10)"/>
                <xsl:with-param name="y1" select="number($y2)-((number($y2)-number($y1)) div 10)"/>
                <xsl:with-param name="x2" select="$x2"/>
                <xsl:with-param name="y2" select="$y2"/>
            </xsl:call-template>
        </xsl:if>

        <line>
            <xsl:attribute name="x1"><xsl:value-of select="$x1"/></xsl:attribute>
            <xsl:attribute name="y1"><xsl:value-of select="$y1"/></xsl:attribute>
            <xsl:attribute name="x2"><xsl:value-of select="$x2"/></xsl:attribute>
            <xsl:attribute name="y2"><xsl:value-of select="$y2"/></xsl:attribute>
            <xsl:call-template name="getSvgAttributesFromOsmTags"/>
        </line>

    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" name="drawSegmentFragment">
        <xsl:param name="x1"/>
        <xsl:param name="x2"/>
        <xsl:param name="y1"/>
        <xsl:param name="y2"/>
            <line>
                <xsl:attribute name="x1"><xsl:value-of select="$x1"/></xsl:attribute>
                <xsl:attribute name="y1"><xsl:value-of select="$y1"/></xsl:attribute>
                <xsl:attribute name="x2"><xsl:value-of select="$x2"/></xsl:attribute>
                <xsl:attribute name="y2"><xsl:value-of select="$y2"/></xsl:attribute>
                <!-- add the rounded linecap attribute -->
                <xsl:attribute name="stroke-linecap">round</xsl:attribute>
                <!-- suppress any markers else these could be drawn in the wrong place -->
                <xsl:attribute name="marker-start">none</xsl:attribute>
                <xsl:attribute name="marker-end">none</xsl:attribute>
                <xsl:call-template name="getSvgAttributesFromOsmTags"/>
            </line>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" name="drawWay">
        <xsl:param name="instruction"/>
        <xsl:param name="way"/>  <!-- The current way element if applicable -->
        <xsl:param name="layer"/>
        <xsl:param name="classes"/>

        <xsl:variable name="tunnel" select="$way/tag[@k='tunnel']"/>
        <xsl:variable name="railway" select="$way/tag[@k='railway' and @v='rail']"/>

        <xsl:if test="not($tunnel and ($tunnel/@v = 'yes') or ($tunnel/@v = 'true'))">  <!-- if this is not a tunnel -->

            <xsl:if test="not($railway)">

                <!-- For the first and last segments in the way if the start or end is a continuation, then draw a round-capped stub segment
                        that is 1/10th the length of the segment and without any markers.  TODO: do this for all sub-paths within the path.
                    Count the number of segments that link to the from node of this segment.  Only count them if they belong to a way that
                    has a layer tag that is greater than the layer of this way.  If there are any such segments then draw rounded
                    end fragments. -->
                <!-- Process the first segment in the way -->
                <xsl:variable name="firstSegment" select="key(&quot;segmentById&quot;,$way/seg[1]/@id)"/>
                <xsl:variable name="firstSegmentFromNode" select="key(&quot;nodeById&quot;,$firstSegment/@from)"/>
                <xsl:variable name="firstSegmentToNode" select="key(&quot;nodeById&quot;,$firstSegment/@to)"/>
                <xsl:variable name="firstSegmentInboundLayerCount" select="count(key(&quot;wayBySegment&quot;,key(&quot;segmentByToNode&quot;,$firstSegmentFromNode/@id)/@id)/tag[@k=&quot;layer&quot; and @v &gt;= $layer])"/>
                <xsl:variable name="firstSegmentInboundNoLayerCount" select="count(key(&quot;wayBySegment&quot;,key(&quot;segmentByToNode&quot;,$firstSegmentFromNode/@id)/@id)[count(tag[@k=&quot;layer&quot;])=0 and $layer &lt; 1])"/>
                <xsl:variable name="firstSegmentOutboundLayerCount" select="count(key(&quot;wayBySegment&quot;,key(&quot;segmentByFromNode&quot;,$firstSegmentFromNode/@id)/@id)/tag[@k=&quot;layer&quot; and @v &gt;= $layer])"/>
                <xsl:variable name="firstSegmentOutboundNoLayerCount" select="count(key(&quot;wayBySegment&quot;,key(&quot;segmentByFromNode&quot;,$firstSegmentFromNode/@id)/@id)[count(tag[@k=&quot;layer&quot;])=0 and $layer &lt; 1])"/>
                <xsl:variable name="firstSegmentLayerCount" select="($firstSegmentInboundLayerCount+$firstSegmentInboundNoLayerCount+$firstSegmentOutboundLayerCount+$firstSegmentOutboundNoLayerCount)&gt;1"/>

                <xsl:if test="$firstSegmentLayerCount">
                    <xsl:variable name="x1" select="($width)-((($topRightLongitude)-($firstSegmentFromNode/@lon))*10000*$scale)"/>
                    <xsl:variable name="y1" select="($height)+((($bottomLeftLatitude)-($firstSegmentFromNode/@lat))*10000*$scale*$projection)"/>
                    <xsl:variable name="x2" select="($width)-((($topRightLongitude)-($firstSegmentToNode/@lon))*10000*$scale)"/>
                    <xsl:variable name="y2" select="($height)+((($bottomLeftLatitude)-($firstSegmentToNode/@lat))*10000*$scale*$projection)"/>
                    <xsl:call-template name="drawSegmentFragment">
                        <xsl:with-param name="x1" select="$x1"/>
                        <xsl:with-param name="y1" select="$y1"/>
                        <xsl:with-param name="x2" select="number($x1)+((number($x2)-number($x1)) div 10)"/>
                        <xsl:with-param name="y2" select="number($y1)+((number($y2)-number($y1)) div 10)"/>
                    </xsl:call-template>
                </xsl:if>

                <!-- Process the last segment in the way -->
                <xsl:variable name="lastSegment" select="key(&quot;segmentById&quot;,$way/seg[last()]/@id)"/>
                <xsl:variable name="lastSegmentFromNode" select="key(&quot;nodeById&quot;,$lastSegment/@from)"/>
                <xsl:variable name="lastSegmentToNode" select="key(&quot;nodeById&quot;,$lastSegment/@to)"/>
                <xsl:variable name="lastSegmentToNodeLayer" select="(count(key(&quot;segmentByFromNode&quot;,$lastSegmentToNode/@id)[@k=&quot;layer&quot; and @v &gt; $layer])+count(key(&quot;segmentByToNode&quot;,$lastSegmentToNode/@id)[@k=&quot;layer&quot; and @v &gt; $layer]))&gt;0"/>
                <xsl:variable name="lastSegmentInboundLayerCount" select="count(key(&quot;wayBySegment&quot;,key(&quot;segmentByToNode&quot;,$lastSegmentToNode/@id)/@id)/tag[@k=&quot;layer&quot; and @v &gt;= $layer])"/>
                <xsl:variable name="lastSegmentInboundNoLayerCount" select="count(key(&quot;wayBySegment&quot;,key(&quot;segmentByToNode&quot;,$lastSegmentToNode/@id)/@id)[count(tag[@k=&quot;layer&quot;])=0 and $layer &lt; 1])"/>
                <xsl:variable name="lastSegmentOutboundLayerCount" select="count(key(&quot;wayBySegment&quot;,key(&quot;segmentByFromNode&quot;,$lastSegmentToNode/@id)/@id)/tag[@k=&quot;layer&quot; and @v &gt;= $layer])"/>
                <xsl:variable name="lastSegmentOutboundNoLayerCount" select="count(key(&quot;wayBySegment&quot;,key(&quot;segmentByFromNode&quot;,$lastSegmentToNode/@id)/@id)[count(tag[@k=&quot;layer&quot;])=0 and $layer &lt; 1])"/>
                <xsl:variable name="lastSegmentLayerCount" select="($lastSegmentInboundLayerCount+$lastSegmentInboundNoLayerCount+$lastSegmentOutboundLayerCount+$lastSegmentOutboundNoLayerCount)&gt;1"/>

                <xsl:if test="$lastSegmentLayerCount">
                    <xsl:variable name="x1" select="($width)-((($topRightLongitude)-($lastSegmentFromNode/@lon))*10000*$scale)"/>
                    <xsl:variable name="y1" select="($height)+((($bottomLeftLatitude)-($lastSegmentFromNode/@lat))*10000*$scale*$projection)"/>
                    <xsl:variable name="x2" select="($width)-((($topRightLongitude)-($lastSegmentToNode/@lon))*10000*$scale)"/>
                    <xsl:variable name="y2" select="($height)+((($bottomLeftLatitude)-($lastSegmentToNode/@lat))*10000*$scale*$projection)"/>
                    <xsl:call-template name="drawSegmentFragment">
                        <xsl:with-param name="x1" select="number($x2)-((number($x2)-number($x1)) div 10)"/>
                        <xsl:with-param name="y1" select="number($y2)-((number($y2)-number($y1)) div 10)"/>
                        <xsl:with-param name="x2" select="$x2"/>
                        <xsl:with-param name="y2" select="$y2"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:if>

            <!-- Now draw the way itself -->
            <use xlink:href="#way_{$way/@id}">
                <xsl:apply-templates select="$instruction/@*" mode="copyAttributes">
                    <xsl:with-param name="classes" select="$classes"/>
                </xsl:apply-templates>
            </use>
        </xsl:if>

    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" name="drawTunnel">
        <xsl:param name="instruction"/>
        <xsl:param name="way"/>
        <xsl:param name="layer"/>
        <xsl:param name="classes"/>

        <xsl:choose>
            <xsl:when test="$instruction/@width &gt; 0">
                <!-- wide tunnels use a dashed line as wide as the road casing with a mask as wide as the road core which will be
                rendered as a double dotted line -->
                <mask id="mask_{@id}" maskUnits="userSpaceOnUse">
                    <use xlink:href="#way_{@id}" style="stroke:black;fill:none;" class="{$instruction/@class}-core"/>
                    <rect x="0px" y="0px" height="{$documentHeight}px" width="{$documentWidth}px" style="fill:white;"/>
                </mask>
                <use xlink:href="#way_{$way/@id}" mask="url(#mask_{@id})" style="stroke-dasharray:0.2,0.2;" class="{$instruction/@class}-casing"/>
                <use xlink:href="#way_{$way/@id}" class="tunnel-casing" style="stroke-width:{$instruction/@width}px;"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- narrow tunnels will use a single dotted line -->
                <use xlink:href="#way_{$way/@id}">
                    <xsl:apply-templates select="$instruction/@*" mode="copyAttributes">
                        <xsl:with-param name="classes" select="$classes"/>
                    </xsl:apply-templates>
                </use>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" name="drawCircle">
        <xsl:param name="instruction"/>

        <xsl:variable name="x" select="($width)-((($topRightLongitude)-(@lon))*10000*$scale)"/>
        <xsl:variable name="y" select="($height)+((($bottomLeftLatitude)-(@lat))*10000*$scale*$projection)"/>

        <circle cx="{$x}" cy="{$y}">
            <xsl:apply-templates select="$instruction/@*" mode="copyAttributes"/> <!-- Copy all the svg attributes from the <circle> instruction -->
        </circle>

    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" name="drawSymbol">
        <xsl:param name="instruction"/>

        <xsl:variable name="x" select="($width)-((($topRightLongitude)-(@lon))*10000*$scale)"/>
        <xsl:variable name="y" select="($height)+((($bottomLeftLatitude)-(@lat))*10000*$scale*$projection)"/>

        <use x="{$x}" y="{$y}">
            <xsl:apply-templates select="$instruction/@*" mode="copyAttributes"/> <!-- Copy all the attributes from the <symbol> instruction -->
        </use>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" name="renderText">
        <xsl:param name="instruction"/>

        <xsl:variable name="x" select="($width)-((($topRightLongitude)-(@lon))*10000*$scale)"/>
        <xsl:variable name="y" select="($height)+((($bottomLeftLatitude)-(@lat))*10000*$scale*$projection)"/>

        <text>
            <xsl:apply-templates select="$instruction/@*" mode="copyAttributes"/>
            <xsl:attribute name="x"><xsl:value-of select="$x"/></xsl:attribute>
            <xsl:attribute name="y"><xsl:value-of select="$y"/></xsl:attribute>
            <xsl:call-template name="getSvgAttributesFromOsmTags"/>
            <xsl:value-of select="tag[@k=$instruction/@k]/@v"/>
        </text>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" name="renderTextPath">
        <xsl:param name="instruction"/>
        <xsl:param name="pathId"/>
        <text>
            <xsl:apply-templates select="$instruction/@*" mode="renderTextPath-text"/>
            <textPath xlink:href="#{$pathId}">
                <xsl:apply-templates select="$instruction/@*" mode="renderTextPath-textPath"/>
                <xsl:call-template name="getSvgAttributesFromOsmTags"/>
                <xsl:value-of select="tag[@k=$instruction/@k]/@v"/>
            </textPath>
        </text>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="@startOffset|@method|@spacing|@lengthAdjust|@textLength|@k" mode="renderTextPath-text">
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="@*" mode="renderTextPath-text">
        <xsl:copy/>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="@startOffset|@method|@spacing|@lengthAdjust|@textLength" mode="renderTextPath-textPath">
        <xsl:copy/>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="@*" mode="renderTextPath-textPath">
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" name="renderArea">
        <xsl:param name="instruction"/>
        <xsl:param name="pathId"/>

        <use xlink:href="#{$pathId}">
            <xsl:apply-templates select="$instruction/@*" mode="copyAttributes"/>
        </use>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="getSvgAttributesFromOsmTags">
        <xsl:for-each select="tag[contains(@k,&quot;svg:&quot;)]">
            <xsl:attribute name="{substring-after(@k,&quot;svg:&quot;)}"><xsl:value-of select="@v"/></xsl:attribute>
        </xsl:for-each>
    </xsl:template>
    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" name="drawUntaggedSegments">
        <g id="segments" inkscape:groupmode="layer" inkscape:label="Segments">
            <xsl:for-each select="$data/osm/segment[not(key('wayBySegment', @id))]">
                <xsl:if test="not(tag[@key!='created_by'])">
                    <xsl:variable name="fromNode" select="key('nodeById', @from)"/>
                    <xsl:variable name="toNode" select="key('nodeById', @to)"/>
                    <xsl:variable name="x1" select="($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)"/>
                    <xsl:variable name="y1" select="($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)"/>
                    <xsl:variable name="x2" select="($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)"/>
                    <xsl:variable name="y2" select="($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)"/>
                    <line class="untagged-segments" x1="{$x1}" y1="{$y1}" x2="{$x2}" y2="{$y2}"/>
                </xsl:if>
            </xsl:for-each>
        </g>
    </xsl:template>
    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" match="line">
        <xsl:param name="elements"/>
        <xsl:param name="layer"/>
        <xsl:param name="classes"/>

        <!-- This is the instruction that is currently being processed -->
        <xsl:variable name="instruction" select="."/>

        <g>
            <xsl:apply-templates select="@*" mode="copyAttributes"> <!-- Add all the svg attributes of the <line> instruction to the <g> element -->
                <xsl:with-param name="classes" select="$classes"/>
            </xsl:apply-templates>

            <!-- For each segment and way -->
            <xsl:apply-templates select="$elements" mode="line">
                <xsl:with-param name="instruction" select="$instruction"/>
                <xsl:with-param name="layer" select="$layer"/>
                <xsl:with-param name="classes" select="$classes"/>
            </xsl:apply-templates>

        </g>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="*" mode="line"/><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="segment" mode="line">
        <xsl:param name="instruction"/>
        <xsl:param name="classes"/>

        <xsl:call-template name="drawLine">
            <xsl:with-param name="instruction" select="$instruction"/>
            <xsl:with-param name="segment" select="."/>
            <xsl:with-param name="classes" select="$classes"/>
        </xsl:call-template>

    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="way" mode="line">
        <xsl:param name="instruction"/>
        <xsl:param name="layer"/>
        <xsl:param name="classes"/>

        <!-- The current <way> element -->
        <xsl:variable name="way" select="."/>

        <xsl:call-template name="drawWay">
            <xsl:with-param name="instruction" select="$instruction"/>
            <xsl:with-param name="way" select="$way"/>
            <xsl:with-param name="layer" select="$layer"/>
            <xsl:with-param name="classes" select="$classes"/>
        </xsl:call-template>

    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" match="tunnel">
        <xsl:param name="elements"/>
        <xsl:param name="layer"/>
        <xsl:param name="classes"/>

        <!-- This is the instruction that is currently being processed -->
        <xsl:variable name="instruction" select="."/>

        <g>
            <xsl:apply-templates select="@*" mode="copyAttributes"> <!-- Add all the svg attributes of the <tunnel> instruction to the <g> element -->
                <xsl:with-param name="classes" select="$classes"/>
            </xsl:apply-templates>

            <!-- For each segment and way -->
            <xsl:apply-templates select="$elements" mode="tunnel">
                <xsl:with-param name="instruction" select="$instruction"/>
                <xsl:with-param name="layer" select="$layer"/>
                <xsl:with-param name="classes" select="$classes"/>
            </xsl:apply-templates>
        </g>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="way" mode="tunnel">
        <xsl:param name="instruction"/>
        <xsl:param name="layer"/>
        <xsl:param name="classes"/>

        <!-- The current <way> element -->
        <xsl:variable name="way" select="."/>

        <xsl:call-template name="drawTunnel">
            <xsl:with-param name="instruction" select="$instruction"/>
            <xsl:with-param name="way" select="$way"/>
            <xsl:with-param name="layer" select="$layer"/>
            <xsl:with-param name="classes" select="$classes"/>
        </xsl:call-template>

    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" match="area">
        <xsl:param name="elements"/>
        <xsl:param name="classes"/>

        <!-- This is the instruction that is currently being processed -->
        <xsl:variable name="instruction" select="."/>

        <g>
            <xsl:apply-templates select="@*" mode="copyAttributes"/> <!-- Add all the svg attributes of the <line> instruction to the <g> element -->

            <!-- For each segment and way -->
            <xsl:apply-templates select="$elements" mode="area">
                <xsl:with-param name="instruction" select="$instruction"/>
            </xsl:apply-templates>
        </g>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="*" mode="area"/><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="way" mode="area">
        <xsl:param name="instruction"/>

        <xsl:call-template name="generateAreaPath"/>

        <xsl:call-template name="renderArea">
            <xsl:with-param name="instruction" select="$instruction"/>
            <xsl:with-param name="pathId" select="concat(&quot;area_&quot;,@id)"/>
        </xsl:call-template>

    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="circle">
        <xsl:param name="elements"/>
        <xsl:param name="classes"/>

        <!-- This is the instruction that is currently being processed -->
        <xsl:variable name="instruction" select="."/>

        <xsl:for-each select="$elements[name()=&quot;node&quot;]">
            <xsl:call-template name="drawCircle">
                <xsl:with-param name="instruction" select="$instruction"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="symbol">
        <xsl:param name="elements"/>
        <xsl:param name="classes"/>

        <!-- This is the instruction that is currently being processed -->
        <xsl:variable name="instruction" select="."/>

        <xsl:for-each select="$elements[name()=&quot;node&quot;]">
            <xsl:call-template name="drawSymbol">
                <xsl:with-param name="instruction" select="$instruction"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="text">
        <xsl:param name="elements"/>
        <xsl:param name="classes"/>

        <!-- This is the instruction that is currently being processed -->
        <xsl:variable name="instruction" select="."/>

        <!-- Select all <node> elements that have a key that matches the k attribute of the text instruction -->
        <xsl:for-each select="$elements[name()=&quot;node&quot;][tag[@k=$instruction/@k]]">
                <xsl:call-template name="renderText">
                    <xsl:with-param name="instruction" select="$instruction"/>
                </xsl:call-template>
        </xsl:for-each>

        <!-- Select all <segment> and <way> elements that have a key that matches the k attribute of the text instruction -->
        <xsl:apply-templates select="$elements[name()=&quot;segment&quot; or name()=&quot;way&quot;][tag[@k=$instruction/@k]]" mode="textPath">
            <xsl:with-param name="instruction" select="$instruction"/>
        </xsl:apply-templates>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="*" mode="textPath"/><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="segment" mode="textPath">
        <xsl:param name="instruction"/>

        <!-- The current <segment> element -->
        <xsl:variable name="segment" select="."/>

        <!-- Generate the path for the segment -->
        <!-- Text on segments should be relatively uncommon so only generate a <path> when one is needed -->
        <xsl:call-template name="generateSegmentPath"/>

        <xsl:call-template name="renderTextPath">
            <xsl:with-param name="instruction" select="$instruction"/>
            <xsl:with-param name="pathId" select="concat(&quot;segment_&quot;,@id)"/>
        </xsl:call-template>

    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="way" mode="textPath">
        <xsl:param name="instruction"/>

        <!-- The current <way> element -->
        <xsl:variable name="way" select="."/>

        <xsl:call-template name="renderTextPath">
            <xsl:with-param name="instruction" select="$instruction"/>
            <xsl:with-param name="pathId" select="concat(&quot;way_&quot;,@id,&quot;t&quot;)"/>
        </xsl:call-template>

    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" name="generateSegmentPath">
        <xsl:variable name='pathData'>
            <xsl:choose>
				<!-- Manual override -->
                <xsl:when test='tag[@k="name_direction"]/@v="-1" or tag[@k="osmarender:nameDirection"]/@v="-1"'>
                    <xsl:call-template name='segmentMoveToEnd'/>
                    <xsl:call-template name='segmentLineToStart'/>
                </xsl:when>
                <xsl:when test='tag[@k="name_direction"]/@v="1" or tag[@k="osmarender:nameDirection"]/@v="1"'>
                    <xsl:call-template name='segmentMoveToStart'/>
                    <xsl:call-template name='segmentLineToEnd'/>
                </xsl:when>
                <!-- Automatic direction -->
                <xsl:when test='(key("nodeById",@from)/@lon &gt; key("nodeById",@to)/@lon)'>
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

    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" name="generateWayPath">

        <!-- Generate the path for the way that will be used by the street
        name rendering. This is horribly inefficient, because we will later
        also have the path used for the rendering of the path itself. So
        each path is twice in the SVG file. But this path here needs to
        have the right direction for the names to render right way up
        and the other path needs to be the right direction for rendering
        the oneway arrows. This can probably be done better, but currently
        I don't know how. -->
        <xsl:variable name='pathData'>
            <xsl:choose>
				<!-- Manual override, reverse direction -->
                <xsl:when test='tag[@k="name_direction"]/@v="-1" or tag[@k="osmarender:nameDirection"]/@v="-1"'>
					<xsl:call-template name='generateWayPathReverse'/>
                </xsl:when>
				<!-- Manual override, normal direction -->
                <xsl:when test='tag[@k="name_direction"]/@v="1" or tag[@k="osmarender:nameDirection"]/@v="1"'>
					<xsl:call-template name='generateWayPathNormal'/>
                </xsl:when>
				<!-- Automatic, reverse direction -->
                <xsl:when test='(key("nodeById",key("segmentById",seg[1]/@id)/@from)/@lon &gt; key("nodeById",key("segmentById",seg[last()]/@id)/@to)/@lon)'>
					<xsl:call-template name='generateWayPathReverse'/>
                </xsl:when>
				<!-- Automatic, normal direction -->
                <xsl:otherwise>
					<xsl:call-template name='generateWayPathNormal'/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <path id="way_{@id}t" d="{$pathData}"/>

        <!-- Generate the path for the way itself. Used for rendering the
        way and, possibly, oneway arrows. -->
        <xsl:variable name="pathDataFixed">
			<xsl:call-template name='generateWayPathNormal'/>
        </xsl:variable>

        <path id="way_{@id}" d="{$pathDataFixed}"/>

    </xsl:template>
    
    
    <!-- Generate a way path in the normal order of the segments in the way -->
    <xsl:template name="generateWayPathNormal">
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
    </xsl:template>


    <!-- Generate a way path in the reverse order of the segments in the way -->
    <xsl:template name="generateWayPathReverse">
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
    </xsl:template>
    
    
    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" name="generateAreaPath">

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

	</xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="segmentMoveToStart">
        <xsl:variable name="from" select="@from"/>
        <xsl:variable name="fromNode" select="key(&quot;nodeById&quot;,$from)"/>

        <xsl:variable name="x1" select="($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)"/>
        <xsl:variable name="y1" select="($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)"/>
        <xsl:text>M</xsl:text>
        <xsl:value-of select="$x1"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$y1"/>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="segmentLineToStart">
        <xsl:variable name="from" select="@from"/>
        <xsl:variable name="fromNode" select="key(&quot;nodeById&quot;,$from)"/>

        <xsl:variable name="x1" select="($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)"/>
        <xsl:variable name="y1" select="($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)"/>
        <xsl:text>L</xsl:text>
        <xsl:value-of select="$x1"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$y1"/>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="segmentMoveToEnd">
        <xsl:variable name="to" select="@to"/>
        <xsl:variable name="toNode" select="key(&quot;nodeById&quot;,$to)"/>

        <xsl:variable name="x2" select="($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)"/>
        <xsl:variable name="y2" select="($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)"/>
        <xsl:text>M</xsl:text>
        <xsl:value-of select="$x2"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$y2"/>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="segmentLineToEnd">
        <xsl:variable name="to" select="@to"/>
        <xsl:variable name="toNode" select="key(&quot;nodeById&quot;,$to)"/>

        <xsl:variable name="x2" select="($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)"/>
        <xsl:variable name="y2" select="($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)"/>
        <xsl:text>L</xsl:text>
        <xsl:value-of select="$x2"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$y2"/>
    </xsl:template>
    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="@class" mode="copyAttributes">
        <xsl:param name="classes"/>
        <xsl:attribute name="class">
            <xsl:value-of select="normalize-space(concat($classes,' ',.))"/>
        </xsl:attribute>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="@type" mode="copyAttributes">
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="@*" mode="copyAttributes">
        <xsl:param name="classes"/>
        <xsl:copy/>
    </xsl:template>

    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="processRules">
      
        <xsl:choose>

            <!-- Process all the rules, one layer at a time -->
            <xsl:when test="$withOSMLayers='yes'">
                <xsl:call-template name="processLayer"><xsl:with-param name="layer" select="&quot;-5&quot;"/></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name="layer" select="&quot;-4&quot;"/></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name="layer" select="&quot;-3&quot;"/></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name="layer" select="&quot;-2&quot;"/></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name="layer" select="&quot;-1&quot;"/></xsl:call-template>
                <xsl:call-template name="processLayer">
                    <xsl:with-param name="layer" select="&quot;0&quot;"/>
                    <xsl:with-param name="elements" select="$data/osm/*[not(@action=&quot;delete&quot;) and count(tag[@k=&quot;layer&quot;])=0 or tag[@k=&quot;layer&quot; and @v=&quot;0&quot;]]"/>
                </xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name="layer" select="&quot;1&quot;"/></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name="layer" select="&quot;2&quot;"/></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name="layer" select="&quot;3&quot;"/></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name="layer" select="&quot;4&quot;"/></xsl:call-template>
                <xsl:call-template name="processLayer"><xsl:with-param name="layer" select="&quot;5&quot;"/></xsl:call-template>
            </xsl:when>

            <!-- Process all the rules, without looking at the layers -->
            <xsl:otherwise>
                <xsl:apply-templates select="/rules/rule">
                    <xsl:with-param name="elements" select="$data/osm/*[not(@action=&quot;delete&quot;)]"/>
                    <xsl:with-param name="layer" select="&quot;0&quot;"/>
                    <xsl:with-param name="classes" select="''"/>
                </xsl:apply-templates>
            </xsl:otherwise>

        </xsl:choose>
	</xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" name="processLayer">
        <xsl:param name="layer"/>
        <xsl:param name="elements" select="$data/osm/*[not(@action=&quot;delete&quot;) and tag[@k=&quot;layer&quot; and @v=$layer]]"/>

        <g inkscape:groupmode="layer" id="layer{$layer}" inkscape:label="Layer {$layer}">
            <xsl:apply-templates select="/rules/rule">
                <xsl:with-param name="elements" select="$elements"/>
                <xsl:with-param name="layer" select="$layer"/>
                <xsl:with-param name="classes" select="''"/>
            </xsl:apply-templates>
        </g>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="rule">
        <xsl:param name="elements"/>
        <xsl:param name="layer"/>
        <xsl:param name="classes"/>

        <!-- This is the rule currently being processed -->
        <xsl:variable name="rule" select="."/>

        <!-- Make list of elements that this rule should be applied to -->
        <xsl:variable name="eBare">
            <xsl:choose>
                <xsl:when test="$rule/@e=&quot;*&quot;">node|segment|way</xsl:when>
                <xsl:when test="$rule/@e"><xsl:value-of select="$rule/@e"/></xsl:when>
                <xsl:otherwise>node|segment|way</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- List of keys that this rule should be applied to -->
        <xsl:variable name="kBare" select="$rule/@k"/>

        <!-- List of values that this rule should be applied to -->
        <xsl:variable name="vBare" select="$rule/@v"/>

        <!-- Top'n'tail selectors with | for contains usage -->
        <xsl:variable name="e">|<xsl:value-of select="$eBare"/>|</xsl:variable>
        <xsl:variable name="k">|<xsl:value-of select="$kBare"/>|</xsl:variable>
        <xsl:variable name="v">|<xsl:value-of select="$vBare"/>|</xsl:variable>

        <xsl:variable name="selectedElements" select="$elements[contains($e,concat(&quot;|&quot;,name(),&quot;|&quot;))or (contains($e,&quot;|waysegment|&quot;) and name()=&quot;segment&quot; and key(&quot;wayBySegment&quot;,@id))]"/>

        <xsl:choose>
            <xsl:when test="contains($k,&quot;|*|&quot;)">
                <xsl:choose>
                    <xsl:when test="contains($v,&quot;|~|&quot;)">
                        <xsl:variable name="elementsWithNoTags" select="$selectedElements[count(tag)=0]"/>
                        <xsl:call-template name="processElements">
                            <xsl:with-param name="eBare" select="$eBare"/>
                            <xsl:with-param name="kBare" select="$kBare"/>
                            <xsl:with-param name="vBare" select="$vBare"/>
                            <xsl:with-param name="layer" select="$layer"/>
                            <xsl:with-param name="elements" select="$elementsWithNoTags"/>
                            <xsl:with-param name="rule" select="$rule"/>
                            <xsl:with-param name="classes" select="$classes"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="contains($v,&quot;|*|&quot;)">
                        <xsl:variable name="allElements" select="$selectedElements"/>
                        <xsl:call-template name="processElements">
                            <xsl:with-param name="eBare" select="$eBare"/>
                            <xsl:with-param name="kBare" select="$kBare"/>
                            <xsl:with-param name="vBare" select="$vBare"/>
                            <xsl:with-param name="layer" select="$layer"/>
                            <xsl:with-param name="elements" select="$allElements"/>
                            <xsl:with-param name="rule" select="$rule"/>
                            <xsl:with-param name="classes" select="$classes"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="allElementsWithValue" select="$selectedElements[tag[contains($v,concat(&quot;|&quot;,@v,&quot;|&quot;))]]"/>
                        <xsl:call-template name="processElements">
                            <xsl:with-param name="eBare" select="$eBare"/>
                            <xsl:with-param name="kBare" select="$kBare"/>
                            <xsl:with-param name="vBare" select="$vBare"/>
                            <xsl:with-param name="layer" select="$layer"/>
                            <xsl:with-param name="elements" select="$allElementsWithValue"/>
                            <xsl:with-param name="rule" select="$rule"/>
                            <xsl:with-param name="classes" select="$classes"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="contains($v,&quot;|~|&quot;)">
                <xsl:variable name="elementsWithoutKey" select="$selectedElements[count(tag[contains($k,concat(&quot;|&quot;,@k,&quot;|&quot;))])=0]"/>
                <xsl:call-template name="processElements">
                    <xsl:with-param name="eBare" select="$eBare"/>
                    <xsl:with-param name="kBare" select="$kBare"/>
                    <xsl:with-param name="vBare" select="$vBare"/>
                    <xsl:with-param name="layer" select="$layer"/>
                    <xsl:with-param name="elements" select="$elementsWithoutKey"/>
                    <xsl:with-param name="rule" select="$rule"/>
                    <xsl:with-param name="classes" select="$classes"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="contains($v,&quot;|*|&quot;)">
                <xsl:variable name="allElementsWithKey" select="$selectedElements[tag[contains($k,concat(&quot;|&quot;,@k,&quot;|&quot;))]]"/>
                <xsl:call-template name="processElements">
                    <xsl:with-param name="eBare" select="$eBare"/>
                    <xsl:with-param name="kBare" select="$kBare"/>
                    <xsl:with-param name="vBare" select="$vBare"/>
                    <xsl:with-param name="layer" select="$layer"/>
                    <xsl:with-param name="elements" select="$allElementsWithKey"/>
                    <xsl:with-param name="rule" select="$rule"/>
                    <xsl:with-param name="classes" select="$classes"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="elementsWithKey" select="$selectedElements[tag[contains($k,concat(&quot;|&quot;,@k,&quot;|&quot;)) and contains($v,concat(&quot;|&quot;,@v,&quot;|&quot;))]]"/>
                <xsl:call-template name="processElements">
                    <xsl:with-param name="eBare" select="$eBare"/>
                    <xsl:with-param name="kBare" select="$kBare"/>
                    <xsl:with-param name="vBare" select="$vBare"/>
                    <xsl:with-param name="layer" select="$layer"/>
                    <xsl:with-param name="elements" select="$elementsWithKey"/>
                    <xsl:with-param name="rule" select="$rule"/>
                    <xsl:with-param name="classes" select="$classes"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="processElements">
        <xsl:param name="eBare"/>
        <xsl:param name="kBare"/>
        <xsl:param name="vBare"/>
        <xsl:param name="layer"/>
        <xsl:param name="elements"/>
        <xsl:param name="rule"/>
        <xsl:param name="classes"/>
        
        <xsl:if test="$elements">
            <xsl:message>
Processing &lt;rule e="<xsl:value-of select="$eBare"/>" k="<xsl:value-of select="$kBare"/>" v="<xsl:value-of select="$vBare"/>" &gt; 
Matched by <xsl:value-of select="count($elements)"/> elements for layer <xsl:value-of select="$layer"/>.
            </xsl:message>

            <xsl:apply-templates select="*">
                <xsl:with-param name="layer" select="$layer"/>
                <xsl:with-param name="elements" select="$elements"/>
                <xsl:with-param name="rule" select="$rule"/>
                <xsl:with-param name="classes" select="$classes"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" match="addclass">
        <xsl:param name="elements"/>
        <xsl:param name="layer"/>
        <xsl:param name="classes"/>

        <!-- This is the rule currently being processed -->
        <xsl:variable name="rule" select="."/>

        <!-- Additional classes from class attribute of this rule -->
        <xsl:variable name="addclasses" select="@class"/>

        <!-- Make list of elements that this rule should be applied to -->
        <xsl:variable name="eBare">
            <xsl:choose>
                <xsl:when test="$rule/@e=&quot;*&quot;">node|segment|way</xsl:when>
                <xsl:when test="$rule/@e"><xsl:value-of select="$rule/@e"/></xsl:when>
                <xsl:otherwise>node|segment|way</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- List of keys that this rule should be applied to -->
        <xsl:variable name="kBare" select="$rule/@k"/>

        <!-- List of values that this rule should be applied to -->
        <xsl:variable name="vBare" select="$rule/@v"/>

        <!-- Top'n'tail selectors with | for contains usage -->
        <xsl:variable name="e">|<xsl:value-of select="$eBare"/>|</xsl:variable>
        <xsl:variable name="k">|<xsl:value-of select="$kBare"/>|</xsl:variable>
        <xsl:variable name="v">|<xsl:value-of select="$vBare"/>|</xsl:variable>

        <xsl:variable name="selectedElements" select="$elements[contains($e,concat(&quot;|&quot;,name(),&quot;|&quot;))or (contains($e,&quot;|waysegment|&quot;) and name()=&quot;segment&quot; and key(&quot;wayBySegment&quot;,@id))]"/>

        <xsl:choose>
            <xsl:when test="contains($k,&quot;|*|&quot;)">
                <xsl:choose>
                    <xsl:when test="contains($v,&quot;|~|&quot;)">
                        <xsl:variable name="elementsWithNoTags" select="$selectedElements[count(tag)=0]"/>
                        <xsl:call-template name="processElementsForAddClass">
                            <xsl:with-param name="eBare" select="$eBare"/>
                            <xsl:with-param name="kBare" select="$kBare"/>
                            <xsl:with-param name="vBare" select="$vBare"/>
                            <xsl:with-param name="layer" select="$layer"/>
                            <xsl:with-param name="elements" select="$elementsWithNoTags"/>
                            <xsl:with-param name="rule" select="$rule"/>
                            <xsl:with-param name="classes" select="$classes"/>
                            <xsl:with-param name="addclasses" select="$addclasses"/>
                            <xsl:with-param name="allelements" select="$elements"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="contains($v,&quot;|*|&quot;)">
                        <xsl:variable name="allElements" select="$selectedElements"/>
                        <xsl:call-template name="processElementsForAddClass">
                            <xsl:with-param name="eBare" select="$eBare"/>
                            <xsl:with-param name="kBare" select="$kBare"/>
                            <xsl:with-param name="vBare" select="$vBare"/>
                            <xsl:with-param name="layer" select="$layer"/>
                            <xsl:with-param name="elements" select="$allElements"/>
                            <xsl:with-param name="rule" select="$rule"/>
                            <xsl:with-param name="classes" select="$classes"/>
                            <xsl:with-param name="addclasses" select="$addclasses"/>
                            <xsl:with-param name="allelements" select="$elements"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="allElementsWithValue" select="$selectedElements[tag[contains($v,concat(&quot;|&quot;,@v,&quot;|&quot;))]]"/>
                        <xsl:call-template name="processElementsForAddClass">
                            <xsl:with-param name="eBare" select="$eBare"/>
                            <xsl:with-param name="kBare" select="$kBare"/>
                            <xsl:with-param name="vBare" select="$vBare"/>
                            <xsl:with-param name="layer" select="$layer"/>
                            <xsl:with-param name="elements" select="$allElementsWithValue"/>
                            <xsl:with-param name="rule" select="$rule"/>
                            <xsl:with-param name="classes" select="$classes"/>
                            <xsl:with-param name="addclasses" select="$addclasses"/>
                            <xsl:with-param name="allelements" select="$elements"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="contains($v,&quot;|~|&quot;)">
                <xsl:variable name="elementsWithoutKey" select="$selectedElements[count(tag[contains($k,concat(&quot;|&quot;,@k,&quot;|&quot;))])=0]"/>
                <xsl:call-template name="processElementsForAddClass">
                    <xsl:with-param name="eBare" select="$eBare"/>
                    <xsl:with-param name="kBare" select="$kBare"/>
                    <xsl:with-param name="vBare" select="$vBare"/>
                    <xsl:with-param name="layer" select="$layer"/>
                    <xsl:with-param name="elements" select="$elementsWithoutKey"/>
                    <xsl:with-param name="rule" select="$rule"/>
                    <xsl:with-param name="classes" select="$classes"/>
                    <xsl:with-param name="addclasses" select="$addclasses"/>
                    <xsl:with-param name="allelements" select="$elements"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="contains($v,&quot;|*|&quot;)">
                <xsl:variable name="allElementsWithKey" select="$selectedElements[tag[contains($k,concat(&quot;|&quot;,@k,&quot;|&quot;))]]"/>
                <xsl:call-template name="processElementsForAddClass">
                    <xsl:with-param name="eBare" select="$eBare"/>
                    <xsl:with-param name="kBare" select="$kBare"/>
                    <xsl:with-param name="vBare" select="$vBare"/>
                    <xsl:with-param name="layer" select="$layer"/>
                    <xsl:with-param name="elements" select="$allElementsWithKey"/>
                    <xsl:with-param name="rule" select="$rule"/>
                    <xsl:with-param name="classes" select="$classes"/>
                    <xsl:with-param name="addclasses" select="$addclasses"/>
                    <xsl:with-param name="allelements" select="$elements"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="elementsWithKey" select="$selectedElements[tag[contains($k,concat(&quot;|&quot;,@k,&quot;|&quot;)) and contains($v,concat(&quot;|&quot;,@v,&quot;|&quot;))]]"/>
                <xsl:call-template name="processElementsForAddClass">
                    <xsl:with-param name="eBare" select="$eBare"/>
                    <xsl:with-param name="kBare" select="$kBare"/>
                    <xsl:with-param name="vBare" select="$vBare"/>
                    <xsl:with-param name="layer" select="$layer"/>
                    <xsl:with-param name="elements" select="$elementsWithKey"/>
                    <xsl:with-param name="rule" select="$rule"/>
                    <xsl:with-param name="classes" select="$classes"/>
                    <xsl:with-param name="addclasses" select="$addclasses"/>
                    <xsl:with-param name="allelements" select="$elements"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="processElementsForAddClass">
        <xsl:param name="eBare"/>
        <xsl:param name="kBare"/>
        <xsl:param name="vBare"/>
        <xsl:param name="layer"/>
        <xsl:param name="elements"/>
        <xsl:param name="allelements"/>
        <xsl:param name="rule"/>
        <xsl:param name="classes"/>
        <xsl:param name="addclasses"/>
        
        <xsl:variable name="newclasses" select="concat($classes,' ',$addclasses)"/>
        <xsl:variable name="otherelements" select="set:difference($allelements, $elements)"/>

        <xsl:if test="$elements">
            <xsl:message>
Processing &lt;addclass e="<xsl:value-of select="$eBare"/>" k="<xsl:value-of select="$kBare"/>" v="<xsl:value-of select="$vBare"/>" &gt; 
Positive match by <xsl:value-of select="count($elements)"/> elements for layer <xsl:value-of select="$layer"/>.
            </xsl:message>

            <xsl:apply-templates select="*">
                <xsl:with-param name="layer" select="$layer"/>
                <xsl:with-param name="elements" select="$elements"/>
                <xsl:with-param name="rule" select="$rule"/>
                <xsl:with-param name="classes" select="$newclasses"/>
            </xsl:apply-templates>
        </xsl:if>

        <xsl:if test="$otherelements">
            <xsl:message>
Processing &lt;addclass e="<xsl:value-of select="$eBare"/>" k="<xsl:value-of select="$kBare"/>" v="<xsl:value-of select="$vBare"/>" &gt; 
Negative match by <xsl:value-of select="count($otherelements)"/> elements for layer <xsl:value-of select="$layer"/>.
            </xsl:message>
            <xsl:apply-templates select="*">
                <xsl:with-param name="layer" select="$layer"/>
                <xsl:with-param name="elements" select="$otherelements"/>
                <xsl:with-param name="rule" select="$rule"/>
                <xsl:with-param name="classes" select="$classes"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>
    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" match="layer">
        <xsl:param name="elements"/>
        <xsl:param name="layer"/>
        <xsl:param name="rule"/>
        <xsl:param name="classes"/>

        <xsl:message>Processing SVG layer: <xsl:value-of select="@name"/> (at OSM layer <xsl:value-of select="$layer"/>)
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
                <xsl:with-param name="classes" select="$classes"/>
            </xsl:apply-templates>
        </g>

    </xsl:template>
    
    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" name="borderDraw">
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
    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" name="gridDraw">
        <g id="grid" inkscape:groupmode="layer" inkscape:label="Grid">
            <xsl:call-template name="gridDrawHorizontals">
                <xsl:with-param name="line" select="&quot;1&quot;"/>
            </xsl:call-template>
            <xsl:call-template name="gridDrawVerticals">
                <xsl:with-param name="line" select="&quot;1&quot;"/>
            </xsl:call-template>
        </g>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" name="gridDrawHorizontals">
        <xsl:param name="line"/>
        <xsl:if test="($line*$km) &lt; $documentHeight">
            <line id="grid-hori-{$line}" x1="0px" y1="{$line*$km}px" x2="{$documentWidth}px" y2="{$line*$km}px" class="map-grid-line"/>
            <xsl:call-template name="gridDrawHorizontals">
                <xsl:with-param name="line" select="$line+1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" name="gridDrawVerticals">
        <xsl:param name="line"/>
        <xsl:if test="($line*$km) &lt; $documentWidth">
            <line id="grid-vert-{$line}" x1="{$line*$km}px" y1="0px" x2="{$line*$km}px" y2="{$documentHeight}px" class="map-grid-line"/>
            <xsl:call-template name="gridDrawVerticals">
                <xsl:with-param name="line" select="$line+1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" name="titleDraw">
        <xsl:param name="title"/>

        <xsl:variable name="x" select="$documentWidth div 2"/>
        <xsl:variable name="y" select="30"/>

        <g id="marginalia-title" inkscape:groupmode="layer" inkscape:label="Title">
            <rect id="marginalia-title-background" x="0px" y="0px" height="{$marginaliaTopHeight - 5}px" width="{$documentWidth}px" class="map-title-background"/>
            <text id="marginalia-title-text" class="map-title" x="{$x}" y="{$y}"><xsl:value-of select="$title"/></text>
        </g>
    </xsl:template>
    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" name="scaleDraw">
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
    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:cc="http://web.resource.org/cc/" xmlns:dc="http://purl.org/dc/elements/1.1/" name="metadata">
        <xsl:variable name="date" select="date:date()"/>
        <xsl:variable name="year" select="date:year()"/>

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
                    <dc:title><xsl:value-of select="$title"/></dc:title>
                    <dc:date><xsl:value-of select="substring($date,1,10)"/></dc:date>
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
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape" xmlns:xlink="http://www.w3.org/1999/xlink" name="in-image-license">
        <xsl:param name="dx"/>
        <xsl:param name="dy"/>

        <xsl:variable name="year" select="date:year()"/>

        <g id="license" inkscape:groupmode="layer" inkscape:label="Copyright" transform="translate({$dx},{$dy})">
            <style type="text/css"><![CDATA[
                .license-text {
                    text-anchor: start;
                    font-family: "DejaVu Sans",sans-serif;
                    font-size: 6px;
                    fill: black;
                }
            ]]></style>
            <a id="license-cc-logo-link" xlink:href="http://creativecommons.org/licenses/by-sa/2.0/">
                <g id="license-cc-logo" transform="scale(0.5,0.5) translate(-604,-49)">
                    <path id="path3817_2_" nodetypes="ccccccc" d="M                     182.23532,75.39014 L 296.29928,75.59326 C                     297.89303,75.59326 299.31686,75.35644 299.31686,78.77344 L                     299.17721,116.34033 L 179.3569,116.34033 L                     179.3569,78.63379 C 179.3569,76.94922 179.51999,75.39014                     182.23532,75.39014 z " style="fill:#aab2ab"/>
                    <g id="g5908_2_" transform="matrix(0.872921,0,0,0.872921,50.12536,143.2144)">
                        <path id="path5906_2_" type="arc" cx="296.35416" cy="264.3577" ry="22.939548" rx="22.939548" d="M                         187.20944,-55.6792 C 187.21502,-46.99896                         180.18158,-39.95825 171.50134,-39.95212 C                         162.82113,-39.94708 155.77929,-46.97998                         155.77426,-55.66016 C 155.77426,-55.66687                         155.77426,-55.67249 155.77426,-55.6792 C                         155.76922,-64.36054 162.80209,-71.40125                         171.48233,-71.40631 C 180.16367,-71.41193                         187.20441,-64.37842 187.20944,-55.69824 C                         187.20944,-55.69263 187.20944,-55.68591                         187.20944,-55.6792 z " style="fill:white"/>
                        <g id="g5706_2_" transform="translate(-289.6157,99.0653)">
                            <path id="path5708_2_" d="M 473.88455,-167.54724 C                             477.36996,-164.06128 479.11294,-159.79333                             479.11294,-154.74451 C 479.11294,-149.69513                             477.40014,-145.47303 473.9746,-142.07715 C                             470.33929,-138.50055 466.04281,-136.71283                             461.08513,-136.71283 C 456.18736,-136.71283                             451.96526,-138.48544 448.42003,-142.03238 C                             444.87419,-145.57819 443.10158,-149.81537                             443.10158,-154.74451 C 443.10158,-159.6731                             444.87419,-163.94049 448.42003,-167.54724 C                             451.87523,-171.03375 456.09728,-172.77618                             461.08513,-172.77618 C 466.13342,-172.77618                             470.39914,-171.03375 473.88455,-167.54724 z M                             450.76657,-165.20239 C 447.81982,-162.22601                             446.34701,-158.7395 446.34701,-154.74005 C                             446.34701,-150.7417 447.80529,-147.28485                             450.72125,-144.36938 C 453.63778,-141.45288                             457.10974,-139.99462 461.1383,-139.99462 C                             465.16683,-139.99462 468.66848,-141.46743                             471.64486,-144.41363 C 474.47076,-147.14947                             475.88427,-150.59069 475.88427,-154.74005 C                             475.88427,-158.85809 474.44781,-162.35297                             471.57659,-165.22479 C 468.70595,-168.09546                             465.22671,-169.53131 461.1383,-169.53131 C                             457.04993,-169.53131 453.59192,-168.08813                             450.76657,-165.20239 z M 458.52106,-156.49927 C                             458.07074,-157.4809 457.39673,-157.9715                             456.49781,-157.9715 C 454.90867,-157.9715                             454.11439,-156.90198 454.11439,-154.763 C                             454.11439,-152.62341 454.90867,-151.55389                             456.49781,-151.55389 C 457.54719,-151.55389                             458.29676,-152.07519 458.74647,-153.11901 L                             460.94923,-151.94598 C 459.8993,-150.0805                             458.32417,-149.14697 456.22374,-149.14697 C                             454.60384,-149.14697 453.30611,-149.64367                             452.33168,-150.63653 C 451.35561,-151.62994                             450.86894,-152.99926 450.86894,-154.7445 C                             450.86894,-156.46008 451.37123,-157.82159                             452.37642,-158.83013 C 453.38161,-159.83806                             454.63347,-160.34264 456.13423,-160.34264 C                             458.35435,-160.34264 459.94407,-159.46776                             460.90504,-157.71978 L 458.52106,-156.49927 z M                             468.8844,-156.49927 C 468.43353,-157.4809                             467.77292,-157.9715 466.90201,-157.9715 C                             465.28095,-157.9715 464.46988,-156.90198                             464.46988,-154.763 C 464.46988,-152.62341                             465.28095,-151.55389 466.90201,-151.55389 C                             467.95304,-151.55389 468.68918,-152.07519                             469.10925,-153.11901 L 471.36126,-151.94598 C                             470.31301,-150.0805 468.74007,-149.14697                             466.64358,-149.14697 C 465.02587,-149.14697                             463.73095,-149.64367 462.75711,-150.63653 C                             461.78494,-151.62994 461.29773,-152.99926                             461.29773,-154.7445 C 461.29773,-156.46008                             461.79221,-157.82159 462.78061,-158.83013 C                             463.76843,-159.83806 465.02588,-160.34264                             466.55408,-160.34264 C 468.77027,-160.34264                             470.35776,-159.46776 471.3154,-157.71978 L                             468.8844,-156.49927 z "/>
                        </g>
                    </g>
                    <path d="M 297.29639,74.91064 L 181.06688,74.91064 C                     179.8203,74.91064 178.80614,75.92529 178.80614,77.17187 L                     178.80614,116.66748 C 178.80614,116.94922                     179.03466,117.17822 179.31639,117.17822 L                     299.04639,117.17822 C 299.32813,117.17822                     299.55713,116.94922 299.55713,116.66748 L                     299.55713,77.17188 C 299.55713,75.92529 298.54297,74.91064                     297.29639,74.91064 z M 181.06688,75.93213 L                     297.29639,75.93213 C 297.97998,75.93213 298.53565,76.48828                     298.53565,77.17188 C 298.53565,77.17188 298.53565,93.09131                     298.53565,104.59034 L 215.4619,104.59034 C                     212.41698,110.09571 206.55077,113.83399 199.81835,113.83399                     C 193.083,113.83399 187.21825,110.09913 184.1748,104.59034                     L 179.82666,104.59034 C 179.82666,93.09132                     179.82666,77.17188 179.82666,77.17188 C 179.82664,76.48828                     180.38329,75.93213 181.06688,75.93213 z " id="frame"/>
                    <g enable-background="new" id="g2821">
                        <path d="M 265.60986,112.8833 C 265.68994,113.03906                         265.79736,113.16504 265.93115,113.26172 C                         266.06494,113.35791 266.22119,113.42969                         266.40088,113.47608 C 266.58154,113.52296                         266.76807,113.54639 266.96045,113.54639 C                         267.09033,113.54639 267.22998,113.53565                         267.3794,113.51368 C 267.52784,113.4922                         267.66749,113.44972 267.79835,113.3877 C                         267.92823,113.32569 268.03761,113.23975                         268.12355,113.13086 C 268.21144,113.02197                         268.25441,112.88379 268.25441,112.71533 C                         268.25441,112.53515 268.19679,112.38916                         268.08156,112.27685 C 267.9673,112.16455                         267.81594,112.07177 267.62941,111.99658 C                         267.44386,111.92236 267.23195,111.85693                         266.9966,111.80078 C 266.76027,111.74463                         266.52101,111.68262 266.27883,111.61377 C                         266.02981,111.55176 265.78762,111.47559                         265.55129,111.38525 C 265.31594,111.29541                         265.10402,111.17822 264.9175,111.03515 C                         264.73098,110.89208 264.58059,110.71337                         264.46535,110.49853 C 264.35109,110.28369                         264.29347,110.02392 264.29347,109.71923 C                         264.29347,109.37646 264.36671,109.07958                         264.51222,108.82763 C 264.6587,108.57568                         264.85011,108.36572 265.08644,108.19726 C                         265.32179,108.02929 265.58937,107.90478                         265.8882,107.82372 C 266.18605,107.74315                         266.48488,107.70263 266.78273,107.70263 C                         267.13136,107.70263 267.46535,107.74169                         267.78566,107.81982 C 268.105,107.89746                         268.39015,108.02392 268.6382,108.19824 C                         268.88722,108.37256 269.08449,108.59521                         269.23097,108.86621 C 269.37648,109.13721                         269.44972,109.46582 269.44972,109.85156 L                         268.02784,109.85156 C 268.01514,109.65234                         267.97315,109.4873 267.90284,109.35693 C                         267.83155,109.22607 267.73682,109.12353                         267.61964,109.04834 C 267.50148,108.97412                         267.36671,108.9209 267.21534,108.89014 C                         267.063,108.85889 266.89796,108.84326                         266.71827,108.84326 C 266.60108,108.84326                         266.48292,108.85596 266.36573,108.88037 C                         266.24757,108.90576 266.14112,108.94922                         266.04542,109.01123 C 265.94874,109.07373                         265.86964,109.15137 265.80812,109.24463 C                         265.7466,109.33838 265.71535,109.45654                         265.71535,109.59961 C 265.71535,109.73047                         265.73976,109.83643 265.78957,109.91699 C                         265.83937,109.99804 265.93801,110.07275                         266.08352,110.14111 C 266.22903,110.20947                         266.43118,110.27832 266.68899,110.34668 C                         266.9468,110.41504 267.28372,110.50244                         267.70071,110.60791 C 267.82473,110.63281                         267.99661,110.67822 268.21731,110.74365 C                         268.43801,110.80908 268.65676,110.91308                         268.87454,111.05615 C 269.09231,111.1997                         269.27981,111.39111 269.43899,111.63037 C                         269.59719,111.87012 269.67629,112.17676                         269.67629,112.55029 C 269.67629,112.85547                         269.61672,113.13867 269.49856,113.3999 C                         269.3804,113.66162 269.20461,113.8872                         268.97122,114.07666 C 268.73782,114.26709                         268.44876,114.41455 268.10403,114.52051 C                         267.75833,114.62647 267.35794,114.6792                         266.90481,114.6792 C 266.53762,114.6792                         266.18118,114.63379 265.83547,114.54346 C                         265.49074,114.45313 265.18508,114.31104                         264.92043,114.11768 C 264.65676,113.92432                         264.4468,113.67774 264.29055,113.37891 C                         264.13528,113.07959 264.06106,112.7251                         264.06692,112.31397 L 265.4888,112.31397 C                         265.48877,112.53809 265.52881,112.72803                         265.60986,112.8833 z " id="path2823" style="fill:white"/>
                        <path d="M 273.8667,107.8667 L                         276.35986,114.53076 L 274.8374,114.53076 L                         274.33349,113.04638 L 271.84033,113.04638 L                         271.31787,114.53076 L 269.84326,114.53076 L                         272.36377,107.8667 L 273.8667,107.8667 z M                         273.95068,111.95264 L 273.11084,109.50928 L                         273.09229,109.50928 L 272.22315,111.95264 L                         273.95068,111.95264 z " id="path2825" style="fill:white"/>
                    </g>
                    <g enable-background="new" id="g2827">
                        <path d="M 239.17821,107.8667 C 239.49559,107.8667                         239.78563,107.89502 240.04735,107.95068 C                         240.30907,108.00683 240.53368,108.09863                         240.72118,108.22607 C 240.9077,108.35351                         241.05321,108.52295 241.15575,108.73437 C                         241.25829,108.94579 241.31005,109.20703                         241.31005,109.51806 C 241.31005,109.854                         241.23388,110.13329 241.08056,110.35742 C                         240.92822,110.58154 240.70165,110.76465                         240.40283,110.90771 C 240.81494,111.02587                         241.12256,111.23291 241.32568,111.5288 C                         241.5288,111.82469 241.63037,112.18114                         241.63037,112.59814 C 241.63037,112.93408                         241.56494,113.22509 241.43408,113.47119 C                         241.30322,113.7168 241.12646,113.91748                         240.90576,114.07324 C 240.68408,114.229                         240.43115,114.34424 240.14795,114.41845 C                         239.86377,114.49365 239.57275,114.53075                         239.27295,114.53075 L 236.03662,114.53075 L                         236.03662,107.86669 L 239.17821,107.86669 L                         239.17821,107.8667 z M 238.99071,110.56201 C                         239.25243,110.56201 239.46727,110.5 239.63622,110.37597                         C 239.80419,110.25146 239.88817,110.05029                         239.88817,109.77099 C 239.88817,109.61572                         239.85985,109.48828 239.80419,109.38915 C                         239.74755,109.28954 239.67333,109.21239                         239.57958,109.15624 C 239.48583,109.10058                         239.37841,109.06151 239.25731,109.04003 C                         239.13524,109.01806 239.00926,109.00732                         238.8784,109.00732 L 237.50535,109.00732 L                         237.50535,110.56201 L 238.99071,110.56201 z M                         239.07664,113.39014 C 239.22019,113.39014                         239.35691,113.37647 239.48777,113.34815 C                         239.61863,113.32032 239.73484,113.27344                         239.83445,113.2085 C 239.93406,113.14307                         240.01316,113.0542 240.07273,112.94239 C                         240.1323,112.83058 240.1616,112.68751                         240.1616,112.51319 C 240.1616,112.17139                         240.06492,111.92725 239.87156,111.78126 C                         239.6782,111.63527 239.42234,111.56202                         239.10496,111.56202 L 237.50535,111.56202 L                         237.50535,113.39014 L 239.07664,113.39014 z " id="path2829" style="fill:white"/>
                        <path d="M 241.88914,107.8667 L 243.53269,107.8667 L                         245.09324,110.49854 L 246.64402,107.8667 L                         248.27781,107.8667 L 245.80418,111.97315 L                         245.80418,114.53077 L 244.33543,114.53077 L                         244.33543,111.93604 L 241.88914,107.8667 z " id="path2831" style="fill:white"/>
                    </g>
                    <g id="g6316_1_" transform="matrix(0.624995,0,0,0.624995,391.2294,176.9332)">
                        <path id="path6318_1_" type="arc" cx="475.97119" cy="252.08646" ry="29.209877" rx="29.209877" d="M                         -175.0083,-139.1153 C -175.00204,-129.7035                         -182.62555,-122.06751 -192.03812,-122.06049 C                         -201.44913,-122.05341 -209.08512,-129.67774                         -209.09293,-139.09028 C -209.09293,-139.09809                         -209.09293,-139.10749 -209.09293,-139.1153 C                         -209.09919,-148.52784 -201.47413,-156.1623                         -192.06311,-156.17011 C -182.65054,-156.17713                         -175.01456,-148.55207 -175.0083,-139.14026 C                         -175.0083,-139.13092 -175.0083,-139.1239                         -175.0083,-139.1153 z " style="fill:white"/>
                        <g id="g6320_1_" transform="translate(-23.9521,-89.72962)">
                            <path id="path6322_1_" d="M -168.2204,-68.05536 C                             -173.39234,-68.05536 -177.76892,-66.25067                             -181.35175,-62.64203 C -185.02836,-58.90759                             -186.86588,-54.48883 -186.86588,-49.38568 C                             -186.86588,-44.28253 -185.02836,-39.89416                             -181.35175,-36.22308 C -177.67673,-32.55114                             -173.29859,-30.71521 -168.2204,-30.71521 C                             -163.07974,-30.71521 -158.62503,-32.56677                             -154.85312,-36.26996 C -151.30307,-39.78558                             -149.52652,-44.15827 -149.52652,-49.38568 C                             -149.52652,-54.6123 -151.33432,-59.03265                             -154.94843,-62.64203 C -158.5625,-66.25067                             -162.98599,-68.05536 -168.2204,-68.05536 z M                             -168.17352,-64.69519 C -163.936,-64.69519                             -160.33752,-63.20221 -157.37655,-60.21466 C                             -154.38748,-57.25836 -152.89214,-53.64899                             -152.89214,-49.38568 C -152.89214,-45.09186                             -154.35466,-41.52856 -157.28438,-38.69653 C                             -160.36876,-35.64727 -163.99849,-34.12304                             -168.17351,-34.12304 C -172.34856,-34.12304                             -175.94701,-35.63244 -178.96892,-38.64965 C                             -181.9908,-41.66918 -183.50176,-45.24657                             -183.50176,-49.38567 C -183.50176,-53.52398                             -181.97518,-57.13414 -178.92205,-60.21465 C                             -175.9939,-63.20221 -172.41107,-64.69519                             -168.17352,-64.69519 z "/>
                            <path id="path6324_1_" d="M -176.49548,-52.02087 C                             -175.75171,-56.71856 -172.44387,-59.22949                             -168.30008,-59.22949 C -162.33911,-59.22949                             -158.70783,-54.90448 -158.70783,-49.1372 C                             -158.70783,-43.50982 -162.57194,-39.13793                             -168.39383,-39.13793 C -172.39856,-39.13793                             -175.98297,-41.60277 -176.63611,-46.43877 L                             -171.93292,-46.43877 C -171.7923,-43.92778                             -170.1626,-43.04418 -167.83447,-43.04418 C                             -165.1813,-43.04418 -163.4563,-45.50908                             -163.4563,-49.27709 C -163.4563,-53.22942                             -164.94693,-55.32244 -167.74228,-55.32244 C                             -169.79074,-55.32244 -171.55948,-54.57787                             -171.93292,-52.02087 L -170.56418,-52.02789 L                             -174.26734,-48.32629 L -177.96894,-52.02789 L                             -176.49548,-52.02087 z "/>
                        </g>
                    </g>
                    <g id="g2838">
                        <circle cx="242.56226" cy="90.224609" r="10.8064" id="circle2840" style="fill:white"/>
                        <g id="g2842">
                            <path d="M 245.68994,87.09766 C 245.68994,86.68116                             245.35205,86.34424 244.93603,86.34424 L                             240.16357,86.34424 C 239.74755,86.34424                             239.40966,86.68115 239.40966,87.09766 L                             239.40966,91.87061 L 240.74071,91.87061 L                             240.74071,97.52295 L 244.3579,97.52295 L                             244.3579,91.87061 L 245.68993,91.87061 L                             245.68993,87.09766 L 245.68994,87.09766 z " id="path2844"/>
                            <circle cx="242.5498" cy="84.083008" r="1.63232" id="circle2846"/>
                        </g>
                        <path clip-rule="evenodd" d="M 242.53467,78.31836 C                         239.30322,78.31836 236.56641,79.4458 234.32715,81.70215                         C 232.0293,84.03516 230.88086,86.79736                         230.88086,89.98633 C 230.88086,93.1753                         232.0293,95.91846 234.32715,98.21338 C                         236.625,100.50781 239.36133,101.65527                         242.53467,101.65527 C 245.74756,101.65527                         248.53272,100.49853 250.88819,98.18359 C                         253.10889,95.98681 254.21827,93.2539 254.21827,89.98632                         C 254.21827,86.71874 253.08936,83.95751                         250.83057,81.70214 C 248.57178,79.4458                         245.80615,78.31836 242.53467,78.31836 z M                         242.56396,80.41797 C 245.2124,80.41797                         247.46142,81.35156 249.31103,83.21875 C                         251.18115,85.06592 252.11572,87.32227                         252.11572,89.98633 C 252.11572,92.66992                         251.20068,94.89746 249.36963,96.66699 C                         247.4419,98.57275 245.17334,99.52539 242.56397,99.52539                         C 239.9546,99.52539 237.70557,98.58252                         235.81739,96.6958 C 233.92774,94.80957                         232.98389,92.57324 232.98389,89.98633 C                         232.98389,87.3999 233.93799,85.14404 235.84619,83.21875                         C 237.67676,81.35156 239.9165,80.41797                         242.56396,80.41797 z " id="path2848" style="fill-rule:evenodd"/>
                    </g>
                </g>
            </a>
            <a id="license-osm-link" xlink:href="http://www.openstreetmap.org/">
                <g transform="translate(-210,10)" id="license-osm-text">
                    <text class="license-text" dx="0" dy="0">Copyright © <xsl:value-of select="$year"/> OpenStreetMap (openstreetmap.org)</text>
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

    <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" name="zoomControl">
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

        <g id="gPanDown" filter="url(#fancyButton)" onclick="fnPan(&quot;down&quot;)">
            <use x="18px" y="60px" xlink:href="#panDown" width="14px" height="14px"/>
        </g>
        <g id="gPanRight" filter="url(#fancyButton)" onclick="fnPan(&quot;right&quot;)">
            <use x="8px" y="70px" xlink:href="#panRight" width="14px" height="14px"/>
        </g>
        <g id="gPanLeft" filter="url(#fancyButton)" onclick="fnPan(&quot;left&quot;)">
            <use x="28px" y="70px" xlink:href="#panLeft" width="14px" height="14px"/>
        </g>
        <g id="gPanUp" filter="url(#fancyButton)" onclick="fnPan(&quot;up&quot;)">
            <use x="18px" y="80px" xlink:href="#panUp" width="14px" height="14px"/>
        </g>

        <xsl:variable name="x1" select="25"/>
        <xsl:variable name="y1" select="105"/>
        <xsl:variable name="x2" select="25"/>
        <xsl:variable name="y2" select="300"/>

        <line style="stroke-width: 10; stroke-linecap: butt; stroke: #8080ff;">
            <xsl:attribute name="x1"><xsl:value-of select="$x1"/></xsl:attribute>
            <xsl:attribute name="y1"><xsl:value-of select="$y1"/></xsl:attribute>
            <xsl:attribute name="x2"><xsl:value-of select="$x2"/></xsl:attribute>
            <xsl:attribute name="y2"><xsl:value-of select="$y2"/></xsl:attribute>
        </line>

        <line style="stroke-width: 8; stroke-linecap: butt; stroke: white; stroke-dasharray: 10,1;">
            <xsl:attribute name="x1"><xsl:value-of select="$x1"/></xsl:attribute>
            <xsl:attribute name="y1"><xsl:value-of select="$y1"/></xsl:attribute>
            <xsl:attribute name="x2"><xsl:value-of select="$x2"/></xsl:attribute>
            <xsl:attribute name="y2"><xsl:value-of select="$y2"/></xsl:attribute>
        </line>

        <!-- Need to use onmousedown because onclick is interfered with by the onmousedown handler for panning -->
        <g id="gZoomIn" filter="url(#fancyButton)" onmousedown="fnZoom(&quot;in&quot;)">
            <use x="15.5px" y="100px" xlink:href="#zoomIn" width="19px" height="19px"/>
        </g>

        <!-- Need to use onmousedown because onclick is interfered with by the onmousedown handler for panning -->
        <g id="gZoomOut" filter="url(#fancyButton)" onmousedown="fnZoom(&quot;out&quot;)">
            <use x="15.5px" y="288px" xlink:href="#zoomOut" width="19px" height="19px"/>
        </g>
    </xsl:template><xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2000/svg" xmlns:xi="http://www.w3.org/2001/XInclude" name="javaScript">
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


        </script>
    </xsl:template>

</xsl:stylesheet>
