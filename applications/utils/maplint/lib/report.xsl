<?xml version='1.0' encoding='UTF-8' ?>
<xsl:stylesheet 
    version="1.0"
    xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" encoding="iso-8859-1"/>

    <!-- new line -->
    <xsl:variable name="nl">
        <xsl:text>
</xsl:text>
    </xsl:variable>

    <xsl:template match="/osm">
        <xsl:text>The following tests were run:

</xsl:text>
        <xsl:apply-templates select="maplint:test">
            <xsl:sort select="@id"/>
        </xsl:apply-templates>
        <xsl:text>

The following results were found:

</xsl:text>
        <xsl:apply-templates select="/osm/*/maplint:result"/>
    </xsl:template>

    <xsl:template match="/osm/maplint:test">
        <xsl:variable name="tid" select="@id"/>
        <xsl:value-of select="concat(@id, ' (group=', @group, ', version=', @version, ', agent=', @agent, ') ', @severity, ': ', count(/osm/*/maplint:result[@ref=$tid]), $nl)"/>
    </xsl:template>

    <xsl:template match="/osm/node/maplint:result">
        <xsl:value-of select="concat(@ref, ': node=',../@id,', lat=', ../@lat, ', lon=',../@lon, $nl, '  ', text(), $nl)"/>
    </xsl:template>

    <xsl:template match="/osm/segment/maplint:result">
        <xsl:value-of select="concat(@ref, ' ', @test, ': segment=', ../@id, ', from=', ../@from, ', to=',../@to, $nl, '  ', text(), $nl)"/>
    </xsl:template>

    <xsl:template match="/osm/way/maplint:result">
        <xsl:variable name="name">
            <xsl:if test="../tag[@k='name']/@v">
                <xsl:text>, name=</xsl:text>
                <xsl:value-of select="../tag[@k='name']/@v"/>
            </xsl:if>
        </xsl:variable>
        <xsl:value-of select="concat(@ref, ' ', @test, ': way=', ../@id, $name, $nl, '  ', text(), $nl)"/>
    </xsl:template>

    <xsl:template match="/osm/relation/maplint:result">
        <xsl:value-of select="concat(@ref, ': relation=', ../@id, ' ', text(), $nl)"/>
    </xsl:template>

</xsl:stylesheet> 
