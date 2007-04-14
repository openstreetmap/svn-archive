<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Draw SVG layers -->

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match='layer'>
        <xsl:param name='elements' />
        <xsl:param name='layer' />
        <xsl:param name='rule' />
        <xsl:param name='classes' />

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
            <xsl:apply-templates select='*'>
                <xsl:with-param name='layer' select='$layer' />
                <xsl:with-param name='elements' select='$elements' />
                <xsl:with-param name='classes' select='$classes' />
            </xsl:apply-templates>
        </g>

    </xsl:template>

</xsl:stylesheet>
