<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:output method="xml" version="1.0" indent="yes"/>

  <xsl:param name="title">Map Features</xsl:param>
  <xsl:param name="page-width">210mm</xsl:param>
  <xsl:param name="page-height">297mm</xsl:param>

  <xsl:template match="mapfeatures">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="main" margin="12mm"
          page-width="{$page-width}" page-height="{$page-height}"
          reference-orientation="90">
          <fo:region-body column-count="4" column-gap="8mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="main">
        <fo:flow flow-name="xsl-region-body" font-size="8pt" line-height="10pt">
          <fo:block font-size="12pt" font-weight="bold" line-height="15pt"
            space-after="6pt" text-align="center">
            <xsl:value-of select="$title"/>
          </fo:block>
          <xsl:apply-templates select="section"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <xsl:template match="section">
    <fo:block font-size="10pt" font-weight="bold"
      line-height="13pt" space-after="1pt">
      <xsl:value-of select="substring-after(@name, 'Map_Features:')"/>
    </fo:block>
    <fo:table table-layout="fixed" width="62mm"
      border-top-style="solid" border-bottom-style="solid"
      space-after="6pt">
      <fo:table-column column-width="22mm"/>
      <fo:table-column column-width="40mm"/>
      <fo:table-body>
        <xsl:apply-templates select="feature"/>
      </fo:table-body>
    </fo:table>
  </xsl:template>

  <xsl:template match="feature">
    <fo:table-row>
      <fo:table-cell>
        <fo:block font-weight="bold">
          <xsl:value-of select="@key"/>
        </fo:block>
      </fo:table-cell>
      <fo:table-cell>
        <fo:block>
          <xsl:call-template name="value">
            <xsl:with-param name="name" select="value/@name"/>
          </xsl:call-template>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>

  <xsl:template name="value">
    <xsl:param name="name"/>
    <xsl:if test="($name = 'User defined')
      or ($name = 'Other Values')
      or ($name = 'Date')
      or ($name = 'URI')
      or ($name = 'Numeric value')
      or starts-with($name, 'List:')
      or starts-with($name, 'Range:')">
      <xsl:attribute name="font-style">italic</xsl:attribute>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="starts-with($name, 'List:')">
      </xsl:when>
    </xsl:choose>
    <xsl:value-of select="$name"/>
  </xsl:template>

</xsl:stylesheet>
