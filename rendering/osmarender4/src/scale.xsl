<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Draw an approximate scale in the bottom left corner of the map -->

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name='scaleDraw'>
        <xsl:variable name='x1' select='14' />
        <xsl:variable name='y1' select='round(($documentHeight)+((($bottomLeftLatitude)-(number($bottomLeftLatitude)))*10000*$scale*$projection))+28'/>
        <xsl:variable name='x2' select='$x1+$km'/>
        <xsl:variable name='y2' select='$y1'/>

        <g id="marginalia-scale" inkscape:groupmode="layer" inkscape:label="Scale">
            <line id="marginalia-scale-casing" class='map-scale-casing'
                x1='{$x1}'
                y1='{$y1}'
                x2='{$x2}'
                y2='{$y2}'/>

            <line id="marginalia-scale-core" class='map-scale-core' stroke-dasharray='{($km div 10)}'
                x1='{$x1}'
                y1='{$y1}'
                x2='{$x2}'
                y2='{$y2}'/>

            <line id="marginalia-scale-bookend-from" class='map-scale-bookend'
                x1='{$x1}'
                y1='{$y1 + 2}'
                x2='{$x1}'
                y2='{$y1 - 10}'/>

            <line id="marginalia-scale-bookend-to" class='map-scale-bookend'
                x1='{$x2}'
                y1='{$y2 + 2}'
                x2='{$x2}'
                y2='{$y2 - 10}'/>

            <text id="marginalia-scale-text-from" class='map-scale-caption' x='{$x1}' y='{$y1 - 10}'>0</text>

            <text id="marginalia-scale-text-to" class='map-scale-caption' x='{$x2}' y='{$y2 - 10}'>1km</text>
        </g>
    </xsl:template>
</xsl:stylesheet>
