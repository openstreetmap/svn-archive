<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

    <xsl:template match="text()"/>

    <xsl:template match="osm"><osm><xsl:apply-templates select="way|segment|node"/></osm></xsl:template>

    <xsl:key name="node" match="/osm/node" use="@id"/>
    <xsl:key name="segment" match="/osm/segment" use="@id"/>

    <xsl:template match="segment|node">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template name="splitway">
        <xsl:param name="way"/>

        <xsl:for-each select="seg">
            <way id="{../@id}">
                <seg id="{@id}"/>
                <xsl:for-each select="../tag">
                    <xsl:copy-of select="."/>
                </xsl:for-each>
            </way>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="way">
        <xsl:variable name="splitifnotempty">
            <xsl:for-each select="seg">
                <xsl:if test="position() != last()">
                    <xsl:variable name="thissegment" select="key('segment',@id)"/>
                    <xsl:variable name="next" select="position()+1"/>
                    <xsl:variable name="nextsegment" select="key('segment',../seg[$next]/@id)"/>
                    <xsl:variable name="tolon" select="key('node',$thissegment/@to)/@lon"/>
                    <xsl:variable name="tolat" select="key('node',$thissegment/@to)/@lat"/>
                    <xsl:variable name="fromlon" select="key('node',$nextsegment/@from)/@lon"/>
                    <xsl:variable name="fromlat" select="key('node',$nextsegment/@from)/@lat"/>
                    <xsl:if test="$tolon != $fromlon or $tolat != $fromlat">
                        <xsl:value-of select="../@id"/>fail
                    </xsl:if>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:choose> 
            <xsl:when test="$splitifnotempty=''">
                <xsl:copy-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="splitway">
                    <xsl:with-param name="way" select="."/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
