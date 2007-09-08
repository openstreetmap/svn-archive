<?xml version='1.0' encoding='UTF-8' ?>
<xsl:stylesheet 
    version="1.0"
    xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xslout="X">

    <xsl:namespace-alias stylesheet-prefix="xslout" result-prefix="xsl"/>

    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <xsl:template match="maplint:tests">
        <xslout:stylesheet version="1.0"
            xmlns:maplint="http://maplint.openstreetmap.org/xml/1.0">

            <xsl:for-each select="/maplint:tests/maplint:test/maplint:setup[@type='application/xsl+xml']">
                <xsl:copy-of select="*"/>
            </xsl:for-each>

            <xslout:template name="all-tests">
                <xsl:for-each select="/maplint:tests/maplint:test">
                    <maplint:test agent="xsltests">
                        <xsl:copy-of select="@*"/>
                    </maplint:test>
                </xsl:for-each>
            </xslout:template>

            <xslout:template name="call-tests-any">
                <xsl:apply-templates select="maplint:test" mode="call">
                    <xsl:with-param name="data" select="'any'"/>
                </xsl:apply-templates>
            </xslout:template>
            <xslout:template name="call-tests-node">
                <xsl:apply-templates select="maplint:test" mode="call">
                    <xsl:with-param name="data" select="'node'"/>
                </xsl:apply-templates>
            </xslout:template>
            <xslout:template name="call-tests-segment">
                <xsl:apply-templates select="maplint:test" mode="call">
                    <xsl:with-param name="data" select="'segment'"/>
                </xsl:apply-templates>
            </xslout:template>
            <xslout:template name="call-tests-way">
                <xsl:apply-templates select="maplint:test" mode="call">
                    <xsl:with-param name="data" select="'way'"/>
                </xsl:apply-templates>
            </xslout:template>
            <xslout:template name="call-tests-relation">
                <xsl:apply-templates select="maplint:test" mode="call">
                    <xsl:with-param name="data" select="'relation'"/>
                </xsl:apply-templates>
            </xslout:template>

            <xsl:apply-templates select="maplint:test" mode="templates">
                <xsl:with-param name="data" select="'any'"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="maplint:test" mode="templates">
                <xsl:with-param name="data" select="'node'"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="maplint:test" mode="templates">
                <xsl:with-param name="data" select="'segment'"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="maplint:test" mode="templates">
                <xsl:with-param name="data" select="'way'"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="maplint:test" mode="templates">
                <xsl:with-param name="data" select="'relation'"/>
            </xsl:apply-templates>
        </xslout:stylesheet>

    </xsl:template>

    <xsl:template match="maplint:test" mode="call">
        <xsl:param name="data"/>

        <xsl:for-each select="maplint:check[(@data=$data) and (@type='application/xsl+xml')]">
            <xslout:call-template name="test-{../@group}-{../@id}-{$data}"/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="maplint:test" mode="templates">
        <xsl:param name="data"/>

        <xsl:for-each select="maplint:check[(@data=$data) and (@type='application/xsl+xml')]">
            <xslout:template name="test-{../@group}-{../@id}-{$data}">
                <xsl:apply-templates select="node()" mode="copy"/>
            </xslout:template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="maplint:result" mode="copy">
        <maplint:result ref="{ancestor::maplint:test/@id}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="node()" mode="copy"/>
        </maplint:result>
    </xsl:template>

    <xsl:template match="*" mode="copy">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="node()" mode="copy"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
