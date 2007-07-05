<?xml version='1.0' encoding='UTF-8' ?>
<xsl:stylesheet 
    version="1.0"
    xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-result-prefixes="maplint">

    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <xsl:template match="/">
        <html>
            <xsl:comment>This file was created automatically. Don't change.</xsl:comment>
            <head>
                <title>Maplint Test Index</title>
                <link rel="stylesheet" type="text/css" href="style.css" />
            </head>
            <body>
                <h1>Maplint tests</h1>

                <p>This is a list of tests known to the Maplint program.
                Maplint checks
                <a href="http://www.openstreetmap.org/">OpenStreetMap</a>
                data for inconsistencies and other problems.
                See the
                <a href="http://wiki.openstreetmap.org/index.php/Maplint">Maplint
                wiki page</a> for more information.</p>

                <xsl:apply-templates select="maplint:tests"/>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="maplint:tests">
        <table class="testlist">
            <tr>
                <th>Group</th>
                <th>Test ID</th>
                <th>Version</th>
                <th>Severity</th>
            </tr>
            <xsl:apply-templates select="maplint:test">
                <xsl:sort select="@group"/>
                <xsl:sort select="@id"/>
            </xsl:apply-templates>
        </table>
    </xsl:template>

    <xsl:template match="maplint:test">
        <xsl:variable name="groupclass">
            <xsl:if test="@group != following-sibling::maplint:test[1]/@group">
                <xsl:text>newgroup</xsl:text>
            </xsl:if>
        </xsl:variable>
        <tr>
            <td class="{$groupclass}"><xsl:value-of select="@group"/></td>
            <td class="{$groupclass}"><a href="{@id}.html"><xsl:value-of select="@id"/></a></td>
            <td class="{$groupclass}"><xsl:value-of select="@version"/></td>
            <td class="{$groupclass}"><span class="{@severity}">&#160;&#160;&#160;</span>&#160;<xsl:value-of select="@severity"/></td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
