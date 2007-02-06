<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Drawing of segments  -->

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name="drawUntaggedSegments">
        <g id="segments" inkscape:groupmode="layer" inkscape:label="Segments">
            <xsl:for-each select="$data/osm/segment[not(key('wayBySegment', @id))]">
                <xsl:if test="not(tag[@key!='created_by'])">
                    <xsl:variable name="fromNode" select="key('nodeById', @from)"/>
                    <xsl:variable name="toNode" select="key('nodeById', @to)"/>
                    <xsl:variable name='x1' select='($width)-((($topRightLongitude)-($fromNode/@lon))*10000*$scale)' />
                    <xsl:variable name='y1' select='($height)+((($bottomLeftLatitude)-($fromNode/@lat))*10000*$scale*$projection)'/>
                    <xsl:variable name='x2' select='($width)-((($topRightLongitude)-($toNode/@lon))*10000*$scale)' />
                    <xsl:variable name='y2' select='($height)+((($bottomLeftLatitude)-($toNode/@lat))*10000*$scale*$projection)'/>
                    <line class="untagged-segments" x1="{$x1}" y1="{$y1}" x2="{$x2}" y2="{$y2}"/>
                </xsl:if>
            </xsl:for-each>
        </g>
    </xsl:template>

</xsl:stylesheet>
