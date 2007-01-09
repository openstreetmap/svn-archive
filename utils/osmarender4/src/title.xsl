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

        <svg id="meta-title" inkscape:groupmode="layer" inkscape:label="Title">
			<rect id="meta-title-background" x='0px' y='0px' height='{$metaTopHeight - 5}px' width='{$documentWidth}px' class='map-title-background'/>
		    <text id="meta-title-text" class='map-title' x='{$x}' y='{$y}'><xsl:value-of select="$title"/></text>
        </svg>
	</xsl:template>

</xsl:stylesheet>
