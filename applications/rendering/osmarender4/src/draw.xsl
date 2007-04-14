<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Drawing templates  -->

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

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
        <xsl:param name='classes'/>

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
            </xsl:if>

            <!-- Now draw the way itself -->
            <use xlink:href='#way_{$way/@id}'>
                <xsl:apply-templates select='$instruction/@*' mode='copyAttributes'>
                    <xsl:with-param name="classes" select="$classes"/>
                </xsl:apply-templates>
            </use>
        </xsl:if>

    </xsl:template>


    <!-- Draw a tunnel -->
    <xsl:template name='drawTunnel'>
        <xsl:param name='instruction'/>
        <xsl:param name='way'/>
        <xsl:param name='layer'/>
        <xsl:param name='classes'/>

        <xsl:choose>
            <xsl:when test="$instruction/@width &gt; 0">
                <!-- wide tunnels use a dashed line as wide as the road casing with a mask as wide as the road core which will be
                rendered as a double dotted line -->
                <mask id="mask_{@id}" maskUnits="userSpaceOnUse">
                    <use xlink:href="#way_{@id}" style="stroke:black;fill:none;" class="{$instruction/@class}-core"/>
                    <rect x='0px' y='0px' height='{$documentHeight}px' width='{$documentWidth}px' style="fill:white;"/>
                </mask>
                <use xlink:href='#way_{$way/@id}' mask="url(#mask_{@id})" style="stroke-dasharray:0.2,0.2;" class="{$instruction/@class}-casing"/>
                <use xlink:href='#way_{$way/@id}' class="tunnel-casing" style="stroke-width:{$instruction/@width}px;"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- narrow tunnels will use a single dotted line -->
                <use xlink:href='#way_{$way/@id}'>
                    <xsl:apply-templates select='$instruction/@*' mode='copyAttributes'>
                        <xsl:with-param name="classes" select="$classes"/>
                    </xsl:apply-templates>
                </use>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!-- Draw a circle for the current <node> element using the formatting of the current <circle> instruction -->
    <xsl:template name='drawCircle'>
        <xsl:param name='instruction'/>

        <xsl:variable name='x' select='($width)-((($topRightLongitude)-(@lon))*10000*$scale)' />
        <xsl:variable name='y' select='($height)+((($bottomLeftLatitude)-(@lat))*10000*$scale*$projection)'/>

        <circle cx='{$x}' cy='{$y}'>
            <xsl:apply-templates select='$instruction/@*' mode='copyAttributes' /> <!-- Copy all the svg attributes from the <circle> instruction -->
        </circle>

    </xsl:template>

    <!-- Draw a symbol for the current <node> element using the formatting of the current <symbol> instruction -->
    <xsl:template name='drawSymbol'>
        <xsl:param name='instruction'/>

        <xsl:variable name='x' select='($width)-((($topRightLongitude)-(@lon))*10000*$scale)' />
        <xsl:variable name='y' select='($height)+((($bottomLeftLatitude)-(@lat))*10000*$scale*$projection)'/>

        <g transform="translate({$x},{$y}) scale({$symbolScale})">
            <use width="1" height="1">
                <xsl:if test="$instruction/@ref">
                    <xsl:attribute name="xlink:href">
                        <xsl:value-of select="concat('#symbol-', $instruction/@ref)"/>
                    </xsl:attribute>
                </xsl:if>
                <xsl:apply-templates select='$instruction/@*' mode='copyAttributes'/> <!-- Copy all the attributes from the <symbol> instruction -->
            </use>
        </g>
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

    <!-- If there are any tags like <tag k="svg:font-size" v="5"/> then add these as attributes of the svg output -->
    <xsl:template name='getSvgAttributesFromOsmTags'>
        <xsl:for-each select='tag[contains(@k,"svg:")]'>
            <xsl:attribute name='{substring-after(@k,"svg:")}'><xsl:value-of select='@v'/></xsl:attribute>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
