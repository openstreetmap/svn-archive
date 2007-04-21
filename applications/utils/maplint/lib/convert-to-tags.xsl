<?xml version='1.0' encoding='UTF-8' ?>
<xsl:stylesheet 
    version="1.0"
    xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="UTF-8"/>

    <xsl:key name="maplint" match="/osm/maplint:test" use="@id"/>

    <xsl:template match="osm">
        <osm>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*"/>
        </osm>
    </xsl:template>

    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="maplint:result">
        <tag k="maplint:{key('maplint', @ref)/@severity}" v="{@ref}"/>
    </xsl:template>

</xsl:stylesheet>
