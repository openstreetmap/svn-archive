<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Draw map border -->

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name='borderDraw'>
        <!-- dasharray definitions here can be overridden in stylesheet -->
        <g id="border" inkscape:groupmode="layer" inkscape:label="Map Border">
            <line id="border-left-casing" x1="0" y1="0" x2="0" y2="{$documentHeight}" class='map-border-casing' stroke-dasharray="{($km div 10) - 1},1" />
            <line id="border-top-casing" x1="0" y1="0" x2="{$documentWidth}" y2="0" class='map-border-casing' stroke-dasharray="{($km div 10) - 1},1" />
            <line id="border-bottom-casing" x1="0" y1="{$documentHeight}" x2="{$documentWidth}" y2="{$documentHeight}" class='map-border-casing' stroke-dasharray="{($km div 10) - 1},1" />
            <line id="border-right-casing" x1="{$documentWidth}" y1="0" x2="{$documentWidth}" y2="{$documentHeight}" class='map-border-casing' stroke-dasharray="{($km div 10) - 1},1" />

            <line id="border-left-core" x1="0" y1="0" x2="0" y2="{$documentHeight}" class='map-border-core' stroke-dasharray="{($km div 10) - 1},1" />
            <line id="border-top-core" x1="0" y1="0" x2="{$documentWidth}" y2="0" class='map-border-core' stroke-dasharray="{($km div 10) - 1},1" />
            <line id="border-bottom-core" x1="0" y1="{$documentHeight}" x2="{$documentWidth}" y2="{$documentHeight}" class='map-border-core' stroke-dasharray="{($km div 10) - 1},1" />
            <line id="border-right-core" x1="{$documentWidth}" y1="0" x2="{$documentWidth}" y2="{$documentHeight}" class='map-border-core' stroke-dasharray="{($km div 10) - 1},1" />
        </g>
    </xsl:template>
</xsl:stylesheet>
