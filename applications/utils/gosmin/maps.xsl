<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:html="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="html"
>
 
    <xsl:output
        method="html"
		indent="yes"
        doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"
        doctype-public="-//W3C//DTD XHTML 1.1//EN"
    />

<xsl:template match="map">
<tr>

<!-- picture column-->
<td rowspan="2" valign="top">
<xsl:if test="picture/@url != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<a>
<xsl:attribute name="href"><xsl:value-of select="picture/@url"/></xsl:attribute>
<xsl:attribute name="target">_blank</xsl:attribute>
<img height="60" border="0">
<xsl:attribute name="src"><xsl:value-of select="picture/@url"/></xsl:attribute></img>
</a>
</td>

<th>
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
en
</th>

<!-- boundary column-->
<td valign="top">
<xsl:if test="@boundary != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<xsl:value-of select="@boundary"/>
</td>
<!-- name column-->
<td valign="top">
<xsl:if test="@name != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<xsl:value-of select="@name"/>
</td>
<!-- comment column-->
<td valign="top">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
<xsl:value-of select="@comment"/>
</td>
<!-- updated column-->
<td valign="top">
<xsl:if test="@updated != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<xsl:value-of select="@updated"/>
</td>

<!-- download column-->
<td rowspan="2">
<xsl:if test="download/@url != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
<a>
<xsl:attribute name="href"><xsl:value-of select="download/@url"/></xsl:attribute>
<xsl:attribute name="target">_blank</xsl:attribute>
URL</a>
</xsl:if>
</td>
<td align="right" rowspan="2">
<xsl:if test="download/@sizeMB != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<xsl:value-of select="download/@sizeMB"/> MB
</td>

<!-- file column-->
<td rowspan="2">
<xsl:if test="file/@name != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<xsl:value-of select="file/@name"/>
</td>
<td align="right" rowspan="2">
<xsl:if test="file/@sizeMB != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<xsl:value-of select="file/@sizeMB"/> MB
</td>

<!-- routing column-->
<td rowspan="2">
<xsl:if test="file/@name != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<xsl:value-of select="attributes/@routing"/>
</td>
    
<!-- details column-->
<td rowspan="2">
<xsl:if test="details/@url != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
<a>
<xsl:attribute name="href"><xsl:value-of select="details/@url"/></xsl:attribute>
<xsl:attribute name="target">_blank</xsl:attribute>
URL</a>
</xsl:if>
</td>

</tr>

<tr>
<th>
<xsl:attribute name="bgcolor">#80ff80</xsl:attribute>
de
</th>
<!-- boundary column-->
<td valign="top">
<xsl:if test="lang/@boundary != ''">
<xsl:attribute name="bgcolor">#80ff80</xsl:attribute>
</xsl:if>
<xsl:value-of select="lang/@boundary"/>
</td>
<!-- name column-->
<td valign="top">
<xsl:if test="lang/@name != ''">
<xsl:attribute name="bgcolor">#80ff80</xsl:attribute>
</xsl:if>
<xsl:value-of select="lang/@name"/>
</td>
<!-- comment column-->
<td valign="top">
<xsl:attribute name="bgcolor">#80ff80</xsl:attribute>
<xsl:value-of select="lang/@comment"/>
</td>
<!-- updated column-->
<td valign="top">
<xsl:if test="lang/@updated != ''">
<xsl:attribute name="bgcolor">#80ff80</xsl:attribute>
</xsl:if>
<xsl:value-of select="lang/@updated"/>
</td>

</tr>


</xsl:template>
    
<xsl:template match="maps">
<!-- DO NOT EDIT, automatically generated data -->
<html>
<title>List of Maps</title>
<body>

<h1>List of Maps</h1>

<!-- dir currently not required -->
<xsl:for-each select="device/@name">
<xsl:if test=". = 'Colorado 300'"><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'Edge 205'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'eTrex'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'eTrex Summit'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'eTrex Legend'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'eTrex Venture Cx'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'eTrex Vista'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'Forerunner 101'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'Geko 101'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'GPS 12XL'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'GPSMAP 60Cx'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'Nüvi 200'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'Oregon 200'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'Quest 1'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<xsl:if test=". = 'Zumo 400'"><br/><xsl:value-of select="../@series"/>: </xsl:if>
<a>
<xsl:attribute name="href">#<xsl:value-of select="."/></xsl:attribute>
<xsl:value-of select="."/></a>
<xsl:text> </xsl:text>
</xsl:for-each>
<br/>
<br/>

<table border="1">
<tr>
<th>Picture</th>
<th>lang</th>
<th>Boundary</th>
<th>Name</th>
<th>Comment</th>
<th>Updated</th>
<th colspan="2">Download</th>
<th colspan="2">File</th>
<th>Routing</th>
<th>Details</th>
</tr>

<xsl:apply-templates/>

</table>

</body>

</html>
</xsl:template>

 
</xsl:stylesheet>