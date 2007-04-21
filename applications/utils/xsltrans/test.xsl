<?xml version='1.0' encoding='UTF-8' ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" />

    <xsl:param name="p1"/>
    <xsl:param name="p2"/>

    <xsl:template match="/">
        <xsl:text>p1=</xsl:text>
        <xsl:value-of select="$p1"/>
        <xsl:text>
p2=</xsl:text>
        <xsl:value-of select="$p2"/>
        <xsl:text>
</xsl:text>
    </xsl:template>

</xsl:stylesheet>
