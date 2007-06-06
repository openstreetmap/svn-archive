<?xml version='1.0' encoding='UTF-8' ?>
<xsl:stylesheet 
    version="1.0"
    xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-result-prefixes="maplint">

    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <xsl:param name="test"/>

    <xsl:template match="/maplint:tests">
        <html>
            <xsl:comment>This file was created automatically. Don't change.</xsl:comment>
            <head>
                <title>Maplint Test: <xsl:value-of select="$test"/></title>
                <link rel="stylesheet" type="text/css" href="style.css" />
            </head>
            <body>

                <xsl:apply-templates select="maplint:test">
                    <xsl:sort select="@group"/>
                    <xsl:sort select="@id"/>
                </xsl:apply-templates>

            </body>
        </html>
    </xsl:template>

    <xsl:template match="maplint:test">
        <xsl:if test="@id = $test">

            <div class="nav">
                <xsl:if test="position() != 1">
                    <a href="{preceding-sibling::maplint:test[1]/@id}.html">prev</a>
                </xsl:if>

                | <a href="index.html">list</a> |

                <xsl:if test="position() != last()">
                    <a href="{following-sibling::maplint:test[1]/@id}.html">next</a>
                </xsl:if>
            </div>

            <table class="test">
                <tr>
                    <th width="52%">Test ID</th>
                    <th width="16%">Group</th>
                    <th width="16%">Version</th>
                    <th width="16%">Severity</th>
                </tr>
                <tr>
                    <td width="52%"><xsl:value-of select="@id"/></td>
                    <td width="16%"><xsl:value-of select="@group"/></td>
                    <td width="16%"><xsl:value-of select="@version"/></td>
                    <td width="16%"><span class="{@severity}">&#160;&#160;&#160;</span>&#160;<xsl:value-of select="@severity"/></td>
                </tr>
            </table>

            <table class="description">
                <xsl:apply-templates select="maplint:desc">
                    <xsl:sort select="@xml:lang"/>
                </xsl:apply-templates>
            </table>

            <xsl:if test="maplint:garmin">
                <table class="description">
                    <tr>
                        <th>Garmin:</th>
                        <td>
                            <xsl:value-of select="maplint:garmin/@short"/>
                            <xsl:text> / </xsl:text>
                            <xsl:value-of select="maplint:garmin/@icon"/>
                        </td>
                    </tr>
                </table>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template match="maplint:desc">
        <tr>
            <th><xsl:value-of select="@xml:lang"/></th>
            <td><xsl:value-of select="normalize-space(text())"/></td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
