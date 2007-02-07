<?xml version='1.0' encoding='UTF-8' ?>
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

<xsl:stylesheet 
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:cc="http://web.resource.org/cc/"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:date="http://exslt.org/dates-and-times"
    xmlns:set="http://exslt.org/sets"
    extension-element-prefixes="date set"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
    <xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="UTF-8"/>

    <xsl:param name="osmfile" select="/rules/@data"/>
    <xsl:param name="title" select="/rules/@title"/>

    <xsl:key name='nodeById' match='/osm/node' use='@id'/>
    <xsl:key name='segmentById' match='/osm/segment' use='@id'/>
    <xsl:key name='segmentByFromNode' match='/osm/segment' use='@from'/>
    <xsl:key name='segmentByToNode' match='/osm/segment' use='@to'/>
    <xsl:key name='wayBySegment' match='/osm/way' use='seg/@id'/>
    
    <xsl:variable name='data' select='document($osmfile)'/>
    <xsl:variable name='scale' select='/rules/@scale'/>
    <xsl:variable name='withOSMLayers' select='/rules/@withOSMLayers'/>
    <xsl:variable name='withUntaggedSegments' select='/rules/@withUntaggedSegments'/>
    <xsl:variable name='svgBaseProfile' select='/rules/@svgBaseProfile'/>

    <xsl:variable name="marginaliaTopHeight">
        <xsl:choose>
            <xsl:when test="$title!=''">40</xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xi:include href="boundingbox.xsl" xpointer="xpointer(/*/*)"/>

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
         baseProfile="{$svgBaseProfile}"
         height="100%"
         width="100%">        
            <xsl:if test='/rules/@interactive="yes"'>
                <xsl:attribute name='onscroll'>fnOnScroll(evt)</xsl:attribute>
                <xsl:attribute name='onzoom'>fnOnZoom(evt)</xsl:attribute>
                <xsl:attribute name='onload'>fnOnLoad(evt)</xsl:attribute>
                <xsl:attribute name='onmousedown'>fnOnMouseDown(evt)</xsl:attribute>
                <xsl:attribute name='onmousemove'>fnOnMouseMove(evt)</xsl:attribute>
                <xsl:attribute name='onmouseup'>fnOnMouseUp(evt)</xsl:attribute>
            </xsl:if>

            <xsl:call-template name="metadata"/>

            <!-- Include javaScript functions for all the dynamic stuff --> 
            <xsl:if test='/rules/@interactive="yes"'>
                <xsl:call-template name='javaScript'/>
            </xsl:if>

            <defs id="defs-rulefile">
                <!-- Get any <defs> and styles from the rules file -->
                <xsl:copy-of select='defs/*'/>
            </defs>

            <!-- Pre-generate named path definitions for all ways -->
            <xsl:variable name='allWays' select='$data/osm/way' />
            <defs id="paths-of-ways">
                <xsl:for-each select='$allWays'>
                    <xsl:call-template name='generateWayPath'/>
                </xsl:for-each>
            </defs>

            <!-- Clipping rectangle for map -->
            <clipPath id="map-clipping">
                <rect id="map-clipping-rect" x='0px' y='0px' height='{$documentHeight}px' width='{$documentWidth}px'/>
            </clipPath>

            <g id="map" clip-path="url(#map-clipping)" inkscape:groupmode="layer" inkscape:label="Map" transform="translate(0,{$marginaliaTopHeight})">
                <!-- Draw a nice background layer -->
                <rect id="background" x='0px' y='0px' height='{$documentHeight}px' width='{$documentWidth}px' class='map-background'/>

                <!-- If this is set we first draw all untagged segments not belonging to any way -->
                <xsl:if test='$withUntaggedSegments="yes"'>
                    <xsl:call-template name="drawUntaggedSegments"/>
                </xsl:if>

                <!-- Process all the rules drawing all map features -->
                <xsl:call-template name="processRules"/>
            </g>

            <!-- Draw map decoration -->
            <g id="map-decoration" inkscape:groupmode="layer" inkscape:label="Map decoration" transform="translate(0,{$marginaliaTopHeight})">
                <!-- Draw a grid if required -->
                <xsl:if test='/rules/@showGrid="yes"'>
                    <xsl:call-template name="gridDraw"/>
                </xsl:if>

                <!-- Draw a border if required -->
                <xsl:if test='/rules/@showBorder="yes"'>
                    <xsl:call-template name="borderDraw"/>
                </xsl:if>
            </g>

            <!-- Draw map marginalia -->
            <xsl:if test="($title != '') or (/rules/@showScale = 'yes') or (/rules/@showLicense = 'yes')">
                <g id="marginalia" inkscape:groupmode="layer" inkscape:label="Marginalia">
                    <!-- Draw the title -->
                    <xsl:if test="$title!=''">
                        <xsl:call-template name="titleDraw">
                            <xsl:with-param name="title" select="$title"/>
                        </xsl:call-template>
                    </xsl:if>

                    <g id="marginalia-bottom" inkscape:groupmode="layer" inkscape:label="Marginalia (Bottom)" transform="translate(0,{$marginaliaTopHeight})">
                        <!-- Draw background for marginalia at bottom -->
                        <rect id="marginalia-background" x='0px' y='{$documentHeight + 5}px' height='40px' width='{$documentWidth}px' class='map-marginalia-background'/>

                        <!-- Draw the scale in the bottom left corner -->
                        <xsl:if test='/rules/@showScale="yes"'>
                            <xsl:call-template name="scaleDraw"/>
                        </xsl:if>

                        <!-- Draw Creative commons license -->
                        <xsl:if test='/rules/@showLicense="yes"'>
                            <xsl:call-template name="in-image-license">
                                <xsl:with-param name="year" select="2007"/>
                                <xsl:with-param name="dx" select="$documentWidth"/>
                                <xsl:with-param name="dy" select="$documentHeight"/>
                            </xsl:call-template>
                        </xsl:if>
                    </g>
                </g>
            </xsl:if>

            <!-- Draw labels and controls that are in a static position -->
            <g id="staticElements" transform="scale(1) translate(0,0)">
                <!-- Draw the +/- zoom controls -->
                <xsl:if test='/rules/@interactive="yes"'>
                    <xsl:call-template name="zoomControl"/>
                </xsl:if>
            </g>
        </svg>

    </xsl:template>

    <!-- include templates from all the other files -->
    <xi:include href="draw.xsl" xpointer="xpointer(/*/*)"/>
    <xi:include href="segments.xsl" xpointer="xpointer(/*/*)"/>
    <xi:include href="instructions.xsl" xpointer="xpointer(/*/*)"/>
    <xi:include href="util.xsl" xpointer="xpointer(/*/*)"/>

    <xi:include href="rules.xsl" xpointer="xpointer(/*/*)"/>
    <xi:include href="layer.xsl" xpointer="xpointer(/*/*)"/>
    
    <xi:include href="border.xsl" xpointer="xpointer(/*/*)"/>
    <xi:include href="grid.xsl" xpointer="xpointer(/*/*)"/>

    <xi:include href="title.xsl" xpointer="xpointer(/*/*)"/>
    <xi:include href="scale.xsl" xpointer="xpointer(/*/*)"/>
    <xi:include href="attribution.xsl" xpointer="xpointer(/*/*)"/>

    <xi:include href="interactive.xsl" xpointer="xpointer(/*/*)"/>

</xsl:stylesheet>
