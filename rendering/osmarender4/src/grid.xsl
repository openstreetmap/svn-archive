<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Draw a grid over the map in 1km increments -->

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name='gridDraw'>
        <g id="grid" inkscape:groupmode="layer" inkscape:label="Grid">
            <xsl:call-template name='gridDrawHorizontals'>
                <xsl:with-param name='line' select='"1"'/>
            </xsl:call-template>
            <xsl:call-template name='gridDrawVerticals'>
                <xsl:with-param name='line' select='"1"'/>
            </xsl:call-template>
        </g>
    </xsl:template>

    <xsl:template name='gridDrawHorizontals'>
        <xsl:param name='line'/>
        <xsl:if test='($line*$km) &lt; $documentHeight'>
            <line id="grid-hori-{$line}" x1='0px' y1='{$line*$km}px' x2='{$documentWidth}px' y2='{$line*$km}px' class='map-grid-line'/>
            <xsl:call-template name='gridDrawHorizontals'>
                <xsl:with-param name='line' select='$line+1'/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name='gridDrawVerticals'>
        <xsl:param name='line'/>
        <xsl:if test='($line*$km) &lt; $documentWidth'>
            <line id="grid-vert-{$line}" x1='{$line*$km}px' y1='0px' x2='{$line*$km}px' y2='{$documentHeight}px' class='map-grid-line'/>
            <xsl:call-template name='gridDrawVerticals'>
                <xsl:with-param name='line' select='$line+1'/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
