<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Draw map title -->

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name='titleDraw'>
        <xsl:param name="title"/>

        <xsl:variable name='x' select='$documentWidth div 2'/>
        <xsl:variable name='y' select='30'/>

        <g id="marginalia-title" inkscape:groupmode="layer" inkscape:label="Title">
            <rect id="marginalia-title-background" x='0px' y='0px' height='{$marginaliaTopHeight - 5}px' width='{$documentWidth}px' class='map-title-background'/>
            <text id="marginalia-title-text" class='map-title' x='{$x}' y='{$y}'><xsl:value-of select="$title"/></text>
        </g>
    </xsl:template>

</xsl:stylesheet>
