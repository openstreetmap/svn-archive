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

<xsl:template match="device">
<tr>
<!-- series column-->
<td valign="top">
<xsl:if test="@series != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<a><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute></a>
<a><xsl:attribute name="series"><xsl:value-of select="@series"/></xsl:attribute></a>
<b><xsl:value-of select="@series"/></b>
<!--Brand: <xsl:value-of select="@brand"/><br/>-->
</td>
<!-- device column-->
<td>
<xsl:if test="@name != '' and @brand != '' and @series != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<b><xsl:value-of select="@name"/></b><br/>
<!--Brand: <xsl:value-of select="@brand"/><br/>-->
<a><xsl:attribute name="href"><xsl:value-of select="help/@url"/></xsl:attribute>Wiki Page</a>
</td>
<!-- image column-->
<td>
<xsl:if test="picture/@url != '' and picture/@identical = 'yes'">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<xsl:if test="picture/@url != ''">
<a>
<xsl:attribute name="href">images/devices/<xsl:value-of select="picture/@url"/></xsl:attribute>
<xsl:attribute name="target">_blank</xsl:attribute>
<img border="0" width="60" height="60">
<xsl:attribute name="src">images/devices/<xsl:value-of select="picture/@url"/></xsl:attribute>
</img></a>
</xsl:if>
<xsl:if test="picture/@identical = 'similiar'">
<br/>"Image similiar"
</xsl:if>
</td>
<!-- connection1 column-->
<td>
<xsl:if test="(connection/@type = 'USB' and connection/@version != '') or (connection/@type = 'Serial') or (connection/@type = 'No')">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<xsl:choose>
<xsl:when test="connection/@type = 'USB'">
<img src="Images/Usb.png"/><br/>
<xsl:value-of select="connection/@version"/>
</xsl:when>
<xsl:when test="connection/@type = 'Serial'">
<img src="Images/SerialCOM.jpg"/>
</xsl:when>
<xsl:when test="connection/@type = 'No'">
No
</xsl:when>
<xsl:otherwise>
?
</xsl:otherwise>
</xsl:choose>
</td>
<!-- connection2 column-->
<td>
<xsl:if test="connection/@massstoragemode != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
MassStorageMode: <xsl:value-of select="connection/@massstoragemode"/>
</td>
<!-- files column-->
<td>
<xsl:if test="display/@typfile != '' and connection/@imgfiles != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
Typfile: <xsl:value-of select="display/@typfile"/>
<br/>
ImgFiles: <xsl:value-of select="connection/@imgfiles"/>
</td>
<!-- map column-->
<td>
<xsl:if test="display/@map != '' and display/@colors != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
Display: <xsl:value-of select="display/@map"/><br/>
Colors: <xsl:value-of select="display/@colors"/>
</td>
<!-- memory column-->
<td>
<xsl:if test="memory/internal/@size != '' and memory/internal/@unit != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
Internal: <xsl:value-of select="memory/internal/@size"/> <xsl:value-of select="memory/internal/@unit"/>
</td>
<!-- card slot column-->
<td>
<xsl:if test="memory/slot/@type != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<xsl:choose>
<xsl:when test="memory/slot/@type = 'SD'">
<img width="30" height="40" src="Images/SecureDigital.png"/> SD
</xsl:when>
<xsl:when test="memory/slot/@type = 'microSD'">
<img width="18" height="25" src="Images/SecureDigitalMicro.png"/><br/>microSD
</xsl:when>
<xsl:when test="memory/slot/@type = 'No'">
No
</xsl:when>
<xsl:otherwise>
?
</xsl:otherwise>
</xsl:choose>
</td>
<!-- card slot column-->
<td>
<xsl:if test="memory/slot/@type = 'No' or (memory/slot/card/@type != '' and memory/slot/card[@type='SD']/@maxsize != '' and memory/slot/card[@type='SDHC']/@maxsize != '' and memory/slot/card/@unit != '')">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
<xsl:for-each select="memory/slot/card">
<xsl:value-of select="@type"/> Max: <xsl:value-of select="@maxsize"/> <xsl:value-of select="@unit"/>
<br/>
</xsl:for-each>
</td>
<!-- firmware column-->
<td>
<xsl:if test="firmware/@version != ''">
<xsl:attribute name="bgcolor">#00ff00</xsl:attribute>
</xsl:if>
Version: <xsl:value-of select="firmware/@version"/>
</td>
</tr>
</xsl:template>
    
<xsl:template match="devices">
<!-- DO NOT EDIT, automatically generated data -->
<html>
<title>List of Devices</title>
<body>

<h1>List of Devices</h1>

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
<th>Series</th>
<th>Device</th>
<th>Picture</th>
<th colspan="2">Connection</th>
<th>Files</th>
<th>Map</th>
<th>Memory</th>
<th colspan="2">Card Slot</th>
<th>Firmware</th>
</tr>

<xsl:apply-templates/>

</table>

</body>

</html>
</xsl:template>

 
</xsl:stylesheet>