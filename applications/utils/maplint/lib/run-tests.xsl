<?xml version='1.0' encoding='UTF-8' ?>
<xsl:stylesheet 
    version="1.0"
    xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xslout="http://www.w3.org/1999/XSL/Transform">

    <xsl:import href="../tests.xsl"/>

	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="UTF-8"/>

    <xsl:template match="osm">
        <osm>
            <xsl:copy-of select="@*"/>
            <xsl:call-template name="all-tests"/>
            <xsl:apply-templates select="*"/>
        </osm>
    </xsl:template>

    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="*"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="node">
        <node>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="*"/>
            <xsl:call-template name="call-tests-node"/>
            <xsl:call-template name="call-tests-any"/>
        </node>
    </xsl:template>

    <xsl:template match="segment">
        <segment>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="*"/>
            <xsl:call-template name="call-tests-segment"/>
            <xsl:call-template name="call-tests-any"/>
        </segment>
    </xsl:template>

    <xsl:template match="way">
        <way>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="*"/>
            <xsl:call-template name="call-tests-way"/>
            <xsl:call-template name="call-tests-any"/>
        </way>
    </xsl:template>

    <xsl:template match="relation">
        <relation>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="*"/>
            <xsl:call-template name="call-tests-relation"/>
            <xsl:call-template name="call-tests-any"/>
        </relation>
    </xsl:template>

    <xsl:template name="report-result">
        <xsl:param name="text"/>
        <maplint:result test="{$test/@id}" type="{$test/@type}">
            <xsl:value-of select="$text"/>
        </maplint:result>
    </xsl:template>

</xsl:stylesheet>
