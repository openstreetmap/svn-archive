<?xml version='1.0' encoding='UTF-8' ?>

<!-- This file is imported into osmarender.xsl -->

<!-- Misc utility templates -->

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- Copy class attribute  -->
    <xsl:template match='@class' mode='copyAttributes'>
        <xsl:param name="classes"/>
        <xsl:attribute name="class">
            <xsl:value-of select="normalize-space(concat($classes,' ',.))"/>
        </xsl:attribute>
    </xsl:template>

    <!-- Some attribute shouldn't be copied -->
    <xsl:template match='@type|@ref|@scale' mode='copyAttributes'>
    </xsl:template>

    <!-- Copy all attributes  -->
    <xsl:template match='@*' mode='copyAttributes'>
        <xsl:param name="classes"/>
        <xsl:copy/>
    </xsl:template>

<!--
    <xsl:template name='tags'>
        <xsl:text>"</xsl:text>
            <xsl:text>Segment Id = </xsl:text>
            <xsl:value-of select='@id'/>
            <xsl:text>\n</xsl:text>
            <xsl:text>From node = </xsl:text>
            <xsl:value-of select='@from'/>
            <xsl:text>\n</xsl:text>
            <xsl:text>To node = </xsl:text>
            <xsl:value-of select='@to'/>
            <xsl:text>\n</xsl:text>
            <xsl:for-each select='tag'>
                <xsl:value-of select='@k'/>
                <xsl:text>=</xsl:text>
                <xsl:value-of select='@v'/>
                <xsl:text>\n</xsl:text>
            </xsl:for-each>
        <xsl:text>"</xsl:text>
    </xsl:template>
-->

</xsl:stylesheet>
