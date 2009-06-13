<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:output method="xml" version="1.0" indent="yes"/>

  <xsl:param name="document-title">OpenStreetMap: Map Features</xsl:param>
  <!-- A4 paper -->
  <xsl:param name="page-width">210mm</xsl:param>
  <xsl:param name="page-height">297mm</xsl:param>
  <!-- Letter paper -->
  <!--
  <xsl:param name="page-width">8.5in</xsl:param>
  <xsl:param name="page-height">11in</xsl:param>
  -->

  <!-- Meunchian method is used to group by key name.  -->
  <xsl:key name="features-by-key" match="feature" use="concat(../@name, '::', @key)"/>

  <!--
  For now access restrictions for transport modes are hard‐coded here.
  Keys in this list reference the ‘access’ key for possible values.
  -->
  <xsl:key name="access-restrictions" match="feature[../@name = 'Map_Features:restrictions'
    and (@key = 'bicycle'
    or @key = 'foot'
    or @key = 'goods'
    or @key = 'hgv'
    or @key = 'agricultural'
    or @key = 'horse'
    or @key = 'motorcycle'
    or @key = 'motorcar'
    or @key = 'psv'
    or @key = 'motorboat'
    or @key = 'boat')]"
    use="concat(../@name, '::', @key)"/>

  <!-- Define the page layout at the document root. -->
  <xsl:template match="mapfeatures">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="main" margin="12mm"
          page-width="{$page-width}" page-height="{$page-height}"
          reference-orientation="90">
          <fo:region-body column-count="4" column-gap="5mm"/>
          <fo:region-after/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="main">
        <fo:flow flow-name="xsl-region-body" font-size="8pt" line-height="10pt">
          <xsl:call-template name="title-heading">
            <xsl:with-param name="text" select="$document-title"/>
          </xsl:call-template>
          <xsl:apply-templates/>
          <fo:block font-size="6pt" line-height="8pt">
            Generated from OpenStreetMap Map Features.
            Available under the Creative Commons Attribution-ShareAlike 2.0 licence.
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <xsl:template match="section">
    <xsl:call-template name="section-heading">
      <xsl:with-param name="text" select="substring-after(@name, 'Map_Features:')"/>
    </xsl:call-template>
    <fo:table table-layout="fixed" width="64mm"
      border-top-style="solid" border-bottom-style="solid"
      space-after="6pt">
      <fo:table-column column-width="22mm"/>
      <fo:table-column column-width="42mm"/>
      <fo:table-body>
        <xsl:apply-templates select="feature[generate-id() = generate-id(key('features-by-key', concat(../@name, '::', @key)))]"/>
      </fo:table-body>
    </fo:table>
  </xsl:template>

  <xsl:template match="feature">
    <xsl:variable name="currentvalues" select="key('features-by-key', concat(../@name, '::', @key))/value"/>
    <xsl:choose>
      <xsl:when test="key('access-restrictions', concat(../@name, '::', @key))">
        <fo:table-row>
          <xsl:if test="position() != 1">
            <xsl:attribute name="border-top-style">dotted</xsl:attribute>
          </xsl:if>
          <fo:table-cell>
            <xsl:apply-templates select="@key"/>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
              <xsl:attribute name="font-style">italic</xsl:attribute>
              <xsl:text>See: </xsl:text>
              <fo:inline font-weight="bold">access</fo:inline>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
      </xsl:when>
      <xsl:otherwise>
        <!-- Not an access restriction -->
        <fo:table-row>
          <xsl:if test="position() != 1">
            <xsl:attribute name="border-top-style">dotted</xsl:attribute>
          </xsl:if>
          <fo:table-cell>
            <xsl:if test="count($currentvalues[@name != 'User defined' and @name != 'Other Values']) &gt; 1">
              <xsl:attribute name="number-rows-spanned">
                <xsl:value-of select="count($currentvalues[@name != 'User defined' and @name != 'Other Values'])"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="@key"/>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block>
              <xsl:call-template name="value">
                <xsl:with-param name="name" select="$currentvalues[1]/@name"/>
              </xsl:call-template>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <xsl:apply-templates select="$currentvalues"/>
      </xsl:otherwise>
    </xsl:choose>
    </xsl:template>

  <xsl:template match="@key">
    <fo:block font-weight="bold">
      <xsl:value-of select="."/>
    </fo:block>
  </xsl:template>

  <xsl:template match="value">
    <xsl:if test="position() != 1">
      <fo:table-row>
        <xsl:attribute name="keep-with-previous">
          <xsl:choose>
            <xsl:when test="position() = last()">10</xsl:when>
            <xsl:otherwise>5</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <fo:table-cell>
          <fo:block>
            <xsl:call-template name="value">
              <xsl:with-param name="name" select="@name"/>
            </xsl:call-template>
          </fo:block>
        </fo:table-cell>
      </fo:table-row>
    </xsl:if>
  </xsl:template>

  <xsl:template name="title-heading">
    <xsl:param name="text"/>
    <fo:block font-size="11pt" font-weight="bold" line-height="14pt"
      space-after="4pt" text-align="center">
      <xsl:value-of select="$text"/>
    </fo:block>
  </xsl:template>

  <xsl:template name="section-heading">
    <xsl:param name="text"/>
    <fo:block font-size="9pt" font-weight="bold" line-height="12pt" keep-with-next="20">
      <xsl:value-of select="$text"/>
    </fo:block>
  </xsl:template>

  <xsl:template name="value">
    <xsl:param name="name"/>
    <xsl:variable name="weekdays">monday, mon, tuesday, tue, wednesday, wed, thursday, thu, friday, fri, saturday, sat, sunday, sun</xsl:variable>
    <xsl:if test="($name = 'User defined')
      or ($name = 'Date')
      or ($name = 'URI')
      or ($name = 'Numeric value')
      or ($name = 'Other Values')
      or starts-with($name, 'List:')
      or starts-with($name, 'Range:')">
      <xsl:attribute name="font-style">italic</xsl:attribute>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="starts-with($name, 'List:')
        and normalize-space(substring-after($name, 'List: ')) = normalize-space($weekdays)">
        <xsl:text>List: day of week (e.g. monday, mon)</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$name"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
