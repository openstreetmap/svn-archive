<?xml version='1.0' encoding='UTF-8' ?>
<xsl:stylesheet 
    version="1.0"
    xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://www.topografix.com/GPX/1/0"
    xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-result-prefixes="maplint">

	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" encoding="UTF-8"/>

    <xsl:key name="maplint" match="/osm/maplint:test" use="@id"/>
    <xsl:key name='nodeById' match='/osm/node' use='@id'/>
    <xsl:key name='segmentById' match='/osm/segment' use='@id'/>

    <xsl:variable name="tests" select="document('../tests.xml')/maplint:tests/maplint:test"/>

    <xsl:template match="osm">
        <gpx version="1.0" creator="Maplint convert-to-gpx">
            <xsl:apply-templates select="node[tag/@k='todo']" mode="todo"/>
            <xsl:apply-templates select="node[maplint:result]" mode="maplint"/>
            <xsl:apply-templates select="segment[maplint:result]"/>
            <xsl:apply-templates select="way[maplint:result]"/>
        </gpx>
    </xsl:template>

    <xsl:template match="node" mode="todo">
        <wpt lat="{@lat}" lon="{@lon}">
            <name><xsl:text>TODO</xsl:text></name>
            <desc><xsl:value-of select="concat(@id, ' ', tag/@v)"/></desc>
            <sym>Mine</sym>
        </wpt>
    </xsl:template>

    <xsl:template name="waypoint">
        <xsl:param name="element"/>
        <xsl:param name="node"/>
        <xsl:param name="type"/>

        <xsl:variable name="result" select="$element/maplint:result/@ref"/>
        <xsl:variable name="garmin" select="$tests[@id=$result]/maplint:garmin"/>
        <xsl:variable name="severity" select="key('maplint', $result)/@severity"/>

        <xsl:if test="$garmin">
            <wpt lat="{$node/@lat}" lon="{$node/@lon}">
                <name><xsl:value-of select="$garmin/@short"/></name>
                <desc>
                    <xsl:value-of select="$type"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="@id"/>
                    <xsl:text>: </xsl:text>
                    <xsl:value-of select="$element/maplint:result/text()"/>
                </desc>
                <sym><xsl:value-of select="$garmin/@icon"/></sym>
            </wpt>
        </xsl:if>
    </xsl:template>

    <xsl:template match="node" mode="maplint">
        <xsl:call-template name="waypoint">
            <xsl:with-param name="element" select="."/>
            <xsl:with-param name="node" select="."/>
            <xsl:with-param name="type" select="'Node'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="segment">
        <xsl:call-template name="waypoint">
            <xsl:with-param name="element" select="."/>
            <xsl:with-param name="node" select="key('nodeById', @from)"/>
            <xsl:with-param name="type" select="'Segment'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="way">
        <xsl:variable name="segment" select="key('segmentById', seg/@id)"/>

        <xsl:call-template name="waypoint">
            <xsl:with-param name="element" select="."/>
            <xsl:with-param name="node" select="key('nodeById', $segment/@from)"/>
            <xsl:with-param name="type" select="'Way'"/>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>
